package computer.benjamin.zotdr0id.task.callback;

import android.app.Activity;

import java.util.Vector;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.ZoteroResult;

public interface FindChangeAttach {
    // Called when items tasks finish (either one batch or all batches)
    void onFindChangeAttachComplete(ZoteroResult result, Vector<Attachment> changed_attachments,
                                    Vector<String> changed_attachments_sums);

}
