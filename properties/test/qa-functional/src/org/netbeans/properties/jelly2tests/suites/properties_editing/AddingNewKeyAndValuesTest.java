/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.properties.jelly2tests.suites.properties_editing;

import lib.PropertiesEditorTestCase;
import junit.framework.Test;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.PropertiesNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Janie
 */
public class AddingNewKeyAndValuesTest extends PropertiesEditorTestCase {
    //Variables of test
    public String WORKING_PACKAGE = "working";
    public String BUNDLE_NAME = "bundle";
    public ProjectsTabOperator pto;
    public ProjectRootNode prn;
    public PropertiesNode pn;
    public TopComponentOperator tco;

    public AddingNewKeyAndValuesTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddingNewKeyAndValuesTest.class).addTest("testCreateNewBundle" /*, "testOpenningSimpleEditor", "testOpenningAdvanceEditor" */, "testAddNewKeyAndValue").enableModules(".*").clusters(".*"));
    }

    public void testCreateNewBundle() {

        //open default project
        openDefaultProject();

        //Create new properties file
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DEFAULT_PROJECT_NAME);
        nfwo.selectCategory(WIZARD_CATEGORY_FILE);
        nfwo.selectFileType(WIZARD_FILE_TYPE);
        nfwo.next();

        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(BUNDLE_NAME);
        JTextFieldOperator jtfo = new JTextFieldOperator(nfnlso, 2);
        jtfo.setText("src/" + WORKING_PACKAGE);
        nfnlso.finish();

        //Check that bundle was created
        if (!existsFileInEditor(BUNDLE_NAME)) {
            fail("File " + BUNDLE_NAME + " not found in Editor window");
        }
        if (!existsFileInExplorer(WORKING_PACKAGE, BUNDLE_NAME + ".properties")) {
            fail("File " + BUNDLE_NAME + " not found in explorer");
        }
    }

    public void testAddNewKeyAndValue() throws Exception {
        //select bundle node in project window and do open action
        pn = getNode(DEFAULT_PROJECT_NAME, rootPackageName+TREE_SEPARATOR +WORKING_PACKAGE +TREE_SEPARATOR+ BUNDLE_NAME + ".properties");
        pn.open();

        // select advance editor
        tco = new TopComponentOperator(BUNDLE_NAME + ".properties");
        JButtonOperator jbo = new JButtonOperator(tco, BUTTON_NEW_PROPERTY);
        jbo.push();

        //fill new key, value and comments
        fillNewKeyValue("key1", "value1", "testovaci");
        
        //Check if key and value was correctly generated in bundle
        pn.edit();
        
        boolean result = checkKeysAndValues(BUNDLE_NAME, "key1", "value1", "testovaci");
        if (result = true){
            log("Key, Value and comments were correctly generated in " + BUNDLE_NAME + "file");
        } else {
            throw new Exception("Key, Value and comments were correctly generated in " + BUNDLE_NAME + "file");
        }
    }
}
