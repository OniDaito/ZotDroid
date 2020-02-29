package computer.benjamin.zotdr0id.task.callback;

import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 15/11/2017.
 */

public interface Sync {
    void onSyncProgress(ZoteroResult result, float progress);
    void onSyncFinish(ZoteroResult result);
    void onLowSpace();
}
