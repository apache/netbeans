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

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {

    private static File wd;
    private File indexFolder;
    private DocumentIndex index;
    
    public LuceneIndexTest(final String name) {
        super(name);
    }

    @Before
    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        wd = getWorkDir();
        indexFolder = new File (wd, "index");   //NOI18N
        indexFolder.mkdirs();
        index = IndexManager.createDocumentIndex(indexFolder);
    }

    @After
    @Override
    public void tearDown() throws IOException {
        index.close();
    }

    @Test
    public void testIndexAddDelete() throws Exception {
        for (int i=0; i< 1000; i++) {
            IndexDocument docwrap = IndexManager.createDocument(Integer.toString(i));
            docwrap.addPair("bin", Integer.toBinaryString(i), true, true);
            docwrap.addPair("oct", Integer.toOctalString(i), true, true);
            index.addDocument(docwrap);
        }
        index.store(true);
        BitSet expected = new BitSet(1000);
        expected.set(0, 1000);
        assertIndex(expected);
        for (int i = 100; i<200; i++) {
            index.removeDocument(Integer.toString(i));
            expected.clear(i);
        }
        index.store(true);
        assertIndex(expected);
    }

// Commented out as it takes a long time
//    @Test
//    public void testPerformance() throws Exception {
//        System.gc(); System.gc(); System.gc();
//        long st = System.currentTimeMillis();
//        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
//        long start = bean.getHeapMemoryUsage().getUsed();
//        for (int ic = 0; ic < 2; ic++) {
//            final LuceneIndex index = new LuceneIndex(new File(getWorkDir(),Integer.toString(ic)).toURI().toURL());
//            for (int i=0; i< 1000000; i++) {
//                LuceneDocument doc = new LuceneDocument(SPIAccessor.getInstance().create(new FakeIndexableImpl(i)));
//                doc.addPair("bin-value", Integer.toBinaryString(i), true, true);
//                doc.addPair("dec-value", Integer.toString(i), true, true);
//                index.addDocument(doc);
//            }
//            index.store();
//        }
//        long et = System.currentTimeMillis();
//        for (int i=0; i< 2; i++) {
//            System.gc(); System.gc(); System.gc();
//            Thread.sleep(500);
//        }
//
//        long end = bean.getHeapMemoryUsage().getUsed();
//        assertTrue(end < 3 * start);
//    }



    private void assertIndex(final BitSet expected) throws IOException, InterruptedException {
        for (int i=0; i < expected.length(); i++) {
            final Collection<? extends IndexDocument> res = index.query("bin", Integer.toBinaryString(i), Queries.QueryKind.EXACT, "bin","oct");
            boolean should = expected.get(i);
            assertEquals(should, res.size()==1);
            if (should) {
                assertEquals(res.iterator().next().getValue("bin"), Integer.toBinaryString(i));
                assertEquals(res.iterator().next().getValue("oct"), Integer.toOctalString(i));
            }
        }
    }    

}
