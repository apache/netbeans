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

import java.awt.Image;
import java.beans.BeanInfo;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataListener;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeItemData;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataEvent;
import org.netbeans.modules.java.lsp.server.explorer.api.TreeDataProvider;
import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.lsp.server.explorer.TreeItem.CollapsibleState;
import org.netbeans.modules.java.lsp.server.explorer.TreeItem.IconDescriptor;
import org.netbeans.modules.java.lsp.server.explorer.api.NodeChangeType;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public abstract class TreeViewProvider {
    private static final Logger LOG = Logger.getLogger(TreeViewProvider.class.getName());
    static final RequestProcessor INITIALIZE = new RequestProcessor("Initialize nodes", 5); // NOI18N
    
    /**
     * Delay to report node changes to the client, in ms.
     */
    private static final int NODE_CHANGE_DELAY = 100;

    /**
     * ID of this TreeView
     */
    private final String treeId;
    
    private final ExplorerManager manager;
    
    /**
     * Listens for changes on created Nodes
     */
    private final NodeListener nodeListener;
    
    /**
     * Maintains node's identity and caches images.
     */
    private final TreeNodeRegistry nodeRegistry;
    
    /**
     * Context for the tree type.
     */
    private final Lookup context;
    
    /**
     * Decoration factories for this tree type.
     */
    private final Lookup.Result<? extends TreeDataProvider.Factory> factories;

    /**
     * Listens on changes from item decorators.
     */
    // PENDING: (re)attach listener to providers
    private final TreeDataListener l = new TreeDataListener() {
        @Override
        public void treeItemDataChanged(TreeDataEvent e) {
            queueNodeChange(e.getOriginalNode());
        }
    };
    
    /**
     * Fires item changes.
     */
    private final RequestProcessor.Task nodeChanges = INITIALIZE.create(new Firer());
    
    /**
     * Client-configured provider for context values. Comes first.
     */
    // @GuardedBy(this)
    private TreeDataProvider clientProvider;
    
    /**
     * Set of changed nodes to be reported to the client. The set is replaced when 
     * the firer starts after a delay.
     */
    // @GuardedBy(this)
    private Set<Node> changes = new LinkedHashSet<>();
    
    /**
     * Item data providers.
     */
    // @GuardedBy(this)
    private TreeDataProvider[] providers;
    
    /**
     * Maps of TreeItems that should be held in the memory.
     */
    // @GuardedBy(this)
    private SortedMap<Integer, NodeHolder> holdChildren = new TreeMap<>();
    
    /**
     * Node > identity map. Note that FilterNodes equals compare the original node, so
     * IdentityHashMap-style map must be used.
     */
    // @GuardedBy(this)
    private WeakIdentityMap<Node, Integer> idMap = WeakIdentityMap.newHashMap();
    
    protected TreeViewProvider(String treeId, ExplorerManager manager, TreeNodeRegistry registry, Lookup context) {
        this.treeId = treeId;
        this.context = context;
        this.manager = manager;
        
        Node n;
        this.nodeRegistry = registry;
        
        this.nodeListener = new NodeListener() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
                LOG.log(Level.FINER, "tree {0} children of node {2} added: {1}", new Object[] { treeId, ev, ev.getNode() });
                onDidChangeTreeData(ev.getNode(), NodeChangeType.CHILDREN, null);
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                LOG.log(Level.FINER, "tree {0} children of node {2} removed: {1}", new Object[] { treeId, ev, ev.getNode() });
                onDidChangeTreeData(ev.getNode(), NodeChangeType.CHILDREN, null);
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
                LOG.log(Level.FINER, "tree {0} children of node {2} reordered: {1}", new Object[] { treeId, ev, ev.getNode() });
                onDidChangeTreeData(ev.getNode(), NodeChangeType.CHILDREN, null);
            }

            @Override
            public void nodeDestroyed(NodeEvent ev) {
                LOG.log(Level.FINER, "tree {0} children of node {2} destroyed: {1}", new Object[] { treeId, ev, ev.getNode() });
                removeNode(ev.getNode());
                onDidChangeTreeData(ev.getNode(), NodeChangeType.DESTROY, null);
            }

            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                LOG.log(Level.FINER, "tree {0} property of node {2} changed: {1}", new Object[] { treeId, ev, ev.getSource()});
                onDidChangeTreeData((Node)ev.getSource(), NodeChangeType.SELF, null);
            }
        };
        factories = context.lookupResult(TreeDataProvider.Factory.class);
        factories.addLookupListener((e) -> refreshProviders());
        // initialize ID for the root node
        findId(manager.getRootContext());
        refreshProviders();
    }
    
    protected abstract void onDidChangeTreeData(Node n, NodeChangeType type, String property);

    public Lookup getLookup() {
        return context;
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /**
     * Keeps the Nodes that were published to the client in the memory. If the client
     * requested children, also keeps them with their identity.
     */
    static class NodeHolder {
        private final int nodeId;
        private final Node node;
        
        // @GuardedBy(TreeViewProvider.this)
        Map<Integer, Node>  id2Child;

        public NodeHolder(int nodeId, Node node) {
            this.nodeId = nodeId;
            this.node = node;
        }
    }

    // @GuardedBy(this)
    private Integer releaseNode(Node n) {
        LOG.log(Level.FINER, "Released node: {0}", n);
        for (TreeDataProvider p : providers) {
            p.nodeReleased(n);
        }
        if (clientProvider != null) {
            clientProvider.nodeReleased(n);
        }
        Integer lspId = idMap.remove(n);
        if (lspId != null) {
            if (nodeRegistry != null) {
                nodeRegistry.unregisterNode(lspId, n);
            }
        }
        return lspId;
    }
    
    /**
     * Removes the node from tracking maps. Unregisters the node from the node registry (node > provider mapping).
     * @param n node to remove
     */
    private void removeNode(Node n) {
        Node parent = n.getParentNode();
        Integer parentLspId;
        LOG.log(Level.FINER, "Removed node: {0}", n);
        synchronized (this) {
            Integer lspId = releaseNode(n);
            parentLspId = idMap.get(parent);
            if (!(lspId instanceof Integer && parentLspId instanceof Integer)) {
                return;
            }
            NodeHolder nh = holdChildren.get(parentLspId);
            if (nh == null || nh.id2Child == null) {
                return;
            }
            // during getChildren() the id2Child map is replaced.
            if (nh.id2Child.remove(lspId) != n) {
                return;
            }
        }
        // PENDING: perhaps too many changes if many sibling nodes are being removed ?
        onDidChangeTreeData(parent, NodeChangeType.CHILDREN, null);
    }
    
    /**
     * Notifies about children change. Will not actually modify the children - that will be done
     * after the next client's request.
     * @param parent parent whose children changed.
     */
    protected void notifyChildrenChange(Node parent) {
        int id = findId(parent);
        synchronized (this) {
            NodeHolder nh = holdChildren.get(id);
            // block child events for nodes that have not been queried yet or were collapsed
            if (nh == null || nh.id2Child == null) {
                return;
            }
        }
        onDidChangeTreeData(parent, NodeChangeType.CHILDREN, null);
    }
    
    /**
     * Notification from the client, that the nodes have been collapsed. The corresponding
     * Children will be evicted from {@link #holdChildren}
     * @param ids node that has been collapsed.
     * @return 
     */
    public final CompletionStage childrenCollapsed(int id) {
        return childrenCollapsed(id, true);
    }
    
    public void setClientProvider(TreeDataProvider p) {
        synchronized (this) {
            clientProvider = p;
        }
        refreshProviders();
    }

    /**
     * Frees references that keep Nodes in the memory.
     * @param id ID of the collapsed node.
     * @param fromClient true, if the request comes from the client, false, if it cascades from the parent.
     * @return completion handle, no useful info.
     */
    final CompletionStage<Void> childrenCollapsed(int id, boolean fromClient) {
        Set<Integer> childIds;
        
        synchronized (this) {
            NodeHolder nh = holdChildren.get(id);
            if (nh == null || nh.id2Child == null) {
                // WTF ?
                return CompletableFuture.completedFuture(null);
            }
            childIds = nh.id2Child.keySet();
            if (fromClient) {
                LOG.log(Level.FINER, "Client node collapsed: {0}:{1}, will collapse {2} direct children", new Object[] { id, nh.node, childIds.size() });
            }
            nh.id2Child = null;
            // remove holders for all direct children:
            holdChildren.keySet().removeAll(Arrays.asList(childIds));
            idMap.remove(nh.node);
        }
        INITIALIZE.post(() -> {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Cascading collapse from {0} to {1}", new Object[] { id, Arrays.asList(childIds) }); 
            }
            for (int i : childIds) {
                childrenCollapsed(i, false);
            }
        });
        return CompletableFuture.completedFuture(null);
    }
    
    public Node findNode(int id) {
        synchronized (this) {
            NodeHolder nh = holdChildren.get(id);
            return nh == null ? null : nh.node;
        }
    }
    
    /**
     * Obtains an internal ID from the node. 
     * was not passed to the client yet
     * @param n node
     * @return 
     */
    protected int findId(Node n) {
        if (n == null) {
            return -1;
        }
        if (nodeRegistry == null) {
            return -1;
        }
        synchronized (this) {
            Integer lspId = idMap.get(n);
            if (lspId != null) {
                return lspId;
            }
        }
        int id = nodeRegistry.registerNode(n, this);
        synchronized (this) {
            idMap.put(n, id);
            holdChildren.put(id, new NodeHolder(id, n));
        }
        n.addNodeListener(nodeListener);
        return id;
    }

    /**
     * Constructs a TreeItem around the Node.
     * @param n the Node
     * @return a TreeItem suitable for LSP transmit
     */
    public TreeItem findTreeItem(Node n) {
        LOG.log(Level.FINER, "Finding tree item for node {0}", n);
        TreeDataProvider[] pa = this.providers;
        String v;
        boolean expanded;
        int id;
        
        synchronized (this) {
            id = findId(n);
            expanded = id >= 0 && holdChildren.get(id).id2Child != null;
        }

        TreeItemData data = new TreeItemData();
        if (pa != null) {
            for (TreeDataProvider p : pa) {
                TreeItemData contrib = p.createDecorations(n, expanded);
                if (contrib != null) {
                    data.merge(contrib);
                }
            }
        }
        v = data.getContextValues() == null ? "" : String.join(" ", data.getContextValues()); // NOI18N

        TreeItem ti = new TreeItem(id, n, expanded, v);
        if (data.isLeaf()) {
            ti.collapsibleState = CollapsibleState.None;
        }
        
        if (data.getIconImage() != null && data.getIconImage() != DUMMY_NODE.getIcon(BeanInfo.ICON_COLOR_16x16)) {
            TreeNodeRegistry.ImageDataOrIndex idoi = nodeRegistry.imageOrIndex(data.getIconImage());
            if (idoi != null) {
                try {
                    URI baseURI = builtinURI2URI(idoi.baseURI);
                    if (baseURI != null) {
                        ti.iconDescriptor = new IconDescriptor();
                        ti.iconDescriptor.baseUri = baseURI;
                        ti.iconDescriptor.composition = idoi.composition;
                    }
                } catch (URISyntaxException ex) {
                    LOG.log(Level.WARNING, "Cannot convert URL: {0}", idoi.baseURI);
                }
            }
        }
        ti.contextValue = v;
        ti.command = data.getCommand();
        if (data.getResourceURI() != null) {
            ti.resourceUri = data.getResourceURI().toString();
        }
        LOG.log(Level.FINER, "Finding tree item for node {0} => {1} ", new Object[] { n, ti });
        return ti;
    }
    
    private int[] childrenIds(Node parent, Node[] nodes) {
        Map<Integer, Node> newId2Node = new HashMap<>();
        int parentId = findId(parent);
        NodeHolder nh;
        synchronized (this) {
            nh = holdChildren.get(parentId);
            if (nh == null) {
                nh = new NodeHolder(parentId, parent);
                holdChildren.put(nh.nodeId, nh);
            }
        }
        LOG.log(Level.FINER, "Expanded node id {0}: {1}", new Object[] { parentId, parent });
        int[] ids = new int[nodes.length];
        for (int i = 0; i < ids.length; i++) {
            int nid = findId(nodes[i]);
            ids[i] = nid;
            newId2Node.put(nid, nodes[i]);
        }
        Map<Integer, Node> obsolete;
        synchronized (this) {
            if (nh.id2Child != null) {
                obsolete = nh.id2Child;
                obsolete.keySet().removeAll(newId2Node.keySet());
            } else {
                obsolete = null;
            }
            nh.id2Child = newId2Node;
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Children of id {0}: {1}", new Object[] { parentId, Arrays.toString(ids) });
        }
        if (obsolete != null) {
            synchronized (this) {
                for (Node n : obsolete.values()) {
                    releaseNode(n);
                }
            }
        }
        return ids;
    }

    public final CompletionStage<int[]> getChildren(int id) {
        Node n = findNode(id);
        return getChildren(n).thenApply((nodes) -> childrenIds(n, nodes));
    }

    public final CompletionStage<Node[]> getChildren(Node nodeOrNull) {
        Node node = getNodeOrRoot(nodeOrNull);
        return CompletableFuture.completedFuture(node.getChildren().getNodes());
    }

    public final CompletionStage<Node> getParent(Node node) {
        return CompletableFuture.completedFuture(node.getParentNode());
    }
    
    public CompletionStage<Integer> getNodeId(Node n) {
        Integer i;
        List<Node> toExpand = new ArrayList<>();
        toExpand.add(n);
        synchronized (this) {
            i = idMap.get(n);
            if (i != null) {
                return CompletableFuture.completedFuture(i);
            }
            Node parent = n.getParentNode();
            while (parent != null) {
                toExpand.add(parent);
                i = idMap.get(parent);
                if (i != null) {
                    break;
                }
                parent = parent.getParentNode();
            }
            if (parent == null) {
                return CompletableFuture.completedFuture(null);
            }
        }
        CompletionStage<Integer> nextStage = null;
        // do not iterate index #0
        for (int idx = toExpand.size() - 1; idx > 0; idx--) {
            CompletionStage<Node[]> stage = null;
            Node p = toExpand.get(idx);
            if (nextStage == null) {
                stage = getChildren(p);
            } else {
                stage = nextStage.thenCompose((x) -> getChildren(p));
            }
            final int fidx = idx - 1;
            nextStage = stage.thenApply((ch) -> findId(toExpand.get(fidx)));
        }
        return nextStage;
    }

    public final CompletionStage<TreeItem> getTreeItem(int id) {
        return getTreeItem(findNode(id));
    }

    public CompletionStage<TreeItem> getRootInfo() {
        Node n = getNodeOrRoot(null);
        return CompletableFuture.completedFuture(findTreeItem(n));
    }

    private final CompletionStage<TreeItem> getTreeItem(Node n) {
        // findTreeItem will create NodeHolder & attach Listener
        TreeItem item = findTreeItem(n);
        return CompletableFuture.completedFuture(item);
    }

    private Node getNodeOrRoot(Node nodeOrNull) {
        Node node;
        if (nodeOrNull == null) {
            node = manager.getRootContext();
        } else {
            node = nodeOrNull;
        }
        return node;
    }
    
    private void queueNodeChange(Node n) {
        synchronized (this) {
            boolean sch = changes.isEmpty();
            changes.add(n);
            
            if (sch) {
                nodeChanges.schedule(NODE_CHANGE_DELAY);
            }
        }
    }
    
    private synchronized Set<Node> changes() {
        Set<Node> q = changes;
        if (q.isEmpty()) {
            return null;
        }
        changes = new LinkedHashSet<>();
        return q;
    }
    
    // PENDING: watch out for provider-reported node changes.
    private void refreshProviders() {
        TreeDataProvider clP;
        TreeDataProvider[] old;
        
        synchronized (this) {
            clP = this.clientProvider;
            old = this.providers;
        }
        List<TreeDataProvider> l = new ArrayList<>();
        
        if (clP != null) {
            l.add(clP);
        }
        for (TreeDataProvider.Factory f : factories.allInstances()) {
            TreeDataProvider p = f.createProvider(treeId);
            if (p != null) {
                l.add(p);
            }
        }
        TreeDataProvider[] n;
        
        if (l.isEmpty()) {
            n = null;
        } else {
            n = l.toArray(new TreeDataProvider[0]);
        }
        if (Objects.deepEquals(old, n)) {
            return;
        }
        
        synchronized (this) {
            this.providers = n;
        }
        // fire complete tree change
        onDidChangeTreeData(null, null, null);
    }
    
    class Firer implements Runnable {
        Firer() {
        }
        
        @Override
        public void run() {
            Collection<Node> toFire = changes();
            if (toFire == null) {
                return;
            }
            for (Node n : toFire) {
                int id = findId(n);
                if (id != -1) {
                    onDidChangeTreeData(n, null, null);
                }
            }
        }
    }
    
    static final Node DUMMY_NODE = new AbstractNode(Children.LEAF);

    private static ExplorerManager dummyManager() {
        ExplorerManager m = new ExplorerManager();
        m.setRootContext(DUMMY_NODE);
        return m;
    }

    /**
     * Dummy provider that serves root, no children and sinks all events.
     */
    static final TreeViewProvider NONE = new TreeViewProvider("", dummyManager(), null, Lookup.EMPTY) {
        final Node root = super.manager.getRootContext();
        
        @Override
        public CompletionStage<TreeItem> getRootInfo() {
            return super.getRootInfo();
        }

        @Override
        public Node findNode(int id) {
            return root;
        }

        @Override
        protected int findId(Node n) {
            // there are no nodes at all
            return -1; 
        }

        @Override
        protected void onDidChangeTreeData(Node n, NodeChangeType type, String property) {
        }
    };
    
    static URI builtinURI2URI(URI u) throws URISyntaxException {
        if (u == null) {
            return null;
        }
        // I could work through URLMapper + FileUtil, but that would open the JAR
        // as filesystem, which gives some perf overhead:
        try {
            if ("jar".equals(u.getScheme())) { // NOI18N
                URL u2 = u.toURL();
                String s = u2.getPath();
                int i = s.indexOf('!');
                // I don't want to send file: / jar: URLs over LSP wire,
                // let's have just resource path
                if (i != -1) {
                    return new URI("nbres", s.substring(i + 1), null); // NOI18N
                }
            }
        } catch (MalformedURLException ex) {
            throw new URISyntaxException(u.toString(), ex.getMessage());
        }
        return u;
    }

    public static URI findImageURI(Image i) {
        URL u = ImageUtilities.findImageBaseURL(i);
        if (u == null) {
            return null;
        }
        String s = u.toString();
        try {
            if (s.contains(":")) {
                return new URI(s);
            } else {
                return new URI("nbres:/" + s);
            }
        } catch (URISyntaxException ex) {
            LOG.log(Level.WARNING, "Unable to interpret image ID: {0}", s);
            return null;
        }
    }
    
    /* testing */ SortedMap<Integer, NodeHolder> getHolders() {
        synchronized (this) {
            return new TreeMap<>(holdChildren);
        }
    }
}
