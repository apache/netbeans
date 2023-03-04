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
package org.netbeans.api.java.queries;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.fail;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceJavadocAttacherImplementation;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class SourceJavadocAttacherTest extends NbTestCase implements SourceJavadocAttacher.AttachmentListener {
    private static final RequestProcessor TESTRP = new RequestProcessor(SourceJavadocAttacherTest.class);
    
    /**
     * Attacher runs asynchronously, forks into RP
     */
    private boolean async;
    
    /**
     * Blocks the attacher just at the time of its execution (potentially in the async thread) before it 
     * calls to the listener.
     */
    private Semaphore processingBlock = new Semaphore(0);
    
    /**
     * Released after the <b>AttacherImpl</b> calls the listener. Some of those
     * calls should not reach the final listener.
     */
    private Semaphore listenerBlock = new Semaphore(0);
    
    /**
     * Released when the attacher gets called. That means the prev attacher has
     * completed somehow.
     */
    private Semaphore attacherReached = new Semaphore(0);
    
    /**
     * Released when the final listener gets called.
     */
    private CountDownLatch finalListenerBlock = new CountDownLatch(1);

    /**
     * First attacher
     */
    private volatile AttacherImpl impl1;

    /**
     * Second attacher
     */
    private volatile AttacherImpl impl2;
    
     /**
     * Contex for the attachJavadoc/Source. Defaults to null = default Lookup.
     */
    Lookup context = null;
    
    /**
     * The first attacher, null (the default) means {@link #first}
     */
    AttacherImpl first;
    
   public SourceJavadocAttacherTest(String name) {
        super(name);
    }

    public SourceJavadocAttacherTest(boolean async, String name) {
        super(name);
        this.async = async;
    }
    
    public static class AsyncSource extends SourceJavadocAttacherTest {
        public AsyncSource(String name) {
            super(true, name);
        }
    }
    
    public static class Javadoc extends SourceJavadocAttacherTest {
        public Javadoc(String name) {
            super(false, name);
        }

        public Javadoc(boolean async, String name) {
            super(async, name);
        }

        @Override
        protected URL otherRes(AttacherImpl impl) {
            return impl.source;
        }

        @Override
        protected URL res(AttacherImpl impl) {
            return impl.javadoc;
        }

        @Override
        protected void doAttach(URL u) {
            SourceJavadocAttacher.attachJavadoc(u, context, this);
        }
        
        
    }
    public static class AsyncJavadoc extends Javadoc {
        public AsyncJavadoc(String name) {
            super(true, name);
        }
    }

    URL u(String file) throws IOException {
        return  new URL(getWorkDir().toURI().toURL(), file);
    }
    
    AtomicInteger succeeded = new AtomicInteger(0);
    AtomicInteger failed = new AtomicInteger(0);
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTestSuite(SourceJavadocAttacherTest.class);
        s.addTestSuite(AsyncSource.class);
        s.addTestSuite(Javadoc.class);
        s.addTestSuite(AsyncJavadoc.class);
        return s;
    }

    @Override
    protected void tearDown() throws Exception {
        // give enough permits so all async Attachers complete, and do not block the RP.
        processingBlock.release(100);
        super.tearDown();
    }

    @Override
    public void attachmentSucceeded() {
        succeeded.incrementAndGet();
        finalListenerBlock.countDown();
    }

    @Override
    public void attachmentFailed() {
        failed.incrementAndGet();
        finalListenerBlock.countDown();
    }
    
    protected URL res(AttacherImpl impl) {
        return impl.source;
    }
    
    protected URL otherRes(AttacherImpl impl) {
        return impl.javadoc;
    }
    
    /**
     * Checks that first attacher is called, and the 2nd never gets even called,
     * since the 1st accepts & succeeds.
     */
    public void testAttachFirst() throws Exception {
        impl1 = new AttacherImpl(true, null);
        impl2 = new AttacherImpl(true, null);
        
        MockLookup.setInstances(impl1, impl2);

        assertAsyncReturnedAtStart(u("a"), true);
        
        finalListenerBlock.await();
        
        // first attacher contacted
        assertNotNull(res(impl1));
        assertNull(otherRes(impl1));

        // 2nd attacher not reached
        assertNull(res(impl2));
        assertNull(otherRes(impl2));
        
        // listener called exactly once:
        assertEquals(1, succeeded.get());
        assertEquals(0, failed.get());
    }
    
    protected void doAttach(URL u) {
        SourceJavadocAttacher.attachSources(u, context, this);
    }
    
    private void assertAsyncReturnedAtStart(URL u, boolean all) throws Exception {
        if (first == null) {
            first = impl1;
        }
        if (!async) {
            processingBlock.release(3);
        }
        doAttach(u);
        
        if (async) {
            // 1st already contacted
            assertNotNull(res(first));
            assertNull(otherRes(first));
            
            processingBlock.release(all ? 3 : 1);
        }
    }
    
    /**
     * Checks that 2nd Attacher gets the call, since 1st rejects the resource.
     */
    public void testAttachFallback() throws Exception {
        impl1 = new AttacherImpl(true, (u) -> false);
        impl2 = new AttacherImpl(true, null);
        
        MockLookup.setInstances(impl1, impl2);
        assertAsyncReturnedAtStart(u("a"), true);
        
        finalListenerBlock.await();
        
        // first attacher contacted
        assertNotNull(res(impl1));
        assertNull(otherRes(impl1));

        // 2nd attacher reached as well
        assertNotNull(res(impl2));
        assertNull(otherRes(impl2));
        
        // listener called exactly once:
        assertEquals(1, succeeded.get());
        assertEquals(0, failed.get());
    }
    
    /**
     * Checks that 2nd Attacher is called when the 1st accepts, but fails.
     */
    public void testAttachWhenPreviousFails() throws Exception {
        impl1 = new AttacherImpl(false, null);
        impl2 = new AttacherImpl(true, null);
        
        MockLookup.setInstances(impl1, impl2);
        assertAsyncReturnedAtStart(u("a"), false);
        
        attacherReached.acquire();
        assertEquals("Failure not reported YET", 0, failed.get());
        
        processingBlock.release(3);

        finalListenerBlock.await();

        // first attacher contacted
        assertNotNull(res(impl1));
        assertNull(otherRes(impl1));

        // 2nd attacher reached as well
        assertNotNull(res(impl2));
        assertNull(otherRes(impl2));
        
        // listener called exactly once:
        assertEquals(1, succeeded.get());
        assertEquals(0, failed.get());
    }

    /**
     * Checks that context-provided Attacher is used first, before the default
     * Lookup.
     */
    public void testContextAttacherPrecedence() throws Exception {
        AttacherImpl c = new AttacherImpl(true, null);
        impl1 = new AttacherImpl(true, null);
        impl2 = new AttacherImpl(true, null);
        
        first = c;
        
        MockLookup.setInstances(impl1, impl2);
        context = Lookups.fixed(c);
        
        assertAsyncReturnedAtStart(u("a"), false);
        
        finalListenerBlock.await();
        
        // the 'c' has accepted:
        
        assertNotNull(res(c));
        assertNull(otherRes(c));

        assertNull(res(impl1));
        assertNull(otherRes(impl1));
    }
    
    class AttacherImpl implements SourceJavadocAttacherImplementation {
        final boolean succeeds;
        final Predicate<URL> acceptor;
        
        volatile URL source;
        volatile URL javadoc;
        
        volatile Throwable unexpectedException;

        public AttacherImpl(boolean succeeds, Predicate<URL> acceptor) {
            this.succeeds = succeeds;
            this.acceptor = acceptor == null ? (u) -> true : acceptor;
        }
        
        @Override
        public boolean attachSources(URL root, SourceJavadocAttacher.AttachmentListener listener) throws IOException {
            assertNull(source);
            source = root;
            return doComputeAttachment(root, succeeds, listener);
        }
        
        @Override
        public boolean attachJavadoc(URL root, SourceJavadocAttacher.AttachmentListener listener) throws IOException {
            assertNull(javadoc);
            javadoc = root;
            return doComputeAttachment(root, succeeds, listener);
        }
        
        protected boolean doComputeAttachment(URL root, boolean source, SourceJavadocAttacher.AttachmentListener listener) {
            if (!acceptor.test(root)) {
                return false;
            }
            attacherReached.release();
            Runnable r = () -> {
                try {
                    processingBlock.acquire();
                } catch (InterruptedException ex) {
                    unexpectedException = ex;
                    fail();
                }
                if (succeeds) {
                    listener.attachmentSucceeded();
                } else {
                    listener.attachmentFailed();
                }
                listenerBlock.release();
            };

            if (async) {
                TESTRP.post(r);
            } else {
                r.run();
            }
            return true;
        }
    }
}
