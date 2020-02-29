package computer.benjamin.zotdr0id.task.callback;

/**
 * Created by oni on 21/07/2017.
 */

import android.app.Activity;

import java.util.Vector;

import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.ZoteroResult;

/**
 * Special callback for the Items once all items have completed
 * TODO - should probably subclass this as it's getting rather large
 */
public interface Task {
    // Called when collections tasks finish (either one batch, or all batches)
    void onCollectionsCompletion(ZoteroResult result, Group group, String version);
    void onCollectionCompletion(ZoteroResult result, int new_index, int total, Group group, Vector<Collection> collections, String version);
    void onCollectionCompletion(ZoteroResult result, Group group, Vector<Collection> collections, String version);

    // Called when we get the latest version number back from the server
    void onItemVersion(ZoteroResult result, Group group, Vector<String> items, String version);
    void onCollectionVersion(ZoteroResult result, Group group, Vector<String> collections, String version);

    // Called when the various sync tasks have fully completed
    void onSyncDelete (ZoteroResult result, Group group, Vector<String> items, Vector<String> collections, String version);
    void onSyncItemsVersion (ZoteroResult result, Group group, String version);
    void onSyncCollectionsVersion (ZoteroResult result, Group group, String version);

    // Called when all of the above sync tasks are done
    void onSyncCompletion(ZoteroResult result, String version);

    // Called when we have pushed back to the server
    void onPushItemsCompletion(ZoteroResult result, String version);

    // Sizing
    void onSizeCompletion(ZoteroResult result, int total);

}