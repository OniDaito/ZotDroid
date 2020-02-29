package computer.benjamin.zotdr0id.task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Vector;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.task.callback.Task;

/**
 * Created by oni on 01/12/2017.
 * An async task that pushes changed toplevel items and subnotes to the zotero server. The that have
 * their sync flag set to false. We convert to JSON and then push them forward.
 */

public class PushItems extends ZoteroPost  {
    private static final String TAG = "PushItems";
    private Task _callback;
    private Vector<Top>     _changed_tops;
    private Vector<SubNote> _changed_subnotes;

    public PushItems(Task callback, Vector<Top> changed_tops,
                     Vector<SubNote> changed_subnotes) {
        _callback = callback;
        _changed_tops = changed_tops;
        _changed_subnotes = changed_subnotes;
    }

    public void startZoteroTask(){
        String _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items";
        JSONArray jtop = new JSONArray();
        for (Top r : _changed_tops) { jtop.put(r.to_json()); }
        for (SubNote n : _changed_subnotes) { jtop.put(n.to_json()); }
        execute(_url, jtop.toString());
    }

    protected void onPostExecute(String rstring) {
        String version = "0000";
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")) {
            _callback.onPushItemsCompletion(new ZoteroResult(ZoteroResult.ZotError.PUSH_ITEM_TASK_0), version);
            return;
        }

        JSONObject jObject;
        try {
            jObject = new JSONObject(rstring);
            version = jObject.getString("Last-Modified-Version");
            JSONObject results = jObject.getJSONObject("results");

            // We need to check the changed results to make sure we re-sync back
            JSONObject successes = results.getJSONObject("success");
            JSONObject failures = results.getJSONObject("failed");
            //JSONObject unchanged = results.getJSONObject("unchanged");

            if (successes.length() > 0) {
                for (int i = 0; i < successes.names().length(); i++) {
                    JSONArray names = successes.names();

                    for (int j= 0; j < names.length(); j++) {
                        String label = names.getString(j);
                        String key = successes.getString(label);
                        // Run through our records, set the synced flag
                        for (Top r : _changed_tops){
                            if (r.get_zotero_key().equals(key)){
                                r.set_synced(true);
                            }
                        }
                    }
                }
            }
            if (failures.length() > 0) {
                _callback.onPushItemsCompletion(new ZoteroResult(ZoteroResult.ZotError.PUSH_ITEM_TASK_1), version);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            _callback.onPushItemsCompletion(new ZoteroResult(ZoteroResult.ZotError.PUSH_ITEM_TASK_2), version);
        }

        _callback.onPushItemsCompletion(new ZoteroResult(), version);
    }
}
