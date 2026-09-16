package com.example.textrender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TextRenderingTest {

    private static final List<Path> CANDIDATE_FONTS = List.of(
            Path.of("/System/Library/Fonts/Supplemental/Arial.ttf"),
            Path.of("/System/Library/Fonts/Supplemental/Andale Mono.ttf"),
            Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
            Path.of("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"));

    /** Every PNG begins with these eight bytes. */
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

    private static String fontUrl;

    @BeforeAll
    static void locateFont() {
        Optional<Path> available = CANDIDATE_FONTS.stream().filter(Files::isReadable).findFirst();
        assumeTrue(available.isPresent(), "No system font available to use as a fixture");
        fontUrl = available.get().toUri().toString();
    }

    private static TextRenderService service() {
        return new TextRenderService("Hello, World!", fontUrl, 24f, 8);
    }

    private static BufferedImage decode(byte[] png) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image, "Returned bytes were not a decodable image");
        return image;
    }

    private static boolean hasVisiblePixels(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void returnsAPng() throws IOException {
        // Act
        byte[] png = service().renderText(fontUrl, "Hello, World!", 24f, 8);

        // Assert
        assertTrue(png.length > PNG_SIGNATURE.length, "Expected a non-trivial image");
        assertArrayEquals(PNG_SIGNATURE, Arrays.copyOf(png, PNG_SIGNATURE.length));
        assertNotNull(decode(png));
    }

    @Test
    void drawsTheTextOntoTheImage() throws IOException {
        // Act
        BufferedImage image = decode(service().renderText(fontUrl, "Hello, World!", 24f, 8));

        // Assert
        assertTrue(hasVisiblePixels(image), "Expected the rendered text to mark the image");
    }

    @Test
    void padsTheImageOnEverySide() throws IOException {
        // Arrange
        String text = "Hello, World!";
        int padding = 10;

        // Act
        BufferedImage unpadded = decode(service().renderText(fontUrl, text, 24f, 0));
        BufferedImage padded = decode(service().renderText(fontUrl, text, 24f, padding));

        // Assert
        assertEquals(unpadded.getWidth() + padding * 2, padded.getWidth());
        assertEquals(unpadded.getHeight() + padding * 2, padded.getHeight());
    }

    @Test
    void largerFontSizeProducesALargerImage() throws IOException {
        // Arrange
        String text = "Hello, World!";

        // Act
        BufferedImage small = decode(service().renderText(fontUrl, text, 12f, 0));
        BufferedImage large = decode(service().renderText(fontUrl, text, 48f, 0));

        // Assert
        assertTrue(large.getWidth() > small.getWidth(), "Expected a wider image at a larger font size");
        assertTrue(large.getHeight() > small.getHeight(), "Expected a taller image at a larger font size");
    }

    @Test
    void longerTextProducesAWiderImage() throws IOException {
        // Act
        BufferedImage shortText = decode(service().renderText(fontUrl, "Hi", 24f, 0));
        BufferedImage longText = decode(service().renderText(fontUrl, "Hi there, world", 24f, 0));

        // Assert
        assertTrue(longText.getWidth() > shortText.getWidth());
        assertEquals(shortText.getHeight(), longText.getHeight(), "Single line height should not change");
    }

    @Test
    void rendersEmptyTextAsAValidImage() throws IOException {
        // Act
        BufferedImage image = decode(service().renderText(fontUrl, "", 24f, 4));

        // Assert
        assertTrue(image.getWidth() >= 1);
        assertTrue(image.getHeight() >= 1);
    }

    @Test
    void backgroundIsTransparent() throws IOException {
        // Act
        BufferedImage image = decode(service().renderText(fontUrl, "Hello", 24f, 6));

        // Assert
        assertEquals(0, image.getRGB(0, 0) >>> 24, "Expected the top-left padding pixel to be transparent");
    }
}
