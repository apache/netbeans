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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import static org.netbeans.modules.apisupport.project.Bundle.*;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.queries.AccessibilityQueryImpl;
import org.netbeans.modules.apisupport.project.queries.AnnotationProcessingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.AntArtifactProviderImpl;
import org.netbeans.modules.apisupport.project.queries.BinaryForSourceImpl;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImpl;
import org.netbeans.modules.apisupport.project.queries.CompilerOptionsQueryImpl;
import org.netbeans.modules.apisupport.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.JavadocForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.ModuleProjectClassPathExtender;
import org.netbeans.modules.apisupport.project.queries.ProjectWhiteListQueryImplementation;
import org.netbeans.modules.apisupport.project.queries.SourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.apisupport.project.queries.SubprojectProviderImpl;
import org.netbeans.modules.apisupport.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.apisupport.project.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.netbeans.modules.apisupport.project.ui.ModuleActions;
import org.netbeans.modules.apisupport.project.ui.ModuleLogicalView;
import org.netbeans.modules.apisupport.project.ui.ModuleOperations;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.NbModulePackageModifierImplementation;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
@AntBasedProjectRegistration(
    type=NbModuleProject.TYPE,
    iconResource=NbModuleProject.NB_PROJECT_ICON_PATH,
    sharedName=NbModuleProject.NAME_SHARED,
    sharedNamespace= NbModuleProject.NAMESPACE_SHARED,
    privateName="data", // NOI18N
    privateNamespace= "http://www.netbeans.org/ns/nb-module-project-private/1" // NOI18N
)
public final class NbModuleProject implements Project {
    
    static final String TYPE = "org.netbeans.modules.apisupport.project"; // NOI18N
    static final String NAME_SHARED = "data"; // NOI18N
    public static final String NAMESPACE_SHARED = "http://www.netbeans.org/ns/nb-module-project/3"; // NOI18N
    public static final String NAMESPACE_SHARED_2 = "http://www.netbeans.org/ns/nb-module-project/2"; // NOI18N
    public static final String NB_PROJECT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/resources/module.png"; // NOI18N
    public static final String NB_PROJECT_OSGI_ICON_PATH =
            "org/netbeans/modules/apisupport/project/resources/bundle.png"; // NOI18N
    
    private static final Icon NB_PROJECT_ICON = ImageUtilities.loadImageIcon(NB_PROJECT_ICON_PATH, false);
    
    public static final String SOURCES_TYPE_JAVAHELP = "javahelp"; // NOI18N
    static final String[] COMMON_TEST_TYPES = {"unit", "qa-functional"}; // NOI18N
    
    public static final String OPENIDE_MODULE_NAME = "OpenIDE-Module-Name"; // NOI18N
    
    private final AntProjectHelper helper;
    private final Evaluator eval;
    private final Lookup lookup;
    private final InstanceContent ic;
    private Map<FileObject,Element> extraCompilationUnits;
    private final GeneratedFilesHelper genFilesHelper;
    
