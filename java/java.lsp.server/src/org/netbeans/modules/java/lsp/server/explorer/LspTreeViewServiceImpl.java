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

import org.netbeans.modules.java.lsp.server.explorer.api.TreeViewService;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.netbeans.modules.java.lsp.server.explorer.api.ConfigureExplorerParams;
import org.netbeans.modules.java.lsp.server.explorer.api.CreateExplorerParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeOperationParams;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This is a delegate bridge between the {@link TreeNodeRegistryImpl} and the LSP protocol.
 * @author sdedic
 */
@JsonSegment("nodes")
public class LspTreeViewServiceImpl implements TreeViewService, LanguageClientAware {
    private final Lookup sessionLookup;
    /**
     * The delegate tree service.
     */
    private final TreeNodeRegistryImpl treeService;

    private NbCodeLanguageClient langClient;

    public LspTreeViewServiceImpl(Lookup sessionLookup) {
        this.sessionLookup = sessionLookup;
        this.treeService = new TreeNodeRegistryImpl(sessionLookup) {
            @Override
            protected void notifyItemChanged(NodeChangedParams itemId) {
                if (langClient != null) {
                    langClient.notifyNodeChange(itemId);
                }
            }
        };
    }
    
    public TreeNodeRegistry getNodeRegistry() {
        return treeService;
    }
    
    @Override
    public void connect(LanguageClient client) {
        this.langClient = (NbCodeLanguageClient)client;
    }

    @Override
    public CompletableFuture<Void> configure(ConfigureExplorerParams par) {
        TreeViewProvider tvp = treeService.providerOf(par.getRootNodeId());
        CompletableFuture f = new CompletableFuture();
        if (tvp == null) {
            f.completeExceptionally(new IllegalArgumentException("Invalid root ID: " + par.getRootNodeId()));
            return f;
        }
        try {
            NodeLookupContextValues vals = NodeLookupContextValues.nodeLookup(par.getExportClasses());
            tvp.setClientProvider(vals);
            f.complete(null);
        } catch (IllegalArgumentException ex) {
            f.completeExceptionally(ex);
        }
        return f;
    }
    
    // NotificationType<int, void>
    public void nodesCollapsed(int parentId) {
        TreeViewProvider p = treeService.providerOf(parentId);
        p.childrenCollapsed(parentId);
    }
    
    public CompletableFuture<Boolean> nodesDelete(NodeOperationParams params) {
        int nodeId = params.getNodeId();
        CompletableFuture<Boolean> ret = new CompletableFuture<>();
        TreeViewProvider p = treeService.providerOf(nodeId);
        if (p == null) {
            ret.complete(false);
            return ret;
        }

        Node n = p.findNode(nodeId);
        if (n != null && n.canDestroy()) {
            try {
                n.destroy();
                ret.complete(true);
            } catch (IOException ex) {
                ret.completeExceptionally(ex);
            }
        } else {
            ret.complete(false);
        }
        return ret;
    }

    // export const info = new ProtocolRequestType<NodeOperationParams, Data, never,void, void>('nodes/info');
    public CompletableFuture<TreeItem> info(NodeOperationParams params) {
        int nodeId = params.getNodeId();
        TreeViewProvider tvp = treeService.providerOf(nodeId);
        return tvp.getTreeItem(nodeId).toCompletableFuture();
    }

    // export const explorermanager = new ProtocolRequestType<CreateExplorerParams, never, Data, void, void>('nodes/explorermanager');
    public CompletableFuture<TreeItem> explorerManager(CreateExplorerParams params) {
        String id = params.getExplorerId();
        return treeService.createProvider(id).thenCompose(tv -> tv.getRootInfo()).toCompletableFuture();
    }

    // export const children = new ProtocolRequestType<NodeOperationParams, number[], never, void, void>('nodes/children');
    @Override
    public CompletableFuture<int[]> getChildren(NodeOperationParams params) {
        int id = params.getNodeId();
        TreeViewProvider tvp = treeService.providerOf(id);
        return tvp.getChildren(id).toCompletableFuture();
    }

    // export const collapsed = new ProtocolNotificationType<NodeOperationParams, void>('nodes/collapsed');
    @Override
    public void notifyCollapsed(NodeOperationParams params) {
        // no action for now
    }

    @Override
    public CompletableFuture<Boolean> delete(NodeOperationParams params) {
        int id = params.getNodeId();
        TreeViewProvider tvp = treeService.providerOf(id);
        if (tvp != null) {
            Node n = tvp.findNode(id);
            if (n != null && n.canDestroy()) {
                // might prompt, so run asynchronously
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        n.destroy();
                        return true;
                    } catch (IOException ex) {
                        throw new CompletionException(ex);
                    }
                });
            }
        }
        return CompletableFuture.completedFuture(false);
    }
}
