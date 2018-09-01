package org.netbeans.modules.java.editor.semantic.data;

import java.util.ArrayList;

public class NewArrayIsClassUse {
    
    public static void test() {
        Object nue = new ArrayList[0];
    }
    
}
