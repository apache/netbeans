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
package org.netbeans.modules.maven.nodes;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.project.ui.PathFinder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Tomas Zezula
 */
class PathFinders {

    private PathFinders() {
        throw new IllegalStateException();
    }


    @NonNull
    static PathFinder createPathFinder() {
        return new SubNodesPathFinder();
    }

    @NonNull
    static PathFinder createDelegatingPathFinder(@NullAllowed PathFinder delegate) {
        final RebasePathFinder res = new RebasePathFinder();
        res.setDelegate(delegate);
        return res;
    }


    static void updateDelegate(
            @NonNull PathFinder pathFinder,
            @NonNull PathFinder delegate) {
        if (!(pathFinder instanceof RebasePathFinder)) {
            throw new IllegalStateException();
        }
        ((RebasePathFinder)pathFinder).setDelegate(delegate);
    }

    private static final class SubNodesPathFinder implements PathFinder {

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            for (Node  node : root.getChildren().getNodes(true)) {
                final org.netbeans.spi.project.ui.PathFinder pf =
                    node.getLookup().lookup(org.netbeans.spi.project.ui.PathFinder.class);
                if (pf == null) {
                    continue;
                }
                result = pf.findPath(node, target);
                if (result != null) {
                    break;
                }
            }
            return result;
        }

    }


    private static final class RebasePathFinder implements PathFinder {

        private static final String JAVA = ".java"; // NOI18N
        private static final String CLASS = ".class"; // NOI18N

        //@GuardBy("PathFinder.class")
        private static URI currentKey;
        //@GuardBy("PathFinder.class")
        private static Set<URI> currentValues;

        private volatile org.netbeans.spi.project.ui.PathFinder delegate;


        void setDelegate (@NullAllowed final org.netbeans.spi.project.ui.PathFinder delegate) {
            this.delegate = delegate;
        }

        @Override
        public Node findPath(Node root, Object target) {
            Node result = null;
            org.netbeans.spi.project.ui.PathFinder _delegate = delegate;
            if (_delegate != null && target instanceof FileObject) {
                FileObject binRoot = root.getLookup().lookup(FileObject.class);
                if (binRoot == null) {
                    Artifact ar = root.getLookup().lookup(Artifact.class);
                    if (ar != null) {
                        final File arFile = ar.getFile();
                        if (arFile != null) {
                            final URL arURL = FileUtil.urlForArchiveOrDir(arFile);
                            if (arURL != null) {
                                binRoot = URLMapper.findFileObject(arURL);
                            }
                        }
                    }
                }
                if (binRoot == null) {
                    DataObject dobj = root.getLookup().lookup(DataObject.class);
                    if (dobj != null) {
                        binRoot = dobj.getPrimaryFile();
                    }
                }
                if (binRoot != null) {
                    FileObject newTarget = rebase(binRoot, (FileObject) target);
                    if (newTarget != null) {
                        result = _delegate.findPath(root, newTarget);
                    }
                }
            }
            return result;
        }

        @CheckForNull
        static FileObject rebase(
                @NonNull final FileObject binRoot,
                @NonNull final FileObject sourceTarget) {

            if (shouldIgnore(sourceTarget.toURI(), binRoot.toURI())) {
                return null;
            }
            final URL providedBinRootURL = (URL) sourceTarget.getAttribute("classfile-root");    //NOI18N
            final String providedBinaryName = (String) sourceTarget.getAttribute("classfile-binaryName");   //NOI18N
            if (providedBinRootURL != null && providedBinaryName != null) {
                final FileObject providedBinRoot = URLMapper.findFileObject(providedBinRootURL);
                if (binRoot.equals(providedBinRoot)) {
                    return binRoot.getFileObject(providedBinaryName + CLASS);
                }
            } else {
                for (FileObject srcRoot : SourceForBinaryQuery.findSourceRoots(binRoot.toURL()).getRoots()) {
                    if (FileUtil.isParentOf(srcRoot, sourceTarget)) {
                        String path = FileUtil.getRelativePath(srcRoot, sourceTarget);
                        if (path.endsWith(JAVA)) {
                            path = path.substring(0,path.length()-JAVA.length()) + CLASS;
                        }
                        FileObject newTarget = binRoot.getFileObject(path);
                        if (newTarget != null) {
                            return newTarget;
                        }
                    }
                }
            }
            ignore(sourceTarget.toURI(), binRoot.toURI());
            return null;
        }

        private static synchronized boolean shouldIgnore (
                @NonNull final URI key,
                @NonNull final URI value) {
            if (!key.equals(currentKey)) {
                return false;
            }
            return currentValues.contains(value);
        }

        private static synchronized void ignore(
                @NonNull final URI key,
                @NonNull final URI value) {
            if (!key.equals(currentKey)) {
                currentKey = key;
                currentValues = new HashSet<URI>();
            }
            currentValues.add(value);
        }

    }

}
