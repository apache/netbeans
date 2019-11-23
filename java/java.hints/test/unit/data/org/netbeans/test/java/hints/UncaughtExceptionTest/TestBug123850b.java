package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug123850b implements Fire {
    public void run() {
        new FileReader("foo");
    }
}

interface Fire {
    public void run() throws java.io.IOException;
}
