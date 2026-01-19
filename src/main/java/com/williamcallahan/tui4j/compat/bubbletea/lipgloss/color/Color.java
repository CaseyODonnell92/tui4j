package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color;

import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Renderer;
import org.jline.utils.AttributedStyle;

/**
 * Simple hex or ANSI color.
 * <p>
 * Port of `lipgloss/color`.
 * Represents a color that can be applied to terminal output.
 */
public final class Color implements TerminalColor {

    /**
     * Creates a {@link Color} from a string representation.
     *
     * @param color color string
     * @return color instance
     */
    public static Color color(String color) {
        return new Color(color);
    }

    private final String color;

    private Color(String color) {
        this.color = color;
    }

    @Override
    public AttributedStyle applyAsBackground(AttributedStyle style, Renderer renderer) {
        return renderer
                .colorProfile()
                .color(color)
                .applyAsBackground(style, renderer);
    }

    @Override
    public AttributedStyle applyAsForeground(AttributedStyle style, Renderer renderer) {
        return renderer
                .colorProfile()
                .color(color)
                .applyAsForeground(style, renderer);
    }
}
