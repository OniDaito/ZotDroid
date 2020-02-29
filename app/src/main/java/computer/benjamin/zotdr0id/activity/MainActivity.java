package computer.benjamin.zotdr0id.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import computer.benjamin.zotdr0id.BuildConfig;
import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.ZotDroidApp;
import computer.benjamin.zotdr0id.ZotDroidMem;
import computer.benjamin.zotdr0id.auth.LoginActivity;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.data.OrderSearch;
import computer.benjamin.zotdr0id.data.zotero.Item;
import computer.benjamin.zotdr0id.data.zotero.SubNote;
import computer.benjamin.zotdr0id.data.zotero.Top;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.ops.Sync;
import computer.benjamin.zotdr0id.ops.Upgrade;
import computer.benjamin.zotdr0id.ops.User;
import computer.benjamin.zotdr0id.task.ZoteroResult;
import computer.benjamin.zotdr0id.task.callback.Upgraded;
import computer.benjamin.zotdr0id.task.callback.WebDavDown;
import computer.benjamin.zotdr0id.ux.ActivityWithDialogs;
import computer.benjamin.zotdr0id.ux.ZotdroidDialog;
import me.maxwin.view.XListView;
import computer.benjamin.zotdr0id.data.zotero.Attachment;
import computer.benjamin.zotdr0id.data.zotero.Collection;
import computer.benjamin.zotdr0id.data.zotero.Group;
import computer.benjamin.zotdr0id.data.zotero.Tag;
import computer.benjamin.zotdr0id.ux.SettingsActivity;
import computer.benjamin.zotdr0id.ux.ZotDroidDrawer;
import computer.benjamin.zotdr0id.ux.ZotDroidListAdapter;
import computer.benjamin.zotdr0id.ux.ZotDroidNewDrawer;

/**
 * TODO - if we cancel a sync, we need to not replace anything!
 */

