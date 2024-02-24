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
package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.api.EditableManifest;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleType;
import org.netbeans.modules.apisupport.project.ProjectXMLManager.CyclicDependencyException;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.DependencyListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.FriendListModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.PublicPackagesTableModel;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerComponentFactory.RequiredTokenListModel;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.w3c.dom.Element;

/**
 * Provides convenient access to a lot of NetBeans Module's properties.
 *
 * @author Martin Krauskopf
 */
public final class SingleModuleProperties extends ModuleProperties {

    private static final String[] IDE_TOKENS = new String[]{
        "org.openide.modules.os.Windows", // NOI18N
        "org.openide.modules.os.Unix", // NOI18N
        "org.openide.modules.os.Linux", // NOI18N
        "org.openide.modules.os.MacOSX", // NOI18N
        "org.openide.modules.os.PlainUnix", // NOI18N
        "org.openide.modules.os.OS2", // NOI18N
        "org.openide.modules.jre.JavaFX", // NOI18N
    };
    // property keys for project.properties
    public static final String BUILD_COMPILER_DEBUG = "build.compiler.debug"; // NOI18N
    public static final String BUILD_COMPILER_DEPRECATION = "build.compiler.deprecation"; // NOI18N
// XXX unused   public static final String CLUSTER_DIR = "cluster.dir"; // NOI18N
    public static final String IS_AUTOLOAD = "is.autoload"; // NOI18N
    public static final String IS_EAGER = "is.eager"; // NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVADOC_TITLE = "javadoc.title"; // NOI18N
    public static final String LICENSE_FILE = "license.file"; // NOI18N
    public static final String NBM_HOMEPAGE = "nbm.homepage"; // NOI18N
    public static final String NBM_MODULE_AUTHOR = "nbm.module.author"; // NOI18N
    public static final String NBM_NEEDS_RESTART = "nbm.needs.restart"; // NOI18N
    public static final String SPEC_VERSION_BASE = "spec.version.base"; // NOI18N
    /** @see "#66278" */
    public static final String JAVAC_COMPILERARGS = "javac.compilerargs"; // NOI18N
    private static final Map<String, String> DEFAULTS;

    private static final Logger LOG = Logger.getLogger(SingleModuleProperties.class.getName());
    private boolean majorReleaseVersionChanged;
    private boolean specificationVersionChanged;
    private boolean implementationVersionChange;
    private boolean providedTokensChanged;
    private boolean autoUpdateShowInClientChanged;
    private boolean hasExcludedModules;

    static {
        // setup defaults
        Map<String, String> map = new HashMap<String, String>();
        map.put(BUILD_COMPILER_DEBUG, "true"); // NOI18N
        map.put(BUILD_COMPILER_DEPRECATION, "true"); // NOI18N
        map.put(IS_AUTOLOAD, "false"); // NOI18N
        map.put(IS_EAGER, "false"); // NOI18N
        map.put(JAVAC_SOURCE, "1.4"); // NOI18N
        map.put(NBM_NEEDS_RESTART, "false"); // NOI18N
        DEFAULTS = Collections.unmodifiableMap(map);
    }
    // helpers for storing and retrieving real values currently stored on the disk
    private NbModuleType moduleType;
    private SuiteProvider suiteProvider;
    private ProjectXMLManager projectXMLManager;
    private final LocalizedBundleInfo.Provider bundleInfoProvider;
    private LocalizedBundleInfo bundleInfo;
    // keeps current state of the user changes
    private String majorReleaseVersion;
    private String specificationVersion;
    private String implementationVersion;
    private String provTokensString;
    private SortedSet<String> requiredTokens;
    private Boolean autoUpdateShowInClient;
    private NbPlatform activePlatform;
    private NbPlatform originalPlatform;
    private JavaPlatform activeJavaPlatform;
    private boolean javaPlatformChanged; // #115989
    /** package name / selected */
    private SortedSet<String> availablePublicPackages;
    private String[] allTokens;
    /** Unmodifiable sorted set of all categories in the module's universe. */
    private SortedSet<String> modCategories;
    /** Unmodifiable sorted set of all dependencies in the module's universe. */
    private Set<ModuleDependency> universeDependencies;
    // models
    private PublicPackagesTableModel publicPackagesModel;
    private DependencyListModel dependencyListModel;
    private FriendListModel friendListModel;
    private RequiredTokenListModel requiredTokensListModel;
    private DefaultListModel wrappedJarsListModel;
    private boolean wrappedJarsChanged; // #171125
    public static final String NB_PLATFORM_PROPERTY = "nbPlatform"; // NOI18N
    public static final String DEPENDENCIES_PROPERTY = "moduleDependencies"; // NOI18N

    /**
     * Returns an instance of SingleModuleProperties for the given project.
     */
    public static SingleModuleProperties getInstance(final NbModuleProject project) {
        SuiteProvider sp = project.getLookup().lookup(SuiteProvider.class);
        return new SingleModuleProperties(project.getHelper(), project.evaluator(), sp, project.getModuleType(),
                project.getLookup().lookup(LocalizedBundleInfo.Provider.class));
    }
    private ReferenceHelper refHelper;
    private UpdateHelper updHelper;
    private ClassPathSupport cps;

