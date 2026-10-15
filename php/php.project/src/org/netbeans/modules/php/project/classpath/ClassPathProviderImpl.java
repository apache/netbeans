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
package org.netbeans.modules.php.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.api.PhpSourcePath.FileType;
import org.netbeans.modules.php.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Defines the various (BOOT, SOURCE, TEST) class paths for a PHP project.
 */
public final class ClassPathProviderImpl implements ClassPathProvider, PhpSourcePathImplementation, PropertyChangeListener {

    /**
     * Constants for different cached classpaths.
     */
    private static enum ClassPathCache {
        PLATFORM,
        SOURCE,
        TEST
    }

    private final PhpProject project;
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sources;
    private final SourceRoots tests;
    private final SourceRoots selenium;

    // if new item is added to this map, do not forget to update propertyChange() method as well
    private final ConcurrentMap<String, List<FileObject>> dirCache = new ConcurrentHashMap<>();
    // GuardedBy(cache)
    private final Map<ClassPathCache, ClassPath> cache = new EnumMap<>(ClassPathCache.class);

    public ClassPathProviderImpl(PhpProject project, SourceRoots sources, SourceRoots tests, SourceRoots selenium) {
        assert project != null;
        assert sources != null;
        assert tests != null;
        assert selenium != null;

        this.project = project;
        this.helper = project.getHelper();
        projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        assert projectDirectory != null;
        this.evaluator = ProjectPropertiesSupport.getPropertyEvaluator(project);
        this.sources = sources;
        this.tests = tests;
        this.selenium = selenium;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    private List<FileObject> getDirs(String propname) {
        List<FileObject> dirs = dirCache.get(propname);
        if (!checkDirs(dirs)) {
            // #217861 - it is ok if directories are counted more times...
            String prop = evaluator.getProperty(propname);
            if (prop == null) {
                return Collections.<FileObject>emptyList();
            }
            String[] paths = PropertyUtils.tokenizePath(prop);
            dirs = new ArrayList<>(paths.length);
            for (String path : paths) {
                FileObject resolvedFile = helper.resolveFileObject(path);
                if (resolvedFile != null) {
                    dirs.add(resolvedFile);
                }
            }
            dirCache.put(propname, dirs);
        }
        return dirs;
    }

    private boolean checkDirs(List<FileObject> dirs) {
        if (dirs == null) {
            return false;
        }
        for (FileObject fo : dirs) {
            if (!fo.isValid()) {
                return false;
            }
        }
        return true;
    }

    private List<FileObject> getPlatformPath() {
        List<FileObject> files = new ArrayList<>();
        files.addAll(getDirs(PhpProjectProperties.INCLUDE_PATH));
        files.addAll(getDirs(PhpProjectProperties.PRIVATE_INCLUDE_PATH));
        return files;
    }

    // #221036 - order of the directories is from the "nearest"
    // (if one has project on the (global) include path -> SOURCE should be returned, not INCLUDE)
    @Override
    public FileType getFileType(FileObject file) {
        Parameters.notNull("file", file);

        // first check tests because test directory can be underneath sources directory
        for (FileObject root : tests.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.TEST;
            }
        }

        // selenium
        for (FileObject root : selenium.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                // for now, return TEST type as well (it's probably ok)
                return FileType.TEST;
            }
        }

        for (FileObject root : sources.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return FileType.SOURCE;
            }
        }

        for (FileObject dir : getPlatformPath()) {
            if (dir.equals(file) || FileUtil.isParentOf(dir, file)) {
                return FileType.INCLUDE;
            }
        }

        if (PhpProjectUtils.isInternalFile(file)) {
            return FileType.INTERNAL;
        }

        return FileType.UNKNOWN;
    }

    @Override
    public List<FileObject> getIncludePath() {
        return new ArrayList<>(getPlatformPath());
    }

    @Override
    public FileObject resolveFile(FileObject directory, String fileName) {
        FileObject resolved = directory.getFileObject(fileName);
        if (resolved != null) {
            return resolved;
        }
        for (FileObject dir : getPlatformPath()) {
            resolved = dir.getFileObject(fileName);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private ClassPath getSourcePath(FileObject file) {
        return getSourcePath(getFileType(file));
    }

    private ClassPath getSourcePath(FileType type) {
        ClassPath cp = null;
        switch (type) {
            case SOURCE:
                synchronized (cache) {
                    cp = cache.get(ClassPathCache.SOURCE);
                    if (cp == null) {
                        cp = ClassPathFactory.createClassPath(new SourcePathImplementation(project, sources, tests, selenium));
                        cache.put(ClassPathCache.SOURCE, cp);
                    }
                }
                break;
            case TEST:
                synchronized (cache) {
                    cp = cache.get(ClassPathCache.TEST);
                    if (cp == null) {
                        ClassPath testsCp = ClassPathFactory.createClassPath(new SourcePathImplementation(project, tests));
                        ClassPath seleniumCp = ClassPathFactory.createClassPath(new SourcePathImplementation(project, selenium));
                        cp = ClassPathSupport.createProxyClassPath(testsCp, seleniumCp);
                        cache.put(ClassPathCache.TEST, cp);
                    }
                }
                break;
            default:
                // XXX any exception?
                break;
        }
        return cp;
    }

    private ClassPath getBootClassPath() {
        ClassPath cp;
        // #141746
        synchronized (cache) {
            cp = cache.get(ClassPathCache.PLATFORM);
            if (cp == null) {
                List<FileObject> internalFolders = CommonPhpSourcePath.getInternalPath();
                ClassPath internalClassPath = ClassPathSupport.createClassPath(internalFolders.toArray(new FileObject[0]));
                ClassPath includePath = ClassPathFactory.createClassPath(
                        ProjectClassPathSupport.createPropertyBasedClassPathImplementation(projectDirectory, evaluator,
                        new String[] {PhpProjectProperties.INCLUDE_PATH, PhpProjectProperties.PRIVATE_INCLUDE_PATH}));
                cp = ClassPathSupport.createProxyClassPath(
                        internalClassPath, includePath);
                cache.put(ClassPathCache.PLATFORM, cp);
            }
        }
        return cp;
    }

    private ClassPath getProjectBootClassPath() {
        return getSourcePath(FileType.SOURCE);
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(PhpSourcePath.BOOT_CP)) {
            return getBootClassPath();
        } else if (type.equals(PhpSourcePath.PROJECT_BOOT_CP)) {
            // XXX check source root and return null for Source Files (FOQ -> project > source roots)
            return getProjectBootClassPath();
        } else if (type.equals(PhpSourcePath.SOURCE_CP)) {
            return getSourcePath(file);
//        } else if (type.equals(ClassPath.COMPILE)) {
//            // ???
//            return getBootClassPath();
        } else if (type.equals("js/library")) { // NOI18N
            return getSourcePath(FileType.SOURCE);
        }
//        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (PhpSourcePath.BOOT_CP.equals(type)) {
            return new ClassPath[] {getBootClassPath()};
        } else if (PhpSourcePath.PROJECT_BOOT_CP.equals(type)) {
            return new ClassPath[] {getProjectBootClassPath()};
        } else if (PhpSourcePath.SOURCE_CP.equals(type)) {
            return new ClassPath[] {
                getSourcePath(FileType.SOURCE),
                getSourcePath(FileType.TEST),
            };
        }
        assert false : "Unknown classpath type requested: " + type;
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (PhpProjectProperties.INCLUDE_PATH.equals(propertyName)
                || PhpProjectProperties.PRIVATE_INCLUDE_PATH.equals(propertyName)) {
            dirCache.remove(propertyName);
        }
    }
}
