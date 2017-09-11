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
package org.netbeans.junit.ide;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.util.test.MockLookup;

/** Test of ProjectSupport class.
 * @author Jiri Skrivanek 
 */
@RandomlyFails
public class ProjectSupportTest extends NbTestCase {
    
    /** Creates a new test. 
     * @param testName name of test
     */
    public ProjectSupportTest(String testName) {
        super(testName);
    }

    /** Set up. */
    protected @Override void setUp() throws IOException {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        System.out.println("########  "+getName()+"  #######");
    }

    private static final String PROJECT_NAME = "SampleProject";
    private static File projectParentDir;
    
    /** Test createProject method. */
    public void testCreateProject() throws Exception {
        projectParentDir = this.getWorkDir();
        Project project = (Project)ProjectSupport.createProject(this.getWorkDir(), PROJECT_NAME);
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Created project not opened.", project, projects[0]);
    }
    
    /** Test closeProject method. */
    @SuppressWarnings("deprecation")
    public void testCloseProject() {
        assertTrue("Should return true if succeeded.", ProjectSupport.closeProject(PROJECT_NAME));
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("None project should be opened.", 0, projects.length);
        assertFalse("Should return false if project doesn't exist.", ProjectSupport.closeProject("Dummy"));
    }
    
    /** Test openProject method. */
    @SuppressWarnings("deprecation")
    public void testOpenProject() throws Exception {
        Project project = (Project)ProjectSupport.openProject(new File(projectParentDir, PROJECT_NAME));
        Project[] projects = OpenProjectList.getDefault().getOpenProjects();
        assertEquals("Only 1 project should be opened.", 1, projects.length);
        assertSame("Opened project not opened.", project, projects[0]);
        assertNull("Should return null if project doesn't exist.", ProjectSupport.openProject(new File(projectParentDir, "Dummy")));
    }
    
    /** Test waitScanFinished method. */
    public void testWaitScanFinished() {
        // TODO - somehow check this functionality
        ProjectSupport.waitScanFinished();
    }
}
