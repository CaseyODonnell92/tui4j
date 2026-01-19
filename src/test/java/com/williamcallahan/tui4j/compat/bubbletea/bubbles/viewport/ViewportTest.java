package com.williamcallahan.tui4j.compat.bubbletea.bubbles.viewport;

import com.williamcallahan.tui4j.compat.bubbletea.message.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.Key;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyType;
import com.williamcallahan.tui4j.term.TerminalInfo;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.NoColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ViewportTest {

    @BeforeEach
    public void setup() {
        TerminalInfo.provide(() -> new TerminalInfo(false, new NoColor()));
    }

    @Test
    public void testNewViewport() {
        Viewport viewport = Viewport.create(80, 24);
        assertEquals(80, viewport.getWidth());
        assertEquals(24, viewport.getHeight());
        assertTrue(viewport.atTop());
        assertTrue(viewport.atBottom());
        assertEquals(1.0, viewport.scrollPercent());
    }

    @Test
    public void testSetContent() {
        Viewport viewport = Viewport.create(80, 10);
        String content = "line1\nline2\nline3\nline4\nline5";
        viewport.setContent(content);
        assertEquals(5, viewport.totalLineCount());
        assertTrue(viewport.atTop());
    }

    @Test
    public void testScrollDown() {
        Viewport viewport = Viewport.create(80, 2);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);
        assertTrue(viewport.atTop());

        viewport.scrollDown(1);
        assertEquals(1, viewport.getYOffset());
        assertFalse(viewport.atTop());
        assertFalse(viewport.atBottom());

        viewport.scrollDown(5);
        assertEquals(6, viewport.getYOffset());
    }

    @Test
    public void testScrollUp() {
        Viewport viewport = Viewport.create(80, 2);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.scrollDown(3);
        assertEquals(3, viewport.getYOffset());

        viewport.scrollUp(2);
        assertEquals(1, viewport.getYOffset());

        viewport.scrollUp(10);
        assertEquals(0, viewport.getYOffset());
        assertTrue(viewport.atTop());
    }

    @Test
    public void testPageDown() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        assertEquals(0, viewport.getYOffset());
        viewport.pageDown();
        assertEquals(3, viewport.getYOffset());

        viewport.pageDown();
        assertEquals(6, viewport.getYOffset());

        viewport.pageDown();
        assertEquals(7, viewport.getYOffset());
        assertTrue(viewport.atBottom());
    }

    @Test
    public void testPageUp() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.gotoBottom();
        int bottomOffset = viewport.getYOffset();

        viewport.pageUp();
        assertEquals(bottomOffset - 3, viewport.getYOffset());

        viewport.pageUp();
        assertEquals(bottomOffset - 6, viewport.getYOffset());
    }

    @Test
    public void testHalfPageDown() {
        Viewport viewport = Viewport.create(80, 4);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.halfPageDown();
        assertEquals(2, viewport.getYOffset());

        viewport.halfPageDown();
        assertEquals(4, viewport.getYOffset());
    }

    @Test
    public void testHalfPageUp() {
        Viewport viewport = Viewport.create(80, 4);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.scrollDown(5);
        assertEquals(5, viewport.getYOffset());

        viewport.halfPageUp();
        assertEquals(3, viewport.getYOffset());
    }

    @Test
    public void testGotoTop() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.scrollDown(5);
        assertEquals(5, viewport.getYOffset());

        viewport.gotoTop();
        assertEquals(0, viewport.getYOffset());
        assertTrue(viewport.atTop());
    }

    @Test
    public void testGotoBottom() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.gotoBottom();
        assertTrue(viewport.atBottom());
        assertTrue(viewport.pastBottom() || viewport.getYOffset() >= viewport.getMaxYOffset());
    }

    @Test
    public void testScrollPercent() {
        Viewport viewport = Viewport.create(80, 2);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        assertEquals(0.0, viewport.scrollPercent(), 0.01);

        viewport.scrollDown(2);
        assertEquals(0.25, viewport.scrollPercent(), 0.01);

        viewport.scrollDown(2);
        assertEquals(0.5, viewport.scrollPercent(), 0.01);

        viewport.gotoBottom();
        assertEquals(1.0, viewport.scrollPercent(), 0.01);
    }

    @Test
    public void testVisibleLineCount() {
        Viewport viewport = Viewport.create(80, 5);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        assertEquals(5, viewport.visibleLineCount());

        viewport.scrollDown(1);
        assertEquals(5, viewport.visibleLineCount());

        viewport.scrollDown(4);
        assertEquals(5, viewport.visibleLineCount());
    }

    @Test
    public void testAtTop() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        assertTrue(viewport.atTop());

        viewport.scrollDown(1);
        assertFalse(viewport.atTop());

        viewport.gotoTop();
        assertTrue(viewport.atTop());
    }

    @Test
    public void testAtBottom() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        assertFalse(viewport.atBottom());

        viewport.gotoBottom();
        assertTrue(viewport.atBottom());
    }

    @Test
    public void testContentShorterThanViewport() {
        Viewport viewport = Viewport.create(80, 10);
        String content = "line1\nline2\nline3";
        viewport.setContent(content);

        assertEquals(3, viewport.totalLineCount());
        assertTrue(viewport.atBottom());
        assertEquals(1.0, viewport.scrollPercent());
    }

    @Test
    public void testUpdateWithKeyBindings() {
        Viewport viewport = Viewport.create(80, 5);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        Key downKey = new Key(KeyType.KeyDown);
        KeyPressMessage downMessage = new KeyPressMessage(downKey);
        viewport.update(downMessage);
        assertEquals(1, viewport.getYOffset());

        Key upKey = new Key(KeyType.KeyUp);
        KeyPressMessage upMessage = new KeyPressMessage(upKey);
        viewport.update(upMessage);
        assertEquals(0, viewport.getYOffset());
    }

    @Test
    public void testHorizontalScrolling() {
        Viewport viewport = Viewport.create(10, 3);
        viewport.setHorizontalStep(3);
        String content = "This is a very long line that should be truncated";
        viewport.setContent(content);

        assertEquals(0, viewport.getXOffset());

        viewport.scrollRight(3);
        assertEquals(3, viewport.getXOffset());

        viewport.scrollLeft(3);
        assertEquals(0, viewport.getXOffset());
    }

    @Test
    public void testView() {
        Viewport viewport = Viewport.create(80, 5);
        viewport.setContent("line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10");

        String view = viewport.view();
        assertNotNull(view);
    }

    @Test
    public void testMouseWheelSettings() {
        Viewport viewport = Viewport.create(80, 10);

        assertTrue(viewport.isMouseWheelEnabled());
        assertEquals(3, viewport.getMouseWheelDelta());

        viewport.setMouseWheelEnabled(false);
        assertFalse(viewport.isMouseWheelEnabled());

        viewport.setMouseWheelDelta(5);
        assertEquals(5, viewport.getMouseWheelDelta());
    }

    @Test
    public void testScrollDownClamping() {
        Viewport viewport = Viewport.create(80, 5);
        String content = "line1\nline2\nline3\nline4\nline5";
        viewport.setContent(content);

        viewport.scrollDown(100);
        assertTrue(viewport.atBottom());
    }

    @Test
    public void testScrollUpClamping() {
        Viewport viewport = Viewport.create(80, 5);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        viewport.scrollUp(100);
        assertTrue(viewport.atTop());
        assertEquals(0, viewport.getYOffset());
    }

    @Test
    public void testMaxYOffsetCalculation() {
        Viewport viewport = Viewport.create(80, 3);
        String content = "line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10";
        viewport.setContent(content);

        int maxOffset = viewport.getMaxYOffset();
        assertEquals(7, maxOffset);
    }

    @Test
    public void testViewRendersContent() {
        Viewport viewport = Viewport.create(80, 5);
        viewport.setContent("line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10");

        String view = viewport.view();
        assertNotNull(view);
        assertTrue(view.length() > 0);
    }

    @Test
    public void testHorizontalScrollPercent() {
        Viewport viewport = Viewport.create(10, 3);
        String content = "This is a very long line that exceeds viewport width";
        viewport.setContent(content);

        assertTrue(viewport.horizontalScrollPercent() >= 0);
        assertTrue(viewport.horizontalScrollPercent() <= 1.0);

        viewport.setXOffset(5);
        assertTrue(viewport.horizontalScrollPercent() > 0);

        viewport.setXOffset(0);
        assertTrue(viewport.horizontalScrollPercent() >= 0);
    }

    @Test
    public void testPastBottom() {
        Viewport viewport = Viewport.create(80, 10);
        String content = "line1\nline2\nline3";
        viewport.setContent(content);

        assertFalse(viewport.pastBottom());
    }

    @Test
    public void testSetStyle() {
        Viewport viewport = Viewport.create(80, 5);
        assertNotNull(viewport.getStyle());

        com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style newStyle = com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style.newStyle();
        viewport.setStyle(newStyle);
        assertEquals(newStyle, viewport.getStyle());
    }

    @Test
    public void testHighPerformanceRendering() {
        Viewport viewport = Viewport.create(80, 5);

        assertFalse(viewport.isHighPerformanceRendering());

        viewport.setHighPerformanceRendering(true);
        assertTrue(viewport.isHighPerformanceRendering());

        String view = viewport.view();
        assertNotNull(view);
    }

    @Test
    public void testYPosition() {
        Viewport viewport = Viewport.create(80, 5);

        assertEquals(0, viewport.getYPosition());

        viewport.setYPosition(10);
        assertEquals(10, viewport.getYPosition());
    }

    @Test
    public void testKeyMap() {
        Viewport viewport = Viewport.create(80, 5);
        Viewport.KeyMap keyMap = viewport.getKeyMap();

        assertNotNull(keyMap);
        assertNotNull(keyMap.pageDown());
        assertNotNull(keyMap.pageUp());
        assertNotNull(keyMap.up());
        assertNotNull(keyMap.down());
    }

    @Test
    public void testViewWithNoContent() {
        Viewport viewport = Viewport.create(80, 5);
        String view = viewport.view();
        assertNotNull(view);
    }

    @Test
    public void testSetContentWithEmptyString() {
        Viewport viewport = Viewport.create(80, 5);
        viewport.setContent("");

        assertEquals(0, viewport.totalLineCount());
    }

    @Test
    public void testSetContentWithNull() {
        Viewport viewport = Viewport.create(80, 5);
        viewport.setContent(null);

        assertEquals(0, viewport.totalLineCount());
    }
}
