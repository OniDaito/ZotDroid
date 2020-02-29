package computer.benjamin.zotdr0id.data.zotero;

/**
 * Created by oni on 27/07/2017.
 */

import java.util.Date;

/**
 * Created by oni on 11/07/2017.
 */

public class Summary {
    public static final String TAG = "zotdr0id.data.Summary";

    public Date get_date_synced() {
        return _date_synced;
    }
    public void set_date_synced(Date _date_synced) {
        this._date_synced = _date_synced;
    }

    public String get_last_version() {
        return _last_version;
    }
    public void set_last_version(String last_version) {
        this._last_version = last_version;
    }

    public int get_upgrade() {
        return _upgrade;
    }
    public void set_upgrade(int upgrade) { this._upgrade = upgrade;}


    private Date _date_synced;
    private String _last_version;
    private int _upgrade;

    public Summary() {
        _date_synced = new Date();
        _last_version = "0000";
        _upgrade = 0;
    }

    public String get_table_name() {
        return "summary";
    }
}
