package edu.swarthmore.cs.moodtracker;

/**
 * Created by Peng on 10/19/2014.
 */

import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.swarthmore.cs.moodtracker.R;
import edu.swarthmore.cs.moodtracker.TrackService;
import edu.swarthmore.cs.moodtracker.util.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.util.AppUsageListAdapter;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows App Usage tracking information.
 */
public class AppUsageSectionFragment extends Fragment {
    public static final String TAG = "AppUsageSectionFragment";
    public static final String STATE_DATE_RANGE_SELECTION = "date_range_selection";
    public static final String STATE_DISPLAY_LIMIT_SELECTION = "display_limit_selection";

    // Initialization Variables. See allInitialized() for more details.
    private boolean mResumeInitialized = false;
    private boolean mDateRangeInitialized = false;
    private boolean mDisplayLimitInitialized = false;

    // Filter variables.
    private int mDateRange = 0;
    private int mDisplayLimit = -1;

    // Other saved variables.
    private View mWaitingView = null;
    private View mContentView = null;
    private ListView mAppUsageListView = null;
    private Spinner mDateRangeSpinner = null;
    private Spinner mDisplayLimitSpinner = null;
    private TrackService mService = null;


    /*-----------------------*/
    /*        Methods        */
    /*-----------------------*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_section_app_usage, container, false);
        mContentView = rootView.findViewById(R.id.app_usage_content_view);
        mWaitingView = rootView.findViewById(R.id.app_usage_waiting_view);
        mAppUsageListView = (ListView) rootView.findViewById(R.id.app_usage_list);
        mDateRangeSpinner = (Spinner) rootView.findViewById(R.id.app_usage_date_range_spinner);
        mDisplayLimitSpinner = (Spinner) rootView.findViewById(R.id.app_usage_display_limit_spinner);

        syncLayoutWithService();
        setupFilter();

        // Before we get connected, just display a waiting spinner. See fragment_section_app_usage.xml
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mResumeInitialized = true;
        if (allInitialized())
            updateAppUsageList();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_DATE_RANGE_SELECTION, mDateRangeSpinner.getSelectedItemPosition());
        outState.putInt(STATE_DISPLAY_LIMIT_SELECTION, mDisplayLimitSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }


    /**
     * When MainActivity connects to the service, call this function to deliver service instance.
     */
    public void setService(TrackService service) {
        mService = service;

        if (mWaitingView != null && mContentView != null) {
            syncLayoutWithService();
            updateAppUsageList();
        }
    }

    /**
     * When MainActivity disconnects to the service, call this function to update status.
     */
    public void unsetService() {
        mService = null;
        syncLayoutWithService();
    }

    /**
     * Update layout based on the current service status.
     * If we are connected to service, hide the waiting section and display usage list.
     * If we are disconnected to service, hide the usage list and display waiting section.
     */
    private void syncLayoutWithService() {
        if (mService != null) {
            mWaitingView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        }
        else {
            mWaitingView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    /**
     * Update the usage times in app usage list.
     */
    private void updateAppUsageList() {
        if (!allInitialized())
            return;

        // Usage AsyncTask to update app usage, because it involves a database query.
        new UpdateAppUsageTask().execute(mDateRange);
    }

    /**
     * Setup the Spinners in filter section.
     */
    private void setupFilter() {
        // Initialize the date range spinner
        ArrayAdapter<CharSequence> dateRangeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.app_usage_date_range_array, R.layout.list_item_spinner_filter);

        dateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDateRangeSpinner.setAdapter(dateRangeAdapter);
        mDateRangeSpinner.setOnItemSelectedListener(new DateRangeSpinnerListener());

        // Initialize the display limit spinner
        ArrayAdapter<CharSequence> displayLimitAdapater = ArrayAdapter.createFromResource(getActivity(),
                R.array.app_usage_display_limit_array,  R.layout.list_item_spinner_filter);

        displayLimitAdapater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mDisplayLimitSpinner.setAdapter(displayLimitAdapater);
        mDisplayLimitSpinner.setOnItemSelectedListener(new DisplayLimitSpinnerListener());

        // Select spinners to default values.
        mDateRangeSpinner.setSelection(0);
        mDisplayLimitSpinner.setSelection(4);
    }


    /**
     * We want to call updateUsageList() when the following things happen:
     *   - onResume(), onCreateView(), setService()
     *   - a new value is selected for mDateRangeSpinner and mDisplayLimitSpinner
     * However, some of them might throw errors if others are not initialized. Moreover, some of them
     * can overlap when the fragment is first created, and we call updateUsageList() 3-4 times, which
     * is really slow and inefficient. This function helps by checking everything has been initialized,
     * and we only update list in updateUsageList() if it returns true.
     * @return true if all necessary components have been initialized. false otherwise.
     */
    private boolean allInitialized() {
        return (mDateRangeInitialized && mDisplayLimitInitialized && mResumeInitialized &&
                mService != null && mAppUsageListView != null);
    }

    /*-----------------------*/
    /*     Inner Classes     */
    /*-----------------------*/

    /**
     * Comparator for two AppUsageEntries. Used in sorting the list.
     */
    private class AppUsageEntryComparator implements Comparator<AppUsageEntry> {
        @Override
        public int compare(AppUsageEntry entry1, AppUsageEntry entry2)
        {
            return  (entry2.UsageTimeSec - entry1.UsageTimeSec);
        }
    }

    /**
     * Selector for date range spinner.
     */
    private class DateRangeSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final int[] mPositionToPrevDaysArray = {0, 1, 2, 7, 14, 30, 60, -1};

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mDateRangeInitialized = true;
            mDateRange = mPositionToPrevDaysArray[pos];
            updateAppUsageList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    /**
     * Selector for display limit spinner.
     */
    private class DisplayLimitSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final int[] mPositionToLimitArray = {5, 10, 20, 30, -1};

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            mDisplayLimitInitialized = true;
            mDisplayLimit = mPositionToLimitArray[pos];
            updateAppUsageList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    /**
     * The AsyncTask that handles getting app usage information from TrackService, which involves
     * database queryies and could potentially be slow.
     */
    private class UpdateAppUsageTask extends AsyncTask<Integer, Integer, List<AppUsageEntry> > {
        protected List<AppUsageEntry> doInBackground(Integer... params) {
            if (params.length > 0) {

                // Query, sort, and apply display limit to app usage list.
                List<AppUsageEntry> result = mService.getAppUsageInfo(params[0]);
                Collections.sort(result, new AppUsageEntryComparator());
                if (mDisplayLimit >= 0 && mDisplayLimit < result.size()) {
                    result = result.subList(0, mDisplayLimit);
                }
                return result;
            }
            else
                return null;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute( List<AppUsageEntry> result) {
            if (result != null) {
                AppUsageListAdapter adapter = (AppUsageListAdapter) mAppUsageListView.getAdapter();
                if (adapter != null) {
                    adapter.clear();
                    adapter.addAll(result);
                }
                else {
                    adapter = new AppUsageListAdapter(
                            getActivity(), R.layout.list_item_app_usage, result);
                    mAppUsageListView.setAdapter(adapter);
                }
            }
        }
    }
}
