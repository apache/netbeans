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
package org.openide.loaders;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;

public class InstanceDataObjectSynchronizedTest extends NbTestCase {

    public InstanceDataObjectSynchronizedTest(String name) {
        super(name);
    }

    public void testSynchronization() throws Exception {
        FileSystem fs = new XMLFileSystem(this.getClass().getClassLoader().getResource("org/openide/loaders/data/InstanceDataObjectTest.xml"));
        FileObject fo = fs.findResource("testInstanceDefinitions/s.instance");
        assertNotNull(fo);
        final String filename = fo.getNameExt();
        DataObject dobj = DataObject.find(fo);
        final InstanceCookie ic = dobj.getLookup().lookup(InstanceCookie.class);
        assertNotNull(filename, ic);

        int numThreads = 10;

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(numThreads);
        final Set<TestSync> all = new CopyOnWriteArraySet<TestSync>();

        // first create some threads
        while (numThreads-- > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startSignal.await();
                        TestSync ts = (TestSync) ic.instanceCreate();
                        all.add(ts);
                        doneSignal.countDown();
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
        // now start them all
        startSignal.countDown();
        doneSignal.await();
        // this is certainly the goal:
        assertEquals("Only one instance of TestSync returned:\n" + all, 1, all.size());
        // the following may be hard to guarantee however:
        assertEquals("Just one instance of TestSync created:\n" + all, 1, TestSync.instanceCounter.get());
    }

    public static class TestSync {

        private static Exception prevCreator;
        private static final AtomicInteger instanceCounter = new AtomicInteger(0);
        final Exception created;

        TestSync() {
            synchronized (TestSync.class) {
                int previous = instanceCounter.incrementAndGet();
                created = new Exception("Instance " + previous);
                if (prevCreator != null) {
                    created.initCause(prevCreator);
                }
                prevCreator = created;
            }
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            created.printStackTrace(pw);
            return sw.toString();
        }
    }
    
}
