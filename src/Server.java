import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Server {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static Map<String, PrintWriter> clientWritersMap = new HashMap<>();
    static Set<String> blockedUsers = new HashSet<>();

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345);
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
                    } else if (clientMessage.equals("USERS")) {
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
                    } else if (clientMessage.startsWith("BLOCK ")) {
                        String blockedUser = clientMessage.substring(6);
                        blockedUsers.add(blockedUser);
                    } else if (clientMessage.startsWith("UNBLOCK ")) {
                        String unblockedUser = clientMessage.substring(8);
                        blockedUsers.remove(unblockedUser);
                    } else if (clientMessage.equalsIgnoreCase("YODA")) {
                        drawYoda(writer);
                    } else if (clientMessage.startsWith("COFFEE ")) {
                        String[] parts = clientMessage.split(" ", 2);
                        String targetUser = parts[1];
                        sendCoffee(clientName, targetUser);
                    } else {
                        System.out.println(clientMessage);
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
                targetWriter.println(sender + " is sending a c[_] coffe for you! ;)");
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
    }

    public static void drawYoda(PrintWriter writer) {
        writer.println("           .--.                  Try not.\r\n" + //
                " ::\\`--._,'.::.`._.--'/::     Do or do not.\r\n" + //
                " ::::.  ` __::__ '  .::::    There is no try.\r\n" + //
                " ::::::-:.`'..`'.:-::::::\r\n" + //
                " ::::::::\\ `--' /::::::::              -Yoda");
    }
}
