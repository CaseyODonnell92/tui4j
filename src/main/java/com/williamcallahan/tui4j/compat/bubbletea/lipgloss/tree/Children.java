package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.tree;

import java.util.Arrays;

/**
 * Port of Lip Gloss children.
 * Bubble Tea: bubbletea/examples/list-fancy/main.go
 */
public interface Children {

    /**
     * Builds children from string data.
     *
     * @param strings leaf strings
     * @return children collection
     */
    static Children newStringData(String... strings) {
        return new NodeChildren(Arrays.stream(strings)
                .map(Leaf::new)
                .map(Node.class::cast)
                .toList());
    }

    /**
     * Returns the child at an index.
     *
     * @param index child index
     * @return child node
     */
    Node at(int index);

    /**
     * Removes a child at an index.
     *
     * @param index child index
     * @return updated children
     */
    Children remove(int index);

    /**
     * Appends a child node.
     *
     * @param child child node
     * @return updated children
     */
    Children append(Node child);

    /**
     * Returns the number of children.
     *
     * @return child count
     */
    int length();
}
