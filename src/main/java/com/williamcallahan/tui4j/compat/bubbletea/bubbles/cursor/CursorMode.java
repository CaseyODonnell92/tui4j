package com.williamcallahan.tui4j.compat.bubbletea.bubbles.cursor;

import java.util.stream.Stream;

/**
 * Port of Bubbles cursor mode.
 * Bubble Tea: bubbletea/examples/textinputs/main.go
 */
public enum CursorMode {
    /** Blinking cursor. */
    Blink,
    /** Static cursor. */
    Static,
    /** Hidden cursor. */
    Hide;

    /**
     * Returns the cursor mode for an ordinal value.
     *
     * @param value ordinal value
     * @return cursor mode or null if not found
     */
    public static CursorMode fromOrdinal(int value) {
        return Stream.of(CursorMode.values()).filter(mode -> mode.ordinal() == value).findAny().orElse(null);
    }
}
