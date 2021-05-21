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
package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex2;
import org.netbeans.modules.parsing.lucene.support.Index.Status;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;
import org.openide.util.Exceptions;
import static org.netbeans.modules.parsing.impl.indexing.TransientUpdateSupport.isTransientUpdate;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Convertors;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.util.Pair;
/**
 *
 * @author Tomas Zezula
 */
public final class LayeredDocumentIndex implements DocumentIndex2.Transactional {
    
    private final DocumentIndex2.Transactional base;
    
    private final Set<String> filter = new HashSet<String>();
    //@GuardedBy("this")
    private DocumentIndex2 overlay;
    
    
    LayeredDocumentIndex(@NonNull final DocumentIndex2.Transactional base) {
        assert base != null;
        this.base = base;
    }

    @Override
    public void addDocument(IndexDocument document) {
        if (isTransientUpdate()) {
            try {
                getOverlay().addDocument(document);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        } else {
            base.addDocument(document);
        }
    }

    @Override
    public void removeDocument(String primaryKey) {
        if (!isTransientUpdate()) {
            base.removeDocument(primaryKey);
        }
    }

    @Override
    public Status getStatus() throws IOException {
        if (isTransientUpdate()) {
            return Status.VALID;
        } else {
            return base.getStatus();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            base.close();
        } finally {
            clearOverlay();
        }
    }

    @Override
    public void store(boolean optimize) throws IOException {
        if (isTransientUpdate()) {
            final Pair<DocumentIndex2,Set<String>> ovl = getOverlayIfExists();
            if (ovl.first() != null) {
                ovl.first().store(false);
            }
        } else {
            base.store(optimize);
        }
    }

    @Override
    public void rollback() throws IOException {
        if (isTransientUpdate()) {
            clearOverlay();
        } else {
            base.rollback();
        }
    }

    @Override
    public void commit() throws IOException {
        if (isTransientUpdate()) {
            throw new UnsupportedOperationException("Transactions not supported for overlay."); //NOI18N
        } else {
            base.commit();
        }
    }    

    @Override
    public void txStore() throws IOException {
        if (isTransientUpdate()) {
            throw new UnsupportedOperationException("Transactions not supported for overlay."); //NOI18N
        } else {
            base.txStore();
        }
    }

    @Override
    public void clear() throws IOException {
        if (isTransientUpdate()) {
            clearOverlay();
        } else {
            base.clear();
        }
    }


    @Override
    public Collection<? extends IndexDocument> query(String fieldName, String value, QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
        return query(Queries.createQuery(fieldName, fieldName, value, kind), Convertors.<IndexDocument>identity(), fieldsToLoad);
    }

    @Override
    public Collection<? extends IndexDocument> findByPrimaryKey(String primaryKeyValue, QueryKind kind, String... fieldsToLoad) throws IOException, InterruptedException {
        final Collection<? extends IndexDocument> br = base.findByPrimaryKey(primaryKeyValue, kind, fieldsToLoad);
        final Pair<DocumentIndex2,Set<String>> ovl = getOverlayIfExists();
        if (ovl.first() == null) {
            return ovl.second() == null ? br : Filter.filter(br, ovl.second());
        } else {
            return new ProxyCollection<IndexDocument>(
                ovl.second() == null ? br : Filter.filter(br,ovl.second()),
                ovl.first().findByPrimaryKey(primaryKeyValue, kind, fieldsToLoad));
        }
    }

    @Override
     @NonNull
     public <T> Collection<? extends T> query(
             @NonNull final Query query,
             @NonNull final Convertor<? super IndexDocument, ? extends T> convertor,
             @NullAllowed final String... fieldsToLoad) throws IOException, InterruptedException {
         final Pair<DocumentIndex2,Set<String>> ovl = getOverlayIfExists();
         Convertor<? super IndexDocument, ? extends T> filterConvertor;
         if (ovl.second() == null) {
             filterConvertor = convertor;
         } else {
             filterConvertor = Convertors.compose(
                     new Filter(ovl.second()),
                     convertor);
         }
         final Collection<? extends T> br = base.query(
                 query,
                 filterConvertor,
                 fieldsToLoad);

         if (ovl.first() == null) {
             return br;
         } else {
             return new ProxyCollection<T>(
                 br,
                 ovl.first().query(query, convertor, fieldsToLoad));
         }
     }

    @Override
    public void markKeyDirty(String primaryKey) {
        addToFilter(primaryKey);
        base.markKeyDirty(primaryKey);
    }

    @Override
    public void removeDirtyKeys(Collection<? extends String> dirtyKeys) {
        try {
            base.removeDirtyKeys(dirtyKeys);
        } finally {
            if (!isTransientUpdate()) {
                clearOverlay();
            }
        }
    }

    @Override
    public Collection<? extends String> getDirtyKeys() {
        return base.getDirtyKeys();
    }
    
    public boolean begin() {
        if (!isTransientUpdate() && base instanceof Runnable) {
            ((Runnable)base).run();
            return true;
        }
        return false;
    }
    
    @NonNull
    private synchronized DocumentIndex2 getOverlay() throws IOException {
        if (overlay == null) {
            overlay = (DocumentIndex2) IndexManager.createDocumentIndex(IndexManager.createMemoryIndex(new KeywordAnalyzer()));
        }
        return overlay;
    }
    
    @NonNull
    private synchronized Pair<DocumentIndex2,Set<String>> getOverlayIfExists() throws IOException {
        final Set<String> f = filter.isEmpty() ? null : new HashSet<String>(filter);
        return Pair.<DocumentIndex2,Set<String>>of(overlay,f);
    }
    
    private synchronized void addToFilter(@NonNull final String primaryKey) {
        filter.add(primaryKey);
    }
    
    private synchronized void clearOverlay() {
        filter.clear();
        if (overlay != null) {
            try {
                overlay.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                overlay = null;
            }
        }
    }

    private static final class Filter implements Convertor<IndexDocument, IndexDocument> {

        private final Set<String> filter;

        Filter(@NonNull final Set<String> filter) {
            assert filter != null;
            assert !filter.isEmpty();
            this.filter = filter;
        }

        @Override
        @CheckForNull
        public IndexDocument convert(@NullAllowed final IndexDocument p) {
            return filtered(p, filter) ?
                null :
                p;
        }

        @NonNull
        static Collection<? extends IndexDocument> filter (
            @NonNull final Collection<? extends IndexDocument> base,
            @NonNull final Set<String> filter) {
            assert !filter.isEmpty();
            final Collection<IndexDocument> res = new ArrayList<IndexDocument>(base.size());
            for (IndexDocument doc : base) {
                if (!filtered(doc, filter)) {
                    res.add(doc);
                }
            }
            return res;
        }

        private static boolean filtered(
            @NullAllowed final IndexDocument doc,
            @NonNull final Set<? extends String> filter) {
            return doc == null ?
                true :
                filter.contains(doc.getPrimaryKey());
        }

    }
    
    private static class ProxyCollection<E> extends AbstractCollection<E> {
        
        private final Collection<? extends E> base;
        private final Collection<? extends E> overlay;
        
        ProxyCollection(
            @NonNull final Collection<? extends E> base,
            @NonNull final Collection<? extends E> overlay) {
            this.base = base;
            this.overlay = overlay;
        }

        @Override
        public Iterator<E> iterator() {
            return new ProxyIterator<E>(base.iterator(), overlay.iterator());
        }

        @Override
        public int size() {
            return base.size() + overlay.size();
        }
    }
    
    private static class ProxyIterator<E> implements Iterator<E> {
        
        private final Iterator<? extends E> base;
        private final Iterator<? extends E> overlay;
        
        ProxyIterator(
            @NonNull final Iterator<? extends E> base,
            @NonNull final Iterator<? extends E> overlay) {
            this.base = base;
            this.overlay = overlay;
        }

        @Override
        public boolean hasNext() {
            return base.hasNext() || overlay.hasNext();
        }

        @Override
        public E next() {
            //xxx: Cast to workaround javac 1.6.0_29 bug
            return (base.hasNext() ? base.next() : overlay.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Unmodifiable collection"); //NOI18N
        }
        
    }
}
