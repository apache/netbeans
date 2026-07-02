package org.netbeans.test.java.hints;

public class UnknownReturnType {


    public static void main(String[] args) {
        new UnknownReturnType().method(args);
    }

    void method(String[] args){

        var whoKnows = whoKnows(args);
        methodB(whoKnows);
    }

    void methodB(String x){

    }
}
