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

package org.netbeans.modules.apisupport.project.queries;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.loaders.DataFolder;
import org.openide.util.test.MockLookup;

public class TemplateAttributesProviderTest extends NbTestCase {

    public TemplateAttributesProviderTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances();
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
    }

    public void testTemplateProperties () throws Exception {
        SuiteProject suite = TestBase.generateSuite(getWorkDir(), "suite");
        NbModuleProject clientprj = TestBase.generateSuiteComponent(suite, "client");
        
        AntProjectHelper helper = clientprj.getHelper();
        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        properties.put("project.license", "gpl2");
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
        
        TemplateAttributesProvider prov = new TemplateAttributesProvider(clientprj, clientprj.getHelper(), false);
        Map<String, ?> attrs = prov.attributesFor(null, DataFolder.findFolder(clientprj.getProjectDirectory()), "file.properties");
        Map projectAttributes = (Map) attrs.get("project");
        assertEquals("org.example.client", projectAttributes.get("name"));
        assertEquals("Testing Module", projectAttributes.get("displayName"));
    }
        
}
