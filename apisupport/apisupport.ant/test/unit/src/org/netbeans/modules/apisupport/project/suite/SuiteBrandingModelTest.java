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

package org.netbeans.modules.apisupport.project.suite;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuitePropertiesTest;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.test.MockLookup;

public class SuiteBrandingModelTest extends NbTestCase {

    public SuiteBrandingModelTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
    }

    public void testBrandingToken() throws Exception { // #197066
        SuiteProject p = TestBase.generateSuite(getWorkDir(), "s");
        // Adding branding:
        SuiteProperties sp = SuitePropertiesTest.getSuiteProperties(p);
        BrandingModel m = sp.getBrandingModel();
        m.setBrandingEnabled(true);
        m.setName("myapp");
        m.setTitle("My App");
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        EditableProperties ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals("${" + SuiteBrandingModel.BRANDING_TOKEN_PROPERTY + "}", ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals("My App", ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals("myapp", ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        // Removing branding:
        sp = SuitePropertiesTest.getSuiteProperties(p);
        m = sp.getBrandingModel();
        assertEquals("myapp", m.getName());
        m.setBrandingEnabled(false);
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals(null, ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals(null, ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        // Updating branding:
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(SuiteBrandingModel.NAME_PROPERTY, "myoldapp");
        ep.put(SuiteBrandingModel.TITLE_PROPERTY, "My Old App");
        ep.put(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY, "${" + SuiteBrandingModel.NAME_PROPERTY + "}");
        p.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        sp = SuitePropertiesTest.getSuiteProperties(p);
        m = sp.getBrandingModel();
        assertTrue(m.isBrandingEnabled());
        assertEquals("myoldapp", m.getName());
        m.setName("myupdatedapp");
        m.setTitle("My Updated App");
        m.doSave();
        ProjectManager.getDefault().saveProject(p);
        ep = p.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(null, ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
        assertEquals("${" + SuiteBrandingModel.BRANDING_TOKEN_PROPERTY + "}", ep.getProperty(SuiteBrandingModel.NAME_PROPERTY));
        assertEquals("My Updated App", ep.getProperty(SuiteBrandingModel.TITLE_PROPERTY));
        ep = p.getHelper().getProperties("nbproject/platform.properties");
        assertEquals("myupdatedapp", ep.getProperty(SuiteBrandingModel.BRANDING_TOKEN_PROPERTY));
    }

}
