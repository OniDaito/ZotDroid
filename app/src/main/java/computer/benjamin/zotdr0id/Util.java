package computer.benjamin.zotdr0id;

import android.util.Base64;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by oni on 14/07/2017.
 */

// W3C Date JSON Standard - https://www.w3.org/TR/NOTE-datetime
// https://stackoverflow.com/questions/2597083/illegal-pattern-character-t-when-parsing-a-date-string-to-java-util-date#25979660

public class Util {

    //public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    //public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ssZ";

    public static Date jsonStringToDate(String s) {
        // This apparently was supposed to work but really doesnt ><
        /*
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        // This effectively removes the 'T' that the DB complains dialog_about.
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();*/
        SimpleDateFormat date_format = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        try {
            s.replace('T',' ');
            return date_format.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();

    }

    public static String dateToDBString(Date d){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(d);
    }

    public static String dateDBStringNow(){
        SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        Date current_time = Calendar.getInstance().getTime();
        return dateFormat.format(current_time);
    }

    /**
     * Convert the database string to a java Date - this function actually takes forever so I've
     * decided not to bother with it as I doubt we will order by date yet.
     */
    public static Date dbStringToDate(String s) {
        // For some stupid Java reason we need to remove the T in our format
        SimpleDateFormat dateFormat = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static void copyDate(Date from, Date to){
        to.setDate(from.getDate());
        to.setTime(from.getTime());
    }

    public static boolean path_exists(String path) {
        File root_dir = new File(path);
        return root_dir.exists();
    }

    public static boolean create_path(String path) {
        File root_dir = new File(path);
        return root_dir.mkdirs();
    };

    public static String remove_trailing_slash(String path){
        if(path.endsWith("/")){
            path = path.substring(0,path.length()-1);
        }
        return path;
    }

    public static String getDirPath(String path) {
        return path.substring(0,path.lastIndexOf( File.separator));
    }

    public static String getFileFromPath(String path) {
        return path.substring(path.lastIndexOf( File.separator) + 1);
    }

    /**
     * Given two strings, return a proper basic auth string
     */
    public static String getB64Auth (String login, String pass) {
        String source=login+":"+pass;
        return "Basic "+ Base64.encodeToString(source.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);
    }

    public static String sanitise(String statement){
        if (statement != null) {
            // TODO - this DatabaseUtils doesnt work which sucks - No idea why?
            //statement = DatabaseUtils.sqlEscapeString(statement);
            statement = statement.replaceAll("'", Matcher.quoteReplacement("''"));
            statement = statement.replaceAll("\"", Matcher.quoteReplacement("\"\""));
        }
        return  statement;
    }

    public static boolean db_string_to_bool(String s) {
        if (s.equals("1")) { return true; }
        return false;
    }

}
