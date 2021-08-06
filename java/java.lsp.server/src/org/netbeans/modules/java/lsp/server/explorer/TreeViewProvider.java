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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

abstract class TreeViewProvider {
    private static final RequestProcessor INITIALIZE = new RequestProcessor("Initialize nodes", 5);
    private final ExplorerManager manager;

    TreeViewProvider(ExplorerManager manager) {
        this.manager = manager;
    }
    abstract void onDidChangeTreeData(Node n);
    final CompletionStage<Node[]> getChildren(Node nodeOrNull) {
        Node node;
        if (nodeOrNull == null) {
            node = manager.getRootContext();
        } else {
            node = nodeOrNull;
        }
        return CompletableFuture.supplyAsync(() -> {
            return node.getChildren().getNodes(true);
        }, INITIALIZE);
    }

    final CompletionStage<Node> getParent(Node node) {
        return CompletableFuture.completedFuture(node.getParentNode());
    }

    final CompletionStage<TreeItem> getTreeItem(Node n) {
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
