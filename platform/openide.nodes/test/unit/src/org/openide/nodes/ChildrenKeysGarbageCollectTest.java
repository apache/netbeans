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

import java.lang.ref.*;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

public class ChildrenKeysGarbageCollectTest extends NbTestCase
implements NodeListener {
    private Creator creator;
    private int nodeDestroyed;

    public ChildrenKeysGarbageCollectTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite () {
        NbTestSuite general = new NbTestSuite ("General GC test suite");

        general.addTest (createSuite ("Unlimited keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (false);
            }
        }));
        general.addTest (createSuite ("1-1 keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (true);
            }
        }));
        general.addTest (createSuite ("0-1 keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (true);
            }
        }));
        
        return general;
    }
    
    private static NbTestSuite createSuite (String n, Creator c) {
        NbTestSuite s = new NbTestSuite (ChildrenKeysGarbageCollectTest.class);
        s.setName (n);
        Enumeration e = s.tests ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof ChildrenKeysGarbageCollectTest) {
                ChildrenKeysGarbageCollectTest t = (ChildrenKeysGarbageCollectTest)o;
                t.creator = c;
            } else {
                fail ("o is supposed to be of correct instance: " + o);
            }
        }
        return s;
    }

    protected void setUp () throws Exception {
        /*
        System.setProperty("org.openide.util.Lookup", "org.openide.nodes.ChildrenGarbageCollectTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.messages.delete (0, ErrManager.messages.length ());
         */
    }

    
    public void testUsuallyAllNodesAreGCedAtOnce () throws Exception {
        Children.Keys k = creator.createChildren ();
        
        
        k.setKeys (new String[] { "1", "3", "4" });
        
        doFullOperationOnNodes (k);
    }
    
    private void doFullOperationOnNodes (Children.Keys k) {
        Node[] arr = k.getNodes ();
        
        assertEquals ("Three", 3, arr.length);
        
        WeakReference[] refs = new WeakReference[arr.length];
        for (int i = 0; i < arr.length; i++) {
            refs[i] = new WeakReference (arr[i]);
            arr[i].addNodeListener (this);
        }
        
        arr[0] = null;
        try {
            assertGC ("Try to gc only one node", refs[0]);
            fail ("This should not succeed, as others are still referenced");
        } catch (Throwable t) {
            // ok
        }
        arr[1] = arr[2] = null;
        
        assertGC ("Now we gc all", refs[0]);
        assertGC ("#2", refs[1]);
        assertGC ("#3", refs[2]);
        
        assertEquals ("Still 3 children", 3, k.getNodesCount ());
        assertEquals ("No nodes notified to be destoryed", 0, nodeDestroyed);

        arr = k.getNodes ();
        assertEquals ("Again three", 3, arr.length);
    }
    
    
    //
    // Listener methods
    //

    public void childrenAdded (NodeMemberEvent ev) {
    }

    public void childrenRemoved (NodeMemberEvent ev) {
    }

    public void childrenReordered (NodeReorderEvent ev) {
    }

    public void nodeDestroyed (NodeEvent ev) {
        nodeDestroyed++;
    }
    
    public void propertyChange (java.beans.PropertyChangeEvent ev) {
    }
    
    /** Factory for creating 
     */
    private interface Creator {
        public Children.Keys createChildren ();
    }
}
