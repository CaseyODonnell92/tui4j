package com.williamcallahan.tui4j.compat.bubbletea.bubbles.table;

/**
 * Port of Bubbles table Column.
 * Bubble Tea: bubbles/table/table.go
 *
 * @param title column title
 * @param width column width
 */
public record Column(
        String title,
        int width
) {

    /**
     * Normalizes negative widths to zero.
     */
    public Column {
        if (width < 0) {
            width = 0;
        }
    }

    /**
     * Creates a column with a default width of zero.
     *
     * @param title column title
     */
    public Column(String title) {
        this(title, 0);
    }
}
