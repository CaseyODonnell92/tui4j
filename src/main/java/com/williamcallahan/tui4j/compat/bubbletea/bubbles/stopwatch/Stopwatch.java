package com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch;

import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stopwatch bubble component.
 * Port of charmbracelet/bubbles/stopwatch.
 */
public class Stopwatch implements Model {

    private static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(1);

    private static final AtomicInteger LAST_ID = new AtomicInteger(0);

    private Duration elapsed;
    private Duration interval;
    private int id;
    private int tag;
    private boolean running;

    public Stopwatch() {
        this(DEFAULT_INTERVAL);
    }

    public Stopwatch(Duration interval) {
        this.elapsed = Duration.ZERO;
        this.interval = interval;
        this.running = false;
        this.id = nextId();
    }

    public int id() {
        return id;
    }

    public Duration elapsed() {
        return elapsed;
    }

    void setElapsed(Duration elapsed) {
        this.elapsed = elapsed;
    }

    public Duration interval() {
        return interval;
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    public boolean running() {
        return running;
    }

    @Override
    public Command init() {
        return start();
    }

    @Override
    public UpdateResult<Stopwatch> update(Message msg) {
        if (msg instanceof StartStopMsg startStopMsg) {
            if (startStopMsg.id() != 0 && startStopMsg.id() != id) {
                return UpdateResult.from(this);
            }
            running = startStopMsg.running();
            if (running) {
                return UpdateResult.from(this, tick());
            }
            return UpdateResult.from(this);
        }
        if (msg instanceof ResetMsg resetMsg) {
            if (resetMsg.id() != 0 && resetMsg.id() != id) {
                return UpdateResult.from(this);
            }
            elapsed = Duration.ZERO;
            return UpdateResult.from(this);
        }
        if (msg instanceof TickMsg tickMsg) {
            if (!running || (tickMsg.id() != 0 && tickMsg.id() != id)) {
                return UpdateResult.from(this);
            }
            if (tickMsg.tag() > 0 && tickMsg.tag() != tag) {
                return UpdateResult.from(this);
            }

            elapsed = elapsed.plus(interval);
            tag++;
            return UpdateResult.from(this, tick());
        }

        return UpdateResult.from(this);
    }

    /**
     * Renders the elapsed time.
     * <p>
     * Formats the duration similar to Go's time.Duration string representation (e.g., "1h2m3s").
     */
    @Override
    public String view() {
        return formatDuration(elapsed);
    }

    /**
     * Returns a command to resume the stopwatch tick loop.
     */
    public Command start() {
        return Command.sequence(
                () -> new StartStopMsg(id, true),
                tick()
        );
    }

    /**
     * Returns a command to halt the tick loop.
     */
    public Command stop() {
        return () -> new StartStopMsg(id, false);
    }

    /**
     * Returns a command to reset the elapsed time to zero.
     */
    public Command reset() {
        return () -> new ResetMsg(id);
    }

    /**
     * Returns a command to switch between running and stopped states.
     */
    public Command toggle() {
        return Command.sequence(
                () -> new StartStopMsg(id, !running),
                tick()
        );
    }

    private static int nextId() {
        return LAST_ID.incrementAndGet();
    }

    private Command tick() {
        return Command.tick(interval, __ -> new TickMsg(id, tag));
    }

    private static String formatDuration(Duration duration) {
        if (duration.isZero()) {
            return "0s";
        }
        String s = duration.toString();
        // Remove PT and convert to lowercase to approximate Go's duration format
        return s.substring(2).toLowerCase();
    }
}
