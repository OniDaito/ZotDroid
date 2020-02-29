package computer.benjamin.zotdr0id.data.zotero;

/**
 * Created by oni on 15/11/2017.
 */

public class Author {
    public static final String TAG = "zotdroid.data.Author";

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_record_key() {
        return _record_key;
    }

    public boolean search(String term){return _name.toLowerCase().contains(term.toLowerCase());}

    public void set_record_key(String _record_key) {
        this._record_key = _record_key;
    }

    String _name;
    private String _record_key;

    public Author (String name, String record_key){
        _name = name;
        _record_key = record_key;
    }

}
