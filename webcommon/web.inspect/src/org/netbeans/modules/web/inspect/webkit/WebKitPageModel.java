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
package org.netbeans.modules.web.inspect.webkit;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolBar;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.spi.EnhancedBrowser;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.inspect.files.Files;
import org.netbeans.modules.web.inspect.webkit.ui.CSSStylesPanel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.css.CSS;
import org.netbeans.modules.web.webkit.debugging.api.css.StyleSheetHeader;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * WebKit-based implementation of {@code PageModel}.
 *
 * @author Jan Stola
 */
public class WebKitPageModel extends PageModel {
    /** Request processor used by this class. */
    private final RequestProcessor RP = new RequestProcessor(WebKitPageModel.class);
    /** Entry point to WebKit debugging API. */
    WebKitDebugging webKit;
    /** Document node. */
    private DOMNode documentNode;
    /** Lock that guards the access to {@code documentNode} field. */
    private final Object DOCUMENT_NODE_LOCK = new Object();
    /** Nodes of the document (maps ID of the node to the node itself).*/
    private final Map<Integer,DOMNode> nodes = Collections.synchronizedMap(new HashMap<Integer,DOMNode>());
    /** Selected nodes. */
    private List<? extends org.openide.nodes.Node> selectedNodes = Collections.EMPTY_LIST;
    /** Highlighted nodes. */
    private List<? extends org.openide.nodes.Node> highlightedNodes = Collections.EMPTY_LIST;
    /** Nodes matching the selected rule. */
    private List<? extends org.openide.nodes.Node> nodesMatchingSelectedRule = Collections.EMPTY_LIST;
    /** Selector of the selected rule. */
    private String selectedSelector;
    /** Selector of the highlighted rule. */
    private String highlightedSelector;
    /** WebKit DOM domain listener. */
    private final DOM.Listener domListener;
    /** WebKit CSS domain listener. */
    private final CSS.Listener cssListener;
    /** Determines whether the selection mode is switched on. */
    private boolean selectionMode;
    /** Determines whether the selection between the IDE and the browser pane is synchronized. */
    private boolean synchronizeSelection = true;
    /** Owner project of the inspected page. */
    private final Project project;
    /** Page context. */
    private final Lookup pageContext;
    /** Updater of the style-sheets in the browser according to changes of the corresponding source files. */
    private final CSSUpdater cSSUpdater = CSSUpdater.getDefault();
    /**
     * Map with content documents in the inspected page. Maps node ID of
     * the document node to the corresponding {@code RemoteObject}.
     */
    private final Map<Integer,RemoteObject> contentDocumentMap = new HashMap<Integer,RemoteObject>();
    /** Cache of {@code RemoteObject}s. Maps node ID to the corresponding {@code RemoteObject}. */
    private final Map<Integer,RemoteObject> remoteObjectMap = Collections.synchronizedMap(
            new HashMap<Integer,RemoteObject>());
    /** Maps a node ID to pseudo-classes forced for the node. */
    private final Map<Integer,EnumSet<CSS.PseudoClass>> pseudoClassMap = Collections.synchronizedMap(
            new HashMap<Integer,EnumSet<CSS.PseudoClass>>());
    /** Logger used by this class */
    static final Logger LOG = Logger.getLogger(WebKitPageModel.class.getName());

    /**
     * Creates a new {@code WebKitPageModel}.
     *
     * @param pageContext page context.
     */
    public WebKitPageModel(Lookup pageContext) {
        this.pageContext = pageContext;
        this.webKit = pageContext.lookup(WebKitDebugging.class);
        this.project = pageContext.lookup(Project.class);
        this.external = (pageContext.lookup(JToolBar.class) == null); // Ugly heuristics
        addPropertyChangeListener(new WebPaneSynchronizer());
        addPropertyChangeListener(new EditorSynchronizer());

        // Register DOM domain listener
        domListener = createDOMListener();
        DOM dom = webKit.getDOM();
        dom.setClassForHover(CSSUtils.HOVER_CLASS);
        dom.addListener(domListener);
        
        // Register CSS domain listener
        cssListener = createCSSListener();
        CSS css = webKit.getCSS();
        css.addListener(cssListener);

        initializePage();
    }

    /**
     * Determines whether there is a dummy page (that will be replaced by
     * the actual inspected page soon) loaded in the browser pane.
     */
    private boolean dummyPage;
    
