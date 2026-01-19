package com.williamcallahan.tui4j.compat.bubbletea.bubbles.timer;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of Bubbles start stop message.
 * Bubble Tea: bubbletea/examples/timer/main.go
 *
 * @param id timer id
 * @param running whether the timer should run
 */
public record StartStopMessage(
        int id,
        boolean running
) implements Message {
}
