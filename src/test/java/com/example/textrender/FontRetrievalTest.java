package com.example.textrender;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FontRetrievalTest {

    private static final List<Path> CANDIDATE_FONTS = List.of(
            Path.of("/System/Library/Fonts/Supplemental/Arial.ttf"),
            Path.of("/System/Library/Fonts/Supplemental/Andale Mono.ttf"),
            Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
            Path.of("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"));

    private static Path fontFile;
    private static byte[] fontBytes;
    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws IOException {
        Optional<Path> available = CANDIDATE_FONTS.stream().filter(Files::isReadable).findFirst();
        assumeTrue(available.isPresent(), "No system font available to use as a fixture");
        fontFile = available.get();
        fontBytes = Files.readAllBytes(fontFile);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/font.ttf", exchange -> respond(exchange, 200, fontBytes));
        server.createContext("/garbage", exchange -> respond(exchange, 200, "not a font".getBytes(StandardCharsets.UTF_8)));
        server.createContext("/missing", exchange -> respond(exchange, 404, new byte[0]));
        server.createContext("/empty", exchange -> respond(exchange, 200, new byte[0]));
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }

    private static TextRenderService serviceFor(String fontUrl) {
        return new TextRenderService("Hello, World!", fontUrl, 24f, 8);
    }

    @Test
    void downloadsFontBytesOverHttp() {
        // Arrange
        String fontUrl = baseUrl + "/font.ttf";

        // Act
        byte[] downloaded = serviceFor(fontUrl).fetchFontBytes(fontUrl);

        // Assert
        assertArrayEquals(fontBytes, downloaded);
    }

    @Test
    void loadsFontAtRequestedSize() {
        // Arrange
        String fontUrl = baseUrl + "/font.ttf";

        // Act
        Font font = serviceFor(fontUrl).loadFont(fontUrl, 24f);

        // Assert
        assertEquals(24f, font.getSize2D());
    }

    @Test
    void loadsTheFontServedAtThatUrl() throws FontFormatException, IOException {
        // Arrange
        String fontUrl = baseUrl + "/font.ttf";
        Font expected = Font.createFont(Font.TRUETYPE_FONT, fontFile.toFile());

        // Act
        Font downloaded = serviceFor(fontUrl).loadFont(fontUrl, 24f);

        // Assert
        assertEquals(expected.getFontName(), downloaded.getFontName());
        assertEquals(expected.getFamily(), downloaded.getFamily());
    }

    @Test
    void readsFontFromFileUrl() {
        // Arrange
        String fontUrl = fontFile.toUri().toString();

        // Act
        Font font = serviceFor(fontUrl).loadFont(fontUrl, 16f);

        // Assert
        assertEquals(16f, font.getSize2D());
    }

    @Test
    void failsWhenFontIsNotFound() {
        // Arrange
        String fontUrl = baseUrl + "/missing";

        // Act & Assert
        FontLoadException thrown =
                assertThrows(FontLoadException.class, () -> serviceFor(fontUrl).loadFont(fontUrl, 24f));
        assertEquals(true, thrown.getMessage().contains("404"));
    }

    @Test
    void failsWhenUrlReturnsNoFontBytes() {
        // Arrange
        String fontUrl = baseUrl + "/empty";

        // Act & Assert
        FontLoadException thrown =
                assertThrows(FontLoadException.class, () -> serviceFor(fontUrl).loadFont(fontUrl, 24f));
        assertTrue(thrown.getMessage().contains("empty body"));
    }

    @Test
    void failsWhenResponseIsNotAFont() {
        // Arrange
        String fontUrl = baseUrl + "/garbage";

        // Act & Assert
        assertThrows(FontLoadException.class, () -> serviceFor(fontUrl).loadFont(fontUrl, 24f));
    }

    @Test
    void rejectsUnsupportedScheme() {
        // Arrange
        String fontUrl = "ftp://fonts.example.com/Roboto-Regular.ttf";

        // Act & Assert
        assertThrows(FontLoadException.class, () -> serviceFor(fontUrl).loadFont(fontUrl, 24f));
    }
}
