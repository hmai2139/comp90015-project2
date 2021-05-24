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

