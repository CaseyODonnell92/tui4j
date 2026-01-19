package com.williamcallahan.tui4j.compat.bubbletea.lipgloss.table;

import com.williamcallahan.tui4j.ansi.TextWidth;
import com.williamcallahan.tui4j.ansi.Truncate;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Renderer;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Size;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.TextLines;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.border.Border;

import java.util.ArrayList;
import java.util.List;

/**
 * Data table component.
 * <p>
 * Port of `lipgloss/table`.
 * Renders structured data in a grid with customizable borders, headers, and cell styling.
 */
public class Table {

    public static final int HEADER_ROW = -1;

    private static final StyleFunc DEFAULT_STYLES = (row, col) -> Style.newStyle();

    private StyleFunc styleFunc = DEFAULT_STYLES;
    private Border border;

    private boolean borderTop = true;
    private boolean borderBottom = true;
    private boolean borderLeft = true;
    private boolean borderRight = true;
    private boolean borderHeader = true;
    private boolean borderColumn = true;
    private boolean borderRow = true;

    private Style borderStyle;
    private List<String> headers = new ArrayList<>();
    private Data data;

    private int width = 0;
    private int height = 0;
    private boolean useManualHeight = false;
    private int offset = 0;
    private boolean wrap = true;

    private int[] widths;
    private int[] heights;

    private final Renderer renderer;

    public Table(Renderer renderer) {
        this.renderer = renderer;
        this.border = createDefaultBorder();
        this.borderStyle = Style.newStyle();
    }

    private Border createDefaultBorder() {
        return new Border(
                " ", " ", " ", " ",
                " ", " ", " ", " ",
                " ", " ", " ", " ", " "
        );
    }

    public static Table create() {
        return new Table(Renderer.defaultRenderer());
    }

    public Table styleFunc(StyleFunc styleFunc) {
        this.styleFunc = styleFunc;
        return this;
    }

    Style style(int row, int col) {
        if (styleFunc == null) {
            return Style.newStyle();
        }
        return styleFunc.apply(row, col);
    }

    public Table data(Data data) {
        this.data = data;
        return this;
    }

    public Table rows(String[]... rows) {
        if (data instanceof StringData stringData) {
            for (String[] row : rows) {
                stringData.append(row);
            }
        }
        return this;
    }

    public Table row(String... row) {
        if (data instanceof StringData stringData) {
            stringData.append(row);
        }
        return this;
    }

    public Table headers(String... headers) {
        for (String header : headers) {
            this.headers.add(header);
        }
        return this;
    }

    public Table border(Border border) {
        this.border = border;
        return this;
    }

    public Table borderTop(boolean borderTop) {
        this.borderTop = borderTop;
        return this;
    }

    public Table borderBottom(boolean borderBottom) {
        this.borderBottom = borderBottom;
        return this;
    }

    public Table borderLeft(boolean borderLeft) {
        this.borderLeft = borderLeft;
        return this;
    }

    public Table borderRight(boolean borderRight) {
        this.borderRight = borderRight;
        return this;
    }

    public Table borderHeader(boolean borderHeader) {
        this.borderHeader = borderHeader;
        return this;
    }

    public Table borderColumn(boolean borderColumn) {
        this.borderColumn = borderColumn;
        return this;
    }

    public Table borderRow(boolean borderRow) {
        this.borderRow = borderRow;
        return this;
    }

    public Table borderStyle(Style borderStyle) {
        this.borderStyle = borderStyle;
        return this;
    }

    public Table width(int width) {
        this.width = width;
        return this;
    }

    public Table height(int height) {
        this.height = height;
        this.useManualHeight = true;
        return this;
    }

    public Table offset(int offset) {
        this.offset = offset;
        return this;
    }

    public Table wrap(boolean wrap) {
        this.wrap = wrap;
        return this;
    }

    /**
     * Renders the table to a string.
     * Computes dimensions, applies borders, and renders headers and rows.
     */
    public String render() {
        boolean hasHeaders = !headers.isEmpty();
        boolean hasRows = data != null && data.rows() > 0;

        if (!hasHeaders && !hasRows) {
            return "";
        }

        if (hasHeaders) {
            while (headers.size() < data.columns()) {
                headers.add("");
            }
        }

        resize();

        StringBuilder sb = new StringBuilder();

        if (borderTop) {
            sb.append(constructTopBorder()).append("\n");
        }

        if (hasHeaders) {
            sb.append(constructHeaders()).append("\n");
        }

        String bottom = "";
        if (borderBottom) {
            bottom = constructBottomBorder();
        }

        if (data.rows() > 0) {
            if (useManualHeight) {
                int topHeight = Size.height(sb.toString()) - 1;
                int availableLines = height - (topHeight + Size.height(bottom));

                if (availableLines > data.rows()) {
                    availableLines = data.rows();
                }
                sb.append(constructRows(availableLines));
            } else {
                for (int r = offset; r < data.rows(); r++) {
                    sb.append(constructRow(r, false));
                }
            }
        }

        sb.append(bottom);

        return Style.newStyle()
                .height(height > 0 ? height : computeHeight())
                .width(width > 0 ? width : computeWidth())
                .render(sb.toString());
    }

