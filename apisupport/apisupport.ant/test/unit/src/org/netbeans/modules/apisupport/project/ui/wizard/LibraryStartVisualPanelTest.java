/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test wizard logic.
 * @author Milos Kleint, Jesse Glick
 */
public class LibraryStartVisualPanelTest extends NbTestCase {

    public LibraryStartVisualPanelTest(String testName) {
        super(testName);
    }

    public void testPopulateProjectData() throws Exception {
        Map<String, String> contents = new HashMap<>();
        contents.put("org/apache/commons/logging/Log.class", "");
        contents.put("1.0-beta/X.class", ""); // #72669
        contents.put("org/apache/commons/logging/impl/NoOpLog.class", "");
        File libraryPath = new File(getWorkDir(), "test-library-0.1_01.jar");
        TestBase.createJar(libraryPath, contents, new Manifest());
        NewModuleProjectData data = new NewModuleProjectData(NewNbModuleWizardIterator.Type.LIBRARY_MODULE);
        LibraryStartVisualPanel.populateProjectData(data, libraryPath.getAbsolutePath(), true);
        assertEquals("test-library", data.getProjectName());
        assertEquals("org.apache.commons.logging", data.getCodeNameBase());
    }
    
}
