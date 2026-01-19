package com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch;

import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Stopwatch.
 */
class StopwatchTest {

    @Test
    void testInitialState() {
        Stopwatch stopwatch = new Stopwatch();
        assertThat(stopwatch.running()).isFalse();
        assertThat(stopwatch.elapsed()).isEqualTo(Duration.ZERO);
        assertThat(stopwatch.interval()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void testCustomInterval() {
        Duration interval = Duration.ofMillis(100);
        Stopwatch stopwatch = new Stopwatch(interval);
        assertThat(stopwatch.interval()).isEqualTo(interval);
    }

    @Test
    void testStartStopMessageUpdatesRunningState() {
        Stopwatch stopwatch = new Stopwatch();
        assertThat(stopwatch.running()).isFalse();

        stopwatch.update(new StartStopMsg(stopwatch.id(), true));
        assertThat(stopwatch.running()).isTrue();

        stopwatch.update(new StartStopMsg(stopwatch.id(), false));
        assertThat(stopwatch.running()).isFalse();
    }

    @Test
    void testStartCommandDoesNotMutateState() {
        Stopwatch stopwatch = new Stopwatch();

        stopwatch.start();

        assertThat(stopwatch.running()).isFalse();
    }

    @Test
    void testStopCommandDoesNotMutateState() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));

        stopwatch.stop();

        assertThat(stopwatch.running()).isTrue();
    }

    @Test
    void testResetCommandDoesNotMutateElapsed() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));
        stopwatch.update(new TickMsg(stopwatch.id(), 0));

        stopwatch.reset();

        assertThat(stopwatch.elapsed()).isNotEqualTo(Duration.ZERO);
    }

    @Test
    void testStartStopMessageFromDifferentIdIsIgnored() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.update(new StartStopMsg(999, true));
        assertThat(stopwatch.running()).isFalse();
    }

    @Test
    void testTickIncrementsElapsed() {
        Stopwatch stopwatch = new Stopwatch(Duration.ofSeconds(1));
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));

        Duration before = stopwatch.elapsed();
        stopwatch.update(new TickMsg(stopwatch.id(), 0));

        assertThat(stopwatch.elapsed()).isEqualTo(before.plus(Duration.ofSeconds(1)));
    }

    @Test
    void testTickIgnoredWhenStopped() {
        Stopwatch stopwatch = new Stopwatch(Duration.ofSeconds(1));
        Duration before = stopwatch.elapsed();
        stopwatch.update(new TickMsg(stopwatch.id(), 0));

        assertThat(stopwatch.elapsed()).isEqualTo(before);
    }

    @Test
    void testTickWithDifferentIdIsIgnored() {
        Stopwatch stopwatch = new Stopwatch(Duration.ofSeconds(1));
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));

        Duration before = stopwatch.elapsed();
        stopwatch.update(new TickMsg(999, 0));

        assertThat(stopwatch.elapsed()).isEqualTo(before);
    }

    @Test
    void testTickWithWrongTagIsIgnored() {
        Stopwatch stopwatch = new Stopwatch(Duration.ofSeconds(1));
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));

        stopwatch.update(new TickMsg(stopwatch.id(), 0));
        Duration afterFirstTick = stopwatch.elapsed();

        stopwatch.update(new TickMsg(stopwatch.id(), 999));

        assertThat(stopwatch.elapsed()).isEqualTo(afterFirstTick);
    }

    @Test
    void testResetResetsElapsed() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));
        stopwatch.update(new TickMsg(stopwatch.id(), 0));
        stopwatch.update(new TickMsg(stopwatch.id(), 1));

        stopwatch.update(new ResetMsg(stopwatch.id()));

        assertThat(stopwatch.elapsed()).isEqualTo(Duration.ZERO);
    }

    @Test
    void testResetWithDifferentIdIsIgnored() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));
        stopwatch.update(new TickMsg(stopwatch.id(), 0));

        stopwatch.update(new ResetMsg(999));

        assertThat(stopwatch.elapsed()).isEqualTo(Duration.ofSeconds(1));
    }

    @Test
    void testTagIncrementsOnTick() {
        Stopwatch stopwatch = new Stopwatch(Duration.ofMillis(100));
        stopwatch.update(new StartStopMsg(stopwatch.id(), true));

        UpdateResult<Stopwatch> result1 = stopwatch.update(new TickMsg(stopwatch.id(), 0));
        assertThat(result1.command()).isNotNull();

        stopwatch.update(new TickMsg(stopwatch.id(), 1));
        UpdateResult<Stopwatch> result2 = stopwatch.update(new TickMsg(stopwatch.id(), 2));

        TickMsg tickMsg = (TickMsg) result2.command().execute();
        assertThat(tickMsg.tag()).isEqualTo(3);
    }

    @Test
    void testUniqueIds() {
        Stopwatch stopwatch1 = new Stopwatch();
        Stopwatch stopwatch2 = new Stopwatch();
        assertThat(stopwatch1.id()).isNotEqualTo(stopwatch2.id());
    }
}
