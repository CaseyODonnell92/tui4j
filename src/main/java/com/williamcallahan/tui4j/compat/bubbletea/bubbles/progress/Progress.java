package com.williamcallahan.tui4j.compat.bubbletea.bubbles.progress;

import com.williamcallahan.tui4j.compat.bubbletea.Command;
import com.williamcallahan.tui4j.compat.bubbletea.Message;
import com.williamcallahan.tui4j.compat.bubbletea.Model;
import com.williamcallahan.tui4j.compat.bubbletea.UpdateResult;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Renderer;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.Style;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.ColorProfile;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.RGB;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.RGBColor;
import com.williamcallahan.tui4j.compat.bubbletea.lipgloss.color.TerminalColor;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Progress bar bubble with gradient support.
 * <p>
 * Port of `bubbles/progress`.
 * Visualizes a percentage value, optionally using a spring animation for smooth transitions.
 */
public class Progress implements Model {

    private static final double FPS = 60.0;
    private static final int DEFAULT_WIDTH = 40;
    private static final double DEFAULT_FREQUENCY = 18.0;
    private static final double DEFAULT_DAMPING = 1.0;
    private static final AtomicInteger LAST_ID = new AtomicInteger(0);

    private final int id;
    private int tag;

    private int width;
    private char full;
    private char empty;
    private String fullColor;
    private String emptyColor;
    private boolean showPercentage;
    private String percentFormat;
    private Style percentageStyle;

    private Spring spring;
    private double percentShown;
    private double targetPercent;
    private double velocity;
    private boolean springCustomized;

    private boolean useRamp;
    private RGB rampColorA;
    private RGB rampColorB;
    private boolean scaleRamp;

    private ColorProfile colorProfile;
    private ColorProfile cachedColorProfile;

    public Progress() {
        this.id = nextId();
        this.width = DEFAULT_WIDTH;
        this.full = '█';
        this.fullColor = "#7571F9";
        this.empty = '░';
        this.emptyColor = "#606060";
        this.showPercentage = true;
        this.percentFormat = " %3.0f%%";
        this.percentageStyle = Style.newStyle();

        setSpringOptions(DEFAULT_FREQUENCY, DEFAULT_DAMPING);
    }

    private static int nextId() {
        return LAST_ID.incrementAndGet();
    }

    public Progress withWidth(int width) {
        this.width = width;
        return this;
    }

    public Progress withFull(char full) {
        this.full = full;
        return this;
    }

    public Progress withEmpty(char empty) {
        this.empty = empty;
        return this;
    }

    public Progress withFullColor(String fullColor) {
        this.fullColor = fullColor;
        this.useRamp = false;
        return this;
    }

    public Progress withEmptyColor(String emptyColor) {
        this.emptyColor = emptyColor;
        return this;
    }

    public Progress withoutPercentage() {
        this.showPercentage = false;
        return this;
    }

    public Progress withShowPercentage(boolean showPercentage) {
        this.showPercentage = showPercentage;
        return this;
    }

    public Progress withPercentFormat(String percentFormat) {
        this.percentFormat = percentFormat;
        return this;
    }

    public Progress withPercentageStyle(Style percentageStyle) {
        this.percentageStyle = percentageStyle;
        return this;
    }

    public Progress withDefaultGradient() {
        return withGradient("#5A56E0", "#EE6FF8");
    }

    /**
     * Configures a color gradient for the filled portion of the bar.
     */
    public Progress withGradient(String colorA, String colorB) {
        return setRamp(colorA, colorB, false);
    }

    public Progress withDefaultScaledGradient() {
        return withScaledGradient("#5A56E0", "#EE6FF8");
    }

    public Progress withScaledGradient(String colorA, String colorB) {
        return setRamp(colorA, colorB, true);
    }

    private Progress setRamp(String colorA, String colorB, boolean scaled) {
        RGB a = parseColor(colorA);
        RGB b = parseColor(colorB);

        this.useRamp = true;
        this.scaleRamp = scaled;
        this.rampColorA = a;
        this.rampColorB = b;
        return this;
    }

    private static RGB parseColor(String color) {
        return RGB.fromHexString(color);
    }

    public void setSpringOptions(double frequency, double damping) {
        this.spring = new Spring(frequency, damping);
        this.springCustomized = true;
    }

    public void setColorProfile(ColorProfile colorProfile) {
        this.colorProfile = colorProfile;
    }

    private ColorProfile getColorProfile() {
        if (colorProfile != null) {
            return colorProfile;
        }
        if (cachedColorProfile == null) {
            cachedColorProfile = Renderer.defaultRenderer().colorProfile();
        }
        return cachedColorProfile;
    }

    public int id() {
        return id;
    }

    public int width() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public char full() {
        return full;
    }

    public char empty() {
        return empty;
    }

    public String fullColor() {
        return fullColor;
    }

    public String emptyColor() {
        return emptyColor;
    }

    public boolean showPercentage() {
        return showPercentage;
    }

    public String percentFormat() {
        return percentFormat;
    }

    public double percent() {
        return targetPercent;
    }

    public double targetPercent() {
        return targetPercent;
    }

    public int tag() {
        return tag;
    }

    public double percentShown() {
        return percentShown;
    }

