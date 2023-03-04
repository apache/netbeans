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
package org.netbeans.modules.parsing.impl.indexing;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class ArchiveTimeStampsTest extends IndexingTestBase {

    public ArchiveTimeStampsTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        CacheFolder.setCacheFolder(wd);
    }

    public void testArchiveTimeStamps_BatchStore() throws Exception {
        final H h = new H();
        final Logger log = Logger.getLogger(ArchiveTimeStamps.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            final Map<Pair<String,Integer>,Integer> indexers =
                new HashMap<Pair<String, Integer>, Integer>() {
                {
                    put(Pair.<String,Integer>of("java",1),0);       //NOI18N
                    put(Pair.<String,Integer>of("groovy",1),0);     //NOI18N
                }
            };
            final Map<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> expected =
                    new HashMap<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>>();
            final File base = getWorkDir();
            for (int i=1; i<=100; i++) {
                final File f = new File(base,String.format("archive%d.jar", i));    //NOI18N
                final URL u = Utilities.toURI(f).toURL();
                final Pair<Long,Map<Pair<String,Integer>,Integer>> state =
                        Pair.<Long,Map<Pair<String,Integer>,Integer>>of((long)i,indexers);
                expected.put(u, state);
                if (i == 99) {
                    h.last = true;
                }
                ArchiveTimeStamps.setLastModified(u,state);
            }
            assertTrue(h.await());
            assertEquals(1, h.count.get());
            for (Map.Entry<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> e : expected.entrySet()) {
                final Pair<Long,Map<Pair<String,Integer>,Integer>> state = ArchiveTimeStamps.getLastModified(e.getKey());
                assertEquals(e.getValue(), state);
            }
        } finally {
            log.removeHandler(h);
        }
    }

    public void testArchiveTimeStamps_ScatteredStore() throws Exception {
        final H h = new H();
        final Logger log = Logger.getLogger(ArchiveTimeStamps.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            final Map<Pair<String,Integer>,Integer> indexers =
                new HashMap<Pair<String, Integer>, Integer>() {
                {
                    put(Pair.<String,Integer>of("java",1),0);       //NOI18N
                    put(Pair.<String,Integer>of("groovy",1),0);     //NOI18N
                }
            };
            final Map<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> expected =
                    new HashMap<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>>();
            final File base = getWorkDir();
            for (int i=1; i<=100; i++) {
                final File f = new File(base,String.format("archive%d.jar", i));    //NOI18N
                final URL u = Utilities.toURI(f).toURL();
                final Pair<Long,Map<Pair<String,Integer>,Integer>> state =
                        Pair.<Long,Map<Pair<String,Integer>,Integer>>of((long)i,indexers);
                expected.put(u, state);
                if (i%30 == 0 || i == 100) {
                    h.last = true;
                }
                ArchiveTimeStamps.setLastModified(u,state);
                if (i%30 == 0) {
                    assertTrue(h.await());
                }
            }
            assertTrue(h.await());
            for (Map.Entry<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> e : expected.entrySet()) {
                final Pair<Long,Map<Pair<String,Integer>,Integer>> state = ArchiveTimeStamps.getLastModified(e.getKey());
                assertEquals(e.getValue(), state);
            }
        } finally {
            log.removeHandler(h);
        }
    }

    public void testArchiveTimeStamps_CahangeDuringStore() throws Exception {
        final H h = new H();
        final Logger log = Logger.getLogger(ArchiveTimeStamps.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(h);
        try {
            final Map<Pair<String,Integer>,Integer> indexers =
                new HashMap<Pair<String, Integer>, Integer>() {
                {
                    put(Pair.<String,Integer>of("java",1),0);       //NOI18N
                    put(Pair.<String,Integer>of("groovy",1),0);     //NOI18N
                }
            };
            final Map<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> expected =
                    new HashMap<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>>();
            final File base = getWorkDir();
            h.storingHook = new Runnable() {
                @Override
                public void run() {
                    try {
                        File f = new File(base,String.format("ar_bs.jar"));    //NOI18N
                        Pair<Long,Map<Pair<String,Integer>,Integer>> state =
                            Pair.<Long,Map<Pair<String,Integer>,Integer>>of(1L,indexers);
                        URL u = Utilities.toURI(f).toURL();
                        expected.put(u, state);
                        ArchiveTimeStamps.setLastModified(u,state);
                    } catch (IOException e) {
                        ArchiveTimeStampsTest.<Void>rethrowAsUnchecked(e);
                    } finally {
                        h.storingHook = null;
                    }
                }
            };
            h.storedHook = new Runnable() {
                @Override
                public void run() {
                    try {
                        File f = new File(base,String.format("ar_as.jar"));    //NOI18N
                        Pair<Long,Map<Pair<String,Integer>,Integer>> state =
                            Pair.<Long,Map<Pair<String,Integer>,Integer>>of(1L,indexers);
                        URL u = Utilities.toURI(f).toURL();
                        expected.put(u, state);
                        ArchiveTimeStamps.setLastModified(u,state);
                    } catch (IOException e) {
                        ArchiveTimeStampsTest.<Void>rethrowAsUnchecked(e);
                    } finally {
                        h.storedHook = null;
                    }
                }
            };
            {
                File f = new File(base,String.format("start.jar"));    //NOI18N
                Pair<Long,Map<Pair<String,Integer>,Integer>> state =
                    Pair.<Long,Map<Pair<String,Integer>,Integer>>of(1L,indexers);
                URL u = Utilities.toURI(f).toURL();
                expected.put(u, state);
                h.last = true;
                ArchiveTimeStamps.setLastModified(u,state);
            }
            assertTrue(h.await());
            assertEquals(3, expected.size());
            for (Map.Entry<URL,Pair<Long,Map<Pair<String,Integer>,Integer>>> e : expected.entrySet()) {
                final Pair<Long,Map<Pair<String,Integer>,Integer>> state = ArchiveTimeStamps.getLastModified(e.getKey());
                assertEquals(e.getValue(), state);
            }
        } finally {
            log.removeHandler(h);
        }
    }

    private static class H extends Handler {

        private final AtomicInteger count = new AtomicInteger();
        private final Semaphore sem = new Semaphore(0);
        private volatile boolean last;
        private volatile Runnable storingHook;
        private volatile Runnable storedHook;

        @Override
        public void publish(LogRecord record) {
            if ("STORED".equals(record.getMessage())) {     //NOI18N
                try {
                    final Runnable toCall = storedHook;
                    if (toCall != null) {
                        toCall.run();
                    }
                } finally {
                    count.incrementAndGet();
                    if (last) {
                        sem.release();
                    }
                }
            } else if ("STORING".equals(record.getMessage())) { //NOI18N
                final Runnable toCall = storingHook;
                if (toCall != null) {
                    toCall.run();
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean await() throws InterruptedException {
            return sem.tryAcquire(4*ArchiveTimeStamps.SAVE_DELAY, TimeUnit.MILLISECONDS);
        }

        public int getAndSetCount(int value) {
            return count.getAndSet(value);
        }
    }

    private static <B> B rethrowAsUnchecked (@NonNull final Throwable t) {
        return ArchiveTimeStampsTest.<B,RuntimeException>rethrowAsErasedThrowable(t);
    }

    @SuppressWarnings("unchecked")
    private static <B,T extends Throwable>
     B rethrowAsErasedThrowable(@NonNull final Throwable t) throws T {
      throw (T) t;
   }
}
