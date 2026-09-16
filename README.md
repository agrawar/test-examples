# hello-world

At Canva, millions of users create designs with custom text and typography. A critical part of our platform is rendering text with various fonts and styles.
You've been asked to create a text rendering service prototype that will be used by our design editor. This service should accept text content and a font url, then return an image of the rendered text. An interface to implement the service will be provided. Throughout the problem we’ll aim to evolve this from a prototype to something production-like.
This is likely to be an unfamiliar domain space for you and you're welcome and encouraged to use AI tools during this process to assist with implementation.

Requirements
1. Pass in the text content
2. Retrieve the font from the url 
3. Format the text content to match the font --> convert into an image
4. Return that image to the user

Domains
1. Input - text context and font url 
2. TextRenderService - 


public interface TextRenderService {
    byte[] renderText(String fontUrl, String text, float fontSize, int padding);
}
