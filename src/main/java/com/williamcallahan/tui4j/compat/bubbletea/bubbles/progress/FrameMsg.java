package com.williamcallahan.tui4j.compat.bubbletea.bubbles.progress;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of the progress animation frame message.
 * Upstream: github.com/charmbracelet/bubbles/progress (frameMsg)
 *
 * @param id animation id
 * @param tag animation tag
 */
public record FrameMsg(int id, int tag) implements Message {
}
