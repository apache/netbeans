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
package org.netbeans.modules.gradle.api.execute;

import java.io.File;
import java.util.Collection;
import java.util.function.Function;
import static junit.framework.TestCase.assertEquals;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.AbstractGradleProjectTestCase;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.execute.GradleDaemonExecutor;
import org.netbeans.modules.gradle.execute.GradleExecutor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lkishalmi
 */
public class RunUtilsTest extends AbstractGradleProjectTestCase {

    public RunUtilsTest(String name) {
        super(name);
    }

    public void testIncludeOpenProjects1() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        FileObject b = createGradleProject("projectB",
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        Project prjB = ProjectManager.getDefault().findProject(b);
        ProjectTrust.getDefault().trustProject(prjB);
        OpenProjects.getDefault().open(new Project[] {prjA, prjB}, false);
        GradleCommandLine cmd = RunUtils.getIncludedOpenProjects(prjB);
        Collection<String> params = cmd.getParameters(GradleCommandLine.Parameter.INCLUDE_BUILD);
        assertEquals(1, params.size());
        String value = params.iterator().next();
        assertEquals(".." + File.separator + "projectA", value);
    }

    public void testIncludeOpenProjects2() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        FileObject b = createGradleProject("projectB",
                "apply plugin: 'java'\n", "includeBuild '../projectA'\n");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        Project prjB = ProjectManager.getDefault().findProject(b);
        ProjectTrust.getDefault().trustProject(prjB);
        OpenProjects.getDefault().open(new Project[] {prjA, prjB}, false);
        GradleCommandLine cmd = RunUtils.getIncludedOpenProjects(prjB);
        Collection<String> params = cmd.getParameters(GradleCommandLine.Parameter.INCLUDE_BUILD);
        assertEquals(0, params.size());
    }

    public void testEvaluateArgs1() throws Exception {
        FileObject a = createGradleProject("projectA",
                "apply plugin: 'java'\n", "");
        Project prjA = ProjectManager.getDefault().findProject(a);
        ProjectTrust.getDefault().trustProject(prjA);
        openProject(a);
        String[] args = RunUtils.evaluateActionArgs(prjA, null, "Project: ${projectPath}${projectName}", Lookups.singleton(prjA));
        assertEquals(2, args.length);
        assertEquals("Project:", args[0]);
        assertEquals(":projectA", args[1]);
    }
    
    public static void setExecutonFactory(Function<RunConfig, GradleExecutor> factory) {
        RunUtils.EXECUTOR_FACTORY = factory != null ? factory : GradleDaemonExecutor::new;
    }
}
