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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.utils.CndFileVisibilityQuery;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.makeproject.api.CodeStyleWrapper;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifactProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectCustomizer;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectFileProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectEvent;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectLife;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectListener;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersProjectMetadataFactory;
import org.netbeans.modules.cnd.makeproject.options.FullFileIndexer;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.toolchain.ToolchainProject;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Represents one plain Make project.
 */
public final class MakeProjectImpl implements Project, MakeProjectListener, MakeProject {

    private static final boolean UNIT_TEST_MODE = CndUtils.isUnitTestMode();
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private static final String HEADER_EXTENSIONS = "header-extensions"; // NOI18N
    private static final String C_EXTENSIONS = "c-extensions"; // NOI18N
    private static final String CPP_EXTENSIONS = "cpp-extensions"; // NOI18N
    private final RequestProcessor RP;
    private final MakeProjectTypeImpl kind;
    private final MakeProjectHelper helper;
    //private final PropertyEvaluator eval;
    //private final ReferenceHelper refHelper;
    private final NativeProject nativeProject;
    private final Lookup lookup;
    private final ConfigurationDescriptorProviderImpl projectDescriptorProvider;
    private MakeProjectConfigurationProvider makeProjectConfigurationProvider;
    private final Set<String> headerExtensions = MakeProjectImpl.createExtensionSet();
    private final Set<String> cExtensions = MakeProjectImpl.createExtensionSet();
    private final Set<String> cppExtensions = MakeProjectImpl.createExtensionSet();
    private String sourceEncoding = null;
    private FormattingStyle projectFormattingStyle;
    private CodeStyleWrapper cFormattingSytle;
    private CodeStyleWrapper cppFormattingSytle;
    private CodeStyleWrapper headerFormattingSytle;
    private CodeStyleWrapper clangFormattingSytle;
    // lock and open/close state of make project
    private final AtomicBoolean openStateAndLock = new AtomicBoolean(false);
    private final AtomicBoolean isDeleted = new AtomicBoolean(false);
    private final AtomicBoolean isDeleting = new AtomicBoolean(false);
    private final MakeSources sources;
    private final MutableCP sourcepath;
    private final PropertyChangeListener indexerListener;
    private String configurationXMLComment;
    private final Set<MyInterrupter> interrupters = new WeakSet<>();

