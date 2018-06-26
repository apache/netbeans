/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.test.j2ee;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lm97939
 */
public class FreeFormProjects extends J2eeTestCase {

    /** Creates a new instance of AddMethodTest */
    public FreeFormProjects(String name) {
        super(name);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, FreeFormProjects.class,
                "testEjbWithSources",
                "testEarWithSources");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    public void testEjbWithSources() {
        String location = new File(getDataDir(), "freeform_projects/Secure/Secure-ejb").getAbsolutePath();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE");
        npwo.selectProject("EJB Module with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        //server settings panel - accept defaults
        npnlso.next();
        new JButtonOperator(npwo, "Add Folder...", 0).pushNoBlock();
        JFileChooserOperator j = new JFileChooserOperator();
        j.chooseFile("src" + File.separator + "java");
        j.approveSelection();
        npnlso.finish();
        //wait project appear in projects view
        Node projectNode = new ProjectsTabOperator().getProjectRootNode("Secure-ejb");
        // wait classpath scanning finished
        waitScanFinished();
        Node beansNode = new Node(projectNode, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node"));
        Node node = new Node(beansNode, "AccountStateSB");
        node.expand();
        String children[] = node.getChildren();
        assertTrue("AccountStateSB node has no children.", children.length > 0);
        new OpenAction().perform(node);
        new EditorOperator("AccountStateBean").close();
        new Node(projectNode, "Configuration Files|ejb-jar.xml");
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        projectNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
        new CloseAction().perform(projectNode);
    }

    public void testEarWithSources() {
        String location = new File(getDataDir(), "freeform_projects/Secure").getAbsolutePath();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE");
        npwo.selectProject("Enterprise Application with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        npnlso.next();
        npnlso.btFinish().pushNoBlock();
        new NbDialogOperator("Warning").ok();
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        npnlso.waitClosed();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode("Secure");

        Node n = new Node(rootNode, "Java EE Modules|Secure-war.war");
        n.performPopupAction("Open Project");
        n = new Node(rootNode, "Java EE Modules|Secure-ejb.jar");
        n.performPopupAction("Open Project");
        // wait classpath scanning finished
        waitScanFinished();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        rootNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
    }
}
