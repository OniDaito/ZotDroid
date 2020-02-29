package computer.benjamin.zotdr0id.task;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HttpsURLConnection;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.task.callback.WebDavDown;

/**
 * Created by oni on 13/07/2017.
 * This class deals with all the attachment downloads. It contains a couple of internal private
 * classes that split between Zotero and Webdav downloading. I think this class could be better.
 */

public class Download {
    //public static final String TAG = "zotdroid.Download";
    private AsyncTask<String,Integer,String> _request;
    private WebDavTest _test;

    /**
     * A class that is used to test the WebDavDown connection
     */
    private static class WebDavTest extends AsyncTask<String,Integer,String> {
        WebDavDown callback;
        WebDavTest(WebDavDown callback){
            this.callback = callback;
        }

        protected String doInBackground(String... address) {
            String result = "SUCCESS";
            URL url;

            try {
                url = new URL(address[0]);
            } catch (MalformedURLException e) {
                result = "Malformed URL";
                return result;
            }

            final String username = address[1];
            final String password = address[2];
            HttpsURLConnection urlConnection;

            try {
                String basic_auth = getB64Auth(username,password);

                if (url.getProtocol().contentEquals("https") ) {
                    urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("OPTIONS");
                    urlConnection.setRequestProperty("Authorization", basic_auth);
                    urlConnection.connect();

                    try {
                        String line = urlConnection.getContent().toString();
                    } catch (IOException e) {
                        result = "Failed to access: " + e.getMessage() + ". " + R.string.error_check_webdav;
                    } catch (Exception e) {
                        result = "Failed to access personal webdav." + R.string.error_check_webdav;
                    } finally {
                        urlConnection.disconnect();
                    }
                } else {
                    result = "Failed to read";
                }
            } catch (IOException e) {
                result = e.getMessage();
            }
            return result;
        }

