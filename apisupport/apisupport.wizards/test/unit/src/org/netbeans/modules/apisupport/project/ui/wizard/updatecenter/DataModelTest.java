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

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import org.netbeans.junit.NbTestCase;

/**
 * Tests {@link DataModel}.
 *
 * @author Jiri Rechtacek
 */
public class DataModelTest extends NbTestCase {
    
    public DataModelTest(String name) {
        super(name);
    }
    
    private void testAddUpdateCenter(String[] supposedContent) throws Exception {
    /* XXX rewrite to use mock data
        WizardDescriptor wd = new WizardDescriptor() {};
        wd.putProperty (ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        DataModel data = new DataModel (wd);
        
        // create declaration UC panel, sets the default values into model
        UpdateCenterRegistrationPanel p = new UpdateCenterRegistrationPanel (wd, data);
        p.updateData ();
        
        CreatedModifiedFiles cmf = data.refreshCreatedModifiedFiles ();
        assertEquals (
                Arrays.asList (new String[] {}),
                Arrays.asList (cmf.getCreatedPaths ()));
        assertEquals(
                Arrays.asList(
                "nbproject/project.xml",
                "src/org/example/module1/resources/Bundle.properties",
                "src/org/example/module1/resources/layer.xml"),
                Arrays.asList(cmf.getModifiedPaths()));
        
        cmf.run();
        
        CreatedModifiedFilesTest.assertLayerContent(supposedContent,
                new File(getWorkDir(), "module1/src/org/example/module1/resources/layer.xml"));
                */
    }
    
    public void testAddUpdateCenterWithDefaultValues () throws Exception {
        String[] supposedContent = new String [] {
            "<filesystem>",
                "<folder name=\"Services\">",
                    "<folder name=\"AutoupdateType\">",
                        "<file name=\"org_example_module1_update_center.instance\">",
                            "<attr name=\"displayName\" bundlevalue=\"org.example.module1.resources.Bundle#" +
                            "Services/AutoupdateType/org_example_module1_update_center.instance\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"instanceCreate\" " +
                            "methodvalue=\"org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory.createUpdateProvider\"/>",
                            "<attr name=\"instanceOf\" stringvalue=\"org.netbeans.spi.autoupdate.UpdateProvider\"/>",                            
                            "<attr name=\"url\" bundlevalue=\"org.example.module1.resources.Bundle#org_example_module1_update_center\"/>",
                        "</file>",
                    "</folder>",
                "</folder>",
            "</filesystem>"
        };

        testAddUpdateCenter(supposedContent);
    }
    
    public void testAddUpdateCenterDouble () throws Exception {
        String[] supposedContent = new String [] {
            "<filesystem>",
                "<folder name=\"Services\">",
                    "<folder name=\"AutoupdateType\">",
                        "<file name=\"org_example_module1_update_center.instance\">",
                            "<attr name=\"displayName\" bundlevalue=\"org.example.module1.resources.Bundle#" +
                            "Services/AutoupdateType/org_example_module1_update_center.instance\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"instanceCreate\" " +
                            "methodvalue=\"org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory.createUpdateProvider\"/>",
                            "<attr name=\"instanceOf\" stringvalue=\"org.netbeans.spi.autoupdate.UpdateProvider\"/>",
                            "<attr name=\"url\" bundlevalue=\"org.example.module1.resources.Bundle#org_example_module1_update_center\"/>",
                        "</file>",
                        "<file name=\"org_example_module1_update_center_1.instance\">",
                            "<attr name=\"displayName\" bundlevalue=\"org.example.module1.resources.Bundle#" +
                            "Services/AutoupdateType/org_example_module1_update_center_1.instance\"/>",
                            "<attr name=\"enabled\" boolvalue=\"true\"/>",
                            "<attr name=\"instanceCreate\" " +
                            "methodvalue=\"org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogFactory.createUpdateProvider\"/>",
                            "<attr name=\"instanceOf\" stringvalue=\"org.netbeans.spi.autoupdate.UpdateProvider\"/>",                            
                            "<attr name=\"url\" bundlevalue=\"org.example.module1.resources.Bundle#org_example_module1_update_center_1\"/>",
                        "</file>",
                    "</folder>",
                "</folder>",
            "</filesystem>"
        };

        testAddUpdateCenterWithDefaultValues ();
        testAddUpdateCenter(supposedContent);
    }
    
}

