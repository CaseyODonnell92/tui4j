package com.williamcallahan.tui4j.examples.stopwatch;

import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.Program;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.message.KeyPressMessage;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.help.Help;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.help.KeyMap;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.key.Binding;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch.ResetMsg;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch.StartStopMsg;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch.Stopwatch;
import com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch.TickMsg;

import java.time.Duration;

/**
 * Stopwatch example mirroring Bubble Tea's stopwatch sample.
 * tui4j: examples/generic/src/main/java/com/williamcallahan/tui4j/examples/stopwatch/StopwatchExample.java
 */
public class StopwatchExample implements Model {

    private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(100);

    private final Help help;
    private final StopwatchKeyMap keys;
    private Stopwatch stopwatch;
    private boolean quitting;

    public StopwatchExample() {
        this.stopwatch = new Stopwatch(DEFAULT_INTERVAL);
        this.help = new Help();
        this.keys = new StopwatchKeyMap();
        this.keys.stop().setEnabled(false);
    }

    @Override
    public Command init() {
        return stopwatch.init();
    }

    @Override
    public UpdateResult<? extends Model> update(Message msg) {
        if (msg instanceof TickMsg || msg instanceof StartStopMsg) {
            UpdateResult<Stopwatch> updateResult = stopwatch.update(msg);
            this.stopwatch = updateResult.model();
            if (msg instanceof StartStopMsg startStopMsg) {
                keys.stop().setEnabled(stopwatch.running());
                keys.start().setEnabled(!stopwatch.running());
            }
            return UpdateResult.from(this, updateResult.command());
        }

        if (msg instanceof ResetMsg) {
            UpdateResult<Stopwatch> updateResult = stopwatch.update(msg);
            this.stopwatch = updateResult.model();
            return UpdateResult.from(this, updateResult.command());
        }

        if (msg instanceof KeyPressMessage keyPressMessage) {
            if (Binding.matches(keyPressMessage, keys.quit())) {
                quitting = true;
                return UpdateResult.from(this, Command.quit());
            }
            if (Binding.matches(keyPressMessage, keys.start())) {
                return UpdateResult.from(this, stopwatch.start());
            }
            if (Binding.matches(keyPressMessage, keys.stop())) {
                return UpdateResult.from(this, stopwatch.stop());
            }
            if (Binding.matches(keyPressMessage, keys.reset())) {
                return UpdateResult.from(this, stopwatch.reset());
            }
        }

        return UpdateResult.from(this);
    }

    @Override
    public String view() {
        String view = stopwatch.view();
        view += "\n";

        if (!quitting) {
            view += helpView();
        }
        return view;
    }

    private String helpView() {
        return "\n" + help.render(keys);
    }

    public static void main(String[] args) {
        new Program(new StopwatchExample()).run();
    }

    private static final class StopwatchKeyMap implements KeyMap {
        private final Binding start;
        private final Binding stop;
        private final Binding reset;
        private final Binding quit;

        private StopwatchKeyMap() {
            this.start = new Binding(
                    Binding.withKeys("s"),
                    Binding.withHelp("s", "start")
            );
            this.stop = new Binding(
                    Binding.withKeys("x"),
                    Binding.withHelp("x", "stop")
            );
            this.reset = new Binding(
                    Binding.withKeys("r"),
                    Binding.withHelp("r", "reset")
            );
            this.quit = new Binding(
                    Binding.withKeys("q", "ctrl+c"),
                    Binding.withHelp("q", "quit")
            );
        }

        private Binding start() {
            return start;
        }

        private Binding stop() {
            return stop;
        }

        private Binding reset() {
            return reset;
        }

        private Binding quit() {
            return quit;
        }

        @Override
        public Binding[] shortHelp() {
            return new Binding[]{start, stop, reset, quit};
        }

        @Override
        public Binding[][] fullHelp() {
            return new Binding[][]{shortHelp()};
        }
    }
}
