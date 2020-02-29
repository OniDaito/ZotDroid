package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Group;

/**
 * Created by oni on 01/01/2018.
 */

public class Groups {

    protected static final String TABLE_NAME = "groups";

    protected final String TAG= "Groups";

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_COLLECTIONS = "CREATE TABLE \"" + TABLE_NAME + "\" ( " +
                "\"zotero_key\" VARCHAR PRIMARY KEY, \"title\" VARCHAR, \"version\" VARCHAR )";
        db.execSQL(CREATE_TABLE_COLLECTIONS);
    }

    public ContentValues getValues(Group group) {
        ContentValues values = new ContentValues();
        values.put("zotero_key", group.get_zotero_key());
        values.put("version", group.get_version());
        values.put("title", group.get_title());
        return values;
    }

    public Group getGroupFromValues(ContentValues values) {
        return new Group((String)values.get("zotero_key"), (String) values.get("title"), (String)values.get("version"));
    }

    public String get_table_name(){
        return TABLE_NAME;
    }

    public void updateGroup(Group group, SQLiteDatabase db) {
        String statement = "UPDATE " + get_table_name() +
                " SET title=\"" + Util.sanitise(group.get_title()) + "\", " +
                "version=\"" + group.get_version() + "\" " +
                "WHERE zotero_key=\"" + group.get_zotero_key() + "\";";
        db.execSQL(statement);
    }

}
