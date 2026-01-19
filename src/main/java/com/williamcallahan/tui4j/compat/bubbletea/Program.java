package com.williamcallahan.tui4j.compat.bubbletea;

import com.williamcallahan.tui4j.compat.bubbletea.input.InputHandler;
import com.williamcallahan.tui4j.compat.bubbletea.input.MouseAction;

import com.williamcallahan.tui4j.input.MouseClickMessage;
import com.williamcallahan.tui4j.input.MouseClickTracker;
import com.williamcallahan.tui4j.input.MouseHoverTextDetector;
import com.williamcallahan.tui4j.compat.bubbletea.input.MouseMessage;
import com.williamcallahan.tui4j.input.MouseSelectionAutoScroller;
import com.williamcallahan.tui4j.input.MouseSelectionTracker;
import com.williamcallahan.tui4j.input.MouseSelectionUpdate;
import com.williamcallahan.tui4j.input.MouseTarget;
import com.williamcallahan.tui4j.input.MouseTargetProvider;
import com.williamcallahan.tui4j.input.MouseTargets;
import com.williamcallahan.tui4j.compat.bubbletea.input.NewInputHandler;
import com.williamcallahan.tui4j.compat.bubbletea.message.BatchMessage;
import com.williamcallahan.tui4j.message.CheckWindowSizeMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.ClearScreenMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.EnterAltScreen;
import com.williamcallahan.tui4j.message.ErrorMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.ExitAltScreen;
import com.williamcallahan.tui4j.compat.bubbletea.message.ExecCompletedMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.ExecProcessMessage;
import com.williamcallahan.tui4j.message.OpenUrlMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.QuitMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.ResumeMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.SequenceMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.SuspendMessage;
import com.williamcallahan.tui4j.compat.bubbletea.message.WindowSizeMessage;
import com.williamcallahan.tui4j.compat.bubbletea.render.Renderer;
import com.williamcallahan.tui4j.compat.bubbletea.render.StandardRenderer;
import com.williamcallahan.tui4j.runtime.CommandExecutor;
import com.williamcallahan.tui4j.runtime.UrlOpener;
import com.williamcallahan.tui4j.term.TerminalInfo;
import com.williamcallahan.tui4j.term.jline.JLineTerminalInfoProvider;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;
import org.jline.utils.Signals;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Runs the TUI event loop and manages terminal IO.
 * <p>
 * Port of Bubble Tea's `Program` (tea.go).
 * Manages the lifecycle of a TUI application, including terminal initialization,
 * event polling, and rendering loops.
 */
public class Program {

    private static final Logger logger = Logger.getLogger(Program.class.getName());

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final CommandExecutor commandExecutor;
    private final InputHandler inputHandler;

    private Throwable lastError;
    private volatile Model currentModel;
    private final Renderer renderer;
    private final Terminal terminal;
    private final MouseSelectionTracker mouseSelectionTracker = new MouseSelectionTracker();
    private final MouseHoverTextDetector mouseHoverTextDetector = new MouseHoverTextDetector();
    private final MouseClickTracker mouseClickTracker = new MouseClickTracker();
    private boolean extendSelectionOnScroll;
    private boolean manageMouseSelectionCursor;
    private boolean mouseSelectionCursorActive;
    private boolean hoverTextCursorEnabled;
    private boolean hoverTextCursorActive;
    private boolean mouseClicksEnabled;
    private volatile boolean isSuspended = false;

    private final MouseSelectionAutoScroller mouseSelectionAutoScroller;

    public Program(Model initialModel) {
        this.currentModel = initialModel;
        this.commandExecutor = new CommandExecutor();

        try {
            this.terminal = TerminalBuilder.builder()
                    .system(true)
                    .jni(true)
                    .build();
            terminal.enterRawMode();

            TerminalInfo.provide(new JLineTerminalInfoProvider(terminal));

            this.renderer = new StandardRenderer(terminal);
            this.inputHandler = new NewInputHandler(terminal, this::send);
            this.mouseSelectionAutoScroller = new MouseSelectionAutoScroller(terminal::getHeight, mouseSelectionTracker, this::send);
        } catch (IOException e) {
            throw new ProgramException("Failed to initialize terminal", e);
        }
    }

    public Program withAltScreen() {
        renderer.enterAltScreen();
        return this;
    }

    public Program withReportFocus() {
        renderer.enableReportFocus();
        return this;
    }

    public Program withMouseAllMotion() {
        renderer.enableMouseAllMotion();
        renderer.enableMouseSGRMode();
        return this;
    }

