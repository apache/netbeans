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

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.clientproject.classpath.DelagatingProjectClassPathModifierImpl;
import org.netbeans.modules.j2ee.clientproject.ui.AppClientLogicalViewProvider;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectJAXWSClientSupport;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectWebServicesClientSupport;
import org.netbeans.modules.j2ee.clientproject.wsclient.AppClientProjectWebServicesSupportProvider;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.spi.ejbjar.CarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettingConstants;
import org.netbeans.modules.javaee.project.api.ant.AntProjectUtil;
import org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents one plain Application Client project.
 * @author Jesse Glick, et al.
 */
@AntBasedProjectRegistration(
    iconResource="org/netbeans/modules/j2ee/clientproject/ui/resources/appclient.gif",
    type=AppClientProjectType.TYPE,
    sharedNamespace=AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=AppClientProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class AppClientProject implements Project, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(AppClientProject.class.getName());

    private final Icon CAR_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/clientproject/ui/resources/appclient.gif", false); // NOI18N
    
    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final UpdateHelper updateHelper;
    private MainClassUpdater mainClassUpdater;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    
    // WS client support
    private AppClientProjectWebServicesClientSupport carProjectWebServicesClientSupport;
    private AppClientProjectJAXWSClientSupport jaxWsClientSupport;
    private WebServicesClientSupport apiWebServicesClientSupport;
    private JAXWSClientSupport apiJAXWSClientSupport;
    
    private PropertyChangeListener j2eePlatformListener;
    private final AppClientProvider appClient;
    private final Car apiJar;
    private JarContainerImpl enterpriseResourceSupport;
    private FileObject libFolder;
    private final DelagatingProjectClassPathModifierImpl cpMod;
    private final ClassPathProviderImpl cpProvider;
    private ClassPathUiSupport.Callback classPathUiSupportCallback;
    private final AppClientCompilationClassPathModifierImpl libMod;
    
    // use AntBuildExtender to enable Ant Extensibility
    private AntBuildExtender buildExtender;
    
    // set to true when project customizer is being closed and changes persisted
    private final ThreadLocal<Boolean> projectPropertiesSave;
    
    public AppClientProject(AntProjectHelper helper) throws IOException {
        this.projectPropertiesSave = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
        this.helper = helper;
        eval = createEvaluator();
        aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
        buildExtender = AntBuildExtenderFactory.createAntExtender(new AppClientExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);
        UpdateImplementation updateProject = new UpdateProjectImpl(this, this.helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        carProjectWebServicesClientSupport = new AppClientProjectWebServicesClientSupport(this, helper, refHelper);
        jaxWsClientSupport = new AppClientProjectJAXWSClientSupport(this, helper);
        apiWebServicesClientSupport = WebServicesClientSupportFactory.createWebServicesClientSupport(carProjectWebServicesClientSupport);
        apiJAXWSClientSupport = JAXWSClientSupportFactory.createJAXWSClientSupport(jaxWsClientSupport);
        libMod = new AppClientCompilationClassPathModifierImpl(this, this.updateHelper, eval, refHelper);
        this.cpProvider = new ClassPathProviderImpl(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                ProjectProperties.BUILD_CLASSES_DIR, AppClientProjectProperties.DIST_JAR, ProjectProperties.BUILD_TEST_CLASSES_DIR,
                new String[] {"javac.classpath", AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {ProjectProperties.JAVAC_PROCESSORPATH},
                new String[] {"javac.test.classpath", AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {"debug.classpath", AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH }, // NOI18N
                new String[] {"run.test.classpath", AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH },
                new String[] {ProjectProperties.ENDORSED_CLASSPATH}); // NOI18N
        appClient = new AppClientProvider(this, helper, cpProvider);
        apiJar = CarFactory.createCar(new CarImpl2(appClient));
        enterpriseResourceSupport = new JarContainerImpl(this, refHelper, helper);
        ClassPathModifier cpModPrevious = new ClassPathModifier(this, this.updateHelper, eval, refHelper,
            new ClassPathSupportCallbackImpl(helper), createClassPathModifierCallback(), 
            getClassPathUiSupportCallback());
        cpMod = new DelagatingProjectClassPathModifierImpl(cpModPrevious, libMod);
        lookup = createLookup(aux, cpProvider);
    }
    
    public void setProjectPropertiesSave(boolean value) {
        this.projectPropertiesSave.set(value);
    }
    
    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String classPathProperty[] = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                return classPathProperty[0];
            }

            public String getElementName(String classpathProperty) {
                if (ProjectProperties.JAVAC_CLASSPATH.equals(classpathProperty)) {
                    return ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES;
                }
                return null;
            }
        };        
    }

    public synchronized ClassPathUiSupport.Callback getClassPathUiSupportCallback() {
        if (classPathUiSupportCallback == null) {
            classPathUiSupportCallback = new ClassPathUiSupport.Callback() {
                public void initItem(ClassPathSupport.Item item) {
                    if (item.getType() != ClassPathSupport.Item.TYPE_LIBRARY || !item.getLibrary().getType().equals(J2eePlatform.LIBRARY_TYPE)) {
                        item.setAdditionalProperty(ClassPathSupportCallbackImpl.INCLUDE_IN_DEPLOYMENT, "true");
                    }
                }
            };
            
        }
        return classPathUiSupportCallback;
    }

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    @Override
    public String toString() {
        return "CarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        return helper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    public ReferenceHelper getReferenceHelper() {
        return this.refHelper;
    }
    
    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), AppClientProjectProperties.SOURCE_ENCODING);
        final AppClientLogicalViewProvider lvp  = new AppClientLogicalViewProvider(this, this.updateHelper, evaluator(), refHelper, appClient);
        Lookup base = Lookups.fixed(new Object[] {
            QuerySupport.createProjectInformation(helper, this, CAR_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            spp,
            new AppClientActionProvider( this, this.updateHelper ),
            lvp,
            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(),getTestSourceRoots()),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator()),
            QuerySupport.createAnnotationProcessingQuery(helper, eval, ProjectProperties.ANNOTATION_PROCESSING_ENABLED, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
            new AntArtifactProviderImpl(),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(),getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery2(evaluator()),
            QuerySupport.createSources(this, helper, evaluator(),
                    getSourceRoots(),
                    getTestSourceRoots(),
                    Roots.propertyBased(new String[]{AppClientProjectProperties.META_INF}, new String[]{NbBundle.getMessage(AppClientLogicalViewProvider.class, "LBL_Node_ConfFiles")}, false, null, null),
                    Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, AppClientProjectProperties.DIST_DIR)),
            QuerySupport.createSharabilityQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots(),
                    AppClientProjectProperties.META_INF),
            QuerySupport.createFileBuiltQuery(helper,  evaluator(), getSourceRoots(), getTestSourceRoots()),
            encodingQuery,
            QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
            new RecommendedTemplatesImpl(this.updateHelper),
            ProjectClassPathModifier.extenderForModifier(cpMod),
            cpMod,
            cpMod.getClassPathModifier(),
            libMod,
            buildExtender,
            AppClientProject.this, // never cast an externally obtained Project to AppClientProject - use lookup instead
            new AppClientProjectOperations(this),
            new AppClientProjectWebServicesSupportProvider(),
            
            new ProjectAppClientProvider(this),
            appClient,
            // FIXME this is just fallback for code searching for the old SPI in lookup
            // remove in next release
            new CarImpl(apiJar),
            new AppClientPersistenceProvider(this, evaluator(), cpProvider),
            enterpriseResourceSupport,
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, eval),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, eval),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createBinaryForSourceQueryImplementation(sourceRoots, testRoots, helper, eval),
            new JavaEEProjectSettingsImpl(this),
            BrokenReferencesSupport.createReferenceProblemsProvider(helper, refHelper, eval, lvp.getBreakableProperties(), lvp.getPlatformProperties()),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(helper, eval, PlatformUpdatedCallBackImpl.create(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, updateHelper), JavaPlatform.getDefault().getSpecification().getName(), ProjectProperties.PLATFORM_ACTIVE, ProjectProperties.JAVAC_SOURCE, ProjectProperties.JAVAC_TARGET),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
        });
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-j2ee-clientproject/Lookup"); //NOI18N
    }
    
    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }
    
    // Package private methods -------------------------------------------------
    
    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(), AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }
    
    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(this.updateHelper, evaluator(), getReferenceHelper(), AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
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
    
    // Currently unused (but see #47230):
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
            }
        });
    }
    
    /** Return configured project name. */
    public String getName() {
        return ProjectUtils.getInformation(this).getName();
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }    
    
    public void fileChanged (FileEvent fe) {
    }
    
    public void fileDataCreated (FileEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }
    
    public void fileDeleted (FileEvent fe) {
    }
    
    public void fileFolderCreated (FileEvent fe) {
    }
    
    public void fileRenamed (FileRenameEvent fe) {
        FileObject fo = fe.getFile ();
        checkLibraryFolder (fo);
    }

    private void checkLibraryFolder (FileObject fo) {
        if (!FileUtil.isArchiveFile(fo)) {
            return;
        }
        
        if (fo.getParent ().equals (libFolder)) {
            try {
                cpMod.getClassPathModifier().addRoots(new URL[] {FileUtil.getArchiveRoot(fo.toURL())}, ProjectProperties.JAVAC_CLASSPATH);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        public void run() {
                            EditableProperties ep = helper.getProperties(
                                    AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties projectProps = helper.getProperties(
                                    AntProjectHelper.PROJECT_PROPERTIES_PATH);

                                Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(platform);
                                String classpath = J2EEProjectProperties.toClasspathString(platform.getClasspathEntries(), roots);
                                ep.setProperty(AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(AppClientProject.this);
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
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
    
    public Car getAPICar() {
        return apiJar;
    }
    
    public AppClientProvider getCarModule() {
        return appClient;
    }
    
    
    private String getProperty(String path, String name) {
        return helper.getProperties(path).getProperty(name);
    }
    
    String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
        
    // Private innerclasses ----------------------------------------------------
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        @Override
        protected void projectXmlSaved() throws IOException {
            int state = genFilesHelper.getBuildScriptState(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                AppClientProject.class.getResource("resources/build-impl.xsl"));
            final Boolean projectPropertiesSave = AppClientProject.this.projectPropertiesSave.get();
            if ((projectPropertiesSave && (state & GeneratedFilesHelper.FLAG_MODIFIED) == GeneratedFilesHelper.FLAG_MODIFIED) ||
                state == (GeneratedFilesHelper.FLAG_UNKNOWN | GeneratedFilesHelper.FLAG_MODIFIED | 
                    GeneratedFilesHelper.FLAG_OLD_PROJECT_XML | GeneratedFilesHelper.FLAG_OLD_STYLESHEET)) {  //missing genfiles.properties
                try {
                    AntProjectUtil.backupBuildImplFile(updateHelper);
                    genFilesHelper.generateBuildScriptFromStylesheet(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            AppClientProject.class.getResource("resources/build-impl.xsl"));
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (IllegalStateException e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                                  AppClientProject.class.getResource("resources/build-impl.xsl"),
                                                  false);
            }
            
            genFilesHelper.refreshBuildScript(
                getBuildXmlName(),
                AppClientProject.class.getResource("resources/build.xsl"), //NOI18N
                false);
        }
        
    }
    
    /** Package-private for unit tests only. */
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            evaluator().addPropertyChangeListener(AppClientProject.this.appClient);

            AppClientLogicalViewProvider logicalViewProvider =  AppClientProject.this.getLookup().lookup(AppClientLogicalViewProvider.class);
            if (logicalViewProvider != null) {
                logicalViewProvider.initialize();
            }

            // Check up on build scripts.
            try {
                //Check libraries and add them to classpath automatically
                String libFolderName = helper.getStandardPropertyEvaluator ().getProperty (AppClientProjectProperties.LIBRARIES_DIR);

                //DDDataObject initialization to be ready to listen on changes (#49656)
                try {
                    FileObject ddFO = appClient.getDeploymentDescriptor();
                    if (ddFO != null) {
                        DataObject.find(ddFO);
                    }
                } catch (DataObjectNotFoundException ex) {}
                
                if (libFolderName != null && helper.resolveFile (libFolderName).isDirectory ()) {
                    libFolder = helper.resolveFileObject(libFolderName);
                        FileObject children [] = libFolder.getChildren ();
                        List<URL> libs = new LinkedList<URL>();
                        for (int i = 0; i < children.length; i++) {
                            if (FileUtil.isArchiveFile(children[i])) {
                                libs.add(FileUtil.getArchiveRoot(children[i].toURL()));
                            }
                        }
                        cpMod.getClassPathModifier().addRoots(libs.toArray(new URL[0]), ProjectProperties.JAVAC_CLASSPATH);
                        libFolder.addFileChangeListener (AppClientProject.this);
                }
                
                // Check up on build scripts.
                
                int flags = genFilesHelper.getBuildScriptState(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    AppClientProject.class.getResource("resources/build-impl.xsl"));
                if ((flags & GeneratedFilesHelper.FLAG_MODIFIED) != 0
                    && (flags & GeneratedFilesHelper.FLAG_OLD_PROJECT_XML) != 0) {
                        AntProjectUtil.backupBuildImplFile(updateHelper);
                        genFilesHelper.generateBuildScriptFromStylesheet(
                            GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                            AppClientProject.class.getResource("resources/build-impl.xsl"));
                } else {
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        AppClientProject.class.getResource("resources/build-impl.xsl"), true);
                }
                
                genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    AppClientProject.class.getResource("resources/build.xsl"), //NOI18N
                    true);
                
                String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_INSTANCE);
                J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
                String serverType = null;
                if (platform != null) {
                    // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                    AppClientProjectProperties.setServerInstance(AppClientProject.this, AppClientProject.this.helper, servInstID);
                } else {
                    // if there is some server instance of the type which was used
                    // previously do not ask and use it
                    serverType = getProperty(AntProjectHelper.PROJECT_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_TYPE);
                    if (serverType != null) {
                        String instanceID = J2EEProjectProperties.getMatchingInstance(serverType, Type.CAR, AppClientProject.this.getAPICar().getJ2eeProfile());
                        if (instanceID != null) {
                            AppClientProjectProperties.setServerInstance(AppClientProject.this, AppClientProject.this.helper, instanceID);
                            platform = Deployment.getDefault().getJ2eePlatform(instanceID);
                        }
                    }
                    if (platform == null) {
                        BrokenServerSupport.showAlert();
                    }
                }
                // UI Logging
                Utils.logUI(NbBundle.getBundle(AppClientProject.class), "UI_APP_CLIENT_PROJECT_OPENED", // NOI18N
                        new Object[] {(serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)), servInstID});                
                
                // Usage Logging
                String serverName = ""; // NOI18N
                try {
                    if (servInstID != null) {
                        serverName = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
                    }
                } catch (InstanceRemovedException ier) {
                    // ignore
                }
                Profile profile = AppClientProject.this.getAPICar().getJ2eeProfile();
                Utils.logUsage(AppClientProject.class, "USG_PROJECT_OPEN_APPCLIENT", new Object[] { serverName, profile }); // NOI18N
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }

            
            // register project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            
            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry!
            // AppClient DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            appClient.getConfigSupport().ensureConfigurationReady();
            
            //register updater of main.class
            //the updater is active only on the opened projects
            mainClassUpdater = new MainClassUpdater(AppClientProject.this, eval, updateHelper,
                    cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0], AppClientProjectProperties.MAIN_CLASS);

            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run() {
                                updateProject();
                            }
                        });
                    }
                });
                
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks()) {
                ProjectProblems.showAlert(AppClientProject.this);
            }
            if(WebServicesClientSupport.isBroken(AppClientProject.this)) {
                WebServicesClientSupport.showBrokenAlert(AppClientProject.this);
            }
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N

            //remove jaxws.endorsed.dir property
            ep.remove("jaxws.endorsed.dir");

            // #134642 - use Ant task from copylibs library
            SharabilityUtility.makeSureProjectHasCopyLibsLibrary(helper, refHelper);
            
            //update lib references in project properties
            EditableProperties props = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            J2EEProjectProperties.removeObsoleteLibraryLocations(ep);
            J2EEProjectProperties.removeObsoleteLibraryLocations(props);
            
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

            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
            
            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

            // update a dual build directory project to use a single build directory
            ep = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            String earBuildDir = ep.getProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR);
            if (null != earBuildDir) {
                // there is an BUILD_EAR_CLASSES_DIR property... we may
                //  need to change its value
                String buildDir = ep.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
                if (null != buildDir) {
                    // there is a value that we may need to change the
                    // BUILD_EAR_CLASSES_DIR property value to match.
                    if (!buildDir.equals(earBuildDir)) {
                        // the values do not match... update the property and save it
                        ep.setProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR,
                                buildDir);
                        updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,
                                ep);
                    }
                    // else {
                    //   the values match and we don't need to do anything
                    // }
                }
                // else {
                //   the project doesn't have a BUILD_CLASSES_DIR property
                //   ** This is not an expected state, but if the project 
                //      properties evolve, this property may go away...
                // }
            }
            // else {
            //   there isn't a BUILD_EAR_CLASSES_DIR in this project...
            //     so we should not create one, by setting it.
            // }

            try {
                ProjectManager.getDefault().saveProject(AppClientProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        protected void projectClosed() {
            evaluator().removePropertyChangeListener(AppClientProject.this.appClient);

            // unregister j2ee platform classpath change listener
            String servInstID = getProperty(AntProjectHelper.PRIVATE_PROPERTIES_PATH, AppClientProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }

            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(AppClientProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            // unregister project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            if (mainClassUpdater != null) {
                mainClassUpdater.unregister();
                mainClassUpdater = null;
            }
        }
        
    }
    
    public WebServicesClientSupport getAPIWebServicesClientSupport() {
        return apiWebServicesClientSupport;
    }

    public JAXWSClientSupport getAPIJAXWSClientSupport() {
        return apiJAXWSClientSupport;
    }
    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {
        
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean"), // NOI18N
                //new CarAntArtifact(helper.createSimpleAntArtifact(AppClientProjectConstants.ARTIFACT_TYPE_CAR, "dist.jar", helper.getStandardPropertyEvaluator(), "dist", "clean")), // NOI18N
                new CarAntArtifact(helper.createSimpleAntArtifact(AppClientProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE, "dist.ear.jar", helper.getStandardPropertyEvaluator(), "dist-ear", "clean-ear")) // NOI18N
        };
        }
        
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        RecommendedTemplatesImpl(UpdateHelper helper) {
            this.helper = helper;
        }
        
        private final transient UpdateHelper helper;
        private transient boolean isArchive = false;
        
        // List of primarily supported templates
        
        private static final String[] APPLICATION_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "j2ee-types",           // NOI18N                    
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "web-service-clients",  // NOI18N
            "wsdl",                 // NOI18N
            "sunresource-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/J2EE/CachingServiceLocator.java", //NOI18N
            "Templates/WebServices/WebServiceClient"   // NOI18N
        };
        
        private static final String[] APPLICATION_TYPES_ARCHIVE = new String[] {
            "deployment-descriptor",           // NOI18N                    
            "XML",                             // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES_ARCHIVE = new String[] {
            "Templates/J2EE/applicationClientXml,"  // NOI18N
        };

        public String[] getRecommendedTypes() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = APPLICATION_TYPES_ARCHIVE.clone();
            } else {
                retVal = APPLICATION_TYPES.clone();
            }
            return retVal;
        }
        
        public String[] getPrivilegedTemplates() {
            String[] retVal = null;
            checkEnvironment();
            if (isArchive) {
                retVal = PRIVILEGED_NAMES_ARCHIVE.clone();
            } else {
                retVal = PRIVILEGED_NAMES.clone();
            }
            return retVal;
        }

        private transient boolean checked = false;
        
        private void checkEnvironment() {
            if (!checked) {
                final Object srcType = helper.getAntProjectHelper().
                        getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.JAVA_SOURCE_BASED);
                if ("false".equals(srcType)) {
                    isArchive = true;
                }
                checked = true;
            }
        }
        
    }

    private final class CarAntArtifact extends AntArtifact {
    
        private final AntArtifact impl;
    
        CarAntArtifact(AntArtifact aa) {
            impl = aa;
        }
    
        @Override
        public String getID() {
            if (AppClientProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE.equals(getType())) {
                return AppClientProjectConstants.CAR_ANT_ARTIFACT_ID;
            }
            return impl.getID();
        }

        public String getType() {
            return impl.getType();
        }

        public String getTargetName() {
            return impl.getTargetName();
        }

        public File getScriptLocation() {
            return impl.getScriptLocation();
        }

        public String getCleanTargetName() {
            return impl.getCleanTargetName();
        }

        @Override
        public URI[] getArtifactLocations() {
            return impl.getArtifactLocations();
        }
    
        @Override
        public Project getProject() {
            return impl.getProject();
        }
    
    }

    // FIXME this is just fallback for code searching for the old SPI in lookup
    // remove in next release
    @SuppressWarnings("deprecation")
    private class CarImpl implements CarImplementation {

        private final Car apiCar;

        public CarImpl(Car apiCar) {
            this.apiCar = apiCar;
        }

        public FileObject getDeploymentDescriptor() {
            return apiCar.getDeploymentDescriptor();
        }

        public String getJ2eePlatformVersion() {
            return apiCar.getJ2eePlatformVersion();
        }

        public FileObject[] getJavaSources() {
            return apiCar.getJavaSources();
        }

        public FileObject getMetaInf() {
            return apiCar.getMetaInf();
        }
    }

    private class CarImpl2 implements CarImplementation2 {

        private final AppClientProvider provider;

        public CarImpl2(AppClientProvider provider) {
            this.provider = provider;
        }

        public FileObject getDeploymentDescriptor() {
            return provider.getDeploymentDescriptor();
        }

        public Profile getJ2eeProfile() {
            return provider.getJ2eeProfile();
        }

        public FileObject[] getJavaSources() {
            return provider.getJavaSources();
        }

        public FileObject getMetaInf() {
            return provider.getMetaInf();
        }
    }

    private class AppClientExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "-do-init", "-init-check", "-post-clean", "jar", "-pre-pre-compile","-do-compile","-do-compile-single", "-pre-dist" //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return AppClientProject.this;
        }

    }

    private class JavaEEProjectSettingsImpl implements JavaEEProjectSettingsImplementation {

        private final AppClientProject project;

        public JavaEEProjectSettingsImpl(AppClientProject project) {
            this.project = project;
        }

        @Override
        public void setProfile(Profile profile) {
            setInSharedProperties(JavaEEProjectSettingConstants.J2EE_PLATFORM, profile.toPropertiesString());
        }

        @Override
        public Profile getProfile() {
            return project.getAPICar().getJ2eeProfile();
        }
        
        @Override
        public void setBrowserID(String browserID) {
            // Does not make sense for Application Client project - simply do nothing
        }

        @Override
        public String getBrowserID() {
            // Does not make sense for Application Client project - simply do nothing
            return null;
        }

        @Override
        public void setServerInstanceID(String serverInstanceID) {
            setInPrivateProperties(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE, serverInstanceID);
        }

        @Override
        public String getServerInstanceID() {
            return evaluator().getProperty(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE);
        }

        private void setInSharedProperties(String key, String value) {
            setInProperties(key, value, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        }

        private void setInPrivateProperties(String key, String value) {
            setInProperties(key, value, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        }

        private void setInProperties(String key, String value, String propertiesPath) {
            try {
                UpdateHelper helper = project.getUpdateHelper();
                EditableProperties projectProperties = helper.getProperties(propertiesPath);
                projectProperties.setProperty(key, value);
                helper.putProperties(propertiesPath, projectProperties);
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Project properties couldn't be saved.", ex);
            }
        }
    }
}

