package javahints;

import java.awt.Font;

public class TestShortErrorsNewClass2 {
    
    public void test() {
        new java.awt.Font(getSetting("fontName"), Font.BOLD, 12);
    }
    
    private Object getSetting(String setting) {
        return setting;
    }
    
}
