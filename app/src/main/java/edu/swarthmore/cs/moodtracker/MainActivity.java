package edu.swarthmore.cs.moodtracker;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

import edu.swarthmore.cs.moodtracker.receivers.NotificationAlarmReceiver;
import edu.swarthmore.cs.moodtracker.receivers.OutgoingTextMsgObserver;
import edu.swarthmore.cs.moodtracker.receivers.getLastDayMsgReceiver;
import edu.swarthmore.cs.moodtracker.services.TrackService;


public class MainActivity extends FragmentActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = "MainActivity";
    public static final String EXTRA_DRAWER_SELECT = "SelectDrawerItem";

    // Navigation Drawer
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment mCurrentSectionFragment;
    private int mSelectedSection = -1;
    private CharSequence mTitle;

    // Connection to TrackService
    private TrackService mService;
    private ServiceConnection mServiceConnection = new TrackServiceConnection();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start and bind to the TrackService
        Intent intent = new Intent(this, TrackService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        // Set up the drawer.
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // Handles state preservation during rotation, and redirection from notification.
        if (savedInstanceState != null) {
            mSelectedSection = savedInstanceState.getInt(EXTRA_DRAWER_SELECT, 0);
        } else {
            mSelectedSection = getIntent().getIntExtra(EXTRA_DRAWER_SELECT, 0);
        }

        setNotificationForSurvey();
        // I think we only need to choose one of these two:
        trackTextMessages();
        getWholeDayMessages();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mSelectedSection = intent.getIntExtra(EXTRA_DRAWER_SELECT, mSelectedSection);
        super.onNewIntent(intent);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_DRAWER_SELECT, mSelectedSection);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        mSelectedSection = position;
        switch (position) {
            case 0:
                AppUsageSectionFragment usageFragment = new AppUsageSectionFragment();
                if (mService != null)
                    usageFragment.setService(mService);
                mCurrentSectionFragment = usageFragment;
                mTitle = getString(R.string.title_section_app_usage);
                break;
            case 1:
                mCurrentSectionFragment = new TextSectionFragment();
                mTitle = getString(R.string.title_section_text_analysis);
                break;
            case 2:
                mCurrentSectionFragment = new SurveySectionFragment();
                mCurrentSectionFragment.setRetainInstance(true);
                mTitle = getString(R.string.title_section_surveys);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (mService != null)
            mService.saveDataToDatabase();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mNavigationDrawerFragment.selectItem(mSelectedSection);
        invalidateOptionsMenu();
        super.onResume();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    /**
     * Set notifications to appear three times a day and remind users to take the survey.
     */
    private void setNotificationForSurvey() {
        Log.d("setNotificationForSurvey", "called");
        // Set alarm for survey
        Calendar mCalendar;
        PendingIntent mPendingIntent;
        AlarmManager mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, NotificationAlarmReceiver.class);
        int[] times = {9,15,22};    //survey at 9am, 3pm and 10pm

        for (int i=0; i<3; i++) {
            mCalendar = Calendar.getInstance();
            // Avoid sending notification for past time
            if (times[i]<= mCalendar.get(Calendar.HOUR_OF_DAY)) {
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            mCalendar.set(Calendar.HOUR_OF_DAY, times[i]);
            mCalendar.set(Calendar.MINUTE, 0);
            mCalendar.set(Calendar.SECOND, 0);
            mPendingIntent = PendingIntent.getBroadcast(this, i, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPendingIntent);
        }
    }

    /**
     * Set up tracking of incoming and outgoing messages.
     */
    private void trackTextMessages() {
        //Cursor mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"),null,null,null,null);
        //mCursor.moveToFirst();
        ContentResolver contentResolver = getContentResolver();
        OutgoingTextMsgObserver outgoingTextMsgObserver = new OutgoingTextMsgObserver(new Handler(), this);
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, outgoingTextMsgObserver);
    }

    /**
     * Collect an entire day of messages at 00:01:00 the next day
     */
    private void getWholeDayMessages() {
        PendingIntent mPendingIntent;
        AlarmManager mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(this, getLastDayMsgReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0);
        Calendar mCalendar = Calendar.getInstance();
        //if (mCalendar.get(Calendar.HOUR_OF_DAY)>=0 && mCalendar.get(Calendar.MINUTE)>=1) {
        //    mCalendar.add(Calendar.DAY_OF_MONTH, 1);
        //}
        mCalendar.set(Calendar.HOUR_OF_DAY,00);
        mCalendar.set(Calendar.MINUTE,01);
        mCalendar.set(Calendar.SECOND,00);
        mAlarmManager.setRepeating(AlarmManager.RTC, mCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, mPendingIntent);

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
