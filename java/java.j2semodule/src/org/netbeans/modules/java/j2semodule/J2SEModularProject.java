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

package org.netbeans.modules.java.j2semodule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.api.common.ModuleRoots;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.classpath.MultiModuleClassPathProvider;
import org.netbeans.modules.java.api.common.project.JavaActionProvider;
import org.netbeans.modules.java.api.common.project.MultiModuleActionProviderBuilder;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.api.common.project.ProjectHooks;
import org.netbeans.modules.java.api.common.project.ProjectOperations;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.PropertyEvaluatorProvider;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProviders;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.j2semodule.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.java.j2semodule.ui.customizer.J2SECompositePanelProvider;
import org.netbeans.modules.java.j2semodule.ui.customizer.J2SEModularProjectProperties;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.netbeans.spi.whitelist.support.WhiteListQueryMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.openide.util.HelpCtx;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
/**
 * Represents one plain J2SE modular project.
 * @author Jesse Glick, et al.
 */
@AntBasedProjectRegistration(
    type=J2SEModularProject.TYPE,
    iconResource="org/netbeans/modules/java/j2semodule/ui/resources/j2seModuleProject.png", // NOI18N
    sharedName=J2SEModularProject.PROJECT_CONFIGURATION_NAME,
    sharedNamespace= J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,
    privateName=J2SEModularProject.PRIVATE_CONFIGURATION_NAME,
    privateNamespace= J2SEModularProject.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class J2SEModularProject implements Project {

    public static final String TYPE = "org.netbeans.modules.java.j2semodule"; // NOI18N
    static final String PROJECT_CONFIGURATION_NAME = "data"; // NOI18N
    public static final String PROJECT_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-modular-project/1"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAME = "data"; // NOI18N
    static final String PRIVATE_CONFIGURATION_NAMESPACE = "http://www.netbeans.org/ns/j2se-modular-project-private/1"; // NOI18N

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
    private static final Icon J2SE_MODULE_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/java/j2semodule/ui/resources/j2seModuleProject.png", false); // NOI18N
    private static final Logger LOG = Logger.getLogger(J2SEModularProject.class.getName());

    private final AuxiliaryConfiguration aux;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private Lookup lookup;
    private final UpdateHelper updateHelper;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private ModuleRoots moduleRoots;
    private ModuleRoots testModuleRoots;
    private final MultiModuleClassPathProvider cpProvider;
    private final ClassPathModifier cpMod;
    private MainClassUpdater mainClassUpdater;
    private AntBuildExtender buildExtender;

    /**
     * @see J2SEProject.ProjectXmlSavedHookImpl#projectXmlSaved()
     */
    private final ThreadLocal<Boolean> projectPropertiesSave;

    @SuppressWarnings("LeakingThisInConstructor")
    public J2SEModularProject(AntProjectHelper helper) throws IOException {
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
        eval = ProjectConfigurations.createPropertyEvaluator(this, helper);
        for (int v = 2; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/j2se-modular-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(J2SEModularProject.class, "J2SEModularProject.too_new", FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        refHelper = new ReferenceHelper(helper, aux, evaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new J2SEModularExtenderImplementation(), refHelper);
        genFilesHelper = new GeneratedFilesHelper(helper, buildExtender);

        this.cpProvider = MultiModuleClassPathProvider.Builder.newInstance(
                helper,
                evaluator(),
                getModuleRoots(),
                getSourceRoots(),
                getTestModuleRoots(),
                getTestSourceRoots())
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
        return "J2SEModularProject[" + FileUtil.getFileDisplayName(getProjectDirectory()) + "]"; // NOI18N
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

    private Lookup createLookup(final AuxiliaryConfiguration aux, final ProjectOperations.Callback opsCallback) {
        final PlatformChangedHook platformChangedHook = new PlatformChangedHook();
        final FileEncodingQueryImplementation encodingQuery = QuerySupport.createFileEncodingQuery(evaluator(), ProjectProperties.SOURCE_ENCODING);
        Sources src;
        final Lookup base = Lookups.fixed(J2SEModularProject.this,
            //REVIEWED FOR MODULAR PROJECT
            QuerySupport.createProjectInformation(updateHelper, this, J2SE_MODULE_PROJECT_ICON),
            aux,
            helper.createCacheDirectoryProvider(),
            helper.createAuxiliaryProperties(),
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            QuerySupport.createMultiModuleSourceForBinaryQuery(helper, evaluator(), getModuleRoots(), getSourceRoots(), getTestModuleRoots(), getTestSourceRoots()),
            QuerySupport.createMultiModuleBinaryForSourceQuery(helper, evaluator(), getModuleRoots(), getSourceRoots(), getTestModuleRoots(), getTestSourceRoots()),
            QuerySupport.createMultiModuleJavadocForBinaryQuery(helper, evaluator(), getModuleRoots(), getSourceRoots()),
            ProjectHooks.createProjectXmlSavedHookBuilder(eval, updateHelper, genFilesHelper).
                    setBuildImplTemplate(J2SEModularProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                    setBuildTemplate(J2SEModularProject.class.getResource("resources/build.xsl")).             //NOI18N
                    setOverrideModifiedBuildImplPredicate(new Callable<Boolean>(){
                        @Override
                        public Boolean call() throws Exception {
                            return projectPropertiesSave.get();
                        }
                    }).
                    build(),
            UILookupMergerSupport.createProjectOpenHookMerger(ProjectHooks.createProjectOpenedHookBuilder(this, eval, updateHelper, genFilesHelper, cpProvider).
                        addClassPathType(ClassPath.BOOT).
                        addClassPathType(ClassPath.COMPILE).
                        addClassPathType(ClassPath.SOURCE).
                        addClassPathType(JavaClassPathConstants.MODULE_COMPILE_PATH).   //For DefaultClassPathProvider
                        setBuildImplTemplate(J2SEModularProject.class.getResource("resources/build-impl.xsl")).    //NOI18N
                        setBuildTemplate(J2SEModularProject.class.getResource("resources/build.xsl")).             //NOI18N
                        addOpenPostAction(newStartMainUpdaterAction()).
//                        addOpenPostAction(newWebServicesAction()).
//                        addOpenPostAction(newMissingPropertiesAction()).
//                        addOpenPostAction(newUpdateCopyLibsAction()).
                        addClosePostAction(newStopMainUpdaterAction()).
                        build()),
            QuerySupport.createSourceLevelQuery2(evaluator()),
                    src = QuerySupport.createSources(this, helper, evaluator(),
                    getSourceRoots(),
                    getTestSourceRoots(),
                    getModuleRoots(),
                    getTestModuleRoots(),
                    Roots.nonSourceRoots(ProjectProperties.BUILD_DIR, ProjectProperties.DIST_DIR)),
            QuerySupport.createMultiModuleGroupQuery(helper, eval,  src,
                    getSourceRoots(),
                    getTestSourceRoots(),
                    getModuleRoots(),
                    getTestModuleRoots()),
            new RecommendedTemplatesImpl(getProjectDirectory()),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            encodingQuery,
            QuerySupport.createTemplateAttributesProvider(helper, encodingQuery),
            QuerySupport.createCompilerOptionsQuery(eval, ProjectProperties.JAVAC_COMPILERARGS),
            LookupMergerSupport.createCompilerOptionsQueryMerger(),
            ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createSFBLookupMerger(),
            ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, helper, evaluator()),
            LookupMergerSupport.createJFBLookupMerger(),
            QuerySupport.createAnnotationProcessingQuery(this.helper, this.evaluator(), ProjectProperties.ANNOTATION_PROCESSING_ENABLED, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS),
            LookupProviderSupport.createActionProviderMerger(),
            LogicalViewProviders.createBuilder(
                this,
                eval,
                "org-netbeans-modules-java-j2semodule").   //NOI18N
                setHelpCtx(new HelpCtx("org.netbeans.modules.java.j2semodule.ui.J2SEModularLogicalViewProvider.J2SEModularLogicalViewRootNode")).    //NOI18N
                setCompileOnSaveBadge(newCoSBadge()).
                build(),
            QuerySupport.createModuleInfoAccessibilityQuery(
                    getModuleRoots(),
                    getSourceRoots(),
                    getTestModuleRoots(),
                    getTestSourceRoots()),
            QuerySupport.createMultiModuleAntArtifactProvider(
                    helper,
                    evaluator(),
                    getModuleRoots(),
                    getSourceRoots()),
            QuerySupport.createSharabilityQuery2(
                    helper,
                    evaluator(),
                    getModuleRoots(),
                    getTestModuleRoots()),
            refHelper.createSubprojectProvider(),
            new CustomizerProviderImpl(this, this.updateHelper, evaluator(), refHelper, this.genFilesHelper),
            newActionProvider(),
            ProjectOperations.createBuilder(this, evaluator(), updateHelper, refHelper, getModuleRoots(), getTestModuleRoots()).
                    addDataFiles("manifest.mf","master-application.jnlp","master-applet.jnlp","master-component.jnlp","preview-application.html","preview-applet.html").    //NOI18N
                    addMetadataFiles("xml-resources","catalog.xml").    //NOI18N
                    addPreservedPrivateProperties(ProjectProperties.APPLICATION_ARGS, ProjectProperties.RUN_WORK_DIR, ProjectProperties.COMPILE_ON_SAVE, ProjectProperties.DO_JLINK, ProjectProperties.JLINK_STRIP).
                    addUpdatedNameProperty(J2SEModularProjectProperties.APPLICATION_TITLE, "{0}", false).  //NOI18N
                    addUpdatedNameProperty(ProjectProperties.DIST_JLINK_OUTPUT, "$'{'"+ProjectProperties.DIST_JLINK_DIR+"'}'/{0}", true).    //NOI18N
                    addUpdatedNameProperty(ProjectProperties.JLINK_LAUNCHER_NAME, "{0}", true).    //NOI18N
                    setCallback(opsCallback).
                    build(),
            new CoSAwareFileBuiltQueryImpl(QuerySupport.createMultiModuleFileBuiltQuery(
                    helper,
                    evaluator(),
                    getModuleRoots(),
                    getSourceRoots(),
                    getTestModuleRoots(),
                    getTestSourceRoots()), this),
            QuerySupport.createMultiModuleUnitTestForSourceQuery(
                    getModuleRoots(),
                    getSourceRoots(),
                    getTestModuleRoots(),
                    getTestSourceRoots()),
            ProjectConfigurations.createConfigurationProviderBuilder(this, evaluator(), updateHelper).
                    addConfigurationsAffectActions(ActionProvider.COMMAND_RUN, ActionProvider.COMMAND_DEBUG).
                    setCustomizerAction(newConfigCustomizerAction()).
                    build(),
            WhiteListQueryMergerSupport.createWhiteListQueryMerger(),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
            new PropertyEvaluatorProviderImpl(evaluator()),
            QuerySupport.createMultiModuleUnitTestsCompilerOptionsQuery(
                    this,
                    getModuleRoots(),
                    getSourceRoots(),
                    getTestModuleRoots(),
                    getTestSourceRoots()),
            BrokenReferencesSupport.createReferenceProblemsProvider(
                    helper,
                    refHelper,
                    evaluator(),
                    platformChangedHook,
                    J2SEModularProjectUtil.getBreakableProperties(
                            getSourceRoots(),
                            getTestSourceRoots()),
                    new String[] {
                            ProjectProperties.PLATFORM_ACTIVE
            }),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(
                    helper,
                    evaluator(),
                    platformChangedHook,
                    CommonProjectUtils.J2SE_PLATFORM_TYPE,
                    J2SEModularProjectUtil.MIN_SOURCE_LEVEL,
                    ProjectProperties.PLATFORM_ACTIVE,
                    ProjectProperties.JAVAC_SOURCE,
                    ProjectProperties.JAVAC_TARGET),
            new ProjectPlatformProviderImpl(this),
            ProjectClassPathModifier.extenderForModifier(cpMod),
            buildExtender,
            cpMod,
            new J2SEModularPersistenceProvider(this, cpProvider)
            //UNKNOWN FOR MODULAR PROJECT
//            J2SEFileWizardIterator.create()
        );
        lookup = base; // in case LookupProvider's call Project.getLookup
        return LookupProviderSupport.createCompositeLookup(base, "Projects/org-netbeans-modules-java-j2semodule/Lookup"); //NOI18N
    }

    private MultiModuleClassPathProvider getClassPathProvider () {
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
                    J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = SourceRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testRoots;
    }

    public synchronized ModuleRoots getModuleRoots() {
        if (this.moduleRoots == null) { //Local caching, no project metadata access
            this.moduleRoots = ModuleRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots", false, "src.{0}{1}.dir"); //NOI18N
        }
        return this.moduleRoots;
    }

    public synchronized ModuleRoots getTestModuleRoots() {
        if (this.testModuleRoots == null) { //Local caching, no project metadata access
            this.testModuleRoots = ModuleRoots.create(updateHelper, evaluator(), getReferenceHelper(),
                    J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "test-roots", true, "test.{0}{1}.dir"); //NOI18N
        }
        return this.testModuleRoots;
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
                NodeList nl = data.getElementsByTagNameNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE, "name");
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

    @NonNull
    private ActionProvider newActionProvider() {
        return MultiModuleActionProviderBuilder.newInstance(
                this, getUpdateHelper(), evaluator(),
                getSourceRoots(), getTestSourceRoots(), cpProvider)
                .setCompileOnSaveOperationsProvider(() -> {
                        return J2SEModularProjectUtil.isCompileOnSaveEnabled(this) ?
                            EnumSet.of(JavaActionProvider.CompileOnSaveOperation.UPDATE, JavaActionProvider.CompileOnSaveOperation.EXECUTE) :
                            Collections.emptySet();
                })
                .build();
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        // List of primarily supported templates
        private static final String[] PROJECT_TEMPLATES = new String[] {
            "java-modules",         // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] MODULE_TEMPLATES = new String[] {
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

        private static final String[] PRIVILEGED_PROJECT_NAMES = new String[] {
            "Templates/J2SEModule/module-info.java" // NOI18N
        };

        private static final String[] PRIVILEGED_MODULE_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/Persistence/Entity.java", // NOI18N
            "Templates/Persistence/RelatedCMP", // NOI18N
            "Templates/WebServices/WebServiceClient"   // NOI18N
        };
        
        private final FileObject projectDir;

        private RecommendedTemplatesImpl(FileObject projectDir) {
            this.projectDir = projectDir;
        }

        @Override
        public String[] getRecommendedTypes() {
            return isProject() ? PROJECT_TEMPLATES : MODULE_TEMPLATES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return isProject() ? PRIVILEGED_PROJECT_NAMES : PRIVILEGED_MODULE_NAMES;
        }
        
        private boolean isProject() {
            return projectDir == Utilities.actionsGlobalContext().lookup(FileObject.class);
        }
    }

    private class J2SEModularExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        @Override
        public List<String> getExtensibleTargets() {
            return Arrays.asList(EXTENSIBLE_TARGETS);
        }

        @Override
        public Project getOwningProject() {
            return J2SEModularProject.this;
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
//    private static Document createNewDocument() {
//        // #50198: for thread safety, use a separate document.
//        // Using XMLUtil.createDocument is much too slow.
//        synchronized (db) {
//            return db.newDocument();
//        }
//    }
//
//
    @NonNull
    private Runnable newStartMainUpdaterAction() {
        return () -> {
            //register updater of main.class
            //the updater is active only on the opened projects
            mainClassUpdater = new MainClassUpdater (
                    J2SEModularProject.this,
                    evaluator(),
                    updateHelper,
                    getSourceRoots(),
                    ProjectProperties.MAIN_CLASS);
            mainClassUpdater.start();
        };
    }

    @NonNull
    private Runnable newStopMainUpdaterAction() {
        return () -> {
            if (mainClassUpdater != null) {
                mainClassUpdater.stop();
                mainClassUpdater = null;
            }
        };
    }
//
//    @NonNull
//    private Runnable newWebServicesAction() {
//        return new Runnable() {
//            private final String JAX_RPC_NAMESPACE="http://www.netbeans.org/ns/j2se-project/jax-rpc"; //NOI18N
//            private final String JAX_RPC_CLIENTS="web-service-clients"; //NOI18N
//            private final String JAX_RPC_CLIENT="web-service-client"; //NOI18N
//            @Override
//            public void run() {
//                //remove jaxws.endorsed.dir property
//                EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
//                ep.remove("jaxws.endorsed.dir");
//                // move web-service-clients one level up from in project.xml
//                // WS should be part of auxiliary configuration
//                Element data = helper.getPrimaryConfigurationData(true);
//                NodeList nodes = data.getElementsByTagName(JAX_RPC_CLIENTS);
//                if(nodes.getLength() > 0) {
//                    Element oldJaxRpcClients = (Element) nodes.item(0);
//                    Document doc = createNewDocument();
//                    Element newJaxRpcClients = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENTS);
//                    NodeList childNodes = oldJaxRpcClients.getElementsByTagName(JAX_RPC_CLIENT);
//                    for (int i=0;i<childNodes.getLength();i++) {
//                        Element oldJaxRpcClient = (Element) childNodes.item(i);
//                        Element newJaxRpcClient = doc.createElementNS(JAX_RPC_NAMESPACE, JAX_RPC_CLIENT);
//                        NodeList nodeProps = oldJaxRpcClient.getChildNodes();
//                        for (int j=0;j<nodeProps.getLength();j++) {
//                            Node n = nodeProps.item(j);
//                            if (n instanceof Element) {
//                                Element oldProp = (Element) n;
//                                Element newProp = doc.createElementNS(JAX_RPC_NAMESPACE, oldProp.getLocalName());
//                                String text = oldProp.getTextContent();
//                                newProp.setTextContent(text);
//                                newJaxRpcClient.appendChild(newProp);
//                            }
//                        }
//                        newJaxRpcClients.appendChild(newJaxRpcClient);
//                    }
//                    aux.putConfigurationFragment(newJaxRpcClients, true);
//                    data.removeChild(oldJaxRpcClients);
//                    helper.putPrimaryConfigurationData(data, true);
//                }
//                updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
//            }
//        };
//    }
//
//    @NonNull
//    private Runnable newMissingPropertiesAction() {
//        return new Runnable() {
//            @Override
//            public void run() {
//                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                if (!ep.containsKey(ProjectProperties.INCLUDES)) {
//                    ep.setProperty(ProjectProperties.INCLUDES, "**"); // NOI18N
//                }
//                if (!ep.containsKey(ProjectProperties.EXCLUDES)) {
//                    ep.setProperty(ProjectProperties.EXCLUDES, ""); // NOI18N
//                }
//                if (!ep.containsKey("build.generated.sources.dir")) { // NOI18N
//                    ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
//                }
//                J2SEProjectBuilder.createDefaultModuleProperties(
//                        ep,
//                        testRoots.getRoots().length > 0);
//                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
//            }
//        };
//    }
//
//    @NonNull
//    private Runnable newUpdateCopyLibsAction() {
//        return new Runnable() {
//            private final String LIB_COPY_LIBS = "CopyLibs"; //NOI18N
//            private final String PROP_VERSION = "version";   //NOI18N
//            private final String VOL_CP = "classpath";       //NOI18N
//
//            @Override
//            public void run() {
//                final LibraryManager projLibManager = refHelper.getProjectLibraryManager();
//                if (projLibManager == null) {
//                    return;
//                }
//                final Library globalCopyLibs = LibraryManager.getDefault().getLibrary(LIB_COPY_LIBS);
//                final Library projectCopyLibs = projLibManager.getLibrary(LIB_COPY_LIBS);
//                if (globalCopyLibs == null || projectCopyLibs == null) {
//                    return;
//                }
//                final String globalStr = globalCopyLibs.getProperties().get(PROP_VERSION);
//                if (globalStr == null) {
//                    return;
//                }
//                try {
//                    final SpecificationVersion globalVersion = new SpecificationVersion(globalStr);
//                    final String projectStr = projectCopyLibs.getProperties().get(PROP_VERSION);
//                    if (projectStr != null && globalVersion.compareTo(new SpecificationVersion(projectStr)) <= 0) {
//                        return;
//                    }
//
//                    final List<URL> content = projectCopyLibs.getContent(VOL_CP);
//                    projLibManager.removeLibrary(projectCopyLibs);
//                    final FileObject projLibLoc = URLMapper.findFileObject(projLibManager.getLocation());
//                    if (projLibLoc != null) {
//                        final FileObject libFolder = projLibLoc.getParent();
//                        boolean canDelete = libFolder.canWrite();
//                        FileObject container = null;
//                        for (URL u : content) {
//                            FileObject fo = toFile(u);
//                            if (fo != null) {
//                                canDelete &= fo.canWrite();
//                                if (container == null) {
//                                    container = fo.getParent();
//                                    canDelete &= container.canWrite();
//                                    canDelete &= LIB_COPY_LIBS.equals(container.getName());
//                                    canDelete &= libFolder.equals(container.getParent());
//                                } else {
//                                    canDelete &= container.equals(fo.getParent());
//                                }
//                            }
//                        }
//                        if (canDelete && container != null) {
//                            container.delete();
//                        }
//                    }
//                    refHelper.copyLibrary(globalCopyLibs);
//
//                } catch (IllegalArgumentException iae) {
//                    LOG.log(
//                            Level.WARNING,
//                            "Cannot update {0} due to invalid version.", //NOI18N
//                            projectCopyLibs.getDisplayName());
//                } catch (IOException ioe) {
//                    Exceptions.printStackTrace(ioe);
//                }
//            }
//
//            @CheckForNull
//            private FileObject toFile(@NonNull final URL url) {
//                final URL file = FileUtil.getArchiveFile(url);
//                return URLMapper.findFileObject(file != null ? file : url);
//            }
//        };
//    }
//
    @NonNull
    private Runnable newConfigCustomizerAction() {
        return () -> {
            J2SEModularProject.this.getLookup().lookup(CustomizerProviderImpl.class).
                showCustomizer(J2SECompositePanelProvider.RUN);
        };
    }

    @NonNull
    private LogicalViewProviders.CompileOnSaveBadge newCoSBadge() {
        return new LogicalViewProviders.CompileOnSaveBadge() {
            @Override
            public boolean isBadgeVisible() {
                return !J2SEModularProjectUtil.isCompileOnSaveEnabled(J2SEModularProject.this) &&
                    J2SEModularProjectUtil.isCompileOnSaveSupported(J2SEModularProject.this);
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
        @NonNull final J2SEModularProject project,
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

    private static final class PropertyEvaluatorProviderImpl implements PropertyEvaluatorProvider {
        private final PropertyEvaluator eval;

        PropertyEvaluatorProviderImpl(@NonNull final PropertyEvaluator eval) {
            Parameters.notNull("eval", eval);   //NOI18N
            this.eval = eval;
        }

        @NonNull
        @Override
        public PropertyEvaluator getPropertyEvaluator() {
            return this.eval;
        }
    }

    private final class PlatformChangedHook implements BrokenReferencesSupport.PlatformUpdatedCallBack {
        @Override
        public void platformPropertyUpdated(@NonNull final JavaPlatform platform) {
            ProjectPlatformProviderImpl.updateProjectXml(platform, updateHelper);
        }
    }
}
