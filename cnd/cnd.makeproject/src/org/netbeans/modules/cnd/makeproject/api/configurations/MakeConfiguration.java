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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectCustomizer;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.configurations.ConfigurationMakefileWriter;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class MakeConfiguration extends Configuration implements Cloneable {

    public static final String NBPROJECT_FOLDER = "nbproject"; // NOI18N
    public static final String PRIVATE_FOLDER = "private"; // NOI18N
    public static final String NBPROJECT_PRIVATE_FOLDER = "nbproject/private"; // NOI18N
    public static final String PROJECT_XML = "project.xml"; // NOI18N
    public static final String CONFIGURATIONS_XML = "configurations.xml"; // NOI18N
    public static final String MAKEFILE_IMPL = "Makefile-impl.mk"; // NOI18N
    public static final String MAKEFILE_VARIABLES = "Makefile-variables.mk"; // NOI18N
    public static final String BUILD_FOLDER = "build"; // NOI18N
    public static final String DIST_FOLDER = "dist"; // NOI18N
    public static final String EXT_FOLDER = "_ext"; // NOI18N
    public static final String OBJECTDIR_MACRO_NAME = "OBJECTDIR"; // NOI18N
    public static final String OBJECTDIR_MACRO = "${" + OBJECTDIR_MACRO_NAME + "}"; // NOI18N
    public static final String CND_CONF_MACRO = "${CND_CONF}"; // NOI18N
    public static final String CND_PLATFORM_MACRO = "${CND_PLATFORM}"; // NOI18N
    public static final String CND_DISTDIR_MACRO = "${CND_DISTDIR}"; // NOI18N
    public static final String CND_BUILDDIR_MACRO = "${CND_BUILDDIR}"; // NOI18N
    public static final String CND_DLIB_EXT_MACRO = "${CND_DLIB_EXT}"; // NOI18N
    public static final String CND_OUTPUT_PATH_MACRO = "${OUTPUT_PATH}"; // NOI18N
    public static final String PROJECTDIR_MACRO = "${PROJECT_DIR}"; // NOI18N
    // Project Types
    private static final String[] TYPE_NAMES_UNMANAGED = {
        getString("MakefileName")
    };
    private static final String[] TYPE_NAMES_MANAGED = {
        getString("ApplicationName"),
        getString("DynamicLibraryName"),
        getString("StaticLibraryName"),};
    private static final String[] TYPE_NAMES_MANAGED_DB = {
        getString("DBApplicationName")
    };
    private static final String[] TYPE_NAMES_MANAGED_QT = {
        getString("QtApplicationName"),
        getString("QtDynamicLibraryName"),
        getString("QtStaticLibraryName")
    };
    
    private static final String[] TYPE_NAMES_CUSTOM = {
        "CUSTOM"                                        // <=== FIXUP // NOI18N
    };
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    public static final int TYPE_DB_APPLICATION = 7;
    public static final int TYPE_CUSTOM = 10;
    
    // Configurations
    private IntConfiguration configurationType;
    private PreBuildConfiguration preBuildConfiguration;
    private MakefileConfiguration makefileConfiguration;
    private CompileConfiguration compileConfiguration;
    private CompilerSet2Configuration compilerSet;
    private LanguageBooleanConfiguration cRequired;
    private LanguageBooleanConfiguration cppRequired;
    private LanguageBooleanConfiguration fortranRequired;
    private LanguageBooleanConfiguration assemblerRequired;
    private DevelopmentHostConfiguration developmentHost;
    private BooleanConfiguration dependencyChecking;
    private BooleanConfiguration rebuildPropChanged;
    private BooleanConfiguration prependToolCollectionPath;
    private CCompilerConfiguration cCompilerConfiguration;
    private CCCompilerConfiguration ccCompilerConfiguration;
    private FortranCompilerConfiguration fortranCompilerConfiguration;
    private AssemblerConfiguration assemblerConfiguration;
    private LinkerConfiguration linkerConfiguration;
    private ArchiverConfiguration archiverConfiguration;
    private PackagingConfiguration packagingConfiguration;
    private RequiredProjectsConfiguration requiredProjectsConfiguration;
    private DebuggerChooserConfiguration debuggerChooserConfiguration;
    private QmakeConfiguration qmakeConfiguration;
    private boolean languagesDirty = true;
    private RemoteSyncFactory fixedRemoteSyncFactory;
    private CodeAssistanceConfiguration codeAssistanceConfiguration;
    private BooleanConfiguration platformSpecificConfiguration;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    
    private String customizerId = null;

    private MakeConfiguration(FSPath fsPath, String name, int configurationTypeValue,
            String customizerId, String hostUID, CompilerSet hostCS,
            boolean defaultToolCollection, boolean platformSpecific) {
        super(fsPath, name);
        hostUID = (hostUID == null) ? CppUtils.getDefaultDevelopmentHost(fsPath.getFileSystem()) : hostUID;
        if (configurationTypeValue == TYPE_MAKEFILE) {
            configurationType = new IntConfiguration(null, configurationTypeValue, TYPE_NAMES_UNMANAGED, null);
        } else if (configurationTypeValue == TYPE_APPLICATION || configurationTypeValue == TYPE_DYNAMIC_LIB || configurationTypeValue == TYPE_STATIC_LIB) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED, null, TYPE_APPLICATION);
        } else if (configurationTypeValue == TYPE_DB_APPLICATION) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED_DB, null, TYPE_DB_APPLICATION);
        } else if (configurationTypeValue == TYPE_QT_APPLICATION || configurationTypeValue == TYPE_QT_DYNAMIC_LIB || configurationTypeValue == TYPE_QT_STATIC_LIB) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_MANAGED_QT, null, TYPE_QT_APPLICATION);
        } else if (configurationTypeValue == TYPE_CUSTOM) {
            configurationType = new ManagedIntConfiguration(null, configurationTypeValue, TYPE_NAMES_CUSTOM, null, TYPE_CUSTOM);
        } else {
            assert false;
        }
        setCustomizerId(customizerId);
        developmentHost = new DevelopmentHostConfiguration(ExecutionEnvironmentFactory.fromUniqueID(hostUID));
        if (defaultToolCollection) {
            compilerSet = new CompilerSet2Configuration(developmentHost);
        } else {
            CompilerSet defCS = (hostCS != null) ? hostCS : CompilerSetManager.get(developmentHost.getExecutionEnvironment()).getDefaultCompilerSet();
            compilerSet = new CompilerSet2Configuration(developmentHost, defCS);
        }
        cRequired = new LanguageBooleanConfiguration();
        cppRequired = new LanguageBooleanConfiguration();
        fortranRequired = new LanguageBooleanConfiguration();
        assemblerRequired = new LanguageBooleanConfiguration();
        preBuildConfiguration = new PreBuildConfiguration(this);
        makefileConfiguration = new MakefileConfiguration(this);
        compileConfiguration = new CompileConfiguration(this);
        if (isMakefileConfiguration()) {
            dependencyChecking = new BooleanConfiguration(false);
        } else {
            dependencyChecking = new BooleanConfiguration(true);
            dependencyChecking.setValue(MakeProjectOptions.getDepencyChecking());
        }
        rebuildPropChanged = new BooleanConfiguration(false);
        prependToolCollectionPath = new BooleanConfiguration(true);
        if (!isMakefileConfiguration()) {
            rebuildPropChanged.setValue(MakeProjectOptions.getRebuildPropChanged());
        }
        cCompilerConfiguration = new CCompilerConfiguration(fsPath.getPath(), null, this); //XXX:fullRemote:fileSystem - use FSPath
        ccCompilerConfiguration = new CCCompilerConfiguration(fsPath.getPath(), null, this); //XXX:fullRemote:fileSystem - use FSPath
        fortranCompilerConfiguration = new FortranCompilerConfiguration(fsPath.getPath(), null, this); //XXX:fullRemote:fileSystem - use FSPath
        assemblerConfiguration = new AssemblerConfiguration(fsPath.getPath(), null, this); //XXX:fullRemote:fileSystem - use FSPath
        linkerConfiguration = new LinkerConfiguration(this);
        archiverConfiguration = new ArchiverConfiguration(this);
        packagingConfiguration = new PackagingConfiguration(this);
        requiredProjectsConfiguration = new RequiredProjectsConfiguration();

        debuggerChooserConfiguration = new DebuggerChooserConfiguration(Lookup.EMPTY);
        qmakeConfiguration = new QmakeConfiguration(this);

        developmentHost.addPropertyChangeListener(compilerSet);
        codeAssistanceConfiguration = new CodeAssistanceConfiguration(this);
        platformSpecificConfiguration = new BooleanConfiguration(false);
        platformSpecificConfiguration.setValue(platformSpecific);
        initAuxObjects();
    }
    
    /**
     * Will create the configuration of type MakeConfiguration.TYPE_MAKEFILE, the configuration will be saved on the local machine
     * and will be created for default Build Host, which is returned by {@link #org.netbeans.modules.cnd.makeproject.configurations.CppUtils.getDefaultDevelopmentHost()} method
     * For full remote project should use {@link #createMakefileConfiguration(FSPath, String, String)}
     * @param baseDir path to the folder the configuration will be saved in
     * @param name the name of the configuration
     * @return the MakeConfiguration
     */
    public static MakeConfiguration createDefaultHostMakefileConfiguration(String baseDir, String name) {
        return new MakeConfiguration(new FSPath(CndFileUtils.getLocalFileSystem(), baseDir), name, MakeConfiguration.TYPE_MAKEFILE,
                null, null, null,
                true ,false);
    }
    public static MakeConfiguration createMakefileConfiguration(FSPath baseDir, String name, String hostID) {
        return new MakeConfiguration(baseDir, name, MakeConfiguration.TYPE_MAKEFILE,
                null, hostID, null,
                true, false);
    }
    
    public static MakeConfiguration createMakefileConfiguration(FSPath baseDir, String name,
            String hostID, CompilerSet hostCS,
            boolean defaultToolCollection) {
        return new MakeConfiguration(baseDir, name, MakeConfiguration.TYPE_MAKEFILE,
                null, hostID, hostCS,
                defaultToolCollection, false);
    }
    
    
    public static MakeConfiguration createConfiguration(FSPath baseDir, String name, int configurationType,
            String customizerID, String hostID) {
        return new MakeConfiguration(baseDir, name, configurationType,
                customizerID, hostID, null,
                true, false);
    }    

    public static MakeConfiguration createConfiguration(FSPath baseDir, String name, int configurationType,
            String customizerID, String hostID,
            boolean platformSpecific) {
        return new MakeConfiguration(baseDir, name, configurationType,
                customizerID, hostID, null,
                true, platformSpecific);
    }
    
    public static MakeConfiguration createConfiguration(FSPath baseDir, String name, int configurationType,
            String customizerID, String hostID, CompilerSet hostCS,
            boolean defaultToolCollection) {
        return new MakeConfiguration(baseDir, name, configurationType,
                customizerID, hostID, hostCS,
                defaultToolCollection, false);
    }        

    public void setPreBuildConfiguration(PreBuildConfiguration makefileConfiguration) {
        this.preBuildConfiguration = makefileConfiguration;
        this.preBuildConfiguration.setMakeConfiguration(this);
    }

    public PreBuildConfiguration getPreBuildConfiguration() {
        return preBuildConfiguration;
    }

    public void setMakefileConfiguration(MakefileConfiguration makefileConfiguration) {
        this.makefileConfiguration = makefileConfiguration;
        this.makefileConfiguration.setMakeConfiguration(this);
    }

    public MakefileConfiguration getMakefileConfiguration() {
        return makefileConfiguration;
    }

    public void setCompileConfiguration(CompileConfiguration compileConfiguration) {
        this.compileConfiguration = compileConfiguration;
        this.compileConfiguration.setMakeConfiguration(this);
    }

    public CompileConfiguration getCompileConfiguration() {
        return compileConfiguration;
    }

    public IntConfiguration getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(IntConfiguration configurationType) {
        this.configurationType = configurationType;
    }

    public BooleanConfiguration getDependencyChecking() {
        return dependencyChecking;
    }

    public void setDependencyChecking(BooleanConfiguration dependencyChecking) {
        this.dependencyChecking = dependencyChecking;
    }

    public BooleanConfiguration getRebuildPropChanged() {
        return rebuildPropChanged;
    }

    public void setRebuildPropChanged(BooleanConfiguration rebuildPropChanged) {
        this.rebuildPropChanged = rebuildPropChanged;
    }

    public BooleanConfiguration getPrependToolCollectionPath() {
        return prependToolCollectionPath;
    }

    public void setPrependToolCollectionPath(BooleanConfiguration prependToolCollectionPath) {
        this.prependToolCollectionPath = prependToolCollectionPath;
    }

    public CompilerSet2Configuration getCompilerSet() {
        return compilerSet;
    }

    public void setCompilerSet(CompilerSet2Configuration compilerSet) {
        this.compilerSet = compilerSet;
    }

    public LanguageBooleanConfiguration getCRequired() {
        return cRequired;
    }

    public LanguageBooleanConfiguration getCppRequired() {
        return cppRequired;
    }

    public LanguageBooleanConfiguration getFortranRequired() {
        return fortranRequired;
    }

    public void setCRequired(LanguageBooleanConfiguration cRequired) {
        this.cRequired = cRequired;
    }

    public void setCppRequired(LanguageBooleanConfiguration cppRequired) {
        this.cppRequired = cppRequired;
    }

    public void setFortranRequired(LanguageBooleanConfiguration fortranRequired) {
        this.fortranRequired = fortranRequired;
    }

    public LanguageBooleanConfiguration getAssemblerRequired() {
        return assemblerRequired;
    }

    public void setAssemblerRequired(LanguageBooleanConfiguration assemblerRequired) {
        this.assemblerRequired = assemblerRequired;
    }

    public BooleanConfiguration getPlatformSpecific() {
        return platformSpecificConfiguration;
    }

    public void setPlatformSpecificConfiguration(BooleanConfiguration platformSpecificConfiguration) {
        this.platformSpecificConfiguration = platformSpecificConfiguration;
    }

    public PlatformInfo getPlatformInfo() {
        PlatformInfo platformInfo = PlatformInfo.getDefault(getDevelopmentHost().getExecutionEnvironment());
//        assert platformInfo.getPlatform() == getPlatform().getValue();
        return platformInfo;

    }

    public DevelopmentHostConfiguration getDevelopmentHost() {
        return developmentHost;
    }

    public void setDevelopmentHost(DevelopmentHostConfiguration developmentHost) {
        this.developmentHost = developmentHost;
    }

    public boolean isApplicationConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_QT_APPLICATION:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isApplicationConfiguration();
            default:
                return false;
        }
    }

    public boolean isCompileConfiguration() {
//        return getConfigurationType().getValue() == TYPE_APPLICATION ||
//               getConfigurationType().getValue() == TYPE_DB_APPLICATION ||
//               getConfigurationType().getValue() == TYPE_DYNAMIC_LIB ||
//               getConfigurationType().getValue() == TYPE_STATIC_LIB ||
//               getConfigurationType().getValue() == TYPE_CUSTOM;    // <=== FIXUP
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_DYNAMIC_LIB:
            case TYPE_STATIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isCompileConfiguration();
            default:
                return false;
        }
    }

    public boolean isLibraryConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_DYNAMIC_LIB:
            case TYPE_STATIC_LIB:
            case TYPE_QT_DYNAMIC_LIB:
            case TYPE_QT_STATIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isLibraryConfiguration();
            default:
                return false;
        }
    }
    
    public boolean isCustomConfiguration() {
        return getConfigurationType().getValue() == TYPE_CUSTOM;
    }

    public boolean isLinkerConfiguration() {
//        return getConfigurationType().getValue() == TYPE_APPLICATION ||
//               getConfigurationType().getValue() == TYPE_DB_APPLICATION ||
//               getConfigurationType().getValue() == TYPE_DYNAMIC_LIB ||
//               getConfigurationType().getValue() == TYPE_CUSTOM;   // <=== FIXUP
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_DYNAMIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isLinkerConfiguration();
            default:
                return false;
        }
    }

    public final boolean isMakefileConfiguration() {
        return getConfigurationType().getValue() == TYPE_MAKEFILE;
    }

    public boolean isDynamicLibraryConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_DYNAMIC_LIB:
            case TYPE_QT_DYNAMIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isDynamicLibraryConfiguration();
            default:
                return false;
        }
    }

    public boolean isArchiverConfiguration() {
//        return getConfigurationType().getValue() == TYPE_STATIC_LIB;
        switch (getConfigurationType().getValue()) {
            case TYPE_STATIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isArchiverConfiguration();
            default:
                return false;
        }
    }

    public boolean isQmakeConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_QT_APPLICATION:
            case TYPE_QT_DYNAMIC_LIB:
            case TYPE_QT_STATIC_LIB:
                return true;
            default:
                return false;
        }
    }

    public final boolean isStandardManagedConfiguration() {
        switch (getConfigurationType().getValue()) {
            case TYPE_APPLICATION:
            case TYPE_DB_APPLICATION:
            case TYPE_DYNAMIC_LIB:
            case TYPE_STATIC_LIB:
                return true;
            case TYPE_CUSTOM:
                return getProjectCustomizer().isStandardManagedConfiguration();
            default:
                return false;
        }
    }

    public void setCCompilerConfiguration(CCompilerConfiguration cCompilerConfiguration) {
        this.cCompilerConfiguration = cCompilerConfiguration;
    }

    public CCompilerConfiguration getCCompilerConfiguration() {
        return cCompilerConfiguration;
    }

    public void setCCCompilerConfiguration(CCCompilerConfiguration ccCompilerConfiguration) {
        this.ccCompilerConfiguration = ccCompilerConfiguration;
    }

    public CCCompilerConfiguration getCCCompilerConfiguration() {
        return ccCompilerConfiguration;
    }

    public void setFortranCompilerConfiguration(FortranCompilerConfiguration fortranCompilerConfiguration) {
        this.fortranCompilerConfiguration = fortranCompilerConfiguration;
    }

    public FortranCompilerConfiguration getFortranCompilerConfiguration() {
        return fortranCompilerConfiguration;
    }

    public void setAssemblerConfiguration(AssemblerConfiguration assemblerConfiguration) {
        this.assemblerConfiguration = assemblerConfiguration;
    }

    public AssemblerConfiguration getAssemblerConfiguration() {
        return assemblerConfiguration;
    }

    public void setLinkerConfiguration(LinkerConfiguration linkerConfiguration) {
        this.linkerConfiguration = linkerConfiguration;
        this.linkerConfiguration.setMakeConfiguration(this);
    }

    public LinkerConfiguration getLinkerConfiguration() {
        return linkerConfiguration;
    }

    public void setArchiverConfiguration(ArchiverConfiguration archiverConfiguration) {
        this.archiverConfiguration = archiverConfiguration;
        this.archiverConfiguration.setMakeConfiguration(this);
    }

    public ArchiverConfiguration getArchiverConfiguration() {
        return archiverConfiguration;
    }

    public void setPackagingConfiguration(PackagingConfiguration packagingConfiguration) {
        this.packagingConfiguration = packagingConfiguration;
        this.packagingConfiguration.setMakeConfiguration(this);
    }

    public PackagingConfiguration getPackagingConfiguration() {
        return packagingConfiguration;
    }

    // LibrariesConfiguration
    public RequiredProjectsConfiguration getRequiredProjectsConfiguration() {
        return requiredProjectsConfiguration;
    }

    public void setRequiredProjectsConfiguration(RequiredProjectsConfiguration requiredProjectsConfiguration) {
        this.requiredProjectsConfiguration = requiredProjectsConfiguration;
    }

    public DebuggerChooserConfiguration getDebuggerChooserConfiguration() {
        return debuggerChooserConfiguration;
    }

    public void setDebuggerChooserConfiguration(DebuggerChooserConfiguration debuggerChooserConfiguration) {
        this.debuggerChooserConfiguration = debuggerChooserConfiguration;
    }

    public QmakeConfiguration getQmakeConfiguration() {
        return qmakeConfiguration;
    }

    public void setQmakeConfiguration(QmakeConfiguration qmakeConfiguration) {
        this.qmakeConfiguration = qmakeConfiguration;
    }

    @Override
    public void assign(Configuration conf) {
        MakeConfiguration makeConf = (MakeConfiguration) conf;
        setName(makeConf.getName());
        setBaseFSPath(makeConf.getBaseFSPath());
        getConfigurationType().assign(makeConf.getConfigurationType());
        getDevelopmentHost().assign(makeConf.getDevelopmentHost());
        fixedRemoteSyncFactory = makeConf.fixedRemoteSyncFactory;
        customizerId = makeConf.getCustomizerId();
        getCompilerSet().assign(makeConf.getCompilerSet());
        getCRequired().assign(makeConf.getCRequired());
        getCppRequired().assign(makeConf.getCppRequired());
        getFortranRequired().assign(makeConf.getFortranRequired());
        getAssemblerRequired().assign(makeConf.getAssemblerRequired());
        getDependencyChecking().assign(makeConf.getDependencyChecking());
        getRebuildPropChanged().assign(makeConf.getRebuildPropChanged());
        getPrependToolCollectionPath().assign(makeConf.getPrependToolCollectionPath());
        getPreBuildConfiguration().assign(makeConf.getPreBuildConfiguration());
        getMakefileConfiguration().assign(makeConf.getMakefileConfiguration());
        getCompileConfiguration().assign(makeConf.getCompileConfiguration());
        getCCompilerConfiguration().assign(makeConf.getCCompilerConfiguration());
        getCCompilerConfiguration().setOwner(makeConf);
        getCCCompilerConfiguration().assign(makeConf.getCCCompilerConfiguration());
        getCCCompilerConfiguration().setOwner(makeConf);
        getFortranCompilerConfiguration().assign(makeConf.getFortranCompilerConfiguration());
        getAssemblerConfiguration().assign(makeConf.getAssemblerConfiguration());
        getLinkerConfiguration().assign(makeConf.getLinkerConfiguration());
        getArchiverConfiguration().assign(makeConf.getArchiverConfiguration());
        getPackagingConfiguration().assign(makeConf.getPackagingConfiguration());
        getRequiredProjectsConfiguration().assign(makeConf.getRequiredProjectsConfiguration());
        getDebuggerChooserConfiguration().assign(makeConf.getDebuggerChooserConfiguration());
        getQmakeConfiguration().assign(makeConf.getQmakeConfiguration());
        getCodeAssistanceConfiguration().assign(makeConf.getCodeAssistanceConfiguration());
        getPlatformSpecific().assign(makeConf.getPlatformSpecific());

        // do assign on all aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects(); // from this profile
        //ConfigurationAuxObject[] p_auxs = conf.getAuxObjects(); // from the 'other' profile
        for (int i = 0; i < auxs.length; i++) {
            // unsafe using! suppose same set of objects and same object order
            String id = auxs[i].getId();
            ConfigurationAuxObject object = conf.getAuxObject(id);
            if (object != null) {
                // safe using
                auxs[i].assign(object);
            } else {
                System.err.println("Configuration - assign: Object ID " + id + " do not found"); // NOI18N
            }
        }
    }

    @Override
    public Configuration cloneConf() {
        return (Configuration) clone();
    }

    /**
     * Make a copy of configuration requested from Project Properties
     * @return Copy of configuration
     */
    @Override
    public Configuration copy() {
        MakeConfiguration copy = 
                MakeConfiguration.createConfiguration(getBaseFSPath(), getName(), getConfigurationType().getValue(), 
                getCustomizerId(), ExecutionEnvironmentFactory.toUniqueID(developmentHost.getExecutionEnvironment()));
        copy.assign(this);
        // copy aux objects
        ConfigurationAuxObject[] auxs = getAuxObjects();
        List<ConfigurationAuxObject> copiedAuxs = new ArrayList<>();
        for (int i = 0; i < auxs.length; i++) {
            if (auxs[i] instanceof ItemConfiguration) {
                copiedAuxs.add(((ItemConfiguration) auxs[i]).copy(copy));
            } else if (auxs[i] instanceof FolderConfiguration) {
                copiedAuxs.add(((FolderConfiguration) auxs[i]).copy(copy));
            } else {
                String id = auxs[i].getId();
                ConfigurationAuxObject copyAux = copy.getAuxObject(id);
                if (copyAux != null) {
                    copyAux.assign(auxs[i]);
                    copiedAuxs.add(copyAux);
                } else {
                    copiedAuxs.add(auxs[i]);
                }
            }
        }
        copy.setAuxObjects(copiedAuxs);
        // Fixup folder and item configuration links links
        fixupMasterLinks(copy);

        return copy;
    }

    private void fixupMasterLinks(MakeConfiguration makeConf) {
        FileObject projectDirFO = getBaseFSPath().getFileObject();
        Project project = null;
        try {
            if (projectDirFO != null && projectDirFO.isValid()) {
                project = ProjectManager.getDefault().findProject(projectDirFO);
            }
        } catch (IOException ioe) {
            // Error
            return;
        }
        if (project == null) {
            return; // IZ 172628 (basedir is a valid directory but doesn't contain a project!)
        }
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (!pdp.gotDescriptor()) {
            return;
        }
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();

        Folder root = makeConfigurationDescriptor.getLogicalFolders();
        fixupFolderItemLinks(makeConf, root, makeConf.getCCompilerConfiguration(), makeConf.getCCCompilerConfiguration());
    }

    private void fixupFolderItemLinks(MakeConfiguration makeConf, Folder folder, BasicCompilerConfiguration cCompilerConf, BasicCompilerConfiguration ccCompilerConf) {
        if (!folder.isProjectFiles()) {
            return;
        }
        FolderConfiguration folderConfiguration = (FolderConfiguration) makeConf.getAuxObject(folder.getId());
        if (folderConfiguration == null) {
            return;
        }
        if (folderConfiguration.getCCompilerConfiguration() != null) {
            folderConfiguration.getCCompilerConfiguration().setMaster(cCompilerConf);
        }
        if (folderConfiguration.getCCCompilerConfiguration() != null) {
            folderConfiguration.getCCCompilerConfiguration().setMaster(ccCompilerConf);
        }
        for (Item item : folder.getItemsAsArray()) {
            ItemConfiguration itemConfiguration = (ItemConfiguration) makeConf.getAuxObject(item.getId());
            if (itemConfiguration != null) {
                if (itemConfiguration.getCCompilerConfiguration() != null) {
                    itemConfiguration.getCCompilerConfiguration().setMaster(folderConfiguration.getCCompilerConfiguration());
                    itemConfiguration.getCCompilerConfiguration().fixupMasterLinks(makeConf.getCCompilerConfiguration());
                }
                if (itemConfiguration.getCCCompilerConfiguration() != null) {
                    itemConfiguration.getCCCompilerConfiguration().setMaster(folderConfiguration.getCCCompilerConfiguration());
                    itemConfiguration.getCCCompilerConfiguration().fixupMasterLinks(makeConf.getCCCompilerConfiguration());
                }
            }
        }
        for (Folder subfolder : folder.getFoldersAsArray()) {
            fixupFolderItemLinks(makeConf, subfolder, folderConfiguration.getCCompilerConfiguration(), folderConfiguration.getCCCompilerConfiguration());
        }
    }

    /**
     * Clone object
     */
    @Override
    public MakeConfiguration clone() {
        MakeConfiguration clone = new MakeConfiguration(getBaseFSPath(), getName(),
                getConfigurationType().getValue(), getCustomizerId(), getDevelopmentHost().getHostKey(), null, true, getPlatformSpecific().getValue());
        super.cloneConf(clone);
        clone.setCloneOf(this);

        DevelopmentHostConfiguration dhconf = getDevelopmentHost().clone();
        clone.setDevelopmentHost(dhconf);
        clone.fixedRemoteSyncFactory = this.fixedRemoteSyncFactory;
        clone.customizerId = this.customizerId;
        CompilerSet2Configuration csconf = getCompilerSet().clone();
        csconf.setDevelopmentHostConfiguration(dhconf);
        clone.setCompilerSet(csconf);
        clone.setCRequired(getCRequired().clone());
        clone.setCppRequired(getCppRequired().clone());
        clone.setFortranRequired(getFortranRequired().clone());
        clone.setAssemblerRequired(getAssemblerRequired().clone());
        clone.setPreBuildConfiguration(getPreBuildConfiguration().clone());
        clone.setMakefileConfiguration(getMakefileConfiguration().clone());
        clone.setCompileConfiguration(getCompileConfiguration().clone());
        clone.setDependencyChecking(getDependencyChecking().clone());
        clone.setRebuildPropChanged(getRebuildPropChanged().clone());
        clone.setPrependToolCollectionPath(getPrependToolCollectionPath().clone());
        clone.setCCompilerConfiguration(getCCompilerConfiguration().clone());
        clone.getCCompilerConfiguration().setOwner(clone);
        clone.setCCCompilerConfiguration(getCCCompilerConfiguration().clone());
        clone.getCCCompilerConfiguration().setOwner(clone);
        clone.setFortranCompilerConfiguration(getFortranCompilerConfiguration().clone());
        clone.setAssemblerConfiguration(getAssemblerConfiguration().clone());
        clone.setLinkerConfiguration(getLinkerConfiguration().clone());
        clone.setArchiverConfiguration(getArchiverConfiguration().clone());
        clone.setPackagingConfiguration(getPackagingConfiguration().clone());
        clone.setRequiredProjectsConfiguration(getRequiredProjectsConfiguration().clone());
        clone.setDebuggerChooserConfiguration(getDebuggerChooserConfiguration().clone());
        clone.setQmakeConfiguration(getQmakeConfiguration().clone());
        clone.setCodeAssistanceConfiguration(getCodeAssistanceConfiguration().clone());

        dhconf.addPropertyChangeListener(csconf);

        // Clone all the aux objects
        //Vector clonedAuxObjects = new Vector();
        //for (Enumeration e = auxObjects.elements() ; e.hasMoreElements() ;) {
        //    ConfigurationAuxObject o = (ConfigurationAuxObject)e.nextElement();
        //    ConfigurationAuxObject clone2 = (ConfigurationAuxObject)o.clone();
        //    clonedAuxObjects.add(clone2);
        //}
        ConfigurationAuxObject[] objects = getAuxObjects();
        List<ConfigurationAuxObject> clonedAuxObjects = new ArrayList<>();
        for (int i = 0; i < objects.length; i++) {
            clonedAuxObjects.add(objects[i].clone(this));
        }
        clone.setAuxObjects(clonedAuxObjects);
        return clone;
    }

    public RemoteSyncFactory getRemoteSyncFactory() {
        RemoteSyncFactory result = fixedRemoteSyncFactory;
        synchronized (this) {
            if (result != null) {
                return result;
            }
        }
        //if (CndFileUtils.isLocalFileSystem(getBaseFSPath().getFileSystem())) {
        ExecutionEnvironment execEnv = getDevelopmentHost().getExecutionEnvironment();
        if (execEnv.isLocal()) {            
            return null;
        } else {
            ExecutionEnvironment fsEnv = FileSystemProvider.getExecutionEnvironment(getBaseFSPath().getFileSystem());
            if (execEnv.equals(fsEnv)) {
                return RemoteSyncFactory.fromID(RemoteProject.FULL_REMOTE_SYNC_ID);
            }
            return ServerList.get(execEnv).getSyncFactory();
        }
    }

    public RemoteSyncFactory getFixedRemoteSyncFactory() {
        return fixedRemoteSyncFactory;
    }

    public void setFixedRemoteSyncFactory(RemoteSyncFactory fixedRemoteSyncFactory) {
        this.fixedRemoteSyncFactory = fixedRemoteSyncFactory;
    }

    public FileSystem getSourceFileSystem() {
        return getBaseFSPath().getFileSystem();
    }
    
    public ExecutionEnvironment getFileSystemHost() {
        return FileSystemProvider.getExecutionEnvironment(getBaseFSPath().getFileSystem());
    }
    
    public void setRequiredLanguagesDirty(boolean b) {
        languagesDirty = b;
    }

    public boolean getRequiredLanguagesDirty() {
        return languagesDirty;
    }

    public boolean hasCFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return cRequired.getValue();
    }

    public boolean hasCPPFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return cppRequired.getValue();
    }

    public boolean hasFortranFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return fortranRequired.getValue();
    }

    public boolean hasAssemblerFiles(MakeConfigurationDescriptor configurationDescriptor) {
        reCountLanguages(configurationDescriptor);
        return assemblerRequired.getValue();
    }

