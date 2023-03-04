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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import org.netbeans.junit.NbTestCase;

/**
 * Tests {@link DataModel}.
 *
 * @author Martin Krauskopf
 */
public class DataModelTest extends NbTestCase {
    
    public DataModelTest(String name) {
        super(name);
    }
    
    public void testDataModelGenerationForCustomBranchingWizard() throws Exception {
    /* XXX rewrite to use mock data
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(true);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(2);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookVisualPanel2.form",
                    "src/org/example/module1/DocBookVisualPanel2.java",
                    "src/org/example/module1/DocBookWizardIterator.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                    "src/org/example/module1/DocBookWizardPanel2.java"
                ),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("project.xml was modified",
                Arrays.asList("nbproject/project.xml"),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
    }
    
    public void testDataModelGenerationForFileTemplateBranchingWizard() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(true);
        data.setFileTemplateType(true);
        data.setNumberOfSteps(2);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setDisplayName("DocBook Document");
        data.setCategory("Templates/XML");
        data.setPackageName("org.example.module1");
        
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookVisualPanel2.form",
                    "src/org/example/module1/DocBookVisualPanel2.java",
                    "src/org/example/module1/DocBookWizardIterator.java",
                    "src/org/example/module1/DocBookWizardPanel1.java",
                    "src/org/example/module1/DocBookWizardPanel2.java",
                    "src/org/example/module1/docBook.html"
                ),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("modified files",
                Arrays.asList(
                    "nbproject/project.xml",
                    "src/org/example/module1/resources/Bundle.properties",
                    "src/org/example/module1/resources/layer.xml"
                ),
                Arrays.asList(cmf.getModifiedPaths()));

        cmf.run();
    }
    
    public void testDataModelGenerationForCustomSimpleWizard() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        
        // first panel data (Wizard Type)
        data.setBranching(false);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(1);
        
        // second panel data (Name and Location)
        data.setClassNamePrefix("DocBook");
        data.setPackageName("org.example.module1");
        
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        assertEquals("created files",
                Arrays.asList(
                    "src/org/example/module1/DocBookVisualPanel1.form",
                    "src/org/example/module1/DocBookVisualPanel1.java",
                    "src/org/example/module1/DocBookWizardAction.java",
                    "src/org/example/module1/DocBookWizardPanel1.java"
                ),
                Arrays.asList(cmf.getCreatedPaths()));
        assertEquals("project.xml was modified",
                Arrays.asList("nbproject/project.xml"),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
    }
    
    public void testDataModelCMFUpdated() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel(wd);
        data.setBranching(false);
        data.setFileTemplateType(false);
        data.setNumberOfSteps(1);
        data.setClassNamePrefix("X");
        data.setPackageName("x");
        assertEquals("initial files correct",
                Arrays.asList(
                    "src/x/XVisualPanel1.form",
                    "src/x/XVisualPanel1.java",
                    "src/x/XWizardAction.java",
                    "src/x/XWizardPanel1.java"
                ),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
        data.setClassNamePrefix("Y");
        assertEquals("class name change takes effect",
                Arrays.asList(
                    "src/x/YVisualPanel1.form",
                    "src/x/YVisualPanel1.java",
                    "src/x/YWizardAction.java",
                    "src/x/YWizardPanel1.java"
                ),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
        data.setPackageName("y");
        assertEquals("package change takes effect",
                Arrays.asList(
                    "src/y/YVisualPanel1.form",
                    "src/y/YVisualPanel1.java",
                    "src/y/YWizardAction.java",
                    "src/y/YWizardPanel1.java"
                ),
                Arrays.asList(data.getCreatedModifiedFiles().getCreatedPaths()));
    */
    }
    
}
