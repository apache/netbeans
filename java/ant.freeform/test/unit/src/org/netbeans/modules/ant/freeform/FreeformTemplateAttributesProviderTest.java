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

package org.netbeans.modules.ant.freeform;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.loaders.CreateFromTemplateAttributesProvider;
import org.openide.loaders.DataFolder;

/**
 * Test for freeform template attributes provider, currently providing only
 * project-license value from project.xml
 * 
 * @author Milan Kubec
 */
public class FreeformTemplateAttributesProviderTest extends TestBase {
    
    public FreeformTemplateAttributesProviderTest(String testName) {
        super(testName);
    }
    
    public void testAttributesFor() throws Exception {
        FileObject projdir = egdirFO.getFileObject("simplewithlicense");
        Project simpleWithLicense = ProjectManager.getDefault().findProject(projdir);
        CreateFromTemplateAttributesProvider provider = simpleWithLicense.getLookup().lookup(CreateFromTemplateAttributesProvider.class);
        SourceGroup[] groups = ProjectUtils.getSources(simpleWithLicense).getSourceGroups("java"); // JavaProjectConstants.SOURCES_TYPE_JAVA
        for (SourceGroup group : groups) {
            FileObject root = group.getRootFolder();
            Map result = provider.attributesFor(null, DataFolder.findFolder(root), null);
            assertEquals(1, result.size());
            Map values = (Map)result.get("project");
            if (root.getName().equals("src")) {
                Map<String, String> expected = new HashMap<String, String>();
                expected.put("license", "cddl-netbeans-sun");
                expected.put("encoding", "UTF-8");
                assertEquals(expected, values);
            } else {
                assertEquals(Collections.singletonMap("license", "cddl-netbeans-sun"), values);
            }
        }
    }
    
}
