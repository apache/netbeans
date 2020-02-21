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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.test.cnd;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JDialog;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * CND commit validation suite.
 *
 */
public class CNDValidation extends JellyTestCase {

    static final String[] tests = {
        "testCreateSampleProject",
        "testClassView",
        "testBuildProject"
    };

    /**
     * Creates a new instance of CNDValidation
     */
    public CNDValidation(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(CNDValidation.class).addTest(tests).clusters(".*").enableModules(".*").gui(true).suite();
    }

    /**
     * Setup before every test case.
     */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /**
     * Clean up after every test case.
     */
    @Override
    public void tearDown() {
    }
    private static final String SAMPLE_PROJECT_NAME = "Welcome";

    /**
     * Test new project
     * <pre>
     * - open new project wizard
     * - select Samples|C/C++ Development|C/C++ category
     * - select Welcome project
     * - wait until wizard is closed
     * - close possible error dialogs when compiler is not found
     * - check project node appears in project view
     * </pre> 
     */
    public void testCreateSampleProject() throws Exception {
        NewProjectWizardOperator.invoke().cancel(); //MacOS issue workaround
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Samples"
        String samplesLabel = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
        String develLabel = Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Samples/Native");
        //String ccLabel = Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle", "Templates/Project/Samples/Native/Applications");
        //npwo.selectCategory(samplesLabel + "|" + develLabel + "|" + ccLabel);
        npwo.selectCategory(samplesLabel + "|" + develLabel);
        npwo.selectProject(SAMPLE_PROJECT_NAME);
        npwo.next();
        // close "No C/C++ Compilers Found" dialog
        final AtomicBoolean stopClosingThread = new AtomicBoolean(false);
        new Thread(new Runnable() {

            @Override
            public void run() {
                String title = Bundle.getStringTrimmed("org.netbeans.modules.cnd.toolchain.compilerset.Bundle", "NO_COMPILERS_FOUND_TITLE");
                while (!stopClosingThread.get()) {
                    JDialog jDialog =  JDialogOperator.findJDialog(title, false, false);
                    if (jDialog != null) {
                        new NbDialogOperator(jDialog).close();
                        return;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                }
            }
        }).start();
        NewCNDProjectNameLocationStepOperator npnlso = new NewCNDProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 120000);
        // wait for initialization
        new JComboBoxOperator(npnlso).waitComponentEnabled();
        new EventTool().waitNoEvent(500);
        // finish wizard
        npnlso.finish();
        // wait project appear in projects view
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
        stopClosingThread.set(true);
    }

    /**
     * Test Class View
     * <pre>
     * - open Window|Classes View
     * - check Welcome|main node is available
     * </pre>
     */
    public void testClassView() {
//        TopComponentOperator projectView = new TopComponentOperator("Projects");
//        new Node(new JTreeOperator(projectView), SAMPLE_PROJECT_NAME+"|Header Files|welcome.h").performPopupActionNoBlock("Open");
        new Action("Window|Classes", null).perform(); // NOI18N
        TopComponentOperator classView = new TopComponentOperator("Classes"); // NOI18N
        Node node = new Node(new JTreeOperator(classView), SAMPLE_PROJECT_NAME + "|main");
    }

    /**
     * Test build project
     * <pre>
     * - call Clean and Build on project node
     * - if compiler is not set, close 'Resolve Missing...' dialog
     * - otherwise wait for clean and build finished
     * </pre>
     */
    public void testBuildProject() {
        Node projectNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_PROJECT_NAME);
        // "Clean and Build"
        String buildItem = Bundle.getString("org.netbeans.modules.cnd.makeproject.ui.Bundle", "LBL_RebuildAction_Name");
        // start to track Main Window status bar
        MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
        stt.start();
        new ActionNoBlock(null, buildItem).perform(projectNode);
        try {
            // wait for possible dialog when compiler are not set
            NbDialogOperator resolveOper = new NbDialogOperator("Resolve Missing Native Build Tools");
            // close and finish the test
            resolveOper.close();
            return;
        } catch (TimeoutExpiredException e) {
            // ignore when it doesn't appear
        }
        // if some compiler exists
        try {
            NbDialogOperator downloadOper = new NbDialogOperator("Download Tool Collection");
            new JButtonOperator(downloadOper, "No").clickMouse();

            NbDialogOperator resolveOper = new NbDialogOperator(Bundle.getString("org.netbeans.modules.cnd.api.toolchain.ui.Bundle", "LBL_ResolveMissingTools_Title"));
            // select GNU collection

            JButtonOperator removeButton = new JButtonOperator(resolveOper, "Remove");
            removeButton.press();
            removeButton.release();

            new JButtonOperator(resolveOper, "OK").clickMouse();
        } catch (TimeoutExpiredException e) {
            // ignore when it doesn't appear
        }


        // wait message "Clean successful"
        stt.waitText("Clean successful", true); // NOI18N
        // wait message "Build successful."
        stt.waitText(Bundle.getString("org.netbeans.modules.cnd.builds.Bundle", "MSG_BuildFinishedOK"), true);
        stt.stop();
    }
}
