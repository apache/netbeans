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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener.Artifact;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSClientSupport;
import org.netbeans.modules.web.project.jaxws.WebProjectJAXWSSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor.Task;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.WebLogicalViewProvider;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.javaee.project.api.ant.ArtifactCopyOnSaveSupport;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategyResolverFactory;
import org.netbeans.modules.javaee.project.api.PersistenceProviderSupplierImpl;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.javaee.project.api.WhiteListUpdater;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifierSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.ConfigSupport.DeployOnSaveListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.DeployOnSaveSupport;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbJarSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettingConstants;
import org.netbeans.modules.javaee.project.api.ant.AntProjectUtil;
import org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.ui.api.CssPreprocessorsUI;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.web.project.classpath.DelagatingProjectClassPathModifierImpl;
import org.netbeans.modules.web.project.classpath.WebProjectLibrariesModifierImpl;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilter;
import org.netbeans.modules.web.project.spi.BrokenLibraryRefFilterProvider;
import org.netbeans.modules.web.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.web.spi.webmodule.WebPrivilegedTemplates;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.whitelist.support.WhiteListQueryMergerSupport;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataObject;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 * Represents one plain Web project.
 * @author Jesse Glick, et al., Pavel Buzek
 * @author kaktus
 */
public final class WebProject implements Project {

    private static final Logger LOGGER = Logger.getLogger(WebProject.class.getName());

    private static final Icon WEB_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/web/project/ui/resources/webProjectIcon.gif", false); // NOI18

    private static final Pattern TLD_PATTERN = Pattern.compile("(META-INF/.*\\.tld)|(META-INF/tlds/.*\\.tld)");

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    private final ProjectWebModule webModule;
    private final CopyOnSaveSupport copyOnSaveSupport;
    private final ClientSideDevelopmentSupport easelSupport;
    private final ArtifactCopyOnSaveSupport artifactSupport;
    private final DeployOnSaveSupport deployOnSaveSupport;
    private final EjbJarProvider webEjbJarProvider;
    private final EjbJar apiEjbJar;
    private WebModule apiWebModule;
    private WebServicesSupport apiWebServicesSupport;
    private JAXWSSupport apiJaxwsSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    private WebContainerImpl enterpriseResourceSupport;
    private FileWatch webPagesFileWatch;
    private FileWatch webInfFileWatch;
    private PropertyChangeListener j2eePlatformListener;
    private PropertyChangeListener enterpriseBeansListener;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final UpdateHelper updateHelper;
    private final UpdateProjectImpl updateProject;
    private final AuxiliaryConfiguration aux;
    private final DelagatingProjectClassPathModifierImpl cpMod;
    private final WebProjectLibrariesModifierImpl libMod;
    private final ClassPathProviderImpl cpProvider;
    private ClassPathUiSupport.Callback classPathUiSupportCallback;
    private WhiteListUpdater whiteListUpdater;
    private CssPreprocessorsSupport cssSupport;

    private AntBuildExtender buildExtender;

