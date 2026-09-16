package com.example.textrender;

public interface ITextRenderService {
    byte[] renderText(String fontUrl, String text, float fontSize, int padding);
}