public class MainActivity extends ActivityWithDialogs
        implements NavigationView.OnNavigationItemSelectedListener,
        computer.benjamin.zotdr0id.task.callback.Sync, WebDavDown, Upgraded,
        XListView.IXListViewListener, ZotDroidNewDrawer {

    public static final String      TAG = "zotdr0id.MainActivity";

    // TODO - too many dialogs - need another UX handler of somekind
    private Dialog                  _current_dialog; // Should only be one visible at a time?
    private ZotDroidListAdapter     _main_list_adapter;
    private XListView               _main_list_view;
    private Handler                 _handler;
    private Button                  _sync_button;
    private ZotDroidDrawer          _drawer;
    private Group                   _local_group = new Group(); // place holder for local items

    // Our main list memory locations
    ArrayList< String >                    _main_list_items = new ArrayList<>  ();
    HashMap< String, ArrayList<String> >   _main_list_sub_items =  new HashMap<>();

    // Our current mapping, given search and similar. List ID to Item basically
    SparseArray<Top>       _main_list_map = new SparseArray<>();

    /**
     * A small class that listens for Intents. Mostly used to change font size on the fly.
     */

    public class PreferenceChangeBroadcastReceiver extends BroadcastReceiver {
        public PreferenceChangeBroadcastReceiver () {}
        //private static final String TAG = "PreferenceChangeBroadcastReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("FONT_SIZE_PREFERENCE_CHANGED")){ changeFontSize();}
        }
    }

    PreferenceChangeBroadcastReceiver _broadcast_receiver;

    /**
     * onCreate as standard. Attempts to auth and if we arent authed, launches the login screen.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Log.i(TAG,"Creating ZotDroid...");
        setContentView(R.layout.activity_main);
        _broadcast_receiver = new PreferenceChangeBroadcastReceiver();

        // Setup the toolbar with the extra search and buttons
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _sync_button = (Button) findViewById(R.id.buttonToolbarSync);
        final MainActivity _ref = this;

        _sync_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ZoteroDeets.is_authed()) {
                    upgrade();
                } else {
                    //Log.i(TAG,"Not authed. Performing OAUTH.");
                    Intent loginIntent = new Intent(_ref, LoginActivity.class);
                    loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
                    startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
                }
            }
        });


        ZoteroDeets.load(this); // See if we are authed

        final Runnable run_layout = new Runnable() {
            public void run() {
                // Hide the intro text and show main view
                clearRecordList();
                if (haveItems()) {
                    removeHelp();
                    addMainList();
                    redrawRecordList();
                    buildDrawer();
                }

            }
        };

        // Set the font preference stuff
        IntentFilter filter = new IntentFilter("FONT_SIZE_PREFERENCE_CHANGED");
        registerReceiver(_broadcast_receiver, filter);

        // Search bar
        SearchView sv = (SearchView) findViewById(R.id.recordsearch);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                User user = ((ZotDroidApp)getApplication()).getUserOps();
                user.search(query);
                runOnUiThread(run_layout);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // I put a check in here to reset if everything is blank
                // Also initialise needs to have completed - sometimes this fires off when
                // it shouldn't
                User user = ((ZotDroidApp)getApplication()).getUserOps();
                if (user != null) {
                    if (newText.isEmpty()) {
                        user.reset();
                        runOnUiThread(run_layout);
                    } else {
                        // TODO - this requires some kind of view that grows continuously
                        // Start a partial search automatically
                    }
                }
                return false;
            }
        });

        // Link our ops to this activity - bit messy but means we always have only one ops object
        ((ZotDroidApp) getApplication()).getUserOps().set_callback(this);
        ((ZotDroidApp) getApplication()).getSyncOps().set_callback(this);
        ((ZotDroidApp) getApplication()).getUpgradeOps().set_callback(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        toggle.syncState();
        changeFontSize();
        sortPermissions();

        // If we have intent then ignore the init and perform this instead
        String ss = getIntent().getStringExtra("resetAndSync");
        if (ss != null) {
            if (ss.contentEquals("resetAndSync")) {
                _reset_and_sync();
                return;
            }
        }
        initialise();
    }

    private void _reset_and_sync () {
        final MainActivity _ref = this;
        if (ZoteroDeets.is_authed()) {
            new AlertDialog.Builder(this)
                    .setTitle("Reset and sync")
                    .setMessage("Do you really want to reset. This will remove all data from your device, except for downloaded attachments.?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            resetAndSync();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            initialise();
                        }
                    }).show();
        } else {
            //Log.i(TAG,"Not authed. Performing OAUTH.");
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
            this.startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
        }
    }


    /**
     * Get permission to read / write to external storage. If we already have it, initialise the filemanager.
     */
    private void sortPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            FileManager.intialise();
        }
    }

    private boolean haveItems(){
        ZotDroidApp app = (ZotDroidApp) getApplication();
        ZotDroidMem mem = app.getMem();
        if (mem.getTops().size() > 0){ return true;}
        return false;
    }

    private void addMainList(){
        // Setup the main list of items
        if (_main_list_view == null) {
            _main_list_view = (XListView) findViewById(R.id.listViewMain);
            _main_list_view.setPullLoadEnable(false);
            _handler = new Handler();
            _main_list_view.setXListViewListener(this);
        }
    }

    private void removeHelp(){
        TextView qb = (TextView)findViewById(R.id.intro_help_text);
        if (qb != null) {
            RelativeLayout rp = (RelativeLayout) qb.getParent();
            rp.removeView(qb);
        }
    }

    /**
     * Function when the Special Scrolling Listview is refreshed
     * This does nothing for now but might do later
     */
    @Override
    public void onRefresh() {
        _handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listLoaded();
            }
        }, 1000);
    }
    private void listLoaded() {
        _main_list_view.stopRefresh();
        _main_list_view.stopLoadMore();
        _main_list_view.setRefreshTime("-");
    }

    /**
     * Called when our special list view wants to load more things
     */
    @Override
    public void onLoadMore() {
        final User user = ((ZotDroidApp)getApplication()).getUserOps();
        if(user.hasMoreResults()) {
            _handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    user.getMoreResults(Constants.PAGINATION_SIZE);
                    expandRecordList();
                    _main_list_adapter.notifyDataSetChanged();
                    listLoaded();
                }
            }, 2000);
        }
    }

    private void initialise(){
        // Start initialisation in a separate thread for now.
        // Don't quite rememeber why I did this?
        // Magical runnable that performs re-layout when the list changes
        final Runnable run_layout = new Runnable() {
            public void run() {
                // Hide the intro text and show main view
                clearRecordList();
                if (haveItems()) {
                    removeHelp();
                    addMainList();
                    redrawRecordList();
                    buildDrawer();
                }
            }
        };

        final MainActivity _ref = this;
        _current_dialog = ZotdroidDialog.launchInitDialog(_ref);
        // TODO - is _current_dialog really the best thing?
        Runnable run = new Runnable() {
            public void run() {
                // Start tracing the bootup
                //Debug.startMethodTracing("zotdroid_trace_startup");
                ZotDroidApp app = (ZotDroidApp) getApplication();
                app.getUserOps().reset();
                // Stop tracing here.
                //Debug.stopMethodTracing();
                if (_current_dialog != null) {
                    _current_dialog.dismiss();
                    _current_dialog = null;
                }
                runOnUiThread(run_layout);
            }
        };

        Thread thread = new Thread(null, run, "Background");
        thread.start();
    }

    /**
     * Don't redraw completely, just check the size of the _main_list_items and add more
     * This is where we create the subitems to view from the records we have in memory.
     */
    private void expandRecordList() {
        ZotDroidApp app = (ZotDroidApp) getApplication();
        ZotDroidMem mem = app.getMem();

        int idx = 0;
        for (Top top : mem.getTops()) {
            if (idx >= _main_list_items.size()) {
                String tt = top.get_title();
                _main_list_map.put(_main_list_items.size(), top);
                _main_list_items.add(tt);

                // We add metadata first, followed by attachments (TODO - Add a divider?)
                ArrayList<String> tl = top.get_display_items();

                if (top.can_tags()) {
                    String tags = "Tags:";
                    StringBuilder sb = new StringBuilder();
                    for (Tag t : top.get_tags()) {
                        sb.append(" ");
                        sb.append(t.get_name());
                    }

                    tags += sb.toString();
                    tl.add(tags);
                }

                int max;
                if (top.can_notes()) {
                    for (SubNote n : top.get_sub_notes()) {
                        max = n.get_note().length();
                        max = max > Constants.NOTE_LISTING_MAX ? Constants.NOTE_LISTING_MAX : max;
                        tl.add("SubNote: " + n.get_note().subSequence(0, max) + "...");
                    }
                }

                if (top.can_attachments()) {
                    for (Attachment attachment : top.get_attachments()) {
                        tl.add("Attachment:" + attachment.get_file_name());
                    }
                }

                _main_list_sub_items.put(tt, tl);
            }
            idx +=1;
        }

        _main_list_view.setPullLoadEnable(false);
        User user = ((ZotDroidApp)getApplication()).getUserOps();
        if (user.hasMoreResults()) {
            _main_list_view.setPullLoadEnable(true);
        }
    }

    /**
     * We redraw our record list completely, based on the information held in the ZotDroidMem 'pool'
     */
    private void redrawRecordList() {
        if (_main_list_view == null) { return; }
        _main_list_items.clear();
        _main_list_map.clear();

        // Possibly a better way to pass font size but for now
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String font_size = "medium";
        font_size = settings.getString("settings_font_size",font_size);
        _main_list_adapter = new ZotDroidListAdapter(this,_main_list_items, _main_list_sub_items,font_size);
        _main_list_view.setAdapter(_main_list_adapter);
        expandRecordList();
        final MainActivity this_ref = this;

        // What happens when we click on a subitem
        _main_list_view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Top record;
                // TODO - Eventually we will replace TextView with some better class for this.
                int total = _main_list_adapter.getChildrenCount(groupPosition);
                // Overkill and messy ><
                int aidx;
                for (aidx = 0; aidx < total; aidx++){
                    String tv = (String)_main_list_adapter.getChild(groupPosition,aidx);
                    if (tv.contains("Attachment")){
                        break;
                    }
                }

                int nidx;
                for (nidx = 0; nidx < total; nidx++){
                    String tv = (String)_main_list_adapter.getChild(groupPosition,nidx);
                    if (tv.contains("SubNote") && !tv.contains("TopLevel")){
                        break;
                    }
                }

                User user = ((ZotDroidApp)getApplication()).getUserOps();
                String tv = (String)_main_list_adapter.getChild(groupPosition,childPosition);
                // TODO - This is a bit flimsy! :(
                // Check to see if the download exists - grab it if not, or open if it does.
                if (tv.contains("Attachment")) {
                    record = _main_list_map.get(groupPosition);
                    if (record != null) {
                        if(!user.getAttachmentDownloadOpen(record, childPosition - aidx, this_ref)) {
                            _current_dialog = ZotdroidDialog.launchDownloadDialog(this_ref);
                        }
                    }
                } else if (tv.contains("Tags")) {
                    // Cheeky cast!
                    final Item r = (Item)_main_list_map.get(groupPosition);
                    _current_dialog = ZotdroidDialog.launchTagDialog(this_ref, r);
                } else if (tv.contains("SubNote")) {
                    final Top r = _main_list_map.get(groupPosition);
                    final SubNote n = r.get_sub_notes().get(childPosition - nidx);
                    _current_dialog = ZotdroidDialog.launchNoteDialog(this_ref, r, n);
                }

                return true;
            }
        });
    }

    private void changeFontSize() {
        if (_main_list_view == null) { return;} // Can be null because of intro help screen
        TextView groupTitle = _main_list_view.findViewById(R.id.main_list_group);
        TextView groupSubText = _main_list_view.findViewById(R.id.main_list_subtext);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String font_size = settings.getString("settings_font_size","medium");

        if (groupTitle != null ) {
            if (font_size.contains("small")) {
                groupTitle.setTextAppearance(this, R.style.MainList_Title_Small);
            } else if (font_size.contains("medium")) {
                groupTitle.setTextAppearance(this, R.style.MainList_Title_Medium);
            } else if (font_size.contains("large")) {
                groupTitle.setTextAppearance(this, R.style.MainList_Title_Large);
            } else {
                groupTitle.setTextAppearance(this, R.style.MainList_Title_Medium);
            }
        }
        if (groupSubText != null ) {
            if (font_size.contains("small")){ groupSubText.setTextAppearance(this, R.style.MainList_SubText_Small);}
            else if (font_size.contains("medium")){ groupSubText.setTextAppearance(this, R.style.MainList_SubText_Medium);}
            else if (font_size.contains("large")) { groupSubText.setTextAppearance(this, R.style.MainList_SubText_Large);}
            else { groupSubText.setTextAppearance(this, R.style.MainList_SubText_Medium);}
        }

        // This is expensive but I think it's what we have to do really.
        ((ZotDroidApp)getApplication()).getUserOps().reset();
        redrawRecordList();
        buildDrawer();
    }

    /**
     * Reset everything and do a full sync from scratch
     */
    protected void resetAndSync() {
        Sync sync = ((ZotDroidApp)getApplication()).getSyncOps();
        _current_dialog = ZotdroidDialog.launchLoadingDialog(this);
        sync.resetAndSync();
    }

    /**
     * Do a standard, partial sync.
     */
    protected void sync() {
        Sync sync = ((ZotDroidApp)getApplication()).getSyncOps();
        sync.sync();
        _current_dialog = ZotdroidDialog.launchLoadingDialog(this);
    }

    @Override
    public void onSyncProgress(ZoteroResult result, float progress) {
        // TODO - update the progress on the sync dialog
        if (_current_dialog != null) {
            String status_message = "Syncing with Zotero: " + Float.toString(Math.round(progress * 100.0f)) + "% complete.";
            TextView messageView = _current_dialog.findViewById(R.id.textViewLoading);
            messageView.setText(status_message);
            //Log.i(TAG,status_message);
        }
    }

    @Override
    public void onSyncFinish(ZoteroResult result) {
        removeHelp();
        addMainList();
        User user = ((ZotDroidApp)getApplication()).getUserOps();
        user.reset();
        redrawRecordList();
        buildDrawer();
        if (_current_dialog != null) {
            _current_dialog.dismiss();
            //Log.i(TAG,"Sync Version: " + _zotdroid_sync_ops.getVersion());
        }
    }

    /**
     * Called when we are running low on space for the database
     */
    @Override
    public void onLowSpace() {
        if (_current_dialog != null){ _current_dialog.dismiss();}

        AlertDialog.Builder builder  = new AlertDialog.Builder(this, R.style.ZotDroidAlertDialogStyle);
        builder.setTitle(R.string.low_free_space_title)
                .setMessage(R.string.low_free_space_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                //this.startActivityForResult(new Intent(this, SettingsActivity.class), 1);
                Intent si = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(si);
                return true;
            /*case R.id.action_reset_sync:
                if (ZoteroDeets.is_authed()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Reset and sync")
                            .setMessage("Do you really want to reset. This will remove all data from your device, except for downloaded attachments.?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    resetAndSync();
                                }
                            }).setNegativeButton(android.R.string.no, null).show();

                } else {
                    //Log.i(TAG,"Not authed. Performing OAUTH.");
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
                    this.startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
                }
                return true;*/
            /*case R.id.action_sync:
                if (ZoteroDeets.is_authed()) {
                    upgrade();
                } else {
                    //Log.i(TAG,"Not authed. Performing OAUTH.");
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
                    this.startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
                }
                return true;

            case R.id.action_test_webdav:
                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                Boolean custom_webdav = settings.getBoolean("settings_use_webdav_storage",false);
                if (custom_webdav) {
                    startTestWebDav();
                } else {
                    AlertDialog.Builder builder  = new AlertDialog.Builder(this, R.style.ZotDroidAlertDialogStyle);
                    builder.setTitle(R.string.testing_webdav_local_title)
                            .setMessage(R.string.testing_webdav_local_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return true;*/

            case R.id.action_download_all:
                if (ZoteroDeets.is_authed()) {
                    new AlertDialog.Builder(this)
                            .setTitle("Download all Attachments")
                            .setMessage("Do you really want to download all attachments? This may take a long time depending on the number and size of attachments.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    downloadAllAttachments();
                                }
                            }).setNegativeButton(android.R.string.no, null).show();

                } else {
                    //Log.i(TAG,"Not authed. Performing OAUTH.");
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
                    this.startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
                }
                return true;

            case R.id.action_order_author: {
                User user = ((ZotDroidApp) getApplication()).getUserOps();
                user.changeOrder(new OrderSearch(OrderSearch.Order.AUTHOR));
                redrawRecordList();
                return true;
            }

            case R.id.action_order_basic: {
                User user = ((ZotDroidApp) getApplication()).getUserOps();
                user.changeOrder(new OrderSearch(OrderSearch.Order.BASIC));
                redrawRecordList();
                return true;
            }

            case R.id.action_order_title: {
                User user = ((ZotDroidApp) getApplication()).getUserOps();
                user.changeOrder(new OrderSearch(OrderSearch.Order.TITLE));
                redrawRecordList();
                return true;
            }

            case R.id.action_show_about:
                _current_dialog = ZotdroidDialog.launchAboutDialog(this);
                return true;

            case R.id.action_show_help:
                _current_dialog = ZotdroidDialog.launchHelpDialog(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileManager.intialise();
                } else {
                    // Show warning dialog
                    AlertDialog.Builder builder  = new AlertDialog.Builder(this, R.style.ZotDroidAlertDialogStyle);
                    builder.setTitle(R.string.testing_webdav_local_title)
                            .setMessage(R.string.request_write_permission)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                // Now ask for the next thing! :)
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }

                return;
            }
            case Constants.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Show warning dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ZotDroidAlertDialogStyle);
                    builder.setTitle(R.string.permissions_not_granted)
                            .setMessage(R.string.permissions_not_granted_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Simple function to clear the record list
     */
    public void clearRecordList() {
        _main_list_items.clear();
        _main_list_map.clear();
        if (_main_list_adapter != null) { _main_list_adapter.notifyDataSetChanged(); }
    }

    /**
     * A subroutine to set the left-hand collections drawer
     * We order by groups first and then alphabetical
     */
    public void buildDrawer() {
        ViewAnimator drawer_view = (ViewAnimator)findViewById(R.id.left_drawer_view);
        if (drawer_view != null) { drawer_view.removeAllViews(); }
        _drawer = new ZotDroidDrawer(this, this);

        // Now create our lefthand drawer from the collections
        drawer_view.removeView(_drawer.getView());
        //LinearLayout linear_layout = new LinearLayout(this);
        //linear_layout.setOrientation(LinearLayout.VERTICAL);
        ZotDroidApp app = (ZotDroidApp) getApplication();
        ZotDroidMem mem = app.getMem();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String font_size = "medium";
        font_size = settings.getString("settings_font_size",font_size);

        // Do all groups
        /*for (Group g : mem._groups){
            ZotDroidDrawer new_drawer;
            TextView title = new TextView(this);
            title = new TextView(this);
            // TODO - this text change bit could be a lot better
            title.setTextAppearance(this, R.style.DrawerList_Title_Medium);
            if (font_size.contains("small")) {
                title.setTextAppearance(this, R.style.DrawerList_Title_Small);
            } else if (font_size.contains("large")) {
                title.setTextAppearance(this, R.style.DrawerList_Title_Large);
            }

            title.setText(g.get_title());
            linear_layout.addView(title);

            //_drawer.add(g);
        }*/

        _drawer.populate(((ZotDroidApp)getApplication()).getMem());
        drawer_view.addView(_drawer.getView());

        //drawer_view.addView(linear_layout);
    }

    public void onDownloadProgress(float progress) {
        // We dont always get progress - we occasionally get a -1 due to Zotero badness
        if (_current_dialog == null) { return; }
        if (progress == -1.0){
            TextView messageView = _current_dialog.findViewById(R.id.textViewDownloading);
            String tt = messageView.getText().toString();
            if (tt.length() >= 13){
                messageView.setText("Progress: ");
            } else {
                messageView.setText(tt + ".");
            }
        } else {
            String status_message = "Progress: " + Float.toString(progress) + "%";
            TextView messageView = _current_dialog.findViewById(R.id.textViewDownloading);
            messageView.setText(status_message);
        }
        //Log.i(TAG, status_message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == Constants.INTENT_RESET_SYNC){
            if (ZoteroDeets.is_authed()) {
                new AlertDialog.Builder(this)
                        .setTitle("Reset and sync")
                        .setMessage("Do you really want to reset. This will remove all data from your device, except for downloaded attachments.?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                resetAndSync();
                            }
                        }).setNegativeButton(android.R.string.no, null).show();

            } else {
                //Log.i(TAG,"Not authed. Performing OAUTH.");
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.setAction("zotdr0id.LoginActivity.LOGIN");
                this.startActivityForResult(loginIntent, Constants.ZOTERO_LOGIN_REQUEST);
            }
        }

        if (resultCode == Constants.ZOTERO_LOGIN_REQUEST) {
            // TODO - we want to automatically start sync nce we've returned from a successful auth
            sync();
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(_broadcast_receiver);
        super.onDestroy();
    }

    /**
     * Called when we select a new collection - lets redraw and animate our drawer if
     * we have more subcollections. Set a few titles such as main and searchbar hint.
     */
    @Override
    public void onNewDrawerSelected(Group g, Collection c) {
        if (g != null) {
            clearRecordList();
            ((ZotDroidApp) getApplication()).getUserOps().swapCollection(g, c);
            redrawRecordList();
        }
        /*setDrawer(g, c);
        ViewAnimator drawer_view = (ViewAnimator)findViewById(R.id.left_drawer_view);
        drawer_view.showNext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        SearchView sv = (SearchView) findViewById(R.id.recordsearch);
        if (c != null) {
            toolbar.setTitle("ZotDroid: " + c.get_title());
            sv.setQueryHint("Search: " + c.get_title() );
        } else {
            toolbar.setTitle("ZotDroid:");
            sv.setQueryHint("Search ALL");
        }*/
    }

    public String getStringResource(int idx) {
        return getResources().getString(idx);
    }

    /**
     * Called before sync if we need to update the database with more requests.
     * Means we don't reset and recreate all the time, even though that'd be easier.
     */
    protected void upgrade(){
        Upgrade upgrade = ((ZotDroidApp)getApplication()).getUpgradeOps();

        if(upgrade.upgrade()){
            // Create and show our dialog
            if (_current_dialog != null) {
                _current_dialog = ZotdroidDialog.launchUpgradeDialog(this);
            }
        } else {
            sync();
        }
    }


    public void downloadAllAttachments() {
        if (_current_dialog != null) {
            _current_dialog = ZotdroidDialog.launchDownloadDialog(this);
        }
        ((ZotDroidApp)getApplication()).getUserOps().downloadAllAttachments(this);
    }

    @Override
    public void onUpgradeProgress(ZoteroResult result) {

    }

    @Override
    public void onUpgradeFinish(ZoteroResult result) {

    }

    @Override
    /**
     * Called by the ZoteroRequest from UserOps when we are downloading an attachment.
     */
    public void onDownloadProgress(ZoteroResult result, float progress) {
        // We dont always get progress - we occasionally get a -1 due to Zotero badness
        if (_current_dialog == null) { return; }
        if (progress == -1.0){
            TextView messageView = _current_dialog.findViewById(R.id.textViewDownloading);
            String tt = messageView.getText().toString();
            if (tt.length() >= 13){
                messageView.setText("Progress: ");
            } else {
                messageView.setText(tt + ".");
            }
        } else {
            String status_message = "Progress: " + Float.toString(progress) + "%";
            TextView messageView = _current_dialog.findViewById(R.id.textViewDownloading);
            messageView.setText(status_message);
        }
        //Log.i(TAG, status_message);
    }

    @Override
    public void onDownloadFinish(final ZoteroResult result, final Attachment attachment) {
        final MainActivity _ref = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (_current_dialog != null) {
                    ProgressBar pb = _current_dialog.findViewById(R.id.progressBarDownload);
                    pb.setVisibility(View.INVISIBLE);
                }
                if (!result.isSuccess()) {
                    String status_message = "Error: " + attachment.get_file_name()
                            + ", " + result.toString();
                    if (_current_dialog != null) {
                        TextView messageView =
                                _current_dialog.findViewById(R.id.textViewDownloading);
                        messageView.setText(status_message);
                    }
                    //Log.i(TAG, status_message);
                } else {
                    Intent intent = new Intent();
                    File ff7 = new File(FileManager.getAttachmentsDirectory()
                            + "/" + attachment.get_file_name());
                    if (ff7.exists()) {
                        intent.setAction(Intent.ACTION_VIEW);
                        //Log.i(TAG, "Attempting to open " + message);
                        try {
                            Uri uri = Uri.fromFile(ff7);
                            // Allegedly we need to have this for Android 7+ ?
                            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                uri = FileProvider.getUriForFile(MainActivity.this,
                                        BuildConfig.APPLICATION_ID + ".provider", ff7);
                            } else { intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); } // This causes issues on Android 7+
                            String ext = ff7.getName().substring(ff7.getName().lastIndexOf(".") + 1);
                            MimeTypeMap mime = MimeTypeMap.getSingleton();
                            String type = mime.getMimeTypeFromExtension(ext);
                            //intent.setDataAndType(Uri.fromFile(ff7), filetype);
                            intent.setDataAndType(uri, type);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                            if (_current_dialog != null) {
                                _current_dialog.dismiss();
                                _current_dialog = null;
                            }
                        } catch (Exception e) {
                            //Log.d(TAG, "Error opening file");
                            String status_message =
                                    "Error: No program available to open this attachment.";
                            if (_current_dialog != null) {
                                _current_dialog.dismiss();
                            }

                            _current_dialog =
                                    ZotdroidDialog.launchErrorDialog(_ref, status_message);

                            e.printStackTrace();
                        }
                        _main_list_view.invalidate();
                        _main_list_view.invalidateViews();
                        _main_list_view.refreshDrawableState();

                    } else {
                        String status_message = "Error: " + attachment.get_file_name()
                                + " does not appear to exist.";
                        if (_current_dialog != null) {
                            _current_dialog.dismiss();
                        }

                        _current_dialog =
                                ZotdroidDialog.launchErrorDialog(_ref, status_message);
                        //Log.i(TAG, status_message);
                    }
                }
            }
        });
    }

    @Override
    public void onDownloadAllProgress(ZoteroResult result, float progress) {

    }

    @Override
    public void onDownloadAllFinish(ZoteroResult result) {

    }

    /**
     * Called when a user hits cancel and kills the dialog. It's annoying to do this, but as
     * this activity 'owns' the dialog, it's the only real way to do it I think.
     */
    @Override
    public void onClosedDialog() {
        _current_dialog = null;
    }

}
