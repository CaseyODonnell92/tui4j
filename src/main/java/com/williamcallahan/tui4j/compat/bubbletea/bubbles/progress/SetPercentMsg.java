package com.williamcallahan.tui4j.compat.bubbletea.bubbles.progress;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of the progress percentage update message.
 * Upstream: github.com/charmbracelet/bubbles/progress (setPercentMsg)
 *
 * @param percent new progress percentage
 */
public record SetPercentMsg(double percent) implements Message {
}
