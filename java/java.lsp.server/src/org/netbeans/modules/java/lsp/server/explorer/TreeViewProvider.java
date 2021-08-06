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

import java.beans.PropertyChangeEvent;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

public abstract class TreeViewProvider {
    private static final RequestProcessor INITIALIZE = new RequestProcessor("Initialize nodes", 5);
    private final ExplorerManager manager;
    /** @GuardedBy(this) */
    private final Set<Node> nodeListenerAttached = new WeakSet<>();
    private final NodeListener nodeListener;

    protected TreeViewProvider(ExplorerManager manager) {
        this.manager = manager;
        this.nodeListener = new NodeListener() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                notifyChange(ev.getNode());
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                notifyChange(ev.getNode());
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
                notifyChange(ev.getNode());
            }

            @Override
            public void nodeDestroyed(NodeEvent ev) {
                notifyChange(ev.getNode());
            }

            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                notifyChange((Node) ev.getSource());
            }

            private void notifyChange(Node src) {
                onDidChangeTreeData(src, TreeItem.findId(src));
            }
        };
    }
    protected abstract void onDidChangeTreeData(Node n, int id);

    public final CompletionStage<int[]> getChildren(int id) {
        return getChildren(TreeItem.findNode(id)).thenApply((nodes) -> {
            int[] ids = new int[nodes.length];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = TreeItem.findId(nodes[i]);
            }
            return ids;
        });
    }

    public final CompletionStage<Node[]> getChildren(Node nodeOrNull) {
        Node node = getNodeOrRoot(nodeOrNull);
        return CompletableFuture.supplyAsync(() -> {
            return node.getChildren().getNodes(true);
        }, INITIALIZE);
    }


    public final CompletionStage<Node> getParent(Node node) {
        return CompletableFuture.completedFuture(node.getParentNode());
    }

    public final CompletionStage<TreeItem> getTreeItem(int id) {
        return getTreeItem(TreeItem.findNode(id));
    }

    public CompletionStage<TreeItem> getRootInfo() {
        Node n = getNodeOrRoot(null);
        return CompletableFuture.completedFuture(TreeItem.find(n));
    }

    private final CompletionStage<TreeItem> getTreeItem(Node n) {
        ensureListenerAttached(n);
        TreeItem item = TreeItem.find(n);
        return CompletableFuture.completedFuture(item);
    }

    private Node getNodeOrRoot(Node nodeOrNull) {
        Node node;
        if (nodeOrNull == null) {
            node = manager.getRootContext();
        } else {
            node = nodeOrNull;
        }
        ensureListenerAttached(node);
        return node;
    }

    private synchronized void ensureListenerAttached(Node node) {
        if (nodeListenerAttached.add(node)) {
            node.addNodeListener(nodeListener);
        }
    }
}
