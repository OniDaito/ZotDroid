package computer.benjamin.zotdr0id.data.zotero;

//import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;

/**
 * Created by oni on 14/07/2017.
 * The major class that represents an entry in our Zotero library. It features as the main top
 * level item that appears in our list.
 */

public class Item extends Top {
    // TODO - At some point we need to move away from strings for everything I think.
    public static final String TAG = "zotdroid.data.Item";
    // TODO - do we really need _item_type and _content_type? Is it not implicit in the class?
    private String    _content_type;
    private String    _title;
    private String    _item_type;
    // TODO - decided to change from Date to string as Date conversion takes for ages! ><
    private String      _date_added;
    private String      _date_modified;
    private String      _date;
    //protected Date      _date_added;
    //protected Date      _date_modified;
    private String    _version;
    private String    _zotero_key;
    private String    _group_key;
    private String    _parent;
    private String    _abstract;
    private String    _language;
    private String    _issn;
    private String    _pages;
    private String    _extra;
    private String    _publication;
    private Vector<Attachment> _attachments;
    private Vector<Collection> _collections;
    private Vector<Tag>       _tags;
    private Vector<SubNote> _subnotes;
    private Vector<Author>    _authors;
    private boolean           _synced;

    public Item(String key){
        _zotero_key = key;
        _authors = new Vector<>();
        _date_added = "no date";
        _date_modified = "no date";
        _date = "no date";
        _synced = false;
        _abstract = "";
        _issn = "";
        _pages = "";
        _language = "";
        _extra = "";
        _publication = "";
        _version = "0000";
        _group_key = Constants.LOCAL_GROUP;
        _attachments = new Vector<>();
        _collections = new Vector<>();
        _temp_collections = new Vector<>(); // TODO - temporarily holding collection keys might not be the best way
        _tags = new Vector<>();
        _subnotes = new Vector<>();

    }

    public String toString() {
        return _title + " - " + _authors.firstElement();
    }
    public boolean can_tags() { return true; };
    public boolean can_attachments() { return true; };
    public boolean can_notes() { return true; };

    public String get_zotero_key() {
        return _zotero_key;
    }

    public void set_zotero_key(String key) {
        if (key != null) {
            this._zotero_key = key;
        }
    }

    public String get_tag() { return TAG; }

    public Vector<Author> get_authors() {
        return _authors;
    }
    public void add_author(Author author) {
        _authors.add(author);
    }

    public String get_title() {
        return _title;
    }
    public void set_title(String title) { if (title != null) {this._title = title;}}

    public String get_issn() {
        return _issn;
    }
    public void set_issn(String issn) { if (issn != null) {this._issn = issn;}}

    public String get_language() {
        return _language;
    }
    public void set_language(String lang) { if (lang != null) {this._language = lang;}}

    public String get_extra() {
        return _extra;
    }
    public void set_extra(String extra) { if (extra != null) {this._extra = extra;}}

    public String get_publication() { return _publication; }
    public void set_publication(String pub) { if (pub != null) {this._publication = pub;}}

    public String get_pages() { return _pages; }
    public void set_pages(String pages) { if (pages != null) {this._pages = pages;}}

    public String get_abstract() {
        return _abstract;
    }
    public void set_abstract(String abs) { if (abs != null) {this._abstract = abs; } }

    public String get_parent() {
        return _parent;
    }
    public void set_parent(String parent) {
        this._parent = parent;
    }

    public String get_content_type() {
        return _content_type;
    }
    public void set_content_type(String content_type) {
        this._content_type = content_type;
    }

    public String get_item_type() {
        return _item_type;
    }
    public void set_item_type(String item_type) {
        if (item_type != null ) {
            this._item_type = item_type;}
    }

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

    public String get_date() {
        return _date;
    }
    public void set_date(String date) { this._date = date; }

    public String get_date_modified() {
        return _date_modified;
    }
    public void set_date_modified(String date_added) {
        this._date_modified = date_added;
    }

    public void add_attachment(Attachment attachment) { _attachments.add(attachment); }
    public void add_collection(Collection c) { _collections.add(c);}

    public Vector<Attachment> get_attachments() {return _attachments;}

    public Vector<Tag> get_tags() {return _tags;}
    public void add_tag(Tag tag) {
        for (Tag t : _tags){
            if (t.get_name().equals(tag.get_name())) { return; }
        }
        _tags.add(tag);
    }
    public void remove_tag(Tag tag) {
        for (int i = 0; i < _tags.size(); i++) {
            if (_tags.get(i).get_record_key().equals(tag.get_record_key()) &&
                    _tags.get(i).get_name().equals(tag.get_name())) {
                _tags.remove(i);
            }
        }
    }

    public Vector<SubNote> get_sub_notes() {return _subnotes;}
    public void add_note(SubNote subNote) { _subnotes.add(subNote); }

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
        for (Author author : _authors) {
            if (author._name.toLowerCase().contains(tt)) {
                return true;
            }
        }

        if (_title.toLowerCase().contains(tt)) {return true;}
        if (_extra.toLowerCase().contains(tt)) {return true;}
        if (_issn.toLowerCase().contains(tt)) {return true;}
        if (_publication.toLowerCase().contains(tt)) {return true;}

        for (Tag tag : _tags) {
            if (tag.get_name().toLowerCase().contains(tt)) {
                return true;
            }
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
            JSONArray jtags = new JSONArray();

            for (Tag t : _tags){
                JSONObject jtag = new JSONObject();
                jtag.put("tag", t.get_name());
                jtags.put(jtag);
            }

            jobj.put("tags",jtags);
        } catch (JSONException e) { e.printStackTrace(); }
        return jobj;
    }

    /**
     * A function that returns all the strings we might want to display on a UX MainActivity list
     * We ignore tags and notes as they are special items. This is just the things to display for
     * information purposes
     * @return
     */
    public ArrayList<String> get_display_items() {
        ArrayList<String> items = new ArrayList<String>();
        // TODO - this is also in the redraw method - duplicating :/
        // We add metadata first, followed by attachments (TODO - Add a divider?)
        items.add("Title: " + get_title());
        for (Author author : get_authors()) {
            items.add("Author: " + author.get_name());
        }

        int max = get_abstract().length();
        max = max > Constants.ABSTRACT_LISTING_MAX ? Constants.ABSTRACT_LISTING_MAX : max;
        if (get_abstract() != null) {
            items.add("Abstract: " + get_abstract().subSequence(0, max) + "...");
        }
        if (get_date() != null) {
            if (get_date().length() > 0) {
                items.add("Date: " + get_date());
            }
        }
        if (get_date_added().length() > 0) {
            items.add("Date Added: " + get_date_added());
        }
        if (get_date_modified().length() > 0) {
            items.add("Date Modified: " + get_date_modified());
        }
        if (get_issn() != null) {
            if (get_issn().length() > 0) {
                items.add("ISSN: " + get_issn());
            }
        }
        if (get_publication() != null) {
            if (get_publication().length() > 0) {
                items.add("Publication: " + get_publication());
            }
        }
        if (get_pages() != null) {
            if (get_pages().length() > 0) {
                items.add("Pages: " + get_pages());
            }
        }
        if (get_extra() != null) {
            if (get_extra().length() > 0) {
                items.add("Extra: " + get_extra());
            }
        }

        return items;
    }
}