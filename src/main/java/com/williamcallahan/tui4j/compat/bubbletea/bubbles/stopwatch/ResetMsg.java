package com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * ResetMsg is sent when the stopwatch should reset.
 *
 * @param id stopwatch id
 */
public record ResetMsg(
        int id
) implements Message {
}
