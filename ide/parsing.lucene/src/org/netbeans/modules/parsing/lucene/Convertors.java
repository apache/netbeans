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
package org.netbeans.modules.parsing.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexReaderInjection;
import org.netbeans.modules.parsing.lucene.support.StoppableConvertor;

/**
 *
 * @author Tomas Zezula
 */
class Convertors {

    private Convertors() {
        throw  new IllegalStateException();
    }

    static Convertor<IndexDocument, Document> newIndexDocumentToDocumentConvertor() {
        return new AddConvertor();
    }

    static Convertor<Document,IndexDocumentImpl> newDocumentToIndexDocumentConvertor() {
        return new QueryConvertor();
    }

    static Convertor<String,Query> newSourceNameToQueryConvertor() {
        return new RemoveConvertor();
    }

    static <T> StoppableConvertor<TermEnum, T> newTermEnumToTermConvertor(@NonNull StoppableConvertor<Term, T> delegate) {
        return new TermEnumToTerm<>(delegate);
    }

    static <T> StoppableConvertor<TermEnum, T> newTermEnumToFreqConvertor(@NonNull StoppableConvertor<Index.WithTermFrequencies.TermFreq, T> delegate) {
        return new TermEnumToFreq<>(delegate);
    }


    private static final class AddConvertor implements Convertor<IndexDocument, Document> {
        @Override
        public Document convert(IndexDocument p) {
            return ((IndexDocumentImpl)p).doc;
        }
    }

    private static final class RemoveConvertor implements Convertor<String,Query> {
        @Override
        public Query convert(String p) {
            return IndexDocumentImpl.sourceNameQuery(p);
        }
    }

    private static final class QueryConvertor implements Convertor<Document,IndexDocumentImpl> {
        @Override
        public IndexDocumentImpl convert(Document p) {
            return new IndexDocumentImpl(p);
        }
    }

    private static class TermEnumToTerm<T> implements StoppableConvertor<TermEnum,T>, IndexReaderInjection {

        private final StoppableConvertor<Term,T> delegate;

        TermEnumToTerm(@NonNull final StoppableConvertor<Term,T> convertor) {
            this.delegate = convertor;
        }

        @Override
        public T convert(@NonNull final TermEnum terms) throws StoppableConvertor.Stop {
            final Term currentTerm = terms.term();
            if (currentTerm == null) {
                return null;
            }
            return delegate.convert(currentTerm);
        }

        @Override
        public void setIndexReader(@NonNull IndexReader indexReader) {
            if (delegate instanceof IndexReaderInjection iri) {
                iri.setIndexReader(indexReader);
            }
        }
    }

    private static class TermEnumToFreq<T> implements StoppableConvertor<TermEnum, T>, IndexReaderInjection {

        private final SupportAccessor accessor = SupportAccessor.getInstance();
        private final Index.WithTermFrequencies.TermFreq tf = accessor.newTermFreq();
        private final StoppableConvertor<Index.WithTermFrequencies.TermFreq,T> delegate;

        TermEnumToFreq(@NonNull final StoppableConvertor<Index.WithTermFrequencies.TermFreq,T> convertor) {
            this.delegate = convertor;
        }

        @Override
        public T convert(TermEnum terms) throws StoppableConvertor.Stop {
            final Term currentTerm = terms.term();
            if (currentTerm == null) {
                return null;
            }
            final int freq = terms.docFreq();
            return delegate.convert(accessor.setTermFreq(tf, currentTerm, freq));
        }

        @Override
        public void setIndexReader(@NonNull IndexReader indexReader) {
            if (delegate instanceof IndexReaderInjection iri) {
                iri.setIndexReader(indexReader);
            }
        }
    }
    
}
