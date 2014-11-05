package edu.swarthmore.cs.moodtracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by cwang3 on 11/3/14.
 */
public class NotificationAlarmService extends Service{

    public static final String TAG = "NotificationAlarmService";

    @Override
    public IBinder onBind(Intent arg0)
    {
        Log.d(TAG, "onBind()");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        NotificationManager mNotificationManager;
        PendingIntent mPendingIntent;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("MoodTracker")
                .setContentText("Take MoodTracker Survey!")
                .setAutoCancel(true);
        // When user clicks the notification, open MoodTracker survey
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("SelectDrawerItem", 2);
        mPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(mPendingIntent);

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return false;
    }

}
