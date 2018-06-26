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
package org.netbeans.modules.groovy.qaf;

import java.io.File;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.util.Utilities;

/**
 *
 * @author lukas
 */
public abstract class GrailsTestCase extends GroovyTestCase {

    private static final Logger LOGGER = Logger.getLogger(GrailsTestCase.class.getName());
    private static boolean haveGrails = false;

    public GrailsTestCase(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        if (!haveGrails) {
            //we may want to bypass defaults in test
            String grailsHome = System.getProperty("grails.home"); //NOI18N
            if (grailsHome == null || grailsHome.trim().length() == 0) {
                //try fallback to the defaults
                GrailsSettings gs = GrailsSettings.getInstance();
                assertNotNull("Grails missing", gs.getGrailsBase()); //NOI18N
            } else {
                if (Utilities.isUnix()) {
                    assertTrue(new File(grailsHome, "bin/grails").isFile()); //NOI18N
                } else {
                    assertTrue(new File(grailsHome, "bin" + File.separator + "grails.bat").isFile()); //NOI18N
                }
                GrailsSettings gs = GrailsSettings.getInstance();
                gs.setGrailsBase(new File(grailsHome).getCanonicalPath());
            }
            haveGrails = true;
            LOGGER.info("Using Grails at: " + GrailsSettings.getInstance().getGrailsBase()); //NOI18N
        }
        assertTrue("Grails missing", haveGrails); //NOI18N
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.GROOVY;
    }

    protected void createNewGrailsFile(Project p, String type, String name) {
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.wizards.Bundle", type);
        createNewGroovyFile(p, label);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(name);
        String createdFile = op.txtCreatedFile().getText();
        op.btFinish().pushNoBlock();
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 90000); //NOI18N
        op.waitClosed();
        assertTrue(new File(createdFile).isFile());
    }

    protected void runGrailsApp() {
        //Run
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.Bundle", "LBL_RunAction_Name");
        getProjectRootNode().performPopupAction(label);
        waitFor("run-app", ":INFO:  GSP servlet initialized"); //NOI18N
        assertNotNull(getServerNodeForApp());
    }

    protected void stopGrailsApp() {
        //Stop
        String actionLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grails.server.Bundle", "ApplicationNode.stopActionName");
        getServerNodeForApp().performPopupAction(actionLabel);
    }

    private Node getServerNodeForApp() {
        String bundle = "org.netbeans.modules.groovy.grails.server.Bundle"; //NOI18N
        GrailsSettings gs = GrailsSettings.getInstance();
        //Jetty (Grails {0})
        String serverLabel = Bundle.getStringTrimmed(bundle, "GrailsInstance.displayName", //NOI18N
                new Object[] {RuntimeHelper.getRuntimeVersion(new File(gs.getGrailsBase()))});
        J2eeServerNode serverNode = J2eeServerNode.invoke(serverLabel);
        //{app} on {port} //app node
        String nodeLabel = Bundle.getStringTrimmed(bundle, "ApplicationNode.displayName", //NOI18N
                new Object[] {getProjectName(), gs.getPortForProject(getProject())});
        return new Node(serverNode, nodeLabel);
    }

    /**
     * Wait for text in an output tab
     *
     * @param action action name as it appears in output tab label
     * @param text text to wait for
     */
    protected void waitFor(String action, String text) {
        OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = oo.getOutputTab(getProjectName() + " (" + action + ")"); //NOI18N
        // wait at most 360 second until progress dialog dismiss
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 360000); //NOI18N
        oto.waitText(text);
    }
}
