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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * {@code DOM.Node} in WebKit Remote Debugging Protocol.
 *
 * @author Jan Stola
 */
public class Node {
    /** Properties of the node. */
    private final JSONObject properties;
    /** Children of the node ({@code nul} when the children are not known yet). */
    private List<Node> children;
    /** Shadow roots of the node. */
    private List<Node> shadowRoots;
    /** Attributes of the node. */
    private List<Attribute> attributes;
    /** Content document (for {@code Frame} nodes). */
    private Node contentDocument;
    /** Parent node. */
    private Node parent;

    /**
     * Creates a new {@code Node} that corresponds to the given JSONObject.
     * 
     * @param node JSONObject describing the node.
     */
    Node(JSONObject node) {
        this.properties = node;

        // Children
        Object childrenValue = node.get("children"); // NOI18N
        if (childrenValue != null) {
            initChildren();
            JSONArray childrenArray = (JSONArray)childrenValue;
            List<Node> newChildren = new ArrayList<Node>(childrenArray.size());
            for (Object child : childrenArray) {
                newChildren.add(new Node((JSONObject)child));
            }
            addChildren(newChildren);
        }

        Object shadowRootsValue = node.get("shadowRoots"); // NOI18N
        if (shadowRootsValue instanceof JSONArray) {
            JSONArray shadowRootArray = (JSONArray)shadowRootsValue;
            for (Object shadowRoot : shadowRootArray) {
                addShadowRoot(new Node((JSONObject)shadowRoot));
            }
        }

        // Attributes
        JSONArray array = (JSONArray)getProperties().get("attributes"); // NOI18N
        if (array != null) {
            for (int i=0; i<array.size()/2; i++) {
                String name = (String)array.get(2*i);
                String value = (String)array.get(2*i+1);
                setAttribute(name, value);
            }
        }

        // Content document
        JSONObject document = (JSONObject)getProperties().get("contentDocument"); // NOI18N
        if (document != null) {
            contentDocument = new Node(document);
            contentDocument.parent = this;
            // A node cannot have both children and content document.
            // FRAME doesn't support children at all and children
            // of IFRAME are interpreted when frames are not supported
            // only (i.e. when the content document is null)
            // => no need to ask for children of a node with a content
            // document. We can set children to empty collection immediately.
            initChildren();
        }

        // Cleanup
        node.remove("children"); // NOI18N
        node.remove("attributes"); // NOI18N
        node.remove("contentDocument"); // NOI18N
    }

    /**
     * Returns properties of the node.
     * 
     * @return properties of the node.
     */
    private JSONObject getProperties() {
        return properties;
    }

    /**
     * Returns ID of this node.
     * 
     * @return ID of this node.
     */
    public int getNodeId() {
        Number nodeId = (Number)getProperties().get("nodeId"); // NOI18N
        // workaround for mobile safari
        if (nodeId == null) {
            nodeId = (Number)getProperties().get("id"); // NOI18N
        }
        return nodeId.intValue();
    }

    /**
     * Returns type of this node.
     * 
     * @return type of this node.
     */
    public int getNodeType() {
        return ((Number)getProperties().get("nodeType")).intValue(); // NOI18N
        }

    /**
     * Returns node name.
     * 
     * @return node name.
     */
    public String getNodeName() {
        return (String)getProperties().get("nodeName"); // NOI18N
    }

    /**
     * Returns local name.
     * 
     * @return local name.
     */
    public String getLocalName() {
        return (String)getProperties().get("localName"); // NOI18N
    }

    /**
     * Returns node value.
     * 
     * @return node value.
     */
    public synchronized String getNodeValue() {
        return (String)getProperties().get("nodeValue"); // NOI18N
    }

    /**
     * Sets node value.
     * 
     * @param value new node value.
     */
    synchronized void setNodeValue(String value) {
        getProperties().put("nodeValue", value); // NOI18N
    }

    /**
     * Initializes {@code children} field.
     */
    final synchronized void initChildren() {
        children = new CopyOnWriteArrayList<Node>();
        getProperties().remove("childNodeCount"); // NOI18N
    }

    /**
     * Returns sub-nodes of this node. This method returns an empty list
     * if there are no sub-nodes but it can return {@code null} when
     * the sub-nodes are not known yet. If that happens then you may
     * request the sub-nodes using {@code DOM.requestChildNodes()} method.
     * 
     * @return sub-nodes of this node or {@code null} when the sub-nodes
     * are not known yet.
     */
    public synchronized List<Node> getChildren() {
        return children;
    }

    /**
     * Returns number of sub-nodes of this node.
     * 
     * @return number of sub-nodes of this node.
     */
    public synchronized int getChildrenCount() {
        int count = -1;
        if (children == null) {
            Object nodeCount = getProperties().get("childNodeCount"); // NOI18N
            if (nodeCount instanceof Number) {
                count = ((Number)nodeCount).intValue();
            }
        } else {
            count = children.size();
        }
        return count;
    }

    /**
     * Adds children to this node.
     * 
     * @param newChildren new children to add.
     */
    synchronized final void addChildren(List<Node> newChildren) {
        if (children == null) {
            initChildren();
        }
        children.addAll(newChildren);
        for (Node child : newChildren) {
            child.parent = this;
        }
    }

    /**
     * Removes child from this node.
     * 
     * @param child child to remove.
     */
    synchronized final void removeChild(Node child) {
        children.remove(child);
        child.parent = null;
    }