    public MakeProjectImpl(MakeProjectHelper helper) throws IOException {
        LOGGER.log(Level.FINE, "Start of creation MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
        this.kind = MakeBasedProjectFactorySingleton.TYPE_INSTANCE;
        this.helper = helper;
        RP = new RequestProcessor("Open/Close project " + helper.getProjectDirectory(), 1); // NOI18N
        //eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        //refHelper = new ReferenceHelper(helper, aux, eval);
        projectDescriptorProvider = new ConfigurationDescriptorProviderImpl(this, helper.getProjectDirectory());
        LOGGER.log(Level.FINE, "Create ConfigurationDescriptorProvider@{0} for MakeProject@{1} {2}", new Object[]{System.identityHashCode(projectDescriptorProvider), System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
        sources = new MakeSources(this, helper);
        sourcepath = new MutableCP(sources);
        indexerListener = new IndexerOptionsListener(this);
        lookup = createLookup(aux);
        nativeProject = lookup.lookup(NativeProject.class);

        // Find the project type from project.xml
        Element data = helper.getPrimaryConfigurationData(true);

        readProjectExtension(data, HEADER_EXTENSIONS, headerExtensions);
        readProjectExtension(data, C_EXTENSIONS, cExtensions);
        readProjectExtension(data, CPP_EXTENSIONS, cppExtensions);
        sourceEncoding = getSourceEncodingFromProjectXml();

        LOGGER.log(Level.FINE, "End of creation MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
    }

    private void readProjectExtension(Element data, String key, Set<String> set) {
        NodeList nl = data.getElementsByTagName(key);
        if (nl.getLength() == 1) {
            nl = nl.item(0).getChildNodes();
            if (nl.getLength() == 1) {
                String extensions = nl.item(0).getNodeValue();
                set.addAll(Arrays.asList(extensions.split(","))); // NOI18N
            }
        }
    }

    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public String toString() {
        return "MakeProject[" + getProjectDirectory() + "]"; // NOI18N
    }
   
    public void setConfigurationXMLComment(String configurationXMLComment) {
        this.configurationXMLComment = configurationXMLComment;
    }

    public String getConfigurationXMLComment() {
        return configurationXMLComment;
    }
   
    public MakeProjectHelper getMakeProjectHelper() {
        return helper;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = new MakeSubprojectProvider(this); //refHelper.createSubprojectProvider();
        Info info = new Info(this);
        makeProjectConfigurationProvider = new MakeProjectConfigurationProvider(this, projectDescriptorProvider, info);
        final RemoteProjectImpl remoteProject = new RemoteProjectImpl(this);
        final MakeProjectEncodingQueryImpl encodingQuery = new MakeProjectEncodingQueryImpl(this);
        ArrayList<Object> ic = new ArrayList<>();
        ic.add(info);
        ic.add(aux);
        ic.add(spp);
        ic.add(new MakeActionProviderImpl(this));
        //ic.add(new MakeLogicalViewProvider(this));
        //ic.add(BrokenReferencesSupport.createPlatformVersionProblemProvider(this));
        //ic.add(new MakeProjectSearchInfo(projectDescriptorProvider));
        //ic.add(new TemplateAttributesProviderImpl(this));
        //ic.add(new MakeCustomizerProviderImpl(this));
        //ic.add(new CndDocumentCodeStyleProviderImpl());
        Lookup.getDefault().lookupAll(MakeProjectLookupProvider.class).forEach((p) -> {
            p.addLookup(this, ic);
        });
        ic.add(new MakeArtifactProviderImpl(this));
        ic.add(UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl(this)));
        ic.add(new MakeSharabilityQueryImpl(projectDescriptorProvider, getProjectDirectory()));
        ic.add(sources);
        ic.add(helper);
        ic.add(projectDescriptorProvider);
        ic.add(makeProjectConfigurationProvider);
        ic.add(new NativeProjectSettingsImpl(this, this.kind.getPrimaryConfigurationDataElementNamespace(false), false));
        ic.add(new RecommendedTemplatesImpl(projectDescriptorProvider));
        ic.add(new MakeProjectOperations(this));
        ic.add(kind);
        ic.add(encodingQuery);
        ic.add(remoteProject);
        ic.add(new ToolchainProjectImpl(this));
        ic.add(new CacheDirectoryProviderImpl(helper.getProjectDirectory()));
        ic.add(this);
        
        Object[] lookups = ic.toArray(new Object[ic.size()]);
        
        MakeProjectCustomizer makeProjectCustomizer = getProjectCustomizer(getProjectCustomizerId());
        if (makeProjectCustomizer != null) {
            lookups = makeProjectCustomizer.getLookup(getProjectDirectory(), lookups);
        }
        boolean containsNativeProject = false;
        for (Object object : lookups) {
            if(object instanceof NativeProject) {
                containsNativeProject = true;
                break;
            }
        }
        if(!containsNativeProject) {
            lookups = augment(lookups, new NativeProjectProvider(this, remoteProject, projectDescriptorProvider));
        }
        Lookup lkp = Lookups.fixed(lookups);
        return LookupProviderSupport.createCompositeLookup(lkp, kind.getLookupMergerPath());
    }

    @Override
    public MakeProjectHelper getHelper() {
        return helper;
    }

    @Override
    public ConfigurationDescriptorProvider getConfigurationDescriptorProvider() {
        return projectDescriptorProvider;
    }

    @Override
    public ProjectConfigurationProvider<Configuration> getProjectConfigurationProvider() {
        return makeProjectConfigurationProvider;
    }
    
    @Override
    public ExecutionEnvironment getFileSystemHost() {
        return FileSystemProvider.getExecutionEnvironment(helper.getProjectDirectory());
    }

    @Override
    public ExecutionEnvironment getDevelopmentHost() {
        DevelopmentHostConfiguration dc = getDevelopmentHostConfiguration();
        return (dc == null) ? null : dc.getExecutionEnvironment();
    }

    private static final class CacheDirectoryProviderImpl extends ProjectOpenedHook implements CacheDirectoryProvider {
        
        private final FileObject projectDirectory;
        private FileObject cacheDirectory;
        private final Object lock = new Object();

        public CacheDirectoryProviderImpl(FileObject projectDirectory) {
            this.projectDirectory = projectDirectory;
        }

        @Override
        public FileObject getCacheDirectory() throws IOException {
            synchronized (lock) {
                if (cacheDirectory == null) {
                    File cacheFile = getCacheLocation(projectDirectory);
                    if (cacheFile == null) {
                        // TODO: find more elegant soluition than duplicating "cnd.repository.cache.path" in CacheLocation and MakeProject
                        // What we can do is our own Places impl. that delegates to the next Places impl. if this property is not set; is it worth doing?
                        String path = System.getProperty("cnd.repository.cache.path");
                        if (path == null) {
                            cacheFile = Places.getCacheDirectory();
                        } else {
                            cacheFile = new File(path);
                        }
                    }
                    cacheDirectory = FileUtil.createFolder(cacheFile);
                }
                return cacheDirectory;
            }
        }

        @Override
        protected void projectOpened() {}

        @Override
        protected void projectClosed() {
            synchronized (lock) {
                cacheDirectory = null;
            }
        }
    }
        
    private static <T> T[] augment(T[] array, T value) {
        ArrayList<T> newLookups = new ArrayList<>();
        newLookups.addAll(Arrays.asList(array));
            newLookups.add(value);            
        return (T[]) newLookups.toArray();
    }

    @Override
    public Properties getProjectProperties(boolean shared) {
        Properties props = new Properties();
        FileObject propsFO;
        if (shared) {
            propsFO = helper.getProjectDirectory().getFileObject(MakeProjectHelper.PROJECT_PROPERTIES_PATH);
        } else {
            propsFO = helper.getProjectDirectory().getFileObject(MakeProjectHelper.PRIVATE_PROPERTIES_PATH);
        }
        if (propsFO != null && propsFO.isValid()) {
            InputStream is = null;
            try {
                is = propsFO.getInputStream();
                props.load(is);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return props;
    }

    @Override
    public void saveProjectProperties(Properties props, boolean shared) {
        FileObject propsFO = null;
        if (shared) {
            try {
                propsFO = FileUtil.createData(helper.getProjectDirectory(), MakeProjectHelper.PROJECT_PROPERTIES_PATH);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        } else {
            try {
                propsFO = FileUtil.createData(helper.getProjectDirectory(), MakeProjectHelper.PRIVATE_PROPERTIES_PATH);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        if (propsFO != null && propsFO.isValid()) {
            OutputStream os = null;
            try {
                os = propsFO.getOutputStream();
                props.store(os, null);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }

    /**
     * Tries getting cache path from project.properties -
     * first private, then public
     */
    private static File getCacheLocation(FileObject projectDirectory) {

        String[] propertyPaths = new String[] {
            MakeProjectHelper.PRIVATE_PROPERTIES_PATH,
            MakeProjectHelper.PROJECT_PROPERTIES_PATH
        };

        for (int i = 0; i < propertyPaths.length; i++) {
            FileObject propsFO = projectDirectory.getFileObject(propertyPaths[i]);
            if (propsFO != null && propsFO.isValid()) {
                Properties props = new Properties();
                InputStream is = null;
                try {
                    is = propsFO.getInputStream();
                    props.load(is);
                    String path = props.getProperty("cache.location"); //NOI18N
                    if (path != null) {
                        if (CndPathUtilities.isPathAbsolute(path)) {
                            return new File(path);
                        } else {
                            return new File(projectDirectory.getPath() + '/' + path); //NOI18N
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "Error closing " + propsFO.getPath(), ex);
                        }
                    }
                }
            }
        }
        if (DebugUtils.getBoolean("cnd.cache.in.project", false)) { //NOI18N
            if (CndFileUtils.isLocalFileSystem(projectDirectory)) {
                File cache = new File(projectDirectory.getPath() + "/nbproject/private/cache/model"); //NOI18N
                cache.mkdirs();
                if (cache.exists()) {
                    return cache;
                }
            }
        }
        return null;
    }

    @Override
    public void configurationXmlChanged(MakeProjectEvent ev) {
        if (ev.getPath().equals(MakeProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info) getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    @Override
    public void propertiesChanged(MakeProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }

    /**
     * Check needed header extensions and store list in the NB/project properties.
     * @param needAdd list of needed extensions of header files.
     */
    public boolean addAdditionalHeaderExtensions(Collection<String> needAdd) {
        Set<String> headerExtension = MakeProjectImpl.getHeaderSuffixes();
        Set<String> sourceExtension = MakeProjectImpl.getSourceSuffixes();
        Set<String> usedExtension = MakeProjectImpl.createExtensionSet();
        for (String extension : needAdd) {
            if (extension.length() > 0) {
                if (!headerExtension.contains(extension) && !sourceExtension.contains(extension)) {
                    usedExtension.add(extension);
                }
            }
        }
        if (usedExtension.size() > 0) {
            // add unknown extension to header files
            addMIMETypeExtensions(usedExtension, MIMENames.HEADER_MIME_TYPE);
            headerExtensions.addAll(usedExtension);
            saveAdditionalExtensions();
            return true;
        }
        return false;
    }

    private void addMIMETypeExtensions(Collection<String> extensions, String mime) {
        MIMEExtensions exts = MIMEExtensions.get(mime);
        extensions.forEach((ext) -> {
            exts.addExtension(ext);
        });
        CndFileVisibilityQuery.getDefault().stateChanged(null);
    }

    private Set<String> getUnknownExtensions(Set<String> inLoader, Set<String> inProject) {
        Set<String> unknown = MakeProjectImpl.createExtensionSet();
        for (String extension : inProject) {
            if (extension.length() > 0) {
                if (!inLoader.contains(extension)) {
                    unknown.add(extension);
                }
            }
        }
        return unknown;
    }

    private void checkNeededExtensions() {
        if (UNIT_TEST_MODE || CndUtils.isStandalone()) {
            return;
        }
        Set<String> unknownC = getUnknownExtensions(MakeProjectImpl.getCSuffixes(), cExtensions);
        Set<String> unknownCpp = getUnknownExtensions(MakeProjectImpl.getCppSuffixes(), cppExtensions);
        Set<String> unknownH = getUnknownExtensions(MakeProjectImpl.getHeaderSuffixes(), headerExtensions);
        if (!unknownC.isEmpty() && unknownCpp.isEmpty() && unknownH.isEmpty()) {
            if (unknownC.size() > 0 && addNewExtensionDialog(unknownC, "C")) { // NOI18N
                addMIMETypeExtensions(unknownC, MIMENames.C_MIME_TYPE);
            }
        } else if (unknownC.isEmpty() && !unknownCpp.isEmpty() && unknownH.isEmpty()) {
            if (addNewExtensionDialog(unknownCpp, "CPP")) { // NOI18N
                addMIMETypeExtensions(unknownCpp, MIMENames.CPLUSPLUS_MIME_TYPE);
            }
        } else if (unknownC.isEmpty() && unknownCpp.isEmpty() && !unknownH.isEmpty()) {
            if (addNewExtensionDialog(unknownH, "H")) { // NOI18N
                addMIMETypeExtensions(unknownH, MIMENames.HEADER_MIME_TYPE);
            }
        } else if (!(unknownC.isEmpty() && unknownCpp.isEmpty() && unknownH.isEmpty())) {
            ConfirmSupport.MimeExtensions confirm = ConfirmSupport.getDefaultConfirmMimeExtensionsFactory().create(unknownC, unknownCpp, unknownH);
            if (confirm != null) {
                if (confirm.isC()) {
                    addMIMETypeExtensions(unknownC, MIMENames.C_MIME_TYPE);
                }
                if (confirm.isCpp()) {
                    addMIMETypeExtensions(unknownCpp, MIMENames.CPLUSPLUS_MIME_TYPE);
                }
                if (confirm.isHeader()) {
                    addMIMETypeExtensions(unknownH, MIMENames.HEADER_MIME_TYPE);
                }
            }
        }
    }

    public void updateExtensions(Set<String> cSet, Set<String> cppSet, Set<String> hSet) {
        cExtensions.clear();
        cExtensions.addAll(cSet);
        cppExtensions.clear();
        cppExtensions.addAll(cppSet);
        headerExtensions.clear();
        headerExtensions.addAll(hSet);
        saveAdditionalExtensions();
    }

    private synchronized void registerClassPath(boolean register) {
        if (register) {
            MakeProjectClassPathProvider.addProjectCP(sourcepath.getClassPath());
            if (MakeOptions.getInstance().isFullFileIndexer()) {
                GlobalPathRegistry.getDefault().register(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
            }
        } else {
            MakeProjectClassPathProvider.removeProjectCP(sourcepath.getClassPath());
            try {
                GlobalPathRegistry.getDefault().unregister(MakeProjectPaths.SOURCES, sourcepath.getClassPath());
            } catch (Throwable ex) {
                // do nothing because register depends on make options
            }
        }
    }

    private void saveAdditionalExtensions() {
        Element data = helper.getPrimaryConfigurationData(true);
        boolean changed = false;
        changed |= saveAdditionalHeaderExtensions(data, MakeProjectImpl.C_EXTENSIONS, cExtensions);
        changed |= saveAdditionalHeaderExtensions(data, MakeProjectImpl.CPP_EXTENSIONS, cppExtensions);
        changed |= saveAdditionalHeaderExtensions(data, MakeProjectImpl.HEADER_EXTENSIONS, headerExtensions);
        if (changed) {
            helper.putPrimaryConfigurationData(data, true);
        }
    }

    private boolean saveAdditionalHeaderExtensions(Element data, String key, Set<String> set) {
        StringBuilder buf = new StringBuilder();
        set.forEach((e) -> {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(e);
        });
        String newText = buf.toString();
        Element element;
        NodeList nodeList = data.getElementsByTagName(key);
        if (nodeList.getLength() == 1) {
            element = (Element) nodeList.item(0);
            NodeList deadKids = element.getChildNodes();
            if (deadKids.getLength() == 1) {
                String text = deadKids.item(0).getTextContent();
                if (text.equals(newText)) {
                    return false;
                }
            } else if (deadKids.getLength() == 0) {
                if(newText.isEmpty()) {
                    return false;
                }
            }
            while (deadKids.getLength() > 0) {
                element.removeChild(deadKids.item(0));
            }
        } else {
            element = data.getOwnerDocument().createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, key);
            data.appendChild(element);
        }
        element.appendChild(data.getOwnerDocument().createTextNode(buf.toString()));
        return true;
    }

    private boolean addNewExtensionDialog(Set<String> usedExtension, String type) {
        if (UNIT_TEST_MODE || CndUtils.isStandalone()) {
            return true;
        }
        ConfirmSupport.MimeExtension confirm = ConfirmSupport.getDefaultConfirmMimeExtensionsFactory().create(usedExtension, type);
        if (confirm != null) {
            return confirm.addNewExtension();
        }
        return false;
    }

    public static Set<String> createExtensionSet() {
        if (CndFileUtils.isSystemCaseSensitive()) {
            return new TreeSet<>();
        } else {
            return new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        }
    }

    private static Set<String> getSourceSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        suffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getCSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getCppSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        return suffixes;
    }

    private static Set<String> getHeaderSuffixes() {
        Set<String> suffixes = createExtensionSet();
        suffixes.addAll(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
        return suffixes;
    }

    private static String getString(String s) {
        return NbBundle.getMessage(MakeProjectImpl.class, s);
    }

    // Package private methods -------------------------------------------------
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        private static final String[] RECOMMENDED_TYPES = new String[]{
            "c-types", // NOI18N
            "cpp-types", // NOI18N
            "shell-types", // NOI18N
            "makefile-types", // NOI18N
            "simple-files", // NOI18N
            "fortran-types", // NOI18N
            "asm-types", // NOI18N
            "qt-types", // NOI18N
            "cncpp-test-types"}; // NOI18N
        private static final String[] PRIVILEGED_NAMES = new String[]{
            "Templates/cFiles/main.c", // NOI18N
            "Templates/cFiles/file.c", // NOI18N
            "Templates/cFiles/file.h", // NOI18N
            "Templates/cppFiles/class.cc", // NOI18N
            "Templates/cppFiles/main.cc", // NOI18N
            "Templates/cppFiles/file.cc", // NOI18N
            "Templates/cppFiles/file.h", // NOI18N
            "Templates/fortranFiles/fortranFreeFormatFile.f90", // NOI18N
            "Templates/MakeTemplates/ComplexMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/ExecutableMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/SharedLibMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/StaticLibMakefile"}; // NOI18N
        private static final String[] PRIVILEGED_NAMES_QT = new String[]{
            // Qt-specific templates fist:
            "Templates/qtFiles/main.cc", // NOI18N
            "Templates/qtFiles/form.ui", // NOI18N
            "Templates/qtFiles/resource.qrc", // NOI18N
            "Templates/qtFiles/translation.ts", // NOI18N
            // the rest is exact copy of PRIVILEGED_NAMES:
            "Templates/cFiles/main.c", // NOI18N
            "Templates/cFiles/file.c", // NOI18N
            "Templates/cFiles/file.h", // NOI18N
            "Templates/cppFiles/class.cc", // NOI18N
            "Templates/cppFiles/main.cc", // NOI18N
            "Templates/cppFiles/file.cc", // NOI18N
            "Templates/cppFiles/file.h", // NOI18N
            "Templates/fortranFiles/fortranFreeFormatFile.f90", // NOI18N
            "Templates/MakeTemplates/ComplexMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/ExecutableMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/SharedLibMakefile", // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/StaticLibMakefile"}; // NOI18N
        private final ConfigurationDescriptorProvider configurationProvider;

        public RecommendedTemplatesImpl(ConfigurationDescriptorProvider configurationProvider) {
            this.configurationProvider = configurationProvider;
        }

        @Override
        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            if (configurationProvider.gotDescriptor()) {
                MakeConfigurationDescriptor configurationDescriptor = configurationProvider.getConfigurationDescriptor();
                if (configurationDescriptor != null) {
                    MakeConfiguration conf = configurationDescriptor.getActiveConfiguration();
                    if (conf != null && conf.isQmakeConfiguration()) {
                        return PRIVILEGED_NAMES_QT;
                    }
                }
            }
            return PRIVILEGED_NAMES;
        }
    }

    /*
     * Return source encoding if in project.xml (only project version >= 50)
     */
    public String getSourceEncodingFromProjectXml() {
        Element data = helper.getPrimaryConfigurationData(true);

        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.SOURCE_ENCODING_TAG);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                return node.getTextContent();
            }
        }

        return null;
    }

    @Override
    public synchronized FormattingStyle isProjectFormattingStyle() {
        if (projectFormattingStyle == null) {
            Element data = helper.getPrimaryConfigurationData(true);
            NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.FORMATTING_STYLE_PROJECT_ELEMENT);
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if ("true".equals(node.getTextContent())) { //NOI18N
                        projectFormattingStyle = FormattingStyle.Project;
                    } else if ("false".equals(node.getTextContent())) { //NOI18N
                        projectFormattingStyle = FormattingStyle.Global;
                    }  else if ("clang-format".equals(node.getTextContent())) { //NOI18N
                        projectFormattingStyle = FormattingStyle.ClangFormat;
                    }
                    break;
                }
            }
            if (projectFormattingStyle == null) {
                projectFormattingStyle = FormattingStyle.Global;
            }
        }
        return projectFormattingStyle;
    }

    @Override
    public synchronized void setProjectFormattingStyle(FormattingStyle isProject) {
        projectFormattingStyle = isProject;
    }

    @Override
    public CodeStyleWrapper getProjectFormattingStyle(String mime) {
        NodeList nodeList = null;
        if (MIMENames.C_MIME_TYPE.equals(mime)) {
            if (cFormattingSytle != null) {
                return cFormattingSytle;
            }
            nodeList = helper.getPrimaryConfigurationData(true).getElementsByTagName(MakeProjectTypeImpl.C_FORMATTING_STYLE_ELEMENT);
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
            if (cppFormattingSytle != null) {
                return cppFormattingSytle;
            }
            nodeList = helper.getPrimaryConfigurationData(true).getElementsByTagName(MakeProjectTypeImpl.CPP_FORMATTING_STYLE_ELEMENT);
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
            if (headerFormattingSytle != null) {
                return headerFormattingSytle;
            }
            nodeList = helper.getPrimaryConfigurationData(true).getElementsByTagName(MakeProjectTypeImpl.HEADER_FORMATTING_STYLE_ELEMENT);
        }
        String res = null;
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                res = node.getTextContent();
                break;
            }
        }
        if (res != null) {
            if (MIMENames.C_MIME_TYPE.equals(mime)) {
                cFormattingSytle = CodeStyleWrapper.decodeProjectStyle(MakeProject.FormattingStyle.Project, res);
                return cFormattingSytle;
            } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
                cppFormattingSytle = CodeStyleWrapper.decodeProjectStyle(MakeProject.FormattingStyle.Project, res);
                return cppFormattingSytle;
            } else if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
                headerFormattingSytle = CodeStyleWrapper.decodeProjectStyle(MakeProject.FormattingStyle.Project, res);
                return headerFormattingSytle;
            }
        }
        if (mime == null) {
            if (clangFormattingSytle != null) {
                return clangFormattingSytle;
            }
            nodeList = helper.getPrimaryConfigurationData(true).getElementsByTagName(MakeProjectTypeImpl.CLANG_FORMAT_STYLE_ELEMENT);
            res = null;
            if (nodeList != null && nodeList.getLength() > 0) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    res = node.getTextContent();
                    break;
                }
            }
            if (res != null) {
                clangFormattingSytle = CodeStyleWrapper.decodeProjectStyle(MakeProject.FormattingStyle.ClangFormat, res);
                return clangFormattingSytle;
            }
        }
        return null;
    }
    
    @Override
    public void setProjectFormattingStyle(String mime, CodeStyleWrapper style) {
        if (MIMENames.C_MIME_TYPE.equals(mime)) {
            cFormattingSytle = style;
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mime)) {
            cppFormattingSytle = style;
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mime)) {
            headerFormattingSytle = style;
        }
        if (mime == null) {
            clangFormattingSytle = style;
        }
    }

    @Override
    public String getSourceEncoding() {
        if (sourceEncoding == null) {
            // Read configurations.xml. That's where encoding is stored for project version < 50)
            if (!projectDescriptorProvider.gotDescriptor()) {
                return FileEncodingQuery.getDefaultEncoding().name();
            }
        }
        if (sourceEncoding == null) {
            sourceEncoding = FileEncodingQuery.getDefaultEncoding().name();
        }
        return sourceEncoding;
    }

    @Override
    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }
    
    private MakeProjectCustomizer getProjectCustomizer(String customizerId) {
        if (customizerId == null) {
            return null;
        }
        MakeProjectCustomizer makeProjectCustomizer = null;
        Collection<? extends MakeProjectCustomizer> mwc = Lookup.getDefault().lookupAll(MakeProjectCustomizer.class);
        for (MakeProjectCustomizer instance : mwc) {
            if (customizerId.equals(instance.getCustomizerId())) {
                makeProjectCustomizer = instance;
                break;
            }
        }
        return makeProjectCustomizer;
    }
    
    private String getProjectCustomizerId() {
        String id = getCustomizerIdFromProjectXML();
        if (id == null) {
            FileObject[] children = getProjectDirectory().getChildren();
            for (FileObject c : children) {
                String name = c.getName();
                String ext = c.getExt();
                if (name.equals("cndcustomizerid")) { // NOI18N
                    id = ext;
                    break;
                }
            }
        }
        //System.out.println("getProjectCustomizerId " + id);
        return id;
    }

    /**
     * @return 
     */
    private String getCustomizerIdFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.CUSTOMIZERID_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node typeNode = nodeList.item(0).getFirstChild();
            if (typeNode != null) {
                String type = typeNode.getNodeValue();
                return type;
            }
        }
        return null;
    }

    /**
     * @return active configuration type (doesn't force reading configuration metadata)
     * If metadata already read, get type from the active configuration (it may have changed)
     * If not read, try private.xml (V >= V77)
     * If not found, try project.xml (V >= V78)
     */
    private int getActiveConfigurationType() {
        // If configurations already read, get it from active configuration (it may have changed)
        MakeConfiguration makeConfiguration = getActiveConfiguration();
        if (makeConfiguration != null) {
            return makeConfiguration.getConfigurationType().getValue();
        }

        // Get it from private.xml (version >= V77)
        int type = getActiveConfigurationTypeFromPrivateXML();
        if (type >= 0) {
            return type;
        }

        // Get it from project.xml (version >= V77)
        type = getActiveConfigurationTypeFromProjectXML();
        if (type >= 0) {
            return type;
        }

        return type;
    }

    /**
     * @return active configuration type (doesn't force reading configuration metadata) (V >= V78). Returns -1 if not found.
     */
    private int getActiveConfigurationTypeFromProjectXML() {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.CONFIGURATION_TYPE_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node typeNode = nodeList.item(0).getFirstChild();
            if (typeNode != null) {
                String type = typeNode.getNodeValue();
                try {
                    return Integer.parseInt(type);
                } catch (NumberFormatException nfe) {
                }
            }
        }
        return -1;
    }

    /**
     * @return active configuration type from private.xml (doesn't force reading configuration metadata) (V >= V77). Returns -1 if not found.
     */
    private int getActiveConfigurationTypeFromPrivateXML() {
        Element data = helper.getPrimaryConfigurationData(false);
        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_TYPE_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                try {
                    return Integer.parseInt(node.getTextContent());
                } catch (NumberFormatException nfe) {
                }
            }
        }
        return -1;
    }

    /**
     * @return active configuration index (doesn't force reading configuration metadata) from private.xml, if exists. Returns -1 if not found.
     */
    public int getActiveConfigurationIndexFromPrivateXML() {
        // Get it from xml (version >= V77)
        Element data = helper.getPrimaryConfigurationData(false);

        NodeList nodeList = data.getElementsByTagName(MakeProjectTypeImpl.ACTIVE_CONFIGURATION_INDEX_ELEMENT);
        if (nodeList != null && nodeList.getLength() > 0) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                try {
                    return Integer.parseInt(node.getTextContent());
                } catch (NumberFormatException nfe) {
                }
            }
        }

        return -1;
    }

