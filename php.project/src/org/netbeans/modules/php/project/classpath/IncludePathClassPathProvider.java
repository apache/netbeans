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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
            globalIncludePathClassPath = ClassPathSupport.createClassPath(includePath.toArray(new FileObject[includePath.size()]));
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
