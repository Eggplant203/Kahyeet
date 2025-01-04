import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClientHandler class handles communication with a single client.
 * It processes messages from the client and sends responses back.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Player player;

    /**
     * Constructor for ClientHandler.
     * @param socket The socket connected to the client.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Receive initial message to get the username from the client
            String initialMessage = in.readLine();
            if (initialMessage != null && initialMessage.startsWith("USERNAME:")) {
                String username = initialMessage.substring(9);
                if (Server.isPlayerKicked(username)) {
                    sendMessageToClient("ERROR: You have been kicked from the server and cannot reconnect.");
                    closeConnection();
                    return;
                }
                if (Server.isUsernameTaken(username)) {
                    sendMessageToClient("ERROR: Username already taken.");
                    closeConnection();
                    return;
                } else if (Server.isGameStarted()) {
                    sendMessageToClient("ERROR: Game already started.");
                    closeConnection();
                    return;
                }
                player = new Player(username);
                Server.broadcast(username + " has joined.");
                Server.addClient(true);
                Server.updateCompletedClientsCount();
                try {
                    Thread.sleep(1000);
                    Server.updateWaitingPlayers();
                } catch (InterruptedException e) {
                    System.err.format("IOException: %s%n", e);
                }
            }

            // Receive subsequent messages from the client
            String message;
            while ((message = in.readLine()) != null) {  // Read messages from client
                System.out.println(player.getUsername() + ": " + message);
                // Handle answer message
                if (message.startsWith("ANSWER:")) {
                    handleAnswer(message);
                } else if (message.startsWith("SCORE:")) {
                    int score = Integer.parseInt(message.split(":")[1]);
                    player.setScore(score);
                } else if (message.equals("END")) {
                    player.markFinished();
                    player.saveScore();
                    Server.checkAllPlayersFinished();
                }
                // Add more client messages here
            }
        } catch (IOException e) {
            disconnectPlayer();
        } finally {
            closeConnection();
        }
    }

    /**
     * Sends a message to the client.
     * @param message The message to send.
     */
    public void sendMessageToClient(String message) {
        out.println(message);  // Send message to client
    }

    /**
     * Handles the answer message from the client.
     * @param messageResult The answer message.
     */
    private void handleAnswer(String messageResult) {
        System.out.println("PLAYER " + player.getUsername() + " " + messageResult);
    }

    /**
     * Disconnects the player and updates the server state.
     */
    public void disconnectPlayer() {
        if (player != null) {
            Server.broadcast(player.getUsername() + " disconnected.");
            if (!player.isFinished() && !Server.isPlayerKicked(player.getUsername())) {
                player.saveScore("disconnected");
            }
            Server.removeClient(player.getUsername());
            Server.addClient(false);
            Server.updateCompletedClientsCount();
            Server.updateWaitingPlayers();
        }
    }

    /**
     * Closes the connection to the client.
     */
    public void closeConnection() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the player associated with this handler.
     * @return The player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sends the latest score data to the client.
     */
    public void sendLatestScoreDataToClient() {
        try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
            List<String> latestSection = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-------------***---------------")) {
                    latestSection.clear();  // Clear previous lines when a new section starts
                }
                latestSection.add(line);  // Add line to latest section
            }
            
            // Send the latest section to the client
            for (String sectionLine : latestSection) {
                if (!sectionLine.startsWith("-------------------------------") && !sectionLine.startsWith("-------------***---------------")) {
                    sendMessageToClient("SCORE_DATA:" + sectionLine);  // Send only relevant score lines
                }
            }
            sendMessageToClient("SCORE_DATA_END");  // End marker for score data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the questions to the client.
     */
    public void sendQuestionsToClient() {
        try (BufferedReader reader = new BufferedReader(new FileReader("questions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sendMessageToClient("QUESTION:" + line);
            }
            sendMessageToClient("QUESTION_END");
        } catch (IOException e) {
            System.out.println("Failed to load questions.");
            e.printStackTrace();
        }
    }
}