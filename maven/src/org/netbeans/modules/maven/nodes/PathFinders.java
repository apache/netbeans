/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
