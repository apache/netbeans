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

