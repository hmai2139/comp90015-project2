package assignment2;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.Flow;

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

    //private ShapeDrawer shapeDrawer;
    // GUI frame.
    private final JFrame frame;

    public static void main(String args[]) {
        WhiteboardGUI whiteboardGUI = new WhiteboardGUI();
        whiteboardGUI.getFrame().setContentPane(whiteboardGUI.panelMain);
        whiteboardGUI.getFrame().setVisible(true);
    }

    public WhiteboardGUI() {
        shapeDrawer = new ShapeDrawer();
        frame = new JFrame("User interface");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setMinimumSize(new Dimension(700, 700));
        frame.setLocation(screenSize.width/2, screenSize.height/10);

        this.setTitle("Whiteboard");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(700, 700));
        this.setLocation(screenSize.width/20, screenSize.height/10);
        this.add(shapeDrawer);
        this.setVisible(true);


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