//    private void dumpNode(Node node, int indent) {
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("-----------------------------");
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("nodeName    " + node.getNodeName());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("nodeValue   " + node.getNodeValue());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("localName   " + node.getLocalName());
//        System.out.print("            ".subSequence(0, indent));
//        System.out.println("prefix      " + node.getPrefix());
////        System.out.print("            ".subSequence(0, indent));
////        System.out.println("textContent " + node.getTextContent());
//        NodeList nodeList = node.getChildNodes();
//        for (int i = 0; i < nodeList.getLength(); i++) {
//            dumpNode(nodeList.item(i), indent+2);
//        }
//    }
    /** NPE-safe method for getting active configuration */
    @Override
    public MakeConfiguration getActiveConfiguration() {
        if (projectDescriptorProvider.gotDescriptor()) {
            MakeConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (projectDescriptor != null) {
                return projectDescriptor.getActiveConfiguration();
            }
        }
        return null;
    }

    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N

    /**
     * Checks whether the name is usable as Ant property name.
     * @param name name to check for usability as Ant property
     * @return true if name is usable otherwise false
     */
    private static boolean isUsablePropertyName(String name) {
        return VALID_PROPERTY_NAME.matcher(name).matches();
    }

    /**
     * Returns name usable as Ant property which is based on the given
     * name. All forbidden characters are either removed or replaced with
     * suitable ones.
     * @param name name to use as base for Ant property name
     * @return name usable as Ant property name
     */
    private static String getUsablePropertyName(String name) {
        if (isUsablePropertyName(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        for (int i=0; i<sb.length(); i++) {
            if (!isUsablePropertyName(sb.substring(i,i+1))) {
                sb.replace(i,i+1,"_"); // NOI18N
            }
        }
        return sb.toString();
    }


    // Private innerclasses ----------------------------------------------------

    /*
    private class CustomActionsHookImpl implements CustomActionsHook {
    private Vector customActions = null;
    public CustomActionsHookImpl() {
    customActions = new Vector();
    }
    public void addCustomAction(Action action) {
    synchronized (customActions) {
    customActions.add(action);
    }
    }
    public void removeCustomAction(Action action) {
    synchronized (customActions) {
    customActions.add(action);
    }
    }
    public Vector getCustomActions() {
    return customActions;
    }
    }
     */
    private static final class MakeSubprojectProvider implements SubprojectProvider {
        private final MakeProjectImpl project;

        public MakeSubprojectProvider(MakeProjectImpl prj) {
            this.project = prj;
        }
        
        // Add a listener to changes in the set of subprojects.
        @Override
        public void addChangeListener(ChangeListener listener) {
        }

        // Get a set of projects which this project can be considered to depend upon somehow.
        @Override
        public Set<Project> getSubprojects() {
            Set<Project> subProjects = new HashSet<>();
            Set<String> subProjectLocations = new HashSet<>();

            // Try project.xml first if project not already read (this is cheap)
            Element data = project.helper.getPrimaryConfigurationData(true);
            if (!project.projectDescriptorProvider.gotDescriptor() && data.getElementsByTagName(MakeProjectTypeImpl.MAKE_DEP_PROJECTS).getLength() > 0) {
                NodeList nl4 = data.getElementsByTagName(MakeProjectTypeImpl.MAKE_DEP_PROJECT);
                if (nl4.getLength() > 0) {
                    for (int i = 0; i < nl4.getLength(); i++) {
                        Node node = nl4.item(i);
                        NodeList nl2 = node.getChildNodes();
                        for (int j = 0; j < nl2.getLength(); j++) {
                            String typeTxt = nl2.item(j).getNodeValue();
                            subProjectLocations.add(typeTxt);
                        }
                    }
                }
            } else {
                // Then read subprojects from configuration.zml (expensive)
                ConfigurationDescriptor projectDescriptor = project.projectDescriptorProvider.getConfigurationDescriptor();
                if (projectDescriptor == null) {
                    // Something serious wrong. Return nothing...
                    return subProjects;
                }
                subProjectLocations = ((MakeConfigurationDescriptor) projectDescriptor).getSubprojectLocations();
            }

            FileObject baseDir = project.getProjectDirectory();
            for (String loc : subProjectLocations) {
                String location = CndPathUtilities.toAbsolutePath(baseDir, loc);
                try {
		    FileObject fo = RemoteFileUtil.getFileObject(baseDir, location);
                    if (fo != null && fo.isValid()) {
                        fo = CndFileUtils.getCanonicalFileObject(fo);
                    }
                    if (fo != null && fo.isValid()) {
                        Project subProject = ProjectManager.getDefault().findProject(fo);
                        if (subProject != null) {
                            subProjects.add(subProject);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Cannot find subproject in '" + location + "' " + e); // FIXUP // NOI18N
                }
            }

            return subProjects;
        }

        //Remove a listener to changes in the set of subprojects.
        @Override
        public void removeChangeListener(ChangeListener listener) {
        }
    }
    /**
     * if specified => project name will have information about directory in project view
     */
    private final static String PROJECT_NAME_WITH_HIDDEN_PATHS = System.getProperty("cnd.project.name.hidden.paths"); //NOI18N
    private final static int PROJECT_NAME_NUM_SHOWN_FOLDERS = Integer.getInteger("cnd.project.name.folders.num", 1); //NOI18N

    interface InfoInterface extends ProjectInformation, PropertyChangeListener {

        void firePropertyChange(String prop);

        void setName(String name);
    }

    private final static class Info implements InfoInterface {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private String name;
        private final MakeProjectImpl project;

        Info(MakeProjectImpl prj) {
            this.project = prj;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
                firePropertyChange(ProjectInformation.PROP_ICON);
            }
        }

        @Override
        public void firePropertyChange(String prop) {
            if (ProjectInformation.PROP_NAME.equals(prop)) {
                name = null;
            }
            pcs.firePropertyChange(prop, null, null);
        }

        @Override
        public String getName() {
            return MakeProjectImpl.getUsablePropertyName(_getName());
        }

        /** Return configured project name. */
        private String _getName() {
            String res = name;
            if (res == null) {
                res = getNameImpl();
                name = res;
            }
            return res;
        }

        /** Return configured project name. */
        private String getNameImpl() {
            return ProjectManager.mutex().readAccess((Mutex.Action<String>) () -> {
                Element data = project.helper.getPrimaryConfigurationData(true);
                Element nameEl =  getNameElement(data);
                if (nameEl != null) {
                    NodeList nl = nameEl.getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                FileObject fo = project.getProjectDirectory();
                if (fo != null && fo.isValid()) {
                    return fo.getNameExt();
                }
                return "???"; // NOI18N
            });
        }

        @Override
        public void setName(final String name) {
            ProjectManager.mutex().writeAccess((Mutex.Action<Void>) () -> {
                Element data = project.helper.getPrimaryConfigurationData(true);
                Element nameEl =  getNameElement(data);
                if (nameEl != null) {
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
                    data.insertBefore(nameEl, data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                project.helper.putPrimaryConfigurationData(data, true);
                return null;
            });
            // reinit cache
            _getName();
        }

        private Element getNameElement(Element data) {
            NodeList nl = data.getElementsByTagNameNS(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectTypeImpl.PROJECT_CONFIGURATION__NAME_NAME);
            if (nl.getLength() > 0) {
                for(int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i) instanceof Element) {
                        Element e = (Element) nl.item(i);
                        Node parentNode = e.getParentNode();
                        if (parentNode != null && parentNode.getLocalName().equals(MakeProjectTypeImpl.PROJECT_CONFIGURATION_NAME)) {
                            return e;
                        }
                    }
                }
            }
            return null;
        }
        
        @Override
        public String getDisplayName() {
            String aName = _getName();

            if (PROJECT_NAME_WITH_HIDDEN_PATHS != null) {
                FileObject fo = project.getProjectDirectory();
                if (fo != null && fo.isValid()) {
                    String prjDirDispName = FileUtil.getFileDisplayName(fo);
                    String[] split = PROJECT_NAME_WITH_HIDDEN_PATHS.split(":"); // NOI18N
                    for (String skipPath : split) {
                        if (prjDirDispName.startsWith(skipPath)) {
                            prjDirDispName = prjDirDispName.substring(skipPath.length());
                            break;
                        }
                    }
                    if (prjDirDispName.startsWith("/") || prjDirDispName.startsWith("\\")) { // NOI18N
                        prjDirDispName = prjDirDispName.substring(1);
                    }
                    int sep = 0;
                    for (int i = 0; i < PROJECT_NAME_NUM_SHOWN_FOLDERS; i++) {
                        int nextSep = prjDirDispName.indexOf('\\', sep);
                        nextSep = (nextSep == -1) ? prjDirDispName.indexOf('/', sep) : nextSep;
                        if (nextSep > 0) {
                            sep = nextSep + 1;
                        } else {
                            // name has less elements than asked
                            sep = prjDirDispName.length();
                            break;
                        }
                    }
                    if (sep > 0) {
                        prjDirDispName = prjDirDispName.substring(0, sep);
                    }
                    if (prjDirDispName.length() > 0) {
                        if (prjDirDispName.endsWith("/") || prjDirDispName.endsWith("\\")) { // NOI18N
                            prjDirDispName = prjDirDispName.substring(0, prjDirDispName.length() - 1);
                        }
                        aName = NbBundle.getMessage(getClass(), "PRJ_DISPLAY_NAME_WITH_FOLDER", aName, prjDirDispName); // NOI18N
                    }
                }
            }
//            if (OpenProjects.getDefault().isProjectOpen(MakeProject.this)){
//                DevelopmentHostConfiguration devHost = getDevelopmentHostConfiguration();
//                if (devHost != null && ! devHost.isLocalhost()) {
//                    name = NbBundle.getMessage(getClass(), "PRJ_DISPLAY_NAME",
//                            name, devHost.getHostDisplayName(false));
//                }
//            }
            return aName;
        }

        private int getProjectType() {
            int aProjectType = project.getActiveConfigurationType();
            if (aProjectType != -1) {
                return aProjectType;
            }
            return -1;
        }

        @Override
        public Icon getIcon() {
            int type = getProjectType();
            Icon icon = MakeConfigurationDescriptor.MAKEFILE_ICON;
            switch (type) {
                case MakeConfiguration.TYPE_MAKEFILE: {
                    MakeConfiguration activeConfiguration = project.getActiveConfiguration();
                    if (activeConfiguration != null) {
                        String outputValue = activeConfiguration.getOutputValue();
                        if (outputValue != null) {
                            if (outputValue.endsWith(".so") || // NOI18N
                                    outputValue.endsWith(".dll") || // NOI18N
                                    outputValue.endsWith(".dylib")) { // NOI18N
                                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/resources/projects-unmanaged-dynamic.png", false); // NOI18N
                            } else if (outputValue.endsWith(".a")) { // NOI18N
                                icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/resources/projects-unmanaged-static.png", false); // NOI18N
                            } else {
                                icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_MAKEFILE_ICON, false);
                            }
                        } else {
                            icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_MAKEFILE_ICON, false); // NOI18N
                        }
                    }
                    break;
                }
                case MakeConfiguration.TYPE_APPLICATION:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_APPLICATION_ICON, false);
                    break;
                case MakeConfiguration.TYPE_DB_APPLICATION:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_DB_APPLICATION_ICON, false);
                    break;
                case MakeConfiguration.TYPE_DYNAMIC_LIB:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_DYNAMIC_LIB_ICON, false);
                    break;
                case MakeConfiguration.TYPE_STATIC_LIB:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_STATIC_LIB_ICON, false);
                    break;
                case MakeConfiguration.TYPE_QT_APPLICATION:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_APPLICATION_ICON, false);
                    break;
                case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_DYNAMIC_LIB_ICON, false);
                    break;
                case MakeConfiguration.TYPE_QT_STATIC_LIB:
                    icon = ImageUtilities.loadImageIcon(MakeProjectTypeImpl.TYPE_QT_STATIC_LIB_ICON, false);
                    break;
                case MakeConfiguration.TYPE_CUSTOM:
                    MakeProjectCustomizer makeProjectCustomizer = project.getProjectCustomizer(project.getProjectCustomizerId());
                    if (makeProjectCustomizer != null) {
                        icon = ImageUtilities.loadImageIcon(makeProjectCustomizer.getIconPath(), false); // NOI18N
                    }
                    break;
            }
            return icon;
        }
        
