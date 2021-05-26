/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Server implementation.
 */

package assignment2;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        canvas = gui.whiteboard();

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

                // Notify incoming client that they have chosen the same name as manager.
                if (request != null) {
                    if (request.user().trim().equalsIgnoreCase(MANAGER.trim())) {
                        dataOutputStream.writeUTF(Response.USERNAME_TAKEN.name());
                        client.close();
                    }

                    else {

                        // Notify incoming client that they have chosen the same name as as an existing client.
                        for (Socket socket : clients.keySet()) {
                            if (clients.get(socket).trim().equalsIgnoreCase(request.user().trim())) {
                                dataOutputStream.writeUTF(Response.USERNAME_TAKEN.name());
                                client.close();
                            }
                        }

                        // Notify incoming client of successful login.
                        dataOutputStream.writeUTF(Response.LOGIN_SUCCESS.name());

                        // Create Object I/O streams to send/receive Objects to/from client.
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                        ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());

                        // Create new threads to handle requests.
                        System.out.println("Creating a new thread for the client " + client + "...");
                        ClientHandler clientHandler = new ClientHandler(client, dataInputStream, dataOutputStream,
                                objectOutputStream, objectInputStream);

                        // Add client to list of clients.
                        clients.put(client, request.user());
                        handlers.put(clientHandler, request.user());

                        // Start the threads.
                        clientHandler.start();
                    }
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
}

/*
 ** Thread for handling clients' requests.
 */
class ClientHandler extends Thread {

    final Socket client;
    final DataInputStream dataInputStream;
    final DataOutputStream dataOutputStream;
    final ObjectOutputStream objectOutputStream;
    final ObjectInputStream objectInputStream;

    public ClientHandler(Socket client,
                         DataInputStream dataInputStream, DataOutputStream dataOutputStream,
                         ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.client = client;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
    }

    @Override
    public synchronized void run() {
        try {
            // Send current canvas data to client.
            objectOutputStream.writeObject(Server.canvas);

            // Send current chat log to client.
            objectOutputStream.writeObject(Server.chatlog);

            String chatMessage;
            Message chat;

            while (true) {

                // Receive request (a JSON string) from client and convert it to a TextRequest Object.
                chatMessage = dataInputStream.readUTF();
                System.out.println(chatMessage);
                chat = parseRequest(chatMessage);

                // Empty request.
                if (chat == null) {
                    dataOutputStream.writeUTF(Response.INVALID.name());
                    continue;
                }

                // Broadcast new message to other clients.
                broadcast(chatMessage);
            }
        }
        catch (SocketException e) {
            System.out.println("Socket error: connection with " + client + " has been terminated.");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // Take a client request (a JSON string) and convert it to a Message object.
    public static Message parseRequest(String requestJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(requestJSON, Message.class);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Chat functionality for manager.
    public synchronized static void chat(String message) {
        String chatMessage = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\" }",
                Request.CHAT.name(), Server.MANAGER, message);
        try {
            Message chat = parseRequest(chatMessage);
            if (chat != null) {
                chat.setDateTime(LocalDateTime.now());
            }

            // Broadcast new message to other clients.
            broadcast(chatMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcast chat functionality for manager.
    public synchronized static void broadcast(String chatMessage) {
        Message chat = parseRequest(chatMessage);

        // Add message to server chat log.
        chat.setDateTime((LocalDateTime.now()));
        Server.chatlog.add(chat);
        Server.gui.logArea().append(
                Server.gui.localDateTime(chat.dateTime()) + chat.user() + ": " + chat.message() + "\n");

        // Broadcast to all other clients.
        for (ClientHandler thread: Server.handlers.keySet()) {
            if (!Server.handlers.get(thread).equals(chat.user())){
                try {
                    thread.out().writeUTF(chatMessage);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public DataInputStream in() { return this.dataInputStream; }

    public DataOutputStream out() { return this.dataOutputStream; }
}

/*
 ** Representation of a text message between server and clients.
 */
class Message implements Comparable<Message>, Serializable {
    private String operation;
    private String user;
    private String message;
    //private String
    private LocalDateTime dateTime;

    public Message(String operation, String user, String message) {
        this.operation = operation;
        this.user = user;
        this.message = message;
        dateTime = LocalDateTime.now();
    }

    @Override
    public int compareTo(Message o) {
        return this.dateTime.compareTo(o.dateTime);
    }

    public String operation() { return this.operation; }

    public String user() { return this.user; }

    public LocalDateTime dateTime() { return this.dateTime; }

    public String message() { return this.message; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}