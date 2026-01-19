package com.williamcallahan.tui4j.compat.bubbletea.bubbles.textarea;

import com.ibm.icu.lang.UCharacter;
import com.williamcallahan.tui4j.ansi.TextWidth;
import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.AdaptiveColor;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.Color;
import com.williamcallahan.tui4j.compat.bubbletea.message.BlurMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.FocusMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.cursor.Cursor;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.cursor.CursorMode;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.key.Binding;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.runeutil.Sanitizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static com.williamcallahan.tui4j.compat.bubbletea.Command.batch;

/**
 * Multi-line text editor bubble.
 * <p>
 * Port of `bubbles/textarea`.
 * Supports cursor movement, insertion, deletion, and line wrapping.
 */
public class Textarea implements Model {

    public static class Style {
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style base;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLine;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLineNumber;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style endOfBuffer;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style lineNumber;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style placeholder;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style prompt;
        private com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style text;

        public Style() {
            this.base = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.cursorLine = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.cursorLineNumber = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.endOfBuffer = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.lineNumber = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.placeholder = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.prompt = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
            this.text = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
        }

        public Style base(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style base) {
            this.base = base;
            return this;
        }

        public Style cursorLine(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLine) {
            this.cursorLine = cursorLine;
            return this;
        }

        public Style cursorLineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLineNumber) {
            this.cursorLineNumber = cursorLineNumber;
            return this;
        }

        public Style endOfBuffer(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style endOfBuffer) {
            this.endOfBuffer = endOfBuffer;
            return this;
        }

