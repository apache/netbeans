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
