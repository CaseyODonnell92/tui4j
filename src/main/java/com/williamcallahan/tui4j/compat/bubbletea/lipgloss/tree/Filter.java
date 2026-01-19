package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.tree;

/**
 * Port of the Lip Gloss tree filter wrapper.
 * Upstream: github.com/charmbracelet/lipgloss/tree (Filter)
 */
public class Filter implements Children {

    /**
     * Port of the tree filter predicate.
     * Upstream: github.com/charmbracelet/lipgloss/tree (FilterFunc)
     */
    public interface FilterFunction {
        boolean filter(int index);
    }

    private final Children data;
    private FilterFunction filterFunction = index -> true;

    public Filter(Children data) {
        this.data = data;
    }

    @Override
    public Node at(int index) {
        int j = 0;
        for (int i = 0; i < data.length(); i++) {
            if (filterFunction.filter(i)) {
                if (j == index) {
                    return data.at(i);
                }
                j++;
            }
        }

        return null;
    }

    public Filter filter(FilterFunction filterFunction) {
        this.filterFunction = filterFunction;
        return this;
    }

    @Override
    public Children remove(int index) {
        throw new UnsupportedOperationException("Filter is a read-only view");
    }

    @Override
    public Children append(Node child) {
        throw new UnsupportedOperationException("Filter is a read-only view");
    }

    @Override
    public int length() {
        int j = 0;
        for (int i = 0; i < data.length(); i++) {
            if (filterFunction.filter(i)) {
                j++;
            }
        }
        return j;
    }
}
