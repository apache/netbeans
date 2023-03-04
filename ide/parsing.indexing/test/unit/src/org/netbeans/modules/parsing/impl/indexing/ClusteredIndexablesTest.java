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

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.util.Exceptions;

/**
 *
 * @author vita
 */
public class ClusteredIndexablesTest {

    public ClusteredIndexablesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSimple() {
        List<Indexable> textPlains = Arrays.asList(new Indexable [] {
            SPIAccessor.getInstance().create(new TestIndexable("foo/indexable1", "text/plain")),
            SPIAccessor.getInstance().create(new TestIndexable("foo/indexable2", "text/plain")),
            SPIAccessor.getInstance().create(new TestIndexable("foo/indexable3", "text/plain")),
        });
        List<Indexable> javas = Arrays.asList(new Indexable [] {
            SPIAccessor.getInstance().create(new TestIndexable("java/indexable1", "text/x-java")),
            SPIAccessor.getInstance().create(new TestIndexable("java/indexable2", "text/x-java")),
        });
        List<Indexable> xmls = Arrays.asList(new Indexable [] {
            SPIAccessor.getInstance().create(new TestIndexable("xml/indexable1", "text/xml")),
            SPIAccessor.getInstance().create(new TestIndexable("xml/indexable2", "text/xml")),
            SPIAccessor.getInstance().create(new TestIndexable("xml/indexable3", "text/xml")),
            SPIAccessor.getInstance().create(new TestIndexable("xml/indexable4", "text/xml")),
            SPIAccessor.getInstance().create(new TestIndexable("xml/indexable5", "text/xml")),
        });

        List<Indexable> indexables = new ArrayList<Indexable>();
        indexables.addAll(textPlains);
        indexables.addAll(javas);
        indexables.addAll(xmls);
        Collections.shuffle(indexables);

        ClusteredIndexables ci = new ClusteredIndexables(indexables);

        // when asking for all mime types we should get everything
        List<Indexable> all = toList(ci.getIndexablesFor(null));
        check("Wrong all indexables", indexables, all);

        List<Indexable> tp = toList(ci.getIndexablesFor("text/plain"));
        check("Wrong text/plain indexables", textPlains, tp);

        List<Indexable> j = toList(ci.getIndexablesFor("text/x-java"));
        check("Wrong text/x-java indexables", javas, j);

        List<Indexable> x = toList(ci.getIndexablesFor("text/xml"));
        check("Wrong text/xml indexables", xmls, x);

        List<Indexable> allAgain = toList(ci.getIndexablesFor(null));
        check("Wrong all indexables", indexables, allAgain);
    }

    @Test
    public void testRaceIssue222383() throws Exception {
        final ClusteredIndexables.AttachableDocumentIndexCache cache = ClusteredIndexables.createDocumentIndexCache();
        final ClusteredIndexables indexables1 = new ClusteredIndexables(Collections.<Indexable>emptyList());
        final ClusteredIndexables indexables2 = new ClusteredIndexables(Collections.<Indexable>emptyList());
        final CountDownLatch indexing = new CountDownLatch(1);
        final CountDownLatch changed = new CountDownLatch(1);
        final AtomicReference<Throwable> res = new AtomicReference<Throwable>();
        final Thread fastChangeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    indexing.await();
                } catch (InterruptedException ex) {
                    res.set(ex);
                    return;
                }
                try {
                    cache.attach(ClusteredIndexables.INDEX, indexables2);
                    cache.detach();
                } catch (IllegalStateException e) {
                    res.set(e);
                } finally {
                    changed.countDown();
                }
            }
        });
        fastChangeThread.start();
        cache.attach(ClusteredIndexables.INDEX, indexables1);
        indexing.countDown();
        changed.await();
        Assert.assertEquals(indexables1, getCacheField(cache, "indexIndexables"));  //NOI18N
        cache.detach();
        Assert.assertTrue(res.get() instanceof IllegalStateException);
    }

    @Test
    public void testRaceIssue222383TransientUpdate() throws Exception {
        final ClusteredIndexables.AttachableDocumentIndexCache cache = ClusteredIndexables.createDocumentIndexCache();
        final ClusteredIndexables indexables1 = new ClusteredIndexables(Collections.<Indexable>emptyList());
        final ClusteredIndexables indexables2 = new ClusteredIndexables(Collections.<Indexable>emptyList());
        final CountDownLatch indexing = new CountDownLatch(1);
        final CountDownLatch changed = new CountDownLatch(1);
        final AtomicBoolean success = new AtomicBoolean();
        final Thread fastChangeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TransientUpdateSupport.setTransientUpdate(true);
                try {
                    indexing.await();
                    cache.attach(ClusteredIndexables.INDEX, indexables2);
                    cache.detach();
                    success.set(true);
                } catch (InterruptedException ioe) {
                    //Ignore
                } finally {
                    TransientUpdateSupport.setTransientUpdate(false);
                    changed.countDown();
                }
            }
        });
        fastChangeThread.start();
        cache.attach(ClusteredIndexables.INDEX, indexables1);
        indexing.countDown();
        changed.await();
        Assert.assertEquals(indexables1, getCacheField(cache, "indexIndexables"));  //NOI18N
        cache.detach();
        Assert.assertTrue(success.get());
    }

    private Object getCacheField(
       @NonNull final ClusteredIndexables.AttachableDocumentIndexCache cache,
       @NonNull final String fieldName) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        final Class<?> clz = Class.forName("org.netbeans.modules.parsing.impl.indexing.ClusteredIndexables$DocumentIndexCacheImpl");    //NOI18N
        final Field f = clz.getDeclaredField(fieldName);
        if (f == null) {
            return null;
        }
        f.setAccessible(true);
        return f.get(cache);
    }

    private void check(String message, Collection<Indexable> indexableImpls, Collection<Indexable> indexables) {
        Assert.assertEquals(message, indexableImpls.size(), indexables.size());

        Map<String, String> iiMap = new HashMap<String, String>();
        for(Indexable ii : indexableImpls) {
            iiMap.put(ii.getRelativePath(), ii.getMimeType());
        }

        Map<String, String> iMap = new HashMap<String, String>();
        for(Indexable i : indexables) {
            iMap.put(i.getRelativePath(), i.getMimeType());
        }

        Assert.assertEquals(message, iiMap, iMap);
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        Assert.assertNotNull(iterable);
        List<T> list = new ArrayList<T>();
        for(T o : iterable) {
            list.add(o);
        }
        return list;
    }

    private static final class TestIndexable implements IndexableImpl {

        private final String relativePath;
        private final String mimeType;

        public TestIndexable(String relativePath, String mimeType) {
            this.relativePath = relativePath;
            this.mimeType = mimeType;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public URL getURL() {
            try {
                return new URL(relativePath);
            } catch (MalformedURLException ex) {
                return null;
            }
        }

        public String getMimeType() {
            return mimeType;
        }

        public boolean isTypeOf(String mimeType) {
            return mimeType.equals(this.mimeType);
        }

    } // End of TestIndexable class
}
