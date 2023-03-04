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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileUtil;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.lib.Reporter;
import org.netbeans.test.j2ee.lib.RequiredFiles;

/**
 * Test creation of new projects via API
 * These tests are part of J2EE Functional test suite.
 *
 * @author jungi
 * @see <a href="http://qa.netbeans.org/modules/j2ee/promo-f/testspec/j2ee-wizards-testspec.html">J2EE Wizards Test Specification</a>
 */
public class J2eeProjectsTest extends J2eeTestCase {

    private Reporter reporter;
    private static final File projectsHome = new File(System.getProperty("nbjunit.workdir"));

    /**
     * Creates a new instance of J2eeProjectsTest
     */
    public J2eeProjectsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, J2eeProjectsTest.class,
                "testCreateEjbProject",
                "testCreateWebProject",
                "testCreateEmptyJ2eeProject",
                "testAddModulesToJ2eeProject");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        reporter = Reporter.getReporter((NbTestCase) this);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (reporter != null) {
            reporter.close();
        }
    }

    /**
     * Creates EJB Module project and checks if all files are created
     * succesfully (including server specific deployment descriptor)
     */
    public void testCreateEjbProject() {
        assertNotNull(projectsHome);
        assertTrue(projectsHome.exists() && projectsHome.isDirectory());
        RequiredFiles rf = null;
        try {
            rf = new RequiredFiles(new File(getDataDir(), "structures/ejbProject6.str"));
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
        }
        assertNotNull(rf);
        Project p = (Project) J2eeProjectSupport.createProject(projectsHome, "EJBModule", J2eeProjectSupport.EJB_PROJECT, null);
        assertNotNull(p);
        checkProjectStructure(p, rf);
    }

    /**
     * Creates Web Application project and checks if all files are created
     * succesfully (including server specific deployment descriptor)
     */
    public void testCreateWebProject() {
        assertNotNull(projectsHome);
        assertTrue(projectsHome.exists() && projectsHome.isDirectory());
        RequiredFiles rf = null;
        try {
            rf = new RequiredFiles(new File(getDataDir(), "structures/webProject6.str"));
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
        }
        assertNotNull(rf);
        Project p = (Project) J2eeProjectSupport.createProject(projectsHome, "WebModule", J2eeProjectSupport.WEB_PROJECT, null);
        assertNotNull(p);
        checkProjectStructure(p, rf);
    }

    /**
     * Creates empty Enterprise Application project and checks if all files
     * are created succesfully (including server specific deployment descriptor)
     */
    public void testCreateEmptyJ2eeProject() {
        assertNotNull(projectsHome);
        assertTrue(projectsHome.exists() && projectsHome.isDirectory());
        RequiredFiles rf = null;
        try {
            rf = new RequiredFiles(new File(getDataDir(), "structures/emptyJ2eeProject.str"));
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
        }
        assertNotNull(rf);
        Project p = (Project) J2eeProjectSupport.createProject(projectsHome, "J2eePrj", J2eeProjectSupport.J2EE_PROJECT, null);
        assertNotNull(p);
        checkProjectStructure(p, rf);
    }

    /**
     * Add war and jar to ear.
     */
    public void testAddModulesToJ2eeProject() {
        EarProject earPrj = null;
        Project warPrj = null;
        Project ejbPrj = null;
        ProjectManager pm = ProjectManager.getDefault();
        try {
            earPrj = (EarProject) pm.findProject(FileUtil.toFileObject(new File(projectsHome, "J2eePrj").getCanonicalFile()));
            warPrj = pm.findProject(FileUtil.toFileObject(new File(projectsHome, "WebModule").getCanonicalFile()));
            ejbPrj = pm.findProject(FileUtil.toFileObject(new File(projectsHome, "EJBModule").getCanonicalFile()));
            AntProjectHelper h = earPrj.getAntProjectHelper();
            AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
            ReferenceHelper refHelper = new ReferenceHelper(h, aux, h.getStandardPropertyEvaluator());
            EarProjectProperties.addJ2eeSubprojects(earPrj, new Project[]{warPrj, ejbPrj});
        } catch (IOException ioe) {
            ioe.printStackTrace(reporter.getLogStream());
            fail("IOEx while adding modules to EAR project.");
        }
    }

    protected void checkProjectStructure(Project p, RequiredFiles r) {
        Set<String> l = J2eeProjectSupport.getFileSet(p);
        Set<String> rf = r.getRequiredFiles();
        reporter.ref("Project: " + p.toString());
        reporter.ref("Expected: " + rf);
        reporter.ref("Real: " + l);
        assertTrue("Files: " + getDifference(l, rf) + " are missing in project: " + p.toString(), l.containsAll(rf));
        rf = r.getRequiredFiles();
        reporter.ref("Project: " + p.toString());
        reporter.ref("Expected: " + rf);
        reporter.ref("Real: " + l);
        Set s = getDifference(rf, l);
        assertTrue("Files: " + s + " are new in project: " + p.toString(), s.isEmpty());
    }

    private Set getDifference(Set s1, Set s2) {
        Set result = new HashSet();
        s2.removeAll(s1);
        for (Iterator i = s2.iterator(); i.hasNext();) {
            String s = (String) i.next();
            if (s.indexOf(".LCK") < 0) {
                result.add(s);
            } else {
                reporter.log("Additional file: " + s);
            }
        }
        return result;
    }
}
