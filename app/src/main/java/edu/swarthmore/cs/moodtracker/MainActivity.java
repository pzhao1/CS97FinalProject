package edu.swarthmore.cs.moodtracker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    // Navigation Drawer
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment mCurrentSectionFragment;
    private CharSequence mTitle;

    // Connection to TrackService
    private TrackService mService;
    private ServiceConnection mServiceConnection = new TrackServiceConnection();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start and bind to the TrackService
        Intent intent = new Intent(this, TrackService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set alarm for survey
        Calendar mCalendar1 = Calendar.getInstance();
        mCalendar1.set(Calendar.HOUR_OF_DAY, 9); //add more here
        mCalendar1.set(Calendar.MINUTE, 0);
        mCalendar1.set(Calendar.SECOND, 0);
        mCalendar1.set(Calendar.AM_PM, Calendar.AM);
        Calendar mCalendar2 = Calendar.getInstance();
        mCalendar2.set(Calendar.HOUR_OF_DAY, 3); //add more here
        mCalendar2.set(Calendar.MINUTE, 0);
        mCalendar2.set(Calendar.SECOND, 0);
        mCalendar2.set(Calendar.AM_PM, Calendar.PM);
        Calendar mCalendar3 = Calendar.getInstance();
        mCalendar3.set(Calendar.HOUR_OF_DAY, 10); //add more here
        mCalendar3.set(Calendar.MINUTE, 00);
        mCalendar3.set(Calendar.SECOND, 00);
        mCalendar3.set(Calendar.AM_PM, Calendar.PM);

        Intent myIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);

        AlarmManager mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar1.getTimeInMillis(),AlarmManager.INTERVAL_DAY, mPendingIntent);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar2.getTimeInMillis(),AlarmManager.INTERVAL_DAY, mPendingIntent);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar3.getTimeInMillis(),AlarmManager.INTERVAL_DAY, mPendingIntent);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        switch (position) {
            case 0:
                AppUsageSectionFragment usageFragment = new AppUsageSectionFragment();
                if (mService != null)
                    usageFragment.setService(mService);
                mTitle = getString(R.string.title_section1);
                mCurrentSectionFragment = usageFragment;
                break;
            case 1:
                mCurrentSectionFragment = new TextSectionFragment();
                mTitle = getString(R.string.title_section2);
                break;
            case 2:
                mCurrentSectionFragment = new SurveySectionFragment();
                mTitle = getString(R.string.title_section3);
                break;
        }

        fragmentManager.beginTransaction().replace(R.id.container, mCurrentSectionFragment).commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mService != null)
            mService.saveDataToDatabase();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mServiceConnection);
    }


    /**
     * The ServiceConnection class used by this activity.
     * Handles the relationship between TrackService and different fragments.
     */
    private class TrackServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            TrackService.TrackBinder binder = (TrackService.TrackBinder) iBinder;
            mService = binder.getService();

            // Deliver service to fragment.
            if (mCurrentSectionFragment instanceof AppUsageSectionFragment)
                ((AppUsageSectionFragment) mCurrentSectionFragment).setService(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;

            // Tell the fragments TrackService has disconnected.
            // Deliver service to fragment.
            if (mCurrentSectionFragment instanceof AppUsageSectionFragment)
                ((AppUsageSectionFragment) mCurrentSectionFragment).unsetService();
        }
    }
}
