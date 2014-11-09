package edu.swarthmore.cs.moodtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by cwang3 on 11/9/14.
 * Detect when text messages are received and record it (not yet implemented)
 */
public class TextMsgReceiver extends BroadcastReceiver{
    public static final String TAG = "TextMsgReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        Bundle bundle = intent.getExtras();
        SmsMessage[] message;
        String messageString = "";
        if (bundle != null) {
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            message = new SmsMessage[pdusObj.length];
            for (int i=0; i< pdusObj.length; i++) {
                message[i] = SmsMessage.createFromPdu((byte[])pdusObj[i]);
                messageString += message[i].getMessageBody();
            }
            Toast toast = Toast.makeText(context, "Received: " + messageString, Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
