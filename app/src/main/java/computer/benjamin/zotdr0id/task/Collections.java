package computer.benjamin.zotdr0id.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.task.callback.Task;

/**
 * Created by oni on 21/07/2017.
 * This task gets hold of all the collections from the zotero server.
 */

public class Collections extends ZoteroGet {
    //private static final String TAG = "Collections";
    private Task callback;
    private int startItem = 0;
    private int itemLimit = 25;

    private Group _group = new Group();
    private String _url = "";
    private boolean _reset_mode = true;

    public Collections(Task callback, Group group, int start, int limit) {
        this.callback = callback;
        this.startItem = start;
        this.itemLimit = limit;
        _group = group;
        _reset_mode = true;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid(  ) + "/collections?start=" + Integer.toString(this.startItem);

        if (!group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _url = Constants.BASE_URL + "/groups/" + group.get_zotero_key() + "/collections?start=" + Integer.toString(this.startItem);
        }
    }

    public Collections(Task callback, Group group, Vector<String> keys) {
        this.callback = callback;
        _reset_mode = false;
        _group = group;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/collections?collectionKey=";

        if (!group.get_zotero_key().equals(Constants.LOCAL_GROUP)){
            _url = Constants.BASE_URL + "/groups/" + group.get_zotero_key() + "/collections?collectionKey=";
        }

        StringBuilder sb = new StringBuilder();
        for (String key: keys){
            sb.append(key);
            sb.append(",");
        }
        _url += sb.toString();
        _url = _url.substring(0, _url.length()-1);
    }

    /**
     * Actually execute the async task.
     * Not quite an override but it sets up the string for us when we do actually execute, so things are in sync
     * For some reason, it only works if I pass the start (and possibly limit) as URL params instead of headers
     * but the desc and dateAdded seem ok. It could be an integer thing I suspect
     * If _reset_mode is not true then we are returning objects that need to be updated, not fresh objects
     */
    public void startZoteroTask() {
        if (_reset_mode) {
            execute(_url,
                    "start", Integer.toString(this.startItem),
                    "limit", Integer.toString(this.itemLimit),
                    "direction", "desc",
                    "sort", "dateAdded");
        } else {
            execute(_url,
                    "direction", "desc",
                    "sort", "dateAdded");
        }
    }

    protected void onPostExecute(String rstring) {
        Vector<Collection> collections =  new Vector<>();

        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            callback.onCollectionsCompletion(
                    new ZoteroResult(ZoteroResult.ZotError.COLLECTIONS_TASK_2),
                    _group, "0000");
            return;
        }

        // TODO - Not sure if we have to request multiple times for collections :/

        try {
            JSONObject jObject = new JSONObject(rstring);

            int total = 0;
            try {
                total = jObject.getInt("Total-Results");
            } catch (JSONException e) {
                //Log.i(TAG,"No Total-Results in request.");
            }

            String version = "0000";
            try {
                version = jObject.getString("Last-Modified-Version");
            } catch (JSONException e) {
                //Log.i(TAG,"No Last-Modified-Version in request.");
            }

            JSONArray jArray = jObject.getJSONArray("results");

            for (int i=0; i < jArray.length(); i++) {
                try {
                    JSONObject jobjtop = jArray.getJSONObject(i);
                    JSONObject jobj = jobjtop.getJSONObject("data");
                    Collection c = processEntry(jobj);
                    c.set_group_key(_group.get_zotero_key());
                    collections.add(c);
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onCollectionsCompletion(
                            new ZoteroResult(ZoteroResult.ZotError.COLLECTIONS_TASK_0), _group,
                            version);
                    return;
                }
            }

            if (_reset_mode) {
                callback.onCollectionCompletion(new ZoteroResult(),
                        startItem + jArray.length(), total, _group, collections, version);
            } else {
                callback.onCollectionCompletion(new ZoteroResult(), _group, collections, version);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            callback.onCollectionsCompletion(
                    new ZoteroResult(ZoteroResult.ZotError.COLLECTIONS_TASK_1), _group,
                    "0000");
        }
    }

    private Collection processEntry(JSONObject jobj) {
        Collection collection = new Collection();
        try {
            collection.set_zotero_key(jobj.getString("key"));
        } catch (JSONException e) {
            // We should always have a key. If we dont then bad things :S
        }

        try {
            collection.set_title(jobj.getString("name"));
        } catch (JSONException e) {
            collection.set_title("No title");
        }

        try {
            collection.set_version(jobj.getString("version"));
        } catch (JSONException e) {
            collection.set_version("0000");
        }

        try {
            collection.set_parent_key(jobj.getString("parentCollection"));
        } catch (JSONException e) {
            collection.set_parent_key("");
        }

        return collection;
    }
}
