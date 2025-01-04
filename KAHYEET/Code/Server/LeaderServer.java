import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * LeaderServer class represents the server-side leaderboard user interface for the Kahyeet game.
 * It displays the scores of players in a table, highlighting disconnected users.
 */
public class LeaderServer extends JFrame {
    private static boolean windowOpen = false;
    private DefaultTableModel model; // Model of the table to update when needed
    private List<ScoreEntry> scores = new ArrayList<>(); // List of ScoreEntry to use in cellRenderer

    /**
     * Constructor for LeaderServer.
     * Initializes the leaderboard window and displays the scores.
     */
    public LeaderServer() {
        windowOpen = true;
        setTitle("Leaderboard - SERVER");
        setSize(new Dimension(400, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Rank", "Username", "Score"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);

        // Read and display the leaderboard initially
        updateLeaderboard();

        // Set up the cell renderer for the table
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setHorizontalAlignment(JLabel.CENTER);

                // Use ScoreEntry object from the scores list instead of the table model
                ScoreEntry entry = scores.get(row);
                String rank = (String) table.getValueAt(row, 0);

                // Color the cell based on rank or status
                if (entry.isDisconnected()) cell.setBackground(Color.RED);
                else if (rank.equals("GOLD")) cell.setBackground(Color.YELLOW);
                else if (rank.equals("SILVER")) cell.setBackground(Color.LIGHT_GRAY);
                else if (rank.equals("BRONZE")) cell.setBackground(new Color(205, 127, 50));
                else cell.setBackground(Color.WHITE);

                cell.setForeground(entry.isDisconnected() ? Color.WHITE : Color.BLACK);
                return cell;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        setVisible(true);
    }

    /**
     * Updates the leaderboard by reading the latest scores from the file.
     */
    public void updateLeaderboard() {
        model.setRowCount(0); // Clear current rows
        scores = readScoresFromFile(); // Read new scores from file

        // Sort and add data to the table
        scores.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            String rank = switch (i) {
                case 0 -> "GOLD";
                case 1 -> "SILVER";
                case 2 -> "BRONZE";
                default -> String.valueOf(i + 1);
            };
            model.addRow(new Object[]{rank, entry.getUsername(), entry.getScore()});
        }
    }

    /**
     * Reads the scores from the file and returns a list of ScoreEntry objects.
     * @return A list of ScoreEntry objects.
     */
    private List<ScoreEntry> readScoresFromFile() {
        List<ScoreEntry> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-------------***---------------")) {
                    scores.clear();
                } else if (line.contains(":")) {
                    String[] parts = line.split(":");
                    String username = parts[0].trim();
                    boolean isDisconnected = line.contains("(disconnected)");
                    String scoreText = parts[1].trim().split(" ")[0];
                    
                    try {
                        int score = Integer.parseInt(scoreText);
                        scores.add(new ScoreEntry(username, score, isDisconnected));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format for user " + username + ": " + parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scores;
    }    

    /**
     * ScoreEntry class represents a single entry in the leaderboard.
     */
    private class ScoreEntry {
        private final String username;
        private final int score;
        private final boolean isDisconnected;

        /**
         * Constructor for ScoreEntry.
         * @param username The username of the player.
         * @param score The score of the player.
         * @param isDisconnected Whether the player is disconnected.
         */
        public ScoreEntry(String username, int score, boolean isDisconnected) {
            this.username = username;
            this.score = score;
            this.isDisconnected = isDisconnected;
        }

        public String getUsername() {
            return username;
        }

        public int getScore() {
            return score;
        }

        public boolean isDisconnected() {
            return isDisconnected;
        }
    }

    /**
     * Checks if the leaderboard window is open.
     * @return True if the window is open, false otherwise.
     */
    public boolean isWindowOpen() {
        return windowOpen;
    }

    @Override
    public void dispose() {
        windowOpen = false;
        super.dispose();
    }
}