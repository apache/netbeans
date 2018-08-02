package org.netbeans.test.java.hints;

public abstract class CreateLocalVariable8 {
    
    /** Creates a new instance of AbstractClass */
    public CreateLocalVariable8() {
        foo = "bar";
        {
            foo = "bar";
        }
    }
    
}
