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
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // Use
                                                                                                                   // UTF-8
                                                                                                                   // encoding
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter your name: ");
            String clientName = consoleReader.readLine();
            writer.println(clientName);

            Thread messageSender = new Thread(() -> {
                try {
                    String message;
                    while (true) {
                        message = consoleReader.readLine();
                        writer.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageSender.start();

            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); // Use
                                                                                                                       // UTF-8
                                                                                                                       // encoding
            String serverMessage;
            while ((serverMessage = serverReader.readLine()) != null) {
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
