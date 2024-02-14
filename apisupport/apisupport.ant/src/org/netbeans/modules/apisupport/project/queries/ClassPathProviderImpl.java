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

package org.netbeans.modules.apisupport.project.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Evaluator;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Element;

public final class ClassPathProviderImpl implements ClassPathProvider {

    public static final String BOOTCLASSPATH_PREPEND = "bootclasspath.prepend";
    
    private final NbModuleProject project;
    
    public ClassPathProviderImpl(NbModuleProject project) {
        this.project = project;
    }

    private final Object cpLock = new Object();
    private volatile ClassPath boot;
    private volatile ClassPath source;
    private volatile ClassPath compile;
    private volatile ClassPath execute;
    private volatile ClassPath processor;
    private volatile ClassPath testSource;
    private volatile ClassPath testCompile;
    private volatile ClassPath testExecute;
    private volatile ClassPath testProcessor;
    private volatile ClassPath funcTestSource;
    private volatile ClassPath funcTestCompile;
    private volatile ClassPath funcTestExecute;
    private volatile ClassPath funcTestProcessor;
    private volatile Map<FileObject,ClassPath> extraCompilationUnitsCompile = null;
    private volatile Map<FileObject,ClassPath> extraCompilationUnitsExecute = null;

    private static final Logger LOG = Logger.getLogger(ClassPathProviderImpl.class.getName());

