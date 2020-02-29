package computer.benjamin.zotdr0id.task;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.task.callback.WebDavUp;

/**
 * Created by oni on 01/12/2017.
 * This task uploads a file to Zotero or External webdav once we have requested access and gained
 * the token we need, using PushAttachementReq class
 */

public class PushWebDav extends ZoteroTask {

    private WebDavUp _callback;
    private Attachment _changed_attachment;
    private String _url;
    private String _username;
    private String _password;
    private String _mod_time;
    private String _new_md5;

    public PushWebDav(WebDavUp callback, String url, String username, String password,
                      String new_md5, String mod_time, Attachment changed_attachment) {
        _callback = callback;
        _url = url;
        _changed_attachment = changed_attachment;
        _username = username;
        _password = password;
        _new_md5 = new_md5;
        _mod_time = mod_time;
    }

    public void startZoteroTask() {
        // Setup the URL, read in the new file to memory, and then concat with _prefix and _suffix.
        // At this point, we should be ready to upload the lot to Zotero.
        execute(_url);
    }

    /**
     * Do the actual upload. We need to do four things :
     * 1) Upload the changed file.
     * 2) Download the existing .prop file.
     * 3) Alter the prop file with the new data.
     * 4) upload the new prop file.
     * @param address
     * @return
     */
    protected String doInBackground(String... address) {
        String zip_name = _changed_attachment.get_zotero_key() +".zip";
        String prop_name  = _changed_attachment.get_zotero_key() +".prop";
        Sardine sardine = SardineFactory.begin(_username, _password);

        // Zip up the file in the temp dir
        String zip_path = FileManager.zipFile(_changed_attachment.get_file_name(), zip_name);
        try {
            InputStream fis = new FileInputStream(new File(zip_path));
            sardine.put(_url + "/" + zip_name, fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FAIL";
        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }

        // Now grab the prop file
        try {
            InputStream is = sardine.get(_url + "/" + prop_name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String str = "";
            StringBuffer buf = new StringBuffer();
            if (is != null) {
                while ((str = reader.readLine()) != null) {
                    buf.append(str + "\n" );
                }
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }

        // Write a new prop file
        String xmlstr = "<properties version=\"1\">"
                + "<mtime>" + _mod_time + "</mtime>"
                + "<hash>" + _new_md5 + "</hash>"
                + "</properties>";

        try {
            InputStream fis = new ByteArrayInputStream(xmlstr.getBytes(StandardCharsets.UTF_8));
            sardine.put(_url + "/" + prop_name, fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FAIL";
        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }

        return "SUCCESS";
    }

    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")) {
            _callback.onWebDavUploadFinish(
                    new ZoteroResult(ZoteroResult.ZotError.WEB_DAV_UP_0),
                    _changed_attachment, "", "");
            return;
        }
        _callback.onWebDavUploadFinish(new ZoteroResult(), _changed_attachment, _new_md5,
                _mod_time);
    }
}

