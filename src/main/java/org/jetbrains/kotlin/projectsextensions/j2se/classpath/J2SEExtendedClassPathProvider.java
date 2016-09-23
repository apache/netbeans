/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.j2se.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender;
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 *
 * @author Alexander.Baratynski
 */
public class J2SEExtendedClassPathProvider implements ClassPathProvider, PropertyChangeListener, ClassPathExtender {

    private static final String BUILD_CLASSES_DIR = "build.classes.dir"; // NOI18N
    private static final String DIST_JAR = "dist.jar"; // NOI18N
    private static final String BUILD_TEST_CLASSES_DIR = "build.test.classes.dir"; // NOI18N
    private static final String JAVAC_CLASSPATH = "javac.classpath";    //NOI18N
    private static final String KOTLINC_CLASSPATH = "kotlinc.classpath";
    private static final String JAVAC_TEST_CLASSPATH = "javac.test.classpath";  //NOI18N
    private static final String RUN_CLASSPATH = "run.classpath";    //NOI18N
    private static final String RUN_TEST_CLASSPATH = "run.test.classpath";  //NOI18N
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final ClassPath[] cache = new ClassPath[8];
    private final Map<String, FileObject> dirCache = new HashMap<String, FileObject>();
    private final BootClassPathImplementation bootClassPathImpl;
    private final Project project;
    
