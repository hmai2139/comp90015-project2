/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Client implementation.
 */

package assignment2;

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
    private ArrayList<Message> chatlog;
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

                Client client = new Client(USER, server, dataInputStream, dataOutputStream,
                        objectOutputStream, objectInputStream);

                /*// Infinite loop to handle communication between client and server's client handler.
                while (true) {

                    // Get reply from client handler.
                    String replyJSON = dataInputStream.readUTF();
                    Message reply = ChatHandler.parseRequest(replyJSON);
                    gui.logArea().append(gui.localDateTime(reply.dateTime) + reply.user + ": " + reply.message + "\n");
                    System.out.println(replyJSON);
                }*/
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
                  ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream)
            throws IOException, ClassNotFoundException {

        this.user = user;
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;

        // Obtain canvas data and chat log from server.
        Canvas fromServer = (Canvas) objectInputStream.readObject();
        chatlog = (ArrayList<Message>) objectInputStream.readObject();
        System.out.println(chatlog.size());

        // Extract needed data and create own canvas.
        Canvas canvas = new Canvas(fromServer.manager(), user, fromServer.name());
        canvas.setShapes(fromServer.shapes());
        canvas.setTexts(fromServer.texts());

        // Initialise GUI with Canvas data from server.
        this.gui = new WhiteboardGUI(fromServer.manager(), user, fromServer.name(), this, chatlog);
        this.gui.overwrite(canvas);

        // Display chat log to-date.
        for (Message chat: chatlog) {
            this.gui.logArea().append(gui.localDateTime(chat.dateTime) + chat.user + ": " + chat.message + "\n");
        }

        // Infinite loop to handle communication between client and server's client handler.
        while (true) {
            // Get reply from client handler.
            String replyJSON = dataInputStream.readUTF();
            Message reply = ChatHandler.parseRequest(replyJSON);
            gui.logArea().append(gui.localDateTime() + reply.user + ": " + reply.message + "\n");
            System.out.println(replyJSON);
        }
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
        String chatMessage = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\" }",
                Request.CHAT.name(), user, message);
        try {
            dataOutputStream.writeUTF(chatMessage);
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