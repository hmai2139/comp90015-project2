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
    public static ConcurrentHashMap<RequestHandler, String> requestThreads = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<ChatHandler, String> chatThreads = new ConcurrentHashMap<>();

    // Chat log.
    public static ArrayList<Message> chatlog = new ArrayList<>();

    // Whiteboard manager.
    public static String MANAGER;

    // Whiteboard and whiteboard GUI.
    public static Whiteboard whiteboard;
    public static WhiteboardGUI gui;

    public static Server server;

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

        // Create a new whiteboard and become its manager.
        gui = new WhiteboardGUI(MANAGER, MANAGER, MANAGER);
        whiteboard = gui.whiteboard();

        // Awaiting potential requests from clients.
        while (true) {
            Socket client = null;

            try {
                // Initialise socket to receive incoming requests from client.
                client = server.accept();
                System.out.println("A new client is connected : " + client);

                // Create Data I/O streams to communicate with client.
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());

                // Check if client's chosen username is unique.
                String requestJSON = dataInputStream.readUTF();
                System.out.println(requestJSON);
                Message request = RequestHandler.parseRequest(requestJSON);

                // Same name as manager.
                if (request.user.trim().equalsIgnoreCase(MANAGER.trim())) {
                    dataOutputStream.writeUTF(Response.USERNAME_TAKEN.name());
                    client.close();
                }

                else {
                    // Same name as an existing client.
                    for (Socket socket : clients.keySet()) {
                        if (clients.get(socket).trim().equalsIgnoreCase(request.user.trim())) {
                            dataOutputStream.writeUTF(Response.USERNAME_TAKEN.name());
                            client.close();
                        }
                    }
                    dataOutputStream.writeUTF(Response.LOGIN_SUCCESS.name());

                    // Create Object I/O streams to send/receive Objects to/from client.
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());

                    // Create new threads to handle requests.
                    System.out.println("Creating new thread for the client " + client + "...");
                    RequestHandler requestHandler = new RequestHandler(client, objectOutputStream, objectInputStream);
                    ChatHandler chatHandler = new ChatHandler(client, dataInputStream, dataOutputStream);

                    // Add client to list of clients.
                    clients.put(client, request.user);
                    requestThreads.put(requestHandler, request.user);
                    chatThreads.put(chatHandler, request.user);

                    // Start the threads.
                    requestHandler.start();
                    chatHandler.start();
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
                client.close();
                e.printStackTrace();
            }
        }
    }
}

/*
 ** Thread for handling clients' requests.
 */
class RequestHandler extends Thread {

    // Socket, input streams, output streams.
    private final Socket client;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    // Class constructor.
    public RequestHandler(Socket client, ObjectOutputStream out, ObjectInputStream in) {
        this.client = client;
        this.out = out;
        this.in = in;
    }

    // Take a client request (a JSON string) and convert it to a TextRequest object.
    public static Message parseRequest(String requestJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Message request = mapper.readValue(requestJSON, Message.class);
            return request;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Overrides default run method.
    @Override
    public synchronized void run() {
        try {
            // Send current whiteboard data to client.
            out.writeObject(Server.whiteboard);

            // Send current chat log to client.
            out.writeObject(Server.chatlog);

            while (true) {
                /*
                // Receive request (a JSON string) from client and convert it to a TextRequest Object.
                requestJSON = dataInputStream.readUTF();
                System.out.println(requestJSON);
                request = parseRequest(requestJSON);

                // Empty request.
                if (request == null) {
                    dataOutputStream.writeUTF(Response.INVALID.name());
                    continue;
                }
                Request operation = Request.valueOf(request.operation.toUpperCase());

                // Handle chat request.
                if (operation == Request.CHAT) {

                    // Empty message.
                    if (request.message == null || request.message.trim().equals("")) {
                        dataOutputStream.writeUTF(Response.INVALID.name());
                    }

                    // Add message to chat log and notify all other clients.
                    else {
                        request.dateTime = LocalDateTime.now();
                        Server.chatlog.add(request);
                        System.out.println(Server.chatlog.size());
                        Collections.sort(Server.chatlog);
                        for (RequestHandler thread : Server.threads.keySet()) {
                            thread.objectOutputStream().writeObject(Server.chatlog);
                        }
                    }
                }*/
            }
        }
        // Socket error, close thread.
        catch (SocketException e) {
            System.out.println("Socket error: connection with " + client + " has been terminated.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            System.out.println(Response.INVALID);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ObjectOutputStream objectOutputStream() { return this.out; }

    public ObjectInputStream getIn() { return this.in; }
}
/*
 ** Thread for handling chat functionality.
 */
class ChatHandler extends Thread {

    // Socket, input streams, output streams.
    final Socket client;
    final DataInputStream in;
    final DataOutputStream out;

    // Class constructor.
    public ChatHandler(Socket client, DataInputStream in, DataOutputStream out) {
        this.client = client;
        this.in = in;
        this.out = out;
    }

    // Overrides default run method.
    @Override
    public synchronized void run() {
        try {
            String chatMessage;
            Message chat;

            while (true) {

                // Receive request (a JSON string) from client and convert it to a TextRequest Object.
                chatMessage = in.readUTF();
                System.out.println(chatMessage);
                chat = parseRequest(chatMessage);

                // Empty request.
                if (chat == null) {
                    out.writeUTF(Response.INVALID.name());
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

    // Take a client request (a JSON string) and convert it to a TextRequest object.
    public static Message parseRequest(String requestJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Message request = mapper.readValue(requestJSON, Message.class);
            return request;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void chat(String message) {
        String chatMessage = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\" }",
                Request.CHAT.name(), Server.MANAGER, message);
        try {
            Message chat = parseRequest(chatMessage);
            chat.dateTime = LocalDateTime.now();

            // Broadcast new message to other clients.
            broadcast(chatMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String chatMessage) {
        Message chat = parseRequest(chatMessage);

        // Add message to server chat log.
        chat.dateTime = LocalDateTime.now();
        Server.chatlog.add(chat);
        Server.gui.logArea().append(
                Server.gui.localDateTime(chat.dateTime) + chat.user + ": " + chat.message + "\n");

        // Broadcast to all other clients.
        for (ChatHandler thread: Server.chatThreads.keySet()) {
            if (!Server.chatThreads.get(thread).equals(chat.user)){
                try {
                    thread.out().writeUTF(chatMessage);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public DataInputStream in() { return in; }

    public DataOutputStream out() { return out; }
}

/*
 ** Representation of a given client text-based request.
 */
class Message
        implements Comparable<Message>, Serializable {
    public String operation;
    public String user;
    public String message;
    public LocalDateTime dateTime;

    // Class constructor.
    public Message(String operation, String user, String message) {
        this.operation = operation;
        this.user = user;
        this.message = message;
        dateTime = LocalDateTime.now();
    }

    // Default constructor.
    public Message() {
    }

    @Override
    public int compareTo(Message o) {
        return this.dateTime.compareTo(o.dateTime);
    }
}