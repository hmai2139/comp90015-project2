package assignment2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class TextField extends JTextField
        implements ActionListener, FocusListener, MouseListener, DocumentListener {
    private Whiteboard whiteboard;
    public TextField(Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(Color.black));
        setSize(getPreferredSize());
        addActionListener(this);
        addFocusListener(this);
        addMouseListener(this);
        getDocument().addDocumentListener(this);
    }

    //  Implement ActionListener
    public void actionPerformed(ActionEvent e) { setEditable(false); }

    //  When user moves away from this field.
    public void focusLost(FocusEvent e) {
        whiteboard.insertText(getText(), getLocation());
        setEditable(false);
        setBorder(null);
        setText("");
    }

    // Create border around text field to indicate it is being edited.
    public void focusGained(FocusEvent e) {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    // Edit text field when double-clicked.
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2)
            setEditable(true);
    }

    public void mouseEntered( MouseEvent e ) {}

    public void mouseExited( MouseEvent e ) { System.out.println("exited");}

    public void mousePressed( MouseEvent e ) {}

    public void mouseReleased( MouseEvent e ) {}

    //  Implement DocumentListener

    public void insertUpdate(DocumentEvent e) {
        updateSize();
    }

    public void removeUpdate(DocumentEvent e) {
        updateSize();
    }

    public void changedUpdate(DocumentEvent e) {}

    private void updateSize() {
        setSize(getPreferredSize());
    }

    /*public static void main(String[] args) {
        JPanel panel = new JPanel();
        panel.setFocusable( true );
        panel.setLayout( null );
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JPanel panel = (JPanel) e.getSource();

                if (e.getClickCount() == 1) {
                    panel.requestFocusInWindow();
                }

                if (e.getClickCount() == 2) {
                    TextField tf = new TextField();
                    tf.setLocation(e.getPoint());
                    panel.add( tf );
                    tf.requestFocusInWindow();
                }
            }
        });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.add(new JLabel("Double Click to Add Text"), BorderLayout.NORTH);
        frame.add(panel);
        frame.setSize(650, 300);
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }*/
}
