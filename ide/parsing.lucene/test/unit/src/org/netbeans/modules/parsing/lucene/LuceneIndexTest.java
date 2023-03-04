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

/**
 *
 * @author Tomas Zezula
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;

/**
 *
 * @author Tomas Zezula
 */
public class LuceneIndexTest extends NbTestCase {
        
    public LuceneIndexTest (String testName) {
        super (testName);                
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
	this.clearWorkDir();
        //Prepare indeces
    }
                        
    public void testIsValid() throws Exception {
        final File wd = getWorkDir();
        final File cache = new File(wd,"cache");
        cache.mkdirs();
        final LuceneIndex index = LuceneIndex.create(cache, new KeywordAnalyzer());
        //Empty index => invalid
        assertEquals(Index.Status.EMPTY, index.getStatus(true));

        clearValidityCache(index);
        List<String> refs = new ArrayList<String>();
        refs.add("A");
        Set<String> toDel = new HashSet<String>();
        index.store(
                refs,
                toDel,
                new StrToDocConvertor("resources"),
                new StrToQueryCovertor("resource"),
                true);
        //Existing index => valid
        assertEquals(Index.Status.VALID, index.getStatus(true));
        assertTrue(cache.listFiles().length>0);

        clearValidityCache(index);
        createLock(index);
        //Index with orphan lock => invalid
        assertEquals(Index.Status.INVALID, index.getStatus(true));
        assertTrue(cache.listFiles().length==0);

        refs.add("B");
        clearValidityCache(index);
        index.store(
                refs,
                toDel,
                new StrToDocConvertor("resources"),
                new StrToQueryCovertor("resource"),
                true);
        assertEquals(Index.Status.VALID, index.getStatus(true));
        assertTrue(cache.listFiles().length>0);

        //Broken index => invalid
        clearValidityCache(index);
        File bt = null;
        for (File file : cache.listFiles()) {
            // either compound file or filds information must be present
            if (file.getName().endsWith(".cfs") || file.getName().endsWith(".fnm")) {
                bt = file;
                break;
            }
        }
        assertNotNull(bt);
        FileOutputStream out = new FileOutputStream(bt);
        try {
            out.write(new byte[] {0,0,0,0,0,0,0,0,0,0}, 0, 10);
        } finally {
            out.close();
        }
        assertEquals(Index.Status.INVALID, index.getStatus(true));
        assertTrue(cache.listFiles().length==0);
        
    }
    
    public void testAssertNoModifiedWriter() throws IOException {
        final File wd = getWorkDir();
        final File cache = new File (wd,"cache");   //NOI18N
        cache.mkdir();
        Index index = IndexManager.createIndex(cache, new KeywordAnalyzer());
        assertTrue(index instanceof Runnable);
        final Logger log = Logger.getLogger("org.netbeans.modules.parsing.lucene.LuceneIndex");
        log.setLevel(Level.WARNING);
        final WarningHandler handler = new WarningHandler();
        log.addHandler(handler);
        
        //No TX store -> no warning
        handler.clear();
        List<String> refs = new ArrayList<String>();
        refs.add("A");
        Set<String> toDel = new HashSet<String>();
        index.store(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"),
            true);
        assertFalse(handler.hasWarning());

        //TX store  + commit -> no warning
        handler.clear();
        refs.clear();
        refs.add("B");
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).commit();
        assertFalse(handler.hasWarning());

        //TX store  + rollback -> no warning
        handler.clear();
        refs.clear();
        refs.add("C");
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).rollback();
        assertFalse(handler.hasWarning());
        
        //No TX store + commit -> no warning
        handler.clear();
        refs.clear();
        ((Runnable)index).run();
        ((Index.Transactional)index).commit();

        //2*( TX store  + commit) -> no warning
        handler.clear();
        refs.clear();
        refs.add("D");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).commit();
        refs.clear();
        refs.add("E");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).commit();
        assertFalse(handler.hasWarning());
        
        //TX store  + rollback + TX store + commit -> no warning
        handler.clear();
        refs.clear();
        refs.add("F");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).rollback();
        refs.clear();
        refs.add("G");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).commit();
        assertFalse(handler.hasWarning());
        
        //TX store  + NO commit + TX store + commit -> warning
        handler.clear();
        refs.clear();
        refs.add("H");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        //Forgot to call commit - should cause warning
        refs.clear();
        refs.add("I");
        ((Runnable)index).run();
        ((Index.Transactional)index).txStore(
            refs,
            toDel,
            new StrToDocConvertor("resources"),
            new StrToQueryCovertor("resource"));
        ((Index.Transactional)index).commit();
        assertTrue(handler.hasWarning());
    }

    private void createLock(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field dirCache = li.getDeclaredField("dirCache");   //NOI18N
        dirCache.setAccessible(true);
        Object  o = dirCache.get(index);
        final java.lang.reflect.Field directory = o.getClass().getDeclaredField("fsDir");   //NOI18N
        directory.setAccessible(true);
        Directory dir = (Directory) directory.get(o);
        dir.makeLock("test").obtain();   //NOI18N
    }


    private void clearValidityCache(final LuceneIndex index) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        final Class<LuceneIndex> li = LuceneIndex.class;
        final java.lang.reflect.Field dirCache = li.getDeclaredField("dirCache");   //NOI18N
        dirCache.setAccessible(true);
        Object  o = dirCache.get(index);
        final java.lang.reflect.Field reader = o.getClass().getDeclaredField("reader");
        reader.setAccessible(true);
        IndexReader r = (IndexReader) reader.get(o);
        if (r != null) {
            r.close();
        }
        reader.set(o,null);
    }
    
    private static class StrToDocConvertor implements Convertor<String, Document>{
        
        private final String name;
        
        public StrToDocConvertor(final String name) {
            this.name = name;
        }
        
        @Override
        public Document convert(final String p) {
            final Document doc = new Document();
            doc.add(new Field(name, p, Field.Store.YES, Field.Index.ANALYZED));
            return doc;
        }        
    }
    
    private static class StrToQueryCovertor implements Convertor<String, Query> {
        
        private final String name;
        
        public StrToQueryCovertor(final String name) {
            this.name = name;
        }
        
        @Override
        public Query convert(String p) {
            return new TermQuery(new Term(name, p));
        }        
    }
    
    private static class WarningHandler extends Handler {
        
        private volatile boolean warning;

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel() == Level.WARNING && record.getThrown() != null) {
                warning = true;
            }
        }
        
        public void clear() {
            warning = false;
        }
        
        public boolean hasWarning() {
            return warning;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
        
    }
    
}

