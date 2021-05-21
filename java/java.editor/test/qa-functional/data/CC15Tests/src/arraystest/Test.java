package arraystest;

public class Test {
    
    String[] testArray;
    String oneString;

    public static void main(String[] args) {        

    }

    private void doSomething(){
        Runnable runner = new Runnable(){
            public void run(){

                String testString = Test.this.testArray[2];

            }
        };
    }
    
    private void op(Object[] objs) {
        if (objs.length > 0 && objs[0] instanceof String) {

        }
    }
}
