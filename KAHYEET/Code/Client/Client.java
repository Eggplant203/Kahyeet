import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Client class represents the client-side of the Kahyeet game.
 * It handles the connection to the server, communication, and game logic.
 */
public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private WaitUI waitUI;
    private LoginUI loginUI;
    private List<Question> questions = new ArrayList<>();
    private GameUI gameUI;
    private boolean connected = false;
    private boolean finished = false;
    private boolean shuffleQuestions = false;
    private boolean shuffleAnswers = false;
    private boolean dontShowAnswers = false;
    private boolean noBonusPoint = false;
    private int questionTimer;

    private Sound leaderboardSound;
    private Sound backgroundMusic;

    /**
     * Constructor for Client.
     * @param loginUI The login UI.
     * @param username The username of the player.
     * @param address The server address.
     * @param port The server port.
     */
    public Client(LoginUI loginUI, String username, String address, int port) {
        this.loginUI = loginUI;
        this.username = username;
        connectToServer(address, port);
    }

    /**
     * Connects to the server and starts communication.
     * @param address The server address.
     * @param port The server port.
     */
    private void connectToServer(String address, int port) {
        try {
            socket = new Socket(address, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Send username to server
            sendMessageToServer("USERNAME:" + username);
            String serverResponse = reader.readLine();
            if (serverResponse != null && serverResponse.startsWith("ERROR")) {
                loginUI.showMessage(serverResponse);
                socket.close();
                return;
            }

            leaderboardSound = new Sound("show_leaderboard.wav");
            backgroundMusic = new Sound("background_game.wav");
            
            // Start a thread to listen for messages from the server
            Thread listenThread = new Thread(new ClientListener());
            listenThread.start();
            waitUI = new WaitUI(username);
            connected = true;
        } catch (UnknownHostException e) {
            loginUI.showMessage("Invalid server address.");
        } catch (IOException e) {
            loginUI.showMessage("Unable to establish a connection.");
        }
    }

    /**
     * Sends a message to the server.
     * @param message The message to send.
     */
    public void sendMessageToServer(String message) {
        writer.println(message);  // Send message to server
        System.out.println("Sent to server: " + message);
    }

    public boolean isConnected() {
        return connected; // Return connection status
    }

    public boolean isFinished() {
        return finished;
    }
    
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isShuffleQuestions() {
        return shuffleQuestions;
    }

    public boolean isShuffleAnswers() {
        return shuffleAnswers;
    }

    public boolean isDontShowAnswers() {
        return dontShowAnswers;
    }

    public boolean isNoBonusPoint() {
        return noBonusPoint;
    }

    public void setShuffleQuestions(boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public void setShuffleAnswers(boolean shuffleAnswers) {
        this.shuffleAnswers = shuffleAnswers;
    }

    public void setDontShowAnswers(boolean dontShowAnswers) {
        this.dontShowAnswers = dontShowAnswers;
    }

    public void setNoBonusPoint(boolean noBonusPoint) {
        this.noBonusPoint = noBonusPoint;
    }

    /**
     * Handles connection loss by showing a dialog and closing the application.
     */
    private void handleConnectionLoss() {
        // Create a JOptionPane to notify about connection loss
        JOptionPane optionPane = new JOptionPane("Connection to server lost.", JOptionPane.WARNING_MESSAGE);
        JDialog dialog = optionPane.createDialog(loginUI, "Connection Lost");
        dialog.setAlwaysOnTop(true);
        
        // Create a Timer to automatically close the dialog
        Timer timer = new Timer(2000, e -> dialog.dispose());
        timer.setRepeats(false);  // Run once and stop
        
        timer.start();  // Start the Timer
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);  // Allow dialog to be closed
        dialog.setVisible(true);  // Show the dialog

        // Close other windows and exit the application
        if (waitUI != null) {
            waitUI.close();
        }
        if (gameUI != null) {
            gameUI.close();
        }
        System.exit(0);
    }

    /**
     * ClientListener class listens for messages from the server.
     */
    private class ClientListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverMessage;
                StringBuilder scoreData = new StringBuilder();  // Buffer for score data
                List<String> currentOptions = new ArrayList<>(); // Temporary options storage
                String currentQuestionText = null; // Temporary question text storage
                int correctAnswerIndex = -1;
                while ((serverMessage = reader.readLine()) != null) {
                    if (serverMessage.equals("KICK")) {
                        handleConnectionLoss();
                    }
                    // QUESTION
                    else if (serverMessage.startsWith("QUESTION:")) {
                        String line = serverMessage.substring(9).trim();

                        if (line.isEmpty()) {
                            continue;
                        } else if (currentQuestionText == null) {
                            // Set question text
                            currentQuestionText = line;
                        } else {
                            // Process options
                            if (line.endsWith("_@#")) {
                                currentOptions.add(line.replace("_@#", ""));
                                correctAnswerIndex = currentOptions.size() - 1;
                            } else {
                                currentOptions.add(line);
                            }

                            // When four options are collected, add the question
                            if (currentOptions.size() == 4) {
                                correctAnswerIndex = shuffleOptionsWithPrefixes(currentOptions, correctAnswerIndex);
                                questions.add(new Question(currentQuestionText, new ArrayList<>(currentOptions), correctAnswerIndex));

                                currentQuestionText = null;
                                currentOptions.clear();
                                correctAnswerIndex = -1;
                            }
                        }
                    } else if (serverMessage.equals("QUESTION_END")) {
                        if (isShuffleQuestions()) Collections.shuffle(questions);
                    // SHUFFLE
                    } else if (serverMessage.equals("SHUFFLE_QUESTIONS")) {
                        setShuffleQuestions(true);
                    } else if (serverMessage.equals("SHUFFLE_ANSWERS")) {
                        setShuffleAnswers(true);
                    // SHOW_ASWERS
                    } else if (serverMessage.equals("DONT_SHOW_TRUE_ANSWERS")) {
                        setDontShowAnswers(true);
                    // NO_BONUS_POINT
                    } else if (serverMessage.equals("NO_BONUS_POINT")) {
                        setNoBonusPoint(true);
                    // START_GAME
                    } else if (serverMessage.equals("START_GAME")) {
                        if (waitUI != null) {
                            waitUI.close();
                        }
                        gameUI = new GameUI(Client.this, username, getQuestionTimer(), questions);
                        // Play background music when entering GameUI
                        backgroundMusic.playLoop();
                    // TIMER
                    } else if (serverMessage.startsWith("TIMER:")) {
                        questionTimer = Integer.parseInt(serverMessage.substring(6));
                    // UPDATE_WAITING_LIST
                    } else if (serverMessage.startsWith("UPDATE_WAITING_LIST:")) {
                        String[] usernames = serverMessage.substring(20).split(",");
                        List<String> usernameList = Arrays.asList(usernames);
                        if (waitUI != null) {
                        waitUI.updateWaitingPlayers(usernameList);
                        }
                    // FINISH
                    } else if (serverMessage.equals("FINISH")) {
                        gameUI.finish();
                    // SHOW_LEADERBOARD
                    } else if (serverMessage.equals("SHOW_LEADERBOARD")) {
                        scoreData.setLength(0);  // Clear buffer before receiving new score data
                    } else if (serverMessage.startsWith("SCORE_DATA:")) {
                        scoreData.append(serverMessage.substring(11)).append("\n");  // Accumulate score data
                    } else if (serverMessage.equals("SCORE_DATA_END")) {
                        if (backgroundMusic != null) {
                            backgroundMusic.stop();
                        }
                        leaderboardSound.playOnce();
                        new LeaderUI(username, scoreData.toString());  // Pass accumulated score data to LeaderUI
                    }
                    // Add more server messages here
                }
            } catch (IOException e) {
                e.printStackTrace();
                handleConnectionLoss();
            }
        }
    }

    /**
     * Shuffles the options with prefixes and updates the correct answer index.
     * @param options The list of options.
     * @param correctIndex The index of the correct answer.
     * @return The new index of the correct answer after shuffling.
     */
    private int shuffleOptionsWithPrefixes(List<String> options, int correctIndex) {
        List<String> prefixedOptions = new ArrayList<>();
        int newCorrectAnswerIndex = correctIndex;
    
        if (isShuffleAnswers()) {
            // Shuffle indices if shuffling is enabled
            List<Integer> indices = Arrays.asList(0, 1, 2, 3);
            Collections.shuffle(indices);
    
            // Shuffle options and add prefixes
            for (int i = 0; i < indices.size(); i++) {
                int originalIndex = indices.get(i);
                String prefix = (char) ('A' + i) + ". ";
                prefixedOptions.add(prefix + options.get(originalIndex));
    
                if (originalIndex == correctIndex) {
                    newCorrectAnswerIndex = i; // Update the correct answer index based on shuffled order
                }
            }
        } else {
            // No shuffle: Add prefixes in original order
            for (int i = 0; i < options.size(); i++) {
                String prefix = (char) ('A' + i) + ". ";
                prefixedOptions.add(prefix + options.get(i));
            }
        }
    
        options.clear();
        options.addAll(prefixedOptions);
        return newCorrectAnswerIndex;
    }

    public int getQuestionTimer() {
        return questionTimer * 1000;
    }
}