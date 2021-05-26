package assignment2;

public enum Response {

    // Non error response.
    CANVAS_FROM_FILE,
    CLEAR,
    JOIN_DECLINED,
    LOGIN_SUCCESS,
    WHITEBOARD_CLOSED,
    WHITEBOARD_KICKED,

    // Error response.
    INVALID,
    USERNAME_TAKEN,

}
