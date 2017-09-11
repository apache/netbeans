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

package org.openide.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;

public class UtilitiesActiveQueueTest extends NbTestCase {

    public UtilitiesActiveQueueTest(String testName) {
        super(testName);
    }

    public void testRunnableReferenceIsExecuted () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        synchronized (ref) {
            obj = null;
            assertGC ("Should be GCed quickly", ref);
            ref.wait ();
            assertTrue ("Run method has been executed", ref.executed);
        }
    }
    
    public void testRunnablesAreProcessedOneByOne () throws Exception {
        Object obj = new Object ();
        RunnableRef ref = new RunnableRef (obj);
        ref.wait = true;
        
        
        synchronized (ref) {
            obj = null;
            assertGC ("Is garbage collected", ref);
            ref.wait ();
            assertTrue ("Still not executed, it is blocked", !ref.executed);
        }    

        RunnableRef after = new RunnableRef (new Object ());
        synchronized (after) {
            assertGC ("Is garbage collected", after);
            after.wait (100); // will fail
            assertTrue ("Even if GCed, still not processed", !after.executed);
        }

        synchronized (after) {
            synchronized (ref) {
                ref.notify ();
                ref.wait ();
                assertTrue ("Processed", ref.executed);
            }
            after.wait ();
            assertTrue ("Processed too", after.executed);
        }
    }
    
    public void testManyReferencesProcessed() throws InterruptedException {
        int n = 10;
        Object[] objects = new Object[n];
        ExpensiveRef[] refs = new ExpensiveRef[n];
        for (int i = 0; i < n; i++) {
            objects[i] = new Object();
            refs[i] = new ExpensiveRef(objects[i], Integer.toString(i));
        }
        objects = null;
        for (int i = 0; i < n; i++) {
            assertGC("is GC'ed", refs[i]);
        }
        for (int i = 0; i < n; i++) {
            synchronized (refs[i]) {
                while (!refs[i].executed) {
                    refs[i].wait();
                }
            }
        }
    }
    
    public void testCallingPublicMethodsThrowsExceptions () {
        try {
            BaseUtilities.activeReferenceQueue().poll();
            fail ("One should not call public method from outside");
        } catch (RuntimeException ex) {
        }
        try {
            BaseUtilities.activeReferenceQueue ().remove ();
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
        try {
            BaseUtilities.activeReferenceQueue ().remove (10);
            fail ("One should not call public method from outside");
        } catch (InterruptedException ex) {
        }
    }
    
    private static class RunnableRef extends WeakReference<Object>
    implements Runnable {
        public boolean wait;
        public boolean entered;
        public boolean executed;
        
        public RunnableRef (Object o) {
            this(o, BaseUtilities.activeReferenceQueue());
        }
        
        public RunnableRef(Object o, ReferenceQueue<Object> q) {
            super(o, q);
        }
        
        public synchronized void run () {
            entered = true;
            if (wait) {
                // notify we are here
                notify ();
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
            }
            executed = true;
            
            notifyAll ();
        }
    }
    
    private static class ExpensiveRef extends WeakReference<Object>
    implements Runnable {
        public boolean executed;
        private final String name;
        
        public ExpensiveRef (Object o, String name) {
            super(o, BaseUtilities.activeReferenceQueue());
            this.name = name;
        }
        
        @Override
        public synchronized void run () {
            executed = true;
            try {
                Thread.sleep(10);
                System.gc();
                Thread.sleep(10);
            } catch (InterruptedException iex) {}
            notifyAll ();
            System.err.println(name+" executed.");
        }
    }
}
