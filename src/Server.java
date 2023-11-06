import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static List<String> clientNames = new ArrayList<>();

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Server is running and waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"),
                        true); // Use UTF-8 encoding
                clientWriters.add(writer);

                BufferedReader scanner = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), "UTF-8")); // Use UTF-8 encoding
                String clientName = scanner.readLine(); // Receive client's name
                clientNames.add(clientName);

                sendToAllClients("Server: " + clientName + " has joined the chat.");

                Thread clientHandler = new Thread(new ClientHandler(clientSocket, writer, clientName));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    System.out.println(clientName + ": " + message);
                    broadcastMessage(clientName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastMessage(String message) {
            for (PrintWriter clientWriter : clientWriters) {
                clientWriter.println(message);
                clientWriter.flush();
            }
        }
    }

    private static void sendToAllClients(String message) {
        for (PrintWriter clientWriter : clientWriters) {
            clientWriter.println(message);
            clientWriter.flush();
        }
    }
}
