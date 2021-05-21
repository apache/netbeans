package encapsulateField;

public class Class_A_B {

    private int     field1;
    private String  field2 = "1234";
    private boolean field3;
    private ClassA  field4 = new ClassA(1234);

    public void m1(int field1) {
        this.field1 = 5;

    }

    public Class_A_B(int f) {
        this.field1 = f;
    }

    public boolean isField3() {
        return field3;
    }

    public void setField3(boolean field3) {
        this.field3 = field3;
    }

    class ClassA {

        protected int val = 5;

        public ClassA(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

        public void setVal(int val) {
            this.val = val;
        }
    }

    class ClassB extends ClassA {

        public ClassB(int val) {
            super(val);
        }

        public void m1() {
            val = 10;
            field1 = (int) Math.asin((double) field1) + Integer.valueOf(field2) + Boolean.compare(isField3(), true) + field4.getVal();
        }
    }

    public void m2() {
    }

    class ClassC extends ClassB {

        public ClassC(int val) {
            super(val);
        }

        public void m1() {
            val = 10;
        }
    }
}
