package org.netbeans.test.java.hints;

import java.io.FileReader;

public class TestBug123850c extends Fire4 implements Fire3 {
    public void run() {
        new FileReader("foo");
    }
}

interface Fire3 {
    public void run() throws java.io.IOException;
}

abstract class Fire4 {
    public abstract void run() throws java.beans.PropertyVetoException;
}
