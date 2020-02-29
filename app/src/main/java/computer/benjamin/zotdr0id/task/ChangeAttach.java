package computer.benjamin.zotdr0id.task;

import java.io.File;
import java.util.Vector;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.file.MD5;
import computer.benjamin.zotdr0id.task.callback.FindChangeAttach;

/**
 * A class that looks at all attachments on the device, checks if they've changed and creates a list
 * of these changed files that need to be sent back up to Zotero. Most "tasks" are web aysnc related
 * but we also do this local one as an async as well, because we might have many files to check.
 *
 * We only re-upload if the md5 is different. The sync flag is used with the version number for the
 * data held with the zotero servers.
 */
public class ChangeAttach extends ZoteroTask {

    private static final String TAG = "Sizer";
    private FindChangeAttach _callback;
    private Vector<Attachment>          _changed_attachments;
    private Vector<String>              _changed_attachments_sums;
    private ZotDroidDB                  _db;

    public ChangeAttach(FindChangeAttach callback, ZotDroidDB db){
        // TODO - I don't quite like passing in the DB here :/ But it's either that or a load of
        // attachments in a vector which is no good either :/
        this._callback = callback;
        this._changed_attachments = new Vector<Attachment>();
        this._changed_attachments_sums = new Vector<>();
        this._db = db;
    }

    @Override
    public void startZoteroTask() {
        execute();
    }

    @Override
    protected String doInBackground(String... strings) {
        // Check all the files for MD5 changes
        Vector<String> names = FileManager.getAllFilenames(
                FileManager.getAttachmentsDirectory());

        for (String name : names){
            Vector<Attachment> attachments = _db.getAttachmentsFilename(name);
            for (Attachment attachment : attachments){
                // Check MD5 against DB
                String md5 = attachment.get_md5();

                try {
                    File cx_file = new File(FileManager.getAttachmentsDirectory() + "/" + name);
                    boolean result = MD5.checkMD5(md5, cx_file);
                    if (!result){
                        // We don't change the attachment here - that goes on later once we have
                        // started getting upload permissions and what not.
                        _changed_attachments_sums.add(MD5.calculateMD5(cx_file));
                        _changed_attachments.add(attachment);
                    }
                } catch (NullPointerException e){
                    // Pass - we can't get an MD5 score so no update
                }
            }
        }
        return "SUCCESS";
    }

    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        // TODO - Due to above this is always a success but should we stop if any attachment fails?
        if (rstring == "SUCCESS") {
            _callback.onFindChangeAttachComplete(new ZoteroResult(), _changed_attachments,
                _changed_attachments_sums);
        }
        else {
            _callback.onFindChangeAttachComplete(
                new ZoteroResult(ZoteroResult.ZotError.CHANGED_ATTACH_1), _changed_attachments,
                _changed_attachments_sums);
        }
    }
}
