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

package org.netbeans.test.junit.creation;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.modules.junit.testcases.JunitTestCase;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.junit.utils.Utilities;

/**
 * Tests "Goto Test" action
 * @author Max Sauer
 */
public class GotoTest extends JunitTestCase {
    private static final String TEST_PACKAGE_NAME =
            "org.netbeans.test.junit.testresults.test";
    
    private static final String TEST_PACKAGE_PACKAGEGOTO_NAME =
            "org.netbeans.test.junit.go";
    
    /** Creates a new instance of GotoTest */
    public GotoTest(String testName) {
        super(testName);
    }
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GotoTest.class).addTest(
                "testSelectTestFromMainMenu",
                "testSelectTestFromExplorer",
                "testSelectTestFromEditorContextMenu").enableModules(".*").clusters(".*"));
    }
    
    /**
     * Test selecting appropriate test from Main menu
     */
    public void testSelectTestFromMainMenu() {
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        
        EditorOperator eos = new EditorOperator(Utilities.TEST_CLASS_NAME);
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        String[] sf = {"Navigate", "Go to Test/Tested class"};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jbo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(4));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Editor!", jmio.isEnabled());
        jbo.pushMenu(sf);
        //Operator for opened TestClassTest
        EditorOperator eo = new EditorOperator(Utilities.TEST_CLASS_NAME + "Test");
        assertTrue("Test for \"" + TEST_PACKAGE_NAME +
                Utilities.TEST_CLASS_NAME + "\" not opened!", eo.isVisible());
        eo.close(false);
        eos.close(false);
    }
    
    /**
     * Test selecting appropriate test from Explorer
     */
    public void testSelectTestFromExplorer() {
        //open sample class
        Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        EditorOperator eos = new EditorOperator(Utilities.TEST_CLASS_NAME);
        
        //select sample class in explorer
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                Utilities.TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        n.select();
        
        JMenuBarOperator jbo = new JMenuBarOperator(
                MainWindowOperator.getDefault().getJMenuBar());
        
        String[] sf = {"Navigate", "Go to Test/Tested class"};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jbo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(4));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Explorer, over a class node!" +
                "see: http://www.netbeans.org/issues/show_bug.cgi?id=88599",
                jmio.isEnabled());
        jbo.pushMenu(sf);
        EditorOperator eot = new EditorOperator(Utilities.TEST_CLASS_NAME);
        assertTrue("Test for \"" + TEST_PACKAGE_NAME +
                Utilities.TEST_CLASS_NAME + "\" not opened!", eot.isVisible());
        eot.close(false);
        eos.close(false);
    }
    
    /**
     * Test selecting appropriate test from Editor's context menu
     */
    public void testSelectTestFromEditorContextMenu() {
        //open sample class
        Node n = Utilities.openFile(Utilities.SRC_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME+ "|" + Utilities.TEST_CLASS_NAME);
        EditorOperator eos = new EditorOperator(Utilities.TEST_CLASS_NAME);
        eos.clickForPopup();
        JPopupMenuOperator jpmo = new JPopupMenuOperator();
        
        String[] sf = {"Navigate", "Go to Test/Tested class"};
        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
        jpmo.pushMenu(sf[0]);
        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jpmo, sf[0]).getItem(2));
        //Check if goto test is enabled inside menu
        assertTrue("Goto Test disabled when invoked from Explorer, over a class node!" +
                "see: http://www.netbeans.org/issues/show_bug.cgi?id=88599",
                jmio.isEnabled());
        jpmo.pushMenu(sf);
        EditorOperator eot = new EditorOperator(Utilities.TEST_CLASS_NAME);
        assertTrue("Test for \"" + TEST_PACKAGE_NAME +
                Utilities.TEST_CLASS_NAME + "\" not opened!", eot.isVisible());
        eot.close(false);
        eos.close(false);
    }
    
    /**
     * Tests selecting of suite test when invoking GoTo Test for a java package
     */
// BUG: [Issue 120356]
    
//    public void testSelectTestFromExplorerPackage() {
//        //select sample class in explorer
//        Node pn = new ProjectsTabOperator().getProjectRootNode(
//                Utilities.TEST_PROJECT_NAME);
//        pn.select();
//        Node n = new Node(pn, Utilities.SRC_PACKAGES_PATH +
//                "|" + TEST_PACKAGE_PACKAGEGOTO_NAME);
//        n.select(); //select the 'go' package from test project 
//        
//        JMenuBarOperator jbo = new JMenuBarOperator(
//                MainWindowOperator.getDefault().getJMenuBar());
//        
//        String[] sf = {Bundle.getStringTrimmed("org.netbeans.core.Bundle",
//                "Menu/GoTo"),
//                Bundle.getStringTrimmed("org.netbeans.modules.junit.Bundle",
//                "LBL_Action_GoToTest")};
//        Utilities.takeANap(Utilities.ACTION_TIMEOUT);        
//        jbo.pushMenu(sf[0]);
//        JMenuItemOperator jmio = new JMenuItemOperator(new JMenuOperator(jbo, sf[0]).getItem(0));
//        //Check if goto test is enabled inside menu
//        assertTrue("Goto Test disabled when invoked from Explorer, over a package node!" +
//                "see: http://www.netbeans.org/issues/show_bug.cgi?id=88599",
//                jmio.isEnabled());
//        jbo.pushMenu(sf);
//        Utilities.takeANap(3000);
//        EditorOperator eot = new EditorOperator("GoSuite"); //test suite for the package
//        assertTrue("Test suite for \"" + TEST_PACKAGE_PACKAGEGOTO_NAME +
//                 "\" (GoSuite.java) not opened!", eot.isVisible());
//        eot.close(false);
//    }
    
    

}
