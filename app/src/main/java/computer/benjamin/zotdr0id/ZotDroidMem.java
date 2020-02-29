package computer.benjamin.zotdr0id;

import java.util.HashMap;
import java.util.Vector;

import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.data.zotero.Note;

/**
 * Created by oni on 15/11/2017.
 */

// TODO - this class should be the first port of call for all queries and activities.

/**
 * A class designed to hold our current working memory 'snapshot' of the underlying database.
 * TODO - this might be better as a sort of *memory pool* kind of approach?
 */
public class ZotDroidMem {
    protected  Vector<Top>                 _tops               = new Vector<>();
    protected  HashMap<String, Top>        _key_to_top         = new HashMap<>();
    protected  HashMap<String, Note>       _key_to_note        = new HashMap<>();
    protected  Vector<Collection>          _collections        = new Vector<>();
    protected  HashMap<String, Collection> _key_to_collection  = new HashMap<>();
    protected  Vector<Group>               _groups             = new Vector<>();

    // Used for pagination of the records. For now, we ALWAYS start at 0 and only ever expand
    public int _start_index = 0;
    public int _end_index = 0;

    /**
     * Erase all the currently held records, collections etc, in memory.
     */
    public void nukeMemory(){

        _tops.clear();
        _collections.clear();
        _key_to_top.clear();
        _key_to_note.clear();
        _groups.clear();
        _key_to_collection.clear();
        _end_index = 0;

    }

    /**
     * Get all the subnotes in memory as they can be modified so our ops classes need to check
     * them. We don't hold them in memory directly, they are owned by the Top they belong to.
     * @return
     */
    public Vector<SubNote> getSubNotes() {
        Vector<SubNote> notes = new Vector<>();
        for (Top top : _tops) {
            notes.addAll(top.get_sub_notes());
        }
        return notes;
    }

    public Vector<Collection> getGroupTopCollections(Group group) {
        Vector<Collection> collections = new Vector<>();
        for (Collection c : _collections){
            if (c.get_group_key().equals(group.get_zotero_key())){
                if (c.get_parent() == null){
                    collections.add(c);
                }
            }
        }
        return collections;
    }

    // TODO is there a way to return a read-only version of these?
    public Vector<Group> getGroups() { return _groups; }
    public Vector<Top> getTops() { return _tops; }

    public Vector<Collection> getCollectionsGroup(Group group) {
        Vector<Collection> collections = new Vector<>();
        for (Collection c : _collections){
            if (c.get_group_key().equals(group.get_zotero_key())){
                collections.add(c);
            }
        }
        return collections;
    }

    public boolean topExists(Top top) {
        return _key_to_top.containsKey(top.get_zotero_key());
    }

    public void addTop(Top top) {
        _key_to_top.put(top.get_zotero_key(), top);
        _tops.add(top);
    }

    public boolean collectionExists(Collection c) {
        return _key_to_collection.containsKey(c.get_zotero_key());
    }

    public void addCollection(Collection c) {
        _key_to_collection.put(c.get_zotero_key(), c);
        _collections.add(c);
    }

    public Collection getCollectionByKey(String key){
        return _key_to_collection.get(key);
    }

    public void rebuildGroups(Vector<Group> groups) {
        _groups.clear();
        _groups.addAll(groups);
    }
}
