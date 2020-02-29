package computer.benjamin.zotdr0id.task;

/**
 * Created by oni on 21/07/2017.
 * Get the number of total items so we can guesstimate whether we have enough db space
 */

import org.json.JSONException;
import org.json.JSONObject;
import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.task.callback.Task;

public class Sizer extends ZoteroGet {
    private static final String TAG = "Sizer";
    Task callback;
    private String _url = "";

    public Sizer(Task callback) {
        this.callback = callback;
        _url = Constants.BASE_URL + "/users/" + ZoteroDeets.get_userid() + "/items";

    }

    /**
     * Actually execute the async task.
     * Not quite an override but it sets up the string for us when we do actually execute, so things are in sync
     * For some reason, it only works if I pass the start (and possibly limit) as URL params instead of headers
     * but the desc and dateAdded seem ok. It could be an integer thing I suspect
     */
    public void startZoteroTask(){
        execute(_url, "direction", "desc", "sort", "dateAdded");
    }

    protected void onPostExecute(String rstring) {
        // Check we didn't get a failure on that rsync call
        if (rstring.equals("FAIL")){
            callback.onSizeCompletion(new ZoteroResult(ZoteroResult.ZotError.SIZE_TASK_0), -1);
            return;
        }

        try {
            JSONObject jObject = new JSONObject(rstring);
            // Grab the totals and the versions from the headers we found
            int total = 0;
            try {
                total = jObject.getInt("Total-Results");
            } catch (JSONException e) {
                //Log.i(TAG,"No Total-Results in request.");
                callback.onSizeCompletion(new ZoteroResult(ZoteroResult.ZotError.SIZE_TASK_1), -1);
            }

            callback.onSizeCompletion(new ZoteroResult(), total);

        } catch (JSONException e) {
            e.printStackTrace();
            //Log.e(TAG,"Error in parsing JSON Object.");
            callback.onSizeCompletion(new ZoteroResult(ZoteroResult.ZotError.SIZE_TASK_2), -1);
        }
    }
}
