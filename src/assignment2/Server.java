package assignment2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {

    // Allowed port range.
    public static final int PORT_MIN = 1024;
    public static final int PORT_MAX = 65535;

    // Currently connected users and their whiteboards.
    public static Map<String, Whiteboard> users = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Get port from STDIN.
        final int PORT = Integer.parseInt(args[1]);

        // Invalid port, exit program.
        if (PORT < PORT_MIN || PORT > PORT_MAX) {
            System.out.printf("Port must be in range %s-%s. %n", PORT_MIN, PORT_MAX );
            System.exit(-1);
        }

        users.put("hoang", new Whiteboard());

        // Open the Server Socket.
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server started, waiting for connection...");

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

                // Create Object I/O streams to send/receive Objects to/from client.
                //ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                //ObjectInputStream objectInputStream =  new ObjectInputStream(client.getInputStream());
                System.out.println("Creating new thread for the client " + client + "...");

                // Create a new TextRequestHandler thread to handle text-based request.
                Thread textThread = new TextRequestHandler(client, dataInputStream, dataOutputStream);

                // Start the threads.
                textThread.start();
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
 ** Thread for handling a given client text-based request.
 */
class TextRequestHandler extends Thread {

    // Types of request.
    public final String LOGIN = "login";
    public final String CHAT = "chat";
    public final String EXIT = "exit";

    // Type of response to failed requests.
    public final String USERNAME_TAKEN = "Username already exists.";
    public final String INVALID = "Invalid request.";

    // Type of response to successful requests.
    public final String LOGIN_SUCCESS = "Successfully logged in.";

    // Socket, input stream, output stream.
    final Socket socket;
    final DataInputStream in;
    final DataOutputStream out;

    // Class constructor.
    public TextRequestHandler(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    // Take a client request (a JSON string) and convert it to a TextRequest object.
    private TextRequest parseRequest(String requestJSON) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TextRequest request = mapper.readValue(requestJSON, TextRequest.class);
            return request;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Overrides default run method.
    @Override
    public void run() {
        String requestJSON;
        TextRequest request;

        while(true) {
            String reply;
            try {
                // Receive request (a JSON string) from client and convert it to a TextRequest Object.
                requestJSON = in.readUTF();
                System.out.println(requestJSON);
                request = parseRequest(requestJSON);

                // Empty request.
                if (request == null) {
                    //out.writeUTF(INVALID);
                    continue;
                }

                switch (request.operation.toLowerCase()) {
                    case LOGIN:
                        // No username provided or username contains only whitespace character(s).
                        if (request.user == null || request.user.trim().isEmpty()) {
                            out.writeUTF(INVALID);
                        }
                        else {
                            reply = login(request.user);
                            System.out.println("reply is " + reply);
                            out.writeUTF(reply);
                        }
                        break;
                }
            }
            // Socket error, close thread.
            catch (SocketException e) {
                System.out.println("Socket error: connection with " + socket + " has been terminated.");
                return;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                System.out.println(INVALID);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    // Handles login request.
    public synchronized String login(String user) {

        // Create user and initialise their whiteboard as null.
        if (!Server.users.containsKey(user.toLowerCase())) {
            Server.users.put(user, null);
            return (LOGIN_SUCCESS);

        }

        // Chosen username already exists.
        System.out.println("taken");
        return (USERNAME_TAKEN);
    }
}

/*
 ** Representation of a given client text-based request.
 */
class TextRequest {
    public String operation;
    public String user;
    public String message;

    // Class constructor.
    public TextRequest(String operation, String user, String message) {
        this.operation = operation;
        this.user = user;
        this.message = message;
    }

    // Default constructor.
    public TextRequest() {
    }
}
