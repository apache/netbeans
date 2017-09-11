/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
