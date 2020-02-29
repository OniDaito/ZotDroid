package computer.benjamin.zotdr0id.task.callback;

import java.util.Vector;

import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Created by oni on 13/03/2018.
 */

public interface GroupTask {
    // Called when we have downloaded a set of groups
    void onGroupsCompletion (ZoteroResult results, Vector<Group> groups, boolean sync);
}
