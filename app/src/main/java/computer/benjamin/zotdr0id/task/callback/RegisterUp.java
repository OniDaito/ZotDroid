package computer.benjamin.zotdr0id.task.callback;

import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 15/11/2017.
 * This interface is called once we've registered our new upload to the Zotero webserver or
 * external webdav. It's the last stage in the upload process.
 */

public interface RegisterUp {
    void onRegisterUpFinish(ZoteroResult result, Attachment attachment, String upload_key);
}
