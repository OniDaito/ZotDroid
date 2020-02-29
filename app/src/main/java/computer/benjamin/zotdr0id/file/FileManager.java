package computer.benjamin.zotdr0id.file;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.data.zotero.Attachment;

/**
 * Created by oni on 06/04/2018.
 */

public class FileManager {

    public static boolean intialise(){
        // Download directory for syncing - external normally
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
        String base_path = settings.getString("settings_download_location", "");

        if (base_path.equals("")) {
            if (isExternalStorageReadable() && isExternalStorageWritable()) {
                base_path = initBasePath();
                if (testDir(base_path)) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("settings_download_location", base_path);
                    editor.apply();
                    create_sub_dirs(base_path);
                    return true;
                }
            }
        }
        else {
            // Already set to default or user has set it
            if (testDir(base_path)) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("settings_download_location", base_path);
                editor.apply();
                create_sub_dirs(base_path);
                return true;
            }
        }

        // Make a final ditch attempt to find a good directory - could be on an older device
        Vector<String> paths = new Vector<>();
        paths.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + "ZotDroid");
        paths.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ZotDroid");

        // Some older Android versions might require the stuff below
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            paths.add(ZotDroidApp.getContext().getFilesDir() + "/" + "ZotDroid");
            File root_dir = new File(ZotDroidApp.getContext().getExternalCacheDir(), "ZotDroid");
            paths.add(root_dir.getAbsolutePath());
            root_dir = new File(ZotDroidApp.getContext().getExternalCacheDir(), "ZotDroid");
            String download_path = root_dir.getAbsolutePath();
            paths.add(download_path);
        }

        for (String path : paths) {
            if (testDir(path)){
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("settings_download_location", path);
                editor.apply();
                create_sub_dirs(path);
                return true;
            }
        }

        // We've failed :/

        // Fire up the warning dialog
        launchWarningDialog(base_path);
        return false;
    }

    public static boolean setDownloadDirectory( String path) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
        if (testDir(path)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("settings_download_location", path);
            editor.apply();
            create_sub_dirs(path);
            return true;
        }
        // TODO - this really should be somewhere else
        // launchWarningDialog(context,path);
        return false;
    }

    /**
     * Given our webdav zotero attachment .zip file, unzip it inside the temp directory and return
     * the path to it, so we can read it.
     */
    public static String unzipFile(String input_file, String output_file) {
        String input_path = getTempDirectory();
        String output_path = getAttachmentsDirectory();

        File infile = new File(input_path + "/"  + input_file);
        File outfile = new File(output_path + "/" + output_file);
        if (outfile.exists()) { outfile.delete(); }

        // Now do the reading but save to a file
        byte[] bytes = new byte[1024]; // read in 1024 chunks

        BufferedInputStream buf = null;
        try {
            buf = new BufferedInputStream(new FileInputStream(infile));
            FileOutputStream out = new FileOutputStream(outfile);
            ZipInputStream zin = new ZipInputStream (buf);
            zin.getNextEntry();

            int bytes_read;
            while ((bytes_read = zin.read(bytes, 0, bytes.length)) > 0) {
                out.write(bytes, 0, bytes_read);
            }

            out.flush();
            out.close();
            zin.close();
            buf.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output_path + "/" + output_file;
    }


    /**
     * Given our webdav zotero attachment file, zip it inside the temp directory and return
     * the path to it, so we can post it.
     */
    public static String zipFile( String input_file, String output_file) {
        String output_path = getTempDirectory();
        String input_path = getAttachmentsDirectory();

        File infile = new File(input_path + "/"  + input_file);
        File outfile = new File(output_path + "/" + output_file);
        if (outfile.exists()) { outfile.delete(); }

        // Now do the reading but save to a file
        byte[] bytes = new byte[1024]; // read in 1024 chunks

        BufferedInputStream buf = null;
        try {
            FileOutputStream out = new FileOutputStream(outfile);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            ZipOutputStream zout = new ZipOutputStream(bos);
            try {
                FileInputStream fin = new FileInputStream(infile);
                buf = new BufferedInputStream(fin);

                zout.putNextEntry(new ZipEntry(input_file));

                int bytes_read;
                while ((bytes_read = buf.read(bytes, 0, bytes.length)) > 0) {
                    zout.write(bytes, 0, bytes_read);
                }
                zout.closeEntry();

                out.flush();
                //out.close();
                zout.close();
                buf.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        catch (FileNotFoundException e) {
                e.printStackTrace();
        }

        return output_path + "/" + output_file;
    }

    public static Vector<String> getAllFilenames(String path){
        Vector<String> results = new Vector<>();
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            results.add(files[i].getName());
        }
        return results;
    }

    public static long getAttachmentSize(Attachment attachment){
        String path = getAttachmentsDirectory() + "/" +
                attachment.get_file_name();
        if (attachmentExists(attachment)){
            File file = new File(path);
            return file.length();
        }
        return 0;
    }

    public static Date getAttachmentLastModified(Attachment attachment){
        String path = getAttachmentsDirectory() + "/" +
                attachment.get_file_name();
        if (attachmentExists(attachment)){
            File file = new File(path);
            return new Date(file.lastModified());
        }
        return null;
    }

    private static String initBasePath(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        return file.getAbsolutePath() + "/"  + "ZotDroid";
    }

    private static void create_sub_dirs(String base_path) {
        if (!Util.path_exists(base_path + "/" + "attachments")){
            Util.create_path(base_path + "/" + "attachments");
        }
        if (!Util.path_exists(base_path + "/" + "temp")){
            Util.create_path(base_path + "/" + "temp");
        }
    }

    public static boolean attachmentExists(Attachment attachment) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
        Boolean webdav = settings.getBoolean("settings_use_webdav_storage", false);
        if (webdav) {
            //boolean zipexists = Util.path_exists(getAttachmentsDirectory(activity) + File.separator + attachment.get_zotero_key() + ".zip");
            boolean realexists =  Util.path_exists(getAttachmentsDirectory() + "/" + attachment.get_file_name());
            return realexists;
        }
        return Util.path_exists(getAttachmentsDirectory() + "/" + attachment.get_file_name());
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Test the directory we have, making it and the temp dir if we need to
     */
    public static boolean testDir(String path){
        File root_dir = new File(path);
        if (!root_dir.exists()){
            if(!root_dir.mkdirs()){
                // We have a problem!
                return false;
            }
        }
        // TODO - this test does not work and I've no idea why :S
        /*root_dir.setReadable(true,false);
        if (root_dir.canWrite() && root_dir.canRead()) {
            return true;
        }*/
        return  true;
    }


    /**
     * Get the download directory we are using
     */

    public static String getAttachmentsDirectory() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
        String download_path = settings.getString("settings_download_location", "");
        return download_path + "/" +  "attachments";
    }

    public static String getTempDirectory() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
        String download_path = settings.getString("settings_download_location", "");
        return download_path + "/" + "temp";
    }

    public static Vector<String> getAttachmentFiles(Attachment attachment){
        Vector<String> paths = new Vector<>();
        String d0 = getAttachmentsDirectory() + "/" + attachment.get_file_name() ;
        String d1 = getTempDirectory() + "/" + attachment.get_zotero_key()  + ".zip";
        String d2 = getAttachmentsDirectory() + "/" + attachment.get_file_name();
        if ( Util.path_exists(d0)) {paths.add(d0); }
        if ( Util.path_exists(d1)) {paths.add(d1); }
        if ( Util.path_exists(d2)) {paths.add(d2); }
        return paths;
    }

    /**
     * Delete all the files for an attachment
     */
    public static void deleteAttachment(Attachment attachment){
        if (attachmentExists(attachment)) {
            Vector<String> vs = getAttachmentFiles(attachment);
            for (String ss : vs) {
                File file = new File(ss);
                file.delete();
            }
        }
    }

    // TODO - Move this to dialog class
    private static void launchWarningDialog(String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ZotDroidApp.getContext(),
                R.style.ZotDroidAlertDialogStyle);
        builder.setTitle(R.string.settings_download_location)
        .setMessage(R.string.settings_download_message + ": " + path)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
    }
}
