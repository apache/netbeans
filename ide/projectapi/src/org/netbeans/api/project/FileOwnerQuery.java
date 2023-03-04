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

package org.netbeans.api.project;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Find the project which owns a file.
 * <p>
 * There is a default implementation of {@link org.netbeans.spi.project.FileOwnerQueryImplementation}
 * which considers a file owned by the project corresponding to the nearest enclosing
 * project directory or marked external owner, if such a directory exists. But
 * other implementations can be registered to lookup as well.
 * <p>
 * Warning: This class and it's methods may not be used within DataObject recognition in DataLoaders.
 * eg. in {@link org.openide.loaders.MultiFileLoader#findPrimaryFile}
 *  
 * @author Jesse Glick
 */
public class FileOwnerQuery {
    
    // XXX acquire the appropriate ProjectManager.mutex for the duration of calls

    private static final Logger LOG = Logger.getLogger(FileOwnerQuery.class.getName());
    private static final Lookup.Result<FileOwnerQueryImplementation> implementations;
    static {
        implementations = Lookup.getDefault().lookupResult(FileOwnerQueryImplementation.class);
        implementations.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged (LookupEvent ev) {
                synchronized (FileOwnerQuery.class) {
                    cache = null;
                    changeId++;
                }
            }});
    }

    /** Cache of all available FileOwnerQueryImplementation instances. */
    //@GuardedBy("FileOwnerQuery.class")
    private static List<FileOwnerQueryImplementation> cache;
    //@GuardedBy("FileOwnerQuery.class")
    private static long changeId;
    
    private FileOwnerQuery() {}

    /**
     * Find the project, if any, which "owns" the given file.
     * @param file the file (generally on disk)
     * @return a project which contains it, or null if there is no known project containing it
     */
    public static Project getOwner(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Passed null to FileOwnerQuery.getOwner(FileObject)"); // NOI18N
        }
        FileObject archiveRoot = FileUtil.getArchiveFile(file);
        if (archiveRoot != null) {
            file = archiveRoot;
        }
        for (FileOwnerQueryImplementation q : getInstances()) {
            Project p = q.getOwner(file);
            if (p != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "getOwner({0}) -> {1} @{2} from {3}", new Object[] {file, p, p.hashCode(), q});
                }
                return p == UNOWNED ? null : p;
            }
        }
        LOG.log(Level.FINE, "getOwner({0}) -> nil", file);
        return null;
    }

    /**
     * Find the project, if any, which "owns" the given URI.
     * @param uri the URI to the file (generally on disk); must be absolute and not opaque (though {@code jar}-protocol URIs are unwrapped as a convenience)
     * @return a project which contains it, or null if there is no known project containing it
     * @throws IllegalArgumentException if the URI is relative or opaque
     */
    public static Project getOwner(URI uri) {
        try {
            URL url = uri.toURL();
            if (FileUtil.isArchiveArtifact(url)) {
                url = FileUtil.getArchiveFile(url);
                if (url != null) {
                    uri = url.toURI();
                }
            }
        } catch (MalformedURLException | URISyntaxException e) {
            LOG.log(Level.INFO, null, e);
        }
        if (!uri.isAbsolute() || uri.isOpaque()) {
            throw new IllegalArgumentException("Bad URI: " + uri); // NOI18N
        }
        for (FileOwnerQueryImplementation q : getInstances()) {
            Project p = q.getOwner(uri);
            if (p != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "getOwner({0}) -> {1} from {2}", new Object[] {uri, p, q});
                }
                return p == UNOWNED ? null : p;
            }
        }
        LOG.log(Level.FINE, "getOwner({0}) -> nil", uri);
        return null;
    }
    
    /**
     * Intended for use from unit tests. Clears internal state such as
     * external file owners.
     */
    static void reset() {
        SimpleFileOwnerQueryImplementation.reset();
    }
    
    /**
     * Pseudoproject indicating just that a directory is definitely unowned. May
     * be returned by either {@code getOwner} overload of
     * {@link FileOwnerQueryImplementation}, in which case null is returned from
     * either {@code getOwner} overload here. May also be passed to either
     * {@code markExternalOwner} overload, in which case the standard directory
     * search will be pruned at this point with no result.
     *
     * @since 1.46
     */
    public static final Project UNOWNED = new Project() {
        @Override
        public FileObject getProjectDirectory() {
            return FileUtil.createMemoryFileSystem().getRoot();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        @Override public String toString() {
            return "UNOWNED";
        }
    };
    
    /**
     * Simplest algorithm for marking external file owners, which just keeps
     * a cache of this information.
     * <strong>The external marking may be persisted across VM sessions, despite the name.</strong>
     */
    public static final int EXTERNAL_ALGORITHM_TRANSIENT = 0;
    
    /**
     * Mark an external folder or file as being owned by a particular project.
     * After this call is made, for the duration appropriate to the selected
     * algorithm, that folder or file and its ancestors will be considered owned
     * by the project (if any) matching the named project directory, except in
     * the case that a lower enclosing project directory can be found.
     * <p class="nonnormative">
     * Typical usage would be to call this method for each external source root
     * of a project (if any) as soon as the project is loaded, if a transient
     * algorithm is selected, or only when the project is created, if a reliable
     * persistent algorithm is selected.
     * </p>
     * @param root a folder or a file which should be considered part of a project
     * @param owner a project which should be considered to own that folder tree
     *              (any prior marked external owner is overridden),
     *              or null to cancel external ownership for this folder root
     *              or {@link #UNOWNED} if the directory is known definitely to be unowned
     * @param algorithm an algorithm to use for retaining this information;
     *                  currently may only be {@link #EXTERNAL_ALGORITHM_TRANSIENT}
     * @throws IllegalArgumentException if the root or owner is null, if an unsupported
     *                                  algorithm is requested,
     *                                  if the root is already a project directory,
     *                                  or if the root is already equal to or inside the owner's
     *                                  project directory (it may however be an ancestor)
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
     */
    public static void markExternalOwner(FileObject root, Project owner, int algorithm) throws IllegalArgumentException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "markExternalOwner({0}, {1}, {2})", new Object[] {root, owner, algorithm});
        }
        switch (algorithm) {
        case EXTERNAL_ALGORITHM_TRANSIENT:
            // XXX check args
            SimpleFileOwnerQueryImplementation.markExternalOwnerTransient(root, owner);
            break;
        default:
            throw new IllegalArgumentException("No such algorithm: " + algorithm); // NOI18N
        }
    }
    
    /**
     * Mark an external URI (folder or file) as being owned by a particular project.
     * After this call is made, for the duration appropriate to the selected
     * algorithm, that folder or file and its ancestors will be considered owned
     * by the project (if any) matching the named project directory, except in
     * the case that a lower enclosing project directory can be found.
     * <p class="nonnormative">
     * Typical usage would be to call this method for each external source root
     * of a project (if any) as soon as the project is loaded, if a transient
     * algorithm is selected, or only when the project is created, if a reliable
     * persistent algorithm is selected.
     * </p>
     * @param root an URI of a folder or a file which should be considered part of a project
     * @param owner a project which should be considered to own that folder tree
     *              (any prior marked external owner is overridden),
     *              or null to cancel external ownership for this folder root
     * @param algorithm an algorithm to use for retaining this information;
     *                  currently may only be {@link #EXTERNAL_ALGORITHM_TRANSIENT}
     * @throws IllegalArgumentException if the root or owner is null, if an unsupported
     *                                  algorithm is requested,
     *                                  if the root is already a project directory,
     *                                  or if the root is already equal to or inside the owner's
     *                                  project directory (it may however be an ancestor)
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/SourcesHelper.html"><code>SourcesHelper</code></a>
     */
    public static void markExternalOwner(URI root, Project owner, int algorithm) throws IllegalArgumentException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "markExternalOwner({0}, {1}, {2})", new Object[] {root, owner, algorithm});
        }
        switch (algorithm) {
        case EXTERNAL_ALGORITHM_TRANSIENT:
            // XXX check args
            SimpleFileOwnerQueryImplementation.markExternalOwnerTransient(root, owner);
            break;
        default:
            throw new IllegalArgumentException("No such algorithm: " + algorithm); // NOI18N
        }
    }
    
    /* TBD whether this is necessary:
    public static FileObject getMarkedExternalOwner(FileObject root) {}
     */

    private static List<FileOwnerQueryImplementation> getInstances() {
        List<FileOwnerQueryImplementation> res;
        long currentId;
        synchronized (FileOwnerQuery.class) {
            res = cache;
            currentId = changeId;
        }
        if (res != null) {
            return res;
        }
        res = new ArrayList<FileOwnerQueryImplementation>(implementations.allInstances());
        synchronized (FileOwnerQuery.class) {
            if (currentId == changeId) {
                cache = res;
            } else if (cache != null) {
                res = cache;
            }
            return res;
        }
        
    }
    
}
