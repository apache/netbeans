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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.TransientUpdateSupport;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.DocumentIndexCache;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public final class LuceneIndexFactory implements IndexFactoryImpl {
    
    private static final int VERSION = 1;
    //@GuardedBy("LuceneIndexFactory.class")
    private static LuceneIndexFactory instance;
    //@GuardedBy("indexes")
    private final Map<URL,LayeredDocumentIndex> indexes = new HashMap<URL, LayeredDocumentIndex>();
    //@GuardedBy("indexes")
    private boolean closed;

    
    private LuceneIndexFactory(){}

    @Override
    @NonNull
    public IndexDocument createDocument(@NonNull final Indexable indexable) {
        Parameters.notNull("indexable", indexable); //NOI18N
        return TransientUpdateSupport.isTransientUpdate() ?
                IndexManager.createDocument(indexable.getRelativePath()) :
                ClusteredIndexables.createDocument(indexable.getRelativePath());
    }

    @Override
    @CheckForNull
    public LayeredDocumentIndex createIndex (@NonNull final Context ctx) throws IOException {
        Parameters.notNull("ctx", ctx); //NOI18N
        final FileObject indexBaseFolder = ctx.getIndexFolder();
        if (indexBaseFolder == null) {
            throw new IOException("No index base folder."); //NOI18N
        }
        return getIndexImpl(indexBaseFolder, DocumentBasedIndexManager.Mode.CREATE);
    }

    @Override
    @CheckForNull
    public DocumentIndexCache getCache (@NonNull final Context ctx) throws IOException {
        Parameters.notNull("ctx", ctx); //NOI18N
        final FileObject indexBaseFolder = ctx.getIndexFolder();
        if (indexBaseFolder == null) {
            throw new IOException("No index base folder."); //NOI18N
        }
        return DocumentBasedIndexManager.getDefault().getCache(getIndexFolder(indexBaseFolder));
    }

    @Override
    @CheckForNull
    public LayeredDocumentIndex getIndex(@NonNull final FileObject indexFolder) throws IOException {
        Parameters.notNull("indexFolder", indexFolder); //NOI18N
        return getIndexImpl(indexFolder, DocumentBasedIndexManager.Mode.IF_EXIST);
    }
    
    @CheckForNull
    private LayeredDocumentIndex getIndexImpl(
        @NonNull final FileObject indexBaseFolder,
        @NonNull DocumentBasedIndexManager.Mode mode) throws IOException {        
        final URL luceneIndexFolder = getIndexFolder(indexBaseFolder);
        
        synchronized (indexes) {
            if (closed) {
                return null;
            }
            LayeredDocumentIndex res = indexes.get(luceneIndexFolder);
            if (res == null) {
                final DocumentIndex2.Transactional base = DocumentBasedIndexManager.getDefault().getIndex(
                        luceneIndexFolder,
                        mode);
                if (base != null) {
                    res = new LayeredDocumentIndex(base);
                    indexes.put(luceneIndexFolder, res);
                }
            }
            return res;
        }
    }

    @NonNull
    private URL getIndexFolder (@NonNull final FileObject indexFolder) throws IOException {
        assert indexFolder != null;
        final String indexVersion = Integer.toString(VERSION);
        URL result = Util.resolveFile(FileUtil.toFile(indexFolder), indexVersion, Boolean.TRUE);
        final String surl = result.toExternalForm();
        if (surl.charAt(surl.length()-1) != '/') {       //NOI18N
            result = new URL(surl+'/');  //NOI18N
        }
        return result;
    }
    
    public void close() {
        synchronized (indexes) {
            if (closed) {
                return;
            }
            closed = true;
            for (LayeredDocumentIndex index : indexes.values()) {
                try {
                    index.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    @NonNull
    public static synchronized LuceneIndexFactory getDefault() {
        if (instance == null) {
            instance = new LuceneIndexFactory();
        }
        return instance;
    }

}
