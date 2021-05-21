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

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.ProjectFactory;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterTestCase;
import org.netbeans.modules.projectimport.eclipse.core.WorkspaceFactory;

public class ProjectImportModelTest extends ProjectImporterTestCase {

    public ProjectImportModelTest(String name) {
        super(name);
    }

    public void testTestRootDetection() throws Exception {
        File welltested = extractToWorkDir("welltested.zip");
        assertTrue(welltested.isDirectory());
        EclipseProject prj = ProjectFactory.getInstance().load(welltested);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        assertEquals(Collections.singletonList(new File(welltested, "src")), Arrays.asList(model.getEclipseSourceRootsAsFileArray()));
        assertEquals(Collections.singletonList(new File(welltested, "test")), Arrays.asList(model.getEclipseTestSourceRootsAsFileArray()));
    }

    public void testCompilerOptions() throws Exception {
        File tco = extractToWorkDir("test-compiler-options.zip");
        EclipseProject prj = ProjectFactory.getInstance().load(tco);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        assertEquals("1.5", model.getSourceLevel());
        assertEquals("1.6", model.getTargetLevel());
        assertTrue(model.isDebug());
        assertTrue(model.isDeprecation());
        assertEquals("UTF-8", model.getEncoding());
        assertEquals("-Xlint:fallthrough -Xlint:finally -Xlint:unchecked", model.getCompilerArgs().toString());
    }
    
    public void testLaunchConfigurations() throws Exception {
        File unpacked = extractToWorkDir("launch-config.zip");
        Set<EclipseProject> prjs = WorkspaceFactory.getInstance().load(unpacked).getProjects();
        EclipseProject prj = null;
        for (EclipseProject _p : prjs) {
            if (_p.getName().equals("p")) {
                prj = _p;
                break;
            }
        }
        assertNotNull(prj);
        ProjectImportModel model = new ProjectImportModel(prj, null, null, null);
        Collection<LaunchConfiguration> configs = model.getLaunchConfigurations();
        assertEquals(1, configs.size());
        LaunchConfiguration config = configs.iterator().next();
        assertEquals("Main", config.getName());
        assertEquals(LaunchConfiguration.TYPE_LOCAL_JAVA_APPLICATION, config.getType());
        assertEquals("app.Main", config.getMainType());
        assertEquals("world", config.getProgramArguments());
        assertEquals("-Dgreeting=hello", config.getVmArguments());
    }

}
