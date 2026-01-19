package com.williamcallahan.tui4j.compat.bubbletea.harmonica;

/**
 * Physics-based spring animation primitive.
 * <p>
 * Port of `charmbracelet/harmonica`.
 * Simulates a damped spring system for natural animation curves.
 */
public class Spring {

    private static final double EPSILON = Math.nextAfter(1.0, 2.0) - 1.0;

    private final double posPosCoef;
    private final double posVelCoef;
    private final double velPosCoef;
    private final double velVelCoef;

    private Spring(double posPosCoef, double posVelCoef, double velPosCoef, double velVelCoef) {
        this.posPosCoef = posPosCoef;
        this.posVelCoef = posVelCoef;
        this.velPosCoef = velPosCoef;
        this.velVelCoef = velVelCoef;
    }

    public static double fps(int n) {
        return 1.0 / n;
    }

    public static Spring newSpring(double deltaTime, double angularFrequency, double dampingRatio) {
        double af = Math.max(0.0, angularFrequency);
        double dr = Math.max(0.0, dampingRatio);

        if (af < EPSILON) {
            return new Spring(1.0, 0.0, 0.0, 1.0);
        }

        if (dr > 1.0 + EPSILON) {
            double za = -af * dr;
            double zb = af * Math.sqrt(dr * dr - 1.0);
            double z1 = za - zb;
            double z2 = za + zb;

            double e1 = Math.exp(z1 * deltaTime);
            double e2 = Math.exp(z2 * deltaTime);

            double invTwoZb = 1.0 / (2.0 * zb);

            double e1_Over_TwoZb = e1 * invTwoZb;
            double e2_Over_TwoZb = e2 * invTwoZb;

            double z1e1_Over_TwoZb = z1 * e1_Over_TwoZb;
            double z2e2_Over_TwoZb = z2 * e2_Over_TwoZb;

            double ppc = e1_Over_TwoZb * z2 - z2e2_Over_TwoZb + e2;
            double pvc = -e1_Over_TwoZb + e2_Over_TwoZb;

            double vpc = (z1e1_Over_TwoZb - z2e2_Over_TwoZb + e2) * z2;
            double vvc = -z1e1_Over_TwoZb + z2e2_Over_TwoZb;

            return new Spring(ppc, pvc, vpc, vvc);

        } else if (dr < 1.0 - EPSILON) {
            double omegaZeta = af * dr;
            double alpha = af * Math.sqrt(1.0 - dr * dr);

            double expTerm = Math.exp(-omegaZeta * deltaTime);
            double cosTerm = Math.cos(alpha * deltaTime);
            double sinTerm = Math.sin(alpha * deltaTime);

            double invAlpha = 1.0 / alpha;

            double expSin = expTerm * sinTerm;
            double expCos = expTerm * cosTerm;
            double expOmegaZetaSin_Over_Alpha = expTerm * omegaZeta * sinTerm * invAlpha;

            double ppc = expCos + expOmegaZetaSin_Over_Alpha;
            double pvc = expSin * invAlpha;

            double vpc = -expSin * alpha - omegaZeta * expOmegaZetaSin_Over_Alpha;
            double vvc = expCos - expOmegaZetaSin_Over_Alpha;

            return new Spring(ppc, pvc, vpc, vvc);

        } else {
            double expTerm = Math.exp(-af * deltaTime);
            double timeExp = deltaTime * expTerm;
            double timeExpFreq = timeExp * af;

            double ppc = timeExpFreq + expTerm;
            double pvc = timeExp;

            double vpc = -af * timeExpFreq;
            double vvc = -timeExpFreq + expTerm;

            return new Spring(ppc, pvc, vpc, vvc);
        }
    }

    /**
     * Advances the spring simulation by one step.
     * Calculates the new position and velocity based on the pre-computed coefficients.
     */
    public double[] update(double pos, double vel, double equilibriumPos) {
        double oldPos = pos - equilibriumPos;
        double newPos = oldPos * posPosCoef + vel * posVelCoef + equilibriumPos;
        double newVel = oldPos * velPosCoef + vel * velVelCoef;
        return new double[]{newPos, newVel};
    }
}