    /**
     * Creates a new instance of SingleModuleProperties
     */
    SingleModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator,
            SuiteProvider sp, NbModuleType moduleType,
            LocalizedBundleInfo.Provider bundleInfoProvider) {
        // XXX consider SingleModuleProperties(NbModuleProject) constructor. Life would be easier.
        super(helper, evaluator);
        this.bundleInfoProvider = bundleInfoProvider;
        this.hasExcludedModules = false;
        refresh(moduleType, sp);
    }

    protected void refresh(NbModuleType moduleType,
            SuiteProvider suiteProvider) {
        reloadProperties();
        // reset
        this.suiteProvider = suiteProvider;
        this.moduleType = moduleType;
        universeDependencies = null;
        modCategories = null;
        availablePublicPackages = null;
        dependencyListModel = null;
        friendListModel = null;
        requiredTokensListModel = null;
        wrappedJarsListModel = null;
        wrappedJarsChanged = false;
        projectXMLManager = null;
        if (isSuiteComponent()) {
            if (getSuiteDirectory() != null) {
                ModuleList.refreshModuleListForRoot(getSuiteDirectory());
            }
        } else if (isStandalone()) {
            ModuleList.refreshModuleListForRoot(getProjectDirectoryFile());
        }
        ManifestManager manifestManager = ManifestManager.getInstance(getManifestFile(), false);
        majorReleaseVersion = manifestManager.getReleaseVersion();
        specificationVersion = manifestManager.getSpecificationVersion();
        implementationVersion = manifestManager.getImplementationVersion();
        provTokensString = manifestManager.getProvidedTokensString();
        autoUpdateShowInClient = manifestManager.getAutoUpdateShowInClient();

        String nbDestDirS = getEvaluator().getProperty(ModuleList.NETBEANS_DEST_DIR);
        LOG.log(Level.FINE, "Setting NBPlatform for module. '" + getCodeNameBase() + "' in dir '" + nbDestDirS + "'");
        if (nbDestDirS != null) {
            String harnessDir = getEvaluator().getProperty("harness.dir");
            NbPlatform plaf = NbPlatform.getPlatformByDestDir(getHelper().resolveFile(nbDestDirS), harnessDir != null ? getHelper().resolveFile(harnessDir) : null);
            if (!plaf.isValid()) { // #134492
                NbPlatform def = NbPlatform.getDefaultPlatform();
                if (def != null) {
                    LOG.log(Level.FINE, "Platform not found, switching to default ({0})", def.getDestDir());
                    plaf = def;
                }
            }
            originalPlatform = activePlatform = plaf;
        }
        activeJavaPlatform = getJavaPlatform();
        javaPlatformChanged = false;
        getPublicPackagesModel().reloadData(loadPublicPackages());
        requiredTokens = Collections.unmodifiableSortedSet(
                new TreeSet<String>(Arrays.asList(manifestManager.getRequiredTokens())));
        bundleInfo = bundleInfoProvider.getLocalizedBundleInfo();
        if (bundleInfo != null) {
            try {
                bundleInfo.reload();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
        firePropertiesRefreshed();
    }

    /**
     * Forces set of module deps returned from {@link #getUiverseDependencies(boolean)}
     * to be recomputed next time it is queried.
     */
    void resetUniverseDependencies() {
        universeDependencies = null;
    }

    Map<String, String> getDefaultValues() {
        return DEFAULTS;
    }

    LocalizedBundleInfo getBundleInfo() {
        return bundleInfo;
    }

    // ---- READ ONLY start
    /** Returns code name base of the module this instance managing. */
    String getCodeNameBase() {
        return getProjectXMLManager().getCodeNameBase();
    }

    String getJarFile() {
        String v = getEvaluator().evaluate("${cluster}/${module.jar}");
        return getHelper().resolveFile(v != null ? v : "unknown").getAbsolutePath(); // NOI18N
    }

    @CheckForNull String getSuiteDirectoryPath() {
        File d = getSuiteDirectory();
        return d != null ? d.getPath() : null;
    }

    @CheckForNull File getSuiteDirectory() {
        return suiteProvider != null ? suiteProvider.getSuiteDirectory() : null;
    }

    /** Call only for suite component modules. */
    @CheckForNull SuiteProject getSuite() {
        assert isSuiteComponent();
        SuiteProject suite = null;
        try {
            File dir = getSuiteDirectory();
            if (dir != null) {
                FileObject suiteDir = FileUtil.toFileObject(dir);
                if (suiteDir != null) {
                    suite = (SuiteProject) ProjectManager.getDefault().findProject(suiteDir);
                }
            }
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
        return suite;
    }

    // ---- READ ONLY end
    /** Check whether the active platform is valid. */
    boolean isActivePlatformValid() {
        NbPlatform plaf = getActivePlatform();
        return plaf == null || plaf.isValid();
    }

    /**
     * Returns currently set platform. i.e. platform set in the
     * <em>Libraries</em> panel. Note that it could be <code>null</code> for
     * NetBeans.org modules.
     */
    NbPlatform getActivePlatform() {
        if (moduleType != NbModuleType.NETBEANS_ORG && activePlatform == null) {
            ModuleProperties.reportLostPlatform(activePlatform);
            activePlatform = NbPlatform.getDefaultPlatform();
        }
        return activePlatform;
    }

    void setActivePlatform(NbPlatform newPlaf) {
        if (this.activePlatform != newPlaf) {
            NbPlatform oldPlaf = this.activePlatform;
            this.activePlatform = newPlaf;
            this.dependencyListModel = null;
            this.universeDependencies = null;
            this.modCategories = null;
            firePropertyChange(NB_PLATFORM_PROPERTY, oldPlaf, newPlaf);
        }
    }

    JavaPlatform getActiveJavaPlatform() {
        return activeJavaPlatform;
    }

    void setActiveJavaPlatform(JavaPlatform nue) {
        JavaPlatform old = activeJavaPlatform;
        if (nue != old) {
            activeJavaPlatform = nue;
            firePropertyChange(JAVA_PLATFORM_PROPERTY, old, nue);
            javaPlatformChanged = true;
        }
    }

    String getMajorReleaseVersion() {
        return majorReleaseVersion;
    }

    void setMajorReleaseVersion(String ver) {
        if (!Utilities.compareObjects(majorReleaseVersion, ver)) {
            majorReleaseVersion = ver;
            majorReleaseVersionChanged = true;
        }
    }

    String getSpecificationVersion() {
        return specificationVersion;
    }

    void setSpecificationVersion(String ver) {
        if (!Utilities.compareObjects(specificationVersion, ver)) {
            specificationVersion = ver;
            specificationVersionChanged = true;
        }
    }

    String getImplementationVersion() {
        return implementationVersion;
    }

    void setImplementationVersion(String ver) {
        if (!Utilities.compareObjects(implementationVersion, ver)) {
            implementationVersion = ver;
            implementationVersionChange = true;
        }
    }

    String getProvidedTokens() {
        return provTokensString;
    }

    void setProvidedTokens(String tokens) {
        if (!Utilities.compareObjects(provTokensString, tokens)) {
            provTokensString = tokens;
            providedTokensChanged = true;
        }
    }

    public Boolean getAutoUpdateShowInClient() {
        return autoUpdateShowInClient;
    }

    public void setAutoUpdateShowInClient(Boolean autoUpdateShowInClient) {
        if (!Utilities.compareObjects(this.autoUpdateShowInClient, autoUpdateShowInClient)) {
            this.autoUpdateShowInClient = autoUpdateShowInClient;
            autoUpdateShowInClientChanged = true;
        }
    }

    boolean isStandalone() {
        return moduleType == NbModuleType.STANDALONE;
    }

    boolean isNetBeansOrg() {
        return moduleType == NbModuleType.NETBEANS_ORG;
    }

    boolean isSuiteComponent() {
        return moduleType == NbModuleType.SUITE_COMPONENT;
    }

    boolean dependingOnImplDependency() {
        DependencyListModel depsModel = getDependenciesListModel();
        if (depsModel == CustomizerComponentFactory.INVALID_DEP_LIST_MODEL) {
            return false;
        }
        Set<ModuleDependency> deps = depsModel.getDependencies();
        for (Iterator<ModuleDependency> it = deps.iterator(); it.hasNext();) {
            ModuleDependency dep = it.next();
            if (dep.hasImplementationDependency()) {
                return true;
            }
        }
        return false;
    }

    private ProjectXMLManager getProjectXMLManager() {
        if (projectXMLManager == null) {
            try {
                projectXMLManager = ProjectXMLManager.getInstance(getProjectDirectoryFile());
            } catch (IOException e) {
                assert false : e;
            }
        }
        return projectXMLManager;
    }

    ReferenceHelper getRefHelper() {
        if (refHelper == null) {
            Project p = getProject();
            AuxiliaryConfiguration aux = p != null ? ProjectUtils.getAuxiliaryConfiguration(p) : new AuxiliaryConfiguration() {
                @Override public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
                    return null;
                }
                @Override public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {}
                @Override public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
                    return false;
                }
            };
            refHelper = new ReferenceHelper(
                    getHelper(), aux,
                    getEvaluator());
        }
        return refHelper;
    }

    UpdateHelper getUpdateHelper() {
        if (updHelper == null) {
            updHelper = new UpdateHelper(new UpdateImplementation() {

                public boolean isCurrent() {
                    // XXX is metadata version update needed? Currently not supported here
                    return true;
                }

                public boolean canUpdate() {
                    assert false : "Should not get called";
                    return false;
                }

                public void saveUpdate(EditableProperties props) throws IOException {
                    assert false : "Should not get called";
                }

                public Element getUpdatedSharedConfigurationData() {
                    assert false : "Should not get called";
                    return null;
                }

                public EditableProperties getUpdatedProjectProperties() {
                    assert false : "Should not get called";
                    return null;
                }
            }, getHelper());
        }
        return updHelper;
    }

    DependencyListModel getDependenciesListModelInBg(final Runnable runAfterPopulated) {
        if (dependencyListModel == null) {
            if (isActivePlatformValid()) {
                // XXX wait for some time in AWT and perhaps fill model in the same event?
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        Runnable r;
                        try {
                            final SortedSet<ModuleDependency> deps = getProjectXMLManager().getDirectDependencies(getActivePlatform());
                            for (ModuleDependency dep : deps) {
                                dep.getModuleEntry().getLocalizedName(); // warm up
                            }
                            r = new Runnable() {

                                public void run() {
                                    dependencyListModel.setDependencies(deps);
                                    if (runAfterPopulated != null)
                                        runAfterPopulated.run();
                                }
                            };
                        } catch (IOException ioe) {
                            r = new Runnable() {

                                public void run() {
                                    dependencyListModel.setInvalid();
                                    if (runAfterPopulated != null)
                                        runAfterPopulated.run();
                                }
                            };
                        }
                        EventQueue.invokeLater(r);
                    }
                });
                dependencyListModel = DependencyListModel.createBgWaitModel(true);
                // add listener and fire DEPENDENCIES_PROPERTY when deps are changed
                dependencyListModel.addListDataListener(new ListDataListener() {
                    public void contentsChanged(ListDataEvent e) {
                        firePropertyChange(DEPENDENCIES_PROPERTY, null,
                                getDependenciesListModel());
                    }
                    public void intervalAdded(ListDataEvent e) {
                        contentsChanged(null);
                    }
                    public void intervalRemoved(ListDataEvent e) {
                        contentsChanged(null);
                    }
                });
            } else {
                dependencyListModel = CustomizerComponentFactory.getInvalidDependencyListModel();
                if (runAfterPopulated != null)
                    runAfterPopulated.run();
            }
        } else {
            if (runAfterPopulated != null)
                runAfterPopulated.run();
        }
        return dependencyListModel;
    }

    /**
     * Returns list model of module's dependencies regarding the currently
     * selected platform.
     */
    DependencyListModel getDependenciesListModel() {
        if (dependencyListModel == null) {
            if (isActivePlatformValid()) {
                try {
                    dependencyListModel = new DependencyListModel(
                            getProjectXMLManager().getDirectDependencies(getActivePlatform()), true);
                    // add listener and fire DEPENDENCIES_PROPERTY when deps are changed
                    dependencyListModel.addListDataListener(new ListDataListener() {

                        public void contentsChanged(ListDataEvent e) {
                            firePropertyChange(DEPENDENCIES_PROPERTY, null,
                                    getDependenciesListModel());
                        }

                        public void intervalAdded(ListDataEvent e) {
                            contentsChanged(null);
                        }

                        public void intervalRemoved(ListDataEvent e) {
                            contentsChanged(null);
                        }
                    });
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                    dependencyListModel = CustomizerComponentFactory.getInvalidDependencyListModel();
                }
            } else {
                dependencyListModel = CustomizerComponentFactory.getInvalidDependencyListModel();
            }
        }
        return dependencyListModel;
    }

    /**
     * Returns a set of available {@link ModuleDependency modules dependencies}
     * in the module's universe according to the currently selected {@link
     * #getActivePlatform() platform}.<p>
     *
     * <strong>Note:</strong> Don't call this method from EDT, since it may be
     * really slow. The {@link AssertionError} will be thrown if you try to do
     * so.
     *
     * @param filterExcludedModules if <code>true</code> and this module is a
     *        suite component, modules excluded from the suite's module list
     *        will be excluded from the returned set.
     * @param apiProvidersOnly if <code>true</code> only modules which provide
     *        public packages and have friendly relationship with this module
     *        will be included in the returned set
     */
    Set<ModuleDependency> getUniverseDependencies(
            final boolean filterExcludedModules, final boolean apiProvidersOnly) {
        assert !SwingUtilities.isEventDispatchThread() :
                "SingleModuleProperties.getUniverseDependencies() cannot be called from EDT"; // NOI18N
        if (universeDependencies == null) {
            reloadModuleListInfo();
        }
        if (universeDependencies == null) {
            // Broken platform.
            return Collections.emptySet();
        }
        Set<ModuleDependency> result = new HashSet<ModuleDependency>(universeDependencies);
        if (filterExcludedModules && isSuiteComponent()) {
            SuiteProject suite = getSuite();
            if (suite == null) {
                DialogDisplayer.getDefault().notify(new Message(NbBundle.getMessage(SingleModuleProperties.class,
                        "SingleModuleProperties.incorrectSuite", getSuiteDirectoryPath(), getProjectDisplayName()),
                        Message.WARNING_MESSAGE));
                return Collections.emptySet();
            }
            
            String[] disabledModules = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_MODULES_PROPERTY);
            String[] enabledClusters = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.ENABLED_CLUSTERS_PROPERTY);
            String[] disabledClusters = SuiteProperties.getArrayProperty(
                    suite.getEvaluator(), SuiteProperties.DISABLED_CLUSTERS_PROPERTY);
            String suiteClusterProp = getEvaluator().getProperty("cluster"); // NOI18N
            File suiteClusterDir = suiteClusterProp != null ? getHelper().resolveFile(suiteClusterProp) : null;
            for (Iterator<ModuleDependency> it = result.iterator(); it.hasNext();) {
                ModuleDependency dep = it.next();
                ModuleEntry me = dep.getModuleEntry();
                if (me.getClusterDirectory().equals(suiteClusterDir)) {
                    // #72124: do not filter other modules in the same suite.
                    continue;
                }
                if (isExcluded(me, Arrays.asList(disabledModules), Arrays.asList(enabledClusters), Arrays.asList(disabledClusters))) {
                    this.hasExcludedModules = true;
                    it.remove();
                }
            }
        }
        else if(!filterExcludedModules && isSuiteComponent())
        {
            SuiteProject suite = getSuite();
            if (suite == null) {
                DialogDisplayer.getDefault().notify(new Message(NbBundle.getMessage(SingleModuleProperties.class,
                        "SingleModuleProperties.incorrectSuite", getSuiteDirectoryPath(), getProjectDisplayName()),
                        Message.WARNING_MESSAGE));
                return Collections.emptySet();
            }
            Set<NbModuleProject> subModules = SuiteUtils.getSubProjects(suite);
            SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                                        suite.getEvaluator(), subModules);
            if(suiteProps.getActivePlatform() != null)
            {
                for(ModuleEntry entryIter:suiteProps.getActivePlatform().getModules())
                {
                    result.add(new ModuleDependency(entryIter));
                }
            }
        }
        if (apiProvidersOnly) { // remove module without public/friend API
            for (Iterator<ModuleDependency> it = result.iterator(); it.hasNext();) {
                ModuleDependency dep = it.next();
                ModuleEntry me = dep.getModuleEntry();
                if (me.getPublicPackages().length == 0 || !me.isDeclaredAsFriend(getCodeNameBase())) {
                    it.remove();
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns whether a set of available {@link ModuleDependency modules dependencies}
     * in the module's universe according to the currently selected {@link
     * #getActivePlatform() platform} has at least one excluded module.<p>
     * 
     */
    public boolean isHasExcludedModules() {
        return hasExcludedModules;
    }

    

    /**
     * Delegates to {@link #getUniverseDependencies(boolean, boolean)} with
     * <code>false</code> as a second parameter.
     */
    Set<ModuleDependency> getUniverseDependencies(final boolean filterExcludedModules) {
        return getUniverseDependencies(filterExcludedModules, false);
    }

    public static boolean isExcluded(ModuleEntry me, Collection<String> disabledModules, Collection<String> enabledClusters, Collection<String> disabledClusters) {
        if (disabledModules.contains(me.getCodeNameBase())) {
            return true;
        }
        String clusterName = me.getClusterDirectory().getName();
        if (!enabledClusters.isEmpty() && !clusterMatch(enabledClusters, clusterName)) {
            return true;
        }
        if (enabledClusters.isEmpty() && disabledClusters.contains(clusterName)) {
            return true;
        }
        return false;
    }

    static boolean clusterMatch(Collection<String> enabledClusters, String clusterName) { // #73706
        String baseName = clusterBaseName(clusterName);
        for (String c : enabledClusters) {
            if (clusterBaseName(c).equals(baseName)) {
                return true;
            }
        }
        return false;
    }

    static String clusterBaseName(String clusterName) {
        // when changing, change also org.netbeans.nbbuild.ModuleSelector
        return clusterName.replaceFirst("[0-9.]+$", ""); // NOI18N
    }

    /**
     * Returns sorted arrays of CNBs of available friends for this module.
     */
    String[] getAvailableFriends() {
        SortedSet<String> set = new TreeSet<String>();
        if (isSuiteComponent()) {
            for (Iterator<NbModuleProject> it = SuiteUtils.getSubProjects(getSuite()).iterator(); it.hasNext();) {
                Project prj = it.next();
                String cnb = ProjectUtils.getInformation(prj).getName();
                if (!getCodeNameBase().equals(cnb)) {
                    set.add(cnb);
                }
            }
        } else if (isNetBeansOrg()) {
            Set<ModuleDependency> deps = getUniverseDependencies(false);
            for (Iterator<ModuleDependency> it = deps.iterator(); it.hasNext();) {
                ModuleDependency dep = it.next();
                set.add(dep.getModuleEntry().getCodeNameBase());
            }
        } // else standalone module - leave empty (see the UI spec)
        return set.toArray(new String[0]);
    }

    FriendListModel getFriendListModel() {
        if (friendListModel == null) {
            friendListModel = new FriendListModel(getProjectXMLManager().getFriends());
        }
        return friendListModel;
    }

    RequiredTokenListModel getRequiredTokenListModel() {
        if (requiredTokensListModel == null) {
            requiredTokensListModel = new RequiredTokenListModel(requiredTokens);
        }
        return requiredTokensListModel;
    }
    private final String CPEXT = "CPEXT";    // NOI18N  // just an arbitrary string

    private ClassPathSupport getClassPathSupport() {
        if (cps == null) {
            ClassPathSupport.Callback cback = new ClassPathSupport.Callback() {

                public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
                    if (wrappedJarsChanged && CPEXT.equals(projectXMLElement)) {
                        for (Item item : items) {
                            // file ref properties for <cp-e> jars so that CPS can handle src&javadoc
                            String ref = getRefHelper().createForeignFileReferenceAsIs(item.getFilePath(), null);
                            item.setReference(ref);
                            item.initSourceAndJavadoc(getHelper());
                        }
                    }
                }

                public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
                
                }
            };
            cps = new ClassPathSupport(getEvaluator(), getRefHelper(), getHelper(), getUpdateHelper(), cback);
        }
        return cps;
    }

    private Iterator<Item> getCPExtIterator() {
        StringBuilder sb = new StringBuilder();
        String[] cpExt = SuiteUtils.getAntProperty(Arrays.asList(getProjectXMLManager().getBinaryOrigins()));
        for (String s : cpExt) {
            sb.append(s);
        }
        List<Item> items = getClassPathSupport().itemsList(sb.toString(), CPEXT);
        for (Item item : items) {
            if(item.getResolvedFile() != null) {
                item.setReference("${file.reference." + item.getResolvedFile().getName() + "}");
                if(getEvaluator().getProperty("source.reference." + item.getResolvedFile().getName()) != null) 
                    item.setSourceFilePath(getEvaluator().getProperty("source.reference." + item.getResolvedFile().getName()));
                if(getEvaluator().getProperty("javadoc.reference." + item.getResolvedFile().getName()) != null) 
                    item.setJavadocFilePath(getEvaluator().getProperty("javadoc.reference." + item.getResolvedFile().getName()));
            }
        }
        return items.iterator();
    }

    DefaultListModel getWrappedJarsListModel() {
        if (wrappedJarsListModel == null) {
            wrappedJarsListModel = ClassPathUiSupport.createListModel(getCPExtIterator());
            wrappedJarsListModel.addListDataListener(new ListDataListener() {
                private void process(ListDataEvent e) {
                    wrappedJarsChanged = true;
                    availablePublicPackages = null;
                    if (publicPackagesModel != null) {
                        publicPackagesModel.reloadData(loadPublicPackages(publicPackagesModel.getSelectedPackages()));
                    }
                }
                public void intervalAdded(ListDataEvent e) {
                    process(e);
                }
                public void intervalRemoved(ListDataEvent e) {
                    process(e);
                }
                public void contentsChanged(ListDataEvent e) {
                    process(e);
                }
            });
        }
        return wrappedJarsListModel;
    }

    // XXX should be probably moved into ModuleList
    String[] getAllTokens() {
        if (allTokens == null) {
            try {
                SortedSet<String> provTokens = new TreeSet<String>();
                provTokens.addAll(Arrays.asList(IDE_TOKENS));
                for (ModuleEntry me : getModuleList().getAllEntries()) {
                    provTokens.addAll(Arrays.asList(me.getProvidedTokens()));
                }
                String[] result = new String[provTokens.size()];
                return provTokens.toArray(result);
            } catch (IOException e) {
                allTokens = new String[0];
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return allTokens;
    }

    static boolean isBuiltInToken(String token) {
        for (String t : IDE_TOKENS) {
            if (t.equals(token)) {
                return true;
            }
        }
        return false;
    }

    PublicPackagesTableModel getPublicPackagesModel() {
        if (publicPackagesModel == null) {
            publicPackagesModel = new PublicPackagesTableModel(loadPublicPackages());
        }
        return publicPackagesModel;
    }

    /** Loads a map of package -&gt; isSelected entries.
     * Available packages are loaded from currently wrapped JARs (via {@link #getWrappedJarsListModel()})
     * plus project sources, selected packages are loaded from <tt>project.xml</tt>.
     * @see #loadAvailablePackages()
     * @return Map of package -&gt; isSelected entries
     */
    private Map<String, Boolean> loadPublicPackages() {
        return loadPublicPackages(getSelectedPackages());
    }

    private Map<String, Boolean> loadPublicPackages(Collection<String> selectedPackages) {
        Map<String, Boolean> publicPackages = new TreeMap<String, Boolean>();
        for (Iterator<String> it = getAvailablePublicPackages().iterator(); it.hasNext();) {
            String pkg = it.next();
            publicPackages.put(pkg, Boolean.valueOf(selectedPackages.contains(pkg)));
        }
        return publicPackages;
    }

    private Collection<String> getSelectedPackages() {
        Collection<String> sPackages = new HashSet<String>();
        ManifestManager.PackageExport[] pexports = getProjectXMLManager().getPublicPackages();
        for (int i = 0; i < pexports.length; i++) {
            ManifestManager.PackageExport pexport = pexports[i];
            if (pexport.isRecursive()) {
                for (Iterator<String> it = getAvailablePublicPackages().iterator(); it.hasNext(); ) {
                    String p = it.next();
                    if (p.startsWith(pexport.getPackage())) {
                        sPackages.add(p);
                    }
                }
            } else {
                sPackages.add(pexport.getPackage());
            }
        }
        return sPackages;
    }

    /**
     * Returns set of all available public packages for the project.
     */
    Set<String> getAvailablePublicPackages() {
        if (availablePublicPackages == null) {
            availablePublicPackages = ApisupportAntUtils.scanProjectForPackageNames(getProjectDirectoryFile(), false);
            // #66188: add c-p-e packages from ClassPathSupport so that they can be updated while customizer is open
            for (Iterator<Item> it = ClassPathUiSupport.getIterator(getWrappedJarsListModel());
                    it.hasNext();) {
                Item item = it.next();
                ApisupportAntUtils.scanJarForPackageNames(availablePublicPackages, item.getResolvedFile());
            }
        }
        return availablePublicPackages;
    }

    @Override
    void storeProperties() throws IOException {
        super.storeProperties();

        // Store chnages in manifest
        storeManifestChanges();

        // store localized info
        if (bundleInfo != null && bundleInfo.isModified()) {
            bundleInfo.store();
        } // XXX else ignore for now but we could save into some default location

        ProjectXMLManager pxm = getProjectXMLManager();

        // Store project.xml changes
        // store module dependencies
        DependencyListModel dependencyModel = getDependenciesListModel();
        if (dependencyModel.isChanged()) {
            Set<ModuleDependency> depsToSave = new TreeSet<ModuleDependency>(dependencyModel.getDependencies());

            logNetBeansAPIUsage("DEPENDENCIES", dependencyModel.getDependencies()); // NOI18N

            try {
                pxm.replaceDependencies(depsToSave);
            } catch (CyclicDependencyException ex) {
                throw new IOException(ex);
            }
        }
        Set<String> friends = getFriendListModel().getFriends();
        Set<String> publicPkgs = getPublicPackagesModel().getSelectedPackages();
        boolean refreshModuleList = false;
        if (getPublicPackagesModel().isChanged() || getFriendListModel().isChanged()) {
            if (friends.size() > 0) { // store friends packages
                pxm.replaceFriends(friends, publicPkgs);
            } else { // store public packages
                pxm.replacePublicPackages(publicPkgs);
            }
            refreshModuleList = true;
        }

        // store class-path-extensions + its src & javadoc
        if (cps != null && wrappedJarsListModel != null && wrappedJarsChanged) {
            final List<Item> cpExtList = ClassPathUiSupport.getList(wrappedJarsListModel);
            Map<String, String> newCpExt = new HashMap<String, String>();

            for (Item item : cpExtList) {
                String binPath = item.getFilePath();
                if (binPath != null) {
                    FileObject fo = FileUtil.toFileObject(PropertyUtils.resolveFile(getProjectDirectoryFile(), binPath));
                    if(fo != null)
                    {
                        String runtimePath = ApisupportAntUtils.CPEXT_RUNTIME_RELATIVE_PATH + fo.getNameExt();
                        newCpExt.put(runtimePath, binPath);
                    }
                }
            }

            // delete removed JARs, remove any remaining exported packages and src&javadoc refs left
            Iterator<Item> it = getCPExtIterator();
            HashSet<String> jarsSet = new HashSet<String>(newCpExt.values());
            while (it.hasNext()) {
                Item item = it.next();
                if (!jarsSet.contains(item.getFilePath())) {
                    // XXX deleting here doesn't work on Windows: 
//                    File f = PropertyUtils.resolveFile(getProjectDirectoryFile(), item.getFilePath());
//                    FileObject toDel = FileUtil.toFileObject(f);
//                    if (toDel != null) {
//                        toDel.delete();
//                    }
                    assert item.getReference() != null : "getCPExtIterator() initializes references to wrapped JARs";
                    item.removeSourceAndJavadoc(getUpdateHelper());
                    getRefHelper().destroyReference(item.getReference());
                }
            }
            cps.encodeToStrings(cpExtList, CPEXT);
            pxm.replaceClassPathExtensions(newCpExt);
            wrappedJarsChanged = false;
        }

        if (isStandalone()) {
            ModuleProperties.storePlatform(getHelper(), getActivePlatform());
            if (javaPlatformChanged) {
                ModuleProperties.storeJavaPlatform(getHelper(), getEvaluator(), getActiveJavaPlatform(), false);
            }
            if (refreshModuleList) {
                ModuleList.refreshModuleListForRoot(getProjectDirectoryFile());
            }
        } else if (isSuiteComponent() && refreshModuleList) {
            ModuleList.refreshModuleListForRoot(getSuiteDirectory());
        } else if (isNetBeansOrg()) {
            if (javaPlatformChanged) {
                ModuleProperties.storeJavaPlatform(getHelper(), getEvaluator(), getActiveJavaPlatform(), true);
            }
            if (refreshModuleList) {
                ModuleList.refreshModuleListForRoot(ModuleList.findNetBeansOrg(getProjectDirectoryFile()));
            }
        }
    }
    /** UI Logger for apisupport */
    static final Logger UI_LOG = Logger.getLogger("org.netbeans.ui.apisupport"); // NOI18N

    /** Sends info to UI handler about NetBeans APIs in use
     */
    private static void logNetBeansAPIUsage(String msg, Collection<ModuleDependency> deps) {
        List<String> cnbs = new ArrayList<String>();
        for (ModuleDependency moduleDependency : deps) {
            String cnb = moduleDependency.getModuleEntry().getCodeNameBase();
            // observe just NetBeans API module usage
            if (cnb.startsWith("org.openide") || cnb.startsWith("org.netbeans")) { // NOI18N
                cnbs.add(cnb);
            }
        }

        if (cnbs.isEmpty()) {
            return;
        }

        LogRecord rec = new LogRecord(Level.CONFIG, msg);
        rec.setParameters(cnbs.toArray(new String[0]));
        rec.setResourceBundleName(SingleModuleProperties.class.getPackage().getName() + ".Bundle"); // NOI18N
        rec.setResourceBundle(NbBundle.getBundle(SingleModuleProperties.class));
        rec.setLoggerName(UI_LOG.getName());
        UI_LOG.log(rec);
    }

    /**
     * Store appropriately properties regarding the manifest file.
     */
    private void storeManifestChanges() throws IOException {
        FileObject manifestFO = FileUtil.toFileObject(getManifestFile());
        EditableManifest em;
        if (manifestFO != null) {
            em = Util.loadManifest(manifestFO);
        } else { // manifest doesn't exist yet
            em = new EditableManifest();
            manifestFO = FileUtil.createData(
                    getHelper().getProjectDirectory(), "manifest.mf"); // NOI18N
        }
        boolean changed = false;
        if (majorReleaseVersionChanged) {
            String module = "".equals(getMajorReleaseVersion()) ? getCodeNameBase() : getCodeNameBase() + '/' + getMajorReleaseVersion();
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE, module);
            changed = true;
        }
        if (specificationVersionChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION,
                    getSpecificationVersion());
            changed = true;
        }
        if (implementationVersionChange) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_IMPLEMENTATION_VERSION,
                    getImplementationVersion());
            changed = true;
        }
        if (providedTokensChanged) {
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_PROVIDES,
                    getProvidedTokens());
            changed = true;
        }
        if (getRequiredTokenListModel().isChanged()) {
            String[] reqTokens = getRequiredTokenListModel().getTokens();
            StringBuilder result = new StringBuilder(reqTokens.length > 1 ? "\n  " : ""); // NOI18N
            for (int i = 0; i < reqTokens.length; i++) {
                if (i != 0) {
                    result.append(",\n  "); // NOI18N
                }
                result.append(reqTokens[i]);
            }
            setManifestAttribute(em, ManifestManager.OPENIDE_MODULE_REQUIRES, result.toString());
            changed = true;
        }
        if (autoUpdateShowInClientChanged) {
            setManifestAttribute(em, ManifestManager.AUTO_UPDATE_SHOW_IN_CLIENT, autoUpdateShowInClient != null ? autoUpdateShowInClient.toString() : "");
            changed = true;
        }
        if (changed) {
            Util.storeManifest(manifestFO, em);
        }
    }

    // XXX should be something similar provided be EditableManifest?
    private void setManifestAttribute(EditableManifest em, String key, String value) {
        boolean isOSGi = em.getAttribute(ManifestManager.BUNDLE_SYMBOLIC_NAME, null) != null;
        if (isOSGi) {
            if (ManifestManager.OPENIDE_MODULE.equals(key)) {
                key = ManifestManager.BUNDLE_SYMBOLIC_NAME;
            }
            if (ManifestManager.OPENIDE_MODULE_SPECIFICATION_VERSION.equals(key)) {
                key = ManifestManager.BUNDLE_VERSION;
            }
        }

        assert value != null;
        if ("".equals(value)) {
            if (em.getAttribute(key, null) != null) {
                em.removeAttribute(key, null);
            }
        } else {
            em.setAttribute(key, value, null);
        }
    }

    // package provide for unit test
    File getManifestFile() {
        String v = getEvaluator().getProperty("manifest.mf");
        return getHelper().resolveFile(v != null ? v : "unknown");
    }

    /**
     * Returns a set of all available categories in the module's universe
     * according to the currently selected platform ({@link
     * #getActivePlatform()})<p>
     * <strong>Note:</strong> Don't call this method from EDT, since it may be
     * really slow. The {@link AssertionError} will be thrown if you try to do
     * so.
     */
    SortedSet<String> getModuleCategories() {
        assert !SwingUtilities.isEventDispatchThread() :
                "SingleModuleProperties.getModuleCategories() cannot be called from EDT"; // NOI18N
        if (modCategories == null && !reloadModuleListInfo()) {
            return new TreeSet<String>();
        }
        return modCategories;
    }

    /**
     * Prepare all ModuleDependencies from this module's universe. Also prepare
     * all categories. <strong>Package-private only for unit tests.</strong>
     */
    boolean reloadModuleListInfo() {
        assert !SwingUtilities.isEventDispatchThread() :
                "SingleModuleProperties.reloadModuleListInfo() cannot be called from EDT"; // NOI18N
        if (isActivePlatformValid()) {
            try {
                SortedSet<String> allCategories = new TreeSet<String>(Collator.getInstance());
                Set<ModuleDependency> allDependencies = new HashSet<ModuleDependency>();
                for (ModuleEntry me : getModuleList().getAllEntries()) {
                    if (!me.getCodeNameBase().equals(getCodeNameBase())) {
                        allDependencies.add(new ModuleDependency(me));
                    }
                    String cat = me.getCategory();
                    if (cat != null) {
                        allCategories.add(cat);
                    }
                }
                modCategories = Collections.unmodifiableSortedSet(allCategories);
                universeDependencies = Collections.unmodifiableSet(allDependencies);
                return true;
            } catch (IOException ioe) {
                LOG.log(Level.INFO, "#213110: broken module/suite metadata?", ioe);
            }
        }
        return false;
    }

    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage. <strong>Package-private only for unit tests.</strong>
     */
    ModuleList getModuleList() throws IOException {
        if (getActivePlatform() != this.originalPlatform) {
            try {
                return ModuleList.getModuleList(getProjectDirectoryFile(), getActivePlatform().getDestDir());
            } catch (IOException x) {
                // #69029: maybe invalidated platform? Try the default platform instead.
                LOG.log(Level.FINE, null, x);
                NbPlatform p = NbPlatform.getDefaultPlatform();
                return ModuleList.getModuleList(getProjectDirectoryFile(), p != null ? p.getDestDir() : null);
            }
        } else {
            return ModuleList.getModuleList(getProjectDirectoryFile());
        }
    }

    /**
     * Just use a combination of evaluator and resolver. May return
     * <code>null</code> if evaluating fails.
     */
    File evaluateFile(final String currentLicence) {
        String evaluated = getEvaluator().evaluate(currentLicence);
        return evaluated == null ? null : getHelper().resolveFile(evaluated);
    }

    @CheckForNull Project getProject() {
        try {
            return ProjectManager.getDefault().findProject(getHelper().getProjectDirectory());
        } catch (IOException e) {
            LOG.log(Level.INFO, null, e);
            return null;
        }
    }

    /**
     * Adds all packages from given JARs to {@link #getPublicPackagesModel()}.
     * Does not store to project.xml
     * @param jars
     * @return Number of actually added packages.
     */
    int exportPackagesFromJars(List<File> jars) {
        PublicPackagesTableModel model = getPublicPackagesModel();
        Set<String> pkgs = new HashSet<String>(model.getSelectedPackages());
        int origC = pkgs.size();
        for (File jar : jars) {
            ApisupportAntUtils.scanJarForPackageNames(pkgs, jar);
        }
        model.reloadData(loadPublicPackages(pkgs));
        return pkgs.size() - origC;
    }

    boolean isOSGi() {
        FileObject manifestFO = FileUtil.toFileObject(getManifestFile());
        if (manifestFO == null) {
            return false;
        }
        EditableManifest em;
        try {
            em = Util.loadManifest(manifestFO);
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
            return false;
        }
        return em.getAttribute(ManifestManager.BUNDLE_SYMBOLIC_NAME, null) != null;
    }

}
