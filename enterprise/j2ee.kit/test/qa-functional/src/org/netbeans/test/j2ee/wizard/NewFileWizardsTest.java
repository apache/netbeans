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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.netbeans.test.j2ee.lib.ContentComparator;
import org.netbeans.test.j2ee.lib.FilteringLineDiff;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.netbeans.test.j2ee.lib.Reporter;
import org.netbeans.test.j2ee.lib.Ejb;
import org.netbeans.test.j2ee.lib.Utils;
import org.openide.filesystems.FileUtil;

/**
 * Test New File wizards in J2EE area. These tests are
 * part of J2EE Functional test suite.
 * In each test is checked if all classes are created
 * and if deployment descriptors are changed accordingly.
 *
 * @author jungi, Jiri Skrivanek
 * @see <a href="http://qa.netbeans.org/modules/j2ee/promo-f/testspec/j2ee-wizards-testspec.html">J2EE Wizards Test Specification</a>
 */
public class NewFileWizardsTest extends J2eeTestCase {

//    private static boolean CREATE_GOLDEN_FILES = Boolean.getBoolean("org.netbeans.test.j2ee.wizard.golden");
    private static boolean CREATE_GOLDEN_FILES = false;
    private static final String EJB_PROJECT_NAME = "NewFileWizardsTestEJB";
    private static final String WEB_PROJECT_NAME = "NewFileWizardsTestWeb";
    private static final String REMOTE_JAVA_PROJECT_NAME = "JavaProject";
    private Reporter reporter;
    private String version;
    private static String projectLocation = null;
    private static String projectsCreated = null;

    public NewFileWizardsTest(String testName, String version) {
        super(testName);
        this.version = version;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (projectLocation == null) {
            projectLocation = getWorkDir().getParentFile().getParentFile().getCanonicalPath();
        }
        if ("1.4".equals(version)) {
            File projectDir = new File(getDataDir(), "projects");
            projectLocation = projectDir.getCanonicalPath();
            String projectPathEJB = new File(projectDir, EJB_PROJECT_NAME + version).getAbsolutePath();
            String projectPathWeb = new File(projectDir, WEB_PROJECT_NAME + version).getAbsolutePath();
            openProjects(projectPathEJB, projectPathWeb);
        } else if (!version.equals(projectsCreated)) {
            projectsCreated = version;
            WizardUtils.createEJBProject(projectLocation, EJB_PROJECT_NAME + version, version);
            WizardUtils.createWebProject(projectLocation, WEB_PROJECT_NAME + version, version);
        }
        reporter = Reporter.getReporter((NbTestCase) this);
        System.out.println("########  " + getName() + "  #######");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        reporter.close();
    }

    private String getMultiEjbPath() {
        return new File(getDataDir(), "projects/MultiSrcRootEjb").getAbsolutePath();
    }

    private String getMultiWebPath() {
        return new File(getDataDir(), "projects/MultiSrcRootWar").getAbsolutePath();
    }

