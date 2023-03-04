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

package org.netbeans.modules.debugger.jpda.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Entlicher
 */
class SourceRootsCache {
    
    private final SourcePath sourcePath;
    private final SourceRootsChangedListener chListener;
    private Set<String> rootPaths;
    private Set<File> rootFiles;
    private Set<String> projectRootPaths;
    private Set<File> projectRootFiles;
    
    SourceRootsCache(SourcePath sourcePath) {
        this.sourcePath = sourcePath;
        this.chListener = new SourceRootsChangedListener();
        sourcePath.addPropertyChangeListener(
                WeakListeners.propertyChange(chListener,
                                             sourcePath));
    }
    
    SourcePath getSourcePath() {
        return sourcePath;
    }
    
    synchronized Set<String> getRootPaths() {
        if (rootPaths == null) {
            String[] sourceRoots = sourcePath.getSourceRoots();
            rootPaths = Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(sourceRoots)));
        }
        return rootPaths;
    }
    
    synchronized Set<File> getRootCanonicalFiles() {
        if (rootFiles == null) {
            String[] sourceRoots = sourcePath.getSourceRoots();
            rootFiles = getCanonicalFiles(sourceRoots);
        }
        return rootFiles;
    }
    
    synchronized Set<String> getProjectRootPaths() {
        if (projectRootPaths == null) {
            String[] sourceRoots = sourcePath.getProjectSourceRoots();
            projectRootPaths = Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(sourceRoots)));
        }
        return projectRootPaths;
    }
    
    synchronized Set<File> getProjectRootCanonicalFiles() {
        if (projectRootFiles == null) {
            String[] sourceRoots = sourcePath.getProjectSourceRoots();
            projectRootFiles = getCanonicalFiles(sourceRoots);
        }
        return projectRootFiles;
    }
    
    private static Set<File> getCanonicalFiles(String[] paths) {
        Set<File> files = new HashSet<>(paths.length);
        for (String root : paths) {
            File rootFile = new File(root);
            try {
                rootFile = rootFile.getCanonicalFile();
            } catch (IOException ioex) {}
            files.add(rootFile);
        }
        return Collections.unmodifiableSet(files);
    }
    
    private class SourceRootsChangedListener implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourcePathProvider.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                synchronized (SourceRootsCache.this) {
                    rootPaths = null;
                    rootFiles = null;
                }
            }
        }
        
    }
}
