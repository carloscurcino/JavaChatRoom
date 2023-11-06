import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Server {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static Map<String, PrintWriter> clientWritersMap = new HashMap<>();
    private static Map<String, Boolean> mutedUsers = new HashMap<>();
    static Set<String> blockedUsers = new HashSet<>();
    private static Map<String, String> clientStatus = new HashMap<>();
    private static Clip musicClip;
    private static boolean isMusicPlaying = false;

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(1337);
            System.out.println("Server is running and waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                Thread clientThread = new Thread(new ClientConnectionHandler(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientConnectionHandler implements Runnable {
        private Socket clientSocket;

        public ClientConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"),
                        true);
                clientWriters.add(writer);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                String clientMessage = reader.readLine();

                if (clientMessage != null && clientMessage.startsWith("JOIN ")) {
                    String clientName = clientMessage.substring(5);
                    synchronized (clientWritersMap) {
                        clientWritersMap.put(clientName, writer);
                    }
                    sendToAllClients("Server: " + clientName + " has joined the chat.", clientName);
                    System.out.println("Server: " + clientName + " has joined the chat.");

                    // Passa o clientName para o ClientHandler
                    Thread clientHandler = new Thread(new ClientHandler(clientSocket, writer, clientName));
                    clientHandler.start();
                } else {
                    System.out.println("Unknown command: " + clientMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String clientName;

        public ClientHandler(Socket clientSocket, PrintWriter writer, String clientName) {
            this.clientSocket = clientSocket;
            this.writer = writer;
            this.clientName = clientName;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                String clientMessage;

                while ((clientMessage = reader.readLine()) != null) {
                    if (clientMessage.startsWith("LEAVE ")) {
                        String leavingClientName = clientMessage.substring(6);
                        synchronized (clientWritersMap) {
                            sendToAllClients("Server: " + leavingClientName + " has left the chat.", clientName);
                            clientWritersMap.remove(leavingClientName);
                        }
                        break;
                    } else if (clientMessage.equalsIgnoreCase("USERS")) {
                        sendUserList();
                    } else if (clientMessage.startsWith("MESSAGE ")) {
                        String message = clientMessage.substring(8);
                        sendToAllClients("MESSAGE " + clientName + ": " + message, clientName);
                    } else if (clientMessage.startsWith("PRIVATE ")) {
                        String[] parts = clientMessage.split(" ", 3);
                        String targetUser = parts[1];
                        String privateMessage = parts[2];
                        sendPrivateMessage(clientName, targetUser, privateMessage);
                    } else if (clientMessage.equalsIgnoreCase("HELP")) {
                        showCommandList(writer);
                    } else if (clientMessage.startsWith("BLOCK")) {
                        String blockedUser = clientMessage.substring(6);
                        blockedUsers.add(blockedUser);
                    } else if (clientMessage.startsWith("UNBLOCK")) {
                        String unblockedUser = clientMessage.substring(8);
                        blockedUsers.remove(unblockedUser);
                    } else if (clientMessage.equalsIgnoreCase("YODA")) {
                        drawYoda(writer);
                    } else if (clientMessage.startsWith("COFFEE")) {
                        String[] parts = clientMessage.split(" ", 2);
                        String targetUser = parts[1];
                        sendCoffee(clientName, targetUser);
                    } else if (clientMessage.startsWith("IMPORTANT")) {
                        String message = clientMessage.substring(9); // Remova o "IMPORTANT " do início
                        String messageImportantRectangle = generateRectangleMessage(message);
                        sendToAllClients("IMPORTANT " + clientName + ": \n" + messageImportantRectangle, clientName);
                    } else if (clientMessage.startsWith("MUTE") || clientMessage.startsWith("MUTE ")) {
                        String mutedUser = clientMessage.substring(5);
                        if (!mutedUsers.containsKey(mutedUser)) {
                            mutedUsers.put(mutedUser, true);
                            writer.println("Server: " + mutedUser + " has been muted.");
                        } else {
                            writer.println("Server: " + mutedUser + " is already muted.");
                        }
                    } else if (clientMessage.startsWith("UNMUTE") || clientMessage.startsWith("UNMUTE ")) {
                        String unmutedUser = clientMessage.substring(7);
                        if (mutedUsers.containsKey(unmutedUser)) {
                            mutedUsers.remove(unmutedUser);
                            writer.println("Server: " + unmutedUser + " has been unmuted.");
                        } else {
                            writer.println("Server: " + unmutedUser + " is not currently muted.");
                        }
                    } else if (clientMessage.startsWith("CHANGE_NAME ")) {
                        String newName = clientMessage.substring(12);
                        changeUserName(clientName, newName);
                        clientName = newName;
                    } else if (clientMessage.startsWith("SET_STATUS ")) {
                        String newStatus = clientMessage.substring(10);
                        setStatus(clientName, newStatus);
                    } else if (clientMessage.startsWith("STATUS ")) {
                        String requestedUser = clientMessage.substring(7);
                        displayStatus(clientName, requestedUser);
                    } else if (clientMessage.startsWith("EMOJI ")) {
                        String[] parts = clientMessage.split(" ", 2);
                        String emoji = parts[1];
                        sendEmoji(clientName, emoji);
                    } else if (clientMessage.equalsIgnoreCase("EMOJI_LIST")) {
                        sendEmojiList(clientName);
                    } else if (clientMessage.equalsIgnoreCase("PLAY_MUSIC")) {
                        playMusic(writer);
                    } else if (clientMessage.equalsIgnoreCase("STOP_MUSIC")) {
                        stopMusic(writer);
                    } else {
                        if (!isMuted(clientName)) {
                            System.out.println(clientMessage);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendToAllClients(String message, String sender) {
        synchronized (clientWriters) {
            for (PrintWriter clientWriter : clientWriters) {
                // Verifique se o remetente não está na lista de bloqueados do cliente atual.
                if (!blockedUsers.contains(sender)) {
                    clientWriter.println(message);
                    clientWriter.flush();
                }
            }
        }
    }

    private static void sendPrivateMessage(String sender, String target, String message) {
        synchronized (clientWritersMap) {
            PrintWriter targetWriter = clientWritersMap.get(target);
            if (targetWriter != null) {
                targetWriter.println("PRIVATE " + sender + ": " + message);
                targetWriter.flush();
            }
        }
    }

    private static void sendCoffee(String sender, String target) {
        synchronized (clientWritersMap) {
            PrintWriter targetWriter = clientWritersMap.get(target);
            if (targetWriter != null) {
                targetWriter.println(sender + " is sending a c[_] coffee for you! ;)");
                targetWriter.flush();
            }
        }
    }

    private static void sendUserList() {
        StringBuilder userList = new StringBuilder("Users in the chat: ");
        for (String userName : clientWritersMap.keySet()) {
            userList.append(userName).append(", ");
        }
        synchronized (clientWriters) {
            for (PrintWriter clientWriter : clientWriters) {
                clientWriter.println(userList.toString());
                clientWriter.flush();
            }
        }
    }

    public static void showCommandList(PrintWriter writer) {
        writer.println("Server: Available commands:");
        writer.println("Server: - JOIN <name>: Join the chat with your name.");
        writer.println("Server: - LEAVE: Leave the chat.");
        writer.println("Server: - USERS: List users in the chat.");
        writer.println("Server: - MESSAGE <text>: Send a public message.");
        writer.println("Server: - PRIVATE <user> <text>: Send a private message to a user.");
        writer.println("Server: - HELP: Show available commands.");
        writer.println("Server: - BLOCK <user>: Block messages from a specific user.");
        writer.println("Server: - UNBLOCK <user>: Unblock messages from a blocked user.");
        writer.println("Server: - YODA: Display a drawing of Yoda.");
        writer.println("Server: - COFFEE <user>: Send a special coffee message to a user.");
        writer.println("Server: - IMPORTANT <text>: Send an important message with a rectangular frame.");
        writer.println("Server: - MUTE <user>: Mute a specific user.");
        writer.println("Server: - UNMUTE <user>: Remove muting from a user.");
        writer.println("Server: - CHANGE_NAME <newName>: Change the username.");
        writer.println("Server: - SET_STATUS <status>: Set the user's status.");
        writer.println("Server: - STATUS <user>: Display a user's status.");
        writer.println("Server: - EMOJI <emoji>: Send an emoji.");
        writer.println("Server: - EMOJI_LIST: Display the list of available emojis.");
        writer.println("Server: - PLAY_MUSIC: Play a music message.");
        writer.println("Server: - STOP_MUSIC: Stop the music.");
    }

    public static void drawYoda(PrintWriter writer) {
        writer.println("           .--.                  Try not.\r\n" + //
                " ::\\`--._,'.::.`._.--'/::     Do or do not.\r\n" + //
                " ::::.  ` __::__ '  .::::    There is no try.\r\n" + //
                " ::::::-:.`'..`'.:-::::::\r\n" + //
                " ::::::::\\ `--' /::::::::              -Yoda");
    }

    // Função para criar uma mensagem com moldura retangular
    private static String generateRectangleMessage(String message) {
        int messageLength = message.length();
        int rectangleWidth = messageLength + 4; // Largura da moldura

        StringBuilder rectangleMessage = new StringBuilder();

        // Linha superior da moldura
        for (int i = 0; i < rectangleWidth; i++) {
            rectangleMessage.append("*");
        }

        // Linhas com a mensagem e os caracteres da moldura
        rectangleMessage.append("\n* ");
        rectangleMessage.append(message);
        rectangleMessage.append(" *\n");

        // Linha inferior da moldura
        for (int i = 0; i < rectangleWidth; i++) {
            rectangleMessage.append("*");
        }

        return rectangleMessage.toString();
    }

    private static boolean isMuted(String userName) {
        return mutedUsers.containsKey(userName) && mutedUsers.get(userName);
    }

    private static void changeUserName(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return; // O novo nome é o mesmo que o antigo, não é necessário fazer nada.
        }

        PrintWriter oldWriter = null;
        synchronized (clientWritersMap) {
            if (clientWritersMap.containsKey(oldName)) {
                oldWriter = clientWritersMap.remove(oldName); // Remove o nome de usuário antigo
                clientWritersMap.put(newName, oldWriter); // Adiciona o novo nome de usuário
            }
        }

        if (oldWriter != null) {
            oldWriter.println("Server: Your name has been changed to " + newName);
        }

        sendToAllClients("Server: " + oldName + " has changed their name to " + newName, oldName);
    }

    private static void setStatus(String userName, String newStatus) {
        synchronized (clientStatus) {
            clientStatus.put(userName, newStatus);
        }
        sendPrivateMessage("Server", userName, "Your status has been set to:" + newStatus);
    }

    private static void displayStatus(String requester, String target) {
        if (clientStatus.containsKey(target)) {
            String statusMessage = "Status of " + target + ": " + clientStatus.get(target);
            clientWritersMap.get(requester).println(statusMessage);
        } else {
            String statusMessage = "Status of " + target + ": Status not set";
            clientWritersMap.get(requester).println(statusMessage);
        }
    }

    private static void sendEmoji(String userName, String emoji) {
        String mappedEmoji = mapEmoji(emoji);
        synchronized (clientWriters) {
            for (PrintWriter clientWriter : clientWriters) {
                // Verifique se o remetente não está na lista de bloqueados do cliente atual.
                if (!blockedUsers.contains(userName)) {
                    clientWriter.println("EMOJI " + userName + ": " + mappedEmoji);
                    clientWriter.flush();
                }
            }
        }
    }

    private static void sendEmojiList(String userName) {
        // Crie uma lista de emojis disponíveis (você pode armazená-los em uma lista ou
        // array)
        List<String> availableEmojis = new ArrayList<>();
        availableEmojis.add("smile");
        availableEmojis.add("heart");
        availableEmojis.add("thumbsup");
        availableEmojis.add("coffee");
        availableEmojis.add("fish");

        // Mapeie emojis personalizados
        Map<String, String> customEmojis = new HashMap<>();
        customEmojis.put("smile", ":D");
        customEmojis.put("heart", "<3");
        customEmojis.put("thumbsup", "|B");
        customEmojis.put("coffee", "c[_]");
        customEmojis.put("fish", "><>");

        // Envie a lista de emojis para o cliente
        PrintWriter clientWriter = clientWritersMap.get(userName);
        if (clientWriter != null) {
            clientWriter.println("Available Emojis:");
            for (String emoji : availableEmojis) {
                String emojiName = emoji;
                String mappedEmoji = customEmojis.getOrDefault(emoji, emoji);
                clientWriter.println(emojiName + " - " + mappedEmoji);
            }
            clientWriter.flush();
        }
    }

    private static String mapEmoji(String emoji) {
        // Mapeie emojis personalizados
        Map<String, String> customEmojis = new HashMap<>();
        customEmojis.put("smile", ":D");
        customEmojis.put("heart", "<3");
        customEmojis.put("thumbsup", "|B");
        customEmojis.put("coffee", "c[_]");
        customEmojis.put("fish", "><>");

        // Verifique se o emoji é personalizado
        String mappedEmoji = customEmojis.getOrDefault(emoji, emoji);

        return mappedEmoji;
    }

    public static void playMusic(PrintWriter writer) {
        writer.println("\n" + //
                "      » [Ela partiu - Tim Maia] «\n" + //
                "           0:00 ─o───── 4:15\n" + //
                "     <=>   <<   II   >>   %\n" + //
                "");

        try {
            // Carrega o arquivo de áudio
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("TimMaia–ElaPartiu.wav"));

            // Obtém um clip de áudio
            musicClip = AudioSystem.getClip();

            // Abre o arquivo de áudio
            musicClip.open(audioInputStream);

            // Reproduz o áudio
            musicClip.start();
            isMusicPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopMusic(PrintWriter writer) {
        if (isMusicPlaying && musicClip != null) {
            musicClip.stop();
            musicClip.close();
            writer.println("Server: Music has been stopped.");
            isMusicPlaying = false;
        } else {
            writer.println("Server: No music is currently playing.");
        }
    }
}