    private int computeHeight() {
        boolean hasHeaders = !headers.isEmpty();
        int sumHeights = 0;
        for (int h : heights) {
            sumHeights += h;
        }
        return sumHeights - 1 + boolToInt(hasHeaders) +
                boolToInt(borderTop) + boolToInt(borderBottom) +
                boolToInt(borderHeader) + data.rows() * boolToInt(borderRow);
    }

    private int computeWidth() {
        int totalWidth = 0;
        for (int w : widths) {
            totalWidth += w;
        }
        if (borderLeft) {
            totalWidth += border.getLeftSize();
        }
        if (borderRight) {
            totalWidth += border.getRightSize();
        }
        if (borderColumn && widths.length > 1) {
            totalWidth += (widths.length - 1) * border.getLeftSize();
        }
        return totalWidth;
    }

    private int boolToInt(boolean b) {
        return b ? 1 : 0;
    }

    private String constructTopBorder() {
        StringBuilder s = new StringBuilder();
        if (borderLeft) {
            s.append(borderStyle.render(border.topLeft()));
        }
        for (int i = 0; i < widths.length; i++) {
            s.append(borderStyle.render(border.top().repeat(widths[i])));
            if (i < widths.length - 1 && borderColumn) {
                s.append(borderStyle.render(border.middleTop()));
            }
        }
        if (borderRight) {
            s.append(borderStyle.render(border.topRight()));
        }
        return s.toString();
    }

    private String constructBottomBorder() {
        StringBuilder s = new StringBuilder();
        if (borderLeft) {
            s.append(borderStyle.render(border.bottomLeft()));
        }
        for (int i = 0; i < widths.length; i++) {
            s.append(borderStyle.render(border.bottom().repeat(widths[i])));
            if (i < widths.length - 1 && borderColumn) {
                s.append(borderStyle.render(border.middleBottom()));
            }
        }
        if (borderRight) {
            s.append(borderStyle.render(border.bottomRight()));
        }
        return s.toString();
    }

