package inspectAndTransform;

public class Class_A_F {

    public void m1(int value) {
        if(value > 10) System.out.println("1234");
    }

    public void m2(String value) {
        if(value.equals("")){
            System.out.println("1234");
        }
    }

    public void m3(int[] array) {
        System.out.println(array);
    }

    public void m2(String value, int[] array) {
        if(value.equals("")) System.out.println(array);
    }
}
