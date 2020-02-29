package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Note;

/**
 * Created by oni on 07/02/2019.
 */

public class Notes {
    public final String TAG = "Notes";
    protected static final String TABLE_NAME = "Notes";
    public static String get_table_name() {
        return TABLE_NAME;
    }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_RECORDS = "CREATE TABLE \"" + TABLE_NAME +
                "\" ( \"date_added\" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "\"date_modified\" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "\"note\" TEXT, \"version\" VARCHAR, \"group_key\" VARCHAR, " +
                "\"zotero_key\" VARCHAR)";
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    public ContentValues getValues(Note note) {
        ContentValues values = new ContentValues();
        values.put("date_added", Util.sanitise(note.get_date_added()));
        values.put("date_modified", Util.sanitise(note.get_date_modified()));
        values.put("note", Util.sanitise(note.get_note()));
        values.put("zotero_key", Util.sanitise(note.get_zotero_key()));
        values.put("version", Util.sanitise(note.get_version()));
        values.put("group_key", Util.sanitise(note.get_group_key()));
        return values;
    }

    public Note getNoteFromValues(ContentValues values) {
        return new Note((String) values.get("zotero_key"),
                (String) values.get("date_added"),
                (String) values.get("date_modified"),
                (String) values.get("note"),
                (String) values.get("version"),
                (String) values.get("group_key")
                );
    }

    // TODO - make sure this really works! - Write a test for it
    // Potentially surround with a try catch
    public void updateNote(Note note, SQLiteDatabase db){
        if (note != null) {
            String nn = Util.sanitise(note.get_note());
            String statement = "UPDATE " + get_table_name() +
                    " SET date_added =\"" + note.get_date_added() + "\", " +
                    "date_modified =\"" + note.get_date_modified() + "\", " +
                    "note =\"" + nn + "\", " +
                    "version =\"" + note.get_version() + "\" " +
                    "WHERE zotero_key=\"" + note.get_zotero_key() + "\";";

            db.execSQL(statement);
        }
    }

}
