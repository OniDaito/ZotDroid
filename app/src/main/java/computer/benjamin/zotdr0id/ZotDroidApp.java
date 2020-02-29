package computer.benjamin.zotdr0id;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import computer.benjamin.zotdr0id.ops.Sync;
import computer.benjamin.zotdr0id.ops.Upgrade;
import computer.benjamin.zotdr0id.ops.User;

/**
 * Created by oni on 15/11/2017.
 */

public class ZotDroidApp extends Application {
    protected ZotDroidDB        _zotdroid_db;
    protected ZotDroidMem       _zotdroid_mem;
    private User                _zotdroid_user_ops;
    private Sync                _zotdroid_sync_ops;
    private Upgrade             _zotdroid_upgrade_ops;
    private static Context      _context;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create the memory pool
        _zotdroid_mem = new ZotDroidMem();
        _context = getApplicationContext();
        // find the database or instead, create it, if it doesnt exist.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String database_path = settings.getString("settings_db_location", "");
        database_path = Util.remove_trailing_slash(database_path);
        if (Util.path_exists(database_path)) {
            _zotdroid_db = new ZotDroidDB(this, database_path);
        } else {
            if (Util.create_path(database_path)){
                _zotdroid_db = new ZotDroidDB(this, database_path);
            } else {
                _zotdroid_db = new ZotDroidDB(this);
            }
        }

        _zotdroid_user_ops = new User(_zotdroid_db, _zotdroid_mem);
        _zotdroid_sync_ops = new Sync(_zotdroid_db, _zotdroid_mem);
        _zotdroid_upgrade_ops = new Upgrade(_zotdroid_db, _zotdroid_mem);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("settings_db_location", _zotdroid_db.get_location());
        editor.apply();

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() { return _context; }

    /**
     * We use this to start the debugging
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public ZotDroidMem getMem() { return _zotdroid_mem; }

    public ZotDroidDB getDB() {
        return _zotdroid_db;
    }

    public Sync getSyncOps() { return _zotdroid_sync_ops; }

    public Upgrade getUpgradeOps() { return _zotdroid_upgrade_ops; }

    public User getUserOps() { return  _zotdroid_user_ops; }
}
