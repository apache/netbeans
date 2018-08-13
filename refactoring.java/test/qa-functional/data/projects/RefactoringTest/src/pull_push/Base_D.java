package pull_push;

public class Base_D {

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

        @Override
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
    }

    private class ClassC extends ClassB {

    }

    private interface InterfaceA {

        public String getSequence(int i);
        public void method();
    }
}
