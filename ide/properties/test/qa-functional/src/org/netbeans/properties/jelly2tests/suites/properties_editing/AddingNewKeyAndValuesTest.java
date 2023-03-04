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