    public boolean isAnimating() {
        double dist = Math.abs(percentShown - targetPercent);
        return !(dist < 0.001 && Math.abs(velocity) < 0.01);
    }

    @Override
    public Command init() {
        return null;
    }

    @Override
    public UpdateResult<Progress> update(Message msg) {
        if (msg instanceof FrameMsg frameMsg) {
            if (frameMsg.id() != id || frameMsg.tag() != tag) {
                return UpdateResult.from(this);
            }

            if (!isAnimating()) {
                return UpdateResult.from(this);
            }

            Spring.SpringUpdateResult result = spring.update(percentShown, velocity, targetPercent);
            this.percentShown = result.position();
            this.velocity = result.velocity();
            return UpdateResult.from(this, nextFrame());
        }

        return UpdateResult.from(this);
    }

    @Override
    public String view() {
        return viewAs(percentShown);
    }

    public String viewAs(double percent) {
        StringBuilder b = new StringBuilder();
        String percentView = percentageView(percent);
        int textWidth = textWidth(percentView);
        barView(b, percent, textWidth);
        b.append(percentView);
        return b.toString();
    }

    private static int textWidth(String s) {
        if (s == null || s.isEmpty()) {
            return 0;
        }
        int width = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x1100) {
                width++;
            } else {
                width += 2;
            }
        }
        return width;
    }

    private void barView(StringBuilder b, double percent, int textWidth) {
        int tw = Math.max(0, width - textWidth);
        int fw = (int) Math.round((double) tw * percent);

        fw = Math.max(0, Math.min(tw, fw));

        if (useRamp) {
            for (int i = 0; i < fw; i++) {
                double p;
                if (fw == 1) {
                    p = 0.5;
                } else if (scaleRamp) {
                    p = (double) i / (fw - 1);
                } else {
                    p = (double) i / (tw - 1);
                }
                RGB blended = blend(rampColorA, rampColorB, p);
                String color = rgbToHex(blended);
                b.append(colorize(String.valueOf(full), color));
            }
        } else {
            String colored = colorize(String.valueOf(full), fullColor);
            b.append(colored.repeat(fw));
        }

        String emptyColored = colorize(String.valueOf(empty), emptyColor);
        int n = Math.max(0, tw - fw);
        b.append(emptyColored.repeat(n));
    }

    private static RGB blend(RGB a, RGB b, double t) {
        return new RGB(
                (float) (a.r() + (b.r() - a.r()) * t),
                (float) (a.g() + (b.g() - a.g()) * t),
                (float) (a.b() + (b.b() - a.b()) * t)
        );
    }

    private static String rgbToHex(RGB rgb) {
        int r = Math.round(rgb.r() * 255.0f);
        int g = Math.round(rgb.g() * 255.0f);
        int b = Math.round(rgb.b() * 255.0f);
        return String.format("#%02x%02x%02x", r, g, b);
    }

    private String colorize(String text, String color) {
        ColorProfile profile = getColorProfile();
        if (profile == null || profile == ColorProfile.Ascii) {
            return text;
        }
        return "\033[" + getANSIColorCode(color, profile) + "m" + text + "\033[0m";
    }

    private String getANSIColorCode(String color, ColorProfile profile) {
        RGB rgb = parseColor(color);
        int r = Math.round(rgb.r() * 255.0f);
        int g = Math.round(rgb.g() * 255.0f);
        int b = Math.round(rgb.b() * 255.0f);

        if (profile == ColorProfile.TrueColor) {
            return "38;2;" + r + ";" + g + ";" + b;
        } else {
            int ansi256 = rgbToANSI256(r, g, b);
            return "38;5;" + ansi256;
        }
    }

    private static int rgbToANSI256(int r, int g, int b) {
        if (r == g && g == b) {
            if (r < 8) {
                return 16;
            }
            if (r > 248) {
                return 231;
            }
            return (int) Math.round(((double) r - 8) / 247 * 24) + 232;
        }

        int rIdx = Math.round((float) r / 255 * 5);
        int gIdx = Math.round((float) g / 255 * 5);
        int bIdx = Math.round((float) b / 255 * 5);

        rIdx = Math.min(5, Math.max(0, rIdx));
        gIdx = Math.min(5, Math.max(0, gIdx));
        bIdx = Math.min(5, Math.max(0, bIdx));

        return 16 + 36 * rIdx + 6 * gIdx + bIdx;
    }

    private String percentageView(double percent) {
        if (!showPercentage) {
            return "";
        }
        percent = Math.max(0, Math.min(1, percent));
        String percentage = String.format(percentFormat, percent * 100);
        if (percentageStyle != null) {
            percentage = percentageStyle.inline(true).render(percentage);
        }
        return percentage;
    }

    private Command nextFrame() {
        return Command.tick(Duration.ofNanos((long) (1_000_000_000.0 / FPS)), time -> new FrameMsg(id, tag));
    }

    public Command setPercent(double p) {
        this.targetPercent = Math.max(0, Math.min(1, p));
        this.tag++;
        return nextFrame();
    }

    public Command incrPercent(double v) {
        return setPercent(targetPercent + v);
    }

    public Command decrPercent(double v) {
        return setPercent(targetPercent - v);
    }
}
