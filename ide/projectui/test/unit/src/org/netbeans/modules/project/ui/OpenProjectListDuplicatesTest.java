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

package org.netbeans.modules.project.ui;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

public class OpenProjectListDuplicatesTest extends NbTestCase {
    static final Logger LOG = Logger.getLogger("test.TestProjectOpenedHookImpl");
    
    public OpenProjectListDuplicatesTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    public void testParallelOpenOfTheSameProjectWithSlowHook() throws Exception {
        MockLookup.setInstances(new TestSupport.TestProjectFactory());
        clearWorkDir();
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        assertNotNull(workDir);
        FileObject prj = TestSupport.createTestProject(workDir, "prj1");
        final TestSupport.TestProject p = (TestSupport.TestProject) ProjectManager.getDefault().findProject(prj);
        assertNotNull("Project found", p);
        final TestProjectOpenedHookImpl hook = new TestProjectOpenedHookImpl();
        p.setLookup(Lookups.singleton(hook));

        OpenProjectList.waitProjectsFullyOpen();
        assertEquals("Initially empty", 0, OpenProjects.getDefault().getOpenProjects().length);
        assertEquals("Initially empty2", 0, OpenProjects.getDefault().openProjects().get().length);


        
        class Fake implements Project {
            public FileObject getProjectDirectory() {
                return p.getProjectDirectory();
            }

            public Lookup getLookup() {
                return p.getLookup();
            }

            @Override
            public boolean equals(Object obj) {
                boolean ret = false;
                if (obj instanceof Project) {
                    Project p = (Project)obj;
                    ret = p.getProjectDirectory().equals(getProjectDirectory());
                }
                LOG.info("Is fake equal to " + obj + " result: " + ret);
                return ret;
            }

            @Override
            public int hashCode() {
                return p.hashCode();
            }
        }

        LOG.info("Before first open");
        Fake f = new Fake();
        assertFalse("null is not open", OpenProjects.getDefault().isProjectOpen(null));
        OpenProjects.getDefault().open(new Project[] { f }, false);
        assertTrue("Fake is open", OpenProjects.getDefault().isProjectOpen(f));
        assertTrue("Fake is open, but real one is reported open too", OpenProjects.getDefault().isProjectOpen(p));
        LOG.info("After first and Before 2nd open");
        OpenProjects.getDefault().open(new Project[] { p }, false);
        LOG.info("After 2nd open");
        assertTrue("Real one is open", OpenProjects.getDefault().isProjectOpen(p));
        assertTrue("Fake is open too", OpenProjects.getDefault().isProjectOpen(f));


        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertEquals("However one instance is there", 1, arr.size());
        assertEquals("Open hook called", 1, TestProjectOpenedHookImpl.opened);
        assertEquals("arr[0] is equal to p", arr.get(0), p);

        OpenProjects.getDefault().close (new Project[] { p });
        if (OpenProjects.getDefault().getOpenProjects().length != 0) {
            fail("All projects shall be closed: " + Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        }
        assertFalse("No project is opened", OpenProjects.getDefault().isProjectOpen(p));
        assertFalse("No project is opened", OpenProjects.getDefault().isProjectOpen(f));

        OpenProjectList.OPENING_RP.post(new Runnable() {public void run() {}}).waitFinished(); // flush running tasks
        assertEquals("Close hook called", 1, TestProjectOpenedHookImpl.closed);
    }
    
    private static class TestProjectOpenedHookImpl extends ProjectOpenedHook {
        
        public static int opened = 0;
        public static int closed = 0;
        
        
        protected void projectClosed() {
            closed++;
        }
        
        protected void projectOpened() {
            LOG.log(Level.INFO, "projectOpened waiting");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            opened++;
            LOG.log(Level.INFO, "projectOpened done", new Exception("From here!"));
        }
        
    }

}
