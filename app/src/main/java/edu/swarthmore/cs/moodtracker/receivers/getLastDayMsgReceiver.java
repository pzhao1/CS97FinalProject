package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by cwang3 on 11/9/14.
 * Collect one day of text messages
 */
public class getLastDayMsgReceiver extends BroadcastReceiver {
    public static final String TAG = "OutgoingTextMsgObserver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        Calendar startTime = Calendar.getInstance();
        startTime.add(Calendar.DAY_OF_MONTH,-1);
        startTime.set(Calendar.HOUR_OF_DAY,0);
        startTime.set(Calendar.MINUTE,0);
        startTime.set(Calendar.SECOND,0);
        Integer start = (int) startTime.getTimeInMillis();

        Cursor cur = context.getContentResolver().query(Uri.parse("content://sms"),
                new String[]{"_id", "address", "person", "date", "body", "type"},
                "date >= " + start,
                null,
                null);
        Log.d(TAG, "cursor created");
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String message = cur.getString(cur.getColumnIndex("body"));
                    Log.d(TAG, message);
                } while (cur.moveToNext());
            }
        }
        cur.close();
    }

}
