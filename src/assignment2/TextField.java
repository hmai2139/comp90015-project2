/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * This class is used to insert text on Canvas.
 */
package assignment2;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class TextField extends JTextField
        implements ActionListener, FocusListener, MouseListener, DocumentListener {

    private Canvas canvas;

    public TextField(Canvas canvas) {
        this.canvas = canvas;
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

    //  When user finishes typing, delete this field from canvas and draw the text.
    public void focusLost(FocusEvent e) {
        canvas.insertText(getText(), getLocation());

        // Server inserted a new text.
        if (canvas.getClient() == null) {
            ClientHandler.broadcast(ClientHandler.sendText(getText(),
                    Integer.toString(getLocation().x),
                    Integer.toString(getLocation().y),
                    Integer.toString(canvas.getColour().getRGB())));
        }

        // Client sent a new text.
        else {
            canvas.getClient().sendText(canvas.getUser(), getText(),
                    Integer.toString(getLocation().x),
                    Integer.toString(getLocation().y),
                    Integer.toString(canvas.getColour().getRGB()));
        }

        setEditable(false);
        setBorder(null);
        setText("");
        canvas.remove(this);
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

}
