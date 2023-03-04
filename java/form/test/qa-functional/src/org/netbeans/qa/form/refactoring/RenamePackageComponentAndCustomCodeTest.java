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
import org.netbeans.jemmy.operators.JDialogOperator;
import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;


/**
 * Tests form refactoring : Refactoring custom component name, custom code and package name
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 26 APRIL 2011 WORKS
 */
public class RenamePackageComponentAndCustomCodeTest extends ExtJellyTestCase {
    private String FORM_NAME = "CustomComponentForm"; // NOI18N
    private String OLD_COMPONENT_NAME = "CustomButton"; // NOI18N    
    private String NEW_COMPONENT_NAME = OLD_COMPONENT_NAME + "Renamed"; // NOI18N    
    private String OLD_PACKAGE_NAME = "data.components"; // NOI18N
    private String NEW_PACKAGE_NAME = "data.renamedcomponents"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenamePackageComponentAndCustomCodeTest(String testName) {
        super(testName);
    }
    
   
   
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(RenamePackageComponentAndCustomCodeTest.class)
                .addTest("testRefactoringComponentName",
                         "testChangesInJavaFile"
                )
                .enableModules(".*").clusters(".*").gui(true));
    }

    /** Runs refactoring  */
    public void testRefactoringComponentName() {
        Node compNode = getProjectFileNode(OLD_COMPONENT_NAME, OLD_PACKAGE_NAME);

        // custom component rename
        runNoBlockPopupOverNode("Refactor|Rename...", compNode); // NOI18N
        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_COMPONENT_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        //dialog.waitClosed();
        waitNoEvent(6000);
        // custom component package rename
        Node node = getProjectFileNode(OLD_PACKAGE_NAME, true);
        runNoBlockPopupOverNode("Refactor|Rename...", node); // NOI18N
        
        // rename dialog ...
        dialog = new JDialogOperator("Rename  " + OLD_PACKAGE_NAME); // NOI18N
        new JTextFieldOperator(dialog).typeText(NEW_PACKAGE_NAME);
        //new JButtonOperator(dialog,"OK").clickMouse(); // NOI18N
        
        // ... refactoring dialog
        //dialog = new JDialogOperator("Rename"); // NOI18N
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        dialog.waitClosed();
        
        // compiling component to avoid load form error
        compNode = getProjectFileNode(NEW_COMPONENT_NAME, NEW_PACKAGE_NAME);
        new CompileJavaAction().perform(compNode);
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(FORM_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(FORM_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();
        
        // custom components refatoring
        lines.add("customButton1 = new data.renamedcomponents.CustomButtonRenamed();"); // NOI18N

        // custom code refactoring
        lines.add("jButton1 = data.renamedcomponents.CustomButtonRenamed.createButton();"); // NOI18N

        // custom component field refactoring
        lines.add("private data.renamedcomponents.CustomButtonRenamed customButton1;"); // NOI18N
        
        findInCode(lines, designer);
    }
}    
