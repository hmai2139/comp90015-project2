package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ServerSocket;
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
    private JPanel userInteractionPanel;

    private Whiteboard whiteboard;

    // Manager, current user and file name of the underlying Whiteboard object.
    private String manager;
    private String user;
    private String name;

    // Server and client sockets.
    private ServerSocket server;
    private Client client;

    // I/O streams.
    //private final DataInputStream dataInputStream;
    //private final DataOutputStream dataOutputStream;

    // Chat log.
    private ArrayList<TextRequest> chatlog;

    // GUI's canvas frame.
    private JFrame canvas;

    // GUI's control frame.
    private final JFrame controlFrame;

    public WhiteboardGUI(String manager, String user, String name,
                         Client client, ArrayList<TextRequest> chatlog/*,
                      DataInputStream dataInputStream, DataOutputStream dataOutputStream*/) {
        this.manager = manager;
        this.user = user;
        this.name = name;
        this.client = client;
        this.chatlog = chatlog;
        //this.dataInputStream = dataInputStream;
        //this.dataOutputStream = dataOutputStream;

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialise whiteboard.
        whiteboard = new Whiteboard(manager, user, name);
        canvas = new JFrame("Canvas");
        canvas.setTitle(name);

        // Customise canvas.
        whiteboard.setBackground(Color.WHITE);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setMinimumSize(new Dimension(screenSize.width/2, (int) (screenSize.height*0.8)));
        canvas.setLocation(5, screenSize.height/20);
        canvas.add(whiteboard);
        colourButton.setBackground(whiteboard.colour());
        canvas.setVisible(true);

        // Whiteboard GUI's initialisation and customisations.
        controlFrame = new JFrame("User interface");
        controlFrame.setContentPane(panelMain);
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.setMinimumSize(new Dimension(screenSize.width / 2,(int) (screenSize.height*0.8)));
        controlFrame.setLocation(canvas.getX() + canvas.getWidth(), screenSize.height/20);
        controlFrame.pack();
        controlFrame.setVisible(true);

        // Add welcome message.
        logArea.append(localDateTime() + "Welcome " + user + " to " + manager + "'s whiteboard!");
        logArea.append("\n");

        // Leave the whiteboard.
        leaveButton.addActionListener(e -> System.exit(0));

        // Insert text at the location of user's cursor.
        whiteboard.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (whiteboard.mode() != Mode.TEXT) {
                    return;
                }
                // Focus on the clicked text field.
                if (e.getClickCount() == 1) {
                    whiteboard.requestFocusInWindow();
                }
                // Create a new text field when user double-clicks on whiteboard.
                if (e.getClickCount() == 2) {
                    TextField textField = new TextField(whiteboard);
                    textField.setLocation(e.getPoint());
                    canvas.add(textField);
                    textField.requestFocusInWindow();
                }
            }
        });

        // Select an insert option.
        insertMenu.addActionListener(e -> {
            JComboBox insertMenu = (JComboBox) e.getSource();
            Mode mode = Mode.valueOf((String) insertMenu.getSelectedItem());
            whiteboard.setMode(mode);
        });

        // Select a colour and update current colour indicator.
        colourButton.addActionListener(e -> {
            Color colour = whiteboard.colour();
            colour = JColorChooser.showDialog(controlFrame, "Select a colour", colour);
            if (colour != null) {
                whiteboard.setColour(colour);
                colourButton.setBackground(colour);
            }
        });

        // Send a chat message.
        chatField.addActionListener(e -> {
            String message = chatField.getText();
            client.chat(user, message);
            chatlog.add(new TextRequest(Request.CHAT.name(), user, message));
            System.out.println(chatlog.size());
            logArea.append(localDateTime() + user + ": " + message + "\n");
        });

        // Switch background.
        switchBackgroundButton.addActionListener(e -> whiteboard.switchGrid());

        // -------- Manager-exclusive action listeners -------- //

        // Disable manager-exclusive listeners for non-manager users.
        if (!this.user.equals(this.manager)) {
            newButton.setEnabled(false);
            openButton.setEnabled(false);
            saveButton.setEnabled(false);
            saveAsButton.setEnabled(false);
            closeButton.setEnabled(false);
        }

        // Clear all drawn shapes from whiteboard.
        newButton.addActionListener(e -> {
            canvas.remove(whiteboard);
            whiteboard = new Whiteboard(manager, user, name);
            canvas.add(whiteboard);
            canvas.revalidate();
            canvas.repaint();
        });

        // Open an existing whiteboard from file.
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

                    // Read whiteboard data from the opened file.
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    Whiteboard opened = (Whiteboard) objectInputStream.readObject();

                    whiteboard.setShapes(opened.shapes());
                    whiteboard.setTexts(opened.texts());
                    whiteboard.setName(opened.name());
                    canvas.setTitle(whiteboard.name());

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

        // Save the current whiteboard by writing to a file with the current name.
        saveButton.addActionListener(e -> {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(whiteboard.name());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(whiteboard);
                JOptionPane.showMessageDialog(controlFrame, "Saved successfully.",
                        "Save file", JOptionPane.INFORMATION_MESSAGE);

                fileOutputStream.close();
                objectOutputStream.close();
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current whiteboard.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Save the current whiteboard as a new file or to a file chosen by manager.
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
                    objectOutputStream.writeObject(whiteboard);

                    fileOutputStream.close();
                    objectOutputStream.close();
                }
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current whiteboard.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close the whiteboard.
        closeButton.addActionListener(e -> System.exit(0));
    }

    public WhiteboardGUI(String manager, String user, String name) {
        this.manager = manager;
        this.user = user;
        this.name = name;
        //this.dataInputStream = dataInputStream;
        //this.dataOutputStream = dataOutputStream;
        //this.objectOutputStream = objectOutputStream;
        //this.objectInputStream = objectInputStream;

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialise whiteboard.
        whiteboard = new Whiteboard(manager, user, name);
        canvas = new JFrame("Canvas");
        canvas.setTitle(name);

        // Customise canvas.
        whiteboard.setBackground(Color.WHITE);
        canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setMinimumSize(new Dimension(screenSize.width/2, (int) (screenSize.height*0.8)));
        canvas.setLocation(5, screenSize.height/20);
        canvas.add(whiteboard);
        colourButton.setBackground(whiteboard.colour());
        canvas.setVisible(true);

        // Whiteboard GUI's initialisation and customisations.
        controlFrame = new JFrame("User interface");
        controlFrame.setContentPane(panelMain);
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.setMinimumSize(new Dimension(screenSize.width / 2,(int) (screenSize.height*0.8)));
        controlFrame.setLocation(canvas.getX() + canvas.getWidth(), screenSize.height/20);
        controlFrame.pack();
        controlFrame.setVisible(true);

        // Add welcome message.
        logArea.append(localDateTime() + "Welcome " + user + " to " + manager + "'s whiteboard!");
        logArea.append("\n");

        // Leave the whiteboard.
        leaveButton.addActionListener(e -> System.exit(0));

        // Insert text at the location of user's cursor.
        whiteboard.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (whiteboard.mode() != Mode.TEXT) {
                    return;
                }
                // Focus on the clicked text field.
                if (e.getClickCount() == 1) {
                    whiteboard.requestFocusInWindow();
                }
                // Create a new text field when user double-clicks on whiteboard.
                if (e.getClickCount() == 2) {
                    TextField textField = new TextField(whiteboard);
                    textField.setLocation(e.getPoint());
                    canvas.add(textField);
                    textField.requestFocusInWindow();
                }
            }
        });

        // Select an insert option.
        insertMenu.addActionListener(e -> {
            JComboBox insertMenu = (JComboBox) e.getSource();
            Mode mode = Mode.valueOf((String) insertMenu.getSelectedItem());
            whiteboard.setMode(mode);
        });

        // Select a colour and update current colour indicator.
        colourButton.addActionListener(e -> {
            Color colour = whiteboard.colour();
            colour = JColorChooser.showDialog(controlFrame, "Select a colour", colour);
            if (colour != null) {
                whiteboard.setColour(colour);
                colourButton.setBackground(colour);
            }
        });

        /*// Send a chat message.
        chatField.addActionListener(e -> {
            String message = chatField.getText();
            client.chat(user, message);
            chatlog.add(new TextRequest(Request.CHAT.name(), user, message));
            System.out.println(chatlog.size());
            logArea.append(localDateTime() + user + ": " + message + "\n");
        });*/

        // Switch background.
        switchBackgroundButton.addActionListener(e -> whiteboard.switchGrid());

        // -------- Manager-exclusive action listeners -------- //

        // Disable manager-exclusive listeners for non-manager users.
        if (!this.user.equals(this.manager)) {
            newButton.setEnabled(false);
            openButton.setEnabled(false);
            saveButton.setEnabled(false);
            saveAsButton.setEnabled(false);
            closeButton.setEnabled(false);
        }

        // Clear all drawn shapes from whiteboard.
        newButton.addActionListener(e -> {
            canvas.remove(whiteboard);
            whiteboard = new Whiteboard(manager, user, name);
            canvas.add(whiteboard);
            canvas.revalidate();
            canvas.repaint();
        });

        // Open an existing whiteboard from file.
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

                    // Read whiteboard data from the opened file.
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    Whiteboard opened = (Whiteboard) objectInputStream.readObject();

                    whiteboard.setShapes(opened.shapes());
                    whiteboard.setTexts(opened.texts());
                    whiteboard.setName(opened.name());
                    canvas.setTitle(whiteboard.name());

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

        // Save the current whiteboard by writing to a file with the current name.
        saveButton.addActionListener(e -> {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(whiteboard.name());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(whiteboard);
                JOptionPane.showMessageDialog(controlFrame, "Saved successfully.",
                        "Save file", JOptionPane.INFORMATION_MESSAGE);

                fileOutputStream.close();
                objectOutputStream.close();
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current whiteboard.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Save the current whiteboard as a new file or to a file chosen by manager.
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
                    objectOutputStream.writeObject(whiteboard);

                    fileOutputStream.close();
                    objectOutputStream.close();
                }
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(controlFrame, "Unable to save the current whiteboard.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close the whiteboard.
        closeButton.addActionListener(e -> System.exit(0));
    }

    // Get underlying Whiteboard object.
    public Whiteboard whiteboard() { return this.whiteboard; }

    // Overwrite whiteboard details/shapes/texts with received data.
    public void overwrite(Whiteboard whiteboard) {
        this.whiteboard.setShapes(whiteboard.shapes());
        this.whiteboard.setTexts(whiteboard.texts());
        this.whiteboard.setName(whiteboard.name());
        canvas.setTitle(whiteboard.name());
    }

    // Set chatlog.
    public void setChatlog(ArrayList<TextRequest> chatlog) { this.chatlog = chatlog; }

    // Get Whiteboard GUI's control frame.
    public JFrame getControlFrame() { return this.controlFrame; }

    // Get local date and time.
    public String localDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss: ");
        LocalDateTime now = LocalDateTime.now();
        return dateTimeFormatter.format(now);
    }

    // Get local date and time.
    public String localDateTime(LocalDateTime dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss: ");
        return dateTimeFormatter.format(dateTime);
    }
}
