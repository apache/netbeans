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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.project.ui.api;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 * Tests for RecentProjects class
 * @author Milan Kubec
 */
public class RecentProjectsTest extends NbTestCase {

    private Project[] testProjects = new Project[15];
    private String[] tpDisplayNames = new String[15];
    private URL[] tpURLs = new URL[15];

    public static final String PRJ_NAME_PREFIX = "Project";

    public RecentProjectsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        clearWorkDir();
        FileObject workDirFO = FileUtil.toFileObject(getWorkDir());
        for (int i = 0; i < testProjects.length; i++) {
            String prjName = PRJ_NAME_PREFIX + (i + 1);
            FileObject p = TestSupport.createTestProject(workDirFO, prjName);
            TestSupport.TestProject tp = (TestSupport.TestProject) ProjectManager.getDefault().findProject(p);
            tp.setLookup(Lookups.fixed(new TestProjectInfo(prjName)));
            testProjects[i] = tp;
            tpDisplayNames[i] = ProjectUtils.getInformation(tp).getDisplayName();
            tpURLs[i] = tp.getProjectDirectory().toURL();
        }
    }

    public void testGetRecentProjectsInformation() {

        List<UnloadedProjectInformation> pil;

        for (Project p : testProjects) {
            OpenProjectList.getDefault().open(p, false);
        }

        // Close all projects in the list one by one
        for (int j = 0; j < testProjects.length; j++) {
            OpenProjectList.getDefault().close(new Project[] {testProjects[j]}, false);
            pil = RecentProjects.getDefault().getRecentProjectInformation();
            assertEquals(1, RecentProjects.getDefault().getRecentProjectInformation().size());
            assertEquals(tpDisplayNames[j], pil.get(0).getDisplayName());
            assertEquals(tpURLs[j], pil.get(0).getURL());
            OpenProjectList.getDefault().open(testProjects[j], false);
        }

        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());

        // Close rand number of rand modules
        OpenProjectList.getDefault().close(new Project[] {testProjects[3]}, false);
        OpenProjectList.getDefault().close(new Project[] {testProjects[4]}, false);
        OpenProjectList.getDefault().close(new Project[] {testProjects[6]}, false);
        OpenProjectList.getDefault().close(new Project[] {testProjects[10]}, false);
        OpenProjectList.getDefault().close(new Project[] {testProjects[12]}, false);

        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(5, RecentProjects.getDefault().getRecentProjectInformation().size());

        assertEquals(tpDisplayNames[12], pil.get(0).getDisplayName());
        assertEquals(tpURLs[12], pil.get(0).getURL());

        assertEquals(tpDisplayNames[10], pil.get(1).getDisplayName());
        assertEquals(tpURLs[10], pil.get(1).getURL());

        assertEquals(tpDisplayNames[6], pil.get(2).getDisplayName());
        assertEquals(tpURLs[6], pil.get(2).getURL());

        assertEquals(tpDisplayNames[4], pil.get(3).getDisplayName());
        assertEquals(tpURLs[4], pil.get(3).getURL());

        assertEquals(tpDisplayNames[3], pil.get(4).getDisplayName());
        assertEquals(tpURLs[3], pil.get(4).getURL());

        OpenProjectList.getDefault().open(testProjects[3], false);
        OpenProjectList.getDefault().open(testProjects[4], false);
        OpenProjectList.getDefault().open(testProjects[6], false);
        OpenProjectList.getDefault().open(testProjects[10], false);
        OpenProjectList.getDefault().open(testProjects[12], false);

        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());

        // Close ten projects
        for (int k = 3; k < 13; k++) {
            OpenProjectList.getDefault().close(new Project[] {testProjects[k]}, false);
        }
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(10, RecentProjects.getDefault().getRecentProjectInformation().size());
        for (int l = 0; l > 10; l++) {
            assertEquals(tpDisplayNames[12 - l], pil.get(l).getDisplayName());
            assertEquals(tpURLs[12 - l], pil.get(l).getURL());
        }
        for (int m = 3; m < 13; m++) {
            OpenProjectList.getDefault().open(testProjects[m], false);
        }

        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());

        // Open and close more than ten projects
        for (Project p : testProjects) {
            OpenProjectList.getDefault().close(new Project[] {p}, false);
        }
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(10, RecentProjects.getDefault().getRecentProjectInformation().size());
        for (int p = 0; p > 10; p++) {
            assertEquals(tpDisplayNames[testProjects.length - p], pil.get(p).getDisplayName());
            assertEquals(tpURLs[testProjects.length - p], pil.get(p).getURL());
        }
        for (Project p : testProjects) {
            OpenProjectList.getDefault().open(p, false);
        }

        assertEquals(0, RecentProjects.getDefault().getRecentProjectInformation().size());

        // close array of projects
        OpenProjectList.getDefault().close(new Project[] {testProjects[2], testProjects[5], testProjects[9], testProjects[11]}, false);
        pil = RecentProjects.getDefault().getRecentProjectInformation();
        assertEquals(4, RecentProjects.getDefault().getRecentProjectInformation().size());

        assertEquals(tpDisplayNames[11], pil.get(0).getDisplayName());
        assertEquals(tpURLs[11], pil.get(0).getURL());

        assertEquals(tpDisplayNames[9], pil.get(1).getDisplayName());
        assertEquals(tpURLs[9], pil.get(1).getURL());

        assertEquals(tpDisplayNames[5], pil.get(2).getDisplayName());
        assertEquals(tpURLs[5], pil.get(2).getURL());

        assertEquals(tpDisplayNames[2], pil.get(3).getDisplayName());
        assertEquals(tpURLs[2], pil.get(3).getURL());

    }

    // -------------------------------------------------------------------------

    private static class TestProjectInfo implements ProjectInformation {

        private String displayName;

        public TestProjectInfo(String dname) {
            displayName = dname;
        }
        public String getName() {
            return displayName;
        }
        public String getDisplayName() {
            return displayName;
        }
        public Icon getIcon() {
            return null;
        }
        public Project getProject() {
            return null;
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {}
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }

}
