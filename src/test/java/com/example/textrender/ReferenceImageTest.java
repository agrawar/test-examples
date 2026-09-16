package com.example.textrender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Golden-image coverage for the render flow. The font is vendored under test resources so the output
 * does not depend on the network or on whichever fonts the host machine happens to have installed.
 *
 * <p>Regenerate the reference after an intentional rendering change with
 * {@code mvn test -Dtest=ReferenceImageTest -Dreference.update=true}.
 */
class ReferenceImageTest {

    private static final String FONT_RESOURCE = "/fonts/MoreSugar-Regular.otf";
    private static final Path REFERENCE_SOURCE = Path.of("src/test/resources/reference/hello-world-64pt.png");
    private static final Path DIFF_OUTPUT = Path.of("target/reference-diff");

    private static final String TEXT = "Hello world";
    private static final float FONT_SIZE = 64f;
    private static final int PADDING = 16;

    /** Antialiased glyph edges can shift slightly between JDK builds, so allow a small channel drift. */
    private static final int CHANNEL_TOLERANCE = 8;

    /** At most this share of pixels may differ from the reference at all. */
    private static final double MAX_DIFFERING_RATIO = 0.005;

    private static String fontUrl;

    @BeforeAll
    static void locateFont() throws URISyntaxException {
        URL resource = ReferenceImageTest.class.getResource(FONT_RESOURCE);
        assertNotNull(resource, "Missing test font at " + FONT_RESOURCE);
        fontUrl = resource.toURI().toString();
    }

    private static byte[] render() {
        return new TextRenderService(TEXT, fontUrl, FONT_SIZE, PADDING).renderText(fontUrl, TEXT, FONT_SIZE, PADDING);
    }

    private static BufferedImage decode(byte[] png) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image, "Rendered bytes were not a decodable image");
        return image;
    }

    /**
     * Reads the reference from the source tree rather than the classpath, so that a regenerated image
     * is visible to the same run that wrote it.
     */
    private static BufferedImage loadReference() throws IOException {
        if (Boolean.getBoolean("reference.update")) {
            Files.createDirectories(REFERENCE_SOURCE.getParent());
            Files.write(REFERENCE_SOURCE, render());
        }
        if (!Files.isReadable(REFERENCE_SOURCE)) {
            return fail("Missing reference image at " + REFERENCE_SOURCE
                    + ". Generate it with: mvn test -Dtest=ReferenceImageTest -Dreference.update=true");
        }
        try (InputStream stored = Files.newInputStream(REFERENCE_SOURCE)) {
            BufferedImage reference = ImageIO.read(stored);
            assertNotNull(reference, "Reference image could not be decoded");
            return reference;
        }
    }

    @Test
    void matchesTheReferenceImage() throws IOException {
        // Arrange
        BufferedImage reference = loadReference();

        // Act
        BufferedImage rendered = decode(render());

        // Assert
        assertEquals(reference.getWidth(), rendered.getWidth(), "Rendered width drifted from the reference");
        assertEquals(reference.getHeight(), rendered.getHeight(), "Rendered height drifted from the reference");

        ImageComparison comparison = ImageComparison.of(reference, rendered, CHANNEL_TOLERANCE);
        int allowedDifferingPixels = (int) Math.floor(comparison.totalPixels() * MAX_DIFFERING_RATIO);

        if (comparison.pixelsBeyondTolerance() > 0 || comparison.differingPixels() > allowedDifferingPixels) {
            writeFailureArtifacts(reference, rendered);
            fail("Rendered image drifted from the reference: " + comparison + ". Allowed at most "
                    + allowedDifferingPixels + " differing pixels and a channel delta of " + CHANNEL_TOLERANCE
                    + ". Wrote the rendered image and a diff to " + DIFF_OUTPUT.toAbsolutePath());
        }
    }

    @Test
    void repeatedRendersAreByteIdentical() {
        // Arrange
        byte[] first = render();

        // Act & Assert
        for (int attempt = 2; attempt <= 5; attempt++) {
            assertArrayEquals(first, render(), "Render attempt " + attempt + " differed from the first");
        }
    }

    @RepeatedTest(5)
    void everyRenderMatchesTheReferencePixelForPixel() throws IOException {
        // Arrange
        BufferedImage reference = loadReference();

        // Act
        ImageComparison comparison = ImageComparison.of(reference, decode(render()), CHANNEL_TOLERANCE);

        // Assert
        assertEquals(0, comparison.pixelsBeyondTolerance(), () -> "Pixels drifted beyond tolerance: " + comparison);
        assertTrue(
                comparison.differingRatio() <= MAX_DIFFERING_RATIO,
                () -> "Too many pixels differ from the reference: " + comparison);
    }

    private static void writeFailureArtifacts(BufferedImage reference, BufferedImage rendered) throws IOException {
        Files.createDirectories(DIFF_OUTPUT);
        ImageIO.write(rendered, "png", DIFF_OUTPUT.resolve("actual.png").toFile());
        ImageIO.write(diffOf(reference, rendered), "png", DIFF_OUTPUT.resolve("diff.png").toFile());
    }

    /** Paints every differing pixel opaque red over a faded copy of the reference. */
    private static BufferedImage diffOf(BufferedImage reference, BufferedImage rendered) {
        BufferedImage diff =
                new BufferedImage(reference.getWidth(), reference.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < reference.getHeight(); y++) {
            for (int x = 0; x < reference.getWidth(); x++) {
                int expected = reference.getRGB(x, y);
                int actual = rendered.getRGB(x, y);
                if (expected == actual) {
                    diff.setRGB(x, y, expected & 0x30FFFFFF);
                } else {
                    diff.setRGB(x, y, 0xFFFF0000);
                }
            }
        }
        return diff;
    }
}
