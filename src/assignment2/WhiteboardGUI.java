package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WhiteboardGUI {

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
    private String manager;
    private String name;

    // GUI's canvas frame.
    private JFrame canvas;

    // GUI's control frame.
    private final JFrame controlFrame;

    public WhiteboardGUI(String manager, String name) {
        this.manager = manager;
        this.name = name;

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialise whiteboard.
        whiteboard = new Whiteboard(manager, name);
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
        logArea.append(localDateTime() + "Welcome.");
        logArea.append("\n");

        // Leave the whiteboard.
        leaveButton.addActionListener(e -> {
            System.exit(0);
        });

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

        // Switch background.
        switchBackgroundButton.addActionListener(e -> {
            whiteboard.switchGrid();
        });

        // -------- Manager-exclusive action listeners -------- //

        // Clear all drawn shapes from whiteboard.
        newButton.addActionListener(e -> {
            canvas.remove(whiteboard);
            whiteboard = new Whiteboard(manager, name);
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
        closeButton.addActionListener(e -> {
            System.exit(0);
        });
    }

    // Get Whiteboard GUI's frame.
    public JFrame getControlFrame() {
        return this.controlFrame;
    }

    // Get local date and time.
    public String localDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss: ");
        LocalDateTime now = LocalDateTime.now();
        return (dateTimeFormatter.format(now));
    }
}
