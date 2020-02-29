package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Tag;

/**
 * Created by oni on 28/11/2017.
 */

public class Tags {

    public static final String TAG = "Tags";
    protected static final String TABLE_NAME = "tags";
    public String get_table_name() {
        return TABLE_NAME;
    }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_RECORDS = "CREATE TABLE \"" + TABLE_NAME + "\" (\"record_key\" VARCHAR, \"name\" VARCHAR)";
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    public ContentValues getValues(Tag tag) {
        ContentValues values = new ContentValues();
        values.put("record_key", Util.sanitise(tag.get_record_key()));
        values.put("name", Util.sanitise(tag.get_name()));
        return values;
    }

    public Tag getTagFromValues(ContentValues values) {
        Tag tag = new Tag("", "");
        tag.set_name((String) values.get("name"));
        tag.set_record_key((String) values.get("record_key"));
        return tag;
    }

}
