/**
 * @author Hoang Viet Mai, vietm@student.unimelb.edu.au, 813361.
 * COMP90015 S1 2021, Assignment 2, Distributed Whiteboard System.
 * Enum of all response type from server.
 */

package assignment2;

public enum Response {

    // Non error response.
    CANVAS_FROM_FILE,
    CLEAR,
    JOIN_DECLINED,
    KICKED,
    LOGIN_SUCCESS,
    NEW_JOINED,
    WHITEBOARD_CLOSED,

    // Error response.
    INVALID,
    USERNAME_TAKEN,

}
