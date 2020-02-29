package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Collection;

/**
 * Created by oni on 11/07/2017.
 */

public class Collections {

    protected static final String TABLE_NAME = "collections";
    protected final String TAG = "Collections";

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_COLLECTIONS = "CREATE TABLE \"" +TABLE_NAME + "\" ( \"group_key\" VARCHAR, \"title\" TEXT, " +
                "\"zotero_key\" VARCHAR PRIMARY KEY, \"parent\" VARCHAR, \"version\" VARCHAR)";
        db.execSQL(CREATE_TABLE_COLLECTIONS);
    }


    public ContentValues getValues(Collection collection) {
        ContentValues values = new ContentValues();
        values.put("zotero_key", collection.get_zotero_key());
        values.put("title", collection.get_title());
        values.put("parent",collection.get_parent_key());
        values.put("version",collection.get_version());
        values.put("group_key",collection.get_group_key());
        return values;
    }


    public Collection getCollectionFromValues(ContentValues values) {
        Collection collection = new Collection();
        collection.set_title((String)values.get("title"));
        collection.set_zotero_key((String)values.get("zotero_key"));
        collection.set_parent_key((String)values.get("parent"));
        collection.set_version((String)values.get("version"));
        collection.set_group_key((String)values.get("group_key"));
        return collection;
    }

    public String get_table_name(){
        return TABLE_NAME;
    }

    public void updateCollection(Collection collection, SQLiteDatabase db) {
        String statement = "UPDATE " + get_table_name() +
                " SET title=\"" + Util.sanitise(collection.get_title()) + "\", " +
                "parent=\"" + collection.get_parent_key() + "\", " +
                "version=\"" + collection.get_version() + "\", " +
                "group_key=\"" + collection.get_group_key() + "\" " +
                "WHERE zotero_key=\"" + collection.get_zotero_key() + "\";";
        db.execSQL(statement);
    }

}
