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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.apisupport.project.universe.DestDirProvider;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.TestModuleDependency;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

/**
 * Property evaluator for {@link NbModuleProject}.
 * Has two special behaviors of note:
 * 1. Does not call ModuleList until it really needs to.
 * 2. Is reset upon project.xml changes.
 * @author Jesse Glick, Martin Krauskopf
 */
public final class Evaluator implements PropertyEvaluator, PropertyChangeListener, AntProjectListener {
    
    public static final String CP = "cp";
    public static final String NBJDK_BOOTCLASSPATH = "nbjdk.bootclasspath";
    public static final String NBJDK_BOOTCLASSPATH_MODULAR = "nbjdk.bootclasspath.modular"; //NOI18N
    static final String NBJDK_HOME = "nbjdk.home"; // NOI18N
    public static final String RUN_CP = "run.cp";
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9"); //NOI18N
    private static final String JAVACAPI_CNB = "org.netbeans.libs.javacapi";
    
    private final NbModuleProject project;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private PropertyEvaluator delegate;
    private volatile boolean loadedModuleList = false;
    
    /** See issue #69440 for more details. */
    private boolean runInAtomicAction;
    private boolean pendingReset = false;   // issue #173792
    
    private static final Map<Project,Map<String, SoftReference<ModuleEntry>>> cachedProjectModuleEntries = new HashMap<Project,Map<String, SoftReference<ModuleEntry>>>();
    
    private static class TestClasspath {
        
        private final String compile;
        private final String runtime;
        private final String testCompile;
        private final String testRuntime;
        
        public TestClasspath(String compile,String runtime,String testCompile,String testRuntime) {
            this.compile = compile;
            this.runtime = runtime;
            this.testCompile = testCompile;
            this.testRuntime = testRuntime;
        }
        
        public String getCompileClasspath() {
            return compile + ':' + testCompile;
        }
        
        public String getRuntimeClasspath() {
            return runtime + ':' + testRuntime;
        }
        
        private static TestClasspath getOrEmpty(Map<String,TestClasspath> testsCPs, String testtype) {
            TestClasspath tcp = testsCPs.get(testtype);
            if (tcp == null ) {
                // create with empty classpaths
                tcp = new TestClasspath("", "", "", ""); // NOI18N
            }
            return tcp;
        }
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    Evaluator(NbModuleProject project) {
        this.project = project;
        delegate = createEvaluator(null);
        delegate.addPropertyChangeListener(this);
        project.getHelper().addAntProjectListener(this);
    }
    
    public @Override String getProperty(String prop) {
        PropertyEvaluator eval = delegatingEvaluator(false);
        assert eval != this;
        String v = eval.getProperty(prop);
        if ((v == null && isModuleListDependentProperty(prop)) || isModuleListDependentValue(v)) {
            return delegatingEvaluator(true).getProperty(prop);
        } else {
            return v;
        }
    }
    
    public @Override String evaluate(String text) {
        String v = delegatingEvaluator(false).evaluate(text);
        if (isModuleListDependentValue(v)) {
            return delegatingEvaluator(true).evaluate(text);
        } else {
            return v;
        }
    }
    
    public @Override Map<String,String> getProperties() {
        return delegatingEvaluator(true).getProperties();
    }

    private boolean isModuleListDependentProperty(String p) {
        return p.equals("module.classpath") || // NOI18N
                p.equals(CP) || p.endsWith(".cp") || p.endsWith(".cp.extra"); // NOI18N
    }
    
