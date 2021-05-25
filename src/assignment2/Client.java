package assignment2;

// Dependencies.
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    private final String user;
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private final Scanner scanner;
    private ArrayList<TextRequest> chatlog;
    private WhiteboardGUI gui;

    public static void main(String[] args) {
        try {
            // Get server address, port and username from STDIN.
            final String SERVER = args[0];
            final int PORT = Integer.parseInt(args[1]);
            final String USER = args[2];

            // Open connection to the dictionary server, at port PORT.
            Socket server = new Socket(SERVER, PORT);
            System.out.println("Successfully connected to " + SERVER + " at port " + PORT);

            // Obtaining Data I/O streams to communicate with server.
            DataInputStream dataInputStream = new DataInputStream(server.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(server.getOutputStream());

            // Submit username to server for authorisation.
            String response = Client.login(USER, dataInputStream, dataOutputStream);
            if (response.equals(Response.USERNAME_TAKEN.name())) {
                Client.showErrorPanel("Please restart client and choose another name.", "Username already exists");
                System.out.println("Username already exists. Please restart client and choose another name.");
                System.out.println("Connection with server has been terminated. Exiting...");
                System.exit(-1);
            }

            // Authorisation is successful.
            else {
                // Obtaining Object I/O streams to send/receive Objects to/from server.
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

                // Scanner to get input from STDIN.
                Scanner scanner = new Scanner(System.in);

                Client client = new Client(USER, server, dataInputStream, dataOutputStream,
                        objectOutputStream, objectInputStream, scanner);

                // Infinite loop to handle communication between client and server's client handler.
                while (true) {

                    // Get request from STDIN and send to client handler.
                    String request = client.scanner.nextLine();
                    dataOutputStream.writeUTF(request);

                    // Get reply from client handler.
                    String reply = dataInputStream.readUTF();
                    System.out.println(reply);
                }
            }
        }

        // Handle connection error.
        catch (ConnectException e) {
            Client.showErrorPanel("The server might be unavailable, or you might have entered invalid server details.",
                    "Cannot connect to specified server.");
            System.out.println("Cannot connect to specified server.");
            System.out.println("The server might be unavailable, or you might have entered invalid server details.");

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
            System.out.println("Please enter a server address and port.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Class constructor.
    public Client(String user, Socket socket,
                  DataInputStream dataInputStream, DataOutputStream dataOutputStream,
                  ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream,
                  Scanner scanner)
            throws IOException, ClassNotFoundException {

        this.user = user;
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.scanner = scanner;

        // Obtain whiteboard data and chat log from server.
        Whiteboard fromServer = (Whiteboard) objectInputStream.readObject();
        chatlog = (ArrayList<TextRequest>) objectInputStream.readObject();

        // Extract needed data and create own whiteboard.
        Whiteboard whiteboard = new Whiteboard(fromServer.manager(), user, fromServer.name());
        whiteboard.setShapes(fromServer.shapes());
        whiteboard.setTexts(fromServer.texts());

        // Initialise GUI with Whiteboard data from server.
        this.gui = new WhiteboardGUI(fromServer.manager(), user, fromServer.name(), this, chatlog);
        this.gui.overwrite(whiteboard);
    }

    // Submits login request.
    public static String login(String user, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        String requestJSON = String.format("{\"operation\": \"%s\", \"user\": \"%s\" }",
                Request.LOGIN.name(), user);
        try {
            dataOutputStream.writeUTF(requestJSON);
            return dataInputStream.readUTF();
        }
        catch (Exception e) {
            return e.getMessage();
        }
    }

    // Submits chat request.
    public void chat(String user, String message) {
        String requestJSON = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\" }",
                Request.CHAT.name(), user, message);
        try {
            dataOutputStream.writeUTF(requestJSON);
            //return (ArrayList<TextRequest>) objectInputStream.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String user() { return this.user; }

    public DataOutputStream outputStream() { return this.dataOutputStream; }

    public DataInputStream inputStream() { return this.dataInputStream; }

    public ObjectOutputStream objectOutputStream() { return this.objectOutputStream; }

    public ObjectInputStream getObjectInputStream() { return this.objectInputStream; }

    // Display error message if error is encountered during start-up.
    public static void showErrorPanel(String message, String error) {
        JFrame errorFrame = new JFrame("Error");
        errorFrame.setMinimumSize(new Dimension(450, 340));
        errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errorFrame.pack();
        errorFrame.setLocationRelativeTo(null);
        JOptionPane.showMessageDialog(errorFrame, message, error, JOptionPane.ERROR_MESSAGE);
    }
}