    public Program withMouseCellMotion() {
        renderer.enableMouseCellMotion();
        renderer.enableMouseSGRMode();
        return this;
    }

    /**
     * While selecting, translate wheel events into selection motion updates.
     */
    public Program withMouseSelectionExtendOnScroll() {
        this.extendSelectionOnScroll = true;
        return this;
    }

    /**
     * While selecting, automatically scroll when the mouse is at the top/bottom edge.
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    public Program withMouseSelectionAutoScroll() {
        // Auto-scroll is implemented by emitting wheel events while selecting, so we must also
        // preserve selection during scroll to avoid breaking the user's selection state.
        this.extendSelectionOnScroll = true;
        mouseSelectionAutoScroller.enable();
        return this;
    }

    /**
     * Configure selection auto-scroll behavior.
     * <p>
     * tui4j extension; no Bubble Tea equivalent.
     */
    public Program withMouseSelectionAutoScroll(int edgeRows, int intervalMs) {
        this.extendSelectionOnScroll = true;
        mouseSelectionAutoScroller.configure(edgeRows, intervalMs);
        return this;
    }

    /**
     * Manage the mouse cursor during selection (OSC 22).
     */
    public Program withMouseSelectionCursor() {
        this.manageMouseSelectionCursor = true;
        return this;
    }

    /**
     * Manage the mouse cursor when hovering non-whitespace text (OSC 22).
     * Requires mouse motion events (e.g. {@link #withMouseAllMotion()}).
     */
    public Program withMouseHoverTextCursor() {
        this.hoverTextCursorEnabled = true;
        return this;
    }

