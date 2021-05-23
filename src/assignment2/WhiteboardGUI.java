package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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

    public WhiteboardGUI(String manager) {
        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialise whiteboard.
        whiteboard = new Whiteboard();
        setTitle(manager + "'s whiteboard");

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

        // Close the whiteboard.
        closeButton.addActionListener(e -> {
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

        // Clear all drawn shapes from whiteboard.
        newButton.addActionListener(e -> whiteboard.clear());

        // Switch background.
        switchBackgroundButton.addActionListener(e -> {
            whiteboard.switchGrid();
        });

        // Add welcome message.
        logArea.append(localDateTime() + "Welcome to " + manager + "'s whiteboard.");
        logArea.append("\n");
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
