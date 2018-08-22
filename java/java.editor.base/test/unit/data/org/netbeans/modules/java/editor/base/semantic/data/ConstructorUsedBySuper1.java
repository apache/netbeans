package test;

public class ConstructorUsedBySuper1 {
    
    private ConstructorUsedBySuper1() {
        
    }
    
    public static class Subclass extends ConstructorUsedBySuper1 {
        private Subclass() {
            
        }
        
        public static Subclass create() {
            return new Subclass();
        }
    }
}
