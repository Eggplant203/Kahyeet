import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * GameUI class represents the user interface for the Kahyeet game.
 * It handles displaying questions, managing the countdown timer, and processing user answers.
 */
public class GameUI extends Frame {
    private Client client;
    private String username;
    private List<Question> questions; // Store questions
    private Label scoreLabel, timerLabel; // Labels for displaying score and timer
    private JTextArea questionLabel; // Text area for displaying questions
    private int currentQuestionIndex = 0; // Index of the current question
    private List<Button> optionButtons = new ArrayList<>(); // Buttons for answer options
    private long questionStartTime; // Start time for each question
    private int score = 0; // Player's score
    private int questionTimer; // Time limit for each question in milliseconds
    private int wait2StartTimer = 3000; // Time limit for waiting to start the game
    private Timer countdownTimer; // Timer for countdown
    private JProgressBar progressBar; // Progress bar for countdown timer
    private int remainingTime; // Time left for the current question
    private boolean isGameStarted = false; // Flag to check if the game has started

    private Sound correctSound;
    private Sound wrongSound;
    private Sound neutralSound;

    /**
     * Constructor for GameUI.
     * @param client The client object.
     * @param username The username of the player.
     * @param questionTimer The time limit for each question.
     * @param questions The list of questions.
     */
    public GameUI(Client client, String username, int questionTimer, List<Question> questions) {
        this.client = client;
        this.username = username;
        this.questionTimer = questionTimer;
        this.questions = questions;

        setTitle("Kahyeet! - " + username);
        setSize(500, 350);
        setLayout(new GridLayout(7, 1)); // Adjusted to fit new layout

        // Load sound effects
        correctSound = new Sound("correct_answer.wav");
        wrongSound = new Sound("wrong_answer.wav");
        neutralSound = new Sound("neutral_answer.wav");

        // Panel to hold score and timer labels
        Panel labelPanel = new Panel(new BorderLayout());
        scoreLabel = new Label("Points: " + score);
        scoreLabel.setAlignment(Label.RIGHT);
        scoreLabel.setPreferredSize(new Dimension(getWidth() / 2, scoreLabel.getHeight()));
        labelPanel.add(scoreLabel, BorderLayout.EAST);

        timerLabel = new Label("Time: " + wait2StartTimer / 1000 + "s");
        timerLabel.setAlignment(Label.LEFT);
        labelPanel.add(timerLabel, BorderLayout.WEST);

        // Progress bar panel for countdown
        Panel progressBarPanel = new Panel(new BorderLayout());
        progressBar = new JProgressBar(0, wait2StartTimer);
        progressBar.setValue(wait2StartTimer);
        progressBar.setForeground(Color.GREEN);
        progressBarPanel.add(progressBar, BorderLayout.CENTER);

        add(labelPanel);
        add(progressBarPanel);
        Font boldFont = new Font("Arial", Font.BOLD, 14);
        questionLabel = new JTextArea(""); // Use TextArea for question display
        questionLabel.setFont(boldFont);
        questionLabel.setEditable(false); // Make it non-editable
        questionLabel.setWrapStyleWord(true); // Enable word wrap
        questionLabel.setLineWrap(true); // Enable line wrap
        questionLabel.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(questionLabel); // Use JScrollPane to contain JTextArea
        add(scrollPane, BorderLayout.CENTER);

        for (int i = 0; i < 4; i++) {
            Button optionButton = new Button();
            optionButtons.add(optionButton);
            add(optionButton);
            int finalI = i;
            optionButton.addActionListener(e -> {
                stopCountdown(); // Stop countdown on answer
                sendAnswer(finalI);
            });
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        setVisible(true);
        wait2Start();
    }

    /**
     * Displays the current question and its options.
     */
    private void displayQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            if (!client.isFinished()) {
                client.sendMessageToServer("END");
                questionLabel.setText("Quiz Completed!");
                client.setFinished(true);
                for (Button button : optionButtons) {
                    button.setEnabled(false);
                }
            }
            return;
        }
        Question question = questions.get(currentQuestionIndex);
        questionLabel.setText("Question " + (currentQuestionIndex + 1) + ": " + question.getQuestionText());

        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            optionButtons.get(i).setLabel(options.get(i));
        }

        questionStartTime = System.currentTimeMillis(); // Record the start time
        startCountdown(questionTimer); // Start countdown timer
    }

    /**
     * Waits for a specified time before starting the game.
     */
    private void wait2Start() {
        questionLabel.setText("Waiting " + wait2StartTimer / 1000 + " s to start the game...\nTime to answer each question is " + questionTimer/1000 + " s");
        for (Button button : optionButtons) {
            button.setEnabled(false);
        }
        startCountdown(wait2StartTimer);
    }

    /**
     * Starts the countdown timer.
     * @param timer The time limit for the countdown.
     */
    private void startCountdown(int timer) {
        remainingTime = timer;
        progressBar.setMaximum(timer);
        progressBar.setValue(timer);

        countdownTimer = new Timer(100, e -> {
            remainingTime -= 100; // Decrease time by 100 ms
            timerLabel.setText("Time: " + (remainingTime / 1000) + "s");
            progressBar.setValue(remainingTime);

            if (remainingTime <= 0) {
                stopCountdown();
                if (!isGameStarted) {
                    progressBar.setForeground(Color.RED);
                    isGameStarted = true;
                    for (Button button : optionButtons) {
                        button.setEnabled(true);
                    }
                    displayQuestion();
                }
                else {
                    sendAnswer(-1); // Send -1 to indicate timeout
                }
            }
        });
        countdownTimer.start();
    }

    /**
     * Stops the countdown timer.
     */
    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }

    /**
     * Sends the selected answer to the server and processes the result.
     * @param answerIndex The index of the selected answer.
     */
    private void sendAnswer(int answerIndex) {
        stopCountdown(); // Stop countdown if answer is submitted

        for (Button button : optionButtons) {
            button.setEnabled(false);
        }

        long responseTime = System.currentTimeMillis() - questionStartTime; // Calculate response time
        Question question = questions.get(currentQuestionIndex);
        int tempScore = 0;

        if (answerIndex == -2) {}
        else if (answerIndex != -1 && question.isCorrectAnswer(answerIndex)) {
            tempScore = calculatePoints(responseTime, questionTimer);
            client.sendMessageToServer("ANSWER TRUE QUESTION NUMBER " + (currentQuestionIndex + 1));
            if (client.isDontShowAnswers()) {
                optionButtons.get(answerIndex).setBackground(Color.BLUE);
                neutralSound.playOnce();
            } else {
                optionButtons.get(answerIndex).setBackground(Color.GREEN);
                correctSound.playOnce();
            }
        } else {
            tempScore = 0;
            client.sendMessageToServer("ANSWER FALSE QUESTION NUMBER " + (currentQuestionIndex + 1));
            if (client.isDontShowAnswers()) {
                if (answerIndex != -1) optionButtons.get(answerIndex).setBackground(Color.BLUE);
                neutralSound.playOnce();
            } else {
                if (answerIndex != -1) optionButtons.get(answerIndex).setBackground(Color.RED);
                optionButtons.get(question.getCorrectAnswerIndex()).setBackground(Color.GREEN);
                wrongSound.playOnce();
            }
        }
        if (client.isDontShowAnswers()) {
            scoreLabel.setText("Points: ??? + ?");
        } else {
            scoreLabel.setText("Points: " + score + " + " + tempScore); // Update score display
        }
        score += tempScore;

        client.sendMessageToServer("SCORE:" + score); // Send current score to ClientHandler
        currentQuestionIndex++;
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        resetAndDisplayNextQuestion();
    }

    /**
     * Calculates the points based on response time and question timer.
     * @param responseTimeMillis The response time in milliseconds.
     * @param questionTimerMillis The question timer in milliseconds.
     * @return The calculated points.
     */
    private int calculatePoints(long responseTimeMillis, int questionTimerMillis) {
        double responseTime = responseTimeMillis / 1000.0; // Convert to seconds
        double questionTimer = questionTimerMillis / 1000.0; // Convert to seconds
        double rawScore = 1000 * (1 - ((responseTime / questionTimer) / 2));
        if (client.isNoBonusPoint()) return 1000;
        else return (int) Math.round(Math.max(rawScore, 0));
    }

    /**
     * Resets the UI and displays the next question.
     */
    private void resetAndDisplayNextQuestion() {
        for (Button button : optionButtons) {
            button.setBackground(null); // Reset button background color
            button.setEnabled(true); // Enable buttons again
        }
        if (client.isDontShowAnswers()) {
            scoreLabel.setText("Points: ???");
        } else {
            scoreLabel.setText("Points: " + score);
        }
        displayQuestion();
    }

    /**
     * Gets the current score.
     * @return The current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Finishes the game.
     */
    public void finish() {
        currentQuestionIndex = questions.size() - 1; // Set the current question index to the final question (end of quiz)
        sendAnswer(-2);
    }

    /**
     * Closes the game UI.
     */
    public void close() {
        setVisible(false);
        dispose();
    }
}