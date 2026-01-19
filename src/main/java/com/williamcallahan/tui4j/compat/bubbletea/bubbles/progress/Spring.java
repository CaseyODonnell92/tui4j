package com.williamcallahan.tui4j.compat.bubbletea.bubbles.progress;

/**
 * Port of the progress spring helper.
 * Upstream: github.com/charmbracelet/bubbles/progress (Spring)
 */
public class Spring {

    /**
     * Port of the spring update strategy.
     * Upstream: github.com/charmbracelet/bubbles/progress (springUpdateFn)
     */
    public interface SpringUpdate {
        SpringUpdateResult update(double position, double velocity, double target);
    }

    public record SpringUpdateResult(double position, double velocity) {
    }

    private static final double FPS = 60.0;

    private final double frequency;
    private final double damping;

    public Spring(double frequency, double damping) {
        this.frequency = frequency;
        this.damping = damping;
    }

    public SpringUpdateResult update(double position, double velocity, double target) {
        double dt = 1.0 / FPS;

        double displacement = position - target;
        double springForce = -frequency * frequency * displacement;
        double dampingForce = -2.0 * damping * frequency * velocity;

        double acceleration = springForce + dampingForce;

        velocity += acceleration * dt;
        position += velocity * dt;

        return new SpringUpdateResult(position, velocity);
    }

    public static Spring withFPS(double fps, double frequency, double damping) {
        return new Spring(frequency, damping);
    }
}
