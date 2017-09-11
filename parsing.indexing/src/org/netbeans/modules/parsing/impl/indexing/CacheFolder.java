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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.implspi.CacheFolderProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public final class CacheFolder {

    private static final Logger LOG = Logger.getLogger(CacheFolder.class.getName());
    private static final Set<CacheFolderProvider.Kind> EVERYTHING =
        Collections.unmodifiableSet(EnumSet.allOf(CacheFolderProvider.Kind.class));

    public static URL getSourceRootForDataFolder (final FileObject dataFolder) {
        try {
            return CacheFolderProvider.getRootForCacheFolder(dataFolder);
        } catch (IOException ioe) {
            LOG.log(Level.FINE, null, ioe);
        }
        return null;
    }

    public static FileObject getDataFolder (final URL root) throws IOException {
        return getDataFolder(root, false);
    }

    @CheckForNull
    public static FileObject getDataFolder (final URL root, final boolean onlyIfAlreadyExists) throws IOException {
        return getDataFolder(
            root,
            EVERYTHING,
            onlyIfAlreadyExists ?
                CacheFolderProvider.Mode.EXISTENT:
                CacheFolderProvider.Mode.CREATE);
    }

    @CheckForNull
    public static FileObject getDataFolder (
            @NonNull final URL root,
            @NonNull final Set<CacheFolderProvider.Kind> kinds,
            @NonNull final CacheFolderProvider.Mode mode) throws IOException {
        return CacheFolderProvider.getCacheFolderForRoot(
                root,
                kinds,
                mode);
    }

    @NonNull
    public static Iterable<? extends FileObject> findRootsWithCacheUnderFolder(@NonNull final FileObject folder) throws IOException {
        final Set<FileObject> roots = new HashSet<>();
        for (URL url : CacheFolderProvider.getRootsInFolder(folder.toURL())) {
            final FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                roots.add(fo);
            }
        }
        return roots;
    }

    /**
     * Returns the cache folder for the default {@link CacheFolderProvider}.
     * @return the cache folder
     */
    @NonNull
    public static FileObject getCacheFolder () {
        return DefaultCacheFolderProvider.getInstance().getCacheFolder();
    }


    /**
     * Only for unit tests! It's used also by CslTestBase, which is not in the
     * same package, hence the public keyword.
     *
     */
    public static void setCacheFolder (final FileObject folder) {
        DefaultCacheFolderProvider.getInstance().setCacheFolder(folder);
    }

    private CacheFolder() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }
}
