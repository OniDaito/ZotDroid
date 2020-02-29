package computer.benjamin.zotdr0id.task.callback;

import android.app.Activity;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 15/11/2017.
 */

public interface WebDavDown {
    void onDownloadProgress(ZoteroResult result, float progress);
    void onDownloadFinish(ZoteroResult result, Attachment attachment);
    void onDownloadAllProgress(ZoteroResult result, float progress);
    void onDownloadAllFinish(ZoteroResult result);
}
