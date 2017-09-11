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

/*
 * CreatingPropertiesFileFromMainWindow2.java
 *
 * This is autometed test for netBeans version 40.
 *
 * Created on 18. September 2002
 */
package org.netbeans.properties.jelly2tests.suites.creating_properties_file;

import java.io.File;
import lib.PropertiesEditorTestCase;
import org.netbeans.jellytools.*;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  Petr Felenda - QA Engineer (petr.felenda@sun.com)
 */
public class CreatingPropertiesFileFromMainWindow2Test extends PropertiesEditorTestCase {

    /*
     * Definition of member variables and objects
     */
    final String FILE_NAME = "testPropertiesFile";
    public String PROJECT_NAME = "properties_test2";

    /**
     * Constructor - Creates a new instance of this class
     */
    public CreatingPropertiesFileFromMainWindow2Test(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreatingPropertiesFileFromMainWindow2Test.class).addTest("testCreatingPropertiesFileFromMainWindow2").enableModules(".*").clusters(".*"));
    }

    /**
     * This method contain body of test
     * @return void
     */
    public void testCreatingPropertiesFileFromMainWindow2() {

        // open project
        openProject(PROJECT_NAME);
        //openDefaultProject();


        /*
         * 1st step of testcase ( here is used toolbar's icon for opening wizard )
         * There will be opened New Wizard from Main Window Toolbar ( icon 'New File...' from toolbar 'System' )
         */
        MainWindowOperator mainWindowOp = MainWindowOperator.getDefault();
        mainWindowOp.getToolbarButton(mainWindowOp.getToolbar("File"), "New File...").pushNoBlock();


        /*
         * 2nd step of testcase
         * Select from wizard Other|Properties File and click next button.
         */
        NewFileWizardOperator nwo = new NewFileWizardOperator();
        nwo.selectProject(PROJECT_NAME);
        nwo.selectCategory(WIZARD_CATEGORY_FILE);
        nwo.selectFileType(WIZARD_FILE_TYPE);
        nwo.next();

        /*
         * 3rd step of testcase
         * Type name and select directory.
         */
        NewJavaFileNameLocationStepOperator nfnlsp = new NewJavaFileNameLocationStepOperator();
        nfnlsp.setObjectName(FILE_NAME);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlsp, 2);
        jtfo.setText("src" + File.separator + "examples");



        /*
         * 4th step of testcase
         * Confirm wizard
         */
        nfnlsp.finish();


        /*
         *  Result
         * Should be added new properties file to adequate place in explorer and opened in editor
         */
        if (!existsFileInEditor(FILE_NAME)) {
            fail("File " + FILE_NAME + " not found in Editor window");
        }
        if (!existsFileInExplorer("examples", FILE_NAME)) {
            fail("File " + FILE_NAME + " not found in explorer");
        }


    }

    public void tearDown() {
        log("Teardown");
        closeOpenedProjects();
        //closePropertiesFile(FILE_NAME); 
    }
}
