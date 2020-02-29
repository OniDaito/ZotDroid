package computer.benjamin.zotdr0id.auth;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Details are needed everywhere so we have a static class which we can
 * refresh
 * Created by oni on 22/03/2018.
 */

public class ZoteroDeets {
    private static String _userid = "";
    private static String _user_secret = "";
    private static String _username = "";
    private static boolean _authed = false;

    public static void load(Activity activity){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        // Assume auth if user_secret is actually set
        _user_secret = settings.getString("settings_user_secret","");
        _userid = settings.getString("settings_user_id","");
        _username = settings.getString("settings_username","");

        // Assume auth if user_secret is actually set
        _authed = true;
        if (_user_secret.equals("") || _user_secret.contains("This preference will be automatically filled upon login")) { _authed = false; };
    }

    public static void set(Activity activity, boolean authed, String user_secret, String userid, String username){
        _userid = userid;
        _user_secret = user_secret;
        _username = username;
        _authed = authed;

        if (_authed) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("settings_user_id", _userid);
            editor.putString("settings_user_secret", _user_secret);
            editor.putString("settings_user_key", _user_secret);
            editor.putString("settings_username", _username);
            editor.apply();
            editor.commit();
        }
    }

    public static boolean is_authed() { return _authed; }
    public static void set_authed(boolean auth) {_authed = auth;}

    public static void set_authed(boolean auth, Activity activity) {
        if (!auth) {
            ZoteroDeets.set_authed(false);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("settings_user_id", "This preference will be automatically filled upon login");
            editor.putString("settings_user_secret", "This preference will be automatically filled upon login");
            editor.putString("settings_user_key", "This preference will be automatically filled upon login");
            editor.apply();
            editor.commit();
        } else {
            load(activity);
        }
    }

    public static String get_userid() { return _userid; }
    public static String get_username() { return _username; }
    public static String get_user_secret() { return _user_secret; }
}
