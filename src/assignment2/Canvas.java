/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Canvas (Drawing area of Whiteboard GUI) implementation.
 */

package assignment2;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;

public class Canvas extends JComponent
        implements Serializable {

    // Shapes drawn on canvas.
    private ArrayList<StyledShape> shapes = new ArrayList<>();

    // Texts typed on canvas.
    private ArrayList<StyledText> texts = new ArrayList<>();

    // Current position of user's cursor.
    private Point pointStart, pointEnd;

    // Default colour.
    private Color colour = Color.BLACK;

    // Default text font.
    private Font font = new Font("Arial", Font.PLAIN, 14);

    // Default insert mode.
    private Mode mode = Mode.LINE;

    // Default canvas background.
    private Boolean grid = false;

    // Canvas manager.
    private String manager;

    // Canvas current user.
    private String user;

    // Canvas filename, to be used in Save/Save As operations.
    private String name;

    // Client object, for client-side Canvas only.
    private Client client;

    // Canvas constructor for Server.
    public Canvas(String manager, String user, String name) {
        this.manager = manager;
        this.user = user;
        this.name = name;
        initialise();
    }

    // Canvas constructor for Client.
    public Canvas(String manager, String user, String name, Client client) {
        this.manager = manager;
        this.user = user;
        this.name = name;
        this.client = client;
        initialise();
    }

    public void initialise() {
        // Get start location of user's cursor.
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ( SwingUtilities.isLeftMouseButton(e) ) {
                    pointStart = new Point(e.getX(), e.getY());
                    pointEnd = pointStart;
                    repaint();
                }
            }

            // Create a new shape and add it to canvas when mouse is released.
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Shape shape = drawShape();

                    // If shape is valid, add it to canvas.
                    if (shape != null) {
                        StyledShape styledShape = new StyledShape(shape, colour);
                        shapes.add(styledShape);

                        // New shape drawn by client, send it to server.
                        if (client != null) {

                        }

                        // New shape drawn by server, broadcast it.
                        else {
                        }
                    }

                    // Nullify coordinates to prevent any unintended drawing.
                    pointStart = null;
                    pointEnd = null;
                    repaint();
                }
            }
        });

        // Get end location of user's cursor.
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pointEnd = new Point(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }

    // Drawing logic.
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Anti-aliasing to produce cleaner drawing.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Whether to draw grid based on user's instruction.
        if (this.grid) { drawGrid(g2); }

        // Set stroke size.
        g2.setStroke(new BasicStroke(2));

        // Set alpha (transparency) value of shape preview.
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        // Draw all shapes.
        for (StyledShape shape: shapes) {
            g2.setPaint(shape.colour());
            g2.draw(shape.shape());
        }

        // Draw all texts.
        for (StyledText text: texts) {
            g.setColor(text.colour());
            g.drawString(text.text(), text.point().x, text.point().y + g.getFontMetrics().getAscent());
        }

        // Show shape preview before drawing.
        if (pointStart != null && pointEnd != null) {
            g2.setPaint(Color.LIGHT_GRAY);
            Shape shape = drawShape();
            if (shape != null) {
                g2.draw(shape);
            }
        }
    }

    private Shape drawShape() {

        switch (mode) {
            case LINE:
                return drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);

            case CIRCLE:
                return drawCircle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);

            case OVAL:
                return drawOval(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);

            case RECTANGLE:
                return drawRectangle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);

            case SQUARE:
                return drawSquare(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
        }
        return null;
    }

    // Insert text.
    void insertText(String text, Point point) {
        texts.add(new StyledText(text, this.colour, this.font, point));
    }

    // Draw a line.
    public static Line2D.Float drawLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }

    // Draw a circle.
    public static Ellipse2D.Float drawCircle(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(x1 - x2));
    }

    // Draw an oval.
    public static Ellipse2D.Float drawOval(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    // Draw a rectangle.
    public static Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    // Draw a square.
    public static Rectangle2D.Float drawSquare(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(x1 - x2));
    }

    // Draw grid background.
    private void drawGrid(Graphics2D g2) {
        g2.setPaint(Color.LIGHT_GRAY);
        for (int i = 0; i < getSize().width; i += 10) {
            Shape line = new Line2D.Float(i, 0, i, getSize().height);
            g2.draw(line);
        }

        for (int i = 0; i < getSize().height; i += 10) {
            Shape line = new Line2D.Float(0, i, getSize().width, i);
            g2.draw(line);
        }
    }

    // Switch between grid and non-grid background.
    public void switchGrid() {
        this.grid = !this.grid;
        repaint();
    }

    public Mode mode() { return this.mode; }

    public void setMode(Mode mode) { this.mode = mode; }

    public Color colour() { return this.colour; }

    public void setColour(Color colour) { this.colour = colour; }

    public Font font() { return this.font; }

    public void setFont(Font font) { this.font = font; }

    public ArrayList<StyledShape> shapes() {
        return this.shapes;
    }

    public void setShapes(ArrayList<StyledShape> shapes) {
        this.shapes = shapes;
        repaint();
    }

    public void addShape(StyledShape shape) {
        this.shapes.add(shape);
        repaint();
    }

    public ArrayList<StyledText> texts() { return this.texts; }

    public void setTexts(ArrayList<StyledText> texts) {
        this.texts = texts;
        repaint();
    }

    public void addText(StyledText text) {
        this.texts.add(text);
        repaint();
    }

    public String manager() { return this.manager; }

    public void setManager(String name) { this.manager = manager; }

    public String name() { return this.name; }

    public void setName(String name) { this.name = name; }

    // Clear canvas.
    public void clear() {
        this.shapes = new ArrayList<>();
        this.texts = new ArrayList<>();
        repaint();
    }
}

