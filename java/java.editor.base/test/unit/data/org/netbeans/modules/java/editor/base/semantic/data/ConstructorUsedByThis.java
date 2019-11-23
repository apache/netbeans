package test;

public class ConstructorUsedByThis {
    
    public ConstructorUsedByThis() {
        this("");
    }
    
    private ConstructorUsedByThis(String s) {
        
    }
    
}
