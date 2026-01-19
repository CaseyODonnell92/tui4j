package com.williamcallahan.tui4j.compat.bubbletea.bubbles.list;

import java.util.List;

/**
 * Port of Bubbles fetched items.
 * Bubble Tea: bubbletea/examples/list-simple/main.go
 *
 * @param items fetched items
 * @param matchedItems matched item count
 * @param totalItems total item count
 * @param totalPages total page count
 */
public record FetchedItems(List<FilteredItem> items, long matchedItems, long totalItems, int totalPages) {

    public FetchedItems() {
        this(List.of(), 0, 0, 0);
    }
}
