package edu.swarthmore.cs.moodtracker.db;

import android.database.Cursor;
import edu.swarthmore.cs.moodtracker.db.TrackContract.TextMsgInfoSchema;
/**
 * Created by cwang3 on 11/26/14.
 */
public class TextMsgEntry {
    public int id;
    public long date;
    public String sender;
    public String receiver;
    public int type;
    public String message;
    public double neutral;
    public double positive;
    public double negative;

    // Constructor
    public TextMsgEntry(int id, long date, String sender, String receiver, int type,
                        String message, double neutral, double positive, double negative) {
        this.id = -1;
        this.date = -1;
        this.sender = "";
        this.receiver = "";
        this.type = -1;
        this.message = "";
        this.neutral = -1;
        this.positive = -1;
        this.negative = -1;
    }

    public TextMsgEntry (Cursor cursor) {
        // Keep default values if cursor is already finished reading.
        if (cursor.isAfterLast()) {
            return;
        }
        this.id = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_ID));
        this.date = cursor.getLong(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_DATE));
        this.sender = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_SENDER));
        this.receiver = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_RECEIVER));
        this.type = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_TYPE));
        this.message = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_MESSAGE));
        this.neutral = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_NEUTRAL));
        this.positive = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_POS));
        this.negative = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_NEG));
    }
}
