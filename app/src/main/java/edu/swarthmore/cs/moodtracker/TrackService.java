package edu.swarthmore.cs.moodtracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import edu.swarthmore.cs.moodtracker.util.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.util.TrackDatabase;

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
    private TimeZone mMyTimeZone = TimeZone.getDefault();
    private long mCurrentDate = getDaysSinceEpoch();

    /* App Usage Tracking Variables */
    private ActivityManager mActivityManager = null;
    private PackageManager mPackageManager = null;
    private HashSet<String> mFilteredSystemPackagesSet = null;
    private HashMap<String, AppUsageEntry> mAppUsageInfo;



    /*----------------------------*/
    /* Service Fields and Methods */
    /*----------------------------*/

    /**
     * Class used for the client Binder.  We can safely return the service itself because
     * we know this service is only going to be used by our application.
     */
    public class TrackBinder extends Binder {
        // Return this instance of LocalService so clients can call public methods
        TrackService getService() {
            return TrackService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        Toast.makeText(this, "onCreate()", Toast.LENGTH_SHORT).show();
        mDatabase = TrackDatabase.getInstance(this);

        // Initialize and run App Usage tracking.
        initializeAppUsageTracking();
        initializeTimer();
        mTimerCallback.run();
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
        Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy()");

        // Stop tracking app usage status.
        mTimer.removeCallbacks(mTimerCallback);

        saveDataToDatabase();
    }


    /*---------------------------------*/
    /* Timer and general timed Methods */
    /*---------------------------------*/

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
        long newDate = getDaysSinceEpoch();
        if (newDate > mCurrentDate) {
            Log.d(TAG, "newDay");
            Toast.makeText(this, "newDay", Toast.LENGTH_SHORT).show();
            saveDataToDatabase();
            mCurrentDate = newDate;

            // Load today's usage data from database.
            readDataFromDatabase(mCurrentDate);
        }
    }

    /**
     * Get the number of days passed since Epoch (1970/1/1). This is the date formate stored in database.
     * @return a long representing the number of days since epoch.
     */
    private long getDaysSinceEpoch() {
        long offset = mMyTimeZone.getOffset(System.currentTimeMillis());
        long millis = System.currentTimeMillis() + offset;
        return millis / (24*3600*1000);
    }

    /**
     * Write app usage data to database (i.e. disk) to store them permanently.
     * Called in onUnbind() and onDestroy().
     */
    public void saveDataToDatabase() {
        Log.d(TAG, "Saving app usage to database");
        Toast.makeText(this, "saving usage", Toast.LENGTH_SHORT).show();
        for (AppUsageEntry entry : mAppUsageInfo.values()) {
            if (entry.Dirty) {
                mDatabase.writeAppUsage(entry);
                entry.Dirty = false;
            }
        }
        mSaveTicks = 0;
    }


    /**
     * Load app usage data from database.
     * @param date The date of usage data we are interested in.
     */
    private void readDataFromDatabase(long date) {
        Log.d(TAG, "Loading app usage from database");
        mAppUsageInfo = new HashMap<String, AppUsageEntry>();
        ArrayList<AppUsageEntry> existingEntries = mDatabase.readAppUsage(date);
        for (AppUsageEntry entry:existingEntries) {
            mAppUsageInfo.put(entry.PackageName, entry);
        }
    }

    /*----------------------------*/
    /* App Usage Specific Methods */
    /*----------------------------*/

    /**
     * Get the current app usage info. Used by the activities bound to this service.
     * @return A collection of AppUsageEntry objects.
     */
    public List<AppUsageEntry> getCurrentAppUsageInfo() {
        return new ArrayList<AppUsageEntry>(mAppUsageInfo.values());
    }


    /**
     * Initialize App Usage tracking.
     */
    private void initializeAppUsageTracking() {
        // Construct activity manager and package manager.
        mActivityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();

        // Initialize the system package names set. These are the packages to ignore.
        mFilteredSystemPackagesSet = new HashSet<String>();
        String[] allowedSystemPackageNames = getResources().getStringArray(R.array.filtered_system_package_array);
        for (String packageName: Arrays.asList(allowedSystemPackageNames)) {
            mFilteredSystemPackagesSet.add(packageName);
        }

        // Initialize the app usage Hashmap. Load today's usage data from database.
        readDataFromDatabase(mCurrentDate);
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
            String packageName = appProcessInfo.processName;
            PackageInfo packageInfo;
            try {
                packageInfo = mPackageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            }
            catch (PackageManager.NameNotFoundException e) {
                continue;
            }

            // If the first process is system process, then we are probably not opening an app. Break here.
            // Examples include com.android.launcher and com.android.settings
            if(isFilteredSystemPackage(packageInfo))
                break;

            // We have found a foreground app that's not system process.
            // It's probably the app user is using right now. Add it to the app usage info.
            if ( mAppUsageInfo.containsKey(packageName) ) {
                AppUsageEntry entry = mAppUsageInfo.get(packageName);
                entry.UsageTimeSec += (mTimerInterval /1000);
                entry.Dirty = true;
            }
            else {
                String appName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
                BitmapDrawable appIcon = (BitmapDrawable) packageInfo.applicationInfo.loadIcon(getPackageManager());

                AppUsageEntry newEntry = new AppUsageEntry(packageName, appName, appIcon.getBitmap(), 1, mCurrentDate);
                newEntry.Dirty = true;
                mAppUsageInfo.put(packageName, newEntry);
            }

            // Break after we have caught 1 foreground process.
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


    /**
     * Decide whether the given package is a system package, and if yes, whether it should
     * be ignored.
     * @param packageInfo Name of the package.
     * @return True if packageInfo represents a system package that should be ignored (such as
     * com.android.systemui). False otherwise.
     */
    private boolean isFilteredSystemPackage(PackageInfo packageInfo) {
        //return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
        return mFilteredSystemPackagesSet.contains(packageInfo.packageName);
    }


    /*--------------------------------------*/
    /* Broadcast Receiver for Screen ON/OFF */
    /*--------------------------------------*/
    private BroadcastReceiver mPowerKeyReceiver = null;

    private void registBroadcastReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mPowerKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String strAction = intent.getAction();

                if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON)) {
                    // > Your playground~!
                }
            }
        };

        getApplicationContext().registerReceiver(mPowerKeyReceiver, theFilter);
    }

    private void unregisterReceiver() {
        int apiLevel = Build.VERSION.SDK_INT;

        if (apiLevel >= 7) {
            try {
                getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
            }
            catch (IllegalArgumentException e) {
                mPowerKeyReceiver = null;
            }
        }
        else {
            getApplicationContext().unregisterReceiver(mPowerKeyReceiver);
            mPowerKeyReceiver = null;
        }
    }

}
