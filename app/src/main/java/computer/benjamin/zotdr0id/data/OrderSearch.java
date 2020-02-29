package computer.benjamin.zotdr0id.data;

import computer.benjamin.zotdr0id.data.zotero.Author;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Tag;
import computer.benjamin.zotdr0id.data.zotero.Top;

/**
 * A class we use to determine searches and ordering from the DB and the memory
 */
public class OrderSearch {

    public enum Order {
        BASIC,
        TITLE,
        AUTHOR,
        DATE_ADDED,
        DATE_MODIFIED,
    }

    Order   _order;
    boolean _reversed;
    String  _searchTerm;

    public OrderSearch(Order order){
        this._order = order;
        this._reversed = false;
        this._searchTerm = "";
    }

    public OrderSearch(Order order, String searchTerm){
        this._order = order;
        this._reversed = false;
        this._searchTerm = searchTerm;
    }

    public Order get_order() {
        return _order;
    }

    public boolean get_reversed() {
        return _reversed;
    }

    public void set_reversed(boolean b) {
        this._reversed = b;
    }

    public void set_searchTerm (String term){
        this._searchTerm = term;
    }

    /**
     * Actually search our top item using the search term and say if it meets the criteria or not.
     * @param top
     * @return
     */
    public boolean filter(Top top){
        if (_searchTerm.equals("")){ return true; }
        if (top.get_title().contains(_searchTerm)) {return true; }

        for (Author a : top.get_authors()){
            if (a.search(_searchTerm)) { return true; }
        }

        if (top.get_abstract().contains(_searchTerm)) { return true; }

        for (Tag t : top.get_tags()) {
            if (t.get_name().contains(_searchTerm)) { return true; }
        }

        for (SubNote n : top.get_sub_notes()) {
            if (n.get_note().contains(_searchTerm)) { return true; }
        }

        // TODO - search the note itself
        return false;
    }


}