//    public boolean hasAsmFiles(MakeConfigurationDescriptor configurationDescriptor) {
//        if (getLanguagesDirty())
//            reCountLanguages(configurationDescriptor);
//        return asmRequired.getValue();
//    }
    public void reCountLanguages(MakeConfigurationDescriptor configurationDescriptor) {
        boolean hasCFiles = false;
        boolean hasCPPFiles = false;
        boolean hasFortranFiles = false;
        boolean hasAssemblerFiles = false;
        
        //boolean hasCAsmFiles = false;


        if (!getRequiredLanguagesDirty()) {
            return;
        }

        Item[] items = configurationDescriptor.getProjectItems();
        if (items.length == 0 && isMakefileConfiguration()) {
            // This may not be true but is our best guess. No way to know since no files have been added to project.
            hasCFiles = true;
            hasCPPFiles = true;
        } else {
            // Base it on actual files added to project
            for (int x = 0; x < items.length; x++) {
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(this);
                if (itemConfiguration == null
                        || itemConfiguration.getExcluded() == null
                        || itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                PredefinedToolKind tool = itemConfiguration.getTool();
                if (tool == PredefinedToolKind.CCompiler) {
                    hasCFiles = true;
                }
                if (tool == PredefinedToolKind.CCCompiler) {
                    hasCPPFiles = true;
                }
                if (tool == PredefinedToolKind.FortranCompiler) {
                    hasFortranFiles = true;
                }
                if (tool == PredefinedToolKind.Assembler) {
                    hasAssemblerFiles = true;
                }
                //            if (itemConfiguration.getTool() == Tool.AsmCompiler) {
                //                hasCAsmFiles = false;
                //            }
            }
        }
        cRequired.setDefault(hasCFiles);
        cppRequired.setDefault(hasCPPFiles);
        fortranRequired.setDefault(hasFortranFiles);
        assemblerRequired.setDefault(hasAssemblerFiles);
        //asmRequired.setValueDef(hasCAsmFiles);

        languagesDirty = false;
    }

    /**
     * @return the customizerId
     */
    public String getCustomizerId() {
        return customizerId;
    }

    /**
     * @param customizerId the customizerId to set
     */
    public final void setCustomizerId(String customizerId) {
        this.customizerId = customizerId;
    }

    public CodeAssistanceConfiguration getCodeAssistanceConfiguration() {
        return codeAssistanceConfiguration;
    }

    public void setCodeAssistanceConfiguration(CodeAssistanceConfiguration codeAssistanceConfiguration) {
        this.codeAssistanceConfiguration = codeAssistanceConfiguration;
    }

    public class LanguageBooleanConfiguration extends BooleanConfiguration implements Cloneable {

        private boolean notYetSet = true;

        LanguageBooleanConfiguration() {
            super(false);
        }

        @Override
        public void setValue(boolean b) {
            if (notYetSet) {
                setValue(b, b);
            } else {
                super.setValue(b);
            }
            notYetSet = false;
        }

        @Override
        public void setDefault(boolean b) {
            if (getValue() == getDefault()) {
                setValue(b, b);
            } else {
                super.setDefault(b);
            }
            notYetSet = false;
        }

        public void setValue(boolean v, boolean d) {
            super.setValue(v);
            super.setDefault(d);
            notYetSet = false;
        }

        @Override
        public LanguageBooleanConfiguration clone() {
            LanguageBooleanConfiguration clone = new LanguageBooleanConfiguration();
            clone.setValue(getValue(), getDefault());
            clone.setModified(getModified());
            return clone;
        }

        public void assign(LanguageBooleanConfiguration conf) {
            setValue(conf.getValue(), conf.getDefault());
            setModified(conf.getModified());
        }
    }

    public String getVariant() {
        String ret = "";
        if (getCompilerSet().getCompilerSet() == null) {
            if (CndUtils.isUnitTestMode()) {
                CndUtils.threadsDump();
            }
            return ret;
        }
        return getVariant(getCompilerSet().getCompilerSet(), getDevelopmentHost().getBuildPlatform());
//        ret += getCompilerSet().getCompilerSet().getName() + "-"; // NOI18N
//        ret += Platforms.getPlatform(getPlatform().getValue()).getName();
//        return ret;
    }

    public static String getVariant(CompilerSet compilerSet, int platform) {
        return compilerSet.getName() + "-" + Platforms.getPlatform(platform).getName(); // NOI18N
    }

    public Set<Project> getSubProjects() {
        Set<Project> subProjects = new HashSet<>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                Project project = projectItem.getProject(getBaseFSPath());
                if (project != null) {
                    subProjects.add(project);
                } else {
                    // FIXUP ERROR
                }
            }
        }
        for (LibraryItem.ProjectItem libProject : getRequiredProjectsConfiguration().getValue()) {
            Project project = libProject.getProject(getBaseFSPath());
            if (project != null) {
                subProjects.add(project);
            }
        }
        return subProjects;
    }

    public Set<String> getSubProjectLocations() {
        Set<String> subProjectLocations = new HashSet<>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                subProjectLocations.add(projectItem.getMakeArtifact().getProjectLocation());
            }
        }
        return subProjectLocations;
    }

    public Set<String> getSubProjectOutputLocations() {
        Set<String> subProjectOutputLocations = new HashSet<>();
        LibrariesConfiguration librariesConfiguration = getLinkerConfiguration().getLibrariesConfiguration();
        for (LibraryItem item : librariesConfiguration.getValue()) {
            if (item instanceof LibraryItem.ProjectItem) {
                LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                String outputLocation = CndPathUtilities.getDirName(projectItem.getMakeArtifact().getOutput());
                if (CndPathUtilities.isPathAbsolute(outputLocation)) {
                    subProjectOutputLocations.add(outputLocation);
                } else {
                    subProjectOutputLocations.add(projectItem.getMakeArtifact().getProjectLocation() + "/" + outputLocation); // NOI18N
                }
            }
        }
        return subProjectOutputLocations;
    }

    public String getOutputValue() {
        String output = null;
        if (isLinkerConfiguration()) {
            output = getLinkerConfiguration().getOutputValue();
        } else if (isArchiverConfiguration()) {
            output = getArchiverConfiguration().getOutputValue();
        } else if (isMakefileConfiguration()) {
            output = getMakefileConfiguration().getOutput().getValue();
        } else if (isQmakeConfiguration()) {
            output = getQmakeConfiguration().getOutputValue();
        } else {
            assert false;
        }
        return output;
    }

    public String getAbsoluteOutputValue() {
        String output = getOutputValue();

        if (output == null || output.isEmpty()) {
            return output;
        }
        if (!CndPathUtilities.isPathAbsolute(output)) {
            output = getBaseDir() + "/" + output; // NOI18N
            output = CndPathUtilities.normalizeSlashes(output);
            boolean isNetworkPath = output.startsWith("//");
            output = CndPathUtilities.normalizeUnixPath(output);
            if (isNetworkPath && !output.startsWith("//")) {
                output = "/"+output; // NOI18N
            }
        }
        return expandMacros(output);
    }

    public boolean hasDebugger() {
        return ProjectActionSupport.getInstance().canHandle(this, null, PredefinedType.DEBUG);
    }

    public String getLibraryExtension() {
        return Platforms.getPlatform(getDevelopmentHost().getBuildPlatform()).getLibraryExtension();
    }
    
    public String expandMacros(String val) {
        // Substitute macros
        val = CndPathUtilities.expandAllMacroses(val, "${TESTDIR}", MakeConfiguration.CND_BUILDDIR_MACRO + '/' + MakeConfiguration.CND_CONF_MACRO + '/' + MakeConfiguration.CND_PLATFORM_MACRO + "/" + "tests"); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_OUTPUT_PATH_MACRO, getOutputValue());
        val = CndPathUtilities.expandAllMacroses(val, "${OUTPUT_BASENAME}", CndPathUtilities.getBaseName(getOutputValue())); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, "${PLATFORM}", getVariant()); // Backward compatibility // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.OBJECTDIR_MACRO, ConfigurationMakefileWriter.getObjectDir(this)); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_PLATFORM_MACRO, getVariant()); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_CONF_MACRO, getName()); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_DISTDIR_MACRO, MakeConfiguration.DIST_FOLDER); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_BUILDDIR_MACRO, MakeConfiguration.BUILD_FOLDER); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.CND_DLIB_EXT_MACRO, getLibraryExtension()); // NOI18N
        val = CndPathUtilities.expandAllMacroses(val, MakeConfiguration.PROJECTDIR_MACRO, getBaseDir());
        return val;
    }

    /*
     * Special version of IntConfiguration
     * Names are shifted by offset to match value and limit choice
     */
    private final static class ManagedIntConfiguration extends IntConfiguration {

        private final int offset;

        public ManagedIntConfiguration(IntConfiguration master, int def, String[] names, String[] options, int offset) {
            super(master, def, names, options);
            this.offset = offset;
        }

        @Override
        public void setValue(String s) {
            String[] names = getNames();
            if (s != null) {
                for (int i = 0; i < names.length; i++) {
                    if (s.equals(names[i])) {
                        setValue(i + offset);
                        break;
                    }
                }
            }
        }

        @Override
        public String getName() {
            return getNames()[getValue() - offset];
        }
    }
    
    public MakeProjectCustomizer getProjectCustomizer() {
        if (getCustomizerId() == null){
            return null;
        }
        MakeProjectCustomizer makeProjectCustomizer = null;
        Collection<? extends MakeProjectCustomizer> mwc = Lookup.getDefault().lookupAll(MakeProjectCustomizer.class);
        for (MakeProjectCustomizer instance : mwc) {
            if (getCustomizerId().equals(instance.getCustomizerId())) {
                makeProjectCustomizer = instance;
                break;
            }
        }
        return makeProjectCustomizer;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeConfiguration.class, s);
    }
}