    @CheckForNull
    @Override
    public ClassPath findClassPath(
            @NonNull final FileObject file,
            @NonNull final String type) {
        if (type.equals(ClassPath.BOOT)) {
            ClassPath bcp = boot;
            if (bcp == null) {
                bcp = runGuarded(new Mutex.Action<ClassPath>() {
                    @Override
                    public ClassPath run() {
                        if (boot == null) {
                            ClassPathImplementation prependCP = createPathFromProperty(BOOTCLASSPATH_PREPEND);
                            final String loc = project.evaluator().getProperty(Evaluator.NBJDK_BOOTCLASSPATH_MODULAR);
                            if(loc != null) {
                                final File locf = new File(loc);
                                for (JavaPlatform jp : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                                    final File jpLocf = jp.getInstallFolders().stream()
                                            .map((fo) -> FileUtil.toFile(fo))
                                            .filter((f) -> f != null)
                                            .findFirst()
                                            .orElse(null);
                                    if (locf.equals(jpLocf)) {
                                        boot = ClassPathSupport.createProxyClassPath(
                                                ClassPathFactory.createClassPath(prependCP),
                                                jp.getBootstrapLibraries());
                                        break;
                                    }
                                }
                            } else {
                                boot = ClassPathFactory.createClassPath(ClassPathSupport.createProxyClassPathImplementation(
                                        prependCP,
                                        createPathFromProperty(Evaluator.NBJDK_BOOTCLASSPATH),
                                        createFxPath()));
                            }
                        }
                        return boot;
                    }
                });
            }
            return bcp;
        }
        final FileObject srcDir = project.getSourceDirectory();
        final FileObject testSrcDir = project.getTestSourceDirectory("unit");
        final FileObject funcTestSrcDir = project.getTestSourceDirectory("qa-functional");
        @NonNull File dir = project.getClassesDirectory();
        // #164282: workaround for not refreshed FS cache
        dir = FileUtil.normalizeFile(dir);
        FileObject classesDir = dir.exists() ? FileUtil.toFileObject(dir) : null;
        dir = project.getTestClassesDirectory("unit");
        dir = FileUtil.normalizeFile(dir);
        FileObject testClassesDir = dir.exists() ? FileUtil.toFileObject(dir) : null;
        File moduleJar;
        final URL generatedClasses = FileUtil.urlForArchiveOrDir(project.getGeneratedClassesDirectory());
        final URL generatedUnitTestClasses = FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("unit"));
        final URL generatedFunctionalTestClasses = FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("qa-functional"));
        String fileU = file.toURL().toString();
        if (srcDir != null && generatedClasses != null &&
                (FileUtil.isParentOf(srcDir, file) || file == srcDir || fileU.startsWith(generatedClasses.toString()))) {
            // Regular sources.
            if (type.equals(ClassPath.COMPILE)) {
                ClassPath ccp = compile;
                if (ccp == null) {
                    ccp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (compile == null) {
                                compile = ClassPathFactory.createClassPath(createCompileClasspath());
                                LOG.log(Level.FINE, "compile/execute-time classpath for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, compile});
                            }
                            return compile;
                        }
                    });
                }
                return ccp;
            } else if (type.equals(ClassPath.EXECUTE)) {
                ClassPath ecp = execute;
                if (ecp == null) {
                    ecp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (execute == null) {
                                execute = ClassPathFactory.createClassPath(createExecuteClasspath());
                            }
                            return execute;
                        }
                    });
                }
                return ecp;
            } else if (type.equals(ClassPath.SOURCE)) {
                ClassPath scp = source;
                if (scp == null) {
                    scp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (source == null) {
                                source = ClassPathSupport.createClassPath(srcDir.toURL(), generatedClasses);
                            }
                            return source;
                        }
                    });
                }
                return scp;
            } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                ClassPath pcp = processor;
                if (pcp == null) {
                    pcp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (processor == null) {
                                processor = ClassPathFactory.createClassPath(createProcessorPath());
                            }
                            return processor;
                        }
                    });
                }
                return pcp;
            }
        } else if (testSrcDir != null && generatedUnitTestClasses != null &&
                (FileUtil.isParentOf(testSrcDir, file) || file == testSrcDir || fileU.startsWith(generatedUnitTestClasses.toString()))) {
            // Unit tests.
            // XXX refactor to use project.supportedTestTypes
            if (type.equals(ClassPath.COMPILE)) {
                ClassPath tcp = testCompile;
                if (tcp == null) {
                    tcp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (testCompile == null) {
                                testCompile = ClassPathFactory.createClassPath(createTestCompileClasspath("unit"));
                                LOG.log(Level.FINE, "compile-time classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testCompile});
                            }
                            return testCompile;
                        }
                    });
                }
                return tcp;
            } else if (type.equals(ClassPath.EXECUTE)) {
                ClassPath te = testExecute;
                if (te == null) {
                    te = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (testExecute == null) {
                                testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("unit"));
                                LOG.log(Level.FINE, "runtime classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testExecute});
                            }
                            return testExecute;
                        }
                    });
                }
                return te;
            } else if (type.equals(ClassPath.SOURCE)) {
                ClassPath tscp = testSource;
                if (tscp == null) {
                    tscp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (testSource == null) {
                                testSource = ClassPathSupport.createClassPath(testSrcDir.toURL(), generatedUnitTestClasses);
                            }
                            return testSource;
                        }
                    });
                }
                return tscp;
            } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                ClassPath tp = testProcessor;
                if (tp == null) {
                    tp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (testProcessor == null) {
                                testProcessor = ClassPathFactory.createClassPath(createTestProcessorPath("unit"));  //NOI18N
                            }
                            return testProcessor;
                        }
                    });
                }
                return tp;
            }
        } else if (funcTestSrcDir != null && generatedFunctionalTestClasses != null &&
                (FileUtil.isParentOf(funcTestSrcDir, file) || file == funcTestSrcDir || fileU.startsWith(generatedFunctionalTestClasses.toString()))) {
            // Functional tests.
            if (type.equals(ClassPath.SOURCE)) {
                ClassPath fts = funcTestSource;
                if (fts == null) {
                    fts = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (funcTestSource == null) {
                                funcTestSource = ClassPathSupport.createClassPath(funcTestSrcDir.toURL(), generatedFunctionalTestClasses);
                            }
                            return funcTestSource;
                        }
                    });
                }
                return fts;
            } else if (type.equals(ClassPath.COMPILE)) {
                // See #42331.
                ClassPath ftc = funcTestCompile;
                if (ftc == null) {
                    ftc = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (funcTestCompile == null) {
                                funcTestCompile = ClassPathFactory.createClassPath(createTestCompileClasspath("qa-functional"));
                                LOG.log(Level.FINE, "compile-time classpath for func tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, funcTestCompile});
                            }
                            return funcTestCompile;
                        }
                    });
                }
                return ftc;
            } else if (type.equals(ClassPath.EXECUTE)) {
                ClassPath fte = funcTestExecute;
                if (fte == null) {
                    fte = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (funcTestExecute == null) {
                                funcTestExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("qa-functional"));
                            }
                            return funcTestExecute;
                        }
                    });
                }
                return fte;
            } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                ClassPath ftp = funcTestProcessor;
                if (ftp == null) {
                    ftp = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (funcTestProcessor == null) {
                                funcTestProcessor = ClassPathFactory.createClassPath(createTestProcessorPath("qa-functional"));  //NOI18N
                            }
                            return funcTestProcessor;
                        }
                    });
                }
                return ftp;
            }
        } else if (classesDir != null && (classesDir.equals(file) || FileUtil.isParentOf(classesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                roots.add ( ClassPathSupport.createResource(classesDir.toURL()));
                roots.addAll(createCompileClasspath().getResources());
                return ClassPathSupport.createClassPath (roots);
            }
        } else if (testClassesDir != null && (testClassesDir.equals(file) || FileUtil.isParentOf(testClassesDir,file))) {
            if (ClassPath.EXECUTE.equals(type)) {
                ClassPath te = testExecute;
                if (te == null) {
                    te = runGuarded(new Mutex.Action<ClassPath>() {
                        @Override
                        public ClassPath run() {
                            if (testExecute == null) {
                                testExecute = ClassPathFactory.createClassPath(createTestExecuteClasspath("unit"));
                                LOG.log(Level.FINE, "runtime classpath for tests for file ''{0}'' (prj: {1}): {2}", new Object[] {file.getPath(), project, testExecute});
                            }
                            return testExecute;
                        }
                    });
                }
                return te;
            }
        } else if (FileUtil.getArchiveFile(file) != null &&
                FileUtil.toFile(FileUtil.getArchiveFile(file)).equals(moduleJar = project.getModuleJarLocation())) {
            if (ClassPath.EXECUTE.equals(type)) {
                List<PathResourceImplementation> roots = new ArrayList<PathResourceImplementation>();
                roots.add(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(moduleJar)));
                roots.addAll(createCompileClasspath().getResources());
                return ClassPathSupport.createClassPath (roots);
            }
        }
        else {
            calculateExtraCompilationUnits();
            for (Map.Entry<FileObject,ClassPath> entry : extraCompilationUnitsCompile.entrySet()) {
                FileObject pkgroot = entry.getKey();
                if (FileUtil.isParentOf(pkgroot, file) || file == pkgroot) {
                    if (type.equals(ClassPath.COMPILE)) {
                        return entry.getValue();
                    } else if (type.equals(ClassPath.EXECUTE)) {
                        return extraCompilationUnitsExecute.get(pkgroot);
                    } else if (type.equals(ClassPath.SOURCE)) {
                        // XXX should these be cached?
                        return ClassPathSupport.createClassPath(new FileObject[] {pkgroot});
                    } else {
                        break;
                    }
                }
            }
        }
        if (type.equals(ClassPath.SOURCE)) {
            Map<String,String> properties = project.evaluator().getProperties();
            if (properties != null) {
                for (Map.Entry<String,String> entry : properties.entrySet()) {
                    if (entry.getKey().startsWith(NbModuleProject.SOURCE_START)) {
                        FileObject sourceReference = project.getHelper().resolveFileObject(entry.getValue());
                        if (sourceReference != null) {
                            if (FileUtil.isArchiveFile(sourceReference)) {
                                sourceReference = FileUtil.getArchiveRoot(sourceReference);
                            }
                            if (sourceReference != null && (sourceReference == file || FileUtil.isParentOf(sourceReference, file))) {
                                return ClassPathSupport.createClassPath(new FileObject[] {sourceReference});
                            }
                        }
                    }
                }
            }
        }
        // Something not supported.
        return null;
    }
    
    private ClassPathImplementation createPathFromProperty(String prop) {
        return ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
            project.getProjectDirectoryFile(), project.evaluator(), new String[] {prop});
    }

    @NonNull
    private ClassPathImplementation createFxPath() {
        return new FxPathImpl(project);
    }
    
    /** &lt;compile-dependency&gt; is what we care about. */
    private ClassPathImplementation createCompileClasspath() {
        return createPathFromProperty(Evaluator.CP);
    }
    
    private void addPathFromProjectEvaluated(List<PathResourceImplementation> entries, String path) {
        if (path != null) {
            for (String piece : PropertyUtils.tokenizePath(path)) {
                URL url = FileUtil.urlForArchiveOrDir(project.getHelper().resolveFile(piece));
                if (url != null) { // #135292
                    entries.add(ClassPathSupport.createResource(url));
                }
            }
        }
    }
    
    private ClassPathImplementation createTestCompileClasspath(String testType) {
        return createPathFromProperty("test." + testType + ".cp"); // NOI18N
    }

    private ClassPathImplementation createTestExecuteClasspath(String testType) {
        return createPathFromProperty("test." + testType + ".run.cp"); // NOI18N
    }

    @NonNull
    private ClassPathImplementation createTestProcessorPath(@NonNull final String testType) {
        return new FilteredClassPathImplementation(
                createTestExecuteClasspath(testType),
                project.getHelper(),
                project.evaluator(),
                MessageFormat.format("build.test.{0}.classes.dir", testType));    //NOI18N
    }
    
    private ClassPathImplementation createExecuteClasspath() {
        return createPathFromProperty(Evaluator.RUN_CP);
    }

    @NonNull
    private ClassPathImplementation createProcessorPath() {
        return new FilteredClassPathImplementation(
                createExecuteClasspath(),
                project.getHelper(),
                project.evaluator(),
                "build.classes.dir");   //NOI18N
    }
    
    private void calculateExtraCompilationUnits() {
        if (extraCompilationUnitsCompile != null) {
            return;
        }
        final Map<FileObject,ClassPath> _extraCompilationUnitsCompile = new HashMap<FileObject,ClassPath>();
        final Map<FileObject,ClassPath> _extraCompilationUnitsExecute = new HashMap<FileObject,ClassPath>();
        for (Map.Entry<FileObject,Element> entry : project.getExtraCompilationUnits().entrySet()) {
            final FileObject pkgroot = entry.getKey();
            Element pkgrootEl = entry.getValue();
            Element classpathEl = XMLUtil.findElement(pkgrootEl, "classpath", NbModuleProject.NAMESPACE_SHARED); // NOI18N
            assert classpathEl != null : "no <classpath> in " + pkgrootEl;
            final String classpathS = XMLUtil.findText(classpathEl);
            if (classpathS == null) {
                _extraCompilationUnitsCompile.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
                _extraCompilationUnitsExecute.put(pkgroot, ClassPathSupport.createClassPath(new URL[0]));
            } else {
                class CPI implements ClassPathImplementation, PropertyChangeListener, AntProjectListener {
                    final Set<String> relevantProperties = new HashSet<String>();
                    final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
                    String cpS = classpathS;
                    @SuppressWarnings("LeakingThisInConstructor")
                    CPI() {
                        project.evaluator().addPropertyChangeListener(WeakListeners.propertyChange(this, project.evaluator()));
                        project.getHelper().addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, project.getHelper()));
                        Matcher m = Pattern.compile("\\$\\{([^{}]+)\\}").matcher(cpS);
                        while (m.find()) {
                            relevantProperties.add(m.group(1));
                        }
                    }
                    public @Override List<? extends PathResourceImplementation> getResources() {
                        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
                        addPathFromProjectEvaluated(resources, project.evaluator().evaluate(cpS));
                        return resources;
                    }
                    public @Override void addPropertyChangeListener(PropertyChangeListener listener) {
                        pcs.addPropertyChangeListener(listener);
                    }
                    public @Override void removePropertyChangeListener(PropertyChangeListener listener) {
                        pcs.removePropertyChangeListener(listener);
                    }
                    public @Override void propertyChange(PropertyChangeEvent evt) {
                        if (relevantProperties.contains(evt.getPropertyName())) {
                            pcs.firePropertyChange(PROP_RESOURCES, null, null);
                        }
                    }
                    public @Override void configurationXmlChanged(AntProjectEvent ev) {
                        Element pkgrootEl = project.getExtraCompilationUnits().get(pkgroot);
                        Element classpathEl = XMLUtil.findElement(pkgrootEl, "classpath", NbModuleProject.NAMESPACE_SHARED); // NOI18N
                        assert classpathEl != null : "no <classpath> in " + pkgrootEl;
                        cpS = XMLUtil.findText(classpathEl);
                        pcs.firePropertyChange(PROP_RESOURCES, null, null);
                    }
                    public @Override void propertiesChanged(AntProjectEvent ev) {}
                }
                ClassPathImplementation ecuCompile = new CPI();
                _extraCompilationUnitsCompile.put(pkgroot, ClassPathFactory.createClassPath(ecuCompile));
                // Add <built-to> dirs and JARs for ClassPath.EXECUTE.
                List<PathResourceImplementation> extraEntries = new ArrayList<PathResourceImplementation>();
                for (Element kid : XMLUtil.findSubElements(pkgrootEl)) {
                    if (!kid.getLocalName().equals("built-to")) { // NOI18N
                        continue;
                    }
                    String rawtext = XMLUtil.findText(kid);
                    assert rawtext != null : "Null content for <built-to> in " + project;
                    String text = project.evaluator().evaluate(rawtext);
                    if (text == null) {
                        continue;
                    }
                    addPathFromProjectEvaluated(extraEntries, text);
                }
                _extraCompilationUnitsExecute.put(pkgroot, ClassPathFactory.createClassPath(
                        ClassPathSupport.createProxyClassPathImplementation(ecuCompile, ClassPathSupport.createClassPathImplementation(extraEntries))));
            }
        }
        extraCompilationUnitsExecute = Collections.unmodifiableMap(_extraCompilationUnitsExecute);
        extraCompilationUnitsCompile = Collections.unmodifiableMap(_extraCompilationUnitsCompile);
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                return new ClassPath[] {findClassPath(srcDir, ClassPath.BOOT)};
            }
        }
        List<ClassPath> paths = new ArrayList<ClassPath>(3);
        if (ClassPath.COMPILE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.COMPILE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.COMPILE));
                }
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsCompile.values());
        } else if (ClassPath.EXECUTE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.EXECUTE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.EXECUTE));
                }
            }
            calculateExtraCompilationUnits();
            paths.addAll(extraCompilationUnitsExecute.values());
        } else if (ClassPath.SOURCE.equals(type)) {
            FileObject srcDir = project.getSourceDirectory();
            if (srcDir != null) {
                paths.add(findClassPath(srcDir, ClassPath.SOURCE));
            }
            for (String testType : project.supportedTestTypes()) {
                FileObject testSrcDir = project.getTestSourceDirectory(testType);
                if (testSrcDir != null) {
                    paths.add(findClassPath(testSrcDir, ClassPath.SOURCE));
                }
            }
            calculateExtraCompilationUnits();
            for (FileObject root : extraCompilationUnitsCompile.keySet()) {
                paths.add(ClassPathSupport.createClassPath(new FileObject[] {root}));
            }
        }
        return paths.toArray(new ClassPath[0]);
    }

    private static final class FilteredClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

        private final ClassPathImplementation delegate;
        private final AntProjectHelper helper;
        private final PropertyEvaluator eval;
        private final String filteredProp;
        private final AtomicReference<List<PathResourceImplementation>> cache;
        private final PropertyChangeSupport listeners;

        FilteredClassPathImplementation(
            @NonNull final ClassPathImplementation delegate,
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String filteredProp) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            Parameters.notNull("helper", helper);       //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("filteredProp", filteredProp);   //NOI18N
            this.delegate = delegate;
            this.helper = helper;
            this.eval = eval;
            this.filteredProp = filteredProp;
            this.cache = new AtomicReference<List<PathResourceImplementation>>();
            this.listeners = new PropertyChangeSupport(this);
            this.delegate.addPropertyChangeListener(WeakListeners.propertyChange(this, this.delegate));
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = cache.get();
            if (res != null) {
                return res;
            }
            final String propVal = eval.getProperty(filteredProp);
            final File propFile = propVal == null ? null : helper.resolveFile(propVal);
            URL propURL = null;
            try {
                if (propFile != null) {
                    propURL = Utilities.toURI(propFile).toURL();
                }
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
            }
            final List<? extends PathResourceImplementation> resources = delegate.getResources();
            res = new ArrayList<PathResourceImplementation>(resources.size());
