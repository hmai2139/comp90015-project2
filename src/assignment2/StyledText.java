package assignment2;

import java.awt.*;

public class StyledText {
    private String text;
    private Color colour;
    private Font font;

    public StyledText(String text, Color colour, Font font) {
        this.text = text;
        this.colour = colour;
        this.font = font;
    }

    public String text() {
        return this.text;
    }

    public Color colour() {
        return this.colour;
    }

    public Font font() { return this.font; }
}
