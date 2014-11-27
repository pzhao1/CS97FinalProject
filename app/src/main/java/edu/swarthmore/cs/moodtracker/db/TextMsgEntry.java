package edu.swarthmore.cs.moodtracker.db;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

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
    public double neutral = -1.0;
    public double positive = -1.0;
    public double negative = -1.0;

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
        this.id = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_ID));
        this.date = cursor.getLong(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_DATE));
        this.sender = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_SENDER));
        this.receiver = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_RECEIVER));
        this.type = cursor.getInt(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_TYPE));
        this.message = cursor.getString(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_MESSAGE));
        this.neutral = cursor.getDouble(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_NEUTRAL));
        this.positive = cursor.getDouble(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_POS));
        this.negative = cursor.getDouble(cursor.getColumnIndex(TextMsgInfoSchema.COLUMN_NEG));

        cursor.moveToNext();
    }

    /**
     * Converts class to a JSON object. Used for exporting data.
     * @return The JSON representation of this class.
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("id", this.id);
        jsonObj.put("data", this.date);
        jsonObj.put("sender", this.sender);
        jsonObj.put("receiver", this.receiver);
        jsonObj.put("type", this.type);
        jsonObj.put("message", this.message);
        jsonObj.put("neutral", this.neutral);
        jsonObj.put("positive", this.positive);
        jsonObj.put("negative", this.negative);
        return jsonObj;
    }
}