    public J2SEExtendedClassPathProvider(Project project) {
        Class projectClass = project.getClass();
        this.project = project;
        this.helper = getAntProjectHelper(projectClass, project);
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = getEvaluator(projectClass, project);
        this.sourceRoots = getSourceRoots(projectClass, project);
        this.testSourceRoots = getTestSourceRoots(projectClass, project);
        this.bootClassPathImpl = new BootClassPathImplementation(project, evaluator);
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(J2SEExtendedClassPathProvider.this, evaluator));
    }
    
    private SourceRoots getTestSourceRoots(Class clazz, Project project) {
        SourceRoots roots = null;
        
        try {
            Method ev = clazz.getMethod("getTestSourceRoots");
            roots = (SourceRoots) ev.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        } 
        
        return roots;
    }
    
    private SourceRoots getSourceRoots(Class clazz, Project project) {
        SourceRoots roots = null;
        
        try {
            Method ev = clazz.getMethod("getSourceRoots");
            roots = (SourceRoots) ev.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assert roots != null : "SourceRoots NPE";
        
        return roots;
    }
    
    private PropertyEvaluator getEvaluator(Class clazz, Project project) {
        PropertyEvaluator eval = null;
        
        try {
            Method ev = clazz.getMethod("evaluator");
            eval = (PropertyEvaluator) ev.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assert eval != null : "PropertyEvaluator NPE";
        
        return eval;
    }
    
    private AntProjectHelper getAntProjectHelper(Class clazz, Project project) {
        AntProjectHelper helper = null;
        
        try {
            Method getAntProjectHelper = clazz.getMethod("getAntProjectHelper");
            helper = (AntProjectHelper) getAntProjectHelper.invoke(project);
        } catch (ReflectiveOperationException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assert helper != null : "AntProjectHelper NPE";
        
        return helper;
    }
    
    private FileObject getDir(final String propname) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject>() {

            @Override
            public FileObject run() {
                synchronized (J2SEExtendedClassPathProvider.this) {
                    FileObject fo = (FileObject) J2SEExtendedClassPathProvider.this.dirCache.get(propname);
                    if (fo == null || !fo.isValid()) {
                        String prop = evaluator.getProperty(propname);
                        if (prop != null) {
                            fo = helper.resolveFileObject(prop);
                            J2SEExtendedClassPathProvider.this.dirCache.put(propname, fo);
                        }
                    }
                    return fo;
                }
            }
        });
    }

    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }

    private FileObject[] getTestSrcDir() {
        return this.testSourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir(BUILD_CLASSES_DIR);
    }

    private FileObject getDistJar() {
        return getDir(DIST_JAR);
    }

    private FileObject getBuildTestClassesDir() {
        return getDir(BUILD_TEST_CLASSES_DIR);
    }

    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>built class (unpacked)</dd>
     *         <dt>3</dt> <dd>built test class</dd>
     *         <dt>4</dt> <dd>built class (in dist JAR)</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    public int getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (FileObject root : srcPath) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }
        srcPath = getTestSrcDir();
        for (FileObject root : srcPath) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        FileObject dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 2;
        }
        dir = getDistJar(); // not really a dir at all, of course
        if (dir != null && dir.equals(FileUtil.getArchiveFile(file))) {
            // XXX check whether this is really the root
            return 4;
        }
        dir = getBuildTestClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return 3;
        }
        return -1;
    }

    private ClassPath getCompileTimeClasspath(FileObject file) {
        int type = getType(file);
        return this.getCompileTimeClasspath(type);
    }

    private synchronized ClassPath getCompileTimeClasspath(int type) {
        if (type < 0 || type > 1) {
            // Not a source file.
            return null;
        }

        ClassPath cp = cache[2 + type];
        if (cp == null) {
            List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();

            
            List<URL> kotlinBoot = bootClassPathImpl.getKotlinBootClassPath();
            for (URL url : kotlinBoot){
                resources.add(ClassPathSupport.createResource(url));
            }
            //lightclasses directory
            resources.add(ClassPathSupport.createResource(KotlinProjectHelper.INSTANCE.getLightClassesDirectory(project).toURL()));
            
            if (type == 0) {
                cp = ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                        projectDirectory, evaluator, new String[]{JAVAC_CLASSPATH, KOTLINC_CLASSPATH})); // NOI18N
            } else {
                cp = ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                        projectDirectory, evaluator, new String[]{JAVAC_TEST_CLASSPATH, KOTLINC_CLASSPATH})); // NOI18N
            }

            for (ClassPath.Entry entry : cp.entries()) {
                resources.add(ClassPathSupport.createResource(entry.getURL()));
            }
            cp = ClassPathSupport.createClassPath(resources);

            cache[2 + type] = cp;
        }
        return cp;
    }

    private ClassPath getRunTimeClasspath(FileObject file) {
        int type = getType(file);
        if (type < 0 || type > 4) {
            // Unregistered file, or in a JAR.
            // For jar:file:$projdir/dist/*.jar!/**/*.class, it is misleading to use
            // run.classpath since that does not actually contain the file!
            // (It contains file:$projdir/build/classes/ instead.)
            return null;
        } else if (type > 1) {
            type -= 2;            //Compiled source transform into source
        }
        return getRunTimeClasspath(type);
    }

    private synchronized ClassPath getRunTimeClasspath(final int type) {
        ClassPath cp = cache[4 + type];
        if (cp == null) {
            List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();

            List<URL> kotlinBoot = bootClassPathImpl.getKotlinBootClassPath();
            for (URL url : kotlinBoot){
                resources.add(ClassPathSupport.createResource(url));
            }

            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                                    projectDirectory, evaluator, new String[]{RUN_CLASSPATH, KOTLINC_CLASSPATH})); // NOI18N
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                                    projectDirectory, evaluator, new String[]{RUN_TEST_CLASSPATH, KOTLINC_CLASSPATH}));
                    
                    break;
                case 2:
                    cp = ClassPathFactory.createClassPath(
                            ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                                    projectDirectory, evaluator, new String[]{DIST_JAR, KOTLINC_CLASSPATH})); // NOI18N
                    break;
                default:
                    break;
            }

            for (ClassPath.Entry entry : cp.entries()) {
                resources.add(ClassPathSupport.createResource(entry.getURL()));
            }
            cp = ClassPathSupport.createClassPath(resources);

            cache[4 + type] = cp;
        }
        return cp;
    }

    private ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }

    private synchronized ClassPath getSourcepath(int type) {
        if (type < 0 || type > 1) {
            return null;
        }
        ClassPath cp = cache[type];
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(this.sourceRoots, helper, evaluator));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(this.testSourceRoots, helper, evaluator));
                    break;
            }
        }
        cache[type] = cp;
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {
        ClassPath cp = cache[7];
        if (cp == null) {
            cp = ClassPathFactory.createClassPath(new BootClassPathImplementation(project, evaluator));
            cache[7] = cp;
        }
        return cp;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.COMPILE)) {
            return getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            return getRunTimeClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else {
            return null;
        }
    }

    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ClassPath[]>() {

            @Override
            public ClassPath[] run() {
                if (ClassPath.BOOT.equals(type)) {
                    return new ClassPath[]{getBootClassPath()};
                }
                if (ClassPath.COMPILE.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getCompileTimeClasspath(0);
                    l[1] = getCompileTimeClasspath(1);
                    return l;
                }
                if (ClassPath.SOURCE.equals(type)) {
                    ClassPath[] l = new ClassPath[2];
                    l[0] = getSourcepath(0);
                    l[1] = getSourcepath(1);
                    return l;
                }
                assert false;
                return null;
            }
        });
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    @Override
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return getBootClassPath();
        }
        if (ClassPath.COMPILE.equals(type)) {
            return getCompileTimeClasspath(0);
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        if (ClassPath.EXECUTE.equals(type)) {
            return getRunTimeClasspath(0);
        }
        assert false;
        return null;
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
        KotlinProjectHelper.INSTANCE.updateExtendedClassPath(project);
    }

    public String[] getPropertyName(final SourceRoots roots, final String type) {
        if (roots.isTest()) {
            if (ClassPath.COMPILE.equals(type)) {
                return new String[] {JAVAC_TEST_CLASSPATH};
            } else if (ClassPath.EXECUTE.equals(type)) {
                return new String[]{RUN_TEST_CLASSPATH};
            } else {
                return null;
            }
        } else {
            if (ClassPath.COMPILE.equals(type)) {
                return new String[] {JAVAC_CLASSPATH};
            } else if (ClassPath.EXECUTE.equals(type)) {
                return new String[]{RUN_CLASSPATH};
            } else {
                return null;
            }
        }
    }

    public String[] getPropertyName(SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        FileObject[] path = getPrimarySrcPath();
        for (FileObject path1 : path) {
            if (root.equals(path1)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return new String[]{JAVAC_CLASSPATH};
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return new String[]{RUN_CLASSPATH};
                } else {
                    return null;
                }
            }
        }
        path = getTestSrcDir();
        for (FileObject path1 : path) {
            if (root.equals(path1)) {
                if (ClassPath.COMPILE.equals(type)) {
                    return new String[]{JAVAC_TEST_CLASSPATH};
                } else if (ClassPath.EXECUTE.equals(type)) {
                    return new String[]{RUN_TEST_CLASSPATH};
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}