package computer.benjamin.zotdr0id.ops;

/**
 * Created by oni on 15/11/2017.
 */

import java.util.Vector;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.data.OrderSearch;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.CollectionTop;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.task.ZoteroTask;

/**
 * A class that deals in operations. It is abstract and is inherited by Sync and User ops
 */

public class Ops {
    Vector<ZoteroTask>    _current_tasks;
    int                   _num_current_tasks = 0;
    ZotDroidDB            _zotdroid_db;
    ZotDroidMem           _zotdroid_mem;

    Ops(ZotDroidDB db, ZotDroidMem mem) {
        _zotdroid_db = db;
        _current_tasks = new Vector<ZoteroTask>();
        _zotdroid_mem = mem;
    }

    /**
     * Remove the finished task, and start the next in the queue, returning true
     * Returns false if this was the last task.
     */

    boolean nextTask() {
        if (_current_tasks.isEmpty()) { return false; }
        _current_tasks.get(0).startZoteroTask();
        _current_tasks.remove(0);
        return true;
    }

    /**
     * Stop the current task and clear all remaining tasks.
     * Also cancel any download operations
     */
    public void stop() {
        for (ZoteroTask t : _current_tasks){ t.cancel(true); }
        _current_tasks.clear();
        _num_current_tasks = 0;
    }

    /**
     * Everything starts with a set of records derived somehow. From these records we can rebuild
     * all our datastructures in memory again, from the database of all our zotero collection
     */

     void rebuildMemory(Vector<Top> tops) {
         // TODO - this might be better in the memory class?
         // Now add trv to our working memory - it always starts with records
         for (Top top : tops) {
             if (!_zotdroid_mem.topExists(top)) {
                 _zotdroid_mem.addTop(top);
             }
         }

         // Now go with collections
         // We don't paginate collections at this point
         // Most of the time, this will not be required as all the collections will be in place
         // but for now it makes sense to have it here.
         int numrows = _zotdroid_db.getNumCollections();
         Vector<Collection> newcollections = new Vector<>();

         for (int i=0; i < numrows; ++i) {
             Collection collection = _zotdroid_db.getCollection(i);
             if(!_zotdroid_mem.collectionExists(collection)) {
                 newcollections.add(collection);
                 _zotdroid_mem.addCollection(collection);
             }
         }

         // Now link each collection to it's parent. Memory duplication I fear :/
         for (Collection c : newcollections){
             Collection zp = _zotdroid_mem.getCollectionByKey(c.get_parent_key());
             if (zp != null){
                 zp.add_collection(c);
             } else {
                // TODO - not sure this is right at all :/
                //Collection allc = _zotdroid_mem._collections.elementAt(0);
                //allc.add_collection(c);
             }
         }

         // This bit could be slow if there are loads of collections. There will be a faster
         // way to do it I would say.
         // Add the new records to the collections they belong to.

         Vector<CollectionTop> items = _zotdroid_db.getCollectionItems();

         for (CollectionTop ct : items) {
            for (Collection c : newcollections){
                if (ct.get_collection().contains(c.get_zotero_key())){
                    for (Top r : tops){
                        if ( ct.get_item().contains(r.get_zotero_key())){
                            r.add_collection(c);
                            c.add_child(r);
                            break;
                        }
                    }
                    break;
                }
            }
         }

         // Rebuild the groups
         _zotdroid_mem.rebuildGroups(_zotdroid_db.getGroups());
     }

    /**
     * Get a subset of the records in the database, finding any that are new. Then rebuild
     * our memory so it reflects this window
     *
     * TODO - Do we pass in a searchorder class here do we think?
     */
     void populateFromDB(int end) {
         OrderSearch order = new OrderSearch(OrderSearch.Order.BASIC);
         Vector<Top> tops = _zotdroid_db.getTops(0, end, null, null, order);
         rebuildMemory(tops);
         return;
    }

}
