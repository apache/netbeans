package test;

public class SemanticInnerClasses {
    
    /** Creates a new instance of SemanticInnerClasses */
    public SemanticInnerClasses() {
        new Inner2();
        Inner3.create();
        new Inner5();
    }
    
    private static class Inner1 {
        public Inner1() {
            
        }
        
        public static void create() {
            
        }
    }
    
    private static class Inner2 {
        public Inner2() {
            
        }
    }

    private static class Inner3 {
        public static void create() {
            
        }
    }

    private class Inner4 {
        public Inner4() {
            
        }
    }

    private class Inner5 {
        public Inner5() {
            
        }
    }
    
    class Inner6 {
        
    }
}
