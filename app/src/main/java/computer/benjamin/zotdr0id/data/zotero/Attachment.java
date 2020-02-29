package computer.benjamin.zotdr0id.data.zotero;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oni on 14/07/2017.
 */

public class Attachment {

    public static final String TAG = "zotdroid.data.Attachment";

    private String  _file_name;
    String          _zotero_key;
    private String  _parent;
    private String  _file_type;
    private String  _date_added;
    private String  _date_modified;
    private String  _md5;
    private Boolean _synced;

    //protected Date      _date_added;
    //protected Date      _date_modified;
    String _version;

    public String get_version() { return _version; }

    public void set_version(String version) { _version = version; }

    public String get_file_name() {
        return _file_name;
    }

    public String get_date_added() {
        return _date_added;
    }

    public void set_date_added(String date_added) { this._date_added = date_added; }

    public String get_date_modified() {
        return _date_modified;
    }

    public void set_date_modified(String date_modified) {
        this._date_modified = date_modified;
    }

    public void set_file_name(String _file_name) {
        this._file_name = _file_name;
    }

    public String get_zotero_key() {
        return _zotero_key;
    }

    public void set_zotero_key(String _zotero_key) {
        this._zotero_key = _zotero_key;
    }

    public String get_parent_key() {
        return _parent;
    }

    public void set_parent_key(String parent) {
        this._parent = parent;
    }

    public String get_file_type() {
        return _file_type;
    }

    public void set_md5(String md5) {
        this._md5 = md5;
    }

    public String get_md5() { return _md5; }

    public boolean is_synced() {
        return _synced;
    }
    public void set_synced(boolean _synced) {
        this._synced = _synced;
    }

    public void set_file_type(String _file_type) {
        this._file_type = _file_type;
    }

    // TODO - this is used only for updates at the moment - so we ONLY return these
    // things we are allowing the user to change on the server from this program
    public JSONObject to_json() {
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("key",_zotero_key);
            jobj.put("version", _version);
        } catch (JSONException e) { e.printStackTrace(); }
        return jobj;
    }

    public Attachment() {
        _date_added = "no date";
        _date_modified = "no date";
        _version = "0000";
        _synced = false;
    }

    public Attachment(String filename, String filetype, String key, String parent,
                      String version, String md5,
                      String date_modified, boolean synced, String date_added) {
        _date_added = date_added;
        _date_modified = date_modified;
        _version = version;
        _synced = synced;
        _file_name = filename;
        _file_type = filetype;
        _zotero_key = key;
        _parent = parent;
        _md5 = md5;
    }

}