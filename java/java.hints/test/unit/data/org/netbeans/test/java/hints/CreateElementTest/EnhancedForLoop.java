package org.netbeans.test.java.hints;

public class EnhancedForLoop {
    
    public EnhancedForLoop() {
    }
    
    public static void main(String[] args) {
        for (String s : u) {
	}
        for (java.util.List<? extends String> s : u) {
	}
    }
}