    /**
     * Prepares the page for inspection.
     */
    private void initializePage() {
        Resource.clearCache();
        // documentUpdated event is not delivered when no node information
        // was sent to the client => requesting document node to make sure
        // that we obtain next documentUpdated event (that we need to be able
        // to reinitialize the page)
        org.openide.nodes.Node node = getDocumentNode();

        if (node == null) {
            LOG.info("getDocumentNode() returned null!"); // NOI18N
        } else {
            // Do not initialize the temporary page unnecessarily
            Node webKitNode = node.getLookup().lookup(Node.class);
            webKitNode = convertNode(webKitNode);
            Node.Attribute attr = webKitNode.getAttribute(":netbeans_temporary"); // NOI18N
            dummyPage = (attr != null);
            if (!dummyPage) {
                // init
                String initScript = Files.getScript("initialization"); // NOI18N
                webKit.getRuntime().evaluate(initScript);
                if (isExternal()) {
                    String shortcutsScript = Files.getScript("shortcuts"); // NOI18N
                    webKit.getRuntime().evaluate(shortcutsScript);
                }
                cSSUpdater.start(webKit, getProject());
            }
        }
    }

    /**
     * Returns the underlying {@code WebKitDebugging} object.
     *
     * @return the underlying {@code WebKitDebugging} object.
     */
    public WebKitDebugging getWebKit() {
        return webKit;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public Lookup getPageContext() {
        return pageContext;
    }

    @Override
    protected void dispose() {
        invokeInAllDocuments("NetBeans.releasePage();", false); // NOI18N
        DOM dom = webKit.getDOM();
        dom.removeListener(domListener);
        CSS css = webKit.getCSS();
        css.removeListener(cssListener);
        cSSUpdater.stop();
    }

    @Override
    public org.openide.nodes.Node getDocumentNode() {
        assert !EventQueue.isDispatchThread();
        synchronized (DOCUMENT_NODE_LOCK) {
            if (documentNode == null) {
                DOM dom = webKit.getDOM();
                Node node = dom.getDocument();
                if (node != null) {
                    synchronized (this) {
                        documentNode = updateNodes(node);
                    }
                }
            }
            return documentNode;
        }
    }

    @Override
    public void removeNode(org.openide.nodes.Node node) {
        Node webKitNode = node.getLookup().lookup(Node.class);
        if (webKitNode != null) {
            webKit.getDOM().removeNode(webKitNode);
        }
    }

    @Override
    public String getDocumentURL() {
        String documentURL = null;
        org.openide.nodes.Node node = getDocumentNode();
        if (node != null) {
            Node webKitNode = node.getLookup().lookup(Node.class);
            if (webKitNode != null) {
                documentURL = webKitNode.getDocumentURL();
            }
        }
        return documentURL;
    }

    /**
     * Creates DOM domain listener.
     *
     * @return DOM domain listener.
     */
    private DOM.Listener createDOMListener() {
        return (DOM.Listener)Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {DOM.Listener.class},
                new RPInvocationHandler(new DOMListener()));
    }

    /**
     * Invocation handler that invokes the methods in {@code RequestProcessor}.
     */
    private static class RPInvocationHandler implements InvocationHandler {
        /** {@code RequestProcessor} used by the invocation handler. */
        private static final RequestProcessor RP = new RequestProcessor(RPInvocationHandler.class);
        /** Object on which the methods are invoked. */
        private final Object target;

