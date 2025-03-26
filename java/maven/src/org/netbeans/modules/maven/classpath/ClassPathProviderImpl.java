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

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupportFactory;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

import static org.netbeans.spi.java.classpath.support.ClassPathSupport.Selector.PROP_ACTIVE_CLASS_PATH;

/**
 * Defines class path for maven2 projects..
 *
 * @author  Milos Kleint 
 */
@ProjectServiceProvider(service={ClassPathProvider.class, ActiveJ2SEPlatformProvider.class, ProjectSourcesClassPathProvider.class}, projectType="org-netbeans-modules-maven")
public final class ClassPathProviderImpl implements ClassPathProvider, ActiveJ2SEPlatformProvider, ProjectSourcesClassPathProvider {

    private static final Logger LOGGER = Logger.getLogger(ClassPathProviderImpl.class.getName());
    
    public static final String MODULE_INFO_JAVA = "module-info.java"; // NOI18N
    
    private static final int TYPE_SRC = 0;
    private static final int TYPE_TESTSRC = 1;
    private static final int TYPE_WEB = 5;
    private static final int TYPE_UNKNOWN = -1;
    
    private final @NonNull Project proj;
    
    private static final int SOURCE_PATH = 0;                   // TEST_SOURCE_PATH = 1 
    private static final int COMPILE_TIME_PATH = 2;             // TEST_COMPILE_TIME_PATH = 3
    private static final int RUNTIME_PATH = 4;                  // TEST_RUNTIME_PATH = 5
    private static final int BOOT_PATH = 6;                     // TEST_BOOT_PATH = 7
    private static final int ENDORSED_PATH = 8;
    private static final int MODULE_BOOT_PATH = 9;
    private static final int MODULE_COMPILE_PATH = 10;          // TEST_MODULE_COMPILE_PATH = 11
    private static final int MODULE_LEGACY_PATH = 12;           // TEST_MODULE_LEGACY_PATH = 13
    
    private static final int MODULE_EXECUTE_PATH = 14;          // TEST_MODULE_EXECUTE_PATH = 15
    private static final int MODULE_EXECUTE_CLASS_PATH = 16;    // TEST_MODULE_EXECUTE_CLASS_PATH = 17
    
    private static final int JAVA8_COMPILE_PATH = 18;
    private static final int JAVA8_TEST_COMPILE_PATH = 19;
    private static final int JAVA8_TEST_SCOPED_COMPILE_PATH = 20;
    private static final int JAVA8_RUNTIME_PATH = 21;           // JAVA8_TEST_RUNTIME_PATH = 22
    private static final int JAVA8_TEST_SCOPED_RUNTIME_PATH = 23;
    
    private static final int ANNOTATION_PROC_PATH = 24;         // TEST_ANNOTATION_PROC_PATH = 25
    
    private final ClassPath[] cache = new ClassPath[26];
    
    private BootClassPathImpl bcpImpl;
    private EndorsedClassPathImpl ecpImpl;
    
