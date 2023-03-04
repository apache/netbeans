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
package org.netbeans.modules.web.clientproject.ui.customizer;

import org.netbeans.modules.web.clientproject.env.Licenses;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectConstants;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProviders;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Env;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public final class ClientSideProjectProperties {

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectProperties.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(ClientSideProjectProperties.class);

    final ClientSideProject project;

    private volatile AtomicReference<String> sourceFolder = null;
    private volatile AtomicReference<String> siteRootFolder = null;
    private volatile AtomicReference<String> testFolder = null;
    private volatile AtomicReference<String> testSeleniumFolder = null;
    private volatile String jsLibFolder = null;
    private volatile String encoding = null;
    private volatile Boolean runBrowser = null;
    private volatile AtomicReference<String> runAs = null;
    private volatile String startFile = null;
    private volatile String selectedBrowser = null;
    private volatile String webRoot = null;
    private volatile String projectUrl = null;
    private volatile ProjectServer projectServer = null;
    private volatile ClientProjectEnhancedBrowserImplementation enhancedBrowserSettings = null;
    private volatile Boolean autoconfigured = null;

    //customizer license headers
    private Licenses licenseSupport;

    public ClientSideProjectProperties(ClientSideProject project) {
        this.project = project;
    }

    public void save() {
        assert !EventQueue.isDispatchThread();
        try {
            getLicenseSupport().saveLicenseFile();
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    saveProperties();
                    saveEnhancedBrowserConfiguration();
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
            RP.post(new Runnable() {
                @Override
                public void run() {
                    firePropertyChanges();
                }
            }, 100);
        } catch (MutexException | IOException e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    void saveProperties() {
        // first, create possible foreign file references
        String sourceFolderReference = createForeignFileReference(sourceFolder, true);
        String siteRootFolderReference = createForeignFileReference(siteRootFolder, true);
        String testFolderReference = createForeignFileReference(testFolder, false);
        String testSeleniumFolderReference = createForeignFileReference(testSeleniumFolder, false);
        // save properties
        EditableProperties privateProperties = project.getProjectHelper().getProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties projectProperties = project.getProjectHelper().getProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH);

        if (sourceFolder != null) {
            if (sourceFolderReference != null) {
                putProperty(projectProperties, ClientSideProjectConstants.PROJECT_SOURCE_FOLDER, sourceFolderReference);
            } else {
                // source dir removed
                projectProperties.remove(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER);
            }
        }
        if (siteRootFolder != null) {
            if (siteRootFolderReference != null) {
                // do not overwrite site root if target folder does not exist (issue #248174)
                File siteRootF = project.getProjectHelper().resolveFile(siteRootFolder.get());
                if (siteRootF != null && siteRootF.exists()) {
                    putProperty(projectProperties, ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, siteRootFolderReference);
                }
            } else {
                // siteroot dir removed
                projectProperties.remove(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER);
            }
        }
        if (testFolder != null) {
            if (testFolderReference != null) {
                putProperty(projectProperties, ClientSideProjectConstants.PROJECT_TEST_FOLDER, testFolderReference);
            } else {
                // tests dir removed
                projectProperties.remove(ClientSideProjectConstants.PROJECT_TEST_FOLDER);
            }
        }
        if (testSeleniumFolder != null) {
            if (testSeleniumFolderReference != null) {
                putProperty(projectProperties, ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER, testSeleniumFolderReference);
            } else {
                // tests dir removed
                projectProperties.remove(ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER);
            }
        }
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_ENCODING, encoding);
        if (runAs != null) {
            String runAsValue = runAs.get();
            if (runAsValue != null) {
                putProperty(projectProperties, ClientSideProjectConstants.PROJECT_RUN_AS, runAsValue);
            } else {
                projectProperties.remove(ClientSideProjectConstants.PROJECT_RUN_AS);
            }
        }
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_RUN_BROWSER, runBrowser);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_START_FILE, startFile);
        // #227995: store PROJECT_SELECTED_BROWSER in private.properties:
        projectProperties.remove(ClientSideProjectConstants.PROJECT_SELECTED_BROWSER);
        putProperty(privateProperties, ClientSideProjectConstants.PROJECT_SELECTED_BROWSER, selectedBrowser);
        if (projectServer != null) {
            // #230903: store PROJECT_SERVER in private.properties:
            projectProperties.remove(ClientSideProjectConstants.PROJECT_SERVER);
            putProperty(privateProperties, ClientSideProjectConstants.PROJECT_SERVER, projectServer.name());
        }
        // #230903: store PROJECT_PROJECT_URL in private.properties:
        projectProperties.remove(ClientSideProjectConstants.PROJECT_PROJECT_URL);
        putProperty(privateProperties, ClientSideProjectConstants.PROJECT_PROJECT_URL, projectUrl);
        putProperty(projectProperties, ClientSideProjectConstants.PROJECT_WEB_ROOT, webRoot);
        getLicenseSupport().updateProperties(projectProperties);
        if (autoconfigured != null) {
            if (autoconfigured) {
                privateProperties.put(ClientSideProjectConstants.PROJECT_AUTOCONFIGURED, Boolean.TRUE.toString());
            } else {
                privateProperties.remove(ClientSideProjectConstants.PROJECT_AUTOCONFIGURED);
            }
        }
        project.getProjectHelper().putProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        project.getProjectHelper().putProperties(CommonProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
    }

    void saveEnhancedBrowserConfiguration() {
        assert ProjectManager.mutex().isWriteAccess() : "Write mutex required"; //NOI18N
        if (enhancedBrowserSettings != null) {
            enhancedBrowserSettings.save();
        }
    }

    void firePropertyChanges() {
        if (runAs != null) {
            String runAsValue = runAs.get();
            PlatformProviders.getDefault().notifyPropertyChanged(project,
                    new PropertyChangeEvent(project, PlatformProvider.PROP_RUN_CONFIGURATION, null, runAsValue));
        }
    }

    ClientProjectEnhancedBrowserImplementation createEnhancedBrowserSettings(WebBrowser wb) {
        enhancedBrowserSettings =
                ClientSideProject.createEnhancedBrowserImpl(project, wb);
        return enhancedBrowserSettings;
    }

    public ClientSideProject getProject() {
        return project;
    }

    public boolean isAutoconfigured() {
        if (autoconfigured == null) {
            autoconfigured = Boolean.parseBoolean(getProjectProperty(ClientSideProjectConstants.PROJECT_AUTOCONFIGURED, Boolean.FALSE.toString()));
        }
        return autoconfigured;
    }

    public void setAutoconfigured(boolean autoconfigured) {
        this.autoconfigured = autoconfigured;
    }

    public AtomicReference<String> getSourceFolder() {
        if (sourceFolder == null) {
            sourceFolder = new AtomicReference<>(getProjectProperty(ClientSideProjectConstants.PROJECT_SOURCE_FOLDER, null));
        }
        return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder) {
        this.sourceFolder = new AtomicReference<>(sourceFolder);
    }

    public AtomicReference<String> getSiteRootFolder() {
        if (siteRootFolder == null) {
            siteRootFolder = new AtomicReference<>(getProjectProperty(ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER, null));
        }
        return siteRootFolder;
    }

    public void setSiteRootFolder(String siteRootFolder) {
        this.siteRootFolder = new AtomicReference<>(siteRootFolder);
    }

    public AtomicReference<String> getTestFolder() {
        if (testFolder == null) {
            testFolder = new AtomicReference<>(getProjectProperty(ClientSideProjectConstants.PROJECT_TEST_FOLDER, null));
        }
        return testFolder;
    }

    public void setTestFolder(String testFolder) {
        this.testFolder = new AtomicReference<>(testFolder);
    }

    public AtomicReference<String> getTestSeleniumFolder() {
        if (testSeleniumFolder == null) {
            testSeleniumFolder = new AtomicReference<>(getProjectProperty(ClientSideProjectConstants.PROJECT_TEST_SELENIUM_FOLDER, null));
        }
        return testSeleniumFolder;
    }

    public void setTestSeleniumFolder(String testSeleniumFolder) {
        this.testSeleniumFolder = new AtomicReference<>(testSeleniumFolder);
    }

    public String getEncoding() {
        if (encoding == null) {
            encoding = getProjectProperty(ClientSideProjectConstants.PROJECT_ENCODING, ClientSideProjectUtilities.DEFAULT_PROJECT_CHARSET.name());
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public AtomicReference<String> getRunAs() {
        if (runAs == null) {
            runAs = new AtomicReference<>(getProjectProperty(ClientSideProjectConstants.PROJECT_RUN_AS, null));
        }
        return runAs;
    }

    public void setRunAs(String runAs) {
        this.runAs = new AtomicReference<>(runAs);
    }

    public boolean isRunBrowser() {
        if (runBrowser == null) {
            runBrowser = project.isRunBrowser();
        }
        return runBrowser;
    }

    public void setRunBrowser(boolean runBrowser) {
        this.runBrowser = runBrowser;
    }

    public String getStartFile() {
        if (startFile == null) {
            startFile = project.getStartFile();
        }
        return startFile;
    }

    public void setStartFile(String startFile) {
        this.startFile = startFile;
    }

    public String getSelectedBrowser() {
        if (selectedBrowser == null) {
            selectedBrowser = project.getSelectedBrowser();
        }
        return selectedBrowser;
    }

    public void setSelectedBrowser(String selectedBrowser) {
        this.selectedBrowser = selectedBrowser;
    }

    public String getWebRoot() {
        if (webRoot == null) {
            webRoot = project.getWebContextRoot();
        }
        return webRoot;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public String getProjectUrl() {
        if (projectUrl == null) {
            projectUrl = getProjectProperty(ClientSideProjectConstants.PROJECT_PROJECT_URL, ""); // NOI18N
        }
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public ProjectServer getProjectServer() {
        if (projectServer == null) {
            String value = getProjectProperty(ClientSideProjectConstants.PROJECT_SERVER, ProjectServer.INTERNAL.name());
            // toUpperCase() so we are backward compatible, can be later removed
            try {
                projectServer = ProjectServer.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, "Unknown project server type", ex);
                // fallback
                projectServer = ProjectServer.INTERNAL;
            }
        }
        return projectServer;
    }

    public void setProjectServer(ProjectServer projectServer) {
        this.projectServer = projectServer;
    }

    public void setJsLibFolder(String jsLibFolder) {
        assert jsLibFolder != null;
        this.jsLibFolder = jsLibFolder;
    }

    public String getJsLibFolder() {
        return jsLibFolder;
    }

    @CheckForNull
    public File getResolvedSourceFolder() {
        return resolveFile(getSourceFolder().get());
    }

    @CheckForNull
    public File getResolvedSiteRootFolder() {
        return resolveFile(getSiteRootFolder().get());
    }

    @CheckForNull
    public File getResolvedTestFolder() {
        return resolveFile(getTestFolder().get());
    }

    @CheckForNull
    public File getResolvedStartFile() {
        String siteRoot = getSiteRootFolder().get();
        if (siteRoot == null) {
            return null;
        }
        return resolveFile(siteRoot + (siteRoot.isEmpty() ? "" : "/") + getStartFile()); // NOI18N
    }

    private String getProjectProperty(String property, String defaultValue) {
        String value = project.getEvaluator().getProperty(property);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private void putProperty(EditableProperties properties, String property, String value) {
        if (value != null) {
            properties.put(property, value);
        }
    }

    private void putProperty(EditableProperties properties, String property, Boolean value) {
        if (value != null) {
            properties.put(property, value.toString());
        }
    }

    private String createForeignFileReference(AtomicReference<String> filePath, boolean storeEmptyPath) {
        if (filePath == null) {
            return null;
        }
        return createForeignFileReference(filePath.get(), storeEmptyPath);
    }

    private String createForeignFileReference(String filePath, boolean storeEmptyPath) {
        if (filePath == null) {
            // not set at all
            return null;
        }
        if (filePath.isEmpty()) {
            if (storeEmptyPath) {
                // empty value will be saved
                return ""; // NOI18N
            }
            return null;
        }
        File file = project.getProjectHelper().resolveFile(filePath);
        return project.getReferenceHelper().createForeignFileReference(file, null);
    }

    @CheckForNull
    private File resolveFile(String path) {
        if (path == null) {
            return null;
        }
        if (path.isEmpty()) {
            return FileUtil.toFile(project.getProjectDirectory());
        }
        return project.getProjectHelper().resolveFile(path);
    }

    public Licenses getLicenseSupport() {
        if (licenseSupport == null) {
            Env is = project.is;
            licenseSupport = is.newLicensePanelSupport(project.getEvaluator(), project.getProjectHelper(),
                getProjectProperty(Licenses.LICENSE_PATH, null),
                getProjectProperty(Licenses.LICENSE_NAME, null));
        }
        return licenseSupport;
    }

    //~ Inner classes

    @NbBundle.Messages({
        "ProjectServer.internal.title=Embedded Lightweight",
        "ProjectServer.external.title=External"
    })
    public static enum ProjectServer {
        INTERNAL(Bundle.ProjectServer_internal_title()),
        EXTERNAL(Bundle.ProjectServer_external_title());

        private final String title;

        private ProjectServer(String title) {
            assert title != null;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

    }

}
