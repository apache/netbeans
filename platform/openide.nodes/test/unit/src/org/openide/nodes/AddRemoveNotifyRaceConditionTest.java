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

package org.openide.nodes;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import junit.framework.AssertionFailedError;
import org.openide.ErrorManager;

import org.netbeans.junit.*;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.util.RequestProcessor;


public class AddRemoveNotifyRaceConditionTest extends NbTestCase {
    public AddRemoveNotifyRaceConditionTest(java.lang.String testName) {
        super(testName);
    }

    protected void setUp () throws Exception {
        System.setProperty("org.openide.util.Lookup", "org.openide.nodes.AddRemoveNotifyRaceConditionTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.messages.delete (0, ErrManager.messages.length ());
    }
    

    protected void runTest () throws Throwable {
        try {
            super.runTest();
        } catch (Error err) {
            AssertionFailedError newErr = new AssertionFailedError (err.getMessage () + "\n" + ErrManager.messages);
            newErr.initCause (err);
            throw newErr;
        }
    }
    
    public void testChildrenCanBeSetToNullIfGCKicksIn () throws Exception {
        Keys k = new Keys();
        AbstractNode n = new AbstractNode(k);
        
        ErrorManager.getDefault().log("Initialize first array");
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Ok, one", 1, arr.length);
        ErrorManager.getDefault().log("Array initialized");

        final Reference<Node> ref = new SoftReference<Node>(arr[0]);
        arr = null;
        
        class R implements Runnable {
            public void run() {
                ErrorManager.getDefault().log("Ready to GC");
                try {
                    assertGC("Node can go away in the worst possible moment", ref);
                } catch (Throwable t) {
                    // ok, may not happen
                }
                ErrorManager.getDefault().log("Gone");
                System.runFinalization();
                System.runFinalization();
                
            }
        }
        k.run = new R();
        
        ErrorManager.getDefault().log("Before getNodes(true)");
        int cnt = n.getChildren().getNodes(true).length;
        ErrorManager.getDefault().log("After getNodes(true)");
        
        assertEquals("Count is really one", 1, cnt);
    }

    private static class Keys extends Children.Keys<Integer> implements Runnable {
        private Runnable run;
        private int removeNotify;
        private RequestProcessor.Task task = new RequestProcessor("blbni").create(this);
        
        @Override
        protected void addNotify() {
            task.schedule(0);
        }
        
        public void run() {
            ErrorManager.getDefault().log("before setKeys");
            setKeys(Collections.singleton(1));
            ErrorManager.getDefault().log("after setKeys");
            if (run != null) {
                ErrorManager.getDefault().log("running inner runnable");
                run.run();
                ErrorManager.getDefault().log("end of inner runnable");
                run = null;
                return;
            }
        }

        @Override
        protected void removeNotify() {
            ErrorManager.getDefault().log("removeNotify");
            setKeys(Collections.<Integer>emptyList());
            ErrorManager.getDefault().log("removeNotifyEnd");
        }

        protected Node[] createNodes(Integer key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.toString());
            return new Node[] { an };
        }
        
        @Override
        public Node[] getNodes(boolean optimalResult) {
            Node[] res;
            if (optimalResult) {
                ErrorManager.getDefault().log("getNodes optimalResult");
                res = getNodes();
                task.schedule(0);
                ErrorManager.getDefault().log("getNodes scheduled");
                task.waitFinished();
                ErrorManager.getDefault().log("wait finished");
            }
            res = getNodes();
            // they are no longer needed
            return res;
        }        
    }
    
    
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
            messages.append ("THREAD: ");
            messages.append (Thread.currentThread().getName());
            messages.append (" MSG: ");
            messages.append (s);
            messages.append ('\n');
        }
        
        public void notify (int severity, Throwable t) {
            messages.append (t.getMessage ());
        }
        
    } 

}
