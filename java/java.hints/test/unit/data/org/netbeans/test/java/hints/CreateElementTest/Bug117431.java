package org.netbeans.test.java.hints;

public class Bug117431 {

    public Bug117431() {
    }

    public static void main(String[] args) {
        for(ii = 0; ii < args.length; ii++) {
        }
        for(ii = 0; kk < args.length; ii++) {
        }
    }
}
