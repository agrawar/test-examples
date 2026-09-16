package com.example.textrender;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import javax.imageio.ImageIO;

public class TextRenderService implements ITextRenderService {

    private final String textContent;
    private final String fontUrl;
    private final float fontSize;
    private final int padding;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public TextRenderService(String textContent, String fontUrl, float fontSize, int padding) {
        this.textContent = textContent;
        this.fontUrl = fontUrl;
        this.fontSize = fontSize;
        this.padding = padding;
    }

    public String textContent() {
        return textContent;
    }

    public String fontUrl() {
        return fontUrl;
    }

    public float fontSize() {
        return fontSize;
    }

    public int padding() {
        return padding;
    }

    @Override
    public byte[] renderText(String fontUrl, String text, float fontSize, int padding) {
        Objects.requireNonNull(fontUrl);
        Objects.requireNonNull(text);
        Objects.requireNonNull(fontSize);
        Objects.requireNonNull(padding);
        if (fontSize <= 0) {
            throw new IllegalArgumentException("Font size must be greater than 0");
        }
        if (padding < 0) {
            throw new IllegalArgumentException("Padding must be greater than or equal to 0");
        }

        Font font = loadFont(fontUrl, fontSize);
        BufferedImage image = drawText(font, text, padding);
        return encodeAsPng(image);
    }

    BufferedImage drawText(Font font, String text, int padding) {
        FontRenderContext measureContext = new FontRenderContext(null, true, true);
        Rectangle2D bounds = font.getStringBounds(text, measureContext);
        LineMetrics lineMetrics = font.getLineMetrics(text, measureContext);

        int width = Math.max(1, (int) Math.ceil(bounds.getWidth()) + padding * 2);
        int height = Math.max(1, (int) Math.ceil(bounds.getHeight()) + padding * 2);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setFont(font);
            graphics.setColor(Color.BLACK);
            // Shift by the bounds origin so glyphs that overhang to the left are not clipped.
            graphics.drawString(text, padding - (float) bounds.getX(), padding + lineMetrics.getAscent());
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private byte[] encodeAsPng(BufferedImage image) {
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        try {
            if (!ImageIO.write(image, "png", encoded)) {
                throw new IllegalStateException("No PNG writer is available in this runtime");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not encode the rendered text as a PNG", e);
        }
        return encoded.toByteArray();
    }

    Font loadFont(String fontUrl, float fontSize) {
        byte[] fontBytes = fetchFontBytes(fontUrl);
        try (InputStream fontStream = new ByteArrayInputStream(fontBytes)) {
            return Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(fontSize);
        } catch (FontFormatException | IOException e) {
            throw new FontLoadException(fontUrl + " did not contain a readable TrueType or OpenType font", e);
        }
    }

    byte[] fetchFontBytes(String fontUrl) {
        URI uri;
        try {
            uri = new URI(fontUrl);
        } catch (URISyntaxException e) {
            throw new FontLoadException("Font url is not a valid URI: " + fontUrl, e);
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        return switch (scheme) {
            case "http", "https" -> fetchOverHttp(uri);
            case "file" -> readFromDisk(uri);
            default -> throw new FontLoadException(
                    "Unsupported font url scheme in " + fontUrl + "; expected http, https or file");
        };
    }

    private byte[] fetchOverHttp(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .GET()
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new FontLoadException("Could not download font from " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FontLoadException("Interrupted while downloading font from " + uri, e);
        }

        if (response.statusCode() != 200) {
            throw new FontLoadException("Font download from " + uri + " returned HTTP " + response.statusCode());
        }
        byte[] body = response.body();
        if (body.length == 0) {
            throw new FontLoadException("Font download from " + uri + " returned an empty body");
        }
        return body;
    }

    private byte[] readFromDisk(URI uri) {
        try {
            return Files.readAllBytes(Path.of(uri));
        } catch (IOException | IllegalArgumentException e) {
            throw new FontLoadException("Could not read font file at " + uri, e);
        }
    }
}
