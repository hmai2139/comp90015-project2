package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private JButton colourIndicator;
    private JButton switchBackgroundButton;
    private final Whiteboard whiteboard;

    // GUI frame.
    private final JFrame frame;

    public static void main(String[] args) {
        WhiteboardGUI whiteboardGUI = new WhiteboardGUI();
        whiteboardGUI.frame().setContentPane(whiteboardGUI.panelMain);
        whiteboardGUI.frame().setVisible(true);
    }

    public WhiteboardGUI() {

        // Get screen size.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        whiteboard = new Whiteboard();
        whiteboard.setBackground(Color.WHITE);

        // Drawing area's initialisation and customisations.
        this.setTitle("Whiteboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(screenSize.width/2, screenSize.width/2));
        this.setLocation(screenSize.width/20, screenSize.height/20);
        this.add(whiteboard);
        this.setVisible(true);

        // User interface's initialisation and customisations.
        frame = new JFrame("User interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(screenSize.width/3, screenSize.width/2));
        frame.setLocation(this.getWidth() + this.getX(), screenSize.height/20);
        colourIndicator.setBackground(whiteboard.colour());

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
            Color colour = Color.BLACK;
            colour = JColorChooser.showDialog(frame, "Select a colour", colour);
            whiteboard.setColour(colour);
            colourIndicator.setBackground(colour);
        });

        // Clear all drawn shapes from whiteboard.
        clearShapeButton.addActionListener( e -> whiteboard.clear() );

        // Switch background.
        switchBackgroundButton.addActionListener( e -> {
            whiteboard.switchGrid();
        });

    }

    // Get Whiteboard GUI's frame.
    public JFrame frame() {
        return this.frame;
    }
}
