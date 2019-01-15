package org.netbeans.test.java.editor.actions.JavaEditActionsTest;

public class testCommentUncomment {

    public void code() {
        System.out.println("1");
    }

    public void code2() {
        System.out.println("1");
//        System.out.println("2");
    }

    public void code3() {
        System.out.println("1");
        //System.out.println("2");
    }

    public void code4() {
//        System.out.println("1");
        //System.out.println("2");
    }
}