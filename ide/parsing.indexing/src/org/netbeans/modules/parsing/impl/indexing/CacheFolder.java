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
