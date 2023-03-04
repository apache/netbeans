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

import org.netbeans.junit.*;

/** Test updating of bean children in proper circumstances, e.g.
 * deleting nodes or beans.
 * @author Jesse Glick
 */
public class FilterChildrenEventsTest extends NbTestCase {

    public FilterChildrenEventsTest(String name) {
        super(name);
    }

    
    public void testNodesNodeDestroyed() throws Exception {
        
        Node[] chNodes = createTestNodes();
        Children ch = new Children.Array();
        ch.add( chNodes );
        
        Node n = new AbstractNode( ch );
        FilterNode fn = new FilterNode( n );
        n.setName( "X" );
        MyListener ml = new MyListener();
        
        fn.addNodeListener( ml );
        
        n.setName( "Y" );
        
        List events = ml.getEvents();
        
        assertTrue("correct events", events.size() == 2 );
    }
    
    public void testRefreshOnFavorites() throws Exception {
        Node[] chNodes = createTestNodes();
        Children ch = new Children.Array();
        ch.add(chNodes);

        Node n = new AbstractNode(ch);
        Chldrn filterCh = new Chldrn(n);
        FilterNode fn = new FilterNode(n, filterCh);

        Node[] now = fn.getChildren().getNodes();
        assertEquals("Three", 3, now.length);

        MyListener ml = new MyListener();
        fn.addNodeListener( ml );

        filterCh.makeInvisible(now[1].getName());

        assertEquals("One event", 1, ml.getEvents().size());

        Node[] after = fn.getChildren().getNodes();
        assertEquals("Just two", 2, after.length);

        assertSame("First node the same", now[0], after[0]);
        assertSame("Last node the same", now[2], after[1]);
    }

    public void testChildrenAdded() throws Exception {
        Node[] chNodes = createTestNodes();
        Children ch = new Children.Array();
        ch.add( chNodes );
        
        Node n = new AbstractNode( ch );
        FilterNode fn = new FilterNode( n );
        n.setName( "X" );
        MyListener ml = new MyListener();
        
        fn.addNodeListener( ml );
        Node[] hold = fn.getChildren().getNodes();
        
        ch.add( new Node[] { new AbstractNode( Children.LEAF) } );
        
        List events = ml.getEvents();
        
        assertEquals("correct events", 1, events.size() );
    }
    
    
    private static Node[] createTestNodes() {
        
        Node[] tNodes = new Node[] {
            new AbstractNode( Children.LEAF ),
            new AbstractNode( Children.LEAF ),
            new AbstractNode( Children.LEAF )
        };
        
        tNodes[0].setName( "A" );
        tNodes[1].setName( "B" );
        tNodes[2].setName( "C" );
        
        return tNodes;
    }
    
    private static class MyListener implements NodeListener {
        
        ArrayList events = new ArrayList();
        
        
        /** Fired when a set of new children is added.
         * @param ev event describing the action
         *
         */
        public void childrenAdded(NodeMemberEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            events.add( ev );
        }
        
        /** Fired when a set of children is removed.
         * @param ev event describing the action
         *
         */
        public void childrenRemoved(NodeMemberEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            events.add( ev );
        }
        
        /** Fired when the order of children is changed.
         * @param ev event describing the change
         *
         */
        public void childrenReordered(NodeReorderEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            events.add( ev );
        }
        
        /** Fired when the node is deleted.
         * @param ev event describing the node
         *
         */
        public void nodeDestroyed(NodeEvent ev) {
            ChildFactoryTest.assertNodeAndEvent(ev, Collections.<Node>emptyList());
            events.add( ev );
        }
        
        /** This method gets called when a bound property is changed.
         * @param evt A PropertyChangeEvent object describing the event source
         *   	and the property that has changed.
         *
         */
        public void propertyChange(PropertyChangeEvent ev) {
            events.add( ev );
        }
        
        public List getEvents() {
            return events;
        }
        
    }
    static class Chldrn extends FilterNode.Children
    implements Runnable {
        final Set<String> toHide = new HashSet<String>();

        public Chldrn (Node node) {
            super (node);
        }

        @Override
        protected Node[] createNodes(Node node) {
            if (toHide.contains(node.getName())) {
                return null;
            }
            return super.createNodes(node);
        }

        public void makeInvisible(String name) {
            toHide.add(name);
            MUTEX.postWriteRequest(this);
        }
        public void makeVisible(String name) {
            toHide.remove(name);
            MUTEX.postWriteRequest(this);
        }

        public void run() {
            Node[] arr = original.getChildren().getNodes();
            for (int i = 0; i < arr.length; i++) {
                refreshKey(arr[i]);
            }
        }

    } // end of Chldrn
}
