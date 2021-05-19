package assignment2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WhiteboardGUI extends JFrame {

    private JPanel panelMain;
    private JPanel drawingPanel;
    private JPanel buttonPanel;
    private JButton ovalButton;
    private JButton circleButton;
    private JButton lineButton;
    private JButton rectangleButton;
    private JButton textButton;
    private JButton squareButton;
    private JButton clearButton;

    private ShapeDrawer shapeDrawer;
    // GUI frame.
    private final JFrame frame;

    public static void main(String args[]) {
        WhiteboardGUI whiteboardGUI = new WhiteboardGUI();
        whiteboardGUI.getFrame().setContentPane(whiteboardGUI.panelMain);
        whiteboardGUI.getFrame().setVisible(true);
    }

    public WhiteboardGUI() {
        frame = new JFrame("Whiteboard");
        shapeDrawer = new ShapeDrawer();
        //frame.add(shapeDrawer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);

        this.setSize(500, 500);
        this.add(shapeDrawer, BorderLayout.CENTER);
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        System.out.println(shapeDrawer.getSize());

        lineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.setMode(Mode.LINE);
            }
        });

        circleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.setMode(Mode.CIRCLE);
            }
        });

        ovalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.setMode(Mode.OVAL);
            }
        });

        rectangleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.setMode(Mode.RECTANGLE);
            }
        });

        squareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.setMode(Mode.SQUARE);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shapeDrawer.clear();
            }
        });
    }

    public JFrame getFrame() {
        return this.frame;
    }
}
