package com.williamcallahan.tui4j.compat.bubbletea.bubbles.filepicker;

import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Position;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.Color;

/**
 * Port of Bubbles file picker styles.
 * Upstream: github.com/charmbracelet/bubbles/filepicker (Styles)
 */
public class Styles {

    private Style disabledCursor;
    private Style cursor;
    private Style symlink;
    private Style directory;
    private Style file;
    private Style disabledFile;
    private Style permission;
    private Style selected;
    private Style disabledSelected;
    private Style fileSize;
    private Style emptyDirectory;

    public Styles() {
    }

    public static Styles defaultStyles() {
        Styles styles = new Styles();
        styles.disabledCursor = Style.newStyle().foreground(Color.color("247"));
        styles.cursor = Style.newStyle().foreground(Color.color("212"));
        styles.symlink = Style.newStyle().foreground(Color.color("36"));
        styles.directory = Style.newStyle().foreground(Color.color("99"));
        styles.file = Style.newStyle();
        styles.disabledFile = Style.newStyle().foreground(Color.color("243"));
        styles.permission = Style.newStyle().foreground(Color.color("244"));
        styles.selected = Style.newStyle().foreground(Color.color("212")).bold(true);
        styles.disabledSelected = Style.newStyle().foreground(Color.color("247"));
        styles.fileSize = Style.newStyle().foreground(Color.color("240")).width(FILE_SIZE_WIDTH);
        styles.fileSize.align(Position.Right);
        styles.emptyDirectory = Style.newStyle()
                .foreground(Color.color("240"))
                .paddingLeft(PADDING_LEFT)
                .setString("Bummer. No Files Found.");
        return styles;
    }

    public Style disabledCursor() {
        return disabledCursor;
    }

    public void setDisabledCursor(Style disabledCursor) {
        this.disabledCursor = disabledCursor;
    }

    public Style cursor() {
        return cursor;
    }

    public void setCursor(Style cursor) {
        this.cursor = cursor;
    }

    public Style symlink() {
        return symlink;
    }

    public void setSymlink(Style symlink) {
        this.symlink = symlink;
    }

    public Style directory() {
        return directory;
    }

    public void setDirectory(Style directory) {
        this.directory = directory;
    }

    public Style file() {
        return file;
    }

    public void setFile(Style file) {
        this.file = file;
    }

    public Style disabledFile() {
        return disabledFile;
    }

    public void setDisabledFile(Style disabledFile) {
        this.disabledFile = disabledFile;
    }

    public Style permission() {
        return permission;
    }

    public void setPermission(Style permission) {
        this.permission = permission;
    }

    public Style selected() {
        return selected;
    }

    public void setSelected(Style selected) {
        this.selected = selected;
    }

    public Style disabledSelected() {
        return disabledSelected;
    }

    public void setDisabledSelected(Style disabledSelected) {
        this.disabledSelected = disabledSelected;
    }

    public Style fileSize() {
        return fileSize;
    }

    public void setFileSize(Style fileSize) {
        this.fileSize = fileSize;
    }

    public Style emptyDirectory() {
        return emptyDirectory;
    }

    public void setEmptyDirectory(Style emptyDirectory) {
        this.emptyDirectory = emptyDirectory;
    }

    private static final int FILE_SIZE_WIDTH = 7;
    private static final int PADDING_LEFT = 2;
}
