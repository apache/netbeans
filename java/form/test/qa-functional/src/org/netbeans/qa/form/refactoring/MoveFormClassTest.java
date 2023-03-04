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
package org.netbeans.qa.form.refactoring;

import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbModuleSuite;


/**
 * Tests form refactoring, 3rd scenarion : Move form class into dif package
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 26 APRIL 2011 WORKS
 */
public class MoveFormClassTest extends ExtJellyTestCase {

    private String CLASS_NAME = "FrameWithBundleToMove"; // NOI18N
//    private String CLASS_NAME = "ClassToMove"; // NOI18N    
    private String NEW_PACKAGE_NAME = "subdata";
    private String PACKAGE_NAME = "." + NEW_PACKAGE_NAME; // NOI18N

    /**
     * Constructor required by JUnit
     * @param testName
     */
    public MoveFormClassTest(String testName) {
        super(testName);
    }

    
      public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(MoveFormClassTest.class).addTest(
                "testCreatePackage", 
                "testRefactoring", 
                "testChangesInJavaFile", 
                "testChangesInPropertiesFile" 
                ).clusters(".*").enableModules(".*").gui(true));

    }

    /** Creates subdata package  */
    public void testCreatePackage() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();

        Node formnode = new Node(prn, "Source Packages"); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
        formnode.select();

        runNoBlockPopupOverNode("New|Java Package...", formnode); // NOI18N

        NbDialogOperator dialog = new NbDialogOperator("New Java Package");
        new JTextFieldOperator(dialog,0).typeText( getTestPackageName() + PACKAGE_NAME);
        new JButtonOperator(dialog, "Finish").push();
    }

    /** Runs refactoring  */
    public void testRefactoring() throws Exception {
        Node node = openFile(CLASS_NAME);

        /*Task manager takes a long time for scanning and due to this case, file is not opened in time.
        Implemented workaround - sleep for a while
         */
        waitNoEvent(1000);

        runNoBlockPopupOverNode("Refactor|Move...", node); // NOI18N
        waitNoEvent(3000);
        JDialogOperator dialog = new JDialogOperator("Move"); // NOI18N
        JComboBoxOperator combo = new JComboBoxOperator(dialog, 2);
        combo.selectItem( getTestPackageName() + PACKAGE_NAME);

        new JButtonOperator(dialog,"Refactor").clickMouse();

        // this refactoring case takes sometimes a very long time
        // that's way there is following code with for loop
        boolean isClosed = false;
        TimeoutExpiredException lastExc = null;

        for (int i=0; i < 3; i++) {
            try {
                dialog.waitClosed();
                isClosed = true;
            } catch (TimeoutExpiredException e) {
                lastExc = e;
            } catch (Exception e) {
                throw e;
            }
        }

        if (!isClosed) {
            throw (lastExc != null) ? lastExc : new Exception("Something strange while waiting using waitClosed() method");
        }
    }

    /** Tests content of java file */
    public void testChangesInJavaFile() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(getTestProjectName());
        prn.select();

        String path = "Source Packages|" + getTestPackageName() + PACKAGE_NAME + "|" + CLASS_NAME + ".java"; // NOI18N
        //p(path);
        Node formnode = new Node(prn, path ); // NOI18N
        formnode.setComparator(new Operator.DefaultStringComparator(true, false));
//        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);

        FormDesignerOperator designer = new FormDesignerOperator(CLASS_NAME);
        
        // new class package
        findInCode("package data.subdata;", designer);
    }

    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage(NEW_PACKAGE_NAME
                                        + File.separator
                                        +"Bundle.properties");

        String key = "FrameWithBundleToMove.lanButton.text";
        assertTrue("Key \"" + key + "\" not found in Bundle.properties file.",
                findInFile( key, sourceFilePath)); // NOI18N
    }
}    
