package computer.benjamin.zotdr0id.task;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.Vector;
import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.callback.Task;

/**
 * Created by oni on 27/07/2017.
 * This task gets the versions for all the tops (items and notes).
 */

public class VerTops extends ZoteroGet {
    //private static final String TAG = "VerTops";
    private Task _callback;
    private Group _group = new Group();
    private String _url;

    public VerTops(Task callback, Group group, String since_version) {
        _callback = callback;
        _group = group;
        String _since_version = since_version;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items?since=" + _since_version + "&format=versions";

        if (!_group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _since_version = _group.get_version();
            _url = Constants.BASE_URL + "/groups/" + _group.get_zotero_key() + "/items?since=" + _since_version + "&format=versions";
        }
    }

    @Override
    public void startZoteroTask() {
        execute(_url);
    }

    protected void onPostExecute(String rstring) {
        Vector<String> item_keys = new Vector<>();
        String version = "0000";

        try {
            JSONObject jObject = new JSONObject(rstring);
            version = jObject.getString("Last-Modified-Version");
            JSONObject items = jObject.getJSONObject("results");

            Iterator i = items.keys();
            while (i.hasNext()) {
                String key = (String)i.next();
                item_keys.add(key);
            }
            _callback.onItemVersion(new ZoteroResult(), _group, item_keys, version);
        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            _callback.onItemVersion(new ZoteroResult(ZoteroResult.ZotError.VERTOPS_TASK_0),
                    _group,null, version);
        }
    }
}

