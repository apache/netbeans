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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
