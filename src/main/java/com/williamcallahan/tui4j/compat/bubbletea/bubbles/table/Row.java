package com.williamcallahan.tui4j.compat.bubbletea.bubbles.table;

import java.util.Arrays;
import java.util.List;

/**
 * Port of Bubbles table Row.
 * Bubble Tea: bubbles/table/table.go
 *
 * @param cells row cell values
 */
public record Row(
        List<String> cells
) {

    public Row(String... cells) {
        this(Arrays.asList(cells));
    }

    public String at(int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index);
    }

    public int size() {
        return cells.size();
    }
}
