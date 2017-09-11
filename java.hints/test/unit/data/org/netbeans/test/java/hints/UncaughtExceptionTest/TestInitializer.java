package org.netbeans.test.java.hints;

import java.net.URL;

public class TestInitializer {
    static {
        new URL("http://www.test.com");
    }
}
