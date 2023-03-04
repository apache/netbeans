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

package org.netbeans.modules.web.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupportFactory;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 * Defines the various class paths for a web project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PropertyChangeListener {
    
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testSourceRoots;
    private final Map<ClassPathCache, ClassPath> cache = new HashMap<ClassPathCache, ClassPath>();

    private final Map<String,FileObject> dirCache = new HashMap<String,FileObject>();
    
    private org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl javaClassPathProvider;

    /**
     * Type of file classpath is required for.
     */
    private static enum FileType {
        SOURCE,         // java source
        CLASS,          // compiled java class
        WEB_SOURCE,     // web source
        UNKNOWN }

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        WEB_SOURCE,
        WEB_COMPILATION,
        WEB_RUNTIME,
    }
    
    public ClassPathProviderImpl(AntProjectHelper helper, PropertyEvaluator evaluator, 
            SourceRoots sourceRoots, SourceRoots testSourceRoots) {
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert this.projectDirectory != null;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testSourceRoots = testSourceRoots;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        this.javaClassPathProvider = new org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl(
                helper, evaluator, sourceRoots, testSourceRoots, 
                ProjectProperties.BUILD_CLASSES_DIR, WebProjectProperties.DIST_WAR, ProjectProperties.BUILD_TEST_CLASSES_DIR,
                new String[] {"javac.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH },
                new String[] {ProjectProperties.JAVAC_PROCESSORPATH},
                new String[] {"javac.test.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH },
                new String[] {"debug.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH },
                new String[] {"run.test.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH },
                new String[] {ProjectProperties.ENDORSED_CLASSPATH});
    }

    private FileObject getDir(final String propname) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<FileObject>() {
            public FileObject run() {
                synchronized (ClassPathProviderImpl.this) {
                    FileObject fo = (FileObject) ClassPathProviderImpl.this.dirCache.get (propname);
                    if (fo == null ||  !fo.isValid()) {
                        String prop = evaluator.getProperty(propname);
                        if (prop != null) {
                            fo = helper.resolveFileObject(prop);
                            ClassPathProviderImpl.this.dirCache.put (propname, fo);
                        }
                    }
                    return fo;
                }
            }});
    }
    
    private FileObject[] getPrimarySrcPath() {
        return this.sourceRoots.getRoots();
    }

    private FileObject getBuildClassesDir() {
        return getDir(ProjectProperties.BUILD_CLASSES_DIR);
    }

    private FileObject getDocumentBaseDir() {
        return getDir(WebProjectProperties.WEB_DOCBASE_DIR);
    }
    
     /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of FileType.* constants
     */
   private FileType getType(FileObject file) {
        FileObject[] srcPath = getPrimarySrcPath();
        for (int i=0; i < srcPath.length; i++) {
            FileObject root = srcPath[i];
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.SOURCE;
            }
        }
        FileObject dir = getDocumentBaseDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir,file))) {
            return FileType.WEB_SOURCE;
        }
        dir = getBuildClassesDir();
        if (dir != null && (dir.equals(file) || FileUtil.isParentOf(dir, file))) {
            return FileType.CLASS;
        }
        
        return FileType.UNKNOWN;
    }
    
    private synchronized ClassPath getCompileTimeClasspath(FileType type) {        
        if (type == FileType.WEB_SOURCE) {
            if (sourceRoots.getRoots().length > 0) {
                return javaClassPathProvider.findClassPath(sourceRoots.getRoots()[0], ClassPath.COMPILE);
            } else {
                ClassPath cp = cache.get(ClassPathCache.WEB_COMPILATION);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                        projectDirectory, evaluator, new String[] {"javac.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                    cache.put(ClassPathCache.WEB_COMPILATION, cp);
                }
                return cp;
            }
        }
        return null;
    }
    
    private synchronized ClassPath getRunTimeClasspath(FileType type) {
        if (type == FileType.WEB_SOURCE) {
            if (sourceRoots.getRoots().length > 0) {
               return javaClassPathProvider.findClassPath(sourceRoots.getRoots()[0], ClassPath.EXECUTE);
            } else {
                ClassPath cp = cache.get(ClassPathCache.WEB_RUNTIME);
                if (cp == null) {
                    cp = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                        projectDirectory, evaluator, new String[] {"debug.classpath", WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
                    cache.put(ClassPathCache.WEB_RUNTIME, cp);
                }
                return cp;
            }
        }
        return null;
    }
    
    private synchronized ClassPath getSourcepath(FileType type) {
        if (type == FileType.WEB_SOURCE) {
            ClassPath cp = cache.get(ClassPathCache.WEB_SOURCE);
            if (cp == null) {
                cp = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                        ClassPathFactory.createClassPath(new JspSourcePathImplementation(helper, evaluator)),
                        ClassPathFactory.createClassPath(ClassPathSupportFactory.createSourcePathImplementation (this.sourceRoots, helper, evaluator)),
                    });
                cache.put(ClassPathCache.WEB_SOURCE, cp);

            }
            return cp;
        }
        return null;
    }
    
    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        ClassPath cp = javaClassPathProvider.findClassPath(file, type);
        if (cp != null) {
            return cp;
        }
        FileType fileType = getType(file);
        if (type.equals(ClassPath.COMPILE)) {
            cp = getCompileTimeClasspath(fileType);
        } else if (type.equals(ClassPath.EXECUTE)) {
            cp = getRunTimeClasspath(fileType);
        } else if (type.equals(ClassPath.SOURCE)) {
            cp = getSourcepath(fileType);
        } else if (type.equals("js/library")) { // NOI18N
            cp = getSourcepath(FileType.WEB_SOURCE);
        }
        return cp;
    }
    
    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] base = javaClassPathProvider.getProjectClassPaths(type);
            ClassPath[] l = new ClassPath[base.length+1];
            System.arraycopy(base, 0, l, 0, base.length);
            l[l.length-1] = getSourcepath(FileType.WEB_SOURCE);
            return l;
        } else {
            return javaClassPathProvider.getProjectClassPaths(type);
        }
    }
    
    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        return javaClassPathProvider.getProjectSourcesClassPath(type);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        dirCache.remove(evt.getPropertyName());
    }
    
    public String[] getPropertyName (SourceGroup sg, String type) {
        return javaClassPathProvider.getPropertyName(sg, type);
    }

}
