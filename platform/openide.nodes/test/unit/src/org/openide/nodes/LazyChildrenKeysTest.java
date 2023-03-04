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

import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Children.Keys;

/**
 *
 * @author Tomas Holy
 */
public class LazyChildrenKeysTest extends NbTestCase {
    
    public LazyChildrenKeysTest(String testName) {
        super(testName);
    }

    public void testFFNWithEmptyEntries() {
        LazyKeys keys = new LazyKeys();
        keys.keys("a", "-b");
        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(keys)));
        Node[] nodes = fn.getChildren().getNodes(true);
        assertEquals(1, nodes.length);
        assertEquals("a", nodes[0].getName());
    }

    public void testCreateNodesIsNotCalledForDummyNode() {
        class FCh extends FilterNode.Children {

            public FCh(Node or) {
                super(or);
            }

            @Override
            protected Node[] createNodes(Node key) {
                if (EntrySupportLazy.isDummyNode(key)) {
                    fail("Should not call createNodes() for DummyNode");
                }
                return super.createNodes(key);
            }
        }

        LazyKeys keys = new LazyKeys();
        keys.keys("a", "-b", "b");
        Node or = new AbstractNode(keys);
        FilterNode fn = new FilterNode(or, new FCh(or));
        fn.getChildren().getNodesCount();
        List<Node> snapshot = fn.getChildren().snapshot();
        assertEquals(3, snapshot.size());
        assertEquals("a", snapshot.get(0).getName());
        assertEquals("", snapshot.get(1).getName());
        assertEquals("b", snapshot.get(2).getName());

        Node[] nodes = fn.getChildren().getNodes();
        assertEquals(2, nodes.length);
        assertEquals("a", nodes[0].getName());
        assertEquals("b", nodes[1].getName());
    }

    private static class LazyKeys extends Keys<String> {

        public LazyKeys() {
            super(true);
        }

        public void keys(String... args) {
            super.setKeys(args);
        }

        @Override
        protected Node[] createNodes(String key) {
            if (key.startsWith("-")) {
                return null;
            } else {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key);
                return new Node[]{n};
            }
        }
    }
}