        /**
         * Creates a new {@code RPInvocationHandler}.
         *
         * @param target object on which the methods are invoked.
         */
        RPInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, final Object[] args) throws Throwable {
            Class[] parameters = method.getParameterTypes();        
            final Method thisMethod = target.getClass().getMethod(method.getName(), parameters);        
            if (Void.TYPE.equals(thisMethod.getReturnType())) {
                // Method of WebKit Debugging API interfaces
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        invoke0(thisMethod, args);
                    }
                });
                return null;
            } else if (Object.class.equals(method.getDeclaringClass())
                    && "equals".equals(method.getName())) { // NOI18N
                return args[0] == proxy;
            } else {
                return invoke0(thisMethod, args);
            }
        }

        /**
         * Invokes the given method on the {@code target}.
         * 
         * @param method method to invoked.
         * @param args arguments of the method.
         * @return return value of the method.
         */
        Object invoke0(Method method, Object[] args) {
            Object result = null;
            try {
                result = method.invoke(target, args);                      
            } catch (IllegalAccessException iaex) {
                Logger.getLogger(RPInvocationHandler.class.getName()).log(Level.INFO, null, iaex);
            } catch (InvocationTargetException itex) {
                Logger.getLogger(RPInvocationHandler.class.getName()).log(Level.INFO, null, itex);
            }
            return result;
        }

    }

    /**
     * DOM domain listener.
     */
    private class DOMListener implements DOM.Listener {

        @Override
        public void childNodesSet(Node parent) {
            synchronized(WebKitPageModel.this) {
                int nodeId = parent.getNodeId();
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    updateNodes(parent);
                    domNode.updateChildren(parent);
                }
            }
        }

        @Override
        public void childNodeRemoved(Node parent, Node child) {
            synchronized(WebKitPageModel.this) {
                int nodeId = parent.getNodeId();
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    domNode.updateChildren(parent);
                }
                // Nodes with a content document are removed and added
                // again when a content document changes (and sometimes
                // even when it doesn't change) => we are not removing
                // them from 'nodes' collection to be able to reuse
                // them once they are back.
                Node contentDocument = child.getContentDocument();
                if (contentDocument == null) {
                    nodes.remove(child.getNodeId());
                } else {
                    contentDocumentMap.remove(contentDocument.getNodeId());
                }
            }
        }

        @Override
        public void childNodeInserted(Node parent, Node child) {
            synchronized(WebKitPageModel.this) {
                int nodeId = parent.getNodeId();
                updateNodes(child);
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    domNode.updateChildren(parent, child);
                }
            }
        }

        @Override
        public void documentUpdated() {
            synchronized (DOCUMENT_NODE_LOCK) {
                documentNode = null;
                synchronized(WebKitPageModel.this) {
                    nodes.clear();
                    contentDocumentMap.clear();
                    remoteObjectMap.clear();
                    pseudoClassMap.clear();
                    selectedNodes = Collections.EMPTY_LIST;
                    highlightedNodes = Collections.EMPTY_LIST;
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            firePropertyChange(PROP_DOCUMENT, null, null);
                        }
                    });
                }
            }
        }

        @Override
        public void attributeModified(Node node, String attrName, String attrValue) {
            synchronized(WebKitPageModel.this) {
                // Attribute modifications that represent selection/highlight
                final boolean selected = ":netbeans_selected".equals(attrName); // NOI18N
                final boolean highlighted = ":netbeans_highlighted".equals(attrName); // NOI18N
                final boolean selectMode = ":netbeans_select_mode".equals(attrName); // NOI18N
                if (selected || highlighted) {
                    if (!isSelectionMode()) {
                        // Some delayed selection/highlight modifications
                        // can appear after deactivation of the selection mode
                        // => ignore these delayed events
                        return;
                    }
                    DOMNode n = getNode(node.getNodeId());
                    final List<? extends org.openide.nodes.Node> selection;
                    if (n == null) {
                        selection = Collections.EMPTY_LIST;
                    } else {
                        if ("set".equals(attrValue)) { // NOI18N
                            selection = Collections.singletonList(n);
                        } else if ("clear".equals(attrValue)) { // NOI18N
                            selection = Collections.EMPTY_LIST;
                        } else {
                            List<org.openide.nodes.Node> newSelection = new ArrayList<org.openide.nodes.Node>();
                            newSelection.addAll(selectedNodes);
                            if ("add".equals(attrValue)) { // NOI18N
                                newSelection.add(n);
                            } else if ("remove".equals(attrValue)) { // NOI18N
                                newSelection.remove(n);
                            }
                            selection = newSelection;
                        }
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            if (selected) {
                                setSelectedNodes(selection);
                                firePropertyChange(PageModel.PROP_BROWSER_SELECTED_NODES, null, null);
                            } else {
                                setHighlightedNodesImpl(selection);
                            }
                        }
                    });
                    return;
                } else if (selectMode) {
                    final boolean newSelectMode = !isSelectionMode();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            setSelectionMode(newSelectMode);
                        }
                    });
                    return;
                }

                // Update DOMNode
                int nodeId = node.getNodeId();
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    domNode.updateAttributes();
                }
            }
        }

        @Override
        public void attributeRemoved(Node node, String attrName) {
            synchronized(WebKitPageModel.this) {
                int nodeId = node.getNodeId();
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    domNode.updateAttributes();
                }
            }
        }

        @Override
        public void characterDataModified(Node node) {
            synchronized(WebKitPageModel.this) {
                int nodeId = node.getNodeId();
                DOMNode domNode = nodes.get(nodeId);
                if (domNode != null) {
                    domNode.updateCharacterData();
                }
            }
        }

        @Override
        public void shadowRootPushed(Node host, Node shadowRoot) {
            synchronized(WebKitPageModel.this) {
                int hostId = host.getNodeId();
                updateNodes(shadowRoot);
                DOMNode domNode = nodes.get(hostId);
                if (domNode != null) {
                    domNode.updateChildren(host, shadowRoot);
                }
            }
        }

        @Override
        public void shadowRootPopped(Node host, Node shadowRoot) {
            synchronized(WebKitPageModel.this) {
                int hostId = host.getNodeId();
                DOMNode domNode = nodes.get(hostId);
                if (domNode != null) {
                    domNode.updateChildren(host);
                }
                nodes.remove(shadowRoot.getNodeId());
            }
        }

    }

    /**
     * Creates CSS domain listener.
     * 
     * @return CSS domain listener.
     */
    private CSS.Listener createCSSListener() {
        return new CSS.Listener() {
            @Override
            public void mediaQueryResultChanged() {
            }

            @Override
            public void styleSheetAdded(StyleSheetHeader header) {
            }

            @Override
            public void styleSheetRemoved(String styleSheetId) {
            }

            @Override
            public void styleSheetChanged(String styleSheetId) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        // Issue 217896
                        String script = "NetBeans.repaintGlassPane();"; // NOI18N
                        invokeInAllDocuments(script);
                    }
                });
            }
        };
    }

    /**
     * Updates the map of known nodes with the information about the specified
     * node and its sub-nodes.
     *
     * @param node node to start the update at.
     * @return {@code DOMNode} that corresponds to the specified node.
     */
    private DOMNode updateNodes(Node node) {
        int nodeId = node.getNodeId();
        DOMNode domNode = nodes.get(nodeId);
        if (domNode == null) {
            domNode = new DOMNode(this, node);
            nodes.put(nodeId, domNode);
            if (nodes.size() > MAX_NODES) { // Check page size
                showPageSizeWarning();
            }
        }
        boolean updateChildren = false;
        List<Node> subNodes = node.getChildren();
        if (subNodes == null) {
            int nodeType = node.getNodeType();
            if (nodeType == org.w3c.dom.Node.ELEMENT_NODE
                    || nodeType == org.w3c.dom.Node.DOCUMENT_NODE
                    || nodeType == org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE) {
                if (node.getChildrenCount() > MAX_CHILD_NODES) {
                    showPageSizeWarning();
                }
                webKit.getDOM().requestChildNodes(nodeId);
            }
        } else {
            for (Node subNode : subNodes) {
                updateNodes(subNode);
            }
            updateChildren = true;
        }
        for (Node shadowRoot : node.getShadowRoots()) {
            updateNodes(shadowRoot);
            updateChildren = true;
        }
        final Node contentDocument = node.getContentDocument();
        if (contentDocument != null) {
            updateNodes(contentDocument);
            updateChildren = true;
            RP.post(new Runnable() {
                @Override
                public void run() {
                    String initScript = Files.getScript("initialization") // NOI18N
                        + "\nNetBeans.setSelectionMode("+selectionMode+");"; // NOI18N
                    RemoteObject remote = webKit.getDOM().resolveNode(contentDocument, null);
                    if (remote == null) {
                        LOG.log(Level.INFO, "Node with ID {0} resolved to null RemoteObject!", contentDocument.getNodeId()); // NOI18N
                    } else {
                        webKit.getRuntime().callFunctionOn(remote, "function() {\n"+initScript+"\n}"); // NOI18N
                        synchronized (WebKitPageModel.this) {
                            contentDocumentMap.put(contentDocument.getNodeId(), remote);
                        }
                    }
                }
            });
        }
        if (updateChildren) {
            domNode.updateChildren(node);
        }
        return domNode;
    }

    /** Maximum number of elements in a page for safe inspection. */
    private static final int MAX_NODES = 100000;
    /** Maximum number of child nodes of one node for safe inspection. */
    private static final int MAX_CHILD_NODES = 20000;
    /** Determines whether a warning about the size of the page was shown. */
    private boolean pageSizeWarningShown = false;
    
    /**
     * Checks if the current information that we have about the inspected
     * page is too large to continue with the page inspection safely.
     */
    @NbBundle.Messages({
        "WebKitPageModel.pageSizeWarningTitle=Page Too Large",
        "WebKitPageModel.pageSizeWarningMessage="
                + "The page is too large to inspect it in NetBeans safely. "
                + "You may run out of memory if you continue with the inspection. "
                + "Do you want to close this page and stop its inspection?"
    })
    private void showPageSizeWarning() {
        if (!pageSizeWarningShown) {
            pageSizeWarningShown = true;
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    Bundle.WebKitPageModel_pageSizeWarningMessage(),
                    Bundle.WebKitPageModel_pageSizeWarningTitle(),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE
            );
            DialogDisplayer.getDefault().notify(descriptor);
            if (descriptor.getValue() == NotifyDescriptor.YES_OPTION) {
                EnhancedBrowser browser = pageContext.lookup(EnhancedBrowser.class);
                if (browser != null) {
                    browser.close(true);
                }
            }
        }
    }

    /**
     * Returns {@code DOMNode} with the specified ID.
     *
     * @param nodeId ID of the requested {@code DOMNode}.
     * @return {@code DOMNode} with the specified ID.
     */
    public DOMNode getNode(int nodeId) {
        return nodes.get(nodeId);
    }

    @Override
    public void setSelectedNodes(List<? extends org.openide.nodes.Node> nodes) {
        assert !EventQueue.isDispatchThread();
        synchronized (this) {
            if (selectedNodes.equals(nodes)) {
                return;
            }
            selectedNodes = knownNodes(nodes);
        }
        logPageInspectionUsage();
        firePropertyChange(PROP_SELECTED_NODES, null, null);
    }

    private List<? extends org.openide.nodes.Node> knownNodes(List<? extends org.openide.nodes.Node> nodeList) {
        List<org.openide.nodes.Node> knownNodes = new ArrayList<org.openide.nodes.Node>(nodeList.size());
        for (org.openide.nodes.Node node : nodeList) {
            Node webKitNode = node.getLookup().lookup(Node.class);
            if (webKitNode == null) {
                knownNodes.add(node);
            } else {
                int nodeId = webKitNode.getNodeId();
                org.openide.nodes.Node knownNode = nodes.get(nodeId);
                if (knownNode == null) {
                    LOG.log(Level.INFO, "Ignoring node that is not (no longer?) valid: {0}.", node); // NOI18N
                } else {
                    knownNodes.add(knownNode);
                }
            }
        }
        return knownNodes;
    }

    private static final UsageLogger USAGE_LOGGER = new UsageLogger.Builder("org.netbeans.ui.metrics.web.inspect")  // NOI18N
            .firstMessageOnly(true)
            .message(WebKitPageModel.class, "USG_PAGE_INSPECTION") // NOI18N
            .create();

    /** Logs the usage of page inspection. */
    private void logPageInspectionUsage() {
        USAGE_LOGGER.log();
    }

    @Override
    public List<org.openide.nodes.Node> getSelectedNodes() {
        synchronized (this) {
            return (List<org.openide.nodes.Node>) Collections.unmodifiableList(selectedNodes);
        }
    }

    @Override
    public void setHighlightedNodes(List<? extends org.openide.nodes.Node> nodes) {
        assert !EventQueue.isDispatchThread();
        if (isSynchronizeSelection()) {
            setHighlightedNodesImpl(nodes);
        }
    }

    void setHighlightedNodesImpl(List<? extends org.openide.nodes.Node> nodes) {
        synchronized (this) {
            if (highlightedNodes.equals(nodes)) {
                return;
            }
            highlightedNodes = nodes;
        }
        firePropertyChange(PROP_HIGHLIGHTED_NODES, null, null);
    }

    @Override
    public List<? extends org.openide.nodes.Node> getHighlightedNodes() {
        synchronized (this) {
            return Collections.unmodifiableList(highlightedNodes);
        }
    }

    @Override
    public void setSelectedSelector(String selector) {
        synchronized (this) {
            selectedSelector = selector;
        }
        setNodesMatchingSelectedRule(matchingNodes(selector));
        firePropertyChange(PROP_SELECTED_RULE, null, null);
    }

    @Override
    public String getSelectedSelector() {
        synchronized (this) {
            return selectedSelector;
        }
    }

    private void setNodesMatchingSelectedRule(List<? extends org.openide.nodes.Node> nodes) {
        synchronized (this) {
            nodesMatchingSelectedRule = nodes;
        }
    }

    @Override
    public List<? extends org.openide.nodes.Node> getNodesMatchingSelectedRule() {
        synchronized (this) {
            return nodesMatchingSelectedRule;
        }
    }

    @Override
    public void setHighlightedSelector(String selector) {
        synchronized (this) {
            highlightedSelector = selector;
        }
        setHighlightedNodes(matchingNodes(selector));
        firePropertyChange(PROP_HIGHLIGHTED_RULE, null, null);
    }

    @Override
    public String getHighlightedSelector() {
        synchronized (this) {
            return highlightedSelector;
        }
    }

    /**
     * Returns the nodes matching the specified selector.
     *
     * @param selector selector that should match the nodes.
     * @return nodes matching the specified selector.
     */
    List<DOMNode> matchingNodes(String selector) {
        List<DOMNode> domNodes = Collections.EMPTY_LIST;
        if (selector != null) {
            selector = selector.replace(":hover", "." + CSSUtils.HOVER_CLASS); // NOI18N
            DOM dom = webKit.getDOM();
            Node documentElement = dom.getDocument();
            if (documentElement != null) {
                List<Node> matchingNodes = dom.querySelectorAll(documentElement, selector);
                domNodes = new ArrayList<DOMNode>(matchingNodes.size());
                for (Node node : matchingNodes) {
                    int nodeId = node.getNodeId();
                    DOMNode domNode = getNode(nodeId);
                    if (domNode != null) {
                        domNodes.add(domNode);
                    }
                }
            }
        }
        return domNodes;
    }

    @Override
    public void setSelectionMode(boolean selectionMode) {
        synchronized (this) {
            if (this.selectionMode == selectionMode) {
                return;
            }
            this.selectionMode = selectionMode;
            webKit.getCSS().setClassForHover(selectionMode ? CSSUtils.HOVER_CLASS : null);
        }
        firePropertyChange(PROP_SELECTION_MODE, !selectionMode, selectionMode);
        // Reset highlighted nodes
        if (!selectionMode) {
            setHighlightedNodesImpl(Collections.EMPTY_LIST);
        }
    }

    @Override
    public boolean isSelectionMode() {
        synchronized (this) {
            return selectionMode;
        }
    }

    @Override
    public void setSynchronizeSelection(boolean synchronizeSelection) {
        synchronized (this) {
            if (this.synchronizeSelection == synchronizeSelection) {
                return;
            }
            this.synchronizeSelection = synchronizeSelection;
        }
        if (!dummyPage) {
            firePropertyChange(PROP_SYNCHRONIZE_SELECTION, !synchronizeSelection, synchronizeSelection);
        }
    }

    @Override
    public boolean isSynchronizeSelection() {
        synchronized (this) {
            return synchronizeSelection;
        }
    }

    /**
     * Invoke the specified script in all content documents.
     *
     * @param script script to invoke.
     */
    void invokeInAllDocuments(String script) {
        invokeInAllDocuments(script, true);
    }

    /**
     * Invoke the specified script in all content documents.
     *
     * @param script script to invoke.
     * @param synchronous determines whether the invocation
     * should be synchronous or asynchronous.
     */
    void invokeInAllDocuments(String script, boolean synchronous) {
        // Main document
        org.netbeans.modules.web.webkit.debugging.api.Runtime runtime = webKit.getRuntime();
        if (synchronous) {
            runtime.evaluate(script);
        } else {
            runtime.execute(script);
        }

        // Content documents
        script = "function() {\n" + script + "\n}"; // NOI18N
        List<RemoteObject> documents;
        synchronized (this) {
            documents = new ArrayList<RemoteObject>(contentDocumentMap.size());
            documents.addAll(contentDocumentMap.values());
        }
        for (RemoteObject contentDocument : documents) {
            if (synchronous) {
                runtime.callFunctionOn(contentDocument, script);
            } else {
                runtime.callProcedureOn(contentDocument, script);
            }
        }
    }

    /**
     * Returns the {@code RemoteObject} that corresponds to the specified node.
     * 
     * @param webKitNode node whose {@code RemoteObject} should be returned.
     * @return {@code RemoteObject} that corresponds to the specified node
     * or {@code null} when the retrieval of such {@code RemoteObject} failed.
     */
    RemoteObject getRemoteObject(Node webKitNode) {
        int id = webKitNode.getNodeId();
        RemoteObject remote = remoteObjectMap.get(id);
        if (remote == null) {
            remote = webKit.getDOM().resolveNode(webKitNode, null);
            if (remote != null) {
                remoteObjectMap.put(id, remote);
            }
        }
        return remote;
    }

    /**
     * Converts the WebKit node into a node that should be highlighted/selected.
     * Usually this method returns the passed node, but there are some exceptions
     * like document nodes.
     *
     * @param node node to convert.
     * @return node that should be highlighted/selected instead of the given node.
     */
    Node convertNode(Node node) {
        Node result = node;
        int type = node.getNodeType();
        if (type == org.w3c.dom.Node.DOCUMENT_NODE) {
            // Highlight/select document element
            List<Node> subNodes = node.getChildren();
            if (subNodes != null) {
                for (Node subNode : subNodes) {
                    // There should be just one element
                    if (subNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        result = subNode;
                        break;
                    }
                }
            }
        } else if (type == org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE) {
            // Shadow root => highlight/select the host
            result = node.getParent();
        }
        return result;
    }

    @Override
    public CSSStylesView getCSSStylesView() {
        CSSStylesPanel view = CSSStylesPanel.getDefault();
        view.updatePageModel();
        return view;
    }

    /**
     * Returns pseudo-classes forced for the specified node.
     * 
     * @param node node whose forced pseudo-classes should be returned.
     * @return pseudo-classes forced for the specified node.
     */
    public CSS.PseudoClass[] getPseudoClasses(Node node) {
        int nodeId = node.getNodeId();
        Set<CSS.PseudoClass> pseudoClassSet = pseudoClassMap.get(nodeId);
        if (pseudoClassSet == null) {
            pseudoClassSet = Collections.EMPTY_SET;
        }
        CSS.PseudoClass[] pseudoClasses = new CSS.PseudoClass[pseudoClassSet.size()];
        int i=0;
        for (CSS.PseudoClass pseudoClass : pseudoClassSet) {
            pseudoClasses[i++]=pseudoClass;
        }
        return pseudoClasses;
    }

    /**
     * Adds a forced pseudo-class for the specified node.
     * 
     * @param node node for which the pseudo-class should be forced.
     * @param pseudoClass pseudo-class to force.
     */
    public void addPseudoClass(Node node, CSS.PseudoClass pseudoClass) {
        int nodeId = node.getNodeId();
        EnumSet<CSS.PseudoClass> pseudoClassSet = pseudoClassMap.get(nodeId);
        if (pseudoClassSet == null) {
            pseudoClassSet = EnumSet.noneOf(CSS.PseudoClass.class);
            pseudoClassMap.put(nodeId, pseudoClassSet);
        }
        pseudoClassSet.add(pseudoClass);
    }

    /**
     * Removes a pseudo-class from the set of pseudo-classes forced for a node.
     * 
     * @param node node for which the pseudo-class should removed.
     * @param pseudoClass pseudo-class that should no longer be forced.
     */
    public void removePseudoClass(Node node, CSS.PseudoClass pseudoClass) {
        int nodeId = node.getNodeId();
        Set<CSS.PseudoClass> pseudoClassSet = pseudoClassMap.get(nodeId);
        if (pseudoClassSet != null) {
            pseudoClassSet.remove(pseudoClass);
        }
    }

    /** Determines whether this page model corresponds to a page in an external browser. */
    private final boolean external;
    
    /**
     * Determines whether this page model corresponds to a page in an external browser.
     * 
     * @return {@code true} when this page model corresponds to a page
     * in an external browser, returns {@code false} otherwise.
     */
    boolean isExternal() {
        return external;
    }

    /** Request processor for {@code WebPaneSynchronizer}. */
    private static final RequestProcessor WPRP = new RequestProcessor(WebPaneSynchronizer.class);
    
    class WebPaneSynchronizer implements PropertyChangeListener {
        private final Object LOCK_HIGHLIGHT = new Object();
        private final Object LOCK_SELECTION = new Object();

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            WPRP.post(new Runnable() {
                @Override
                public void run() {
                    String propName = evt.getPropertyName();
                    if (propName.equals(PageModel.PROP_HIGHLIGHTED_NODES)) {
                        if (shouldSynchronizeHighlight()) {
                            updateHighlight();
                        }
                    } else if (propName.equals(PageModel.PROP_SELECTED_NODES)) {
                        if (shouldSynchronizeSelection()) {
                            updateSelection();
                        }
                    } else if (propName.equals(PageModel.PROP_SELECTED_RULE)) {
                        if (shouldSynchronizeSelection()) {
                            updateSelectedRule(getNodesMatchingSelectedRule());
                        }
                    } else if (propName.equals(PageModel.PROP_SELECTION_MODE)) {
                        updateSelectionMode();
                        updateSynchronization();
                    } else if (propName.equals(PageModel.PROP_SYNCHRONIZE_SELECTION)) {
                        updateSelectionMode();
                        updateSynchronization();
                    } else if (propName.equals(PageModel.PROP_DOCUMENT)) {
                        initializePage();
                        updateSelectionMode();
                    }
                }
            });
        }

        private boolean shouldSynchronizeSelection() {
            return isSelectionMode();
        }

        private boolean shouldSynchronizeHighlight() {
            return true;
        }

        private void updateSynchronization() {
            if (shouldSynchronizeSelection()) {
                updateSelection();
                updateSelectedRule(getNodesMatchingSelectedRule());
            } else {
                updateSelection(Collections.EMPTY_LIST);
                updateSelectedRule(Collections.EMPTY_LIST);
            }
            if (shouldSynchronizeHighlight()) {
                updateHighlight();
            } else {
                updateHighlight(Collections.EMPTY_LIST);
            }
        }

        private void updateHighlight() {
            List<? extends org.openide.nodes.Node> nodes = getHighlightedNodes();
            updateHighlight(nodes);
        }

        private void updateHighlight(List<? extends org.openide.nodes.Node> nodes) {
            synchronized (LOCK_HIGHLIGHT) {
                // Initialize the next highlight in all content documents
                invokeInAllDocuments("NetBeans.initNextHighlight();"); // NOI18N

                // Add highlighted nodes into the next highlight (in their document)
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    webKitNode = convertNode(webKitNode);
                    RemoteObject remote = getRemoteObject(webKitNode);
                    if (remote != null) {
                        webKit.getRuntime().callFunctionOn(remote, "function() {NetBeans.addElementToNextHighlight(this);}"); // NOI18N
                    }
                }

                // Finalize the next highlight in all content documents
                invokeInAllDocuments("NetBeans.finishNextHighlight();"); // NOI18N
            }
        }

        private void updateSelection() {
            List<? extends org.openide.nodes.Node> nodes = getSelectedNodes();
            updateSelection(nodes);
        }

        private void updateSelection(List<? extends org.openide.nodes.Node> nodes) {
            updateSelection(nodes, ""); // NOI18N
        }
        
        private void updateSelection(List<? extends org.openide.nodes.Node> nodes, String type) {
            synchronized (LOCK_SELECTION) {
                // Initialize the next selection in all content documents
                invokeInAllDocuments("NetBeans.initNext" + type + "Selection();"); // NOI18N

                // Add selected nodes into the next selection (in their document)
                for (org.openide.nodes.Node node : nodes) {
                    Node webKitNode = node.getLookup().lookup(Node.class);
                    if (webKitNode == null) {
                        continue;
                    }
                    webKitNode = convertNode(webKitNode);
                    RemoteObject remote = getRemoteObject(webKitNode);
                    if (remote != null) {
                        webKit.getRuntime().callProcedureOn(remote, "function() {NetBeans.addElementToNext" + type + "Selection(this);}"); // NOI18N
                    }
                }

                // Finalize the next selection in all content documents
                invokeInAllDocuments("NetBeans.finishNext" + type + "Selection();"); // NOI18N
            }
        }

        private void updateSelectedRule(List<? extends org.openide.nodes.Node> nodes) {
            updateSelection(nodes, "Rule"); // NOI18N
        }

        private synchronized void updateSelectionMode() {
            boolean selectionMode = isSelectionMode();
            
            // Activate/deactivate (observation of mouse events over) canvas
            invokeInAllDocuments("NetBeans.setSelectionMode("+selectionMode+")"); // NOI18N

            performHoverRelatedStyleSheetUpdate(selectionMode);
        }

        /**
         * Performs the replacement of {@code :hover} pseudo-class
         * by the class used to simulate hovering (and vice versa).
         * 
         * @param selectionMode current value of selection mode.
         */
        private void performHoverRelatedStyleSheetUpdate(final boolean selectionMode) {
            String hover = "':hover'"; // NOI18N
            String clazz = "'." + CSSUtils.HOVER_CLASS + "'"; //NOI18N
            String params;
            if (selectionMode) {
                params = hover + "," + clazz; // NOI18N
            } else {
                params = clazz + "," + hover; // NOI18N
            }
            invokeInAllDocuments("NetBeans.replaceInCSSSelectors("+params+")"); // NOI18N
        }

    }

}
