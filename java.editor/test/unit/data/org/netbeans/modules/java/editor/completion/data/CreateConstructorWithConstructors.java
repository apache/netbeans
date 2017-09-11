package org.netbeans.modules.java.editor.completion.ElementCreatingJavaCompletionProviderTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Test {

    private int time;
    private double something = 0.0;
    private String text = "";
    private List<String> list;

    public Test() {}
    public Test(int time, List<String> list) {}

    private static class X implements Runnable {

        private byte a;
        private Map<String, Set<String>> map1;
        private Map<String, Set<String>> map2 = new HashMap();

        public X() {}
        public X(byte a, Map<String, Set<String>> map1) {}

        public void run() {
        }

    }

}
