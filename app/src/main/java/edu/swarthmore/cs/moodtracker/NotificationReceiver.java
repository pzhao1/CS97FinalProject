package edu.swarthmore.cs.moodtracker;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by cwang3 on 11/3/14.
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service1 = new Intent(context, NotificationAlarmService.class);
        context.startService(service1);
    }
}
