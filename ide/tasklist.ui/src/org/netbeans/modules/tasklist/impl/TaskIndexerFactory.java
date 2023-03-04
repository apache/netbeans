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

package org.netbeans.modules.tasklist.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author S. Aubrecht
 */
public class TaskIndexerFactory extends CustomIndexerFactory {

    static final String INDEXER_NAME = "TaskListIndexer"; //NOI18N
    static final int INDEXER_VERSION = 2;

    public TaskIndexerFactory() {
    }

    @Override
    public String getIndexerName() {
        return INDEXER_NAME;
    }

    @Override
    public int getIndexVersion() {
        return INDEXER_VERSION;
    }

    @Override
    public CustomIndexer createIndexer() {
        return new TaskIndexer( TaskManagerImpl.getInstance().getTasks() );
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for( Indexable idx : deleted ) {
                is.removeDocuments(idx);
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
        TaskManagerImpl tm = TaskManagerImpl.getInstance();
        tm.getTasks().clearDeletedFiles();
    }

    @Override
    public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        TaskManagerImpl manager = TaskManagerImpl.getInstance();
        boolean refresh = false;
        for (Iterator it = removedRoots.iterator(); it.hasNext();) {
            URL url = (URL) it.next();
            final FileObject root = FileUtil.toFileObject(FileUtil.normalizeFile(new File(url.getFile())));
            if (manager.getScope().isInScope(root)) {
                refresh = true;
                break;
            }
        }
        if (refresh) {
            manager.refresh(manager.getScope());
        }
    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for( Indexable idx : dirty ) {
                is.markDirtyDocuments(idx);
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean supportsEmbeddedIndexers() {
        return true;
    }

    @Override
    public boolean scanStarted(Context context) {
        try {
            return IndexingSupport.getInstance(context).isValid();
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
}
