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
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakSet;

/**
 * Provides ClassPath for php files on include path or without a project.
 */
@org.openide.util.lookup.ServiceProvider(service = ClassPathProvider.class, position = 200)
public class IncludePathClassPathProvider implements ClassPathProvider {

    private static final boolean RUNNING_IN_TEST = Boolean.getBoolean("nb.php.test.run"); // NOI18N

    // @GuardedBy(PROJECT_INCLUDES_LOCK)
    private static final Set<ClassPath> PROJECT_INCLUDES = new WeakSet<>();
    private static final ReadWriteLock PROJECT_INCLUDES_LOCK = new ReentrantReadWriteLock();

    // @GuardedBy("IncludePathClassPathProvider.class")
    static ClassPath globalIncludePathClassPath = null;
    // @GuardedBy("IncludePathClassPathProvider.class")
    private static PropertyChangeListener includePathListener = null;


    public static void addProjectIncludePath(final ClassPath classPath) {
        runUnderWriteLock(new Runnable() {
            @Override
            public void run() {
                PROJECT_INCLUDES.add(classPath);
            }
        });
    }

    public static void removeProjectIncludePath(final ClassPath classPath) {
        runUnderWriteLock(new Runnable() {
            @Override
            public void run() {
                PROJECT_INCLUDES.remove(classPath);
            }
        });
    }

    public static ClassPath findProjectIncludePath(FileObject file) {
        PROJECT_INCLUDES_LOCK.readLock().lock();
        try {
            for (ClassPath classPath : PROJECT_INCLUDES) {
                if (classPath.contains(file)) {
                    return classPath;
                }
            }
        } finally {
            PROJECT_INCLUDES_LOCK.readLock().unlock();
        }
        return null;
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (!PhpSourcePath.BOOT_CP.equals(type)) {
            return null;
        }
        ClassPath cp = findProjectIncludePath(file);
        if (cp != null) {
            return cp;
        }
        if (RUNNING_IN_TEST) {
            return null;
        }
        // not found, then return CP for global include path
        return getGlobalIncludePathClassPath();
    }

    private static synchronized ClassPath getGlobalIncludePathClassPath() {
        if (includePathListener == null) {
            includePathListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (PhpOptions.PROP_PHP_GLOBAL_INCLUDE_PATH.equals(evt.getPropertyName())) {
                        resetGlobalIncludePathClassPath();
                    }
                }
            };
            PhpOptions.getInstance().addPropertyChangeListener(includePathListener);
        }
        if (globalIncludePathClassPath == null) {
            List<FileObject> includePath = PhpSourcePath.getIncludePath(null);
            globalIncludePathClassPath = ClassPathSupport.createClassPath(includePath.toArray(new FileObject[0]));
        }
        return globalIncludePathClassPath;
    }

    static synchronized void resetGlobalIncludePathClassPath() {
        globalIncludePathClassPath = null;
    }

    private static void runUnderWriteLock(Runnable runnable) {
        PROJECT_INCLUDES_LOCK.writeLock().lock();
        try {
            runnable.run();
        } finally {
            PROJECT_INCLUDES_LOCK.writeLock().unlock();
        }
    }

}