    // set to true when project customizer is being closed and changes persisted
    private final ThreadLocal<Boolean> projectPropertiesSave;

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
            easelSupport.close();
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }

    };

    private class FileWatch implements AntProjectListener, FileChangeListener {

        private String propertyName;

        private FileObject fileObject = null;
        private boolean watchRename = false;

        public FileWatch(String property) {
            this.propertyName = property;
        }

        public void init() {
            helper.addAntProjectListener(this);
            updateFileChangeListener();
        }

        public void reset() {
            helper.removeAntProjectListener(this);
            setFileObject(null);
        }

        public void updateFileChangeListener() {
            File resolvedFile;
            FileObject fo = null;
            String propertyValue = helper.getStandardPropertyEvaluator().getProperty(propertyName);
            if (propertyValue != null) {
                String resolvedPath = helper.resolvePath(propertyValue);
                resolvedFile = new File(resolvedPath).getAbsoluteFile();
                File f = resolvedFile;
                while (f != null && (fo = FileUtil.toFileObject(f)) == null) {
                    f = f.getParentFile();
                }
                watchRename = f == resolvedFile;
            } else {
                resolvedFile = null;
                watchRename = false;
            }
            setFileObject(fo);
        }

        private void setFileObject(FileObject fo) {
            if (!isEqual(fo, fileObject)) {
                if (fileObject != null) {
                    fileObject.removeFileChangeListener(this);
                }
                fileObject = fo;
                if (fileObject != null) {
                    fileObject.addFileChangeListener(this);
                }
            }
        }

        private boolean isEqual(Object object1, Object object2) {
            if (object1 == object2) {
                return true;
            }
            if(object1 == null) {
                return false;
            }
            return object1.equals(object2);
        }

        // AntProjectListener

        @Override
        public void configurationXmlChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        @Override
        public void propertiesChanged(AntProjectEvent ev) {
            updateFileChangeListener();
        }

        // FileChangeListener

        @Override
        public void fileFolderCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            updateFileChangeListener();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            updateFileChangeListener();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            updateFileChangeListener();
        }

        @Override
        public void fileRenamed(final FileRenameEvent fe) {
            if(watchRename && fileObject.isValid()) {
                String prop = helper.getStandardPropertyEvaluator().getProperty(propertyName);
                if (prop == null) {
                    return;
                }
                final File f = new File(prop);
                if(f.getName().equals(fe.getName())) {
                    ProjectManager.mutex().postWriteRequest(new Runnable() {
                        public void run() {
                            EditableProperties properties = new EditableProperties(true);
                            properties.setProperty(propertyName, new File(f.getParentFile(), fe.getFile().getName()).getPath());
                            Utils.updateProperties(helper, AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
                            try {
                                ProjectManager.getDefault().saveProject(WebProject.this);
                                updateFileChangeListener();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            }
            updateFileChangeListener();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    };

    WebProject(final AntProjectHelper helper) throws IOException {
        this.projectPropertiesSave = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
        this.helper = helper;
        aux = helper.createAuxiliaryConfiguration();
        updateProject = new UpdateProjectImpl(this, this.helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        updateProject.setUpdateHelper(updateHelper);
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, aux, eval);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new WebExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        this.cpProvider = new ClassPathProviderImpl(this.helper, evaluator(), getSourceRoots(),getTestSourceRoots());
        webModule = new ProjectWebModule (this, updateHelper, cpProvider);
        apiWebModule = WebModuleFactory.createWebModule(new WebModuleImpl2(webModule));
        webEjbJarProvider = new EjbJarProvider(webModule, cpProvider);
        apiEjbJar = EjbJarFactory.createEjbJar(webEjbJarProvider);
        WebProjectWebServicesSupport webProjectWebServicesSupport = new WebProjectWebServicesSupport(this, helper, refHelper);
        WebProjectJAXWSSupport jaxwsSupport = new WebProjectJAXWSSupport(this, helper);
        WebProjectJAXWSClientSupport jaxWsClientSupport = new WebProjectJAXWSClientSupport(this, helper);
        WebProjectWebServicesClientSupport webProjectWebServicesClientSupport = new WebProjectWebServicesClientSupport(this, helper, refHelper);
        apiWebServicesSupport = WebServicesSupportFactory.createWebServicesSupport (webProjectWebServicesSupport);
        apiJaxwsSupport = JAXWSSupportFactory.createJAXWSSupport(jaxwsSupport);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport (webProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        enterpriseResourceSupport = new WebContainerImpl(this, refHelper, helper);
        ClassPathModifier cpModTemp = new ClassPathModifier(this, this.updateHelper, eval, refHelper,
            new ClassPathSupportCallbackImpl(helper), createClassPathModifierCallback(), getClassPathUiSupportCallback());
        libMod = new WebProjectLibrariesModifierImpl(this, this.updateHelper, eval, refHelper);
        cpMod = new DelagatingProjectClassPathModifierImpl(cpModTemp, libMod);
        easelSupport = ClientSideDevelopmentSupport.createInstance(this, WebProjectType.TYPE, Utils.USG_LOGGER_NAME);
        cssSupport = new CssPreprocessorsSupport(this);
        lookup = createLookup(aux, cpProvider);
        copyOnSaveSupport = new CopyOnSaveSupport();
        artifactSupport = new ArtifactCopySupport();
        deployOnSaveSupport = new DeployOnSaveSupportProxy();
        webPagesFileWatch = new FileWatch(WebProjectProperties.WEB_DOCBASE_DIR);
        webInfFileWatch = new FileWatch(WebProjectProperties.WEBINF_DIR);
        // whitelist updater listens on project properties and pays attention to whitelist changes
        whiteListUpdater = new WhiteListUpdaterImpl(this);
        WindowManager windowManager = WindowManager.getDefault();
        windowManager.addWindowSystemListener(WeakListeners.create(WindowSystemListener.class, windowSystemListener, windowManager));
    }

    public void setProjectPropertiesSave(boolean value) {
        this.projectPropertiesSave.set(value);
    }

    public DelagatingProjectClassPathModifierImpl getClassPathModifier() {
        return cpMod;
    }

    public WebProjectLibrariesModifierImpl getLibrariesModifier() {
        return libMod;
    }

    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return deployOnSaveSupport;
    }

    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String[] classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                assert !classPathProperty[0].equals(WebProjectProperties.J2EE_PLATFORM_CLASSPATH);
                return classPathProperty[0];
            }

            public String getElementName(String classpathProperty) {
                if (ProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
                    return ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES;
                }
                return null;
            }
        };
    }

    public synchronized ClassPathUiSupport.Callback getClassPathUiSupportCallback() {
        if (classPathUiSupportCallback == null) {
            classPathUiSupportCallback = new ClassPathUiSupport.Callback() {
                public void initItem(ClassPathSupport.Item item) {
                    switch (item.getType()) {
                        case ClassPathSupport.Item.TYPE_JAR:
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT,
                                    item.getResolvedFile().isDirectory() ?
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_DIR :
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB);
                            break;
                        case ClassPathSupport.Item.TYPE_LIBRARY:
                            if (item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                                break;
                            }
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT,
                                    Utils.isLibraryDirectoryBased(item) ?
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_DIR :
                                        ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB);
                            break;
                        default:
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT,
                                    ClassPathSupportCallbackImpl.PATH_IN_WAR_LIB);
                    }
                }
            };

        }
        return classPathUiSupportCallback;
    }

    public UpdateProjectImpl getUpdateImplementation() {
        return updateProject;
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }

    public String toString() {
        return "WebProject[" + getProjectDirectory() + "]"; // NOI18N
    }

    private PropertyEvaluator createEvaluator() {
        // XXX might need to add a custom evaluator to handle active platform substitutions... TBD
        helper.getStandardPropertyEvaluator();//workaround for issue for #181253, need to call before custom creation
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH),
                UPDATE_PROPERTIES);
    }

    private static final PropertyProvider UPDATE_PROPERTIES;

    static {
        Map<String, String> defs = new HashMap<String, String>();

        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
        defs.put(ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
        defs.put("javac.test.processorpath", "${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); // NOI18N

        UPDATE_PROPERTIES = PropertyUtils.fixedPropertyProvider(defs);
    }

    public PropertyEvaluator evaluator() {
        return eval;
    }

    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), WebProjectProperties.SOURCE_ENCODING);
        final WebLogicalViewProvider lvp = new WebLogicalViewProvider(this, this.updateHelper, evaluator (), refHelper, webModule);
        Lookup base = Lookups.fixed(new Object[] {
            QuerySupport.createProjectInformation(updateHelper, this, WEB_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            refHelper.createSubprojectProvider(),
            new ProjectWebModuleProvider (),
            new ProjectWebServicesSupportProvider(),
            webModule, //implements J2eeModuleProvider
            // FIXME this is just fallback for code searching for the old SPI in lookup
            // remove in next release
            new WebModuleImpl(apiWebModule),
            enterpriseResourceSupport,
            lvp,
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper),
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                    new String[]{"build.classes.dir", "dist.war"}, new String[]{"build.test.classes.dir"}),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator(), new String[]{"build.classes.dir", "dist.war"}),
            QuerySupport.createAnnotationProcessingQuery(helper, eval, ProjectProperties.ANNOTATION_PROCESSING_ENABLED, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery2(evaluator()),
            QuerySupport.createSources(this, helper, evaluator(),
                    getSourceRoots(),
                    getTestSourceRoots(),
                    Roots.propertyBased(new String[] {WebProjectProperties.SOURCE_ROOT}, new String[] {NbBundle.getMessage(WebProject.class, "LBL_Node_WebModule")}, false, null, null),
                    Roots.propertyBased(new String[] {WebProjectProperties.WEB_DOCBASE_DIR}, new String[] {NbBundle.getMessage(WebProject.class, "LBL_Node_DocBase")}, true, WebProjectConstants.TYPE_DOC_ROOT, null),
                    Roots.propertyBased(new String[] {WebProjectProperties.WEBINF_DIR}, new String[] {NbBundle.getMessage(WebProject.class, "LBL_Node_WebInf")}, false, WebProjectConstants.TYPE_WEB_INF, null),
                    Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, WebProjectProperties.DIST_DIR)),
            QuerySupport.createSharabilityQuery2(helper, evaluator(), getSourceRoots(),
                getTestSourceRoots(), WebProjectProperties.WEB_DOCBASE_DIR),
            LookupProviderSupport.createSharabilityQueryMerger(),
            new RecommendedTemplatesImpl(this),
            new CoSAwareFileBuiltQueryImpl(QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()), this),
            ProjectClassPathModifier.extenderForModifier(cpMod),
            buildExtender,
            cpMod,
            cpMod.getClassPathModifier(),
            new WebProjectOperations(this),
            new WebPersistenceProvider(this, evaluator(), cpProvider),
            new PersistenceProviderSupplierImpl(this),
            EntityManagerGenerationStrategyResolverFactory.createInstance(this),
            new WebJPADataSourceSupport(this),
            ServerUtil.createServerStatusProvider(getWebModule()),
            new WebJPAModuleInfo(this),
            new WebJPATargetInfo(this),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            LookupProviderSupport.createActionProviderMerger(),
            WhiteListQueryMergerSupport.createWhiteListQueryMerger(),
            new WebPropertyEvaluatorImpl(evaluator()),
            WebProject.this, // never cast an externally obtained Project to WebProject - use lookup instead
            libMod,
            encodingQuery,
            new CreateFromTemplateAttributesImpl(helper, encodingQuery),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createBinaryForSourceQueryImplementation(getSourceRoots(), getTestSourceRoots(), helper, eval),
            new ProjectWebRootProviderImpl(this),
            easelSupport,
            new JavaEEProjectSettingsImpl(this),
            BrokenReferencesSupport.createReferenceProblemsProvider(helper, refHelper, eval, lvp.getBreakableProperties(), lvp.getPlatformProperties()),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(helper, eval, PlatformUpdatedCallBackImpl.create(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, updateHelper), JavaPlatform.getDefault().getSpecification().getName(), ProjectProperties.PLATFORM_ACTIVE, ProjectProperties.JAVAC_SOURCE, ProjectProperties.JAVAC_TARGET),
            CssPreprocessorsUI.getDefault().createProjectProblemsProvider(this),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
        });

        Lookup ee6 = Lookups.fixed(new Object[]{
            EjbJarSupport.createEjbJarProvider(this, apiEjbJar),
            EjbJarSupport.createEjbJarsInProject(apiEjbJar)
        });

        WebProjectLookup wpl = new WebProjectLookup(this, base, ee6, new WebProjectBrowserProvider(this));
        eval.addPropertyChangeListener(wpl);
        lookup = wpl;
        return LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-web-project/Lookup"); //NOI18N
    }

    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }

    String getBuildXmlName () {
        String storedName = helper.getStandardPropertyEvaluator ().getProperty (WebProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }

    // Package private methods -------------------------------------------------

    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty(ProjectProperties.BUILD_TEST_CLASSES_DIR);
        if (testClassesDir == null) {
            return null;
        }
        return helper.resolveFile(testClassesDir);
    }

    public ProjectWebModule getWebModule () {
        return webModule;
    }

    public WebModule getAPIWebModule () {
        return apiWebModule;
    }

    public EjbJar getAPIEjbJar() {
        return apiEjbJar;
    }

    WebServicesSupport getAPIWebServicesSupport () {
            return apiWebServicesSupport;
    }

    JAXWSSupport getAPIJAXWSSupport () {
            return apiJaxwsSupport;
    }

    WebServicesClientSupport getAPIWebServicesClientSupport () {
            return apiWebServicesClientSupport;
    }

    JAXWSClientSupport getAPIJAXWSClientSupport () {
            return apiJAXWSClientSupport;
    }

    /** Return configured project name. */
    public String getName() {
        return ProjectUtils.getInformation(this).getName();
    }

    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }

    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    updateClasspath(platform);
                }
            }
        };
        platform.addPropertyChangeListener(j2eePlatformListener);
    }

    public void unregisterJ2eePlatformListener(J2eePlatform platform) {
        if (j2eePlatformListener != null) {
            platform.removePropertyChangeListener(j2eePlatformListener);
        }
    }

    private void updateClasspath(final J2eePlatform platform) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                EditableProperties ep = helper.getProperties(
                        AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                EditableProperties projectProps = helper.getProperties(
                        AntProjectHelper.PROJECT_PROPERTIES_PATH);

                    Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(platform);
                    String classpath = J2EEProjectProperties.toClasspathString(
                            ClasspathUtil.getJ2eePlatformClasspathEntries(WebProject.this, null), roots);
                    if (roots != null) {
                        projectProps.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                    } else {
                        ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                    }
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    try {
                        ProjectManager.getDefault().saveProject(WebProject.this);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                return null;
            }
        });
    }

    // Private innerclasses ----------------------------------------------------

    public static void makeSureProjectHasJspCompilationLibraries(final ReferenceHelper refHelper) {
        if (refHelper.getProjectLibraryManager() == null) {
            return;
        }
        ProjectManager.mutex().writeAccess(new Runnable() {

            public void run() {
                Library lib = refHelper.getProjectLibraryManager().getLibrary("jsp-compilation");

                // #198056 - force libraries update by deleting old versions first:
                if (lib != null) {
                    boolean oldNB69ver = lib.getURIContent("classpath").toString().contains("glassfish-jspparser-2.0.jar");
                    if (oldNB69ver) {
                        try {
                            refHelper.getProjectLibraryManager().removeLibrary(lib);
                            lib = refHelper.getProjectLibraryManager().getLibrary("jsp-compiler");
                            refHelper.getProjectLibraryManager().removeLibrary(lib);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }

                lib = refHelper.getProjectLibraryManager().getLibrary("jsp-compiler");
                if (lib == null) {
                    try {
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("jsp-compiler")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                lib = refHelper.getProjectLibraryManager().getLibrary("jsp-compilation");
                if (lib == null) {
                    try {
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("jsp-compilation")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                lib = refHelper.getProjectLibraryManager().getLibrary("jsp-compilation-syscp");
                if (lib == null) {
                    try {
                        refHelper.copyLibrary(LibraryManager.getDefault().getLibrary("jsp-compilation-syscp")); // NOI18N
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        ProjectXmlSavedHookImpl() {}

        protected void projectXmlSaved() throws IOException {
            int state = genFilesHelper.getBuildScriptState(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                WebProject.class.getResource("resources/build-impl.xsl"));
            final Boolean projectPropertiesSave = WebProject.this.projectPropertiesSave.get();
            if ((projectPropertiesSave && (state & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) ||
                state == (GeneratedFilesHelper.FLAG_UNKNOWN | GeneratedFilesHelper.FLAG_MODIFIED |
                    GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_OLD_STYLESHEET)) {  //missing genfiles.properties
                try {
                    AntProjectUtil.backupBuildImplFile(updateHelper);
                    genFilesHelper.generateBuildScriptFromStylesheet(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"));
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (IllegalStateException e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                                  org.netbeans.modules.web.project.WebProject.class.getResource("resources/build-impl.xsl"),
                                                  false);
            }
            genFilesHelper.refreshBuildScript(
                getBuildXmlName (),
                WebProject.class.getResource("resources/build.xsl"),false);
        }

    }

    final class ProjectOpenedHookImpl extends ProjectOpenedHook {

        ProjectOpenedHookImpl() {}

        @Override
        @NbBundle.Messages("ERR_ProjectReadOnly=The project folder is read-only.")
        protected void projectOpened() {
            evaluator().addPropertyChangeListener(WebProject.this.webModule);

            WebLogicalViewProvider logicalViewProvider = WebProject.this.getLookup().lookup(WebLogicalViewProvider.class);
            if (logicalViewProvider != null) {
                logicalViewProvider.initialize();
            }

            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run()  {
                                updateProject();
                            }
                        });
                    }
                });
            } catch (IOException e) {
                // #222721 - Provide a better error message in case of read-only location of project.
                if (!WebProject.this.getProjectDirectory().canWrite()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_ProjectReadOnly());
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                }
            }

            try {

                if (webModule.getDeploymentDescriptor() == null
                        && webModule.getConfigSupport().isDescriptorRequired()) {
                    DDHelper.createWebXml(webModule.getJ2eeProfile(), webModule.getWebInf());
                }

                //DDDataObject initialization to be ready to listen on changes (#45771)

                // web.xml
                try {
                    FileObject ddFO = webModule.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject dobj = DataObject.find(ddFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING
                }

                // ejb-jar.xml
                try {
                    FileObject ejbDdFO = webEjbJarProvider.getDeploymentDescriptor();
                    if (ejbDdFO != null) {
                        DataObject ejbdobj = DataObject.find(ejbDdFO);
                    }
                } catch (org.openide.loaders.DataObjectNotFoundException ex) {
                    //PENDING`
                }

                // Register copy on save support
                copyOnSaveSupport.initialize();

                // Check up on build scripts.
                if (updateHelper.isCurrent()) {
                    int flags = genFilesHelper.getBuildScriptState(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        WebProject.class.getResource("resources/build-impl.xsl"));
                    if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0
                        && (flags & GeneratedFilesHelper.FLAG_OLD_PROJECT_XML) != 0) {
                            AntProjectUtil.backupBuildImplFile(updateHelper);
                            genFilesHelper.generateBuildScriptFromStylesheet(
                                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                WebProject.class.getResource("resources/build-impl.xsl"));
                    } else {
                        genFilesHelper.refreshBuildScript(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            WebProject.class.getResource("resources/build-impl.xsl"), true);
                    }
                    genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        WebProject.class.getResource("resources/build.xsl"), true);

                    String servInstID = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    String serverType = null;
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                        WebProjectProperties.setServerInstance(WebProject.this, WebProject.this.updateHelper, servInstID);
                    } else {
                        // if there is some server instance of the type which was used
                        // previously do not ask and use it
                        serverType = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_TYPE);
                        if (serverType != null) {
                            String instanceID = J2EEProjectProperties.getMatchingInstance(serverType, Type.WAR, WebProject.this.getWebModule().getJ2eeProfile());
                            if (instanceID != null) {
                                WebProjectProperties.setServerInstanceInner(WebProject.this, WebProject.this.updateHelper, instanceID);
                                platform = Deployment.getDefault().getJ2eePlatform(instanceID);
                            }
                        }
                        if (platform == null) {
                            BrokenServerSupport.showAlert();
                        }
                    }
                    // UI Logging
                    Utils.logUI(NbBundle.getBundle(WebProject.class), "UI_WEB_PROJECT_OPENED", // NOI18N
                            new Object[] {(serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)), servInstID});
                    // Usage Logging
                    String serverName = ""; // NOI18N
                    try {
                        if (servInstID != null) {
                            serverName = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
                        }
                    }
                    catch (InstanceRemovedException ire) {
                        // do nothing
                    }
                    Profile profile = WebProject.this.getWebModule().getJ2eeProfile();
                    Utils.logUsage(WebProject.class, "USG_PROJECT_OPEN_WEB", new Object[] { serverName, profile }); // NOI18N
                }

            } catch (IOException e) {
                // #222721 - Provide a better error message in case of read-only location of project.
                if (!WebProject.this.getProjectDirectory().canWrite()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_ProjectReadOnly());
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    LOGGER.log(Level.INFO, null, e);
                }
            }

            webModule.getConfigSupport().addLibraryChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    String servInstID = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
                    J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                    if (platform != null) {
                        updateClasspath(platform);
                    }
                }
            });

            if (BrokenServerLibrarySupport.isBroken(WebProject.this)) {
                BrokenServerLibrarySupport.fixOrShowAlert(WebProject.this, new Runnable() {
                    @Override
                    public void run() {
                        WebLogicalViewProvider viewProvider = WebProject.this.getLookup().lookup(
                                WebLogicalViewProvider.class);
                        if (viewProvider != null) {
                            viewProvider.testBroken();
                        }
                    }
                });
            }

            // register project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));

            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            webModule.getConfigSupport().ensureConfigurationReady();

            //check the config context path
            String ctxRoot = webModule.getContextPath ();
            if (ctxRoot == null) {
                String sysName = getProjectDirectory ().getName (); //NOI18N
                sysName = Utils.createDefaultContext(sysName); //NOI18N
                webModule.setContextPath (sysName);
            }
            try {
                getAPIEjbJar().getMetadataModel().runReadActionWhenReady(new MetadataModelAction<EjbJarMetadata, Void>() {
                    public Void run(EjbJarMetadata metadata) throws Exception {
                        enterpriseBeansListener = new EnterpriseBeansListener(WebProject.this);
                        metadata.getRoot().getEnterpriseBeans().addPropertyChangeListener(enterpriseBeansListener);
                        return null;
                    }
                });
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            if (Boolean.parseBoolean(evaluator().getProperty(
                    WebProjectProperties.J2EE_COMPILE_ON_SAVE))) {
                Deployment.getDefault().enableCompileOnSaveSupport(webModule);
            }
            artifactSupport.enableArtifactSynchronization(true);

            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks()) {
                ProjectProblems.showAlert(WebProject.this);
            }
            if(apiWebServicesSupport.isBroken(WebProject.this)) {
                apiWebServicesSupport.showBrokenAlert(WebProject.this);
            }
            else if(WebServicesClientSupport.isBroken(WebProject.this)) {
                WebServicesClientSupport.showBrokenAlert(WebProject.this);
            }
            webPagesFileWatch.init();
            webInfFileWatch.init();

            CssPreprocessors.getDefault().addCssPreprocessorsListener(cssSupport);
        }

        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

            File buildProperties = new File(Places.getUserDirectory(), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

            //remove jaxws.endorsed.dir property
            ep.remove("jaxws.endorsed.dir");

            filterBrokenLibraryRefs();

            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
            if (props.getProperty(WebProjectProperties.J2EE_DEPLOY_ON_SAVE) == null) {
                String server = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
                props.setProperty(WebProjectProperties.J2EE_DEPLOY_ON_SAVE,
                    server == null ? "false" : DeployOnSaveUtils.isDeployOnSaveSupported(server));
            }

            if (props.getProperty(WebProjectProperties.J2EE_COMPILE_ON_SAVE) == null) {
                props.setProperty(WebProjectProperties.J2EE_COMPILE_ON_SAVE,
                        props.getProperty(WebProjectProperties.J2EE_DEPLOY_ON_SAVE));
            }

            // #134642 - use Ant task from copylibs library
            SharabilityUtility.makeSureProjectHasCopyLibsLibrary(helper, refHelper);

            // make sure that sharable project which requires JSP compilation has its own
            // copy of jsp-compiler and jsp-compilation library:
            if (helper.isSharableProject() && "true".equals(props.get(WebProjectProperties.COMPILE_JSPS))) {
                makeSureProjectHasJspCompilationLibraries(refHelper);
            }

            J2EEProjectProperties.removeObsoleteLibraryLocations(ep);
            J2EEProjectProperties.removeObsoleteLibraryLocations(props);

            //add webinf.dir required by 6.0 projects
            if (props.getProperty(WebProjectProperties.WEBINF_DIR) == null) {
                //we can do this because in previous versions WEB-INF was expected under docbase
                String web = props.get(WebProjectProperties.WEB_DOCBASE_DIR);
                props.setProperty(WebProjectProperties.WEBINF_DIR, web + "/WEB-INF"); //NOI18N
            }

            //add persistence.xml.dir introduced in 6.5 - see issue 143884 and 142164
            if (props.getProperty(WebProjectProperties.PERSISTENCE_XML_DIR) == null) {
                props.setProperty(WebProjectProperties.PERSISTENCE_XML_DIR, "${" + WebProjectProperties.CONF_DIR + "}");
            }



            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

            // update a dual build directory project to use a single directory
            if (updateHelper.isCurrent()) { // #113297, #118187
                // this operation should be safe in future as well - of course if properties with the same name weren't re-introduced
                props.remove("build.ear.web.dir");      // used to be WebProjectProperties.BUILD_EAR_WEB_DIR    // NOI18N
                props.remove("build.ear.classes.dir");  // used to be WebProjectProperties.BUILD_EAR_CLASSES_DIR    // NOI18N
            }
            // check debug.classpath - can be done every time, whenever
            String debugClassPath = props.getProperty(WebProjectProperties.DEBUG_CLASSPATH);
            props.setProperty(WebProjectProperties.DEBUG_CLASSPATH, Utils.correctDebugClassPath(debugClassPath));

            if (!props.containsKey(ProjectProperties.INCLUDES)) {
                props.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
            }
            if (!props.containsKey(ProjectProperties.EXCLUDES)) {
                props.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
            }
            if (!props.containsKey("build.generated.sources.dir")) { // NOI18N
                props.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
            }

            if (!props.containsKey(ProjectProperties.ANNOTATION_PROCESSING_ENABLED))props.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
            if (!props.containsKey(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR))props.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "true"); //NOI18N
            if (!props.containsKey(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS))props.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
            if (!props.containsKey(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST))props.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
            if (!props.containsKey(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT))props.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
            if (!props.containsKey(ProjectProperties.JAVAC_PROCESSORPATH))props.setProperty(ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
            if (!props.containsKey("javac.test.processorpath"))props.setProperty("javac.test.processorpath", "${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); // NOI18N

            // #207149
            if (!props.containsKey(WebProjectProperties.J2EE_COPY_STATIC_FILES_ON_SAVE)) {
                boolean b = Boolean.parseBoolean(props.getProperty(WebProjectProperties.J2EE_COMPILE_ON_SAVE));
                props.setProperty(WebProjectProperties.J2EE_COPY_STATIC_FILES_ON_SAVE, b ? "true" : "false");
            }

            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);

            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                // #222721 - Provide a better error message in case of read-only location of project.
                if (!WebProject.this.getProjectDirectory().canWrite()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_ProjectReadOnly());
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    Exceptions.printStackTrace(e);
                }
            }
        }

        /**
         * Filters the broken library references (see issue 110040).
         */
        private void filterBrokenLibraryRefs() {
            // filter the compilation CP
            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            List<ClassPathSupport.Item> toRemove = filterBrokenLibraryItems(cpMod.getClassPathSupport().itemsList(props.getProperty(ProjectProperties.JAVAC_CLASSPATH), WebProjectProperties.TAG_WEB_MODULE_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken classpath library references: " + toRemove);
                try {
                    ClassPathModifierSupport.handleLibraryClassPathItems(WebProject.this, getAntProjectHelper(), cpMod.getClassPathSupport(),
                            toRemove, ProjectProperties.JAVAC_CLASSPATH, WebProjectProperties.TAG_WEB_MODULE_LIBRARIES, ClassPathModifier.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
            // filter the additional (packaged) items
            // need to re-read the properites as the handleLibraryClassPathItems() might have changed them
            props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            toRemove = filterBrokenLibraryItems(libMod.getClassPathSupport().itemsList(props.getProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL), WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));
            if (!toRemove.isEmpty()) {
                LOGGER.log(Level.FINE, "Will remove broken additional library references: " + toRemove);
                try {
                    ClassPathModifierSupport.handleLibraryClassPathItems(WebProject.this, getAntProjectHelper(), cpMod.getClassPathSupport(),
                            toRemove, WebProjectProperties.WAR_CONTENT_ADDITIONAL, WebProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES, ClassPathModifier.REMOVE, false);
                } catch (IOException e) {
                    // should only occur when passing true as the saveProject parameter which we are not doing here
                    Exceptions.printStackTrace(e);
                }
            }
        }

        private List<ClassPathSupport.Item> filterBrokenLibraryItems(List<ClassPathSupport.Item> items) {
            List<ClassPathSupport.Item> toRemove = new LinkedList<ClassPathSupport.Item>();
            Collection<? extends BrokenLibraryRefFilter> filters = null;
            for (ClassPathSupport.Item item : items) {
                if (!item.isBroken() || item.getType() != ClassPathSupport.Item.TYPE_LIBRARY) {
                    continue;
                }
                String libraryName = ClassPathSupport.getLibraryNameFromReference(item.getReference());
                LOGGER.log(Level.FINE, "Broken reference to library: " + libraryName);
                if (filters == null) {
                    // initializing the filters lazily because usually they will not be needed anyway
                    // (most projects have no broken references)
                    filters = createFilters(WebProject.this);
                }
                for (BrokenLibraryRefFilter filter : filters) {
                    if (filter.removeLibraryReference(libraryName)) {
                        LOGGER.log(Level.FINE, "Will remove broken reference to library " + libraryName + " because of filter " + filter.getClass().getName());
                        toRemove.add(item);
                        break;
                    }
                }
            }
            return toRemove;
        }

        private List<BrokenLibraryRefFilter> createFilters(Project project) {
            List<BrokenLibraryRefFilter> filters = new LinkedList<BrokenLibraryRefFilter>();
            for (BrokenLibraryRefFilterProvider provider : Lookups.forPath("Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders").lookupAll(BrokenLibraryRefFilterProvider.class)) { // NOI18N
                BrokenLibraryRefFilter filter = provider.createFilter(project);
                if (filter != null) {
                    filters.add(filter);
                }
            }
            return filters;
        }

        @Override
        protected void projectClosed() {
            evaluator().removePropertyChangeListener(WebProject.this.webModule);

            webPagesFileWatch.reset();
            webInfFileWatch.reset();

            // listen to j2ee platform classpath changes
            String servInstID = evaluator().getProperty(WebProjectProperties.J2EE_SERVER_INSTANCE);
            if (servInstID != null) {
                try {
                    J2eePlatform platform = Deployment.getDefault().getServerInstance(servInstID).getJ2eePlatform();
                    if (platform != null) {
                        unregisterJ2eePlatformListener(platform);
                    }
                } catch (InstanceRemovedException ex) {
                    // ignore in this case
                }
            }

            if (enterpriseBeansListener != null){
                try {
                    getAPIEjbJar().getMetadataModel().runReadActionWhenReady(new MetadataModelAction<EjbJarMetadata, Void>() {
                        public Void run(EjbJarMetadata metadata) throws Exception {
                            metadata.getRoot().getEnterpriseBeans().removePropertyChangeListener(enterpriseBeansListener);
                            enterpriseBeansListener = null;
                            return null;
                        }
                    });
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            // remove ServiceListener from jaxWsModel
            //if (jaxWsModel!=null) jaxWsModel.removeServiceListener(jaxWsServiceListener);

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(WebProject.this);
            } catch (IOException e) {
                // #222721 - Provide a better error message in case of read-only location of project.
                if (!WebProject.this.getProjectDirectory().canWrite()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_ProjectReadOnly());
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    Exceptions.printStackTrace(e);
                }
            }

            // Unregister copy on save support
            try {
                copyOnSaveSupport.cleanup();
            }
            catch (FileStateInvalidException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }

            artifactSupport.enableArtifactSynchronization(false);
            Deployment.getDefault().disableCompileOnSaveSupport(webModule);
            // TODO: dongmei: anything for EJBs???????

            // unregister project's classpaths to GlobalPathRegistry
            try {
                GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
                GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            } catch (IllegalArgumentException e) {
                // Classpath were not registered before unregistration
                LOGGER.log(Level.INFO, "While trying to close the project, it was not possible to unregister classpath items "
                        + "because they were not registered in the first place.", e);
            }

            CssPreprocessors.getDefault().removeCssPreprocessorsListener(cssSupport);

            easelSupport.close();
        }

    }

    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR, "dist.war", evaluator(), "dist", "clean", WebProjectProperties.BUILD_FILE), // NOI18N
                helper.createSimpleAntArtifact(WebProjectConstants.ARTIFACT_TYPE_WAR_EAR_ARCHIVE, "dist.ear.war", evaluator(), "dist-ear", "clean-ear", WebProjectProperties.BUILD_FILE) // NOI18N
            };
        }

    }

    // List of primarily supported templates

    private static final String[] TYPES = new String[] {
        "java-classes",         // NOI18N
        "java-main-class",      // NOI18N
        "java-forms",           // NOI18N
        "java-beans",           // NOI18N
        "persistence",          // NOI18N
        "oasis-XML-catalogs",   // NOI18N
        "XML",                  // NOI18N
        "ant-script",           // NOI18N
        "ant-task",             // NOI18N
        "REST-clients",         // NOI18N
        "servlet-types",        // NOI18N
        "web-types",            // NOI18N
        "web-types-server",     // NOI18N
        "web-services",         // NOI18N
        "web-service-clients",  // NOI18N
        "wsdl",                 // NOI18N
        "junit",                // NOI18N
        "html5",                // NOI18N
        "simple-files"          // NOI18N
    };

    private static final String[] TYPES_EJB31 = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types-server",     // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1",        // NOI18N
        "ejb-types_3_1_full",   // NOI18N
        "ejb-deployment-descriptor", // NOI18N
    };

    private static final String[] TYPES_EJB31_LITE = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1",        // NOI18N
        "ejb-deployment-descriptor", // NOI18N
    };

    private static final String[] TYPES_EJB32_LITE = new String[] {
        "ejb-types",            // NOI18N
        "ejb-types_3_0",        // NOI18N
        "ejb-types_3_1",        // NOI18N
        "ejb-types_3_2",        // NOI18N
        "ejb-deployment-descriptor", // NOI18N
    };

    private static final String[] TYPES_ARCHIVE = new String[] {
        "deployment-descriptor",          // NOI18N
        "XML",                            // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N
        "Templates/Other/Folder"                    // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_EE5 = new String[] {
        "Templates/JSP_Servlet/JSP.jsp",            // NOI18N
        "Templates/JSP_Servlet/Html.html",          // NOI18N
        "Templates/JSP_Servlet/Servlet.java",       // NOI18N
        "Templates/Classes/Class.java",             // NOI18N
        "Templates/Classes/Package",                // NOI18N
        "Templates/Persistence/Entity.java", // NOI18N
        "Templates/Persistence/RelatedCMP", // NOI18N
        "Templates/Persistence/JsfFromDB", // NOI18N
        "Templates/WebServices/WebService.java",    // NOI18N
        "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        "Templates/WebServices/WebServiceClient",   // NOI18N
        "Templates/WebServices/RestServicesFromEntities", // NOI18N
        "Templates/WebServices/RestServicesFromPatterns",  //NOI18N
        "Templates/Other/Folder"                   // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_EE6_FULL = new String[] {
        "Templates/J2EE/Session", // NOI18N
        "Templates/J2EE/Message", // NOI18N
        "Templates/J2EE/TimerSession"   // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_EE6_WEB = new String[] {
        "Templates/J2EE/Session"  // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_EE7_WEB = new String[] {
        "Templates/J2EE/Session",       // NOI18N
        "Templates/J2EE/TimerSession"   // NOI18N
    };

    private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
        "Templates/JSP_Servlet/webXml"     // NOI18N  ---
    };

    // guarded by this, #115809
    private List<String> privilegedTemplatesEE5 = null;
    private List<String> privilegedTemplates = null;

    // Path where instances of privileged templates are registered
    private static final String WEBTEMPLATE_PATH = "j2ee/webtier/templates"; //NOI18N

    synchronized List<String> getPrivilegedTemplates() {
        ensureTemplatesInitialized();
        return privilegedTemplates;
    }

    synchronized List<String> getPrivilegedTemplatesEE5() {
        ensureTemplatesInitialized();
        return privilegedTemplatesEE5;
    }

    public synchronized void resetTemplates() {
        privilegedTemplates = null;
        privilegedTemplatesEE5 = null;
    }

    private void ensureTemplatesInitialized() {
        assert Thread.holdsLock(this);
        if (privilegedTemplates != null
                && privilegedTemplatesEE5 != null
                ) {
            return;
        }

        privilegedTemplatesEE5 = new ArrayList<String>();
        privilegedTemplates = new ArrayList<String>();

        for (WebPrivilegedTemplates webPrivililegedTemplates : Lookups.forPath(WEBTEMPLATE_PATH).lookupAll(WebPrivilegedTemplates.class)) {
            String[] addedTemplates = webPrivililegedTemplates.getPrivilegedTemplates(apiWebModule);
            if (addedTemplates != null && addedTemplates.length > 0){
                List<String> addedList = Arrays.asList(addedTemplates);
                privilegedTemplatesEE5.addAll(addedList);
                privilegedTemplates.addAll(addedList);
            }
        }

        privilegedTemplatesEE5.addAll(Arrays.asList(PRIVILEGED_NAMES_EE5));
        privilegedTemplates.addAll(Arrays.asList(PRIVILEGED_NAMES));
    }

    private final class RecommendedTemplatesImpl implements RecommendedTemplates,
            PrivilegedTemplates, PropertyChangeListener {
        private WebProject project;
        private J2eeProjectCapabilities projectCap;

        RecommendedTemplatesImpl (WebProject project) {
            this.project = project;
            project.evaluator().addPropertyChangeListener(this);
        }

        private boolean checked = false;
        private boolean isArchive = false;
        private boolean isEE5 = false;
        private boolean serverSupportsEJB31 = false;
        private boolean serverSupportsEJB40 = false;

        @Override
        public String[] getRecommendedTypes() {
            checkEnvironment();
            if (isArchive) {
                return TYPES_ARCHIVE;
            } else if (projectCap.isEjb31LiteSupported() || projectCap.isEjb40LiteSupported()) {
                Set<String> set = new HashSet<>(Arrays.asList(TYPES));
                if (projectCap.isEjb31Supported() || projectCap.isEjb40Supported() 
                        || serverSupportsEJB31 || serverSupportsEJB40) {
                    set.addAll(Arrays.asList(TYPES_EJB31));
                }

                if (projectCap.isEjb32LiteSupported() || projectCap.isEjb40LiteSupported()) {
                    set.addAll(Arrays.asList(TYPES_EJB32_LITE));
                } else {
                    set.addAll(Arrays.asList(TYPES_EJB31_LITE));
                }
                return set.toArray(new String[0]);
            } else {
                return TYPES;
            }
        }

        @Override
        public String[] getPrivilegedTemplates() {
            checkEnvironment();
            if (isArchive) {
                return PRIVILEGED_NAMES_ARCHIVE;
            } else {
                Set<String> set = new HashSet<String>();
                if (projectCap.isEjb31LiteSupported()) {
                    set.addAll(getPrivilegedTemplatesEE5());
                    if (projectCap.isEjb31Supported() || projectCap.isEjb40Supported() 
                            || serverSupportsEJB31 || serverSupportsEJB40) {
                        set.addAll(Arrays.asList(PRIVILEGED_NAMES_EE6_FULL));
                    }

                    if (projectCap.isEjb32LiteSupported() || projectCap.isEjb40LiteSupported()) {
                        set.addAll(Arrays.asList(PRIVILEGED_NAMES_EE7_WEB));
                    } else {
                        set.addAll(Arrays.asList(PRIVILEGED_NAMES_EE6_WEB));
                    }
                } else if (isEE5) {
                    set.addAll(getPrivilegedTemplatesEE5());
                } else {
                    set.addAll(WebProject.this.getPrivilegedTemplates());
                }
                return set.toArray(new String[0]);
            }
        }

        private void checkEnvironment() {
            if (!checked) {
                final Object srcType = helper.getStandardPropertyEvaluator().
                        getProperty(WebProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                projectCap = J2eeProjectCapabilities.forProject(project);
                Profile profile = Profile.fromPropertiesString(eval.getProperty(WebProjectProperties.J2EE_PLATFORM));
                isEE5 = (profile == Profile.JAVA_EE_5);
                
                for (Profile _profile : ProjectUtil.getSupportedProfiles(project)) {
                    if (_profile.isFullProfile()) {
                        if (_profile.isAtLeast(Profile.JAKARTA_EE_9_FULL)) {
                            serverSupportsEJB40 = true;
                            break;
                        } else if (_profile.isAtLeast(Profile.JAVA_EE_6_FULL)) {
                            serverSupportsEJB31 = true;
                            break;
                        }
                    }
                }
                checked = true;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (WebProjectProperties.J2EE_SERVER_INSTANCE.equals(evt.getPropertyName())) {
                checked = false;
            }
        }

    }

    private class DeployOnSaveSupportProxy implements DeployOnSaveSupport {

        public synchronized void addArtifactListener(ArtifactListener listener) {
            copyOnSaveSupport.addArtifactListener(listener);
            artifactSupport.addArtifactListener(listener);
        }

        public synchronized void removeArtifactListener(ArtifactListener listener) {
            copyOnSaveSupport.removeArtifactListener(listener);
            artifactSupport.removeArtifactListener(listener);
        }

        public boolean containsIdeArtifacts() {
            return DeployOnSaveUtils.containsIdeArtifacts(eval, updateHelper, "build.classes.dir");
        }

    }

    /**
     * This class handle copying of web resources to appropriate place in build
     * dir. User is not forced to perform redeploy on JSP change. This
     * class is also used in true Deploy On Save.
     *
     * Class should not request project lock from FS listener methods
     * (deadlock prone).
     */
    private class CopyOnSaveSupport extends FileChangeAdapter implements PropertyChangeListener, DeployOnSaveListener {

        private FileObject docBase = null;

        private String docBaseValue = null;

        private FileObject webInf = null;

        private String webInfValue = null;

        private File resources = null;

        private String buildWeb = null;
        private String buildClasses = null;

        private final List<ArtifactListener> listeners = new CopyOnWriteArrayList<ArtifactListener>();

        /** Creates a new instance of CopyOnSaveSupport */
        public CopyOnSaveSupport() {
            super();
        }

        public void addArtifactListener(ArtifactListener listener) {
            listeners.add(listener);
        }

        public void removeArtifactListener(ArtifactListener listener) {
            listeners.remove(listener);
        }

        private boolean isCopyOnSaveEnabled() {
            return Boolean.parseBoolean(WebProject.this.evaluator().getProperty(WebProjectProperties.J2EE_COPY_STATIC_FILES_ON_SAVE));
        }

        public void initialize() throws FileStateInvalidException {
            WebProject.this.evaluator().addPropertyChangeListener(this);

            if (!isCopyOnSaveEnabled()) {
                return;
            }

            docBase = getWebModule().getDocumentBase();
            docBaseValue = evaluator().getProperty(WebProjectProperties.WEB_DOCBASE_DIR);
            webInf = getWebModule().getWebInf();
            webInfValue = evaluator().getProperty(WebProjectProperties.WEBINF_DIR);
            if (resources != null) {
                FileUtil.removeFileChangeListener(this, resources);
            }
            resources = getWebModule().getResourceDirectory();
            buildWeb = evaluator().getProperty(WebProjectProperties.BUILD_WEB_DIR);
            buildClasses = evaluator().getProperty("build.classes.dir");

            if (docBase != null) {
                docBase.addRecursiveListener(this);
            }

            if (webInf != null && !FileUtil.isParentOf(docBase, webInf)) {
                webInf.addRecursiveListener(this);
            }

            if (resources != null) {
                FileUtil.addFileChangeListener(this, resources);
            }

            // Add deployed resources notification listener
            webModule.getConfigSupport().addDeployOnSaveListener(this);

            LOGGER.log(Level.FINE, "Web directory is {0}", docBaseValue);
            LOGGER.log(Level.FINE, "WEB-INF directory is {0}", webInfValue);
        }

        public void cleanup() throws FileStateInvalidException {
            if (docBase != null) {
                docBase.removeRecursiveListener(this);
            }
            if (webInf != null && !FileUtil.isParentOf(docBase, webInf)) {
                webInf.removeRecursiveListener(this);
            }
            if (resources != null) {
                FileUtil.removeFileChangeListener(this, resources);
                resources = null;
            }

            WebProject.this.evaluator().removePropertyChangeListener(this);

            webModule.getConfigSupport().removeDeployOnSaveListener(this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (WebProjectProperties.WEB_DOCBASE_DIR.equals(evt.getPropertyName())
                    || WebProjectProperties.WEBINF_DIR.equals(evt.getPropertyName())
                    || WebProjectProperties.J2EE_COPY_STATIC_FILES_ON_SAVE.equals(evt.getPropertyName())
                    || WebProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
                try {
                    cleanup();
                    initialize();
                } catch (org.openide.filesystems.FileStateInvalidException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            } else if (WebProjectProperties.BUILD_WEB_DIR.equals(evt.getPropertyName())) {
                // TODO copy all files ?
                Object value = evt.getNewValue();
                buildWeb = value == null ? null : value.toString();
            }
        }

        private void checkPreprocessors(FileObject fileObject) {
            CssPreprocessors.getDefault().process(WebProject.this, fileObject);
        }

        private void checkPreprocessors(FileObject fileObject, String originalName, String originalExtension) {
            CssPreprocessors.getDefault().process(WebProject.this, fileObject, originalName, originalExtension);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            checkPreprocessors(fe.getFile());
            try {
                if (!handleResource(fe)) {
                    handleCopyFileToDestDir(fe.getFile());
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            try {
                if (!handleResource(fe)) {
                    handleCopyFileToDestDir(fe.getFile());
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            checkPreprocessors(fe.getFile());
            try {
                if (!handleResource(fe)) {
                    handleCopyFileToDestDir(fe.getFile());
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            checkPreprocessors(fe.getFile(), fe.getName(), fe.getExt());
            try {
                if (handleResource(fe)) {
                    return;
                }

                FileObject fo = fe.getFile();
                handleCopyFileToDestDir(fo);

                FileObject persistenceXmlDir = getWebModule().getPersistenceXmlDir();
                if (persistenceXmlDir != null && FileUtil.isParentOf(persistenceXmlDir, fo)
                        && "persistence.xml".equals(fe.getName() + "." + fe.getExt())) { // NOI18N
                    String path = "WEB-INF/classes/META-INF/" + FileUtil.getRelativePath(persistenceXmlDir, fo.getParent())
                            + "/" + fe.getName() + "." + fe.getExt(); // NOI18N
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                    return;
                }

                FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, true, true);
                FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

                if (webInf != null && FileUtil.isParentOf(webInf, fo)
                        && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                    // inside webinf
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(webInf, parent)) {
                        path = FileUtil.getRelativePath(webInf, fo.getParent())
                                + "/" + fe.getName() + "." + fe.getExt(); // NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); // NOI18N
                    }
                    path = "WEB-INF/" + path;

                    if (!isSynchronizationAppropriate(path))  {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }

                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    FileObject parent = fo.getParent();
                    String path;
                    if (FileUtil.isParentOf(docBase, parent)) {
                        path = FileUtil.getRelativePath(docBase, fo.getParent())
                                + "/" + fe.getName() + "." + fe.getExt(); // NOI18N
                    } else {
                        path = fe.getName() + "." + fe.getExt(); // NOI18N
                    }
                    if (!isSynchronizationAppropriate(path))  {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            checkPreprocessors(fe.getFile());
            try {
                if (handleResource(fe)) {
                    return;
                }

                FileObject fo = fe.getFile();

                FileObject persistenceXmlDir = getWebModule().getPersistenceXmlDir();
                if (persistenceXmlDir != null && FileUtil.isParentOf(persistenceXmlDir, fo)
                        && "persistence.xml".equals(fo.getNameExt())) { // NOI18N
                    String path = "WEB-INF/classes/META-INF/" + FileUtil.getRelativePath(persistenceXmlDir, fo); // NOI18N
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                    return;
                }

                FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, true, true);
                FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

                if (webInf != null && FileUtil.isParentOf(webInf, fo)
                        && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                    // inside webInf
                    String path = "WEB-INF/" + FileUtil.getRelativePath(webInf, fo); // NOI18N
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
                if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                    // inside docbase
                    String path = FileUtil.getRelativePath(docBase, fo);
                    if (!isSynchronizationAppropriate(path)) {
                        return;
                    }
                    handleDeleteFileInDestDir(path);
                }
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }

        private boolean isSynchronizationAppropriate(String filePath) {
            if (filePath.startsWith("WEB-INF/classes") && !filePath.startsWith("WEB-INF/classes/META-INF")) { // NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/src")) { // NOI18N
                return false;
            }
            if (filePath.startsWith("WEB-INF/lib")) { // NOI18N
                return false;
            }
            return true;
        }

        private void fireArtifactChange(Iterable<ArtifactListener.Artifact> artifacts) {
            for (ArtifactListener listener : listeners) {
                listener.artifactsUpdated(artifacts);
            }
        }

        private boolean handleResource(FileEvent fe) {
            // this may happen in broken project - see issue #191516
            // in any case it can't be resource event when resources is null
            if (resources == null) {
                return false;
            }
            FileObject resourceFo = FileUtil.toFileObject(resources);
            if (resourceFo != null
                    && (resourceFo.equals(fe.getFile()) || FileUtil.isParentOf(resourceFo, fe.getFile()))) {

                fireArtifactChange(Collections.singleton(
                        Artifact.forFile(FileUtil.toFile(fe.getFile())).serverResource()));
                return true;
            }
            return false;
        }

        private void handleDeleteFileInDestDir(String resourcePath) throws IOException {
            File deleted = null;
            FileObject webBuildBase = buildWeb == null ? null : helper.resolveFileObject(buildWeb);
            if (webBuildBase != null) {
                // project was built
                FileObject toDelete = webBuildBase.getFileObject(resourcePath);
                if (toDelete != null) {
                    deleted = FileUtil.toFile(toDelete);
                    toDelete.delete();
                }
                if (deleted != null) {
                    fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(deleted)));
                }
            }
        }

        private void handleCopyFileToDestDir(FileObject fo) throws IOException {
            if (fo.isVirtual()) {
                return;
            }

            FileObject persistenceXmlDir = getWebModule().getPersistenceXmlDir();
            if (persistenceXmlDir != null && FileUtil.isParentOf(persistenceXmlDir, fo)
                    && "persistence.xml".equals(fo.getNameExt())) { // NOI18N
                handleCopyFileToDestDir("WEB-INF/classes/META-INF", persistenceXmlDir, fo); // NOI18N
                return;
            }

            FileObject webInf = getWebModule().resolveWebInf(docBaseValue, webInfValue, true, true);
            FileObject docBase = getWebModule().resolveDocumentBase(docBaseValue, false);

            if (webInf != null && FileUtil.isParentOf(webInf, fo)
                    && !(webInf.getParent() != null && webInf.getParent().equals(docBase))) {
                handleCopyFileToDestDir("WEB-INF", webInf, fo); // NOI18N
                return;
            }
            if (docBase != null && FileUtil.isParentOf(docBase, fo)) {
                handleCopyFileToDestDir(null, docBase, fo);
                return;
            }
        }

        private void handleCopyFileToDestDir(String prefix, FileObject baseDir, FileObject fo) throws IOException {
            if (fo.isVirtual()) {
                return;
            }

            if (baseDir != null && FileUtil.isParentOf(baseDir, fo)) {
                // inside docbase
                String path = FileUtil.getRelativePath(baseDir, fo);
                if (prefix != null) {
                    path = prefix + "/" + path;
                }
                if (!isSynchronizationAppropriate(path)) {
                    return;
                }

                FileObject webBuildBase = buildWeb == null ? null : helper.resolveFileObject(buildWeb);
                if (webBuildBase != null) {
                    // project was built
                    if (FileUtil.isParentOf(baseDir, webBuildBase) || FileUtil.isParentOf(webBuildBase, baseDir)) {
                        //cannot copy into self
                        return;
                    }
                    FileObject destFile = ensureDestinationFileExists(webBuildBase, path, fo.isFolder());
                    if (!fo.isFolder()) {
                        InputStream is = null;
                        OutputStream os = null;
                        FileLock fl = null;
                        try {
                            is = fo.getInputStream();
                            fl = destFile.lock();
                            os = destFile.getOutputStream(fl);
                            FileUtil.copy(is, os);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                            if (os != null) {
                                os.close();
                            }
                            if (fl != null) {
                                fl.releaseLock();
                            }
                            File file = FileUtil.toFile(destFile);
                            if (file != null) {
                                fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(file)));
                            }
                        }
                    } else {
                        File file = FileUtil.toFile(destFile);
                        if (file != null) {
                            fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(file)));
                        }
                    }
                }
            }
        }

        /**
         * Returns the destination (parent) directory needed to create file
         * with relative path path under webBuilBase
         */
        @NonNull
        private FileObject ensureDestinationFileExists(FileObject webBuildBase, String path, boolean isFolder) throws IOException {
            FileObject current = webBuildBase;
            StringTokenizer st = new StringTokenizer(path, "/");
            while (st.hasMoreTokens()) {
                String pathItem = st.nextToken();
                FileObject newCurrent = current.getFileObject(pathItem);
                if (newCurrent == null) {
                    // need to create it
                    if (isFolder || st.hasMoreTokens()) {
                        // create a folder
                        newCurrent = FileUtil.createFolder(current, pathItem);
                        assert newCurrent != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    } else {
                        newCurrent = FileUtil.createData(current, pathItem);
                        assert newCurrent != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
                    }
                }
                current = newCurrent;
            }
            assert current != null : "webBuildBase: " + webBuildBase + ", path: " + path + ", isFolder: " + isFolder;
            return current;
        }

        public void deployed(Iterable<Artifact> artifacts) {
            if (!easelSupport.canReload()) {
                return;
            }
            for (Artifact artifact : artifacts) {
                FileObject fileObject = getReloadFileObject(artifact);
                if (fileObject != null) {
                    easelSupport.reload(fileObject);
                }
            }
        }

        private FileObject getReloadFileObject(Artifact artifact) {
            File file = artifact.getFile();
            FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
            if (fileObject == null) {
                return null;
            }
            return findSourceForBinary(fileObject);
        }

        // "Binary" means compiled class file or web root document copied into build directory
        private FileObject findSourceForBinary(FileObject artifact) {
            FileObject webClassesBase = buildClasses == null ? null : helper.resolveFileObject(buildClasses);
            FileObject webBuildBase = buildWeb == null ? null : helper.resolveFileObject(buildWeb);

            if (webClassesBase != null && FileUtil.isParentOf(webClassesBase, artifact)) {
                String path = FileUtil.getRelativePath(webClassesBase, artifact).replace(".class", ".java"); // NOI18N
                for (SourceGroup sg : ProjectUtils.getSources(WebProject.this).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    FileObject fo = sg.getRootFolder().getFileObject(path);
                    if (fo != null) {
                        return fo;
                    }
                }
                return null;
            }

            if (docBase != null && webBuildBase != null) {
                if (FileUtil.isParentOf(webBuildBase, artifact)) {
                    String path = FileUtil.getRelativePath(webBuildBase, artifact);
                    return docBase.getFileObject(path);
                }
            }
            return null;
        }
    }

    private class ArtifactCopySupport extends ArtifactCopyOnSaveSupport {

        public ArtifactCopySupport() {
            super(WebProjectProperties.BUILD_WEB_DIR, evaluator(), getAntProjectHelper());
        }

        @Override
        public List<ArtifactCopyOnSaveSupport.Item> getArtifacts() {
            final AntProjectHelper helper = getAntProjectHelper();

            ClassPathSupport cs = new ClassPathSupport(evaluator(), getReferenceHelper(),
                    helper, getUpdateHelper(), new ClassPathSupportCallbackImpl(helper));

            List<ArtifactCopyOnSaveSupport.Item> result = new ArrayList<ArtifactCopyOnSaveSupport.Item>();
            for (ClassPathSupport.Item item : cs.itemsList(
                    helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(ProjectProperties.JAVAC_CLASSPATH),
                    WebProjectProperties.TAG_WEB_MODULE_LIBRARIES)) {

                if (!item.isBroken() && (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT
                        || item.getType() == ClassPathSupport.Item.TYPE_LIBRARY
                        || item.getType() == ClassPathSupport.Item.TYPE_JAR)) {
                    String path = item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
                    String dirs = item.getAdditionalProperty(AntProjectConstants.DESTINATION_DIRECTORY);
                    if (path != null) {
                        result.add(new Item(item, new ItemDescription(path, RelocationType.fromString(dirs))));
                    }
                }
            }
            return result;
        }

        @Override
        protected Artifact filterArtifact(Artifact artifact, RelocationType type) {
            if (containsTLD(artifact.getFile()) || type == RelocationType.NONE) {
                return artifact;
            }
            if (type == RelocationType.ROOT) {
                return artifact.relocatable();
            } else if (type == RelocationType.LIB) {
                return artifact.relocatable("lib"); // NOI18N
            } else {
                return artifact;
            }
        }

        private boolean containsTLD(File f) {
            if (f.exists() && f.isFile() && f.canRead()) {
                ZipFile zip = null;
                try {
                    zip = new ZipFile(f);
                    for (Enumeration entries = zip.entries(); entries.hasMoreElements();) {
                        String zipEntryName = ((ZipEntry) entries.nextElement()).getName();
                        if (TLD_PATTERN.matcher(zipEntryName).matches()) {
                            return true;
                        }
                    }
                    return false;
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                } finally {
                    if (zip != null) {
                        try {
                            zip.close();
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    }
                }
            }

            return false;
        }
    }

    public boolean isJavaEE5(Project project) {
        return Profile.JAVA_EE_5.equals(getAPIWebModule().getJ2eeProfile());
    }

    private static final class WebPropertyEvaluatorImpl implements WebPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public WebPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }

    private class WebExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-ws-compile","-do-compile","-do-compile-single", "-post-compile", "-pre-dist", //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return WebProject.this;
        }

    }

    private static final class CoSAwareFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener
    {

        private final FileBuiltQueryImplementation delegate;
        private final WebProject project;
        private final AtomicBoolean cosEnabled = new AtomicBoolean();
        private final Map<FileObject, Reference<StatusImpl>> file2Status = new WeakHashMap<FileObject, Reference<StatusImpl>>();

        public CoSAwareFileBuiltQueryImpl(FileBuiltQueryImplementation delegate, WebProject project)
        {

            this.delegate = delegate;
            this.project = project;
            project.evaluator().addPropertyChangeListener(this);
            setCoSEnabledAndXor();

        }

        private synchronized StatusImpl readFromCache(FileObject file)
        {
            Reference<StatusImpl> r = file2Status.get(file);
            return r != null ? r.get() : null;

        }

        public Status getStatus(FileObject file)
        {
            StatusImpl result = readFromCache(file);
            if (result != null)
            {
                return result;
            }


            Status status = delegate.getStatus(file);
            if (status == null)
            {
                return null;
            }

            synchronized (this)
            {
                StatusImpl foisted = readFromCache(file);
                if (foisted != null)
                {
                    return foisted;
                }

                file2Status.put(file, new WeakReference<StatusImpl>(result = new StatusImpl(cosEnabled, status)));
            }

            return result;

        }

        boolean setCoSEnabledAndXor()
        {
            boolean nue = Boolean.parseBoolean(project.evaluator().getProperty(
                                     WebProjectProperties.J2EE_COMPILE_ON_SAVE));
            boolean old = cosEnabled.getAndSet(nue);

            return old != nue;

        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            if (!setCoSEnabledAndXor())
            {
                return;
            }

            Collection<Reference<StatusImpl>> toRefresh;

            synchronized (this)
            {
                toRefresh = new LinkedList<Reference<StatusImpl>>(file2Status.values());
            }

            for (Reference<StatusImpl> r : toRefresh)
            {
                StatusImpl s = r.get();

                if (s != null)
                {
                    s.stateChanged(null);
                }
            }
        }

        private static final class StatusImpl implements Status, ChangeListener
        {

            private final ChangeSupport cs = new ChangeSupport(this);
            private final AtomicBoolean cosEnabled;
            private final Status delegate;

            public StatusImpl(AtomicBoolean cosEnabled, Status delegate)
            {
                this.cosEnabled = cosEnabled;
                this.delegate = delegate;
                this.delegate.addChangeListener(this);
            }

            public boolean isBuilt()
            {
                return cosEnabled.get() || delegate.isBuilt();
            }

            public void addChangeListener(ChangeListener l)
            {
                cs.addChangeListener(l);
            }

            public void removeChangeListener(ChangeListener l)
            {
                cs.removeChangeListener(l);
            }

            public void stateChanged(ChangeEvent e)
            {

                cs.fireChange();

            }
        }
    }

    // FIXME this is just fallback for code searching for the old SPI in lookup
    // remove in next release
    @SuppressWarnings("deprecation")
    private static class WebModuleImpl implements WebModuleImplementation {

        private final WebModule apiModule;

        public WebModuleImpl(WebModule apiModule) {
            this.apiModule = apiModule;
        }

        public FileObject getWebInf() {
            return apiModule.getWebInf();
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return apiModule.getMetadataModel();
        }

        public FileObject[] getJavaSources() {
            return apiModule.getJavaSources();
        }

        public String getJ2eePlatformVersion() {
            return apiModule.getJ2eePlatformVersion();
        }

        public FileObject getDocumentBase() {
            return apiModule.getDocumentBase();
        }

        public FileObject getDeploymentDescriptor() {
            return apiModule.getDeploymentDescriptor();
        }

        public String getContextPath() {
            return apiModule.getContextPath();
        }
    }

    private static class WebModuleImpl2 implements WebModuleImplementation2 {

        private final ProjectWebModule webModule;

        public WebModuleImpl2(ProjectWebModule webModule) {
            this.webModule = webModule;
        }

        public String getContextPath() {
            return webModule.getContextPath();
        }

        public FileObject getDeploymentDescriptor() {
            return webModule.getDeploymentDescriptor();
        }

        public FileObject getDocumentBase() {
            return webModule.getDocumentBase();
        }

        public Profile getJ2eeProfile() {
            return webModule.getJ2eeProfile();
        }

        public FileObject[] getJavaSources() {
            return webModule.getJavaSources();
        }

        public MetadataModel<WebAppMetadata> getMetadataModel() {
            return webModule.getMetadataModel();
        }

        public FileObject getWebInf() {
            return webModule.getWebInf();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            webModule.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            webModule.removePropertyChangeListener(listener);
        }

    }

    private static class WebProjectLookup extends ProxyLookup implements PropertyChangeListener{
        private Lookup base, ee6;
        private WebProject project;
        private ProjectBrowserProvider browserProvider;

        public WebProjectLookup(WebProject project, Lookup base, Lookup ee6, ProjectBrowserProvider browserProvider) {
            super(base);
            this.project = project;
            this.base = base;
            this.ee6 = ee6;
            this.browserProvider = browserProvider;
            updateLookup();
        }

        private void updateLookup(){
            List<Lookup> lookups = new ArrayList<>();
            lookups.add(base);
            Profile profile = Profile.fromPropertiesString(project.evaluator().getProperty(WebProjectProperties.J2EE_PLATFORM));
            if (Profile.JAVA_EE_6_FULL.equals(profile) || Profile.JAVA_EE_6_WEB.equals(profile)
                    || Profile.JAVA_EE_7_FULL.equals(profile) || Profile.JAVA_EE_7_WEB.equals(profile)
                    || Profile.JAVA_EE_8_FULL.equals(profile) || Profile.JAVA_EE_8_WEB.equals(profile)
                    || Profile.JAKARTA_EE_8_FULL.equals(profile) || Profile.JAKARTA_EE_8_WEB.equals(profile)
                    || Profile.JAKARTA_EE_9_FULL.equals(profile) || Profile.JAKARTA_EE_9_WEB.equals(profile)
                    || Profile.JAKARTA_EE_9_1_FULL.equals(profile) || Profile.JAKARTA_EE_9_1_WEB.equals(profile)
                    || Profile.JAKARTA_EE_10_FULL.equals(profile) || Profile.JAKARTA_EE_10_WEB.equals(profile)
                    || Profile.JAKARTA_EE_11_FULL.equals(profile) || Profile.JAKARTA_EE_11_WEB.equals(profile)) {
                lookups.add(ee6);
            }
            if ("true".equals(project.evaluator().getProperty(WebProjectProperties.DISPLAY_BROWSER))) {
                lookups.add(Lookups.singleton(browserProvider));
            }
            setLookups(lookups.toArray(new Lookup[0]));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(WebProjectProperties.J2EE_PLATFORM) ||
                evt.getPropertyName().equals(WebProjectProperties.DISPLAY_BROWSER) ){
                updateLookup();
            }
        }
    }

    private static class EnterpriseBeansListener implements PropertyChangeListener{
        private static final RequestProcessor rp = new RequestProcessor();
        private Task upgradeTask = null;
        private WebProject project;

        public EnterpriseBeansListener(WebProject project) {
            this.project = project;
        }

        public synchronized void propertyChange(final PropertyChangeEvent evt) {
            if (upgradeTask != null){
                upgradeTask.schedule(100);
                return;
            }
            upgradeTask = rp.post(new Runnable() {
                public void run() {
                    WebProjectUtilities.upgradeJ2EEProfile(project);
                }
            }, 100);
        }
    }

    private static final class ProjectWebRootProviderImpl implements ProjectWebRootProvider {

        private final WebProject project;


        private ProjectWebRootProviderImpl(WebProject project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public FileObject getWebRoot(FileObject file) {
            WebModule webModule = WebModule.getWebModule(file);
            FileObject webRoot = webModule != null ? webModule.getDocumentBase() : null;
            if(webRoot != null) {
                //#181480 - the WebModule.getWebModule() returns a webmodule instance
                //also for files outside of document base, which is OK.
                return FileUtil.isParentOf(webRoot, file) ?
                    webRoot :
                    null;
            }
            return null;
        }

        @Override
        public Collection<FileObject> getWebRoots() {
            WebModule webModule = project.getAPIWebModule();
            FileObject webRoot = webModule != null ? webModule.getDocumentBase() : null;
            if (webRoot != null) {
                return Collections.singleton(webRoot);
            }
            return Collections.emptyList();
        }

    }

    private final class WhiteListUpdaterImpl extends WhiteListUpdater {

        public WhiteListUpdaterImpl(Project project) {
            super(project);
        }

        @Override
        public void addSettingListener() {
            evaluator().addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE)){
                        checkWhiteLists();
                    }
                    if (evt.getPropertyName().equals(ProjectProperties.JAVAC_CLASSPATH)){
                        // if classpath changes refresh whitelists as well:
                        updateWhitelist(null, getServerWhiteList());
                    }
                }
            });
        }
    }

    private class JavaEEProjectSettingsImpl implements JavaEEProjectSettingsImplementation {

        private final WebProject project;


        public JavaEEProjectSettingsImpl(WebProject project) {
            this.project = project;

            evaluator().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (WebProjectProperties.SELECTED_BROWSER.equals(evt.getPropertyName())) {
                        easelSupport.resetBrowserSupport();
                    }
                }
            });
        }

        @Override
        public void setProfile(Profile profile) {
            setInSharedProperties(JavaEEProjectSettingConstants.J2EE_PLATFORM, profile.toPropertiesString());
        }

        @Override
        public Profile getProfile() {
            return webModule.getJ2eeProfile();
        }

        @Override
        public void setBrowserID(String browserID) {
            setInSharedProperties(JavaEEProjectSettingConstants.SELECTED_BROWSER, browserID);
        }

        @Override
        public String getBrowserID() {
            return evaluator().getProperty(JavaEEProjectSettingConstants.SELECTED_BROWSER);
        }

        @Override
        public void setServerInstanceID(String serverInstanceID) {
            setInSharedProperties(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE, serverInstanceID);
        }

        @Override
        public String getServerInstanceID() {
            return evaluator().getProperty(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE);
        }

        private void setInSharedProperties(String key, String value) {
            try {
                UpdateHelper helper = project.getUpdateHelper();
                EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                projectProperties.setProperty(key, value);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Project properties couldn't be saved.", ex);
            }
        }
    }
}
