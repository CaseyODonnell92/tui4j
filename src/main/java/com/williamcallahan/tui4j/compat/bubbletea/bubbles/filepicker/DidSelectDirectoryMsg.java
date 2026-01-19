package com.williamcallahan.tui4j.compat.bubbletea.bubbles.filepicker;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Port of the file picker directory selection message.
 * Upstream: github.com/charmbracelet/bubbles/filepicker (didSelectDirectoryMsg)
 */
public class DidSelectDirectoryMsg implements Message {

    private final String path;

    public DidSelectDirectoryMsg(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
