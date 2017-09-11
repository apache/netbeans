/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.openide.util;

import java.util.concurrent.CountDownLatch;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MutexTryTest extends NbTestCase {
    private Mutex.Privileged p;
    private Mutex m;

    public MutexTryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        p = new Mutex.Privileged();
        m = new Mutex(p);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    public void testTimeOutTryRead() throws Exception {
        Block b = new Block(p, false);
        b.block();
        
        long now = System.currentTimeMillis();
        boolean res = p.tryReadAccess(1000);
        long then = System.currentTimeMillis();
        
        long delay = then - now;
        
        assertFalse("Locking has not succeeded", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
        
        assertTrue("Delay is 1s, was: " + delay + " ms", delay > 900);
    }

    public void testSuccessfulTryRead() throws Exception {
        Block b = new Block(p, true);
        b.block();
        
        boolean res = p.tryReadAccess(1000);
        
        assertTrue("Locking has succeeded", res);
        assertTrue("read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
    }
    
    public void testTimeOutTryWrite() throws Exception {
        Block b = new Block(p, false);
        b.block();
        
        long now = System.currentTimeMillis();
        boolean res = p.tryWriteAccess(1000);
        long then = System.currentTimeMillis();
        
        long delay = then - now;
        
        assertFalse("Locking has not succeeded", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertFalse("No write lock held", m.isWriteAccess());
        
        assertTrue("Delay is 1s, was: " + delay + " ms", delay > 900);
    }
    
    public void testSuccessfulTryWrite() throws Exception {
        boolean res = p.tryWriteAccess(1000);
        assertTrue("Access granted", res);
        assertFalse("No read lock held", m.isReadAccess());
        assertTrue("Write lock held", m.isWriteAccess());
    }
    
    
    private static class Block implements Runnable {
        private final Mutex.Privileged p;
        private final boolean read;
        private final CountDownLatch runs = new CountDownLatch(1);
        
        Block(Mutex.Privileged p, boolean read) {
            this.p = p;
            this.read = read;
        }
        
        void block() throws InterruptedException {
            Thread t = new Thread(this, "Block mutex");
            t.start();
            runs.await();
        }
        
        @Override
        public synchronized void run() {
            if (read) {
                p.enterReadAccess();
            } else {
                p.enterWriteAccess();
            }
            runs.countDown();
            for (;;) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
    }
}
