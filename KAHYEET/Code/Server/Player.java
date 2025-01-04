import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Player class represents a player in the Kahyeet game.
 * It stores the player's username, score, and completion status.
 */
public class Player {
    private String username;
    private int score;
    private boolean finished; // Field to track if the player has completed all questions

    /**
     * Constructor for Player.
     * @param username The username of the player.
     */
    public Player(String username) {
        this.username = username;
        this.score = 0;
        this.finished = false;
    }

    /**
     * Gets the username of the player.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the score of the player.
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score of the player.
     * @param score The score to set.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Marks the player as finished.
     */
    public void markFinished() {
        this.finished = true;
    }

    /**
     * Checks if the player has finished.
     * @return True if the player has finished, false otherwise.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Saves the player's score to a file.
     */
    public void saveScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(username + ": " + score + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the player's score to a file with additional notes.
     * @param notes Additional notes to save with the score.
     */
    public void saveScore(String notes) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write(username + ": " + score + " (" + notes + ")" + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a separator line to the score file after all players finish.
     */
    public static void addSeparatorLine() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("scores.txt", true))) {
            writer.write("-------------------------------\n");
            writer.write(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n");
            writer.write("-------------***---------------\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}