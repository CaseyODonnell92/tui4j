package com.williamcallahan.tui4j.message;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Requests copying text to the clipboard.
 * tui4j extension; no Bubble Tea equivalent.
 * tui4j: src/main/java/com/williamcallahan/tui4j/message/CopyToClipboardMessage.java
 *
 * @param text text to copy
 */
public record CopyToClipboardMessage(String text) implements Message {
}
