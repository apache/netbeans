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

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileBuiltQuery.Status;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.problems.ProjectProblemsProviders;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.api.common.project.ProjectHooks;
import org.netbeans.modules.java.api.common.project.ProjectOperations;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.java.j2seproject.api.J2SEProjectBuilder;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.java.j2seproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SECompositePanelProvider;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.modules.java.j2seproject.ui.wizards.J2SEFileWizardIterator;
import org.netbeans.modules.project.ui.spi.TemplateCategorySorter;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.spi.whitelist.support.WhiteListQueryMergerSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.HelpCtx;
import org.openide.util.Pair;
/**
 * Represents one plain J2SE project.
 * @author Jesse Glick, et al.
 */
@AntBasedProjectRegistration(
    type=J2SEProject.TYPE,
    iconResource="org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", // NOI18N
    sharedName=J2SEProject.PROJECT_CONFIGURATION_NAME,
    sharedNamespace= J2SEProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateName=J2SEProject.PRIVATE_CONFIGURATION_NAME,
    privateNamespace= J2SEProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class J2SEProject implements Project {

    public static final String TYPE = "org.netbeans.modules.java.j2seproject"; // NOI18N
    static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project/3"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-project-private/1"; // NOI18N

    private static final String[] EXTENSIBLE_TARGETS = new String[] {
        "-do-init",             //NOI18N
        "-init-check",          //NOI18N
        "-post-clean",          //NOI18N
        "-pre-pre-compile",     //NOI18N
        "-do-compile",          //NOI18N
        "-do-compile-single",   //NOI18N
        "jar",                  //NOI18N
        "-post-jar",            //NOI18N
        "run",                  //NOI18N
        "debug",                //NOI18N
        "profile",              //NOI18N
    };
    private static final Icon J2SE_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/java/j2seproject/ui/resources/j2seProject.png", false); // NOI18N
    private static final Logger LOG = Logger.getLogger(J2SEProject.class.getName());

    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    private final UpdateHelper updateHelper;
    private MainClassUpdater mainClassUpdater;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private final ClassPathProviderImpl cpProvider;
    private final ClassPathModifier cpMod;

    private AntBuildExtender buildExtender;

    /**
     * @see J2SEProject.ProjectXmlSavedHookImpl#projectXmlSaved()
     */
    private final ThreadLocal<Boolean> projectPropertiesSave;

    @SuppressWarnings("LeakingThisInConstructor")
    public J2SEProject(AntProjectHelper helper) throws IOException {
        this.projectPropertiesSave = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
        this.helper = helper;
        aux = helper.createAuxiliaryConfiguration();
        UpdateProjectImpl updateProject = new UpdateProjectImpl(this, helper, aux);
        this.updateHelper = new UpdateHelper(updateProject, helper);
        eval = ProjectConfigurations.createPropertyEvaluator(this, helper, UPDATE_PROPERTIES);
        for (int v = 4; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/j2se-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(J2SEProject.class, "J2SEProject.too_new", FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        refHelper = new ReferenceHelper(helper, aux, evaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new J2SEExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);

        this.cpProvider = ClassPathProviderImpl.Builder.create(helper, evaluator(), getSourceRoots(), getTestSourceRoots())
                .setProject(this)
                .build();
        this.cpMod = new ClassPathModifier(this, this.updateHelper, evaluator(), refHelper, null, createClassPathModifierCallback(), null);
        lookup = createLookup(aux, newProjectOperationsCallback(this, updateProject));
    }

    private ClassPathModifier.Callback createClassPathModifierCallback() {
        return new ClassPathModifier.Callback() {
            @Override
            public String getClassPathProperty(SourceGroup sg, String type) {
                assert sg != null : "SourceGroup cannot be null";  //NOI18N
                assert type != null : "Type cannot be null";  //NOI18N
                final String[] classPathProperty = getClassPathProvider().getPropertyName (sg, type);
                if (classPathProperty == null || classPathProperty.length == 0) {
                    throw new UnsupportedOperationException ("Modification of [" + sg.getRootFolder().getPath() +", " + type + "] is not supported"); //NOI18N
                }
                return classPathProperty[0];
            }

            @Override
            public String getElementName(String classpathProperty) {
                return null;
            }
        };
    }

    /**
     * Returns the project directory
     * @return the directory the project is located in
     */
    @Override
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    @Override
    public String toString() {
        return "J2SEProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
    }
        
    private static final PropertyProvider UPDATE_PROPERTIES;
    static {
        Map<String, String> defs = new HashMap<String, String>();

        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); //NOI18N
        defs.put(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); //NOI18N
        defs.put(ProjectProperties.JAVAC_PROCESSORPATH,"${" + ProjectProperties.JAVAC_CLASSPATH + "}"); //NOI18N
        defs.put("javac.test.processorpath", "${" + ProjectProperties.JAVAC_TEST_CLASSPATH + "}"); // NOI18N

        UPDATE_PROPERTIES = PropertyUtils.fixedPropertyProvider(defs);
    }

    public PropertyEvaluator evaluator() {
        assert eval != null;
        return eval;
    }

    public ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }

    public UpdateHelper getUpdateHelper() {
        return this.updateHelper;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    private static class SiteRootFolderListener implements FileChangeListener {

        private final J2SEProject p;
        private final FileObject siteRootFolder;

        SiteRootFolderListener(J2SEProject p) {
            this.p = p;
            siteRootFolder = p.getProjectDirectory();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            checkPreprocessors(fe.getFile());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            // XXX: notify BrowserReload about filename change
            checkPreprocessors(fe.getFile(), fe.getName(), fe.getExt());
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
        private void checkPreprocessors(FileObject fileObject) {
            try {
                ClassLoader globalCl = Lookup.getDefault().lookup(ClassLoader.class);
                Class<?> clazz = Class.forName("org.netbeans.modules.web.common.api.CssPreprocessors", true, globalCl);
                Object instance = clazz.getMethod("getDefault").invoke(null);
                Method processMethod = clazz.getMethod("process",  Project.class, FileObject.class);
                processMethod.invoke(instance, p, fileObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void checkPreprocessors(FileObject fileObject, String originalName, String originalExtension) {
            try {
                ClassLoader globalCl = Lookup.getDefault().lookup(ClassLoader.class);
                Class<?> clazz = Class.forName("org.netbeans.modules.web.common.api.CssPreprocessors", true, globalCl);
                Object instance = clazz.getMethod("getDefault").invoke(null);
                Method processMethod = clazz.getMethod("process",   Project.class, FileObject.class, String.class, String.class);
                processMethod.invoke(instance, p, fileObject, originalName, originalExtension);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        private void checkPreprocessors(FileObject fileObject) {
//            CssPreprocessors.getDefault().process(p, fileObject);
//        }
//
//        private void checkPreprocessors(FileObject fileObject, String originalName, String originalExtension) {
//            CssPreprocessors.getDefault().process(p, fileObject, originalName, originalExtension);
//        }
    }
    
    private static class OpenHookImpl extends ProjectOpenedHook implements PropertyChangeListener {
        private final J2SEProject project;
        private FileChangeListener siteRootChangesListener;

        // @GuardedBy("this")
        private File siteRootFolder;


        public OpenHookImpl(J2SEProject project) {
            this.project = project;
        }

        @Override
        protected void projectOpened() {
            addSiteRootListener();
        }

      
        @Override
        protected void projectClosed() {
            removeSiteRootListener();
        }

        private synchronized void addSiteRootListener() {
            assert siteRootFolder == null : "Should not be listening to " + siteRootFolder;
            FileObject siteRoot = project.getProjectDirectory();
            if (siteRoot == null) {
                return;
            }
            siteRootFolder = FileUtil.toFile(siteRoot);
            if (siteRootFolder == null) {
                // should not happen
                LOG.log(Level.WARNING, "File not found for FileObject: {0}", siteRoot);
                return;
            }
            siteRootChangesListener = new SiteRootFolderListener(project);
            FileUtil.addRecursiveListener(siteRootChangesListener, siteRootFolder);
        }

        private synchronized void removeSiteRootListener() {
            if (siteRootFolder == null) {
                // no listener
                return;
            }
            try {
                FileUtil.removeRecursiveListener(siteRootChangesListener, siteRootFolder);
            } catch (IllegalArgumentException ex) {
                // #216349
                LOG.log(Level.INFO, null, ex);
            }
            siteRootFolder = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            // change in project properties
//            if (ClientSideProjectConstants.PROJECT_SITE_ROOT_FOLDER.equals(evt.getPropertyName())) {
                synchronized (this) {
                    removeSiteRootListener();
                    addSiteRootListener();
                }
//            }
        }
     }

    private Lookup createLookup(final AuxiliaryConfiguration aux, final ProjectOperations.Callback opsCallback) {
        final PlatformChangedHook platformChangedHook = new PlatformChangedHook();
        final FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), ProjectProperties.SOURCE_ENCODING);
        final Lookup base = Lookups.fixed(
            J2SEProject.this,
            QuerySupport.createProjectInformation(updateHelper, this, J2SE_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            refHelper.createSubprojectProvider(),
            new OpenHookImpl(this),
            LogicalViewProviders.createBuilder(
                this,
                eval,
                "org-netbeans-modules-java-j2seproject").   //NOI18N
                setHelpCtx(new HelpCtx("org.netbeans.modules.java.j2seproject.ui.J2SELogicalViewProvider.J2SELogicalViewRootNode")).    //NOI18N
                setCompileOnSaveBadge(newCoSBadge()).
                build(),
            // new J2SECustomizerProvider(this, this.updateHelper, evaluator(), refHelper),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),        
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createCompiledSourceForBinaryQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createJavadocForBinaryQuery(helper, evaluator()),
            new AntArtifactProviderImpl(),
            ProjectHooks.createProjectXmlSavedHookBuilder(eval, updateHelper, genFilesHelper).
                    setBuildImplTemplate(J2SEProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                    setBuildTemplate(J2SEProject.class.getResource("resources/build.xsl")).             //NOI18N
                    setOverrideModifiedBuildImplPredicate(new Callable<Boolean>(){
                        @Override
                        public Boolean call() throws Exception {
                            return projectPropertiesSave.get();
                        }
                    }).
                    build(),
            UILookupMergerSupport.createProjectOpenHookMerger(
                ProjectHooks.createProjectOpenedHookBuilder(this, eval, updateHelper, genFilesHelper, cpProvider).
                        addClassPathType(ClassPath.BOOT).
                        addClassPathType(ClassPath.COMPILE).
                        addClassPathType(ClassPath.SOURCE).
                        addClassPathType(JavaClassPathConstants.MODULE_COMPILE_PATH).   //For DefaultClassPathProvider
                        setBuildImplTemplate(J2SEProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                        setBuildTemplate(J2SEProject.class.getResource("resources/build.xsl")).             //NOI18N
                        addOpenPostAction(newStartMainUpdaterAction()).
                        addOpenPostAction(newWebServicesAction()).
                        addOpenPostAction(newMissingPropertiesAction()).
                        addOpenPostAction(newUpdateCopyLibsAction()).
                        addClosePostAction(newStopMainUpdaterAction()).
                        build()),
            QuerySupport.createUnitTestForSourceQuery(getSourceRoots(), getTestSourceRoots()),
            QuerySupport.createSourceLevelQuery2(evaluator()),
            QuerySupport.createSources(this, helper, evaluator(), getSourceRoots(), getTestSourceRoots(), Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, ProjectProperties.DIST_DIR)),
            QuerySupport.createSharabilityQuery2(helper, evaluator(), getSourceRoots(), getTestSourceRoots()),
            new CoSAwareFileBuiltQueryImpl(QuerySupport.createFileBuiltQuery(helper, evaluator(), getSourceRoots(), getTestSourceRoots()), this),
            new RecommendedTemplatesImpl (this.updateHelper),
            ProjectClassPathModifier.extenderForModifier(cpMod),
            buildExtender,
            cpMod,
            ProjectOperations.createBuilder(this, eval, updateHelper, refHelper, sourceRoots, testRoots).
                    addDataFiles("manifest.mf","master-application.jnlp","master-applet.jnlp","master-component.jnlp","preview-application.html","preview-applet.html").    //NOI18N
                    addMetadataFiles("xml-resources","catalog.xml").    //NOI18N
                    addPreservedPrivateProperties(ProjectProperties.APPLICATION_ARGS, ProjectProperties.RUN_WORK_DIR, ProjectProperties.COMPILE_ON_SAVE, ProjectProperties.DO_JLINK, ProjectProperties.JLINK_STRIP).
                    addUpdatedNameProperty(ProjectProperties.DIST_JAR, "$'{'dist.dir'}'/{0}.jar", true).    //NOI18N
                    addUpdatedNameProperty(J2SEProjectProperties.APPLICATION_TITLE, "{0}", false).  //NOI18N
                    addUpdatedNameProperty(ProjectProperties.DIST_JLINK_OUTPUT, "$'{'"+ProjectProperties.DIST_JLINK_DIR+"'}'/{0}", true).    //NOI18N
                    addUpdatedNameProperty(ProjectProperties.JLINK_LAUNCHER_NAME, "{0}", true).    //NOI18N
                    setCallback(opsCallback).
                    build(),
            ProjectConfigurations.createConfigurationProviderBuilder(this, eval, updateHelper).
                    addConfigurationsAffectActions(ActionProvider.COMMAND_RUN, ActionProvider.COMMAND_DEBUG).
                    setCustomizerAction(newConfigCustomizerAction()).
                    build(),
            new J2SEPersistenceProvider(this, cpProvider),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            encodingQuery,
            new J2SEPropertyEvaluatorImpl(evaluator()),
            QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createBinaryForSourceQueryImplementation(this.sourceRoots, this.testRoots, this.helper, this.evaluator()), //Does not use APH to get/put properties/cfgdata
            QuerySupport.createAnnotationProcessingQuery(this.helper, this.evaluator(), ProjectProperties.ANNOTATION_PROCESSING_ENABLED, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
            LookupProviderSupport.createActionProviderMerger(),
            WhiteListQueryMergerSupport.createWhiteListQueryMerger(),
            BrokenReferencesSupport.createReferenceProblemsProvider(helper, refHelper, eval, platformChangedHook, J2SEProjectUtil.getBreakableProperties(this), new String[]{ProjectProperties.PLATFORM_ACTIVE}),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(helper, eval, platformChangedHook, JavaPlatform.getDefault().getSpecification().getName(), ProjectProperties.PLATFORM_ACTIVE, ProjectProperties.JAVAC_SOURCE, ProjectProperties.JAVAC_TARGET),
            BrokenReferencesSupport.createProfileProblemProvider(helper, refHelper, eval, ProjectProperties.JAVAC_PROFILE, ProjectProperties.RUN_CLASSPATH, ProjectProperties.ENDORSED_CLASSPATH),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
            new J2SEProjectPlatformImpl(this),
            QuerySupport.createCompilerOptionsQuery(eval, ProjectProperties.JAVAC_COMPILERARGS),
            QuerySupport.createUnitTestsCompilerOptionsQuery(eval, sourceRoots, testRoots),
            QuerySupport.createAutomaticModuleNameQuery(helper, eval, sourceRoots, ProjectProperties.MANIFEST_FILE),
            QuerySupport.createModuleInfoAccessibilityQuery(sourceRoots, testRoots),
            LookupMergerSupport.createCompilerOptionsQueryMerger(),
            J2SEFileWizardIterator.create(),
            ProjectProblemsProviders.createMissingModuleProjectProblemsProvider(this)
        );
        lookup = base; // in case LookupProvider's call Project.getLookup
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-java-j2seproject/Lookup"); //NOI18N
    }

    public ClassPathProviderImpl getClassPathProvider () {
        return this.cpProvider;
    }

    public ClassPathModifier getProjectClassPathModifier () {
        return this.cpMod;
    }

    // Package private methods -------------------------------------------------

    /**
     * Returns the source roots of this project
     * @return project's source roots
     */
    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
       }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
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
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                Element data = updateHelper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2SEProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                updateHelper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }


    /**
     * J2SEProjectProperties helper method to notify ProjectXmlSavedHookImpl about customizer save
     * @see J2SEProject.ProjectXmlSavedHookImpl#projectXmlSaved()
     * @param value true = active
     */
    public void setProjectPropertiesSave(boolean value) {
        this.projectPropertiesSave.set(value);
    }

    // Private innerclasses ----------------------------------------------------    
    /**
     * Exports the main JAR as an official build product for use from other scripts.
     * The type of the artifact will be {@link AntArtifact#TYPE_JAR}.
     */
    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        @Override
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(JavaProjectConstants.ARTIFACT_TYPE_JAR, "dist.jar", evaluator(), "jar", "clean", ProjectProperties.BUILD_SCRIPT), // NOI18N
            };
        }

    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates, TemplateCategorySorter {
        RecommendedTemplatesImpl (UpdateHelper helper) {
            this.helper = helper;
        }

        private UpdateHelper helper;

        // List of primarily supported templates

        private static final String[] APPLICATION_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "web-service-clients",  // NOI18N
            "REST-clients",         // NOI18N
            "wsdl",                 // NOI18N
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] LIBRARY_TYPES = new String[] {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            //"gui-java-application", // NOI18N
            "java-beans",           // NOI18N
            "persistence",          // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "servlet-types",        // NOI18N
            "servlet-types-j2se-only",// NOI18N
            "web-service-clients",  // NOI18N
            "REST-clients",         // NOI18N
            "wsdl",                 // NOI18N
            // "web-types",         // NOI18N
            "junit",                // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"         // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/Persistence/Entity.java", // NOI18N
            "Templates/Persistence/RelatedCMP", // NOI18N
            "Templates/WebServices/WebServiceClient"   // NOI18N
        };

        private static final Map<String,Integer>  CAT_MAP;
        static {
            final Map<String,Integer> m = new HashMap<>();
            m.put("Classes",0);     //NOI18N
            m.put("GUIForms",1);    //NOI18N
            m.put("Beans",2);       //NOI18N
            m.put("AWTForms",3);    //NOI18N
            m.put("UnitTests",4);   //NOI18N
            CAT_MAP = Collections.unmodifiableMap(m);
        };

        @Override
        public String[] getRecommendedTypes() {            
            return isLibrary() ? LIBRARY_TYPES : APPLICATION_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        @Override
        @NonNull
        public List<DataObject> sort(@NonNull final List<DataObject> original) {
            if (!isLibrary()) {
                return original;
            }
            final List<DataObject> result = new ArrayList<>(Collections.<DataObject>nCopies(CAT_MAP.size(), null));
            for (DataObject dobj : original) {
                final String name = dobj.getName();
                final Integer index = CAT_MAP.get(name);
                if (index == null) {
                    result.add(dobj);
                } else {
                    result.set (index, dobj);
                }
            }
            return filterNulls(result);
        }

        @NonNull
        private List<DataObject> filterNulls(@NonNull final List<DataObject> list) {
            boolean hasNull = false;
            for (int i=0; i<CAT_MAP.size(); i++) {
                if (list.get(i) == null) {
                    hasNull = true;
                    break;
                }
            }
            if (!hasNull) {
                //No copy needed
                return list;
            }
            final List<DataObject> result = new ArrayList<>(list.size());
            for (DataObject dobj : list) {
                if (dobj != null) {
                    result.add(dobj);
                }
            }
            return result;
        }

        private boolean isLibrary() {
            final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            // if the project has no main class, it's not really an application
            return ep.getProperty (ProjectProperties.MAIN_CLASS) == null || "".equals (ep.getProperty (ProjectProperties.MAIN_CLASS)); // NOI18N
        }

    }

    private static final class J2SEPropertyEvaluatorImpl implements J2SEPropertyEvaluator {
        private PropertyEvaluator evaluator;
        public J2SEPropertyEvaluatorImpl (PropertyEvaluator eval) {
            evaluator = eval;
        }
        @Override
        public PropertyEvaluator evaluator() {
            return evaluator;
        }
    }

    private class J2SEExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        @Override
        public List<String> getExtensibleTargets() {
            return Arrays.asList(EXTENSIBLE_TARGETS);
        }

        @Override
        public Project getOwningProject() {
            return J2SEProject.this;
        }

    }

    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }

    private static final class CoSAwareFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {

        private final FileBuiltQueryImplementation delegate;
        private final J2SEProject project;
        private final AtomicBoolean cosEnabled = new AtomicBoolean();
        private final Map<FileObject, Reference<StatusImpl>> file2Status = new WeakHashMap<FileObject, Reference<StatusImpl>>();

        @SuppressWarnings("LeakingThisInConstructor")
        public CoSAwareFileBuiltQueryImpl(FileBuiltQueryImplementation delegate, J2SEProject project) {
            this.delegate = delegate;
            this.project = project;

            project.evaluator().addPropertyChangeListener(this);

            setCoSEnabledAndXor();
        }

        private synchronized StatusImpl readFromCache(FileObject file) {
            Reference<StatusImpl> r = file2Status.get(file);

            return r != null ? r.get() : null;
        }

        @Override
        public Status getStatus(FileObject file) {
            StatusImpl result = readFromCache(file);

            if (result != null) {
                return result;
            }

            Status status = delegate.getStatus(file);

            if (status == null) {
                return null;
            }

            synchronized (this) {
                StatusImpl foisted = readFromCache(file);

                if (foisted != null) {
                    return foisted;
                }

                file2Status.put(file, new WeakReference<StatusImpl>(result = new StatusImpl(cosEnabled, status)));
            }

            return result;
        }

        boolean setCoSEnabledAndXor() {
            boolean nue = J2SEProjectUtil.isCompileOnSaveEnabled(project);
            boolean old = cosEnabled.getAndSet(nue);

            return old != nue;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!setCoSEnabledAndXor()) {
                return ;
            }

            Collection<Reference<StatusImpl>> toRefresh;

            synchronized (this) {
                toRefresh = new LinkedList<Reference<StatusImpl>>(file2Status.values());
            }

            for (Reference<StatusImpl> r : toRefresh) {
                StatusImpl s = r.get();

                if (s != null) {
                    s.stateChanged(null);
                }
            }
        }

        private static final class StatusImpl implements Status, ChangeListener {

            private final ChangeSupport cs = new ChangeSupport(this);
            private final AtomicBoolean cosEnabled;
            private final Status delegate;

            @SuppressWarnings("LeakingThisInConstructor")
            public StatusImpl(AtomicBoolean cosEnabled, Status delegate) {
                this.cosEnabled = cosEnabled;
                this.delegate = delegate;
                this.delegate.addChangeListener(this);
            }

            @Override
            public boolean isBuilt() {
                return cosEnabled.get() || delegate.isBuilt();
            }

            @Override
            public void addChangeListener(ChangeListener l) {
                cs.addChangeListener(l);
            }

            @Override
            public void removeChangeListener(ChangeListener l) {
                cs.removeChangeListener(l);
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                cs.fireChange();
            }

        }
    }

    private final class PlatformChangedHook implements BrokenReferencesSupport.PlatformUpdatedCallBack {
        @Override
        public void platformPropertyUpdated(@NonNull final JavaPlatform platform) {
            J2SEProjectPlatformImpl.updateProjectXml(platform, updateHelper);
        }
    }

    @NonNull
    private Runnable newStartMainUpdaterAction() {
        return new Runnable() {
            @Override
            public void run() {
                //register updater of main.class
                //the updater is active only on the opened projects
                mainClassUpdater = new MainClassUpdater (
                        J2SEProject.this,
                        evaluator(),
                        updateHelper,
                        cpProvider.getProjectClassPaths(ClassPath.SOURCE)[0],
                        ProjectProperties.MAIN_CLASS);
                mainClassUpdater.start();
            }
        };
    }

    @NonNull
    private Runnable newStopMainUpdaterAction() {
        return new Runnable() {
            @Override
            public void run() {
                if (mainClassUpdater != null) {
                    mainClassUpdater.stop();
                    mainClassUpdater = null;
                }
            }
        };
    }

    @NonNull
    private Runnable newWebServicesAction() {
        return new Runnable() {
            private final String JAX_RPC_NAMESPACE="http://www.netbeans.org/ns/j2se-project/jax-rpc"; //NOI18N
            private final String JAX_RPC_CLIENTS="web-service-clients"; //NOI18N
            private final String JAX_RPC_CLIENT="web-service-client"; //NOI18N
            @Override
            public void run() {
                //remove jaxws.endorsed.dir property
                EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                ep.remove("jaxws.endorsed.dir");
                // move web-service-clients one level up from in project.xml
                // WS should be part of auxiliary configuration
                Element data = helper.getPrimaryConfigurationData(true);
                NodeList nodes = data.getElementsByTagName(JAX_RPC_CLIENTS);
                if(nodes.getLength() > 0) {
                    Element oldJaxRpcClients = (Element) nodes.item(0);
                    Document doc = createNewDocument();
                    Element newJaxRpcClients = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENTS);
                    NodeList childNodes = oldJaxRpcClients.getElementsByTagName(JAX_RPC_CLIENT);
                    for (int i=0;i<childNodes.getLength();i++) {
                        Element oldJaxRpcClient = (Element) childNodes.item(i);
                        Element newJaxRpcClient = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENT);
                        NodeList nodeProps = oldJaxRpcClient.getChildNodes();
                        for (int j=0;j<nodeProps.getLength();j++) {
                            Node n = nodeProps.item(j);
                            if (n instanceof Element) {
                                Element oldProp = (Element) n;
                                Element newProp = doc.createElementNS(JAX_RPC_NAMESPACE, oldProp.getLocalName());
                                String text = oldProp.getTextContent();
                                newProp.setTextContent(text);
                                newJaxRpcClient.appendChild(newProp);
                            }
                        }
                        newJaxRpcClients.appendChild(newJaxRpcClient);
                    }
                    aux.putConfigurationFragment(newJaxRpcClients, true);
                    data.removeChild(oldJaxRpcClients);
                    helper.putPrimaryConfigurationData(data, true);
                }
                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            }
        };
    }

    @NonNull
    private Runnable newMissingPropertiesAction() {
        return new Runnable() {
            @Override
            public void run() {
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (!ep.containsKey(ProjectProperties.INCLUDES)) {
                    ep.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
                }
                if (!ep.containsKey(ProjectProperties.EXCLUDES)) {
                    ep.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
                }
                if (!ep.containsKey("build.generated.sources.dir")) { // NOI18N
                    ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
                }
                J2SEProjectBuilder.createDefaultModuleProperties(
                        ep,
                        testRoots.getRoots().length > 0);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }
        };
    }

    @NonNull
    private Runnable newUpdateCopyLibsAction() {
        return new Runnable() {
            private final String LIB_COPY_LIBS = "CopyLibs"; //NOI18N
            private final String PROP_VERSION = "version";   //NOI18N
            private final String VOL_CP = "classpath";       //NOI18N

            @Override
            public void run() {
                final LibraryManager projLibManager = refHelper.getProjectLibraryManager();
                if (projLibManager == null) {
                    return;
                }
                final Library globalCopyLibs = LibraryManager.getDefault().getLibrary(LIB_COPY_LIBS);
                final Library projectCopyLibs = projLibManager.getLibrary(LIB_COPY_LIBS);
                if (globalCopyLibs == null || projectCopyLibs == null) {
                    return;
                }
                final String globalStr = globalCopyLibs.getProperties().get(PROP_VERSION);
                if (globalStr == null) {
                    return;
                }
                try {
                    final SpecificationVersion globalVersion = new SpecificationVersion(globalStr);
                    final String projectStr = projectCopyLibs.getProperties().get(PROP_VERSION);
                    if (projectStr != null && globalVersion.compareTo(new SpecificationVersion(projectStr)) <= 0) {
                        return;
                    }

                    final List<URL> content = projectCopyLibs.getContent(VOL_CP);
                    projLibManager.removeLibrary(projectCopyLibs);
                    final FileObject projLibLoc = URLMapper.findFileObject(projLibManager.getLocation());
                    if (projLibLoc != null) {
                        final FileObject libFolder = projLibLoc.getParent();
                        boolean canDelete = libFolder.canWrite();
                        FileObject container = null;
                        for (URL u : content) {
                            FileObject fo = toFile(u);
                            if (fo != null) {
                                canDelete &= fo.canWrite();
                                if (container == null) {
                                    container = fo.getParent();
                                    canDelete &= container.canWrite();
                                    canDelete &= LIB_COPY_LIBS.equals(container.getName());
                                    canDelete &= libFolder.equals(container.getParent());
                                } else {
                                    canDelete &= container.equals(fo.getParent());
                                }
                            }
                        }
                        if (canDelete && container != null) {
                            container.delete();
                        }
                    }
                    refHelper.copyLibrary(globalCopyLibs);

                } catch (IllegalArgumentException iae) {
                    LOG.log(
                            Level.WARNING,
                            "Cannot update {0} due to invalid version.", //NOI18N
                            projectCopyLibs.getDisplayName());
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            @CheckForNull
            private FileObject toFile(@NonNull final URL url) {
                final URL file = FileUtil.getArchiveFile(url);
                return URLMapper.findFileObject(file != null ? file : url);
            }
        };
    }

    @NonNull
    private Runnable newConfigCustomizerAction() {
        return new Runnable() {
            @Override
            public void run() {
                J2SEProject.this.getLookup().lookup(CustomizerProviderImpl.class).
                    showCustomizer(J2SECompositePanelProvider.RUN);
            }
        };
    }

    @NonNull
    private LogicalViewProviders.CompileOnSaveBadge newCoSBadge() {
        return new LogicalViewProviders.CompileOnSaveBadge() {
            @Override
            public boolean isBadgeVisible() {
                return !J2SEProjectUtil.isCompileOnSaveEnabled(J2SEProject.this) &&
                    J2SEProjectUtil.isCompileOnSaveSupported(J2SEProject.this);
            }
            @Override
            public boolean isImportant(@NonNull final String propertyName) {
                return ProjectProperties.COMPILE_ON_SAVE.equals(propertyName) ||
                propertyName.startsWith(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX);
            }
        };
    }

    @NonNull
    private static ProjectOperations.Callback newProjectOperationsCallback (
        @NonNull final J2SEProject project,
        @NonNull final UpdateProjectImpl projectUpdate) {
        return new ProjectOperations.Callback() {
            @Override
            public void beforeOperation(@NonNull final ProjectOperations.Callback.Operation operation) {
            }
            @Override
            @SuppressWarnings("fallthrough")
            public void afterOperation(
                    @NonNull final ProjectOperations.Callback.Operation operation,
                    @NullAllowed final String newName,
                    @NullAllowed final Pair<File, Project> oldProject) {
                switch (operation) {
                    case COPY:
                        projectUpdate.setTransparentUpdate(true);
                    case MOVE:
                    case RENAME:
                        project.setName(newName);
                }
            }
        };
    }
}
