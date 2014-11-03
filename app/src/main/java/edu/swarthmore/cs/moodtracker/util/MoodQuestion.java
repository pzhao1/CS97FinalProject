package edu.swarthmore.cs.moodtracker.util;

/**
 * Created by rliang on 11/3/14.
 */
public class MoodQuestion {
    public static final String MOOD_ACTIVE = "active";
    public static final String MOOD_DETERMINED = "determined";
    public static final String MOOD_ATTENTIVE = "mood_attentive";
    public static final String MOOD_INSPIRED = "mood_inspired";
    public static final String MOOD_ALERT = "mood_alert";
    public static final String MOOD_AFRAID = "afraid";
    public static final String MOOD_NERVOUS = "nervous";
    public static final String MOOD_UPSET = "upset";
    public static final String MOOD_HOSTILE = "hostile";

    public enum QuestionType {
        QUESTION_TYPE_RATING ("rating");
        private final String type;

        private QuestionType(String s) {
            type = s;
        }

        public boolean equalsType(String otherType){
            return otherType != null && type.equals(otherType);
        }

        public String toString(){
            return type;
        }
    }

    private QuestionType mQuestionType;
    private String mQuestion;
    private String mAnswer;

    public static MoodQuestion questionFromDBString(String encodedString) {
        String[] decodedInfo = StringEncodeUtil.decode(encodedString);
        return new MoodQuestion(QuestionType.valueOf(decodedInfo[0]), decodedInfo[1], decodedInfo[2]);
    }

    public MoodQuestion(QuestionType questionType, String question, String answer) {
        mQuestionType = questionType;
        mQuestion = question;
        mAnswer = answer;
    }

    public String getmQuestion() {
        return mQuestion;
    }

    public String getmAnswer() {
        return mAnswer;
    }

    public QuestionType getmQuestionType() {
        return mQuestionType;
    }

    @Override
    public String toString() {
        String[] toEncode = new String[] {mQuestionType.toString(), mQuestion, mAnswer};
        return StringEncodeUtil.encode(toEncode);
    }
}
