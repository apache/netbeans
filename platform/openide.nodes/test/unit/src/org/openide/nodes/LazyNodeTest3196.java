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

import org.netbeans.junit.NbTestCase;

/**
 * Test for NETBEANS-3196, where calling {@code Children.LEAF.getNodesCount()} (e.g. via
 * {@code Node.EMPTY.getChildren().getNodesCount()}) will prevent lazy loading from working in
 * certain cases in the future.
 */
public class LazyNodeTest3196 extends NbTestCase {
    {
        // Not sure if this is needed, but I saw it in some other tests, so it can't hurt...
        System.setProperty("org.openide.explorer.VisualizerNode.prefetchCount", "0");
    }

    public LazyNodeTest3196(String name) {
        super(name);
    }

    public void testGetLeafGetNodesCountLazy() throws Exception {
        assertEquals(0, Children.LEAF.getNodesCount());
        assertEquals(0, Children.LEAF.getNodesCount(true));
        assertEquals(0, Node.EMPTY.getChildren().getNodesCount());
        assertEquals(0, Node.EMPTY.getChildren().getNodesCount(true));
        MyRootNode rootNode = new MyRootNode();
        assertEquals(0, rootNode.getChildren().getNodesCount());
        assertEquals(0, rootNode.getChildren().getNodesCount(true));

        /* After calling getNodesCount() on any Children object, including Children.LEAF,
        isInitialized should return true. */
        assertEquals(true, Children.LEAF.isInitialized());
        assertEquals(true, rootNode.getChildren().isInitialized());
        /* Bug NETBEANS-3196 used to happen when setChildren() was a called on a Node for which
        the value was previously Children.LEAF. In this case, setChildren() would immediately
        initialize the new Children eagerly, rather than allowing lazy expansion. */
        rootNode.setChildrenExposed(new LazyChildrenImplementation());

        /* We haven't queried the new children at this point, so the Children instance should not
        be initialized. Prior to the fix for NETBEANS-3196, the test would fail at this point. */
        assertEquals(false, rootNode.getChildren().isInitialized());
        assertEquals(false, ((LazyChildrenImplementation) rootNode.getChildren()).getWasEverAdded());

        /* I don't think the optimizedResult parameter makes a difference here. Even
        getNodesCount(false) will cause keys to be computed. */
        assertEquals(3, rootNode.getChildren().getNodesCount(true));
        assertEquals(true, rootNode.getChildren().isInitialized());
        assertEquals(true, ((LazyChildrenImplementation) rootNode.getChildren()).getWasEverAdded());

        /* Now that there is a real, non-LEAF value for setChildren that was genuinely expanded,
        the intended behavior of setChildren immediately expanding the new value should still
        work. */
        rootNode.setChildrenExposed(new LazyChildrenImplementation());
        assertEquals(true, rootNode.getChildren().isInitialized());
        assertEquals(true, ((LazyChildrenImplementation) rootNode.getChildren()).getWasEverAdded());
    }

    private static final class LazyChildrenImplementation extends Children.Keys<Integer> {
        boolean added;
        boolean wasEverAdded;

        public boolean getWasEverAdded() {
            return wasEverAdded;
        }

        @Override
        protected Node[] createNodes(Integer key) {
            return new Node[] { new LazyChildNode(key)} ;
        }

        @Override
        protected void addNotify() {
            if (added) {
                throw new AssertionError("Already added");
            }
            added = true;
            wasEverAdded = true;
            setKeys(new Integer[] {1, 2, 3});
        }

        @Override
        protected void removeNotify() {
            if (!added) {
                throw new AssertionError("Can't remove; wasn't added yet!");
            }
            added = false;
            setKeys(new Integer[] {});
        }
    }

    private static final class MyRootNode extends AbstractNode {
        public MyRootNode() {
            super(Children.LEAF);
        }

        public void setChildrenExposed(final Children ch) {
            setChildren(ch);
        }
    }

    private static final class LazyChildNode extends AbstractNode {
        private final int key;

        public LazyChildNode(int key) {
            super(Children.LEAF);
            this.key = key;
        }

        public int getKey() {
            return key;
        }
    }
}
