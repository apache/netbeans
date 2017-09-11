/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
