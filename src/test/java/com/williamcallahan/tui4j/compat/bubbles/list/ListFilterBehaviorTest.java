package com.williamcallahan.tui4j.compat.bubbles.list;

import static com.williamcallahan.tui4j.compat.bubbles.list.ListTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.input.key.KeyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Progressive filter behavior tests for {@link List}. Each test combines
 * UpdateResult assertions with exact rendered view output. Builds from
 * entering filter mode through typing, accepting, and clearing filters.
 */
class ListFilterBehaviorTest {

    @BeforeEach
    void setUp() {
        initTestEnvironment();
    }

    @Test
    void slashKeyEntersFilterMode() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));

        UpdateResult<List> result = list.update(runeKey('/'));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.filterState()).isEqualTo(FilterState.Filtering);
        assertThat(list.view()).isEqualTo(view("""
                  Filter:
                \s
                  3 items
                \s
                > foo
                  bar
                  baz
                \s
                \s
                \s
                \s
                  esc cancel"""));
    }

    @Test
    void typingInFilterModeNarrowsResults() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('/'));

        UpdateResult<List> result = list.update(runeKey('b'));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.filterState()).isEqualTo(FilterState.Filtering);
        java.util.List<String> visible = list.visibleItems().stream()
            .map(fi -> fi.item().filterValue())
            .toList();
        assertThat(visible).containsExactly("bar", "baz");
        assertThat(list.view()).isEqualTo(view("""
                  Filter: b
                \s
                  2 items \u2022 1 filtered
                \s
                > bar
                  baz
                \s
                \s
                \s
                \s
                \s
                  enter apply filter \u2022 esc cancel"""));
    }

    @Test
    void enterAcceptsFilter() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('/'));
        applyMessage(list, runeKey('b'));

        UpdateResult<List> result = list.update(specialKey(KeyType.keyCR));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.filterState()).isEqualTo(FilterState.FilterApplied);
        assertThat(list.view()).isEqualTo(view("""
                   List
                \s
                  \u201Cb\u201D 2 items \u2022 1 filtered
                \s
                > bar
                  baz
                \s
                \s
                \s
                \s
                \s
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 esc clear filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void escClearsAppliedFilter() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('/'));
        applyMessage(list, runeKey('b'));
        applyMessage(list, specialKey(KeyType.keyCR));

        UpdateResult<List> result = list.update(specialKey(KeyType.keyESC));

        assertThat(result.model()).isSameAs(list);
        applyCommand(list, result.command());
        assertThat(list.filterState()).isEqualTo(FilterState.Unfiltered);
        assertThat(list.view()).isEqualTo(view("""
                   List
                \s
                  3 items
                \s
                > foo
                  bar
                  baz
                \s
                \s
                \s
                \s
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void escCancelsFilterDuringTyping() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyMessage(list, runeKey('/'));
        applyMessage(list, runeKey('x'));
        assertThat(list.filterState()).isEqualTo(FilterState.Filtering);

        applyMessage(list, specialKey(KeyType.keyESC));

        assertThat(list.filterState()).isEqualTo(FilterState.Unfiltered);
        assertThat(list.view()).isEqualTo(view("""
                   List
                \s
                  3 items
                \s
                > foo
                  bar
                  baz
                \s
                \s
                \s
                \s
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void customStatusBarItemName() {
        List list = createFullList(40, 12, items("foo", "bar"));
        list.setStatusBarItemName("file", "files");

        assertThat(list.view()).isEqualTo(view("""
                   List
                \s
                  2 files
                \s
                > foo
                  bar
                \s
                \s
                \s
                \s
                \s
                  \u2191/k up \u2022 \u2193/j down \u2022 / filter \u2022 q quit \u2022 ? more"""));
    }

    @Test
    void nothingMatchedFilterShowsEmptyResults() {
        List list = createFullList(40, 12, items("foo", "bar", "baz"));
        applyCommand(list, list.setFilterText("zzz"));

        assertThat(list.filterState()).isEqualTo(FilterState.Filtering);
        assertThat(list.view()).isEqualTo(view("""
                  Filter: zzz
                \s
                  Nothing matched \u2022 3 filtered
                \s
                \s
                \s
                \s
                \s
                \s
                \s
                \s
                  enter apply filter \u2022 esc cancel"""));
    }
}
