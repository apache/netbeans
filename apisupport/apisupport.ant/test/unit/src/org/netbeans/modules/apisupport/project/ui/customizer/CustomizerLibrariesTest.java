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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

/**
 * Tests {@link CustomizerLibraries}.
 *
 * @author Martin Krauskopf
 */
public class CustomizerLibrariesTest extends TestBase {
    
    public CustomizerLibrariesTest(String testName) {
        super(testName);
    }
    
    public void testCustomizerLibrariesCanBeGCedAfterProjectIsClosed() throws Exception {
        NbModuleProject p = generateStandaloneModule("module1");
        SingleModuleProperties props = SingleModulePropertiesTest.loadProperties(p);
        ProjectCustomizer.Category cat = ProjectCustomizer.Category.create("XX", "xx", null);
                
        CustomizerLibraries panel = new CustomizerLibraries(props, cat, p);
        panel.refresh();
        Reference<?> ref = new WeakReference<Object>(panel);
        OpenProjects.getDefault().close(new Project[] { p });
        panel = null;
        p = null;
        props = null;
        assertGC("CustomizerLibraries panel cannot be GCed", ref);
    }
    
}
