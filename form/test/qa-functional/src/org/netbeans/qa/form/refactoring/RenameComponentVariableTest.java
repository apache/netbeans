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