    @Messages({
        "# {0} - project directory", "NbModuleProject.too_new=This version of the IDE is too old to read metadata in {0}.",
        "LBL_source_packages=Source Packages",
        "LBL_qa-functional_test_packages=Functional Test Packages",
        "LBL_unit_test_packages=Unit Test Packages",
        "LBL_javahelp_packages=JavaHelp"
    })
    public NbModuleProject(AntProjectHelper helper) throws IOException {
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        for (int v = 4; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/nb-module-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbModuleProject_too_new(FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " has no defined <code-name-base>"); // NOI18N
        }
        if (getModuleType() == NbModuleType.NETBEANS_ORG && ModuleList.findNetBeansOrg(getProjectDirectoryFile()) == null) {
            // #69097: preferable to throwing an assertion error later...
            throw new IOException("netbeans.org-type module requires at least nbbuild: " + FileUtil.getFileDisplayName(helper.getProjectDirectory())); // NOI18N
        }
        eval = new Evaluator(this);
        // XXX could add globs for other package roots too
        List<String> from = new ArrayList<String>();
        List<String> to = new ArrayList<String>();
        from.add("${src.dir}/*.java"); // NOI18N
        to.add("${build.classes.dir}/*.class"); // NOI18N
        for (String type : supportedTestTypes(false)) {
            from.add("${test." + type + ".src.dir}/*.java"); // NOI18N
            to.add("${build.test." + type + ".classes.dir}/*.class"); // NOI18N
        }
        FileBuiltQueryImplementation fileBuilt = helper.createGlobFileBuiltQuery(
                eval,from.toArray(new String[0]), to.toArray(new String[0]));
        SourcesHelper sourcesHelper = new SourcesHelper(this, helper, eval);
        // Temp build dir is always internal; NBM build products go elsewhere, but
        // difficult to predict statically exactly what they are!
        // XXX would be good to mark at least the module JAR as owned by this project
        // (currently FOQ/SH do not support that)
        sourcesHelper.sourceRoot("${src.dir}")// NOI18N
                .hint(JavaProjectConstants.SOURCES_HINT_MAIN)
                .displayName(LBL_source_packages()).add() // as principal root
                .type(JavaProjectConstants.SOURCES_TYPE_JAVA).add();    // as typed root
        for (String type : supportedTestTypes(false)) {
            sourcesHelper.sourceRoot("${test." + type + ".src.dir}")// NOI18N
                    .hint(JavaProjectConstants.SOURCES_HINT_TEST)
                    .displayName(type.equals("qa-functional") ? LBL_qa_functional_test_packages() : LBL_unit_test_packages()).add() // as principal root
                    .type(JavaProjectConstants.SOURCES_TYPE_JAVA).add();    // as typed root
        }
        // XXX other principal source roots, as needed...
        if (helper.resolveFileObject("javahelp/manifest.mf") == null) { // NOI18N
            // Special hack for core - ignore core/javahelp
            sourcesHelper.sourceRoot("javahelp").type(SOURCES_TYPE_JAVAHELP)
                    .displayName(LBL_javahelp_packages()).add();
        }
        for (Map.Entry<FileObject,Element> entry : getExtraCompilationUnits().entrySet()) {
            Element pkgrootEl = XMLUtil.findElement(entry.getValue(), "package-root", NbModuleProject.NAMESPACE_SHARED); // NOI18N
            String pkgrootS = XMLUtil.findText(pkgrootEl);
            FileObject root = entry.getKey();
            // #192773: try to make a unique display name; a schema addition might be better
            String displayName = FileUtil.getRelativePath(getProjectDirectory(), root);
            if (displayName == null) {
                displayName = root.getNameExt();
            }
            sourcesHelper.sourceRoot(pkgrootS).type(JavaProjectConstants.SOURCES_TYPE_JAVA).displayName(displayName).add();
        }
        // #56457: support external source roots too.
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        ic = new InstanceContent();
        lookup = createLookup(new Info(), aux, helper, fileBuilt, sourcesHelper);
    }

    public @Override String toString() {
        return "NbModuleProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    @Override public Lookup getLookup() {
        return lookup;
    }

    public void refreshLookup() {
        if (getModuleType() == NbModuleType.SUITE_COMPONENT) {
            if (lookup.lookup(SuiteProvider.class) == null) {
                ic.add(new SuiteProviderImpl());
            }
        } else {
            SuiteProvider sp = lookup.lookup(SuiteProvider.class);
            if (sp != null) {
                ic.remove(sp);
            }
        }
    }

