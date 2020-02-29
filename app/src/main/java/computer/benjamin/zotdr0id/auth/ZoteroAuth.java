package computer.benjamin.zotdr0id.auth;

/**
 * Created by oni on 20/03/2018.
 */

import android.app.Dialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Window;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import computer.benjamin.zotdr0id.R;

public class ZoteroAuth extends android.os.Handler {

    /** And these are the OAuth endpoints we talk to.
     *
     * We embed the requested permissions in the endpoint URLs; see
     * http://www.zotero.org/support/dev/server_api/oauth#requesting_specific_permissions
     * for more details.
     */
    private static final String OAUTH_REQUEST = "https://www.zotero.org/oauth/request?" +
            "library_access=1&" +
            "notes_access=1&" +
            "write_access=1&" +
            "all_groups=write";
    private static final String OAUTH_ACCESS = "https://www.zotero.org/oauth/access?" +
            "library_access=1&" +
            "notes_access=1&" +
            "write_access=1&" +
            "all_groups=write";
    private static final String OAUTH_AUTHORIZE = "https://www.zotero.org/oauth/authorize?" +
            "library_access=1&" +
            "notes_access=1&" +
            "write_access=1&" +
            "all_groups=write";

    public static String CONSUMER_KEY = "50e538ada5d8c4f40e01";
    public static String CONSUMER_SECRET = "ef03b60b207aef632c24";

    private static String CREDENTIALS_STORE_PREF_FILE = "oauth";

    private LoginActivity _login_activity;

    private OAuthManager manager = null;
    private SharedPreferences settings = null;

    private Thread _thread = null; // We keep this so we definitely close it before finalise is called.

    private String user_secret = "";
    private SharedPreferencesCredentialStore credentialStore;

    public static final JsonFactory JSON_FACTORY = new JacksonFactory();
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    public ZoteroAuth(LoginActivity activity) {
        _login_activity = activity;
        ClientParametersAuthentication client = new ClientParametersAuthentication(CONSUMER_KEY, CONSUMER_SECRET);

        // setup credential store
        credentialStore = new SharedPreferencesCredentialStore(activity.getApplicationContext(),
                        CREDENTIALS_STORE_PREF_FILE, JSON_FACTORY);

        AuthorizationFlow.Builder flowBuilder = new AuthorizationFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(OAUTH_ACCESS),
                client,
                CONSUMER_KEY,
                OAUTH_AUTHORIZE)
                .setCredentialStore(credentialStore);

        flowBuilder.setTemporaryTokenRequestUrl(OAUTH_REQUEST);
        AuthorizationFlow flow = flowBuilder.build();

        // setup authorization UI controller
        AuthorizationDialogController controller =
            new DialogFragmentController(activity.getFragmentManager(), false) {
                @Override
                public String getRedirectUri() throws IOException {
                    return OAUTH_AUTHORIZE;
                }

                @Override
                public boolean isJavascriptEnabledForWebView() {
                    return true;
                }

                @Override
                public boolean disableWebViewCache() {
                    return false;
                }

                @Override
                public boolean removePreviousCookie() {
                    return true;
                }

                /*@Override
                public void onPrepareDialog(Dialog dialog){
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_login);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(true);
                    DisplayMetrics displayMetrics = _login_activity.getResources().getDisplayMetrics();
                    int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
                    int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
                    dialog.getWindow().setLayout(dialogWidth, dialogHeight);

                }*/
            };


        manager = new OAuthManager(flow, controller);
    }

    /**
     * The last step
     */
    private Thread _get_userid(String token) {

        // Start initialisation in a separate thread for now.
        final String _token = token;

        // TODO - if this fails for whatever reason, we cant tidy up UX as this is on a different
        // thread.

        Runnable run = new Runnable() {

            public void run() {
                // Only one address please
                URL url;
                String rval = "";
                try {
                    url = new URL("https://api.zotero.org/keys/" + _token);
                    HttpsURLConnection urlConnection = null;

                    try {
                        urlConnection = (HttpsURLConnection) url.openConnection();
                        urlConnection.setInstanceFollowRedirects(true);
                        urlConnection.setRequestProperty("Zotero-API-Key",_token);

                        try {
                            BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            StringBuilder total = new StringBuilder();
                            String line;
                            while ((line = r.readLine()) != null) {
                                total.append(line).append('\n');
                            }
                            rval = total.toString();

                            try {
                                JSONObject jObject = new JSONObject(rval);
                                String username = jObject.getString("username");
                                String user_id = jObject.getString("userID");
                                finish(true, user_id, username);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                finish(false,"","");
                            }


                        } catch (IOException e) {
                            finish(false,"","");
                        } finally {
                            urlConnection.disconnect();
                        }
                    } catch (IOException e) {
                        finish(false,"","");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    finish(false,"","");
                }
            }
        };

        Thread thread = new Thread(null, run, "Background");
        thread.start();
        return thread;
    }

    void finish(final boolean success, final String user_id, final String username) {
        // We need to call onAuthFinish on the main thread then finish this one so all this looks
        // a bit messy but it works.
        try {
            _login_activity.runOnUiThread(new Runnable() {
                public void run() {
                    if (success) {
                        ZoteroDeets.set(_login_activity, true, user_secret, user_id, username);
                    }
                    _login_activity.onAuthFinish(success);
                }
            });
            _thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void start() {
        final ZoteroAuth thisref = this;

        final OAuthManager.OAuthCallback<Credential> authCallback = new OAuthManager.OAuthCallback<Credential>() {
            @Override public void run(OAuthManager.OAuthFuture<Credential> future) {
                try {
                    Credential credential = future.getResult();
                    user_secret = credential.getAccessToken();
                    if (!user_secret.equals("")) {
                        _thread = _get_userid(user_secret);
                    }
                } catch (IOException e) {
                    _login_activity.onAuthFinish(false);
                    e.printStackTrace();
                } catch (java.util.concurrent.CancellationException e) {
                    _login_activity.onAuthFinish(false);
                    e.printStackTrace();
                }
                // make API queries with credential.getAccessToken()
            }
        };

        final OAuthManager.OAuthCallback<Boolean> deleteCallback = new OAuthManager.OAuthCallback<Boolean>() {
            @Override public void run(OAuthManager.OAuthFuture<Boolean> future) {
                manager.authorize10a("userID", authCallback, thisref);
            }
        };

        if(!ZoteroDeets.is_authed()) {
            manager.deleteCredential("userID", deleteCallback, this );
        }
    }
}
