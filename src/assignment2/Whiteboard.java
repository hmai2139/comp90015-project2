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
    private ArrayList<StyledShape> shapes = new ArrayList<>();
    private Point pointStart, pointEnd;
    private Color colour = Color.BLACK;
    private Mode mode = Mode.LINE;
    private Boolean grid = true;

    public Whiteboard() {

        //setPreferredSize(new Dimension(500, 500));

        this.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if ( SwingUtilities.isLeftMouseButton(e) ) {
                    pointStart = new Point(e.getX(), e.getY());
                    pointEnd = pointStart;
                    repaint();
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Shape shape = null;

                    if (mode == Mode.LINE) {
                        shape = drawLine(pointStart.x, pointStart.y, e.getX(), e.getY());
                    } else if (mode == Mode.CIRCLE) {
                        shape = drawCircle(pointStart.x, pointStart.y, e.getX(), e.getY());
                    } else if (mode == Mode.OVAL) {
                        shape = drawOval(pointStart.x, pointStart.y, e.getX(), e.getY());
                    } else if (mode == Mode.RECTANGLE) {
                        shape = drawRectangle(pointStart.x, pointStart.y, e.getX(), e.getY());
                    } else if (mode == Mode.SQUARE) {
                        shape = drawSquare(pointStart.x, pointStart.y, e.getX(), e.getY());
                    }

                    shapes.add(new StyledShape(shape, colour));
                    pointStart = null;
                    pointEnd = null;
                    repaint();
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged (MouseEvent e) {
                if ( SwingUtilities.isLeftMouseButton(e) ) {
                    pointEnd = new Point(e.getX(), e.getY());
                    repaint();
                }
            }
        });
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (this.grid) { drawGrid(g2); }

        g2.setStroke(new BasicStroke(2));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        for (StyledShape shape : shapes) {
            g2.setPaint(shape.colour());
            g2.draw(shape.shape());
            //g2.setPaint(this.colour);
            //g2.fill(shape);
        }

        if (pointStart != null && pointEnd != null) {

            g2.setPaint(Color.LIGHT_GRAY);
            Shape shape = null;
            if (mode == Mode.LINE) {
                shape = drawLine(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            }

            else if (mode == Mode.CIRCLE) {
                shape = drawCircle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            }

            else if (mode == Mode.OVAL) {
                shape = drawOval(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            }

            else if (mode == Mode.RECTANGLE) {
                shape = drawRectangle(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            }

            else if (mode == Mode.SQUARE) {
                shape = drawSquare(pointStart.x, pointStart.y, pointEnd.x, pointEnd.y);
            }

            g2.draw(shape);
        }
    }

    // Draw a line.
    private Line2D.Float drawLine (int x1, int y1, int x2, int y2) {
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

    // Draw grid on whiteboard.
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
    }

    // Get current insert mode.
    public Mode mode() { return this.mode; }

    // Set insert mode.
    public void setMode(Mode mode) { this.mode = mode; }

    // Get current colour.
    public Color colour() { return this.colour; }

    // Set shape colour.
    public void setColour(Color colour) {
        this.colour = colour;
    }

    // Get all drawn shapes.
    public ArrayList<StyledShape> getShapes() {
        return this.shapes;
    }

    // Clear all shapes.
    public void clear() {
        this.shapes = new ArrayList<>();
        repaint();
    }
}

