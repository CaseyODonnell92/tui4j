package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.tree;

/**
 * Port of the Lip Gloss tree enumerator.
 * Upstream: github.com/charmbracelet/lipgloss/tree (Enumerator)
 */
@FunctionalInterface
public interface TreeEnumerator {

    class DefaultEnumerator implements TreeEnumerator {

        @Override
        public String enumerate(Children children, int index) {
            if (children.length() - 1 == index) {
                return "└──";
            }
            return "├──";
        }
    }

    class RounderEnumerator implements TreeEnumerator {

        @Override
        public String enumerate(Children children, int index) {
            if (children.length() - 1 == index) {
                return "╰──";
            }
            return "├──";
        }
    }

    String enumerate(Children children, int index);
}
