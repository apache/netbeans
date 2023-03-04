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

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @author Tomas Zezula
 */
public final class ProxyBinaryIndexerFactory extends BinaryIndexerFactory {

    private static final String ATTR_DELEGATE = "delegate";     //NOI18N
    private static final String ATTR_INDEXER_NAME = "name";     //NOI18N
    private static final String ATTR_INDEXER_VERSION = "version";   //NOI18N
    private static final String ATTR_REQUIRED_RES = "requiredResource"; //NOI18N
    private static final String ATTR_REQUIRED_MIME = "mimeType";    //NOI18N
    private static final String ATTR_NAME_PATTERN = "namePattern";    //NOI18N
    private static final String PROP_CHECKED = "ProxyBinaryIndexerFactory_checked";  //NOI18N
    private static final String PROP_MATCHED_FILES = "ProxyBinaryIndexerFactory_matchedFiles";  //NOI18N
    private static final String MIME_UNKNOWN = "content/unknown";   //NOI18N
    private static final int USED = 1;

    private final Map<String,Object> params;
    private final String indexerName;
    private final int indexerVersion;
    @org.netbeans.api.annotations.common.SuppressWarnings(
            value="DMI_COLLECTION_OF_URLS",
            justification="URLs have never host part")    //NOI18N
    private final Set<URL> activeRoots;

    public ProxyBinaryIndexerFactory(
            @NonNull final Map<String,Object> params) {
        Parameters.notNull("params", params);       //NOI18N
        this.params = params;
        indexerName = (String)params.get(ATTR_INDEXER_NAME);
        Parameters.notNull("indexerName", indexerName);     //NOI18N
        Integer iv = (Integer)params.get(ATTR_INDEXER_VERSION);
        Parameters.notNull("version", iv);                  //NOI18N
        indexerVersion = iv;
        activeRoots = new HashSet<URL>();
    }

    @Override
    public String getIndexerName() {
        return indexerName;
    }

    @Override
    public int getIndexVersion() {
        return indexerVersion;
    }

    @Override
    public BinaryIndexer createIndexer() {
        return new Indexer();
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part. Already verified by PathRegistry and RepositoryUpdater.")
    public void rootsRemoved(Iterable<? extends URL> removedRoots) {
        final Set<URL> filtered = new HashSet<URL>();
        for (URL removedRoot : removedRoots) {
            if (activeRoots.remove(removedRoot)) {
                filtered.add(removedRoot);
            }
        }
        if (!filtered.isEmpty()) {
            SPIAccessor.getInstance().rootsRemoved(
                    getDelegate(),
                    Collections.unmodifiableSet(filtered));
        }
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part. Already verified by PathRegistry and RepositoryUpdater.")
    public boolean scanStarted (final Context context) {
        if (supports(context) != null) {
            return SPIAccessor.getInstance().scanStarted(getDelegate(),context);
        }
        boolean vote = true;
        if (isUpToDate(context) && ArchiveTimeStamps.getIndexerState(context) == USED) {
            vote = SPIAccessor.getInstance().scanStarted(getDelegate(),context);
            if (!vote) {
                SPIAccessor.getInstance().putProperty(context, PROP_CHECKED, Boolean.FALSE);
            } else {
                activeRoots.add(context.getRootURI());
            }
        }
        return vote;
     }

    @Override
    public void scanFinished (final Context context) {
        if (supports(context) != null) {
            SPIAccessor.getInstance().scanFinished(getDelegate(),context);
        }
    }


    @CheckForNull
    private Map<String,? extends Iterable<? extends FileObject>> supports(
            @NonNull final Context ctx) {
        final FileObject root = ctx.getRoot();
        if (root == null) {
            return null;
        }
        if (Boolean.TRUE == SPIAccessor.getInstance().getProperty(ctx, PROP_CHECKED)) {
            return (Map<String,? extends Iterable<? extends FileObject>>) SPIAccessor.getInstance().getProperty(ctx, PROP_MATCHED_FILES);
        }
        final Map<String,Queue<FileObject>> matchedFiles = new HashMap<String, Queue<FileObject>>();

        String s = (String) params.get(ATTR_REQUIRED_RES);
        final String[] requiredResources = s == null ? null : s.split(","); //NOI18N
        s = (String) params.get(ATTR_REQUIRED_MIME);
        final String[] mimeTypes = s == null ? null : s.split(",");         //NOI18N
        s = (String) params.get(ATTR_NAME_PATTERN);
        final Pattern pattern = s == null ? null : Pattern.compile(s);
        try {
            if (isUpToDate(ctx)) {
                return null;
            }
            if (requiredResources != null) {
                boolean found = false;
                for (String r : requiredResources) {
                    if (root.getFileObject(r) != null) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return null;
                }
            }
            if (mimeTypes != null || pattern != null) {
                final Enumeration<? extends FileObject> fos = root.getChildren(true);
                final Set<String> mimeTypesSet = mimeTypes == null ?
                        null :
                        new HashSet<String>(Arrays.asList(mimeTypes));
                while (fos.hasMoreElements()) {
                    final FileObject f = fos.nextElement();
                    if (pattern != null) {
                        final String name = f.getNameExt();
                        if (!pattern.matcher(name).matches()) {
                            continue;
                        }
                    }
                    String mt = MIME_UNKNOWN;
                    if (mimeTypes != null) {
                        mt = f.getMIMEType(mimeTypes);
                        if (!mimeTypesSet.contains(mt)) {
                            continue;
                        }
                    }
                    Queue<FileObject> q = matchedFiles.get(mt);
                    if (q == null) {
                        q = new ArrayDeque<FileObject>();
                        matchedFiles.put(mt, q);
                    }
                    q.offer(f);
                }
                if (matchedFiles.isEmpty()) {
                    return null;
                }
            }
            final Map<String, ? extends Iterable<? extends FileObject>> res = Collections.unmodifiableMap(matchedFiles);
            SPIAccessor.getInstance().putProperty(ctx, PROP_MATCHED_FILES, res);
            return res;
        } finally {
            SPIAccessor.getInstance().putProperty(ctx, PROP_CHECKED, Boolean.TRUE);
        }
    }

    @NonNull
    private ConstrainedBinaryIndexer getDelegate() {
        final Object delegate = params.get(ATTR_DELEGATE);
        if (!(delegate instanceof ConstrainedBinaryIndexer)) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid indexer %s registered as %s %d",   //NOI18N
                    delegate,
                    indexerName,
                    indexerVersion));
        }
        return (ConstrainedBinaryIndexer) delegate;
    }

    private static boolean isUpToDate(@NonNull final Context ctx) {
        return !ctx.isAllFilesIndexing();
    }

    private final class Indexer extends BinaryIndexer {

        @Override
        @org.netbeans.api.annotations.common.SuppressWarnings(
        value="DMI_COLLECTION_OF_URLS",
        justification="URLs have never host part. Already verified by PathRegistry and RepositoryUpdater.")
        protected void index(@NonNull final Context context) {
            final Map<String,? extends Iterable<? extends FileObject>> matchedFiles = supports(context);
            if (matchedFiles != null) {
                activeRoots.add(context.getRootURI());
                SPIAccessor.getInstance().index(
                    getDelegate(),
                    matchedFiles,
                    context);
                ArchiveTimeStamps.setIndexerState(context, USED);
            }
        }
    }
}
