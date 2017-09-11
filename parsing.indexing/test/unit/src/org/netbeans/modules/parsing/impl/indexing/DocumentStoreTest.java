/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentStoreTest extends NbTestCase {

    private static final long CACHE_SIZE =
            (long) (Runtime.getRuntime().maxMemory() * 0.1);

    public DocumentStoreTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBasicOperations() {
        final int testSize = 100000;
        final Collection<IndexDocument> store = new ClusteredIndexables.DocumentStore(CACHE_SIZE);
        final String path = getWorkDirPath();
        for (int i=0; i<testSize; i++) {
            store.add(fill(ClusteredIndexables.createDocument(
                    String.format("%s%d", path, i)),i));        //NOI18N
        }        
        assertFalse(store.isEmpty());
        assertEquals(testSize, store.size());
        Iterator<IndexDocument> it = store.iterator();
        for (int i=0; i<testSize; i++) {
            final IndexDocument doc = it.next();
            assertEquals(String.format("%s%d", path, i), doc.getPrimaryKey());  //NOI18N
            assertEquals(1, doc.getValues("val").length);                       //NOI18N
            assertEquals(Integer.toString(i), doc.getValue("val"));     //NOI18N
            assertEquals(1, doc.getValues("bin").length);                       //NOI18N
            assertEquals(Integer.toBinaryString(i), doc.getValue("bin"));     //NOI18N
            assertEquals(1, doc.getValues("hex").length);                       //NOI18N
            assertEquals(Integer.toHexString(i), doc.getValue("hex"));     //NOI18N

        }
        assertFalse(it.hasNext());
        store.clear();
        assertTrue(store.isEmpty());
        assertEquals(0, store.size());
        assertFalse(store.iterator().hasNext());

        final Collection<IndexDocument> base = new ArrayList<IndexDocument>();
        for (int i=0; i<testSize; i++) {
            base.add(fill(ClusteredIndexables.createDocument(
                    String.format("%s%d", path, i)),i));        //NOI18N
        }
        store.addAll(base);
        assertFalse(store.isEmpty());
        assertEquals(testSize, store.size());
        it = store.iterator();
        for (int i=0; i<testSize; i++) {
            final IndexDocument doc = it.next();
            assertEquals(String.format("%s%d", path, i), doc.getPrimaryKey());  //NOI18N
            assertEquals(1, doc.getValues("val").length);                       //NOI18N
            assertEquals(Integer.toString(i), doc.getValue("val"));     //NOI18N
            assertEquals(1, doc.getValues("bin").length);                       //NOI18N
            assertEquals(Integer.toBinaryString(i), doc.getValue("bin"));     //NOI18N
            assertEquals(1, doc.getValues("hex").length);                       //NOI18N
            assertEquals(Integer.toHexString(i), doc.getValue("hex"));     //NOI18N

        }
        assertFalse(it.hasNext());        
        store.clear();
        assertTrue(store.isEmpty());
        assertEquals(0, store.size());
        assertFalse(store.iterator().hasNext());
    }

    public void testLargeFieldDocumentAdded() {
        final ClusteredIndexables.DocumentStore store = new ClusteredIndexables.DocumentStore(CACHE_SIZE);
        final IndexDocument doc = ClusteredIndexables.createDocument(getWorkDirPath());
        final String bigValue = newRandomString(16<<10);
        doc.addPair("big", bigValue, true, true);               //NOI18N
        store.add(doc);
        final Iterator<IndexDocument> it = store.iterator();
        assertTrue(it.hasNext());
        final IndexDocument res = it.next();
        assertEquals(getWorkDirPath(), res.getPrimaryKey());
        assertEquals(1, res.getValues("big").length);           //NOI18N
        assertEquals(bigValue, res.getValue("big"));            //NOI18N
        assertFalse(it.hasNext());

    }

    public void testFieldOnBoundsAdded() {
        final ClusteredIndexables.DocumentStore store = new ClusteredIndexables.DocumentStore(CACHE_SIZE);
        final String value = newRandomString(2<<10);
        final String padding = newRandomString(1<<4);
        final IndexDocument doc = ClusteredIndexables.createDocument(value);
        doc.addPair("padding", padding, true, true); //NOI18N
        store.add(doc);
        final Iterator<IndexDocument> it = store.iterator();
        assertTrue(it.hasNext());
        final IndexDocument res = it.next();
        assertEquals(value, res.getPrimaryKey());
        assertEquals(1, res.getValues("padding").length);           //NOI18N
        assertEquals(padding, res.getValue("padding"));            //NOI18N
        assertFalse(it.hasNext());
    }

    public void testIterator() {
        final ClusteredIndexables.DocumentStore store = new ClusteredIndexables.DocumentStore(CACHE_SIZE);
        final Iterator<IndexDocument> it = store.iterator();
        assertFalse(it.hasNext());
        boolean thrown = false;
        try {
           it.next();
        } catch (NoSuchElementException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testCacheFlush() {
        final ClusteredIndexables.DocumentStore store = new ClusteredIndexables.DocumentStore(4<<10);   //4KB
        final String val = newRandomString((1<<9)); //1KB
        assertFalse(store.addDocument(ClusteredIndexables.createDocument(val)));
        assertFalse(store.addDocument(ClusteredIndexables.createDocument(val)));
        assertFalse(store.addDocument(ClusteredIndexables.createDocument(val)));
        assertFalse(store.addDocument(ClusteredIndexables.createDocument(val)));
        assertTrue(store.addDocument(ClusteredIndexables.createDocument(val)));
        store.clear();
        assertFalse(store.addDocument(ClusteredIndexables.createDocument(val)));
    }

    public void testBrokenStorage231322() {
        final Logger log = Logger.getLogger(ClusteredIndexables.class.getName());
        final Level origLevel = log.getLevel();
        log.setLevel(Level.FINEST);
        final Handler h = new Handler() {
            @Override
            public void publish(LogRecord record) {
                final String msg = record.getMessage();
                if ("alloc".equals(msg)) {  //NOI18N
                    //Symulate OOM in new char[]
                    throw new OutOfMemoryError();
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        log.addHandler(h);
        try {
            final ClusteredIndexables.DocumentStore store = new ClusteredIndexables.DocumentStore(4<<10);
            final String val = newRandomString(512);
            final Deque<String> collector = new ArrayDeque<String>();
            boolean flush = store.addDocument(ClusteredIndexables.createDocument(val));
            flushIfNeeded(flush, store, collector);
            flush = store.addDocument(ClusteredIndexables.createDocument(val));
            flushIfNeeded(flush, store, collector);
            flush = store.addDocument(ClusteredIndexables.createDocument(val));
            flushIfNeeded(flush, store, collector);
            store.addDocument(ClusteredIndexables.createDocument(val));
            flushIfNeeded(true, store, collector);
            assertEquals(4, collector.size());
            for (String pk : collector) {
                assertEquals(val, pk);
            }
        } finally {
            log.setLevel(origLevel);
            log.removeHandler(h);
        }
    }

    private static void flushIfNeeded(
            final boolean flush,
            @NonNull final ClusteredIndexables.DocumentStore store,
            @NonNull final Deque<? super String> collector) {
        if (flush) {
            for (IndexDocument doc : store) {
                collector.offer(doc.getPrimaryKey());
            }
            store.clear();
        }
    }


    public void testPerformance() throws IOException {
        final boolean packed = true;
        final int size = 500000;
        final File wd = getWorkDir();
        final Index index = IndexManager.createIndex(wd, new KeywordAnalyzer());
        final Pair<Collection<? extends IndexDocument>,Convertor<IndexDocument, Document>> p =
                packed ?
                createPackedData(size) :
                createDefaultData(size);
        final long st = System.currentTimeMillis();
        index.store(
            p.first(),
            Collections.<String>emptySet(),
            p.second(),
            new Convertor<String, Query>() {
                @Override
                public Query convert(String p) {
                    return null;
                }
            },
            true);
        final long et = System.currentTimeMillis();
        System.out.println(p.first().getClass().getSimpleName() + " : " + (et-st));
        index.close();
    }

    private Pair<Collection<? extends IndexDocument>,Convertor<IndexDocument, Document>> createDefaultData (
            final int size) {
        final Collection<IndexDocument> toAdd = new ArrayList<IndexDocument>(size);
        for (int i=0; i < size; i++) {
            final IndexDocument doc = IndexManager.createDocument(Integer.toString(i));
            toAdd.add(fill(doc,i));
        }
        try {
            final Class<?> clz = Class.forName("org.netbeans.modules.parsing.lucene.Convertors");   //NOI18N
            final Method m = clz.getDeclaredMethod("newIndexDocumentToDocumentConvertor");   //NOI18N
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Convertor<IndexDocument, Document> c = (Convertor<IndexDocument, Document>) m.invoke(m);
            return Pair.<Collection<? extends IndexDocument>,Convertor<IndexDocument, Document>>of(toAdd,c);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private Pair<Collection<? extends IndexDocument>,Convertor<IndexDocument, Document>> createPackedData (
            final int size) {
        final Collection<IndexDocument> toAdd = new ClusteredIndexables.DocumentStore(CACHE_SIZE);
        for (int i=0; i < size; i++) {
            final IndexDocument doc = ClusteredIndexables.createDocument(Integer.toString(i));
            toAdd.add(fill(doc,i));
        }
        return Pair.<Collection<? extends IndexDocument>,Convertor<IndexDocument, Document>>of(
            toAdd,
            ClusteredIndexables.createDocumentIndexCache().createAddConvertor());
    }

    @NonNull
    private static String newRandomString(final int len) {
        final Random rnd = new Random();
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < len) {
            sb.append(Integer.toHexString(rnd.nextInt(0x10)));
        }
        return sb.toString();
    }

    @NonNull
    private static IndexDocument fill(
       @NonNull final IndexDocument doc,
       final int id) {
        doc.addPair("val", Integer.toString(id), true, true);        //NOI18N
        doc.addPair("bin", Integer.toBinaryString(id), false, true); //NOI18N
        doc.addPair("hex", Integer.toHexString(id), true, false); //NOI18N
        return doc;
    }

}
