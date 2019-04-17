package de.wolfgangkronberg;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ZoomLevelSteps {

    private static final List<Double> normalizedSteps = Arrays.asList(1d, 1.25d, 1.5d, 1.75d, 2d, 3d, 4d, 5d, 7.5d);

    private final double zoom;
    private int pow10;  // normalizedZoom * pow10 = zoom
    private double normalizedZoom;  // in the range 0.875 .. 8.75

    public ZoomLevelSteps(GlobalElements ge) {
        double zoom = ge.getImageZoom();
        if (zoom < 0) {
            zoom = ge.getEffectiveImageZoom();
        }
        this.zoom = zoom;
        normalize();
    }

    public double stepify(boolean forward) {
        int bestIndex = IntStream.range(0, normalizedSteps.size()).boxed()
                .min((i1, i2) -> (int) Math.signum(Math.abs(
                        normalizedSteps.get(i1) - normalizedZoom)
                        - Math.abs(normalizedSteps.get(i2) - normalizedZoom)))
                .orElse(0);
        if (forward) {
            bestIndex++;
        } else {
            bestIndex--;
        }
        int myPow10 = pow10;
        if (bestIndex >= normalizedSteps.size()) {
            bestIndex -= normalizedSteps.size();
            myPow10++;
        } else if (bestIndex < 0) {
            bestIndex += normalizedSteps.size();
            myPow10--;
        }
        return normalizedSteps.get(bestIndex) * Math.pow(10d, myPow10);
    }

    private void normalize() {
        pow10 = (int) Math.floor(Math.log10(zoom));
        normalizedZoom = zoom / Math.pow(10, pow10);
        if (normalizedZoom > 8.75) {
            pow10++;
            normalizedZoom /= 10;
        }
    }

}
