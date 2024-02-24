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
package org.netbeans.modules.php.project;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.search.SearchRoot;
import org.netbeans.api.search.SearchScopeOptions;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.api.search.provider.SearchListener;
import org.netbeans.modules.php.api.documentation.PhpDocumentations;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.annotations.ProjectUserAnnotationsProvider;
import org.netbeans.modules.php.project.api.PhpSeleniumProvider;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.php.project.classpath.IncludePathClassPathProvider;
import org.netbeans.modules.php.project.copysupport.CopySupport;
import org.netbeans.modules.php.project.internalserver.InternalWebServer;
import org.netbeans.modules.php.project.problems.ProjectPropertiesProblemProvider;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.actions.support.DebugStarterFactory;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.IgnorePathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.project.util.UsageLogging;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleIgnoredFilesExtender;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.modules.php.spi.testing.PhpTestingProviders;
import org.netbeans.modules.web.browser.api.BrowserSupport;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.browser.spi.PageInspectorCustomizer;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProvider;
import org.netbeans.modules.web.clientproject.api.jstesting.JsTestingProviders;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.modules.web.common.spi.ServerURLMappingImplementation;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchInfoDefinitionFactory;
import org.netbeans.spi.search.SubTreeSearchOptions;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads, Tomas Mysik
 */
