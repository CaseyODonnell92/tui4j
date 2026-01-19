package com.williamcallahan.tui4j.compat.bubbletea.bubbles.filepicker;

import com.williamcallahan.tui4j.compat.bubbletea.bubbles.key.Binding;

/**
 * Port of Bubbles file picker key bindings.
 * Upstream: github.com/charmbracelet/bubbles/filepicker (KeyMap)
 */
public class KeyMap {

    private Binding goToTop;
    private Binding goToLast;
    private Binding down;
    private Binding up;
    private Binding pageUp;
    private Binding pageDown;
    private Binding back;
    private Binding open;
    private Binding select;

    public KeyMap() {
        this.goToTop = new Binding(
                Binding.withKeys("g"),
                Binding.withHelp("g", "first")
        );
        this.goToLast = new Binding(
                Binding.withKeys("G"),
                Binding.withHelp("G", "last")
        );
        this.down = new Binding(
                Binding.withKeys("j", "down", "ctrl+n"),
                Binding.withHelp("j", "down")
        );
        this.up = new Binding(
                Binding.withKeys("k", "up", "ctrl+p"),
                Binding.withHelp("k", "up")
        );
        this.pageUp = new Binding(
                Binding.withKeys("K", "pgup"),
                Binding.withHelp("pgup", "page up")
        );
        this.pageDown = new Binding(
                Binding.withKeys("J", "pgdown"),
                Binding.withHelp("pgdown", "page down")
        );
        this.back = new Binding(
                Binding.withKeys("h", "backspace", "left", "esc"),
                Binding.withHelp("h", "back")
        );
        this.open = new Binding(
                Binding.withKeys("l", "right", "enter"),
                Binding.withHelp("l", "open")
        );
        this.select = new Binding(
                Binding.withKeys("enter"),
                Binding.withHelp("enter", "select")
        );
    }

    public Binding goToTop() {
        return goToTop;
    }

    public void setGoToTop(Binding goToTop) {
        this.goToTop = goToTop;
    }

    public Binding goToLast() {
        return goToLast;
    }

    public void setGoToLast(Binding goToLast) {
        this.goToLast = goToLast;
    }

    public Binding down() {
        return down;
    }

    public void setDown(Binding down) {
        this.down = down;
    }

    public Binding up() {
        return up;
    }

    public void setUp(Binding up) {
        this.up = up;
    }

    public Binding pageUp() {
        return pageUp;
    }

    public void setPageUp(Binding pageUp) {
        this.pageUp = pageUp;
    }

    public Binding pageDown() {
        return pageDown;
    }

    public void setPageDown(Binding pageDown) {
        this.pageDown = pageDown;
    }

    public Binding back() {
        return back;
    }

    public void setBack(Binding back) {
        this.back = back;
    }

    public Binding open() {
        return open;
    }

    public void setOpen(Binding open) {
        this.open = open;
    }

    public Binding select() {
        return select;
    }

    public void setSelect(Binding select) {
        this.select = select;
    }
}
