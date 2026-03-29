package com.williamcallahan.tui4j.compat.bubbles.list;

import static com.williamcallahan.tui4j.compat.bubbles.list.ListTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.williamcallahan.tui4j.compat.bubbletea.QuitMessage;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Progressive tests for {@link List} combining UpdateResult assertions
 * with exact rendered view output. Tests build from simple init through
 * cursor movement, pagination, and quit handling.
 */
class ListBehaviorTest {

    @BeforeEach
    void setUp() {
        initTestEnvironment();
    }

    @Test
    void initialStateShowsFirstItemSelected() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));

        assertThat(list.cursor()).isZero();
        assertThat(list.index()).isZero();
        assertThat(list.selectedItem().filterValue()).isEqualTo("foo");
        assertThat(list.filterState()).isEqualTo(FilterState.Unfiltered);
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  3 items
                  
                > foo
                  bar
                  baz
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void cursorDownViaJKeyMovesToSecondItem() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));

        UpdateResult<List> result = list.update(runeKey('j'));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.cursor()).isEqualTo(1);
        assertThat(list.selectedItem().filterValue()).isEqualTo("bar");
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  3 items
                  
                  foo
                > bar
                  baz
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void consecutiveCursorDownsReachLastItem() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('j'));

        UpdateResult<List> result = list.update(runeKey('j'));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.cursor()).isEqualTo(2);
        assertThat(list.selectedItem().filterValue()).isEqualTo("baz");
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  3 items
                  
                  foo
                  bar
                > baz
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void cursorUpAfterDownReturnsToSecondItem() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('j'));
        applyMessage(list, runeKey('j'));

        UpdateResult<List> result = list.update(runeKey('k'));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.cursor()).isEqualTo(1);
        assertThat(list.selectedItem().filterValue()).isEqualTo("bar");
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  3 items
                  
                  foo
                > bar
                  baz
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void cursorUpAtTopClampedToFirstItem() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));

        applyMessage(list, runeKey('k'));

        assertThat(list.cursor()).isZero();
        assertThat(list.selectedItem().filterValue()).isEqualTo("foo");
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  3 items
                  
                > foo
                  bar
                  baz
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void cursorDownPastEndClampedToLastItem() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('j'));
        applyMessage(list, runeKey('j'));

        applyMessage(list, runeKey('j'));

        assertThat(list.cursor()).isEqualTo(2);
        assertThat(list.selectedItem().filterValue()).isEqualTo("baz");
    }

    @Test
    void paginatedInitialViewShowsFirstPage() {
        List list = createMinimalList(30, 2, items("a", "b", "c", "d"));

        assertThat(list.cursor()).isZero();
        assertThat(list.view()).isEqualTo(view("""
                > a
                  b"""));
    }

    @Test
    void nextPageViaRightArrowShowsSecondPage() {
        List list = createMinimalList(30, 2, items("a", "b", "c", "d"));

        UpdateResult<List> result = list.update(specialKey(KeyType.KeyRight));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.index()).isEqualTo(2);
        assertThat(list.selectedItem().filterValue()).isEqualTo("c");
        assertThat(list.view()).isEqualTo(view("""
                > c
                  d"""));
    }

    @Test
    void cursorDownAcrossPageBoundary() {
        List list = createMinimalList(30, 2, items("a", "b", "c", "d"));
        applyMessage(list, runeKey('j'));

        applyMessage(list, runeKey('j'));

        assertThat(list.index()).isEqualTo(2);
        assertThat(list.selectedItem().filterValue()).isEqualTo("c");
        assertThat(list.view()).isEqualTo(view("""
                > c
                  d"""));
    }

    @Test
    void prevPageReturnsToFirstPage() {
        List list = createMinimalList(30, 2, items("a", "b", "c", "d"));
        applyMessage(list, specialKey(KeyType.KeyRight));

        applyMessage(list, specialKey(KeyType.KeyLeft));

        assertThat(list.index()).isZero();
        assertThat(list.view()).isEqualTo(view("""
                > a
                  b"""));
    }

    @Test
    void emptyListShowsNoItemsView() {
        List list = createFullList(40, 12, items());

        assertThat(list.selectedItem()).isNull();
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  No items
                  
                No items.
                  
                  
                  
                  
                  
                  
                  q quit"""));
    }

    @Test
    void singleItemShowsSingularCount() {
        List list = createFullList(40, 12, items("only"));

        assertThat(list.selectedItem().filterValue()).isEqualTo("only");
        assertThat(list.view()).isEqualTo(view("""
                   List
                  
                  1 item
                  
                > only
                  
                  
                  
                  
                  
                  
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void quitKeyProducesQuitCommand() {
        List list = createFullList(40, 12, items("foo", "bar"));

        UpdateResult<List> result = list.update(runeKey('q'));

        assertThat(result.model()).isSameAs(list);
        assertThat(producesQuit(result.command())).isTrue();
    }

    @Test
    void forceQuitProducesDirectQuitMessage() {
        List list = createFullList(40, 12, items("foo", "bar"));

        UpdateResult<List> result = list.update(specialKey(KeyType.keyETX));

        assertThat(result.model()).isSameAs(list);
        assertThat(result.command().execute()).isInstanceOf(QuitMessage.class);
    }

    @Test
    void disabledQuitDoesNotProduceQuitCommand() {
        List list = createFullList(40, 12, items("foo", "bar"));
        list.disableQuitKeybindings();

        UpdateResult<List> result = list.update(runeKey('q'));

        assertThat(result.model()).isSameAs(list);
        assertThat(producesQuit(result.command())).isFalse();
    }
}