    private Lookup createLookup(ProjectInformation info, AuxiliaryConfiguration aux, AntProjectHelper helper, FileBuiltQueryImplementation fileBuilt, final SourcesHelper sourcesHelper) {
        Sources srcs = sourcesHelper.createSources();
        srcs.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                // added source root, probably via SourceGroupModifiedImplementation
                getLookup().lookup(ModuleActions.class).refresh();
            }
        });
        ExtraSJQEvaluator eJSQEval = new ExtraSJQEvaluator();

        ic.add(this);
        ic.add(info);
        ic.add(aux);
        ic.add(helper.createCacheDirectoryProvider());
        ic.add(helper.createAuxiliaryProperties());
        ic.add(new SavedHook());
        ic.add(UILookupMergerSupport.createProjectOpenHookMerger(new OpenedHook()));
        ic.add(new ModuleActions(this));
        ic.add(new ClassPathProviderImpl(this));
        ic.add(new SourceForBinaryImpl(this));
        ic.add(new BinaryForSourceImpl(this));
        ic.add(LookupMergerSupport.createSFBLookupMerger());
        ic.add(createESQI(eJSQEval));
        ic.add(new JavadocForBinaryImpl(this));
        ic.add(LookupMergerSupport.createJFBLookupMerger());
        ic.add(createEJQI(eJSQEval));
        ic.add(new UnitTestForSourceQueryImpl(this));
        ic.add(new ModuleLogicalView(this));
        ic.add(new SubprojectProviderImpl(this));
        ic.add(fileBuilt);
        ic.add(new AccessibilityQueryImpl(this));
        ic.add(new SourceLevelQueryImpl(this));
        //ic.add(new ProjectWhiteListQueryImplementation(this));
        ic.add(new ProjectWhiteListQueryImplementation(this));
        ic.add(new NbModulePackageModifierImplementation(this));
        ic.add(helper.createSharabilityQuery2(evaluator(), new String[0], new String[]{
                    "${build.dir}", // NOI18N
                }));
        ic.add(srcs);
        ic.add(sourcesHelper.createSourceGroupModifierImplementation());    // XXX only for unit tests, will need custom impl for qa-functional
        ic.add(new AntArtifactProviderImpl(this, helper, evaluator()));
        ic.add(new CustomizerProviderImpl(this, getHelper(), evaluator()));
        ic.add(new NbModuleProviderImpl(this));
        ic.add(new NbRefactoringProviderImpl(this));
        ic.add(new NbProjectProviderImpl(this));
        ic.add(new PrivilegedTemplatesImpl());
        ic.add(new ModuleProjectClassPathExtender(this));
        ic.add(new LocalizedBundleInfoProvider());
        ic.add(new ModuleOperations(this));
        ic.add(LookupProviderSupport.createSourcesMerger());
        ic.add(UILookupMergerSupport.createPrivilegedTemplatesMerger());
        ic.add(UILookupMergerSupport.createRecommendedTemplatesMerger());
        ic.add(new TemplateAttributesProvider(this, getHelper(), getModuleType() == NbModuleType.NETBEANS_ORG));
        ic.add(new FileEncodingQueryImpl());
        ic.add(new AnnotationProcessingQueryImpl(this));
        ic.add(new PlatformJarProviderImpl());
        ic.add(new CompilerOptionsQueryImpl(this));

        if (getModuleType() == NbModuleType.SUITE_COMPONENT) {
            ic.add(new SuiteProviderImpl());
        }
        Lookup baseLookup = new AbstractLookup(ic);
        return  LookupProviderSupport.createCompositeLookup(baseLookup, "Projects/org-netbeans-modules-apisupport-project/Lookup"); //NOI18N
    }



    @Override public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public File getProjectDirectoryFile() {
        return FileUtil.toFile(getProjectDirectory());
    }
    
    /**
     * Get the minimum harness version required to work with this module.
     */
    public HarnessVersion getMinimumHarnessVersion() {
        if (helper.createAuxiliaryConfiguration().getConfigurationFragment(NbModuleProject.NAME_SHARED, NbModuleProject.NAMESPACE_SHARED_2, true) != null) {
            return HarnessVersion.V50;
        } else {
            return HarnessVersion.V55u1;
        }
    }

    /**
     * Replacement for {@link AntProjectHelper#getPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public Element getPrimaryConfigurationData() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            @Override public Element run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                Element data = ac.getConfigurationFragment(NbModuleProject.NAME_SHARED, NbModuleProject.NAMESPACE_SHARED_2, true);
                if (data != null) {
                    return XMLUtil.translateXML(data, NbModuleProject.NAMESPACE_SHARED);
                } else {
                    return helper.getPrimaryConfigurationData(true);
                }
            }
        });
    }

    /**
     * Replacement for {@link AntProjectHelper#putPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public void putPrimaryConfigurationData(final Element data) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override public Void run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                if (ac.getConfigurationFragment(NbModuleProject.NAME_SHARED, NbModuleProject.NAMESPACE_SHARED_2, true) != null) {
                    ac.putConfigurationFragment(XMLUtil.translateXML(data, NbModuleProject.NAMESPACE_SHARED_2), true);
                } else {
                    helper.putPrimaryConfigurationData(data, true);
                }
                return null;
            }
        });
    }

    /** Returns a relative path to a project's source directory. */
    public String getSourceDirectoryPath() {
        return evaluator().getProperty("src.dir"); // NOI18N
    }

    public NbModuleType getModuleType() {
        Element data = getPrimaryConfigurationData();
        if (XMLUtil.findElement(data, "suite-component", NbModuleProject.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleType.SUITE_COMPONENT;
        } else if (XMLUtil.findElement(data, "standalone", NbModuleProject.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleType.STANDALONE;
        } else {
            return NbModuleType.NETBEANS_ORG;
        }
    }
    
    public @CheckForNull FileObject getManifestFile() {
        String v = evaluator().getProperty("manifest.mf");
        return v != null ? helper.resolveFileObject(v) : null;
    }
    
    public @CheckForNull Manifest getManifest() {
        return Util.getManifest(getManifestFile());
    }

    public AntProjectHelper getHelper() {
        return helper;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private FileObject getDir(String prop) {
        String v = evaluator().getProperty(prop);
        if (v == null) {
            Logger.getLogger(NbModuleProject.class.getName()).log(Level.WARNING,
                    "#150612: property {0} was undefined in {1}", new Object[] {prop, this});
            return null;
        }
        return helper.resolveFileObject(v);
    }

    public FileObject getSourceDirectory() {
        return getDir("src.dir"); // NOI18N
    }
    
    public FileObject getTestSourceDirectory(String type) {
        return getDir("test." + type + ".src.dir"); // NOI18N
    }

    private File getTestSourceDirectoryFile(String type) {
        String dir = evaluator().getProperty("test." + type + ".src.dir"); // NOI18N
        return dir != null ? helper.resolveFile(dir) : null;
    }
    
    public @NonNull File getClassesDirectory() {
        String classesDir = evaluator().getProperty("build.classes.dir"); // NOI18N
        return helper.resolveFile(classesDir != null ? classesDir : "unknown");
    }
    
    public @NonNull File getTestClassesDirectory(String type) {
        String testClassesDir = evaluator().getProperty("build.test." + type + ".classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir != null ? testClassesDir : "unknown");
    }

    public File getGeneratedClassesDirectory() {
        return gensrc(getClassesDirectory());
    }

    public File getTestGeneratedClassesDirectory(String type) {
        return gensrc(getTestClassesDirectory(type));
    }

    private File gensrc(File clazz) {
        return new File(clazz.getParentFile(), clazz.getName() + "-generated"); // NOI18N
    }
    
    public FileObject getJavaHelpDirectory() {
        if (helper.resolveFileObject("javahelp/manifest.mf") != null) { // NOI18N
            // Special hack for core.
            return null;
        }
        return helper.resolveFileObject("javahelp"); // NOI18N
    }
    
    public @NonNull File getModuleJarLocation() {
        String v = evaluator().evaluate("${cluster}/${module.jar}");
        // XXX could use ModuleList here instead
        return helper.resolveFile(v != null ? v : "unknown");
    }
    
    public @NonNull File getTestUserDirLockFile() {
        String v = evaluator().evaluate("${test.user.dir}/lock");
        return getHelper().resolveFile(v != null ? v : "unknown");
    }

    public String getCodeNameBase() {
        Element config = getPrimaryConfigurationData();
        Element cnb = XMLUtil.findElement(config, "code-name-base", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        if (cnb != null) {
            return XMLUtil.findText(cnb);
        } else {
            return null;
        }
    }
    
    private String getModuleName() {
        Manifest m = getManifest();
        if (m != null) {
            String moduleName = m.getMainAttributes().getValue(OPENIDE_MODULE_NAME);
            if (moduleName != null) {
                return moduleName;
            }
        }
        return null;
    }
    
    public @CheckForNull String getSpecVersion() {
        //TODO shall we check for illegal cases like "none-defined" or "both-defined" here?
        Manifest m = getManifest();
        if (m != null) {
            String manVersion = ManifestManager.getInstance(m, false).getSpecificationVersion();
            if (manVersion != null) {
                return manVersion;
            }
        }
        String svb = evaluator().getProperty(SingleModuleProperties.SPEC_VERSION_BASE);
        if (svb != null) {
            return svb/* #72826 */.replaceAll("(\\.[0-9]+)\\.0$", "$1"); // NOI18N
        }
        return null;
    }
    
    /**
     * Slash-separated path inside netbeans.org sources, or null for external modules.
     */
    public String getPathWithinNetBeansOrg() {
        FileObject nbroot = getNbrootFileObject(null);
        if (nbroot != null) {
            return FileUtil.getRelativePath(nbroot, getProjectDirectory());
        } else {
            return null;
        }
    }
    
    private File getNbroot() {
        File dir = getProjectDirectoryFile();
        File nbroot = ModuleList.findNetBeansOrg(dir);
        if (nbroot != null) {
            return nbroot;
        } else {
            // OK, not it.
            NbPlatform platform = getPlatform();
            if (platform != null) {
                URL[] roots = platform.getSourceRoots();
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].getProtocol().equals("file")) { // NOI18N
                        File f = Utilities.toFile(URI.create(roots[i].toExternalForm()));
                        if (ModuleList.isNetBeansOrg(f)) {
                            return f;
                        }
                    }
                }
            }
            // Did not find it.
            return null;
        }
    }
    
    public File getNbrootFile(String path) {
        File nbroot = getNbroot();
        if (nbroot != null) {
            return new File(nbroot, path.replace('/', File.separatorChar));
        } else {
            return null;
        }
    }
    
    public FileObject getNbrootFileObject(String path) {
        File f = path != null ? getNbrootFile(path) : getNbroot();
        if (f != null) {
            return FileUtil.toFileObject(f);
        } else {
            return null;
        }
    }
    
    public ModuleList getModuleList() throws IOException {
        NbPlatform p = getPlatform(false);
        if (p == null || ! p.isValid()) {
            // #67148: have to use something... (and getEntry(codeNameBase) will certainly fail!)
            if (getModuleType() == NbModuleType.NETBEANS_ORG) {
                // #174689: NB.org modules scan sources, not binary platform
                return ModuleList.getModuleList(getProjectDirectoryFile(), null);
            }
            // TODO dealing with nonexistent platforms probably not complete / 100% correct yet,
            // see #61227; but project with unresolved platform may also load as result
            // of suite-chaining; perhaps resolve already in loadProject
            Util.err.log(ErrorManager.WARNING, "Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) // NOI18N
                    + " is missing its platform '" + evaluator().getProperty("nbplatform.active") + "', switching to default platform");    // NOI18N
            NbPlatform p2 = NbPlatform.getDefaultPlatform();
            return ModuleList.getModuleList(getProjectDirectoryFile(), p2 != null ? p2.getDestDir() : null);
        }
        ModuleList ml;
        try {
            ml = ModuleList.getModuleList(getProjectDirectoryFile(), p.getDestDir());
        } catch (IOException x) {
            // #69029: maybe invalidated platform? Try the default platform instead.
            Logger.getLogger(NbModuleProject.class.getName()).log(Level.FINE, null, x);
            NbPlatform p2 = NbPlatform.getDefaultPlatform();
            return ModuleList.getModuleList(getProjectDirectoryFile(), p2 != null ? p2.getDestDir() : null);
        }
        if (ml.getEntry(getCodeNameBase()) == null) {
            ModuleList.refresh();
            ml = ModuleList.getModuleList(getProjectDirectoryFile());
            if (ml.getEntry(getCodeNameBase()) == null) {
                // XXX try to give better diagnostics - as examples are discovered
                Util.err.log(ErrorManager.WARNING, "Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " does not appear to be listed in its own module list; some sort of misconfiguration (e.g. not listed in its own suite)"); // NOI18N
            }
        }
        return ml;
    }
    
    /**
     * Get the platform which this project is currently associated with.
     * @param fallback if true, fall back to the default platform if necessary
     * @return the current platform; or null if fallback is false and there is no
     *         platform specified, or an invalid platform is specified, or even if
     *         fallback is true but even the default platform is not available
     */
    public NbPlatform getPlatform(boolean fallback) {
        NbPlatform p = getPlatform();
        if (fallback && (p == null || !p.isValid())) {
            p = NbPlatform.getDefaultPlatform();
        }
        return p;
    }
    
    private NbPlatform getPlatform() {
        File file = getPlatformFile();
        if (file == null) {
            return null;
        }
        String harnessDir = evaluator().getProperty("harness.dir");
        return NbPlatform.getPlatformByDestDir(file, harnessDir != null ? getHelper().resolveFile(harnessDir) : null);
    }
    
    File getPlatformFile() {
        String prop = evaluator().getProperty(ModuleList.NETBEANS_DEST_DIR);
        if (prop == null) {
            return null;
        }
        return getHelper().resolveFile(prop);
    }

    /**
     * Check whether Javadoc generation is possible.
     */
    public boolean supportsJavadoc() {
        if (evaluator().getProperty("module.javadoc.packages") != null) {
            return true;
        }
        Element config = getPrimaryConfigurationData();
        Element pubPkgs = XMLUtil.findElement(config, "public-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        if (pubPkgs == null) {
            // Try <friend-packages> too.
            pubPkgs = XMLUtil.findElement(config, "friend-packages", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        }
        return pubPkgs != null && !XMLUtil.findSubElements(pubPkgs).isEmpty();
    }
    
    public List<String> supportedTestTypes() {
        return supportedTestTypes(true);
    }

    public List<String> supportedTestTypes(boolean mustExist) {
        List<String> types = new ArrayList<String>();
        for (String type : COMMON_TEST_TYPES) {
            if (((mustExist && getTestSourceDirectory(type) != null)
                        || (! mustExist && getTestSourceDirectoryFile(type) != null))
                    && !Boolean.parseBoolean(evaluator().getProperty("disable." + type + ".tests"))) {
                types.add(type);
            }
        }
        // XXX could look for others in project.xml, in which case fix Evaluator to use that
        return types;
    }
    
    /**
     * Find marked extra compilation units.
     * Gives a map from the package root to the defining XML element.
     */
    public Map<FileObject,Element> getExtraCompilationUnits() {
        if (extraCompilationUnits == null) {
            extraCompilationUnits = new HashMap<FileObject,Element>();
            for (Element ecu : XMLUtil.findSubElements(getPrimaryConfigurationData())) {
                if (ecu.getLocalName().equals("extra-compilation-unit")) { // NOI18N
                    Element pkgrootEl = XMLUtil.findElement(ecu, "package-root", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                    String pkgrootS = XMLUtil.findText(pkgrootEl);
                    String pkgrootEval = evaluator().evaluate(pkgrootS);
                    FileObject pkgroot = pkgrootEval != null ? getHelper().resolveFileObject(pkgrootEval) : null;
                    if (pkgroot == null) {
                        Util.err.log(ErrorManager.WARNING, "Could not find package-root " + pkgrootEval + " for " + getCodeNameBase());
                        continue;
                    }
                    extraCompilationUnits.put(pkgroot, ecu);
                }
            }
        }
        return extraCompilationUnits;
    }
    
    /** Get the Java source level used for this module. Default is 1.4. */
    public String getJavacSource() {
        String sourceLevel = evaluator().getProperty(SingleModuleProperties.JAVAC_RELEASE);
        if (sourceLevel != null && !sourceLevel.isEmpty()) {
            return sourceLevel;
        }
        sourceLevel = evaluator().getProperty(SingleModuleProperties.JAVAC_SOURCE);
        assert sourceLevel != null;
        return sourceLevel;
    }
    
    private ClassPath[] boot, source, compile;
    private final class OpenedHook extends ProjectOpenedHook {
        OpenedHook() {}
        @Override protected void projectOpened() {
            open();
        }
        @Override protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(NbModuleProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            // XXX could discard caches, etc.
            // unregister project's classpaths to GlobalClassPathRegistry
            if (boot != null) {
                assert source != null && compile != null : "#46802 / #201230";
                GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, boot);
                GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, source);
                GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, compile);
                boot = null;
                source = null;
                compile = null;
            }
        }
    }
    /**
     * Run the open hook.
     * For use from unit tests.
     */
    public void open() {
        // write user.properties.file=$userdir/build.properties to platform-private.properties
        if (getModuleType() == NbModuleType.STANDALONE) {
            // XXX skip this in case nbplatform.active is not defined
            final Project p = this; // XXX workaround for NB editor bug
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                @Override public Void run() {
                    String path = "nbproject/private/platform-private.properties"; // NOI18N
                    EditableProperties ep = getHelper().getProperties(path);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    getHelper().putProperties(path, ep);
                    try {
                        ProjectManager.getDefault().saveProject(p);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
        }
        // register project's classpaths to GlobalClassPathRegistry
        ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
        ClassPath[] _boot = cpProvider.getProjectClassPaths(ClassPath.BOOT);
        assert _boot != null : "No BOOT path";
        ClassPath[] _source = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
        assert _source != null : "No SOURCE path";
        ClassPath[] _compile = cpProvider.getProjectClassPaths(ClassPath.COMPILE);
        assert _compile != null : "No COMPILE path";
        // Possible cause of #68414: do not change instance vars until after the dangerous stuff has been computed.
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, _boot);
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, _source);
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, _compile);
        boot = _boot;
        source = _source;
        compile = _compile;
        // refresh build.xml and build-impl.xml for external modules
        if (getModuleType() != NbModuleType.NETBEANS_ORG) {
            try {
                refreshBuildScripts(true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /**
     * <strong>For use from unit tests only.</strong> Returns {@link
     * LocalizedBundleInfo} for this project.
     */
    public @CheckForNull LocalizedBundleInfo getBundleInfo() {
        return getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
    }
    
    private int atomicActionCounter = 0;

    /** See issue #69440 for more details. */
    public synchronized void setRunInAtomicAction(boolean runInAtomicAction) {
        if (atomicActionCounter == 0 && ! runInAtomicAction) {
            throw new IllegalArgumentException("Not in atomic action");
        }    // NOI18N
        atomicActionCounter += runInAtomicAction ? 1 : -1;
        if (runInAtomicAction && atomicActionCounter == 1) {
            eval.setRunInAtomicAction(true);
        } else if (! runInAtomicAction && atomicActionCounter == 0) {
            eval.setRunInAtomicAction(false);
        }
    }
    
    /** See issue #69440 for more details. */
    public synchronized boolean isRunInAtomicAction() {
        return eval.isRunInAtomicAction();
    }
    
    private final class Info implements ProjectInformation, PropertyChangeListener {
        
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        private final String name;
        private final String openideModuleName;
        private String displayName;
        
        Info() {
            String cnb = getCodeNameBase();
            name = cnb != null ? cnb : /* #70490 */getProjectDirectory().toString();
            String omn = getModuleName();
            openideModuleName = omn;
        }
        
        @Override public String getName() {
            return name;
        }

        private String getOpenideModuleName() {
            return openideModuleName;
        }
        
        @Override public String getDisplayName() {
            if (displayName == null) {
                LocalizedBundleInfo bundleInfo = getBundleInfo();
                if (bundleInfo != null) {
                    displayName = bundleInfo.getDisplayName();
                }
            }
            if (displayName == null) {
                displayName = getOpenideModuleName();
            }
            if (/* #70490 */displayName == null) {
                displayName = getName();
            }
            assert displayName != null : NbModuleProject.this;
            return displayName;
        }
        
        private void setDisplayName(String newDisplayName) {
            String oldDisplayName = getDisplayName();
            displayName = newDisplayName == null ? getName() : newDisplayName;
            firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, displayName);
        }
        
        @Override public Icon getIcon() {
            return NB_PROJECT_ICON;
        }
        
        @Override public Project getProject() {
            return NbModuleProject.this;
        }
        
        @Override public void addPropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.addPropertyChangeListener(pchl);
        }
        
        @Override public void removePropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.removePropertyChangeListener(pchl);
        }
        
        private void firePropertyChange(String propName, Object oldValue, Object newValue) {
            changeSupport.firePropertyChange(propName, oldValue, newValue);
        }

        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                setDisplayName((String) evt.getNewValue());
            }
        }
        
    }
    
    public void notifyDeleting() {
        eval.removeListeners();
    }
        
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        @Override protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml for external modules
            if (getModuleType() != NbModuleType.NETBEANS_ORG) {
                refreshBuildScripts(false);
            }
        }
        
    }
    
    public void refreshBuildScripts(boolean checkForProjectXmlModified) throws IOException {
        refreshBuildScripts(checkForProjectXmlModified, getPlatform(true));
    }
    
    public void refreshBuildScripts(boolean checkForProjectXmlModified, NbPlatform customPlatform) throws IOException {
        if (customPlatform == null) { // #181798
            return;
        }
        String buildImplPath =
                    customPlatform.getHarnessVersion().compareTo(HarnessVersion.V65) <= 0
                    || eval.getProperty(SuiteProperties.CLUSTER_PATH_PROPERTY) == null
                    ? "build-impl-65.xsl" : "build-impl.xsl";    // NOI18N
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                NbModuleProject.class.getResource("resources/" + buildImplPath), // NOI18N
                checkForProjectXmlModified);
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                NbModuleProject.class.getResource("resources/build.xsl"), // NOI18N
                checkForProjectXmlModified);
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {

        @Override public File getSuiteDirectory() {
            String suiteDir = evaluator().getProperty("suite.dir"); // NOI18N
            return suiteDir == null ? null : helper.resolveFile(suiteDir);
        }

        @Override public File getClusterDirectory() {
            return getModuleJarLocation().getParentFile().getParentFile().getAbsoluteFile();
        }
        
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            //"Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/JUnit/SimpleJUnitTest.java", // NOI18N
            "Templates/" + UIUtil.TEMPLATE_FOLDER + "/" + UIUtil.TEMPLATE_ACTION_ID,
            "Templates/" + UIUtil.TEMPLATE_FOLDER + "/newHTML",
            "Templates/" + UIUtil.TEMPLATE_FOLDER + "/" + UIUtil.TEMPLATE_WINDOW_ID,
            //"Templates/Other/properties.properties", // NOI18N
        };
        static {
            assert PRIVILEGED_NAMES.length <= 10 : "Too many privileged templates to fit! extras will be ignored: " +
                    Arrays.asList(PRIVILEGED_NAMES).subList(10, PRIVILEGED_NAMES.length);
        }
        
        private static final String[] RECOMMENDED_TYPES = {
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "REST-clients",         // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "junit",                // NOI18N                    
            "simple-files",         // NOI18N
            UIUtil.TEMPLATE_CATEGORY,
        };
        
        @Override public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        @Override public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }    

    private final class LocalizedBundleInfoProvider extends FileChangeAdapter implements LocalizedBundleInfo.Provider {

        private LocalizedBundleInfo bundleInfo;
        private FileObject manifestFO;
        private final FileChangeListener listener = FileUtil.weakFileChangeListener(this, null);

        @Override public LocalizedBundleInfo getLocalizedBundleInfo() {
            if (bundleInfo == null) {
                Manifest mf = getManifest();
                FileObject srcFO = getSourceDirectory();
                if (mf != null && srcFO != null) {
                    bundleInfo = ApisupportAntUtils.findLocalizedBundleInfo(srcFO, mf);
                }
                if (bundleInfo != null) {
                    bundleInfo.addPropertyChangeListener(getLookup().lookup(Info.class));
                }
                if (mf != null) {
                    manifestFO = getManifestFile();
                    if (manifestFO != null) {
                        manifestFO.addFileChangeListener(listener);
                    }
                }
            }
            return bundleInfo;
        }

        @Override public void fileChanged(FileEvent fe) {
            // cannot reload manifest-dependent things immediately (see 67961 for more details)
            bundleInfo = null;
        }

        @Override public void fileDeleted(FileEvent fe) {
            manifestFO = null;
        }

    }

    private SourceForBinaryQueryImplementation createESQI(ExtraSJQEvaluator eJSQEval) {
        SourceForBinaryQueryImplementation sfbqi = ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, getHelper(), eJSQEval);
        registerListener(sfbqi, eJSQEval);
        return sfbqi;
    }

    private JavadocForBinaryQueryImplementation createEJQI(ExtraSJQEvaluator eJSQEval) {
        JavadocForBinaryQueryImplementation jfbqi = ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, getHelper(), eJSQEval);
        registerListener(jfbqi, eJSQEval);
        return jfbqi;
    }

    private void registerListener(Object qimpl, ExtraSJQEvaluator eJSQEval) {
        try {
            // XXX #66275: ugly hack until proper API change;
            // we need ESJQI to work when project is loaded - not opened - to resolve deps on lib wrapper modules correctly
            Field listenerF = qimpl.getClass().getDeclaredField("listener"); // NOI18N
            listenerF.setAccessible(true);
            eJSQEval.addPropertyChangeListener((PropertyChangeListener) listenerF.get(qimpl));
        } catch (Exception ex) {
            Logger.getLogger(NbModuleProject.class.getName()).log(Level.FINE, "Turning off source query support for loaded project '" + ProjectUtils.getInformation(this).getDisplayName() + "'", ex);
        }
    }

    /** Matches ExtraProjectSourceForBinaryQueryImpl*/
    public static final String SOURCE_START = "source.reference."; //NOI18N
    /**
     * <tt>&lt;class-path-extension&gt;</tt> to <tt>file/source/javadoc.reference....</tt> properties adapter
     * for {@link ExtraSourceJavadocSupport} query implementations.
     */
    private final class ExtraSJQEvaluator implements PropertyEvaluator {

        private static final String REF_START = "file.reference."; //NOI18N
        private static final String JAVADOC_START = "javadoc.reference."; //NOI18N

        ExtraSJQEvaluator() {
            evaluator().addPropertyChangeListener(new PropertyChangeListener() {
                @Override public void propertyChange(PropertyChangeEvent evt) {
                    final String prop = evt.getPropertyName();
                    if (prop == null || prop.startsWith(SOURCE_START) || prop.startsWith(JAVADOC_START)) {
                        cache.set(null);
                        pcs.firePropertyChange(evt);
                    }
                }
            });
        }

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private final AtomicReference<Map<String, String>> cache = new AtomicReference<Map<String, String>>();

        @Override public String getProperty(String prop) {
            return getProperties().get(prop);
        }

        @Override public String evaluate(String text) {
            throw new UnsupportedOperationException();  // currently not needed
        }

        @Override @NonNull public Map<String, String> getProperties() {
            Map<String,String> ce = cache.get();
            if (ce == null) {
                ce = new HashMap<String, String>();
                ProjectXMLManager pxm = new ProjectXMLManager(NbModuleProject.this);
                String[] cpExts = pxm.getBinaryOrigins();
                for (String cpe : cpExts) {
                    addFileRef(ce, cpe);
                }
                Map<String, String> prjProps = evaluator().getProperties();
                if (prjProps != null) {
                    for (Map.Entry<String, String> entry : prjProps.entrySet()) {
                        if (entry.getKey().startsWith(SOURCE_START) || entry.getKey().startsWith(JAVADOC_START)) {
                            ce.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                if (!cache.compareAndSet(null, ce)) {
                    Map<String,String> tmp = cache.get();
                    if (tmp != null) {
                        ce = tmp;
                    }
                }
            }
            return ce;
        }

        private void addFileRef(Map<String, String> props, String path) {
            // #66275:
            // XXX parts of code copied from o.n.spi.project.ant.ReferenceHelper;
            // will do proper API change later with issue #70894, will also simplify impl of isssue #66188
            final File normalizedFile = FileUtil.normalizeFile(PropertyUtils.resolveFile(getProjectDirectoryFile(), path));
            String fileID = normalizedFile.getName();
            // if the file is folder then add to ID string also parent folder name,
            // i.e. if external source folder name is "src" the ID will
            // be a bit more selfdescribing, e.g. project-src in case
            // of ID for ant/project/src directory.
            if (normalizedFile.isDirectory() && normalizedFile.getParentFile() != null) {
                fileID = normalizedFile.getParentFile().getName()+"-"+normalizedFile.getName();
            }
            fileID = PropertyUtils.getUsablePropertyName(fileID);
            // we don't need to resolve duplicate file names here, all <c-p-e>-s reside in release/modules/ext
            props.put(REF_START + fileID, path);
        }

        @Override public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

    }

    private class PlatformJarProviderImpl implements PlatformJarProvider {

        @Override public Set<File> getPlatformJars() throws IOException {
            NbPlatform platform = getPlatform(true);
            if (platform == null) {
                return Collections.emptySet();
            }
            Set<ModuleEntry> entries = platform.getModules();
            Set<File> jars = new HashSet<File>(entries.size());
            for (ModuleEntry entry : entries) {
                jars.add(entry.getJarLocation());
            }
            return jars;
        }

    }

}
