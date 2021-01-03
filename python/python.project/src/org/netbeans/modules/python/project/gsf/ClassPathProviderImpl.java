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
package org.netbeans.modules.python.project.gsf;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.python.project.PythonProject;
import org.netbeans.modules.python.project.SourceRoots;
import org.netbeans.modules.python.project.util.Pair;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
//import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
//import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
//import org.netbeans.modules.gsfpath.spi.classpath.ClassPathImplementation;
//import org.netbeans.modules.gsfpath.spi.classpath.ClassPathProvider;
//import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Defines various paths for the Python Project.
 * Based on the Ruby project, in turn based on the J2SE project.
 * Greatly simplified at the moment since Python projects don't have a Sources object,
 * and there's no separate source/test folders.
 */
public final class ClassPathProviderImpl implements ClassPathProvider {
    private final PythonProject project;
    private final SourceRoots sources;
    private final SourceRoots tests;
    private final Map<Pair<String,Integer>,ClassPath> cache = new HashMap<>();

    public ClassPathProviderImpl(final PythonProject project) {
        assert project != null;
        this.project = project;
        this.sources = project.getSourceRoots();
        assert this.sources != null;
        this.tests = project.getTestRoots();
        assert this.tests != null;
    }

    private static final int MAX_TYPES = 3;
    /**
     * Find what a given file represents.
     * @param file a file in the project
     * @return one of: <dl>
     *         <dt>0</dt> <dd>normal source</dd>
     *         <dt>1</dt> <dd>test source</dd>
     *         <dt>2</dt> <dd>the project root</dd>
     *         <dt>-1</dt> <dd>something else</dd>
     *         </dl>
     */
    private int getType(FileObject file) {
        if (file == project.getProjectDirectory()) {
            return 2;
        }
        for (FileObject root : sources.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 0;
            }
        }
        for (FileObject root : tests.getRoots()) {
            if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                return 1;
            }
        }
        return -1;
    }

    private synchronized ClassPath getSourcepath(FileObject file) {
        int type = getType(file);
        return this.getSourcepath(type);
    }

    private ClassPath getSourcepath(int type) {
        if (type < 0 || type > MAX_TYPES) {
            return null;
        }
        final Pair<String,Integer> key = Pair.of(ClassPath.SOURCE, type);
        ClassPath cp = cache.get(key);
        if (cp == null) {
            switch (type) {
                case 0:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sources));
                    break;
                case 1:
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(tests));
                    break;
                case 2:
                    // Classpath for the "whole project" - for now just use the sources
                    // Used from the tasklist for example.
                    cp = ClassPathFactory.createClassPath(new SourcePathImplementation(sources));
                    break;

                default:
                    throw new UnsupportedOperationException("Only sources are available in the Python project at this point");
           }
           cache.put (key,cp);
        }
        return cp;
    }

    private synchronized ClassPath getBootClassPath() {        
        final Pair<String,Integer> key = Pair.of(ClassPath.BOOT, 0);
        ClassPath cp = cache.get(key);
        if (cp == null) {
            //todo: For now merge compile and platform class paths
            //under parsing api they should be separated
            final ClassPathImplementation boot = new BootClassPathImplementation(project);
            final ClassPathImplementation compile = new CompilePathImplementation(project);
            cp = ClassPathFactory.createClassPath(ClassPathSupport.createProxyClassPathImplementation(boot,compile));
           cache.put (key,cp);
        }        
        return cp;
    }

//    private synchronized ClassPath getCompileClassPath() {
//        final Pair<String,Integer> key = Pair.of(ClassPath.COMPILE, 0);
//        ClassPath cp = cache.get(key);
//        if (cp == null) {
//            cp = ClassPathFactory.createClassPath(new CompilePathImplementation(this.project));
//           cache.put (key,cp);
//        }
//        return cp;
//    }

    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        if (type.equals(ClassPath.SOURCE)) {
            return getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            return getBootClassPath();
        } else if (type.equals(ClassPath.COMPILE)) {
            // Bogus
            return getBootClassPath();
        } else {
            return null;
        }
    }

    /**
     * Returns array of all classpaths of the given type in the project.
     * The result is used for example for GlobalPathRegistry registrations.
     */
    public ClassPath[] getProjectClassPaths(String type) {
        if (ClassPath.BOOT.equals(type)) {
            return new ClassPath[]{getBootClassPath()};
        }
        if (ClassPath.SOURCE.equals(type)) {
            ClassPath[] l = new ClassPath[1];
            l[0] = getSourcepath(0);
            return l;
        }
        return null;
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots). Valid types are BOOT, SOURCE and COMPILE.
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        if (ClassPath.BOOT.equals(type)) {
             return getBootClassPath();
        }
        if (ClassPath.SOURCE.equals(type)) {
            return getSourcepath(0);
        }
        return null;
    }            
}