next:       for (PathResourceImplementation pri : resources) {
                if (propURL != null) {
                    final URL[] roots = pri.getRoots();
                    for (URL root : roots) {
                        if (propURL.equals(root) && roots.length == 1) {
                            continue next;
                        }
                    }
                }
                res.add(pri);
            }
            res = Collections.unmodifiableList(res);
            if (!cache.compareAndSet(null, res)) {
                final List<PathResourceImplementation> cur = cache.get();
                if (cur != null) {
                    res = cur;
                }
            }
            return res;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent event) {
            final String propName = event.getPropertyName();
            if (propName == null ||
                PROP_RESOURCES.equals(propName) ||
                filteredProp.equals(propName)) {
                reset();
            }
        }

        private void reset() {
            cache.set(null);
            listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }

    }

    private static final class FxPathImpl implements ClassPathImplementation, PropertyChangeListener {

        private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
        private final NbModuleProject project;
        private volatile List<? extends PathResourceImplementation> jfx;

        FxPathImpl(@NonNull final NbModuleProject project) {
            Parameters.notNull("project", project); //NOI18N
            this.project = project;
            this.project.evaluator().addPropertyChangeListener(this);
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<? extends PathResourceImplementation> res = jfx;
            if (res == null) {
                PathResourceImplementation pr = null;
                try {
                    for(ModuleEntry moduleEntryIter : project.getModuleList().getAllEntries()) {
                        if (moduleEntryIter.getCodeNameBase().equals("org.netbeans.libs.javafx")) {
                            File jre = new File(System.getProperty("java.home")); // NOI18N
                            File jdk8 = new File(new File(new File(jre, "lib"), "ext"), "jfxrt.jar"); // NOI18N
                            if (!jdk8.exists()) {
                                File jdk7 = new File(new File(jre, "lib"), "jfxrt.jar"); // NOI18N
                                if (jdk7.exists()) {
                                    // jdk7 add the classes on bootclasspath
                                    if (FileUtil.isArchiveFile(FileUtil.toFileObject(jdk7))) {
                                        pr = ClassPathSupport.createResource(FileUtil.getArchiveRoot(Utilities.toURI(jdk7).toURL()));
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException ioe) {
                    LOG.log(Level.INFO, null, ioe);
                }
                res = jfx = pr == null ?
                    Collections.<PathResourceImplementation>emptyList():
                    Collections.<PathResourceImplementation>singletonList(pr);
            }
            assert res != null;
            return res;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            this.jfx = null;
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }

    private <T> T runGuarded(@NonNull final Mutex.Action<T> action) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<T>(){
            @Override
            public T run() {
                synchronized(cpLock) {
                    return action.run();
                }
            }
        });
    }
}
