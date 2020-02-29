package computer.benjamin.zotdr0id.data.zotero;

/**
 * Created by oni on 21/07/2017.
 */

import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;

/**
 * Created by oni on 14/07/2017.
 */

public class Collection {

    public static final String TAG = "zotdroid.data.Collection";

    public String get_zotero_key() {
        return _zotero_key;
    }
    public void set_zotero_key(String key) {
        this._zotero_key = key;
    }

    public String get_title() {
        return _title;
    }
    public void set_title(String title) { this._title = title;}

    public void set_group_key(String group) { this._group_key = group;}
    public String get_group_key() { return _group_key;}

    public String get_parent_key() {
        return _parent_key;
    }
    public void set_parent_key(String parent) {
        this._parent_key = parent;
    }

    public Collection get_parent() {return _parent; }
    private void set_parent(Collection _parent) { this._parent = _parent;}

    public void add_collection(Collection c) {
        _sub_collections.add(c);
        c.set_parent_key(get_zotero_key());
        c.set_parent(this);
    }

    public Vector<Collection> get_sub_collections() { return _sub_collections;}

    public String get_version() {
        return _version;
    }

    public void set_version(String _version) {
        this._version = _version;
    }

    public void add_child(Top r) {_records.add(r); }

    // For now, we cover all the bases we need for all possible items
    // Eventually we might have separate record tables

    private String        _title;
    private String        _zotero_key;
    private String        _parent_key;
    private Collection    _parent;
    private String        _group_key;
    private String        _version;
    private Vector<Collection>    _sub_collections; // In-efficient in memory terms but whatever
    private Vector<Top>        _records;

    public String toString() {
        return _title;
    }
    public Collection(){
        _sub_collections = new Vector<>();
        _records = new Vector<>();
        _zotero_key = Constants.TOP_COLLECTION;
        _title = "Top";
        _version = "0000";
        _group_key = Constants.LOCAL_GROUP; // Basic group
        _parent_key = ""; // Default no parent
    }
}