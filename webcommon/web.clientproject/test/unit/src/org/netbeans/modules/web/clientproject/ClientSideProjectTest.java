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
package org.netbeans.modules.web.clientproject;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.spi.EnhancedBrowserFactory;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.sites.SiteZip;
import org.netbeans.modules.web.clientproject.sites.SiteZipPanel;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.test.MockLookup;

public class ClientSideProjectTest extends NbTestCase {

    private static int projectCounter = 1;


    public ClientSideProjectTest() {
        super("ClientSideProjectTest");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockLookup.setLayersAndInstances("smth");
        // Prepare template file for testProjectCreationFromZipTemplate
        File zipFile = new File(getDataDir(), "TestTemplate.zip");
        if (!zipFile.exists()) {
            try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fos)) {
                Path basePath = new File(getDataDir(), "sample-project").toPath();
                Files.walk(basePath).forEachOrdered(p -> {
                    try {
                        if (Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)) {
                            ZipEntry ze = new ZipEntry(basePath.relativize(p).toString());
                            zipOutputStream.putNextEntry(ze);
                            Files.copy(p, zipOutputStream);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }
        }
    }

    public void testProjectWithSiteRootCreation() throws Exception {
        ClientSideProject project = createProject(null, "public_html_XX", "test", "selenium");
        assertNoProjectProblems(project);
        assertTrue(project.isHtml5Project());
        assertFalse(project.isJsLibrary());
    }

    public void testProjectWithSourcesCreation() throws Exception {
        ClientSideProject project = createProject("src_XX", null, "test", "selenium");
        assertNoProjectProblems(project);
        assertTrue(project.isJsLibrary());
        assertFalse(project.isHtml5Project());
    }

    public void testProjectWithSourcesAndSiteRootCreation() throws Exception {
        ClientSideProject project = createProject("src_XX", "public_html_XX", "test", "selenium");
        assertNoProjectProblems(project);
        assertTrue(project.isHtml5Project());
        assertFalse(project.isJsLibrary());
    }

    public void testProjectCreationWithProblems() throws Exception {
        ClientSideProject project = createProject(null, null, null, null);
        // Site root must be existing folder in order to set it in the project properties, create a temp folder
        FileObject tmpSiteRoot = project.getProjectDirectory().createFolder(ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER);
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        projectProperties.setSourceFolder(ClientSideProjectConstants.DEFAULT_SOURCE_FOLDER);
        projectProperties.setSiteRootFolder(ClientSideProjectConstants.DEFAULT_SITE_ROOT_FOLDER);
        projectProperties.setTestFolder(ClientSideProjectConstants.DEFAULT_TEST_FOLDER);
        projectProperties.save();
        // Delete temp site root folder to trigger the project problem
        tmpSiteRoot.delete();
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull("project does have ProjectProblemsProvider", problemsProvider);
        assertEquals("project does not have any problems", 3, problemsProvider.getProblems().size());
    }

    public void testProjectCreationFromZipTemplate() throws Exception {
        ClientSideProject project = createProject(null, null, null, null);
        SiteZip siteZip = new SiteZip();
        FileObject testTemplate = FileUtil.toFileObject(getDataDir()).getFileObject("TestTemplate.zip");
        ((SiteZipPanel) (siteZip.getCustomizer().getComponent())).setTemplate(FileUtil.getFileDisplayName(testTemplate));
        SiteTemplateImplementation.ProjectProperties templateProperties = new SiteTemplateImplementation.ProjectProperties();
        siteZip.configure(templateProperties);
        ClientSideProjectUtilities.initializeProject(project,
                templateProperties.getSourceFolder(),
                templateProperties.getSiteRootFolder(),
                templateProperties.getTestFolder(),
                templateProperties.getTestSeleniumFolder());
        siteZip.apply(project.getProjectDirectory(), templateProperties, ProgressHandleFactory.createHandle("somename"));
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        assertEquals("site root should be created from template",
                project.getProjectDirectory().getFileObject("custom_siteroot"), FileUtil.toFileObject(projectProperties.getResolvedSiteRootFolder()));
        assertNoProjectProblems(project);
    }

    public void testProjectWithProjectServiceProvider() throws Exception {
        int instances = MySupport.INSTANCES.get();
        ClientSideProject project = createProject("src", "www", "test", "selenium");
        MySupport.query = project;
        MySupport mySupport = project.getLookup().lookup(MySupport.class);
        assertNotNull(mySupport);
        
        mySupport.task.waitFinished();
        assertSame("Instance created in parallel is the same", mySupport, mySupport.parallel);
        
        assertEquals(instances + 1, MySupport.INSTANCES.get());
        assertSame(project, mySupport.getProject());
    }

    private ClientSideProject createProject(@NullAllowed String sources, @NullAllowed String siteRoot, @NullAllowed String tests,
            @NullAllowed String testSelenium) throws Exception {
        FileObject projectDir = FileUtil.toFileObject(getWorkDir()).createFolder("Project" + projectCounter++);
        CommonProjectHelper projectHelper = ClientSideProjectUtilities.setupProject(projectDir, projectDir.getName());
        ClientSideProject project = (ClientSideProject) FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
        if (sources != null
                || siteRoot != null
                || tests != null) {
            ClientSideProjectUtilities.initializeProject(project, sources, siteRoot, tests, testSelenium);
            ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
            if (sources != null) {
                assertEquals("Source folder should exist", projectDir.getFileObject(sources), FileUtil.toFileObject(projectProperties.getResolvedSourceFolder()));
            }
            if (siteRoot != null) {
                assertEquals("Site Root should exist", projectDir.getFileObject(siteRoot), FileUtil.toFileObject(projectProperties.getResolvedSiteRootFolder()));
            }
            if (tests != null) {
                assertEquals("Test folder should exist", projectDir.getFileObject(tests), FileUtil.toFileObject(projectProperties.getResolvedTestFolder()));
            }
        }
        return project;
    }

    private void assertNoProjectProblems(ClientSideProject project) {
        ProjectProblemsProvider problemsProvider = project.getLookup().lookup(ProjectProblemsProvider.class);
        assertNotNull("Project should have ProjectProblemsProvider", problemsProvider);
        assertEquals("Project should not have any problems", 0, problemsProvider.getProblems().size());
    }

    //~ Inner classes

    @ServiceProvider(service = HtmlBrowser.Factory.class, path = "Services/Browsers2")
    public static class DummyBrowser implements HtmlBrowser.Factory, EnhancedBrowserFactory {

        @Override
        public HtmlBrowser.Impl createHtmlBrowserImpl() {
            return new HtmlBrowser.Impl() {

                @Override
                public Component getComponent() {
                    return null;
                }

                @Override
                public void reloadDocument() {
                }

                @Override
                public void stopLoading() {
                }

                @Override
                public void setURL(URL url) {
                }

                @Override
                public URL getURL() {
                    return null;
                }

                @Override
                public String getStatusMessage() {
                    return null;
                }

                @Override
                public String getTitle() {
                    return null;
                }

                @Override
                public boolean isForward() {
                    return false;
                }

                @Override
                public void forward() {
                }

                @Override
                public boolean isBackward() {
                    return false;
                }

                @Override
                public void backward() {
                }

                @Override
                public boolean isHistory() {
                    return false;
                }

                @Override
                public void showHistory() {
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {
                }
            };
        }

        @Override
        public BrowserFamilyId getBrowserFamilyId() {
            return BrowserFamilyId.ANDROID;
        }

        @Override
        public Image getIconImage(boolean small) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "some";
        }

        @Override
        public String getId() {
            return "some";
        }

        @Override
        public boolean hasNetBeansIntegration() {
            return false;
        }

        @Override
        public boolean canCreateHtmlBrowserImpl() {
            return true;
        }

    }

    @ProjectServiceProvider(service = MySupport.class, projectType = "org-netbeans-modules-web-clientproject")
    public static final class MySupport implements Runnable {

        public static final AtomicInteger INSTANCES = new AtomicInteger();
        public static Project query;

        private final Project project;
        MySupport parallel;
        RequestProcessor.Task task;


        public MySupport(Project project) throws Exception {
            INSTANCES.incrementAndGet();
            this.project = project;
            task = RequestProcessor.getDefault().post(this);
            task.waitFinished(1000);
        }

        public Project getProject() {
            return project;
        }

        @Override
        public void run() {
            parallel = query.getLookup().lookup(MySupport.class);
        }

    }

}
