/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
