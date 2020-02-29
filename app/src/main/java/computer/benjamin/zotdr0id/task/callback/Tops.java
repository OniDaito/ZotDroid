package computer.benjamin.zotdr0id.task.callback;

import java.util.Vector;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Note;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 13/03/2018.
 */

public interface Tops {
    // Called when items tasks finish (either one batch or all batches)
    void onTopsCompletion(ZoteroResult result, Group group, String version);
    void onTopCompletion(ZoteroResult result, Group group, int new_index, int total,
                         Vector<Top> tops, Vector<Attachment> attachments,
                         Vector<SubNote> subNotes, String version);
    void onTopCompletion(ZoteroResult result, Group group, Vector<Top> tops,
                         Vector<Attachment> attachments, Vector<SubNote> subNotes, String version);

}
