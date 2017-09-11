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

package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.openide.util.NbMutexEventProvider;

public class ChildrenKeysIssue30907Test extends NbTestCase {

    public ChildrenKeysIssue30907Test(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected void setUp () throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.nodes.ChildrenKeysIssue30907Test$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.messages.delete (0, ErrManager.messages.length ());
    }
    

    @Override
    protected void runTest () throws Throwable {
        try {
            super.runTest();
        } catch (Error err) {
            AssertionFailedError newErr = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            newErr.initCause (err);
            throw newErr;
        }
        
        waitRemoveNotified ();
    }
    
    private Object FINAL_LOCK;
    private boolean removeNotified;
    protected void doRemoveNotify () {
        synchronized (FINAL_LOCK) {
            removeNotified = true;
            FINAL_LOCK.notifyAll ();
        }
    }

    private final void waitRemoveNotified () throws Exception {
        if (FINAL_LOCK == null) {
            return;
        }
        
        int cnt = 0;
        for (;;) {
            synchronized (FINAL_LOCK) {
                if (removeNotified) {
                    return;
                }
                FINAL_LOCK.wait (500);
            }
            if (cnt++ == 10) {
                fail ("waitRemoveNotified failed, too many waits: " + cnt);
            }
            System.gc ();
            System.runFinalization();
        }
    }
    
    public void testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907SlowAddNotifyWithReadAccess () throws Exception {
        doBug30907 (true, true, 0, 2);
    }
    
    public void testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907QuickAddNotifyWithReadAccess () throws Exception {
        doBug30907 (false, true, 0, 2);
    }
    
    public void testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907QuickAddNotify () throws Exception {
        doBug30907 (false, false, 2, 2);
    }
    public void testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907SlowAddNotify () throws Exception {
        doBug30907 (true, false, 2, 2);
    }
    
    public void testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907AddNotifyWithException () throws Exception {
        doBug30907 (true, true, false, 0, 0);
    }
    
    public void testDeadlock50379 () throws Exception {
        class K extends Children.Keys<Object> implements Runnable {
            
            public void run () {
                if (!MUTEX.isWriteAccess ()) {
                    MUTEX.writeAccess (this);
                    return;
                }
                
                synchronized (this) {
                    notifyAll ();
                }
                
                try {
                    Thread.sleep (100);
                } catch (InterruptedException ex) {
                    fail ("No interrupts");
                }
                
                
                setKeys (java.util.Collections.singleton(this));
            }
            
            @Override
            protected synchronized void addNotify () {
                org.openide.util.RequestProcessor.getDefault().post (this);
                try {
                    wait ();
                } catch (InterruptedException ex) {
                    fail ("No interrupts");
                }
            }
            
            protected Node[] createNodes (Object key) {
                return new Node[0];
            }

        }
        
        
        K keys = new K ();
        // deadlocks
        keys.getNodes ();
    }
    
    private void doBug30907 (final boolean slowAddNotify, boolean readAccess, int mainCount, int threadCount) throws Exception {
        doBug30907 (slowAddNotify, false, readAccess, mainCount, threadCount);
    }
    
