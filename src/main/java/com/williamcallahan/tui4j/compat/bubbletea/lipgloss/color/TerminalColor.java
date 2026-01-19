package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color;

import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Renderer;
import org.jline.utils.AttributedStyle;

/**
 * Port of Lip Gloss terminal color.
 * Bubble Tea: bubbletea/examples/list-fancy/main.go
 */
public interface TerminalColor {
    /**
     * Applies this color as a background style.
     *
     * @param style base style
     * @param renderer renderer in use
     * @return updated style
     */
    AttributedStyle applyAsBackground(AttributedStyle style, Renderer renderer);

    /**
     * Applies this color as a foreground style.
     *
     * @param style base style
     * @param renderer renderer in use
     * @return updated style
     */
    AttributedStyle applyAsForeground(AttributedStyle style, Renderer renderer);
}
