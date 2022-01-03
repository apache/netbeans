/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class CndIndexer extends CustomIndexer {
    
    /*package*/ interface Delegate {
        void index(FileObject file);
        void removed(FileObject root);
    }
    
    private static volatile Delegate delegate;
    
    /*package*/ static void setDelegate(Delegate d) {
        delegate = d;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        if (!CndTraceFlags.USE_INDEXING_API) {
            return;
        }
        // for now we're not interested in such events (project open for example)
        if (context.isAllFilesIndexing()) {
            return;
        }
        FileObject root = context.getRoot();
        for (Indexable idx : files) {
            final FileObject fo = root.getFileObject(idx.getRelativePath());
            if (delegate != null) {
                delegate.index(fo);
            }
        }
    }

    public static final String NAME = "cnd"; //NOI18N

    @MimeRegistrations({
        @MimeRegistration(mimeType = MIMENames.C_MIME_TYPE, service = CustomIndexerFactory.class),
        @MimeRegistration(mimeType = MIMENames.CPLUSPLUS_MIME_TYPE, service = CustomIndexerFactory.class),
        @MimeRegistration(mimeType = MIMENames.HEADER_MIME_TYPE, service = CustomIndexerFactory.class),
        @MimeRegistration(mimeType = MIMENames.FORTRAN_MIME_TYPE, service = CustomIndexerFactory.class)
    })
    public static final class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new CndIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> files, Context context) {
            if (!CndTraceFlags.USE_INDEXING_API) {
                return;
            }
            FileObject root = context.getRoot();
            if (delegate != null && root != null) {
                delegate.removed(root);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return 1;
        }
    }
}
