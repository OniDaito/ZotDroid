package computer.benjamin.zotdr0id.task.callback;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * This interface is called when a request to upload a file has either been accepted or denied
 * by the zotero servers.
 */
public interface AttachReq {
    void onPushAttachmentReqCompletion(ZoteroResult result, String url, String upload_key,
                                       String prefix, String suffix, String content_type,
                                       Attachment changed_attachment);

}