    private String constructHeaders() {
        int headerHeight = heights[0];

        StringBuilder s = new StringBuilder();
        if (borderLeft) {
            s.append(borderStyle.render(border.left()));
        }
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            Style cellStyle = style(HEADER_ROW, i);

            if (!wrap) {
                header = truncateCell(header, HEADER_ROW, i);
            }

            int cellWidth = widths[i] - cellStyle.getHorizontalMargins();
            s.append(cellStyle
                    .height(headerHeight - cellStyle.getVerticalMargins())
                    .width(cellWidth)
                    .render(truncateCell(header, HEADER_ROW, i)));
            if (i < headers.size() - 1 && borderColumn) {
                s.append(borderStyle.render(border.left()));
            }
        }
        if (borderHeader) {
            if (borderRight) {
                s.append(borderStyle.render(border.right()));
            }
            s.append("\n");
            if (borderLeft) {
                s.append(borderStyle.render(border.middleLeft()));
            }
            for (int i = 0; i < headers.size(); i++) {
                s.append(borderStyle.render(border.top().repeat(widths[i])));
                if (i < headers.size() - 1 && borderColumn) {
                    s.append(borderStyle.render(border.middle()));
                }
            }
            if (borderRight) {
                s.append(borderStyle.render(border.middleRight()));
            }
        }
        if (borderRight && !borderHeader) {
            s.append(borderStyle.render(border.right()));
        }
        return s.toString();
    }

    private String constructRows(int availableLines) {
        StringBuilder sb = new StringBuilder();

        int offsetRowCount = data.rows() - offset;

        int rowsToRender = Math.max(availableLines, 1);

        boolean needsOverflow = rowsToRender < offsetRowCount;

        int rowIdx = offset;
        if (!needsOverflow) {
            rowIdx = Math.max(0, data.rows() - rowsToRender);
        }
        while (rowsToRender > 0 && rowIdx < data.rows()) {
            boolean isOverflow = needsOverflow && rowsToRender == 1;

            sb.append(constructRow(rowIdx, isOverflow));

            rowIdx++;
            rowsToRender--;
        }
        return sb.toString();
    }

    private String constructRow(int index, boolean isOverflow) {
        StringBuilder s = new StringBuilder();

        boolean hasHeaders = !headers.isEmpty();
        int rowHeight = heights[index + boolToInt(hasHeaders)];
        if (isOverflow) {
            rowHeight = 1;
        }

        List<String> cells = new ArrayList<>();
        String leftBorder = borderStyle.render(border.left()) + "\n";
        if (borderLeft) {
            for (int i = 0; i < rowHeight; i++) {
                cells.add(leftBorder);
            }
        }

        if (borderRight) {
            String rightBorder = borderStyle.render(border.right()) + "\n";
            for (int i = 0; i < rowHeight; i++) {
                cells.add(rightBorder);
            }
        }

        for (int i = 0; i < cells.size(); i++) {
            cells.set(i, cells.get(i).replaceAll("\n$", ""));
        }

        return joinHorizontal(cells.toArray(new String[0]));
    }

    private String joinHorizontal(String... parts) {
        if (parts.length == 0) {
            return "";
        }
        if (parts.length == 1) {
            return parts[0];
        }
        StringBuilder result = new StringBuilder();
        int maxLines = 0;
        String[][] linesParts = new String[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            linesParts[i] = parts[i].split("\n");
            maxLines = Math.max(maxLines, linesParts[i].length);
        }
        for (int line = 0; line < maxLines; line++) {
            if (line > 0) {
                result.append("\n");
            }
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append("");
                }
                if (line < linesParts[i].length) {
                    result.append(linesParts[i][line]);
                } else {
                    int width = TextWidth.measureCellWidth(parts[i].split("\n")[0]);
                    result.append(" ".repeat(width));
                }
            }
        }
        return result.toString();
    }

    private String truncateCell(String cell, int rowIndex, int colIndex) {
        boolean hasHeaders = !headers.isEmpty();
        int rowHeight = heights[rowIndex + boolToInt(hasHeaders)];
        int cellWidth = widths[colIndex];
        Style cellStyle = style(rowIndex, colIndex);

        int length = (cellWidth * rowHeight) - cellStyle.getHorizontalPadding() - cellStyle.getHorizontalMargins();
        return Truncate.truncate(cell, length, "…");
    }

    private void resize() {
        int numCols = Math.max(headers.size(), data != null ? data.columns() : 0);
        calculateWidths(numCols);
        calculateHeights(numCols);
        adjustWidthsForConstraint(numCols);
    }

    private void calculateWidths(int numCols) {
        widths = new int[numCols];
        for (int c = 0; c < numCols; c++) {
            int maxWidth = 0;
            for (int r = 0; r < data.rows(); r++) {
                String cell = data.at(r, c);
                int cellWidth = TextWidth.measureCellWidth(cell);
                maxWidth = Math.max(maxWidth, cellWidth);
            }
            if (c < headers.size()) {
                int headerWidth = TextWidth.measureCellWidth(headers.get(c));
                maxWidth = Math.max(maxWidth, headerWidth);
            }
            widths[c] = maxWidth;
        }
    }

    private void calculateHeights(int numCols) {
        heights = new int[data.rows() + boolToInt(!headers.isEmpty())];
        for (int r = 0; r < data.rows(); r++) {
            int maxHeight = 1;
            for (int c = 0; c < numCols; c++) {
                String cell = data.at(r, c);
                int cellWidth = widths[c];
                if (cellWidth > 0 && wrap) {
                    TextLines lines = TextLines.fromText(cell);
                    for (String line : lines.lines()) {
                        int numLines = (int) Math.ceil((double) TextWidth.measureCellWidth(line) / cellWidth);
                        maxHeight = Math.max(maxHeight, numLines);
                    }
                }
            }
            heights[r + boolToInt(!headers.isEmpty())] = maxHeight;
        }

        if (!headers.isEmpty()) {
            int headerHeight = 1;
            for (int c = 0; c < headers.size(); c++) {
                String header = headers.get(c);
                int headerWidth = widths[c];
                if (headerWidth > 0 && wrap) {
                    TextLines lines = TextLines.fromText(header);
                    for (String line : lines.lines()) {
                        int numLines = (int) Math.ceil((double) TextWidth.measureCellWidth(line) / headerWidth);
                        headerHeight = Math.max(headerHeight, numLines);
                    }
                }
            }
            heights[0] = headerHeight;
        }
    }

    private void adjustWidthsForConstraint(int numCols) {
        if (width > 0) {
            int availableWidth = width;
            if (borderLeft) {
                availableWidth -= border.getLeftSize();
            }
            if (borderRight) {
                availableWidth -= border.getRightSize();
            }
            if (borderColumn && numCols > 1) {
                availableWidth -= (numCols - 1) * border.getLeftSize();
            }

            int totalContentWidth = 0;
            for (int w : widths) {
                totalContentWidth += w;
            }

            if (totalContentWidth < availableWidth) {
                int extraWidth = availableWidth - totalContentWidth;
                int numExpandable = widths.length;
                for (int i = 0; i < widths.length; i++) {
                    int add = extraWidth / numExpandable;
                    widths[i] += add;
                    extraWidth -= add;
                }
            } else if (totalContentWidth > availableWidth) {
                int overflow = totalContentWidth - availableWidth;
                for (int i = widths.length - 1; i >= 0 && overflow > 0; i--) {
                    int reduce = Math.min(widths[i] - 1, overflow);
                    widths[i] -= reduce;
                    overflow -= reduce;
                }
            }
        }
    }

    @Override
    public String toString() {
        return render();
    }
}
