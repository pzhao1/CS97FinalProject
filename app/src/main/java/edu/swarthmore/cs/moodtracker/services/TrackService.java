package edu.swarthmore.cs.moodtracker.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.swarthmore.cs.moodtracker.R;
import edu.swarthmore.cs.moodtracker.db.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.db.ReadAppUsageTask;
import edu.swarthmore.cs.moodtracker.db.SaveAppUsageTask;
import edu.swarthmore.cs.moodtracker.db.TrackDatabase;
import edu.swarthmore.cs.moodtracker.util.TrackDateUtil;

/**
 * The service that tracks various stats on the phone, such as app usage, movement, text, voice, etc.
 * Started when the user launches application for the first time. Runs indefinitely.
 */
public class TrackService extends Service{

    public static final String TAG = "TrackService";

    // Binder given to clients
    private final IBinder mBinder = new TrackBinder();

    // Database that stores all tracking information
    private TrackDatabase mDatabase;

    /* Timer and Date Variables  */
    private Handler mTimer = new Handler();
    private Runnable mTimerCallback = null;
    private final int mTimerInterval = 1000;
    private final int mSaveInterval = 60;
    private int mSaveTicks = 0;
    private long mCurrentDate = TrackDateUtil.getDaysSinceEpoch();

    /* App Usage Tracking Variables */
    private ActivityManager mActivityManager = null;
    private PackageManager mPackageManager = null;
    private HashSet<String> mLauncherProcessNames = null;
    private HashMap<String, AppUsageEntry> mAppUsageInfo = null;


    /*----------------------------*/
    /* Service Fields and Methods */
    /*----------------------------*/

    /**
     * Class used for the client Binder.  We can safely return the service itself because
     * we know this service is only going to be used by our application.
     */
    public class TrackBinder extends Binder {
        // Return this instance of LocalService so clients can call public methods
        public TrackService getService() {
            return TrackService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        mDatabase = TrackDatabase.getInstance(this);

        // Initialize and run App Usage tracking.
        initializeAppUsageTracking();
        initializeTimer();
        registerBroadcastReceiver();
        //mTimerCallback.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        // Stop timer ticking.
        mTimer.removeCallbacks(mTimerCallback);

        // Clean up.
        unregisterBroadcastReceiver();
        saveDataToDatabase();
    }


    /*-----------------------*/
    /* Time and Date Methods */
    /*-----------------------*/

    /**
     * Set up the timer interval and callbacks.
     */
    private void initializeTimer() {
        // Start the call back that updates app usage data every mTimerInterval ms.
        mTimerCallback = new Runnable() {
            @Override
            public void run() {
                checkForNewDay();
                updateAppUsageInfo();

                // Save data to database every mSaveInterval seconds.
                mSaveTicks += (mTimerInterval /1000);
                if (mSaveTicks > mSaveInterval) {
                    saveDataToDatabase();
                }

                // Post this runnable again for next tick.
                mTimer.postDelayed(this, mTimerInterval);
            }
        };
    }

    /**
     * Check whether we just passed 11:59:59 pm. If yes, save yesterday's data to database
     * and initialize today's data.
     */
    private void checkForNewDay() {
        long newDate = TrackDateUtil.getDaysSinceEpoch();
        if (newDate > mCurrentDate) {
            Log.d(TAG, "newDay");
            saveDataToDatabase();
            mCurrentDate = newDate;

            // Renew app usage info map.
            mAppUsageInfo = new HashMap<String, AppUsageEntry>();
        }
    }

    /**
     * Write app usage data to database (i.e. disk) to store them permanently.
     * Called in onUnbind() and onDestroy().
     */
    public void saveDataToDatabase() {
        mSaveTicks = 0;
        new SaveAppUsageTask(this, mAppUsageInfo.values()) {
            @Override
            public void onFinish() {
                Log.d(TAG, "App usage saved to database");
            }
        }.execute();
    }


    /*----------------------------*/
    /* App Usage Specific Methods */
    /*----------------------------*/

    /**
     * Get the current app usage info. Used by the activities bound to this service.
     * This involves database query and is best
     * @return A collection of AppUsageEntry objects.
     */
    public List<AppUsageEntry> getTodayAppUsage() {
        if (mAppUsageInfo == null)
            return null;
        return new ArrayList<AppUsageEntry>(mAppUsageInfo.values());
    }


    /**
     * Initialize App Usage tracking.
     */
    private void initializeAppUsageTracking() {
        // Construct activity manager and package manager.
        mActivityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();

        // Construct the launcher app filter
        populateLauncherProcessNames();

        // Read existing usage entries from database (asynchronously).
        mAppUsageInfo = new HashMap<String, AppUsageEntry>();
        new ReadAppUsageTask(this) {
            @Override
            public void onFinish(List<AppUsageEntry> result) {
                Log.d(TAG, "Loaded app usage from database");
                for (AppUsageEntry entry:result) {
                    mAppUsageInfo.put(entry.PackageName, entry);
                }
                mTimerCallback.run();
            }
        }.execute(mCurrentDate, mCurrentDate);
    }

