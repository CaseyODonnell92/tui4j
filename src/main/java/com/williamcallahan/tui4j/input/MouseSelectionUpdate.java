package com.williamcallahan.tui4j.input;

import com.williamcallahan.tui4j.compat.bubbletea.input.MouseMessage;

/**
 * Represents a selection update from a mouse event.
 * tui4j extension; no Bubble Tea equivalent.
 * tui4j: src/main/java/com/williamcallahan/tui4j/input/MouseSelectionUpdate.java
 *
 * @param selectionStarted whether selection began on this event
 * @param selectionEnded whether selection ended on this event
 * @param selectionActive whether selection is active
 * @param selectionScrollUpdate mouse scroll update
 */
public record MouseSelectionUpdate(
        boolean selectionStarted,
        boolean selectionEnded,
        boolean selectionActive,
        MouseMessage selectionScrollUpdate
) {
}
