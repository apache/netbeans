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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyException;

/**
 * Test of org.netbeans.jellytools.NewFileWizardOperator.
 * @author tb115823
 */
public class NewFileWizardOperatorTest extends JellyTestCase {

    public static NewFileWizardOperator op;
    public static final String[] tests = new String[]{
        "testInvokeTitle", "testInvoke", "testSelectProjectAndCategoryAndFileType",
        "testGetDescription", "testCreate"};

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewFileWizardOperatorTest.class,
                tests);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewFileWizardOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method with title parameter. Opens New File wizard, waits for the dialog and closes it. */
    public void testInvokeTitle() {
        // "New File"
        String title = Bundle.getString("org.netbeans.modules.project.ui.Bundle", "LBL_NewFileWizard_Title");
        op = NewFileWizardOperator.invoke(title);
        op.close();
    }

    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        op = NewFileWizardOperator.invoke();
    }

    /** Test components on New File Wizard panel 
     *  sets category and filetype
     */
    public void testSelectProjectAndCategoryAndFileType() {
        org.netbeans.jemmy.operators.JComboBoxOperator cbo = op.cboProject();
        cbo.selectItem(0);
        // Java
        op.selectCategory(Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes"));
        op.selectFileType("Java Class");
    }

    /** Test description component on New File Wizard panel
     *  gets description of selected filetype
     */
    public void testGetDescription() {
        assertTrue("Description should contain Java class sub string.", op.getDescription().indexOf("Java class") > 0);
        op.cancel();
    }

    public void testCreate() {
        // Java
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        NewJavaFileWizardOperator.create("SampleProject", javaClassesLabel, "Java Class", "sample1", "TempClass");  // NOI18N
        Node classNode = new Node(new SourcePackagesNode("SampleProject"), "sample1|TempClass");  // NOI18N
        DeleteAction deleteAction = new DeleteAction();
        deleteAction.perform(classNode);
        try {
            new NbDialogOperator("Delete").ok();
        } catch (JemmyException e) {
            // sometimes happens that different dialog is opened
            new NbDialogOperator("Confirm Object Deletion").yes();
        }
    }
}
