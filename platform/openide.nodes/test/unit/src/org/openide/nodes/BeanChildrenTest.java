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

import junit.framework.*;
import junit.textui.TestRunner;
import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;
import org.openide.util.Mutex;

import org.netbeans.junit.*;

/** Test updating of bean children in proper circumstances, e.g.
 * deleting nodes or beans.
 * @author Jesse Glick
 */
public class BeanChildrenTest extends NbTestCase {

    public BeanChildrenTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(BeanChildrenTest.class));
    }
    
    @SuppressWarnings("unchecked")
    private static BeanContext makeContext() {
        BeanContext bc = new BeanContextSupport();
        bc.add("one");
        bc.add("two");
        bc.add("three");
        return bc;
    }
    
    private static String[] nodes2Names(Node[] nodes) {
        String[] names = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            names[i] = nodes[i].getName();
        }
        return names;
    }
    
    public void testNodesAreCorrect() throws Exception {
        BeanContext bc = makeContext();
        Children c = new BeanChildren(bc, new SimpleFactory());
        // Note that BeanContextSupport keeps a HashMap of children
        // so the order is not deterministic.
        assertEquals("correct subnodes",
            new HashSet<String>(Arrays.asList(new String[] {"one", "two", "three"})),
            new HashSet<String>(Arrays.asList(nodes2Names(c.getNodes()))));
    }
    
    public void testRemoveBeanRemovesChild() throws Exception {
        BeanContext bc = makeContext();
        final Children c = new BeanChildren(bc, new SimpleFactory());
        bc.remove("two");
        assertEquals("correct beans",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<Object>(Arrays.asList(bc.toArray())));
        // Make sure we let the children thread run to completion.
        // Check the result in the reader.
        // First make sure it is initialized. Otherwise Children.Keys.getNodes
        // from within the mutex immediately returns no nodes, then when
        // next asked has them all. Checking outside the mutex seems to block
        // until the nodes have been initialized.
        Node[] nodes = c.getNodes(true);
        nodes = Children.MUTEX.readAccess(new Mutex.Action<Node[]>() {
            public Node[] run() {
                return c.getNodes();
            }
        });
        assertEquals("correct subnodes",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<String>(Arrays.asList(nodes2Names(nodes))));
    }
    
    // Cf. #7925.
    public void testDeleteChildRemovesBean() throws Exception {
        BeanContext bc = makeContext();
        Children c = new BeanChildren(bc, new SimpleFactory());
        Node n = c.findChild("two");
        assertNotNull(n);
        assertEquals("two", n.getName());
        n.destroy();
        // Wait for changes, maybe:
        Children.MUTEX.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                return null;
            }
        });
        assertEquals("correct beans",
            new HashSet<String>(Arrays.asList(new String[] {"one", "three"})),
            new HashSet<Object>(Arrays.asList(bc.toArray())));
    }
    
    private static final class SimpleFactory implements BeanChildren.Factory {
        public Node createNode(Object bean) throws IntrospectionException {
            Node n = new AbstractNode(Children.LEAF);
            n.setName((String)bean);
            return n;
        }
    }
    
}
