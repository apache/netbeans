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
package org.netbeans.test.permanentUI;

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.test.permanentUI.utils.ProjectContext;
import org.netbeans.test.permanentUI.utils.Utilities;

/**
 *
 * @author Lukas Hasik, Jan Peska, Marian.Mirilovic@oracle.com
 */
public class TeamMenuVCSActivatedTest extends MainMenuTestCase {

    /**
     * Need to be defined because of JUnit
     *
     * @param name
     */
    public TeamMenuVCSActivatedTest(String name) {
        super(name);
    }

    public static Test suite() {
        return TeamMenuVCSActivatedTest.emptyConfiguration().
                addTest(TeamMenuVCSActivatedTest.class, "testTeamMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_DiffSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_IgnoreSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_PatchesSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_BranchTagSubMenu").
                //no more in 8.0 addTest(TeamMenuVCSActivatedTest.class, "testTeam_QueuesSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_RemoteSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_RecoverSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_OtherVCSSubMenu").
                addTest(TeamMenuVCSActivatedTest.class, "testTeam_HistorySubMenu").
                clusters(".*").enableModules(".*").
                suite();
    }

    @Override
    public void initialize() throws IOException {
        String projectName = "SampleProject";
        openDataProjects(projectName);
        waitScanFinished();
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        projectNode.select();
        if (!isVersioningProject()) {
            versioningActivation(projectNode);
        }
    }

    @Override
    public ProjectContext getContext() {
        return ProjectContext.VERSIONING_ACTIVATED;
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testTeamMenu() {
        oneMenuTest("Team");
    }

    public void testTeam_DiffSubMenu() {
        oneSubMenuTest("Team|Diff", false);
    }

    public void testTeam_IgnoreSubMenu() {
        oneSubMenuTest("Team|Ignore", false);
    }

    public void testTeam_PatchesSubMenu() {
        oneSubMenuTest("Team|Patches", false);
    }

    public void testTeam_BranchTagSubMenu() {
        oneSubMenuTest("Team|Branch/Tag", false);
    }

/** no mor ein 8.0
 * public void testTeam_QueuesSubMenu() {
        oneSubMenuTest("Team|Queues", false);
    }
    */ 

    public void testTeam_RemoteSubMenu() {
        Utilities.projectName = "core-main";
        oneSubMenuTest("Team|Remote", false);
    }

    public void testTeam_RecoverSubMenu() {
        Utilities.projectName = "core-main";
        oneSubMenuTest("Team|Revert/Recover", false);
    }

    public void testTeam_OtherVCSSubMenu() {
        oneSubMenuTest("Team|Other VCS", false);
    }

    public void testTeam_HistorySubMenu() {
        oneSubMenuTest("Team|History", true);
    }

    /**
     * Create repository (Mercurial) for given project.
     *
     * @param projectNode
     */
    public void versioningActivation(ProjectRootNode projectNode) {
        projectNode.callPopup().pushMenu("Versioning|Initialize Git Repository...");
        captureScreen();
        NbDialogOperator wo2 = new NbDialogOperator("Initialize a Git Repository");
        wo2.ok();
    }

    /**
     * Check if project has local repository activated.
     *
     * @return true - VCS activated, else false
     */
    public boolean isVersioningProject() {
        int menuSize = getMainMenuItem("Team").getSubmenu().size();
        // push Escape key to ensure there is no thing blocking shortcut execution
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        return menuSize > 8;
    }

}
