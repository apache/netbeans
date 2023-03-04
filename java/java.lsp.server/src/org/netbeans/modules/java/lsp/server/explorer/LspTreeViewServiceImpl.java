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
package org.netbeans.modules.java.lsp.server.explorer;

import org.netbeans.modules.java.lsp.server.explorer.api.TreeViewService;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.netbeans.modules.java.lsp.server.explorer.api.ConfigureExplorerParams;
import org.netbeans.modules.java.lsp.server.explorer.api.CreateExplorerParams;
import org.netbeans.modules.java.lsp.server.explorer.api.FindPathParams;
import org.netbeans.modules.java.lsp.server.explorer.api.GetResourceParams;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangedParams;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeOperationParams;
import org.netbeans.modules.java.lsp.server.explorer.api.ResourceData;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This is a delegate bridge between the {@link TreeNodeRegistryImpl} and the LSP protocol.
 * @author sdedic
 */
@JsonSegment("nodes")
public class LspTreeViewServiceImpl implements TreeViewService, LanguageClientAware {
    private static final Logger LOG = Logger.getLogger(LspTreeViewServiceImpl.class.getName());
    
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
                    LOG.log(Level.FINER, "Firing item {0} changed", itemId);
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
        LOG.log(Level.FINER, "> info({0})", nodeId);
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
        LOG.log(Level.FINER, "> children({0})", id);
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
        LOG.log(Level.FINER, "> delete({0})", id);
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
    
    @Override
    public CompletableFuture<ResourceData> getResource(GetResourceParams params) {
        URI uri = params.getUri();
        if (params.getAcceptEncodings() != null) {
            if (!Arrays.asList(params.getAcceptEncodings()).contains("base64")) { // NOI18N
                throw new IllegalArgumentException("Base64 encoding must be accepted.");
            }
        }
        return CompletableFuture.completedFuture(treeService.imageContents(uri));
    }

    @Override
    public CompletableFuture<int[]> findPath(FindPathParams params) {
        Object toSelect = params.getSelectData();
        LOG.log(Level.FINER, "> findPath(fromId = {0}, select = {1})", new Object[] { params.getRootNodeId(), toSelect });
        TreeViewProvider tvp = treeService.providerOf(params.getRootNodeId());
        if (tvp == null) {
            return null;
        }
        Node rootNode = tvp.getExplorerManager().getRootContext();
        for (PathFinder finder : tvp.getLookup().lookupAll(PathFinder.class)) {
            Node target = finder.findPath(rootNode, toSelect);
            if (target != null) {
                return constructPath(tvp, rootNode, target);
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    
    private static int[] toIntArray(List<Integer> wrappers) {
        int[] ret = new int[wrappers.size()];
        int idx = 0;
        for (Integer i : wrappers) {
            ret[idx++] = i;
        }
        return ret;
    }
    
    CompletableFuture<int[]> constructLevel(TreeViewProvider tvp, Node from, Node node, List<Integer> collectedIds) {
        if (node == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletionStage<Void> idStage = tvp.getNodeId(node).thenAccept(id -> collectedIds.add(id));
        CompletionStage<int[]> levelStage = idStage.thenCompose(v -> {
            if (node == from) {
                Collections.reverse(collectedIds);
                return CompletableFuture.completedFuture(toIntArray(collectedIds));
            } else {
                return tvp.getParent(node).thenCompose(n -> constructLevel(tvp, from, n, collectedIds));
            }
        });
        
        return levelStage.toCompletableFuture();
    }
    
    CompletableFuture<int[]> constructPath(TreeViewProvider tvp, Node from, Node to) {
        List<Integer> collectedIds = new ArrayList<>();
        return constructLevel(tvp, from, to, collectedIds);
    }
    
}
