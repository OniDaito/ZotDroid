package computer.benjamin.zotdr0id.ops;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Vector;
import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.data.OrderSearch;
import computer.benjamin.zotdr0id.data.ZotDroidDB;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.Note;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.task.ZoteroResult;
import computer.benjamin.zotdr0id.task.callback.WebDavDown;
import computer.benjamin.zotdr0id.task.Download;

/**
 * Created by oni on 14/07/2017.
 *
 * This class performs user operations such as search, requesting a different collection,
 * or downloading an attachment.
 *
 * It also keeps track of the current group and collection we are viewing. Nasty state to keep
 * hold of.
 *
 */

public class User extends Ops {

    private Download            _zotero_download    = new Download();
    private WebDavDown          _callback;
    public static final String   TAG                = "zotdroid.User";
    private Collection          _current_collection;
    private Group               _current_group;
    private static OrderSearch  _current_order      = new OrderSearch(OrderSearch.Order.BASIC);

    public User(ZotDroidDB zotdroid_db, ZotDroidMem mem) {
        super(zotdroid_db, mem);
        // We take this out for now and just build it in memory by default. Should never be null!
        _current_group = _zotdroid_db.getGroup(Constants.LOCAL_GROUP);
        _current_collection = null; // All initially
    }

    public void set_callback(WebDavDown callback) {
        _callback = callback;
    }

    /**
     * Stop the current task and clear all remaining tasks.
     * Also cancel any download operations
     */
    public void stop() {
        super.stop();
        _zotero_download.stop();
    }

    /**
     * A very small class that holds the state for our webdav attachment download
     * Its perhaps a bit complicated but it means we can do multiple requests and
     * not have the main activity worry too much.
     */
    private class OpsDav implements WebDavDown {

        Attachment _attachment;

        private OpsDav(Attachment attachment) { _attachment = attachment; }

        @Override
        public void onDownloadProgress(ZoteroResult result, float progress) {
            _callback.onDownloadProgress(result, progress);
        }

        @Override
        public void onDownloadFinish(ZoteroResult result, Attachment attachment) {
            _callback.onDownloadFinish(result, attachment);
        }

        // Never called for this
        @Override
        public void onDownloadAllProgress(ZoteroResult result, float progress) { }

        @Override
        public void onDownloadAllFinish(ZoteroResult result) { }
    }

    /**
     * Download an attachement, unless it already exists, in which case, callback immediately
     * Returns true if the attachment already exists and false if we need to download it.
     * TODO - we should create directories or somthing if we have filenames that are the same:
     */

    public boolean getAttachmentDownloadOpen(Top record, int attachment_idx, Activity activity) {
        if (attachment_idx < record.get_attachments().size()) {
            Attachment za = record.get_attachments().elementAt(attachment_idx);
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
            if (_current_group.isLocal()) {
                Boolean usewebdav = settings.getBoolean("settings_use_webdav_storage", false);
                if (usewebdav) {
                    if ( FileManager.attachmentExists(za)) {
                        _callback.onDownloadFinish(new ZoteroResult(), za);
                        return true;
                    } else {
                        String username =
                                settings.getString("settings_webdav_username", "username");
                        String password =
                                settings.getString("settings_webdav_password", "password");
                        String server_address =
                                settings.getString("settings_webdav_address", "address");
                        _zotero_download.downloadAttachment(za,
                                FileManager.getAttachmentsDirectory(),
                                username, password, server_address, new OpsDav(za));
                        return false;
                    }
                } else {
                    if ( FileManager.attachmentExists(za)) {
                        OpsDav op = new OpsDav(za);
                        op.onDownloadFinish(new ZoteroResult(), za);
                        return true;
                    } else {
                        _zotero_download.downloadAttachmentZotero(za, new OpsDav(za), activity);
                        return false;
                    }
                }
            } else {
                // Different if we are downloading group
                _zotero_download.downloadAttachmentGroup(_current_group, za, new OpsDav(za), activity);
                // TODO - what to return here? Hmmm Still provide a test I think
            }
        }

        return true;
    }

    /**
     * Nuke memory and go back to where we were before, usually after a search
     */
    public void reset() {
        _zotdroid_mem.nukeMemory();
        _current_order = new OrderSearch(OrderSearch.Order.BASIC);
        filter(_current_collection, _current_group, 0, Constants.PAGINATION_SIZE, _current_order);
    }

    /**
     * Change the order, without changing anything else
     */
    public void changeOrder(OrderSearch order){
        filter(_current_collection, _current_group, 0, Constants.PAGINATION_SIZE, order);
    }

    /**
     * Filter the collection based on the previous state of the system and the new state as
     * passed in by searchterm, end index and current collection.
     */
    private void filter(Collection collection, Group group, int start, int end, OrderSearch order) {
        // Swapping collections with no search or enhance

        if (collection != _current_collection || group != _current_group){
            _zotdroid_mem.nukeMemory();
        }
        /*if (!collection.equals(_current_collection)) {
            // Swapping collections so nuke entirely
            _zotdroid_mem.nukeMemory();
        }*/


        // TODO - can we actually do this sort of equals check now?
        if(!order.equals( _current_order)) {// Swapping order so we need to nuke
           _zotdroid_mem.nukeMemory();
        }
        _current_collection = collection;
        _current_group = group;
        _current_order  = order;

        Vector<Top> trv = new Vector<>();
        trv = _zotdroid_db.getTops(start, end, _current_group, _current_collection, _current_order);


        /*
        // Search is an entirely new view across the entire collection, not just what is
        // visible currently. I figure that might be better. This does make the scroll harder
        // TODO - create a method to see if there are more records available in current search
        if (!searchterm.isEmpty() && !_search_term.equals(searchterm)) {
            _zotdroid_mem.nukeMemory();
            trv = _zotdroid_db.searchRecords(_current_collection, searchterm,
                    Constants.PAGINATION_SIZE, _current_order);
            tnv = _zotdroid_db.searchTopLevelNotes(_current_collection, searchterm,
                    Constants.PAGINATION_SIZE, _current_order);

            _search_term = searchterm;
        } else if (!searchterm.isEmpty() && _search_term.equals(searchterm)){
            trv = _zotdroid_db.searchRecords(_current_collection, searchterm, end, _current_order);
            tnv = _zotdroid_db.searchTopLevelNotes(_current_collection, searchterm,
                    end, _current_order);

        }*/

        // Make sure the size is correct

        if (end < trv.size()) {
            _zotdroid_mem._end_index = end;
            trv.setSize(_zotdroid_mem._end_index + 1);
        } else {
            _zotdroid_mem._end_index = trv.size() - 1;
        }
        rebuildMemory(trv);
    }

