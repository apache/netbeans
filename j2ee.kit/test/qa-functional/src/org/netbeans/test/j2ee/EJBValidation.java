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
package org.netbeans.test.j2ee;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase.Server;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.j2ee.addmethod.AddCMPFieldTest;
import org.netbeans.test.j2ee.addmethod.AddFinderMethodTest;
import org.netbeans.test.j2ee.addmethod.AddMethodTest;
import org.netbeans.test.j2ee.addmethod.AddSelectMethodTest;
import org.netbeans.test.j2ee.addmethod.CallEJBTest;
import org.netbeans.test.j2ee.addmethod.SendMessageTest;
import org.netbeans.test.j2ee.addmethod.UseDatabaseTest;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.lib.Utils;

/**
 * EJBValidation suite for J2EE. Uses 1.4 version.
 * 
 * @author Libor Martinek
 */
public class EJBValidation extends J2eeTestCase {

    public static final String EAR_PROJECT_NAME = "TestingEntApp";
    public static final String WEB_PROJECT_NAME = EAR_PROJECT_NAME + "-WebModule";
    public static final String EJB_PROJECT_NAME = EAR_PROJECT_NAME + "-EJBModule";
    public static File EAR_PROJECT_FILE;
    public static File EJB_PROJECT_FILE;
    public static File WEB_PROJECT_FILE;

    /** Need to be defined because of JUnit */
    public EJBValidation(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class, "openProjects");
        conf = addServerTests(Server.GLASSFISH, conf, AddMethodTest.class,
                "testAddBusinessMethod1InSB",
                "testAddBusinessMethod2InSB",
                "testAddBusinessMethod1InEB",
                "testAddBusinessMethod2InEB",
                "testAddCreateMethod1InEB",
                "testAddCreateMethod2InEB",
                "testAddHomeMethod1InEB",
                "testAddHomeMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, AddFinderMethodTest.class,
                "testAddFinderMethod1InEB",
                "testAddFinderMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, AddSelectMethodTest.class,
                "testAddSelectMethod1InEB",
                "testAddSelectMethod2InEB");
        conf = addServerTests(Server.GLASSFISH, conf, CallEJBTest.class,
                "testCallEJBInServlet",
                "testCallEJB1InSB");
                //"testCallEJB2InSB");  test needs to be fixed
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class, "prepareDatabase");
        conf = addServerTests(Server.GLASSFISH, conf, UseDatabaseTest.class, "testUseDatabase1InSB");
        conf = addServerTests(Server.GLASSFISH, conf, SendMessageTest.class, "testSendMessage1InSB");
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class,
                "testStartServer",
                "testDeployment",
                "testUndeploy",
                "testStopServer");
        conf = addServerTests(Server.GLASSFISH, conf, EJBValidation.class, "closeProjects");
        conf = conf.enableModules(".*").clusters(".*");
        return conf.suite();
    }

    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    public void tearDown() {
    }

    public void openProjects() {
        EAR_PROJECT_FILE = new File(getDataDir(), EAR_PROJECT_NAME);
        try {
            openProjects(EAR_PROJECT_FILE.getAbsolutePath());
            waitScanFinished();
            EJB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME + "-ejb");
            openProjects(EJB_PROJECT_FILE.getAbsolutePath());
            waitScanFinished();
            WEB_PROJECT_FILE = new File(EAR_PROJECT_FILE, EAR_PROJECT_NAME + "-war");
            openProjects(WEB_PROJECT_FILE.getAbsolutePath());
            waitScanFinished();
        } catch (IOException ex) {
            System.out.println("IOException " + ex.getMessage());
        }

        String files[] = {"TestingSession", "TestingEntity"};
        for (int i = 0; i < files.length; i++) {
            Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                    Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node") + "|" + files[i]);
            new OpenAction().performAPI(openFile);
            EditorWindowOperator.getEditor(files[i] + "Bean.java");
        }
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }

    public void closeProjects() {
        EditorOperator.closeDiscardAll();
        J2eeProjectSupport.closeProject(EAR_PROJECT_NAME);
        J2eeProjectSupport.closeProject(EJB_PROJECT_NAME);
        J2eeProjectSupport.closeProject(WEB_PROJECT_NAME);
    }

    public void prepareDatabase() {
        Utils.prepareDatabase();
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }

    public void testStartServer() throws IOException {
        Utils.startStopServer(true);
        String url = "http://localhost:8080/";
        String page = Utils.loadFromURL(url);
        log(page);
        String text = "Your server is now running";
        assertTrue("AppServer start page doesn't contain text '" + text + "'. See log for page content.", page.contains(text));
    }

    public void testDeployment() throws IOException {
        final String CONTROL_TEXT = "ControlTextABC";
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node") + "|TestingSession");
        new OpenAction().performAPI(openFile);
        EditorOperator editorOper = new EditorOperator("TestingSessionBean.java");
        editorOper.replace("return null;", "return \"" + CONTROL_TEXT + "\";");
        
        openFile = new Node(new SourcePackagesNode(EJBValidation.WEB_PROJECT_NAME), "test|TestingServlet");
        new OpenAction().performAPI(openFile);
        editorOper = new EditorOperator("TestingServlet.java");
        editorOper.replace("out.println(\"</body>\");", "out.println(lookupTestingSessionBeanLocal().testBusinessMethod1());");
        
        String page = Utils.deploy(EAR_PROJECT_NAME, "http://localhost:8080/TestingEntApp-WebModule/TestingServlet", true);
        log(page);
        assertTrue("TestingServlet doesn't contain expected text '" + CONTROL_TEXT + "'. See log for page content.", page.contains(CONTROL_TEXT));
    }

    public void testUndeploy() {
        Utils.undeploy(EAR_PROJECT_NAME);
    }

    public void testStopServer() {
        Utils.startStopServer(false);
    }
}
