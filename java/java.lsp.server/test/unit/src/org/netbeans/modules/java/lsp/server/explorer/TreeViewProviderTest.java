/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.explorer;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangeType;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class TreeViewProviderTest {

    public TreeViewProviderTest() {
    }
    
    TreeNodeRegistry registry = new TreeNodeRegistryImpl(Lookup.EMPTY);

    @Test
    public void simpleHierarchyTestWithChange() throws Exception {
        ExplorerManager em = new ExplorerManager();
        em.setRootContext(new FibNode(10));
        TreeViewProviderImpl tvp = new TreeViewProviderImpl(em, registry);

        TreeItem rootInfo = tvp.getRootInfo().toCompletableFuture().get();
        int[] two = tvp.getChildren(rootInfo.id).toCompletableFuture().get();
        assertEquals(2, two.length);

        TreeItem two0 = tvp.getTreeItem(two[0]).toCompletableFuture().get();
        TreeItem two1 = tvp.getTreeItem(two[1]).toCompletableFuture().get();

        assertNotEquals("Different node and ids", two1.id, two0.id);

        assertEquals("9", two0.name);
        assertEquals("8", two1.name);

        assertEquals("Fib(9)", two0.label);
        assertEquals("Fib(8)", two1.label);

        assertEquals("Fib(9) = 34", two0.tooltip);
        assertEquals("Fib(8) = 21", two1.tooltip);

        assertEquals(TreeItem.CollapsibleState.Collapsed, two0.collapsibleState);
        assertEquals(TreeItem.CollapsibleState.Collapsed, two1.collapsibleState);

        Node twoOneNode = tvp.findNode(two[1]);
        ((FibNode)twoOneNode).extraAdd(8);

        tvp.assertChanged(twoOneNode);

        TreeItem second1 = tvp.getTreeItem(two[1]).toCompletableFuture().get();
        assertEquals("8", second1.name);
        assertEquals("Fib(8)", second1.label);
        assertEquals("Fib(8) = 42", second1.tooltip);
        assertEquals("Node id remains", two1.id, second1.id);

        assertSame("Nodes are cached", twoOneNode, tvp.findNode(two1.id));
    }

    @Test
    public void dontInheritTheSameDescription() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setName("a node");

        TreeItem item = new TreeItem(0, an, false, "");
        assertEquals("name is set", "a node", item.name);
        assertEquals("label is inherited", "a node", item.label);
        assertNull("description isn't copied", item.tooltip);
    }

    @Test
    public void useDifferentDescription() {
        AbstractNode an = new AbstractNode(Children.LEAF);
        an.setName("a node");
        an.setShortDescription("better node");

        TreeItem item = new TreeItem(0, an, false, "");
        assertEquals("name is set", "a node", item.name);
        assertEquals("label is inherited", "a node", item.label);
        assertEquals("description is set", "better node", item.tooltip);
    }

    @Test
    public void resourceUriSpecifiedOnFileObjects() throws Exception {
        File tmp = File.createTempFile("sample", ".tmp");
        FileObject fo = FileUtil.toFileObject(tmp);
        assertNotNull("File object for temporary file found", fo);

        AbstractNode an = new AbstractNode(Children.LEAF, fo.getLookup());
        an.setName(fo.getNameExt());

        TreeItem item = new TreeItem(0, an, false, "");
        assertEquals("label is inherited", fo.getNameExt(), item.label);
        assertNotNull("resource uri specified", item.resourceUri);
        assertEquals(item.resourceUri, URLMapper.findURL(fo, URLMapper.EXTERNAL).toString());
    }


    /**
     * A call to getNodeId for a nested node should materialize & register parents
     * in the TreeViewProvider.
     */
    @Test
    public void getNodeIdRegistersParents() throws Exception {
        ExplorerManager em = new ExplorerManager();
        Node r = new FibNode(10);
        em.setRootContext(r);
        
        Node[] firstLevel = r.getChildren().getNodes(true);
        Node[] secondLevel = firstLevel[0].getChildren().getNodes(true);
        Node[] thirdLevel = secondLevel[1].getChildren().getNodes(true);
        
        Node toFind = thirdLevel[0];
        
        TreeViewProviderImpl tvp = new TreeViewProviderImpl(em, registry);
        
        CompletionStage<Integer> nodeId = tvp.getNodeId(toFind);
        int id = nodeId.toCompletableFuture().get(30, TimeUnit.SECONDS);
        assertSame(toFind, tvp.findNode(id));
        
        // make clone of the map before asking for node IDs.
        SortedMap<Integer, TreeViewProvider.NodeHolder> map = tvp.getHolders();
        int parentId = tvp.findId(toFind.getParentNode());
        int grandParentId = tvp.findId(toFind.getParentNode().getParentNode());
        
        assertNotNull(map.get(parentId));
        assertNotNull(map.get(grandParentId));
    }

    static final class TreeViewProviderImpl extends TreeViewProvider {
        private Set<Node> changed = new HashSet<>();

        TreeViewProviderImpl(ExplorerManager em, TreeNodeRegistry r) {
            super("", em, r, Lookup.EMPTY);
        }

        @Override
        protected void onDidChangeTreeData(Node n, NodeChangeType type, String property) {
            changed.add(n);
        }
        
        public void assertChanged(Node n) {
            assertTrue("Expecting " + n + " among " + changed, changed.remove(n));
        }
    }

    private static class FibNode extends AbstractNode {
        private final int value;

        public FibNode(int value) {
            super(value <= 2 ? Children.LEAF : new FibChildren(value));
            this.value = value;
        }

        @Override
        public String getName() {
            return "" + value;
        }

        @Override
        public String getDisplayName() {
            return "Fib(" + value + ")";
        }

        @Override
        public String getShortDescription() {
            return "Fib(" + value + ") = " + result();
        }

        void extraAdd(int value) {
            getChildren().add(new Node[] { new FibNode(value) });
            fireDisplayNameChange(null, null);
            fireNameChange(null, null);
            fireShortDescriptionChange(null, null);
        }

        int result() {
            if (isLeaf()) {
                return 1;
            }
            int sum = 0;
            for (Node n : getChildren().getNodes(true)) {
                sum += ((FibNode)n).result();
            }
            return sum;
        }
    }

    private static final class FibChildren extends Children.Keys<Integer> {
        private FibChildren(int value) {
            setKeys(new Integer[] {
                value - 1,
                value - 2,
            });
        }

        @Override
        protected Node[] createNodes(Integer key) {
            return new Node[] { new FibNode(key) };
        }
    }
}
