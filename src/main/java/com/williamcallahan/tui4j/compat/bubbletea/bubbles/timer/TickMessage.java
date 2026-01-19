package com.williamcallahan.tui4j.compat.bubbletea.bubbles.timer;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of Bubbles tick message.
 * Bubble Tea: bubbletea/examples/timer/main.go
 *
 * @param id timer id
 * @param timeout whether this tick hit the timeout
 * @param tag timer tag
 */
public record TickMessage(
        int id,
        boolean timeout,
        int tag
) implements Message {
}
