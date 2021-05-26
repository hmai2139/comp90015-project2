/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Whiteboard Graphical User Interface (GUI) implementation.
 */

package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class WhiteboardGUI {

    // GUI components.
    private JPanel panelMain;
    private JScrollPane logScrollPanel;
    private JComboBox insertMenu;
    private JButton newButton;
    private JPanel fileMenuPanel;
    private JButton colourButton;
    private JButton switchBackgroundButton;
    private JButton leaveButton;
    private JTextField chatField;
    private JLabel userSelection;
    private JPanel drawPanel;
    private JButton openButton;
    private JButton saveButton;
    private JButton saveAsButton;
    private JButton closeButton;
    private JPanel userMenuPanel;
    private JTextArea logArea;
    private JTextField removeUserField;
    private JScrollPane userScrollPanel;
    private JTextArea userActiveArea;
    private JPanel userInteractionPanel;

    private Canvas canvas;

    // Manager, current user and file name of the underlying Canvas object.
    private String manager;
    private String user;
    private String name;

    // Client object, for client-side Whiteboard GUI only.
    private Client client;

    // Chat log.
    private ArrayList<Message> chatlog;

    // GUI's canvas frame.
    private JFrame canvasFrame;

    // GUI's control frame.
    private JFrame controlFrame;

    // Class constructor for Whiteboard GUI used by clients.
    public WhiteboardGUI(String manager, String user, String name,
                         Client client, ArrayList<Message> chatlog) {
        this.manager = manager;
        this.user = user;
        this.name = name;
        this.client = client;
        this.chatlog = chatlog;

        // Create a new canvas.
        canvas = new Canvas(manager, user, name, client);
        canvasFrame = new JFrame(name);

        // Initialise whiteboard.
        initialise(manager, user);

        // Send a chat message.
        chatField.addActionListener(e -> {
            String message = chatField.getText();
            client.chat(user, message);
            chatlog.add(new Message(Request.CHAT.name(), user, message));
            logArea.append(localDateTime() + user + ": " + message + "\n");
            chatField.setText("");
        });

        // Disable manager-exclusive listeners for non-manager users.
        if (!this.user.equals(this.manager)) {
            newButton.setEnabled(false);
            openButton.setEnabled(false);
            saveButton.setEnabled(false);
            saveAsButton.setEnabled(false);
            closeButton.setEnabled(false);
        }
    }

    // Class constructor for Whiteboard GUI used by server.
    public WhiteboardGUI(String manager, String user, String name) {
        this.manager = manager;
        this.user = user;
        this.name = name;

        // Create a new canvas.
        canvas = new Canvas(manager, user, name);
        canvasFrame = new JFrame("Canvas");
        canvasFrame.setTitle(name);

        // Initialise whiteboard.
        initialise(manager, user);
        managerInitialise();

        // Broadcast message from server to clients.
        chatField.addActionListener(e -> {
            String message = chatField.getText();
            ClientHandler.chat(message);
            chatField.setText("");
        });
    }

    private void initialise(String manager, String user) {
        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Customise canvas.
        canvas.setBackground(Color.WHITE);
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setMinimumSize(new Dimension(screenSize.width/2, (int) (screenSize.height*0.8)));
        canvasFrame.setLocation(5, screenSize.height/20);
        canvasFrame.add(canvas);
        colourButton.setBackground(canvas.colour());
        canvasFrame.setVisible(true);

        // Whiteboard GUI's initialisation and customisations.
        controlFrame = new JFrame(user + "'s interface");
        controlFrame.setContentPane(panelMain);
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.setMinimumSize(new Dimension(screenSize.width / 2,(int) (screenSize.height*0.8)));
        controlFrame.setLocation(canvasFrame.getX() + canvasFrame.getWidth(), screenSize.height/20);
        controlFrame.pack();
        controlFrame.setVisible(true);

        // Add welcome message.
        logArea.append(localDateTime() + "Welcome " + user + " to " + manager + "'s whiteboard!");
        logArea.append("\n");

        // Leave the whiteboard.
        leaveButton.addActionListener(e -> System.exit(0));

        // Insert text at the location of user's cursor.
        canvas.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (canvas.mode() != Mode.TEXT) {
                    return;
                }
                // Focus on the clicked text field.
                if (e.getClickCount() == 1) {
                    canvas.requestFocusInWindow();
                }
                // Create a new text field when user double-clicks on canvas.
                if (e.getClickCount() == 2) {
                    TextField textField = new TextField(canvas);
                    textField.setLocation(e.getPoint());
                    canvasFrame.add(textField);
                    textField.requestFocusInWindow();
                }
            }
        });

        // Select an insert option.
        insertMenu.addActionListener(e -> {
            JComboBox insertMenu = (JComboBox) e.getSource();
            Mode mode = Mode.valueOf((String) insertMenu.getSelectedItem());
            canvas.setMode(mode);
        });

        // Select a colour and update current colour indicator.
        colourButton.addActionListener(e -> {
            Color colour = canvas.colour();
            colour = JColorChooser.showDialog(controlFrame, "Select a colour", colour);
            if (colour != null) {
                canvas.setColour(colour);
                colourButton.setBackground(colour);
            }
        });

        // Switch background.
        switchBackgroundButton.addActionListener(e -> canvas.switchGrid());
    }

    // Initialise manager-exclusive listeners.
    private void managerInitialise() {

        // Clear all drawn shapes from canvas.
        newButton.addActionListener(e -> {
            canvas.clear();
            ClientHandler.clearCommand();
        });

        // Open an existing canvas from file.
        openButton.addActionListener(e -> {
            try {
                // Open the file chooser at the current directory.
                JFileChooser fileChooser = new JFileChooser();
                File workingDir = new File(System.getProperty("user.dir"));
                fileChooser.setCurrentDirectory(workingDir);

                // returnValue = 0 indicates that a file has been chosen, = 1 otherwise.
                int returnValue = fileChooser.showOpenDialog(controlFrame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // Open the selected file.
                    FileInputStream fileInputStream = new FileInputStream(file);

                    // Read canvas data from the opened file.
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    Canvas opened = (Canvas) objectInputStream.readObject();

                    canvas.clear();
                    canvas.setShapes(opened.shapes());
                    canvas.setTexts(opened.texts());
                    canvas.setName(opened.name());
                    canvasFrame.setTitle(file.getName());

                    // Broadcast data from file to client.
                    ClientHandler.openCommand(opened);

                    fileInputStream.close();
                    objectInputStream.close();
                }
            }
            catch (FileNotFoundException fileNotFoundException) {
                JOptionPane.showMessageDialog(controlFrame, "No such file exists.",
                        "File not found", JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "File may be corrupted/incompatible.",
                        "Cannot open selected file", JOptionPane.ERROR_MESSAGE);
            }
            catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        });

        // Save the current canvas by writing to a file with the current name.
        saveButton.addActionListener(e -> {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(canvasFrame.getTitle());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(canvas);
                JOptionPane.showMessageDialog(controlFrame, "Saved successfully.",
                        "Save file", JOptionPane.INFORMATION_MESSAGE);

                fileOutputStream.close();
                objectOutputStream.close();
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current canvas.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Save the current canvas as a new file or to a file chosen by manager.
        saveAsButton.addActionListener(e -> {
            try {
                // Open the file chooser at the current directory.
                JFileChooser fileChooser = new JFileChooser();
                File workingDir = new File(System.getProperty("user.dir"));
                fileChooser.setCurrentDirectory(workingDir);

                // returnValue = 0 indicates that user pressed Save, = 1 otherwise.
                int returnValue = fileChooser.showSaveDialog(controlFrame);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(canvas);

                    fileOutputStream.close();
                    objectOutputStream.close();
                }
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current canvas.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close the canvas and GUI.
        closeButton.addActionListener(e -> {
            ClientHandler.closeCommand();
            System.exit(0);
        });
    }

    // Get underlying Canvas object.
    public Canvas canvas() { return this.canvas; }

    // Overwrite canvas details/shapes/texts with received data.
    public void overwrite(Canvas canvas) {
        this.canvas.setShapes(canvas.shapes());
        this.canvas.setTexts(canvas.texts());
        this.canvas.setName(canvas.name());
        canvasFrame.setTitle(canvas.name());
    }

    public void setChatlog(ArrayList<Message> chatlog) { this.chatlog = chatlog; }

    public JFrame getControlFrame() { return this.controlFrame; }

    public JTextArea logArea() { return this.logArea; }

    // Get local date and time.
    public String localDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return " [" + dateTimeFormatter.format(now) + "] ";
    }

    // Get local date and time.
    public String localDateTime(LocalDateTime dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        return " [" + dateTimeFormatter.format(dateTime) + "] ";
    }
}
