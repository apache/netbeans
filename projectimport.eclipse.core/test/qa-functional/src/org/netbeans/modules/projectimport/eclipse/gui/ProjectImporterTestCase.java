/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.projectimport.eclipse.gui;

import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTestCase;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author mkhramov@netbeans.org
 */
public abstract class ProjectImporterTestCase  extends NbTestCase {
    
    private final static String caption = Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.wizard.Bundle", "CTL_WizardTitle");

    protected ProjectsTabOperator pto = null;
    public ProjectImporterTestCase(String testName) {
        super(testName);
    }
    protected static void ExtractToWorkDir(File dataDir, String archiveName) throws FileNotFoundException, Exception {
        TestFileUtils.unpackZipFile(new File(dataDir, archiveName), dataDir);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log("Executing: "+this.getName());
        clearWorkDir();
    }
    protected static WizardOperator invokeImporterWizard() {
        new ActionNoBlock(ImporterMenu.menuItemString, null).performMenu();
        return new WizardOperator(caption);
    }
    
    protected static void selectProjectByIndex(TableModel model, int index) {
        model.setValueAt(true, index, 0);
    }
    protected static void selectProjectByName(TableModel model, String projectName) {
        int index = getIndexByName(model,projectName);
        selectProjectByIndex(model, index);
    }
    private static int getIndexByName(TableModel model, String projectName) {
        int length = model.getRowCount();
        String name;
        for(int i =0; i< length; i++) {
            name = (String)model.getValueAt(i, 1);
            if(name.startsWith(projectName)) return i;
        }
        return 0;
    }
    protected void waitForProjectsImporting() {
        String importingProjectsTitle = Bundle.getStringTrimmed("org.netbeans.modules.projectimport.eclipse.core.Bundle", "CTL_ProgressDialogTitle");
        try {
            // wait at most 120 second until progress dialog dismiss
            NbDialogOperator openingOper = new NbDialogOperator(importingProjectsTitle);
            openingOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
            openingOper.waitClosed();
        } catch(TimeoutExpiredException ex) {
            //ignore
        }

    }
    protected void validateProjectRootNode(String projectName) {
        pto = new ProjectsTabOperator();
        try {
            pto.getProjectRootNode(projectName);
        } catch(TimeoutExpiredException tex) {
            fail("No project [ "+projectName+" ]loaded");
        } 
    }
    protected void validateProjectSrcNode(String projectName, String srcRootName) {
        validateProjectNode(projectName, srcRootName);
    }
    protected void validateProjectTestNode(String projectName, String testRootName) {
        validateProjectNode(projectName, testRootName);
    }
    protected void validateProjectTestLibNode(String projectName) {
        String nodeName = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "CTL_TestLibrariesNode");
        validateProjectNode(projectName, nodeName);
    }
    protected void validateProjectWebNode(String projectName) {
        pto = new ProjectsTabOperator();
        String nodeName = Bundle.getStringTrimmed("org.netbeans.modules.web.project.Bundle", "LBL_Node_DocBase");
        validateProjectNode(projectName, nodeName);
    }
    protected void validateLibrary(String projectName, String libraryName) {
        String librariesNode = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "CTL_LibrariesNode");
        validateProjectNode(projectName,librariesNode+"|"+libraryName);
    }    
    private void validateProjectNode(String projectName,String nodeName) {
        pto = new ProjectsTabOperator();
        try {
           ProjectRootNode node = pto.getProjectRootNode(projectName);
           new Node(node,nodeName);
        } catch(TimeoutExpiredException exc) {
            fail("Cannot find expected [ "+nodeName+" ] node in "+projectName);
        }        
    }

    protected void selectProjectFromWS(WizardOperator wizz, String workspace, String projectToImport) {
        JTextFieldOperator txtWorkspaceLocation = new JTextFieldOperator(wizz, 0);
        String workspacePath = getDataDir().getAbsolutePath() + File.separatorChar + workspace;
        txtWorkspaceLocation.setText(workspacePath);
        wizz.next();
        JTableOperator projectsTable = new JTableOperator(wizz);
        TableModel model = projectsTable.getModel();
        selectProjectByName(model, projectToImport);
    }

    protected NbDialogOperator invokeProjectPropertiesDialog(String projectName, String nodePath) {
        pto = new ProjectsTabOperator();
        ProjectRootNode projectRoot = null;
        try {
            projectRoot = pto.getProjectRootNode(projectName);
        } catch (TimeoutExpiredException tex) {
            fail("No project [ " + projectName + " ] loaded");
        }
        projectRoot.properties();
        String propsDialogCaption = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.customizer.Bundle", "LBL_Customizer_Title", new Object[]{projectName});
        NbDialogOperator propsDialog = null;
        try {
            propsDialog = new NbDialogOperator(propsDialogCaption);
        } catch (TimeoutExpiredException tex) {
            fail("Unable to open project [ " + projectName + " ] properties dialog");
        }
        JTreeOperator tree = new JTreeOperator(propsDialog);
        TreePath path = tree.findPath(nodePath);
        tree.selectPath(path);
        return propsDialog;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log("Test "+this.getName()+" completed");
    }

}
