package edu.swarthmore.cs.moodtracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * When it is time to take surveys, send a notification.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        NotificationManager mNotificationManager;
        PendingIntent mPendingIntent;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("MoodTracker")
                        .setContentText("Take MoodTracker Survey!")
                        .setAutoCancel(true);
        // When user clicks the notification, open MoodTracker survey
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(MainActivity.EXTRA_DRAWER_SELECT, 2);
        mPendingIntent = PendingIntent.getActivity(context,0,resultIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(mPendingIntent);
        mBuilder.setNumber(0);

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
