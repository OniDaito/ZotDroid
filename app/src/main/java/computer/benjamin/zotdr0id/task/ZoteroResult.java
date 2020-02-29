package computer.benjamin.zotdr0id.task;

/**
 * Our class that represents a result from our async tasks, errors or successes with appropriate
 * messages we can work with.
 */
public class ZoteroResult {

    /**
     * All the errors (and successes) that can be passed around the system. We can convert these
     * to proper messages with localisation further up the stack.
     */
    public enum ZotError{
        SUCCESS,
        COLLECTIONS_TASK_0,
        COLLECTIONS_TASK_1,
        COLLECTIONS_TASK_2,
        DEL_TASK_0,
        DEL_TASK_1,
        DEL_TASK_2,
        DEL_TASK_3,
        DEL_TASK_4,
        GROUPS_TASK_0,
        GROUPS_TASK_1,
        GROUPS_TASK_2,
        ITEMS_TASK_0,
        ITEMS_TASK_1,
        ITEMS_TASK_2,
        VERCOL_TASK_0,
        VERTOPS_TASK_0,
        SYNC_OPS_0,
        UPLOAD_TASK_0,
        UPLOAD_TASK_1,
        UPLOAD_TASK_2,
        UPLOAD_TASK_3,
        UPLOAD_TASK_4,
        UPLOAD_TASK_5,
        UPLOAD_TASK_6,
        UPLOAD_TASK_7,
        DOWNLOAD_TASK_0,
        DOWNLOAD_TASK_1,
        DOWNLOAD_TASK_2,
        DOWNLOAD_TASK_3,
        DOWNLOAD_TASK_4,
        DOWNLOAD_TASK_5,
        DOWNLOAD_TASK_6,
        PUSH_TASK_0,
        PUSH_TASK_1,
        PUSH_TASK_2,
        PUSH_ITEM_TASK_0,
        PUSH_ITEM_TASK_1,
        PUSH_ITEM_TASK_2,
        SIZE_TASK_0,
        SIZE_TASK_1,
        SIZE_TASK_2,
        SYNCCOL_TASK_0,
        SYNCCOL_TASK_1,
        SYNC_ITEMS_TASK_0,
        SYNC_ITEMS_TASK_1,
        CHANGED_ATTACH_1,
        REG_TASK_0,
        WEB_DAV_UP_0,
        WEB_DAV_UP_1
    }

    private ZotError _error = ZotError.SUCCESS;
    private String _msg = "";

    public ZoteroResult(){}
    public ZoteroResult( ZotError error){
        _error = error;
    }
    public ZoteroResult( ZotError error, String msg){
        _error = error; _msg = msg;
    }

    public boolean isSuccess(){
        return _error == ZotError.SUCCESS;
    }
}
