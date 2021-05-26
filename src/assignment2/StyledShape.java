/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * A wrapper for Java Shape object to be drawn on Canvas.
 * This class includes the colour with which the shape was drawn.
 */

package assignment2;

import java.awt.*;
import java.io.Serializable;

public class StyledShape implements Serializable {
    private Shape shape;
    private Color colour;

    public StyledShape(Shape shape, Color colour) {
        this.shape = shape;
        this.colour = colour;
    }

    public Shape shape() {
        return this.shape;
    }

    public Color colour() {
        return this.colour;
    }
}

// JSON-convertible representation of the StyledText class.
class StyledShapeJSON {
    private final String shape;
    private final String colour;
    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;

    public StyledShapeJSON(String shape, String colour, int x1, int y1, int x2, int y2) {
        this.shape = shape;
        this.colour = colour;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public String shape() { return this.shape; }

    public String colour() { return this.colour; }

    public int x1() { return this.x1; }
    public int y1() { return this.x1; }
    public int x2() { return this.x1; }
    public int y2() { return this.x1; }
}
