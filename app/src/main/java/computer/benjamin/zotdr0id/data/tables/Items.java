package computer.benjamin.zotdr0id.data.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.data.zotero.Item;

/**
 * Created by oni on 11/07/2017.
 */

public class Items {

    public final String TAG = "Items";
    protected static final String TABLE_NAME = "items";
    public static String get_table_name(){
        return TABLE_NAME;
    }

    public void createTable(SQLiteDatabase db) {
        String CREATE_TABLE_RECORDS = "CREATE TABLE \"" +TABLE_NAME + "\" (\"date_added\" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "\"date_modified\" DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "\"content_type\" VARCHAR, \"item_type\" VARCHAR, \"title\" TEXT, \"abstract\" TEXT, " +
                "\"zotero_key\" VARCHAR PRIMARY KEY, \"parent\" VARCHAR, \"group_key\" VARCHAR, " +
                "\"extra\" TEXT, \"issn\" VARCHAR, \"language\" VARCHAR, \"publication\" VARCHAR, \"pages\" VARCHAR, " +
                "\"date\" DATETIME, " +
                " \"version\" VARCHAR, \"synced\" INTEGER)";
        db.execSQL(CREATE_TABLE_RECORDS);
    }

    public ContentValues getValues (Item item) {
        ContentValues values = new ContentValues();
        //values.put("date_added", Util.dateToDBString(item.get_date_added()));
        //values.put("date_modified", Util.dateToDBString(item.get_date_modified()));
        values.put("date_added", Util.sanitise(item.get_date_added()));
        values.put("date_modified", Util.sanitise(item.get_date_modified()));
        values.put("zotero_key", Util.sanitise(item.get_zotero_key()));
        values.put("content_type", Util.sanitise(item.get_content_type()));
        values.put("item_type", Util.sanitise(item.get_item_type()));
        values.put("title", Util.sanitise(item.get_title()));
        values.put("parent", Util.sanitise(item.get_parent()));
        values.put("abstract", Util.sanitise(item.get_abstract()));
        values.put("version", Util.sanitise(item.get_version()));
        values.put("synced", (item.is_synced()) ? 1 : 0);
        values.put("group_key", Util.sanitise(item.get_group_key()));
        values.put("issn", Util.sanitise(item.get_issn()));
        values.put("language", Util.sanitise(item.get_language()));
        values.put("extra", Util.sanitise(item.get_extra()));
        values.put("publication", Util.sanitise(item.get_publication()));
        values.put("pages", Util.sanitise(item.get_pages()));
        values.put("date", Util.sanitise(item.get_date()));
        return values;
    }

    public Item getRecordFromValues(ContentValues values) {
        Item item = new Item((String)values.get("zotero_key"));
        //item.set_date_added( Util.dbStringToDate((String)values.get("date_added")));
        //item.set_date_modified( Util.dbStringToDate((String)values.get("date_modified")));
        item.set_date_added((String)values.get("date_added"));
        item.set_date_modified((String)values.get("date_modified"));
        item.set_content_type((String)values.get("content_type"));
        item.set_item_type((String)values.get("item_type"));
        item.set_title((String)values.get("title"));
        item.set_abstract((String)values.get("abstract"));
        item.set_zotero_key((String)values.get("zotero_key"));
        item.set_parent((String)values.get("parent"));
        item.set_version((String)values.get("version"));
        item.set_group_key((String)values.get("group_key"));
        item.set_issn((String)values.get("issn"));
        item.set_language((String)values.get("language"));
        item.set_extra((String)values.get("extra"));
        item.set_publication((String)values.get("publication"));
        item.set_pages((String)values.get("pages"));
        item.set_date((String)values.get("date"));
        item.set_synced(true);
        int ts = Integer.valueOf((String)(values.get("synced")));
        if (ts != 1) { item.set_synced(false); }

        //item.set_synced(  Integer.valueOf((String)(values.get("synced"))) == 1 ? true : false);
        return item;
    }

    public void updateRecord(Item item, SQLiteDatabase db) {
        String statement = "UPDATE " + get_table_name() +
            //" SET date_added=\"" +  Util.dateToDBString(item.get_date_added()) + "\", " +
            //"date_modified=\"" +  Util.dateToDBString(item.get_date_modified()) + "\", " +
            " SET date_added=\"" +  item.get_date_added() + "\", " +
            "date_modified=\"" +  item.get_date_modified() + "\", " +
            "content_type=\"" + item.get_content_type() + "\", " +
            "item_type=\"" + item.get_item_type() + "\", " +
            "title=\"" + Util.sanitise(item.get_title()) + "\", " +
            "abstract=\"" + Util.sanitise(item.get_abstract()) + "\", " +
            "parent=\"" + item.get_parent() + "\", " +
            "group_key=\"" + item.get_group_key() + "\", " +
            "version=\"" + item.get_version() + "\", " +
            "synced=\"" + ( item.is_synced() ? 1 : 0 ) + "\" " +
            "WHERE zotero_key=\"" + item.get_zotero_key() + "\";";

        db.execSQL(statement);
    }

}
