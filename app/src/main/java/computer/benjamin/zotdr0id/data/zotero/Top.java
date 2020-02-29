package computer.benjamin.zotdr0id.data.zotero;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Top abstracts out the Note and Item classes, as both must appear in the
 * main list now which is a tricky thing indeed!
 */

public abstract class Top {
    public abstract String get_title();
    public Vector<Author> get_authors() {
        // Return an empy list by default. Bit of a waste but it works.
        return new Vector<Author>();
    }

    public Vector<Attachment> get_attachments() {
        // Return an empy list by default. Bit of a waste but it works.
        return new Vector<Attachment>();
    }

    public abstract void add_collection(Collection c);
    public abstract boolean is_synced();
    public abstract String get_tag();
    public abstract ArrayList<String> get_display_items();
    public abstract void set_synced(boolean b);
    public Vector<String> get_temp_collections() { return _temp_collections; }
    public void add_temp_collection(String s) { _temp_collections.add(s);}
    public void clear_temp_collection() { _temp_collections.clear(); }

    public Vector<Tag> get_tags() {
        // Return an empy list by default. Bit of a waste but it works.
        return new Vector<Tag>();
    }
    public Vector<SubNote> get_sub_notes() {
        // Return an empy list by default. Bit of a waste but it works.
        return new Vector<SubNote>();
    }
    public String get_abstract() {
        return "";
    }

    public abstract JSONObject to_json();
    public abstract String get_zotero_key();
    public abstract String get_date_added();
    public abstract String get_date_modified();
    public abstract String get_version();

    // Used to layout the main list view - do we list tags at all? etc
    public boolean can_tags() { return false; };
    public boolean can_attachments() { return false; };
    public boolean can_notes() { return false; };

    protected Vector<String> _temp_collections;
}
