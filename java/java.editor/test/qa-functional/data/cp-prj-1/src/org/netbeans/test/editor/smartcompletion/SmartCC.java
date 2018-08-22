/*
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.netbeans.test.editor.smartcompletion;

import java.io.FileReader;


/**
 * 
 */
public class SmartCC extends Exception{
    
    /** Creates a new instance of SmartCC */
    public SmartCC() {        
        //super();
    }
    
    class MyClass {};
    class InnerClass extends MyClass {}
    
    public void method(MyClass s) {        
    }
    
    public MyClass action() {
        //method( )
        //return new 
        return null;
        
    }
    
    public void throwTest(int a) {
        try {
            FileReader f = new FileReader("sss");
        } 
        
        
        
        
    }
    
    
    
}
