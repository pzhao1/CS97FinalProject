package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

import edu.swarthmore.cs.moodtracker.db.TrackDatabase;

/**
 * Created by cwang3 on 11/9/14.
 * Collect one day of text messages
 */
public class getLastDayMsgReceiver extends BroadcastReceiver {
    public static final String TAG = "OutgoingTextMsgObserver";
    private TrackDatabase mDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        mDatabase = TrackDatabase.getInstance(context);

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
                    Integer id = cur.getInt(cur.getColumnIndex("_id"));
                    Integer date = cur.getInt(cur.getColumnIndex("date"));
                    Integer sender = cur.getInt(cur.getColumnIndex("person"));
                    Integer receiver = cur.getInt(cur.getColumnIndex("address"));
                    String message = cur.getString(cur.getColumnIndex("body"));
                    Log.d(TAG, message);
                    mDatabase.writeTextMsgRecord(id, date, sender, receiver,message);
                } while (cur.moveToNext());
            }
        }
        cur.close();
    }

}
