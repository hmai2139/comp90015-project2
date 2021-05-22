package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WhiteboardGUI extends JFrame {

    private JPanel panelMain;
    private JPanel insertPanel;
    private JScrollPane logScrollPanel;
    private JTextPane logPanel;
    private JPanel controlPanel;
    private JComboBox insertMenu;
    private JButton clearShapeButton;
    private JPanel stylingPanel;
    private JButton colourButton;
    private JButton switchBackgroundButton;
    private JButton createWhiteboardButton;
    private JComboBox whiteboardSelection;
    private JButton leaveButton;
    private JTextField textField1;
    private JLabel userSelection;
    private Whiteboard whiteboard;

    // GUI main frame.
    private final JFrame frame;

    public static void main(String[] args) {
        WhiteboardGUI whiteboardGUI = new WhiteboardGUI();
        whiteboardGUI.frame().setContentPane(whiteboardGUI.panelMain);
        whiteboardGUI.frame().setVisible(true);
    }

    public WhiteboardGUI() {

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // User interface's initialisation and customisations.
        frame = new JFrame("User interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(screenSize.width / 2, screenSize.width / 2));
        frame.setLocationRelativeTo(null);

        // Allows user to create a new whiteboard and become its manage by default.
        createWhiteboardButton.addActionListener(e -> {
            // Initialise whiteboard.
            whiteboard = new Whiteboard();
            setTitle("Whiteboard");

            // Customise whiteboard.
            whiteboard.setBackground(Color.WHITE);
            //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.setMinimumSize(new Dimension(screenSize.width / 2, screenSize.width / 2));
            this.setLocationRelativeTo(null);
            this.add(whiteboard);
            this.setVisible(true);
            colourButton.setBackground(whiteboard.colour());

            // Disable Create button.
            createWhiteboardButton.setEnabled(false);

            // Enable Leave button.
            leaveButton.setEnabled(true);

            this.addListeners();

            // Revalidate the frame.
            frame.revalidate();
        });
    }

    // Add necessary listeners.
    public void addListeners() {
        // Enable necessary listeners for whiteboards.
        if (whiteboard != null) {
            // Behaviours when user clicks on whiteboard.
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
            clearShapeButton.addActionListener(e -> whiteboard.clear());

            // Switch background.
            switchBackgroundButton.addActionListener(e -> {
                whiteboard.switchGrid();
            });
        }
        leaveButton.addActionListener(e -> {
            remove(whiteboard);
            createWhiteboardButton.setEnabled(true);
            leaveButton.setEnabled(false);
            repaint();
            dispose();
        });
    }

    // Get Whiteboard GUI's frame.
    public JFrame frame() {
        return this.frame;
    }
}
