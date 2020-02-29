package computer.benjamin.zotdr0id.ops;

import android.content.SharedPreferences;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
//import android.util.Log;
import java.io.File;
import java.util.Date;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.CollectionTop;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.Note;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Summary;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.file.MD5;
import computer.benjamin.zotdr0id.task.ChangeAttach;
import computer.benjamin.zotdr0id.task.Collections;
import computer.benjamin.zotdr0id.task.PushWebDav;
import computer.benjamin.zotdr0id.task.PushZotero;
import computer.benjamin.zotdr0id.task.PushItems;
import computer.benjamin.zotdr0id.task.PushAttachmentReq;
import computer.benjamin.zotdr0id.task.RegisterUpload;
import computer.benjamin.zotdr0id.task.Sizer;
import computer.benjamin.zotdr0id.task.SyncCol;
import computer.benjamin.zotdr0id.task.Topper;
import computer.benjamin.zotdr0id.task.VerCol;
import computer.benjamin.zotdr0id.task.VerTops;
import computer.benjamin.zotdr0id.task.ZoteroResult;
import computer.benjamin.zotdr0id.task.callback.AttachReq;
import computer.benjamin.zotdr0id.task.Deleter;
import computer.benjamin.zotdr0id.task.Groups;
import computer.benjamin.zotdr0id.task.SyncItems;
import computer.benjamin.zotdr0id.task.ZoteroTask;
import computer.benjamin.zotdr0id.task.callback.FindChangeAttach;
import computer.benjamin.zotdr0id.task.callback.GroupTask;
import computer.benjamin.zotdr0id.task.callback.RegisterUp;
import computer.benjamin.zotdr0id.task.callback.Task;
import computer.benjamin.zotdr0id.task.callback.Tops;
import computer.benjamin.zotdr0id.task.callback.WebDavDown;
import computer.benjamin.zotdr0id.task.callback.WebDavUp;
import computer.benjamin.zotdr0id.task.callback.ZoteroUp;

/**
 * Created by oni on 15/11/2017.
 * Sync ops creates a stack of tasks, each with a callback. When one completes another can be
 * started. The proceed serially as one seems to depend on another finishing. It may be possible in
 * the future to go parallel for speed.
 */

