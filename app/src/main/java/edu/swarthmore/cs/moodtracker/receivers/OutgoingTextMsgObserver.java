package edu.swarthmore.cs.moodtracker.receivers;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by cwang3 on 11/9/14.
 * Detect outgoing messages and record contents (not implemented yet)
 */
public class OutgoingTextMsgObserver extends ContentObserver {
    private Context mContext;
    public static final String TAG = "OutgoingTextMsgObserver";

    public OutgoingTextMsgObserver(Handler handler, Context context) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.d(TAG, "onChange called");
        Cursor cur = mContext.getContentResolver().query(Uri.parse("content://sms"),null,null,null,null);
        if (cur.moveToNext()) {
            Integer protocol = cur.getInt(cur.getColumnIndex("protocol"));
            Integer type=cur.getInt(cur.getColumnIndex("type"));
            // We are only concerned with messages that are just sent
            if (protocol == 0 &&  type == 2) {
                String messageString = cur.getString(cur.getColumnIndex("body"));
                Toast toast = Toast.makeText(mContext, "Sent: " + messageString, Toast.LENGTH_LONG);
                toast.show();
            }
        }
        cur.close();
    }
}

