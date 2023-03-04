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

package org.openide.explorer.view;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/*
 * Tests for class NodeListModel
 */
public class NodeListModelTest extends NbTestCase {

    private static final int NO_OF_NODES = 20;

    public NodeListModelTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    /*
     * Tests whether children of the root node are
     * kept in the memory after the root is passed
     * to the constructor of NodeListModel.
     */
    public void testNodesAreReferenced() {
        
        WeakReference[] tn;
        
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        
        
        tn = new WeakReference[model.getSize()];
        for (int i = 0; i < model.getSize(); i++) {
            tn[i] = new WeakReference(model.getElementAt(i));
        }
        
        assertTrue("Need to have more than one child", tn.length > 0);
        
        boolean fail;
        try {
            assertGC("First node should not be gone", tn[0]);
            fail = true;
        } catch (Error err) {
            fail = false;
        }
        if (fail) {
            fail("First node garbage collected!!! " + tn[0].get());
        }
        
        for (int i = 0; i < tn.length; i++) {
            // else fail
            assertNotNull("One of the nodes was gone. Index: " + i, tn[i].get());
        }
    }
    
    /**
     * Tests proper initialization in constructors.
     */
    public void testConstructors() {
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        // the following line used to fail if the
        // no parameter costructor does not initialize
        // childrenCount
        model.getSize();
    }

    public void testIsRootIncluded() {
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel();
        model.setNode(c, true);

        assertEquals(NO_OF_NODES + 1, model.getSize());

        assertNode("Parent is first", c, model.getElementAt(0));
        for (int i= 0; i < NO_OF_NODES; i++) {
            assertNode(i + "th node", c.getChildren().getNodeAt(i), model.getElementAt(i + 1));
        }
    }

    private static void assertNode(String msg, Node n, Object e) {
        TreeNode v = Visualizer.findVisualizer(n);
        assertEquals(msg, v, e);
    }
    
    /*
     * Children for testNodesAreReferenced.
     */
    private static class CNodeChildren extends Children.Keys {
        public CNodeChildren() {
            List myKeys = new LinkedList();
            for (int i = 0; i < NO_OF_NODES; i++) {
                myKeys.add(Integer.valueOf(i));
            }
            
            setKeys(myKeys);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.toString());
            return  new Node[] { an };
        }
    }
}
