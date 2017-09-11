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
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.*;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SPIAccessor {
    
    private static volatile SPIAccessor instance;

    public static void setInstance (final SPIAccessor _instance) {
        assert _instance != null;
        instance = _instance;
    }

    public static synchronized SPIAccessor getInstance () {
        if (instance == null) {
            try {
                Class.forName(Indexable.class.getName(), true, Indexable.class.getClassLoader());
                assert instance != null;
            } catch (ClassNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return instance;
    }

    public abstract Indexable create (final IndexableImpl delegate);

    @NonNull
    public abstract Context createContext(
            @NonNull FileObject indexFolder,
            @NonNull URL rootURL,
            @NonNull String indexerName,
            int indexerVersion,
            @NullAllowed IndexFactoryImpl factory,
            boolean followUpJob,
            boolean checkForEditorModifications,
            boolean sourceForBinaryRoot,
            @NonNull final SuspendStatus suspendedStatus,
            @NullAllowed final CancelRequest cancelRequest,
            @NullAllowed final LogContext logContext) throws IOException;

    @NonNull
    public abstract Context createContext(
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
            @NullAllowed final LogContext logContext) throws IOException;
    
    @NonNull
    public abstract SuspendStatus createSuspendStatus(@NonNull SuspendSupport.SuspendStatusImpl impl);

    public abstract void context_attachIndexingSupport(Context context, IndexingSupport support);

    public abstract IndexingSupport context_getAttachedIndexingSupport(Context context);

    public abstract void context_clearAttachedIndexingSupport(Context context);
    
    public abstract String getIndexerName (Context ctx);

    public abstract int getIndexerVersion (Context ctx);

    public abstract String getIndexerPath (String indexerName, int indexerVersion);

    public abstract IndexFactoryImpl getIndexFactory (Context ctx);

    public abstract void index (BinaryIndexer indexer, Context context);

    public abstract void index (@NonNull ConstrainedBinaryIndexer indexer, @NonNull Map<String,? extends Iterable<? extends FileObject>> files, @NonNull Context context);

    public abstract void index (CustomIndexer indexer, Iterable<? extends Indexable> files, Context context);

    public abstract void index (EmbeddingIndexer indexer, Indexable indexable, Parser.Result parserResult, Context ctx);

    public abstract void setAllFilesJob (Context context, boolean allFilesJob);

    public abstract boolean scanStarted(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Context context);

    public abstract void scanFinished(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Context context);

    public abstract void rootsRemoved(@NonNull ConstrainedBinaryIndexer indexer, @NonNull Iterable<? extends URL> removed);

    public abstract void putProperty(@NonNull Context context, @NonNull String propName, @NullAllowed Object value);

    public abstract Object getProperty(@NonNull Context context, @NonNull String propName);

    public abstract boolean isTypeOf (@NonNull Indexable indexable, @NonNull String mimeType);

    public abstract FileObject getFileObject(@NonNull Indexable indexable);
}
