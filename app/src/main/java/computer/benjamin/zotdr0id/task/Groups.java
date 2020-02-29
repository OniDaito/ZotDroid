package computer.benjamin.zotdr0id.task;

/**
 * Created by oni on 01/01/2018.
 * Our task for getting hold of the Groups.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.callback.GroupTask;

public class Groups extends ZoteroGet {

    private static final String TAG = "Groups";
    private GroupTask callback;
    private String _url = "";
    private boolean _sync = false;

    public Groups(GroupTask callback, boolean sync) {
        this.callback = callback;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/groups";
        _sync =  sync;
    }

    /**
     * Actually execute the async task.
     * Not quite an override but it sets up the string for us when we do actually execute, so things are in sync
     * For some reason, it only works if I pass the start (and possibly limit) as URL params instead of headers
     * but the desc and dateAdded seem ok. It could be an integer thing I suspect
     */
    public void startZoteroTask() {
        execute(_url, "direction", "desc", "sort", "dateAdded");
    }

    protected void onPostExecute(String rstring) {
        Vector<Group> groups =  new Vector<>();

        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            callback.onGroupsCompletion(new ZoteroResult(ZoteroResult.ZotError.GROUPS_TASK_0),
                    null, _sync);
            return;
        }

        // TODO - Not sure if we have to request multiple times for collections :/

        try {
            JSONObject jObject = new JSONObject(rstring);
            /*int total = 0;
            try {
                total = jObject.getInt("Total-Results");
            } catch (JSONException e) {
                //Log.i(TAG,"No Total-Results in request.");
            }*/

            JSONArray jArray = jObject.getJSONArray("results");

            for (int i=0; i < jArray.length(); i++) {
                try {
                    JSONObject jobjtop = jArray.getJSONObject(i);
                    JSONObject jobj = jobjtop.getJSONObject("data");
                    groups.add(processEntry(jobj));
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onGroupsCompletion(
                            new ZoteroResult(ZoteroResult.ZotError.GROUPS_TASK_1),null, _sync);
                    return;
                }
            }
            callback.onGroupsCompletion(new ZoteroResult(), groups, _sync);

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            callback.onGroupsCompletion(new ZoteroResult(ZoteroResult.ZotError.GROUPS_TASK_2),
                    null, _sync);
        }
    }

    private Group processEntry(JSONObject jobj) {
        Group group = new Group();

        try {
            group.set_zotero_key(jobj.getString("id"));
        } catch (JSONException e) {
            // We should always have a key. If we dont then bad things :S
        }

        try {
            group.set_title(jobj.getString("name"));
        } catch (JSONException e) {
            group.set_title("No title");
        }

        try {
            group.set_version(jobj.getString("version"));
        } catch (JSONException e) {
            group.set_version("0000");
        }

        return group;
    }
}
