package computer.benjamin.zotdr0id.ux;

import android.app.Activity;
import android.app.Dialog;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Tag;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.ops.Sync;
import computer.benjamin.zotdr0id.ops.User;

/**
 * Created by oni on 17/04/2018.
 * TODO - should we make these none static? We do alter the progress in some of these that suggests
 * we need to keep hold of state.
 */

public class ZotdroidDialog {

    public static Dialog launchInitDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        //_init_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_init);
        dialog.setCanceledOnTouchOutside(false);
        ProgressBar pb = dialog.findViewById(R.id.progressBarInit);
        pb.setVisibility(View.VISIBLE);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        dialog.show();
        return dialog;
    }

    public static Dialog launchAboutDialog(final ActivityWithDialogs activity) {
        final Dialog dialog = new Dialog(activity);
        //_about_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        Button cancelButton = (Button) dialog.findViewById(R.id.buttonAboutOk);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); activity.onClosedDialog(); }
        });
        dialog.show();
        return dialog;
    }

    public static Dialog launchErrorDialog(final ActivityWithDialogs activity, String error_msg) {
        final Dialog dialog = new Dialog(activity);
        //_about_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        TextView messageView = dialog.findViewById(R.id.textViewError);
        messageView.setText(error_msg);
        Button cancelButton = (Button) dialog.findViewById(R.id.buttonErrorCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { dialog.dismiss(); activity.onClosedDialog(); }
        });
        dialog.show();
        return dialog;
    }

    public static Dialog launchHelpDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        //_help_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.help);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.75);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.65);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);

        WebView helpview = (WebView) dialog.findViewById(R.id.help_webview);
        //if(helpview != null) {
        //helpview.loadUrl("https://www.google.com");
        helpview.loadUrl("https://zotdroid.benjamin.computer/help");
        //}
        dialog.show();
        return dialog;
    }

    /**
     * A handy function that loads our dialog to show we are loading.
     * TODO - needs messages to show what we are doing.
     * https://stackoverflow.com/questions/37038835/how-do-i-create-a-popup-overlay-view-in-an-activity-without-fragment
     */
    public static Dialog launchLoadingDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        //_loading_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        ProgressBar pb = (ProgressBar) dialog.findViewById(R.id.progressBarLoading);
        pb.setVisibility(View.VISIBLE);

        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        Button cancelButton = (Button) dialog.findViewById(R.id.buttonCancelLoading);

        final Sync syncops = ((ZotDroidApp)activity.getApplication()).getSyncOps();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncops.stop();
                dialog.dismiss();
            }
        });

        dialog.show();
        return dialog;
    }


    public static Dialog launchNoteDialog(Activity activity, Top item, SubNote subNote){
        final Dialog dialog = new Dialog(activity);
        //_note_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_notes);
        dialog.setCanceledOnTouchOutside(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.75);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);

        final SubNote n = subNote;
        Button qb = dialog.findViewById(R.id.fragment_notes_quit);
        final TextView ll = dialog.findViewById(R.id.fragment_notes_note);

        final User userops =  ((ZotDroidApp)activity.getApplication()).getUserOps();
        qb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Write back the notes
                n.set_note(ll.getText().toString());
                userops.commitSubNote(n, false);
                // Later we will need to update this too.
                //_zotdroid_user_ops.commitRecord(r); // Change to be synced
                dialog.dismiss();
            }
        });

        ll.setText(Html.fromHtml(n.get_note()));
        dialog.show();
        return dialog;
    }

    /**
     * Launch a loading dialog for showing progress and the like
     */

    public static Dialog launchDownloadDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        //_download_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_downloading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);

        Button cancelButton = (Button) dialog.findViewById(R.id.buttonCancelDownload);
        final User userops = ((ZotDroidApp)activity.getApplication()).getUserOps();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                userops.stop();
            }
        });

        dialog.show();
        return dialog;
    }

    public static Dialog launchTagDialog(Activity activity, Item item){
        final Dialog dialog = new Dialog(activity);
        //_tag_dialog = dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tags);
        dialog.setCanceledOnTouchOutside(true);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        final int dialogWidth = (int)(displayMetrics.widthPixels * 0.75);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.75);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        final Item r = item;
        final LinearLayout ll = dialog.findViewById(R.id.fragment_tags_list);

        Button qb = dialog.findViewById(R.id.fragment_tags_newtag_button);
        final TextView tv = (TextView) dialog.findViewById(R.id.fragment_tags_newtag);
        final User ops_ref = ((ZotDroidApp)activity.getApplication()).getUserOps();
        final Activity act_ref = activity;

        qb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Tag tt : r.get_tags()){
                    if (tt.get_name().equals(tv.getText().toString())){ return; }
                }

                Tag newtag = new Tag(tv.getText().toString(),r.get_zotero_key());
                r.add_tag(newtag);
                ops_ref.commitItem(r, false); // Change to be synced
                _addTag(act_ref, newtag,r,ll, dialogWidth);
                //TODO - need to update the view here
            }
        });

        Button ab = dialog.findViewById(R.id.fragment_tags_quit);
        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        for (Tag t : r.get_tags()){ _addTag(activity, t,r,ll,dialogWidth); }
        dialog.show();
        return dialog;
    }

    protected static void _addTag(Activity activity, Tag t, Item item, LinearLayout topview, int dialogWidth) {
        final Tag tag = t;
        final Item r = item;
        final LinearLayout ll = topview;
        final LinearLayout lt = new LinearLayout(activity);
        lt.setOrientation(LinearLayout.HORIZONTAL);
        lt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        final TextView tf = new TextView(activity);
        tf.setText(tag.get_name());
        tf.setMinimumWidth((int)(dialogWidth * 0.65));
        lt.addView(tf);
        // Button for removal of tags
        final Button bf = new Button(activity);
        final User ops_ref = ((ZotDroidApp)activity.getApplication()).getUserOps();
        bf.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        bf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                r.remove_tag(tag);
                ops_ref.commitItem(r, false);
                ll.removeView(lt);
                //redrawRecordList();
            }
        });
        bf.setText("-");
        lt.addView(bf);
        lt.setVisibility(View.VISIBLE);
        ll.addView(lt,0);

        //redrawRecordList();
    }

    public static Dialog launchUpgradeDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_upgrading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        dialog.show();
        return dialog;
    }

}
