import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your name: ");
            String clientName = consoleReader.readLine();
            writer.println("JOIN " + clientName);

            Thread messageSender = new Thread(() -> {
                try {
                    String message;
                    while (true) {
                        message = consoleReader.readLine();
                        if (message.equalsIgnoreCase("LEAVE")) {
                            writer.println("LEAVE " + clientName); // Send LEAVE command
                            break;
                        } else if (message.equalsIgnoreCase("USERS")) {
                            writer.println("USERS"); // Send USERS command
                        } else if (message.startsWith("PRIVATE ")) {
                            String[] parts = message.split(" ", 3);
                            String targetUser = parts[1];
                            String privateMessage = parts[2];
                            writer.println("PRIVATE " + targetUser + " " + privateMessage);
                        } else if (message.equalsIgnoreCase("HELP")) {
                            writer.println("HELP");
                        } else {
                            writer.println("MESSAGE " + message); // Send MESSAGE command with the client's name
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageSender.start();

            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String serverMessage;
            while ((serverMessage = serverReader.readLine()) != null) {
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
