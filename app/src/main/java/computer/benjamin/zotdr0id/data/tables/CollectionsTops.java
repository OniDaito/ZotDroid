package computer.benjamin.zotdr0id.data.tables;

/**
 * Created by oni on 26/07/2017.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.CollectionTop;

public class CollectionsTops {

    protected static final String TABLE_NAME = "collections_tops";

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_COLLECTIONS = "CREATE TABLE \"" +TABLE_NAME + "\" ( \"collection\" VARCHAR, \"item\" VARCHAR )";
        db.execSQL(CREATE_TABLE_COLLECTIONS);
    }

    public ContentValues getValues(CollectionTop ic) {
        ContentValues values = new ContentValues();
        values.put("item", Util.sanitise(ic.get_item()));
        values.put("collection", Util.sanitise(ic.get_collection()));
        return values;
    }

    public static CollectionTop getCollectionItemFromValues(ContentValues values) {
        CollectionTop ic = new CollectionTop();
        ic.set_collection((String)values.get("collection"));
        ic.set_item((String)values.get("item"));
        return ic;
    }

    public String get_table_name(){
        return TABLE_NAME;
    }

}
