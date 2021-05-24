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
    private JLabel usernameLabel;
    public JLabel connectionInfoLabel;
    private final JFrame frame;
    private final Action login;
    private String user;

    // Error message components.
    public static JFrame errorFrame;
    public static JPanel errorPanel;

    // Client.
    private final Client client;

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

        // GUI login action.
        login = new Action() {

            @Override
            public void actionPerformed(ActionEvent e) {
                user = usernameField.getText();

                // Check if username is empty or contains only whitespace character(s).
                // This test is performed server-side as well.
                if (user == null || user.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username.",
                            "No username provided.", JOptionPane.ERROR_MESSAGE);
                }

                // Username is provided, attempts to login.
                else {
                    String response = client.login(user, client.outputStream(), client.inputStream());
                        if (response.equals(Response.USERNAME_TAKEN.name())) {
                            JOptionPane.showMessageDialog(frame, "Username already exists.",
                                    "Login error", JOptionPane.ERROR_MESSAGE);
                        }

                        else if (response.equals(Response.LOGIN_SUCCESS.name())) {
                            for (Component component : panelMain.getComponents()) {
                                component.setEnabled(true);
                            }
                            usernameConfirmButton.setEnabled(false);
                            usernameField.setEditable(false);
                            usernameLabel.setText("You are logged in as: ");
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
        usernameConfirmButton.addActionListener(login);
        usernameField.addActionListener(login);
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
