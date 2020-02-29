package computer.benjamin.zotdr0id.data.zotero;

import computer.benjamin.zotdr0id.Constants;

/**
 * Created by oni on 01/01/2018.
 */

public class Group {

    public static final String TAG = "zotdroid.data.Group";

    private String _zotero_key;
    private String _version;
    private String _title;

    public String get_version() { return _version; }
    public void set_version(String version) { _version = version; }

    public String get_title() { return _title; }
    public void set_title(String title) { _title = title; }

    public String get_zotero_key() {
        return _zotero_key;
    }
    public void set_zotero_key(String _zotero_key) {
        this._zotero_key = _zotero_key;
    }

    public boolean isLocal() { return _zotero_key.equals(Constants.LOCAL_GROUP); }

    public Group(String zotero_key, String title, String version) {
        _zotero_key = zotero_key;
        _version = version;
        _title = title;
    }

    public Group() {
        _zotero_key = Constants.LOCAL_GROUP;
        _title = "Local Library";
        _version = "0000";
    }
}