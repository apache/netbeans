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
package org.netbeans.modules.php.project.ui.customizer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.ConfigManager.Configuration;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.UsageLogging;
import org.netbeans.modules.php.spi.framework.PhpModuleCustomizerExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik, Radek Matous
 */
public final class PhpProjectProperties implements ConfigManager.ConfigProvider {

    private static final Logger LOGGER = Logger.getLogger(PhpProjectProperties.class.getName());

    public static final int DEFAULT_DEBUG_PROXY_PORT = 9001;

    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; // NOI18N
    public static final String SELENIUM_SRC_DIR = "selenium.src.dir"; // NOI18N
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    public static final String COPY_SRC_FILES = "copy.src.files"; // NOI18N
    public static final String COPY_SRC_TARGET = "copy.src.target"; // NOI18N
    public static final String COPY_SRC_ON_OPEN = "copy.src.on.open"; // NOI18N
    public static final String BROWSER_ID = "browser.id"; // NOI18N
    public static final String BROWSER_RELOAD_ON_SAVE = "browser.reload.on.save"; // NOI18N
    public static final String WEB_ROOT = "web.root"; // NOI18N
    public static final String URL = "url"; // NOI18N
    public static final String INDEX_FILE = "index.file"; // NOI18N
    public static final String INCLUDE_PATH = "include.path"; // NOI18N
    public static final String PRIVATE_INCLUDE_PATH = "include.path.private"; // NOI18N
    public static final String GLOBAL_INCLUDE_PATH = "php.global.include.path"; // NOI18N
    public static final String ARGS = "script.arguments"; // NOI18N
    public static final String PHP_ARGS = "php.arguments"; // NOI18N
    public static final String WORK_DIR = "work.dir"; // NOI18N
    public static final String INTERPRETER = "interpreter"; // NOI18N
    public static final String HOSTNAME = "hostname"; // NOI18N
    public static final String PORT = "port"; // NOI18N
    public static final String ROUTER = "router"; // NOI18N
    public static final String RUN_AS = "run.as"; // NOI18N
    public static final String REMOTE_CONNECTION = "remote.connection"; // NOI18N
    public static final String REMOTE_DIRECTORY = "remote.directory"; // NOI18N
    public static final String REMOTE_UPLOAD = "remote.upload"; // NOI18N
    public static final String REMOTE_PERMISSIONS = "remote.permissions"; // NOI18N
    public static final String REMOTE_UPLOAD_DIRECTLY = "remote.upload.directly"; // NOI18N
    public static final String DEBUG_URL = "debug.url"; // NOI18N
    public static final String DEBUG_PATH_MAPPING_REMOTE = "debug.path.mapping.remote"; // NOI18N
    public static final String DEBUG_PATH_MAPPING_LOCAL = "debug.path.mapping.local"; // NOI18N
    public static final String DEBUG_PROXY_HOST = "debug.proxy.host"; // NOI18N
    public static final String DEBUG_PROXY_PORT = "debug.proxy.port"; // NOI18N
    public static final String SHORT_TAGS = "tags.short"; // NOI18N
    public static final String ASP_TAGS = "tags.asp"; // NOI18N
    public static final String PHP_VERSION = "php.version"; // NOI18N
    public static final String IGNORE_PATH = "ignore.path"; // NOI18N
    public static final String CODE_ANALYSIS_EXCLUDES = "code.analysis.excludes"; // NOI18N
    public static final String LICENSE_NAME = "project.license";
    public static final String LICENSE_PATH = "project.licensePath";
    public static final String TESTING_PROVIDERS = "testing.providers";
    public static final String AUTOCONFIGURED = "autoconfigured";

    public static final String DEBUG_PATH_MAPPING_SEPARATOR = "||NB||"; // NOI18N
    public static final String TESTING_PROVIDERS_SEPARATOR = ";"; // NOI18N

