package com.example.textrender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Exercises the real font host. Run offline builds with -DexcludedGroups=network. */
@Tag("network")
class LiveFontUrlTest {

    private static final String FONT_URL = "https://font-public.canva.com/YAFdJkVWBPo/0/"
            + "MoreSugar-Regular.62992e429acdec5e01c3db.6f7a950ef2bb9f1314d37ac4a660925e.otf";

    /** OpenType fonts with CFF outlines open with the ASCII tag OTTO. */
    private static final byte[] OPENTYPE_CFF_SIGNATURE = "OTTO".getBytes(StandardCharsets.US_ASCII);

    /** Every PNG begins with these eight bytes. */
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'};

    private static TextRenderService service() {
        return new TextRenderService("Hello, World!", FONT_URL, 24f, 8);
    }

    /** Skips rather than fails when the host cannot be reached, so an outage is not a red build. */
    private static <T> T skippingWhenHostUnreachable(Supplier<T> download) {
        try {
            return download.get();
        } catch (FontLoadException e) {
            if (e.getCause() instanceof IOException) {
                return abort("Could not reach " + FONT_URL + ": " + e.getCause());
            }
            throw e;
        }
    }

    @Test
    void downloadsOpenTypeFontBytes() {
        // Act
        byte[] fontBytes = skippingWhenHostUnreachable(() -> service().fetchFontBytes(FONT_URL));

        // Assert
        assertTrue(fontBytes.length > 0, "Expected font bytes from " + FONT_URL);
        assertArrayEquals(OPENTYPE_CFF_SIGNATURE, Arrays.copyOf(fontBytes, OPENTYPE_CFF_SIGNATURE.length));
    }

    @Test
    void loadsMoreSugarAtTheRequestedSize() {
        // Act
        Font font = skippingWhenHostUnreachable(() -> service().loadFont(FONT_URL, 32f));

        // Assert
        assertEquals("More Sugar", font.getFamily());
        assertEquals(32f, font.getSize2D());
        assertEquals(-1, font.canDisplayUpTo("Hello, World!"), "Font should cover the text being rendered");
    }

    @Test
    void rendersHelloWorldToAPng() throws IOException {
        // Arrange
        String text = "Hello world";
        float fontSize = 64f;
        int padding = 16;
        TextRenderService service = new TextRenderService(text, FONT_URL, fontSize, padding);

        // Act
        byte[] png = skippingWhenHostUnreachable(() -> service.renderText(FONT_URL, text, fontSize, padding));

        // Assert
        assertArrayEquals(PNG_SIGNATURE, Arrays.copyOf(png, PNG_SIGNATURE.length), "Expected PNG bytes");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertNotNull(image, "Returned bytes were not a decodable image");
        assertTrue(image.getWidth() > padding * 2, "Expected the glyphs to widen the image beyond the padding");
        assertTrue(image.getHeight() > padding * 2, "Expected the glyphs to deepen the image beyond the padding");
        assertEquals(0, image.getRGB(0, 0) >>> 24, "Expected a transparent background in the padding");
        assertTrue(countOpaquePixels(image) > 0, "Expected the text to be drawn onto the image");
    }

    private static int countOpaquePixels(BufferedImage image) {
        int opaque = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    opaque++;
                }
            }
        }
        return opaque;
    }
}
