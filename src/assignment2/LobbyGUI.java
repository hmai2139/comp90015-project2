package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;

public class LobbyGUI {
    private JPanel panelMain;
    private JTextField usernameField;
    private JButton usernameConfirmButton;
    private JButton createWhiteboardButton;
    private JComboBox whiteboardSelection;
    private final JFrame frame;
    private final Action confirmName;
    private String user;

    public static void main(String[] args) {
        new LobbyGUI();
    }

    public LobbyGUI() {
        frame = new JFrame("Lobby");
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

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
}
