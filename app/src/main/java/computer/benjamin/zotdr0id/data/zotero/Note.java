package computer.benjamin.zotdr0id.data.zotero;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

public class Note extends Top {

    public static final String TAG = "zotdroid.data.Note";

    public String get_zotero_key() {
        return _zotero_key;
    }

    public void set_zotero_key(String key) {
        if (key != null) {
            this._zotero_key = key;
        }
    }

    public String get_tag() { return TAG; }

    public String get_note() {
        return _note;
    }

    public void set_note(String note) { if (note != null) {this._note = note;}}

    public String get_group_key() {
        return _group_key;
    }

    public void set_group_key(String key) {
        if (key != null) {
            this._group_key = key;
        }
    }


    public String get_date_added() {
        return _date_added;
    }

    public void set_date_added(String date_added) { this._date_added = date_added; }

    public String get_date_modified() {
        return _date_modified;
    }

    public void set_date_modified(String date_added) {
        this._date_modified = date_added;
    }

    public void addCollection(Collection c) { _collections.add(c);}

    public boolean is_synced() {
        return _synced;
    }

    public void set_synced(boolean _synced) {
        this._synced = _synced;
    }

    public String get_version() {
        return _version;
    }

    public void set_version(String _version) {
        this._version = _version;
    }

    /**
     * Search the title, authors and tags for this particular record
     */

    public boolean search(String term) {
        String tt = term.toLowerCase();
        if (_note.toLowerCase().contains(tt)) {
            return true;
        }
        return false;
    }

    // TODO - this is used only for updates at the moment - so we ONLY return these
    // things we are allowing the user to change on the server from this program
    public JSONObject to_json() {
        JSONObject jobj = new JSONObject();

        try {
            jobj.put("key",_zotero_key);
            jobj.put("version", _version);
            jobj.put("note", _note);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jobj;
    }

    // For now, we cover all the bases we need for all possible items
    // Eventually we might have separate record tables

    // TODO - do we really need _item_type and _content_type? Is it not implicit in the class?
    private String    _note;

    // TODO - decided to change from Date to string as Date conversion takes for ages! ><
    private String              _date_added;
    private String              _date_modified;
    private String              _version;
    private Vector<Collection>  _collections;
    private String              _zotero_key;
    private String              _group_key;
    private boolean             _synced;

    public String toString() {
        return _note;
    }

    public String get_title() { return  _note.substring(0, Math.min(_note.length(), 50)); }

    @Override
    public void add_collection(Collection c) { _collections.add(c); }

    public ArrayList<String> get_display_items() {
        ArrayList<String> items =  new ArrayList<String>();
        // TODO - this is also in the redraw method - duplicating :/
        // We add metadata first, followed by attachments (TODO - Add a divider?)
        items.add("Title: " + get_title());
        items.add("Date Added: " + get_date_added());
        items.add("Date Modified: " + get_date_modified());
        items.add(get_note());
        return items;
    }

    public Note(String key){
        _zotero_key = key;
        _date_added = "no date";
        _date_modified = "no date";
        _collections = new Vector<>();
        _temp_collections = new Vector<>(); // TODO - temporarily holding collection keys might not be the best way
        _synced = false;
        _version = "0000";
        _note = "";
    }

    public Note(String key, String date_added, String date_modified, String note,
                String version, String group_key) {
        _zotero_key = key;
        _date_added = date_added;
        _date_modified = date_modified;
        _collections = new Vector<>();
        _temp_collections = new Vector<>(); // TODO - temporarily holding collection keys might not be the best way
        _synced = false;
        _version = version;
        _note = note;
        _group_key = group_key;
    }
}
