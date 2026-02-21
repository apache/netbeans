package org.netbeans.test.java.hints;

public class KnownReturnType {


    public static void main(String[] args) {
        new KnownReturnType().method(args);
    }

    void method(String[] args){

        int whoKnows = whoKnows(args);
        methodB(whoKnows);
    }

    void methodB(String x){

    }
}

