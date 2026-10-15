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
package org.netbeans.modules.web.clientproject.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProvider;
import org.netbeans.modules.selenium2.webclient.api.SeleniumTestingProviders;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.ClientSideProjectType;
import org.netbeans.modules.web.clientproject.ant.AntServices;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProvider;
import org.netbeans.modules.web.clientproject.api.platform.PlatformProviders;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.env.Env;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author david
 */
public final class ClientSideProjectUtilities {

    public static final String USAGE_LOGGER_NAME = "org.netbeans.ui.metrics.web.clientproject"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(ClientSideProjectUtilities.class.getName());
    private static final Logger USG_LOGGER = Logger.getLogger(USAGE_LOGGER_NAME);

    public static final Charset DEFAULT_PROJECT_CHARSET = getDefaultProjectCharset();


    private ClientSideProjectUtilities() {
    }

    /**
     * Check whether the given folder is already a project.
     * <p>
     * This method ignores ProjectConvertor projects.
     * @param folder folder to be checked
     * @return {@code true} if the given folder is already a project, {@code false} otherwise
     */
    public static boolean isProject(File folder) {
        Project prj = null;
        boolean foundButBroken = false;
        try {
            prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(FileUtil.normalizeFile(folder)));
        } catch (IOException ex) {
            foundButBroken = true;
        } catch (IllegalArgumentException ex) {
            // noop
        }
        if (prj != null
                && !ProjectConvertors.isConvertorProject(prj)) {
            return true;
        }
        return foundButBroken;
    }

    // XXX
    public static boolean isCordovaProject(Project project) {
        FileObject projectDirectory = project.getProjectDirectory();
        FileObject cordova = projectDirectory.getFileObject(".cordova"); // NOI18N
        if (cordova == null) {
            cordova = projectDirectory.getFileObject("hooks"); // NOI18N
        }
        return cordova != null
                && cordova.isFolder();
    }

    /**
     * Setup project with the given name and also set the following properties:
     * <ul>
     *   <li>file encoding - set to UTF-8 (or default charset if UTF-8 is not available)</li>
     * </ul>
     * @param dirFO project directory
     * @param name project name
     * @return {@link CommonProjectHelper}
     * @throws IOException if any error occurs
     */
    public static CommonProjectHelper setupProject(FileObject dirFO, String name) throws IOException {
        // clearly creation of new project must depend on Ant
        Env is = AntServices.newServices();
        // create project
        CommonProjectHelper projectHelper = is.createProject(dirFO, ClientSideProjectType.TYPE);
        setProjectName(projectHelper, name, false);
        // #231319
        ProjectManager.getDefault().clearNonProjectCache();
        Project project = FileOwnerQuery.getOwner(dirFO);
        assert project != null;
        ClientSideProject clientSideProject = project.getLookup().lookup(ClientSideProject.class);
        if (clientSideProject == null) {
            throw new IllegalStateException("HTML5 project needed but found " + project.getClass().getName());
        }
        // set encoding
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(clientSideProject);
        projectProperties.setEncoding(DEFAULT_PROJECT_CHARSET.name());
        projectProperties.save();
        return projectHelper;
    }

    public static void initializeProject(@NonNull ClientSideProject project, @NullAllowed String sources, @NullAllowed String siteRoot,
            @NullAllowed String test, @NullAllowed String testSelenium) throws IOException {
        assert sources != null || siteRoot != null : "Sources and/or Site Root must be set";
        File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
        assert projectDirectory != null;
        assert projectDirectory.isDirectory();
        // ensure directories exists
        Env is = project.is;
        if (sources != null) {
            ensureDirectoryExists(is.resolveFile(projectDirectory, sources));
        }
        if (siteRoot != null) {
            ensureDirectoryExists(is.resolveFile(projectDirectory, siteRoot));
        }
        if (test != null) {
            ensureDirectoryExists(is.resolveFile(projectDirectory, test));
        }
        if (testSelenium != null) {
            ensureDirectoryExists(is.resolveFile(projectDirectory, testSelenium));
        }
        // save project
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(project);
        projectProperties.setSourceFolder(sources);
        projectProperties.setSiteRootFolder(siteRoot);
        projectProperties.setTestFolder(test);
        projectProperties.setTestSeleniumFolder(testSelenium);
        projectProperties.setSelectedBrowser(project.getProjectWebBrowser().getId());
        projectProperties.save();
    }

    public static void setJsTestingProvider(@NonNull Project project, @NonNull String jsTestingProviderIdentifier) {
        assert project != null;
        assert jsTestingProviderIdentifier != null;
        JsTestingProvider testingProvider = JsTestingProviders.getDefault().findJsTestingProvider(jsTestingProviderIdentifier);
        if (testingProvider == null) {
            LOGGER.log(Level.WARNING, "JS testing provider {0} was not found", jsTestingProviderIdentifier);
        } else {
            JsTestingProviders.getDefault().setJsTestingProvider(project, testingProvider);
        }
    }

    public static void setSeleniumTestingProvider(@NonNull Project project, @NonNull String seleniumTestingProviderIdentifier) {
        assert project != null;
        assert seleniumTestingProviderIdentifier != null;
        SeleniumTestingProvider testingProvider = SeleniumTestingProviders.getDefault().findSeleniumTestingProvider(seleniumTestingProviderIdentifier);
        if (testingProvider == null) {
            LOGGER.log(Level.WARNING, "Selenium testing provider {0} was not found", seleniumTestingProviderIdentifier);
        } else {
            SeleniumTestingProviders.getDefault().setSeleniumTestingProvider(project, testingProvider);
        }
    }

    public static void setPlatformProvider(@NonNull Project project, @NonNull String platformProviderIdentifier) {
        assert project != null;
        assert platformProviderIdentifier != null;
        PlatformProvider platformProvider = PlatformProviders.getDefault().findPlatformProvider(platformProviderIdentifier);
        if (platformProvider == null) {
            LOGGER.log(Level.WARNING, "platform provider {0} was not found", platformProviderIdentifier);
        } else {
            PlatformProviders.getDefault().setPlatformProvider(project, platformProvider);
        }
    }

    private static void ensureDirectoryExists(File folder) throws IOException {
        if (!folder.isDirectory()) {
            if (!folder.mkdirs()) {
                throw new IOException("Cannot create folder " + folder);
            }
        }
    }

    public static void setProjectName(final CommonProjectHelper projectHelper, final String name, final boolean saveProject) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                Element data = projectHelper.getPrimaryConfigurationData(true);
                Document document = data.getOwnerDocument();
                NodeList nameList = data.getElementsByTagNameNS(ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameElement;
                if (nameList.getLength() == 1) {
                    nameElement = (Element) nameList.item(0);
                    NodeList deadKids = nameElement.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameElement.removeChild(deadKids.item(0));
                    }
                } else {
                    nameElement = document.createElementNS(
                            ClientSideProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameElement, data.getChildNodes().item(0));
                }
                nameElement.appendChild(document.createTextNode(name));
                projectHelper.putPrimaryConfigurationData(data, true);
                if (saveProject) {
                    Project project = FileOwnerQuery.getOwner(projectHelper.getProjectDirectory());
                    assert project != null;
                    try {
                        ProjectManager.getDefault().saveProject(project);
                    } catch (IOException ex) {
                        LOGGER.log(Level.INFO, "Cannot save project", ex);
                    }
                }
            }
        });
    }

    public static SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        List<SourceGroup> res = new ArrayList<SourceGroup>();
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5)));
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT)));
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST)));
        res.addAll(Arrays.asList(sources.getSourceGroups(WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST_SELENIUM)));
        return res.toArray(new SourceGroup[0]);
    }

    public static SourceGroup[] getSourceGroups(Project project, String type) {
        Sources sources = ProjectUtils.getSources(project);
        return sources.getSourceGroups(type);
    }

    public static FileObject[] getSourceObjects(Project project) {
        SourceGroup[] groups = getSourceGroups(project);

        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    // #217970
    private static Charset getDefaultProjectCharset() {
        return StandardCharsets.UTF_8;
    }

    /**
     * Splits paths like 'index.html#/path' into  'index.html' and '#/path'
     */
    public static String[] splitPathAndFragment(String url) {
        int index = url.lastIndexOf('#');
        if (index != -1) {
            return new String[]{url.substring(0, index), url.substring(index)};
        } else {
            return new String[]{url,""};
        }
    }

    public static void logUsage(Class<? extends Object> srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }

    public static void logUsageProjectCreate(boolean existing, @NullAllowed SiteTemplateImplementation siteTemplate, Boolean siteRootUnderProjectDir,
            boolean isJsLibraryProject, @NullAllowed String platformProvider, boolean autoconfigured) {
        logUsage(ClientSideProjectUtilities.class, "USG_PROJECT_HTML5_CREATE", new Object[] { // NOI18N
            existing ? "EXISTING" : "NEW", // NOI18N
            siteTemplate != null ? siteTemplate.getId() : "NONE", // NOI18N
            "", // NOI18N
            siteRootUnderProjectDir == null ? "" : (siteRootUnderProjectDir ? "YES" : "NO"), // NOI18N
            isJsLibraryProject ? "YES" : "NO", // NOI18N
            platformProvider != null ? platformProvider : "", // NOI18N
            autoconfigured ? "YES" : "NO", // NOI18N
        });
    }

    public static boolean hasErrors(ClientSideProject project) {
        return !project.getLookup().lookup(ProjectProblemsProvider.class).getProblems().isEmpty();
    }

    @NonNull
    public static Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  // NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return getSafeColor(result.getRed(), result.getGreen(), result.getBlue());
    }

    public static Color getSafeColor(int red, int green, int blue) {
        red = Math.max(red, 0);
        red = Math.min(red, 255);
        green = Math.max(green, 0);
        green = Math.min(green, 255);
        blue = Math.max(blue, 0);
        blue = Math.min(blue, 255);
        return new Color(red, green, blue);
    }

    public static boolean isParentOrItself(@NullAllowed FileObject folder, @NullAllowed FileObject fo) {
        if (folder == null
                || fo == null) {
            return false;
        }
        if (folder.equals(fo)) {
            return true;
        }
        return FileUtil.isParentOf(folder, fo);
    }

}
