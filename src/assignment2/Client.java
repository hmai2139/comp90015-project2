package assignment2;

// Dependencies.
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.simple.JSONArray;

public class Client {

    // Types of request.
    private static final String LOGIN = "login";
    private static final String CHAT = "chat";
    private static final String EXIT = "exit";

    // Socket, input and output streams.
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Scanner scanner;

    // Client GUI.
    private ClientGUI gui;

    public static void main(String[] args) {
        try {
            // Get server address and port from STDIN.
            final String SERVER = args[0];
            final int PORT = Integer.parseInt(args[1]);

            // Open connection to the dictionary server, at port PORT.
            Socket socket = new Socket(SERVER, PORT);
            System.out.println("Successfully connected to " + SERVER + " at port " + PORT);

            // Obtaining input and output streams.
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Scanner to get input from STDIN.
            Scanner scanner = new Scanner(System.in);

            Client client = new Client(socket, in, out, scanner);

            // Infinite loop to handle communication between client and server's client handler.
            while (true) {
                // Get request from STDIN and send to client handler.
                String request = client.scanner.nextLine();
                out.writeUTF(request);

                // Get reply from client handler.
                String reply = in.readUTF();
                System.out.println(reply);
            }
        }

        // Handle connection error.
        catch (ConnectException e) {
            System.out.println("Cannot connect to specified server.");
            System.out.println("The server might be unavailable, or you might have entered invalid server details.");

            // Display error message in GUI.
            ClientGUI.showErrorPanel(
                    "The server might be unavailable, or you might have entered invalid server details.",
                    "Cannot connect to the specified server"
            );
            // Exit.
            System.exit(-1);
        }

        // Handle socket error.
        catch (SocketException e) {
            System.out.println("Socket error: connection to server has been terminated.");
            System.out.println("Please try relaunching the client.");
        }

        // Handle empty launch argument errors.
        catch (ArrayIndexOutOfBoundsException e) {

            // Display error message in GUI.
            ClientGUI.showErrorPanel(
                    "Please enter a server address and port.",
                    "No server address or/and port provided");
            // Exit.
            System.exit(-1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Class constructor.
    public Client(Socket socket, DataInputStream in, DataOutputStream out, Scanner scanner) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.scanner = scanner;
        this.gui = new ClientGUI(this);
    }

    // Submits login request.
    public String login(String user, DataOutputStream out, DataInputStream in) {
        String requestJSON = String.format("{\"operation\": \"%s\", \"user\": \"%s\" }",
                LOGIN, user);
        try {
            out.writeUTF(requestJSON);
            String reply = in.readUTF();
            return reply;
        }
        catch (Exception e) {
            String reply = e.getMessage();
            return reply;
        }
    }

    // Submits chat request.
    public String chat(String user, String message, DataOutputStream out, DataInputStream in) {
        String requestJSON = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\" }",
                LOGIN, user, message);
        try {
            out.writeUTF(requestJSON);
            String reply = in.readUTF();
            return reply;
        }
        catch (Exception e) {
            String reply = e.getMessage();
            return reply;
        }
    }

    public DataOutputStream outputStream() {
        return out;
    }

    public DataInputStream inputStream() {
        return in;
    }

    // Close client.
    private void exit(Socket socket) throws Exception {
        socket.close();
    }
}