package markOccurrences;

public class Test3 {

    String str = "lore";
    public Test3() {
        str = "abc";
    }
    
    String x = str;
    
    public void setStr() {
        str = "123";
    }
    
    {
        str = "";
    }
    
    public void method(String str) {
        str = "123";
    }

}
