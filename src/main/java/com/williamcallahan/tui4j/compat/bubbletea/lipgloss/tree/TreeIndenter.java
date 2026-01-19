package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.tree;

/**
 * Port of the Lip Gloss tree indenter.
 * Upstream: github.com/charmbracelet/lipgloss/tree (Indenter)
 */
@FunctionalInterface
public interface TreeIndenter {

    class DefaultIndenter implements TreeIndenter {

        @Override
        public String indent(Children children, int index) {
            if (children.length() - 1 == index) {
                return "   ";
            }
            return "│  ";
        }
    }

    String indent(Children children, int index);
}
