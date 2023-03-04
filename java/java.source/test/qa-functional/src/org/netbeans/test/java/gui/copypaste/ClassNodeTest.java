/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.java.gui.copypaste;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.CutAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.java.SimpleCopyAction;
import org.netbeans.jellytools.modules.java.SimpleMoveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.JavaTestCase;
import org.netbeans.test.java.Utilities;
import org.netbeans.test.java.gui.GuiUtilities;



/**
 * Tests copy, cut and paste operations on class node.
 * @author Roman Strobl
 */
public class ClassNodeTest extends JavaTestCase {
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "default"; 

    // path to sample files
    private static final String TEST_PACKAGE_PATH = 
            "org.netbeans.test.java.gui.copypaste";
            
    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test"; 

    // name of sample package 2
    private static final String TEST_PACKAGE_NAME_2 = 
            TEST_PACKAGE_PATH+".test2"; 

    // name of sample package 3
    private static final String TEST_PACKAGE_NAME_3 = 
            TEST_PACKAGE_PATH+".test3"; 

    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass"; 

    // name of sample interface
    private static final String TEST_IFACE_NAME = "TestInterface"; 

    /**
     * error log
     */
    protected static PrintStream err;
    
    /**
     * standard log
     */
    protected static PrintStream log;
   
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    // actual directory with project
    private static String projectDir;
    
    
    /**
     * Needs to be defined because of JUnit
     * @param name Name of test
     */
    public ClassNodeTest(String name) {
        super(name);
    } 

    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  "+getName()+"  #######");
        err = getLog();
        log = getRef();        
        JemmyProperties.getProperties().setOutput(new TestOut(null, 
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
        openDefaultProject();
    }
    
    /**
     * Tests copy and paste on a class node.
     */
    public void testCopyPaste() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
        
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir"), TEST_PACKAGE_NAME_2);        

        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME, 
                TEST_CLASS_NAME);
        
        // perform copy
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME); 
        //n.select();        
        //Utilities.takeANap(10000);
        
        new CopyAction().perform(n);
        
        // perform paste        
        Node n2 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_2);
  
        n2.select();
        new SimpleCopyAction().perform();
                
        Utilities.takeANap(1000);
        
        // check if pasted file exists
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"               
                +TEST_PACKAGE_NAME_2, TEST_CLASS_NAME);

        Node node = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"               
                +TEST_PACKAGE_NAME_2+"|"+TEST_CLASS_NAME);
        new OpenAction().perform();
        
        // open new testclass        
        Node n3 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_2+"|"
                +TEST_CLASS_NAME); 
        n3.select();        
        new OpenAction().perform();
                
        // update testclass window
        JEditorPaneOperator txtOper = new EditorOperator(TEST_CLASS_NAME).txtEditorPane();            
        
        // compare files
        ref(txtOper.getText());
        compareReferenceFiles();                        
    }


    /**
     * Tests cut and paste operations on a class node.
     */
    public void testCutPaste() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir"), TEST_PACKAGE_NAME_2);
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME); 
        
        // perform cut
        n.select();
        new CutAction().perform();
        
        // perform paste
        Node n2 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_3);
        n2.select();
        new SimpleMoveAction().perform();
        
        Utilities.takeANap(1000);
        
        // cancel refactoring dialog
        
//        refactoring is not invoked when moving files in project view        
//        NbDialogOperator refact = new NbDialogOperator(
//                org.netbeans.jellytools.Bundle.getString(
//                "org.netbeans.modules.refactoring.ui.Bundle", 
//                "LBL_MoveClass"));
//        refact.cancel();
//        refact.waitClosed();
        
        // check if pasted file exists !!! - Cannot exist if refact was canceled ???
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"                
                +TEST_PACKAGE_NAME_3, TEST_CLASS_NAME);
        
        Node n3 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_3+"|"
                +TEST_CLASS_NAME);
                
        // close old testclass window
        new EditorOperator(TEST_CLASS_NAME).close(true);
        
        // open new testclass        
        Node n4 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_3+"|"
                +TEST_CLASS_NAME); 
        n4.select();        
        new OpenAction().perform();
        
        // update testclass window
        JEditorPaneOperator txtOper = new EditorOperator(TEST_CLASS_NAME).txtEditorPane();            

        // compare files
        ref(txtOper.getText());
        compareReferenceFiles();    
        
        // check if original file was deleted
        Node n5 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        String[] children = n5.getChildren();
        assertEquals(children.length, 1);        
    }
    
    /**
     * Tests copy and paste on an interface.
     */
    public void testCopyPasteInterface() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
                        
        // perform copy
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_IFACE_NAME); 
        //n.select();
        
        //Utilities.takeANap(3000);
        new CopyAction().perform(n);
        
        // perform paste        
        Node n2 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_2);
  
        n2.select();
        new SimpleCopyAction().perform();
        
        Utilities.takeANap(1000);

        // check if pasted file exists
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"
                +TEST_PACKAGE_NAME_2, TEST_IFACE_NAME);
        
        // open new testiface
        Node n3 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_2+"|"
                +TEST_IFACE_NAME); 
        n3.select();        
        new OpenAction().perform();
                
        // update testiface window
        JEditorPaneOperator txtOper = new EditorOperator(TEST_IFACE_NAME).txtEditorPane();            
        
        // compare files
        ref(txtOper.getText());
        compareReferenceFiles();                        
    }
    
    /**
     * Tests cut and paste on an interface.
     */
    public void testCutPasteInterface() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
                    
        // perform cut
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_IFACE_NAME); 
        n.select();
        
        new CutAction().perform();
        
        // perform paste        
        Node n2 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_3);
  
        n2.select();
        new SimpleMoveAction().perform();
        
        Utilities.takeANap(1000);

//        refactoring is not invoked when moving files in project window          
//        NbDialogOperator refact = new NbDialogOperator(
//                org.netbeans.jellytools.Bundle.getString(
//                "org.netbeans.modules.refactoring.ui.Bundle", 
//                "LBL_MoveClass"));
//        refact.cancel();
//        refact.waitClosed();
        
        // check if pasted file exists !!! - Cannot exist if refact was canceled ???
        GuiUtilities.waitForChildNode(TEST_PROJECT_NAME, 
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"
                +TEST_PACKAGE_NAME_3, TEST_IFACE_NAME);
        
        // open new testiface
        Node n3 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME_3+"|"
                +TEST_IFACE_NAME);
        n3.select();        
        new OpenAction().perform();
                
        // compare files
        ref(new EditorOperator(TEST_IFACE_NAME).getText());
        compareReferenceFiles();

        // check if original file was deleted
        Node n5 = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle", 
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        String[] children = n5.getChildren();
        assertEquals(children.length, 0);                
    }    
        
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ClassNodeTest.class).enableModules(".*").clusters(".*"));
    }
}
