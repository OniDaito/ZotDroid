package computer.benjamin.zotdr0id.ux;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

import computer.benjamin.zotdr0id.Constants;
import computer.benjamin.zotdr0id.activity.MainActivity;
import computer.benjamin.zotdr0id.R;
import computer.benjamin.zotdr0id.Util;
import computer.benjamin.zotdr0id.auth.ZoteroDeets;
import computer.benjamin.zotdr0id.file.FileManager;
import computer.benjamin.zotdr0id.task.TestWebDav;
import computer.benjamin.zotdr0id.task.ZoteroResult;
import computer.benjamin.zotdr0id.task.callback.WebDavTest;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    //private static final String TAG = "zotdroid.Settings";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);

            }
            return true;
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.INTENT_RESET_SYNC) {
            this.finish();
        }
    }

    /**
     * Clear the Auth state so we can restart.
     */
    public void clearAuth() { ZoteroDeets.set_authed(false,this); }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || WebDavDownPreferenceFragment.class.getName().equals(fragmentName)
                || ExperimentalPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        /**
         * Another listener that I've made to post messages for certain changes
         * It creates a popup dialog to warn the user to restart ZotDroid
         */
        private Preference.OnPreferenceChangeListener _messenger_db_location;
        private Preference.OnPreferenceChangeListener _messenger_download_location;
        private Preference.OnPreferenceChangeListener _messenger_username;
        private Preference.OnPreferenceChangeListener _messenger_font_size;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            final GeneralPreferenceFragment _ref  = this;
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("settings_user_id"));
            bindPreferenceSummaryToValue(findPreference("settings_username"));
            bindPreferenceSummaryToValue(findPreference("settings_user_secret"));
            bindPreferenceSummaryToValue(findPreference("settings_user_key"));
            bindPreferenceSummaryToValue(findPreference("settings_download_location"));
            bindPreferenceSummaryToValue(findPreference("settings_db_location"));

            Preference button = findPreference(getString(R.string.reset_and_sync));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    //Intent intent = new Intent(getActivity().getApplicationContext(),
                     //       MainActivity.class);
                    Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    intent.putExtra("resetAndSync","resetAndSync");
                    //startActivityForResult(intent,Constants.INTENT_RESET_SYNC);
                    //getActivity().setResult(Constants.INTENT_RESET_SYNC, null);
                    //getActivity().finish();
                    //Activity a = getActivity();
                    //getActivity().finishActivity(Constants.INTENT_RESET_SYNC);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                    return true;
                }
            });

            _messenger_db_location = new Preference.OnPreferenceChangeListener() {
                AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    final String new_value = o.toString();

                    if (preference == findPreference("settings_db_location")){
                        builder.setTitle(R.string.moving_database_title)
                                .setMessage(R.string.moving_database_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        preference.getEditor().putString(new_value, new_value);
                                        preference.setSummary(new_value);
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    return true;
                }
            };

            // Flash an alert dialog to warn the user if this download path is no good - this
            // responsibility is now handled via the File.FileManager class
            _messenger_download_location = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    final String new_value = o.toString();
                    if (preference == findPreference("settings_download_location")) {
                        String tvalue = Util.remove_trailing_slash(new_value);
                        FileManager.setDownloadDirectory(tvalue);
                        preference.getEditor().putString(tvalue, tvalue);
                        preference.setSummary(tvalue);
                    }
                    return true;
                }
            };

            _messenger_username = new Preference.OnPreferenceChangeListener() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    final String new_value = o.toString();
                    final String sp = preference.getSharedPreferences().getString("settings_username","");
                    if (!new_value.equals(sp)) {
                        if (preference == findPreference("settings_username")){
                            builder.setTitle(R.string.settings_username_title)
                                    .setMessage(R.string.settings_username_message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            preference.getEditor().putString(new_value, new_value);
                                            preference.setSummary(new_value);
                                            SettingsActivity sa = (SettingsActivity) getActivity();
                                            sa.clearAuth();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                    return true;
                }
            };

            _messenger_font_size = new Preference.OnPreferenceChangeListener() {
                AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (preference == findPreference("settings_font_size")){
                        // TODO - change the major text elements here
                        Intent fontChangeIntent = new Intent();
                        String value = o.toString();
                        fontChangeIntent.setAction("FONT_SIZE_PREFERENCE_CHANGED");
                        fontChangeIntent.putExtra("fontsize",value);
                        getActivity().sendBroadcast(fontChangeIntent);
                    }
                    return true;
                }
            };

            findPreference("settings_db_location").setOnPreferenceChangeListener(_messenger_db_location);
            findPreference("settings_font_size").setOnPreferenceChangeListener(_messenger_font_size);
            findPreference("settings_username").setOnPreferenceChangeListener(_messenger_username);
            findPreference("settings_download_location").setOnPreferenceChangeListener(_messenger_download_location);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows webdav preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class WebDavDownPreferenceFragment extends PreferenceFragment implements WebDavTest {

        WebDavDownPreferenceFragment _ref = this;
        // TODO - testwebdav might just need strings instead of the activity
        private final TestWebDav _testWebDav = new TestWebDav(getActivity(), this);
        private Dialog _webdav_dialog;
        private Button _webdav_button;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            _webdav_dialog = createLoadingDialog();
            _webdav_button = new Button(getActivity());
            //_webdav_button.setText(R.string.test_webdav_settings);
            //setListFooter(_webdav_button);

            /*_webdav_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SharedPreferences settings =
                            PreferenceManager.getDefaultSharedPreferences(getActivity());

                    Boolean custom_webdav = settings.getBoolean("settings_use_webdav_storage",false);
                    if (custom_webdav) {
                        _webdav_dialog.show();
                        String status_message = "Testing Webdav Connection.";
                        TextView messageView = _webdav_dialog.findViewById(R.id.textViewLoading);
                        messageView.setText(status_message);
                        _zotero_download.testWebDav(getActivity(), _ref);
                    } else {
                        AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(),
                                R.style.ZotDroidAlertDialogStyle);
                        builder.setTitle(R.string.testing_webdav_local_title)
                                .setMessage(R.string.testing_webdav_local_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {}
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    //_zotdroid_user_ops.testWebDav();
                }
            });*/

            addPreferencesFromResource(R.xml.pref_webdav);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("settings_webdav_address"));
            bindPreferenceSummaryToValue(findPreference("settings_webdav_username"));
            Preference settings_webdav_password = findPreference("settings_webdav_password");
            settings_webdav_password.setSummary("hidden");

            Preference.OnPreferenceChangeListener webdav_settings_verifier = new Preference.OnPreferenceChangeListener() {
                AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    if (preference == findPreference("settings_webdav_address")){
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                        final String new_value = o.toString();
                        final String sp = preference.getSharedPreferences().getString("settings_webdav_address","");
                        // Dont need to check this username
                        /*if (!new_value.equals(sp)) {
                            if (preference == findPreference("settings_webdav_address")){
                                builder.setTitle(R.string.settings_username_title)
                                .setMessage(R.string.settings_username_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        preference.getEditor().putString(new_value, new_value);
                                        preference.setSummary(new_value);
                                        SettingsActivity sa = (SettingsActivity) getActivity();
                                        sa.clearAuth();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                            }
                        }*/

                        if (!new_value.contains("https")) {
                            builder.setTitle("WebDavDown Security")
                                    .setMessage(R.string.https_warning_message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            EditTextPreference editTextPref = (EditTextPreference) findPreference("settings_webdav_address");
                                            editTextPref.setSummary(new_value);
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }

                        if (!new_value.contains("zotero")) {
                            builder.setTitle("WebDavDown Address Warning")
                                    .setMessage(R.string.zotero_address_warning_message)
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            EditTextPreference editTextPref = (EditTextPreference) findPreference("settings_webdav_address");
                                            editTextPref.setSummary(new_value);
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                    return true;
                }
            };

            findPreference("settings_webdav_address").setOnPreferenceChangeListener(webdav_settings_verifier);

            // Test Webdav Settings button
            Preference button_test_webdav = findPreference(getString(R.string.test_webdav_settings));
            button_test_webdav.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    Boolean custom_webdav = settings.getBoolean("settings_use_webdav_storage",false);
                    if (custom_webdav) {
                        _webdav_dialog.show();
                        String status_message = "Testing Webdav Connection.";
                        TextView messageView = _webdav_dialog.findViewById(R.id.textViewLoading);
                        messageView.setText(status_message);
                        _testWebDav.go_test();
                    } else {
                        AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                        builder.setTitle(R.string.testing_webdav_local_title)
                                .setMessage(R.string.testing_webdav_local_message)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {}
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                    return true;
                }
            });
        }

        private Dialog createLoadingDialog() {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_loading);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(true);

            ProgressBar pb = (ProgressBar) dialog.findViewById(R.id.progressBarLoading);
            pb.setVisibility(View.VISIBLE);

            DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
            int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
            int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
            dialog.getWindow().setLayout(dialogWidth, dialogHeight);

            Button cancelButton = (Button) dialog.findViewById(R.id.buttonCancelLoading);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //_zotdroid_sync_ops.stop();
                    dialog.dismiss();
                }
            });

            return dialog;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onTestFinish(ZoteroResult result) {
            String status_message = "Connection Failed.";
            if (result.isSuccess()) {
                status_message = "Connection succeded";
            }
            TextView messageView = _webdav_dialog.findViewById(R.id.textViewLoading);
            messageView.setText(status_message);
            Button button = _webdav_dialog.findViewById(R.id.buttonCancelLoading);
            button.setText(R.string.zotdroid_dismiss);

            ProgressBar pb = _webdav_dialog.findViewById(R.id.progressBarLoading);
            pb.setVisibility(View.INVISIBLE);
            _webdav_button.setClickable(true);
        }
    }


    /**
     * This fragment shows webdav preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExperimentalPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_experimental);
            setHasOptionsMenu(true);
            //bindPreferenceSummaryToValue(findPreference("settings_unzip_on_all"));


            Preference.OnPreferenceChangeListener experimental_settings_verifier = new Preference.OnPreferenceChangeListener() {
                AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity(), R.style.ZotDroidAlertDialogStyle);
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (preference == findPreference("settings_unzip_on_all")){
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    }
                    return true;
                }
            };
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


}
