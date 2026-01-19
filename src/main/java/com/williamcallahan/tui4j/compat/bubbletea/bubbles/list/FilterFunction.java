package com.williamcallahan.tui4j.compat.bubbletea.bubbles.list;

/**
 * Port of the list filter function contract.
 * Upstream: github.com/charmbracelet/bubbles/list (FilterFunc)
 */
@FunctionalInterface
public interface FilterFunction {

    Rank[] apply(String term, String[] targets);
}
