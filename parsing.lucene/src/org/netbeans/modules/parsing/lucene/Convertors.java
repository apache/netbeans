/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    static <T> StoppableConvertor<TermEnum,T> newTermEnumToTermConvertor(
        @NonNull StoppableConvertor<Term,T> delegate) {
        return new TermEnumToTerm<T>(delegate);
    }

    static <T> StoppableConvertor<TermEnum,T> newTermEnumToFreqConvertor(
        @NonNull StoppableConvertor<Index.WithTermFrequencies.TermFreq,T> delegate) {
        return new TermEnumToFreq<T>(delegate);
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
        public void setIndexReader(@NonNull final IndexReader indexReader) {
            if (delegate instanceof IndexReaderInjection) {
                ((IndexReaderInjection)delegate).setIndexReader(indexReader);
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
        public void setIndexReader(@NonNull final IndexReader indexReader) {
            if (delegate instanceof IndexReaderInjection) {
                ((IndexReaderInjection)delegate).setIndexReader(indexReader);
            }
        }
    }
    
}
