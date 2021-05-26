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
                    if (message.getOperation().equals(Request.CHAT.name())) {
                        // Broadcast new message to other clients.
                        broadcastChat(messageJSON);
                    }

                    else if (message.getOperation().equals(Request.SHAPE.name())) {
                        broadcastShape(messageJSON);
                    }

                    else if (message.getOperation().equals(Request.TEXT.name())) {
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
    public static String sendShape(String x1, String y1, String x2, String y2, String colour) {
        return  String.format("{\"operation\": \"%s\", \"user\": \"%s\"," +
                        "\"shape\": \"%s\", \"x1\": \"%s\", \"y1\": \"%s\"," +
                        " \"x2\": \"%s\", \"y2\": \"%s\", \"colour\": \"%s\"}",
                Request.SHAPE.name(), Server.MANAGER, Server.canvas.mode(), x1, y1, x2, y2, colour);
    }

    // Broadcast a JSON string to clients.
    public synchronized  static void broadcast(String message) {
        for (ClientHandler handler: Server.handlers.keySet()) {
            try {
                handler.out().writeUTF(message);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    // Broadcast chat functionality for manager.
    public synchronized static void broadcastChat(String chatMessage) {
        Message message = parseRequest(chatMessage);

        // Add message to server chat log.
        message.setDateTime((LocalDateTime.now()));
        Server.chatlog.add(message);
        Server.gui.logArea().append(Server.gui.localDateTime(message.getDateTime()) +
                message.getUser() + ": " + message.getMessage() + "\n");

        // Broadcast to all other clients.
        for (ClientHandler handler: Server.handlers.keySet()) {
            if (!Server.handlers.get(handler).equals(message.getUser())){
                try {
                    handler.out().writeUTF(chatMessage);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
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
                    handler.out().writeUTF(shapeMessage);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
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
                    handler.out().writeUTF(textMessage);
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    // Broadcast clear command to client.
    public static void clearCommand() {
        String command = String.format("{\"operation\": \"%s\", \"user\": \"%s\"}",
                Response.CLEAR.name(), Server.MANAGER);
        broadcast(command);

    }

    public DataInputStream in() { return this.dataInputStream; }

    public DataOutputStream out() { return this.dataOutputStream; }
}