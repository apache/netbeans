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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.Query;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class NativeFSLockFactoryTest extends NbTestCase {
    
    private File indexFolder;

    public NativeFSLockFactoryTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        indexFolder = new File(getWorkDir(),"index");   //NOI18N
        assertTrue(indexFolder.mkdirs());
    }


    public void testLockFreedDuringStoreError() throws Exception {
        final LuceneIndex index = LuceneIndex.create(indexFolder, new KeywordAnalyzer());
        final Collection<? extends Integer> dataSet = generateDataSet(1000);
        final Logger log = Logger.getLogger(LuceneIndex.class.getName());
        final FieldType ft = new FieldType();
        ft.setStored(true);
        ft.setTokenized(true);
        final TestHandler handler = new TestHandler(
            new Runnable() {
                @Override
                public void run() {
                    //Break index a bit ;-)
                    for (File f : indexFolder.listFiles()) {
                        if (f.getName().startsWith("nb-lock")) {    //NOI18N
                            continue;
                        }
                        f.delete();
                    }                    
                }
        });
        log.setLevel(Level.FINE);
        log.addHandler(handler);
        boolean success = false;
        try {
            index.store(
                    dataSet,
                    Collections.<String>emptySet(),
                    new Convertor<Integer, Document>() {
                        @Override
                        public Document convert(Integer p) {
                            final Document doc = new Document();
                            doc.add(new Field(
                                    "val",                  //NOI18N
                                    Integer.toString(p),
                                    ft));
                            return doc;
                        }
                    },
                    new Convertor<>() {
                        @Override
                        public Query convert(String p) {
                            throw new UnsupportedOperationException();
                        }
                    },
                    true);
            success = true;
        } catch (Throwable t) {
            //Ignore - should be thrown and success should be false
        } finally {
            log.removeHandler(handler);
        }
        assertFalse(success);
        success = false;
        try {
            index.store(
                dataSet,
                Collections.emptySet(),
                new Convertor<Integer, Document>() {
                    @Override
                    public Document convert(Integer p) {
                        final Document doc = new Document();
                        doc.add(new Field(
                                "val",                  //NOI18N
                                Integer.toString(p),
                                ft));
                        return doc;
                    }
                },
                new Convertor<String, Query>() {
                    @Override
                    public Query convert(String p) {
                        throw new UnsupportedOperationException();
                    }
                },
                true);
            success = true;
        } catch (Throwable t) {
            //Should not be thrown and success should be true
            t.printStackTrace();
        }
        assertTrue(success);
    }



    private static Collection<? extends Integer> generateDataSet(final int count) {
        final List<Integer> res = new ArrayList<>(count);
        for (int i=0; i< count; i++) {
            res.add(i);
        }
        return res;
    }

    private static class TestHandler extends Handler {

        private final Runnable action;

        TestHandler(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            this.action = action;
        }

        @Override
        public void publish(LogRecord record) {
            if ("Committing {0}".equals(record.getMessage())) { //NOI18N
                action.run();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }
}
