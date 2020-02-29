package computer.benjamin.zotdr0id.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.callback.Task;

/**
 * Created by oni on 27/07/2017.
 * Find things that have been deleted and that we need to remove from our DB.
 */

public class Deleter extends ZoteroGet {
    //private static final String TAG = "VerTops";
    private Task _callback;
    private String _since_version;
    private Group _group = new Group();
    private String _url;

    public Deleter(Task callback, Group group, String since_version) {
        _callback = callback;
        _group = group;
        _since_version = since_version;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/deleted?since=" + _since_version + "&format=versions";

        if (!_group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _since_version = _group.get_version();
            _url = Constants.BASE_URL + "/groups/" + _group.get_zotero_key() + "/deleted?since=" + _since_version + "&format=versions";
        }
    }

    @Override
    public void startZoteroTask() {
       execute(_url);
    }

    protected void onPostExecute(String rstring) {
        Vector<String> item_keys = new Vector<>();
        Vector<String> collection_keys = new Vector<>();
        try {
            JSONObject jObject = new JSONObject(rstring);
            String version = "0000";
            try {
                version = jObject.getString("Last-Modified-Version");
            } catch (JSONException e) {
                //Log.i(TAG,"No Last-Modified-Version in request.");
                _callback.onSyncDelete(new ZoteroResult(ZoteroResult.ZotError.DEL_TASK_0), _group,
                        null, null, "0000");
            }

            jObject = jObject.getJSONObject("results");
            JSONArray items = jObject.getJSONArray("items");
            for (int i=0; i < items.length(); i++) {
                try {
                    String tj = items.getString(i);
                    item_keys.add(tj);
                } catch (JSONException e) {
                    _callback.onSyncDelete(new ZoteroResult(ZoteroResult.ZotError.DEL_TASK_1),
                            _group, null, null, "0000");
                }
            }

            JSONArray collections = jObject.getJSONArray("collections");
            for (int i=0; i < collections.length(); i++) {
                try {
                    String tj = collections.getString(i);
                    collection_keys.add(tj);
                } catch (JSONException e) {
                    _callback.onSyncDelete(new ZoteroResult(ZoteroResult.ZotError.DEL_TASK_2),
                            _group, null, null, "0000");
                }
            }

            _callback.onSyncDelete(new ZoteroResult(), _group, item_keys, collection_keys,
                    version );

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            _callback.onSyncDelete(new ZoteroResult(ZoteroResult.ZotError.DEL_TASK_4),
                    _group, null, null, "0000");
        }

    }
}

