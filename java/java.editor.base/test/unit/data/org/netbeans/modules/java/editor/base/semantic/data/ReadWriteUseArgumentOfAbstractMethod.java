package org.netbeans.modules.java.editor.semantic.data;

public class ReadWriteUseArgumentOfAbstractMethod {
    
    public static interface X {
        
        public void test(int arg1, Object arg2);
        
    }
            
    public static interface Y {
        
        public abstract void test(int arg1, Object arg2);
        
    }
    
    public static abstract class Z {
        
        public abstract void test(int arg1, Object arg2);
        
    }
    
}
