package assignment2;

public enum Response {

    // Non error response.
    LOGIN_SUCCESS,
    JOIN_DECLINED,
    WHITEBOARD_CLOSED,
    WHITEBOARD_KICKED,
    CLEAR,

    // Error response.
    USERNAME_TAKEN,
    INVALID
}
