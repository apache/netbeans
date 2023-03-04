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

import java.beans.*;
import java.util.*;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;


/** Test Children.Array.
 * @author Jesse Glick
 */
public class ChildrenArrayTest extends NbTestCase {

    public ChildrenArrayTest(String name) {
        super(name);
    }

    protected Children.Array createChildren () {
        return new Children.Array ();
    }

    public void testThereIsNoSupportBeforeNodeIsUsed() {
        Children kids = new Children.Array();
        assertSize("Not big", 48, kids);
    }
    
    public void testAdditionIsFiredWhenWeKnowTheSize () {
        Children kids = createChildren ();
        Node root = new AbstractNode (kids);
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        root.addNodeListener (l);
        
        assertEquals ("Empty", 0, root.getChildren ().getNodesCount ());
        
        kids.add (new Node[] { Node.EMPTY.cloneNode () });
        
        l.assertAddEvent ("One node added", 1);
        assertEquals ("One", 1, root.getChildren ().getNodesCount ());
       
    }
    

    /** Tests that node membership events are fired even before getNodes is called.
     * @see "#24851"
     */
    public void testNodeEventsFiredAtOnce() {
        // Stage 1: call getNodes first, after addNodeListener.
        Children kids = createChildren ();
        Node root = new AbstractNode(kids);
        WaitNL l = new WaitNL();
        root.addNodeListener(l);
        Node[] remember = kids.getNodes();
        Node a = node("a");
        kids.add(new Node[] {a});
        assertTrue(l.didAdd(a));
        Node b = node("b");
        kids.add(new Node[] {b});
        assertTrue(l.didAdd(b));
        Node c = node("c");
        kids.add(new Node[] {c});
        assertTrue(l.didAdd(c));
    }
    
    public void testCallBeforeAddNodeListener () throws Exception {
        // Stage 2: call before addNodeListener.
        Children kids = createChildren ();
        Node root = new AbstractNode(kids);
        kids.getNodes();
        WaitNL l = new WaitNL();
        root.addNodeListener(l);
        Node a = node("a");
        kids.add(new Node[] {a});
        assertTrue(l.didAdd(a));
        Node b = node("b");
        kids.add(new Node[] {b});
        assertTrue(l.didAdd(b));
        Node c = node("c");
        kids.add(new Node[] {c});
        assertTrue(l.didAdd(c));
    }
    
    public void testDontCallGetNodesExplicitly () throws Exception {
        // Stage 3: don't call getNodes explicitly. Events should not be fired      
        Children kids = createChildren ();
        Node root = new AbstractNode(kids);
        WaitNL l = new WaitNL();
        root.addNodeListener(l);
        Node a = node("a");
        kids.add(new Node[] {a});
        assertFalse ("First node added w/o explicit getNodes() is notified", l.didAdd(a));
        Node b = node("b");
        kids.add(new Node[] {b});
        assertFalse(l.didAdd(b));
        Node c = node("c");
        kids.add(new Node[] {c});
        assertFalse(l.didAdd(c));
    }
    
    public void testComplexReorderAndAddAndRemoveEvent () {
        Children.Array k = createChildren ();
        // warning this can actually assign FilterNode$Children.nodes!!!
        k.nodes = new ArrayList ();
        AbstractNode a1 = new AbstractNode(Children.LEAF);
        a1.setName ("remove");
        AbstractNode a2 = new AbstractNode(Children.LEAF);
        a2.setName ("1");
        AbstractNode a3 = new AbstractNode(Children.LEAF);
        a3.setName ("0");
        AbstractNode a4 = new AbstractNode(Children.LEAF);
        a4.setName ("add");
        k.nodes.add (a1);
        k.nodes.add (a2);
        k.nodes.add (a3);
        
        Node n = new AbstractNode (k);
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        n.addNodeListener (l);
        assertEquals (3, k.getNodes ().length);
        
        k.nodes.clear ();
        k.nodes.add (a3);
        k.nodes.add (a2);
        k.nodes.add (a4);
        k.refresh ();

        l.assertRemoveEvent ("Removed index 0", 1);
        l.assertReorderEvent ("0->1 and 1->0", new int[] { 1, 0 });
        l.assertAddEvent ("Adding at index 2", 1);
        l.assertNoEvents ("And that is all");

        Node[] arr = k.getNodes ();
        assertEquals (3, arr.length);
        assertEquals ("0", arr[0].getName ());
        assertEquals ("1", arr[1].getName ());
        assertEquals ("add", arr[2].getName ());
    }
    
    
    public void testParentAssigned() {
        Children ch = createChildren ();
        Node n1 = new AbstractNode( ch );
        Node n2 = new AbstractNode( Children.LEAF );
        ChildrenKeysTest.Listener l = new ChildrenKeysTest.Listener ();
        n1.addNodeListener (l);

        assertEquals ("Empty", 0, ch.getNodesCount ());
        ch.add( new Node[] { n2 } );
        
        Node parent = n2.getParentNode();
        assertNotNull ( "Parent is assigned",  parent);
        assertEquals ("One", 1, ch.getNodesCount ());
        l.assertAddEvent ("One node added", 1);
        
        Node n3 = new AbstractNode( Children.LEAF );
        ch.add (new Node[] { n3 });
        Node p3 = n3.getParentNode();
        assertSame ( "n3 parent is the same", parent, p3);
        assertEquals ("Two", 2, ch.getNodesCount ());
        l.assertAddEvent ("One node added", 1);
        
        assertContains ("n2 is in the list of nodes", true, n2, parent.getChildren ().getNodes ());
        assertContains ("something equals to n2 is in this list", false, n2, ch.getNodes ());
    }
    
    private static Node node(String name) {
        Node n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }

    private static void assertContains (String msg, boolean same, Node n, Node[] arr) {
        for (int i = 0; i < arr.length; i++) {
            boolean is = same ? n == arr[i] : n.equals (arr[i]);
            if (is) {
                return;
            }
        }
        fail ("Node " + n + " not found in " + Arrays.asList (arr));
    }
    
    
    /** Node listener that will tell you if it gets a change.
     */
    private static final class WaitNL implements NodeListener {
        
        private final Set added = new HashSet(); // Set<Node>
        private final Set removed = new HashSet(); // Set<Node>
        
        public WaitNL() {}
        
        public synchronized boolean didAdd(Node n) {
            if (added.contains(n)) return true;
            try {
                wait(1500);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return added.contains(n);
        }
        
        public synchronized Set getAdded() {
            return new HashSet(added);
        }
        public synchronized Set getRemoved() {
            return new HashSet(removed);
        }
        
        public synchronized void childrenAdded(NodeMemberEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            added.addAll(Arrays.asList(ev.getDelta()));
            notifyAll();
        }
        
        public synchronized void childrenRemoved(NodeMemberEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            removed.addAll(Arrays.asList(ev.getDelta()));
            notifyAll();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {}
        public void childrenReordered(NodeReorderEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
        }
        public void nodeDestroyed(NodeEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, Collections.<Node>emptyList());
        }
        
    }
    
}