@AntBasedProjectRegistration(
    type=PhpProjectType.TYPE,
    iconResource=PhpProject.PROJECT_ICON,
    sharedNamespace=PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=PhpProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class PhpProject implements Project {

    static final Logger LOGGER = Logger.getLogger(PhpProject.class.getName());

    @StaticResource
    public static final String PROJECT_ICON = "org/netbeans/modules/php/project/ui/resources/phpProject.png"; // NOI18N

    final AntProjectHelper helper;
    final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final SourceRoots seleniumRoots;

    private final SearchFilterDefinition searchFilterDef = new PhpSearchFilterDef();

    // ok to read it more times
    volatile FileObject webRootDirectory;

    volatile String name;
    private final AntProjectListener phpAntProjectListener = new PhpAntProjectListener();
    private final PropertyChangeListener projectPropertiesListener = new ProjectPropertiesListener();

    // @GuardedBy("ProjectManager.mutex() & ignoredFoldersLock") #211924
    Set<BasePathSupport.Item> ignoredFolders;
    final Object ignoredFoldersLock = new Object();
    // changes in ignored files - special case because of PhpVisibilityQuery
    final ChangeSupport ignoredFoldersChangeSupport = new ChangeSupport(this);

    final Frameworks frameworks;
    final TestingProviders testingProviders;
    private final ChangeListener frameworksListener;

    // FS changes
    private final SourceDirectoryFileChangeListener sourceDirectoryFileChangeListener = new SourceDirectoryFileChangeListener();

    // project's property changes
    public static final String PROP_FRAMEWORKS = "frameworks"; // NOI18N
    public static final String PROP_WEB_ROOT = "webRoot"; // NOI18N
    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final Set<PropertyChangeListener> propertyChangeListeners = new WeakSet<>();

    // css preprocessors
    final CssPreprocessorsListener cssPreprocessorsListener = new CssPreprocessorsListener() {
        @Override
        public void preprocessorsChanged() {
            // noop?
        }
        @Override
        public void optionsChanged(CssPreprocessor cssPreprocessor) {
            recompileSources(cssPreprocessor);
        }
        @Override
        public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
            if (project.equals(PhpProject.this)) {
                recompileSources(cssPreprocessor);
            }
        }
        @Override
        public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
            // noop
        }
    };

    // #233052
    private final WindowSystemListener windowSystemListener = new WindowSystemListener() {

        @Override
        public void beforeLoad(WindowSystemEvent event) {
        }

        @Override
        public void afterLoad(WindowSystemEvent event) {
        }

        @Override
        public void beforeSave(WindowSystemEvent event) {
            // browser
            getLookup().lookup(ClientSideDevelopmentSupport.class).close();
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }

    };


    @NbBundle.Messages({
        "PhpProject.sourceRoots.sources=Source Files",
        "PhpProject.sourceRoots.tests=Test Files",
        "PhpProject.sourceRoots.selenium=Selenium Test Files",
    })
    public PhpProject(AntProjectHelper helper) {
        assert helper != null;

        this.helper = helper;
        updateHelper = new UpdateHelper(UpdateImplementation.NULL, helper);
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        sourceRoots = SourceRoots.Builder.create(updateHelper, eval, Bundle.PhpProject_sourceRoots_sources())
                .setProperties(PhpProjectProperties.SRC_DIR)
                .build();
        testRoots = SourceRoots.Builder.create(updateHelper, eval, Bundle.PhpProject_sourceRoots_tests())
                .setPropertyNumericPrefix(PhpProjectProperties.TEST_SRC_DIR)
                .setTests(true)
                .build();
        seleniumRoots = SourceRoots.Builder.create(updateHelper, eval, Bundle.PhpProject_sourceRoots_selenium())
                .setPropertyNumericPrefix(PhpProjectProperties.SELENIUM_SRC_DIR)
                .setTests(true)
                .build();

        PhpModuleImpl phpModule = new PhpModuleImpl(this);
        frameworks = new Frameworks(phpModule);
        testingProviders = TestingProviders.create(this);

        // lookup
        lookup = createLookup(configuration, phpModule);

        // listeners
        addWeakPropertyEvaluatorListener(projectPropertiesListener);
        helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, phpAntProjectListener, helper));
        sourceRoots.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                removeSourceDirListener();
                addSourceDirListener();
                resetFrameworks();
            }
        });
        frameworksListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fireFrameworksChange();
            }
        };
        frameworks.addChangeListener(WeakListeners.change(frameworksListener, frameworks));
        WindowManager windowManager = WindowManager.getDefault();
        windowManager.addWindowSystemListener(WeakListeners.create(WindowSystemListener.class, windowSystemListener, windowManager));
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    PropertyEvaluator getEvaluator() {
        return eval;
    }

    void addWeakPropertyEvaluatorListener(PropertyChangeListener listener) {
        eval.addPropertyChangeListener(WeakListeners.propertyChange(listener, eval));
    }

    void addWeakIgnoredFilesListener(ChangeListener listener) {
        ignoredFoldersChangeSupport.addChangeListener(WeakListeners.change(listener, ignoredFoldersChangeSupport));

        VisibilityQuery visibilityQuery = VisibilityQuery.getDefault();
        visibilityQuery.addChangeListener(WeakListeners.change(listener, visibilityQuery));
    }

    // add as a weak listener, only once
    boolean addWeakPropertyChangeListener(PropertyChangeListener listener) {
        if (!propertyChangeListeners.add(listener)) {
            // already added
            return false;
        }
        addPropertyChangeListener(WeakListeners.propertyChange(listener, propertyChangeSupport));
        return true;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public SearchFilterDefinition getSearchFilterDefinition() {
        return searchFilterDef;
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    @Override
    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    public SourceRoots getSourceRoots() {
        return sourceRoots;
    }

    public SourceRoots getTestRoots() {
        return testRoots;
    }

    public SourceRoots getSeleniumRoots() {
        return seleniumRoots;
    }

    @CheckForNull
    FileObject getSourcesDirectory() {
        for (FileObject root : sourceRoots.getRoots()) {
            // return the first one
            return root;
        }
        return null;
    }

    /**
     * @return tests directory or <code>null</code>
     */
    FileObject[] getTestsDirectories() {
        return testRoots.getRoots();
    }

    /**
     * @return selenium tests directory or <code>null</code>
     */
    FileObject[] getSeleniumDirectories() {
        return seleniumRoots.getRoots();
    }

    /**
     * @return selenium tests directory or <code>null</code>
     */
    FileObject getSeleniumDirectory() {
        for (FileObject root : seleniumRoots.getRoots()) {
            // return the first one
            return root;
        }
        return null;
    }

    /**
     * @return web root directory or sources directory if not set
     */
    FileObject getWebRootDirectory() {
        if (webRootDirectory == null) {
            webRootDirectory = resolveWebRootDirectory();
        }
        return webRootDirectory;
    }

    private FileObject resolveWebRootDirectory() {
        if (PhpProjectValidator.isFatallyBroken(this)) {
            // corrupted project
            return null;
        }
        FileObject sources = getSourcesDirectory();
        assert sources != null;
        String webRootProperty = eval.getProperty(PhpProjectProperties.WEB_ROOT);
        if (webRootProperty == null) {
            // web root directory not set, return sources
            return sources;
        }
        FileObject webRootDir = sources.getFileObject(webRootProperty);
        if (webRootDir != null) {
            return webRootDir;
        }
        LOGGER.log(Level.INFO, "Web root directory {0} not found for project {1}", new Object[] {webRootProperty, getName()});
        // web root directory not found, return sources
        return sources;
    }

    void addSourceDirListener() {
        FileObject sourcesDirectory = getSourcesDirectory();
        if (sourcesDirectory == null) {
            return;
        }
        if (sourcesDirectory.equals(sourceDirectoryFileChangeListener.getSourceDir())) {
            // already listening to this source dir
            // this usually happens for new project - property change is fired _before_ project open
            return;
        }
        synchronized (sourceDirectoryFileChangeListener) {
            sourceDirectoryFileChangeListener.setSourceDir(sourcesDirectory);
            FileUtil.addRecursiveListener(sourceDirectoryFileChangeListener, FileUtil.toFile(sourcesDirectory), new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return isVisible(pathname);
                }
            }, null);
        }
    }

    void removeSourceDirListener() {
        FileObject sourceDir = sourceDirectoryFileChangeListener.getSourceDir();
        if (sourceDir == null) {
            // not listening
            return;
        }
        synchronized (sourceDirectoryFileChangeListener) {
            try {
                FileUtil.removeRecursiveListener(sourceDirectoryFileChangeListener, FileUtil.toFile(sourceDir));
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.INFO, null, ex);
            } finally {
                sourceDirectoryFileChangeListener.setSourceDir(null);
            }
        }
    }

    void recompileSources(CssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        FileObject sourcesDirectory = getSourcesDirectory();
        if (sourcesDirectory == null) {
            return;
        }
        // force recompiling
        CssPreprocessors.getDefault().process(cssPreprocessor, this, sourcesDirectory);
    }

    public PhpModule getPhpModule() {
        PhpModule phpModule = getLookup().lookup(PhpModuleImpl.class);
        assert phpModule != null;
        return phpModule;
    }

    boolean isVisible(File file) {
        if (getIgnoredFiles().contains(file)) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    boolean isVisible(FileObject fileObject) {
        File file = FileUtil.toFile(fileObject);
        if (file == null) {
            if (getIgnoredFileObjects().contains(fileObject)) {
                return false;
            }
            return VisibilityQuery.getDefault().isVisible(fileObject);
        }
        return isVisible(file);
    }

    public Set<File> getIgnoredFiles() {
        Set<File> ignored = new HashSet<>();
        addIgnoredProjectFiles(ignored);
        addIgnoredFrameworkFiles(ignored);
        return ignored;
    }

    // #172139 caused NPE in GlobalVisibilityQueryImpl
    public Set<FileObject> getIgnoredFileObjects() {
        Set<FileObject> ignoredFileObjects = new HashSet<>();
        for (File file : getIgnoredFiles()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                ignoredFileObjects.add(fo);
            }
        }
        return ignoredFileObjects;
    }

    private void addIgnoredProjectFiles(final Set<File> ignored) {
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                synchronized (ignoredFoldersLock) {
                    if (ignoredFolders == null) {
                        ignoredFolders = resolveIgnoredFolders(PhpProjectProperties.IGNORE_PATH);
                    }
                    assert ignoredFolders != null : "Ignored folders cannot be null";

                    for (BasePathSupport.Item item : ignoredFolders) {
                        if (item.isBroken()) {
                            continue;
                        }
                        ignored.add(new File(item.getAbsoluteFilePath(helper.getProjectDirectory())));
                    }
                }
                return null;
            }
        });
    }

    private void resetIgnoredFolders() {
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                synchronized (ignoredFoldersLock) {
                    ignoredFolders = null;
                }
                return null;
            }
        });
    }

    private void addIgnoredFrameworkFiles(Set<File> ignored) {
        PhpModule phpModule = getPhpModule();
        for (PhpFrameworkProvider provider : getFrameworks()) {
            PhpModuleIgnoredFilesExtender ignoredFilesExtender = provider.getIgnoredFilesExtender(phpModule);
            if (ignoredFilesExtender == null) {
                continue;
            }
            for (File file : ignoredFilesExtender.getIgnoredFiles()) {
                assert file != null : "Ignored file = null found in " + provider.getIdentifier();
                assert file.isAbsolute() : "Not absolute file found in " + provider.getIdentifier();

                ignored.add(file);
            }
        }
    }

    // no need to cache it, it is called only for code analysis
    public Set<FileObject> getCodeAnalysisExcludeFileObjects() {
        Set<FileObject> excludedFileObjects = new HashSet<>();
        Set<BasePathSupport.Item> excluded = resolveIgnoredFolders(PhpProjectProperties.CODE_ANALYSIS_EXCLUDES);
        assert excluded != null : "Ignored folders cannot be null";
        for (BasePathSupport.Item item : excluded) {
            if (item.isBroken()) {
                continue;
            }
            File file = new File(item.getAbsoluteFilePath(helper.getProjectDirectory()));
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                excludedFileObjects.add(fo);
            }
        }
        return excludedFileObjects;
    }

    private Set<BasePathSupport.Item> resolveIgnoredFolders(String propertyName) {
        IgnorePathSupport ignorePathSupport = new IgnorePathSupport(eval, refHelper, helper);
        Set<BasePathSupport.Item> ignored = new HashSet<>();
        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Iterator<BasePathSupport.Item> itemsIterator = ignorePathSupport.itemsIterator(properties.getProperty(propertyName));
        while (itemsIterator.hasNext()) {
            ignored.add(itemsIterator.next());
        }
        return ignored;
    }

    public List<PhpFrameworkProvider> getFrameworks() {
        return frameworks.getFrameworks();
    }

    public void resetFrameworks() {
        List<PhpFrameworkProvider> oldFrameworkProviders = getFrameworks();
        frameworks.resetFrameworks();
        List<PhpFrameworkProvider> newFrameworkProviders = getFrameworks();
        if (!oldFrameworkProviders.equals(newFrameworkProviders)) {
            fireFrameworksChange();
        }
    }

    void fireFrameworksChange() {
        propertyChangeSupport.firePropertyChange(PROP_FRAMEWORKS, null, null);
        // #209206 - also, likely some files are newly hidden/visible
        fireIgnoredFilesChange();
    }

    public List<PhpTestingProvider> getTestingProviders() {
        return testingProviders.getTestingProviders();
    }

    public String getName() {
        if (name == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                @Override
                public Void run() {
                    Element data = getHelper().getPrimaryConfigurationData(true);
                    NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1
                                && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            name = ((Text) nl.item(0)).getNodeValue();
                        }
                    }
                    if (name == null) {
                        name = "???"; // NOI18N
                    }
                    return null;
                }
            });
        }
        assert name != null;
        return name;
    }

    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(
                            PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                getHelper().putPrimaryConfigurationData(data, true);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(getClass().getName());
        buffer.append(" [ project directory: ");
        buffer.append(getProjectDirectory());
        buffer.append(" ]");
        return buffer.toString();
    }

    public AntProjectHelper getHelper() {
        return helper;
    }

    public CopySupport getCopySupport() {
        return getLookup().lookup(CopySupport.class);
    }

    private Lookup createLookup(AuxiliaryConfiguration configuration, PhpModule phpModule) {
        PhpProjectEncodingQueryImpl phpProjectEncodingQueryImpl = new PhpProjectEncodingQueryImpl(getEvaluator());
        Lookup base = Lookups.fixed(new Object[] {
                this,
                CopySupport.getInstance(this),
                new SeleniumProvider(),
                new PhpCoverageProvider(this),
                new Info(),
                configuration,
                new PhpOpenedHook(),
                new PhpProjectXmlSavedHook(),
                new PhpActionProvider(this),
                new PhpConfigurationProvider(this),
                phpModule,
                PhpLanguagePropertiesAccessor.getDefault().createForProject(this),
                new PhpEditorExtender(this),
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                new ClassPathProviderImpl(this, getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                new PhpLogicalViewProvider(this),
                new CustomizerProviderImpl(this),
                PhpSharabilityQuery.create(helper, getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                LookupProviderSupport.createSharabilityQueryMerger(),
                new PhpProjectOperations(this) ,
                phpProjectEncodingQueryImpl,
                new CreateFromTemplateAttributesImpl(getHelper(), phpProjectEncodingQueryImpl),
                new PhpTemplates(),
                new PhpSources(this, getHelper(), getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                getHelper(),
                getEvaluator(),
                PhpSearchInfo.create(this),
                new PhpSubTreeSearchOptions(),
                new PhpTestingProvidersImpl(testingProviders),
                InternalWebServer.createForProject(this),
                CssPreprocessorsUI.getDefault().createProjectProblemsProvider(this),
                ProjectPropertiesProblemProvider.createForProject(this),
                UILookupMergerSupport.createProjectProblemsProviderMerger(),
                new ProjectWebRootProviderImpl(),
                ClientSideDevelopmentSupport.create(this),
                ProjectBrowserProviderImpl.create(this),
                new PhpVisibilityQuery.PhpVisibilityQueryImpl(this),
                new UsageLogging(),
                new ImportantFilesImpl(this),
                new ProjectUserAnnotationsProvider(this),
                // ?? getRefHelper()
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-php-project/Lookup"); // NOI18N
    }

    public ReferenceHelper getRefHelper() {
        return refHelper;
    }

    public void fireIgnoredFilesChange() {
        resetIgnoredFolders();
        ignoredFoldersChangeSupport.fireChange();
    }


    private final class Info implements ProjectInformation {

        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        @Override
        public String getDisplayName() {
            return PhpProject.this.getName();
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.image2Icon(ImageUtilities.loadImage(PROJECT_ICON));
        }

        @Override
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }

        @Override
        public Project getProject() {
            return PhpProject.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener  listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String prop) {
            propertyChangeSupport.firePropertyChange(prop , null, null);
        }

    }

    private final class PhpOpenedHook extends ProjectOpenedHook {
        @Override
        protected void projectOpened() {
            new ProjectUpgrader(PhpProject.this).upgrade();

            addSourceDirListener();
            CssPreprocessors.getDefault().addCssPreprocessorsListener(cssPreprocessorsListener);

            frameworks.projectOpened();

            // avoid slowness
            readFolders();
            getTestingProviders();
            PhpDocumentations.getDocumentations();

            getName();

            PhpOptions.getInstance().ensurePhpGlobalIncludePath();

            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            ClassPath[] bootClassPaths = cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.BOOT_CP, bootClassPaths);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.PROJECT_BOOT_CP, cpProvider.getProjectClassPaths(PhpSourcePath.PROJECT_BOOT_CP));
            GlobalPathRegistry.getDefault().register(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));
            for (ClassPath classPath : bootClassPaths) {
                IncludePathClassPathProvider.addProjectIncludePath(classPath);
            }

            // ensure that code coverage is initialized in case it's enabled...
            PhpCoverageProvider coverageProvider = getLookup().lookup(PhpCoverageProvider.class);
            if (coverageProvider.isEnabled()) {
                PhpCoverageProvider.notifyProjectOpened(PhpProject.this);
            }

            JsTestingProvider jsTestingProvider = JsTestingProviders.getDefault().getJsTestingProvider(PhpProject.this, false);
            if (jsTestingProvider != null) {
                jsTestingProvider.projectOpened(PhpProject.this);
            }

            // autoconfigured?
            checkAutoconfigured();

            // #187060 - exception in projectOpened => project IS NOT opened (so move it at the end of the hook)
            getCopySupport().projectOpened();

            // log usage
            PhpProjectUtils.logUsage(PhpProject.class, "USG_PROJECT_OPEN_PHP", Arrays.asList(PhpProjectUtils.getFrameworksForUsage(frameworks.getFrameworks()))); // NOI18N
            // #192386
            LOGGER.finest("PROJECT_OPENED_FINISHED");
        }

        @Override
        protected void projectClosed() {
            try {
                removeSourceDirListener();
                CssPreprocessors.getDefault().removeCssPreprocessorsListener(cssPreprocessorsListener);

                frameworks.projectClosed();

                ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
                ClassPath[] bootClassPaths = cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP);
                GlobalPathRegistry.getDefault().unregister(PhpSourcePath.BOOT_CP, bootClassPaths);
                GlobalPathRegistry.getDefault().unregister(PhpSourcePath.PROJECT_BOOT_CP, cpProvider.getProjectClassPaths(PhpSourcePath.PROJECT_BOOT_CP));
                GlobalPathRegistry.getDefault().unregister(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));
                for (ClassPath classPath : bootClassPaths) {
                    IncludePathClassPathProvider.removeProjectIncludePath(classPath);
                }

                // internal web server
                lookup.lookup(InternalWebServer.class).stop();

                // browser
                lookup.lookup(ClientSideDevelopmentSupport.class).close();

                JsTestingProvider jsTestingProvider = JsTestingProviders.getDefault().getJsTestingProvider(PhpProject.this, false);
                if (jsTestingProvider != null) {
                    jsTestingProvider.projectClosed(PhpProject.this);
                }

            } finally {
                // #187060 - exception in projectClosed => project IS closed (so do it in finally block)
                getCopySupport().projectClosed();
                // #192386
                LOGGER.finest("PROJECT_CLOSED_FINISHED");
            }
        }

        private void readFolders() {
            // #165494 - moved from projectClosed() to projectOpened()
            // clear references to ensure that all the dirs are read again
            webRootDirectory = null;
            resetIgnoredFolders();
            // read dirs
            getIgnoredFiles();
            getSourceRoots().getRoots();
            getTestRoots().getRoots();
            getSeleniumRoots().getRoots();
        }

        @NbBundle.Messages({
            "# {0} - project name",
            "PhpOpenedHook.notification.autoconfigured.title=Project {0} automatically configured",
            "PhpOpenedHook.notification.autoconfigured.details=Review and correct important project settings detected by the IDE.",
        })
        private void checkAutoconfigured() {
            PhpProjectProperties projectProperties = new PhpProjectProperties(PhpProject.this);
            if (projectProperties.isAutoconfigured()) {
                NotificationDisplayer.getDefault().notify(
                        Bundle.PhpOpenedHook_notification_autoconfigured_title(ProjectUtils.getInformation(PhpProject.this).getDisplayName()),
                        NotificationDisplayer.Priority.LOW.getIcon(),
                        Bundle.PhpOpenedHook_notification_autoconfigured_details(),
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                PhpProjectUtils.openCustomizer(PhpProject.this, CompositePanelProviderImpl.SOURCES);
                            }
                        },
                        NotificationDisplayer.Priority.LOW);
                projectProperties.setAutoconfigured(false);
                projectProperties.save();
            }
        }

    }

    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (PhpConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(PhpConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            }
            return PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap());
        }
    }

    public final class PhpProjectXmlSavedHook extends ProjectXmlSavedHook {

        @Override
        protected void projectXmlSaved() throws IOException {
            Info info = getLookup().lookup(Info.class);
            assert info != null;
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    private final class SeleniumProvider implements PhpSeleniumProvider {
        @Override
        public FileObject getTestDirectory(boolean showCustomizer) {
            return ProjectPropertiesSupport.getSeleniumDirectory(PhpProject.this, showCustomizer);
        }

        @Override
        public void runAllTests() {
            ConfigAction.get(ConfigAction.Type.SELENIUM, PhpProject.this).runProject();
        }

        @Override
        public boolean isSupportEnabled(FileObject[] activatedFOs) {
            if (activatedFOs.length == 0) {
                return false;
            }

            PhpProject onlyOneProjectAllowed = null;
            for (FileObject fileObj : activatedFOs) {
                if (fileObj == null) {
                    return false;
                }

                // only php files or folders allowed
                if (fileObj.isData() && !FileUtils.isPhpFile(fileObj)) {
                    return false;
                }

                PhpProject phpProject = PhpProjectUtils.getPhpProject(fileObj);
                if (phpProject == null) {
                    return false;
                }
                if (PhpProjectValidator.isFatallyBroken(phpProject)) {
                    return false;
                }
                if (onlyOneProjectAllowed == null) {
                    onlyOneProjectAllowed = phpProject;
                } else {
                    if (!onlyOneProjectAllowed.equals(phpProject)) {
                        // tests can be generated only for one project at one time
                        return false;
                    }
                }

                if (fileObj == phpProject.getProjectDirectory()) { // "Run Selenium Tests" action should be active for the project node
                    return true;
                }

                FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
                if (sources == null || sources.equals(fileObj)) {
                    return false;
                }

                if (!CommandUtils.isUnderSources(phpProject, fileObj)
                        || CommandUtils.isUnderTests(phpProject, fileObj, false)
                        || CommandUtils.isUnderSelenium(phpProject, fileObj, false)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public List<Object> getTestSourceRoots(Collection<SourceGroup> createdSourceRoots, FileObject refFileObject) {
            ArrayList<Object> folders = new ArrayList<>();
            Project p = FileOwnerQuery.getOwner(refFileObject);
            if (p instanceof PhpProject) {
                List<FileObject> seleniumDirectories = ProjectPropertiesSupport.getSeleniumDirectories((PhpProject) p, true);
                SourceGroup[] sourceGroups = PhpProjectUtils.getSourceGroups((PhpProject) p);
                for (SourceGroup sg : sourceGroups) {
                    if (!sg.contains(refFileObject)) {
                        if (seleniumDirectories.contains(sg.getRootFolder())) {
                            folders.add(sg);
                        }
                    }
                }
            }
            return folders;
        }
    }

    private final class ProjectPropertiesListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PhpProjectProperties.IGNORE_PATH.equals(propertyName)) {
                fireIgnoredFilesChange();
            } else if (PhpProjectProperties.WEB_ROOT.equals(propertyName)
                    || PhpProjectProperties.SRC_DIR.equals(propertyName)) {
                FileObject oldWebRoot = webRootDirectory;
                webRootDirectory = null;
                // useful since it fires changes with fileobjects -> client can better use it than "htdocs/web/" values
                propertyChangeSupport.firePropertyChange(PROP_WEB_ROOT, oldWebRoot, getWebRootDirectory());
            }
        }
    }

    private final class SourceDirectoryFileChangeListener implements FileChangeListener {

        private volatile FileObject sourceDir;


        @CheckForNull
        public FileObject getSourceDir() {
            return sourceDir;
        }

        public void setSourceDir(FileObject sourceDir) {
            this.sourceDir = sourceDir;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            FileObject file = fe.getFile();
            if (!isVisible(file)) {
                return;
            }
            frameworksReset(file);
            processChange(file);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            FileObject file = fe.getFile();
            if (!isVisible(file)) {
                return;
            }
            frameworksReset(file);
            browserReload(file);
            processChange(file);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject file = fe.getFile();
            if (!isVisible(file)) {
                return;
            }
            browserReload(file);
            processChange(file);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            FileObject file = fe.getFile();
            if (!isVisible(file)) {
                return;
            }
            frameworksReset(file);
            browserReload(file);
            processChange(file);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            FileObject file = fe.getFile();
            if (!isVisible(file)) {
                return;
            }
            frameworksReset(file);
            processChange(file, fe.getName(), fe.getExt());
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
        }

        private boolean isVisible(FileObject file) {
            return PhpProjectUtils.isVisible(PhpVisibilityQuery.forProject(PhpProject.this), file);
        }

        // if any direct child of source folder changes, reset frameworks (new framework can be found in project)
        private void frameworksReset(FileObject file) {
            FileObject sourcesDirectory = getSourcesDirectory();
            if (sourcesDirectory == null) {
                // corrupted project
                return;
            }
            if (file.getParent().equals(sourcesDirectory)) {
                LOGGER.fine("file change, frameworks back to null");
                resetFrameworks();
            }
        }

        // possible browser reload, if nb integration is present
        private void browserReload(FileObject file) {
            ClientSideDevelopmentSupport easelSupport = PhpProject.this.getLookup().lookup(ClientSideDevelopmentSupport.class);
            assert easelSupport != null;
            easelSupport.reload(file);
        }

        private void processChange(FileObject fileObject) {
            CssPreprocessors.getDefault().process(PhpProject.this, fileObject);
        }

        private void processChange(FileObject fileObject, String originalName, String originalExtension) {
            CssPreprocessors.getDefault().process(PhpProject.this, fileObject, originalName, originalExtension);
        }

    }

    private final class PhpAntProjectListener implements AntProjectListener {

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            name = null;
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
        }
    }

    private final class ProjectWebRootProviderImpl implements ProjectWebRootProvider {

        @Override
        public FileObject getWebRoot(FileObject file) {
            FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(PhpProject.this);
            if (webRoot == null) {
                return null;
            }
            if (webRoot.equals(file)
                    || FileUtil.isParentOf(webRoot, file)) {
                return webRoot;
            }
            return null;
        }

        @Override
        public Collection<FileObject> getWebRoots() {
            FileObject webRoot = ProjectPropertiesSupport.getWebRootDirectory(PhpProject.this);
            if (webRoot == null) {
                return Collections.emptyList();
            }
            return Collections.singleton(webRoot);
        }

    }

    private static final class PhpSearchInfo extends SearchInfoDefinition implements PropertyChangeListener {

        private static final Logger LOGGER = Logger.getLogger(PhpSearchInfo.class.getName());

        private final PhpProject project;
        // @GuardedBy(this)
        private SearchInfo delegate = null;

        private PhpSearchInfo(PhpProject project) {
            this.project = project;
        }

        public static SearchInfoDefinition create(PhpProject project) {
            PhpSearchInfo phpSearchInfo = new PhpSearchInfo(project);
            project.getSourceRoots().addPropertyChangeListener(phpSearchInfo);
            project.getTestRoots().addPropertyChangeListener(phpSearchInfo);
            project.getSeleniumRoots().addPropertyChangeListener(phpSearchInfo);
            return phpSearchInfo;
        }

        private SearchInfo createDelegate() {
            SearchInfo searchInfo = SearchInfoUtils.createSearchInfoForRoots(
                    getRoots(), false, project.getSearchFilterDefinition(),
                    SearchInfoDefinitionFactory.SHARABILITY_FILTER);
            return searchInfo;
        }

        @Override
        public boolean canSearch() {
            return true;
        }

        @Override
        public Iterator<FileObject> filesToSearch(
                SearchScopeOptions searchScopeOptions,
                SearchListener listener,
                AtomicBoolean terminated) {
            return getDelegate().getFilesToSearch(searchScopeOptions,
                    listener, terminated).iterator();
        }

        @Override
        public List<SearchRoot> getSearchRoots() {
            return getDelegate().getSearchRoots();
        }

        private FileObject[] getRoots() {
            List<FileObject> roots = new LinkedList<>();
            addRoots(roots, project.getSourceRoots());
            addRoots(roots, project.getTestRoots());
            addRoots(roots, project.getSeleniumRoots());
            addIncludePath(roots, PhpSourcePath.getIncludePath(project.getSourcesDirectory()));
            return roots.toArray(new FileObject[0]);
        }

        // #197968
        private void addRoots(List<FileObject> roots, SourceRoots sourceRoots) {
            for (FileObject root : sourceRoots.getRoots()) {
                if (!root.isFolder()) {
                    LOGGER.log(Level.WARNING, "Not folder {0} for source roots {1}", new Object[] {root, Arrays.toString(sourceRoots.getRootNames())});
                } else {
                    roots.add(root);
                }
            }
        }

        private void addIncludePath(List<FileObject> roots, List<FileObject> includePath) {
            for (FileObject folder : includePath) {
                if (!folder.isFolder()) {
                    LOGGER.log(Level.WARNING, "Not folder {0} for Include path {1}", new Object[] {folder, includePath});
                } else {
                    roots.add(folder);
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                synchronized (this) {
                    delegate = createDelegate();
                }
            }
        }

        /**
         * @return the delegate
         */
        private synchronized SearchInfo getDelegate() {
            if (delegate == null) {
                delegate = createDelegate();
            }
            return delegate;
        }
    }

    private final class PhpSearchFilterDef extends SearchFilterDefinition {

        @Override
        public boolean searchFile(FileObject file) {
            if (!file.isData()) {
                throw new IllegalArgumentException("File expected");
            }
            return PhpVisibilityQuery.forProject(PhpProject.this).isVisible(file);
        }

        @Override
        public FolderResult traverseFolder(FileObject folder) {
            if (!folder.isFolder()) {
                throw new IllegalArgumentException("Folder expected");
            }
            if (PhpVisibilityQuery.forProject(PhpProject.this).isVisible(folder)) {
                return FolderResult.TRAVERSE;
            }
            return FolderResult.DO_NOT_TRAVERSE;
        }

    }

    private final class PhpSubTreeSearchOptions extends SubTreeSearchOptions {

        private List<SearchFilterDefinition> filterList;

        public PhpSubTreeSearchOptions() {
            this.filterList = this.createList();
        }

        @Override
        public List<SearchFilterDefinition> getFilters() {
            return filterList;
        }

        private List<SearchFilterDefinition> createList() {
            List<SearchFilterDefinition> list = new ArrayList<>(2);
            list.add(getSearchFilterDefinition());
            list.add(SearchInfoDefinitionFactory.SHARABILITY_FILTER);
            return Collections.unmodifiableList(list);
        }
    }

    public static final class ClientSideDevelopmentSupport implements ServerURLMappingImplementation, PageInspectorCustomizer, PropertyChangeListener {

        private static final RequestProcessor RP = new RequestProcessor(ClientSideDevelopmentSupport.class);

        private final UsageLogger browserUsageLogger = UsageLogger.projectBrowserUsageLogger(PhpProjectUtils.USAGE_LOGGER_NAME);

        final PhpProject project;
        private final RequestProcessor.Task reloadTask;

        private volatile String projectRootUrl;
        private volatile String browserId;
        private volatile Boolean browserReloadOnSave = null;

        // @GuardedBy("this")
        private BrowserSupport browserSupport = null;
        // @GuardedBy("this")
        private boolean browserSupportInitialized = false;
        volatile JButton customizerButton = null;


        private ClientSideDevelopmentSupport(PhpProject project) {
            assert project != null;
            this.project = project;
            reloadTask = RP.create(new Runnable() {
                @Override
                public void run() {
                    reload();
                }
            });
        }

        public static ClientSideDevelopmentSupport create(PhpProject project) {
            ClientSideDevelopmentSupport serverMapping = new ClientSideDevelopmentSupport(project);
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, serverMapping);
            return serverMapping;
        }

        @Override
        public URL toServer(int projectContext, FileObject projectFile) {
            initProjectUrl();
            if (projectRootUrl == null) {
                return null;
            }
            FileObject webRoot = project.getWebRootDirectory();
            if (webRoot == null) {
                return null;
            }
            String relPath = FileUtil.getRelativePath(webRoot, projectFile);
            if (relPath == null) {
                return null;
            }
            try {
                URL u = new URL(projectRootUrl + relPath);
                WebBrowser browser = getWebBrowser();
                if (browser != null) {
                    u = browser.toBrowserURL(project, projectFile, u);
                }
                return u;
            } catch (MalformedURLException ex) {
                return null;
            }
        }

        @Override
        public FileObject fromServer(int projectContext, URL serverURL) {
            // #219339 - strip down query and/or fragment:
            serverURL = WebUtils.stringToUrl(WebUtils.urlToString(serverURL, true));
            if (serverURL == null) {
                return null;
            }

            initProjectUrl();
            if (projectRootUrl == null) {
                return null;
            }
            FileObject webRoot = project.getWebRootDirectory();
            if (webRoot == null) {
                return null;
            }
            WebBrowser browser = getWebBrowser();
            if (browser != null) {
                serverURL = browser.fromBrowserURL(project, serverURL);
            }
            String url = CommandUtils.urlToString(serverURL, true);
            if (url.startsWith(projectRootUrl)) {
                return webRoot.getFileObject(url.substring(projectRootUrl.length()));
            }
            return null;
        }

        @Override
        public boolean isHighlightSelectionEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            // noop
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            // noop
        }

        public void showFileUrl(URL url, FileObject file) {
            // let browser update URL if necessary:
            WebBrowser browser = getWebBrowser();
            if (browser != null) {
                url = browser.toBrowserURL(project, file, url);
            }
            BrowserSupport support = getBrowserSupport();
            if (support != null) {
                support.load(url, file);
            } else {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
            // usage logging
            if (browser == null) {
                // default ide browser
                browser = BrowserUISupport.getDefaultBrowserChoice(true);
            }
            browserUsageLogger.log(PhpProjectType.TYPE, browser.getId(), browser.getBrowserFamily().name());
        }

        private void initProjectUrl() {
            if (projectRootUrl == null) {
                projectRootUrl = getProjectRootUrl();
            }
        }

        private String getProjectRootUrl() {
            try {
                String url = CommandUtils.urlToString(CommandUtils.getBaseURL(project, true), true);
                if (!url.endsWith("/")) { // NOI18N
                    url += "/"; // NOI18N
                }
                return url;
            } catch (MalformedURLException ex) {
                return null;
            }
        }

        public boolean canReload(FileObject fo) {
            initBrowser();
            // #226389
            DebugStarter debugStarter = DebugStarterFactory.getInstance();
            if (debugStarter != null
                    && debugStarter.isAlreadyRunning()) {
                return false;
            }
            BrowserSupport support = getBrowserSupport();
            if (support == null || support.ignoreChange(fo)) {
                return false;
            }
            // #226256
            return browserReloadOnSave;
        }

        public void reload(FileObject file) {
            if (canReload(file)) {
                reloadTask.schedule(200);
            }
        }

        @NbBundle.Messages("ClientSideDevelopmentSupport.reload.copySupportRunning=Copy Support is still running - do you really want to reload the page?")
        void reload() {
            assert RP.isRequestProcessorThread();
            BrowserSupport support = getBrowserSupport();
            if (support == null) {
                return;
            }
            if (!support.isWebBrowserPaneOpen()) {
                return;
            }
            // #226884, 227281 - wait till copysupport finishes
            if (!project.getCopySupport().waitFinished(Bundle.ClientSideDevelopmentSupport_reload_copySupportRunning(), 5000, getCustomizerButton())) {
                return;
            }
            support.reload();
        }

        public void close() {
            BrowserSupport support = getBrowserSupport();
            if (support != null) {
                support.close(true);
            }
        }

        @NbBundle.Messages("ClientSideDevelopmentSupport.reload.customize=Customize...")
        private JButton getCustomizerButton() {
            if (customizerButton != null) {
                return customizerButton;
            }
            customizerButton = Mutex.EVENT.readAccess(new Mutex.Action<JButton>() {
                @Override
                public JButton run() {
                    assert EventQueue.isDispatchThread();
                    JButton button = new JButton(Bundle.ClientSideDevelopmentSupport_reload_customize());
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            PhpProjectUtils.openCustomizer(project, CompositePanelProviderImpl.BROWSER);
                        }
                    });
                    return button;
                }
            });
            return customizerButton;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (PhpProjectProperties.URL.equals(propertyName)) {
                projectRootUrl = null;
            } else if (PhpProjectProperties.BROWSER_ID.equals(propertyName)) {
                browserUsageLogger.reset();
                resetBrowser();
                resetBrowserSupport();
            } else if (PhpProjectProperties.BROWSER_RELOAD_ON_SAVE.equals(propertyName)) {
                resetBrowserReloadOnSave();
            }
        }

        private void initBrowser() {
            if (browserId == null) {
                browserId = project.getEvaluator().getProperty(PhpProjectProperties.BROWSER_ID);
            }
            if (browserReloadOnSave == null) {
                if (browserId == null) {
                    // default ide browser
                    browserReloadOnSave = false;
                } else {
                    WebBrowser browser = BrowserUISupport.getBrowser(browserId);
                    if (browser != null
                            && browser.hasNetBeansIntegration()) {
                        browserReloadOnSave = ProjectPropertiesSupport.getBrowserReloadOnSave(project);
                    } else {
                        browserReloadOnSave = false;
                    }
                }
            }
            assert browserReloadOnSave != null;
        }

        private void resetBrowser() {
            browserId = null;
            resetBrowserReloadOnSave();
        }

        private void resetBrowserReloadOnSave() {
            browserReloadOnSave = null;
        }

        private synchronized void resetBrowserSupport() {
            if (browserSupport != null) {
                browserSupport.close(false);
            }
            browserSupport = null;
            browserSupportInitialized = false;
        }

        private synchronized BrowserSupport getBrowserSupport() {
            if (browserSupportInitialized) {
                return browserSupport;
            }
            browserSupportInitialized = true;
            WebBrowser browser = getWebBrowser();
            if (browser == null) {
                browserSupport = null;
                return null;
            }
            browserSupport = BrowserSupport.create(browser);
            return browserSupport;
        }

        @CheckForNull
        private WebBrowser getWebBrowser() {
            initBrowser();
            if (browserId == null) {
                return null;
            }
            return BrowserUISupport.getBrowser(browserId);
        }

    }

    private static final class PhpTestingProvidersImpl implements PhpTestingProviders {

        private final TestingProviders testingProviders;


        public PhpTestingProvidersImpl(TestingProviders testingProviders) {
            assert testingProviders != null;
            this.testingProviders = testingProviders;
        }

        @Override
        public List<PhpTestingProvider> getEnabledTestingProviders() {
            return testingProviders.getTestingProviders();
        }

    }

}
