package org.netbeans.test.java.hints;

import java.io.FileReader;
import java.io.IOException;

public class TestBug123093 {
    public static void main(String[] args) {
        try {
            new FileReader("");
        } catch (IOException ex) {
            throw new IOException();
        }
    }

}
