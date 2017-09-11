/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
