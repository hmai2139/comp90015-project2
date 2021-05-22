package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LobbyGUI {
    private JPanel panelMain;
    private JTextField usernameField;
    private JButton usernameConfirmButton;
    private JButton createWhiteboardButton;
    private JComboBox whiteboardSelection;
    private final JFrame frame;

    private String user;

    public static void main(String[] args) {
        new LobbyGUI();
    }

    public LobbyGUI() {
        frame = new JFrame("User lobby");
        frame.setContentPane(panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        usernameConfirmButton.addActionListener(e -> {
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
        });

    }
}
