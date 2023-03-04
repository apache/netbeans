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

/*
 * CreateTestTest.java
 *
 * Created on August 2, 2006, 1:39 PM
 */

package org.netbeans.test.junit.creation;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.jellytools.modules.junit.testcases.JunitTestCase;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author ms159439
 */
public class CreateTestTest extends JunitTestCase {
    
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.junit.testcreation";
    
    /** name of sample package */
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    private final String TEMP_SRC_PACKAGES_PATH = "Temp Source Packages";
    
    /**
     * Creates a new instance of CreateTestTest
     */
    public CreateTestTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite(CreateTestTest.class);
//        return suite;
          return NbModuleSuite.create(NbModuleSuite.createConfiguration(CreateTestTest.class).addTest(
                  "testCreateTestByPopup",
                  "testCreateTestAndSuiteByPopup"/*,
                  "testCreateTestByPopupNoPublicMethods",
                  "testCreateTestByPopup3",
                  "testCreateTestByPopup4",
                  "testCreateTestByWizard",
                  "testCreateWODefMethodBodies",
                  "testCreateWOHints",
                  "testCreateWOJavadoc",
                  "testCreateWOsetUp",
                  "testCreateWOtearDown"*/)
                  .enableModules(".*").clusters(".*"));
    }
    
    /**
     * Test creation accessed from popup menu
     * With default options (checkboxes)
     */
    public void testCreateTestByPopup() {
        ProjectSupport.waitScanFinished();
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        ndo.btOK().push(); //defaults checked
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles(this.getName()+".ref",this.getName()+".pass",this.getName()+".diff");
    }
    
    /**
     * Test creation accessed from popup menu
     * With default options (checkboxes)
     */
    public void testCreateTestAndSuiteByPopup() {
        ProjectSupport.waitScanFinished();
        
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        ndo.btOK().push(); //defaults checked
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node testCreationPackage = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        String[] children = testCreationPackage.getChildren();
        assertEquals("Test class and test suite expected", 2, children.length);
        assertEquals("Test class not found", Utilities.TEST_CLASS_NAME + "Test.java", children[0]);
        assertEquals("Test suite not found", "TestSuite.java", children[1]);
    }
    
    
    
    /**
     * Test creation accessed from popup menu
     * With default options (checkboxes)
     */
    public void testCreateTestAndSuiteByPopup2() {
        ProjectSupport.waitScanFinished();
        
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        //open sample class
        Node n = Utilities.openFile(TEMP_SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        ndo.btOK().push(); //defaults checked
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node testCreationPackage = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        String[] children = testCreationPackage.getChildren();
        assertEquals("Test class and test suite expected", 2, children.length);
        assertEquals("Test class not found", Utilities.TEST_CLASS_NAME + "Test.java", children[0]);
        assertEquals("Test suite not found", "TestSuite.java", children[1]);
    }
    
    /**
     * Integration Test creation accessed from popup menu
     * With default options (checkboxes)
     */
    public void testCreateIntegrationTestAndSuiteByPopup() {
        ProjectSupport.waitScanFinished();
        
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);        
        Utilities.checkAllCheckboxes(ndo, true);//generate integration test
        ndo.btOK().push(); //defaults checked
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node testCreationPackage = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        String[] children = testCreationPackage.getChildren();
        assertEquals("Test class and test suite expected", 2, children.length);
        assertEquals("Test class not found", Utilities.TEST_CLASS_NAME + "IT.java", children[0]);
        assertEquals("Test suite not found", "TestITSuite.java", children[1]);
    }

    /**
     * Test creation accessed from popup menu
     * Without public methods
     */
    public void testCreateTestByPopupNoPublicMethods() {
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 1);//public methods
        System.out.println(jbo.getText());
        jbo.clickMouse();
        ndo.btOK().push();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME +
                "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from popup menu
     * Without public methods and w/o protected methods
     */
    public void testCreateTestByPopup3() {
        //necessary to delete created tests from testCreateTestByPopup2
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|"  + Utilities.TEST_CLASS_NAME);
        
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 2);//protected methods
        System.out.println(jbo.getText());
        jbo.clickMouse();
        ndo.btOK().push();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from popup menu
     * Without public methods and w/o protected methods and w/o friendly
     * should allow to create anything -- OK should be disabled
     */
    public void testCreateTestByPopup4() {
        //necessary to delete created tests from testCreateTestByPopup3
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 3);//friendly methods
        System.out.println(jbo.getText());
        jbo.push();
        JTextFieldOperator tfo = new JTextFieldOperator(ndo);
        tfo.setText(tfo.getText() + ".");
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        assertFalse(ndo.btOK().isEnabled()); // OK button should be disabled
        ndo.btCancel().push(); //cancel the dialog
    }
    
    /**
     * Test creation w/o setUp() 
     */
    public void testCreateWOsetUp() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo, false);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 4);//tearDown methods
        System.out.println(jbo.getText());
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o tearDown() 
     */
    public void testCreateWOtearDown() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo, false);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 5);//tearDown methods
        System.out.println(jbo.getText());
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o default method bodies 
     */
    public void testCreateWODefMethodBodies() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo, false);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 8);//methods bodies
        System.out.println(jbo.getText());
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o javdoc comments 
     */
    public void testCreateWOJavadoc() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo, false);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 9);//javadoc
        System.out.println(jbo.getText());
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation w/o source code hints() 
     */
    public void testCreateWOHints() {
        //necessary to delete created tests from testCreateTestWOsetUp
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);
//        Utilities.pushCreateTestsPopup(n);
        n.performPopupActionNoBlock("Tools|Create/Update Tests");
        
        NbDialogOperator ndo = new NbDialogOperator(CREATE_TESTS_DIALOG);
        Utilities.checkAllCheckboxes(ndo, false);
        JCheckBoxOperator jbo = new JCheckBoxOperator(ndo, 10);//hints
        System.out.println(jbo.getText());
        jbo.clickMouse();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        ndo.btOK().push();
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
    }
    
    /**
     * Test creation accessed from wizard
     */
    public void testCreateTestByWizard() {
        //necessary to delete created tests previous
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME);
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        op.selectCategory(Bundle.getString(Utilities.JUNIT_BUNDLE,
                "Templates/UnitTests"));
        op.selectFileType(Bundle.getString(Utilities.JUNIT_BUNDLE,
                "Templates/UnitTests/SimpleJUnitTest.java"));
        op.next();
        new JTextFieldOperator(op,0).
                setText("org.netbeans.test.junit.testcreation.test.TestClass");
        op.finish();
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);
        
        ref(filter.filter(new EditorOperator(Utilities.TEST_CLASS_NAME + "Test.java").getText()));
        compareReferenceFiles();
        
        //For some resaon, this couses test to fail
        //        Utilities.deleteNode(Utilities.TEST_PACKAGES_PATH +
        //                "|" + TEST_PACKAGE_NAME);
    }
    
}
