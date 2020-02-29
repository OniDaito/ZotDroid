package computer.benjamin.zotdr0id.task.callback;

import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 13/03/2018.
 */

public interface Upgraded {
    void onUpgradeProgress(ZoteroResult result);
    void onUpgradeFinish(ZoteroResult result);
}
