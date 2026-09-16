package com.example.textrender;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TextRenderServiceTest {

    @Test
    void storesValuesCorrectly() {
        // Arrange
        String textContent = "Hello, World!";
        String fontUrl = "https://fonts.example.com/Roboto-Regular.ttf";
        float fontSize = 24f;
        int padding = 8;

        // Act
        TextRenderService request = new TextRenderService(textContent, fontUrl, 24f, 8);
        // request.renderText(fontUrl, textContent, 24f, 8);

        // Assert
        assertEquals(textContent, request.textContent());
        assertEquals(fontUrl, request.fontUrl());
        assertEquals(24f, request.fontSize());
        assertEquals(8, request.padding());
    }

    @Test
    void preservesTextContentExactly() {
        // Arrange
        String textContent = "  Canva \u2014 \u65e5\u672c\u8a9e  ";
        String fontUrl = "https://fonts.example.com/NotoSansJP-Regular.otf";

        // Act
        TextRenderService request = new TextRenderService(textContent, fontUrl, 16f, 0);

        // Assert
        assertEquals(textContent, request.textContent());
        assertEquals(fontUrl, request.fontUrl());
    }
    
}
