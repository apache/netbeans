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
package org.netbeans.modules.cnd.makeproject;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakSet;

/**
 * Provides ClassPath for CND projects related files. See Bug #215926 for more
 * details.
 *
 * The idea and code is borrowed from
 * org.modules.php.project.classpath.IncludePathClassPathProvider
 *
 */
//
@org.openide.util.lookup.ServiceProvider(service = ClassPathProvider.class, position = 200)
public class MakeProjectClassPathProvider implements ClassPathProvider {
    private static final Logger LOG = Logger.getLogger(MakeProjectClassPathProvider.class.getName());

    private static final Set<ClassPath> PROJECT_CPS = new WeakSet<>();
    private static final ReadWriteLock PROJECT_LOCK = new ReentrantReadWriteLock();

    public static void addProjectCP(final ClassPath[] cp) {
        runUnderWriteLock(() -> {
            Collections.addAll(PROJECT_CPS, cp);
        });
    }

    public static void removeProjectCP(final ClassPath[] cp) {
        runUnderWriteLock(() -> {
            for (ClassPath classPath : cp) {
                PROJECT_CPS.remove(classPath);
            }
        });
    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (MakeProjectPaths.SOURCES.equals(type)) {
            PROJECT_LOCK.readLock().lock();
            try {
                for (ClassPath spc : PROJECT_CPS) {
                    boolean accept = false;
                    if (spc.contains(file)) {
                        Project owner = FileOwnerQuery.getOwner(file);
                        if (owner instanceof MakeProject) {
                            accept = true;
                        }
                    }
                    if (accept) {
                        LOG.log(Level.FINE, "findClassPath({0}, {1}) -> {2} from {3}", new Object[] {file, type, spc, MakeProjectClassPathProvider.class});
                        return spc;
                    }
                }
            } finally {
                PROJECT_LOCK.readLock().unlock();
            }
        }
        LOG.log(Level.FINE, "findClassPath({0}, {1}) -> null", new Object[] {file, type});
        return null;
    }

    private static void runUnderWriteLock(Runnable runnable) {
        PROJECT_LOCK.writeLock().lock();
        try {
            runnable.run();
        } finally {
            PROJECT_LOCK.writeLock().unlock();
        }
    }
}
