package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.SubNote;

/**
 * Created by oni on 28/11/2017.
 */

public class SubNotes {

    public static final String TAG = "SubNotes";
    protected static final String TABLE_NAME = "SubNotes";
    public String get_table_name() {
        return TABLE_NAME;
    }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_RECORDS = "CREATE TABLE \"" + TABLE_NAME + "\" (\"record_key\" VARCHAR, \"note\" TEXT, \"version\" VARCHAR, \"zotero_key\" VARCHAR)";
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + get_table_name());
    }

    public ContentValues getValues(SubNote subNote) {
        ContentValues values = new ContentValues();
        values.put("record_key",  Util.sanitise(subNote.get_record_key()));
        values.put("subNote", Util.sanitise(subNote.get_note()));
        values.put("zotero_key", Util.sanitise(subNote.get_zotero_key()));
        values.put("version", Util.sanitise(subNote.get_version()));
        return values;
    }

    public SubNote getSubNoteFromValues(ContentValues values) {
        return new SubNote((String) values.get("zotero_key"),
                (String) values.get("record_key"),
                (String) values.get("note"),
                (String) values.get("version")
                );
    }

    // TODO - make sure this really works! - Write a test for it
    // Potentially surround with a try catch
    public void updateNote(SubNote subNote, SQLiteDatabase db){
        if (subNote != null) {
            String nn = Util.sanitise(subNote.get_note());
            String statement = "UPDATE " + get_table_name() +
                    " SET record_key =\"" + subNote.get_record_key() + "\", " +
                    "note =\"" + nn + "\", " +
                    "version =\"" + subNote.get_version() + "\" " +
                    "WHERE zotero_key=\"" + subNote.get_zotero_key() + "\";";

            db.execSQL(statement);
        }
    }


}
