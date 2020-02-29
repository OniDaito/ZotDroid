package computer.benjamin.zotdr0id.task;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Base64;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.task.callback.WebDavTest;

/**
 * Created by oni on 13/07/2017.
 * TODO - Alter and try and actually download a random attachment? Need a better test
 */

public class TestWebDav extends AsyncTask<String,Integer,String>{
    //public static final String TAG = "zotdroid.Download";
    private AsyncTask<String,Integer,String> _request;
    WebDavTest callback;
    Activity _activity;

    public TestWebDav(Activity activity, WebDavTest callback){
        this.callback = callback;
        this._activity = activity;
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
            callback.onTestFinish(new ZoteroResult());
            return;
        }
        callback.onTestFinish(new ZoteroResult(ZoteroResult.ZotError.DOWNLOAD_TASK_0));
    }


    /**
     * Given two strings, return a proper basic auth string
     */
    private static String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        return "Basic "+ Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
    }
    public void go_test() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_activity);
        String username = settings.getString("settings_webdav_username", "username");
        String password = settings.getString("settings_webdav_password", "password");
        String server_address = settings.getString("settings_webdav_address", "address");
        execute(server_address, username, password);
    }

}


