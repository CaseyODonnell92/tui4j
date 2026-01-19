package com.williamcallahan.tui4j.compat.bubbletea.message;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Clears the terminal on the next render.
 * Bubble Tea: bubbletea/screen.go
 */
public class ClearScreenMessage implements Message {

    /**
     * Creates a clear screen message.
     */
    public ClearScreenMessage() {
    }
}
