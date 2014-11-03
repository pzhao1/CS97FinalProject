package edu.swarthmore.cs.moodtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cwang3 on 11/3/14.
 */
public class NotificationReceiver extends BroadcastReceiver {
    public static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        Intent service1 = new Intent(context, NotificationAlarmService.class);
        context.startService(service1);
    }
}