    public ClassPathProviderImpl(@NonNull Project proj) {
        this.proj = proj;
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    @Override public ClassPath[] getProjectClassPaths(String type) {
        return ProjectManager.mutex().readAccess((Mutex.Action<ClassPath[]>) () -> {
            if (null != type) switch (type) {
                case ClassPath.BOOT:
                    return new ClassPath[] { getBootClassPath(TYPE_SRC), getBootClassPath(TYPE_TESTSRC) };
                case ClassPathSupport.ENDORSED:
                    return new ClassPath[] { getEndorsedClassPath() };
                case ClassPath.COMPILE:
                    return new ClassPath[] { getCompileTimeClasspath(TYPE_SRC), getCompileTimeClasspath(TYPE_TESTSRC) };
                case ClassPath.EXECUTE:
                    return new ClassPath[] { getRuntimeClasspath(TYPE_SRC), getRuntimeClasspath(TYPE_TESTSRC) };
                case ClassPath.SOURCE:
                    return new ClassPath[] { getSourcepath(TYPE_SRC), getSourcepath(TYPE_TESTSRC) };
                case JavaClassPathConstants.MODULE_BOOT_PATH:
                    return new ClassPath[] { getModuleBootPath() };
                case JavaClassPathConstants.MODULE_COMPILE_PATH:
                    return new ClassPath[] { getModuleCompilePath(TYPE_SRC), getModuleCompilePath(TYPE_TESTSRC) };
                case JavaClassPathConstants.MODULE_CLASS_PATH:
                    return new ClassPath[] { getModuleLegacyClassPath(TYPE_SRC), getModuleLegacyClassPath(TYPE_TESTSRC) };
            }
            return new ClassPath[0];
        });
    }
    
    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    @Override public ClassPath getProjectSourcesClassPath(String type) {
        switch (type) {
            case ClassPath.BOOT: return getBootClassPath(TYPE_SRC);
            case ClassPath.COMPILE: return getCompileTimeClasspath(TYPE_SRC);
            case ClassPath.EXECUTE: return getRuntimeClasspath(TYPE_SRC);
            case ClassPath.SOURCE: return getSourcepath(TYPE_SRC);
            case ClassPathSupport.ENDORSED: return getEndorsedClassPath();
            case JavaClassPathConstants.MODULE_BOOT_PATH: return getModuleBootPath();
            case JavaClassPathConstants.MODULE_COMPILE_PATH: return getModuleCompilePath(TYPE_SRC);
            case JavaClassPathConstants.MODULE_CLASS_PATH: return getModuleLegacyClassPath(TYPE_SRC);
            case JavaClassPathConstants.MODULE_EXECUTE_PATH: return getModuleExecutePath(TYPE_SRC);
            case JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH: return getModuleLegacyRuntimeClassPath(TYPE_SRC);
        }
        return null;
    }
    
    @Override public ClassPath findClassPath(FileObject file, String type) {
        assert file != null;
        if(file == null) {            
            LOGGER.log(Level.WARNING, " passed null fileobject fo ClassPathProviderImpl.findClassPath."); //NOI18N
            return null;
        }
        int fileType = getType(file);
        if (fileType != TYPE_SRC &&  fileType != TYPE_TESTSRC && fileType != TYPE_WEB) {
            LOGGER.log(Level.FINEST, " bad type={0} for {1}", new Object[] {type, file}); //NOI18N
            return null;
        }
        switch (type) {
            case ClassPath.BOOT: return getBootClassPath(fileType);
            case ClassPath.COMPILE: return getCompileTimeClasspath(fileType);
            case ClassPath.EXECUTE: return getRuntimeClasspath(fileType);
            case ClassPath.SOURCE: return getSourcepath(fileType);
            case ClassPathSupport.ENDORSED: return getEndorsedClassPath();
            case JavaClassPathConstants.PROCESSOR_PATH: return getAnnotationProcClassPath(fileType);
            case JavaClassPathConstants.MODULE_BOOT_PATH: return getModuleBootPath();
            case JavaClassPathConstants.MODULE_COMPILE_PATH: return getModuleCompilePath(fileType);
            case JavaClassPathConstants.MODULE_CLASS_PATH: return getModuleLegacyClassPath(fileType);
            case JavaClassPathConstants.MODULE_EXECUTE_PATH: return getModuleExecutePath(fileType);
            case JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH: return getModuleLegacyRuntimeClassPath(fileType);
            default: return null;
        }
    }

    @Override public @NonNull JavaPlatform getJavaPlatform() {
        return getBootClassPathImpl().findActivePlatform();
    }
    
    private boolean isChildOf(FileObject child, URI[] uris) {
        for (int i = 0; i < uris.length; i++) {
            FileObject fo = FileUtilities.convertURItoFileObject(uris[i]);
            if (fo != null  && fo.isFolder() && (fo.equals(child) || FileUtil.isParentOf(fo, child))) {
                return true;
            }
        }
        return false;
    }
    
    private int getType(FileObject file) {
        if(file == null) {
            return TYPE_UNKNOWN;
        }
        NbMavenProjectImpl project = getNBMavenProject();
        if (isChildOf(file, project.getSourceRoots(false)) ||
            isChildOf(file, project.getGeneratedSourceRoots(false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, project.getSourceRoots(true)) ||
            isChildOf(file, project.getGeneratedSourceRoots(true))) {
            return TYPE_TESTSRC;
        }
        
        URI web = project.getWebAppDirectory();
        FileObject fo = FileUtil.toFileObject(Utilities.toFile(web));
        if (fo != null && (fo.equals(file) || FileUtil.isParentOf(fo, file))) {
            return TYPE_WEB;
        }
        
        //MEVENIDE-613, #125603 need to check later than the actual java sources..
        // sometimes the root of resources is the basedir for example that screws up 
        // test sources.
        if (isChildOf(file, project.getResources(false))) {
            return TYPE_SRC;
        }
        if (isChildOf(file, project.getResources(true))) {
            return TYPE_TESTSRC;
        }
        return TYPE_UNKNOWN;
    }
    
    private ClassPath getSourcepath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(
                SOURCE_PATH + ftype, 
                () -> ClassPathFactory.createClassPath(ftype == TYPE_SRC ? new SourceClassPathImpl(getNBMavenProject()) : new TestSourceClassPathImpl(getNBMavenProject())));
    }
    
    private ClassPath getAnnotationProcClassPath(int type) {
        final int ftype;
        switch (type) {
            case TYPE_WEB: 
            default:
                ftype = TYPE_SRC;
                break;
            case TYPE_SRC:
            case TYPE_TESTSRC:
                ftype = type;
                break;
        }
        int index = ANNOTATION_PROC_PATH + ftype;
        return computeIfAbsent(
                index,
                () -> {
                    ClassPath anno = ClassPathFactory.createClassPath(new AnnotationProcClassPathImpl(getNBMavenProject(), ftype == TYPE_SRC));
                    return createMultiplexClassPath(
                            new AnnotationPathSelector(
                                    getNBMavenProject(), anno, 
                                    () -> getCompileTimeClasspath(type)
                            )
                    );
                }
                        
        );
    }
    
    private ClassPath getCompileTimeClasspath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(
                COMPILE_TIME_PATH + ftype, 
                () -> createModuleInfoSelector(
                        // if there is a main module-info
                        () -> createModuleInfoBasedPath(
                                getModuleCompilePath(ftype), // base
                                getSourcepath(ftype),        // source 
                                getModuleBootPath(),         // system modules
                                getModuleCompilePath(ftype), // usermodules                                
                                ftype == TYPE_SRC ?          // legacy   
                                    getJava8CompileClasspath() :
                                    createTestClassPathSelector(() -> getJava8TestCompileClasspath(), () -> getTestScopedCompileClasspath(), "TestsCompileTimeLegacyClasspath"),
                                null),
                        // if there is no module-info: 
                        // note that the maven compile plugin (3.5.1) does not allow 
                        // a test module-info while there is no main module info
                        () -> ftype == TYPE_SRC ? getJava8CompileClasspath() : getJava8TestCompileClasspath(),
                        "CompileTimeClasspath")); // NOI18N           
    }        
    
