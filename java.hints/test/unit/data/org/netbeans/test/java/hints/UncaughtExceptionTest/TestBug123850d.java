package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug123850d extends Fire6 implements Fire5 {
    public void run() {
        new FileReader("foo");
    }
}

interface Fire5 {
    public void run() throws java.io.FileNotFoundException;
}

abstract class Fire6 {
    public abstract void run() throws java.io.IOException;
}
