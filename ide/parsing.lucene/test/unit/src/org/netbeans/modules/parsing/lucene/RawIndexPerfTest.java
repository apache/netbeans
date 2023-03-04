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
package org.netbeans.modules.parsing.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;

/**
 *
 * @author Tomas Zezula
 */
public class RawIndexPerfTest extends NbTestCase {

    private static final int NO_RUNS = 0;   //10;
    private static final int NO_ROUNDS = 100;
    private static final int ROUND_SIZE = 10_000;
    private static final int NO_QUERIES = ROUND_SIZE/10;

    private final AtomicLong seq = new AtomicLong();
    private final Random rnd = new Random();

    public RawIndexPerfTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }


    public void testPerf() throws Exception {
        if (NO_RUNS > 0) {
            long indexTime = 0L;
            long searchTime = 0L;
            final File indexFolder = getWorkDir();
            for (int i = 0; i< NO_RUNS; i++) {
                deleteContent(indexFolder);
                long[] times = measure(indexFolder);
                indexTime+=times[0];
                searchTime+=times[1];
            }
            System.out.printf("Index time: %d%n",indexTime/(NO_RUNS*1000000));
            System.out.printf("Search time: %d%n", searchTime/(NO_RUNS*1000000));
        }
    }

    private long[] measure(final File indexDir) throws Exception {
        long it = 0;
        final LuceneIndex index = LuceneIndex.create(indexDir, new KeywordAnalyzer());
        for (int i=0; i< NO_ROUNDS; i++) {
            final List<Long> items = generateData(ROUND_SIZE);
            final long st = System.nanoTime();
            index.store(
                items,
                Collections.<Long>emptySet(),
                new LongToDoc(),
                new LongToQuery(),
                false);
            it+= System.nanoTime() - st;
        }
        long qt = 0;
        final List<Long> res = new ArrayList<>();
        final Convertor<Document,Long> c = new DocToLong();
        for (int i=0; i< NO_QUERIES; i++) {
            final Query q = createQuery();
            final long st = System.nanoTime();
            index.query(res, c, null, null, q);
            qt += System.nanoTime() - st;
            res.clear();
        }
        index.close();
        return new long[] {it, qt};
    }

    private List<Long> generateData(int size) {
        final List<Long> res = new ArrayList<>(size);
        for (int i=0; i<size; i++) {
            res.add(seq.incrementAndGet());
        }
        return res;
    }

    private Query createQuery() {
        int i = rnd.nextInt((int)seq.get());
        return new TermQuery(new Term("dec", String.valueOf(i)));
    }

    private static void deleteContent(File folder) {
        final File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteContent(child);
                }
                child.delete();
            }
        }
    }

    private static final class LongToDoc implements Convertor<Long, Document> {
        @Override
        public Document convert(Long p) {
            final Document doc = new Document();
            doc.add(new Field("dec", Long.toString(p), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("hex", Long.toHexString(p), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
            doc.add(new Field("bin", Long.toBinaryString(p), Field.Store.YES, Field.Index.NO));
            return doc;
        }
    }

    private static final class LongToQuery implements Convertor<Long, Query> {
        @Override
        public Query convert(Long p) {
            return new TermQuery(new Term("dec", Long.toString(p)));
        }
    }

    private static final class DocToLong implements Convertor<Document,Long> {
        @Override
        public Long convert(Document p) {
            return Long.valueOf(p.getFieldable("dec").stringValue());
        }
    }
}
