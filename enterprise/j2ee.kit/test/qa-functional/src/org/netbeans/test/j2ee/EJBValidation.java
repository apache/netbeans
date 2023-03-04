  /*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