    /**
     * Populate the launcher app process name HashSet. Only process names in this set are tracked.
     * We only want to track apps whose icons show in Launcher, because these are the apps that
     * user is aware of. We don't need things like "NFC Service" or "ASUS Keyboard", even if sometimes
     * they have foreground importance.
     */
    private void populateLauncherProcessNames() {

        HashSet<String> filteredLauncherProcesses = new HashSet<String>();
        filteredLauncherProcesses.addAll(Arrays.asList(
                getResources().getStringArray(R.array.filtered_launcher_process_names)));

        mLauncherProcessNames = new HashSet<String>();
        // Use an Launcher intent to resolve all Activities that can be launched from home.
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(intent, 0);

        // Translate resolveInfo to packageName, then to appInfo, then to processName.
        for(ResolveInfo resolveInfo : resolveInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            try {
                ApplicationInfo appInfo = mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (!filteredLauncherProcesses.contains(appInfo.processName))
                    mLauncherProcessNames.add(appInfo.processName);
            }
            catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
            }
        }
    }


    /**
     * Update the App Usage track information.
     * Called by mTimer every [mTimerInterval] milliseconds.
     */
    private void updateAppUsageInfo() {
        List<RunningAppProcessInfo> appProcesses = mActivityManager.getRunningAppProcesses();
        for(RunningAppProcessInfo appProcessInfo : appProcesses){
            // Skip non-foreground processes.
            if (appProcessInfo.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                continue;

            // If this process is important because it's a provider / service of a foreground app,
            // then also ignore it. For example, com.android.providers.calendar will have
            // foreground importance when user is using com.google.android.calendar , because
            // it's the provider of the calendar app.
            if (isProviderOrService(appProcessInfo))
                continue;

            // Get the package info of this process
            String processName = appProcessInfo.processName;

            // If the first non-service foreground process is not a launcher app, then we are most
            // likely in the launcher itself, or the settings page. Break in here.
            // See populateLauncherProcessNames() for more details.
            if(! mLauncherProcessNames.contains(processName))
                break;

            PackageInfo packageInfo;
            try {
                packageInfo = mPackageManager.getPackageInfo(processName, PackageManager.GET_ACTIVITIES);
            }
            catch (PackageManager.NameNotFoundException e) {
                continue;
            }


            // We have found a foreground launcher app. Update usage info now.
            if ( mAppUsageInfo.containsKey(processName) ) {
                AppUsageEntry entry = mAppUsageInfo.get(processName);
                entry.UsageTimeSec += (mTimerInterval /1000);
            }
            else {
                String appName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
                BitmapDrawable appIcon = (BitmapDrawable) packageInfo.applicationInfo.loadIcon(getPackageManager());

                AppUsageEntry newEntry = new AppUsageEntry(processName, appName, appIcon.getBitmap(), 1, mCurrentDate);

                mAppUsageInfo.put(processName, newEntry);
            }

            // Break after we found one foreground app.
            break;
        }
    }


    /**
     * Decide whether this app process is a provider or service.
     * @param appProcessInfo Information of the running process.
     * @return Boolean True if this process is a service or provider, false otherwise.
     */
    private boolean isProviderOrService(RunningAppProcessInfo appProcessInfo) {
        return (appProcessInfo.importanceReasonCode == RunningAppProcessInfo.REASON_PROVIDER_IN_USE ||
                appProcessInfo.importanceReasonCode == RunningAppProcessInfo.REASON_SERVICE_IN_USE);
    }



    /*--------------------------------------*/
    /* Broadcast Receiver for Screen ON/OFF */
    /*--------------------------------------*/
    private BroadcastReceiver mScreenOnOffReceiver = new TrackBroadcastReceiver();

    /**
     * Our broadcast receiver that listens to system events we are interested in.
     */
    private class TrackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG, "Screen off, stop ticking timer");
                mTimer.removeCallbacks(mTimerCallback);
            }

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                Log.d(TAG, "Screen on, start ticking timer");
                mTimerCallback.run();
            }
        }
    }

    /**
     * Register our broadcast receiver, meaning we start listening to system broadcasts.
     */
    private void registerBroadcastReceiver() {

        // Only receive Screen On/Off broadcasts.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        getApplicationContext().registerReceiver(mScreenOnOffReceiver, filter);
    }

    /**
     * Unregister our broadcast receiver, meaning we stop listening to system broadcasts.
     */
    private void unregisterBroadcastReceiver() {
        try {
            getApplicationContext().unregisterReceiver(mScreenOnOffReceiver);
        }
        catch (IllegalArgumentException e) {
            mScreenOnOffReceiver = null;
        }
    }

}
