/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl;


import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.TestEnvironmentFactory;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Pair;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tomas Zezula
 */
public class RunWhenScanFinishedSupportTest extends NbTestCase {
    
    private static final int NEGATIVE_TIME = Integer.getInteger("RunWhenScanFinishedSupportTest.negative.timeout",5000);
    
    private static final String MIME_FOO = "text/x-foo";    //NOI18N
    private static final String FOO_EXT = "foo";    //NOI18N
    
    private Source src;
    private TestHandler handler;
    private Logger log;
    
    public RunWhenScanFinishedSupportTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        FileUtil.setMIMEType(FOO_EXT, MIME_FOO);
        final FileObject wd = FileUtil.toFileObject(getWorkDir());
        final FileObject file = FileUtil.createData(wd, "test.foo");    //NOI18N
        assertNotNull(file);
        MockLookup.setInstances(new MockMimeLookup(), new TestEnvironmentFactory(), new IndexerEmulator());
        src = Source.create(file);
        assertNotNull(src);
        handler = new TestHandler();
        log = Logger.getLogger(RunWhenScanFinishedSupport.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log.removeHandler(handler);
    }
    
    public void testPerformScanExclusiveToRunWhenScanFinished1() throws Exception {
        final TestTask task = new TestTask("task",null,-1,null);    //NOI18N
        final CountDownLatch taskRunning = handler.condition("runWhenScanFinished:entry", task);    //NOI18N
        final AtomicBoolean notCalledInConcurrent = new AtomicBoolean();
        final Runnable scan = new ScanTask(taskRunning, NEGATIVE_TIME, notCalledInConcurrent);
        final CountDownLatch scannerRunning = handler.condition("performScan:entry", scan); //NOI18N
        
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                RunWhenScanFinishedSupport.performScan(scan, Lookup.getDefault());
            }
        });
        t.start();
        scannerRunning.await();
        RunWhenScanFinishedSupport.runWhenScanFinished(task, Collections.singleton(src));
        t.join();
        assertTrue(notCalledInConcurrent.get());
    }
    
    private static final class TestTask implements Mutex.ExceptionAction<Void> {
        
        private final String name;
        private final CountDownLatch latch;
        private final int timeOut;
        private final AtomicBoolean resHolder;
        
        private TestTask(
                final String name,
                final CountDownLatch latch,
                final int timeOut,
                final AtomicBoolean res) {
            this.name = name;
            this.latch = latch;
            this.timeOut = timeOut;
            this.resHolder = res;
        }
        
        @Override
        public Void run() throws Exception {
            if (latch != null) {
                if (timeOut == -1) {
                    latch.await();
                } else {
                    final boolean res = !latch.await(timeOut, TimeUnit.MILLISECONDS);
                    if (resHolder != null) {
                        resHolder.set(res);
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name;
        }

    }
    
    private static class ScanTask implements Runnable {
        
        private final CountDownLatch latch;
        private final int timeOut;
        private final AtomicBoolean resHolder;
        
        private ScanTask(
            final CountDownLatch latch,
            final int timeOut,
            final AtomicBoolean resHolder) {
            this.latch = latch;
            this.timeOut = timeOut;
            this.resHolder = resHolder;
        }
        
        @Override
        public void run() {
            if (latch != null) {
                try {
                    final boolean res = !latch.await(timeOut, TimeUnit.MILLISECONDS);
                    if (resHolder != null) {
                        resHolder.set(res);
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        @Override
        public String toString() {
            return "scan";  //NOI18N
        }
    }
    
    private static class TestHandler extends Handler {
        
        private final Queue<Pair<Pair<String,Object>,CountDownLatch>> condition =
                new ConcurrentLinkedQueue<Pair<Pair<String, Object>, CountDownLatch>>();
        
        
        public CountDownLatch condition(@NonNull final String message, @NonNull final Object param) throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            condition.offer(Pair.<Pair<String,Object>,CountDownLatch>of(Pair.<String,Object>of(message,param),latch));
            return latch;
        }

        @Override
        public void publish(LogRecord record) {            
            final String message = record.getMessage();
            final Object param = record.getParameters()[0];
            for (Iterator<Pair<Pair<String, Object>, CountDownLatch>> it = condition.iterator(); it.hasNext();) {
                final Pair<Pair<String,Object>,CountDownLatch> cnd = it.next();
                if (cnd != null && cnd.first().first().equals(message) && cnd.first().second().equals(param)) {
                    //System.out.println("GOT: " + cnd.first.first + " " + cnd.first.second);
                    it.remove();
                    cnd.second().countDown();
                    break;
                }
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
