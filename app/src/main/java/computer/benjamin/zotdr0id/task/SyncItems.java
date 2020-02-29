package computer.benjamin.zotdr0id.task;

import org.json.JSONException;
import org.json.JSONObject;
import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.callback.Task;

/**
 * Created by oni on 27/07/2017.
 * This class looks at the versions and tries to work out what has changed in the meantime.
 */

public class SyncItems extends ZoteroGet {

    //private static final String TAG = "SyncCol";
    private Task _callback;
    private Group _group = new Group();

    public SyncItems(Task callback, Group group) {
        _callback = callback;
        _group = group;
    }

    @Override
    public void startZoteroTask() {
        execute(Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items");
    }

    @Override
    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            _callback.onSyncItemsVersion(new ZoteroResult(ZoteroResult.ZotError.SYNC_ITEMS_TASK_0), _group, "0000");
            return;
        }

        try {
            JSONObject jObject = new JSONObject(rstring);
            try {
                String _new_version_items = jObject.getString("Last-Modified-Version");
                _callback.onSyncItemsVersion(new ZoteroResult(), _group, _new_version_items);
            } catch (JSONException e) {
                //Log.i(TAG, "No Last-Modified-Version in request.");
                _callback.onSyncItemsVersion(new ZoteroResult(ZoteroResult.ZotError.SYNC_ITEMS_TASK_1), _group, "0000");
            }
        } catch (JSONException e) {

        }
    }
}
