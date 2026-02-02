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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.netbeans.api.annotations.common.NonNull;

/**
 * This class serves on two places. It is called by {@link TermCollecting} class
 * to add documents. It is also used as a proxy to the real Collector passed to the searcher,
 * it intercepts and <b>remembers</b> document base indexes associated with IndexReaders
 * from the {@link #setNextReader(org.apache.lucene.index.IndexReader, int)} call.
 *
 * @author Tomas Zezula
 */
public final class TermCollector extends SimpleCollector {
    private final SimpleCollector delegate;
    private final Map<Integer, Set<Term>> doc2Terms;

    TermCollector(SimpleCollector collector) {
        this.delegate = collector;
        doc2Terms = new HashMap<>();
    }

    public void add (final int docId, final @NonNull Term term) {
        doc2Terms.computeIfAbsent(docId, k -> new HashSet<>())
                 .add(term);
    }

    Set<Term> get(final int docId) {
        return doc2Terms.get(docId);
    }

    Set<? extends Integer> docs() {
        return Collections.unmodifiableSet(doc2Terms.keySet());
    }

    @Override
    public ScoreMode scoreMode() {
        return ScoreMode.COMPLETE_NO_SCORES;
    }

    public static interface TermCollecting {
        void attach (TermCollector collector);
    }

    @Override
    public void collect(int i) throws IOException {
        delegate.collect(i);
    }

    @Override
    public void setScorer(Scorable scorer) throws IOException {
        super.setScorer(scorer);
    }

    @Override
    protected void doSetNextReader(LeafReaderContext context) throws IOException {
        super.doSetNextReader(context);
    }

}
