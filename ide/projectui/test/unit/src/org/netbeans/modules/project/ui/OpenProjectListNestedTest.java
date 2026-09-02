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

package org.netbeans.modules.project.ui;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class OpenProjectListNestedTest extends NbTestCase {
    static final Logger LOG = Logger.getLogger("test.OpenProjectListNestedTest");
    
    public OpenProjectListNestedTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        OpenProjects.getDefault().close(OpenProjects.getDefault().openProjects().get());
        OpenProjectList.waitProjectsFullyOpen();
    }

    public void testOpenNestedProjects() throws Exception {
        doOpenProjects(true, () -> {
            var em = OpenProjects.getDefault().createLogicalView();
            var all = em.getRootContext().getChildren().getNodes(true);
            assertEquals("Two projects are visible", 2, all.length);
            return null;
        });
    }

    public void testOpenNonNestedProjects() throws Exception {
        doOpenProjects(false, () -> {
            var em = OpenProjects.getDefault().createLogicalView();
            var all = em.getRootContext().getChildren().getNodes(true);
            assertEquals("One project is visible", 1, all.length);
            return null;
        });
    }

    private void doOpenProjects(boolean withSubprojects, Callable<Void> inner) throws Exception {
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        FileObject prjFo = TestSupport.createTestProject(workDir, "prj1");
        FileObject nestedFo = TestSupport.createTestProject(prjFo, "nested1");
        final TestSupport.TestProject mainPrj = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prjFo);
        final TestSupport.TestProject nestedPrj = (TestSupport.TestProject) ProjectManager.getDefault().findProject(nestedFo);
        assertNotNull("Project found", mainPrj);
        var subProvider = new SubprojectProvider() {
            @Override
            public Set<? extends Project> getSubprojects() {
                return Set.of(nestedPrj);
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
            }
        };
        if (withSubprojects) {
            mainPrj.setLookup(Lookups.singleton(subProvider));
        }

        OpenProjectList.waitProjectsFullyOpen();
        assertEquals("Initially empty", 0, OpenProjects.getDefault().openProjects().get().length);

        OpenProjects.getDefault().open(new Project[] { mainPrj }, true);
        
        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        if (withSubprojects) {
            assertEquals("Both projects open", 2, arr.size());
            assertTrue("Prj1 is there", arr.contains(mainPrj));
            assertTrue("Nested1 is there", arr.contains(nestedPrj));
            inner.call();
            OpenProjects.getDefault().close (new Project[] { nestedPrj, mainPrj });
        } else {
            assertEquals("However one project instance is there", 1, arr.size());
            assertEquals("arr[0] is equal to p", arr.get(0), mainPrj);
            inner.call();
            OpenProjects.getDefault().close (new Project[] { mainPrj });
        }

        if (OpenProjects.getDefault().getOpenProjects().length != 0) {
            fail("All projects shall be closed: " + Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        }
        assertFalse("No project is opened", OpenProjects.getDefault().isProjectOpen(mainPrj));
        assertFalse("No project is opened", OpenProjects.getDefault().isProjectOpen(nestedPrj));

        OpenProjectList.OPENING_RP.post(new Runnable() {public void run() {}}).waitFinished(); // flush running tasks
    }
}
