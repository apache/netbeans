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
package org.netbeans.modules.projectapi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public class SimpleFileOwnerQueryImplementationTest extends NbTestCase {

    private SimpleFileOwnerQueryImplementation instance;
    private MockHandler handler;
    private Level origLogLevel;

    public SimpleFileOwnerQueryImplementationTest(@NonNull final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        resetSimpleFileOwnerQueryImplementation();
        instance = new SimpleFileOwnerQueryImplementation();
        handler = new MockHandler();
        final Logger log = Logger.getLogger(SimpleFileOwnerQueryImplementation.class.getName());
        origLogLevel = log.getLevel();
        log.setLevel(Level.FINEST);
        log.addHandler(handler);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        final Logger log = Logger.getLogger(SimpleFileOwnerQueryImplementation.class.getName());
        log.removeHandler(handler);
        log.setLevel(origLogLevel);
    }


    public void testUncontended() throws IOException {
        handler.expect(
                "External Roots State: NEW",        //NOI18N
                "External Roots Deserialized",      //NOI18N
                "External Roots State: LOADED");    //NOI18N
        assertNull(instance.getOwner(BaseUtilities.toURI(FileUtil.normalizeFile(getWorkDir()))));   //NEW
        assertNull(instance.getOwner(BaseUtilities.toURI(FileUtil.normalizeFile(getWorkDir()))));   //LOADED
        handler.assertExpected();
    }

    public void testContended() throws IOException, InterruptedException {
        final CountDownLatch contended = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);
        handler.expect(
                "External Roots State: NEW",        //NOI18N
                "External Roots Deserialized",      //NOI18N
                "External Roots State: LOADING",    //NOI18N
                "External Roots State: LOADED");    //NOI18N
        handler.on("External Roots Deserialized", new Runnable() {
            @Override
            public void run() {
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            assertNull(instance.getOwner(BaseUtilities.toURI(FileUtil.normalizeFile(getWorkDir()))));   //NEW
                            finished.countDown();
                        } catch (IOException ioe) {
                            throw new RuntimeException(ioe);
                        }
                    }
                });
                t.start();
                try {
                    contended.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        handler.on("External Roots State: LOADING", new Runnable() {
            @Override
            public void run() {
                contended.countDown();
            }
        });
        assertNull(instance.getOwner(BaseUtilities.toURI(FileUtil.normalizeFile(getWorkDir()))));   //NEW
        finished.await();
        assertNull(instance.getOwner(BaseUtilities.toURI(FileUtil.normalizeFile(getWorkDir()))));   //LOADED
        handler.assertExpected();
    }

    private static void resetSimpleFileOwnerQueryImplementation() throws ReflectiveOperationException {
        final Class valClz = Class.forName("org.netbeans.modules.projectapi.SimpleFileOwnerQueryImplementation$ExternalRootsState");
        final Field val = valClz.getDeclaredField("NEW");
        val.setAccessible(true);
        final Class<SimpleFileOwnerQueryImplementation> clz = SimpleFileOwnerQueryImplementation.class;
        final Field fld = clz.getDeclaredField("externalRootsState");
        fld.setAccessible(true);
        fld.set(null, val.get(null));
    }

    private static final class MockHandler extends Handler {
        private static final boolean VERBOSE = false;
        //@GuardedBy("this")
        private final Queue<String> expected = new ArrayDeque<String>();
        //@GuardedBy("this")
        private final Map<String,Runnable> actions = new HashMap<String, Runnable>();

        synchronized void expect(String... messages) {
            expected.clear();
            actions.clear();
            Collections.addAll(expected, messages);
        }

        synchronized void on(String msg, Runnable action) {
            actions.put(msg, action);
        }

        synchronized void assertExpected() {
            assertTrue(expected.isEmpty());
        }

        @Override
        public void publish(LogRecord record) {
            Runnable r = null;
            synchronized (this) {
                String message = record.getMessage();
                if (message != null) {
                    final Object[] params = record.getParameters();
                    if (params != null && params.length > 0) {
                        message = MessageFormat.format(message, params);
                    }
                    if (message.equals(expected.peek())) {
                        if (VERBOSE) {
                            System.out.println(message);
                        }
                        expected.remove();
                    }
                    r = actions.get(message);
                }
            }
            if (r != null) {
                r.run();
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
