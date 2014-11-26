package edu.swarthmore.cs.moodtracker.db;

import android.database.Cursor;

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
        this.id = id;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.message = message;
        this.neutral = neutral;
        this.positive = positive;
        this.negative = negative;
    }

    public TextMsgEntry (Cursor cursor) {
        // Keep default values if cursor is already finished reading.
        if (cursor.isAfterLast()) {
            return;
        }
        this.id = cursor.getInt(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_ID));
        this.date = cursor.getLong(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_DATE));
        this.sender = cursor.getString(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_SENDER));
        this.receiver = cursor.getString(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_RECEIVER));
        this.type = cursor.getInt(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_TYPE));
        this.message = cursor.getString(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_MESSAGE));
        this.neutral = cursor.getInt(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_NEUTRAL));
        this.positive = cursor.getInt(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_POS));
        this.negative = cursor.getInt(cursor.getColumnIndex(TrackContract.TextMsgInfoSchema.COLUMN_NEG));
    }
}
