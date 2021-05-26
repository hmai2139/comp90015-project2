/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * A wrapper for Java String object to be drawn on Canvas
 * This class includes the String's colour, font and x-y coordinate.
 */

package assignment2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.awt.*;
import java.io.Serializable;

public class StyledText implements Serializable {
    private String text;
    private Color colour;
    private Point point;

    public StyledText(String text, Color colour, Point point) {
        this.text = text;
        this.colour = colour;
        this.point = point;
    }

    public String getText() {
        return this.text;
    }

    public Color getColour() {
        return this.colour;
    }

    public Point getPoint() { return this.point; }
}
