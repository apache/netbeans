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

import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
final class FileObjectCrawler extends Crawler {

    private static final char SEPARATOR = '/';  //NOI18N
    private static final Logger LOG = Logger.getLogger(FileObjectCrawler.class.getName());
    /*test*/ static Map<Pair<FileObject,FileObject>,Boolean> mockLinkTypes;

    private final FileObject root;
    private final ClassPath.Entry entry;
    private final FileObject[] files;
    

    FileObjectCrawler(
            @NonNull final FileObject root,
            final Set<? extends TimeStampAction> checkTimeStamps,
            @NullAllowed final ClassPath.Entry entry,
            @NonNull final CancelRequest cancelRequest,
            @NonNull final SuspendStatus suspendStatus) throws IOException {
        super (root.toURL(), checkTimeStamps, true, true, cancelRequest, suspendStatus);
        this.root = root;
        this.entry = entry;
        this.files = null;
    }

    FileObjectCrawler(
            @NonNull final FileObject root,
            @NullAllowed final FileObject[] files,
            final Set<? extends TimeStampAction> checkTimeStamps,
            @NullAllowed final ClassPath.Entry entry,
            @NonNull final CancelRequest cancelRequest,
            @NonNull final SuspendStatus suspendStatus) throws IOException {
        super (root.toURL(), checkTimeStamps, false, supportsAllFiles(root, files), cancelRequest, suspendStatus);
        this.root = root;
        this.entry = entry;
        this.files = files;
    }