    private void doBug30907 (final boolean slowAddNotify, final boolean throwException, boolean readAccess, int mainCount, int threadCount) throws Exception {
        FINAL_LOCK = new Object ();        
        
        ErrManager.messages.append ("doBug30907 slowAddNotify: " + slowAddNotify 
            + " throwException: " + throwException + " readAccess: " + readAccess + " mainCount: " + mainCount +
            " threadCount: " + threadCount + '\n');
        
        //
        // the purpose of this test is to create a livelock - execution never ends
        // as described in the bug
        //
        
        
        final Node node[] = { null };
        
        final Object LOCK = new Object ();
        
        class K extends Children.Keys implements Runnable {
            private String[] arr;
            final Set<Reference<Node>> toClear = new HashSet<Reference<Node>>();
            
            public K (String[] arr) {
                this.arr = arr;
            }
            
            protected Node[] createNodes(Object key) {
                AbstractNode an = new AbstractNode (Children.LEAF);
                an.setName (key.toString ());
                ErrManager.messages.append (" creating node: " + key.toString () + " by thread: " + Thread.currentThread ().getName () + "\n");

                toClear.add(new WeakReference<Node>(an));
                return new Node[] { an };
            }
            
            @Override
            public void addNotify () {                
                if (slowAddNotify) {
                    try {
                        // let the main thread run
                        synchronized (LOCK) {
                            ErrManager.messages.append ("  blocking in addNotify: " + Thread.currentThread ().getName () + "\n");
                            LOCK.notify (); // to N1
                        }

                        // and wait a while it reaches getNodes
                        Thread.sleep (300);
                        ErrManager.messages.append ("  end of blocking in addNotify: " + Thread.currentThread ().getName () + "\n");
                    } catch (InterruptedException ex) {
                        fail ("Exception");
                    }
                }
                
                ErrManager.messages.append ("  set keys: " + java.util.Arrays.asList (arr) + " by " + Thread.currentThread ().getName () + "\n");
                setKeys (arr);
                ErrManager.messages.append ("  end of keys by " + Thread.currentThread ().getName () + "\n");
                
                if (throwException) {
                    ErrManager.messages.append ("  throwing exception by " + Thread.currentThread ().getName () + "\n");
                    throw new IllegalStateException( "testing exception" );
                }
            }
            
            Node[] result;
            public void run () {
                // forces initialization
                Node[] tmpArr = new Node[]{};
                try {
                    ErrManager.messages.append ("Run: computing nodes\n");
                    tmpArr = node[0].getChildren ().getNodes ();
                    ErrManager.messages.append ("Run: nodes computed" + Arrays.asList (tmpArr) + "\n");
                }
                catch ( IllegalStateException e ) {
                    // Our exception
                    ErrManager.messages.append ("Run: exception caught: " + e.getMessage () + "\n");
                }

                if (!slowAddNotify) {
                    // qucik addNotify => notify the main thread to run after the 
                    // finish of getNodes
                    synchronized (LOCK) {
                        ErrManager.messages.append ("Run: Notifying others to run\n");
                        LOCK.notify (); // to N1
                    }
                }
                
                synchronized (LOCK) {
                    ErrManager.messages.append ("Run: Assigning result: " + Arrays.asList (tmpArr) + "\n");
                    result = tmpArr;
                    LOCK.notify (); // to N2
                }
            }
            
            @Override
            protected void removeNotify () {
                super.removeNotify();
                doRemoveNotify ();
            }
        }

        K k = new K (new String[] { "1", "2" });
        node[0] = new FilterNode (new AbstractNode (k));
        
        Node[] result;
        synchronized (LOCK) {
            try {
                if (readAccess) {
                    ErrManager.messages.append ("Main: Before read access\n");
                    Children.PR.enterReadAccess ();
                    ErrManager.messages.append ("Main: In read access\n");
                }

                Thread t = new Thread (k, "testProperInitializationEvenIfInvokedFromMultipleThreadsBug30907Thread");
                t.setDaemon(true);
                ErrManager.messages.append ("Main: Starting the thread\n");
                t.start ();

                if (!readAccess) {
                    ErrManager.messages.append ("Main: Wait for N1\n");
                    LOCK.wait (); // from N1
                    ErrManager.messages.append ("Main: Waiting for N1 is over\n");
                }

                ErrManager.messages.append ("Main: Calling getNodes()\n");
                result = node[0].getChildren ().getNodes ();
                ErrManager.messages.append ("Main: getNodes() finished: " + Arrays.asList (result) + "\n");
                assertNotNull("Get nodes cannot return null", result);
                assertEquals ("Returns proper value for children as it waits until addNotify finishes", mainCount, result.length);
            } finally {   
                if (readAccess) {
                    ErrManager.messages.append ("Main: before exitReadAccess\n");
                    Children.PR.exitReadAccess ();
                    ErrManager.messages.append ("Main: after exitReadAccess\n");
                }
            }
            
            if (readAccess) {
                ErrManager.messages.append ("Main: wait for N1 two\n");
                LOCK.wait (); // from N1
                ErrManager.messages.append ("Main: wait for N1 two finished\n");
            }
            
            // finish the work in thread
            while (k.result == null) {
                LOCK.wait (); // from N2
            }
        }
        assertEquals ("Two children there even in the initialization thread", threadCount, k.result.length);

        k.result = null;
        result = null;
        HOLDER = node[0];
        for (Reference<Node> ref : k.toClear) {
            assertGC("Cleaning nodes: " + ref.get(), ref);
        }
        ChildrenKeysTest.waitActiveReferenceQueue();
    }
    static Object HOLDER;
    
    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        public Lkp () {
            this (new org.openide.util.lookup.InstanceContent ());
        }
        
        private Lkp (org.openide.util.lookup.InstanceContent ic) {
            super (ic);
            ic.add (new ErrManager ());
            ic.add (new NbMutexEventProvider());
        }
    }
    
    private static final class ErrManager extends org.openide.ErrorManager {
        public static final StringBuffer messages = new StringBuffer ();
        
        public Throwable annotate (Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, java.util.Date date) {
            return t;
        }
        
        public Throwable attachAnnotations (Throwable t, org.openide.ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public org.openide.ErrorManager.Annotation[] findAnnotations (Throwable t) {
            return null;
        }
        
        public org.openide.ErrorManager getInstance (String name) {
            return this;
        }
        
        public void log (int severity, String s) {            
            messages.append (s);
            messages.append ('\n');
        }
        
        public void notify (int severity, Throwable t) {
            messages.append (t.getMessage ());
        }
        
    } 
}
