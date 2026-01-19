package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.tree;

import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style;

/**
 * Port of the Lip Gloss tree style callback.
 * Upstream: github.com/charmbracelet/lipgloss/tree (StyleFunc)
 */
@FunctionalInterface
public interface StyleFunction {

    Style apply(Children children, int index);
}
