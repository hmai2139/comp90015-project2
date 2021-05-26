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

