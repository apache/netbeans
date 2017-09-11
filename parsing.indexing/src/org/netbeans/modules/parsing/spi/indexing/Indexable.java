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

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.FileObjectProvider;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.IndexableImpl;
import org.netbeans.modules.parsing.impl.indexing.LogContext;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.impl.indexing.SPIAccessor;
import org.netbeans.modules.parsing.impl.indexing.SuspendSupport.SuspendStatusImpl;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Parameters;


/**
 * Represens a file to be procesed by an indexer.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Indexable {

    static {
        SPIAccessor.setInstance(new MyAccessor());
    }

    private IndexableImpl delegate;

    Indexable(final @NonNull IndexableImpl delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns a relative path from root to the
     * represented file.
     * @return the relative path from root
     */
    public @NonNull String getRelativePath () {
        return delegate.getRelativePath();
    }


    /**
     * Returns absolute URL of the represented file
     * @return the URL of file or null in case of IO error
     */
    public @CheckForNull URL getURL () {
        return delegate.getURL();
    }

    /**
     * Returns a mime type of the {@link Indexable}
     * @return the mime type
     * @throws UnsupportedOperationException if called on deleted {@link Indexable}
     * @since 1.13
     */
    public @NonNull String getMimeType() {
        return delegate.getMimeType();
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Indexable other = (Indexable) obj;
        return delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    private static final class MyAccessor extends SPIAccessor {

        @Override
        public Indexable create(IndexableImpl delegate) {
            return new Indexable(delegate);
        }

        @Override
        public void index(final BinaryIndexer indexer, final Context context) {
            assert indexer != null;
            assert context != null;
            RepositoryUpdater.getDefault().runIndexer(new Runnable() {
                public void run() {
                    indexer.index(context);
                }
            });
        }

        @Override
        public void index(final CustomIndexer indexer, final Iterable<? extends Indexable> files, final Context context) {
            assert indexer != null;
            assert files != null;
            assert context != null;
            RepositoryUpdater.getDefault().runIndexer(new Runnable() {
                public void run() {
                    indexer.index(files, context);
                }
            });
        }

        @Override
        public Context createContext(
                FileObject indexFolder,
                URL rootURL,
                String indexerName,
                int indexerVersion,
                IndexFactoryImpl factory,
                boolean followUpJob,
                boolean checkForEditorModifications,
                boolean sourceForBinaryRoot,
                @NonNull SuspendStatus suspendedStatus,
                @NullAllowed CancelRequest cancelRequest,
                @NullAllowed final LogContext logContext) throws IOException {
            return new Context(
                    indexFolder,
                    rootURL,
                    indexerName,
                    indexerVersion,
                    factory,
                    followUpJob,
                    checkForEditorModifications,
                    sourceForBinaryRoot,
                    suspendedStatus,
                    cancelRequest,
                    logContext);
        }
    
        @Override
        @NonNull
        public Context createContext(
                @NonNull final Callable<FileObject> indexFolderFactory,
                @NonNull final URL rootURL,
                @NonNull final String indexerName,
                int indexerVersion,
                @NullAllowed final IndexFactoryImpl factory,
                boolean followUpJob,
                boolean checkForEditorModifications,
                boolean sourceForBinaryRoot,
                @NonNull final SuspendStatus suspendedStatus,
                @NullAllowed final CancelRequest cancelRequest,
                @NullAllowed final LogContext logContext) throws IOException {
            return new Context(
                indexFolderFactory,
                rootURL,
                indexerName,
                indexerVersion,
                factory,
                followUpJob,
                checkForEditorModifications,
                sourceForBinaryRoot,
                suspendedStatus,
                cancelRequest,
                logContext);
        }


        @NonNull
        @Override
        public SuspendStatus createSuspendStatus(@NonNull final SuspendStatusImpl impl) {
            return new SuspendStatus(impl);
        }

        @Override
        public String getIndexerName(Context ctx) {
            assert ctx != null;
            return ctx.getIndexerName();
        }

        @Override
        public int getIndexerVersion(Context ctx) {
            assert ctx != null;
            return ctx.getIndexerVersion();
        }

        @Override
        public void index(final EmbeddingIndexer indexer, final Indexable indexable, final Result parserResult, final Context ctx) {
            assert indexer != null;
            assert indexable != null;
            assert parserResult != null;
            assert ctx != null;
            RepositoryUpdater.getDefault().runIndexer(new Runnable() {
                public void run() {
                    indexer.index(indexable, parserResult, ctx);
                }
            });
        }

        @Override
        public String getIndexerPath(final String indexerName, final int indexerVersion) {
            assert indexerName != null;
            return Context.getIndexerPath(indexerName, indexerVersion);
        }

        @Override
        public IndexFactoryImpl getIndexFactory(Context ctx) {
            assert ctx != null;
            return ctx.getIndexFactory();
        }

        @Override
        public void context_attachIndexingSupport(Context context, IndexingSupport support) {
            context.attachIndexingSupport(support);
        }

        @Override
        public IndexingSupport context_getAttachedIndexingSupport(Context context) {
            return context.getAttachedIndexingSupport();
        }

        @Override
        public void context_clearAttachedIndexingSupport(final Context context) {
            context.clearAttachedIndexingSupport();
        }

        @Override
        public void setAllFilesJob(final Context context, final boolean allFilesJob) {
            context.setAllFilesJob(allFilesJob);
        }

        @Override
        public void index(
                @NonNull final ConstrainedBinaryIndexer indexer,
                @NonNull final Map<String,? extends Iterable<? extends FileObject>> files,
                @NonNull final Context context) {
            Parameters.notNull("indexer", indexer);     //NOI18N
            Parameters.notNull("files", files);     //NOI18N
            Parameters.notNull("context", context); //NOI18N
            indexer.index(files, context);
        }

        @Override
        public boolean scanStarted(
                @NonNull final ConstrainedBinaryIndexer indexer,
                @NonNull final Context context) {
            Parameters.notNull("indexer", indexer); //NOI18N
            Parameters.notNull("context", context); //NOI18N
            return indexer.scanStarted(context);
        }

        @Override
        public void scanFinished(
                @NonNull final ConstrainedBinaryIndexer indexer,
                @NonNull final Context context) {
            Parameters.notNull("indexer", indexer); //NOI18N
            Parameters.notNull("context", context); //NOI18N
            indexer.scanFinished(context);
        }

        @Override
        public void rootsRemoved(
                @NonNull ConstrainedBinaryIndexer indexer,
                @NonNull Iterable<? extends URL> removed) {
            Parameters.notNull("indexer", indexer); //NOI18N
            Parameters.notNull("removed", removed); //NOI18N
            indexer.rootsRemoved(removed);
        }

        @Override
        public void putProperty(
                @NonNull final Context context,
                @NonNull final String propName,
                @NullAllowed final Object value) {
            context.putProperty(propName, value);
        }

        @Override
        public Object getProperty(
                @NonNull final Context context,
                @NonNull final String propName) {
            return context.getProperty(propName);
        }

        @Override
        public boolean isTypeOf (
                @NonNull final Indexable indexable,
                @NonNull final String mimeType) {
            return indexable.delegate.isTypeOf(mimeType);
        }

        @Override
        public FileObject getFileObject(
                @NonNull final Indexable indexable) {
            return indexable.delegate instanceof FileObjectProvider ?
                ((FileObjectProvider)indexable.delegate).getFileObject() :
                URLMapper.findFileObject(indexable.getURL());
        }
    }

}
