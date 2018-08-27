/*
 * Initial Comment Fold
 * Created on 15 September 2004, 10:48
 */

package code_folds.JavaFoldsTest;

// import section fold
import javax.swing.JApplet;
import javax.swing.JButton;


/**
 * Outer Class Javadoc Fold
 * Demonstration of Code Folding functionality file.
 * @author Martin Roskanin
 */
public class testJavaFolds {

    /** One Line Field Javadoc Fold*/
    JButton button;

    /**
     *  Multi-line Field Favadoc Fold
     */
    JApplet applet;

    /** One-line Constructor Javadoc Fold */
    public testJavaFolds() { } //One-line Constructor Fold

    /**
     *  Multi-line Constructor Javadoc Fold
     */
    public testJavaFolds(String s) { //Multi-line Constructor Fold
        button = new JButton();
        applet = new JApplet();
    }


    /** One-line Method Javadoc Fold */
    public void methodOne(){ } // One-line Method Fold

    /**
     *  Multi-line Method Javadoc Fold
     */
    public void methodTwo(){ // Multi-line Method Fold
        System.out.println(""); //NOI18N
    }

    public void firstMethod(){ } public void secondMethod(){ } public void thirdMethod(){ }

    /** One-line InnerClass Javadoc Fold */
    public static class InnerClassOne{ }

    /**
     *  Multi-line InnerClass Javadoc Fold
     */
    public static class InnerClassTwo{ //Multi-line InnerClass Fold
    }

    public static class InnerClassThree{
        /** One Line InnerClass Field Javadoc Fold*/
        JButton button;

        /**
         *  Multi-line InnerClass Field Favadoc Fold
         */
        JApplet applet;

        /** One-line InnerClass Constructor Javadoc Fold */
        public InnerClassThree() { } //One-line InnerClass Constructor Fold

        /**
         *  Multi-line InnerClass Constructor Javadoc Fold
         */
        public InnerClassThree(String s) { //Multi-line InnerClass Constructor Fold
            button = new JButton();
            applet = new JApplet();
        }


        /** One-line InnerClass Method Javadoc Fold */
        public void methodOne(){ } // One-line InnerClass Method Fold

        /**
         *  Multi-line InnerClass Method Javadoc Fold
         */
        public void methodTwo(){ // Multi-line InnerClass Method Fold
            System.out.println(""); //NOI18N
        }

        public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }
    }

    public static class InnerClassFour{
        public InnerClassFour(){
        }

        /** One-line InnerClassInInnerClass Javadoc Fold */
        public static class InnerClassInInnerClassOne{ } //One-line InnerClassInInnerClass Fold

        /**
         *   Multi-line InnerClassInInnerClass Javadoc Fold
         *
         */
        public static class InnerClassInInnerClassTwo{ //Multi-line InnerClassInInnerClass Fold
            /** One Line InnerClassInInnerClass Field Javadoc Fold*/
            JButton button;

            /**
             *  Multi-line InnerClassInInnerClass Field Favadoc Fold
             */
            JApplet applet;

            /** One-line InnerClassInInnerClass Constructor Javadoc Fold */
            public InnerClassInInnerClassTwo() { } //One-line InnerClassInInnerClassTwo Constructor Fold

            /**
             *  Multi-line InnerClassInInnerClassTwo Constructor Javadoc Fold
             */
            public InnerClassInInnerClassTwo(String s) { //Multi-line InnerClassInInnerClass Constructor Fold
                button = new JButton();
                applet = new JApplet();
            }

            /** One-line InnerClassInInnerClass Method Javadoc Fold */
            public void methodOne(){ } // One-line InnerClassInInnerClass Method Fold

            /**
             *  Multi-line InnerClassInInnerClass Method Javadoc Fold
             */
            public void methodTwo(){ // Multi-line InnerClassInInnerClass Method Fold
                System.out.println(""); //NOI18N
            }

            public void firstMethod(){ }  public void secondMethod(){ } public void thirdMethod(){ }

        }

    }

}
