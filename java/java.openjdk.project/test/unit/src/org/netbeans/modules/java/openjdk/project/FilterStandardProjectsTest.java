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
package org.netbeans.modules.java.openjdk.project;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.Utilities.TestLookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class FilterStandardProjectsTest extends NbTestCase {

    public FilterStandardProjectsTest(String name) {
        super(name);
    }

    public void testProjectsFilteredModularized() throws IOException {
        clearWorkDir();

        ((TestLookup) Lookup.getDefault()).setLookupsImpl(Lookups.metaInfServices(FilterStandardProjectsTest.class.getClassLoader()));

        FileObject workDir = FileUtil.toFileObject(getWorkDir());

        assertNotNull(workDir);

        FileObject javaBase = FileUtil.createFolder(workDir, "langtools/src/java.compiler");
        FileObject modulesXml = FileUtil.createData(workDir, "modules.xml");
        try (OutputStream out = modulesXml.getOutputStream()) {
            out.write(("<?xml version=\"1.0\" encoding=\"us-ascii\"?>\n" +
                       "<modules>\n" +
                       "  <module>\n" +
                       "    <name>java.compiler</name>\n" +
                       "  </module>\n" +
                       "</modules>\n").getBytes(StandardCharsets.UTF_8));
        }
        FileUtil.createFolder(workDir, "langtools/src/java.compiler/share/classes");
        FileObject langtoolsPrj = FileUtil.createFolder(workDir, "langtools/make/netbeans/langtools");
        FileUtil.createData(workDir, "langtools/make/netbeans/langtools/nbproject/project.xml");
        Project javaBaseProject = ProjectManager.getDefault().findProject(javaBase);

        assertNotNull(javaBaseProject);

        OpenProjects.getDefault().open(new Project[] {javaBaseProject}, false);

        try {
            ProjectManager.getDefault().findProject(langtoolsPrj);
        } catch (IOException ex) {
            assertEquals(FilterStandardProjects.MSG_FILTER, ex.getMessage());
        }

        OpenProjects.getDefault().close(new Project[] {javaBaseProject});
    }

    static {
        System.setProperty("nb.jdk.project.block.langtools", "true");
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));
    }
}
