package computer.benjamin.zotdr0id.task;

/**
 * Created by oni on 21/07/2017.
 * Get the items from Zotero. Our main task essentially. We process entries here too, returning
 * a vector of items, notes, attachments and all the rest from the server.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Author;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.Note;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Tag;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.task.callback.Tops;

public class Topper extends ZoteroGet {
    private static final String TAG = "Topper";
    Tops callback;
    private int startItem = 0;
    private int itemLimit = 25; // Seems to be what Zotero likes by default (despite the API)
    private String _url = "";
    private Group _group = new Group();
    private boolean _reset_mode = true;

    public Topper(Tops callback, Group group, int start, int limit) {
        this.callback = callback;
        this.startItem = start;
        this.itemLimit = limit;
        _group = group;
        _reset_mode = true;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items?start=" + Integer.toString(this.startItem);

        if (!_group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _url = Constants.BASE_URL + "/groups/" + _group.get_zotero_key() + "/items?start=" + Integer.toString(this.startItem);
        }
    }

    public Topper(Tops callback, Group group, Vector<String> keys) {
        this.callback = callback;
        _reset_mode = false;
        _group = group;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items?itemKey=";

        if (!_group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _url = Constants.BASE_URL + "/groups/" + _group.get_zotero_key() + "/items?itemKey=";
        }

        StringBuilder bb = new StringBuilder();
        for (String key: keys){
            bb.append(key);
            bb.append(",");
        }
        _url += bb.toString();
    }

    /**
     * Actually execute the async task.
     * Not quite an override but it sets up the string for us when we do actually execute, so things are in sync
     * For some reason, it only works if I pass the start (and possibly limit) as URL params instead of headers
     * but the desc and dateAdded seem ok. It could be an integer thing I suspect
     */
    public void startZoteroTask(){
        if (_reset_mode) {
            execute(_url,
                    "start", Integer.toString(this.startItem),
                    "limit", Integer.toString(this.itemLimit),
                    "direction", "desc",
                    "sort", "dateAdded");
        } else {
            execute(_url,
                    "direction", "desc",
                    "sort", "dateAdded");
        }
    }

    private boolean processEntry(JSONObject jobj, Item item) {

        try { item.set_title(jobj.getString("title"));
        } catch (JSONException e) {
            item.set_title("No title");
        }

        try {
            String td = jobj.getString("dateAdded");
            //item.set_date_added(Util.jsonStringToDate(td));
            item.set_date_added(td);

        } catch (JSONException e) {}

        try {
            String td = jobj.getString("date");
            //item.set_date_added(Util.jsonStringToDate(td));
            item.set_date(td);

        } catch (JSONException e) {}

        try { String td = jobj.getString("ISSN"); item.set_issn(td);
        } catch (JSONException e) { }

        try { String td = jobj.getString("language"); item.set_language(td);
        } catch (JSONException e) { }

        try { String td = jobj.getString("extra"); item.set_extra(td);
        } catch (JSONException e) { }

        try { String td = jobj.getString("publicationTitle"); item.set_publication(td);
        } catch (JSONException e) { }

        try { String td = jobj.getString("pages"); item.set_pages(td); }
        catch (JSONException e) { }

        try { String td = jobj.getString("dateModified");
            //item.set_date_modified(Util.jsonStringToDate(td));
            item.set_date_modified(td);
        } catch (JSONException e) {}

        try { item.set_abstract(jobj.getString("abstractNote")); }
        catch (JSONException e) { item.set_abstract("No abstract"); }

        try {
            JSONArray tags = jobj.getJSONArray("tags");
            for ( int i = 0; i < tags.length(); i++){
                item.add_tag(new Tag(tags.getJSONObject(i).getString("tag"), item.get_zotero_key()) );
            }
        } catch (JSONException e) { }

        try {
            JSONArray authors = jobj.getJSONArray("creators");
            for (int i =0; i < authors.length(); i++) {
                JSONObject creator = authors.getJSONObject(i);
                // TODO - Can we ALWAYS guarantee we have the current item zotero key?
                item.add_author(new Author(creator.getString("lastName") + ", " +
                        creator.getString("firstName"), item.get_zotero_key()));
            }
        } catch (JSONException e){
            // pass, no authors
        }

        try { item.set_parent(jobj.getString("parent"));
        } catch (JSONException e){ item.set_parent(""); }

        try { item.set_item_type(jobj.getString("itemType"));
        } catch (JSONException e){ item.set_item_type(""); }

        try { item.set_item_type(jobj.getString("group"));
        } catch (JSONException e){
            // Default - place in the current group. Might not be the best :/
            item.set_group_key(_group.get_zotero_key());
        }

        try { item.set_version(jobj.getString("version"));
        } catch (JSONException e){ item.set_version("0000"); }

        try {
            JSONArray collections = jobj.getJSONArray("collections");
            for (int i=0; i < collections.length(); i++) {
                try {
                    String tj = collections.getString(i);
                    item.add_temp_collection(tj);
                } catch (JSONException e) {
                }
            }

        } catch (JSONException e) { }

        item.set_synced(true);
        return true;
    }

    private boolean processNote(JSONObject jobj, SubNote subNote) {

        // TODO - complete this
        try {
            subNote.set_zotero_key(jobj.getString("key"));
            subNote.set_synced(true);
            subNote.set_note(jobj.getString("subNote"));
            subNote.set_version( jobj.getString("version"));
            subNote.set_record_key( jobj.getString("parentItem"));
        } catch (JSONException e){
            return false;
        }
        return true;
    }

    private boolean processTopLevelNote(JSONObject jobj, Note note) {
        // Set the correct group
        note.set_group_key(_group.get_zotero_key());
        try {
            note.set_zotero_key(jobj.getString("key"));
            note.set_synced(true);
            note.set_note(jobj.getString("note"));
            note.set_version( jobj.getString("version"));
            note.set_date_added( jobj.getString("dateAdded"));
            note.set_date_modified( jobj.getString("dateModified"));

            try {
                JSONArray collections = jobj.getJSONArray("collections");
                for (int i=0; i < collections.length(); i++) {
                    try {
                        String tj = collections.getString(i);
                        note.add_temp_collection(tj);
                    } catch (JSONException e) {
                    }
                }

            } catch (JSONException e) {
            }
        } catch (JSONException e){
            return false;
        }
        return true;
    }


    private boolean processAttachment(JSONObject jobj, Attachment attachment) {
        try {
            attachment.set_zotero_key(jobj.getString("key"));
        } catch (JSONException e){
            // We should always have a key. If we dont then bad things :S
            return false;
        }

        // For now, ignore non-filename ones
        try { attachment.set_file_name(jobj.getString("filename"));
        } catch (JSONException e){ return false; }

        // TODO - some attachments are top level - do we show these?
        try { attachment.set_parent_key(jobj.getString("parentItem"));
        } catch (JSONException e){ return false; }

        try { attachment.set_version(jobj.getString("version"));
        } catch (JSONException e){ attachment.set_version("0000"); }

        try { attachment.set_file_type(jobj.getString("contentType"));
        } catch (JSONException e){ }

        try { attachment.set_md5(jobj.getString("md5"));
        } catch (JSONException e){ }

        try { attachment.set_date_added(jobj.getString("dateAdded"));
        } catch (JSONException e){ }

        try { attachment.set_date_modified(jobj.getString("dateModified"));
        } catch (JSONException e){ }

        return true;
    }

    protected void onPostExecute(String rstring) {
        Vector<Top> tops                =  new Vector<>();
        Vector<Attachment> attachments  =  new Vector<>();
        Vector<SubNote> subNotes        =  new Vector<>();

        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            callback.onTopsCompletion(new ZoteroResult(ZoteroResult.ZotError.ITEMS_TASK_0),
                    _group, "0000");
            return;
        }

        try {
            JSONObject jObject = new JSONObject(rstring);
            // Grab the totals and the versions from the headers we found
            int total = 0;
            try {
                total = jObject.getInt("Total-Results");
            } catch (JSONException e) {
                //Log.i(TAG,"No Total-Results in request.");
            }

            String version = "0000";
            try {
                version = jObject.getString("Last-Modified-Version");
            } catch (JSONException e) {
                //Log.i(TAG,"No Last-Modified-Version in request.");
            }

            JSONArray jArray = jObject.getJSONArray("results");

            for (int i=0; i < jArray.length(); i++) {
                try {
                    JSONObject jobjtop = jArray.getJSONObject(i);
                    JSONObject jobj = jobjtop.getJSONObject("data");
                    if (jobj.getString("itemType").contains("attachment")){
                        Attachment aa = new Attachment();
                        if (processAttachment(jobj,aa)) {
                            attachments.add(aa);
                        }
                    } else if (jobj.getString("itemType").contains("note")) {

                        // Further check to see if it has a parent item. If not it's a top
                        // level note inside this collection

                        try {
                            String parentItem = jobj.getString("parentItem");
                            SubNote subNote = new SubNote();
                            if(processNote(jobj, subNote)){
                                subNotes.add(subNote);
                            }
                        } catch (JSONException e) {
                            Note note = new Note(jobj.getString("key"));
                            if(processTopLevelNote(jobj, note)){
                                tops.add(note);
                            }
                        }

                    } else {
                        // TODO - We need to handle these exceptions better - possibly by just ignoring this record
                        try {
                            Item item = new Item(jobj.getString("key"));
                            if (processEntry(jobj, item)) {
                                tops.add(item);}
                        } catch (JSONException e){
                            // We should always have a key. If we dont then bad things :S
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onTopCompletion(
                            new ZoteroResult(ZoteroResult.ZotError.ITEMS_TASK_1),
                            _group, 0, total, null,
                            null, null, "0000");
                    return;
                }
            }

            // TODO - There appears to be no difference between these two at the moment in terms
            // of what the callback does
            if (_reset_mode) {
                callback.onTopCompletion(new ZoteroResult(), _group, startItem +
                                jArray.length(), total, tops, attachments, subNotes, version);
            } else {
                callback.onTopCompletion(new ZoteroResult(), _group, tops, attachments,
                        subNotes, version);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            callback.onTopsCompletion(new ZoteroResult(ZoteroResult.ZotError.ITEMS_TASK_2), _group,
                    "0000");
        }
    }
}
