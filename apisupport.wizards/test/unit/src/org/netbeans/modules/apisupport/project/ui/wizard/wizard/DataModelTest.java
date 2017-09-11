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
