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
