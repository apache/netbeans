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

package org.netbeans.modules.jumpto.file;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author vita
 */
public final class FileIndexer extends CustomIndexer {

    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------

    public static final String ID = "org-netbeans-modules-jumpto-file-FileIndexer"; //NOI18N
    public static final int VERSION = 2;

    public static final String FIELD_NAME = "file-name"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_NAME = "ci-file-name"; //NOI18N
    public static final String FIELD_RELATIVE_PATH = "relative-path"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_RELATIVE_PATH = "ci-relative-path"; //NOI18N
    public static final String FIELD_FULL_PATH = "full-path"; //NOI18N
    public static final String FIELD_CASE_INSENSITIVE_FULL_PATH = "ci-full-path"; //NOI18N

    // used from the XML layer
    public static final class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new FileIndexer();
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.FINEST, "removed {0}/{1}", new Object[]{i.getURL(), i.getRelativePath()});
                    }
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            // no need to do anything, we are not indexing anythong from inside of the file
        }

        @Override
        public boolean scanStarted (final Context ctx) {
            try {
                final IndexingSupport is = IndexingSupport.getInstance(ctx);
                return is.isValid();
            } catch (final IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return false;
            }
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {

        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public String getIndexerName() {
            return ID;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    } // End of CustomIndexerFactory class

    // -----------------------------------------------------------------------
    // CustomIndexer implementation
    // -----------------------------------------------------------------------

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        try {
            long tm1 = System.currentTimeMillis();
            int cnt = 0;
            IndexingSupport is = IndexingSupport.getInstance(context);
            for(Indexable i : files) {
                if (context.isCancelled()) {
                    LOG.fine("Indexer cancelled"); //NOI18N
                    break;
                }
                cnt++;
                String nameExt = getNameExt(i);
                FileObject root = context.getRoot();
                final String rootPath = root != null ? root.getPath() : null;
                if (nameExt.length() > 0) {
                    IndexDocument d = is.createDocument(i);
                    d.addPair(FIELD_NAME, nameExt, true, true);
                    d.addPair(FIELD_CASE_INSENSITIVE_NAME, nameExt.toLowerCase(Locale.ENGLISH), true, true);

                    final String relativePath = i.getRelativePath();

                    d.addPair(FIELD_RELATIVE_PATH, relativePath, true, true);
                    d.addPair(FIELD_CASE_INSENSITIVE_RELATIVE_PATH, relativePath.toLowerCase(Locale.ENGLISH), true, true);

                    if (rootPath != null) {
                        String fullPath = rootPath + "/" + i.getRelativePath();
                        d.addPair(FIELD_FULL_PATH, fullPath, true, true);
                        d.addPair(FIELD_CASE_INSENSITIVE_FULL_PATH, fullPath.toLowerCase(Locale.ENGLISH), true, true);
                    }
                    is.addDocument(d);
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.FINEST, "added {0}/{1}", new Object[]{i.getURL(), i.getRelativePath()});
                    }
                }
            }
            long tm2 = System.currentTimeMillis();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Processed {0} files in {1}ms.", new Object[]{cnt, tm2 - tm1}); //NOI18N
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(FileIndexer.class.getName());

    private static String getNameExt(Indexable i) {
        String path = i.getRelativePath();
        int lastSlash = path.lastIndexOf('/'); //NOI18N
        if (lastSlash != -1) {
            return path.substring(lastSlash + 1);
        } else {
            return i.getRelativePath();
        }
    }
}
