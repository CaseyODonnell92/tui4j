package com.williamcallahan.tui4j.compat.bubbles.list;

import static com.williamcallahan.tui4j.compat.bubbletea.Command.batch;
import static com.williamcallahan.tui4j.compat.bubbletea.Command.none;

import com.williamcallahan.tui4j.compat.bubbles.key.Binding;
import com.williamcallahan.tui4j.compat.bubbles.spinner.TickMessage;
import com.williamcallahan.tui4j.compat.bubbles.textinput.TextInput;
import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.QuitMessage;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * Update/message handling for {@link List}.
 * <p>
 * Upstream: bubbles/list/list.go (extracted from the original port).
 */
final class ListUpdateHandler {

    private ListUpdateHandler() {
    }

    static UpdateResult<List> update(List list, Message msg) {
        return switch (msg) {
            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.forceQuit()) ->
                UpdateResult.from(list, QuitMessage::new);

            case FetchedCurrentPageItems(
                FetchedItems fetched, Runnable[] postFetch
            ) -> handleDataSourceFetchComplete(list, fetched, postFetch);

            case TickMessage tick when list.showSpinner ->
                UpdateResult.from(list, list.spinner.update(tick).command());

            case StatusMessageTimeoutMessage ignored -> {
                ListStatusMessageManager.hideStatusMessage(list);
                yield UpdateResult.from(list, Command.none());
            }

            default -> {
                Command cmd = list.filterState == FilterState.Filtering
                    ? handleFiltering(list, msg)
                    : handleBrowsing(list, msg);
                yield UpdateResult.from(list, cmd);
            }
        };
    }

    private static UpdateResult<List> handleDataSourceFetchComplete(List list, FetchedItems fetchedItems, Runnable[] postFetchCallbacks) {
            list.stopSpinner();
            list.fetchingItems = false;

            list.currentPageItems = fetchedItems.items();
            list.matchedItems = fetchedItems.matchedItems();
            list.totalItems = fetchedItems.totalItems();

            list.updateKeybindings();

            for (Runnable runnable : postFetchCallbacks) {
                runnable.run();
            }

            boolean requiresRefetch = ListPaginationUpdater.updatePagination(
                list
            );
            if (requiresRefetch) {
                return UpdateResult.from(
                    list,
                    ListDataFetcher.fetchCurrentPageItems(list)
                );
            }

            return UpdateResult.from(list, ListDataFetcher.updateFilter(list));
    }

    static Command cursorUp(List list) {
        if ((list.cursor - 1) > -1) {
            list.cursor--;
            return Command.none();
        }

        if (list.paginator.page() != 0) {
            list.paginator.prevPage();
            return ListDataFetcher.fetchCurrentPageItems(list, () ->
                list.cursor = Math.max(0, list.currentPageItems.size() - 1)
            );
        }

        if (!list.infiniteScrolling) {
            return Command.none();
        }

        list.paginator.setPage(list.paginator.totalPages() - 1);
        return ListDataFetcher.fetchCurrentPageItems(list, () ->
            list.cursor = Math.max(0, list.currentPageItems.size() - 1)
        );
    }

    static Command cursorDown(List list) {
        if ((list.cursor + 1) < list.currentPageItems.size()) {
            list.cursor++;
            return Command.none();
        }

        if (!list.paginator.onLastPage()) {
            list.paginator.nextPage();
            return ListDataFetcher.fetchCurrentPageItems(list, () ->
                list.cursor = 0
            );
        }

        if (list.infiniteScrolling) {
            list.paginator.setPage(0);
            return ListDataFetcher.fetchCurrentPageItems(list, () ->
                list.cursor = 0
            );
        }
        return Command.none();
    }

    private static Command handleBrowsing(List list, Message msg) {
        return switch (msg) {
            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.clearFilter()) ->
                batch(list.resetFiltering(), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.quit()) ->
                QuitMessage::new;

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.filter()) -> {
                ListStatusMessageManager.hideStatusMessage(list);
                if (!list.paginator.onFirstPage()) {
                    list.paginator.setPage(0);
                }
                list.filterState = FilterState.Filtering;
                list.filterInput.cursorEnd();
                list.filterInput.focus();
                list.updateKeybindings();
                yield batch(
                    TextInput::blink,
                    ListDataFetcher.fetchCurrentPageItems(list, () ->
                        list.cursor = 0
                    )
                );
            }

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.cursorUp()) ->
                batch(cursorUp(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.cursorDown()) ->
                batch(cursorDown(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.prevPage()) ->
                batch(cursorLeft(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.nextPage()) ->
                batch(cursorRight(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.goToStart()) ->
                batch(gotoStart(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.goToEnd()) ->
                batch(gotoEnd(list), list.itemDelegate.update(kpm, list));

            case KeyPressMessage kpm
                when Binding.matches(kpm, list.keys.showFullHelp()) ||
                    Binding.matches(kpm, list.keys.closeFullHelp()) -> {
                list.help.setShowAll(!list.help.showAll());
                ListPaginationUpdater.updatePagination(list);
                yield batch(
                    ListDataFetcher.fetchCurrentPageItems(list),
                    list.itemDelegate.update(kpm, list)
                );
            }

            case KeyPressMessage kpm ->
                list.itemDelegate.update(kpm, list);

            default -> Command.none();
        };
    }

    private static Command gotoStart(List list) {
        if (list.paginator.onFirstPage()) {
            return Command.none();
        }

        list.paginator.setPage(0);
        list.cursor = 0;
        return ListDataFetcher.fetchCurrentPageItems(list);
    }

    private static Command gotoEnd(List list) {
        if (list.paginator.onLastPage()) {
            return Command.none();
        }

        list.paginator.setPage(list.paginator.totalPages() - 1);
        return ListDataFetcher.fetchCurrentPageItems(list, () ->
            keepCursorInBounds(list)
        );
    }

    private static Command cursorLeft(List list) {
        if (list.paginator.onFirstPage()) {
            return Command.none();
        }
        list.paginator.prevPage();
        return ListDataFetcher.fetchCurrentPageItems(list);
    }

    private static Command cursorRight(List list) {
        if (list.paginator.onLastPage()) {
            return Command.none();
        }
        list.paginator.nextPage();
        return ListDataFetcher.fetchCurrentPageItems(list, () ->
            keepCursorInBounds(list)
        );
    }

    static void keepCursorInBounds(List list) {
        if (list.currentPageItems.isEmpty()) {
            list.cursor = 0;
            return;
        }
        list.cursor = Math.clamp(list.cursor, 0, list.currentPageItems.size() - 1);
    }

    private static Command handleFiltering(List list, Message msg) {
        // Handle user key-presses related to filtering
        Command acceptOrCancel = switch(msg) {
           case KeyPressMessage kpm when Binding.matches(kpm, list.keys.cancelWhileFiltering()) -> {
                list.resetFiltering();

                yield ListDataFetcher.fetchCurrentPageItems(list, () -> {
                   list.keys.filter().setEnabled(true);
                   list.keys.clearFilter().setEnabled(false);
                });
           }
           case KeyPressMessage kpm when Binding.matches(kpm, list.keys.acceptWhileFiltering()) -> {
                ListStatusMessageManager.hideStatusMessage(list);

                if (list.totalItems <= 0) {
                   yield none();
                }

                if (list.matchedItems <= 0) {
                   yield list.resetFiltering();
                }

                list.filterInput.blur();
                list.filterState = FilterState.FilterApplied;
                list.updateKeybindings();

                if (list.filterInput.isEmpty()) {
                   yield none();
                }

                yield none();
           }
           default -> none();
        };

        // Allow nested filter input model to update, see if the user has changed the filter text
        String beforeChange = list.filterInput.value();
        UpdateResult<TextInput> updateResult = list.filterInput.update(msg);
        boolean filterChanged = !Objects.equals(
            beforeChange,
            updateResult.model().value()
        );
        list.filterInput = updateResult.model();
        Command updateResultCommand = updateResult.command();

        // Conditionally fetch data based on user input, if configured to fetch new data on updated filter (default
        // behaviour is to re-fetch on every user filter text update)
        Command filterChangedCommand = none();
        if (filterChanged && !list.filterOnAcceptOnly) {
           filterChangedCommand = ListDataFetcher.fetchCurrentPageItems(list, () -> {
                    list.keys
                        .acceptWhileFiltering()
                        .setEnabled(!list.filterInput.isEmpty());
                    ListPaginationUpdater.updatePagination(list);
                });
        }
        return batch(acceptOrCancel, updateResultCommand, filterChangedCommand);
    }
}
