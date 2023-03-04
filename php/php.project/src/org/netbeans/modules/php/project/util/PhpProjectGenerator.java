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

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProjectType;
import org.netbeans.modules.php.project.api.PhpLanguageProperties;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleExtender;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class PhpProjectGenerator {
    private static final Logger LOGGER = Logger.getLogger(PhpProjectGenerator.class.getName());

    public static final Monitor DEV_NULL = new Monitor() {
        @Override
        public void starting() {
        }
        @Override
        public void creatingIndexFile() {
        }
        @Override
        public void finishing() {
        }
    };

    private PhpProjectGenerator() {
    }

    /**
     * Create a new PHP project for the provided properties. This operation is <a href="http://wiki.netbeans.org/UsageLoggingSpecification">logged</a>.
     * @param projectProperties project properties
     * @param monitor monitor, can be <code>null</code> (if so, {@link #DEV_NULL} is used)
     * @return {@link AntProjectHelper}
     * @throws IOException if any error occurs
     */
    public static AntProjectHelper createProject(ProjectProperties projectProperties, Monitor monitor) throws IOException {
        if (monitor == null) {
            monitor = DEV_NULL;
        }
        monitor.starting();

        ProjectProperties projectPropertiesCopy = new ProjectProperties(projectProperties);
        boolean existingSources = projectPropertiesCopy.getSourcesDirectory().exists();

        // #140346
        // first, create sources
        FileObject sourceDir = FileUtil.createFolder(projectPropertiesCopy.getSourcesDirectory());

        // project
        AntProjectHelper helper = createProject0(projectPropertiesCopy);

        // usage logging
        logUsage(helper.getProjectDirectory(), sourceDir, projectPropertiesCopy.getRunAsType(), projectPropertiesCopy.isCopySources(), projectPropertiesCopy.getFrameworkExtenders());

        // index file
        WizardDescriptor descriptor = projectPropertiesCopy.getDescriptor();
        if (descriptor == null) {
            LOGGER.fine("Index file not used, no descriptor given");
        } else {
            String indexFile = projectPropertiesCopy.getIndexFile();
            if (!existingSources && indexFile != null) {
                monitor.creatingIndexFile();

                FileObject template = null;
                RunAsType runAsType = projectPropertiesCopy.getRunAsType();
                if (runAsType == null) {
                    // run configuration panel not shown at all
                    template = Templates.getTemplate(descriptor);
                } else {
                    switch (runAsType) {
                        case SCRIPT:
                            template = FileUtil.getConfigFile("Templates/Scripting/EmptyPHP.php"); // NOI18N
                            break;
                        default:
                            template = Templates.getTemplate(descriptor);
                            break;
                    }
                }
                assert template != null : "Template for Index PHP file cannot be null";
                createIndexFile(template, sourceDir, indexFile);
            }
        }

        monitor.finishing();

        return helper;
    }

    private static AntProjectHelper createProject0(final ProjectProperties projectProperties) throws IOException {
        File projectDirectory = projectProperties.getProjectDirectory();
        if (projectDirectory == null) {
            projectDirectory = projectProperties.getSourcesDirectory();
        }
        assert projectDirectory != null;
        FileObject projectFO = FileUtil.createFolder(projectDirectory);
        final AntProjectHelper helper = ProjectGenerator.createProject(projectFO, PhpProjectType.TYPE);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws MutexException {
                    try {
                        // configure
                        Element data = helper.getPrimaryConfigurationData(true);
                        Document doc = data.getOwnerDocument();
                        Element nameEl = doc.createElementNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                        nameEl.appendChild(doc.createTextNode(projectProperties.getName()));
                        data.appendChild(nameEl);
                        helper.putPrimaryConfigurationData(data, true);

                        EditableProperties sharedProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

                        configureAutoconfigured(projectProperties, sharedProperties, privateProperties);
                        configureSources(helper, projectProperties, sharedProperties, privateProperties);
                        configureEncoding(projectProperties, sharedProperties, privateProperties);
                        configureTags(projectProperties, sharedProperties, privateProperties);
                        configureIncludePath(projectProperties, sharedProperties, privateProperties);
                        // #146882
                        configureUrl(projectProperties, sharedProperties, privateProperties);

                        if (projectProperties.getRunAsType() != null) {
                            // run configuration panel shown
                            configureCopySources(projectProperties, sharedProperties, privateProperties);
                            configureIndexFile(projectProperties, sharedProperties, privateProperties);
                            configureRunConfiguration(projectProperties, sharedProperties, privateProperties);
                        }

                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, sharedProperties);
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

                        Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                        assert project != null;
                        ProjectManager.getDefault().saveProject(project);

                        assert verifyProjectProperties(project);

                    } catch (IOException ioe) {
                        throw new MutexException(ioe);
                    }
                    return null;
                }

                // #213468 - check src.dir property
                private boolean verifyProjectProperties(Project project) throws IOException {
                    FileObject nbproject = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
                    if (nbproject == null) {
                        throw new IllegalStateException("nbproject directory does not exist for project " + project.getProjectDirectory());
                    }
                    if (!nbproject.isValid()) {
                        throw new IllegalStateException("nbproject directory not valid for project " + project.getProjectDirectory());
                    }
                    FileObject projectProperties = nbproject.getFileObject("project.properties"); // NOI18N
                    if (projectProperties == null) {
                        throw new IllegalStateException("nbproject/project.properties does not exist for project " + project.getProjectDirectory());
                    }
                    if (!projectProperties.isValid()) {
                        throw new IllegalStateException("nbproject/project.properties not valid for project " + project.getProjectDirectory());
                    }
                    if (!projectProperties.asText().contains(PhpProjectProperties.SRC_DIR)) {
                        throw new IllegalStateException("src.dir not found in nbproject/project.properties for project " + project.getProjectDirectory());
                    }
                    return true;
                }

            });
        } catch (MutexException e) {
            Exception ie = e.getException();
            if (ie instanceof IOException) {
                throw (IOException) ie;
            }
            Exceptions.printStackTrace(e);
        }
        return helper;
    }

    private static void configureAutoconfigured(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        Boolean autoconfigured = projectProperties.getAutoconfigured();
        if (autoconfigured != null) {
            privateProperties.setProperty(PhpProjectProperties.AUTOCONFIGURED, Boolean.toString(autoconfigured));
        }
    }

    private static void configureSources(AntProjectHelper helper, ProjectProperties projectProperties,
            EditableProperties sharedProperties, EditableProperties privateProperties) {
        File srcDir = projectProperties.getSourcesDirectory();
        File projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        if (srcPath == null) {
            // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
            srcPath = srcDir.getAbsolutePath();
        }
        sharedProperties.setProperty(PhpProjectProperties.SRC_DIR, srcPath);
        sharedProperties.setProperty(PhpProjectProperties.WEB_ROOT, "."); // NOI18N
        sharedProperties.setProperty(PhpProjectProperties.PHP_VERSION, projectProperties.getPhpVersion().name()); // NOI18N
    }

    private static void configureEncoding(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        Charset charset = projectProperties.getCharset();
        sharedProperties.setProperty(PhpProjectProperties.SOURCE_ENCODING, charset.name());
        // #136917
        FileEncodingQuery.setDefaultEncoding(charset);
    }

    private static void configureTags(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        sharedProperties.setProperty(PhpProjectProperties.SHORT_TAGS, String.valueOf(PhpLanguageProperties.SHORT_TAGS_ENABLED));
        sharedProperties.setProperty(PhpProjectProperties.ASP_TAGS, String.valueOf(PhpLanguageProperties.ASP_TAGS_ENABLED));
    }

    private static void configureIncludePath(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        sharedProperties.setProperty(PhpProjectProperties.INCLUDE_PATH, "${" + PhpProjectProperties.GLOBAL_INCLUDE_PATH + "}"); // NOI18N
    }

    private static void configureUrl(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        privateProperties.put(PhpProjectProperties.URL, projectProperties.getUrl());
    }

    private static void configureCopySources(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        String copyTargetString = ""; // NOI18N
        File target = projectProperties.getCopySourcesTarget();
        if (target != null) {
            copyTargetString = target.getAbsolutePath();
        }
        privateProperties.put(PhpProjectProperties.COPY_SRC_FILES, String.valueOf(projectProperties.isCopySources()));
        privateProperties.put(PhpProjectProperties.COPY_SRC_TARGET, copyTargetString);
        privateProperties.put(PhpProjectProperties.COPY_SRC_ON_OPEN, String.valueOf(projectProperties.isCopySourcesOnOpen()));
    }

    private static void configureIndexFile(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        String indexFile = projectProperties.getIndexFile();
        if (indexFile != null) {
            privateProperties.setProperty(PhpProjectProperties.INDEX_FILE, indexFile);
        }
    }

    private static void configureRunConfiguration(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        PhpProjectProperties.RunAsType runAs = projectProperties.getRunAsType();
        privateProperties.put(PhpProjectProperties.RUN_AS, runAs.name());
        switch (runAs) {
            case LOCAL:
            case SCRIPT:
                // nothing to store
                break;
            case REMOTE:
                configureRunAsRemoteWeb(projectProperties, sharedProperties, privateProperties);
                break;
            case INTERNAL:
                configureRunAsInternalServer(projectProperties, sharedProperties, privateProperties);
                break;
            default:
                assert false : "Unhandled RunAsType type: " + runAs;
                break;
        }
    }

    private static void configureRunAsRemoteWeb(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        RemoteConfiguration remoteConfiguration = projectProperties.getRemoteConfiguration();
        String remoteDirectory = projectProperties.getRemoteDirectory();
        PhpProjectProperties.UploadFiles uploadFiles = projectProperties.getUploadFiles();

        assert remoteConfiguration != null;
        assert uploadFiles != null;

        privateProperties.put(PhpProjectProperties.REMOTE_CONNECTION, remoteConfiguration.getName());
        privateProperties.put(PhpProjectProperties.REMOTE_DIRECTORY, remoteDirectory);
        privateProperties.put(PhpProjectProperties.REMOTE_UPLOAD, uploadFiles.name());
    }

    private static void configureRunAsInternalServer(ProjectProperties projectProperties, EditableProperties sharedProperties, EditableProperties privateProperties) {
        String hostname = projectProperties.getHostname();
        Integer port = projectProperties.getPort();
        String router = projectProperties.getRouter();

        assert hostname != null;
        assert port != null;

        privateProperties.put(PhpProjectProperties.HOSTNAME, hostname);
        privateProperties.put(PhpProjectProperties.PORT, String.valueOf(port));
        if (StringUtils.hasText(router)) {
            privateProperties.put(PhpProjectProperties.ROUTER, router);
        }
        // XXX remove index.file from properties, so run/debug project can then work
        privateProperties.remove(PhpProjectProperties.INDEX_FILE);
    }

    private static void createIndexFile(FileObject template, FileObject sourceDir, String indexFile) throws IOException {
        String indexFileName = getIndexFileName(indexFile, template.getExt());

        DataFolder dataFolder = DataFolder.findFolder(sourceDir);
        DataObject dataTemplate = DataObject.find(template);
        DataObject index = dataTemplate.createFromTemplate(dataFolder, indexFileName);

        // #187374
        try {
            FileUtils.reformatFile(index);
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, exc.getMessage(), exc);
        }
    }

    private static String getIndexFileName(String indexFile, String plannedExt) {
        String ext = "." + plannedExt; // NOI18N
        if (indexFile.endsWith(ext)) {
            return indexFile.substring(0, indexFile.length() - ext.length());
        }
        return indexFile;
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage(FileObject projectDir, FileObject sourceDir, RunAsType runAs, Boolean copyFiles,
            Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders) {
        assert projectDir != null;
        assert sourceDir != null;

        PhpProjectUtils.logUsage(PhpProjectGenerator.class, "USG_PROJECT_CREATE_PHP", Arrays.asList(
                projectDir.equals(sourceDir) ? "EXTRA_SRC_DIR_NO" : "EXTRA_SRC_DIR_YES", // NOI18N
                runAs != null ? runAs.name() : "", // NOI18N
                "1", // NOI18N
                (copyFiles != null && copyFiles) ? "COPY_FILES_YES" : "COPY_FILES_NO", // NOI18N
                PhpProjectUtils.getFrameworksForUsage(frameworkExtenders.keySet())));
    }

    /**
     * PHP project properties.
     */
    public static final class ProjectProperties {
        private File projectDirectory;
        private File sourcesDirectory;
        private String name;
        private RunAsType runAsType;
        private PhpVersion phpVersion;
        private Charset charset;
        private String url;
        private String indexFile;
        private WizardDescriptor descriptor;
        private Boolean copySources;
        private File copySourcesTarget;
        private Boolean copySourcesOnOpen;
        private RemoteConfiguration remoteConfiguration;
        private String remoteDirectory;
        private PhpProjectProperties.UploadFiles uploadFiles;
        private String hostname;
        private Integer port;
        private String router;
        private Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders; // for USAGES only
        private Boolean autoconfigured;

        public ProjectProperties() {
        }

        public ProjectProperties(ProjectProperties properties) {
            projectDirectory = properties.projectDirectory;
            sourcesDirectory = properties.sourcesDirectory;
            name = properties.name;
            runAsType = properties.runAsType;
            phpVersion = properties.phpVersion;
            charset = properties.charset;
            url = properties.url;
            indexFile = properties.indexFile;
            descriptor = properties.descriptor;
            copySources = properties.copySources;
            copySourcesTarget = properties.copySourcesTarget;
            copySourcesOnOpen = properties.copySourcesOnOpen;
            remoteConfiguration = properties.remoteConfiguration;
            remoteDirectory = properties.remoteDirectory;
            uploadFiles = properties.uploadFiles;
            hostname = properties.hostname;
            port = properties.port;
            router = properties.router;
            frameworkExtenders = properties.frameworkExtenders;
            autoconfigured = properties.autoconfigured;
        }

        public String getName() {
            return name;
        }

        public ProjectProperties setName(String name) {
            assert name != null;
            this.name = name;
            return this;
        }

        public File getSourcesDirectory() {
            return sourcesDirectory;
        }

        /**
         * @param sourcesDirectory source directory
         */
        public ProjectProperties setSourcesDirectory(File sourcesDirectory) {
            assert sourcesDirectory != null;
            this.sourcesDirectory = FileUtil.normalizeFile(sourcesDirectory);
            return this;
        }

        public File getProjectDirectory() {
            return projectDirectory;
        }

        /**
         * @param projectDirectory project directory, can be <code>null</code> (sourcesDirectory is used then)
         */
        public ProjectProperties setProjectDirectory(File projectDirectory) {
            if (projectDirectory != null) {
                projectDirectory = FileUtil.normalizeFile(projectDirectory);
            }
            this.projectDirectory = projectDirectory;
            return this;
        }

        public RunAsType getRunAsType() {
            return runAsType;
        }

        /**
         * @param runAsType run configuration type, can be <code>null</code>
         */
        public ProjectProperties setRunAsType(RunAsType runAsType) {
            this.runAsType = runAsType;
            return this;
        }

        public PhpVersion getPhpVersion() {
            return phpVersion;
        }

        /**
         * @param phpVersion PHP version
         */
        public ProjectProperties setPhpVersion(PhpVersion phpVersion) {
            assert phpVersion != null;
            this.phpVersion = phpVersion;
            return this;
        }

        public Charset getCharset() {
            return charset;
        }

        /**
         * @param charset project charset
         */
        public ProjectProperties setCharset(Charset charset) {
            assert charset != null;
            this.charset = charset;
            return this;
        }

        public String getUrl() {
            assert url != null;
            return url;
        }

        /**
         * @param url project URL
         */
        public ProjectProperties setUrl(String url) {
            this.url = url;
            return this;
        }

        public Boolean isCopySources() {
            return copySources;
        }

        /**
         * @param copySources <code>true</code> if copying sources is enabled, can be <code>null</code>
         */
        public ProjectProperties setCopySources(Boolean copySources) {
            this.copySources = copySources;
            return this;
        }

        public File getCopySourcesTarget() {
            return copySourcesTarget;
        }

        /**
         * @param copySourcesTarget target for source copying, can be <code>null</code>
         */
        public ProjectProperties setCopySourcesTarget(File copySourcesTarget) {
            if (copySourcesTarget != null) {
                copySourcesTarget = FileUtil.normalizeFile(copySourcesTarget);
            }
            this.copySourcesTarget = copySourcesTarget;
            return this;
        }

        public Boolean isCopySourcesOnOpen() {
            return copySourcesOnOpen;
        }

        /**
         * @param copySourcesOnOpen <code>true</code> if copying sources is enabled on project open, can be <code>null</code>
         */
        public ProjectProperties setCopySourcesOnOpen(Boolean copySourcesOnOpen) {
            this.copySourcesOnOpen = copySourcesOnOpen;
            return this;
        }

        public String getIndexFile() {
            return indexFile;
        }

        /**
         * @param indexFile index file, can be <code>null</code>
         */
        public ProjectProperties setIndexFile(String indexFile) {
            this.indexFile = indexFile;
            return this;
        }

        /**
         * @return needed for template, can be {@code null} if template is not needed
         */
        public WizardDescriptor getDescriptor() {
            return descriptor;
        }

        /**
         * @param descriptor wizard descriptor (used for getting index file template only!)
         */
        public ProjectProperties setDescriptor(WizardDescriptor descriptor) {
            assert descriptor != null;
            this.descriptor = descriptor;
            return this;
        }

        public RemoteConfiguration getRemoteConfiguration() {
            return remoteConfiguration;
        }

        /**
         * @param remoteConfiguration remote server configuration, can be <code>null</code>
         */
        public ProjectProperties setRemoteConfiguration(RemoteConfiguration remoteConfiguration) {
            this.remoteConfiguration = remoteConfiguration;
            return this;
        }

        public String getRemoteDirectory() {
            return remoteDirectory;
        }

        /**
         * @param remoteDirectory upload directory, can be <code>null</code>
         */
        public ProjectProperties setRemoteDirectory(String remoteDirectory) {
            this.remoteDirectory = remoteDirectory;
            return this;
        }

        public UploadFiles getUploadFiles() {
            return uploadFiles;
        }

        /**
         * @param uploadFiles upload files mode, can be <code>null</code>
         */
        public ProjectProperties setUploadFiles(UploadFiles uploadFiles) {
            this.uploadFiles = uploadFiles;
            return this;
        }

        public String getHostname() {
            return hostname;
        }

        /**
         * @param hostname hostname, can be <code>null</code>
         */
        public ProjectProperties setHostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Integer getPort() {
            return port;
        }

        /**
         * @param port port, can be <code>null</code>
         */
        public ProjectProperties setPort(Integer port) {
            this.port = port;
            return this;
        }

        public String getRouter() {
            return router;
        }

        /**
         * @param router hostname, can be <code>null</code>
         */
        public ProjectProperties setRouter(String router) {
            this.router = router;
            return this;
        }

        public Map<PhpFrameworkProvider, PhpModuleExtender> getFrameworkExtenders() {
            if (frameworkExtenders == null) {
                return Collections.emptyMap();
            }
            return frameworkExtenders;
        }

        /**
         * <b>! USED ONLY FOR NB USAGES !</b>
         * @param frameworkExtenders frameworks, empty map for no frameworks
         */
        public ProjectProperties setFrameworkExtenders(Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders) {
            assert frameworkExtenders != null;
            this.frameworkExtenders = frameworkExtenders;
            return this;
        }

        public Boolean getAutoconfigured() {
            return autoconfigured;
        }

        public ProjectProperties setAutoconfigured(Boolean autoconfigured) {
            this.autoconfigured = autoconfigured;
            return this;
        }

    }

    public interface Monitor {
        void starting();
        void creatingIndexFile();
        void finishing();
    }
}
