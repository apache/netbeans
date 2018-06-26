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
package org.netbeans.modules.ws.qaf.rest;

import java.io.File;
import java.io.IOException;
import javax.swing.ListModel;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * Tests for New REST service client stub wizard
 *
 * @author lukas
 */
public class RestCStubsTest extends RestTestBase {

    private static boolean haveProjects = false;

    public RestCStubsTest(String name) {
        super(name);
    }

    public RestCStubsTest(String name, Server server) {
        super(name, server);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!haveProjects) {
            File f = new File(getProjectsRootDir(), getProjectType().isAntBasedProject()
                    ? "FromEntities" : "MvnFromEntities"); //NOI18N
            assertTrue("dependent project not found", f.exists() && f.isDirectory());
            Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
            assertNotNull(p);
            if (!OpenProjectList.getDefault().isOpen(p)) {
                openProjects(f.getAbsolutePath());
            }
            f = new File(getProjectsRootDir(), getProjectType().isAntBasedProject()
                    ? "FromPatterns" : "MvnFromPatterns"); //NOI18N
            assertTrue("dependent project not found", f.exists() && f.isDirectory());
            p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(f));
            assertNotNull(p);
            if (!OpenProjectList.getDefault().isOpen(p)) {
                openProjects(f.getAbsolutePath());
            }
            haveProjects = true;
        }
    }

    @Override
    protected String getProjectName() {
        return "RESTClient"; //NOI18N
    }

    /**
     * Test the wizard:
     * - select target folder using browse... (browse folders dialog)
     * - add 2 projects
     * - remove 1 project
     * - then Cancel the wizard
     */
    public void testWizard() {
        //invoke the wizard
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        String path = FileUtil.toFile(getProject("FromEntities").getProjectDirectory()).getAbsolutePath(); //NOI18N
        String path2 = FileUtil.toFile(getProject("FromPatterns").getProjectDirectory()).getAbsolutePath(); //NOI18N
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        //browse to set target folder
        String browseLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_BrowseFolder");
        JButtonOperator jbo = new JButtonOperator(wo, browseLabel);
        jbo.push();
        String browseFoldersLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_BrowseFolders");
        NbDialogOperator ndo = new NbDialogOperator(browseFoldersLabel);
        JTreeOperator jto = new JTreeOperator(ndo);
        new org.netbeans.jellytools.nodes.Node(jto, jto.findPath("Source Packages")).select(); //NOI18N
        ndo.ok();
        assertEquals("browse selection not propagated", "", new JTextFieldOperator(wo, 2).getText().trim()); //NOI18N
        jbo.push();
        ndo = new NbDialogOperator(browseFoldersLabel);
        jto = new JTreeOperator(ndo);
        new org.netbeans.jellytools.nodes.Node(jto, "Web Pages|WEB-INF").select(); //NOI18N
        ndo.ok();
        assertEquals("browse selection not propagated", "WEB-INF", new JTextFieldOperator(wo, 2).getText().trim()); //NOI18N
        //add project
        addProject(wo, path);
        new EventTool().waitNoEvent(300);
        JListOperator jlo = new JListOperator(wo, 1);
        ListModel lm = jlo.getModel();
        assertEquals(1, lm.getSize());
        //add second project
        addProject(wo, path2);
        new EventTool().waitNoEvent(300);
        assertEquals(2, lm.getSize());
        //select first project
        jlo.selectItem(0);
        //remove it
        String removeLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_RemoveProject");
        new JButtonOperator(wo, removeLabel).push();
        new EventTool().waitNoEvent(300);
        assertEquals(1, lm.getSize());
        //cancel/close the wizard
        wo.cancel();
    }

    /**
     * Test stubs creation from a foreign project
     */
    public void testCreateSimpleStubs() {
        createStubs("FromEntities"); //NOI18N
    }

    /**
     * Test stubs creation from a local WADL file
     */
    public void testFromWADL() throws IOException {
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        String browseLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_BrowseFolder");
        String browseFoldersLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_BrowseFolders");
        JButtonOperator jbo = new JButtonOperator(wo, browseLabel);
        jbo.push();
        new NbDialogOperator(browseFoldersLabel).cancel();
        assertEquals("browse selection propagated", "rest", new JTextFieldOperator(wo, 2).getText().trim()); //NOI18N
        //click on the use wadl button
        JRadioButtonOperator jrbo = new JRadioButtonOperator(wo, 1);
        jrbo.clickMouse();
        JTextFieldOperator jtfo = new JTextFieldOperator(wo, 0);
        jtfo.setText(new File(getRestDataDir(), "testApplication.wadl").getCanonicalFile().getAbsolutePath()); //NOI18N
        //click on the radio button again to force the wizard to revalidate itself
        //WA for: http://www.netbeans.org/issues/show_bug.cgi?id=128445
        jrbo.clickMouse();
        wo.finish();
        //Generating Client Stubs From RESTful Web Services...
        String progressLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientStubsProgress");
        waitDialogClosed(progressLabel);
    }

    protected void createStubs(String sourceProject) {
        String sourcePath = FileUtil.toFile(getProject(sourceProject).getProjectDirectory()).getAbsolutePath();
        //RESTful Web Service Client Stubs
        String cStubsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "Templates/WebServices/RestClientStubs");
        createNewWSFile(getProject(), cStubsLabel);
        WizardOperator wo = new WizardOperator(cStubsLabel);
        addProject(wo, sourcePath);
        wo.finish();
        //Generating Client Stubs From RESTful Web Services...
        String progressLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_ClientStubsProgress");
        waitDialogClosed(progressLabel);
    }

    private void addProject(WizardOperator wo, String path) {
        //Add Project...
        String addProjectLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.wizard.Bundle", "LBL_AddProject");
        new JButtonOperator(wo, addProjectLabel).pushNoBlock();
        JFileChooserOperator fileChooserOp = new JFileChooserOperator();
        fileChooserOp.setSelectedFile(new File(path));
        fileChooserOp.approve();
    }

    private Project getProject(String name) {
        if (!getProjectType().isAntBasedProject()) {
            name = "Mvn" + name;
        }
        ProjectRootNode n = ProjectsTabOperator.invoke().getProjectRootNode(name);
        return ((Node) n.getOpenideNode()).getLookup().lookup(Project.class);
    }

    /**
     * Creates suite from particular test cases. You can define order of testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, RestCStubsTest.class,
                "testWizard", //NOI18N
                "testCreateSimpleStubs", //NOI18N
                "testFromWADL", //NOI18N
                "testCloseProject" //NOI18N
                );
    }
}
