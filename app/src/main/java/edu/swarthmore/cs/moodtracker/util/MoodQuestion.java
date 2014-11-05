package edu.swarthmore.cs.moodtracker.util;

/**
 * Created by rliang on 11/3/14.
 */
public class MoodQuestion {
    public static final String MOOD_ACTIVE = "Active";
    public static final String MOOD_DETERMINED = "Determined";
    public static final String MOOD_ATTENTIVE = "Attentive";
    public static final String MOOD_INSPIRED = "Inspired";
    public static final String MOOD_ALERT = "Alert";
    public static final String MOOD_AFRAID = "Afraid";
    public static final String MOOD_NERVOUS = "Nervous";
    public static final String MOOD_UPSET = "Upset";
    public static final String MOOD_HOSTILE = "Hostile";
    public static final String MOOD_ASHAMED = "Ashamed";

    private String mQuestion;
    private String mAnswer;

    public static MoodQuestion questionFromDBString(String encodedString) {
        String[] decodedInfo = StringEncodeUtil.decode(encodedString);
        return new MoodQuestion(decodedInfo[1], decodedInfo[2]);
    }

    public MoodQuestion(String question, String answer) {
        mQuestion = question;
        mAnswer = answer;
    }

    public String getQuestion() {
        return mQuestion;
    }

    public String getAnswer() {
        return mAnswer;
    }


    @Override
    public String toString() {
        String[] toEncode = new String[] {mQuestion, mAnswer};
        return StringEncodeUtil.encode(toEncode);
    }
}
