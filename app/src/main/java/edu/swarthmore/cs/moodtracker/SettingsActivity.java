package edu.swarthmore.cs.moodtracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import edu.swarthmore.cs.moodtracker.db.ExportDataTask;

/**
 * Activity for settings.
 */
public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_PREF_EXPORT_DATA = "pref_export_data";

    private PreferenceFragment mSettingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new preference fragment
        mSettingsFragment = new SettingsFragment();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettingsFragment)
                .commit();
    }


    /**
     * Fragment that displays the preferences.
     * Android recommends using a fragment to display preferences after API 11.
     */
    public static class SettingsFragment extends PreferenceFragment {
        private SharedPreferences mSharedPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            // Listen to preference item clicks
            setupClickListeners();
        }

        /**
         * Setup onclick listeners for preferences.
         */
        private void setupClickListeners() {

            findPreference(KEY_PREF_EXPORT_DATA).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            SettingsFragment.this.exportDataToJSON();
                            return false;
                        }
                    }
            );
        }

        private void exportDataToJSON() {
            new ExportDataTask(getActivity()) {

                @Override
                public void onFinish(boolean result) {
                    if (result)
                        Toast.makeText(getActivity(), "Export Success", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), "Export Failed", Toast.LENGTH_SHORT).show();
                }
            }.execute();
        }

    }

}
