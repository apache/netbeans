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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.test.j2ee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.ide.WatchProjects;

/**
 * Overall validation suite for Java EE. <br/> To run this single test use "Test
 * File" action and provide path to GlassFish server instance in property
 * test-qa-functional-sys-prop.glassfish.home. It can be stored in
 * nbbuild/user.build.properties and thus available for all modules. <br/> To
 * run this test from command line use:
 * <pre>
 * ant -f j2ee.kit/build.xml -Dtest.config=uicommit test
 * </pre> <br/>To run this test from binary distribution use:
 * <pre>
 * ant -f j2ee.kit/build.xml test-build
 * cd nbbuild/build/testdist
 * ant -Dnetbeans.dest.dir=main/nbbuild/netbeans
 *      -Dtest.types=qa-functional
 *      -Dmodules.list=enterprise/org-netbeans-modules-j2ee-kit
 *      -Dtest.config.default.includes=**\/J2EEValidation.class
 *      -Dtest-sys-prop.glassfish.home=C:/space/hudson/glassfish
 * </pre>
 *
 * @author Jiri Skrivanek
 */
public class J2EEValidation extends J2eeTestCase {

    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "SampleWebProject";  //NOI18N

    /**
     * Need to be defined because of JUnit
     * @param name test name
     */
    public J2EEValidation(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, J2EEValidation.class, "testWebApplication");
    }

    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    public void testEmpty() {
        fail("GlassFish server registration fails.");
    }
    
    /**
     * Test Web Application
     * <pre>
     * - create new Web Application project
     * - wait until project is in Projects view
     * - wait classpath scanning finished
     * - set option to not open browser at run
     * - insert error statement into index.jsp, compile it and verify it failed
     * - correct error in index.jsp, compile it and verify it succeded
     * - run project from context menu on project's root node
     * - wait until JSP Page is accessible through HTTP connection
     * - stop application server
     * </pre>
     */
    public void testWebApplication() {
        // workaround for jelly issue
        NewProjectWizardOperator.invoke().cancel();
        //addServer();
        // create new web application project
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        // "Web"
        String webLabel = Bundle.getString("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        npwo.selectCategory(webLabel);
        // "Web Application"
        String webApplicationLabel = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
        npwo.selectProject(webApplicationLabel);
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtProjectName().setText(SAMPLE_WEB_PROJECT_NAME);
        npnlso.txtProjectLocation().setText(System.getProperty("netbeans.user")); // NOI18N
        npnlso.next();
        npnlso.finish();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // wait classpath scanning finished
        WatchProjects.waitScanFinished();

        // not display browser on run

        // open project properties
        new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME).properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // "Run"
        String runLabel = Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run");
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), runLabel).select();
        String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();

        // Create JSP
        NewJspFileNameStepOperator nameStep = NewJspFileNameStepOperator.invoke();
        nameStep.setJSPFileName("index");
        nameStep.finish();
        // wait index.jsp is opened in editor
        EditorOperator editor = new EditorOperator("index.jsp"); // NOI18N

        // Compile JSP
        Node projectRootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        // "Web Pages"
        String webPagesLabel = Bundle.getString(
                "org.netbeans.modules.web.project.ui.Bundle", "LBL_Node_DocBase");
        Node jspNode = new Node(projectRootNode, webPagesLabel + "|index.jsp"); // NOI18N
        // insert error statement
        editor.insert("<%= nonExistentVar %>", 12, 1);

        WatchProjects.waitScanFinished();

        CompileJavaAction compileAction = new CompileJavaAction();
        compileAction.perform(jspNode);
        // "SampleWebProject (compile-single-jsp)"
        String outputTarget = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "TITLE_output_target",
                new Object[]{SAMPLE_WEB_PROJECT_NAME, null, "compile-single-jsp"});  // NOI18N
        // "Build of SampleWebProject (compile-single-jsp) failed."
        String failedMessage = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_target_failed_status",
                new Object[]{outputTarget});
        // "Finished building SampleWebProject (compile-single-jsp)"
        String finishedMessage = Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                new Object[]{outputTarget});
        MainWindowOperator.getDefault().waitStatusText(failedMessage);
        // check error message is printed
        new OutputTabOperator("compile-single-jsp").waitText("nonExistentVar"); // NOI18N
        // correct JSP file
        editor.replace("<%= nonExistentVar %>", "");
        // compile again
        compileAction.perform(jspNode);
        MainWindowOperator.getDefault().waitStatusText(finishedMessage);

        // Run project
        try {
            new Action(null, "Run").perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
            waitText(SAMPLE_WEB_PROJECT_NAME + "/index.jsp", 240000, "JSP Page");
        } finally {
            // log messages from output
            getLog("RunOutput").print(new OutputTabOperator(SAMPLE_WEB_PROJECT_NAME).getText()); // NOI18N
            getLog("ServerLog").print(new OutputTabOperator("GlassFish").getText());
            // stop server
            try {
                J2eeServerNode serverNode = new J2eeServerNode("GlassFish");
                serverNode.stop();
            } catch (JemmyException e) {
                // ignore it
            }
        }
    }

    /**
     * Opens URL connection and waits for given text. It throws
     * TimeoutExpiredException if timeout expires.
     *
     * @param urlSuffix suffix added to server URL
     * @param timeout time to wait
     * @param text text to be found
     */
    public static void waitText(final String urlSuffix, final long timeout, final String text) {
        Waitable waitable = new Waitable() {
            @Override
            public Object actionProduced(Object obj) {
                InputStream is = null;
                try {
                    URLConnection connection = new URI("http://localhost:8080/" + urlSuffix).toURL().openConnection();
                    connection.setReadTimeout(Long.valueOf(timeout).intValue());
                    is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.indexOf(text) > -1) {
                            return Boolean.TRUE;
                        }
                        line = br.readLine();
                    }
                    is.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return ("Text \"" + text + "\" at http://localhost:8080/" + urlSuffix);
            }
        };
        Waiter waiter = new Waiter(waitable);
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
        try {
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Exception while waiting for connection.", e);
        }
    }
}
