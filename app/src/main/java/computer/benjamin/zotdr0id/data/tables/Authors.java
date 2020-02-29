package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Author;
import computer.benjamin.zotdr0id.data.zotero.Item;

/**
 * Created by oni on 15/11/2017.
 */

public class Authors {

    public final String TAG = "Authors";

    protected static final String TABLE_NAME = "authors";

    public String get_table_name() {
        return TABLE_NAME;
    }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_RECORDS = "CREATE TABLE \"" + TABLE_NAME + "\" (\"record_key\" VARCHAR, \"author\" VARCHAR)";
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    public void deleteTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + get_table_name());
    }

    public ContentValues getValues(Author author) {
        ContentValues values = new ContentValues();
        values.put("record_key", Util.sanitise(author.get_record_key()));
        values.put("author", Util.sanitise(author.get_name()));
        return values;
    }


    /**
     * Convert our database ContentValues to an Author record
     * @param values
     * @return
     */
    public Author getAuthorFromValues(ContentValues values) {
        Author author = new Author("", "");
        author.set_name((String) values.get("author"));
        author.set_record_key((String) values.get("record_key"));
        return author;
    }


}
