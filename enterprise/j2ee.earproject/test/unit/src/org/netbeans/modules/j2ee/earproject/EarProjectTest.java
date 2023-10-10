/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.j2ee.earproject.test.EarTestCase;
import org.netbeans.modules.j2ee.earproject.test.TestUtil;
import org.netbeans.modules.j2ee.earproject.ui.wizards.NewEarProjectWizardIteratorTest;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Martin Krauskopf
 */
public class EarProjectTest extends EarTestCase {

    private String oldNbUser;

    public EarProjectTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.makeScratchDir(this);

        MockLookup.setLayersAndInstances();
    }

    // see testEarWithoutDDOpeningJ2EE()
    public void testEarWithoutDDOpeningJavaEE() throws Exception {
        File prjDirF = new File(getWorkDir(), "TestEarProject_15");
        EarProjectGenerator.createProject(prjDirF, "test-project",
                Profile.JAVA_EE_5, TestUtil.SERVER_URL, "1.5", null);
        File dirCopy = copyFolder(prjDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertFalse("has no deployment descriptor", ddF.isFile());
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
    }

    public void testEarWithoutDDOpeningJ2EE() throws Exception { // #75586
        File prjDirF = new File(getWorkDir(), "TestEarProject_14");
        EarProjectGenerator.createProject(prjDirF, "test-project",
                Profile.J2EE_14, TestUtil.SERVER_URL, "1.4", null);
        File dirCopy = copyFolder(prjDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        ddF.delete(); // one of #75586 scenario
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        // tests #75586
        EarProjectTest.openProject((EarProject) project);
    }

    public void testJavaEEProjectSettingsInEAR() throws Exception { // #75586
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_6_FULL;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertFalse("has no deployment descriptor", ddF.isFile());
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        Profile obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_6_FULL, obtainedProfile);
        JavaEEProjectSettings.setProfile(project, Profile.JAVA_EE_7_FULL);
        obtainedProfile = JavaEEProjectSettings.getProfile(project);
        assertEquals(Profile.JAVA_EE_7_FULL, obtainedProfile);
    }

    public void testThatMissingDDIsNotRegeneratedDuringOpeningJavaEE() throws Exception {
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertFalse("has no deployment descriptor", ddF.isFile());
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
        assertFalse("deployment descriptor was regenerated", ddF.isFile());

        ProjectEar projectEar = project.getLookup().lookup(ProjectEar.class);
        Application app = projectEar.getApplication();
        assertSame("two modules", 2, app.sizeModule());
    }

    public void testThatMissingDDIsRegeneratedCorrectlyDuringOpeningJ2EE() throws Exception { // #81154
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.J2EE_14;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        File ddF = new File(dirCopy, "src/conf/application.xml");
        assertTrue("has deployment descriptor", ddF.isFile());
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        validate(ddF);
        ddF.delete(); // one of #81154 scenario
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
        assertTrue("deployment descriptor was regenerated", ddF.isFile());
        validate(ddF);
        Application app = DDProvider.getDefault().getDDRoot(FileUtil.toFileObject(ddF));
        assertSame("two modules", 2, app.getModule().length);
    }

    public void testOpeningWihtoutPrivateMetadataAndSrcDirectory() throws Exception { // #83507
        File earDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.JAVA_EE_5;
        String ejbName = "testEA-ejb";
        String acName = "testEA-ac";
        NewEarProjectWizardIteratorTest.generateEARProject(earDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, ejbName, acName, null, null, null);
        File dirCopy = copyFolder(earDirF);
        TestUtil.deleteRec(new File(new File(dirCopy, "nbproject"), "private"));
        TestUtil.deleteRec(new File(dirCopy, "src"));
        TestUtil.deleteRec(new File(new File(new File(dirCopy, "testEA-ac"), "nbproject"), "private"));
        TestUtil.deleteRec(new File(new File(new File(dirCopy, "testEA-ejb"), "nbproject"), "private"));
        FileUtil.toFileObject(getWorkDir()).getFileSystem().refresh(false);
        FileObject fo = FileUtil.toFileObject(dirCopy);
        Project project = ProjectManager.getDefault().findProject(fo);
        assertNotNull("project is found", project);
        EarProjectTest.openProject((EarProject) project);
    }

    public void testEarProjectIsGCed() throws Exception { // #83128
        File prjDirF = new File(getWorkDir(), "testEA");
        String name = "Test EnterpriseApplication";
        Profile j2eeProfile = Profile.J2EE_14;

        // creates a project we will use for the import
        NewEarProjectWizardIteratorTest.generateEARProject(prjDirF, name, j2eeProfile,
                TestUtil.SERVER_URL, null, null, null, null, null, null);
        Project earProject = ProjectManager.getDefault().findProject(FileUtil.toFileObject(prjDirF));
        EarProjectTest.openProject((EarProject) earProject);
        Node rootNode = earProject.getLookup().lookup(LogicalViewProvider.class).createLogicalView();
        rootNode.getChildren().getNodes(true); // ping
        Reference<Project> wr = new WeakReference<Project>(earProject);
        OpenProjects.getDefault().close(new Project[] { earProject });
        EarProjectTest.closeProject((EarProject) earProject);
        rootNode = null;
        earProject = null;
        // TimedWeakReference timeout plus some buffer (projectapi.nb)
        Thread.sleep(15000 + 5000);
        assertGC("project cannot be garbage collected", wr);
    }

    /**
     * Accessor method for those who wish to simulate open of a project and in
     * case of suite for example generate the build.xml.
     */
    public static void openProject(final EarProject p) {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
    }

    public static void closeProject(final EarProject p) {
        ProjectOpenedHook hook = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
    }

    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.
     * Stolen from ant/freeform.
     */
    private File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = File.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }

    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (from.getName().equals("CVS")) {
                return;
            }
            FileUtil.createFolder(to);
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }

    public static void validate(final File ddFile) throws Exception {
        SAXParserFactory f = SAXParserFactory.newInstance();
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        URL schemaURL_1_4 = EarProjectTest.class.getResource("/org/netbeans/modules/j2ee/dd/impl/resources/application_1_4.xsd");
        URL schemaURL_5 = EarProjectTest.class.getResource("/org/netbeans/modules/j2ee/dd/impl/resources/application_5.xsd");
        assertNotNull("have access to schema", schemaURL_1_4);
        assertNotNull("have access to schema", schemaURL_5);
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", new String[] {
            schemaURL_1_4.toExternalForm(),
            schemaURL_5.toExternalForm()
        });
        try {
            p.parse(ddFile.toURI().toString(), new Handler());
        } catch (SAXParseException e) {
            fail("Validation of XML document " + ddFile + " against schema failed. Details: " +
                    e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage());
        }
    }

    public static void validate(FileObject ddFO) throws Exception {
        assertNotNull(ddFO);
        File ddF = FileUtil.toFile(ddFO);
        assertNotNull(ddF);
        validate(ddF);
    }

    private static final class Handler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }

}
