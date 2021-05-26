/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Client handler thread implementation.
 */

package assignment2;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;

public class ClientHandler extends Thread {

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

            // Send current user data to client.
            objectOutputStream.writeObject(Server.users);

            String messageJSON;
            Message message;

            while (true) {

                // Receive request (a JSON string) from client and convert it to a TextRequest Object.
                messageJSON = dataInputStream.readUTF();
                System.out.println(messageJSON);
                message = parseRequest(messageJSON);

                // Empty request.
                if (message == null) {
                    dataOutputStream.writeUTF(Response.INVALID.name());
                    continue;
                }

                else {
                    // Broadcast new message to other clients.
                    if (message.getOperation().equals(Request.CHAT.name())) {
                        broadcastChat(messageJSON);
                    }

                    // Broadcast new shape to other clients.
                    else if (message.getOperation().equals(Request.SHAPE.name())) {
                        broadcastShape(messageJSON);
                    }

                    // Broadcast new text to other clients.
                    else if (message.getOperation().equals(Request.TEXT.name())) {
                        broadcastText(messageJSON);
                    }

                    // Broadcast a client's leaving to clients.
                    else if (message.getOperation().equals(Request.LEAVE.name())) {
                        broadcastLeave(messageJSON);
                        Server.removeUser(message.getUser());
                        Server.gui.getActiveUserList().setListData(Server.users.toArray());
                    }
                }
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
            broadcastChat(chatMessage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Construct draw shape request JSON string to broadcast to clients.
    public synchronized static String sendShape(String x1, String y1, String x2, String y2, String colour) {
        return String.format("{\"operation\": \"%s\", \"user\": \"%s\"," +
                        "\"shape\": \"%s\", \"x1\": \"%s\", \"y1\": \"%s\"," +
                        " \"x2\": \"%s\", \"y2\": \"%s\", \"colour\": \"%s\"}",
                Request.SHAPE.name(), Server.MANAGER, Server.canvas.getMode(), x1, y1, x2, y2, colour);
    }

    // Construct insert text request JSON string to broadcast to clients.
    public synchronized static String sendText(String text, String x1, String y1, String colour) {
        return String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"text\": \"%s\","
                        + "\"x1\": \"%s\", \"y1\": \"%s\","
                        + "\"colour\": \"%s\"}}",
                Request.TEXT.name(), Server.MANAGER, text, x1, y1, colour);
    }

    // Broadcast a JSON string to clients.
    public synchronized static void broadcast(String message) {
        for (ClientHandler handler: Server.handlers.keySet()) {
            try {
                handler.dataOutputStream().writeUTF(message);
            }
            catch (IOException exception) {
                System.out.println("Unable to broadcast. Client may be offline.");
            }
        }
    }

    // Broadcast a client's joining.
    public synchronized static void broadcastJoin(String user) {
        String message = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\"}",
                Response.NEW_JOINED, Server.MANAGER, user);
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(user)){
                try {
                    handler.dataOutputStream().writeUTF(message);
                }
                catch (IOException exception) {
                    System.out.println("Unable to broadcast. Client may be offline.");
                }
            }
        }
    }

    // Broadcast a client's leaving.
    public synchronized static void broadcastLeave(String leaveMessage) {
        Message message = parseRequest(leaveMessage);
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(message.getUser())){
                try {
                    handler.dataOutputStream().writeUTF(leaveMessage);
                }
                catch (IOException exception) {
                    System.out.println("Unable to broadcast. Client may be offline.");
                }
            }
        }
    }

    // Broadcast chat functionality for manager.
    public synchronized static void broadcastChat(String chatMessage) {
        Message message = parseRequest(chatMessage);

        // Add message to server chat log.
        message.setDateTime((LocalDateTime.now()));
        Server.chatlog.add(message);
        Server.gui.getLogArea().append(Server.gui.localDateTime(message.getDateTime()) +
                message.getUser() + ": " + message.getMessage() + "\n");

        // Broadcast to all other clients.
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(message.getUser())){
                try {
                    handler.dataOutputStream().writeUTF(chatMessage);
                }
                catch (IOException exception) {
                    System.out.println("Unable to broadcast. Client may be offline.");
                }
            }
        }
    }

    // Broadcast shape functionality for manager.
    public synchronized static void broadcastShape(String shapeMessage) {
        Message message = parseRequest(shapeMessage);

        // Construct shape and add it to server canvas.
        Shape shape = Server.canvas.drawShape(Mode.valueOf(message.getShape()),
                Integer.parseInt(message.getX1()), Integer.parseInt(message.getY1()),
                Integer.parseInt(message.getX2()), Integer.parseInt(message.getY2())
        );
        StyledShape styledShape =
                new StyledShape(shape, new Color(Integer.parseInt(message.getColour())));
        Server.canvas.addShape(styledShape);

        // Broadcast to other clients.
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(message.getUser())){
                try {
                    handler.dataOutputStream().writeUTF(shapeMessage);
                }
                catch (Exception e) {
                    System.out.println("Unable to broadcast. Client may be offline.");
                }
            }
        }
    }

    // Broadcast text functionality for manager.
    public synchronized static void broadcastText(String textMessage) {
        Message message = parseRequest(textMessage);

        // Construct text and add it to server canvas.
        int x1 = Integer.parseInt(message.getX1());
        int y1 = Integer.parseInt(message.getY1());
        Color colour = new Color(Integer.parseInt(message.getColour()));
        StyledText styledText = new StyledText(message.getText(), colour, new Point(x1, y1));
        Server.canvas.addText(styledText);

        // Broadcast to other clients.
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(message.getUser())){
                try {
                    handler.dataOutputStream().writeUTF(textMessage);
                }
                catch (IOException e) {
                    System.out.println("Unable to broadcast. Client may be offline.");
                }
            }
        }
    }

    // Broadcast canvas data from file to clients.
    public synchronized static void broadcastFile(Canvas canvas) {

        // Send current canvas data to clients.
        for (ClientHandler handler: Server.handlers.keySet()) {
            try {
                handler.objectOutputStream().writeObject(canvas);
            }
            catch (IOException e) {
                System.out.println("Unable to broadcast. Client may be offline.");
            }
        }
    }

    // Open canvas from file and broadcast to client.
    public synchronized static void openCommand(Canvas canvas) {
        String command = String.format("{\"operation\": \"%s\", \"user\": \"%s\"}",
                Response.CANVAS_FROM_FILE, Server.MANAGER);
        broadcast(command);
        broadcastFile(canvas);
    }

    // Broadcast clear command to client.
    public static void clearCommand() {
        String command = String.format("{\"operation\": \"%s\", \"user\": \"%s\"}",
                Response.CLEAR.name(), Server.MANAGER);
        broadcast(command);
    }

    // Broadcast kick command to client.
    public static void kick(String user) {
        String command = String.format("{\"operation\": \"%s\", \"user\": \"%s\", \"message\": \"%s\"}",
                Response.KICKED.name(), Server.MANAGER, user);
        //Server.users.remove(user);
        Server.removeUser(user);
        Server.gui.getActiveUserList().setListData(Server.users.toArray());
        broadcast(command);
    }

    // Broadcast close command to client.
    public static void closeCommand() {
        String command = String.format("{\"operation\": \"%s\", \"user\": \"%s\"}",
                Response.WHITEBOARD_CLOSED.name(), Server.MANAGER);
        broadcast(command);
    }

    public DataOutputStream dataOutputStream() { return this.dataOutputStream; }

    public ObjectOutputStream objectOutputStream() { return this.objectOutputStream; }
}