    /**
     * When enabled, emits {@link MouseClickMessage} on press/release clicks.
     * tui4j extension; no Bubble Tea equivalent.
     */
    public Program withMouseClicks() {
        this.mouseClicksEnabled = true;
        return this;
    }
    /**
     * Blocks the calling thread, enters raw mode, and starts the event loop.
     * Takes control of the terminal until the model returns a {@link QuitMessage}.
     */
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Program is already running!");
        }

        handleTerminationSignals();
        handleSuspendSignals();
        handleTerminalResize();
        mouseSelectionAutoScroller.start();

        // start reading keyboard input
        inputHandler.start();

        // starting renderer
        renderer.hideCursor();
        renderer.start();

        Model finalModel = currentModel;
        boolean renderFinalView = false;

        try {
            // execute init command
            Command initCommand = currentModel.init();
            commandExecutor
                    .executeIfPresent(initCommand, this::send, this::sendError)
                    .thenRun(initLatch::countDown);

            // render the initial view
            renderer.write(currentModel.view());

            // run event loop
            finalModel = eventLoop();
            renderFinalView = true;
        } finally {
            cleanup(renderFinalView, finalModel);
        }

        if (lastError != null) {
            throw new ProgramException(lastError);
        }
    }

    private void cleanup(boolean renderFinalView, Model finalModel) {
        // stop reading keyboard input
        inputHandler.stop();

        if (renderFinalView) {
            // render final model view before closing
            renderer.write(finalModel.view());
        }
        renderer.showCursor();
        renderer.stop();

        mouseSelectionAutoScroller.stop();

        if (manageMouseSelectionCursor && mouseSelectionCursorActive) {
            renderer.resetMouseCursor();
        }
        if (hoverTextCursorEnabled && hoverTextCursorActive) {
            renderer.resetMouseCursor();
        }

        // disabling mouse support
        disableMouse();

        if (renderer.reportFocus()) {
            renderer.disableReportFocus();
        }

        if (renderer.altScreen()) {
            renderer.exitAltScreen();
        }

        terminal.puts(InfoCmp.Capability.carriage_return);
        terminal.puts(InfoCmp.Capability.cursor_down);
        terminal.flush();

        // Finally clean up
        isRunning.set(false);
        commandExecutor.shutdown();
        try {
            terminal.close();
        } catch (IOException e) {
            // Chain with any existing error to preserve context
            if (lastError != null) {
                e.addSuppressed(lastError);
            }
            throw new ProgramException(e);
        }
    }

    private void handleTerminationSignals() {
        Signals.register("INT", () -> commandExecutor.executeIfPresent(QuitMessage::new, this::send, this::sendError));
        Signals.register("TERM", () -> commandExecutor.executeIfPresent(QuitMessage::new, this::send, this::sendError));
    }

    private void handleSuspendSignals() {
        Signals.register("TSTP", () -> commandExecutor.executeIfPresent(SuspendMessage::new, this::send, this::sendError));
        Signals.register("CONT", () -> commandExecutor.executeIfPresent(ResumeMessage::new, this::send, this::sendError));
    }

    private void handleTerminalResize() {
        Signals.register("WINCH", () -> commandExecutor.executeIfPresent(CheckWindowSizeMessage::new, this::send, this::sendError));
        commandExecutor.executeIfPresent(CheckWindowSizeMessage::new, this::send, this::sendError);
    }

    private Model eventLoop() {
        while (isRunning.get()) {
            Message msg;
            try {
                msg = messageQueue.poll(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (msg != null) {
                if (handleSystemMessage(msg)) {
                    continue;
                }
                
                if (msg instanceof QuitMessage) {
                    return currentModel;
                } else if (msg instanceof ErrorMessage errorMessage) {
                    this.lastError = errorMessage.error();
                    return currentModel;
                }

                if (msg instanceof MouseMessage mouseMessage) {
                    mouseSelectionAutoScroller.onMouse(mouseMessage);
                    handleMouseClickTracking(mouseMessage);
                    handleMouseSelectionTracking(mouseMessage);
                    handleMouseHoverCursor(mouseMessage);
                }

                // process internal messages for the renderer
                renderer.handleMessage(msg);

                UpdateResult<? extends Model> updateResult = currentModel.update(msg);

                currentModel = updateResult.model();
                renderer.notifyModelChanged();
                commandExecutor.executeIfPresent(updateResult.command(), this::send, this::sendError);

                renderer.write(currentModel.view());
            }

        }
        return currentModel;
    }

    private boolean handleSystemMessage(Message msg) {
        return switch (msg) {
            case ClearScreenMessage ignored -> {
                renderer.clearScreen();
                yield true;
            }
            case EnterAltScreen ignored -> {
                renderer.enterAltScreen();
                yield true;
            }
            case ExitAltScreen ignored -> {
                renderer.exitAltScreen();
                yield true;
            }
            case BatchMessage batchMessage -> {
                handleBatch(batchMessage);
                yield true;
            }
            case SequenceMessage sequenceMessage -> {
                handleSequence(sequenceMessage);
                yield true;
            }
            case CheckWindowSizeMessage ignored -> {
                commandExecutor.executeIfPresent(this::checkSize, this::send, this::sendError);
                yield true;
            }
            case OpenUrlMessage openUrlMessage -> {
                handleOpenUrl(openUrlMessage);
                yield true;
            }
            case ExecProcessMessage execProcessMessage -> {
                executeProcess(execProcessMessage);
                yield true;
            }
            case SuspendMessage ignored -> {
                suspend();
                yield true;
            }
            case ResumeMessage ignored -> {
                resume();
                yield true;
            }
            default -> false;
        };
    }

    private void handleBatch(BatchMessage batchMessage) {
        Arrays.stream(batchMessage.commands())
                .forEach(command -> commandExecutor.executeIfPresent(command, this::send, this::sendError));
    }

    private void handleSequence(SequenceMessage sequenceMessage) {
        Arrays.stream(sequenceMessage.commands())
                .reduce(
                        CompletableFuture.completedFuture(null),
                        (CompletableFuture<Void> future, Command command) ->
                                future.thenCompose(__ ->
                                        commandExecutor.executeIfPresent(command, this::send, this::sendError)
                                ),
                        (f1, f2) -> f2
                ).join();
    }

    private void handleOpenUrl(OpenUrlMessage openUrlMessage) {
        boolean success = UrlOpener.open(openUrlMessage.url());
        if (!success) {
            logger.log(Level.WARNING, "Failed to open URL: " + openUrlMessage.url());
        }
    }

    private void handleMouseSelectionTracking(MouseMessage mouseMessage) {
        MouseSelectionUpdate selectionUpdate = mouseSelectionTracker.update(mouseMessage);

        if (extendSelectionOnScroll && selectionUpdate.selectionScrollUpdate() != null) {
            send(selectionUpdate.selectionScrollUpdate());
        }

        if (!manageMouseSelectionCursor) {
            return;
        }

        if (selectionUpdate.selectionStarted()) {
            setMouseSelectionCursorText();
            return;
        }

        if (selectionUpdate.selectionEnded()) {
            resetMouseSelectionCursor();
            return;
        }

        if (selectionUpdate.selectionActive()) {
            if (mouseMessage.isWheel()) {
                resetMouseSelectionCursor();
                return;
            }

            if (mouseMessage.getAction() == MouseAction.MouseActionMotion
                    || mouseMessage.getAction() == MouseAction.MouseActionPress) {
                setMouseSelectionCursorText();
            }
        }
    }

    private void handleMouseHoverCursor(MouseMessage mouseMessage) {
        if (!hoverTextCursorEnabled) {
            return;
        }
        if (mouseSelectionTracker.isSelecting() || mouseSelectionCursorActive) {
            return;
        }
        if (mouseMessage.isWheel()) {
            return;
        }
        if (mouseMessage.getAction() != MouseAction.MouseActionMotion
                && mouseMessage.getAction() != MouseAction.MouseActionPress
                && mouseMessage.getAction() != MouseAction.MouseActionRelease) {
            return;
        }

        boolean overText = mouseHoverTextDetector.isHoveringText(
                currentModel.view(),
                mouseMessage.column(),
                mouseMessage.row()
        );

        if (overText && !hoverTextCursorActive) {
            renderer.setMouseCursorText();
            hoverTextCursorActive = true;
        } else if (!overText && hoverTextCursorActive) {
            renderer.resetMouseCursor();
            hoverTextCursorActive = false;
        }
    }

    private void setMouseSelectionCursorText() {
        if (mouseSelectionCursorActive) {
            return;
        }
        renderer.setMouseCursorText();
        mouseSelectionCursorActive = true;
    }

    private void resetMouseSelectionCursor() {
        if (!mouseSelectionCursorActive) {
            return;
        }
        renderer.resetMouseCursor();
        mouseSelectionCursorActive = false;
    }

    private void handleMouseClickTracking(MouseMessage mouseMessage) {
        if (!mouseClicksEnabled) {
            return;
        }
        MouseTarget target = resolveMouseTarget(mouseMessage);
        MouseClickMessage clickMessage = mouseClickTracker.handle(mouseMessage, target);
        if (clickMessage != null) {
            send(clickMessage);
        }
    }

    private MouseTarget resolveMouseTarget(MouseMessage mouseMessage) {
        if (!(currentModel instanceof MouseTargetProvider provider)) {
            return null;
        }
        return MouseTargets.hitTest(provider.mouseTargets(), mouseMessage.column(), mouseMessage.row());
    }

    private Message checkSize() {
        Size size = terminal.getSize();
        return new WindowSizeMessage(size.getColumns(), size.getRows());
    }

    private void sendError(Throwable error) {
        send(new ErrorMessage(error));
    }

    public void send(Message msg) {
        if (isRunning.get() && msg != null) {
            messageQueue.offer(msg);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void waitForInit() {
        try {
            initLatch.await();
        } catch (InterruptedException e) {
            throw new ProgramException(e);
        }
    }

    private void disableMouse() {
        renderer.disableMouseSGRMode();
        renderer.disableMouseNormalTracking();
        renderer.disableMouseCellMotion();
        renderer.disableMouseAllMotion();
    }

    private void executeProcess(ExecProcessMessage execProcessMessage) {
        // Run synchronously to block the event loop, matching Bubble Tea's behavior
        Process process = execProcessMessage.process();
        BiConsumer<Integer, byte[]> outputHandler = execProcessMessage.outputHandler();
        BiConsumer<Integer, byte[]> errorHandler = execProcessMessage.errorHandler();
        
        suspend(); // Explicitly suspend the renderer and event loop interactions
        
        try {
            // Terminal is already paused by suspend(), but we ensure raw mode is exited if needed
            // Bubbles/Tea usually relies on restoreTerminal() which suspend() does.
            
            int exitCode = process.waitFor();
            if (outputHandler != null) {
                byte[] output = ExecProcessMessage.readStream(process.getInputStream());
                outputHandler.accept(exitCode, output);
            }
            if (errorHandler != null) {
                byte[] error = ExecProcessMessage.readStream(process.getErrorStream());
                errorHandler.accept(exitCode, error);
            }

            send(new ExecCompletedMessage(exitCode, null));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error executing process", e);
            send(new ExecCompletedMessage(-1, e));
        } finally {
            resume(); // Restore terminal and renderer
        }
    }

    private void suspend() {
        if (isSuspended) {
            return;
        }
        isSuspended = true;
        renderer.showCursor();
        renderer.stop();
        terminal.pause();
    }

    private void resume() {
        if (!isSuspended) {
            return;
        }
        terminal.resume();
        renderer.start();
        renderer.hideCursor();
        renderer.write(currentModel.view());
        isSuspended = false;
    }
}
