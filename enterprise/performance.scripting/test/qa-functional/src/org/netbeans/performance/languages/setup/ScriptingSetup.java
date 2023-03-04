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
package org.netbeans.performance.languages.setup;

import java.io.IOException;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase2;
import org.netbeans.performance.languages.Projects;
import org.openide.util.Exceptions;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingSetup extends PerformanceTestCase2 {

    public ScriptingSetup(String testName) {
        super(testName);
    }

    public void testCloseMemoryToolbar() {
        CommonUtilities.closeMemoryToolbar();
    }

    public void testAddGlassFishServer() {
        CommonUtilities.addApplicationServer();
    }

    public void testOpenScriptingProject() {

        try {
            this.openDataProjects(Projects.SCRIPTING_PROJECT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void testOpenPHPProject() {

        try {
            this.openDataProjects(Projects.PHP_PROJECT);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
