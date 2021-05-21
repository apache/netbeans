package org.netbeans.modules.java.editor.semantic.data;

public class BrokenAfterRewrite {
    
    StringBuilder text = new StringBuilder();
        
    public String text() {
        text.append(text());
        text = null;
        test(text(), text());
        
        text();
        return "";
    }
    
    private void test(String s1, String s2) {}
}
