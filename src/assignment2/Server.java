/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Server implementation.
 */

package assignment2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    // Allowed port range.
    public static final int PORT_MIN = 1024;
    public static final int PORT_MAX = 65535;

    // Currently connected users and their sockets.
    public static ConcurrentHashMap<Socket, String> clients = new ConcurrentHashMap<>();

    // Currently connected users and their threads.
    public static ConcurrentHashMap<ClientHandler, String> handlers = new ConcurrentHashMap<>();

    // Currently connected users by their username.
    public static ArrayList<String> users = new ArrayList<>();

    // Chat log.
    public static ArrayList<Message> chatlog = new ArrayList<>();

    // Whiteboard manager.
    public static String MANAGER;

    // Canvas and Whiteboard GUI.
    public static Canvas canvas;
    public static WhiteboardGUI gui;

    public static void main(String[] args) throws IOException {

        // Get port from STDIN.
        final int PORT = Integer.parseInt(args[1]);

        // Get username from STDIN.
        MANAGER = args[2];
        users.add(MANAGER);

        // Invalid port, exit program.
        if (PORT < PORT_MIN || PORT > PORT_MAX) {
            System.out.printf("Port must be in range %s-%s. %n", PORT_MIN, PORT_MAX );
            System.exit(-1);
        }

        // Open the Server Socket.
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server started, waiting for connection...");


        // Create a new canvas and become its manager.
        gui = new WhiteboardGUI(MANAGER, MANAGER, MANAGER);
        canvas = gui.canvas();

        // Awaiting potential requests from clients.
        while (true) {
            Socket client = null;

            try {
                // Initialise socket to receive incoming requests from client.
                client = server.accept();
                System.out.println("A new client is connected : " + client);

                // Create Data I/O streams to communicate with incoming client.
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());

                // Check if incoming client's chosen username is unique.
                String requestJSON = dataInputStream.readUTF();
                System.out.println(requestJSON);
                Message request = ClientHandler.parseRequest(requestJSON);

                // Notify incoming client that their username is already taken.
                if (request != null) {
                    String username = request.getUser().trim();
                    if (username.equalsIgnoreCase(MANAGER.trim()) || userExists(username)) {
                        dataOutputStream.writeUTF(Response.USERNAME_TAKEN.name());
                        client.close();
                    }

                    // Otherwise ask manager for join permission.
                    else {
                        showJoinDialog(request.getUser(), dataOutputStream);
                    }

                    // Create Object I/O streams to send/receive Objects to/from client.
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());

                    // Create new threads to handle requests.
                    System.out.println("Creating a new thread for the client " + client + "...");
                    ClientHandler clientHandler = new ClientHandler(client, dataInputStream, dataOutputStream,
                            objectOutputStream, objectInputStream);

                    // Add client to list of clients.
                    clients.put(client, request.getUser());
                    handlers.put(clientHandler, request.getUser());
                    users.add(request.getUser());
                    Server.gui.getActiveUserList().setListData(users.toArray());

                    // Notify other users.
                    ClientHandler.broadcastJoin(request.getUser());

                    // Start the threads.
                    clientHandler.start();
                }
            }
            catch (BindException e) {
                System.out.println("Address is already in use.");
            }
            // Socket error as client disconnects.
            catch (SocketException e) {
                System.out.println("Socket error: connection with " + client + " has been terminated.");
            }
            catch (Exception e) {
                if (client != null) {
                    client.close();
                }
                e.printStackTrace();
            }
        }
    }

    // Remove user along with their socket and handler.
    public synchronized static void removeUser(String user) {

        // Remove name.
        users.remove(user);

        // Remove socket.
        clients.entrySet().removeIf(client -> user.equals(clients.get(client)));

        // Remove handler.
        handlers.entrySet().removeIf(handler -> user.equals(handlers.get(handler)));
    }

    // Check if username exists.
    public synchronized static Boolean userExists(String newUser) {
        for (String user : users) {
            if (user.trim().equalsIgnoreCase(newUser.trim())) {
                return true;
            }
        }
        return false;
    }

    // Display user login request message.
    public synchronized static void showJoinDialog(String user, DataOutputStream out) {

        JFrame frame = new JFrame(user + "would like to join your whiteboard.");
        JOptionPane optionPane = new JOptionPane(user + " would like to join your whiteboard.",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION);

        // Show join request dialog.
        final JDialog joinDialog = new JDialog(frame, "Allow user to join?", true);
        joinDialog.setContentPane(optionPane);
        joinDialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);

        // Prevent manager from closing the dialog without pressing either Yes/No.
        joinDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                joinDialog.setTitle("Please accept or decline.");
            }
        });

        optionPane.addPropertyChangeListener(e -> {
            String prop = e.getPropertyName();
            if (joinDialog.isVisible()
                    && (e.getSource() == optionPane)
                    && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                joinDialog.setVisible(false);
            }
        });

        // Set dialog on top and request focus so manager can easily see join request.
        joinDialog.pack();
        joinDialog.setLocationRelativeTo(null);
        joinDialog.toFront();
        joinDialog.setAlwaysOnTop(true);
        joinDialog.requestFocus();
        joinDialog.setVisible(true);

        int value = (Integer) optionPane.getValue();
        try {
            if (value == JOptionPane.YES_OPTION) {
                out.writeUTF(Response.LOGIN_SUCCESS.name());
            }
            else {
                out.writeUTF(Response.JOIN_DECLINED.name());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 ** JSON-convertible representation of a message between server and clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Message implements Comparable<Message>, Serializable {

    // Attributes for chat request.
    private String operation;
    private String user;
    private String message;

    // Attributes for insert text request.
    private String text;
    private String x1;
    private String y1;
    private String colour;

    // Attributes for draw shape request.
    private String shape;
    private String x2;
    private String y2;

    private LocalDateTime dateTime;

    // Constructor for chat request.
    public Message(String operation, String user, String message) {
        this.operation = operation;
        this.user = user;
        this.message = message;
        dateTime = LocalDateTime.now();
    }

    // Constructor for insert text request.
    public Message(String operation, String user, String text, String x1, String y1, String colour) {
        this.operation = operation;
        this.user = user;
        this.text = text;
        this.x1 = x1;
        this.y1 = y1;
        this.colour = colour;
    }

    // Constructor for draw shape request.
    public Message(String operation, String user ,
                   String x1, String y1, String colour,
                   String shape, String x2, String y2) {
        this.operation = operation;
        this.user = user;
        this.x1 = x1;
        this.y1 = y1;
        this.colour = colour;
        this.shape = shape;
        this.x2 = x2;
        this.y2 = y2;
        dateTime = LocalDateTime.now();
    }

    public Message() { }

    @Override
    public int compareTo(Message o) {
        return this.dateTime.compareTo(o.dateTime);
    }

    public String getOperation() { return this.operation; }

    public String getUser() { return this.user; }

    public String getMessage() { return this.message; }

    public String getText() { return this.text; }

    public String getX1() { return this.x1; }

    public String getY1() { return this.y1; }

    public String getColour() { return this.colour; }

    public String getShape() { return this.shape; }

    public String getX2() { return this.x2; }

    public String getY2() { return this.y2; }

    public LocalDateTime getDateTime() { return this.dateTime; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}