/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