    private ClassPath getRuntimeClasspath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(RUNTIME_PATH + ftype, 
            () -> createModuleInfoSelector(
                    // if there is a main module-info
                    () -> createModuleInfoBasedPath(
                            getJava8RunTimeClassPath(ftype), // base
                            getSourcepath(ftype),            // source
                            getModuleBootPath(),             // system modules
                            getJava8RunTimeClassPath(ftype), // user modules
                            ftype == TYPE_SRC ?              // legacy
                                getJava8RunTimeClassPath(TYPE_SRC) :
                                createTestClassPathSelector(() -> getJava8RunTimeClassPath(TYPE_TESTSRC), () -> getTestScopedRuntimeClasspath(), "TestsRuntimeLegacyClasspath"),                         
                            null),
                    // if there is no module-info
                    () -> getJava8RunTimeClassPath(ftype),
                    "RuntimeClasspath"));
    }
    
    private ClassPath getJava8RunTimeClassPath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(JAVA8_RUNTIME_PATH + ftype,
                () -> ClassPathFactory.createClassPath(
                        ftype == TYPE_SRC
                            ? new RuntimeClassPathImpl(getNBMavenProject())
                            : new TestRuntimeClassPathImpl(getNBMavenProject(), false)
                )
        );
    }
    
    private ClassPath getTestScopedRuntimeClasspath() {
        return computeIfAbsent(JAVA8_TEST_SCOPED_RUNTIME_PATH, () -> ClassPathFactory.createClassPath(new TestRuntimeClassPathImpl(getNBMavenProject(), true)));
    }
    
    private ClassPath getBootClassPath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(BOOT_PATH + ftype,
            () -> createModuleInfoSelector(
                    () -> createModuleInfoBasedPath(
                        getModuleBootPath(),                // base
                        getSourcepath(ftype),               // source
                        getModuleBootPath(),                // system modules
                        getModuleCompilePath(ftype),        // user modules
                        null,                               // legacy
                        null),
                    () -> ClassPathFactory.createClassPath(getBootClassPathImpl()),
                    "BootClasspath")); // NOI18N
    }
    
    private BootClassPathImpl getBootClassPathImpl() {
        if (bcpImpl == null) {
            bcpImpl = new BootClassPathImpl(getNBMavenProject(), getEndorsedClassPathImpl());
        }
        return bcpImpl;
    }

    /*
     WARNING: getCompileClasspath, getTestCompileClasspath, getTestScopedCompileClasspath
    
     the classpathElements in maven-compiler-plugin always were:
     - all artifacts from the project with the scope - COMPILE, PROFILE and SYSTEM
     - the path given by project.build.getOutputDirectory

     until jdk9 jigsaw: 
     CompileClassPathImpl provided only the artifacts with the respective scope,
     but NOT the project.build.getOutputDirectory.
     since jdk9 jigsaw (and therefore maven-compiler-plugin 2.6): 
     it is necessary to provide also project.build.getOutputDirectory 
     (as that is the dir where maven copies the dependand jar/modules?)

     The question at this point is if we now should do so for all compiler versions 
     (and also for < 2.6) and jdk-s < 9 or if we should differ between m-c-p < 2.6 and >=2.6 
     and jdk version respectively.
    */
            
    /*
     * see WARNING above.    
     */
    private ClassPath getJava8CompileClasspath() {
        return computeIfAbsent(JAVA8_COMPILE_PATH, () -> ClassPathFactory.createClassPath(new CompileClassPathImpl(getNBMavenProject(), true)));
    }

    /*
     * see WARNING above.    
     */
    private ClassPath getJava8TestCompileClasspath() {
        return computeIfAbsent(JAVA8_TEST_COMPILE_PATH, () -> ClassPathFactory.createClassPath(new TestCompileClassPathImpl(getNBMavenProject(), true)));
    }
    
    /*
     * see WARNING above.    
    */
    private ClassPath getTestScopedCompileClasspath() {
        return computeIfAbsent(JAVA8_TEST_SCOPED_COMPILE_PATH, () -> ClassPathFactory.createClassPath(new TestCompileClassPathImpl(getNBMavenProject(), true, true)));
    }

    private EndorsedClassPathImpl getEndorsedClassPathImpl() {
        if (ecpImpl == null) {
            ecpImpl = new EndorsedClassPathImpl(getNBMavenProject());
        }
        return ecpImpl;
    }

    private ClassPath getEndorsedClassPath() {
        return computeIfAbsent(ENDORSED_PATH, () -> {
            getBootClassPathImpl();
            return ClassPathFactory.createClassPath(getEndorsedClassPathImpl());
        });
    }
    
    private ClassPath getModuleBootPath() {
        return computeIfAbsent(MODULE_BOOT_PATH, () -> createModuleInfoSelector(() -> createPlatformModulesPath(), () -> createPlatformModulesPath(), "ModuleBootPath")); // NOI18N
    }

    private ClassPath getModuleCompilePath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type; 
        return computeIfAbsent(
                MODULE_COMPILE_PATH + ftype, 
                () -> ftype == TYPE_SRC ?
                    // XXX <= jdk8
                    // jdk9 has module-info
                    // jdk9 has no module-info but is SL9
                    createModuleInfoSelector(() -> getJava8CompileClasspath(), () -> ClassPath.EMPTY, "ModuleCompilePath") : // NOI18N
                    createTestModulePathSelector(() -> getJava8CompileClasspath(), () -> getJava8TestCompileClasspath(), "TestModuleCompilePath")); // NOI18N 
    }
        
    private ClassPath getModuleExecutePath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        return computeIfAbsent(
                MODULE_EXECUTE_PATH + ftype,
                () -> ftype == TYPE_SRC ?
                    // XXX <= jdk8
                    // jdk9 has module-info
                    // jdk9 has no module-info but is SL9
                    createModuleInfoSelector(() -> getJava8RunTimeClassPath(TYPE_SRC), () -> ClassPath.EMPTY, "ModuleExecutePath") : // NOI18N
                    createTestModulePathSelector(() -> getJava8RunTimeClassPath(TYPE_SRC), () -> getJava8RunTimeClassPath(TYPE_TESTSRC), "TestModuleExecutePath"));
    }

    @NonNull
    private ClassPath getModuleLegacyClassPath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;        
        assert ftype >=0 && ftype <=1;
        return computeIfAbsent(
                MODULE_LEGACY_PATH + ftype,
                () -> ftype == TYPE_SRC ?
                    createModuleInfoSelector(() -> ClassPath.EMPTY, () -> getJava8CompileClasspath(), "ModuleLegacyClassPath") : // NOI18N
                    createModuleInfoSelector(() -> getTestScopedCompileClasspath(), () -> getJava8TestCompileClasspath(), "TestModuleLegacyClassPath")); // NOI18N
    }

    @NonNull
    private ClassPath getModuleLegacyRuntimeClassPath(int type) {
        final int ftype = type == TYPE_WEB ? TYPE_SRC : type;
        assert ftype >=0 && ftype <=1;
        return computeIfAbsent(
                MODULE_EXECUTE_CLASS_PATH + ftype,
                () -> ftype == TYPE_SRC ?
                        createModuleInfoSelector(() -> ClassPath.EMPTY, () -> getJava8RunTimeClassPath(TYPE_SRC), "ModuleLegacyRuntimeClassPath") : // NOI18N
                        createModuleInfoSelector(() -> getTestScopedRuntimeClasspath(), () -> getJava8RunTimeClassPath(TYPE_TESTSRC), "TestModuleLegacyRuntimeClassPath")); // NOI18N
    }
        
    private ClassPath computeIfAbsent(final int cacheIndex, final Supplier<ClassPath> provider) {
        synchronized (this) {
            ClassPath cp = cache[cacheIndex];
            if (cp != null) {
                return cp;
            }
        }
        return ProjectManager.mutex().readAccess(()-> {
            synchronized(this) {
                ClassPath cp = cache[cacheIndex];
                if (cp == null) {
                    cp = provider.get();
                    cache[cacheIndex] = cp;
                }
                return cp;
            }
        });
    }

    private ClassPath createPlatformModulesPath() {
        return ClassPathFactory.createClassPath(new PlatformModulesPathImpl(getNBMavenProject()));
    }
    
    private ClassPath createModuleInfoBasedPath(ClassPath base, ClassPath sourceRoots, ClassPath systemModules, ClassPath userModules, ClassPath legacyClassPath, Function<URL,Boolean> filter) {
        return ClassPathFactory.createClassPath(ClassPathSupportFactory.createModuleInfoBasedPath(base, sourceRoots, systemModules, userModules, legacyClassPath, filter));
    }
    
    private ClassPath createModuleInfoSelector(Supplier<ClassPath> hasModuleInfoClassPath, Supplier<ClassPath> noModuleInfoClassPath, String logDesc) {
        return createMultiplexClassPath(new ModuleInfoSelector(getNBMavenProject(), hasModuleInfoClassPath, noModuleInfoClassPath, logDesc));
    }
    
    private ClassPath createTestClassPathSelector(Supplier<ClassPath> testPath, Supplier<ClassPath> testScopedPath, String logDesc) {
        return createMultiplexClassPath(new TestClassPathSelector(getNBMavenProject(), testPath, testScopedPath, logDesc));
    }
    
    private ClassPath createTestModulePathSelector(Supplier<ClassPath> path, Supplier<ClassPath> testPath, String logDesc) {
        return createMultiplexClassPath(new TestModulePathSelector(getNBMavenProject(), path, testPath, logDesc));
    }
    
    private ClassPath createMultiplexClassPath(ClassPathSelector selector) {
        return org.netbeans.spi.java.classpath.support.ClassPathSupport.createMultiplexClassPath(selector);
    }
    
    private NbMavenProjectImpl getNBMavenProject() {
        return proj.getLookup().lookup(NbMavenProjectImpl.class);
    }
    
    private static class ModuleInfoSelector extends ClassPathSelector {
                
        private final Supplier<ClassPath> noModuleInfoCP;
        private final Supplier<ClassPath> hasModuleInfoCP;
        private final String logDesc;
        
        public ModuleInfoSelector(NbMavenProjectImpl proj, Supplier<ClassPath> hasModuleInfoClassPath, Supplier<ClassPath> noModuleInfoClassPath, String logDesc) {
            super(proj);
            this.hasModuleInfoCP = hasModuleInfoClassPath;
            this.noModuleInfoCP = noModuleInfoClassPath;
            this.logDesc = logDesc;
        }

        @Override
        public ClassPath getActiveClassPath() {
            ClassPath ret = active;
            if (ret == null) {
                // see org.apache.maven.plugin.compiler.CompilerMojo.classpathElements
                for (String sourceRoot : proj.getOriginalMavenProject().getCompileSourceRoots()) {
                    final File moduleInfoFile = new File(sourceRoot, MODULE_INFO_JAVA);
                    if(moduleInfoFile.exists()) {
                        FileObject moduleInfo = FileUtil.toFileObject(moduleInfoFile);
                        String sourceLevel = SourceLevelQuery.getSourceLevel2(moduleInfo).getSourceLevel();
                        String ide_jdkvers = System.getProperty("java.version"); //NOI18N
                        if(!sourceLevel.startsWith("1.") && !ide_jdkvers.startsWith("1.")) { //NOI18N
                            // both sourceLevel and ideJDK are 9+
                            ret = hasModuleInfoCP.get();  
                        }
                        final Object retObject = ret;
                        LOGGER.log(Level.FINER, () -> String.format("ModuleInfoSelector %s for project %s: has module-info.java %s", logDesc, proj.getProjectDirectory().getPath(), retObject == null ? "IGNORED" : "")); // NOI18N
                        break;
                    }
                }
                if(ret == null) {
                    ret = noModuleInfoCP.get();
                }
                active = ret;
            }            
            LOGGER.log(Level.FINE, "ModuleInfoSelector {0} for project {1} active class path: {2}", new Object[]{logDesc, proj.getProjectDirectory().getPath(), ret}); // NOI18N
            return ret;
        }

        @Override
        protected boolean isReset(PropertyChangeEvent evt) {
            boolean reset = false;            
            if( (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof URI)) {
                File file = Utilities.toFile((URI) evt.getNewValue());
                LOGGER.log(Level.FINER, "{0} checking reset with file {1}", new Object[] { getClass(), file });
                for (String sourceRoot : proj.getOriginalMavenProject().getCompileSourceRoots()) {
                    if(file.equals(new File(sourceRoot, MODULE_INFO_JAVA))) {
                        reset = true;
                        break;
                    }
                }                
                if(reset) {
                    LOGGER.log(Level.FINER, "ModuleInfoSelector {0} for project {1} resource changed: {2}", new Object[]{logDesc, proj.getProjectDirectory().getPath(), evt});
                }
            }
            return reset;
        }
    }
           
    private static class TestModulePathSelector extends TestPathSelector {
        protected final Supplier<ClassPath> path;
        protected final Supplier<ClassPath> testPath;
        TestModulePathSelector(NbMavenProjectImpl proj, Supplier<ClassPath> path, Supplier<ClassPath> testPath, String logDesc) {
            super(proj, logDesc);
            this.path = path;
            this.testPath = testPath;
        }
        @Override
        protected ClassPath getActiveClassPath(boolean hasTestModuleDescriptor, boolean hasMainModuleDescriptor) {
            // see how modulepathElements are set in org.apache.maven.plugin.compiler.TestCompilerMojo
            // XXX at the moment the asumption is made that exec-maven-plugin (runtime) will follow symetric logic like maven-compiler-plugin
            if ( hasTestModuleDescriptor ) {
                return testPath.get();
            } else {
                return hasMainModuleDescriptor ? path.get() : ClassPath.EMPTY;
            }
        }
    }
    
    private static class TestClassPathSelector extends TestPathSelector {
        private final Supplier<ClassPath> testPath;
        private final Supplier<ClassPath> testScopedPath;
        TestClassPathSelector(NbMavenProjectImpl proj, Supplier<ClassPath> testPath, Supplier<ClassPath> testScopedPath, String logDesc) {
            super(proj, logDesc); // NOI18N
            this.testPath = testPath;
            this.testScopedPath = testScopedPath;
        }
        @Override
        protected ClassPath getActiveClassPath(boolean hasTestModuleDescriptor, boolean hasMainModuleDescriptor) {        
            // see how classpathElements are set in org.apache.maven.plugin.compiler.TestCompilerMojo
            // XXX at the moment the asumption is made that exec-maven-plugin (runtime) will follow symetric logic like maven-compiler-plugin
            if ( hasTestModuleDescriptor ) {
                return ClassPath.EMPTY;
            } else {
                return hasMainModuleDescriptor ? testScopedPath.get() : testPath.get();
            }
        }
    }
    
    private abstract static class TestPathSelector extends ClassPathSelector {
        private final String logDesc;
        
        TestPathSelector(NbMavenProjectImpl proj, String logDesc) {
            super(proj);
            this.logDesc = logDesc;
        }

        @Override
        public ClassPath getActiveClassPath() {
            ClassPath ret = active;
            if (ret == null) {
                MavenProject mp = proj.getOriginalMavenProject();
                boolean hasMainModuleDescriptor = hasModuleDescriptor(mp.getCompileSourceRoots());
                if(hasMainModuleDescriptor) {
                    LOGGER.log(Level.FINER, "TestPathSelector {0} for project {1}: has main module-info.java", new Object [] {logDesc, proj.getProjectDirectory().getPath()}); // NOI18N
                }
                
                boolean hasTestModuleDescriptor = hasModuleDescriptor(mp.getTestCompileSourceRoots());
                if(hasTestModuleDescriptor) {
                    LOGGER.log(Level.FINER, "TestPathSelector {0} for project {1}: has test module-info.java", new Object [] {logDesc, proj.getProjectDirectory().getPath()}); // NOI18N
                }
                
                ret = getActiveClassPath(hasTestModuleDescriptor, hasMainModuleDescriptor);

                active = ret;
            }            
            LOGGER.log(Level.FINE, "TestPathSelector {0} for project {1} active class path: {2}", new Object[]{logDesc, proj.getProjectDirectory().getPath(), ret}); // NOI18N
            return ret;
        }

        private boolean hasModuleDescriptor(List<String> roots) {
            return roots.stream().anyMatch((root) -> Files.exists(Paths.get(root, MODULE_INFO_JAVA)));
        }

        protected abstract ClassPath getActiveClassPath(boolean hasTestModuleDescriptor, boolean hasMainModuleDescriptor);

        @Override
        protected boolean isReset(PropertyChangeEvent evt) {
            boolean reset = false;            
            if( (NbMavenProject.PROP_RESOURCE.equals(evt.getPropertyName()) && evt.getNewValue() instanceof URI)) {
                File file = Utilities.toFile((URI) evt.getNewValue());
                MavenProject mp = proj.getOriginalMavenProject();
                reset = mp.getCompileSourceRoots().stream().anyMatch((sourceRoot) -> (file.equals(new File(sourceRoot, MODULE_INFO_JAVA)))) ||
                        mp.getTestCompileSourceRoots().stream().anyMatch((sourceRoot) -> (file.equals(new File(sourceRoot, MODULE_INFO_JAVA))));                
                if(reset) {
                    LOGGER.log(Level.FINER, "TestPathSelector {0} for project {1} resource changed: {2}", new Object[]{logDesc, proj.getProjectDirectory().getPath(), evt});
                }
            }
            return reset;
        }
    }
    
    /**
     * This selector chooses the annotation classpath, if it is not empty (has items, or is broken), or the regular
     * compile classpath if annotation path is empty. The selector reacts 
     */
    private static class AnnotationPathSelector extends ClassPathSelector
            implements PropertyChangeListener {
        private final ClassPath annotationCP;
        private final Supplier<ClassPath> compileClassPath;
        
        public AnnotationPathSelector(NbMavenProjectImpl proj, ClassPath anno, Supplier<ClassPath> compile) {
            super(proj);
            this.annotationCP = anno;
            this.compileClassPath = compile;
            
            anno.addPropertyChangeListener(WeakListeners.propertyChange(this, anno));
            NbMavenProject watcher = proj.getProjectWatcher();
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(this, watcher));
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            active = null;
            support.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
        }

        @Override
        protected boolean isReset(PropertyChangeEvent evt) {
            return NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName());
        }

        @Override
        public ClassPath getActiveClassPath() {
            if (active != null) {
                return active;
            }
            if (annotationCP.getFlags().contains(ClassPath.Flag.INCOMPLETE) ||
                !annotationCP.entries().isEmpty()) {
                return active = annotationCP;
            } else {
                return active = compileClassPath.get();
            }
        }
    }
    
    private abstract static class ClassPathSelector implements org.netbeans.spi.java.classpath.support.ClassPathSupport.Selector {
        protected final PropertyChangeSupport support = new PropertyChangeSupport(this);
        
        protected final NbMavenProjectImpl proj;
        protected ClassPath active = null;
        
        public ClassPathSelector(NbMavenProjectImpl proj) {
            this.proj = proj;   
            // see the usage of org.apache.maven.plugin.compiler.CompilerMojo.preparePaths 
            // maven checks recursively all source roots for module-info,
            // for performace reasons we will be checking and listening only on the root of a source root
            NbMavenProject.addPropertyChangeListener(proj, (evt) -> {
                LOGGER.log(Level.FINER, "{0} got property change {1} from {2}", new Object[] { getClass(), evt, proj });
                if (isReset(evt)) {
                    active = null;
                    support.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
                }
            });
        }

        protected abstract boolean isReset(PropertyChangeEvent evt);
        
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }

    }
}

