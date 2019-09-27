package org.netbeans.modules.java.editor.semantic.data;

import java.util.Collections;
import javax.swing.SwingUtilities;

public class LambdaAndFunctionType {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {System.err.println("asf");});
        Collections.<String>sort(null, (String o1, String o2) -> {return o1.compareTo("");});
        Collections.<String>sort(null, (String o1, String o2) -> o1.compareTo(""));
        Collections.<String>sort(null, String::compareTo);
    }
}
