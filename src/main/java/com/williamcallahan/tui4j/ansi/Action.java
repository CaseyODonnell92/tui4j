package com.williamcallahan.tui4j.ansi;

/**
 * ANSI parser action token.
 * tui4j: src/main/java/com/williamcallahan/tui4j/ansi/Action.java
 */
public enum Action {
    /** No-op. */
    NONE,
    /** Reset parser state. */
    CLEAR,
    /** Collect intermediate bytes. */
    COLLECT,
    /** Marker action for state transitions. */
    MARKER,
    /** Dispatch a control sequence. */
    DISPATCH,
    /** Execute a control character. */
    EXECUTE,
    /** Start a new sequence. */
    START,
    /** Store data bytes. */
    PUT,
    /** Collect parameter bytes. */
    PARAM,
    /** Print a glyph. */
    PRINT,
    /** Ignore input. */
    IGNORE;

    /**
     * Returns the action for a stored ordinal.
     *
     * @param ordinal ordinal value
     * @return matching action
     */
    public static Action fromOrdinal(int ordinal) {
        return values()[ordinal];
    }
}
