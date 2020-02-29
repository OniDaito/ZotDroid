package computer.benjamin.zotdr0id.ops;

import android.util.Log;
import java.util.Vector;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Summary;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.task.Groups;
import computer.benjamin.zotdr0id.task.Topper;
import computer.benjamin.zotdr0id.task.ZoteroResult;
import computer.benjamin.zotdr0id.task.ZoteroTask;
import computer.benjamin.zotdr0id.task.callback.GroupTask;
import computer.benjamin.zotdr0id.task.callback.Tops;
import computer.benjamin.zotdr0id.task.callback.Upgraded;

import static computer.benjamin.zotdr0id.ops.Sync.TAG;

/**
 * Created by oni on 13/03/2018.
 * This class performs updates as necessary if the database has been upgraded.
 */

public class Upgrade extends Ops implements GroupTask, Tops {

    private Upgraded _callback;

    public Upgrade(ZotDroidDB db, ZotDroidMem mem) {
        super(db, mem);
        _callback = null;
    }

    public void set_callback(Upgraded caller) {
        _callback = caller;
    }

    public boolean upgrade() {

        Summary summary = _zotdroid_db.getSummary();
        if (summary.get_upgrade() != 0) {
            _current_tasks.add(new Groups(this, true));
            nextTask();
            return true;
        }
        return false;
    }

    @Override
    public void onGroupsCompletion(ZoteroResult result, Vector<Group> groups, boolean sync) {
        if (result.isSuccess()) {
            boolean has_local = false;
            for (Group g : groups) {
                if (g.isLocal()) {has_local = true; }
                _current_tasks.add(new Topper(this, g, 0, 25));
            }

            // TODO - would this spawn twice? Best double check
            if (!has_local) {
                _current_tasks.add(new Topper(this, new Group(), 0, 25)); // Local Group basically
            }
            nextTask();
        }
    }

    @Override
    public void onTopsCompletion(ZoteroResult result, Group group, String version) {
        // Finished upgrade
        if (!nextTask()) {
            Summary summary = _zotdroid_db.getSummary();
            summary.set_upgrade(0);
            _zotdroid_db.writeSummary(summary);
            _callback.onUpgradeFinish(result);
        }
    }

    @Override
    public void onTopCompletion(ZoteroResult result, Group group, int new_index, int total, Vector<Top> tops, Vector<Attachment> attachments, Vector<SubNote> subNotes, String version) {
        for (Top top : tops) {
            if (_zotdroid_db.topExists(top)) {
                _zotdroid_db.updateTop(top);
            }
        }
        for (SubNote note : subNotes) {
            if (_zotdroid_db.subNoteExists(note)) {
                _zotdroid_db.updateSubNote(note);
            }
        }
        for (Attachment attachment : attachments) {
            if (_zotdroid_db.attachmentExists(attachment)) {
                _zotdroid_db.updateAttachment(attachment);
            } else {
                // Attachment doesn't exist
                Log.d(TAG, "hmmm");
            }
        }
        if (total != 0 ) {
            float pp = ((float) new_index) / ((float)total) * 100.0f;
            _callback.onUpgradeProgress(new ZoteroResult(ZoteroResult.ZotError.SUCCESS,
                    "Updating group " + group.get_title() + " : " + String.format("%.2f", pp) + "%" ));
        }

        if (new_index + 1 <= total){
            ZoteroTask t = new Topper(this, group, new_index ,25);
            _current_tasks.add(0,t);
            nextTask();
        } else {
            onTopsCompletion(result, group, version);
        }
    }

    @Override
    public void onTopCompletion(ZoteroResult result, Group group, Vector<Top> tops, Vector<Attachment> attachments, Vector<SubNote> subNotes, String version) {
        for (Top top : tops) {
            if (_zotdroid_db.topExists(top)) {
                _zotdroid_db.updateTop(top);
            }
        }
        for (SubNote note : subNotes) {
            if (_zotdroid_db.subNoteExists(note)) {
                _zotdroid_db.updateSubNote(note);
            }
        }
    }
}
