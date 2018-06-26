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
package org.netbeans.performance.j2ee.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test create projects
 *
 * @author lmartinek@netbeans.org
 */
public class DeployTest extends PerformanceTestCase {

    private Node node;
    private static final String PROJECT_NAME = "WebApp" + CommonUtilities.getTimeIndex();

    /**
     * Creates a new instance of CreateJ2EEProject
     *
     * @param testName the name of the test
     */
    public DeployTest(String testName) {
        super(testName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN = 5000;
    }

    /**
     * Creates a new instance of CreateJ2EEProject
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeployTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2EEBaseSetup.class)
                .addTest(DeployTest.class)
                .suite();
    }

    public void testDeploy() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        new J2eeServerNode("GlassFish").start();
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Java Web");
        wizard.selectProject("Web Application");
        wizard.next();
        NewWebProjectNameLocationStepOperator wizardLocation = new NewWebProjectNameLocationStepOperator();
        if (System.getProperty("os.name", "").contains("Windows")) {
            // #238007 - wizard too wide
            wizardLocation.txtProjectLocation().setText("C:\\tmp");
        } else {
            wizardLocation.txtProjectLocation().setText(getWorkDirPath());
        }
        wizardLocation.txtProjectName().setText(PROJECT_NAME);
        wizardLocation.next();
        wizardLocation.finish();
        node = pto.getProjectRootNode(PROJECT_NAME);
        node.performPopupAction("Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        MainWindowOperator.getDefault().waitStatusText("Finished building " + node.getText() + " (dist)");
        waitScanFinished();
    }

    @Override
    public void shutdown() {
        J2eeServerNode glassFishNode = J2eeServerNode.invoke("GlassFish");
        Node applicationsNode = new Node(glassFishNode, "Applications");
        new Node(applicationsNode, node.getText()).performPopupAction("Undeploy");
        applicationsNode.waitChildNotPresent(node.getText());
        glassFishNode.stop();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        node.performPopupAction("Deploy");
        MainWindowOperator.getDefault().waitStatusText("Finished building " + node.getText() + " (run-deploy).");
        return null;
    }

    @Override
    public void close() {
        try {
            URL url = new URL("http://localhost:8080/" + node.getText());
            InputStream stream = url.openStream();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Deployed application unavailable.", e);
        }
    }
}
