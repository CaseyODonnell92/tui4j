package com.williamcallahan.tui4j.compat.bubbletea.bubbles.filepicker;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of the file picker file selection message.
 * Upstream: github.com/charmbracelet/bubbles/filepicker (didSelectFileMsg)
 */
public class DidSelectFileMsg implements Message {

    private final String path;

    public DidSelectFileMsg(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
