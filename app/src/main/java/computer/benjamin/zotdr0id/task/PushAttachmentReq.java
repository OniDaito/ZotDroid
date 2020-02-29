package computer.benjamin.zotdr0id.task;

import org.json.JSONException;
import org.json.JSONObject;

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
import computer.benjamin.zotdr0id.task.callback.AttachReq;

/**
 * Created by oni on 05/05/2019.
 * This class sends a post request to Zotero in order to obtain a file upload token. Once we have
 * this token we can upload our new attachment.
 */

public class PushAttachmentReq extends ZoteroTask  {
    //private static final String TAG = "ZoteroPushAttachmentTask";
    private AttachReq   _callback;
    private Attachment  _changed_attachment;
    private String      _changed_attachment_sum;
    private String      _changed_time; // in miliseconds
    private String      _changed_filesize; // in bytes

    public void startZoteroTask(){
        String _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items/" +
                _changed_attachment.get_zotero_key() + "/file";
        String _body = "md5=" + _changed_attachment_sum + "&filename="
                + _changed_attachment.get_file_name() + "&filesize="
                + _changed_filesize + "&mtime=" + _changed_time;

        execute(_url, _body, _changed_attachment.get_md5() );
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

                if (urlConnection.getResponseCode() != 200) {
                    result = "FAIL";

                }

                /*String headers = "";
                // Here, we check for any of the special Zotero headers we might need
                if (urlConnection.getHeaderField("Total-Results") != null){
                    headers += "Total-Results : " + urlConnection.getHeaderField("Total-Results") + ", ";
                }

                if (urlConnection.getHeaderField("Last-Modified-Version") != null){
                    headers += "Last-Modified-Version : " + urlConnection.getHeaderField("Last-Modified-Version") + ", ";
                }

                // TODO - pagination might be a bit tricky.
                result = "{ " + headers + " results : " + result + "}";*/
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
        String version = "0000";
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")) {
            _callback.onPushAttachmentReqCompletion(
                    new ZoteroResult(ZoteroResult.ZotError.PUSH_TASK_0), "", "", "", "", "",
                    _changed_attachment);
            return;
        }

        JSONObject jObject;
        try {
            jObject = new JSONObject(rstring);
            // TODO - Check if the {exists: 1} came back, in which case nothing needs to be done.

            try {
                // Upload key, prefix and suffix are the important bits we need
                String uploadKey = jObject.getString("uploadKey");
                String prefix = jObject.getString("prefix");
                String suffix = jObject.getString("suffix");
                String url = jObject.getString("url");
                String content_type = jObject.getString("contentType");
                _callback.onPushAttachmentReqCompletion(new ZoteroResult(), url, uploadKey,
                        prefix, suffix, content_type, _changed_attachment);

            } catch (JSONException e) {
                // No upload key so this is a failed request or it already exists which is also
                // sort of bad - need to check that
                _callback.onPushAttachmentReqCompletion(
                        new ZoteroResult(ZoteroResult.ZotError.PUSH_TASK_1), "", "", "", "", "",
                        _changed_attachment);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            _callback.onPushAttachmentReqCompletion(
                    new ZoteroResult(ZoteroResult.ZotError.PUSH_TASK_2), "", "", "", "", "",
                    _changed_attachment);
        }
    }

    public PushAttachmentReq(AttachReq callback, Attachment changed_attachment,
                             String new_md5, String changed_time, String changed_filesize) {
        _callback = callback;
        _changed_attachment = changed_attachment;
        _changed_attachment_sum = new_md5;
        _changed_filesize = changed_filesize;
        _changed_time = changed_time;
    }

}
