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

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.qa.form.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Tests form refactoring, 1st scenarion : Rename component variable
 * and tests value and access rights of inherited properties
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 26 APRIL 2011 WORKS
 */
public class RenameFormClassTest extends ExtJellyTestCase {
    private String CLASS_OLD_NAME = "FrameWithBundle"; // NOI18N
    private String CLASS_NEW_NAME = CLASS_OLD_NAME + "Renamed"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenameFormClassTest(String testName) {
        super(testName);
    }
    
        
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(RenameFormClassTest.class)
                .addTest("testRefactoring", "testChangesInJavaFile", "testChangesInPropertiesFile")
                .clusters(".*").enableModules(".*").gui(true));
    }

    /** Runs refactoring  */
    public void testRefactoring() {
        Node node = openFile(CLASS_OLD_NAME);
        runNoBlockPopupOverNode("Refactor|Rename...", node); // NOI18N

        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        waitNoEvent(3000);
        new JTextFieldOperator(dialog).setText(CLASS_NEW_NAME);
        new JButtonOperator(dialog,"Refactor").clickMouse(); // NOI18N
        dialog.waitClosed();
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(CLASS_NEW_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(CLASS_OLD_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();

        // new class name
        lines.add("public class FrameWithBundleRenamed"); // NOI18N

        // new class constructor name
        lines.add("public FrameWithBundleRenamed()"); // NOI18N
        
        // new key name
        lines.add("bundle.getString(\"FrameWithBundleRenamed.lanciaButton.text\")"); // NOI18N
        
        findInCode(lines, designer);
    }
    
    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage("Bundle.properties");
        //p(sourceFilePath);
        
        assertTrue("New class name \""+CLASS_NEW_NAME+"\" not found in Bundle.properties file.",
                findInFile(CLASS_NEW_NAME,sourceFilePath)); // NOI18N
    }
}    
