package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

public class ClientGUI {
    private JPanel panelMain;
    private JTextField usernameField;
    private JButton usernameConfirmButton;
    private JButton createWhiteboardButton;
    private JComboBox whiteboardSelection;
    private final JFrame frame;
    private final Action confirmName;
    private String user;

    // Error message components.
    public static JFrame errorFrame;
    public static JPanel errorPanel;

    // Client.
    private final Client client;

    public static void main(String[] args) {
        //new ClientGUI();
    }

    public ClientGUI(Client client) {
        this.client = client;

        frame = new JFrame("Whiteboard client");
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Create a new whiteboard and set user as its manager.
        createWhiteboardButton.addActionListener(e -> {
            new WhiteboardGUI(user);
        });

        // Username confirmation action.
        confirmName = new Action() {

            @Override
            public void actionPerformed(ActionEvent e) {
                user = usernameField.getText();
                if (user == null || user.equals("")) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username.",
                            "No username provided.", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    for (Component component : panelMain.getComponents()) {
                        component.setEnabled(true);
                    }
                }
            }

            // Following methods must be implemented, but are left empty because they are unnecessary.
            @Override
            public Object getValue(String key) { return null; }

            @Override
            public void putValue(String key, Object value) {}

            @Override
            public void setEnabled(boolean b) { }

            @Override
            public boolean isEnabled() { return false; }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {}

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        };

        // Confirm username when user press Confirm/Enter.
        usernameConfirmButton.addActionListener(confirmName);
        usernameField.addActionListener(confirmName);
    }
    // Display error message if error is encountered during start-up.
    public static void showErrorPanel(String message, String error) {
        errorFrame = new JFrame("Whiteboard client");
        errorFrame.setMinimumSize(new Dimension(450, 340));
        errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errorFrame.pack();
        errorFrame.setLocationRelativeTo(null);
        JOptionPane.showMessageDialog(errorFrame, message, error, JOptionPane.ERROR_MESSAGE);
    }

    public JFrame frame() { return this.frame; }
}