    private static final String[] CFG_PROPS = new String[] {
        URL,
        INDEX_FILE,
        ARGS,
        PHP_ARGS,
        WORK_DIR,
        INTERPRETER,
        HOSTNAME,
        PORT,
        ROUTER,
        RUN_AS,
        REMOTE_CONNECTION,
        REMOTE_DIRECTORY,
        REMOTE_UPLOAD,
        REMOTE_PERMISSIONS,
        REMOTE_UPLOAD_DIRECTLY,
        DEBUG_URL,
        DEBUG_PATH_MAPPING_REMOTE,
        DEBUG_PATH_MAPPING_LOCAL,
        DEBUG_PROXY_HOST,
        DEBUG_PROXY_PORT,
    };

    @NbBundle.Messages({
        "RunAsType.local.label=Local Web Site (running on local web server)",
        "RunAsType.script.label=Script (run in command line)",
        "RunAsType.remote.label=Remote Web Site (FTP, SFTP)",
        "RunAsType.internal.label=PHP Built-in Web Server (running on built-in web server)"
    })
    public static enum RunAsType {
        LOCAL(Bundle.RunAsType_local_label()),
        SCRIPT(Bundle.RunAsType_script_label()),
        REMOTE(Bundle.RunAsType_remote_label()),
        INTERNAL(Bundle.RunAsType_internal_label());

        private final String label;

        private RunAsType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

    }

    public static enum UploadFiles {
        MANUALLY ("LBL_UploadFilesManually", "TXT_UploadFilesManually"), // NOI18N
        ON_RUN ("LBL_UploadFilesOnRun", "TXT_UploadFilesOnRun"), // NOI18N
        ON_SAVE ("LBL_UploadFilesOnSave", "TXT_UploadFilesOnSave"); // NOI18N

        private final String label;
        private final String description;

        UploadFiles(String labelKey, String descriptionKey) {
            label = NbBundle.getMessage(PhpProjectProperties.class, labelKey);
            description = NbBundle.getMessage(PhpProjectProperties.class, descriptionKey);
        }

        public String getLabel() {
            return label;
        }

        public String getDescription() {
          return description;
        }
    }

    public static enum DebugUrl {
        DEFAULT_URL,
        ASK_FOR_URL,
        DO_NOT_OPEN_BROWSER
    }

    public static enum XDebugUrlArguments {
        XDEBUG_SESSION_START,
        XDEBUG_SESSION_STOP,
        XDEBUG_SESSION_STOP_NO_EXEC
    }