    /**
     * Create new stateless session bean with local interface.
     */
    public void testLocalSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "LocalSession",
                "ejbs.local", true, false, true, null);
    }

    /**
     * Create new stateless session bean with remote interface.
     */
    public void testRemoteSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "RemoteSession",
                "ejbs.remote", false, true, true, null);
    }

    /**
     * Create new stateless session bean with local and remote interfaces.
     */
    public void testLocalRemoteSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "LRS",
                "ejbs", true, true, true, null);
    }

    /**
     * Create new stateful session bean with local interface.
     */
    public void testLocalStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "LSS",
                "stateful.ejbs.local", true, false, false, null);
    }

    /**
     * Create new stateful session bean with remote interface.
     */
    public void testRemoteStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "RSS",
                "stateful.ejbs.remote", false, true, false, null);
    }

    /**
     * Create new stateful session bean with local and remote interfaces.
     */
    public void testLocalRemoteStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", EJB_PROJECT_NAME + version, "LRSS",
                "stateful.ejbs", true, true, false, null);
    }

    /**
     * Create new CMP entity bean with local interface
     * and <code>String</code> as primary key class.
     */
    public void testLocalEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "LocalEntity",
                "ejbs.entity.local", true, false, true, null);
    }

    /**
     * Create new CMP entity bean with remote interface
     * and <code>String</code> as primary key class.
     */
    public void testRemoteEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "RemoteEntity",
                "ejbs.entity.remote", false, true, true, null);
    }

    /**
     * Create new CMP entity bean with local and remote interfaces
     * and <code>String</code> as primary key class.
     */
    public void testLocalRemoteEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "LRE",
                "ejbs.entity", true, true, true, null);
    }

    /**
     * Create new BMP entity bean with local interface
     * and <code>String</code> as primary key class.
     */
    public void testLocalBeanEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "LocalBeanEntity",
                "ejbs.entity.bean.local", true, false, false, null);
    }

    /**
     * Create new BMP entity bean with remote interface
     * and <code>String</code> as primary key class.
     */
    public void testRemoteBeanEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "RemoteBeanEntity",
                "ejbs.entity.bean.remote", false, true, false, null);
    }

    /**
     * Create new BMP entity bean with local and remote interfaces
     * and <code>String</code> as primary key class.
     */
    public void testLocalRemoteBeanEntityBean() throws Exception {
        ejbTest("Entity Bean", EJB_PROJECT_NAME + version, "LRBE",
                "ejbs.entity.bean", true, true, false, null);
    }

    /**
     * Create new queue message-driven bean.
     */
    public void testQueueMdbBean() throws Exception {
        ejbTest("Message-Driven Bean", EJB_PROJECT_NAME + version, "QueueMdb",
                "ejbs.mdb", false, false, true, null);
    }

    /**
     * Create new topic message-driven bean.
     */
    public void testTopicMdbBean() throws Exception {
        ejbTest("Message-Driven Bean", EJB_PROJECT_NAME + version, "TopicMdb",
                "ejbs.mdb", false, false, false, null);
    }

    /**
     * Create new persistence unit in Ejb module.
     */
    public void testPersistenceUnitInEjb() throws Exception {
        puTest(EJB_PROJECT_NAME + version, "ejbPu");
    }

    /**
     * Create new persistence unit in Web module.
     */
    public void testPersistenceUnitInWeb() throws Exception {
        puTest(WEB_PROJECT_NAME + version, "webPu");
    }

    /**
     * Create new entity class in Ejb module.
     */
    public void testEntityClassInEjb() throws Exception {
        entityClassTest(EJB_PROJECT_NAME + version, "EjbEntity",
                "ejb.entity", null);
    }

    /**
     * Create new entity class in Web module.
     */
    public void testEntityClassInWeb() throws Exception {
        entityClassTest(WEB_PROJECT_NAME + version, "WebEntity",
                "web.entity", null);
    }

    /**
     * Create new service locator from template in EJB module.
     */
    public void testServiceLocatorInEjb() throws Exception {
        serviceLocatorTest(EJB_PROJECT_NAME + version, "ServiceLocator",
                "locator", false, null);
    }

    /**
     * Create new caching service locator from template in EJB module.
     */
    public void testCachingServiceLocatorInEjb() throws Exception {
        serviceLocatorTest(EJB_PROJECT_NAME + version, "CachingServiceLocator",
                "locator.cache", true, null);
    }

    /**
     * Create new service locator from template in Web application.
     */
    public void testServiceLocatorInWeb() throws Exception {
        serviceLocatorTest(WEB_PROJECT_NAME + version, "ServiceLocator",
                "locator", false, null);
    }

    /**
     * Create new service locator from template in Web application.
     */
    public void testCachingServiceLocatorInWeb() throws Exception {
        serviceLocatorTest(WEB_PROJECT_NAME + version, "CachingServiceLocator",
                "locator.cache", true, null);
    }

    /**
     * Build EJB Module with created beans, web service
     * and other objects.
     */
    public void testBuildDefaultNewEJBMod() {
        tearDownProject(EJB_PROJECT_NAME + version);
    }

    /**
     * Build Web application with  web service
     * and other objects.
     */
    public void testBuildDefaultNewWebMod() {
        tearDownProject(WEB_PROJECT_NAME + version);
    }

    /**
     * Go through New Servlet Wizard
     */
    private void servletTest(String prjRoot, String servletName, String servletPkg)
            throws Exception {
        Project p = J2eeProjectSupport.getProject(new File(prjRoot), ".");
        NewFileWizardOperator nfwo = WizardUtils.createNewFile(p,
                "Web", "Servlet");
        NewJavaFileNameLocationStepOperator nop = WizardUtils.setFileNameLocation(
                servletName, servletPkg, null);
        nop.finish();
        nop.waitClosed();
    }

    /**
     * Go through New Session/Entity/Message-Driven Bean wizard.
     *
     *@param stateless stateless/stateful option in case of session bean,
     *       container/bean in case of entity bean,
     *       queue/topic in case of mdb bean
     */
    private void ejbTest(String type, String prjRoot, String ejbName, String ejbPkg,
            boolean local, boolean remote, boolean stateless, String srcRoot) throws Exception {
        boolean hasMoreSrcRoots = (srcRoot != null);
        Project p = (hasMoreSrcRoots)
                ? J2eeProjectSupport.getProject(new File(prjRoot), ".")
                : J2eeProjectSupport.getProject(new File(projectLocation), prjRoot);
        File remoteJavaProjectDir = new File(FileUtil.toFile(p.getProjectDirectory().getParent()), REMOTE_JAVA_PROJECT_NAME + version);
        if (!remoteJavaProjectDir.exists()) {
            // create java project needed for remote beans
            J2SEProjectGenerator.createProject(remoteJavaProjectDir, REMOTE_JAVA_PROJECT_NAME + version, null, null, null, true);
        }
        J2eeProjectSupport.openProject(remoteJavaProjectDir);
        String category = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.resources.Bundle", "Templates/J2EE");
        NewFileWizardOperator nfwo = WizardUtils.createNewFile(p, category, type);
        NewJavaFileNameLocationStepOperator nop = WizardUtils.setFileNameLocation(
                ejbName, ejbPkg, srcRoot);
        if (type.equals("Message-Driven Bean")) {
            new JButtonOperator(nop, "Add").pushNoBlock();
            NbDialogOperator addMessageDestinationOper = new NbDialogOperator("Add Message Destination");
            new JTextFieldOperator(addMessageDestinationOper).setText(ejbName);
            if (!stateless) {
                new JRadioButtonOperator(addMessageDestinationOper, "Topic").push();
            } else {
                new JRadioButtonOperator(addMessageDestinationOper, "Queue").push();
            }
            addMessageDestinationOper.ok();
            // need to wait until wizard is refreshed after the add dialog is closed
            new EventTool().waitNoEvent(1000);
            nop.next();
        } else {
            if (!stateless) {
                if (type.equals("Session Bean")) {
                    new JRadioButtonOperator(nop, "Stateful").setSelected(true);
                } else {
                    new JRadioButtonOperator(nop, "Bean").setSelected(true);
                }
            }
            new JCheckBoxOperator(nop, "Local").changeSelection(local);
            new JCheckBoxOperator(nop, "Remote").changeSelection(remote);
        }
        nop.finish();
        nop.waitClosed();
        Ejb ejb = (hasMoreSrcRoots)
                ? new Ejb(ejbPkg + "." + ejbName, p, remoteJavaProjectDir, local, remote, srcRoot)
                : new Ejb(ejbPkg + "." + ejbName, p, remoteJavaProjectDir, local, remote);
        if ("1.4".equals(version)) {
            String[] err = ejb.checkExistingFiles();
            assertTrue(Arrays.asList(err).toString(), err.length == 0);
        }
        List<File> files = new ArrayList<File>();
        File prjDir = (hasMoreSrcRoots)
                ? new File(prjRoot).getCanonicalFile()
                : new File(new File(projectLocation), prjRoot).getCanonicalFile();
        if ("1.4".equals(version)) {
            files.add(new File(prjDir, "src/conf/ejb-jar.xml"));
            files.add(new File(prjDir, "src/conf/glassfish-ejb-jar.xml"));
        }
        if (!hasMoreSrcRoots) {
            files.addAll(Arrays.asList(
                    new File(prjDir, "src/java/" + ejbPkg.replace('.', '/') + "/").listFiles(new Filter(ejbName))));
        } else {
            files.addAll(Arrays.asList(
                    new File(prjDir, srcRoot + "/" + ejbPkg.replace('.', '/') + "/").listFiles(new Filter(ejbName))));
        }
        // check files in remote java project
        File[] fileList = new File(remoteJavaProjectDir, "src/" + ejbPkg.replace('.', '/') + "/").listFiles(new Filter(ejbName));
        if (fileList != null) {
            files.addAll(Arrays.asList(fileList));
        }
        checkFiles(files);
    }

    /**
     * Go through New (Caching) Service Locator wizard.
     */
    private void serviceLocatorTest(String prjRoot, String name, String pkg,
            boolean caching, String srcRoot) throws Exception {
        boolean hasMoreSrcRoots = (srcRoot != null);
        Project p = (hasMoreSrcRoots)
                ? J2eeProjectSupport.getProject(new File(prjRoot), ".")
                : J2eeProjectSupport.getProject(new File(projectLocation), prjRoot);
        String type = (caching) ? "Caching Service Locator" : "Service Locator";
        String category = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.resources.Bundle", "Templates/J2EE");
        if (prjRoot.startsWith(WEB_PROJECT_NAME) || prjRoot.startsWith(getMultiWebPath())) {
            category = "Web";
        }
        NewFileWizardOperator nfwo = WizardUtils.createNewFile(p, category, type);
        NewJavaFileNameLocationStepOperator nop = WizardUtils.setFileNameLocation(
                name, pkg, srcRoot);
        nop.finish();
        nop.waitClosed();
        List<File> files = new ArrayList<File>(1);
        File prjDir = (hasMoreSrcRoots)
                ? new File(prjRoot).getCanonicalFile()
                : new File(new File(projectLocation), prjRoot).getCanonicalFile();
        if (!hasMoreSrcRoots) {
            files.add(new File(prjDir, "src/java/" + pkg.replace('.', '/') + "/" + name + ".java"));
        } else {
            files.add(new File(prjDir, srcRoot + "/" + pkg.replace('.', '/') + "/" + name + ".java"));
        }
        checkFiles(files);
    }

    private void puTest(String prjRoot, String name) throws Exception {
        Project p = J2eeProjectSupport.getProject(new File(projectLocation), prjRoot);
        NewFileWizardOperator nfwo = WizardUtils.createNewFile(p,
                "Persistence", "Persistence Unit");
        JTextFieldOperator jtfo = new JTextFieldOperator(nfwo, 0);
        jtfo.clearText();
        jtfo.typeText(name);
        JComboBoxOperator jcbo = new JComboBoxOperator(nfwo, 1);
        jcbo.selectItem("jdbc/sample");
        nfwo.finish();
        nfwo.waitClosed();
        List<File> files = new ArrayList<File>();
        File prjDir = new File(new File(projectLocation), prjRoot).getCanonicalFile();
        files.add(new File(prjDir, "src/conf/persistence.xml"));
        checkFiles(files);
    }

    private void entityClassTest(String prjRoot, String name, String pkg, String srcRoot)
            throws Exception {
        boolean hasMoreSrcRoots = (srcRoot != null);
        Project p = (hasMoreSrcRoots)
                ? J2eeProjectSupport.getProject(new File(prjRoot), ".")
                : J2eeProjectSupport.getProject(new File(projectLocation), prjRoot);
        NewFileWizardOperator nfwo = WizardUtils.createNewFile(p,
                "Persistence", "Entity Class");
        NewJavaFileNameLocationStepOperator nop = WizardUtils.setFileNameLocation(
                name, pkg, srcRoot);
        nop.finish();
        nop.waitClosed();
        List<File> files = new ArrayList<File>();
        File prjDir = (hasMoreSrcRoots)
                ? new File(prjRoot).getCanonicalFile()
                : new File(new File(projectLocation), prjRoot).getCanonicalFile();
        if (!hasMoreSrcRoots) {
            files.addAll(Arrays.asList(
                    new File(prjDir, "src/java/" + pkg.replace('.', '/') + "/").listFiles(new Filter(name))));
        } else {
            files.addAll(Arrays.asList(
                    new File(prjDir, srcRoot + "/" + pkg.replace('.', '/') + "/").listFiles(new Filter(name))));
        }
        checkFiles(files);
    }

    /**
     * Check files against golden files.
     *
     *@param newFiles files to check
     */
    private void checkFiles(List<File> newFiles) {
        new EventTool().waitNoEvent(1000);
        // save all instead of timeout
        new SaveAllAction().performAPI();
        new EventTool().waitNoEvent(1000);
        if (!CREATE_GOLDEN_FILES) {
            List<String> l = new ArrayList<String>(newFiles.size() / 2);
            for (Iterator<File> i = newFiles.iterator(); i.hasNext();) {
                File newFile = i.next();
                File goldenFile;
                try {
                    Logger lo = Logger.getLogger(NewFileWizardsTest.class.getName());
                    goldenFile = getGoldenFile(getName() + "_" + version + "/" + newFile.getName() + ".pass");
                    lo.log(Level.FINE, "comparing: {0}", goldenFile.getAbsolutePath());
                    lo.log(Level.FINE, "with: {0}", newFile.getAbsolutePath());
                    if (newFile.getName().endsWith(".xml") && !newFile.getName().startsWith("glassfish-") && !newFile.getName().startsWith("webservices.xml")) {
                        assertTrue(ContentComparator.equalsXML(goldenFile, newFile));
                    } else {
                        assertFile(newFile, goldenFile,
                                new File(getWorkDirPath(), newFile.getName() + ".diff"),
                                new FilteringLineDiff());
                    }
                } catch (Throwable t) {
                    goldenFile = getGoldenFile(getName() + "_" + version + "/" + newFile.getName() + ".pass");
                    Utils.copyFile(newFile, new File(getWorkDirPath(), newFile.getName() + ".bad"));
                    Utils.copyFile(goldenFile,
                            new File(getWorkDirPath(), newFile.getName() + ".gf"));
                    l.add(newFile.getName());
                }
            }
            assertTrue("File(s) " + l.toString() + " differ(s) from golden files.", l.isEmpty());
        } else {
            createGoldenFiles(newFiles);
        }
    }

    private void createGoldenFiles(List<File> from) {
        File f = getDataDir();
        List<String> names = new ArrayList<String>();
        names.add("goldenfiles");
        while (!f.getName().equals("test")) {
            if (!f.getName().equals("sys") && !f.getName().equals("work") && !f.getName().equals("tests")) {
                names.add(f.getName());
            }
            f = f.getParentFile();
        }
        for (int i = names.size() - 1; i > -1; i--) {
            f = new File(f, names.get(i));
        }
        f = new File(f, getClass().getName().replace('.', File.separatorChar));
        File destDir = new File(f, getName() + "_" + version);
        destDir.mkdirs();
        for (Iterator<File> i = from.iterator(); i.hasNext();) {
            File src = i.next();
            Utils.copyFile(src, new File(destDir, src.getName() + ".pass"));
        }
        assertTrue("Golden files generated.", false);
    }

    /**
     * Build project.
     * @paramprjName project to build
     */
    private void tearDownProject(String prjName) {
        ProjectsTabOperator.invoke().getProjectRootNode(prjName).collapse();
        Utils.buildProject(prjName);
    }