    /**
     * Inserts child into this node.
     * 
     * @param child child to insert.
     * @param previousChild previous child ({@code null} when the new child
     * should be the first one).
     */
    synchronized final void insertChild(Node child, Node previousChild) {
        if (children == null) {
            initChildren();
        }
        int index;
        if (previousChild == null) {
            index = 0;
        } else {
            index = children.indexOf(previousChild)+1;
        }
        children.add(index, child);
        child.parent = this;
    }

    /**
     * Returns shadow roots of this node.
     * 
     * @return shadow roots of this node.
     */
    public final synchronized List<Node> getShadowRoots() {
        return (shadowRoots == null) ? Collections.emptyList() : shadowRoots;
    }

    /**
     * Adds a new shadow root to this node.
     * 
     * @param shadowRoot shadow root to add.
     */
    synchronized final void addShadowRoot(Node shadowRoot) {
        if (shadowRoots == null) {
            shadowRoots = new LinkedList<Node>();
        }
        shadowRoot.parent = this;
        shadowRoots.add(shadowRoot);
    }

    /**
     * Removes a shadow root from this node.
     * 
     * @param shadowRoot shadow root to remove.
     */
    synchronized final void removeShadowRoot(Node shadowRoot) {
        shadowRoots.remove(shadowRoot);
    }

    /**
     * Sets an attribute.
     * 
     * @param name name of the attribute to set.
     * @param value new value of the attribute.
     */
    final synchronized void setAttribute(String name, String value) {
        Attribute attribute = getAttribute(name);
        if (attribute == null) {
            attribute = new Attribute(name, value);
            if (attributes == null) {
                attributes = new ArrayList<Attribute>();
            }
            attributes.add(attribute);
        } else {
            attribute.setValue(value);
        }
    }

    /**
     * Removes an attribute.
     * 
     * @param name name of the attribute to remove.
     */
    final synchronized void removeAttribute(String name) {
        Attribute attribute = getAttribute(name);
        if (attribute != null) {
            attributes.remove(attribute);
        }
    }

    /**
     * Returns the attribute with the specified name.
     * 
     * @param name name of the attribute.
     * @return attribute with the specified name or {@code null} when there
     * is no such attribute.
     */
    public synchronized Attribute getAttribute(String name) {
        Attribute result = null;
        for (Attribute attr : getAttributes()) {
            if (name.equals(attr.getName())) {
                result = attr;
                break;
            }
        }
        return result;
    }

    /**
     * Returns all attributes of this node.
     * 
     * @return attributes of this node ({@code null} is never returned,
     * an empty list is returned when there are no attributes).
     */
    public synchronized List<Attribute> getAttributes() {
        return (attributes == null) ? Collections.emptyList() : attributes;
    }

    /**
     * Returns URL of the document.
     * 
     * @return URL of the document the node points to (for {@code Document}
     * and {@code FrameOwner} nodes) or {@code null} (otherwise).
     */
    public String getDocumentURL() {
        return (String)getProperties().get("documentURL"); // NOI18N
    }

    /**
     * Returns Public ID.
     * 
     * @return public ID (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getPublicId() {
        return (String)getProperties().get("publicId"); // NOI18N
    }

    /**
     * Returns System ID.
     * 
     * @return system ID (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getSystemId() {
        return (String)getProperties().get("systemId"); // NOI18N
    }

    /**
     * Returns internal subset.
     * 
     * @return internal subset (for {@code DocumentType} nodes)
     * or {@code null} (otherwise).
     */
    public String getInternalSubset() {
        return (String)getProperties().get("internalSubset"); // NOI18N
    }

    /**
     * Returns XML version.
     * 
     * @return XML version (for {@code Document} nodes of XML documents)
     * or {@code null} (otherwise).
     */
    public String getXmlVersion() {
        return (String)getProperties().get("xmlVersion"); // NOI18N
    }

    public String getName() {
        return (String)getProperties().get("name"); // NOI18N
    }

    public String getValue() {
        return (String)getProperties().get("value"); // NOI18N
    }

    /**
     * Determines whether this node was injected by NetBeans or whether
     * it is part of the original document.
     * 
     * @return {@code true} when the node was injected by NetBeans,
     * returns {@code false} when the node is part of the original document.
     */
    public boolean isInjectedByNetBeans() {
        return getAttribute(":netbeans_generated") != null; // NOI18N
    }

    /**
     * Returns a content document.
     * 
     * @return content document (for {@code FrameOwner} nodes)
     * or {@code null} (otherwise).
     */
    public Node getContentDocument() {
        return contentDocument;
    }

    /**
     * Returns the parent of this node.
     *
     * @return parent of this node.
     */
    public Node getParent() {
        return parent;
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if (obj instanceof Node) {
            int nodeId = ((Node)obj).getNodeId();
            equal = (nodeId == getNodeId());
        }
        return equal;
    }

    @Override
    public int hashCode() {
        return getNodeId();
    }

    /**
     * Element's attribute.
     */
    public static class Attribute {
        /** Name of this attribute. */
        private final String name;
        /** Value of this attribute. */
        private String value;

        /**
         * Creates a new {@code Attribute}.
         * 
         * @param name name of the attribute.
         * @param value value of the attribute.
         */
        Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }

        /**
         * Returns name of this attribute.
         * 
         * @return name of this attribute.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns value of this attribute.
         * 
         * @return value of this attribute.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets value of this attribute.
         * 
         * @param value new value of this attribute.
         */
        void setValue(String value) {
            this.value = value;
        }
    }

}