    static final String CONFIG_PRIVATE_PROPERTIES_PATH = "nbproject/private/config.properties"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PhpProjectProperties.class.getName(), 2);

    private final PhpProject project;
    private final IncludePathSupport includePathSupport;
    private final IgnorePathSupport ignorePathSupport;
    private final TestDirectoriesPathSupport testDirectoriesPathSupport;
    private final SeleniumTestDirectoriesPathSupport seleniumTestDirectoriesPathSupport;

    // all these fields don't have to be volatile - this ensures request processor
    // CustomizerSources
    private String srcDir;
    private String copySrcFiles;
    private String copySrcTarget;
    private Boolean copySrcOnOpen;
    private String browserId;
    private String browserReloadOnSave;
    private String webRoot;
    private String url;
    private String indexFile;
    private String encoding;
    private String shortTags;
    private String aspTags;
    private String phpVersion;
    private Set<PhpModuleCustomizerExtender> customizerExtenders;
    private List<String> testingProviders;

    // CustomizerRun
    final Map<String/*|null*/, Map<String, String/*|null*/>/*|null*/> runConfigs;
    private final ConfigManager configManager;

    // CustomizerPhpIncludePath
    private DefaultListModel<BasePathSupport.Item> includePathListModel = null;
    private DefaultListModel<BasePathSupport.Item> privateIncludePathListModel = null;
    private ListCellRenderer<BasePathSupport.Item> includePathListRenderer = null;

    // CustomizerIgnorePath
    private DefaultListModel<BasePathSupport.Item> ignorePathListModel = null;
    private ListCellRenderer<BasePathSupport.Item> ignorePathListRenderer = null;
    private DefaultListModel<BasePathSupport.Item> codeAnalysisExcludesListModel = null;
    private ListCellRenderer<BasePathSupport.Item> codeAnalysisExcludesListRenderer = null;

    // Testing
    private DefaultListModel<BasePathSupport.Item> testDirectoriesListModel = null;
    private ListCellRenderer<BasePathSupport.Item> testDirectoriesListRenderer = null;
    // Selenium Testing
    private DefaultListModel<BasePathSupport.Item> seleniumTestDirectoriesListModel = null;
    private ListCellRenderer<BasePathSupport.Item> seleniumTestDirectoriesListRenderer = null;

    // license
    private String licenseNameValue;
    private boolean licenseNameChanged = false;
    private String licensePathValue;
    private boolean licensePathChanged = false;
    private String changedLicensePathContent;

    private volatile Boolean autoconfigured = null;


    public PhpProjectProperties(PhpProject project) {
        this(project, null, null, null, null);
    }

    public PhpProjectProperties(PhpProject project, IncludePathSupport includePathSupport, IgnorePathSupport ignorePathSupport,
            TestDirectoriesPathSupport testDirectoriesPathSupport, SeleniumTestDirectoriesPathSupport seleniumTestDirectoriesPathSupport) {
        assert project != null;

        this.project = project;
        this.includePathSupport = includePathSupport;
        this.ignorePathSupport = ignorePathSupport;
        this.testDirectoriesPathSupport = testDirectoriesPathSupport;
        this.seleniumTestDirectoriesPathSupport = seleniumTestDirectoriesPathSupport;

        runConfigs = readRunConfigs();
        String currentConfig = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty("config"); // NOI18N
        configManager = new ConfigManager(this, currentConfig);
    }

    @Override
    public String[] getConfigProperties() {
        return CFG_PROPS.clone();
    }

    @Override
    public Map<String, Map<String, String>> getConfigs() {
        return runConfigs;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public String getCopySrcFiles() {
        if (copySrcFiles == null) {
            copySrcFiles = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(COPY_SRC_FILES);
        }
        return copySrcFiles;
    }

    public void setCopySrcFiles(String copySrcFiles) {
        this.copySrcFiles = copySrcFiles;
    }

    public String getCopySrcTarget() {
        if (copySrcTarget == null) {
            copySrcTarget = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(COPY_SRC_TARGET);
        }
        return copySrcTarget;
    }

    public void setCopySrcTarget(String copySrcTarget) {
        this.copySrcTarget = copySrcTarget;
    }

    public boolean getCopySrcOnOpen() {
        if (copySrcOnOpen == null) {
            copySrcOnOpen = ProjectPropertiesSupport.isCopySourcesOnOpen(project);
        }
        return copySrcOnOpen;
    }

    public void setCopySrcOnOpen(boolean copySrcOnOpen) {
        this.copySrcOnOpen = copySrcOnOpen;
    }

    public void setShortTags(String shortTags) {
        this.shortTags = shortTags;
    }

    public void setAspTags(String aspTags) {
        this.aspTags = aspTags;
    }

    public void setPhpVersion(String phpVersion) {
        this.phpVersion = phpVersion;
    }

    public String getBrowserId() {
        if (browserId == null) {
            browserId = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(BROWSER_ID);
        }
        return browserId;
    }

    public void setBrowserId(String browserId) {
        this.browserId = browserId;
    }

    public String getBrowserReloadOnSave() {
        if (browserReloadOnSave == null) {
            browserReloadOnSave = String.valueOf(ProjectPropertiesSupport.getBrowserReloadOnSave(project));
        }
        return browserReloadOnSave;
    }

    public void setBrowserReloadOnSave(String browserReloadOnSave) {
        this.browserReloadOnSave = browserReloadOnSave;
    }

    /**
     * @return the webRoot, which is relative path to srcDir.
     */
    public String getWebRoot() {
        if (webRoot == null) {
            webRoot = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(WEB_ROOT);
        }
        return webRoot != null ? webRoot : ""; // NOI18N
    }

    /**
     * @param webRoot the webRoot to set
     */
    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    public String getEncoding() {
        if (encoding == null) {
            encoding = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(SOURCE_ENCODING);
        }
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getSrcDir() {
        if (srcDir == null) {
            srcDir = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(SRC_DIR);
        }
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        assert srcDir != null;
        this.srcDir = srcDir;
    }

    public String getUrl() {
        if (url == null) {
            url = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(URL);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIndexFile() {
        if (indexFile == null) {
            indexFile = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(INDEX_FILE);
        }
        return indexFile;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    public DefaultListModel<BasePathSupport.Item> getIncludePathListModel() {
        if (includePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            includePathListModel = PathUiSupport.createListModel(includePathSupport.itemsIterator(
                    properties.getProperty(INCLUDE_PATH)));
        }
        return includePathListModel;
    }

    public DefaultListModel<BasePathSupport.Item> getPrivateIncludePathListModel() {
        if (privateIncludePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            privateIncludePathListModel = PathUiSupport.createListModel(includePathSupport.itemsIterator(
                    properties.getProperty(PRIVATE_INCLUDE_PATH)));
        }
        return privateIncludePathListModel;
    }

    public ListCellRenderer<BasePathSupport.Item> getIncludePathListRenderer() {
        if (includePathListRenderer == null) {
            includePathListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return includePathListRenderer;
    }

    public DefaultListModel<BasePathSupport.Item> getIgnorePathListModel() {
        if (ignorePathListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ignorePathListModel = PathUiSupport.createListModel(ignorePathSupport.itemsIterator(
                    properties.getProperty(IGNORE_PATH)));
        }
        return ignorePathListModel;
    }

    public ListCellRenderer<BasePathSupport.Item> getIgnorePathListRenderer() {
        if (ignorePathListRenderer == null) {
            ignorePathListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return ignorePathListRenderer;
    }

    public DefaultListModel<BasePathSupport.Item> getCodeAnalysisExcludesListModel() {
        if (codeAnalysisExcludesListModel == null) {
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            codeAnalysisExcludesListModel = PathUiSupport.createListModel(ignorePathSupport.itemsIterator(
                    properties.getProperty(CODE_ANALYSIS_EXCLUDES)));
        }
        return codeAnalysisExcludesListModel;
    }

    public ListCellRenderer<BasePathSupport.Item> getCodeAnalysisExcludesListModelListRenderer() {
        if (codeAnalysisExcludesListRenderer == null) {
            codeAnalysisExcludesListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return codeAnalysisExcludesListRenderer;
    }

    public DefaultListModel<BasePathSupport.Item> getTestDirectoriesListModel() {
        if (testDirectoriesListModel == null) {
            List<String> values = new ArrayList<>();
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            for (String property : project.getTestRoots().getRootProperties()) {
                String value = properties.getProperty(property);
                if (value != null) {
                    values.add(value);
                } else {
                    // #250401
                    LOGGER.log(Level.INFO, "Value must be found for property: {0}", property);
                    assert false : String.valueOf(properties);
                }
            }
            testDirectoriesListModel = PathUiSupport.createListModel(testDirectoriesPathSupport.itemsIterator(values.toArray(new String[0])));
        }
        return testDirectoriesListModel;
    }

    public ListCellRenderer<BasePathSupport.Item> getTestDirectoriesListRenderer() {
        if (testDirectoriesListRenderer == null) {
            testDirectoriesListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return testDirectoriesListRenderer;
    }

    public DefaultListModel<BasePathSupport.Item> getSeleniumTestDirectoriesListModel() {
        if (seleniumTestDirectoriesListModel == null) {
            List<String> values = new ArrayList<>();
            EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            for (String property : project.getSeleniumRoots().getRootProperties()) {
                String value = properties.getProperty(property);
                if (value != null) {
                    values.add(value);
                } else {
                    // #250401
                    LOGGER.log(Level.INFO, "Value must be found for property: {0}", property);
                    assert false : String.valueOf(properties);
                }
            }
            seleniumTestDirectoriesListModel = PathUiSupport.createListModel(seleniumTestDirectoriesPathSupport.itemsIterator(values.toArray(new String[0])));
        }
        return seleniumTestDirectoriesListModel;
    }

    public ListCellRenderer<BasePathSupport.Item> getSeleniumTestDirectoriesListRenderer() {
        if (seleniumTestDirectoriesListRenderer == null) {
            seleniumTestDirectoriesListRenderer = new PathUiSupport.ClassPathListCellRenderer(ProjectPropertiesSupport.getPropertyEvaluator(project),
                project.getProjectDirectory());
        }
        return seleniumTestDirectoriesListRenderer;
    }

    public void addCustomizerExtender(PhpModuleCustomizerExtender customizerExtender) {
        if (customizerExtenders == null) {
            customizerExtenders = new HashSet<>();
        }
        customizerExtenders.add(customizerExtender);
    }

    public String getLicenseNameValue() {
        if (licenseNameValue == null) {
            licenseNameChanged = true;
            licenseNameValue = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(LICENSE_NAME);
        }
        return licenseNameValue;
    }

    public void setLicenseNameValue(String licenseNameValue) {
        licenseNameChanged = true;
        this.licenseNameValue = licenseNameValue;
    }

    public String getLicensePathValue() {
        if (licensePathValue == null) {
            licensePathChanged = true;
            licensePathValue = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(LICENSE_PATH);
        }
        return licensePathValue;
    }

    public void setLicensePathValue(String licensePathValue) {
        licensePathChanged = true;
        this.licensePathValue = licensePathValue;
    }

    public String getChangedLicensePathContent() {
        return changedLicensePathContent;
    }

    public void setChangedLicensePathContent(String changedLicensePathContent) {
        this.changedLicensePathContent = changedLicensePathContent;
    }

    public List<String> getTestingProviders() {
        if (testingProviders == null) {
            String value = ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(TESTING_PROVIDERS);
            testingProviders = StringUtils.explode(value, TESTING_PROVIDERS_SEPARATOR);
        }
        return testingProviders;
    }

    public void setTestingProviders(List<String> testingProviders) {
        this.testingProviders = testingProviders;
    }

    public boolean isAutoconfigured() {
        if (autoconfigured == null) {
            autoconfigured = Boolean.parseBoolean(ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(AUTOCONFIGURED));
        }
        return autoconfigured;
    }

    public void setAutoconfigured(boolean autoconfigured) {
        this.autoconfigured = autoconfigured;
    }

    public void save() {
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    saveProperties();

                    saveCustomizerExtenders();

                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
    }

    /**
     * Add or replace project and/or private properties of the given project.
     * @param project project to be saved
     * @param projectProperties project properties to be added (replaced) to the current project properties
     * @param privateProperties private properties to be added (replaced) to the current private properties
     */
    public static void save(final PhpProject project, final Map<String, String> projectProperties, final Map<String, String> privateProperties) {
        assert !projectProperties.isEmpty() || !privateProperties.isEmpty() : "Neither project nor private properties to be saved";
        try {
            // store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    AntProjectHelper helper = project.getHelper();

                    mergeProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                    mergeProperties(helper, AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }

                private void mergeProperties(AntProjectHelper helper, String path, Map<String, String> properties) {
                    if (properties.isEmpty()) {
                        return;
                    }
                    EditableProperties currentProperties = helper.getProperties(path);
                    for (Map.Entry<String, String> entry : properties.entrySet()) {
                        currentProperties.put(entry.getKey(), entry.getValue());
                    }
                    helper.putProperties(path, currentProperties);
                }
            });
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
    }

    void saveProperties() throws IOException {
        AntProjectHelper helper = project.getHelper();

        // encode include path
        String[] includePath = null;
        if (includePathListModel != null) {
            includePath = includePathSupport.encodeToStrings(PathUiSupport.getIterator(includePathListModel));
        }
        String[] privateIncludePath = null;
        if (privateIncludePathListModel != null) {
            privateIncludePath = includePathSupport.encodeToStrings(PathUiSupport.getIterator(privateIncludePathListModel), false);
        }

        // encode ignore path
        String[] ignorePath = null;
        if (ignorePathListModel != null) {
            ignorePath = ignorePathSupport.encodeToStrings(PathUiSupport.getIterator(ignorePathListModel));
        }
        String[] codeAnalysisExcludes = null;
        if (codeAnalysisExcludesListModel != null) {
            codeAnalysisExcludes = ignorePathSupport.encodeToStrings(PathUiSupport.getIterator(codeAnalysisExcludesListModel));
        }

        // testing
        String[] testDirs = null;
        if (testDirectoriesListModel != null) {
            testDirs = testDirectoriesPathSupport.encodeToStrings(PathUiSupport.getIterator(testDirectoriesListModel), true, false);
        }

        // selenium testing
        String[] seleniumTestDirs = null;
        if (seleniumTestDirectoriesListModel != null) {
            seleniumTestDirs = seleniumTestDirectoriesPathSupport.encodeToStrings(PathUiSupport.getIterator(seleniumTestDirectoriesListModel), true, false);
        }

        // get properties
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        // sources
        if (srcDir != null) {
            projectProperties.setProperty(SRC_DIR, srcDir);
        }
        if (copySrcFiles != null) {
            privateProperties.setProperty(COPY_SRC_FILES, copySrcFiles);
        }
        if (copySrcTarget != null) {
            privateProperties.setProperty(COPY_SRC_TARGET, copySrcTarget);
        }
        if (copySrcOnOpen != null) {
            privateProperties.setProperty(COPY_SRC_ON_OPEN, String.valueOf(copySrcOnOpen));
        }
        if (encoding != null) {
            projectProperties.setProperty(SOURCE_ENCODING, encoding);
        }
        if (browserId != null) {
            privateProperties.setProperty(BROWSER_ID, browserId);
        }
        if (browserReloadOnSave != null) {
            projectProperties.setProperty(BROWSER_RELOAD_ON_SAVE, browserReloadOnSave);
        }
        if (webRoot != null) {
            projectProperties.setProperty(WEB_ROOT, webRoot);
        }
        if (phpVersion != null) {
            projectProperties.setProperty(PHP_VERSION, phpVersion);
        }
        if (shortTags != null) {
            projectProperties.setProperty(SHORT_TAGS, shortTags);
        }
        if (aspTags != null) {
            projectProperties.setProperty(ASP_TAGS, aspTags);
        }

        // php include path
        if (includePath != null) {
            projectProperties.setProperty(INCLUDE_PATH, includePath);
        }
        if (privateIncludePath != null) {
            privateProperties.setProperty(PRIVATE_INCLUDE_PATH, privateIncludePath);
        }

        // ignore path
        if (ignorePath != null) {
            projectProperties.setProperty(IGNORE_PATH, ignorePath);
        }
        if (codeAnalysisExcludes != null) {
            projectProperties.setProperty(CODE_ANALYSIS_EXCLUDES, codeAnalysisExcludes);
        }

        // testing
        if (testDirs != null) {
            // first, remove all current test dirs
            for (String property : project.getTestRoots().getRootProperties()) {
                projectProperties.remove(property);
            }
            // set new ones
            int i = 1;
            for (String testDir : testDirs) {
                String propertyName = TEST_SRC_DIR;
                if (i > 1) {
                    // backward compatibility
                    propertyName += i;
                }
                projectProperties.setProperty(propertyName, testDir);
                i++;
            }
        }
        // selenium testing
        if (seleniumTestDirs != null) {
            // first, remove all current test dirs
            for (String property : project.getSeleniumRoots().getRootProperties()) {
                projectProperties.remove(property);
            }
            // set new ones
            int i = 1;
            for (String seleniumTestDir : seleniumTestDirs) {
                String propertyName = SELENIUM_SRC_DIR;
                if (i > 1) {
                    // backward compatibility
                    propertyName += i;
                }
                projectProperties.setProperty(propertyName, seleniumTestDir);
                i++;
            }
        }

        // license
        if (licensePathValue != null) {
            projectProperties.setProperty(LICENSE_PATH, licensePathValue);
        } else if (licensePathChanged) {
            projectProperties.remove(LICENSE_PATH);
        }
        if (licenseNameValue != null) {
            projectProperties.setProperty(LICENSE_NAME, licenseNameValue);
        } else if (licenseNameChanged) {
            projectProperties.remove(LICENSE_NAME);
        }
        if (changedLicensePathContent != null) {
            assert licensePathValue != null; // path needs to exist once we have content?
            String evaluated = ProjectPropertiesSupport.getPropertyEvaluator(project).evaluate(licensePathValue);
            assert evaluated != null : licensePathValue;
            File file = project.getHelper().resolveFile(evaluated);
            FileObject fo;
            if (!file.exists()) {
                fo = FileUtil.createData(file);
            } else {
                fo = FileUtil.toFileObject(file);
            }
            try (OutputStream out = fo.getOutputStream()) {
                String charsetName;
                if (encoding != null) {
                    charsetName = encoding;
                } else {
                    charsetName = ProjectPropertiesSupport.getEncoding(project);
                }
                FileUtil.copy(new ByteArrayInputStream(changedLicensePathContent.getBytes(charsetName)), out);
            }
        }

        if (testingProviders != null) {
            projectProperties.setProperty(TESTING_PROVIDERS, StringUtils.implode(testingProviders, TESTING_PROVIDERS_SEPARATOR));
        }

        if (autoconfigured != null) {
            if (autoconfigured) {
                privateProperties.put(AUTOCONFIGURED, Boolean.TRUE.toString());
            } else {
                privateProperties.remove(AUTOCONFIGURED);
            }
        }

        // configs
        storeRunConfigs(projectProperties, privateProperties);
        EditableProperties ep = helper.getProperties(CONFIG_PRIVATE_PROPERTIES_PATH);
        String currentConfig = configManager.currentConfiguration().getName();
        if (currentConfig == null) {
            ep.remove("config"); // NOI18N
        } else {
            ep.setProperty("config", currentConfig); // NOI18N
        }

        // store all the properties
        helper.putProperties(CONFIG_PRIVATE_PROPERTIES_PATH, ep);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        // additional changes
        // encoding
        if (encoding != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(encoding));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }

        // reset timestamp of the last upload & download
        ProjectSettings.resetLastUpload(project);
        ProjectSettings.resetLastDownload(project);

        // UI log
        logUsage(helper.getProjectDirectory(), ProjectPropertiesSupport.getSourcesDirectory(project),
                getActiveRunAsType(), getNumOfRunConfigs(), Boolean.valueOf(getCopySrcFiles()));
        // #245518
        if (testingProviders != null) {
            UsageLogging.logTestConfig(project, testingProviders);
        }
    }

    void saveCustomizerExtenders() {
        if (customizerExtenders != null) {
            final EnumSet<PhpModuleCustomizerExtender.Change> changes = EnumSet.noneOf(PhpModuleCustomizerExtender.Change.class);
            final PhpModule phpModule = project.getPhpModule();
            for (PhpModuleCustomizerExtender customizerExtender : customizerExtenders) {
                EnumSet<PhpModuleCustomizerExtender.Change> change = customizerExtender.save(phpModule);
                if (change != null) {
                    changes.addAll(change);
                }
            }

            // fire events (background thread, no locks)
            if (!changes.isEmpty()) {
                RP.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (PhpModuleCustomizerExtender.Change change : changes) {
                            switch (change) {
                                case SOURCES_CHANGE:
                                    project.getSourceRoots().refresh();
                                    break;
                                case TESTS_CHANGE:
                                    project.getTestRoots().refresh();
                                    break;
                                case SELENIUM_CHANGE:
                                    project.getSeleniumRoots().refresh();
                                    break;
                                case IGNORED_FILES_CHANGE:
                                    project.fireIgnoredFilesChange();
                                    break;
                                case FRAMEWORK_CHANGE:
                                    project.resetFrameworks();
                                    break;
                                default:
                                    throw new IllegalStateException("Unknown change: " + change);
                            }
                        }
                    }
                });
            }
        }
    }

    @CheckForNull
    public File getResolvedWebRootFolder() {
        File sourceDir = resolveFile(getSrcDir());
        if (sourceDir == null) {
            return null;
        }
        String wr = getWebRoot();
        if (StringUtils.hasText(wr)) {
            return PropertyUtils.resolveFile(sourceDir, wr);
        }
        return sourceDir;
    }

    @CheckForNull
    private File resolveFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        return project.getHelper().resolveFile(path);
    }

    private String getActiveRunAsType() {
        return configManager.currentConfiguration().getValue(RUN_AS);
    }

    private int getNumOfRunConfigs() {
        int n = 0;
        // removed configs may be null, do not count them
        for (String name : configManager.configurationNames()) {
            if (configManager.exists(name)) {
                ++n;
            }
        }
        return n;
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private void logUsage(FileObject projectDir, FileObject sourceDir, String activeRunAsType, int numOfConfigs, boolean copyFiles) {
        PhpProjectUtils.logUsage(PhpProjectProperties.class, "USG_PROJECT_CONFIG_PHP", Arrays.asList(
                FileUtil.isParentOf(projectDir, sourceDir) ? "EXTRA_SRC_DIR_NO" : "EXTRA_SRC_DIR_YES", // NOI18N
                activeRunAsType,
                Integer.toString(numOfConfigs),
                copyFiles ? "COPY_FILES_YES" : "COPY_FILES_NO")); // NOI18N
    }

    public PhpProject getProject() {
        return project;
    }

    /**
     * A mess.
     */
    Map<String/*|null*/, Map<String, String>> readRunConfigs() {
        Map<String, Map<String, String>> m = ConfigManager.createEmptyConfigs();
        Map<String, String> def = new TreeMap<>();
        EditableProperties privateProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties projectProperties = getProject().getHelper().getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        for (String prop : CFG_PROPS) {
            String v = privateProperties.getProperty(prop);
            if (v == null) {
                v = projectProperties.getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                m.put(kid.getName(), new TreeMap<>(getProject().getHelper().getProperties(path)));
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs"); // NOI18N
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) { // NOI18N
                    continue;
                }
                Map<String, String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                String path = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                c.putAll(new HashMap<>(getProject().getHelper().getProperties(path)));
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * A royal mess.
     */
    void storeRunConfigs(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        Configuration defaultConfiguration = configManager.defaultConfiguration();
        for (String prop : CFG_PROPS) {
            String value = defaultConfiguration.getValue(prop);
            EditableProperties ep = isPrivateProperty(prop) ? privateProperties : projectProperties;
            if (!Utilities.compareObjects(value, ep.getProperty(prop))) {
                if (StringUtils.hasText(value)) {
                    ep.setProperty(prop, value);
                } else {
                    ep.remove(prop);
                }
            }
        }

        for (String name : configManager.configurationNames()) {
            if (name == null) {
                // default config
                continue;
            }
            String sharedPath = "nbproject/configs/" + name + ".properties"; // NOI18N
            String privatePath = "nbproject/private/configs/" + name + ".properties"; // NOI18N

            if (!configManager.exists(name)) {
                // deleted config
                getProject().getHelper().putProperties(sharedPath, null);
                getProject().getHelper().putProperties(privatePath, null);
                continue;
            }

            Configuration configuration = configManager.configurationFor(name);
            // #233356 - display name
            String displayName = configuration.getValue(ConfigManager.PROP_DISPLAY_NAME);
            if (displayName != null) {
                EditableProperties ep = getProject().getHelper().getProperties(sharedPath);
                ep.setProperty(ConfigManager.PROP_DISPLAY_NAME, displayName);
                getProject().getHelper().putProperties(sharedPath, ep);
            }
            for (String prop : CFG_PROPS) {
                String value = configuration.getValue(prop);
                String path = isPrivateProperty(prop) ? privatePath : sharedPath;
                EditableProperties ep = getProject().getHelper().getProperties(path);
                if (!Utilities.compareObjects(value, ep.getProperty(prop))) {
                    if (value != null && (value.length() > 0 || (StringUtils.hasText(defaultConfiguration.getValue(prop))))) {
                        ep.setProperty(prop, value);
                    } else {
                        ep.remove(prop);
                    }
                    getProject().getHelper().putProperties(path, ep);
                }
            }
            // make sure the definition file is always created, even if it is empty.
            getProject().getHelper().putProperties(sharedPath, getProject().getHelper().getProperties(sharedPath));
        }
    }

    private boolean isPrivateProperty(String property) {
        // #145477 - all the config properties are stored in private properties because we don't want them to be versioned
        return true;
    }
}
