package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug123850a implements Runnable {
    public void run() {
        new FileReader("foo");
    }
}
