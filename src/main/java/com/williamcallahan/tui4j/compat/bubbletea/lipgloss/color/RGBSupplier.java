package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color;

/**
 * Port of Lip Gloss rgb supplier.
 * Bubble Tea: bubbletea/examples/list-fancy/main.go
 */
public interface RGBSupplier {
    /**
     * Returns the RGB representation of the color.
     *
     * @return RGB value
     */
    RGB rgb();
}