        protected void onPostExecute(String rstring) {
            //Log.i(TAG, rstring);
            if (rstring.equals("SUCCESS")){
                // TODO - not the best callback
                callback.onDownloadFinish(new ZoteroResult(), null);
                return;
            }
            callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_0), null);
        }
    }

    /**
     * Given two strings, return a proper basic auth string
     */
    private static String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        return "Basic "+ Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
    }

    /**
     * The async derived class that actually performs the real work.
     */
    private class WebDavRequest extends AsyncTask<String,Integer,String> {
        WebDavDown callback;
        Attachment attachment;
        String username;
        String password;
        String file_path;

        WebDavRequest(WebDavDown callback, Attachment attachment, String username,
                      String password, String file_path){
            this.callback = callback;
            this.attachment = attachment;
            this.username = username;
            this.password = password;
            this.file_path = file_path;
        }

        protected String doInBackground(String... address) {
            String result;
            URL url;
            result = "SUCCESS";

            // Credentials are address[1] / username and address[2] / password
            // filename is address[3]

            String filename =  this.attachment.get_zotero_key() + ".zip";
            String file_path = this.file_path;
            String final_filename = this.attachment.get_file_name();

            try {
                url = new URL(address[0] + "/" + filename);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                result = R.string.error_malformed + e.getMessage();
                return result;
            }

            //Log.i(TAG, filename + ", " + url.toString());
            String basic_auth = getB64Auth(username, password);
            HttpsURLConnection urlConnection;

            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", basic_auth);
                urlConnection.setRequestProperty("Keep-Alive", "true");

                try {
                    // We read in the zip file but don't unzip till later and when we do, we go into a temp directory
                    // to do so
                    // https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
                    File file = new File(file_path + File.separator + final_filename);
                    if (file.exists()) {
                        file.delete();
                    }

                    // Now do the reading but save to a file
                    byte[] bytes = new byte[1024]; // read in 1024 chunks
                    InputStream is = urlConnection.getInputStream();
                    ZipInputStream zin = new ZipInputStream(is);
                    ZipEntry entry;
                    int total_bytes_read = 0;
                    int total = urlConnection.getContentLength();
                    //Log.i(TAG,"Bytes Available: " + Integer.toString(total));

                    int bytes_read;
                    entry = zin.getNextEntry();
                    // There is a bug in the ZipInputStream. Any requests for next entry or similar
                    // just cause an error. No idea why.
                    /*String fileInfo = String.format("Entry: [%s] len %d added %TD",
                            entry.getName(), entry.getSize(),
                            new Date(entry.getTime()));
                    Log.v("DOWNLOADER", fileInfo);*/
                    FileOutputStream out = new FileOutputStream(file);
                    while ((bytes_read = zin.read(bytes, 0, bytes.length)) > 0) {
                        out.write(bytes, 0, bytes_read);
                        total_bytes_read += bytes_read;
                        // For some reason, this seems to go over 100% - I guess because there are headers
                        // or some other data not included in the getContentLength field, so we cap it.
                        float tb = total_bytes_read;
                        float tt = total;
                        int progress = (int) Math.min(Math.round((float) total_bytes_read / (float) total * 100.0), 100.0);
                        publishProgress(progress);
                    }

                    out.flush();
                    out.close();
                    zin.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    result = "NOTFOUND";
                    cleanup(file_path + File.separator + final_filename);
                } catch (IOException e) {
                    result = "IOEXCEPTION";
                    cleanup(file_path + File.separator + final_filename);
                } catch (Exception e) {
                    result =  "GENERAL";
                    cleanup(file_path + File.separator  + final_filename);
                } finally {
                    urlConnection.disconnect();
                }
            } catch( IOException e) {
                result =  "CONNECTION";
                cleanup(file_path + File.separator  + final_filename);
            } catch ( Exception e){
                result =  "CONNECTIONGENERAL";
                cleanup(file_path + File.separator  + final_filename);
            }

            return result;
        }

        private void cleanup(String path){
            //InputStream in = new BufferedInputStream(urlConnection.getErrorStream());
            // TODO - do something with the error streams at some point
            File file = new File(path);
            if (file.exists()) { file.delete(); }
        }

        /**
         * Called as the task progresses
         */

        protected void onProgressUpdate(Integer... progress) {
            //Log.i(TAG,"Progress: " + Integer.toString(progress[0]));
            callback.onDownloadProgress(new ZoteroResult(), Float.valueOf(progress[0]));
        }

        /**
         * Called once a task has completed
         */
        protected void onPostExecute(String rstring) {
            //Log.i(TAG, "Post Execute: " + rstring);
            if (rstring.equals("SUCCESS")){
                callback.onDownloadFinish(new ZoteroResult(), attachment);
            } else {
                if (rstring.equals("NOTFOUND")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_0), attachment);
                }
                else if (rstring.equals("IOEXCEPTION")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_1), attachment);
                }
                else if (rstring.equals("GENERAL")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_2), attachment);
                }
                else if (rstring.equals("CONNECTION")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_3), attachment);
                }
                else if (rstring.equals("CONNECTIONGENERAL")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_4), attachment);
                }
            }
        }
    }

    /**
     * The async derived class that downloads from Zotero's servers
     * This usually results in a redirect via a 304 to some final link
     * We also don't end up with zipped file for some reason.
     */
    private class ZoteroRequest extends AsyncTask<String,Integer,String> {
        WebDavDown callback;
        Attachment  _attachment;
        Context     _context;

        ZoteroRequest(WebDavDown callback, Attachment attachment, Context context){
            this.callback = callback;
            this._attachment = attachment;
        }

        protected String doInBackground(String... address) {
            String result = "SUCCESS";
            URL url;
            String file_path = FileManager.getAttachmentsDirectory();
            String final_filename = _attachment.get_file_name();

            try {
                url = new URL(address[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                result = "Malformed URL.";
                return result;
            }

            HttpsURLConnection urlConnection;
            try {
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("Zotero-API-Key", ZoteroDeets.get_user_secret());

                try {
                    // https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
                    File file = new File(file_path + File.separator + final_filename);
                    if (file.exists()) {
                        file.delete();
                    }

                    // Now do the reading but save to a file
                    byte[] bytes = new byte[1024]; // read in 1024 chunks
                    InputStream is = urlConnection.getInputStream();
                    BufferedInputStream buf = new BufferedInputStream(is);
                    Map<String, List<String>> headers = urlConnection.getHeaderFields();
                    int code = urlConnection.getResponseCode();

                    if (headers.containsKey("Content-Length")) {
                        String tt = headers.get("Content-Length").get(0);
                        //result = cleanup(e, callback.getStringResource(R.string.error_file_not_found),file_path + final_filename);

                        //Log.i(TAG,"Bytes Available: " + tt);
                        int total = Integer.decode(tt);
                        FileOutputStream out = new FileOutputStream(file);
                        int total_bytes_read = 0;
                        int bytes_read;

                        while ((bytes_read = buf.read(bytes, 0, bytes.length)) > 0) {
                            out.write(bytes, 0, bytes_read);
                            total_bytes_read += bytes_read;
                            // For some reason, this seems to go over 100% - I guess because there are headers
                            // or some other data not included in the getContentLength field, so we cap it.
                            int progress = (int) Math.min(Math.round((float) total_bytes_read / (float) total * 100.0), 100.0);
                            publishProgress(progress);
                        }
                        out.flush();
                        out.close();

                    } else {
                        cleanup( file_path + File.separator  + final_filename);
                        urlConnection.disconnect();
                        result = "CONTENTLENGTH";
                    }
                    buf.close();
                    // return the full path so we can open it
                    //callback.onWebDavComplete(true, file_path + File.separator  + final_filename);
                    result =  "SUCCESS";
                } catch (FileNotFoundException e) {
                    result = "FILENOTFOUND";
                } catch (IOException e) {
                    cleanup(file_path + File.separator  + final_filename);
                    result = "IOEXCEPTION";
                }
                catch (Exception e) {
                    cleanup(file_path + File.separator  + final_filename);
                    result = "GENERAL";
                } finally {
                    urlConnection.disconnect();
                }
            } catch ( FileNotFoundException e){
                cleanup(file_path + File.separator  + final_filename);
                result = "FILENOTFOUND";
            } catch (IOException e) {
                cleanup(file_path + File.separator  + final_filename);
                result = "CONNECTION";
            }

            return result;
        }

        private void cleanup(String path){
            //InputStream in = new BufferedInputStream(urlConnection.getErrorStream());
            // TODO - do something with the error streams at some point
            File file = new File(path);
            if (file.exists()) { file.delete(); }
        }

        /**
         * Called as the task progresses
         */

        protected void onProgressUpdate(Integer... progress) {
            //Log.i(TAG,"Progress: " + Integer.toString(progress[0]));
            callback.onDownloadProgress(new ZoteroResult(), Float.valueOf(progress[0]));
        }

        /**
         * Called once a task has completed
         */
        protected void onPostExecute(String rstring) {
            //Log.i(TAG, "Post Execute: " + rstring);
            if (rstring.contains("SUCCESS")){
                callback.onDownloadFinish(new ZoteroResult(), _attachment);
                return;
            } else {
                if (rstring.equals("CONTENTLENGTH")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_5), _attachment);
                }
                else if (rstring.equals("IOEXCEPTION")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_1), _attachment);
                }
                else if (rstring.equals("GENERAL")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_2), _attachment);
                }
                else if (rstring.equals("CONNECTION")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_3), _attachment);
                }
                else if (rstring.equals("FILENOTFOUND")) {
                    callback.onDownloadFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_6), _attachment);
                }
            }
        }
    }

    /**
     * Stop any current download request.
     */
    public void stop() {
        if (_request != null) { _request.cancel(true); }
        if (_test != null) { _test.cancel(true);}
    }

    /**
     * Test the webdav connection to see if it works at all
     */
    public void testWebDav(Activity activity, WebDavDown callback){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        String username = settings.getString("settings_webdav_username","username");
        String password = settings.getString("settings_webdav_password","password");
        String server_address = settings.getString("settings_webdav_address","address");
        _test = new WebDavTest(callback);
        _test.execute(server_address, username, password);
    }

    public void downloadAttachment(Attachment attachment, String file_path, String username,
                                   String password, String server_address, WebDavDown callback){
        // Get the credentials we need for this
        _request = new WebDavRequest(callback, attachment, username, password, file_path);
        _request.execute(server_address);
    }

    /**
     * Download the file using the Zotero provided storage
     */
    public void downloadAttachmentZotero(Attachment attachment, WebDavDown callback,
                                         Context context){
        // Get the credentials we need for this
        _request = new ZoteroRequest(callback, attachment, context);
        String server_address = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() +
                "/items/" + attachment.get_zotero_key() + "/file";
        _request.execute(server_address);
    }

    public void downloadAttachmentGroup(Group group, Attachment attachment,
                                        WebDavDown callback, Context context){
        // Get the credentials we need for this
        _request = new ZoteroRequest(callback, attachment, context);
        String server_address = Constants.BASE_URL + "/groups/" + group.get_zotero_key() +
                "/items/" + attachment.get_zotero_key() + "/file";
        _request.execute(server_address);
    }

}


