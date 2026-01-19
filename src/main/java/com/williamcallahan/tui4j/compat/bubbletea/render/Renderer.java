package com.williamcallahan.tui4j.compat.bubbletea.render;

import com.williamcallahan.tui4j.compat.bubbletea.Message;

/**
 * Core interface for rendering the TUI and controlling terminal modes.
 * <p>
 * Port of Bubble Tea's `Renderer` interface (renderer.go).
 * Handles the actual output to the terminal, including screen clearing,
 * cursor management, and mouse event tracking.
 */
public interface Renderer {
    /**
     * Starts the renderer loop.
     */
    void start();

    /**
     * Stops the renderer loop and cleans up resources.
     */
    void stop();

    /**
     * Writes the given view string to the terminal buffer.
     */
    void write(String view);

    /**
     * Forces a full repaint of the screen on the next cycle.
     */
    void repaint();

    /**
     * Clears the terminal screen.
     */
    void clearScreen();

    /**
     * Returns true if the renderer is currently using the alternate screen buffer.
     */
    boolean altScreen();

    /**
     * Switches to the alternate screen buffer (e.g. for full-screen apps).
     */
    void enterAltScreen();

    /**
     * Restores the main screen buffer.
     */
    void exitAltScreen();

    /**
     * Shows the terminal cursor.
     */
    void showCursor();

    /**
     * Hides the terminal cursor.
     */
    void hideCursor();

    /**
     * Enables basic mouse reporting (click/wheel).
     */
    default void enableMouseNormalTracking() {
    }

    /**
     * Disables basic mouse reporting.
     */
    default void disableMouseNormalTracking() {
    }

    /**
     * Enables mouse click, release, wheel and motion events if a button is pressed (drag).
     */
    default void enableMouseCellMotion() {
    }

    /**
     * Disables mouse cell motion tracking.
     */
    default void disableMouseCellMotion() {
    }

    /**
     * Enables all mouse events (click, release, wheel, motion) regardless of button state.
     */
    void enableMouseAllMotion();

    /**
     * Disables all motion mouse tracking.
     */
    void disableMouseAllMotion();

    /**
     * Enables extended mouse mode (SGR) for better coordinate support.
     */
    void enableMouseSGRMode();

    /**
     * Disables extended mouse mode.
     */
    void disableMouseSGRMode();

    /**
     * Changes mouse pointer to I-beam shape (OSC 22).
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    default void setMouseCursorText() {
    }

    /**
     * Changes mouse pointer to hand shape (OSC 22).
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    default void setMouseCursorPointer() {
    }

    /**
     * Resets mouse pointer to default shape (OSC 22).
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    default void resetMouseCursor() {
    }

    /**
     * Copies text to the system clipboard (OSC 52).
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    default void copyToClipboard(String text) {
    }

    /**
     * Returns whether focus event reporting is enabled.
     */
    boolean reportFocus();

    /**
     * Enables reporting of terminal focus gain/loss events.
     */
    void enableReportFocus();

    /**
     * Disables reporting of terminal focus events.
     */
    void disableReportFocus();

    /**
     * Notifies the renderer that the model has changed and a render is needed.
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    void notifyModelChanged();

    /**
     * Processes a message that might affect the renderer state (e.g. window resize).
     */
    void handleMessage(Message msg);
}
