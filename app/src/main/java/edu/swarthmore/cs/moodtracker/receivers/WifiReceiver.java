package edu.swarthmore.cs.moodtracker.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.List;

import edu.swarthmore.cs.moodtracker.db.QuerySentimentTask;
import edu.swarthmore.cs.moodtracker.db.TextMsgEntry;
import edu.swarthmore.cs.moodtracker.db.TrackDatabase;

/**
 * Created by cwang3 on 11/26/14.
 */
public class WifiReceiver extends BroadcastReceiver{
    public final String TAG = "WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive called");
        ConnectivityManager mConManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifi = mConManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
        if (wifi) {

            final TrackDatabase db = TrackDatabase.getInstance(context);
            TextMsgEntry entry = new TextMsgEntry(1, System.currentTimeMillis(), "test", "test", 0,
                    "I feel kind of sad" , -1, -1, -1);
            db.writeTextMsgRecord(entry);

            Log.d(TAG, "Wifi is on!");
            new QuerySentimentTask(context) {
                @Override
                public void onFinish(boolean success, String error) {
                    Log.d(TAG, "query " + ((success) ? "success!" : "failed because of " + error));
                    List<TextMsgEntry> entries = db.readTextMsg(false);
                }
            }.execute();
        }
    }
}
