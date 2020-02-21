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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * Test of org.netbeans.jellytools.NameCNDProjectNameLocationStepOperator.
 */
public class NewCNDProjectNameLocationStepOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static final String[] tests = new String[] {
        "testCNDApplicationPanel", "testCNDStaticLibraryPanel",
                "testCNDDynamicLibraryPanel"
                
    };
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {        
        return createModuleTest(NewCNDProjectNameLocationStepOperatorTest.class,
                tests);
    }
    
    @Override
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        standardLabel = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewCNDProjectNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    // Standard
    private static String standardLabel;
    
    /** Test components on CND Application panel */
    public void testCNDApplicationPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        // Standard
        op.selectCategory(standardLabel);
        // C/C++ Application
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Native/newApplication.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectName().setText("NewProject");   // NOI18N
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectFolder().getText();
        stpop.cbCreateMainFile().setSelected(false);
        stpop.cbSetAsMainProject().setSelected(false);

        stpop.cancel();
    }
    
    
    /** Test components on C/C++ Static Library */
    public void testCNDStaticLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // C/C++ Static Library
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native/newStaticLibrary.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N        
        stpop.cancel();
    }

    /** Test components on C/C++ Dynamic Library */
    public void testCNDDynamicLibraryPanel() {
        NewProjectWizardOperator op = NewProjectWizardOperator.invoke();
        op.selectCategory(standardLabel);
        // C/C++ Static Library
        op.selectProject(Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "Templates/Project/Native/newDynamicLibrary.xml"));
        op.next();
        NewCNDProjectNameLocationStepOperator stpop = new NewCNDProjectNameLocationStepOperator();
        stpop.txtProjectLocation().setText("/tmp"); //NOI18N
        stpop.txtProjectName().setText("NewLibraryProject");
        stpop.txtProjectFolder().getText();
        stpop.btBrowseProjectLocation().pushNoBlock();
        //Select Project Location
        String selectProjectLocation = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                "LBL_NWP1_SelectProjectLocation");
        new NbDialogOperator(selectProjectLocation).cancel(); //I18N
        stpop.cancel();
    }

}
