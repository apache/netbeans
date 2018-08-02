package test;

public class ConstructorUsedBySuper2 {
    
    private ConstructorUsedBySuper2() {
        
    }
    
    public static class Subclass extends ConstructorUsedBySuper2 {
        private Subclass() {
            super();
        }
        
        public static Subclass create() {
            return new Subclass();
        }
    }
}
