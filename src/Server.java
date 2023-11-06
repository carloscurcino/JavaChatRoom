import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static Map<String, PrintWriter> clientWritersMap = new HashMap<>();

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server is running and waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Crie uma nova thread para lidar com cada cliente
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
                    sendToAllClients("Server: " + clientName + " has joined the chat.");
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
                            sendToAllClients("Server: " + leavingClientName + " has left the chat.");
                            clientWritersMap.remove(leavingClientName);
                        }
                        break;
                    } else if (clientMessage.equals("USERS")) {
                        sendUserList();
                    } else if (clientMessage.startsWith("MESSAGE ")) {
                        String message = clientMessage.substring(8);
                        sendToAllClients("MESSAGE " + clientName + ": " + message);
                    } else {
                        System.out.println(clientMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendToAllClients(String message) {
        synchronized (clientWriters) {
            for (PrintWriter clientWriter : clientWriters) {
                clientWriter.println(message);
                clientWriter.flush();
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
}
