package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug123850e extends Fire8 implements Fire7 {
    public void run() {
        new FileReader("foo");
    }
}

interface Fire7 {
    public void run() throws java.io.IOException;
}

abstract class Fire8 {
    public abstract void run() throws java.io.FileNotFoundException;
}
