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
    private ArrayList<String> users;
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

            // Username already exists.
            if (response.equals(Response.USERNAME_TAKEN.name())) {
                Client.showErrorPanel("Please restart client and choose another name.", "Username already exists");
                System.out.println("Username already exists. Please restart client and choose another name.");
                System.out.println("Connection with server has been terminated. Exiting...");
                System.exit(-1);
            }

            // Manager has declined join request.
            else if (response.equals(Response.JOIN_DECLINED.name())) {
                Client.showErrorPanel("The manager has declined your join request :(.", "Request declined.");
                System.out.println("Connection with server has been terminated. Exiting...");
                System.exit(-1);
            }

            // Authorisation is successful.
            else {
                System.out.println("Request to join has been accepted. Loading whiteboard...");

                // Obtaining Object I/O streams to send/receive Objects to/from server.
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(server.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(server.getInputStream());

                new Client(USER, server, dataInputStream, dataOutputStream, objectOutputStream, objectInputStream);
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

        // Obtain canvas data, chat log and user list from server.
        Canvas fromServer = (Canvas) objectInputStream.readObject();
        chatlog = (ArrayList<Message>) objectInputStream.readObject();
        users = (ArrayList<String>) objectInputStream.readObject();

        // Extract needed data and create own canvas.
        Canvas canvas = new Canvas(fromServer.manager(), user, fromServer.name());
        canvas.setShapes(fromServer.shapes());
        canvas.setTexts(fromServer.texts());

        // Initialise GUI with Canvas data from server.
        this.gui = new WhiteboardGUI(fromServer.manager(), user, fromServer.name(), this, chatlog);
        this.gui.overwrite(canvas);

        // Display chat log to-date.
        for (Message chat : chatlog) {
            gui.getLogArea().append(
                    gui.localDateTime(chat.getDateTime()) + chat.getUser() + ": " + chat.getMessage() + "\n");
        }

        // Display user list to-date.
        this.gui.getActiveUserList().setListData(users.toArray());

        // Infinite loop to handle communication between client and server's client handler.
        while (true) {
            // Get reply from client handler.
            String replyJSON = dataInputStream.readUTF();
            Message reply = ClientHandler.parseRequest(replyJSON);
            System.out.println(replyJSON);

            // Valid reply.
            if (reply != null) {

                // Server sent a chat message.
                if (reply.getOperation().equals(Request.CHAT.name())) {
                    gui.getLogArea().append(gui.localDateTime() + reply.getUser() + ": " + reply.getMessage() + "\n");
                }

                // Server sent a new shape.
                else if (reply.getOperation().equals(Request.SHAPE.name())) {

                    // Construct shape and add to canvas.
                    Shape shape = canvas.drawShape(Mode.valueOf(reply.getShape()),
                            Integer.parseInt(reply.getX1()), Integer.parseInt(reply.getY1()),
                            Integer.parseInt(reply.getX2()), Integer.parseInt(reply.getY2())
                            );
                    StyledShape styledShape =
                            new StyledShape(shape, new Color(Integer.parseInt(reply.getColour())));
                    gui.canvas().addShape(styledShape);
                }

                // Server sent a new text.
                else if (reply.getOperation().equals(Request.TEXT.name())) {

                    // Construct text and add to canvas.
                    String text = reply.getText();
                    int x1 = Integer.parseInt(reply.getX1());
                    int y1 = Integer.parseInt(reply.getY1());
                    Color colour = new Color(Integer.parseInt(reply.getColour()));
                    StyledText styledText = new StyledText(text, colour, new Point(x1, y1));
                    gui.canvas().addText(styledText);
                }

                // Server opened a canvas from file.
                else if (reply.getOperation().equals(Response.CANVAS_FROM_FILE.name())) {

                    // Obtain canvas data from server.
                    Canvas opened = (Canvas) objectInputStream.readObject();
                    System.out.println(opened.name());

                    // Extract needed data and overwrite canvas.
                    Canvas newCanvas = new Canvas(opened.manager(), user, opened.name());
                    newCanvas.setShapes(opened.shapes());
                    newCanvas.setTexts(opened.texts());
                    this.gui.canvas().clear();
                    this.gui.overwrite(newCanvas);
                }

                // Server sent a clear canvas command.
                else if (reply.getOperation().equals(Response.CLEAR.name())) {
                    gui.canvas().clear();
                }

                // Server sent a close canvas command.
                else if (reply.getOperation().equals(Response.WHITEBOARD_CLOSED.name())) {
                    showErrorPanel("Manager has closed the whiteboard.", "Whiteboard closed.");
                    System.out.println("Exiting...");
                    System.exit(0);
                }

                // Server sent a kick command.
                else if (reply.getOperation().equals(Response.KICKED.name())) {

                    // You are the chosen one :(.
                    if (reply.getMessage().equalsIgnoreCase(user())) {
                        showErrorPanel("Manager has removed you from whiteboard.", "Removed :(.");
                        System.out.println("Exiting...");
                        System.exit(0);
                    }

                    // Otherwise update current users.
                    else {
                        users.remove(reply.getMessage());
                        gui.getActiveUserList().setListData(users.toArray());
                    }
                }

                // A new user joined.
                else if (reply.getOperation().equals(Response.NEW_JOINED.name())) {
                    users.add(reply.getMessage());
                    System.out.println(reply.getMessage());
                    gui.getActiveUserList().setListData(users.toArray());
                }

                // A user left.
                else if (reply.getOperation().equals(Request.LEAVE.name())) {
                    users.remove(reply.getUser());
                    gui.getActiveUserList().setListData(users.toArray());
                }
            }
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
            return "Connection error. Cannot login.";
        }
    }

    // Submits chat request.
    public void chat(String user, String message) {
        String request = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\"}",
                Request.CHAT.name(), user, message);
        try {
            dataOutputStream.writeUTF(request);
        }
        catch (Exception e) {
            System.out.println("Cannot send chat message.");
        }
    }

    // Submits leave request.
    public void leave(String user) {
        String request = String.format("{\"operation\": \"%s\", \"user\": \"%s\"}",
                Request.LEAVE.name(), user);
        try {
            dataOutputStream.writeUTF(request);
            System.out.println("Leaving...");
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("Cannot send chat message.");
        }
    }

    // Submits shape draw request.
    public void sendShape(String user, String mode, String x1, String y1, String x2, String y2, String colour) {
        String request = String.format("{\"operation\": \"%s\", \"user\": \"%s\"," +
                         "\"shape\": \"%s\", \"x1\": \"%s\", \"y1\": \"%s\"," +
                         " \"x2\": \"%s\", \"y2\": \"%s\", \"colour\": \"%s\"}",
                Request.SHAPE.name(), user, mode, x1, y1, x2, y2, colour);
        try {
            dataOutputStream.writeUTF(request);
        }
        catch (Exception e) {
            System.out.println("Insert shape request failed. Please check connection with server.");
        }
    }

    // Submits text insert request.
    public void text(String user, String text, String x1, String y1, String colour) {
        String request = String.format(
                "{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\"," +
                        "\"x1\": \"%s\", \"y1\": \"%s\"," +
                        "\"colour\": \"%s\"}}",
                Request.TEXT.name(), "", user, x1, y1, colour);
        try {
            dataOutputStream.writeUTF(request);
        }
        catch (Exception e) {
            System.out.println("Insert text request failed. Please check connection with server.");
        }
    }

    public String user() { return this.user; }

    public ArrayList<String> getUsers() { return this.users; }

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