/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProject;

/**
 *
 * @author Alexander.Baratynski
 */
public class JavaProject extends NbTestCase {
    
    public static JavaProject INSTANCE = new JavaProject();
    private final J2SEProject project;
    
    private JavaProject(){
        super("Java project");
        project = createJavaProject();
    }
    
    private J2SEProject createJavaProject() {
        
        
        
        return null;
    }
    
}
