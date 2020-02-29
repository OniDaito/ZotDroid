package computer.benjamin.zotdr0id.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.tables.Attachments;
import computer.benjamin.zotdr0id.data.tables.Authors;
import computer.benjamin.zotdr0id.data.tables.CollectionsTops;
import computer.benjamin.zotdr0id.data.tables.Collections;
import computer.benjamin.zotdr0id.data.tables.Groups;
import computer.benjamin.zotdr0id.data.tables.Notes;
import computer.benjamin.zotdr0id.data.tables.SubNotes;
import computer.benjamin.zotdr0id.data.tables.Items;
import computer.benjamin.zotdr0id.data.tables.Summary;
import computer.benjamin.zotdr0id.data.tables.Tags;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Author;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.CollectionTop;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.Note;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Tag;
import computer.benjamin.zotdr0id.data.zotero.Top;

/**
 * Created by oni on 11/07/2017.
 */

public class ZotDroidDB extends SQLiteOpenHelper {

    // All Static variables
    // TODO move to constants I think

    public static final String TAG = "zotdroid.ZotDroidDB";
    private SQLiteDatabase _db;

    private Collections         _collectionsTable       = new Collections();
    private Attachments         _attachmentsTable       = new Attachments();
    private Items               _itemsTable             = new Items();
    private Summary             _summaryTable           = new Summary();
    private CollectionsTops     _collectionsTopsTable   = new CollectionsTops();
    private Authors             _authorsTable           = new Authors();
    private Tags                _tagsTable              = new Tags();
    private SubNotes            _subnotesTable          = new SubNotes();
    private Groups              _groupsTable            = new Groups();
    private Notes               _notesTable             = new Notes();
    private boolean             _upgraded               = false;

    // A small class to hold caching info
    private class SearchCache {
        String last_search;
        int last_num_results;
        boolean valid = false;
    }

    private SearchCache         _cache                  = new SearchCache();

    /**
     * Main database interface with the context
     * @param context
     */
    public ZotDroidDB(Context context) {
        super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this._db = getWritableDatabase();
        _check_and_create();
    }

    public String get_location() { return Util.getDirPath(_db.getPath()); }

    /**
     * Alternative constructor for if we change the database location
     */
    public ZotDroidDB(Context context, String alternative_location) {
        // Double check we have no trailing slash
        super(new DatabaseContext(context), alternative_location + "/" + Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        this._db = getWritableDatabase();
        _check_and_create();
    }

    /**
     * Check that a particular table exists
     * @param tablename
     * @return
     */
    private boolean checkTableExists(String tablename){
        Cursor cursor = _db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = \""+ tablename +"\"", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * Check if our database exists and all the tables. If not then create
     * TODO - odd split of requirements here, with passing this db into the table classes
     */

    private void _check_and_create() {

        if (!checkTableExists(_itemsTable.get_table_name())) {
            _itemsTable.createTable(_db);
        }

        if (!checkTableExists(_collectionsTable.get_table_name())) {
            _collectionsTable.createTable(_db);
        }

        if (!checkTableExists(_attachmentsTable.get_table_name())) {
            _attachmentsTable.createTable(_db);
        }

        if (!checkTableExists(_collectionsTopsTable.get_table_name())) {
            _collectionsTopsTable.createTable(_db);
        }

        if (!checkTableExists(_authorsTable.get_table_name())) {
            _authorsTable.createTable(_db);
        }

        if (!checkTableExists(_tagsTable.get_table_name())) {
            _tagsTable.createTable(_db);
        }

        if (!checkTableExists(_subnotesTable.get_table_name())) {
            _subnotesTable.createTable(_db);
        }

        if (!checkTableExists(_groupsTable.get_table_name())) {
            _groupsTable.createTable(_db);
            writeGroup(new Group()); // When a new group is written, so is a collection for ALL
        }
        if (!checkTableExists(_notesTable.get_table_name())) {
            _notesTable.createTable(_db);
        }

        if (!checkTableExists(_summaryTable.get_table_name())) {
            _summaryTable.createTable(_db);
            computer.benjamin.zotdr0id.data.zotero.Summary s =
                    new computer.benjamin.zotdr0id.data.zotero.Summary();
            s.set_date_synced(new Date());
            s.set_last_version("0000");
            _summaryTable.writeSummary(s,_db);
        }

        // Set the upgrade flag
        if (_upgraded) {
            computer.benjamin.zotdr0id.data.zotero.Summary summary = _summaryTable.getSummary(_db);
            summary.set_upgrade(1);
            _summaryTable.writeSummary(summary, _db);
        }

    }

    /**
     * Destroy all the saved data and recreate if needed.
     */
    public void reset() {
        if (_db != null){ _db.close();}

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS \"" + _summaryTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _itemsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _collectionsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _attachmentsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _collectionsTopsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _authorsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _tagsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _subnotesTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _groupsTable.get_table_name() + "\"");
        db.execSQL("DROP TABLE IF EXISTS \"" + _notesTable.get_table_name() + "\"");
        onCreate(db);
    }

    /**
     * onCreate override method
     * @param db
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all the tables, presumably in memory
        // We double check to see if we have any database tables already]
        this._db = db;
        _check_and_create();
    }

    /**
     * Upgrading method when we change the DB with each new version
     * @param db
     * @param oldVersion
     * @param newVersion
     */

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
        // This is likely to get longer and longer sadly
        _upgraded = oldVersion != newVersion;

        //if (oldVersion < 2) {
            // Perform upgrade to version 2
            //db.execSQL(DATABASE_ALTER_RECORD_GROUPS);
            //db.execSQL(DATABASE_SET_RECORD_GROUP);
            // Set all records to have a group of local as the 'groups' function only changes
            // these groups that are shared and not 'local'
        //}

    }

