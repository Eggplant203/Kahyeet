import java.util.List;

/**
 * Question class represents a single question in the Kahyeet game.
 * It contains the question text, a list of answer options, and the index of the correct answer.
 */
public class Question {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;

    /**
     * Constructor for Question.
     * @param questionText The text of the question.
     * @param options The list of answer options.
     * @param correctAnswerIndex The index of the correct answer in the options list.
     */
    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    /**
     * Gets the text of the question.
     * @return The question text.
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Gets the list of answer options.
     * @return The list of options.
     */
    public List<String> getOptions() {
        return options;
    }

    /**
     * Gets the index of the correct answer.
     * @return The index of the correct answer.
     */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    /**
     * Checks if the provided answer index is correct.
     * @param answerIndex The index of the selected answer.
     * @return True if the answer is correct, false otherwise.
     */
    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
}