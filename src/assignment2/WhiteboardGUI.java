package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WhiteboardGUI extends JFrame {

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

    // GUI main frame.
    private final JFrame frame;

    public WhiteboardGUI(String manager, String name) {
        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialise whiteboard.
        whiteboard = new Whiteboard(manager, name);
        this.setTitle(name);

        // Customise drawing area of whiteboard.
        whiteboard.setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(screenSize.width/2, (int) (screenSize.height*0.8)));
        this.setLocation(5, screenSize.height/20);
        this.add(whiteboard);
        colourButton.setBackground(whiteboard.colour());
        this.setVisible(true);

        // Whiteboard GUI's initialisation and customisations.
        frame = new JFrame("User interface");
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(screenSize.width / 2,(int) (screenSize.height*0.8)));
        frame.setLocation(this.getX() + this.getWidth(), screenSize.height/20);
        frame.pack();
        frame.setVisible(true);

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
                    add(textField);
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
            colour = JColorChooser.showDialog(frame, "Select a colour", colour);
            if (colour != null) {
                whiteboard.setColour(colour);
                colourButton.setBackground(colour);
            }
        });

        // Switch background.
        switchBackgroundButton.addActionListener(e -> {
            whiteboard.switchGrid();
        });

        // Add welcome message.
        logArea.append(localDateTime() + "Welcome.");
        logArea.append("\n");

        // -------- Manager-exclusive action listeners -------- //

        // Clear all drawn shapes from whiteboard.
        newButton.addActionListener(e -> {
                whiteboard = new Whiteboard(manager, name);
                add(whiteboard);
                revalidate();
                repaint();
        });

        // Open an existing whiteboard from file.
        openButton.addActionListener(e -> {
            try {
                // Open the file chooser at the current directory.
                JFileChooser fileChooser = new JFileChooser();
                File workingDir = new File(System.getProperty("user.dir"));
                fileChooser.setCurrentDirectory(workingDir);

                // returnValue = 0 indicates that a file has been chosen, = 1 otherwise.
                int returnValue = fileChooser.showOpenDialog(this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // Open the selected file.
                    FileInputStream fileInputStream = new FileInputStream(file);

                    // Read whiteboard data from the opened file.
                    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                    whiteboard = (Whiteboard) objectInputStream.readObject();

                    // Update current canvas with data from opened whiteboard file.
                    add(whiteboard);
                    setTitle(whiteboard.name());
                    revalidate();
                    repaint();

                    fileInputStream.close();
                    objectInputStream.close();
                }
            }
            catch (FileNotFoundException fileNotFoundException) {
                JOptionPane.showMessageDialog(frame, "No such file exists.",
                        "File not found", JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(frame, "File may be corrupted/incompatible.",
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
                JOptionPane.showMessageDialog(frame, "Saved successfully.",
                        "Save file",JOptionPane.INFORMATION_MESSAGE);

                fileOutputStream.close();
                objectOutputStream.close();
            }
            catch (IOException ioException) {
                JOptionPane.showMessageDialog(frame, "Unable to save the current whiteboard.",
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
                int returnValue = fileChooser.showSaveDialog(this);
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
                JOptionPane.showMessageDialog(frame, "Unable to save the current whiteboard.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Close the whiteboard.
        closeButton.addActionListener(e -> {
            System.exit(0);
        });
    }

    // Get Whiteboard GUI's frame.
    public JFrame frame() {
        return this.frame;
    }

    // Get local date and time.
    public String localDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss: ");
        LocalDateTime now = LocalDateTime.now();
        return (dateTimeFormatter.format(now));
    }
}
