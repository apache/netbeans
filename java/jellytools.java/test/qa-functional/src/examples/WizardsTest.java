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
package examples;

import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

public class WizardsTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public WizardsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(WizardsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    /** Test new file wizard using predefined operators. */
    public void testWizards() {
        // open new file wizard
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject("SampleProject");
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Class");
        // go to next page
        nfwo.next();
        // create operator for the next page
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.txtObjectName().typeText("MyNewClass");
        // finish wizard
        //nfnlso.finish();
        // cancel wizard
        nfnlso.cancel();
    }

    /** Test new project wizard using generic WizardOperator. */
    public void testGenericWizards() {
        // open new project wizard
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java Web");
        npwo.selectProject("Web Application");
        npwo.next();
        // create operator for next page
        WizardOperator wo = new WizardOperator("Web Application");
        JTextFieldOperator txtName = new JTextFieldOperator((JTextField) new JLabelOperator(wo, "Project Name:").getLabelFor());
        txtName.clearText();
        txtName.typeText("MyApp");
        wo.cancel();
    }
}