    public void getMoreResults(int more) {
        filter(_current_collection, _current_group, 0,
                _zotdroid_mem._end_index + more, _current_order);
    }

    /**
     * See if there are more results to return. If we are currently searching
     * we check if there are more results across the entire collection.
     */
    public boolean hasMoreResults() {
        int total = _zotdroid_db.getNumTops(_current_group, _current_collection, _current_order);
        if (_zotdroid_mem._end_index + 1 < total) {
            return true;
        }

        return false;
    }

    /**
     * Swap collections and therefore re-filter
     * @param group
     * @param collection
     */
    public void swapCollection(Group group, Collection collection) {
        if (collection != _current_collection || group != _current_group) {
            filter(collection, group, 0, Constants.PAGINATION_SIZE, _current_order);
        }
    }

    /**
     * Lets set the searchterm and do a filter.
     * @param term
     */
    public void search(String term) {
        _current_order.set_searchTerm(term);
        filter(_current_collection, _current_group,0, Constants.PAGINATION_SIZE, _current_order);
    }

    /**
     * Update straight to the DB. We can also set the sync flag at the same time. Essentially,
     * this is a permanent change to the DB so we may need to sync it back.
     */
    public void commitItem(Item r, Boolean is_synced) {
        r.set_synced(is_synced);
        _zotdroid_db.updateItem(r); }

    /**
     * Commit a subnote straight to the db
     */
    public void commitSubNote(SubNote n, Boolean is_synced) {
        n.set_synced(is_synced);
        _zotdroid_db.updateSubNote(n);
    }

    /**
     * Commit a note to the DB
     */
    public void commitNote(Note n, Boolean is_synced) {
        n.set_synced(is_synced);
        _zotdroid_db.updateNote(n);
    }

    /**
     * commit a changed attachment straight to the db
     * @param a
     * @param is_synced
     */
    public void commitAttachment(Attachment a,  Boolean is_synced) {
        a.set_synced(is_synced);
        _zotdroid_db.updateAttachment(a);
    }

    /**
        Small callback that we use when downloading the entire set of attachments
     */
    private class OpsDavDownAll implements WebDavDown {
        int _total;
        int _progress;

        public OpsDavDownAll(int total){
            _total = total;
        }

        @Override
        public void onDownloadAllProgress(ZoteroResult result, float progress) {
            _progress += 1;
            if (_progress >= _total) {
                _callback.onDownloadAllFinish(result);
            } else {
                float pp = ((float) _progress) / ((float)_total) * 100.0f;
                _callback.onDownloadAllProgress(result, pp);
            }
        }

        @Override
        public void onDownloadAllFinish(ZoteroResult result) {
           _callback.onDownloadAllFinish(result);
        }

        @Override
        public void onDownloadProgress(ZoteroResult result, float progress) {
            _callback.onDownloadAllProgress(result, progress);
        }

        @Override
        public void onDownloadFinish(ZoteroResult result,  Attachment attachment) {
            if (result.isSuccess()) {
                String unzipped = FileManager.unzipFile(
                        attachment.get_zotero_key() + ".zip", attachment.get_file_name());
                _progress += 1;
                if (_progress >= _total) {
                    _callback.onDownloadAllFinish(result);
                } else {
                    float pp = ((float) _progress) / ((float) _total) * 100.0f;
                    _callback.onDownloadAllProgress(result, pp);
                }
            } else {
                // TODO - continue(silently?) or stop?
            }
        }
    }

    /**
     * Grab all the attachments
     */

    public void downloadAllAttachments(Activity activity) {
        Vector<Attachment> attachments = _zotdroid_db.getAttachments();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        Boolean usewebdav = settings.getBoolean("settings_use_webdav_storage", false);
        OpsDavDownAll op = new OpsDavDownAll(attachments.size());

        for (Attachment attachment : attachments){
            if (usewebdav) {
                if ( FileManager.attachmentExists(attachment)) {
                    op.onDownloadFinish(new ZoteroResult(), attachment);
                } else {
                    String username =
                            settings.getString("settings_webdav_username", "username");
                    String password =
                            settings.getString("settings_webdav_password", "password");
                    String server_address =
                            settings.getString("settings_webdav_address", "address");

                    _zotero_download.downloadAttachment(attachment,
                            FileManager.getAttachmentsDirectory(),
                            username, password, server_address, op);
                }
            } else {
                if ( FileManager.attachmentExists(attachment)) {
                    op.onDownloadFinish(new ZoteroResult(), attachment);
                } else {
                    _zotero_download.downloadAttachmentZotero( attachment, op, activity);
                }
            }
        }
    }
}

