/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package threaddemo.locking;

import junit.framework.TestCase;
import threaddemo.locking.RWLock;
import threaddemo.locking.Locks;
import threaddemo.locking.PrivilegedLock;

/**
 * Test behavior of Locks.readWrite and PrivilegedLock.
 * @author Jaroslav Tulach, Ales Novak, Petr Hrebejk, Jesse Glick
 */
public class ReadWriteLockTest extends TestCase {

    private PrivilegedLock p;
    private RWLock m;

    public ReadWriteLockTest(String testName) {
        super(testName);
    }

    protected void setUp() {
        p = new PrivilegedLock();
        m = Locks.readWrite(p);
    }
    
    public void testReadWriteRead() throws Exception {
        
        final Object lock = new Object();
        
        synchronized ( lock ) {
            p.enterRead();
            
            new Thread() {
                public void run() {
                    synchronized( lock ) {
                        lock.notifyAll();
                    }
                    p.enterWrite();
                    synchronized ( lock ) {
                        lock.notifyAll();
                        p.exitWrite();
                    }
                }
            }.start();
            
            lock.wait();
            
        }
        Thread.sleep(100);
        
        p.enterRead();
        
        p.exitRead();
        
        synchronized ( lock ) {
            p.exitRead();
            lock.wait();
        }
        
        assertTrue(!m.canRead());
        assertTrue(!m.canWrite());
    }
    
    /** Simple test to execute read access and write access immediately.
     */
    public void testPostImmediately() {
        State s = new State();
        
        m.read(s);
        
        if (s.state != 1) {
            fail("Read request not started immediatelly");
        }
        
        m.write(s);
        
        if (s.state != 2) {
            fail("Write request not started immediately");
        }
    }
    
    // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds
    private static void asyncEnter(final PrivilegedLock p, final boolean X, final long timeout) throws InterruptedException {
        asyncEnter(p, X, timeout, null);
    }
    
    // starts a new thread, after return the thread will hold lock "p" in
    // mode X for timeout milliseconds, the new thread execs "run" first
    private static void asyncEnter(final PrivilegedLock p, final boolean X, final long timeout, final Runnable run) throws InterruptedException {
        final Object lock = new Object();
        
        synchronized (lock) {
            new Thread(new Runnable() {
                public void run() {
                    if (X) {
                        p.enterWrite();
                    } else {
                        p.enterRead();
                    }
                    
                    synchronized (lock) {
                        lock.notify();
                    }
                    
                    if (run != null) {
                        run.run();
                    }
                    
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    if (X) {
                        p.exitWrite();
                    } else {
                        p.exitRead();
                    }
                    
                }
            }).start();
            
            lock.wait();
        }
    }
    
    /** Tests enterWrite while the Lock is contended in X mode by
     * another thread
     */
    public void testXContendedX() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterWrite();
        p.exitWrite();
        
        consistencyCheck();
    }
    
    /** Tests enterRead while the Lock is contended in X mode by
     * another thread
     */
    public void testXContendedS() throws InterruptedException {
        asyncEnter(p, true, 2000);
        
        // first enter
        p.enterRead();
        p.exitRead();
        
        consistencyCheck();
    }
    
    /** Tests enterWrite while the Lock is contended in S mode by
     * another thread
     */
    public void testSContendedX() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterWrite();
        p.exitWrite();
        
        consistencyCheck();
    }
    
    /** Tests enterRead while the Lock is contended in S mode by
     * another thread
     */
    public void testSContendedS() throws InterruptedException {
        asyncEnter(p, false, 2000);
        
        // first enter
        p.enterRead();
        p.exitRead();
        
        consistencyCheck();
    }
    
    /** Checks the Lock is in the consistent state, i.e. enterWrite must pass */
    private void consistencyCheck() {
        p.enterWrite();
        p.exitWrite();
    }
    
    private static class State implements Runnable {
        public int state;
        
        public void run() {
            state++;
        }
        
    } // end of State
    
    // --- TESTS ADDED BY JGLICK ---
    
    public void testNestedEntries() throws Exception {
        // can go write -> read
        p.enterWrite();
        try {
            p.enterRead();
            p.exitRead();
        } finally {
            p.exitWrite();
        }
        // and write -> write -> read
        p.enterWrite();
        try {
            p.enterWrite();
            try {
                p.enterRead();
                p.exitRead();
            } finally {
                p.exitWrite();
            }
        } finally {
            p.exitWrite();
        }
        // and write -> read -> read
        p.enterWrite();
        try {
            p.enterRead();
            try {
                p.enterRead();
                p.exitRead();
            } finally {
                p.exitRead();
            }
        } finally {
            p.exitWrite();
        }
        // and even write -> write -> read -> read
        p.enterWrite();
        try {
            p.enterWrite();
            try {
                p.enterRead();
                try {
                    p.enterRead();
                    p.exitRead();
                } finally {
                    p.exitRead();
                }
            } finally {
                p.exitWrite();
            }
        } finally {
            p.exitWrite();
        }
        /* Don't bother testing this, it just deadlocks...
        // but read -> write is forbidden
        p.enterRead();
        try {
            boolean ok = true;
            try {
                p.enterWrite();
                ok = false;
                p.exitWrite();
            } catch (IllegalStateException e) {
                assertTrue(ok);
            }
        } finally {
            p.exitRead();
        }
        // so is write -> read -> write!
        p.enterWrite();
        try {
            p.enterRead();
            try {
                boolean ok = true;
                try {
                    p.enterWrite();
                    ok = false;
                    p.exitWrite();
                } catch (IllegalStateException e) {
                    assertTrue(ok);
                }
            } finally {
                p.exitRead();
            }
        } finally {
            p.exitWrite();
        }
         */
    }
    
    public void testWriteLater() throws Exception {
        assertTrue(!m.canWrite());
        assertTrue(!m.canRead());
        final boolean[] b = new boolean[1];
        synchronized (b) {
            m.writeLater(new Runnable() {
                public void run() {
                    synchronized (b) {
                        b[0] = m.canWrite();
                    }
                }
            });
            b.wait(1000);
            assertTrue(b[0]);
        }
        b[0] = false;
        synchronized (b) {
            p.enterRead();
            try {
                m.writeLater(new Runnable() {
                    public void run() {
                        synchronized (b) {
                            b[0] = m.canWrite();
                        }
                    }
                });
            } finally {
                p.exitRead();
            }
            b.wait(1000);
            assertTrue(b[0]);
        }
    }
    
}
