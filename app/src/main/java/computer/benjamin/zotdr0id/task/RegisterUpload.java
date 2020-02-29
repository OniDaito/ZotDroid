package computer.benjamin.zotdr0id.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.callback.RegisterUp;

/**
 * Created by oni on 05/05/2019.
 * This class sends a post request to zotero to register the upload. We call this task once the
 * upload of the file proper has completed and we wish to finalise the process.
 * https://www.zotero.org/support/dev/web_api/v3/file_upload
 */

public class RegisterUpload extends ZoteroTask  {
    //private static final String TAG = "ZoteroPushAttachmentTask";
    private RegisterUp  _callback;
    private Attachment  _changed_attachment;
    private String      _upload_key;

    public void startZoteroTask(){
        String _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items/" +
                _changed_attachment.get_zotero_key() + "/file";
        String _body = "upload=" + _upload_key;
        execute(_url, _body, _changed_attachment.get_md5());
    }

    protected String doInBackground(String... address) {
        // [0] is address
        // [1] is the post body in json
        // [2] is the previous MD5 hash
        // after that, each pair is a set of headers we want to send

        String result = "";
        URL url = null;
        try {
            url = new URL(address[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return result;
        }

        String post_body = address[1];

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Accept", "application/x-www-form-urlencoded,*/*");
            urlConnection.setRequestProperty("Zotero-API-Key", ZoteroDeets.get_user_secret());
            urlConnection.setRequestProperty("If-Match", address[2]);

            for (int i = 3; i < address.length; i+=2){
                urlConnection.setRequestProperty(address[i], address[i+1]);
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
            writer.write(post_body);
            writer.flush();
            writer.close();

            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                result = total.toString();

                // 204 is the success apparently. Nothing is returned
                if (urlConnection.getResponseCode() != 204) {
                    result = "FAIL";
                }
                return result;

            } catch (IOException e) {
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
        if (rstring.equals("FAIL")) {
            _callback.onRegisterUpFinish(
                    new ZoteroResult(ZoteroResult.ZotError.REG_TASK_0), _changed_attachment,
                        _upload_key);
            return;
        }
        _callback.onRegisterUpFinish(new ZoteroResult(), _changed_attachment, _upload_key);
    }

    public RegisterUpload(RegisterUp callback, Attachment changed_attachment,
                          String upload_key) {
        _callback = callback;
        _changed_attachment = changed_attachment;
        _upload_key = upload_key;
    }

}
