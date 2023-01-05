package org.netbeans.modules.java.editor.semantic.data;

public class ReadWriteUseArgumentOfAbstractMethod {
    
    public interface X {
        
        public void test(int arg1, Object arg2);
        
    }
            
    public interface Y {
        
        public abstract void test(int arg1, Object arg2);
        
    }
    
    public abstract static class Z {
        
        public abstract void test(int arg1, Object arg2);
        
    }
    
}
