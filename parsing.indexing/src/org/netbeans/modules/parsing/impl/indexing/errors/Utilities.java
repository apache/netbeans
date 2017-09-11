/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
