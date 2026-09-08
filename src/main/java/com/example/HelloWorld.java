package com.example;

public class HelloWorld {
    public String greeting() {
        return "Hello, World!";
    }

    public static void main(String[] args) {
        System.out.println(new HelloWorld().greeting());
    }
}
