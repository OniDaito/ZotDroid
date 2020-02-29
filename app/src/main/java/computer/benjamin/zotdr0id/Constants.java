package computer.benjamin.zotdr0id;

/**
 * Created by oni on 03/11/2017.
 */

public class Constants {
    public static final int PAGINATION_SIZE = 50;
    public static final String BASE_URL = "https://api.zotero.org";
    public static final String BASE_DOWNLOAD_URL = "https://www.zotero.org";
    public static final int ATTACHMENT_FILENAME_POS = 11;
    public static final int ENTRY_SIZE = 6;
    public static final String LOCAL_GROUP = "LOCAL";
    public static final String TOP_COLLECTION = "ALL";

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 69;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 70;
    public static final int DATABASE_VERSION = 8;
    public static String DATABASE_NAME = "zotdroid.sqlite";
    public static int   ZOTERO_LOGIN_REQUEST = 1667;
    public static int   NOTE_LISTING_MAX = 10;
    public static int   ABSTRACT_LISTING_MAX = 500;
    public static int   INTENT_RESET_SYNC = 6667;
}
