/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
