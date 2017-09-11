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
