package assignment2;

import java.awt.*;

public class ColouredShape {
    private Shape shape;
    private Color colour;

    public ColouredShape(Shape shape, Color colour) {
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

