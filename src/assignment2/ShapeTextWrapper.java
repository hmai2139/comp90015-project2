/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * A wrapper for the StyledShape and StyledText class, used in socket object transfer.
 * This class facilitates the safe casting of Objects read from a socket's Object Input Stream.
 */

package assignment2;

public class ShapeTextWrapper {
    public StyledShape styledShape;
    public StyledText styledText;

    public ShapeTextWrapper(StyledShape styledShape, StyledText styledText) {
        this.styledShape = styledShape;
        this.styledText = styledText;
    }
}
