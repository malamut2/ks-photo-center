package de.wolfgangkronberg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoomLevelStepsTest {

    class ZoomTest {

        final GlobalElements ge;
        final ZoomLevelSteps steps;
        final double next;
        final double previous;

        ZoomTest(final double zoom) {
            ge = new GlobalElements() {{
                setImageZoom(zoom);
            }};
            steps = new ZoomLevelSteps(ge);
            next = steps.stepify(true);
            previous = steps.stepify(false);
        }

    }

    @Test
    void stepify() {

        ZoomTest zt;

        zt = new ZoomTest(1.05);
        assertEquals(0.75d, zt.previous);
        assertEquals(1.25d, zt.next);

        zt = new ZoomTest(10.5);
        assertEquals(7.5d, zt.previous);
        assertEquals(12.5d, zt.next);

        zt = new ZoomTest(0.105);
        assertEquals(0.075d, zt.previous);
        assertEquals(0.125d, zt.next);

        zt = new ZoomTest(3.6);
        assertEquals(3d, zt.previous);
        assertEquals(5d, zt.next);

        zt = new ZoomTest(4.001);
        assertEquals(3d, zt.previous);
        assertEquals(5d, zt.next);

    }

}