package computer.benjamin.zotdr0id.task.callback;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 15/11/2017.
 * Interface callback for uploading files to the Zotero servers.
 */

public interface WebDavUp {
    void onWebDavUploadProgress(ZoteroResult result, float progress);
    void onWebDavUploadFinish(ZoteroResult result, Attachment attachment,
                              String new_md5, String mod_time);
}
