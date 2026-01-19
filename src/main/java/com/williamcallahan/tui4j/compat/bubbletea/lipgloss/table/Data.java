package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.table;

/**
 * Port of Lipgloss table data interface.
 * Bubble Tea: lipgloss/table/rows.go
 */
public interface Data {

    /**
     * Returns the value at a row and cell index.
     *
     * @param row row index
     * @param cell cell index
     * @return cell value
     */
    String at(int row, int cell);

    /**
     * Returns the number of rows.
     *
     * @return row count
     */
    int rows();

    /**
     * Returns the number of columns.
     *
     * @return column count
     */
    int columns();
}
