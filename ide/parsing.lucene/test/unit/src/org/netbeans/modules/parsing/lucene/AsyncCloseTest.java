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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class AsyncCloseTest extends NbTestCase {

    private static final String FLD_KEY = "key";    //NOI18N

    private File indexFolder;


    public AsyncCloseTest(final String name){
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        indexFolder = FileUtil.normalizeFile(new File(getWorkDir(),"index"));   //NOI18N
        indexFolder.mkdirs();
    }



    public void testAsyncClose() throws Exception {
        final CountDownLatch slot = new CountDownLatch(1);
        final CountDownLatch signal = new CountDownLatch(1);
        final  CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();

        final Index index = IndexManager.createTransactionalIndex(indexFolder, new KeywordAnalyzer());
        final Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    index.store(
                       new ArrayList<String>(Arrays.asList("foo")), //NOI18N
                       Collections.<String>emptySet(),
                       new TestInsertConvertor(slot, signal),
                       new TestDeleteConvertor(),
                       true);
                } catch (Exception ex) {
                    exception.set(ex);
                } finally {
                    done.countDown();
                }
            }
        });
        worker.start();

        signal.await();
        slot.countDown();
        index.close();
        done.await();
        assertNull(exception.get());
    }

    public void testConcurrentReadWrite() throws Exception {
        final Index index = IndexManager.createTransactionalIndex(indexFolder, new KeywordAnalyzer());
        index.store(
            new ArrayList<String>(Arrays.asList("a")), //NOI18N
            Collections.<String>emptySet(),
            new TestInsertConvertor(),
            new TestDeleteConvertor(),
            true);

        final CountDownLatch slot = new CountDownLatch(1);
        final CountDownLatch signal = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<Exception> result = new AtomicReference<Exception>();

        final Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    index.store(
                           new ArrayList<String>(Arrays.asList("b")), //NOI18N
                           Collections.<String>emptySet(),
                           new TestInsertConvertor(slot, signal),
                           new TestDeleteConvertor(),
                           true);
                } catch (Exception e) {
                    result.set(e);
                } finally {
                    done.countDown();
                }
            }
        });

        worker.start();
        signal.await();

        final Collection<String> data = new ArrayList<String>();
        index.query(
            data,
            new Convertor<Document,String>(){
                @Override
                public String convert(Document p) {
                    return p.get(FLD_KEY);
                }
            },
            null,
            new AtomicBoolean(),
            new PrefixQuery(new Term(FLD_KEY,""))); //NOI18N
        assertEquals(1, data.size());
        assertEquals("a", data.iterator().next());  //NOI18N
        slot.countDown();
        done.await();
        assertNull(result.get());
    }

    private static final class TestInsertConvertor implements Convertor<String, Document> {

        private final CountDownLatch slot;
        private final CountDownLatch signal;

        TestInsertConvertor(
                @NonNull final CountDownLatch slot,
                @NonNull final CountDownLatch signal) {
            Parameters.notNull("slot", slot);   //NOI18N
            Parameters.notNull("signal", signal);   //NOI18N
            this.slot = slot;
            this.signal = signal;
        }

        TestInsertConvertor() {
            slot = null;
            signal = null;
        }

        @Override
        public Document convert(String p) {
            if (signal != null) {
                signal.countDown();
            }

            if (slot != null) {
                try {
                    this.slot.await();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            final Document doc = new Document();
            doc.add(new Field(FLD_KEY, p, Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));   //NOI18N
            return doc;
        }
    }

    private static final class TestDeleteConvertor implements Convertor<String, Query> {
        @Override
        public Query convert(String p) {
            return new TermQuery(new Term(FLD_KEY,p));    //NOI18N
        }
    }

}
