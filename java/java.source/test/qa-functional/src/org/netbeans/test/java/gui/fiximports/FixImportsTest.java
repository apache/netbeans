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

package org.netbeans.test.java.gui.fiximports;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import javax.swing.ComboBoxModel;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.java.FixAllImports;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.java.JavaTestCase;
import org.netbeans.test.java.Utilities;


/**
 * Tests Fix Imports.
 * @author Roman Strobl
 */
public class FixImportsTest extends JavaTestCase {
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";
    
    // path to sample files
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.java.gui.fiximports";
    
    // name of sample package
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
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
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    @Override
    public void setUp() {
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
     * Creates a new instance of JavaElementsTest
     * @param testName name of test
     */
    public FixImportsTest(String testName) {
        super(testName);
    }
    
    /**
     * Fix imports test.
     */
    public void testFixImports() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        
        n.select();
        new OpenAction().perform();
        
        // test fix imports on Vector
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        editor.insert("Vector v = new Vector();\n", 49, 1);
        Utilities.takeANap(100);
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);        
        //FixAllImports fio = new FixAllImports();
        //fio.ok();
        // wait for fix imports
        for (int i=0; i<10; i++) {
            Utilities.takeANap(1000);
            if(editor.getText().contains("import java.util.Vector;")) break;
            System.out.println(MainWindowOperator.getDefault().getStatusText());
        }
        
        ref(editor.getText());
        
        compareReferenceFiles();
    }
    
    /**
     * Complex fix imports test.
     */
    public void testFixImportsComplex() {
        // test fix imports on List
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        editor.insert("List l = new List();\n", 51, 1);
        
        Utilities.takeANap(100);
        
        // invoke fix imports
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);        
        FixAllImports fio = new FixAllImports();
        
        JComboBoxOperator cbo = fio.cbo(0);
        ComboBoxModel cbm = cbo.getModel();                
        fio.ok();
        // wait for fix imports
        for (int i=0; i<10; i++) {
            Utilities.takeANap(1000);
            if (editor.getText().contains("import java.util.List;")) break;
            //System.out.println(MainWindowOperator.getDefault().getStatusText());
        }
        ref(editor.getText());
        compareReferenceFiles();
        editor.close(false);
    }
    
    public void testRemoveUnused() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {                                 
            editor.insert("List l;\n", 47, 1);
            editor.insert("import java.util.Date;\n", 41, 1);            
            Utilities.takeANap(250);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);          
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().setSelected(false);
            fio.ok();
            assertTrue("Import is removed",editor.getText().contains("import java.util.Date;"));
        } finally {
            editor.close(false);
        }
    }
    
    public void testRemoveAndAdd() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("import java.util.Date;\n", 41, 1);
            editor.insert("List m;\n", 47, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
            
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().changeSelection(true);
            fio.ok();
            Utilities.takeANap(500);
            assertFalse("Import is not removed",editor.getText().contains("import java.util.Date;"));
            assertTrue("Import is not added",editor.getText().contains("import java.util.List;"));
        } finally {
            editor.close(false);
        }
        
    }
    
    public void testNothingToFix() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        editor.setCaretPosition(1, 1);
        try {
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);          
            Utilities.takeANap(200);
            assertEquals("Nothing to fix in import statements.",MainWindowOperator.getDefault().getStatusText());            
        } finally {
            editor.close(false);
        }
        
    }
    
    public void testCancel() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("List m;\n", 46, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            FixAllImports fio = new FixAllImports();
            fio.cancel();
            assertFalse("Import is added",editor.getText().contains("import java.util.List;"));
        } finally {
            editor.close(false);
        }
    }
    
    public void testCheckboxPersistence() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            editor.insert("List m;\n", 46, 1);
            Utilities.takeANap(100);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            FixAllImports fio = new FixAllImports();
            fio.cbRemoveUnusedImports().setSelected(false);
            fio.ok();
            editor.insert("Node n;\n", 47, 1);
            Utilities.takeANap(200);            
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
	    Utilities.takeANap(200);            
            fio = new FixAllImports();
            assertEquals("Checkbox state is not persistent",fio.cbRemoveUnusedImports().isSelected(),false);
            fio.cbRemoveUnusedImports().setSelected(true);
            fio.cancel();
        } finally {
            editor.close(false);
        }
    }
    
    public void testStatusBar() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME+"|"
                +TEST_CLASS_NAME);
        n.select();
        new OpenAction().perform();
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        try {
            Utilities.takeANap(100);
            editor.insert("import java.util.Date;\n", 41, 1);
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_I,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);            
            Utilities.takeANap(1000);
            assertEquals("Nothing to fix in import statements.",MainWindowOperator.getDefault().getStatusText());
        } finally {
            editor.close(false);
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(FixImportsTest.class).enableModules(".*").clusters(".*"));
    }
           
}
