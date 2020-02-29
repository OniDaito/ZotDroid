package computer.benjamin.zotdr0id.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;

/**
 * Created by oni on 01/12/2017.
 */

public abstract class ZoteroGet extends ZoteroTask {

    protected String doInBackground(String... address) {
        // [0] is address
        // after that, each pair is a set of headers we want to send
        String result = "";
        URL url;
        try {
            url = new URL(address[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return result;
        }

        HttpsURLConnection urlConnection = null;
        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Zotero-API-Key", ZoteroDeets.get_user_secret());

            for (int i = 1; i < address.length; i+=2){
                urlConnection.setRequestProperty(address[i], address[i+1]);
            }

            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                result = total.toString();

                String headers = "";
                // Here, we check for any of the special Zotero headers we might need
                if (urlConnection.getHeaderField("Total-Results") != null){
                    headers += "Total-Results : " + urlConnection.getHeaderField("Total-Results") + ", ";
                }

                if (urlConnection.getHeaderField("Last-Modified-Version") != null){
                    headers += "Last-Modified-Version : " + urlConnection.getHeaderField("Last-Modified-Version") + ", ";
                }

                // TODO - pagination might be a bit tricky.
                result = "{ " + headers + " results : " + result + "}";

            } catch (IOException e) {
                e.printStackTrace();
                result = "FAIL";
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            // TODO - do something with the error streams at some point
            e.printStackTrace();
            result = "FAIL";
        }
        return result;
    }
}
