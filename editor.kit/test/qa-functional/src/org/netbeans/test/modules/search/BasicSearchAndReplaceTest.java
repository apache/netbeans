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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.modules.search;

import java.awt.event.KeyEvent;
import java.io.IOException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.SearchResultsOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;

/**
 *
 * @author jm202557
 */
public class BasicSearchAndReplaceTest extends JellyTestCase {
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.utilities.basicsearch";
    String workdirpath;
    public String DATA_PROJECT_NAME = "Sample";
    public String PACKAGE_NAME = "Source Package";
    public String PROJECT_NAME = "Java";
    protected EditorOperator oper;
    
    
    
    
    /** Creates a new instance of BasicSearchAndReplace */
    public BasicSearchAndReplaceTest(String testName) {
        super(testName);
    }
    
    /** add tests to test suits */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BasicSearchAndReplaceTest.class));
    }
    
    @Override
    public void setUp() throws IOException{
        workdirpath = getDataDir() + "/projects";
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Called after every test case. */
    @Override
    public void tearDown() {
    }
    
    private void enter(){
        oper.pushKey(KeyEvent.VK_ENTER);
    }
    private void end() {
        oper.pushEndKey();
    }
    
    private void type(String text) {
        oper.txtEditorPane().typeText(text);
    }
    
    private void deleteProject() throws InterruptedException{
        boolean type = Utilities.deleteProject(DATA_PROJECT_NAME, workdirpath + System.getProperty("file.separator"+ DATA_PROJECT_NAME));
        if (type = true) {
            log("File " + DATA_PROJECT_NAME + " was deleted correctly");
        } else {
            log("File " + DATA_PROJECT_NAME + " was not deleted correctly");
        }
    }
    private void sourceEdit(){
        oper = new EditorOperator("Main.java");
        oper.setCaretPosition(18, 18);
        end();
        enter();
        type("System.out.println(\"Hello\");");
    }
    
    /** bug 103067 */
    public void testFindAndReplace() throws InterruptedException{
        
        ProjectsTabOperator pto = new ProjectsTabOperator();
        /** Create new Sample project - Java Application */
        String project = Utilities.createNewProject(PROJECT_NAME, DATA_PROJECT_NAME , workdirpath);
        
        /** open class main method in editor */
        Utilities.openFile("Source Packages|" + project + "|" + "main.java", project);
        
        
        /** write some code in main method */
        Utilities.takeANap(1000);
        sourceEdit();
        Utilities.takeANap(1000);
        
        /**
         * invoke search and replace dialog
         */
        
        //firstly Project root node should be selected
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        
        //open find/replace dialog
        NbDialogOperator ndo = Utilities.getFindAndReplaceMainMenu();
        
        // Select selection radio button in scope section
        Utilities.scopeSelection(ndo, "selection");
        Utilities.takeANap(1000);
        
        // fill string Hello in search combobox
        JComboBoxOperator jcbo = new JComboBoxOperator(ndo, 0);
        jcbo.clearText();
        jcbo.typeText("Hello");
        Utilities.takeANap(100);
        
        // fill string "Hello World" which should replace Hello
        JComboBoxOperator jcbor = new JComboBoxOperator(ndo, 1);
        jcbor.enterText("Hello World");
        
        SearchResultsOperator sro = new SearchResultsOperator();
        assertTrue("Junit Output window should be visible", sro.isVisible());
        System.out.println("Search Window is visible");
        
        Utilities.takeANap(100);
        JButtonOperator replace = new JButtonOperator(sro, "Replace");
        replace.push();
        Utilities.takeANap(100);
        
        NbDialogOperator omyl = new NbDialogOperator("Question");
        JButtonOperator rescan = new JButtonOperator(omyl,"rescan");
        rescan.push();
        Utilities.takeANap(100);
        replace.push();
        
        // Close Find/Replace dialog
        Utilities.takeANap(100);
        sro.close();
        
        
        //check, if required string really replace previous one
        Utilities.openFile("Source Packages|" + project + "|" + "main.java", project);
        assertTrue("check in Editor 9b",Utilities.checkEditor("Hello World"));
        Utilities.takeANap(100);
        Utilities.checkEditor(project);
        
        /** delete already created project */;
        deleteProject();
        
        
    }
    
}