    /**
     * Get the number of rows in a table
     * @param tablename
     * @return
     */
    private int getNumRows(String tablename){
        int result = 0;
        Cursor cursor = _db.rawQuery("select count(*) from \"" + tablename + "\";", null);
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    /**
     * Return a single set of ContentValues from a particular table
     * @param db
     * @param key
     * @return
     */
    public ContentValues getSingle(SQLiteDatabase db, String table_name, String key){
        String q = "select * from " + table_name + " where zotero_key=\"" + key + "\";";
        Cursor cursor = db.rawQuery(q, null);
        if (cursor != null){
            cursor.moveToFirst();
            ContentValues values = new ContentValues();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            cursor.close();
            return values;

        }
        return null;
    }

    /**
     * Get the number of Zotero Items in a particular collection (or ALL if c is null)
     * @param c
     * @return
     */
    public int getNumRecordsCollection(Collection c){
        int result = 0;
        Cursor cursor = _db.rawQuery("select count(*) from \"" +
                _itemsTable.get_table_name() + "\";", null);
        if (c != null) {
            cursor = _db.rawQuery("select count(*) from \"" +
                    _collectionsTopsTable.get_table_name() + "\" where collection = \"" +
                    c.get_zotero_key() + "\";", null);
        }
        if (cursor != null) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
            cursor.close();
        }
        return result;
    }

    /**
     * Useful function that returns how many items we have in this search.
     * TODO - this could be slow, especially if we are using it in a 'has more' capacity?
     * @param group
     * @param collection
     * @param order
     * @return
     */
    public int getNumTops(Group group, Collection collection, OrderSearch order){
        return getTops(0,-1,group,collection,order).size();
    }

    /**
     * Get the current summary
     * @return
     */
    public computer.benjamin.zotdr0id.data.zotero.Summary getSummary(){
        return _summaryTable.getSummary(_db);
    }

    /**
     * Read a single particular row
     * @param tablename
     * @param rownumber
     * @return
     */
    private ContentValues readRow(String tablename, int rownumber) {
        int result = 0;
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("select * from \"" + tablename + "\";", null);
        cursor.moveToPosition(rownumber);

        for (int i = 0; i < cursor.getColumnCount(); i++){
            values.put(cursor.getColumnName(i),cursor.getString(i));
        }

        cursor.close();
        return values;
    }

