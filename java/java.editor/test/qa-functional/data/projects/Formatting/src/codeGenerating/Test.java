/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package codeGenerating;

/**
 *
 * @author jprox
 */
public class Test {
    public void method1() {
        EmptyClass.field = 3;
        EmptyClass.getField();
        new EmptyClass.Inner();
        EmptyClass c = new EmptyClass(1,2,3);
        c.f = "";
        String a = c.getF();                        
    }
    
    public void method2() {
        Visibility v = new Visibility();
        v.fx = 3;
        v.getF();
    }
}
