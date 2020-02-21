/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.disk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.repository.RepositoryCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.PersistentFactory;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class MemoryCacheTest extends NbTestCase {

    private static final boolean TRACE = false;
    private static final int K = 1000;
    private static final int M = K * K;
    private static final int NUMBER_OF_THREADS = 5;

    /** Creates a new instance of BaseTestCase */
    public MemoryCacheTest() {
        super("MemoryCacheTest");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    public void testCache() throws Exception {
        RepositoryCache cache = new RepositoryCache();
        final AtomicBoolean stopFlag = new AtomicBoolean();
        RequestProcessor processor = new RequestProcessor("processor", NUMBER_OF_THREADS + 1);
        List<RequestProcessor.Task> tasks = new ArrayList<RequestProcessor.Task>(NUMBER_OF_THREADS);
        for (int i = 0; i < NUMBER_OF_THREADS; ++i) {
            MyProcess process;
            if (i == 0) {
                process = new MyProcess(cache, i, 5 * M, 10 * M, true, stopFlag);
            } else {
                process = new MyProcess(cache, i, 10 * M, K, false, stopFlag);

            }
            tasks.add(processor.post(process));
        }
        processor.post(new Runnable() {
            @Override
            public void run() {
                stopFlag.set(true);
            }

        }, 60000); // limit execution time to 1 minute
        for (RequestProcessor.Task task : tasks) {
            task.waitFinished();
        }
        processor.stop();
    }

    private static final class MyProcess implements Runnable {

        private static final int CASES = 10;
        private final int max_loop;
        private final int max_key;
        private final RepositoryCache cache;
        private final int process;
        private final boolean onlySoft;
        private final AtomicBoolean stopFlag;

        private MyProcess(RepositoryCache cache, int process, int max_loop, int max_key, boolean onlySoft, AtomicBoolean stopFlag) {
            this.cache = cache;
            this.process = process;
            this.max_loop = max_loop;
            this.max_key = max_key;
            this.onlySoft = onlySoft;
            this.stopFlag = stopFlag;
        }

        @Override
        public void run() {
            if (TRACE) {
                System.out.println("Started " + process);
            }
            for (int i = 0; i < max_loop && !stopFlag.get(); ++i) {
                int c;
                if (onlySoft) {
                    c = 0;
                } else {
                    c = (int) (1000 * Math.random());
                }
                double d = Math.random();
                int k = (int) (max_key * d);
                MyKey myKey = new MyKey(k);
                switch (c % CASES) {
                    case 0:
                        cache.putIfAbsent(myKey, new MyPersistent(d));
                        if (onlySoft && (i % (50 * K)) == 0) {
                            cache.clearSoftRefs();
                        }
                        break;
                    case 1:
                        cache.put(myKey, new MyPersistent(d));
                        break;
                    case 2:
                        cache.hang(myKey, new MyPersistent(d));
                        break;
                    case 3:
                        cache.remove(myKey);
                        break;
                    default:
                        cache.get(myKey);
                        break;
                }
            }
            if (TRACE) {
                System.out.println("Finished " + process);
            }
        }
    }

    private static final class MyPersistent implements Persistent {

        private final double d;

        private MyPersistent(double d) {
            this.d = d;
        }
    }

    private static final class MyKey implements Key {

        int i;

        private MyKey(int i) {
            this.i = i;
        }
        
        @Override
        public int hashCode(int unitID) {
            return unitID;
        }

        @Override
        public int hashCode() {
            return hashCode(i);
        }

        @Override
        public final boolean equals(int thisUnitID, Key object, int objectUnitID) {
            return thisUnitID == objectUnitID;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyKey other = (MyKey) obj;
            return equals(i, other, other.i);
        }


        @Override
        public int getUnitId() {
            return 1;
        }

        @Override
        public PersistentFactory getPersistentFactory() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CharSequence getUnit() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Behavior getBehavior() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getDepth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public CharSequence getAt(int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSecondaryDepth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getSecondaryAt(int level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean hasCache() {
            return false;
        }
    }
}
