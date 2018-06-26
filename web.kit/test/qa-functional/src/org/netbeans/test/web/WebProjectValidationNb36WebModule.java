/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.test.web;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test web application project from existing sources.
 *
 * @author lm97939
 */
public class WebProjectValidationNb36WebModule extends WebProjectValidation {

    public static final String[] TESTS = new String[]{
        "testNewWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCompileAllJSP", "testCompileJSP",
        "testCleanAndBuildProject", "testRunProject", "testRunJSP",
        "testRunServlet", "testCreateTLD", "testCreateTagHandler", "testRunTag",
        "testNewHTML", "testRunHTML", "testNewSegment", "testNewDocument",
        "testFinish"
    };

    /** Need to be defined because of JUnit */
    public WebProjectValidationNb36WebModule(String name) {
        super(name);
        PROJECT_NAME = "WebModuleNB36"; // NOI18N
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebProjectValidationNb36WebModule.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return J2EE_4;
    }

    /** Test creation of web application.
     * - open New Project wizard from main menu (File|New Project)
     * - select Java Web|Web Application with Existing Sources
     * - in the next panel type project name and project location
     * - finish the wizard
     * - wait until scanning of java files is finished
     */
    @Override
    public void testNewWebProject() throws IOException {
        new NewProjectAction().perform();
        NewProjectWizardOperator projectWizard = new NewProjectWizardOperator();
        projectWizard.selectCategory("Java Web"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Web Application with Existing Sources");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtLocation().setText(getDataDir().getAbsolutePath()
                + File.separator + PROJECT_NAME);
        nameStep.txtProjectName().setText(PROJECT_NAME);
        nameStep.txtProjectFolder().setText(getWorkDirPath()
                + File.separator + PROJECT_NAME + "Prj");
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.cboServer().selectItem(0);
        serverStep.next();
        NewWebProjectSourcesStepOperator srcStep = new NewWebProjectSourcesStepOperator();
        srcStep.finish();
        // wait for project creation
        waitScanFinished();
        // not display browser on run
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).properties();
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"));
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(false);
        propertiesDialogOper.ok();
    }
}
