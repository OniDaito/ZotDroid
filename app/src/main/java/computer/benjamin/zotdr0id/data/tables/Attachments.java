package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Attachment;

/**
 * Created by oni on 14/07/2017.
 */

public class Attachments  {
    public static final String TAG = "zotdroid.data.Attachments";
    protected static final String TABLE_NAME = "attachments";
    public String get_table_name(){  return TABLE_NAME; }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_ATTACHMENTS = "CREATE TABLE \"" +TABLE_NAME + "\" ( \"file_type\" VARCHAR, \"file_name\" TEXT," +
                "\"zotero_key\" VARCHAR PRIMARY KEY, \"parent\" VARCHAR, " +
                "\"version\" VARCHAR, \"md5\" VARCHAR, " +  "\"date_modified\" DATETIME, " +
                "\"synced\" INTEGER," + "\"date_added\" DATETIME)";

        db.execSQL(CREATE_TABLE_ATTACHMENTS);
    }

    public static ContentValues getValues (Attachment attachment) {
        ContentValues values = new ContentValues();
        values.put("zotero_key", attachment.get_zotero_key());
        values.put("file_type", attachment.get_file_type());
        values.put("file_name", Util.sanitise(attachment.get_file_name()));
        values.put("parent", attachment.get_parent_key());
        values.put("md5", attachment.get_md5());
        values.put("synced", (attachment.is_synced()) ? 1 : 0);
        values.put("version", attachment.get_version());
        values.put("date_added", Util.sanitise(attachment.get_date_added()));
        values.put("date_modified", Util.sanitise(attachment.get_date_modified()));
        return values;
    }

    public static Attachment getAttachmentFromValues(ContentValues values) {
        Attachment attachment = new Attachment();
        attachment.set_zotero_key((String)values.get("zotero_key"));
        attachment.set_date_added((String)values.get("date_added"));
        attachment.set_date_modified((String)values.get("date_modified"));
        attachment.set_file_type((String)values.get("file_type"));
        attachment.set_file_name((String)values.get("file_name"));
        attachment.set_md5((String)values.get("md5"));
        attachment.set_parent_key((String)values.get("parent"));
        attachment.set_version((String)values.get("version"));
        int ts = Integer.valueOf((String)(values.get("synced")));
        if (ts != 1) { attachment.set_synced(false); }
        return attachment;
    }

    /*private boolean attachmentExists(String key, SQLiteDatabase db){
        return exists(get_table_name(), key, db);
    }

    public Attachment getAttachmentByKey(String key, SQLiteDatabase db){
        if (attachmentExists(key,db)) {
            return getAttachmentFromValues(getSingle(db, key));
        }
        return null;
    }

    public Vector<Attachment> getAttachments(SQLiteDatabase db){
        Vector<Attachment> attachments = new Vector<>();

        ContentValues values = new ContentValues();
        Cursor cursor = db.rawQuery("select * from \"" + get_table_name() + "\";", null);
        while (cursor.moveToNext()){
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++){
                values.put(cursor.getColumnName(i),cursor.getString(i));
            }
            attachments.add(getAttachmentFromValues(values));
        }

        cursor.close();
        return attachments;
    }*/

    public void updateAttachment(Attachment attachment, SQLiteDatabase db) {
        String statement = "UPDATE " + get_table_name() +
                " SET file_type=\"" + Util.sanitise(attachment.get_file_type()) + "\", " +
                "file_name=\"" + Util.sanitise(attachment.get_file_name()) + "\", " +
                "parent=\"" + attachment.get_parent_key() + "\", " +
                "version=\"" + attachment.get_version() + "\", " +
                "md5=\"" + attachment.get_md5() + "\", " +
                "date_modified =\"" + attachment.get_date_modified() + "\", " +
                "date_added =\"" + attachment.get_date_added() + "\", " +
                "version=\"" + attachment.get_version() + "\", " +
                "synced=\"" + ( attachment.is_synced() ? 1 : 0 ) + "\" " +
                "WHERE zotero_key=\"" + attachment.get_zotero_key() + "\";";

        db.execSQL(statement);
    }

    /*public Boolean attachmentExists(Attachment a, SQLiteDatabase db){
        return exists(get_table_name(),a.get_zotero_key(),db);
    }*/

    /*public void deleteAttachment(Attachment a, SQLiteDatabase db){
        db.execSQL("DELETE FROM " + get_table_name() + " WHERE zotero_key=\"" + a.get_zotero_key() + "\";");
    }

    public void writeAttachment (Attachment attachment, SQLiteDatabase db) {
        ContentValues values = getValues(attachment);
        db.insert(get_table_name(), null, values);
    }*/

    /*public Vector<Attachment> getForFilename(SQLiteDatabase db, String filename) {
        Vector<Attachment> zv = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = db.rawQuery("SELECT * FROM " + get_table_name() + " WHERE file_name=\"" + filename + "\";", null);

        while (cursor.moveToNext()) {
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            zv.add(getAttachmentFromValues(values));
        }
        cursor.close();
        return zv;
    }*/

    /**
     * Grrab all the attachments for a particular record - useful in pagination.
     */
    /*public Vector<Attachment> getForRecord(SQLiteDatabase db, String parent_key) {
        Vector<Attachment> zv = new Vector<>();
        ContentValues values = new ContentValues();
        Cursor cursor = db.rawQuery("SELECT * FROM " + get_table_name() + " WHERE parent=\"" + parent_key + "\";", null);

        while (cursor.moveToNext()) {
            values.clear();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                values.put(cursor.getColumnName(i), cursor.getString(i));
            }
            zv.add(getAttachmentFromValues(values));
        }
        cursor.close();
        return zv;
    }*/

}
