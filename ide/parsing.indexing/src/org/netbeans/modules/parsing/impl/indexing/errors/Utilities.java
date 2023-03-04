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

package org.netbeans.modules.parsing.impl.indexing.errors;

import org.netbeans.modules.parsing.impl.indexing.implspi.FileAnnotationsRefresh;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.netbeans.modules.parsing.impl.indexing.PathRecognizerRegistry;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 *
 * @author lahvac
 */
public class Utilities {

    private static final boolean BADGES_ENABLED = !Boolean.getBoolean(String.format("%s.errorBadges.disable",   //NOI18N
            TaskCache.class.getName()));
    private static volatile Pair<FileObject,Reference<ClassPath>> rootCache;

    public static ClassPath getSourceClassPathFor(FileObject file) {
        Pair<FileObject,Reference<ClassPath>> ce = rootCache;
        ClassPath cp;
        if (ce != null &&
            (cp = ce.second().get()) != null &&
            ce.first().equals(cp.findOwnerRoot(file))) {
            return cp;
        }
        for (String sourceCP : PathRecognizerRegistry.getDefault().getSourceIds()) {
            cp = ClassPath.getClassPath(file, sourceCP);
            if (cp != null) {
                final FileObject root = cp.findOwnerRoot(file);
                if (root != null) {
                    rootCache = Pair.<FileObject,Reference<ClassPath>>of(
                        root,
                        new WeakReference<ClassPath>(cp));
                }
                return cp;
            }
        }
        return null;
    }

    public static Iterable<? extends FileObject> findIndexedRootsUnderDirectory(Project p, FileObject bigRoot) {
        List<FileObject> result = new LinkedList<FileObject>();
        try {
            Iterable<? extends FileObject> roots = CacheFolder.findRootsWithCacheUnderFolder(bigRoot);

            for (FileObject root : roots) {
                Project curr = FileOwnerQuery.getOwner(root);

                if (   curr != null
                    && curr.getProjectDirectory() == p.getProjectDirectory()
                    && PathRegistry.getDefault().getSources().contains(root.toURL())) {
                    result.add(root);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
        }
        return result;
    }

    public static boolean isBadgesEnabled() {
        return BADGES_ENABLED;
    }

    static void refreshAnnotations(@NonNull final Set<URL> toRefresh) {
        final Collection<? extends FileAnnotationsRefresh> refreshables = Lookup.getDefault().lookupAll(FileAnnotationsRefresh.class);
        for (FileAnnotationsRefresh refreshable : refreshables) {
            refreshable.refresh(toRefresh);
        }
    }

    private Utilities() {}

}
