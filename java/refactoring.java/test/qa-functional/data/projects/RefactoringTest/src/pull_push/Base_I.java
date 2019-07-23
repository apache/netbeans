package pull_push;

public class Base_I {

    private abstract class ClassA {

    }

    private class ClassB extends ClassA implements InterfaceA {

        public int fact(int i) {
            if (i > 0) {
                return fact(i - 1) * i;
            } else {
                return 1;
            }
        }

        public void prinSomething() {
            System.out.println("aaa");
        }

        public String getSequence(int i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; j++) {
                sb.append(j);
                sb.append("-");
            }
            return sb.toString();
        }

        public void method() {
            System.out.println("method()");
        }
        
        // comment of class
        class c1{ int a = 5; }
        
        // comment of method
        public void m1(){
            int[][] m = new int[][]{{1, 2, 3}, {1, 2, 3}, {1, 2, 3}};
        }
    }

    private class ClassC extends ClassB {

    }

    private class ClassD extends ClassB {

    }

    public interface InterfaceA {

        public String getSequence(int i);
        public void method();
    }
}
