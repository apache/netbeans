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
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
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
 * 26 APRIL 2011 BUG #197860
 */
public class RenameComponentVariableTest extends ExtJellyTestCase {
    private String FILE_NAME = "RenameComponentVariableTestFrame"; // NOI18N
    private String JAVA_FILE_NAME = "RenameComponentVariableTestFrame"; // NOI18N    
    private String VARIABLE_OLD_NAME = "jButton1"; // NOI18N
    private String VARIABLE_NEW_NAME = "myNewButton"; // NOI18N
    private String NODE_PATH = "[JFrame]|" + VARIABLE_OLD_NAME + " [JButton]"; // NOI18N
    
    /**
     * Constructor required by JUnit
     * @param testName
     */
    public RenameComponentVariableTest(String testName) {
        super(testName);
    }
    
    
    
    
    /**
     * Creates suite from particular test cases.
     * @return nb test suite
     */
    public static Test suite() {
       return NbModuleSuite.create(NbModuleSuite.createConfiguration(RenameComponentVariableTest.class).addTest(
               "testRefactoring", 
               "testChangesInJavaFile", 
               "testChangesInFormFile", 
               "testChangesInPropertiesFile"
               ).clusters(".*").enableModules(".*")
               .gui(true));
    }
    
    /** Runs refactoring  */
    public void testRefactoring() {
        openFile(JAVA_FILE_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(JAVA_FILE_NAME);
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        Node node = new Node(inspector.treeComponents(), NODE_PATH);
        
        runNoBlockPopupOverNode("Change Variable Name ...", node); // NOI18N
        
        JDialogOperator dialog = new JDialogOperator("Rename"); // NOI18N
        new JTextFieldOperator(dialog).setText(VARIABLE_NEW_NAME);
        new JButtonOperator(dialog,"OK").clickMouse(); // NOI18N
        dialog.waitClosed();
    }
    
    /** Tests content of java file */
    public void testChangesInJavaFile() {
        openFile(JAVA_FILE_NAME);
        FormDesignerOperator designer = new FormDesignerOperator(JAVA_FILE_NAME);
        
        ArrayList<String> lines = new ArrayList<String>();

        // local variable in my own method with same name
        lines.add("JButton jButton2 = jButton1;"); // NOI18N

        // using renamed button in my own method
        lines.add("String actualButtonText = this.myNewButton.getText();"); // NOI18N
        
        // declaration of renames button
        lines.add("private javax.swing.JButton myNewButton;"); // NOI18N
        
        // renamed event handler name
        lines.add("private void myNewButtonActionPerformed("); // NOI18N
        
        // my own code inside frame contructor
        lines.add("myNewButton.setEnabled(true);"); // NOI18N
        
        // event handling code of renamed button
        lines.add("myNewButton.addActionListener(new"); // NOI18N
        lines.add("myNewButtonActionPerformed(evt);"); // NOI18N
        
        // binding code with renamed button
        lines.add("Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, myNewButton,"); // NOI18N
        
        // initialization of renamed button inside
        lines.add("myNewButton = new javax.swing.JButton()"); // NOI18N
        
        // issue 105649 - getting model from renamed button inside generated event handler method
        lines.add("ButtonModel model = myNewButton.getModel();"); // NOI18N
        
        findInCode(lines, designer);
    }
    
    /** Tests changes in form file */
    public void testChangesInFormFile() {
        String sourceFilePath = getFilePathFromDataPackage(FILE_NAME + ".form"); // NOI18N
            
        assertTrue("Old variable name \""+VARIABLE_OLD_NAME+"\" found in " + FILE_NAME + ".form file.",
                !findInFile(VARIABLE_OLD_NAME,sourceFilePath)
                ); // NOI18N
    }

    /** Test changes in property bundle file */
    public void testChangesInPropertiesFile() {
        String sourceFilePath = getFilePathFromDataPackage("Bundle.properties"); // NOI18N
        
        assertTrue("Old variable name \""+VARIABLE_OLD_NAME+"\" found in Bundle.properties file.",
                !findInFile(VARIABLE_OLD_NAME,sourceFilePath)
                ); // NOI18N
    }
}    
