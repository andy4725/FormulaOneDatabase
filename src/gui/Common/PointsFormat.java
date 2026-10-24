package gui.Common;

/** Turns championship points into text, the same on every screen. */
public final class PointsFormat {

    private PointsFormat() {
        // Only static methods; no instances needed.
    }

    /** 25.0 becomes "25"; 12.5 stays "12.5" (no ".0" for whole numbers). */
    public static String format(double points) {
        if (points == Math.floor(points)) {
            return String.valueOf((long) points);
        }
        return String.valueOf(points);
    }
}
