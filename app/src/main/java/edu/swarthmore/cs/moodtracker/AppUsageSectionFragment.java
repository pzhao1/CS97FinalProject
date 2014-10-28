package edu.swarthmore.cs.moodtracker;

/**
 * Created by Peng on 10/19/2014.
 */

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import edu.swarthmore.cs.moodtracker.util.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.util.AppUsageListAdapter;

/**
 * Created by Peng on 10/19/2014.
 * The fragment that shows App Usage tracking information.
 */
public class AppUsageSectionFragment extends Fragment {

    public static final String TAG = "AppUsageSectionFragment";

    private TrackService mService = null;
    private ListView mAppUsageListView = null;
    private View mWaitingView = null;

    /**
     * Default constructor.
     */
    public AppUsageSectionFragment() {
        super();
    }

    // Connection to TrackService
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackService.TrackBinder binder = (TrackService.TrackBinder) iBinder;
            mService = binder.getService();
            updateViewWithService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_section_app_usage, container, false);
        mAppUsageListView = (ListView) rootView.findViewById(R.id.list_app_usage);
        mWaitingView = rootView.findViewById(R.id.waiting_view);

        // Connect to the TrackService instance.
        // Before we get connected, just display a waiting spinner. See fragment_section_app_usage.xml
        Intent intent = new Intent(getActivity(), TrackService.class);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        return rootView;
    }

    /**
     * Update the fragment layout when we connect to TrackService. Requires mService != null.
     */
    private void updateViewWithService() {
        // Hide the waiting view (progress bar and text).
        mWaitingView.setVisibility(View.GONE);

        // Display the app usage list.
        mAppUsageListView.setVisibility(View.VISIBLE);
        updateAppUsageList();
    }

    /**
     * Update the usage times in app usage list. Requires mService != null.
     */
    private void updateAppUsageList() {
        List<AppUsageEntry> appUsageEntries = mService.getCurrentAppUsageInfo();
        AppUsageListAdapter adapter = (AppUsageListAdapter) mAppUsageListView.getAdapter();
        if (adapter != null) {
            adapter.clear();
            adapter.addAll(appUsageEntries);
        }
        else {
            adapter = new AppUsageListAdapter(
                    getActivity(), R.layout.list_item_app_usage, appUsageEntries);
            mAppUsageListView.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // If we are already connected to the service, update the list.
        if (mService != null) {
            updateAppUsageList();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mServiceConnection);
    }
}
