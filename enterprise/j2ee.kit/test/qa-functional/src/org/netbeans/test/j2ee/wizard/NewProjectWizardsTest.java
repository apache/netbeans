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
package org.netbeans.test.j2ee.wizard;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.test.j2ee.lib.Reporter;
import org.netbeans.test.j2ee.lib.RequiredFiles;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;

/**
 * Test New Project wizards in J2EE area. These tests are
 * part of J2EE Functional test suite. Each test checks
 * if all files were created (deployment descriptors,
 * directories for sources etc.) and if project node
 * is expanded after finishing New Project wizard.
 *
 * @author jungi, Jiri Skrivanek
 * @see <a href="http://qa.netbeans.org/modules/j2ee/promo-f/testspec/j2ee-wizards-testspec.html">J2EE Wizards Test Specification</a>
 */
public class NewProjectWizardsTest extends J2eeTestCase {

    private static final String CATERGORY_JAVA_EE = "Java EE";
    private static final int EJB = 0;
    private static final int WEB = 1;
    private static final int EAR = 3;
    private static final int APP_CLIENT = 4;
    private static String projectLocation = null;
    private String projectName;
    private String version;
    private Reporter reporter;

    public static class NewProjectWizardsTest5 extends NewProjectWizardsTest {

        public NewProjectWizardsTest5(String testName) {
            super(testName, "5");
        }
    }

    public static class NewProjectWizardsTest6 extends NewProjectWizardsTest {

        public NewProjectWizardsTest6(String testName) {
            super(testName, "6");
        }
    }

    public static class NewProjectWizardsTest7 extends NewProjectWizardsTest {

        public NewProjectWizardsTest7(String testName) {
            super(testName, "7");
        }
    }

    public NewProjectWizardsTest(String testName, String version) {
        super(testName);
        this.version = version;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (projectLocation == null) {
            projectLocation = getWorkDir().getParentFile().getParentFile().getCanonicalPath();
        }
        reporter = Reporter.getReporter((NbTestCase) this);
        System.out.println("########  " + getName() + " Java EE " + version + " #######");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        reporter.close();
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, NewProjectWizardsTest5.class);
        conf = addServerTests(Server.GLASSFISH, conf, NewProjectWizardsTest6.class);
        conf = addServerTests(Server.GLASSFISH, conf, NewProjectWizardsTest7.class);
        return conf.suite();
    }

    /**
     * Create EJB Module which name contains spaces
     * and default project settings.
     */
    public void testEJBModWizard() throws Exception {
        OutputOperator.invoke();
        projectName = "def EJB Mod" + version;
        WizardUtils.createEJBProject(projectLocation, projectName, version);
        checkProjectStructure(EJB);
        checkProjectNodes();
    }
    
    /**
     * Create Enterprise Application Client project with default project
     * settings.
     */
    public void testAppClientWizard() throws Exception {
        projectName = "App client" + version;
        File targetDir = new File(projectLocation, projectName);
        WizardUtils.deleteAll(targetDir);
        NewProjectWizardOperator wiz = WizardUtils.createNewProject(CATERGORY_JAVA_EE, "Enterprise Application Client");
        NewJavaProjectNameLocationStepOperator op = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, version);
        wiz.finish();
        checkProjectStructure(APP_CLIENT);
        checkProjectNodes();
    }

    /**
     * Create Web Application which name contains spaces
     * and default project settings.
     */
    public void testWebModWizard() throws Exception {
        projectName = "def Web app" + version;
        WizardUtils.createWebProject(projectLocation, projectName, version);
        checkProjectStructure(WEB);
        checkProjectNodes();
    }

    /**
     * Create Enterprise Application project with default project
     * settings (ejb and web module are as well ).
     */
    public void testEnterpriseAppWizard() throws Exception {
        projectName = "def EAR app" + version;
        File targetDir = new File(projectLocation, projectName);
        WizardUtils.deleteAll(targetDir);
        NewProjectWizardOperator wiz = WizardUtils.createNewProject(CATERGORY_JAVA_EE, "Enterprise Application");
        NewJavaProjectNameLocationStepOperator op = WizardUtils.setProjectNameLocation(projectName,
                projectLocation);
        WizardUtils.setJ2eeSpecVersion(op, version);
        wiz.finish();
        checkProjectStructure(EAR);
        Node root = checkProjectNodes();
        Node modules = new Node(root, "Java EE Modules");
        modules.expand();
        String[] s = modules.getChildren();
        assertEquals("Expected: \"def_EAR_app" + version + "-ejb.jar\", was: \"" + s[1]
                + "\"", "def_EAR_app" + version + "-ejb.jar", s[1]);
        assertEquals("Expected: \"def_EAR_app" + version + "-war.war\", was: \"" + s[0]
                + "\"", "def_EAR_app" + version + "-war.war", s[0]);
    }

    private void checkProjectStructure(final int prjType) {
        final RequiredFiles r;
        switch (prjType) {
            case EJB:
                r = readRF("structures/ejbProject" + version + ".str");
                break;
            case WEB:
                r = readRF("structures/webProject" + version + ".str");
                break;
            case EAR:
                r = readRF("structures/defEAR" + version + ".str");
                break;
            case APP_CLIENT:
                r = readRF("structures/carProject" + version + ".str");
                break;
            default:
                throw new IllegalArgumentException();
        }
        final String projectPath = projectLocation + File.separatorChar + projectName;
        final AtomicReference<Set> missingRef = new AtomicReference<Set>();
        final AtomicReference<Set> extraRef = new AtomicReference<Set>();
        Waiter waiter = new Waiter(new Waitable() {
            @Override
            public Object actionProduced(Object obj) {
                Set<String> expected = r.getRequiredFiles();
                Set<String> actual = J2eeProjectSupport.getFileSet(projectPath);
                reporter.ref("Project: " + projectPath);
                reporter.ref("Expected: " + expected);
                reporter.ref("Real: " + actual);
                Set missing = getDifference(actual, expected);
                missingRef.set(missing);
                Set extra = getDifference(expected, actual);
                extraRef.set(extra);
                if (missing.isEmpty() && extra.isEmpty()) {
                    return Boolean.TRUE;
                } else {
                    new EventTool().waitNoEvent(2000);
                    return null;
                }
            }

            @Override
            public String getDescription() {
                return "wait for project files";
            }
        });
        try {
            waiter.waitAction(null);
        } catch (InterruptedException ex) {
            //do nothing
        } catch (TimeoutExpiredException tee) {
            assertTrue("Files: " + missingRef.get() + " are missing in project at: " + projectPath, missingRef.get().isEmpty());
            assertTrue("Files: " + extraRef.get() + " are new in project: " + projectPath, extraRef.get().isEmpty());
        }
    }

    public void closeProjects() {
        J2eeProjectSupport.closeAllProjects();
        new EventTool().waitNoEvent(2500);
    }

    private RequiredFiles readRF(String fileName) {
        RequiredFiles rf = null;
        try {
            rf = new RequiredFiles(new File(getDataDir(), fileName));
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
        }
        assertNotNull(rf);
        return rf;
    }

    private Set getDifference(Set<String> s1, Set<String> s2) {
        Set<String> result = new HashSet<String>(s2);
        result.removeAll(s1);
        for (String s : result) {
            if (s.indexOf(".LCK") < 0) {
                result.add(s);
            } else {
                reporter.log("Additional file: " + s);
            }
        }
        return result;
    }

    private Node checkProjectNodes() {
        Node node = new ProjectsTabOperator().getProjectRootNode(projectName);
        return node;
    }
}
