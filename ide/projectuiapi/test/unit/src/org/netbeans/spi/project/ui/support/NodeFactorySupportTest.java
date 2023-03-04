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

package org.netbeans.spi.project.ui.support;

import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 * @author mkleint
 */
public class NodeFactorySupportTest extends TestCase {
    
    public NodeFactorySupportTest(String testName) {
        super(testName);
    }

    /**
     * Test of createCompositeChildren method, of class org.netbeans.spi.project.ui.support.NodeFactorySupport.
     */
    public void testCreateCompositeChildren() throws InterruptedException, InvocationTargetException {
        InstanceContent ic = new InstanceContent();
        final Children dels = new TestDelegates(new AbstractLookup(ic));
        final Node node1 = new AbstractNode(Children.LEAF);
        final Node node2 = new AbstractNode(Children.LEAF);
        final Node node3 = new AbstractNode(Children.LEAF);
        final Node node4 = new AbstractNode(Children.LEAF);
        node1.setName("node1");
        node2.setName("node2");
        node3.setName("node3");
        node4.setName("node4");
        NodeFactory fact1 = new TestNodeFactory(node1);
        NodeFactory fact2 = new TestNodeFactory(node2);
        NodeFactory fact3 = new TestNodeFactory(node3);
        NodeFactory fact4 = new TestNodeFactory(node4);
        List<NodeFactory> col = new ArrayList<NodeFactory>();
        col.add(fact1);
        col.add(fact2);
        ic.set(col, null);

        assertEquals(Arrays.asList(node1, node2), Arrays.asList(dels.getNodes(true)));
        
        col.add(0, fact4);
        col.add(fact3);
        col.remove(fact2);
        ic.set(col, null);
        //#115995, caused by fix for #115128
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Node[] nds = dels.getNodes();
                assertEquals(nds[0], node4);
                assertEquals(nds[1], node1);
                assertEquals(nds[2], node3);
            }
        });
        
    }

    public void testFindChild() throws Exception {
        class HelloNode extends AbstractNode {
            HelloNode() {
                super(Children.LEAF);
                setName("hello");
            }
        }
        Node n = new HelloNode();
        assertEquals(Collections.singletonList(n), Arrays.asList(new TestDelegates(Lookups.fixed(new TestNodeFactory(n))).getNodes(true)));
        assertEquals(1, new TestDelegates(Lookups.fixed(new TestNodeFactory(new HelloNode()))).getNodesCount(true));
        n = new HelloNode();
        assertEquals(n, new TestDelegates(Lookups.fixed(new TestNodeFactory(n))).findChild("hello"));
    }

   private class TestNodeFactory implements NodeFactory {
       
       Node node;
       public TestNodeFactory(Node node) {
           this.node = node;
       }
        public NodeList createNodes(Project p) {
            return NodeFactorySupport.fixedNodeList(new Node[] {node});
        }
   }
   
   private class TestDelegates extends NodeFactorySupport.DelegateChildren  {
       public Lookup lkp;
       TestDelegates(Lookup lkp) {
           super(null, null);
           this.lkp = lkp;
       }
       
       protected @Override Lookup createLookup() {
           return lkp;
       }
   }
    
}
