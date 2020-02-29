package computer.benjamin.zotdr0id.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.task.callback.ZoteroUp;

/**
 * Created by oni on 01/12/2017.
 * This task uploads a file to Zotero or External webdav once we have requested access and gained
 * the token we need, using PushAttachementReq class
 */

public class PushZotero extends ZoteroTask {

    private ZoteroUp _callback;
    private Attachment _changed_attachment;
    private String _prefix;
    private String _suffix;
    private String _content_type;
    private String _url;
    private String _upload_key;


    public PushZotero(ZoteroUp callback, String url, String prefix, String suffix, String content_type,
                      String upload_key, Attachment changed_attachment) {
        _callback = callback;
        _prefix = prefix;
        _suffix = suffix;
        _url = url;
        _content_type = content_type;
        _upload_key = upload_key;
        _changed_attachment = changed_attachment;
    }

    public void startZoteroTask() {
        // Setup the URL, read in the new file to memory, and then concat with _prefix and _suffix.
        // At this point, we should be ready to upload the lot to Zotero.
        execute(_url);
    }

    protected String doInBackground(String... address) {
        String name = FileManager.getAttachmentsDirectory() + "/"
                + _changed_attachment.get_file_name();
        // Read in the file so it's ready to write directly to the URL outputstream
        File f = new File(name);
        int file_size = (int) f.length();
        byte[] bytes = new byte[file_size];

        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "FAIL";
        } catch (IOException e) {
            e.printStackTrace();
            return "FAIL";
        }

        String CRLF = "\r\n";
        String pre_body = "";
        pre_body += "Content-Type: application/octet-stream" + CRLF;
        pre_body += "Content-Transfer-Encoding: binary" + CRLF;

        String result = "";
        URL url = null;
        try {
            url = new URL(_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return result;
        }

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setFixedLengthStreamingMode(bytes.length + _suffix.getBytes().length
                    + _prefix.getBytes().length);
            urlConnection.setRequestProperty("Content-Type", _content_type);
            urlConnection.setRequestProperty("Zotero-API-Key", ZoteroDeets.get_user_secret());

            OutputStream os = urlConnection.getOutputStream();
            os.write(_prefix.getBytes());
            os.write(bytes);
            os.write(_suffix.getBytes());
            os.close();

            try {
                BufferedReader r = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder total = new StringBuilder();

                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                result = total.toString();
                return result;

            } catch (IOException e) {
                if (urlConnection.getResponseCode() != 200) {
                    String responsemessage = urlConnection.getResponseMessage();
                    urlConnection.getContent();
                }
                e.printStackTrace();
                result = "FAIL";
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = "FAIL";
        }
        return result;
    }

    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")) {
            _callback.onZoteroUploadFinish(
                    new ZoteroResult(ZoteroResult.ZotError.WEB_DAV_UP_0),
                    _changed_attachment, _upload_key);
            return;
        }
        _callback.onZoteroUploadFinish(new ZoteroResult(), _changed_attachment, _upload_key);
    }
}

