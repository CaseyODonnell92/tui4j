package com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * StartStopMsg is sent when the stopwatch should start or stop.
 *
 * @param id stopwatch id
 * @param running whether the stopwatch should run
 */
public record StartStopMsg(
        int id,
        boolean running
) implements Message {
}
