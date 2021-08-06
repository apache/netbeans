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

abstract class TreeViewProvider {
    private static final RequestProcessor INITIALIZE = new RequestProcessor("Initialize nodes", 5);
    private final ExplorerManager manager;
    /** @GuardedBy(this) */
    private final Set<Node> nodeListenerAttached = new WeakSet<>();
    private final NodeListener nodeListener;

    TreeViewProvider(ExplorerManager manager) {
        this.manager = manager;
        this.nodeListener = new NodeListener() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                onDidChangeTreeData(ev.getNode());
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                onDidChangeTreeData(ev.getNode());
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
                onDidChangeTreeData(ev.getNode());
            }

            @Override
            public void nodeDestroyed(NodeEvent ev) {
                onDidChangeTreeData(ev.getNode());
            }

            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                onDidChangeTreeData((Node) ev.getSource());
            }
        };
    }
    abstract void onDidChangeTreeData(Node n);
    final CompletionStage<Node[]> getChildren(Node nodeOrNull) {
        Node node;
        if (nodeOrNull == null) {
            node = manager.getRootContext();
        } else {
            node = nodeOrNull;
        }
        ensureListenerAttached(node);
        return CompletableFuture.supplyAsync(() -> {
            return node.getChildren().getNodes(true);
        }, INITIALIZE);
    }

    private synchronized void ensureListenerAttached(Node node) {
        if (nodeListenerAttached.add(node)) {
            node.addNodeListener(nodeListener);
        }
    }

    final CompletionStage<Node> getParent(Node node) {
        return CompletableFuture.completedFuture(node.getParentNode());
    }

    final CompletionStage<TreeItem> getTreeItem(Node n) {
        ensureListenerAttached(n);
        TreeItem item = new TreeItem();
        if (n.isLeaf()) {
            item.collapsibleState = TreeItem.CollapsibleState.None;
        } else {
            item.collapsibleState = TreeItem.CollapsibleState.Collapsed;
        }
        item.id = n.getName();
        item.label = n.getDisplayName();
        item.description = n.getShortDescription();
        item.tooltip = n.getHtmlDisplayName();
        return CompletableFuture.completedFuture(item);
    }
}