    /**
     * read and return multiple rows
     * @param tablename
     * @return
     */
    private Vector<ContentValues> readRows(String tablename) {
        Vector<ContentValues> rvals = new Vector<ContentValues>();
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("select * from \"" + tablename + "\";", null);

        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            rvals.add(values);
        }

        cursor.close();
        return rvals;
    }

    /***********************************************************************************************
        Existence methods
     **********************************************************************************************/

    protected boolean exists(String table, String key){
        String q = "select count(*) from " + table + " where zotero_key=\"" + key + "\";";
        Cursor cursor = _db.rawQuery(q, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(0) != 0) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public boolean itemExists(Item r) {
        return exists(_itemsTable.get_table_name(), r.get_zotero_key());
    }

    public boolean topExists(Top t) {
        return exists(_itemsTable.get_table_name(), t.get_zotero_key()) ||
                exists(_notesTable.get_table_name(), t.get_zotero_key());
    }

    public boolean collectionExists(Collection c) {
        return exists(_collectionsTable.get_table_name(), c.get_zotero_key());
    }

    public boolean attachmentExists(Attachment a) {
        return exists(_attachmentsTable.get_table_name(), a.get_zotero_key());
    }

    public boolean noteExists(Note n) {
        return exists(_notesTable.get_table_name(), n.get_zotero_key());
    }

    public boolean subNoteExists(SubNote n) {
        return exists(_subnotesTable.get_table_name(), n.get_zotero_key());
    }
    public boolean groupExists(Group g) {
        return exists(_groupsTable.get_table_name(), g.get_zotero_key());
    }

    // Based on a key string - convinience methods essentially

    public boolean itemExists(String key) {
        return exists(_itemsTable.get_table_name(), key);
    }

    public boolean topExists(String key) {
        return exists(_itemsTable.get_table_name(), key) ||
                exists(_notesTable.get_table_name(), key);
    }

    public boolean collectionExists(String key) {
        return exists(_collectionsTable.get_table_name(), key);
    }

    public boolean attachmentExists(String key) {
        return exists(_attachmentsTable.get_table_name(), key);
    }

    public boolean noteExists(String key) {
        return exists(_notesTable.get_table_name(), key);
    }

    public boolean subNoteExists(String key) {
        return exists(_subnotesTable.get_table_name(), key);
    }
    public boolean groupExists(String key) {
        return exists(_groupsTable.get_table_name(), key);
    }

    /***********************************************************************************************
     Update methods
     **********************************************************************************************/

    public void updateCollection(Collection collection) {
        _collectionsTable.updateCollection(collection,_db);
    }

    // Items also need to update authors and tags and all the rest
    // Easiest way to do this is to delete everything related and re-write (with the exception
    // of attachments which are themselves separate
    public void updateItem(Item item) {
        deleteRecord(item);
        writeItem(item);
        // Now update the CollectionsTops
        // TODO - I think at somepoint we need to do this a different way - roll it into writeRecord
        // and keep a permanent vector in Item of which collections this is part of
        for (String ts : item.get_temp_collections()){
            CollectionTop ci = new CollectionTop();
            ci.set_item(item.get_zotero_key());
            ci.set_collection(ts);
            writeCollectionTop(ci);
        }
        item.get_temp_collections().clear();
    }

    public void updateAttachment(Attachment attachment) {
        _attachmentsTable.updateAttachment(attachment,_db);
    }

    public void updateSubNote(SubNote subNote){ _subnotesTable.updateNote(subNote,_db);}

    public void updateNote(Note note){ _notesTable.updateNote(note,_db);}

    public void updateGroup(Group group){ _groupsTable.updateGroup(group,_db);}

    public void updateTop(Top top) {
        // Naughty test casting here!
        if (top.get_tag().contains("Item")){
            updateItem((Item)top);
        } else if (top.get_tag().contains("Note")){
            updateNote((Note)top);
        }

    }

    /***********************************************************************************************
     Simple get methods
     **********************************************************************************************/

    public Collection getCollection(String key) {
        if (exists(_collectionsTable.get_table_name(), key)) {
            ContentValues values = getSingle(_db, _collectionsTable.get_table_name(), key);
            return _collectionsTable.getCollectionFromValues(values);
        }
        return null;
    }

    public Collection getCollection(int rownum) {
        return _collectionsTable.getCollectionFromValues(
                readRow(_collectionsTable.get_table_name(),rownum));
    }

    public Attachment getAttachment(String key) {
        if (exists( _attachmentsTable.get_table_name(), key)) {
            ContentValues values = getSingle(_db, _attachmentsTable.get_table_name(), key);
            return _attachmentsTable.getAttachmentFromValues(values);
        }
        return null;
    }

    public Vector<Attachment> getAttachments(){
        Vector<Attachment> attachments = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("select * from \"" + _attachmentsTable.get_table_name() + "\";", null);
        while (cursor.moveToNext()){
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++){
                values.put(cursor.getColumnName(i),cursor.getString(i));
            }
            attachments.add(_attachmentsTable.getAttachmentFromValues(values));
        }

        cursor.close();
        return attachments;
    }

    /**
     * Given a filename, return the attachments that match. Could be more than one I guess.s
     * @param filename
     * @return
     */
    public Vector<Attachment> getAttachmentsFilename(String filename) {
        Vector<Attachment> zv = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("SELECT * FROM " + _attachmentsTable.get_table_name()
                + " WHERE file_name=\"" + filename + "\";", null);

        while (cursor.moveToNext()) {
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            zv.add(_attachmentsTable.getAttachmentFromValues(values));
        }
        cursor.close();
        return zv;
    }

    /*
    public Vector<Attachment> getAttachments() {
        return _attachmentsTable.getAttachments(_db);
    }*/

    public SubNote getSubNote(String key) {
        if (exists(_subnotesTable.get_table_name(), key)) {
            ContentValues values = getSingle(_db, _subnotesTable.get_table_name(), key);
            return _subnotesTable.getSubNoteFromValues(values);
        }
        return null;
    }

    public Note getTopLevelNote(String key) {
        ContentValues values = getSingle(_db, _notesTable.get_table_name(), key);
        if (values != null){
            return _notesTable.getNoteFromValues(values);
        }
        return null;
    }

    public Group getGroup(String key) {
        if (exists(_groupsTable.get_table_name(), key)) {
            ContentValues values = getSingle(_db, _groupsTable.get_table_name(), key);
            return _groupsTable.getGroupFromValues(values);
        }
        return null;
    }

    public Vector<Group> getGroups() {
        Vector<Group> groups = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("select * from \"" + _groupsTable.get_table_name() +
                "\"" + ";", null);
        while (cursor.moveToNext()){
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++){
                values.put(cursor.getColumnName(i),cursor.getString(i));
            }

            Group a = _groupsTable.getGroupFromValues(values);
            groups.add(a);
        }
        cursor.close();
        return groups;
    }

    //public  Vector<Attachment> getAttachmentsForRecord(Item item) {
    //     return _attachmentsTable.getForRecord(_db, item.get_zotero_key());
    //}

    public Vector<CollectionTop> getCollectionItems() {
        Vector<CollectionTop> items = new Vector<>();
        for (ContentValues values : readRows(_collectionsTopsTable.get_table_name())){
            items.add(CollectionsTops.getCollectionItemFromValues(values));
        }
        return items;
    }

    /**
     * Get a partial item from a single table (no authors, notes etc) given it's zotero key
     * @param key
     * @return
     */
    public Item getItem(String key) {
        if (exists(_itemsTable.get_table_name(), key)) {
            return _itemsTable.getRecordFromValues(getSingle(_db, _itemsTable.get_table_name(), key));
        }
        return null;
    }

    // TODO - add protections around this one
    /*public Item getRecord(int rownumber) {
        Item r = _itemsTable.getRecordFromValues(
                readRow(_itemsTable.get_table_name(),rownumber));
        for(Author a : _authorsTable.getAuthorsForRecord(r,_db)){ r.add_author(a); }
        for(Tag t : _tagsTable.getTagsForRecord(r,_db)){ r.add_tag(t);}
        for(SubNote n : _subnotesTable.getNotesForRecord(r,_db)){ r.add_note(n);}
        return r;
    }*/

    /***********************************************************************************************
     Complex get methods
     **********************************************************************************************/

    /**
     * Useful utility function that allows us to query the DB for attachments with a given filename.
     * Not sure why we'd get more than one but you never know.
     * @param filename
     * @return
     */
     public Vector<Attachment> getAttachmentFromFilename(String filename) {
        Vector<Attachment> zv = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = _db.rawQuery("SELECT * FROM " + _attachmentsTable.get_table_name() +
                " WHERE file_name=\"" + filename + "\";", null);

        while (cursor.moveToNext()) {
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            zv.add(_attachmentsTable.getAttachmentFromValues(values));
        }
        cursor.close();
        return zv;
    }


    /**
     * Internal function to grab all the keys we want for our tops to show to the user
     * @param start
     * @param end
     * @param group
     * @param collection
     * @param order
     * @return
     */
    private Vector<String> _getKeys(int start, int end, Group group, Collection collection, OrderSearch order) {
        Vector<String> keys = new Vector<>();

        String _d = " ASC";
        if ( order != null) { if (order.get_reversed()) { _d = " DESC"; } }

        // Filter by collection
        // Assume a null collection means ALL (or basically, no filter by collection)
        String _c = "";
        String _c2 = "";
        String _c3 = "";
        if (collection != null){
            _c = " where ci.collection = '" + collection.get_zotero_key() + "'";
            _c2 = " inner join " + _collectionsTopsTable.get_table_name() +
                    " ci on ci.item = items.zotero_key";
            _c3 = " ci on ci.item = " +
                    _notesTable.get_table_name() + ".zotero_key";
        }

        // Filter by group
        String _g = "";
        String _g2 = "";
        if (group != null){
            // Tricky bit here. _g always follows _c but _c might be blank so we must check
            // TODO - Probably a better way to do this SQL query construct
            if (_c != ""){ _g = " and "; } else { _g = " where "; }
            _g2 = _g + _notesTable.get_table_name() +
                    ".group_key = '" + group.get_zotero_key() + "'";
            _g = _g + _itemsTable.get_table_name() +
                    ".group_key = '" + group.get_zotero_key() + "'";
        }

        // Offset?
        String _s = "";
        if (start > 0){
            _s = " offset " + String.valueOf(start);
        }

        // Limit?
        String _l = "";
        if (end >= 0){
            _l = " limit " + String.valueOf(end);
        }

        String _q = "";
        // Order by Author - no notes will be added
        // Order by dates and title will bring in notes as well
        // We need to pull in things like title and date_added so we can sort by it
        if (order.get_order() == OrderSearch.Order.AUTHOR) {
            _q = "select distinct(" + _itemsTable.get_table_name() + ".zotero_key), author from " +
                    _itemsTable.get_table_name() + " inner join " + _authorsTable.get_table_name() +
                    " a on a.record_key = items.zotero_key" + _c2 +
                    _c + _g + " order by a.author " + _d + _l + _s +";";
        } else if (order.get_order() == OrderSearch.Order.DATE_MODIFIED) {
            _q = "select " + _itemsTable.get_table_name() + ".zotero_key, date_modified from " +
                    _itemsTable.get_table_name() + _c2 +
                    _c + _g +
                    " union select zotero_key, date_modified from " + _notesTable.get_table_name() +
                    " inner join " +
                    _collectionsTopsTable.get_table_name() + _c3 +
                    _c + _g2 +
                    " order by date_modified" +
                    _d + _l + _s + ";";
        } else if (order.get_order() == OrderSearch.Order.TITLE) {
            _q = "select distinct(" + _itemsTable.get_table_name() + ".zotero_key), title from " +
                    _itemsTable.get_table_name() + _c2 +
                    _c + _g  +
                    " union select zotero_key, note as title from " + _notesTable.get_table_name() +
                    " inner join " +
                    _collectionsTopsTable.get_table_name() + _c3 +
                    _c + _g2 +
                    " order by " + "title" +
                    _d + _l + _s + ";";
        } else {
            _q = "select distinct(" + _itemsTable.get_table_name() + ".zotero_key), date_added from " +
                    _itemsTable.get_table_name() + _c2 +
                    _c + _g +" union select zotero_key, date_added from " + _notesTable.get_table_name() +
                    " inner join " +
                    _collectionsTopsTable.get_table_name() + _c3 +
                    _c + _g2 +
                    " order by " + "date_added" +
                    _d + _l + _s + ";";
        }

        // Grab our item keys
        // TODO - we are using index numbers here which *could* be problematic later.
        Cursor cursor = _db.rawQuery(_q, null);
        while (cursor.moveToNext()){
            keys.add(cursor.getString(0));
        }
        cursor.close();

        return keys;
    }

    /**
     *  Major function that returns a set of items based on current collection, search order
     *  and a limit with the end number. We return both notes and items in the combined, correct
     *  order.
     *  End can be -1 which means unlimited.
     *  Collection can be 'null' which means ignore
     *  TODO - Add a callback or similar here as this operation could take a while and we should
     *  inform the user.
     */
    public Vector<Top> getTops(int start, int end, Group group, Collection collection, OrderSearch order) {
        Vector<Top> tops = new Vector<>();
        // Make sure our offsets and limits make sense.
        if (start < 0) { start = 0;}
        if (end < 0) { end = -1; }
        if (start < end && end !=-1) { start = 0; }

        // We loop around until we've met the number requested or there are no more to get
        // If we pass in 0 for start and -1 as end we'll get a number bigger than -1 so we go
        // through this loop just once getting everything.
        do {
            Vector<String> keys = _getKeys(start, end, group, collection, order);
            if (keys.size() == 0){
                // No more keys so quit
                break;
            }

            String _q;
            // Now we have our keys, we can make our items proper from the other items
            // Note that keys can be either items or notes
            for (String key : keys) {
                // Authors first
                Vector<Author> authors = new Vector<>();
                _q = "select * from " +
                        _authorsTable.get_table_name() + " ar " +
                        "where ar.record_key='" + key + "' " +
                        "order by ar.author;";
                Cursor cursor = _db.rawQuery(_q, null);

                while (cursor.moveToNext()) {
                    authors.add(new Author(cursor.getString(1), cursor.getString(0)));
                }
                cursor.close();

                // SubNotes
                Vector<SubNote> notes = new Vector<>();
                _q = "select * from " +
                        _subnotesTable.get_table_name() + " ar " +
                        "where ar.record_key='" + key + "';";
                cursor = _db.rawQuery(_q, null);

                while (cursor.moveToNext()) {
                    notes.add(new SubNote(cursor.getString(2),
                            cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(3)));
                }
                cursor.close();


                // Tags (can we have a tag on a note?)
                Vector<Tag> tags = new Vector<>();
                _q = "select * from " +
                        _tagsTable.get_table_name() + " ar " +
                        "where ar.record_key='" + key + "';";
                cursor = _db.rawQuery(_q, null);

                while (cursor.moveToNext()) {
                    tags.add(new Tag(cursor.getString(1),
                            cursor.getString(0)));
                }
                cursor.close();


                // Attachments
                Vector<Attachment> atts = new Vector<>();
                _q = "select * from " +
                        _attachmentsTable.get_table_name() + " ar " +
                        "where ar.parent ='" + key + "';";
                cursor = _db.rawQuery(_q, null);

                while (cursor.moveToNext()) {
                    atts.add(new Attachment(cursor.getString(1),
                            cursor.getString(0),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6),
                            Util.db_string_to_bool(cursor.getString(7)),
                            cursor.getString(8)));
                }
                cursor.close();

                Top new_top;

                if (itemExists(key)) {
                    _q = "select * from " +
                            _itemsTable.get_table_name() + " ar " +
                            "where ar.zotero_key='" + key + "';";
                    cursor = _db.rawQuery(_q, null);

                    if (cursor != null) {
                        cursor.moveToNext();

                        // Now create the item
                        Item item = new Item(key);

                        for (SubNote s : notes) {
                            item.add_note(s);
                        }
                        for (Attachment a : atts) {
                            item.add_attachment(a);
                        }
                        for (Tag t : tags) {
                            item.add_tag(t);
                        }
                        for (Author a : authors) {
                            item.add_author(a);
                        }

                        // TODO - we should possibly do something nicer here one day
                        String date_added = cursor.getString(0);
                        String date_modified = cursor.getString(1);
                        String content_type = cursor.getString(2);
                        String item_type = cursor.getString(3);
                        String title = cursor.getString(4);
                        String abst = cursor.getString(5);
                        String zotero_key = cursor.getString(6);
                        String parent = cursor.getString(7);
                        String group_key = cursor.getString(8);
                        String extra = cursor.getString(9);
                        String issn = cursor.getString(10);
                        String language = cursor.getString(11);
                        String publication = cursor.getString(12);
                        String pages = cursor.getString(13);
                        String date = cursor.getString(14);
                        String version = cursor.getString(15);
                        boolean synced = Util.db_string_to_bool(cursor.getString(16));

                        item.set_abstract(abst);
                        item.set_content_type(content_type);
                        item.set_date(date);
                        item.set_date_added(date_added);
                        item.set_date_modified(date_modified);
                        item.set_extra(extra);
                        item.set_group_key(group_key);
                        item.set_issn(issn);
                        item.set_title(title);
                        item.set_zotero_key(zotero_key);
                        item.set_parent(parent);
                        item.set_language(language);
                        item.set_publication(publication);
                        item.set_pages(pages);
                        item.set_version(version);
                        item.set_synced(synced);
                        new_top = item;
                        if (order.filter(new_top)){
                            tops.add(new_top);
                        }
                    }
                } else if (noteExists(key)) {
                    // It's a Note - least we hope so!
                    _q = "select * from " +
                            _notesTable.get_table_name() + " ar " +
                            "where ar.zotero_key='" + key + "';";
                    cursor = _db.rawQuery(_q, null);
                    // Should always be one but you never know!
                    cursor.moveToNext();
                    Note note = new Note(key);

                    String date_added = cursor.getString(0);
                    String date_modified = cursor.getString(1);
                    String ntxt = cursor.getString(2);
                    String version = cursor.getString(3);
                    String group_key = cursor.getString(4);
                    String zotero_key = cursor.getString(5);

                    note.set_date_added(date_added);
                    note.set_date_modified(date_modified);
                    note.set_note(ntxt);
                    note.set_version(version);
                    note.set_group_key(group_key);
                    note.set_zotero_key(zotero_key);
                    new_top = note;
                    if (order.filter(new_top)){
                        tops.add(new_top);
                    }
                } else {
                    // TODO - couldnt find it - bit of an error we need to catch
                }


                cursor.close();

            }

        } while (tops.size() < end - start);
        return tops;
    }


    /***********************************************************************************************
     Counts from tables methods
     **********************************************************************************************/

    public int getNumRecords() { return getNumRows(_itemsTable.get_table_name()); }
    //public int getNumAttachments() { return getNumRows(_attachmentsTable.get_table_name()); }
    public int getNumCollections () { return getNumRows(_collectionsTable.get_table_name()); }
    public int getNumCollectionsItems () { return getNumRows(_collectionsTopsTable.get_table_name()); }
    //public int getNumNotes () { return getNumRows(_Sub_notesTable.get_table_name()); }
    //public int getNumGroups () { return getNumRows(_groupsTable.get_table_name()); }

    /***********************************************************************************************
     Write methods
     **********************************************************************************************/

    public void writeCollection(Collection collection) {
        ContentValues values = _collectionsTable.getValues(collection);
        _db.insert(_collectionsTable.get_table_name(), null, values);
    }

    public void writeAuthor(Author author) {
        ContentValues values = _authorsTable.getValues(author);
        _db.insert(_authorsTable.get_table_name(), null, values);
    }

    public void writeAttachment(Attachment attachment){
        ContentValues values = _attachmentsTable.getValues(attachment);
        _db.insert(_attachmentsTable.get_table_name(), null, values);
    }

    public void writeSummary(computer.benjamin.zotdr0id.data.zotero.Summary summary) {
        _summaryTable.writeSummary(summary,_db);
    }

    public void writeCollectionTop(CollectionTop ic){
        ContentValues values = _collectionsTopsTable.getValues(ic);
        _db.insert(_collectionsTopsTable.get_table_name(), null, values);
    }

    public void writeSubNote(SubNote subnote) {
        ContentValues values = _subnotesTable.getValues(subnote);
        _db.insert(_subnotesTable.get_table_name(), null, values);
    }

    public void writeNote(Note note) {
        ContentValues values = _notesTable.getValues(note);
        _db.insert(_notesTable.get_table_name(), null, values);
    }

    public void writeTag(Tag tag) {
        ContentValues values = _tagsTable.getValues(tag);
        _db.insert(_tagsTable.get_table_name(), null, values);
    }

    public void writeGroup(Group group) {
        ContentValues values = _groupsTable.getValues(group);
        _db.insert(_groupsTable.get_table_name(), null, values);
    }

    // Composite write methods
    public void writeItem(Item item){
        ContentValues values = _itemsTable.getValues(item);
        _db.insert(_itemsTable.get_table_name(), null, values);
        for (Author a : item.get_authors()) { writeAuthor(a); }
        for(Tag t : item.get_tags()) { writeTag(t); }
        for(SubNote n : item.get_sub_notes()) { writeSubNote(n); }
    }

    /**
     * Given a top item, decide which table we write to.
     * @param top
     */
    public void writeTop(Top top){
        // TODO - Some naughty casting going on here. Might be cleaner to add get_authors and
        // similar to the Top as empty funcs
        if (top.get_tag().contains("Item")){
            writeItem((Item)top);
        } else if (top.get_tag().contains("Note")){
            writeNote((Note)top);
        }
    }

    /***********************************************************************************************
     Delete methods
     **********************************************************************************************/

    public void deleteAttachment(Attachment a) {
        _db.execSQL("DELETE FROM " + _attachmentsTable.get_table_name() +
                " WHERE zotero_key=\"" + a.get_zotero_key() + "\";");
    }

    public void deleteRecord(Item r) {
        _db.execSQL("DELETE FROM " + _itemsTable.get_table_name() +
                " WHERE zotero_key=\"" + r.get_zotero_key() + "\";");

        _db.execSQL("DELETE FROM " + _collectionsTopsTable.get_table_name() +
                " where item=\"" + r.get_zotero_key() + "\"");

        _db.execSQL("DELETE FROM " + _authorsTable.get_table_name() +
                " where record_key=\"" + r.get_zotero_key() + "\"");

        _db.execSQL("DELETE FROM " + _tagsTable.get_table_name() +
                " where record_key=\"" + r.get_zotero_key() + "\"");

        _db.execSQL("DELETE FROM " + _subnotesTable.get_table_name() +
                " where record_key=\"" + r.get_zotero_key() + "\"");
    }

    public void deleteCollection(Collection c) {
        _db.execSQL("DELETE FROM " + _collectionsTable.get_table_name() +
                " WHERE zotero_key=\"" + c.get_zotero_key() + "\";");

        _db.execSQL("DELETE FROM " + _collectionsTopsTable.get_table_name() +
                " where collection=\"" + c.get_zotero_key() + "\"");
    }

    public void deleteTopLevelNote(Note note) {
        _db.execSQL("DELETE FROM " + _notesTable.get_table_name() +
                " WHERE zotero_key=\"" + note.get_zotero_key() + "\";");
    }

    public void deleteSubNote(SubNote note) {
        _db.execSQL("DELETE FROM " + _subnotesTable.get_table_name() +
                " WHERE zotero_key=\"" + note.get_zotero_key() + "\";");
    }
    public void removeTopFromCollections(Top top){
        _db.execSQL("DELETE FROM " + _collectionsTopsTable.get_table_name() +
                " where collection=\"" + top.get_zotero_key() + "\"");
    }

}
