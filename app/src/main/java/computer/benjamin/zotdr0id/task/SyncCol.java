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

public class SyncCol extends ZoteroGet {
    //private static final String TAG = "SyncCol";
    private Task _callback;
    private Group _group;

    public SyncCol(Task callback, Group group) {
        _callback = callback;
        _group = group;
    }

    @Override
    public void startZoteroTask() {
        execute(Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/collections");
    }

    @Override
    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            _callback.onSyncCollectionsVersion(new ZoteroResult(ZoteroResult.ZotError.SYNCCOL_TASK_0), _group, "0000");
            return;
        }

        try {
            JSONObject jObject = new JSONObject(rstring);
            try {
                String _new_version_collections = jObject.getString("Last-Modified-Version");
                _callback.onSyncCollectionsVersion(new ZoteroResult(), _group, _new_version_collections);
            } catch (JSONException e) {
                //Log.i(TAG, "No Last-Modified-Version in request.");
                _callback.onSyncCollectionsVersion(new ZoteroResult(ZoteroResult.ZotError.SYNCCOL_TASK_1), _group,"0000");
            }
        } catch (JSONException e) {

        }
    }
}
