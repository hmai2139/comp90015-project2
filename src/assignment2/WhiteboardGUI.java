package assignment2;

import javax.swing.*;
import java.awt.*;

public class WhiteboardGUI extends JFrame {
    private JPanel panelMain;
    private JPanel drawingPanel;
    private JPanel buttonPanel;
    private JButton ovalButton;
    private JButton circleButton;
    private JButton lineButton;
    private JButton rectangleButton;
    private JButton textButton;

    public static void main(String args[]) {
        new WhiteboardGUI();
    }

    public WhiteboardGUI() {
        this.setSize(500, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new ShapeDrawer(), BorderLayout.CENTER);
        this.setVisible(true);
    }
}
