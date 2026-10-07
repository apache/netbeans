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
package org.netbeans.modules.project.ui.groups;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport;
import org.openide.filesystems.FileUtil;

public class GroupTest extends NbTestCase {
    public GroupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(TestSupport.TestProjectFactory.class);
        Group.setActiveGroup(null, false);
        assertEquals("No group is active", null, OpenProjects.getDefault().getActiveProjectGroup());
    }

    public void testSwitchingToAgroup() {
        var g = AdHocGroup.create("adHoc1", false);
        Group.setActiveGroup(g, true);
        var active = OpenProjects.getDefault().getActiveProjectGroup().getName();
        assertEquals("Group was switched", g.getName(), active);
    }

    public void testSwitchingBackToNoGroupClearsOpenedProjects() throws Exception {
        var root = FileUtil.toFileObject(getWorkDir());
        var fo1 =  TestSupport.createTestProject(root, "prj1");
        var prj1 = ProjectManager.getDefault().findProject(fo1);
        var fo2 =  TestSupport.createTestProject(root, "prj2");
        var prj2 = ProjectManager.getDefault().findProject(fo2);

        OpenProjects.getDefault().open(new Project[] { prj1, prj2 }, false);

        var twoOrig = OpenProjects.getDefault().openProjects().get();
        assertEquals(2, twoOrig.length);
        assertEquals("1st", twoOrig[0], prj1);
        assertEquals("2nd", twoOrig[1], prj2);

        var g = AdHocGroup.create("empty", false);
        Group.setActiveGroup(g, true);
        var none = OpenProjects.getDefault().openProjects().get();
        assertEquals("Now there are no projects open", 0, none.length);

        Group.setActiveGroup(null, false);
        var backToNoGroup = OpenProjects.getDefault().openProjects().get();
        assertEquals("Fresh empty group is opened", 0, backToNoGroup.length);
    }

}
