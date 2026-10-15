package a;

public class TestBuilder {

    public TestBuilder clone(TestBuilder builder) {
        return builder;
    }
    
    public String getInfo() {
        return "info";
    }
}