public class Sync extends Ops implements Task, GroupTask,
        FindChangeAttach, AttachReq, WebDavDown, Tops, ZoteroUp,
        RegisterUp, WebDavUp {

    private computer.benjamin.zotdr0id.task.callback.Sync _callback;

    public static final String TAG = "Sync";

    public Sync(ZotDroidDB zotdroid_db, ZotDroidMem mem) {
        super(zotdroid_db, mem);
        _callback = null;
    }

    public void set_callback(computer.benjamin.zotdr0id.task.callback.Sync callback){
        _callback = callback;
    }

    /**
     * Perform a sync with Zotero, grabbing all the items
     * We start with collections as records depend on them
     * When collections completes, we'll start on items
     * This method nukes everything and rebuilds from scratch
     */
    public void resetAndSync() {
        _zotdroid_db.reset();
        _zotdroid_mem.nukeMemory();
        _current_tasks.add(new Sizer(this));
        _current_tasks.add(new Groups(this, false));
        nextTask();
    }

    public boolean is_syncing() {
        if (_current_tasks.size() != 0 ){
            return true;
        }
        return false;
    }

    /**
     * Get the currently held Items Version
     */
    public String getVersion() {
        return _zotdroid_db.getSummary().get_last_version();
    }


    /**
     * A standard sync where we do things a bit more intelligent like.
     * We start with the groups, collections, then move onto records / items
     * Returns true if syncing is possible, OR false if a full reset and
     * sync is needed (i.e the DB is empty)
     */

    public boolean sync(){
        Summary s = _zotdroid_db.getSummary();
        // TODO - do we need this? Why is our version 0000 even when we've done a sync?
        //if (s.get_last_version().equals("0000")){ return false;}
        // This is order dependent sort of
        _current_tasks.add(new ChangeAttach(this, _zotdroid_db));
        _current_tasks.add(new Sizer(this));
        _current_tasks.add(new Groups(this, true));

        Vector<Top> changed_items = new Vector<>();

        //Vector<Attachment> changed_attachments = new Vector<>();

        // TODO - Can only do a max of 50 here
        for (Top r : _zotdroid_mem.getTops()) {
            if (!r.is_synced()) {
                changed_items.add(r);
            }
        }

        Vector<Note> changed_topnotes = new Vector<>();

        // TODO - Can only do a max of 50 here
        Vector<SubNote> changed_Sub_notes = new Vector<>();
        for (SubNote n : _zotdroid_mem.getSubNotes()) {
            if (!n.is_synced()) {
                changed_Sub_notes.add(n);
            }
        }
        if (changed_items.size() > 0 || changed_Sub_notes.size() > 0) {
            PushItems zp = new PushItems(this, changed_items, changed_Sub_notes);
            _current_tasks.add(zp);
        }

        /*if (changed_attachments.size() > 0 ) {
            PushItems zp = new PushItems(this, changed_items, changed_Sub_notes);
            _current_tasks.add(zp);
        }*/

        nextTask();
        return true;
    }

    /***********************************************************************************************
     * Check and/or create new things in our DB
     **********************************************************************************************/

    /**
     * Given a item, do we add it anew or alter an existing?
     */
    private void checkUpdateTop(Top top) {
        if (!_zotdroid_db.topExists(top)) {
            _zotdroid_db.writeTop(top);
            collectionTopCreate(top); // This is why collections MUST be synced first
        } else {
            // Check the version numbers if this exists and update as necessary
            Top existing = _zotdroid_db.getItem(top.get_zotero_key());
            // NOTE - this item is a none complete one that doesn't include authors etc
            if (existing != null) {
                if (Integer.valueOf(existing.get_version()) < Integer.valueOf(top.get_version())) {
                    // Perform an update :)
                    _zotdroid_db.updateTop(top);
                    top.set_synced(true);
                    // At this point the item will likely have different collections too
                    // so we simply rebuild them from our new fresh item
                    _zotdroid_db.removeTopFromCollections(top);
                    collectionTopCreate(top);
                }
            }
        }
    }

    /**
     * Given an attachment, do we add it anew or alter an existing?
     */
    private void checkUpdateAttachment(Attachment attachment) {
        if (!_zotdroid_db.attachmentExists(attachment)) {
            _zotdroid_db.writeAttachment(attachment);
        } else {
            // Check the version numbers if this exists and update as necessary
            Attachment existing = _zotdroid_db.getAttachment(attachment.get_zotero_key());
            if (existing != null) {
                if (Integer.valueOf(existing.get_version()) < Integer.valueOf(attachment.get_version()) ) {
                    // Perform an update :)
                    attachment.set_synced(true);
                    _zotdroid_db.updateAttachment(attachment);
                }
            }
        }
    }

    /**
     * Given an attachment, do we add it anew or alter an existing?
     */
    private void checkUpdateSubNote(SubNote subNote) {
        if (!_zotdroid_db.subNoteExists(subNote)) {
            _zotdroid_db.writeSubNote(subNote);
        } else {
            // Check the version numbers if this exists and update as necessary
            SubNote existing = _zotdroid_db.getSubNote(subNote.get_zotero_key());
            if (existing != null) {
                if (Integer.valueOf(existing.get_version()) < Integer.valueOf(subNote.get_version())) {
                    // Perform an update :)
                    _zotdroid_db.updateSubNote(subNote);
                    subNote.set_synced(true);
                }
            }
        }
    }

    /**
     * Given a toplevelnote, do we add it anew or alter an existing?
     */
    private void checkUpdateNote(Note note) {
        if (!_zotdroid_db.noteExists(note)) {
            _zotdroid_db.writeNote(note);
            collectionTopCreate(note);
        } else {
            // Check the version numbers if this exists and update as necessary
            Note existing = _zotdroid_db.getTopLevelNote(note.get_zotero_key());
            if (existing != null) {
                if (Integer.valueOf(existing.get_version()) < Integer.valueOf(note.get_version())) {
                    // Perform an update :)
                    _zotdroid_db.updateNote(note);
                    note.set_synced(true);
                    collectionTopCreate(note);
                }
            }
        }
    }

    /**
     * Given a collection, do we add it anew or alter existing?
     */
    private void checkUpdateCollection(Collection collection) {
        if (!_zotdroid_db.collectionExists(collection)) {
            _zotdroid_db.writeCollection(collection);
        } else {
            // Check the version numbers if this exists and update as necessary
            Collection existing = _zotdroid_db.getCollection(collection.get_zotero_key());
            if (existing != null) {
                if (Integer.valueOf(existing.get_version()) < Integer.valueOf(collection.get_version())) {
                    // Perform an update. This is tricky because we could essentially change a collection
                    // and move an object :/
                    // For now we just update the values but we should check whether or not this collection is in the trash
                    _zotdroid_db.updateCollection(collection);
                }
            }
        }
    }

    /**
     * Add a link between a Top Item and a collection
     */
    private void collectionTopCreate(Top top) {
        for (String ts : top.get_temp_collections()){
            CollectionTop ci = new CollectionTop();
            ci.set_item(top.get_zotero_key());
            ci.set_collection(ts);
            _zotdroid_db.writeCollectionTop(ci);
        }
        top.clear_temp_collection();
    }

    /***********************************************************************************************
     * Callback functions from the async tasks when they complete
     **********************************************************************************************/

    /**
     * Final call back - the sync has completed
     */
    public void onSyncCompletion(ZoteroResult result, String version) {
        _num_current_tasks = 0;
        _current_tasks.clear();

        if (result.isSuccess()) {
            // If we've succeeded then we can write our latest version to the place
            Summary s = _zotdroid_db.getSummary();
            s.set_last_version(version);
            _zotdroid_db.writeSummary(s);
        }

        populateFromDB(Constants.PAGINATION_SIZE);
        _callback.onSyncFinish(result);
    }

    /**
     * Called when we have a list of items to sync
     */
    @Override
    public void onSyncItemsVersion(ZoteroResult result, Group group, String version) {
        if (result.isSuccess()) {
            //Log.i(TAG,"Current Sync Items: " + getVersion() + " New Version: " + version);
            // Now we move onto the records if we need to
            if (!getVersion().equals(version)) {
                VerTops zv = new VerTops(this, group, getVersion());
                _current_tasks.add(0, zv);
                nextTask();
            } else if(_current_tasks.size() != 0){
                // More tasks to go apparently :/ Usually this one is last so if we get here
                // this could be problematic :/
                // TODO - Might need to replace the line below with some proper
                onSyncCompletion(result, version);
            } else {
                // We dont need to sync records so we are done (aside from deletion)
                onSyncCompletion(result, version);
            }
        } else { stop(); onSyncCompletion(result, version); }
    }

    @Override
    public void onTopCompletion(ZoteroResult result, Group group, int new_index, int total, Vector<Top> tops, Vector<Attachment> attachments, Vector<SubNote> subNotes, String version) {
        _callback.onSyncProgress( result,
                (float)(_num_current_tasks  - _current_tasks.size()) / (float)_num_current_tasks);
        if (result.isSuccess()) {
            for (Top top : tops){ checkUpdateTop(top); }
            for (Attachment attachment : attachments){ checkUpdateAttachment(attachment); }
            for (SubNote subNote : subNotes) {checkUpdateSubNote(subNote); }
            if (!nextTask()) { onTopsCompletion(result, group, version); }
        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * This is called when an item is completed, specifically on a sync task (i.e not a reset and sync task)
     */
    public void onTopCompletion(ZoteroResult result, Group group,
                                Vector<Top> tops, Vector<Attachment> attachments,
                                Vector<SubNote> subNotes, String version) {
        _callback.onSyncProgress( result,
                (float)(_num_current_tasks  - _current_tasks.size()) / (float)_num_current_tasks);
        if (result.isSuccess()) {
            for (Top top : tops){ checkUpdateTop(top); }
            for (Attachment attachment : attachments){ checkUpdateAttachment(attachment); }
            for (SubNote subNote : subNotes) {checkUpdateSubNote(subNote); }
            if (!nextTask()) { onTopsCompletion(result, group, version); }
        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * Called when the sync task completes and we have a stack of results to process.
     * Clears the list and adds what we get from the server
     */
    public void onTopsCompletion( ZoteroResult result, Group group, String version) {
        if (result.isSuccess()) {
            Summary s = _zotdroid_db.getSummary();
            //Log.i(TAG,"Items Complete Version: " + version);
            Deleter dt = new Deleter(this, group, s.get_last_version());
            _current_tasks.add(0,dt);
            nextTask();
        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * Called when all the collections have finished being pulled down and processed
     */

    // TODO - not sure this ever fires so I think we might need to sort it out. Seems to fire with the sync command
    public void onCollectionsCompletion( ZoteroResult result, Group group,
                                         String version) {
        if (result.isSuccess()) {
            Summary s = _zotdroid_db.getSummary();
            //Log.i(TAG,"Collections Complete Version: " + version);
            //
            nextTask();
        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * Called when a single collection has been completed
     */

    public void onCollectionCompletion(ZoteroResult result, Group group,
                                       Vector<Collection> collections, String version) {
        if (result.isSuccess()) {
            for (Collection collection : collections){
                checkUpdateCollection(collection);
            }
            // TODO - does this ever fire? I suspect not!
            if (!nextTask()) { onCollectionsCompletion(result, group, version); }

        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * Collections have completed. This is called when we have a new set of collections
     * We need to check if these already exist or not, as these can come from both reset
     * or normal sync
     * TODO - We need to check for any possible conflicts here
     */

    public void onCollectionCompletion(ZoteroResult result, int new_index, int total,
                                       Group group, Vector<Collection> collections,
                                       String version) {
        if (result.isSuccess()) {
            for (Collection collection : collections){
                if (!_zotdroid_db.collectionExists(collection)) {
                    _zotdroid_db.writeCollection(collection);
                } /*else {
                    // Check the version numbers if this exists and update as necessary
                    Collection existing = _zotdroid_db.getCollection(collection.get_zotero_key());
                    if (existing != null) {
                        if (Integer.valueOf(existing.get_version()) < Integer.valueOf(collection.get_version())) {
                            // Perform an update :)
                            // TODO - need to do the update thing here!
                        }
                    }
                }*/
            }

            // We fire off another task from here if success and we have more to go
            if (new_index + 1 <= total){
                ZoteroTask t = new Collections(this, group, new_index ,25);
                _current_tasks.add(0,t);
                nextTask();

            } else {
                onCollectionsCompletion( result, group, version);
            }
        } else {
            stop();
            _callback.onSyncFinish(result);
        }
    }

    /**
     * We now have a list of collections that have changed
     */
    public void onCollectionVersion(ZoteroResult result, Group group,
                                    Vector<String> items, String version){
        //Log.i(TAG, "Number of collections that need updating: " + items.size());
        // We now need to stagger the download and processing of these
        if (items.size() > 0 ) {
            Vector<String> keys = new Vector<>();
            for (int i = 0; i < items.size(); ++i) {
                keys.add(items.get(i));
                if (keys.size() >= 20) {
                    Collections zc = new Collections(this, group, keys);
                    _current_tasks.add(0, zc);
                    keys.clear();
                }
            }

            if (!keys.isEmpty()) {
                Collections zc = new Collections(this, group, keys);
                _current_tasks.add(0, zc);
            }
            nextTask();
        } else {
            // Nothing to do so we are up-to-date and can complete
            onCollectionsCompletion(result, group, version);
        }
    }

    /**
     * We now have a list of things that have changed
     */
    public void onItemVersion(ZoteroResult result, Group group, Vector<String> items,
                               String version){
        //Log.i(TAG, "Number of items that need updating: " + items.size());
        if (items.size() > 0) {
            Vector<String> keys = new Vector<>();
            for (int i = 0; i < items.size(); ++i) {
                //Log.i(TAG,"Update Key: " + items.get(i));
                keys.add(items.get(i));
                if (keys.size() >= 20) {
                    Topper zc = new Topper(this, group, keys);
                    _current_tasks.add(0, zc);
                    keys.clear();
                }
            }

            if (!keys.isEmpty()) {
                Topper zc = new Topper(this, group, keys);
                _current_tasks.add(0, zc);
            }

            _num_current_tasks = _current_tasks.size(); // Set this so we can have progress bars
            nextTask();
        } else {
            onTopsCompletion(result, group, version);
        }
    }

    /**
     * Callback from the first step of syncing, where we check the versions we have against the server
     */
    @Override
    public void onSyncCollectionsVersion( ZoteroResult result, Group group,
                                          String version) {
        if (result.isSuccess()) {
            //Log.i(TAG,"Current Sync Collections: " + getVersion() + " New Version: " + version);
            // Start with collections sync and then items afterwards
            if (!getVersion().equals(version)) {
                VerCol zc = new VerCol(this, group, getVersion());
                _current_tasks.add(0,zc);
                nextTask();
            } else {
                onCollectionsCompletion(result, group, getVersion());
            }
        } else { stop(); onSyncCompletion(result, version); }
    }

    /**
     * Called when our task to find items to delete returns
     */
    @Override
    public void onSyncDelete(ZoteroResult result, Group group, Vector<String> items,
                             Vector<String> collections, String version) {
        //Log.i(TAG,"Collections to delete: " + collections.size());
        //Log.i(TAG,"Items to delete: " + items.size());
        for (String key : items){
            if (_zotdroid_db.itemExists(key)){
                Item r = _zotdroid_db.getItem(key);
                _zotdroid_db.deleteRecord(r);
            } else if (_zotdroid_db.attachmentExists(key)) {
                Attachment a = _zotdroid_db.getAttachment(key);
                // Could be either but it wont fail if we get the wrong one
                // Also remove any actual files too!
                FileManager.deleteAttachment(a);
                _zotdroid_db.deleteAttachment(a);
            } else if (_zotdroid_db.subNoteExists(key)) {
                SubNote s = _zotdroid_db.getSubNote(key);
                _zotdroid_db.deleteSubNote(s);
            } else if (_zotdroid_db.subNoteExists(key)){
                Note t = _zotdroid_db.getTopLevelNote(key);
                 _zotdroid_db.deleteTopLevelNote(t);
            }
        }

        for (String key : collections){
            Collection c = _zotdroid_db.getCollection(key);
            if (c != null ){ _zotdroid_db.deleteCollection(c);}
        }
        if (!nextTask()) { onSyncCompletion(result, version); }
    }


    /**
     * Called when we have finished pushing changes to Zotero
     * TODO - we could update or check the DB here as well - possible send back these records
     * that succeeded and failed?
     */
    @Override
    public void onPushItemsCompletion(ZoteroResult result, String version) {
        if (result.isSuccess()) {
            if (!nextTask()) { onSyncCompletion(result, version); }
        } else {
            stop();
            onSyncCompletion(result, version);
        }
    }

    /**
     * Called when we have downloaded our groups
     */
    @Override
    public void onGroupsCompletion(ZoteroResult result, Vector<Group> groups, boolean sync) {
        // Issue here is we dont get a version number for the whole request, but one version per group
        // so I end up passing 0000 instead :/
        Summary s = _zotdroid_db.getSummary();
        if (result.isSuccess()) {
            for (Group g : groups){
                if (!_zotdroid_db.groupExists(g)) {
                    _zotdroid_db.writeGroup(g);
                } else {
                    // Check the version numbers if this exists and update as necessary
                    Group existing = _zotdroid_db.getGroup(g.get_zotero_key());
                    if (existing != null) {
                        if (Integer.valueOf(existing.get_version()) <
                                Integer.valueOf(g.get_version())) {
                            _zotdroid_db.updateGroup(g);
                        }
                    }
                }
                // Get collections for each group - sync task OR new task
                // We push to the front so they execute before the items tasks
                if (sync) {
                    _current_tasks.add(0,new SyncCol(this, g));
                    _current_tasks.add( new SyncItems(this, g));
                } else {
                    _current_tasks.add(0,new Collections(this, g, 0, 25));
                    _current_tasks.add(new Topper(this, g, 0, 25));
                }
            }
            // Add the default group task as well
            // Get collections for each group - sync task OR new task
            if (sync) {
                _current_tasks.add(0,new SyncCol(this, new Group()));
                _current_tasks.add( new SyncItems(this, new Group()));
            } else {
                _current_tasks.add(0,new Collections(this, new Group(), 0, 25));
                _current_tasks.add(new Topper(this, new Group(), 0, 25));
            }
            nextTask();
        } else {
            stop();
            onSyncCompletion(result, null);
        }
    }

    /**
     * Called when the size task completes.
     * We make a decision here to see if we have enough diskspace before continuing any kind of sync.
     * Will do a hard-stop if the size isnt big enough.
     * @param result
     * @param total
     */
    @Override
    public void onSizeCompletion(ZoteroResult result, int total){
        if (total == -1){
            // there was an error but continue for now
            nextTask();
            return;
        }

        File dbpath = new File(_zotdroid_db.get_location());
        String path = dbpath.getAbsolutePath().substring(0,
                dbpath.getAbsolutePath().lastIndexOf(File.separator));
        StatFs stat = new StatFs(path);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();

        // Assume 6k per entry. It's not ideal but still.
        if (total * Constants.ENTRY_SIZE * 1024 > bytesAvailable){
            _callback.onLowSpace();
            stop();
            return;
        }

        nextTask();
    }

    /**
     * Called when we've done the scans of all the attachments and we have a list of these that
     * have changed and their MD5 sums.
     * @param result
     * @param changed_attachments
     * @param changed_attachments_sums
     */

    @Override
    public void onFindChangeAttachComplete(ZoteroResult result, Vector<Attachment> changed_attachments,
                                           Vector<String> changed_attachments_sums) {
        // Called when files are changed on disk
        for (int ida = 0; ida < changed_attachments.size(); ++ida){
            Attachment attachment = changed_attachments.elementAt(ida);
            String md5sum = changed_attachments_sums.elementAt(ida);
            Log.i(TAG, attachment.get_file_name());

            // Time to add push backs
            Date changed_date = FileManager.getAttachmentLastModified(attachment);
            String date_string = Util.dateToDBString(changed_date);
            //long time_millis = changed_date.getTime();
            long time_millis = System.currentTimeMillis();
            long bytesize = FileManager.getAttachmentSize(attachment);

            // Set the attachment here - date modified reflects the file time changed on disk
            // We don't change the sum yet because we might need that later for Zotdroid Sync
            // Annoying as I'd change it here ideally.
            attachment.set_synced(false);
            attachment.set_date_modified(date_string);

            // We commit to the database here in case the push doesn't work but we know we've
            // changed the attachment here
            _zotdroid_db.updateAttachment(attachment);

            // Need to check here and see if we are personal webdav or Zotero Webdav
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ZotDroidApp.getContext());
            Boolean custom_webdav = settings.getBoolean("settings_use_webdav_storage",false);
            if (custom_webdav) {
                String username = settings.getString("settings_webdav_username", "");
                String address = settings.getString("settings_webdav_address", "");
                String password = settings.getString("settings_webdav_password", "");
                PushWebDav upload = new PushWebDav(this, address, username, password,
                        md5sum, Long.toString(time_millis), attachment);
                _current_tasks.add(0, upload);
                // TODO - finish this bit
            } else {
                _current_tasks.add(0, new PushAttachmentReq(this, attachment, md5sum,
                        Long.toString(time_millis), Long.toString(bytesize)));
            }
        }
        nextTask();
    }

    /**
     * Called when our request for an upload key has returned with either a success or fail.
     * On success, we start the next task of the actual upload of the changed attachment. Failures
     * are ignored at this point.
     * @param result
     * @param url
     * @param upload_key
     * @param prefix
     * @param suffix
     * @param content_type
     * @param changed_attachment
     */
    @Override
    public void onPushAttachmentReqCompletion(ZoteroResult result, String url, String upload_key,
                                  String prefix, String suffix, String content_type,
                                              Attachment changed_attachment) {
        // We got a request change back for our attachmentment. Hopefully we have a key
        if (result.isSuccess()) {
            _current_tasks.add(new PushZotero(this, url, prefix, suffix, content_type, upload_key,
                    changed_attachment));
        }
        // Ignore a fail - we'll just have to leave it and try again another time.
        nextTask();
    }


    @Override
    public void onDownloadProgress(ZoteroResult result, float progress) {

    }

    @Override
    public void onDownloadFinish(ZoteroResult result, Attachment attachment) {

    }

    @Override
    public void onDownloadAllProgress(ZoteroResult result, float progress) {

    }

    @Override
    public void onDownloadAllFinish(ZoteroResult result) {

    }

    @Override
    public void onZoteroUploadProgress(ZoteroResult result, float progress) {

    }

    /**
     * Called when we've finished our upload of a changed attachment to the Zotero servers
     * (Amazon backed). Hopefully, we have succeeded. If so, start the RegisterUpload task.
     * @param result
     * @param attachment
     * @param upload_key
     */
    @Override
    public void onZoteroUploadFinish(ZoteroResult result, Attachment attachment, String upload_key) {
        if (result.isSuccess()) {
            _current_tasks.add(new RegisterUpload(this, attachment, upload_key));
        }
        // Ignore failure for now.
        nextTask();
    }

    /**
     * Called when our upload has been registered. If success, we update the attachment with new
     * MD5 sum and changed date and write the lot to the DB. Otherwise we don't update anything
     * which should allow for an other shot at updating later.
     * @param result
     * @param attachment
     * @param upload_key
     */
    @Override
    public void onRegisterUpFinish(ZoteroResult result, Attachment attachment, String upload_key) {
        if (result.isSuccess()) {
            // TODO - check that this is also called with WebDav uploads instead of zotero ones
            // If we did, we need to update the attachment in question, and write the details to our
            // database now that we have successfully uploaded it.
            String name = attachment.get_file_name();
            attachment.set_md5(MD5.calculateMD5(new File(
                    FileManager.getAttachmentsDirectory() + "/" + name)));
            attachment.set_date_modified(Util.dateDBStringNow());
            _zotdroid_db.updateAttachment(attachment);
        }
        nextTask();
    }

    /*
     * Called when we are uploading to personal WebDav. Should update progress.
     */
    @Override
    public void onWebDavUploadProgress(ZoteroResult result, float progress) {

    }

    /*
     * Called when we've finished uploading to personal webdav
     */
    @Override
    public void onWebDavUploadFinish(ZoteroResult result, Attachment attachment, String new_md5,
                                     String mod_time) {
        if (result.isSuccess()) {
            // TODO - check that this is also called with WebDav uploads instead of zotero ones
            // If we did, we need to update the attachment in question, and write the details to our
            // database now that we have successfully uploaded it.
            attachment.set_date_modified(Util.dateDBStringNow()); // TODO - or mod_time?
            attachment.set_md5(new_md5);
            _zotdroid_db.updateAttachment(attachment);
        }
        // Ignoring failure for now
        nextTask();
    }
}
