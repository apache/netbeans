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
package org.netbeans.modules.web.webkit.debugging.api.dom;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the DOM domain of WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class DOM {
    /** Transport used by this instance. */
    private final TransportHelper transport;
    /** WebKit debugging this instance belongs to. */
    private final WebKitDebugging webKit;
    /** Callback for DOM event notifications. */
    private final ResponseCallback callback;
    /** Registered listeners. */
    private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    /** Document node */
    private Node documentNode;
    /** Counter of documents the user navigated across. */
    private int documentCounter;
    /** Known nodes - maps node ID to Node. */
    private final Map<Integer,Node> nodes = new HashMap<Integer,Node>();
    /** Document initialization lock. */
    private final Object DOCUMENT_LOCK = new Object();

    /**
     * Creates a new wrapper for the DOM domain of WebKit Remote Debugging Protocol.
     * 
     * @param transport transport to use.
     * @param webKit WebKit remote debugging API wrapper to use.
     */
    public DOM(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.webKit = webKit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    /**
     * Enables the DOM agent.
     */
    public void enable() {
        transport.sendBlockingCommand(new Command("DOM.enable")); // NOI18N
    }

    /**
     * Disables the DOM agent.
     */
    public void disable() {
        transport.sendCommand(new Command("DOM.disable")); // NOI18N
    }

    /**
     * Returns the document node (the root DOM node).
     * 
     * @return document node.
     */
    public Node getDocument() {
        synchronized (DOCUMENT_LOCK) {
            Node document;
            int counter;
            synchronized (this) {
                document = documentNode;
                counter = documentCounter;
            }
            if (document == null) {
                Response response = transport.sendBlockingCommand(new Command("DOM.getDocument")); // NOI18N
                synchronized (this) {
                    if (counter == documentCounter) {
                        if (response != null) {
                            JSONObject result = response.getResult();
                            if (result != null) {
                                JSONObject node = (JSONObject)result.get("root"); // NOI18N
                                documentNode = new Node(node);
                                updateNodesMap(documentNode);
                            }
                        }
                        return documentNode;
                    }
                    // else {
                    //     Navigation occured before we obtained the response
                    //     => return the document node of the new document
                    //     (which is done by the return statement behind
                    //     this synchronized block)
                    // }
                }
                return getDocument();
            }
            return document;
        }
    }

    /**
     * Ensures that the given node and its sub-nodes are in the map
     * of all known nodes.
     * 
     * @param node node to check/insert.
     */
    private synchronized void updateNodesMap(Node node) {
        removeClassForHover(node);
        nodes.put(node.getNodeId(), node);
        synchronized (node) {
            List<Node> subNodes = node.getChildren();
            if (subNodes != null) {
                for (Node subNode : subNodes) {
                    updateNodesMap(subNode);
                }
            }
            Node document = node.getContentDocument();
            if (document != null) {
                updateNodesMap(document);
            }
            for (Node shadowRoot : node.getShadowRoots()) {
                updateNodesMap(shadowRoot);
            }
        }
    }

    /**
     * Requests children information for the specified node. The children
     * are delivered in the form of {@code setChildNodes} events. This method
     * should be called for nodes whose {@code getChildren()} method
     * returns {@code null} only. Otherwise, the children information
     * of the node should be up to date.
     * 
     * @param nodeId ID of the node whose children are requested.
     */
    public void requestChildNodes(int nodeId) {
        JSONObject params = new JSONObject();
        params.put("nodeId", nodeId); // NOI18N
        if (transport.isVersionUnknownBeforeRequestChildNodes()) {
            transport.sendCommand(new Command("DOM.getChildNodes", params)); // NOI18N
        } else {
            transport.sendCommand(new Command("DOM.requestChildNodes", params)); // NOI18N
        }
    }

    /**
     * Highlights the given rectangle. The coordinates are absolute with
     * respect to the main frame viewport.
     * 
     * Note that this method does nothing in WebView currently.
     * 
     * Note that at most one rectangle or node can be highlighted.
     * Invocation of {@code highlightRect()} or {@code highlightNode()} cancels
     * any existing highlight.
     * 
     * @param rect rectangle to highlight.
     * @param fill fill color (can be {@code null}).
     * @param outline outline color (can be {@cod enull}).
     */
    public void highlightRect(Rectangle rect, Color fill, Color outline) {
        JSONObject params = new JSONObject();
        params.put("x", rect.x); // NOI18N
        params.put("y", rect.y); // NOI18N
        params.put("width", rect.width); // NOI18N
        params.put("height", rect.height); // NOI18N
        if (fill != null) {
            params.put("color", HighlightConfig.colorToRGBA(fill)); // NOI18N
        }
        if (outline != null) {
            params.put("outlineColor", HighlightConfig.colorToRGBA(outline)); // NOI18N
        }
        transport.sendCommand(new Command("DOM.highlightRect", params)); // NOI18N
    }

    /**
     * Highlights the given node.
     * 
     * Note that this method does nothing in WebView currently.
     * 
     * Note that at most one node or rectangle can be highlighted.
     * Invocation of {@code highlightRect()} or {@code highlightNode()} cancels
     * any existing highlight.
     * 
     * @param node node to highlight.
     * @param highlight description of the requested highlight.
     */
    public void highlightNode(Node node, HighlightConfig highlight) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("highlightConfig", highlight.toJSONObject()); // NOI18N
        transport.sendCommand(new Command("DOM.highlightNode", params)); // NOI18N
    }

    /**
     * Hides the current node or rectangle highlight.
     */
    public void hideHighlight() {
        transport.sendCommand(new Command("DOM.hideHighlight")); // NOI18N
    }

    /**
     * Executes the given selector on the specified node and returns
     * a matching node.
     * 
     * @param node context of the query.
     * @param selector selector to execute.
     * @return node matching the selector or {@code null}.
     */
    public Node querySelector(Node node, String selector) {
        Node n = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("selector", selector); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.querySelector", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                int nodeId = ((Number)result.get("nodeId")).intValue(); // NOI18N
                synchronized (this) {
                    n = nodes.get(nodeId);
                }
            }
        }
        return n;
    }

    /**
     * Executes the given selector on the specified node and returns
     * all matching nodes.
     * 
     * @param node context of the query.
     * @param selector selector to execute.
     * @return nodes matching the selector (empty list is returned when
     * no matching node is found).
     */
    public List<Node> querySelectorAll(Node node, String selector) {
        List<Node> list = Collections.emptyList();
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("selector", selector); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.querySelectorAll", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                list = new ArrayList<Node>();
                JSONArray array = (JSONArray)result.get("nodeIds"); // NOI18N
                synchronized (this) {
                    for (Object id : array) {
                        int nodeId = ((Number)id).intValue();
                        Node n = nodes.get(nodeId);
                        if (n != null) {
                            list.add(n);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Sets node name of the given node. In fact, the old node is replaced
     * by a new node with the new name.
     * 
     * @param node node whose name should be set.
     * @param name new name of the node.
     * @return node that has the specified name and that replaces
     * the given old node.
     */
    public Node setNodeName(Node node, String name) {
        Node n = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("name", name); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.setNodeName", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                int nodeId = ((Number)result.get("nodeId")).intValue(); // NOI18N
                synchronized (this) {
                    n = nodes.get(nodeId);
                }
            }
        }
        return n;
    }

    /**
     * Sets node value of the given node.
     * 
     * @param node node whose node value should be replaced.
     * @param value new node value of the node.
     */
    public void setNodeValue(Node node, String value) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("value", value); // NOI18N
        transport.sendCommand(new Command("DOM.setNodeValue", params)); // NOI18N
    }

    /**
     * Removes the specified node.
     * 
     * @param node node to remove.
     */
    public void removeNode(Node node) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        transport.sendCommand(new Command("DOM.removeNode", params)); // NOI18N
    }

    /**
     * Sets value of the specified attribute.
     * 
     * @param node node whose attribute should be modified.
     * @param name name of the attribute to modify.
     * @param value new value of the attribute.
     */
    public void setAttributeValue(Node node, String name, String value) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("name", name); // NOI18N
        params.put("value", value); // NOI18N
        transport.sendCommand(new Command("DOM.setAttributeValue", params)); // NOI18N
    }

    /**
     * Removes the specified attribute.
     * 
     * @param node node whose attribute should be removed.
     * @param name name of the attribute to remove.
     */
    public void removeAttribute(Node node, String name) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("name", name); // NOI18N
        transport.sendCommand(new Command("DOM.removeAttribute", params)); // NOI18N
    }

    /**
     * Returns an outer HTML of the specified node.
     * 
     * @param node node whose outer HTML should be returned.
     * @return outer HTML of the specified node.
     */
    public String getOuterHTML(Node node) {
        String html = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.getOuterHTML", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                html = (String)result.get("outerHTML"); // NOI18N
            }
        }
        return html;
    }

    /**
     * Sets the outer HTML of the specified node.
     * 
     * @param node node whose outer HTML should be returned.
     * @param html new outer HTML of the node.
     */
    public void setOuterHTML(Node node, String html) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        params.put("outerHTML", html); // NOI18N
        transport.sendCommand(new Command("DOM.setOuterHTML", params)); // NOI18N
    }

    /**
     * Requests node that corresponds to the given remote JavaScript node reference.
     *
     * @param remoteObject remote JavaScript reference to the requested node.
     * @return node corresponding to the given remote reference or {@code null}
     * when such node doesn't exit.
     */
    public Node requestNode(RemoteObject remoteObject) {
        Node n = null;
        JSONObject params = new JSONObject();
        params.put("objectId", remoteObject.getObjectID()); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("DOM.requestNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                int nodeId = ((Number)result.get("nodeId")).intValue(); // NOI18N
                synchronized (this) {
                    n = nodes.get(nodeId);
                }
            }
        }
        return n;
    }

    /**
     * Resolves given node into corresponding remote JavaScript node reference.
     * 
     * @param node node to resolve.
     * @param objectGroup group name of the resulting remote reference (can be {@code null}).
     * @return remote reference corresponding to the given node.
     */
    public RemoteObject resolveNode(Node node, String objectGroup) {
        RemoteObject remoteObject = null;
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId()); // NOI18N
        if (objectGroup != null) {
            params.put("objectGroup", objectGroup); // NOI18N
        }
        Response response = transport.sendBlockingCommand(new Command("DOM.resolveNode", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                JSONObject object = (JSONObject)result.get("object"); // NOI18N
                remoteObject = new RemoteObject(object, webKit);
            }
        }
        return remoteObject;
    }

    /**
     * Registers DOM domain listener.
     * 
     * @param listener listener to register.
     */
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters DOM domain listener.
     * 
     * @param listener listener to unregister.
     */
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    /**
     * Resets cached data.
     */
    public synchronized void reset() {
        documentNode = null;
        classForHover = null;
    }

    /** CSS class used to simulate hovering. */
    private String classForHover;

    /**
     * Sets the CSS class that is used to simulate hovering.
     * 
     * @param classForHover class to simulate hovering.
     */
    public void setClassForHover(String classForHover) {
        this.classForHover = classForHover;
    }

    /**
     * Returns the class used to simulate hovering.
     * 
     * @return class to simulate hovering.
     */
    private String getClassForHover() {
        return classForHover;
    }

    /**
     * Remove the class used for simulation of hovering from the {@code class}
     * attribute of the specified node.
     * 
     * @param node node to remove the class from.
     */
    private void removeClassForHover(Node node) {
        Node.Attribute attr = node.getAttribute("class"); // NOI18N
        if (attr != null) {
            String value = attr.getValue();
            String clazz = getClassForHover();
            if (clazz != null && value.contains(clazz)) {
                value = Pattern.compile(Pattern.quote(clazz)).matcher(value).replaceAll("").trim(); // NOI18N
                if (value.isEmpty()) {
                    node.removeAttribute(attr.getName());
                } else {
                    attr.setValue(value);
                }
            }
        }
    }

    /**
     * Notify listeners about {@code childNodesSet} event.
     * 
     * @param parent parent whose children have been set.
     */
    private void notifyChildNodesSet(Node parent) {
        for (Listener listener : listeners) {
            listener.childNodesSet(parent);
        }
    }

    /**
     * Notify listeners about {@code childNodeRemoved} event.
     * 
     * @param parent parent whose child has been removed.
     * @param child child that has been removed.
     */
    private void notifyChildNodeRemoved(Node parent, Node child) {
        for (Listener listener : listeners) {
            listener.childNodeRemoved(parent, child);
        }
    }

    /**
     * Notify listeners about {@code childNodeInserted} event.
     * 
     * @param parent parent whose child has been inserted.
     * @param child child that has been inserted.
     */
    private void notifyChildNodeInserted(Node parent, Node child) {
        for (Listener listener : listeners) {
            listener.childNodeInserted(parent, child);
        }
    }

    /**
     * Notify listeners about {@code documentUpdated} event.
     */
    private void notifyDocumentUpdated() {
        for (Listener listener : listeners) {
            listener.documentUpdated();
        }
    }

    /**
     * Notify listeners about {@code attributeModified} event.
     * 
     * @param node node whose attribute has been modified.
     * @param attrName name of the modified attribute.
     * @param attrValue new value of the attribute.
     */
    private void notifyAttributeModified(Node node, String attrName, String attrValue) {
        for (Listener listener : listeners) {
            listener.attributeModified(node, attrName, attrValue);
        }
    }

    /**
     * Notify listeners about {@code attributeRemoved} event.
     * 
     * @param node node whose attribute has been removed.
     * @param attrName  name of the removed attribute.
     */
    private void notifyAttributeRemoved(Node node, String attrName) {
        for (Listener listener : listeners) {
            listener.attributeRemoved(node, attrName);
        }
    }

    /**
     * Notify listeners about {@code characterDataModified} event.
     * 
     * @param node node whose character data have been modified.
     */
    private void notifyCharacterDataModified(Node node) {
        for (Listener listener : listeners) {
            listener.characterDataModified(node);
        }
    }

    /**
     * Notify listeners about {@code shadowRootPushed} event.
     * 
     * @param host host element.
     * @param shadowRoot new shadow root.
     */
    private void notifyShadowRootPushed(Node host, Node shadowRoot) {
        for (Listener listener : listeners) {
            listener.shadowRootPushed(host, shadowRoot);
        }
    }

    /**
     * Notify listeners about {@code shadowPoppedPushed} event.
     * 
     * @param host host element.
     * @param shadowRoot new shadow root.
     */
    private void notifyShadowRootPopped(Node host, Node shadowRoot) {
        for (Listener listener : listeners) {
            listener.shadowRootPopped(host, shadowRoot);
        }
    }

    void handleSetChildNodes(JSONObject params) {
        Node parent;
        synchronized (this) {
            int parentId = ((Number)params.get("parentId")).intValue(); // NOI18N
            parent = nodes.get(parentId);
            if (parent == null) {
                Logger.getLogger(DOM.class.getName()).log(Level.INFO, "Nodes set to an unknown parent: {0}!", params); // NOI18N
                return;
            }
            JSONArray children = (JSONArray)params.get("nodes"); // NOI18N
            parent.initChildren();
            List<Node> newChildren = new ArrayList<Node>(children.size());
            for (Object child : children) {
                Node node = new Node((JSONObject)child);
                newChildren.add(node);
            }
            parent.addChildren(newChildren);
            updateNodesMap(parent);
        }
        notifyChildNodesSet(parent);
    }

    void handleChildNodeInserted(JSONObject params) {
        Node parent;
        Node child;
        synchronized (this) {
            Number nodeId = (Number)params.get("parentNodeId");
            //TODO: workaround for mobile safari
            if (nodeId==null) {
                nodeId = (Number)params.get("parentId");
            }
            
            int parentId = nodeId.intValue(); // NOI18N
            parent = nodes.get(parentId);
            Number prevId = (Number)params.get("previousNodeId");
            
            //TODO: workaround for mobile safari
            if (prevId == null) {
                prevId = (Number)params.get("prevId");
            }
            int previousNodeId = prevId.intValue(); // NOI18N
            Node previousNode = nodes.get(previousNodeId);
            JSONObject childData = (JSONObject)params.get("node"); // NOI18N
            child = new Node(childData);
            updateNodesMap(child);
            if (parent == null) {
                Logger.getLogger(DOM.class.getName()).log(Level.INFO, "Node inserted into an unknown parent: {0}!", params); // NOI18N
                return;
            }
            parent.insertChild(child, previousNode);
        }
        notifyChildNodeInserted(parent, child);
    }

    void handleChildNodeRemoved(JSONObject params) {
        Node parent;
        Node child;
        synchronized (this) {
            int parentId = ((Number)params.get("parentNodeId")).intValue(); // NOI18N
            parent = nodes.get(parentId);
            int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
            child = nodes.get(nodeId);
            if (parent == null) {
                Logger.getLogger(DOM.class.getName()).log(Level.INFO, "Node removed from an unknown parent: {0}!", params); // NOI18N
                return;
            }
            parent.removeChild(child);
            nodes.remove(nodeId);
        }
        notifyChildNodeRemoved(parent, child);
    }

    void handleDocumentUpdated() {
        synchronized (this) {
            nodes.clear();
            documentNode = null;
            documentCounter++;
        }
        notifyDocumentUpdated();
    }

    void handleAttributeModified(JSONObject params) {
        Node node;
        String name;
        String value;
        synchronized (this) {
            int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
            node = nodes.get(nodeId);
            if (node == null) {
                return;
            }
            name = (String)params.get("name"); // NOI18N
            value = (String)params.get("value"); // NOI18N
            if ("class".equals(name)) { // NOI18N
                String clazz = getClassForHover();
                if (clazz != null && value.contains(clazz)) {
                    value = Pattern.compile(Pattern.quote(clazz)).matcher(value).replaceAll("").trim();
                }
            }
            node.setAttribute(name, value);
        }
        notifyAttributeModified(node, name, value);
    }

    void handleAttributeRemoved(JSONObject params) {
        Node node;
        String name;
        synchronized (this) {
            int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
            node = nodes.get(nodeId);
            if (node == null) {
                return;
            }
            name = (String)params.get("name"); // NOI18N
            node.removeAttribute(name);
        }
        notifyAttributeRemoved(node, name);
    }

    void handleCharacterDataModified(JSONObject params) {
        Node node;
        synchronized (this) {
            int nodeId = ((Number)params.get("nodeId")).intValue(); // NOI18N
            node = nodes.get(nodeId);
            if (node == null) {
                return;
            }
            String characterData = (String)params.get("characterData"); // NOI18N
            node.setNodeValue(characterData);
        }
        notifyCharacterDataModified(node);
    }

    void handleShadowRootPushed(JSONObject params) {
        Node host;
        Node shadowRoot;
        synchronized (this) {
            int hostId = ((Number)params.get("hostId")).intValue(); // NOI18N
            host = nodes.get(hostId);
            if (host == null) {
                return;
            }
            JSONObject root = (JSONObject)params.get("root"); // NOI18N
            shadowRoot = new Node(root);
            host.addShadowRoot(shadowRoot);
            updateNodesMap(shadowRoot);
        }
        notifyShadowRootPushed(host, shadowRoot);
    }

    void handleShadowRootPopped(JSONObject params) {
        Node host;
        Node shadowRoot;
        synchronized (this) {
            int hostId = ((Number)params.get("hostId")).intValue(); // NOI18N
            host = nodes.get(hostId);
            int rootId = ((Number)params.get("rootId")).intValue(); // NOI18N
            shadowRoot = nodes.get(rootId);
            if (host == null || shadowRoot == null) {
                return;
            }
            host.removeShadowRoot(shadowRoot);
            nodes.remove(rootId);
        }
        notifyShadowRootPopped(host, shadowRoot);
    }

    /**
     * DOM domain listener.
     */
    public static interface Listener {
        /**
         * Document has been updated. Old node IDs are no longer valid.
         */
        void documentUpdated();
        
        /**
         * Notification about child nodes of some node. This event is sent
         * at most once. Events that correspond to incremental updates
         * are sent when child nodes are modified after that.
         * 
         * @param parent parent whose nodes has been set.
         */
        void childNodesSet(Node parent);
        
        /**
         * Child node has been removed from the parent, mirrors
         * {@code DOMNodeRemoved} event.
         * 
         * @param parent parent whose child has been removed.
         * @param child child that has been removed.
         */
        void childNodeRemoved(Node parent, Node child);
        
        /**
         * Child node has been inserted into the parent, mirrors
         * {@code DOMNodeInserted} event.
         * 
         * @param parent parent whose child has been inserted.
         * @param child child that has been inserted.
         */
        void childNodeInserted(Node parent, Node child);
        
        /**
         * Attribute has been modified.
         * 
         * @param node node whose attribute has been modified.
         * @param attrName name of the modified attribute.
         * @param attrValue new value of the attribute.
         */
        void attributeModified(Node node, String attrName, String attrValue);
        
        /**
         * Attribute has been removed.
         * 
         * @param node node whose attribute has been removed.
         * @param attrName name of the removed attribute.
         */
        void attributeRemoved(Node node, String attrName);

        /**
         * Character data of node have been modified, mirrors
         * {@code DOMCharacterDataModified} event.
         * 
         * @param node node whose character data have been modified.
         */
        void characterDataModified(Node node);

        /**
         * Shadow root has been pushed into the host element.
         * 
         * @param host host element.
         * @param shadowRoot new shadow root.
         */
        void shadowRootPushed(Node host, Node shadowRoot);

        /**
         * Shadow root has been popped from the host element.
         * 
         * @param host host element
         * @param shadowRoot shadow root that has been removed.
         */
        void shadowRootPopped(Node host, Node shadowRoot);

    }

    /**
     * Callback for DOM domain events.
     */
    class Callback implements ResponseCallback {

        /**
         * Handles DOM domain events.
         * 
         * @param response event description.
         */
        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            JSONObject params = response.getParams();
            if ("DOM.setChildNodes".equals(method)) { // NOI18N
                handleSetChildNodes(params);
            } else if ("DOM.childNodeInserted".equals(method)) { // NOI18N
                handleChildNodeInserted(params);
            } else if ("DOM.childNodeRemoved".equals(method)) { // NOI18N
                handleChildNodeRemoved(params);
            } else if ("DOM.documentUpdated".equals(method)) { // NOI18N
                handleDocumentUpdated();
            } else if ("DOM.attributeModified".equals(method)) { // NOI18N
                handleAttributeModified(params);
            } else if ("DOM.attributeRemoved".equals(method)) { // NOI18N
                handleAttributeRemoved(params);
            } else if ("DOM.characterDataModified".equals(method)) { // NOI18N
                handleCharacterDataModified(params);
            } else if ("DOM.shadowRootPushed".equals(method)) { // NOI18N
                handleShadowRootPushed(params);
            } else if ("DOM.shadowRootPopped".equals(method)) { // NOI18N
                handleShadowRootPopped(params);
            }
        }

    }

}
