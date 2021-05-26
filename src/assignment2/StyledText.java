/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * A wrapper for Java String object to be drawn on Canvas
 * This class includes the String's colour, font and x-y coordinate.
 */

package assignment2;

import java.awt.*;
import java.io.Serializable;

public class StyledText implements Serializable {
    private String text;
    private Color colour;
    private Font font;
    private Point point;

    public StyledText(String text, Color colour, Font font, Point point) {
        this.text = text;
        this.colour = colour;
        this.font = font;
        this.point = point;
    }

    public String text() {
        return this.text;
    }

    public Color colour() {
        return this.colour;
    }

    public Font font() { return this.font; }

    public Point point() { return this.point; }
}

// JSON-convertible representation of the StyledText class.
class StyledTextJSON {
    private final String text;
    private final String colour;
    private final String font;
    private final int x1;
    private final int y1;

    public StyledTextJSON(String text, String colour, String font, int x1, int y1) {
        this.text = text;
        this.colour = colour;
        this.font = font;
        this.x1 = x1;
        this.y1 = y1;
    }

    public String shape() { return this.text; }

    public String colour() { return this.colour; }

    public String font() { return this.font(); }

    public int x1() { return this.x1; }

    public int y1() { return this.x1; }

}