        public Style lineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Style placeholder(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Style prompt(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style prompt) {
            this.prompt = prompt;
            return this;
        }

        public Style text(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style text) {
            this.text = text;
            return this;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style base() {
            return base;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLine() {
            return cursorLine;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style cursorLineNumber() {
            return cursorLineNumber;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style endOfBuffer() {
            return endOfBuffer;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style lineNumber() {
            return lineNumber;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style placeholder() {
            return placeholder;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style prompt() {
            return prompt;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style text() {
            return text;
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedCursorLine() {
            return cursorLine.inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedCursorLineNumber() {
            return cursorLineNumber.inherit(cursorLine).inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedEndOfBuffer() {
            return endOfBuffer.inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedLineNumber() {
            return lineNumber.inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedPlaceholder() {
            return placeholder.inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedPrompt() {
            return prompt.inherit(base).inline(true);
        }

        public com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style computedText() {
            return text.inherit(base).inline(true);
        }
    }

    public static class KeyMap {
        private Binding characterBackward;
        private Binding characterForward;
        private Binding deleteAfterCursor;
        private Binding deleteBeforeCursor;
        private Binding deleteCharacterBackward;
        private Binding deleteCharacterForward;
        private Binding deleteWordBackward;
        private Binding deleteWordForward;
        private Binding insertNewline;
        private Binding lineEnd;
        private Binding lineNext;
        private Binding linePrevious;
        private Binding lineStart;
        private Binding paste;
        private Binding wordBackward;
        private Binding wordForward;
        private Binding inputBegin;
        private Binding inputEnd;
        private Binding uppercaseWordForward;
        private Binding lowercaseWordForward;
        private Binding capitalizeWordForward;
        private Binding transposeCharacterBackward;

        public KeyMap() {
            this.characterBackward = new Binding(Binding.withKeys("left", "ctrl+b"), Binding.withHelp("left", "character backward"));
            this.characterForward = new Binding(Binding.withKeys("right", "ctrl+f"), Binding.withHelp("right", "character forward"));
            this.wordForward = new Binding(Binding.withKeys("alt+right", "alt+f"), Binding.withHelp("alt+right", "word forward"));
            this.wordBackward = new Binding(Binding.withKeys("alt+left", "alt+b"), Binding.withHelp("alt+left", "word backward"));
            this.lineNext = new Binding(Binding.withKeys("down", "ctrl+n"), Binding.withHelp("down", "next line"));
            this.linePrevious = new Binding(Binding.withKeys("up", "ctrl+p"), Binding.withHelp("up", "previous line"));
            this.deleteWordBackward = new Binding(Binding.withKeys("alt+backspace", "ctrl+w"), Binding.withHelp("alt+backspace", "delete word backward"));
            this.deleteWordForward = new Binding(Binding.withKeys("alt+delete", "alt+d"), Binding.withHelp("alt+delete", "delete word forward"));
            this.deleteAfterCursor = new Binding(Binding.withKeys("ctrl+k"), Binding.withHelp("ctrl+k", "delete after cursor"));
            this.deleteBeforeCursor = new Binding(Binding.withKeys("ctrl+u"), Binding.withHelp("ctrl+u", "delete before cursor"));
            this.insertNewline = new Binding(Binding.withKeys("enter", "ctrl+m"), Binding.withHelp("enter", "insert newline"));
            this.deleteCharacterBackward = new Binding(Binding.withKeys("backspace", "ctrl+h"), Binding.withHelp("backspace", "delete character backward"));
            this.deleteCharacterForward = new Binding(Binding.withKeys("delete", "ctrl+d"), Binding.withHelp("delete", "delete character forward"));
            this.lineStart = new Binding(Binding.withKeys("home", "ctrl+a"), Binding.withHelp("home", "line start"));
            this.lineEnd = new Binding(Binding.withKeys("end", "ctrl+e"), Binding.withHelp("end", "line end"));
            this.paste = new Binding(Binding.withKeys("ctrl+v"), Binding.withHelp("ctrl+v", "paste"));
            this.inputBegin = new Binding(Binding.withKeys("alt+<", "ctrl+home"), Binding.withHelp("alt+<", "input begin"));
            this.inputEnd = new Binding(Binding.withKeys("alt+>", "ctrl+end"), Binding.withHelp("alt+>", "input end"));
            this.capitalizeWordForward = new Binding(Binding.withKeys("alt+c"), Binding.withHelp("alt+c", "capitalize word forward"));
            this.lowercaseWordForward = new Binding(Binding.withKeys("alt+l"), Binding.withHelp("alt+l", "lowercase word forward"));
            this.uppercaseWordForward = new Binding(Binding.withKeys("alt+u"), Binding.withHelp("alt+u", "uppercase word forward"));
            this.transposeCharacterBackward = new Binding(Binding.withKeys("ctrl+t"), Binding.withHelp("ctrl+t", "transpose character backward"));
        }

        public Binding characterBackward() {
            return characterBackward;
        }

        public Binding characterForward() {
            return characterForward;
        }

        public Binding deleteAfterCursor() {
            return deleteAfterCursor;
        }

        public Binding deleteBeforeCursor() {
            return deleteBeforeCursor;
        }

        public Binding deleteCharacterBackward() {
            return deleteCharacterBackward;
        }

        public Binding deleteCharacterForward() {
            return deleteCharacterForward;
        }

        public Binding deleteWordBackward() {
            return deleteWordBackward;
        }

        public Binding deleteWordForward() {
            return deleteWordForward;
        }

        public Binding insertNewline() {
            return insertNewline;
        }

        public Binding lineEnd() {
            return lineEnd;
        }

        public Binding lineNext() {
            return lineNext;
        }

        public Binding linePrevious() {
            return linePrevious;
        }

        public Binding lineStart() {
            return lineStart;
        }

        public Binding paste() {
            return paste;
        }

        public Binding wordBackward() {
            return wordBackward;
        }

        public Binding wordForward() {
            return wordForward;
        }

        public Binding inputBegin() {
            return inputBegin;
        }

        public Binding inputEnd() {
            return inputEnd;
        }

        public Binding uppercaseWordForward() {
            return uppercaseWordForward;
        }

        public Binding lowercaseWordForward() {
            return lowercaseWordForward;
        }

        public Binding capitalizeWordForward() {
            return capitalizeWordForward;
        }

        public Binding transposeCharacterBackward() {
            return transposeCharacterBackward;
        }
    }

    public static class LineInfo {
        private int width;
        private int charWidth;
        private int height;
        private int startColumn;
        private int columnOffset;
        private int rowOffset;
        private int charOffset;

        public LineInfo() {
        }

        public LineInfo(int width, int charWidth, int height, int startColumn, int columnOffset, int rowOffset, int charOffset) {
            this.width = width;
            this.charWidth = charWidth;
            this.height = height;
            this.startColumn = startColumn;
            this.columnOffset = columnOffset;
            this.rowOffset = rowOffset;
            this.charOffset = charOffset;
        }

        public int width() {
            return width;
        }

        public int charWidth() {
            return charWidth;
        }

        public int height() {
            return height;
        }

        public int startColumn() {
            return startColumn;
        }

        public int columnOffset() {
            return columnOffset;
        }

        public int rowOffset() {
            return rowOffset;
        }

        public int charOffset() {
            return charOffset;
        }

        public LineInfo width(int width) {
            this.width = width;
            return this;
        }

        public LineInfo charWidth(int charWidth) {
            this.charWidth = charWidth;
            return this;
        }

        public LineInfo height(int height) {
            this.height = height;
            return this;
        }

        public LineInfo startColumn(int startColumn) {
            this.startColumn = startColumn;
            return this;
        }

        public LineInfo columnOffset(int columnOffset) {
            this.columnOffset = columnOffset;
            return this;
        }

        public LineInfo rowOffset(int rowOffset) {
            this.rowOffset = rowOffset;
            return this;
        }

        public LineInfo charOffset(int charOffset) {
            this.charOffset = charOffset;
            return this;
        }
    }

    private static final int MIN_HEIGHT = 1;
    private static final int DEFAULT_HEIGHT = 6;
    private static final int DEFAULT_WIDTH = 40;
    private static final int DEFAULT_CHAR_LIMIT = 0;
    private static final int DEFAULT_MAX_HEIGHT = 99;
    private static final int DEFAULT_MAX_WIDTH = 500;
    private static final int MAX_LINES = 10000;

    private String prompt;
    private String placeholder;
    private boolean showLineNumbers;
    private char endOfBufferCharacter;
    private KeyMap keyMap;
    private Style focusedStyle;
    private Style blurredStyle;
    private Style style;
    private Cursor cursor;
    private int charLimit;
    private int maxHeight;
    private int maxWidth;
    private int promptWidth;
    private int width;
    private int height;
    private List<char[]> value;
    private boolean focus;
    private int col;
    private int row;
    private int lastCharOffset;
    private Sanitizer sanitizer;
    private int lineNumberWidth;

    public Textarea() {
        this.prompt = "| ";
        this.placeholder = "";
        this.showLineNumbers = true;
        this.endOfBufferCharacter = ' ';
        this.keyMap = new KeyMap();
        this.focusedStyle = defaultStyles(true);
        this.blurredStyle = defaultStyles(false);
        this.style = blurredStyle;
        this.cursor = new Cursor();
        this.charLimit = DEFAULT_CHAR_LIMIT;
        this.maxHeight = DEFAULT_MAX_HEIGHT;
        this.maxWidth = DEFAULT_MAX_WIDTH;
        this.promptWidth = TextWidth.measureCellWidth(prompt);
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
        this.value = new ArrayList<>();
        this.value.add(new char[0]);
        this.focus = false;
        this.col = 0;
        this.row = 0;
        this.lastCharOffset = 0;
        this.sanitizer = new Sanitizer(
                Sanitizer.replaceTabs("    "),
                Sanitizer.replaceNewlines(" ")
        );
        this.lineNumberWidth = 4;
    }

    private static Textarea.Style defaultStyles(boolean focused) {
        Textarea.Style style = new Textarea.Style();
        style.base(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle());
        if (focused) {
            style.cursorLine(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().background(new AdaptiveColor("255", "0")));
            style.cursorLineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("240", "240")));
            style.endOfBuffer(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("254", "0")));
            style.lineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("249", "7")));
            style.placeholder(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(Color.color("240")));
            style.prompt(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(Color.color("7")));
            style.text(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle());
        } else {
            style.cursorLine(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("245", "7")));
            style.cursorLineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("249", "7")));
            style.endOfBuffer(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("254", "0")));
            style.lineNumber(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("249", "7")));
            style.placeholder(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(Color.color("240")));
            style.prompt(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(Color.color("7")));
            style.text(com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle().foreground(new AdaptiveColor("245", "7")));
        }
        return style;
    }

    @Override
    public Command init() {
        return null;
    }

    /**
     * Updates the model based on key events or command messages.
     * Handles cursor blink and text mutations.
     */
    @Override
    public UpdateResult<Textarea> update(Message msg) {
        if (!focus) {
            cursor.blur();
            return UpdateResult.from(this);
        }

        int oldRow = cursorLineNumber();
        int oldCol = col;

        if (value.get(row) == null) {
            value.set(row, new char[0]);
        }

        if (msg instanceof KeyPressMessage keyPressMessage) {
            handleKeyPress(keyPressMessage);
        }

        List<Command> commands = new LinkedList<>();

        UpdateResult<Cursor> cursorUpdateResult = cursor.update(msg);
        this.cursor = cursorUpdateResult.model();
        commands.add(cursorUpdateResult.command());

        if ((cursorLineNumber() != oldRow || col != oldCol) && cursor.mode() == CursorMode.Blink) {
            cursor.setBlink(false);
            commands.add(cursor.blinkCommand());
        }

        return UpdateResult.from(this, batch(commands.toArray(new Command[0])));
    }

    private void handleKeyPress(KeyPressMessage keyPressMessage) {
        if (handleEditing(keyPressMessage)) {
            return;
        }
        if (handleNavigation(keyPressMessage)) {
            return;
        }
        if (handleTransform(keyPressMessage)) {
            return;
        }
        insertRunesFromUserInput(keyPressMessage.runes());
    }

    private boolean handleEditing(KeyPressMessage keyPressMessage) {
        if (Binding.matches(keyPressMessage, keyMap.deleteAfterCursor())) {
            handleDeleteAfterCursor();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.deleteBeforeCursor())) {
            handleDeleteBeforeCursor();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.deleteCharacterBackward())) {
            handleDeleteCharacterBackward();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.deleteCharacterForward())) {
            handleDeleteCharacterForward();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.deleteWordBackward())) {
            handleDeleteWordBackward();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.deleteWordForward())) {
            handleDeleteWordForward();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.insertNewline())) {
            handleInsertNewline();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.paste())) {
            insertRunesFromUserInput(keyPressMessage.runes());
            return true;
        }
        return false;
    }

    private boolean handleNavigation(KeyPressMessage keyPressMessage) {
        if (Binding.matches(keyPressMessage, keyMap.lineEnd())) {
            cursorEnd();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.lineStart())) {
            cursorStart();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.characterForward())) {
            characterRight();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.lineNext())) {
            cursorDown();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.wordForward())) {
            wordRight();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.characterBackward())) {
            characterLeft(false);
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.linePrevious())) {
            cursorUp();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.wordBackward())) {
            wordLeft();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.inputBegin())) {
            moveToBegin();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.inputEnd())) {
            moveToEnd();
            return true;
        }
        return false;
    }

    private boolean handleTransform(KeyPressMessage keyPressMessage) {
        if (Binding.matches(keyPressMessage, keyMap.lowercaseWordForward())) {
            lowercaseRight();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.uppercaseWordForward())) {
            uppercaseRight();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.capitalizeWordForward())) {
            capitalizeRight();
            return true;
        } else if (Binding.matches(keyPressMessage, keyMap.transposeCharacterBackward())) {
            transposeLeft();
            return true;
        }
        return false;
    }

    private void handleInsertNewline() {
        if (maxHeight > 0 && value.size() >= maxHeight) {
            return;
        }
        col = clamp(col, 0, value.get(row).length);
        splitLine(row, col);
    }

    private void handleDeleteWordForward() {
        col = clamp(col, 0, value.get(row).length);
        if (col >= value.get(row).length) {
            mergeLineBelow(row);
        } else {
            deleteWordRight();
        }
    }

    private void handleDeleteWordBackward() {
        if (col <= 0) {
            mergeLineAbove(row);
        } else {
            deleteWordLeft();
        }
    }

    private void handleDeleteCharacterForward() {
        if (value.get(row).length > 0 && col < value.get(row).length) {
            char[] newLine = new char[value.get(row).length - 1];
            System.arraycopy(value.get(row), 0, newLine, 0, col);
            System.arraycopy(value.get(row), col + 1, newLine, col, value.get(row).length - col - 1);
            value.set(row, newLine);
        }
        if (col >= value.get(row).length) {
            mergeLineBelow(row);
        }
    }

    private void handleDeleteCharacterBackward() {
        col = clamp(col, 0, value.get(row).length);
        if (col <= 0) {
            mergeLineAbove(row);
        } else if (value.get(row).length > 0) {
            char[] newLine = new char[max(0, value.get(row).length - 1)];
            System.arraycopy(value.get(row), 0, newLine, 0, max(0, col - 1));
            System.arraycopy(value.get(row), col, newLine, max(0, col - 1), value.get(row).length - col);
            value.set(row, newLine);
            if (col > 0) {
                setCursor(col - 1);
            }
        }
    }

    private void handleDeleteBeforeCursor() {
        col = clamp(col, 0, value.get(row).length);
        if (col <= 0) {
            mergeLineAbove(row);
        } else {
            deleteBeforeCursor();
        }
    }

    private void handleDeleteAfterCursor() {
        col = clamp(col, 0, value.get(row).length);
        if (col >= value.get(row).length) {
            mergeLineBelow(row);
        } else {
            deleteAfterCursor();
        }
    }

    @Override
    public String view() {
        if (value() == null || value().isEmpty()) {
            return placeholderView();
        }

        cursor.resetTextStyle();
        cursor.setTextStyle(style.computedCursorLine());

        StringBuilder sb = new StringBuilder();

        int displayLine = 0;
        for (int l = 0; l < value.size(); l++) {
            char[] line = value.get(l);
            List<char[]> wrappedLines = wrap(Arrays.copyOf(line, line.length), width);

            for (int wl = 0; wl < wrappedLines.size(); wl++) {
                char[] wrappedLine = wrappedLines.get(wl);

                String promptStr = getPromptString(displayLine);
                sb.append(style.computedPrompt().render(promptStr));
                displayLine++;

                if (showLineNumbers) {
                    String lineNum;
                    if (wl == 0) {
                        lineNum = formatLineNumber(l + 1);
                    } else {
                        lineNum = " ";
                    }
                    if (row == l) {
                        sb.append(style.computedCursorLine().render(
                            style.computedCursorLineNumber().render(lineNum)));
                    } else {
                        sb.append(style.computedLineNumber().render(lineNum));
                    }
                }

                int lineWidth = TextWidth.measureCellWidth(new String(wrappedLine));
                int padding = max(0, width - lineWidth);

                if (row == l) {
                    LineInfo lineInfo = lineInfo();
                    sb.append(style.computedText().render(
                        new String(Arrays.copyOf(wrappedLine, lineInfo.columnOffset()))));

                    String charAfterCursor = "";
                    if (lineInfo.columnOffset() < wrappedLine.length) {
                        charAfterCursor = new String(new char[]{wrappedLine[lineInfo.columnOffset()]});
                    }
                    cursor.setChar(charAfterCursor);
                    sb.append(cursor.view());

                    if (lineInfo.columnOffset() + 1 < wrappedLine.length) {
                        sb.append(style.computedText().render(
                            new String(Arrays.copyOfRange(wrappedLine, lineInfo.columnOffset() + 1, wrappedLine.length))));
                    }
                } else {
                    sb.append(style.computedText().render(new String(wrappedLine)));
                }

                sb.append(" ".repeat(padding));
                sb.append('\n');
            }
        }

        for (int i = 0; i < height; i++) {
            String promptStr = getPromptString(displayLine);
            sb.append(style.computedPrompt().render(promptStr));
            displayLine++;

            String eob = style.computedEndOfBuffer().render(String.valueOf(endOfBufferCharacter));
            int rightGapWidth = max(0, width - 1 + lineNumberWidth);
            String rightGap = " ".repeat(rightGapWidth);
            sb.append(eob).append(rightGap);
            sb.append('\n');
        }

        return style.base().render(sb.toString());
    }

    private String placeholderView() {
        if (placeholder == null || placeholder.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String[] placeholderLines = placeholder.split("\n");

        for (int i = 0; i < height; i++) {
            String promptStr = getPromptString(i);
            sb.append(style.computedPrompt().render(promptStr));

            if (showLineNumbers) {
                String lineNum = i < placeholderLines.length ? formatLineNumber(i + 1) : " ";
                if (i == 0) {
                    sb.append(style.computedCursorLine().render(
                        style.computedCursorLineNumber().render(lineNum)));
                } else if (i < placeholderLines.length) {
                    sb.append(style.computedLineNumber().render(lineNum));
                } else {
                    sb.append(style.computedLineNumber().render(" "));
                }
            }

            if (i == 0 && placeholderLines.length > 0) {
                cursor.resetTextStyle();
                cursor.setTextStyle(style.computedPlaceholder());
                if (placeholderLines[0].length() > 0) {
                    char firstChar = placeholderLines[0].charAt(0);
                    cursor.setChar(String.valueOf(firstChar));
                    sb.append(cursor.view());
                    sb.append(style.computedPlaceholder().render(
                        placeholderLines[0].substring(1)));
                }
            } else if (i < placeholderLines.length) {
                int lineWidth = TextWidth.measureCellWidth(placeholderLines[i]);
                int padding = max(0, width - lineWidth);
                sb.append(style.computedPlaceholder().render(placeholderLines[i]));
                sb.append(" ".repeat(padding));
            } else {
                sb.append(style.computedEndOfBuffer().render(String.valueOf(endOfBufferCharacter)));
            }

            sb.append('\n');
        }

        return style.base().render(sb.toString());
    }

    private String getPromptString(int displayLine) {
        return prompt;
    }

    private String formatLineNumber(int num) {
        int digits = String.valueOf(maxHeight).length();
        return String.format(" %" + digits + "d ", num);
    }

    private void insertRunesFromUserInput(char[] runes) {
        char[] paste = sanitizer.sanitize(runes);

        if (charLimit > 0) {
            int availSpace = charLimit - length();
            if (availSpace <= 0) {
                return;
            }
            if (availSpace < paste.length) {
                paste = Arrays.copyOfRange(paste, 0, availSpace);
            }
        }

        List<char[]> lines = new ArrayList<>();
        int lstart = 0;
        for (int i = 0; i < paste.length; i++) {
            if (paste[i] == '\n') {
                lines.add(Arrays.copyOfRange(paste, lstart, i));
                lstart = i + 1;
            }
        }
        if (lstart <= paste.length) {
            lines.add(Arrays.copyOfRange(paste, lstart, paste.length));
        }

        if (MAX_LINES > 0 && value.size() + lines.size() - 1 > MAX_LINES) {
            int allowedHeight = max(0, MAX_LINES - value.size() + 1);
            while (lines.size() > allowedHeight) {
                lines.remove(lines.size() - 1);
            }
        }

        if (lines.isEmpty()) {
            return;
        }

        char[] tail = Arrays.copyOfRange(value.get(row), col, value.get(row).length);

        char[] newCurrentLine = new char[col + lines.get(0).length];
        System.arraycopy(value.get(row), 0, newCurrentLine, 0, col);
        System.arraycopy(lines.get(0), 0, newCurrentLine, col, lines.get(0).length);
        value.set(row, newCurrentLine);
        col += lines.get(0).length;

        if (lines.size() > 1) {
            int numExtraLines = lines.size() - 1;

            List<char[]> newValue = new ArrayList<>(value.size() + numExtraLines);
            newValue.addAll(value.subList(0, row + 1));

            for (int i = 1; i < lines.size(); i++) {
                row++;
                newValue.add(lines.get(i));
            }

            newValue.addAll(value.subList(row, value.size()));
            value = newValue;

            for (int i = 1; i < lines.size(); i++) {
                row++;
                col = lines.get(i).length;
            }
        }

        char[] finalLine = new char[value.get(row).length + tail.length];
        System.arraycopy(value.get(row), 0, finalLine, 0, value.get(row).length);
        System.arraycopy(tail, 0, finalLine, value.get(row).length, tail.length);
        value.set(row, finalLine);

        setCursor(col);
    }

    private void setCursor(int newCol) {
        col = clamp(newCol, 0, value.get(row).length);
        lastCharOffset = 0;
    }

    private void cursorStart() {
        setCursor(0);
    }

    private void cursorEnd() {
        setCursor(value.get(row).length);
    }

    private void cursorDown() {
        LineInfo li = lineInfo();
        int charOffset = max(lastCharOffset, li.charOffset());
        lastCharOffset = charOffset;

        if (li.rowOffset() + 1 >= li.height() && row < value.size() - 1) {
            row++;
            col = 0;
        } else {
            int trailingSpace = 2;
            col = min(li.startColumn() + li.width() + trailingSpace, value.get(row).length - 1);
        }

        LineInfo nli = lineInfo();
        col = nli.startColumn();

        if (nli.width() <= 0) {
            return;
        }

        int offset = 0;
        while (offset < charOffset) {
            if (row >= value.size() || col >= value.get(row).length || offset >= nli.charWidth() - 1) {
                break;
            }
            offset += TextWidth.measureCellWidth(String.valueOf(value.get(row)[col]));
            col++;
        }
    }

    private void cursorUp() {
        LineInfo li = lineInfo();
        int charOffset = max(lastCharOffset, li.charOffset());
        lastCharOffset = charOffset;

        if (li.rowOffset() <= 0 && row > 0) {
            row--;
            col = value.get(row).length;
        } else {
            int trailingSpace = 2;
            col = li.startColumn() - trailingSpace;
        }

        LineInfo nli = lineInfo();
        col = nli.startColumn();

        if (nli.width() <= 0) {
            return;
        }

        int offset = 0;
        while (offset < charOffset) {
            if (col >= value.get(row).length || offset >= nli.charWidth() - 1) {
                break;
            }
            offset += TextWidth.measureCellWidth(String.valueOf(value.get(row)[col]));
            col++;
        }
    }

    private void characterRight() {
        if (col < value.get(row).length) {
            setCursor(col + 1);
        } else {
            if (row < value.size() - 1) {
                row++;
                cursorStart();
            }
        }
    }

    private void characterLeft(boolean insideLine) {
        if (col == 0 && row != 0) {
            row--;
            cursorEnd();
            if (!insideLine) {
                return;
            }
        }
        if (col > 0) {
            setCursor(col - 1);
        }
    }

    private void wordLeft() {
        while (true) {
            characterLeft(true);
            if (col < value.get(row).length && !UCharacter.isWhitespace(value.get(row)[col])) {
                break;
            }
        }

        while (col > 0) {
            if (UCharacter.isWhitespace(value.get(row)[col - 1])) {
                break;
            }
            setCursor(col - 1);
        }
    }

    private void wordRight() {
        while (col >= value.get(row).length || UCharacter.isWhitespace(value.get(row)[col])) {
            if (row == value.size() - 1 && col == value.get(row).length) {
                break;
            }
            characterRight();
        }

        while (col < value.get(row).length && !UCharacter.isWhitespace(value.get(row)[col])) {
            setCursor(col + 1);
        }
    }

    private void deleteBeforeCursor() {
        value.set(row, Arrays.copyOfRange(value.get(row), col, value.get(row).length));
        setCursor(0);
    }

    private void deleteAfterCursor() {
        value.set(row, Arrays.copyOfRange(value.get(row), 0, col));
        setCursor(value.get(row).length);
    }

    private void deleteWordLeft() {
        if (col == 0 || value.get(row).length == 0) {
            return;
        }

        int oldCol = col;
        setCursor(col - 1);
        while (col > 0 && UCharacter.isWhitespace(value.get(row)[col])) {
            if (col <= 0) {
                break;
            }
            setCursor(col - 1);
        }

        while (col > 0) {
            if (!UCharacter.isWhitespace(value.get(row)[col])) {
                setCursor(col - 1);
            } else {
                if (col > 0) {
                    setCursor(col + 1);
                }
                break;
            }
        }

        if (oldCol > value.get(row).length) {
            value.set(row, Arrays.copyOfRange(value.get(row), 0, col));
        } else {
            char[] newLine = new char[col + (value.get(row).length - oldCol)];
            System.arraycopy(value.get(row), 0, newLine, 0, col);
            System.arraycopy(value.get(row), oldCol, newLine, col, value.get(row).length - oldCol);
            value.set(row, newLine);
        }
    }

    private void deleteWordRight() {
        if (col >= value.get(row).length || value.get(row).length == 0) {
            return;
        }

        int oldCol = col;

        while (col < value.get(row).length && UCharacter.isWhitespace(value.get(row)[col])) {
            setCursor(col + 1);
        }

        while (col < value.get(row).length) {
            if (!UCharacter.isWhitespace(value.get(row)[col])) {
                setCursor(col + 1);
            } else {
                break;
            }
        }

        if (col > value.get(row).length) {
            value.set(row, Arrays.copyOfRange(value.get(row), 0, oldCol));
        } else {
            char[] newLine = new char[oldCol + (value.get(row).length - col)];
            System.arraycopy(value.get(row), 0, newLine, 0, oldCol);
            System.arraycopy(value.get(row), col, newLine, oldCol, value.get(row).length - col);
            value.set(row, newLine);
        }

        setCursor(oldCol);
    }

    private void splitLine(int splitRow, int splitCol) {
        char[] line = value.get(splitRow);
        char[] head = Arrays.copyOfRange(line, 0, splitCol);
        char[] tail = Arrays.copyOfRange(line, splitCol, line.length);

        value.add(splitRow + 1, tail);
        value.set(splitRow, head);

        col = 0;
        row++;
    }

    private void mergeLineBelow(int mergeRow) {
        if (mergeRow >= value.size() - 1) {
            return;
        }

        char[] newLine = new char[value.get(mergeRow).length + value.get(mergeRow + 1).length];
        System.arraycopy(value.get(mergeRow), 0, newLine, 0, value.get(mergeRow).length);
        System.arraycopy(value.get(mergeRow + 1), 0, newLine, value.get(mergeRow).length, value.get(mergeRow + 1).length);
        value.set(mergeRow, newLine);

        value.remove(mergeRow + 1);
    }

    private void mergeLineAbove(int mergeRow) {
        if (mergeRow <= 0) {
            return;
        }

        col = value.get(mergeRow - 1).length;
        row = mergeRow - 1;

        char[] newLine = new char[value.get(mergeRow - 1).length + value.get(mergeRow).length];
        System.arraycopy(value.get(mergeRow - 1), 0, newLine, 0, value.get(mergeRow - 1).length);
        System.arraycopy(value.get(mergeRow), 0, newLine, value.get(mergeRow - 1).length, value.get(mergeRow).length);
        value.set(mergeRow - 1, newLine);

        value.remove(mergeRow);
    }

    private void transposeLeft() {
        if (col == 0 || value.get(row).length < 2) {
            return;
        }
        if (col >= value.get(row).length) {
            setCursor(col - 1);
        }
        char temp = value.get(row)[col - 1];
        value.get(row)[col - 1] = value.get(row)[col];
        value.get(row)[col] = temp;
        if (col < value.get(row).length) {
            setCursor(col + 1);
        }
    }

    private void uppercaseRight() {
        doWordRight((charIdx, i) -> {
            value.get(row)[i] = Character.toUpperCase(value.get(row)[i]);
        });
    }

    private void lowercaseRight() {
        doWordRight((charIdx, i) -> {
            value.get(row)[i] = Character.toLowerCase(value.get(row)[i]);
        });
    }

    private void capitalizeRight() {
        doWordRight((charIdx, i) -> {
            if (charIdx == 0) {
                value.get(row)[i] = Character.toTitleCase(value.get(row)[i]);
            }
        });
    }

    private void doWordRight(WordConsumer consumer) {
        while (col >= value.get(row).length || UCharacter.isWhitespace(value.get(row)[col])) {
            if (row == value.size() - 1 && col == value.get(row).length) {
                break;
            }
            characterRight();
        }

        int charIdx = 0;
        while (col < value.get(row).length && !UCharacter.isWhitespace(value.get(row)[col])) {
            consumer.accept(charIdx, col);
            setCursor(col + 1);
            charIdx++;
        }
    }

    @FunctionalInterface
    private interface WordConsumer {
        void accept(int charIdx, int pos);
    }

    private void moveToBegin() {
        row = 0;
        setCursor(0);
    }

    private void moveToEnd() {
        row = value.size() - 1;
        setCursor(value.get(row).length);
    }

    public LineInfo lineInfo() {
        List<char[]> grid = wrap(value.get(row), width);

        int counter = 0;
        for (int i = 0; i < grid.size(); i++) {
            char[] line = grid.get(i);
            if (counter + line.length == col && i + 1 < grid.size()) {
                return new LineInfo()
                        .charOffset(0)
                        .columnOffset(0)
                        .height(grid.size())
                        .rowOffset(i + 1)
                        .startColumn(col)
                        .width(grid.get(i + 1).length)
                        .charWidth(TextWidth.measureCellWidth(new String(line)));
            }

            if (counter + line.length >= col) {
                String lineStr = new String(line);
                int charOffset = TextWidth.measureCellWidth(lineStr.substring(0, max(0, col - counter)));
                return new LineInfo()
                        .charOffset(charOffset)
                        .columnOffset(col - counter)
                        .height(grid.size())
                        .rowOffset(i)
                        .startColumn(counter)
                        .width(line.length)
                        .charWidth(TextWidth.measureCellWidth(lineStr));
            }

            counter += line.length;
        }
        return new LineInfo();
    }

    private int cursorLineNumber() {
        int line = 0;
        for (int i = 0; i < row; i++) {
            line += wrap(value.get(i), width).size();
        }
        line += lineInfo().rowOffset();
        return line;
    }

    private List<char[]> wrap(char[] runes, int maxWidth) {
        if (maxWidth <= 0) {
            return List.of(runes);
        }

        List<char[]> lines = new ArrayList<>();
        lines.add(new char[0]);

        List<Character> word = new ArrayList<>();
        int spaces = 0;

        for (char r : runes) {
            if (UCharacter.isWhitespace(r)) {
                spaces++;
            } else {
                word.add(r);
            }

            char[] wordArr = new char[word.size()];
            for (int i = 0; i < word.size(); i++) {
                wordArr[i] = word.get(i);
            }

            if (spaces > 0) {
                int currentLineWidth = TextWidth.measureCellWidth(new String(lines.get(lines.size() - 1)));
                int wordWidth = TextWidth.measureCellWidth(new String(wordArr));
                if (currentLineWidth + wordWidth + spaces > maxWidth) {
                    lines.add(wordArr);
                    char[] spaceArr = new char[spaces];
                    Arrays.fill(spaceArr, ' ');
                    lines.add(spaceArr);
                    word.clear();
                    spaces = 0;
                } else {
                    char[] lastLine = new char[lines.get(lines.size() - 1).length + wordArr.length + spaces];
                    System.arraycopy(lines.get(lines.size() - 1), 0, lastLine, 0, lines.get(lines.size() - 1).length);
                    System.arraycopy(wordArr, 0, lastLine, lines.get(lines.size() - 1).length, wordArr.length);
                    Arrays.fill(lastLine, lines.get(lines.size() - 1).length + wordArr.length, lastLine.length, ' ');
                    lines.set(lines.size() - 1, lastLine);
                    word.clear();
                    spaces = 0;
                }
            } else {
                char[] lastLine = lines.get(lines.size() - 1);
                char lastChar = lastLine.length > 0 ? lastLine[lastLine.length - 1] : ' ';
                int lastCharWidth = TextWidth.measureCellWidth(String.valueOf(lastChar));
                int currentLineWidth = TextWidth.measureCellWidth(new String(lastLine));
                int wordWidth = TextWidth.measureCellWidth(new String(wordArr));

                if (currentLineWidth + wordWidth > maxWidth) {
                    if (lastLine.length > 0) {
                        lines.add(wordArr);
                    } else {
                        lines.set(lines.size() - 1, wordArr);
                    }
                    word.clear();
                } else {
                    char[] newLastLine = new char[lastLine.length + wordArr.length];
                    System.arraycopy(lastLine, 0, newLastLine, 0, lastLine.length);
                    System.arraycopy(wordArr, 0, newLastLine, lastLine.length, wordArr.length);
                    lines.set(lines.size() - 1, newLastLine);
                    word.clear();
                }
            }
        }

        if (!word.isEmpty() || spaces > 0) {
            char[] wordArr = new char[word.size()];
            for (int i = 0; i < word.size(); i++) {
                wordArr[i] = word.get(i);
            }

            int currentLineWidth = TextWidth.measureCellWidth(new String(lines.get(lines.size() - 1)));
            int wordWidth = TextWidth.measureCellWidth(new String(wordArr));
            if (currentLineWidth + wordWidth + spaces >= maxWidth) {
                lines.add(wordArr);
                char[] spaceArr = new char[spaces + 1];
                Arrays.fill(spaceArr, ' ');
                lines.add(spaceArr);
            } else {
                char[] lastLine = new char[lines.get(lines.size() - 1).length + wordArr.length + spaces + 1];
                System.arraycopy(lines.get(lines.size() - 1), 0, lastLine, 0, lines.get(lines.size() - 1).length);
                System.arraycopy(wordArr, 0, lastLine, lines.get(lines.size() - 1).length, wordArr.length);
                Arrays.fill(lastLine, lines.get(lines.size() - 1).length + wordArr.length, lastLine.length, ' ');
                lines.set(lines.size() - 1, lastLine);
            }
        }

        return lines;
    }

    public String value() {
        if (value == null || value.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            sb.append(value.get(i));
            if (i < value.size() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public void setValue(String value) {
        reset();
        insertString(value);
    }

    public void insertString(String s) {
        insertRunesFromUserInput(s.toCharArray());
    }

    public void insertRune(char r) {
        insertRunesFromUserInput(new char[]{r});
    }

    public int length() {
        int l = 0;
        for (char[] rowChars : value) {
            l += TextWidth.measureCellWidth(new String(rowChars));
        }
        return l + value.size() - 1;
    }

    public int lineCount() {
        return value.size();
    }

    public int line() {
        return row;
    }

    public void focus() {
        focus = true;
        style = focusedStyle;
        cursor.focus();
    }

    public void blur() {
        focus = false;
        style = blurredStyle;
        cursor.blur();
    }

    public boolean focused() {
        return focus;
    }

    public void reset() {
        value = new ArrayList<>();
        value.add(new char[0]);
        col = 0;
        row = 0;
        setCursor(0);
    }

    public void setWidth(int w) {
        this.width = w;
    }

    public void setHeight(int h) {
        if (maxHeight > 0) {
            this.height = clamp(h, MIN_HEIGHT, maxHeight);
        } else {
            this.height = max(h, MIN_HEIGHT);
        }
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
        this.promptWidth = TextWidth.measureCellWidth(prompt);
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public void setShowLineNumbers(boolean showLineNumbers) {
        this.showLineNumbers = showLineNumbers;
    }

    public void setCharLimit(int charLimit) {
        this.charLimit = charLimit;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setEndOfBufferCharacter(char endOfBufferCharacter) {
        this.endOfBufferCharacter = endOfBufferCharacter;
    }

    public Style style() {
        return style;
    }

    public Style focusedStyle() {
        return focusedStyle;
    }

    public Style blurredStyle() {
        return blurredStyle;
    }

    public Cursor cursor() {
        return cursor;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private static int clamp(int v, int low, int high) {
        return Math.max(low, Math.min(high, v));
    }
}
