package com.example.textrender;

import java.awt.image.BufferedImage;

/**
 * Compares two images on two axes: how far any single pixel deviates (maxChannelDelta) and how many
 * pixels deviate at all (differingPixels).
 */
record ImageComparison(int differingPixels, int pixelsBeyondTolerance, int totalPixels, int maxChannelDelta) {

    static ImageComparison of(BufferedImage expected, BufferedImage actual, int tolerance) {
        if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
            throw new IllegalArgumentException("Images must have the same dimensions to be compared");
        }

        int differing = 0;
        int beyondTolerance = 0;
        int worstDelta = 0;

        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                int expectedPixel = expected.getRGB(x, y);
                int actualPixel = actual.getRGB(x, y);
                if (expectedPixel == actualPixel) {
                    continue;
                }
                differing++;
                int delta = maxChannelDelta(expectedPixel, actualPixel);
                worstDelta = Math.max(worstDelta, delta);
                if (delta > tolerance) {
                    beyondTolerance++;
                }
            }
        }

        return new ImageComparison(differing, beyondTolerance, expected.getWidth() * expected.getHeight(), worstDelta);
    }

    /** The largest absolute difference across the alpha, red, green and blue channels. */
    private static int maxChannelDelta(int expected, int actual) {
        int delta = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int expectedChannel = (expected >> shift) & 0xFF;
            int actualChannel = (actual >> shift) & 0xFF;
            delta = Math.max(delta, Math.abs(expectedChannel - actualChannel));
        }
        return delta;
    }

    double differingRatio() {
        return totalPixels == 0 ? 0 : (double) differingPixels / totalPixels;
    }

    @Override
    public String toString() {
        return String.format(
                "%d/%d pixels differ (%.4f%%), %d beyond tolerance, worst channel delta %d",
                differingPixels, totalPixels, differingRatio() * 100, pixelsBeyondTolerance, maxChannelDelta);
    }
}
