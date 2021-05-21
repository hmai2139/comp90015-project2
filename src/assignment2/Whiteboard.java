package assignment2;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.*;

public class Whiteboard extends JComponent {

    // Shapes drawn on whiteboard.
    private ArrayList<StyledShape> shapes = new ArrayList<>();

    // Texts typed on whiteboard.
    private ArrayList<StyledText> texts = new ArrayList<>();

    // Current position of user's cursor.
    private Point pointStart, pointEnd;

    // Default colour.
    private Color colour = Color.BLACK;

    // Default text font.
    private Font font = new Font("Arial", Font.PLAIN, 14);

    // Default insert mode.
    private Mode mode = Mode.LINE;

    // Default whiteboard background.
    private Boolean grid = false;

    public Whiteboard() {

        // Get start location of user's cursor.
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ( SwingUtilities.isLeftMouseButton(e) ) {
                    pointStart = new Point(e.getX(), e.getY());
                    pointEnd = pointStart;
                    repaint();
                }
            }

            // Create a new shape and add it to whiteboard when mouse is released.
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Shape shape = null;

                    switch (mode) {
                        case LINE:
                            shape = drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                            break;
                        case CIRCLE:
                            shape = drawCircle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                            break;
                        case OVAL:
                            shape = drawOval(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                            break;
                        case RECTANGLE:
                            shape = drawRectangle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                            break;
                        case SQUARE:
                            shape = drawSquare(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                            break;
                    }

                    // If shape is valid, add it to whiteboard.
                    if (shape != null) {
                        shapes.add(new StyledShape(shape, colour));
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
            public void mouseDragged (MouseEvent e) {
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
            Shape shape = null;

            switch (mode) {
                case LINE:
                    shape = drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                    break;
                case CIRCLE:
                    shape = drawCircle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                    break;
                case OVAL:
                    shape = drawOval(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                    break;
                case RECTANGLE:
                    shape = drawRectangle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                    break;
                case SQUARE:
                    shape = drawSquare(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
                   break;
            }

            if (shape != null) {
                g2.draw(shape);
            }
        }
    }

    // Insert text.
    void insertText(String text, Point point) {
        texts.add(new StyledText(text, this.colour, this.font, point));
    }

    // Draw a line.
    private Line2D.Float drawLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }

    // Draw a circle.
    private Ellipse2D.Float drawCircle(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(x1 - x2));
    }

    // Draw an oval.
    private Ellipse2D.Float drawOval(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    // Draw a rectangle.
    private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    // Draw a square.
    private Rectangle2D.Float drawSquare(int x1, int y1, int x2, int y2) {
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

    // Get current insert mode.
    public Mode mode() { return this.mode; }

    // Set insert mode.
    public void setMode(Mode mode) { this.mode = mode; }

    // Get current colour.
    public Color colour() { return this.colour; }

    // Set colour.
    public void setColour(Color colour) { this.colour = colour; }

    // Get current font.
    public Font font() { return this.font; }

    // Set font.
    public void setFont(Font font) { this.font = font; }

    // Get all drawn shapes.
    public ArrayList<StyledShape> getShapes() {
        return this.shapes;
    }

    // Clear whiteboard.
    public void clear() {
        this.shapes = new ArrayList<>();
        this.texts = new ArrayList<>();
        repaint();
    }
}

