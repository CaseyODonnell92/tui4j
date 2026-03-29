package com.williamcallahan.tui4j.compat.bubbles.list;

import com.williamcallahan.tui4j.compat.bubbletea.BatchMessage;
import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.QuitMessage;
import com.williamcallahan.tui4j.compat.bubbletea.SequenceMessage;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.Key;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyType;
import com.williamcallahan.tui4j.compat.lipgloss.Renderer;
import com.williamcallahan.tui4j.compat.lipgloss.color.ColorProfile;
import com.williamcallahan.tui4j.compat.lipgloss.color.NoColor;
import com.williamcallahan.tui4j.term.TerminalInfo;

/**
 * Shared test utilities for {@link List} component tests.
 */
final class ListTestSupport {

    private ListTestSupport() {}

    /** Configures a deterministic Ascii-only terminal for tests. */
    static void initTestEnvironment() {
        TerminalInfo.provide(() -> new TerminalInfo(false, new NoColor()));
        Renderer.defaultRenderer().setColorProfile(ColorProfile.Ascii);
    }

    /** Creates a {@link KeyPressMessage} for a printable character. */
    static KeyPressMessage runeKey(char c) {
        return new KeyPressMessage(
            new Key(KeyType.KeyRunes, new char[]{c})
        );
    }

    /** Creates a {@link KeyPressMessage} for a special key type. */
    static KeyPressMessage specialKey(KeyType type) {
        return new KeyPressMessage(new Key(type));
    }

    /** Creates an initialized list with the simple numbered delegate. */
    static List createList(int width, int height, Item... items) {
        List list = new List(items, new SimpleDelegate(), width, height);
        applyCommand(list, list.init());
        return list;
    }

    /** Creates a list with all chrome hidden, using the cursor delegate. */
    static List createMinimalList(int width, int height, Item... items) {
        List list = new List(items, new CursorDelegate(), width, height);
        applyCommand(list, list.init());
        stripChrome(list);
        return list;
    }

    /** Hides all non-content sections and refreshes. */
    static void stripChrome(List list) {
        applyCommand(list, list.setShowTitle(false));
        list.setShowFilter(false);
        list.setShowStatusBar(false);
        list.setShowPagination(false);
        applyCommand(list, list.setShowHelp(false));
        applyCommand(list, list.refresh());
    }

    /** Synchronously resolves a command through the model update loop. */
    static void applyCommand(List list, Command command) {
        if (Command.isNone(command)) {
            return;
        }
        applyMessage(list, command.execute());
    }

    private static final int MAX_DEPTH = 100;
    private static int depth = 0;

    /** Synchronously delivers a message through the model update loop. */
    static void applyMessage(List list, Message msg) {
        if (msg == null) {
            return;
        }
        if (depth++ > MAX_DEPTH) {
            depth = 0;
            return;
        }
        try {
            dispatchMessage(list, msg);
        } finally {
            depth--;
        }
    }

    private static void dispatchMessage(List list, Message msg) {
        if (msg instanceof
            com.williamcallahan.tui4j.compat.bubbles.spinner.TickMessage) {
            return;
        }
        String cn = msg.getClass().getSimpleName();
        if ("InitialBlinkMessage".equals(cn) || "BlinkMessage".equals(cn)) {
            return;
        }
        if (msg instanceof BatchMessage bm) {
            for (Command c : bm.commands()) {
                applyCommand(list, c);
            }
            return;
        }
        if (msg instanceof SequenceMessage sm) {
            for (Command c : sm.commands()) {
                applyCommand(list, c);
            }
            return;
        }
        UpdateResult<List> result = list.update(msg);
        if (result != null && !Command.isNone(result.command())) {
            applyCommand(list, result.command());
        }
    }

    /** Resolves a command tree to find whether it produces a {@link QuitMessage}. */
    static boolean producesQuit(Command command) {
        if (Command.isNone(command)) {
            return false;
        }
        Message msg = command.execute();
        if (msg instanceof QuitMessage) {
            return true;
        }
        if (msg instanceof BatchMessage bm) {
            for (Command c : bm.commands()) {
                if (producesQuit(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Right-pads every line in a text block to the width of the widest line.
     * Java text blocks strip trailing whitespace, but TUI views have every
     * line right-padded to a uniform width; this restores that padding.
     */
    static String view(String textBlock) {
        String[] lines = textBlock.split("\n", -1);
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]);
            int pad = width - lines[i].length();
            if (pad > 0) {
                sb.append(" ".repeat(pad));
            }
            if (i < lines.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Like {@link #view(String)} but also pads the result to the given
     * line count, filling trailing lines with spaces to match the width.
     */
    static String view(int totalLines, String textBlock) {
        String[] lines = textBlock.split("\n", -1);
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalLines; i++) {
            String line = i < lines.length ? lines[i] : "";
            sb.append(line);
            int pad = width - line.length();
            if (pad > 0) {
                sb.append(" ".repeat(pad));
            }
            if (i < totalLines - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /** Creates an initialized list with the cursor delegate and all chrome. */
    static List createFullList(int width, int height, Item... items) {
        List list = new List(
            items, new CursorDelegate(), width, height
        );
        applyCommand(list, list.init());
        return list;
    }

    /** Builds an {@link Item} array from string values. */
    static Item[] items(String... values) {
        Item[] result = new Item[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = new TestItem(values[i]);
        }
        return result;
    }

    /** Minimal item backed by a single filter value. */
    record TestItem(String value) implements Item {
        @Override
        public String filterValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /** Delegate that renders items as numbered lines without selection. */
    static final class SimpleDelegate implements ItemDelegate {

        @Override
        public void render(
            StringBuilder output, List list, int index, FilteredItem item
        ) {
            output.append(index + 1)
                .append(". ")
                .append(item.item().filterValue());
        }

        @Override
        public int height() {
            return 1;
        }

        @Override
        public int spacing() {
            return 0;
        }

        @Override
        public Command update(Message msg, List listModel) {
            return Command.none();
        }
    }

    /** Delegate that renders a cursor prefix to show selection state. */
    static final class CursorDelegate implements ItemDelegate {

        @Override
        public void render(
            StringBuilder output, List list, int index, FilteredItem item
        ) {
            String prefix = (index == list.index()) ? "> " : "  ";
            output.append(prefix).append(item.item().filterValue());
        }

        @Override
        public int height() {
            return 1;
        }

        @Override
        public int spacing() {
            return 0;
        }

        @Override
        public Command update(Message msg, List listModel) {
            return Command.none();
        }
    }
}
