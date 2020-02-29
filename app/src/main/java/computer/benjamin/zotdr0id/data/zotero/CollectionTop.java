package computer.benjamin.zotdr0id.data.zotero;

/**
 * Created by oni on 26/07/2017.
 * This class isn't meant to be used outside the database and zotdroidops classes
 * as it represents a database relationship that eventually show with a vector inside
 * Item and Note
 */

public class CollectionTop {

    public static final String TAG = "zotdroid.data.CollectionTop";
    
    // For now, we cover all the bases we need for all possible items
    // Eventually we might have separate record tables

    public String get_collection() {
        return _collection;
    }

    public void set_collection(String _collection) {
        this._collection = _collection;
    }

    public String get_item() {
        return _item;
    }

    public void set_item(String _item) {
        this._item = _item;
    }

    private String    _collection;
    private String    _item;

    public String toString() {
        return _collection + " -> " + _item;
    }
}