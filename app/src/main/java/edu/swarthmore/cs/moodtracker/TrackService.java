package edu.swarthmore.cs.moodtracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
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

    /* Variables for tracking App Usage info */
    private ActivityManager mActivityManager = null;
    private PackageManager mPackageManager = null;
    private HashSet<String> mFilteredSystemPackagesSet = null;
    private HashMap<String, AppUsageEntry> mAppUsageInfo;
    private int mInterval = 1000;
    private Handler mAppUsageTimer = new Handler();
    private Runnable mAppUsageUpdateCallback = null;
    private TimeZone mMyTimeZone = TimeZone.getDefault();
    private long mCurrentDate = 0;  // Current date represented by days since epoch.
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
        mAppUsageUpdateCallback.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        Toast.makeText(this, "onStartCommand()", Toast.LENGTH_SHORT).show();
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        Toast.makeText(this, "onBind()", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        Toast.makeText(this, "onUnBind()", Toast.LENGTH_SHORT).show();
        saveDataToDatabase();
        return false;
    }

    @Override
    public void onDestroy() {
        // TODO: onDestroy() is not guaranteed to be called when system decides to kill this service
        // due to low memory. Save data periodically.
        Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onDestroy()");

        // Stop tracking app usage status.
        mAppUsageTimer.removeCallbacks(mAppUsageUpdateCallback);

        saveDataToDatabase();
    }


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

        // Set the current date (represented by days since epoch)
        mCurrentDate = getDaysSinceEpoch();

        // Initialize the app usage hashmap. Load today's usage data from database.
        // TODO: combine duplicate code.
        mAppUsageInfo = new HashMap<String, AppUsageEntry>();
        ArrayList<AppUsageEntry> existingEntries = mDatabase.readAppUsage(mCurrentDate);
        for (AppUsageEntry entry:existingEntries) {
            mAppUsageInfo.put(entry.PackageName, entry);
        }

        // Start the call back that updates app usage data every mInterval ms.
        mAppUsageUpdateCallback = new Runnable() {
            @Override
            public void run() {
                updateAppUsageInfo();
                mAppUsageTimer.postDelayed(this, mInterval);
            }
        };
    }

    /**
     * Update the App Usage track information.
     * Called by mAppUsageTimer every [mInterval] milliseconds.
     */
    private void updateAppUsageInfo() {

        checkForNewDay();

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
                mAppUsageInfo.get(packageName).UsageTimeSec += 1;
            }
            else {
                String appName = packageInfo.applicationInfo.loadLabel(mPackageManager).toString();
                BitmapDrawable appIcon = (BitmapDrawable) packageInfo.applicationInfo.loadIcon(getPackageManager());

                AppUsageEntry newEntry = new AppUsageEntry(packageName, appName, appIcon.getBitmap(), 1, mCurrentDate);
                mAppUsageInfo.put(packageName, newEntry);
            }

            // Break after we have caught 1 foreground process.
            break;
        }
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

            // Initialize the app usage hashmap. Load today's usage data from database.
            // TODO: combine duplicate code.
            mAppUsageInfo = new HashMap<String, AppUsageEntry>();
            ArrayList<AppUsageEntry> existingEntries = mDatabase.readAppUsage(mCurrentDate);
            for (AppUsageEntry entry:existingEntries) {
                mAppUsageInfo.put(entry.PackageName, entry);
            }
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


    /**
     * Write the tracked app usage info back to database (i.e. disk) to store them permanently.
     * Called in onUnbind() and onDestroy().
     */
    private void saveDataToDatabase() {
        Log.d(TAG, "Saving data to database");
        for (AppUsageEntry entry : mAppUsageInfo.values()) {
            mDatabase.writeAppUsage(entry);
        }
    }

    private long getDaysSinceEpoch() {
        long offset = mMyTimeZone.getOffset(System.currentTimeMillis());
        long millis = System.currentTimeMillis() + offset;
        return millis / (24*3600*1000);
    }
}