//        private String getProjectCustomizerId() {
//            return getCustomizerIdFromProjectXML();
//        }
//        
//        private MakeProjectCustomizer getProjectCustomizer(String customizerId) {
//            MakeProjectCustomizer makeProjectCustomizer = null;
//            Collection<? extends MakeProjectCustomizer> mwc = Lookup.getDefault().lookupAll(MakeProjectCustomizer.class);
//            for (MakeProjectCustomizer instance : mwc) {
//                if (customizerId.equals(instance.getCustomizerId())) {
//                    makeProjectCustomizer = instance;
//                    break;
//                }
//            }
//            return makeProjectCustomizer;
//        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }

    private void onProjectOpened() {
        synchronized (openStateAndLock) {
            if (openStateAndLock.get()) {
                return;
            }
            notifyProjectStartActivity();
            final MyInterrupter interrupter = new MyInterrupter();
            interrupters.add(interrupter);
            final FileObject dir = getProjectDirectory();
            if (dir != null) { // high resistance mode paranoia
                final ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(dir);
                ConnectionHelper.INSTANCE.ensureConnection(env);
            }     
            helper.removeMakeProjectListener(MakeProjectImpl.this);
            projectDescriptorProvider.opening(interrupter);
            helper.addMakeProjectListener(MakeProjectImpl.this);
            checkNeededExtensions();
            MakeOptions.getInstance().addPropertyChangeListener(indexerListener);
            // project is in opened state
            openStateAndLock.set(true);
            // post-initialize configurations in external worker
            RP.post(() -> {
                synchronized (openStateAndLock) {
                    if (!openStateAndLock.get()) {
                        return;
                    }
                }
                projectDescriptorProvider.opened();
                createLaunchersFileIfNeeded(dir);
                synchronized (openStateAndLock) {
                    if (openStateAndLock.get()) {
                        if (nativeProject instanceof NativeProjectProvider) {
                            warmupNativeProject();
                            NativeProjectRegistry.getDefault().register(nativeProject);
                        }
                        registerClassPath(true);
                    }
                }
            });
        }
    }
    
    private void warmupNativeProject() {
        FileSystemProvider.warmup(
                FileSystemProvider.WarmupMode.RECURSIVE_LS,
                FileSystemProvider.getExecutionEnvironment(helper.getProjectDirectory()),
                nativeProject.getSourceRoots(),
                null);
        Set<String> extensions = new HashSet<>();
        extensions.addAll(cExtensions);
        extensions.addAll(cppExtensions);
        extensions.addAll(headerExtensions);
        FileSystemProvider.warmup(
                FileSystemProvider.WarmupMode.FILES_CONTENT,
                FileSystemProvider.getExecutionEnvironment(helper.getProjectDirectory()),
                nativeProject.getSourceRoots(),
                extensions);

        List<IncludePath> sysIncPaths = nativeProject.getSystemIncludePaths();
        if (sysIncPaths != null && !sysIncPaths.isEmpty()) {
            ExecutionEnvironment env = null;
            List<String> paths = new ArrayList<>(sysIncPaths.size());
            for (IncludePath fsp : sysIncPaths) {
                if (env == null) {
                    env = FileSystemProvider.getExecutionEnvironment(fsp.getFileSystem());
                } else {
                    CndUtils.assertTrue(env.equals(FileSystemProvider.getExecutionEnvironment(fsp.getFileSystem())));
                }
                paths.add(fsp.getFSPath().getPath());
            }
            FileSystemProvider.warmup(FileSystemProvider.WarmupMode.FILES_CONTENT, env, paths, null);
        }        
    }
    
    private void createLaunchersFileIfNeeded(final FileObject projectDir) {
        CndUtils.assertNonUiThread();
        if (projectDir == null) {
            return;
        }
        try {
            FileObject projectPrivateFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_PRIVATE_FOLDER);
            if (projectPrivateFolder == null) {
                FileObject projectFolder = projectDir.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                if (projectFolder == null || !projectFolder.canWrite()) {
                    return;
                }
                projectPrivateFolder = projectFolder.createFolder(MakeConfiguration.PRIVATE_FOLDER);
            }
            FileObject launchers = projectPrivateFolder.getFileObject(LaunchersProjectMetadataFactory.NAME);

            if (!projectPrivateFolder.canWrite() || (launchers != null && launchers.isValid())) {
                return;
            }

            String resource = "/org/netbeans/modules/cnd/makeproject/launchers/resources/simple-launcher.template"; // NOI18N

            URL url = MakeConfiguration.class.getResource(resource);
            FileObject fo = URLMapper.findFileObject(url);
            fo.copy(projectPrivateFolder, "launcher", "properties"); // NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "error on creating launchers file ", ex);
        }
    }
    
    private void notifyProjectStartActivity() {
        Lookup.getDefault().lookupAll(MakeProjectLife.class).forEach((service) -> {
            service.start(this);
        });
    }

    public boolean isDeleted() {
        return isDeleted.get() || isDeleting.get();
    }
    
    void setDeleted() {
        LOGGER.log(Level.FINE, "set deleted MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
        isDeleted.set(true);
    }

    void setDeleting(boolean value) {
        LOGGER.log(Level.FINE, "set deleting MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
        isDeleting.set(value);
    }

    private void onProjectClosed() {
        synchronized (openStateAndLock) {
            if (!openStateAndLock.get()) {
                LOGGER.log(Level.WARNING, "on project close for not opened MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
                return;
            }
            Iterator<MyInterrupter> iterator = interrupters.iterator();
            while(iterator.hasNext()) {
                iterator.next().cancel();
            }
            LOGGER.log(Level.FINE, "on project close MakeProject@{0} {1}", new Object[]{System.identityHashCode(MakeProjectImpl.this), helper.getProjectDirectory()}); // NOI18N
            helper.removeMakeProjectListener(this);
            save();
            MakeOptions.getInstance().removePropertyChangeListener(indexerListener);
            MakeProjectFileProvider.removeSearchBase(this);
            // project is in closed state
            openStateAndLock.set(false);
            RP.post(() -> {
                synchronized (openStateAndLock) {
                    if (openStateAndLock.get()) {
                        return;
                    }
                }
                synchronized (openStateAndLock) {
                    if (!openStateAndLock.get()) {
                        if (nativeProject instanceof NativeProjectProvider) {
                            NativeProjectRegistry.getDefault().unregister(nativeProject, isDeleted());
                        }
                        registerClassPath(false);
                        projectDescriptorProvider.closed();
                        notifyProjectStopActivity();
                    }
                }
            });
        }
    }

    private void notifyProjectStopActivity() {
        Lookup.getDefault().lookupAll(MakeProjectLife.class).forEach((service) -> {
            service.stop(this);
        });
    }

    public void save() {
        synchronized (openStateAndLock) {
            if (!isDeleted.get() && !isDeleting.get()) {
                if (projectDescriptorProvider.gotDescriptor()) {
                    projectDescriptorProvider.getConfigurationDescriptor().save();
                }
            }
        }
    }

    private final static class ProjectOpenedHookImpl extends ProjectOpenedHook {
        private final MakeProjectImpl project;
        
        ProjectOpenedHookImpl(MakeProjectImpl prj) {
            this.project = prj;
        }

        @Override
        protected void projectOpened() {
            project.onProjectOpened();            
        }

        @Override
        protected void projectClosed() {
            project.onProjectClosed();
        }
    }

    private final static class MakeArtifactProviderImpl implements MakeArtifactProvider {
        private final MakeProjectImpl project;

        private MakeArtifactProviderImpl(MakeProjectImpl prj) {
            this.project = prj;
        }                

        @Override
        public MakeArtifact[] getBuildArtifacts() {
            List<MakeArtifact> artifacts = new ArrayList<>();

            MakeConfigurationDescriptor projectDescriptor = project.projectDescriptorProvider.getConfigurationDescriptor();
            if (projectDescriptor != null) {
                Configuration[] confs = projectDescriptor.getConfs().toArray();
                for (int i = 0; i < confs.length; i++) {
                    MakeConfiguration makeConfiguration = (MakeConfiguration) confs[i];
                    artifacts.add(new MakeArtifact(projectDescriptor, makeConfiguration));
                }
            }
            return artifacts.toArray(new MakeArtifact[artifacts.size()]);
        }
    }


    /** NPE-safe method for getting active DevelopmentHostConfiguration */
    public DevelopmentHostConfiguration getDevelopmentHostConfiguration() {
        MakeConfiguration conf = getActiveConfiguration();
        if (conf != null) {
            return conf.getDevelopmentHost();
        }
        return null;
    }

    private static final class RemoteProjectImpl implements RemoteProject {
        private final MakeProjectImpl project;

        private RemoteProjectImpl(MakeProjectImpl prj) {
            this.project = prj;
        }
        
        @Override
        public ExecutionEnvironment getDevelopmentHost() {
            DevelopmentHostConfiguration devHost = project.getDevelopmentHostConfiguration();
            return (devHost == null) ? null : devHost.getExecutionEnvironment();
        }

        @Override
        public ExecutionEnvironment getSourceFileSystemHost() {
            return FileSystemProvider.getExecutionEnvironment(project.helper.getProjectDirectory());
        }

        @Override
        public FileSystem getSourceFileSystem() {
            try {
                return project.getProjectDirectory().getFileSystem();
            } catch (FileStateInvalidException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public RemoteSyncFactory getSyncFactory() {
            MakeConfiguration activeConfiguration = project.getActiveConfiguration();
            if (activeConfiguration != null) {
                if (CndFileUtils.isLocalFileSystem(activeConfiguration.getBaseFSPath().getFileSystem())) {
                    return activeConfiguration.getRemoteSyncFactory();
                } else {
                    return RemoteSyncFactory.fromID(RemoteProject.FULL_REMOTE_SYNC_ID);
                }
            } else {
                return null;
            }
        }

        @Override
        public String getSourceBaseDir() {
            return project.helper.getProjectDirectory().getPath();
        }

        @Override
        public FileObject getSourceBaseDirFileObject() {
            return project.getProjectDirectory();
        }
    }

    private static final class ToolchainProjectImpl implements ToolchainProject {
        private final MakeProjectImpl project;

        private ToolchainProjectImpl(MakeProjectImpl prj) {
            this.project = prj;
        }

        @Override
        public CompilerSet getCompilerSet() {
            MakeConfiguration conf = project.getActiveConfiguration();
            if (conf != null) {
                return conf.getCompilerSet().getCompilerSet();
            }
            return null;
        }
    }

    private static final class MutableCP implements ClassPathImplementation, ChangeListener {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final MakeSources sources;
        private List<PathResourceImplementation> resources = null;
        private long eventId = 0;
        private ClassPath[] classpath = null;

        public MutableCP(MakeSources sources) {
            this.sources = sources;
            this.sources.addChangeListener(WeakListeners.change(MutableCP.this, this.sources));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            final long currentEventId;
            synchronized (this) {
                if (resources != null) {
                    return resources;
                }
                currentEventId = eventId;
            }

            List<PathResourceImplementation> list = new LinkedList<>();
            SourceGroup[] groups = sources.getSourceGroups("generic"); // NOI18N
            for (SourceGroup g : groups) {
                FileObject rootFolder = g.getRootFolder();
                //bz#215822 - exception when starting IDE where expected drive is missing
                //should not add invalid objects to the list of resources
                if (!rootFolder.isValid()) {
                    continue;
                }
                URL url = rootFolder.toURL();
                // A workaround for #196328 - IllegalArgumentException on save Project properties
                if (rootFolder.isFolder() && !url.toExternalForm().endsWith("/")) { //NOI18N
                    try {
                        URL url2 = new URL(url.toExternalForm() + '/'); //NOI18N                     
                        FileObject fo = URLMapper.findFileObject(url2);
                        if (fo != null && fo.equals(rootFolder)) {
                            url = url2;
                        }                            
                    } catch (MalformedURLException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                // end of workaround for #196328
                list.add(new PathResourceImpl(ClassPathSupport.createResource(url)));
            }

            synchronized (this) {
                if (currentEventId == eventId) {
                    resources = list;
                }
            }
            return list;
        }
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            synchronized (this) {
                resources = null;
                eventId++;
            }

            pcs.firePropertyChange(PROP_RESOURCES, null, null);
        }

        public synchronized ClassPath[] getClassPath() {
            if (classpath == null) {
                classpath = new ClassPath[]{ClassPathFactory.createClassPath(this)};
            }
            return classpath;
        }
    } // End of ClassPathImplementation class

    static final class PathResourceImpl implements FilteringPathResourceImplementation, PropertyChangeListener {

        private final PathResourceImplementation delegate;

        public PathResourceImpl(PathResourceImplementation delegate) {
            this.delegate = delegate;
            this.delegate.addPropertyChangeListener(PathResourceImpl.this);
        }

        private static final String IGNORE_PATTERN = ".*\\.(html|js|xhtml|css|xml|png|svg|json|java)$"; // NOI18N
        private static final Pattern ignoredFilesPattern = Pattern.compile(IGNORE_PATTERN);

        @Override
        public boolean includes(URL root, String resource) {
            if (ignoredFilesPattern.matcher(resource).find()) {
                return false;
            }
            return !CndFileVisibilityQuery.getDefault().isIgnored(resource);
        }

        @Override
        public URL[] getRoots() {
            return delegate.getRoots();
        }

        @Override
        public ClassPathImplementation getContent() {
            return delegate.getContent();
        }
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange(new PropertyChangeEvent(this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue()));
        }
    }

    private final static class IndexerOptionsListener implements PropertyChangeListener {
        private final MakeProjectImpl project;

        private IndexerOptionsListener(MakeProjectImpl prj) {
            this.project = prj;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (FullFileIndexer.FULL_FILE_INDEXER.equals(evt.getPropertyName())) {
                project.registerClassPath(Boolean.TRUE.equals(evt.getNewValue()));
            }
        }
    }
    
    private static final class MyInterrupter implements Interrupter, Cancellable {
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        @Override
        public boolean cancelled() {
            return cancelled.get();
        }

        @Override
        public boolean cancel() {
            cancelled.set(true);
            return true;
        }
    }    
}
