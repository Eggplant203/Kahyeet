import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * WaitUI class represents the waiting room user interface for the Kahyeet game.
 * It displays the list of players waiting to join the game and plays background music.
 */
public class WaitUI extends JFrame {
    private String username;
    private JTextArea leftWaitingPlayersArea;
    private JTextArea rightWaitingPlayersArea;
    private Sound backgroundMusic;

    /**
     * Constructor for WaitUI.
     * @param username The username of the current player.
     */
    public WaitUI(String username) {
        this.username = username;

        setTitle("Waiting Room - " + username);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel waitingLabel = new JLabel("Waiting for other players to join...", SwingConstants.CENTER);
        add(waitingLabel, BorderLayout.NORTH);

        JPanel waitingPlayersPanel = new JPanel(new GridLayout(1, 2));

        leftWaitingPlayersArea = new JTextArea();
        leftWaitingPlayersArea.setEditable(false);
        leftWaitingPlayersArea.setFocusable(false);
        leftWaitingPlayersArea.setLineWrap(true);
        leftWaitingPlayersArea.setWrapStyleWord(true);
        waitingPlayersPanel.add(new JScrollPane(leftWaitingPlayersArea));

        rightWaitingPlayersArea = new JTextArea();
        rightWaitingPlayersArea.setEditable(false);
        rightWaitingPlayersArea.setFocusable(false);
        rightWaitingPlayersArea.setLineWrap(true);
        rightWaitingPlayersArea.setWrapStyleWord(true);
        waitingPlayersPanel.add(new JScrollPane(rightWaitingPlayersArea));

        add(waitingPlayersPanel, BorderLayout.CENTER);

        setVisible(true);

        // Play background music when the waiting room is opened
        backgroundMusic = new Sound("background_wait.wav");
        backgroundMusic.playLoop();
    }

    /**
     * Updates the list of waiting players displayed in the UI.
     * @param playerUsernames The list of usernames of the waiting players.
     */
    public void updateWaitingPlayers(List<String> playerUsernames) {
        leftWaitingPlayersArea.setText("");
        rightWaitingPlayersArea.setText("");
        for (int i = 0; i < playerUsernames.size(); i++) {
            if (i % 2 == 0) {
                leftWaitingPlayersArea.append(playerUsernames.get(i) + "\n");
            } else {
                rightWaitingPlayersArea.append(playerUsernames.get(i) + "\n");
            }
        }
    }

    /**
     * Closes the waiting room UI and stops the background music.
     */
    public void close() {
        setVisible(false);
        dispose();
        // Stop background music when the waiting room is closed
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
}