    private static final Pattern ANT_PROP_REGEX = Pattern.compile("\\$\\{([a-zA-Z0-9._-]+)\\}"); // NOI18N
    private boolean isModuleListDependentValue(String v) {
        if (v == null) {
            return false;
        }
        Matcher m = ANT_PROP_REGEX.matcher(v);
        while (m.find()) {
            if (isModuleListDependentProperty(m.group(1))) {
                return true;
            }
        }
        return false;
    }
    
    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    private PropertyEvaluator delegatingEvaluator(final boolean reset) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<PropertyEvaluator>() {
            public @Override PropertyEvaluator run() {
                if (reset && !loadedModuleList) {
                    reset();
                    if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                        Util.err.log("Needed to reset evaluator in " + project + "due to use of module-list-dependent property; now cp=" + delegate.getProperty(CP));
                    }
                }
                synchronized (Evaluator.this) {
                    return delegate;
                }
            }
        });
    }
    
    private void reset() {
        if (!project.getProjectDirectoryFile().exists()) {
            return; // recently deleted?
        }
        ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
            public @Override Void run() {
                final ModuleList moduleList;
                try {
                    moduleList = project.getModuleList();
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    // but leave old evaluator in place for now
                    return null;
                }
                synchronized (Evaluator.this) {
                    loadedModuleList = true;
                    delegate.removePropertyChangeListener(Evaluator.this);
                    delegate = createEvaluator(moduleList);
                    delegate.addPropertyChangeListener(Evaluator.this);
                }
                // XXX better to compute diff between previous and new values and fire just those
                pcs.firePropertyChange(null, null, null);
                return null;
            }
        });
    }
    
    public @Override void propertyChange(PropertyChangeEvent evt) {
        if (ModuleList.NETBEANS_DEST_DIR.equals(evt.getPropertyName()) || evt.getPropertyName() == null) {
            // Module list may have changed.
            reset();
        } else {
            Util.err.log("Refiring property change from delegate in " + evt.getPropertyName() + " for " + project);
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
    
    public @Override void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            if (runInAtomicAction) {
                pendingReset = true;
            } else {
                reset();
            }
        }
    }
    
    public @Override void propertiesChanged(AntProjectEvent ev) {
        /* TODO: Not needed now? Put here at least some comment. */
    }
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        assert runInAtomicAction != this.runInAtomicAction : "Nested calls not supported";
        this.runInAtomicAction = runInAtomicAction;
        if (! runInAtomicAction && pendingReset) {
            reset();
        }
        pendingReset = false;
    }
    
    /** See issue #69440 for more details. */
    boolean isRunInAtomicAction() {
        return runInAtomicAction;
    }
    
    public void removeListeners() {
        project.getHelper().removeAntProjectListener(this);
        delegate.removePropertyChangeListener(this);
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     * @param ml this module list, or may be left null to skip all properties which require knowledge of other modules
     */
    private PropertyEvaluator createEvaluator(ModuleList ml) {
        // XXX a lot of this duplicates ModuleList.parseProperties... can they be shared?
        PropertyProvider predefs = project.getHelper().getStockPropertyPreprovider();
        Map<String,String> stock = new HashMap<String,String>();
        File dir = project.getProjectDirectoryFile();
        NbModuleType type = project.getModuleType();
        PropertyProvider privateProperties = project.getHelper().getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        PropertyProvider projectProperties = project.getHelper().getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        File nbroot;
        if (type == NbModuleType.NETBEANS_ORG) {
            nbroot = ModuleList.findNetBeansOrg(dir);
            assert nbroot != null : "netbeans.org-type module not in a complete netbeans.org source root " + dir;
            stock.put("nb_all", nbroot.getAbsolutePath()); // NOI18N
            // Only needed for netbeans.org modules, since for external modules suite.properties suffices.
            File destDir = ModuleList.findNetBeansOrgDestDir(nbroot);
            stock.put(ModuleList.NETBEANS_DEST_DIR, destDir.getAbsolutePath());
            stock.put("nb.build.dir", "${nb_all}/nbbuild/build"); // #199286
            stock.put("nbantext.jar", "${nb.build.dir}/nbantext.jar");
            // Register *.dir for nb.org modules. There is no equivalent for external modules.
            try {
                Map<String,String> clusterProperties = ModuleList.getClusterProperties(nbroot);
                StringBuilder allValsB = new StringBuilder();
                for (PropertyProvider pp : new PropertyProvider[] {privateProperties, projectProperties}) {
                    for (String val : pp.getProperties().values()) {
                        allValsB.append(val);
                    }
                }
                String allVals = allValsB.toString();
                for (Map.Entry<String,String> dirEntry : clusterProperties.entrySet()) {
                    String key = dirEntry.getKey();
                    if (!key.endsWith(".dir")) { // NOI18N
                        continue;
                    }
                    String clusterDir = new File(destDir, dirEntry.getValue()).getAbsolutePath()./* #48449 */intern();
                    String modules = clusterProperties.get(key.substring(0, key.length() - 4));
                    if (modules == null) {
                        continue;
                    }
                    for (String module : modules.split(",")) { // NOI18N
                        String dirProp = module + ".dir";
                        if (allVals.contains(dirProp)) {
                            stock.put(dirProp.intern(), clusterDir); // NOI18N
                        } // #172203: otherwise don't waste space on it
                    }
                }
            } catch (IOException x) {
                Util.err.notify(ErrorManager.INFORMATIONAL, x);
            }
        } else {
            nbroot = null;
        }

        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(PropertyUtils.fixedPropertyProvider(stock));
        // XXX should listen to changes in values of properties which refer to property files:
        if (type == NbModuleType.SUITE_COMPONENT) {
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/suite-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/suite.properties")); // NOI18N
        }

        // 'cluster' prop. evaluation without scanned ModuleList
        String codeNameBase = project.getCodeNameBase();
        PropertyEvaluator suiteEval = null;
        if (type == NbModuleType.SUITE_COMPONENT) {
            suiteEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
        }

        if (type == NbModuleType.SUITE_COMPONENT) {
            String suiteDirS = suiteEval.getProperty("suite.dir"); // NOI18N
            if (suiteDirS != null) {
                File suiteDir = PropertyUtils.resolveFile(dir, suiteDirS);
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (type == NbModuleType.STANDALONE) {
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/platform.properties")); // NOI18N
        }
        if (type == NbModuleType.SUITE_COMPONENT || type == NbModuleType.STANDALONE) {
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
            providers.add(new ApisupportAntUtils.UserPropertiesFileProvider(baseEval, dir));
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
            providers.add(new DestDirProvider(baseEval));
        }
        if (type == NbModuleType.NETBEANS_ORG) {
            // For local definitions of nbjdk.* properties:
            File nbbuild = new File(nbroot, "nbbuild"); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "user.build.properties"))); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "site.build.properties"))); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(System.getProperty("user.home"), ".nbbuild.properties"))); // NOI18N
        }
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
        providers.add(new NbJdkProvider(baseEval));
        providers.add(privateProperties);
        providers.add(projectProperties);
        Map<String,String> defaults = new HashMap<String,String>();
        if (codeNameBase != null) { // #121856
            defaults.put("code.name.base.dashes", codeNameBase.replace('.', '-')); // NOI18N
        }
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.dir", "build"); // NOI18N
        defaults.put("build.classes.dir", "${build.dir}/classes"); // NOI18N
        defaults.put("release.dir", "release"); // NOI18N
        defaults.put(SingleModuleProperties.JAVAC_SOURCE, "1.4"); // NOI18N
        if (type == NbModuleType.NETBEANS_ORG) {
            defaults.put("test.user.dir", "${nb_all}/nbbuild/testuserdir"); // NOI18N
        } else if (type == NbModuleType.STANDALONE) {
            defaults.put("test.user.dir", "${build.dir}/testuserdir"); // NOI18N
        } else {
            defaults.put("suite.build.dir", "${suite.dir}/build"); // NOI18N
            defaults.put("test.user.dir", "${suite.build.dir}/testuserdir"); // NOI18N
        }
        Set<String> testTypes = new HashSet<String>(Arrays.asList(NbModuleProject.COMMON_TEST_TYPES));
        // XXX would be good to add in any other types defined in project.xml
        for (String testType : testTypes) {
            defaults.put("test." + testType + ".src.dir", "test/" + testType + "/src"); // NOI18N
            defaults.put("test." + testType + ".data.dir", "test/" + testType + "/data"); // NOI18N
            defaults.put("build.test." + testType + ".classes.dir", "${build.dir}/test/" + testType + "/classes"); // NOI18N
        }
        providers.add(PropertyUtils.fixedPropertyProvider(defaults));
        try {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("cluster", ModuleList.findClusterLocation(dir, nbroot, type)))); // NOI18N
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, ex);
        }
        if (ml != null) {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("module.classpath", computeModuleClasspath(ml)))); // NOI18N
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("module.run.classpath", computeRuntimeModuleClasspath(ml)))); // NOI18N
            Map<String,String> buildDefaults = new HashMap<String,String>();
            buildDefaults.put("cp.extra", ""); // NOI18N
            buildDefaults.put(CP, "${module.classpath}:${cp.extra}"); // NOI18N
            buildDefaults.put(RUN_CP, "${module.run.classpath}:${cp.extra}:${build.classes.dir}"); // NOI18N
            if (type == NbModuleType.NETBEANS_ORG && "true".equals(projectProperties.getProperties().get("requires.nb.javac"))) {
                ModuleEntry javacLibrary = ml.getEntry(JAVACAPI_CNB);
                if (javacLibrary != null) {
                    boolean implDependencyOnJavac =
                        projectDependencies().filter(dep -> JAVACAPI_CNB.equals(dependencyCNB(dep)))
                                             .map(dep -> XMLUtil.findElement(dep, "run-dependency", NbModuleProject.NAMESPACE_SHARED)) // NOI18N
                                             .filter(runDep -> runDep != null)
                                             .anyMatch(runDep -> XMLUtil.findElement(runDep, "implementation-version", NbModuleProject.NAMESPACE_SHARED) != null); // NOI18N
                    String bootcpPrepend;
                    if (implDependencyOnJavac) {
                        bootcpPrepend = javacLibrary.getClassPathExtensions();
                    } else {
                        bootcpPrepend = Stream.of(javacLibrary.getClassPathExtensions().split(Pattern.quote(File.pathSeparator)))
                                              .filter(ext -> ext.endsWith("-api.jar"))
                                              .collect(Collectors.joining(File.pathSeparator));
                    }
                    buildDefaults.put(ClassPathProviderImpl.BOOTCLASSPATH_PREPEND, bootcpPrepend);
                }
            }
            
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));

            Map<String,TestClasspath> testsCPs = computeTestingClassPaths(ml, baseEval, testTypes);
            testTypes.addAll(testsCPs.keySet());
            for (String testType : testTypes) {
                buildDefaults.put("test." + testType + ".cp.extra", ""); // NOI18N
                TestClasspath tcp = TestClasspath.getOrEmpty(testsCPs, testType);
                // #165446: module.run.classpath on both compile and run test CPs
                String commonCPEntries = "${module.run.classpath}:${cp.extra}:${cluster}/${module.jar}:${test." + testType + ".cp.extra}:";    // NOI18N
                buildDefaults.put("test." + testType + ".cp", commonCPEntries + tcp.getCompileClasspath()); // NOI18N
                buildDefaults.put("test." + testType + ".run.cp.extra", ""); // NOI18N
                buildDefaults.put("test." + testType + ".run.cp", "${build.test." + testType + ".classes.dir}:" +
                        commonCPEntries + "${test." + testType + ".run.cp.extra}:" + tcp.getRuntimeClasspath()); // NOI18N
            }

            providers.add(PropertyUtils.fixedPropertyProvider(buildDefaults));
        }
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[0]));
    }
    
    private static final RequestProcessor RP = new RequestProcessor(Evaluator.class.getName());
    private final class NbJdkProvider implements PropertyProvider, PropertyChangeListener { // #63541: JDK selection

        private final PropertyEvaluator eval;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private final PropertyChangeListener weakListener = WeakListeners.propertyChange(this, null);
        
        public NbJdkProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(weakListener);
            JavaPlatformManager.getDefault().addPropertyChangeListener(weakListener);
        }
        
        public @Override final Map<String,String> getProperties() {
            Map<String,String> props = new HashMap<String,String>();
            String home = eval.getProperty(NBJDK_HOME);
            if (home == null) {
                String active = eval.getProperty(ModuleProperties.JAVA_PLATFORM_PROPERTY);
                if (active != null && !active.equals("default")) { // NOI18N
                    home = eval.getProperty("platforms." + active + ".home"); // NOI18N
                    if (home != null) {
                        props.put(NBJDK_HOME, home);
                    }
                }
            }
            if (home == null) {
                JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
                if (platform != null) {
                    Collection<FileObject> installs = platform.getInstallFolders();
                    if (installs.size() == 1) {
                        home = FileUtil.toFile(installs.iterator().next()).getAbsolutePath();
                    }
                }
            }
            Object bootcp = null;
            if (home != null) {
                FileObject homeFO = FileUtil.toFileObject(FileUtil.normalizeFile(new File(home)));
                if (homeFO != null) {
                    for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                        if (new HashSet<FileObject>(platform.getInstallFolders()).equals(Collections.singleton(homeFO))) {
                            // Matching JDK is registered, so look up its real bootcp.
                            ClassPath boot = platform.getBootstrapLibraries();
                            boot.removePropertyChangeListener(weakListener);
                            boot.addPropertyChangeListener(weakListener);
                            if (JDK9.compareTo(platform.getSpecification().getVersion()) <= 0) {
                                final Collection<? extends FileObject> loc = platform.getInstallFolders();
                                if (!loc.isEmpty()) {
                                    final File locf = FileUtil.toFile(loc.iterator().next());
                                    if (locf != null) {
                                        bootcp = locf;
                                        break;
                                    }
                                }
                            } else {
                                bootcp = boot.toString(ClassPath.PathConversionMode.WARN);
                                break;
                            }
                        }
                    }
                }
                if (bootcp == null) {
                    //TODO: Fixme for JDK9 - ProjectPlatform should be the solution
                    File jHome;
                    if (home != null && (jHome = new File(home, "jre/lib")).isDirectory()) {
                        String[] jars = jHome.list(new FilenameFilter() {
                            public @Override boolean accept(File dir, String name) {
                                String n = name.toLowerCase(Locale.US);
                                return n.endsWith(".jar"); // NOI18N
                            }
                        });
                        StringBuilder sb = new StringBuilder();
                        for (String jar : jars) {
                            if (sb.length() > 0) {
                                sb.append(File.pathSeparator);
                            }
                            sb.append("${" + NBJDK_HOME + "}/jre/lib/").append(jar);
                        }
                        bootcp = sb.toString().replace('/', File.separatorChar); // NOI18N
                    } else {
                        bootcp = "${" + NBJDK_HOME + "}/jre/lib/rt.jar".replace('/', File.separatorChar); // NOI18N
                    }
                }
            }
            if (bootcp == null) {
                // Real fallback...
                bootcp = "${sun.boot.class.path}"; // NOI18N
            }
            if (bootcp instanceof File) {
                props.remove(NBJDK_BOOTCLASSPATH);
                props.put(NBJDK_BOOTCLASSPATH_MODULAR, ((File) bootcp).getAbsolutePath());    //NOI18N
            } else if (bootcp instanceof String) {
                props.remove(NBJDK_BOOTCLASSPATH_MODULAR);
                props.put(NBJDK_BOOTCLASSPATH, (String)bootcp); // NOI18N
            }
            if (home != null) {
                String toolsJar = home + "/lib/tools.jar".replace('/', File.separatorChar);
                if (new File(toolsJar).exists()) { //On Mac OS X with Apple JDK, everything is in classes.jar, there is no tools.jar
                    props.put("tools.jar", toolsJar); // NOI18N
                }
            }
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Map<String,String> _props = new TreeMap<String,String>(eval.getProperties());
                Iterator<String> it = _props.keySet().iterator();
                while (it.hasNext()) {
                    String k = it.next();
                    if (!k.startsWith("nbjdk.") && !k.startsWith("platforms.")) { // NOI18N
                        it.remove();
                    }
                }
                _props.putAll(props);
                Util.err.log("JDK-related properties of " + project + ": " + _props);
            }
            return props;
        }
        
        public @Override final void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public @Override final void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        
        public @Override final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !p.startsWith("nbjdk.") && !p.startsWith("platforms.") && // NOI18N
                    !p.equals(ClassPath.PROP_ENTRIES) && !p.equals(JavaPlatformManager.PROP_INSTALLED_PLATFORMS)) {
                return;
            }
            if (!changeSupport.hasListeners()) {
                return;
            }
            final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                public @Override Void run() {
                    changeSupport.fireChange();
                    return null;
                }
            };
            // See ProjectProperties.PP.fireChange for explanation of this threading stuff:
            if (ProjectManager.mutex().isWriteAccess()) {
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                action.run();
            } else {
                RP.post(new Runnable() {
                    public @Override void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
    }
    
    private String computeModuleClasspath(ModuleList ml) {
        StringBuilder cp = new StringBuilder();
        projectDependencies()
                .filter(dep -> XMLUtil.findElement(dep, "compile-dependency", NbModuleProject.NAMESPACE_SHARED) != null) // NOI18N
                .map(this::dependencyCNB)
                .forEach(cnb -> {
            ModuleEntry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                return ;
            }
            File moduleJar = module.getJarLocation();
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            cp.append(moduleJar.getAbsolutePath());
            cp.append(module.getClassPathExtensions());
        });
        appendMyOwnClassPathExtensions(cp);
        return cp.toString();
    }

    private Stream<Element> projectDependencies() {
        Element data = project.getPrimaryConfigurationData();
        Element moduleDependencies = XMLUtil.findElement(data,
            "module-dependencies", NbModuleProject.NAMESPACE_SHARED); // NOI18N
        assert moduleDependencies != null : "Malformed metadata in " + project;
        return XMLUtil.findSubElements(moduleDependencies).stream();
    }

    private String dependencyCNB(Element dependency) {
        Element cnbEl = XMLUtil.findElement(dependency, "code-name-base", // NOI18N
            NbModuleProject.NAMESPACE_SHARED);
        return XMLUtil.findText(cnbEl);
    }

    /**
     * Follows transitive runtime dependencies.
     * @see "issue #70206"
     */
    private String computeRuntimeModuleClasspath(ModuleList ml) {
        Set<String> unprocessed = new HashSet<String>();
        unprocessed.add(project.getCodeNameBase());
        Set<String> processed = new HashSet<String>();
        StringBuilder cp = new StringBuilder();
        if(cachedProjectModuleEntries.get(project) == null) {
            cachedProjectModuleEntries.put(project, new HashMap<String, SoftReference<ModuleEntry>>());
        }
        Map<String, SoftReference<ModuleEntry>> cachedModuleEntries = cachedProjectModuleEntries.get(project);
        while (!unprocessed.isEmpty()) { // crude breadth-first search
            Iterator<String> it = unprocessed.iterator();
            String cnb = it.next();
            it.remove();
            if (processed.add(cnb)) {
                ModuleEntry module = null;
                if(cachedModuleEntries.get(cnb) != null && cachedModuleEntries.get(cnb).get() != null) {
                    module = cachedModuleEntries.get(cnb).get();
                }
                else {
                    module = ml.getEntry(cnb);
                    cachedProjectModuleEntries.get(project).put(cnb, new SoftReference<ModuleEntry>(module));
                }
                if (module == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                    continue;
                }
                if (!cnb.equals(project.getCodeNameBase())) { // build/classes for this is special
                    if (cp.length() > 0) {
                        cp.append(File.pathSeparatorChar);
                    }
                    cp.append(module.getJarLocation().getAbsolutePath());
                    cp.append(module.getClassPathExtensions());
                }
                String[] newDeps = module.getRunDependencies();
                unprocessed.addAll(Arrays.asList(newDeps));
            }
        }
        appendMyOwnClassPathExtensions(cp); // #76341: must include <class-path-extension>s in ${run.cp} too.
        return cp.toString();
    }

    private void appendMyOwnClassPathExtensions(StringBuilder cp) {
        // XXX #179578: using ModuleEntry.getClassPathExtensions would be more convenient, but the data is stale;
        // should ModuleList recreate ModuleEntry's when project.xml (or project.properties, ...) changes?
        Map<String,String> cpext = new ProjectXMLManager(project).getClassPathExtensions();
        for (Map.Entry<String,String> entry : cpext.entrySet()) {
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            String binaryOrigin = entry.getValue();
            if (binaryOrigin != null) {
                cp.append(project.getHelper().resolveFile(binaryOrigin));
            } else {
                cp.append(PropertyUtils.resolveFile(project.getModuleJarLocation().getParentFile(), entry.getKey()));
            }
        }
    }

    /**
     * Gives a map from test type (e.g. <em>unit</em> or <em>qa-functional</em>)
     * to the {@link TestClasspath test classpath} according to the content in
     * the project's metadata (<em>project.xml<em>).
     */
    private Map<String,TestClasspath> computeTestingClassPaths(ModuleList ml, PropertyEvaluator evaluator, Set<String> extraTestTypes) {
        Map<String, TestClasspath> classpaths = new HashMap<String,TestClasspath>();
        ProjectXMLManager pxm = new ProjectXMLManager(project);
        Map<String, Set<TestModuleDependency>> testDependencies = pxm.getTestDependencies(ml);
        
        String testDistDir =  evaluator.getProperty("test.dist.dir"); // NOI18N
        if (testDistDir == null) {
            NbModuleType type = project.getModuleType();
            if (type == NbModuleType.NETBEANS_ORG) {
                // test.dist.dir = ${nb_all}/nbbuild/build/testdist
                String nball = evaluator.getProperty("nb_all"); // NOI18N
                testDistDir = nball + File.separatorChar + "nbbuild" + File.separatorChar + "build" + File.separatorChar + "testdist"; // NOI18N
            } else if ( type == NbModuleType.SUITE_COMPONENT) {
                // test.dist.dir = ${suite.build.dir}/testdist
                String suiteDir = evaluator.getProperty("suite.build.dir"); // NOI18N
                testDistDir = suiteDir + File.separatorChar + "testdist"; // NOI18N
            } else {
                // standalone module
                // test.dist.dir = ${build.dir}/testdist
                String moduleDir = evaluator.getProperty("build.dir"); // NOI18N
                testDistDir = moduleDir + File.separatorChar + "testdist"; // NOI18N
            }
        }
        for (Map.Entry<String,Set<TestModuleDependency>> entry : testDependencies.entrySet()) {
            computeTestType(entry.getKey(), new File(testDistDir), entry.getValue(), classpaths, ml);
        }
        for (String testType : extraTestTypes) {
            if (!testDependencies.containsKey(testType)) {
                // No declared dependencies of this type, so will definitely need to add in compatibility libraries.
                computeTestType(testType, new File(testDistDir), Collections.<TestModuleDependency>emptySet(), classpaths, ml);
            }
        }
        return classpaths;
    }
    
    private void computeTestType(String ttName, File testDistDir, Set<TestModuleDependency> ttModules, Map<String,TestClasspath> classpaths, ModuleList ml) {
        final Set<String> compileCnbs = new HashSet<String>();
        final Set<String> runtimeCnbs = new HashSet<String>();
        final Set<String> testCompileCnbs = new HashSet<String>();
        final Set<String> testRuntimeCnbs = new HashSet<String>();
        Logger logger = Logger.getLogger(Evaluator.class.getName());
        // maps CNB->status: FALSE - added only to runtime CP, TRUE - added to compile CP too,
        // null - not processed (yet)
        Map<String, Boolean> processedRecursive = new HashMap<String, Boolean>();

        // #139339: optimization using processedRecursive set was too bold, removed
        for (TestModuleDependency td : ttModules) {
            String cnb = td.getModule().getCodeNameBase();
            logger.log(Level.FINE, "computeTestType: processing ''{0}''", cnb);
            if (td.isRecursive()) {
                // scan cp recursively
                Set<String> unprocessed = new HashSet<String>();

                final String codeNameBase = project.getCodeNameBase();
                unprocessed.add(td.getModule().getCodeNameBase());
                while (!unprocessed.isEmpty()) { // crude breadth-first search
                    Iterator<String> it = unprocessed.iterator();
                    String recursiveCNB = it.next();
                    it.remove();

                    // if we've put recursiveCNB module only to runtime CP and now should be also added to compile CP, process once more
                    Boolean alreadyInCompileCP = processedRecursive.get(recursiveCNB);
                    if (Boolean.TRUE.equals(alreadyInCompileCP)
                            || (Boolean.FALSE.equals(alreadyInCompileCP) && ! td.isCompile())) {
                        continue;
                    }

                    logger.log(Level.FINE, "computeTestType: processing ''{0}''", recursiveCNB);
                    ModuleEntry module = ml.getEntry(recursiveCNB);
                    if (module == null) {
                        Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + recursiveCNB + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                        continue;
                    }
                    if (!recursiveCNB.equals(codeNameBase)) { // build/classes for this is special
//                       XXX clb.callback(td, cnb);
                        runtimeCnbs.add(recursiveCNB);
                        if (td.isCompile()) {
                            compileCnbs.add(recursiveCNB);
                        }
                        processedRecursive.put(recursiveCNB, td.isCompile());
                    }
                    String[] newDeps = module.getRunDependencies();
                    unprocessed.addAll(Arrays.asList(newDeps));
                }
//             XXX   processTestEntryRecursive(td,
//                        new Callback() {
//                            public void callback(TestModuleDependency td, String cnb) {
//                            }
//                        }, ml);
            } else {
                runtimeCnbs.add(cnb);
                if (td.isCompile()) {
                    compileCnbs.add(cnb);
                }
            }
            if (td.isTest()) {
                if (td.isCompile()) {
                    testCompileCnbs.add(cnb);
                }
                testRuntimeCnbs.add(cnb);
            }
        }

        StringBuilder extra = new StringBuilder();
        TestClasspath testClasspath = new TestClasspath(
                mergePaths(compileCnbs,false,ttName,testDistDir, ml) + extra,
                mergePaths(runtimeCnbs,false,ttName,testDistDir,ml) + extra,
                mergePaths(testCompileCnbs,true,ttName,testDistDir,ml),
                mergePaths(testRuntimeCnbs,true,ttName,testDistDir,ml));

        classpaths.put(ttName,testClasspath);
    }

   private static final Set<String> warnedModules = Collections.synchronizedSet(new HashSet<String>());
    private String mergePaths(Set<String> cnbs, boolean test,String testtype,File testDistDir,ModuleList ml) {
        StringBuilder cps = new StringBuilder();
        for (String cnb : cnbs) {
                ModuleEntry module = ml.getEntry(cnb);
                if (module == null) {
                    if (warnedModules.add(cnb)) {
                        Logger.getLogger(Evaluator.class.getName()).log(Level.WARNING, "Cannot find test module dependency: {0}", cnb);
                    }
                    continue;
                }
                if (cps.length() > 0) {
                    cps.append(':');
                }
                if (test) {
                    // we need to get cluster name
                    File clusterDir = module.getClusterDirectory();
                    if (clusterDir != null) {
                        String clusterName = clusterDir.getName();
                        char s = File.separatorChar;
                        File jarFile = new File(
                                          testDistDir, testtype + s + clusterName + s + cnb.replace('.','-') + s + "tests.jar"); // NOI18N
                        cps.append(jarFile.getPath());
                        // See ParseProjectXml:
                        if (!testtype.equals("unit")) {
                            cps.append(':');
                            jarFile = new File(testDistDir, "unit" + s + clusterName + s + cnb.replace('.', '-') + s + "tests.jar"); // NOI18N
                            cps.append(jarFile.getPath());
                        }
                    }
                     
                } else {
                    cps.append(module.getJarLocation().getPath());
                    cps.append(module.getClassPathExtensions()); // #105621
                }
        }
        return cps.toString();
    }
   
}
