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

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction;
import org.netbeans.modules.java.source.usages.BuildArtifactMapperImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexerCache;
import org.netbeans.modules.parsing.impl.indexing.IndexerCache.IndexerInfo;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;

/**
 *
 * @author lahvac
 */
public class COSSynchronizingIndexer extends CustomIndexer {
    
    private static final Logger LOG = Logger.getLogger(COSSynchronizingIndexer.class.getName());

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        final URL rootURL = context.getRootURI();
        if (FileUtil.getArchiveFile(rootURL) != null) {
            return;
        }
        if (!BuildArtifactMapperImpl.isUpdateResources(rootURL)) {
            return ;
        }

        Set<String> javaMimeTypes = gatherJavaMimeTypes();
        List<File> updated = new LinkedList<File>();
        final ClassPath srcPath = ClassPath.getClassPath(context.getRoot(), ClassPath.SOURCE);
        if (srcPath == null) {
            LOG.log(
                    Level.INFO,
                    "No source path for: {0}",
                    FileUtil.getFileDisplayName(context.getRoot()));
            return ;
        }
        for (Indexable i : files) {
            if (javaMimeTypes.contains(i.getMimeType()))
                continue;
            
            try {
                URL url = i.getURL();

                if (url == null) {
                    //#174026: presumably a deleted file:
                    continue;
                }
                final FileObject resource = srcPath.findResource(i.getRelativePath());
                if (resource == null) {
                    LOG.log(
                        Level.INFO,
                        "File {0} not on source path {1}, root {2}",    //NOI18N
                        new Object[]{
                            i.getURL(),
                            srcPath,
                            context.getRoot()
                        });
                } else if (FileUtil.isParentOf(context.getRoot(), resource)) {
                    updated.add(BaseUtilities.toFile(url.toURI()));
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        try {
            File sourceRootFile = BaseUtilities.toFile(context.getRootURI().toURI());

            if (!context.checkForEditorModifications()) { // #187514, see also #152222 and JavaCustomIndexer
                BuildArtifactMapperImpl.classCacheUpdated(
                        context.getRootURI(),
                        sourceRootFile,
                        Collections.<File>emptyList(),
                        updated,
                        true,
                        context.isAllFilesIndexing());
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Set<String> gatherJavaMimeTypes() {
        Set<String> mimeTypes = new HashSet<String>();

        final Collection<? extends IndexerInfo<CustomIndexerFactory>> indexers =
            IndexerCache.getCifCache().getIndexersByName(JavaIndex.NAME);
        if (indexers != null) {
            for (IndexerInfo<CustomIndexerFactory> i : indexers) {
                mimeTypes.addAll(i.getMimeTypes());
            }
        } else {
            LOG.warning("No java indexer found.");  //NOI18N
        }
        return mimeTypes;
    }

    public static final class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new COSSynchronizingIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            final File target = CompileOnSaveAction.Context.getTarget(context.getRootURI());
            if (target == null) {
                return;
            }            
            if (!BuildArtifactMapperImpl.isUpdateClasses(context.getRootURI())) {
                return ;
            }

            List<File> deletedFiles = new LinkedList<File>();

            for (Indexable d : deleted) {
                try {
                    deletedFiles.add(BaseUtilities.toFile(d.getURL().toURI()));
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            try {
                File sourceRootFile = BaseUtilities.toFile(context.getRootURI().toURI());

                BuildArtifactMapperImpl.classCacheUpdated(
                        context.getRootURI(),
                        sourceRootFile,
                        deletedFiles,
                        Collections.<File>emptyList(),
                        true,
                        context.isAllFilesIndexing());
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {}

        @Override
        public String getIndexerName() {
            return COSSynchronizingIndexer.class.getName();
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }

    }

}