//----------------------------------------------------- multi src roots projects
    /**
     * Open EJB Module project with multiple source roots.
     */
    public void testOpenEjbMultiRootProject() {
        assertNotNull(J2eeProjectSupport.openProject(getMultiEjbPath()));
        boolean b = Utils.checkMissingServer("MultiSrcRootEjb");
        new ProjectsTabOperator().getProjectRootNode("MultiSrcRootEjb").expand();
    }

    /**
     * Open Web application project with multiple source roots.
     */
    public void testOpenWebMultiRootProject() {
        assertNotNull(J2eeProjectSupport.openProject(getMultiWebPath()));
        boolean b = Utils.checkMissingServer("MultiSrcRootWar");
        new ProjectsTabOperator().getProjectRootNode("MultiSrcRootWar").expand();
    }

    /**
     * Create new stateless session bean with local interface
     * in project with multiple source roots.
     */
    public void testMultiLocalSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "LocalSession",
                "multi.ejbs.local", true, false, true, "src" + File.separator + "beans");
    }

    /**
     * Create new stateless session bean with remote interface
     * in project with multiple source roots.
     */
    public void testMultiRemoteSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "RemoteSession",
                "multi.ejbs.remote", false, true, true, "src" + File.separator + "beans");
    }

    /**
     * Create new stateless session bean with local and remote interfaces
     * in project with multiple source roots.
     */
    public void testMultiLocalRemoteSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "MLRS",
                "multi.ejbs", true, true, true, "src" + File.separator + "beans");
    }

    /**
     * Create new stateful session bean with local interface
     * in project with multiple source roots.
     */
    public void testMultiLocalStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "MLSS",
                "multi.stateful.ejbs.local", true, false, false, "src" + File.separator + "beans");
    }

    /**
     * Create new stateful session bean with remote interface
     * in project with multiple source roots.
     */
    public void testMultiRemoteStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "MRSS",
                "multi.stateful.ejbs.remote", false, true, false, "src" + File.separator + "beans");
    }

    /**
     * Create new stateful session bean with local and remote interfaces
     * in project with multiple source roots.
     */
    public void testMultiLocalRemoteStatefulSessionBean() throws Exception {
        ejbTest("Session Bean", getMultiEjbPath(), "MLRSS",
                "multi.stateful.ejbs", true, true, false, "src" + File.separator + "beans");
    }

    /**
     * Create new CMP entity bean with local interface
     * and <code>String</code> as primary key class
     * in project with multiple source roots.
     */
    public void testMultiLocalEntityBean() throws Exception {
        ejbTest("Entity Bean", getMultiEjbPath(), "LocalEntity",
                "multi.ejbs.entity.local", true, false, true, "src" + File.separator + "beans");
    }

    /**
     * Create new CMP entity bean with remote interface
     * and <code>String</code> as primary key class
     * in project with multiple source roots.
     */
    public void testMultiRemoteEntityBean() throws Exception {
        ejbTest("Entity Bean", getMultiEjbPath(), "RemoteEntity",
                "multi.ejbs.entity.remote", false, true, true, "src" + File.separator + "beans");
    }

    /**
     * Create new CMP entity bean with local and remote interfaces
     * and <code>String</code> as primary key class
     * in project with multiple source roots.
     */
    public void testMultiLocalRemoteEntityBean() throws Exception {
        ejbTest("Entity Bean", getMultiEjbPath(), "MLRE",
                "multi.ejbs.entity", true, true, true, "src" + File.separator + "beans");
    }

    /**
     * Create new queue message-driven bean
     * in project with multiple source roots.
     */
    public void testMultiQueueMdbBean() throws Exception {
        ejbTest("Message-Driven Bean", getMultiEjbPath(), "QueueMdb",
                "multi.ejbs.mdb", false, false, true, "src" + File.separator + "beans");
    }

    /**
     * Create new topic message-driven bean
     * in project with multiple source roots.
     */
    public void testMultiTopicMdbBean() throws Exception {
        ejbTest("Message-Driven Bean", getMultiEjbPath(), "TopicMdb",
                "multi.ejbs.mdb", false, false, false, "src" + File.separator + "beans");
    }

    /**
     * Create new service locator from template in EJB module
     * with multiple source roots.
     */
    public void testMultiServiceLocatorInEjb() throws Exception {
        serviceLocatorTest(getMultiEjbPath(), "ServiceLocator",
                "multi.locator", false, "src" + File.separator + "beans");
    }

    /**
     * Create new caching service locator from template in EJB module
     * with multiple source roots.
     */
    public void testMultiCachingServiceLocatorInEjb() throws Exception {
        serviceLocatorTest(getMultiEjbPath(), "CachingServiceLocator",
                "multi.locator.cache", true, "src" + File.separator + "beans");
    }

    /**
     * Create new service locator from template in Web application
     * with multiple source roots.
     */
    public void testMultiServiceLocatorInWeb() throws Exception {
        serviceLocatorTest(getMultiWebPath(), "ServiceLocator",
                "multi.locator", false, "src" + File.separator + "webservices");
    }

    /**
     * Create new caching service locator from template in Web application
     * with multiple source roots.
     */
    public void testMultiCachingServiceLocatorInWeb() throws Exception {
        serviceLocatorTest(getMultiWebPath(), "CachingServiceLocator",
                "multi.locator.cache", true, "src" + File.separator + "webservices");
    }

    /**
     * Create new servlet from template in Web application.
     */
    public void testMultiServletInWeb() throws Exception {
        servletTest(getMultiWebPath(), "ServletForEJB", "servlet");
    }

    /**
     * Build EJB Module with created beans, web service
     * and other objects in other then default source root..
     */
    public void testBuildEjbMultiRootProject() {
        tearDownProject("MultiSrcRootEjb");
    }

    /**
     * Build Web application with created web service
     * and other objects in other then default source root..
     */
    public void testBuildWebMultiRootProject() {
        tearDownProject("MultiSrcRootWar");
    }

    private class Filter implements FilenameFilter {

        private String start;

        public Filter(String name) {
            start = name;
        }

        @Override
        public boolean accept(File dir, String name) {
            // include files with specified prefix and exclude local history temporary files
            return name.startsWith(start) && !name.endsWith(".nblh~");
        }
    }
}
