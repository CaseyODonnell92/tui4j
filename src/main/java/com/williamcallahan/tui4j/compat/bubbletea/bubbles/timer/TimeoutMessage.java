package com.williamcallahan.tui4j.compat.bubbletea.bubbles.timer;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Emitted once when a timer reaches (or passes) its timeout.
 * Bubble Tea: bubbletea/examples/timer/main.go
 *
 * @param id timer id
 */
public record TimeoutMessage(int id) implements Message {
}
