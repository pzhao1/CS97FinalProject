package edu.swarthmore.cs.moodtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * When it is time to take surveys, initiate a service that creates the notification.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        Intent service1 = new Intent(context, NotificationAlarmService.class);
        context.startService(service1);
        //context.stopService(service1);?? Do I need to stop the service?
    }
}