    @Override
    protected boolean collectResources(Collection<Indexable> resources, Collection<Indexable> allResources) {
        boolean finished = true;
        final long tm1 = System.currentTimeMillis();
        final Stats stats = LOG.isLoggable(Level.FINE) ? new Stats() : null;

        if (files != null) {
            if (files.length > 1) {
                final Map<FileObject, Set<FileObject>> clusters = new HashMap<>();
                final Map<FileObject, StringBuilder> relPaths = new HashMap<>();
NEXT_FILE:      for(FileObject f : files) {
                    FileObject parent = f.getParent();
                    Set<FileObject> cluster = clusters.get(parent);
                    if (cluster == null) {
                        StringBuilder currentRelPath = getRelativePath(root, parent);
                        for (Iterator<Map.Entry<FileObject,StringBuilder>> it = relPaths.entrySet().iterator(); it.hasNext();) {
                            final Map.Entry<FileObject, StringBuilder> relPath = it.next();
                            switch (getFileRelation(currentRelPath, relPath.getValue())) {
                                case FIRST_IN_SECOND:
                                    final Set<FileObject> cs = clusters.get(relPath.getKey());
                                    for (FileObject csFile : cs) {
                                        if (csFile.isFolder() && FileUtil.isParentOf(csFile, f)) {
                                            continue NEXT_FILE;
                                        }
                                    }
                                    break;
                                case SECOND_IN_FIRST:
                                    if (f.equals(relPath.getKey()) || FileUtil.isParentOf(f, relPath.getKey())) {
                                        clusters.remove(relPath.getKey());
                                        it.remove();
                                    }
                                    break;
                                case UNRELATED:
                                    break;
                                case EQUAL:
                                    //Invalid parent file?
                                    final boolean oldValid = relPath.getKey().isValid();
                                    final boolean newValid = parent.isValid();
                                    if (!oldValid) {
                                        final Set<FileObject> invalidSet = clusters.remove(relPath.getKey());
                                        it.remove();
                                        if (newValid) {
                                            cluster = new HashSet<>();
                                            clusters.put(parent, cluster);
                                            relPaths.put(parent, currentRelPath);
                                            for (FileObject ifo : invalidSet) {
                                                final FileObject rfo = parent.getFileObject(ifo.getNameExt());
                                                if (rfo != null) {
                                                    cluster.add(rfo);
                                                }
                                            }
                                            updateRelPaths(relPaths, clusters, f);
                                            cluster.add(f);
                                        }
                                        continue NEXT_FILE;
                                    }
                                    if (!newValid) {
                                        cluster = clusters.get(relPath.getKey());
                                        f = relPath.getKey().getFileObject(f.getNameExt());
                                        if (f != null) {
                                            updateRelPaths(relPaths, clusters, f);
                                            cluster.add(f);
                                        }
                                        continue NEXT_FILE;
                                    }
                                default:
                                    throw new IllegalStateException(String.format(
                                        "clusters: %s, relPaths: %s, file: %s, parent: %s, currentRelPath: %s",  //NOI18N
                                        clusters,
                                        relPaths,
                                        f,
                                        parent,
                                        currentRelPath
                                    ));
                            }
                        }
                        cluster = new HashSet<>();
                        clusters.put(parent, cluster);
                        relPaths.put(parent, currentRelPath);
                    } else {
                        updateRelPaths(relPaths, clusters, f);
                    }
                    cluster.add(f);
                }
                for(FileObject parent : clusters.keySet()) {
                    Set<FileObject> cluster = clusters.get(parent);
                    StringBuilder relativePath = relPaths.get(parent);
                    if (relativePath != null) {
                        finished = collect(
                                cluster.toArray(new FileObject[cluster.size()]),
                                root,
                                resources,
                                allResources,
                                stats,
                                entry,
                                createPathForRoot(root),
                                relativePath);
                        if (!finished) {
                            break;
                        }
                    } // else invalid (eg. deleted) FileObject encountered
                }
            } else if (files.length == 1) {
                StringBuilder relativePath = getRelativePath(root, files[0].getParent());
                if (relativePath != null) {
                    finished = collect(
                        files,
                        root,
                        resources,
                        allResources,
                        stats,
                        entry,
                        createPathForRoot(root),
                        relativePath);
                } // else invalid (eg. deleted) FileObject encountered
            }
        } else {
            finished = collect(
                root.getChildren(),
                root,
                resources,
                allResources,
                stats,
                entry,
                createPathForRoot(root),
                new StringBuilder());
        }

        final long tm2 = System.currentTimeMillis();
        if (LOG.isLoggable(Level.FINE)) {
            final String rootUrl = root.toURL().toString();

            LOG.log(Level.FINE, String.format("Up-to-date check of %d files under %s took %d ms", stats.filesCount, rootUrl, tm2 - tm1 )); //NOI18N

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "File extensions histogram for {0}:", rootUrl);
                Stats.logHistogram(Level.FINER, stats.extensions);
                LOG.finer("----");

// mimetypes histogram is no longer available after crawling the files
//                LOG.log(Level.FINER, "Mime types histogram for {0}:", rootUrl);
//                Stats.logHistogram(Level.FINER, stats.mimeTypes);
//                LOG.finer("----");
            }
            LOG.log(Level.FINE,
                    "Symlink tests took {0}ms, {1} symlinks into root found.",    //NOI18N
                    new Object[] {
                        stats.linkCheckTime,
                        stats.linkCount
                    });
        }

        return finished;
    }

    private void updateRelPaths(
        @NonNull final Map<FileObject,StringBuilder> relPaths,
        @NonNull final Map<FileObject,Set<FileObject>> clusters,
        @NonNull final FileObject newFile) {
        for (Iterator<Map.Entry<FileObject,StringBuilder>> it = relPaths.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<FileObject, StringBuilder> relPath = it.next();
            if (newFile.equals(relPath.getKey()) || FileUtil.isParentOf(newFile, relPath.getKey())) {
                clusters.remove(relPath.getKey());
                it.remove();
            }
        }
    }

    private boolean collect (
            final @NonNull FileObject[] fos,
            final @NonNull FileObject root,
            final @NonNull Collection<Indexable> resources,
            final @NonNull Collection<Indexable> allResources,
            final @NullAllowed Stats stats,
            final @NullAllowed ClassPath.Entry entry,
            final @NonNull Deque<FileObject> path,
            final @NonNull StringBuilder relativePathBuilder)
    {
        parkWhileSuspended();
        int parentPathEnd = relativePathBuilder.length();

        for (FileObject fo : fos) {
            //keep the same logic like in RepositoryUpdater
            if (isCancelled()) {
                return false;
            }
            if (!fo.isValid() || !isVisible(fo)) {
                continue;
            }

            relativePathBuilder.append(fo.getNameExt());
            boolean folder = fo.isFolder();
            if (folder) relativePathBuilder.append(SEPARATOR);
            String relativePath = relativePathBuilder.toString();
            try {
                if (entry != null && !entry.includes(relativePath)) {
                    continue;
                }
                if (folder) {
                    if (!isLink(fo, path, stats)) {
                        path.addLast(fo);
                        try {
                            if (!collect(fo.getChildren(), root, resources, allResources, stats, entry, path, relativePathBuilder)) {
                                return false;
                            }
                        } finally {
                            FileObject last = path.removeLast();
                            assert last == fo;
                        }
                    }
                } else {
                    if (stats != null) {
                        stats.filesCount++;
                        Stats.inc(stats.extensions, fo.getExt());
                    }

                    Indexable indexable = createIndexable(new FileObjectIndexable(root, relativePath));
                    allResources.add(indexable);
                    if (!isUpToDate(fo, relativePath)) {
                        resources.add(indexable);
                    }
                }
            } finally {
                relativePathBuilder.delete(parentPathEnd, relativePathBuilder.length());
            }
        }

        return true;
    }

    private StringBuilder getRelativePath(FileObject folder, FileObject fo) {
        String rp = FileUtil.getRelativePath(folder, fo);
        if (rp != null) {
            StringBuilder relativePath = new StringBuilder(rp);
            if (relativePath.length() > 0) {
                relativePath.append(SEPARATOR);
            }
            return relativePath;
        } else {
            return null;
        }
    }

    private boolean isVisible (final @NonNull FileObject fo) {
        try {
            return VisibilityQuery.getDefault().isVisible(fo);
        } finally {
            setListenOnVisibility(true);
        }
    }
    
    //Todo: Not exaclty correct. The correct implementation should find if whole root content
    //is covered by files. But correct implementation will be very very slow and probably no one
    //calls it with such params.
    private static boolean supportsAllFiles(final FileObject root, final FileObject... files) {
        for (FileObject file : files) {
            if (root == file) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSameFile(
            @NonNull final FileObject check,
            @NonNull final FileObject other) throws IOException {
        if (check.isSymbolicLink()) {
            FileObject otherReslved = other.getCanonicalFileObject();
            if (otherReslved == null) {
                otherReslved = other;
            }
            return otherReslved.equals(check.getCanonicalFileObject());
        }
        return false;
    }

    private static boolean isLink(
        @NonNull final FileObject file,
        @NonNull final Deque<? extends FileObject> path,
        @NullAllowed final Stats stats) {
        final long st = System.currentTimeMillis();
        boolean hasLink = false;
        try {
            final Iterator<? extends FileObject> it = path.descendingIterator();
            while (it.hasNext()) {
                final FileObject pathElement = it.next();
                if (file.getNameExt().equals(pathElement.getNameExt())) {
                    try {
                        if (mockLinkTypes != null ?
                            mockLinkTypes.get(Pair.<FileObject,FileObject>of(pathElement, file)) :
                            isSameFile(file, pathElement)) {
                            hasLink = true;
                            break;
                        }
                    } catch (IOException ioe) {
                        LOG.log(
                            Level.INFO,
                            "Cannot convert to cannonical files {0} and {1}",   //NOI18N
                            new Object[]{
                                file,
                                pathElement
                            });
                        LOG.log(
                            Level.FINE,
                            null,
                            ioe);
                        break;
                    }
                }
            }
            return hasLink;
        } finally {
            long et = System.currentTimeMillis();
            if (stats != null) {
                stats.linkCheckTime+= (et-st);
                if (hasLink) {
                    stats.linkCount++;
                }
            }
        }
    }

    private static Deque<FileObject> createPathForRoot(@NonNull FileObject root) {
        final Deque<FileObject> result = new ArrayDeque<>();
        while (root != null) {
            result.addFirst(root);
            root = root.getParent();
        }
        return result;
    }

    @NonNull
    private static PathRelation getFileRelation (
            @NonNull final StringBuilder firstPath,
            @NonNull final StringBuilder secondPath) {
        final int min = Math.min(firstPath.length(),secondPath.length());
        for (int i=0; i<min; i++) {
            if (firstPath.charAt(i) != secondPath.charAt(i)) {
                return PathRelation.UNRELATED;
            }
        }
        if (firstPath.length() > secondPath.length()) {
            assert secondPath.length() == 0 || secondPath.charAt(secondPath.length()-1) == SEPARATOR;
            return PathRelation.FIRST_IN_SECOND;
        } else if (firstPath.length() < secondPath.length()) {
            assert firstPath.length() == 0 || firstPath.charAt(firstPath.length()-1) == SEPARATOR;
            return PathRelation.SECOND_IN_FIRST;
        } else {
            return PathRelation.EQUAL;
        }
    }
    
    private static final class Stats {
        public int filesCount;
        public long linkCheckTime;
        public int linkCount;
        public Map<String, Integer> extensions = new HashMap<String, Integer>();
        public Map<String, Integer> mimeTypes = new HashMap<String, Integer>();
        public static void inc(Map<String, Integer> m, String k) {
            Integer i = m.get(k);
            if (i == null) {
                m.put(k, 1);
            } else {
                m.put(k, i.intValue() + 1);
            }
        }
        public static void logHistogram(Level level, Map<String, Integer> data) {
            Map<Integer, Set<String>> sortedMap = new TreeMap<Integer, Set<String>>(REVERSE);
            for(String item : data.keySet()) {
                Integer freq = data.get(item);
                Set<String> items = sortedMap.get(freq);
                if (items == null) {
                    items = new TreeSet<String>();
                    sortedMap.put(freq, items);
                }
                items.add(item);
            }
            for(Integer freq : sortedMap.keySet()) {
                for(String item : sortedMap.get(freq)) {
                    LOG.log(level, "{0}: {1}", new Object [] { item, freq }); //NOI18N
                }
            }
        }
        private static final Comparator<Integer> REVERSE = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return -1 * o1.compareTo(o2);
            }
        };
    } // End of Stats class

    private enum PathRelation {
        UNRELATED,
        EQUAL,
        FIRST_IN_SECOND,
        SECOND_IN_FIRST
    }
}
