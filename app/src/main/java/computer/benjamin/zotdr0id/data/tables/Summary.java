package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import computer.benjamin.zotdr0id.Util;

/**
 * Created by oni on 11/07/2017.
 */

public class Summary {

    protected static final String TABLE_NAME = "summary";
    public String get_table_name(){
        return TABLE_NAME;
    }

    public final String TAG = "zotdroid.data.Summary";

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_SUMMARY = "CREATE TABLE \"" + TABLE_NAME + "\" (\"date_synced\" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "\"upgrade\" INTEGER, \"last_version\" VARCHAR)";
        db.execSQL(CREATE_TABLE_SUMMARY);
    }

    private ContentValues getValues (computer.benjamin.zotdr0id.data.zotero.Summary summary) {
        ContentValues values = new ContentValues();
        values.put("date_synced", Util.dateToDBString(summary.get_date_synced()));
        values.put("last_version", summary.get_last_version());
        values.put("upgrade", String.valueOf(summary.get_upgrade()));
        return values;
    }

    public computer.benjamin.zotdr0id.data.zotero.Summary getSummaryFromValues(ContentValues values) {
        computer.benjamin.zotdr0id.data.zotero.Summary summary = new computer.benjamin.zotdr0id.data.zotero.Summary();
        summary.set_date_synced( Util.dbStringToDate((String)values.get("date_synced")));
        summary.set_last_version((String)values.get("last_version"));
        String ts = (String)values.get("upgrade");
        // Older versions might have null which we don't want
        if (ts != null) {
            summary.set_upgrade(Integer.valueOf(ts));
        } else {
            summary.set_upgrade(0);
        }
        return summary;
    }

    public computer.benjamin.zotdr0id.data.zotero.Summary getSummary(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select * from \"" + this.get_table_name() + "\";", null);
        cursor.moveToFirst();
        ContentValues values = new ContentValues();

        // Assume inly one - should be only one
        for (int i = 0; i < cursor.getColumnCount(); i++){
            values.put(cursor.getColumnName(i),cursor.getString(i));
        }
        cursor.close();
        return getSummaryFromValues(values);
    }

    /**
     * Take a record and write it to the database
     */
    public void writeSummary(computer.benjamin.zotdr0id.data.zotero.Summary summary, SQLiteDatabase db) {
        clearSummary(db);
        ContentValues values = getValues(summary);
        db.insert(get_table_name(), null, values);
    }

    /**
     * Delete the summary
     */
    private void clearSummary(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + get_table_name());
    }
}
