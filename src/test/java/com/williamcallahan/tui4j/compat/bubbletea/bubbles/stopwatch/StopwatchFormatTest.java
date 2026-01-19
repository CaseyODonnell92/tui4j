package com.williamcallahan.tui4j.compat.bubbletea.bubbles.stopwatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Stopwatch view formatting.
 */
class StopwatchFormatTest {

    @ParameterizedTest
    @MethodSource("provideDurations")
    void testViewFormatting(Duration duration, String expected) {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.setElapsed(duration);

        assertThat(stopwatch.view()).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDurations() {
        return Stream.of(
                Arguments.of(Duration.ZERO, "0s"),
                Arguments.of(Duration.ofSeconds(1), "1s"),
                Arguments.of(Duration.ofSeconds(59), "59s"),
                Arguments.of(Duration.ofSeconds(60), "1m"),
                Arguments.of(Duration.ofSeconds(61), "1m1s"),
                Arguments.of(Duration.ofSeconds(3599), "59m59s"),
                Arguments.of(Duration.ofSeconds(3600), "1h"),
                Arguments.of(Duration.ofSeconds(3661), "1h1m1s"),
                Arguments.of(Duration.ofHours(1).plusMinutes(30).plusSeconds(45), "1h30m45s"),
                Arguments.of(Duration.ofHours(10).plusMinutes(5).plusSeconds(30), "10h5m30s")
        );
    }

    @Test
    void testViewFormattingWithNanos() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.setElapsed(Duration.ofSeconds(1).plusNanos(500_000_000));

        assertThat(stopwatch.view()).isEqualTo("1.5s");
    }

    @Test
    void testViewFormattingWithSubSecondNanos() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.setElapsed(Duration.ofSeconds(1).plusNanos(123_000_000));

        assertThat(stopwatch.view()).isEqualTo("1.123s");
    }
}
