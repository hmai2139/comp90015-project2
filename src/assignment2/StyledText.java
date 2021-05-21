package assignment2;

import java.awt.*;

public class StyledText {
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
