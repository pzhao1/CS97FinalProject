package edu.swarthmore.cs.moodtracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.swarthmore.cs.moodtracker.util.AppUsageEntry;
import edu.swarthmore.cs.moodtracker.util.TrackDatabase;

/**
 * The service that tracks various stats on the phone, such as app usage, movement, text, voice, etc.
 * Started when the user launches application for the first time. Runs indefinitely.
 */
public class TrackService extends Service{

    public static final String TAG = "TRACK_SERVICE";

    // Binder given to clients
    private final IBinder mBinder = new TrackBinder();

    // Database that stores all tracking information
    private TrackDatabase mDatabase;

    /* Variables for tracking App Usage info */
    private HashMap<String, AppUsageEntry> mAppUsageInfo;
    private int mInterval = 1000;
    private Handler mAppUsageTimer = new Handler();
    private Runnable mAppUsageUpdateCallback;
    private ActivityManager mActivityManager;

    // Used to translate package name to app name.
    List<PackageInfo> mInstalledPackages = null;
    PackageManager mPackageManager = null;

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
        mDatabase = TrackDatabase.getInstance(this);

        // Initialize package name -> app name translate tools
        mPackageManager = getPackageManager();
        mInstalledPackages = mPackageManager.getInstalledPackages(0);

        // Initialize and run App Usage tracking.
        initializeAppUsageTracking();
        mAppUsageUpdateCallback.run();
    }

    /**
     * Initialize App Usage tracking.
     * Construct ActivityManager, usageInfo HashMap, and the timed callback.
     */
    private void initializeAppUsageTracking() {
        mActivityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mAppUsageInfo = new HashMap<String, AppUsageEntry>();

        // Load existing app usage entries from database.
        ArrayList<AppUsageEntry> existingEntries = mDatabase.getAllAppUsage();
        for (AppUsageEntry entry:existingEntries) {
            mAppUsageInfo.put(entry.PackageName, entry);
        }

        mAppUsageUpdateCallback = new Runnable() {
            @Override
            public void run() {
                updateAppUsage();
                mAppUsageTimer.postDelayed(this, mInterval);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");

        saveEntriesToDatabase();
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        // Stop tracking app usage status.
        mAppUsageTimer.removeCallbacks(mAppUsageUpdateCallback);

        saveEntriesToDatabase();
    }


    /**
     * Update the App Usage track information.
     * Called by mAppUsageTimer every [mInterval] milliseconds.
     */
    private void updateAppUsage() {

        List<RunningAppProcessInfo> appProcesses = mActivityManager.getRunningAppProcesses();
        for(RunningAppProcessInfo appProcess : appProcesses){
            if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && ! isSystemProcess(appProcess)){
                // We have found a foreground app that's not system process.
                // It's probably the app user is using right now. Add it to the app usage info.
                String packageName = appProcess.processName;

                // Update the app usage info HashMap.
                if ( mAppUsageInfo.containsKey(packageName) ) {
                    mAppUsageInfo.get(packageName).UsageTimeSec += 1;
                }
                else {
                    String appName = getAppName(packageName);
                    if (appName == null) {
                        continue;
                    }

                    AppUsageEntry newEntry = new AppUsageEntry(packageName, appName, 1);
                    mAppUsageInfo.put(packageName, newEntry);
                }
            }
        }
    }

    /**
     * Decide whether a process is Android system process or not.
     * @param appProcess The running app process to be determined.
     * @return True if appProcess is a system process, false otherwise.
     */
    private boolean isSystemProcess(RunningAppProcessInfo appProcess) {
        return (appProcess.processName.contains("com.android")
                || appProcess.processName.equals("system")
                || appProcess.processName.contains("com.nuance"));
    }

    /**
     * Get the current app usage info. Used by the activities bound to this service.
     * @return A collection of AppUsageEntry objects.
     */
    public List<AppUsageEntry> getCurrentAppUsageInfo() {
        Collection<AppUsageEntry> entriesCollection = mAppUsageInfo.values();
        List entriesList;
        if (entriesCollection instanceof List) {
            entriesList = (List) entriesCollection;
        }
        else {
            entriesList = new ArrayList<AppUsageEntry>(entriesCollection);
        }
        return entriesList;
    }

    /**
     * Get the application name from package name.
     * @param packageName Package name of an application.
     * @return Name of the application, or null if packageName is not found in installed packages.
     */
    private String getAppName(String packageName) {
        for (PackageInfo pkg : mInstalledPackages) {
            if ( pkg.packageName.equals(packageName) ){
                //this is the application name
                return pkg.applicationInfo.loadLabel(mPackageManager).toString();
            }
        }
        return null;
    }
    /**
     * Write the tracked app usage info back to database (i.e. disk) to store them permanently.
     * Called in onUnbind() and onDestroy().
     */
    private void saveEntriesToDatabase() {
        Log.d(TAG, "Saving entries to database");
        for (AppUsageEntry entry : mAppUsageInfo.values()) {
            mDatabase.writeAppUsage(entry);
        }
    }
}
