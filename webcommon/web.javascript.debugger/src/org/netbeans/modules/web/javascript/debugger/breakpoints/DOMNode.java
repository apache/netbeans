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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.web.webkit.debugging.api.dom.DOM;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node.Attribute;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Representation of a node in a DOM document.
 * It's storable as a String and it's able to adapt to changes in a DOM document.
 * 
 * @author Martin
 */
public final class DOMNode {
    
    public static final String PROP_NODE_CHANGED = "nodeChanged";               // NOI18N
    public static final String PROP_NODE_PATH_FAILED = "nodePathRequestFailed"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(DOMNode.class.getName());
    private static final char ETX = 0x0003;
    private final List<NodeId> path;
    private final String id;
    
    private transient DOM dom;
    private transient Node node;
    private transient List<Node> nodePath;
    private transient DOMListener domListener;
    
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    private DOMNode(List<NodeId> path) {
        this.path = path;
        this.id = null;
    }
    
    private DOMNode(String id) {
        this.path = null;
        this.id = id;
    }
    
    public static DOMNode create(Node node) {
        Attribute id = node.getAttribute("id");
        if (id != null) {
            String idValue = id.getValue();
            DOMNode dn = new DOMNode(idValue);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new DOMNode(#"+idValue+") created from "+node);
            }
            return dn;
        }
        // No id, remember the DOM three path
        List<NodeId> path = new LinkedList<NodeId>();
        Node parent = node.getParent();
        Node origNode = node;
        while (parent != null) {
            List<Node> children = parent.getChildren();
            int childNumber = acceptedIndexOf(children, node);
            String localName = node.getLocalName();
            path.add(0, new NodeId(localName, childNumber));
            node = parent;
            parent = node.getParent();
        }
        path.add(0, new NodeId(node.getLocalName(), -1));
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+origNode);
        }
        return dn;
    }
    
    private static boolean acceptNode(Node node) {
        boolean isElement = (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE);
        return isElement && !node.isInjectedByNetBeans();
    }
    
    private static int acceptedIndexOf(List<Node> list, Node node) {
        int i = 0;
        for (Node n : list) {
            if (acceptNode(n)) {
                if (node.equals(n)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }
    
    private static Node getAcceptedAt(List<Node> list, int i) {
        for (Node n : list) {
            if (acceptNode(n)) {
                if (i == 0) {
                    return n;
                }
                i--;
            }
        }
        return null;
    }
    
    public static DOMNode create(org.openide.nodes.Node node) {
        String idValue = tryGetID(node);
        if (idValue != null) {
            DOMNode dn = new DOMNode(idValue);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new DOMNode("+idValue+") created from "+node);
            }
            return dn;
        }
        List<NodeId> path = new LinkedList<NodeId>();
        org.openide.nodes.Node parent = node.getParentNode();
        org.openide.nodes.Node origNode = node;
        while (parent != null) {
            List<org.openide.nodes.Node> children = Arrays.asList(parent.getChildren().getNodes(true));
            int childNumber = children.indexOf(node);
            String localName = node.getName();
            path.add(0, new NodeId(localName, childNumber));
            node = parent;
            if ("html".equalsIgnoreCase(localName)) {       // NOI18N
                parent = null;
            } else {
                parent = node.getParentNode();
            }
        }
        path.add(0, new NodeId(""/*node.getName()*/, -1));
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+origNode);
        }
        return dn;
    }
    
    private static String tryGetID(org.openide.nodes.Node node) {
        // TODO: Request API to be able to access org.netbeans.modules.html.navigator.Description from the Node's lookup
        // Reading attributes from HTMLElementNode
        try {
            Field sourceField = node.getClass().getDeclaredField("source");
            sourceField.setAccessible(true);
            Object source = sourceField.get(node);
            Method getAttributes = source.getClass().getDeclaredMethod("getAttributes");
            getAttributes.setAccessible(true);
            Object attributesObj = getAttributes.invoke(source);
            Map<String, String> attributes = (Map<String, String>) attributesObj;
            return attributes.get("id");
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Problem while getting id from node "+node+", class = "+node.getClass()+": "+ex.toString());
            return null;
        }
    }
    
    /**
     * A utility method to find node's URL.
     */
    public static URL findURL(Node node) {
        String urlStr = null;
        while (urlStr == null && node != null) {
            urlStr = node.getDocumentURL();
            node = node.getParent();
        }
        if (urlStr != null) {
            try {
                return new URL(urlStr);
            } catch (MalformedURLException ex) {
            }
        }
        return null;
    }
    
    /**
     * @see #getStringDefinition()
     */
    public static DOMNode create(String stringDefinition) {
        if (stringDefinition.charAt(0) != '[') {
            return createFromPathNames(stringDefinition);
            //throw new IllegalArgumentException("Missing opening bracket in '"+stringDefinition+"'");
        }
        if (stringDefinition.charAt(stringDefinition.length() - 1) != ']') {
            throw new IllegalArgumentException("Missing closing bracket in '"+stringDefinition+"'");
        }
        if (stringDefinition.startsWith("[#")) {
            String id = stringDefinition.substring(2, stringDefinition.length() - 1);
            DOMNode dn = new DOMNode(id);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new DOMNode("+id+") created from "+stringDefinition);
            }
            return dn;
        }
        int n = stringDefinition.length() - 1;
        List<NodeId> path = new LinkedList<NodeId>();
        int i1 = 1;
        do {
            int i2 = stringDefinition.indexOf(ETX, i1);
            if (i2 < 0) {
                throw new IllegalArgumentException("Missing end text delimeter in '"+stringDefinition+"' after pos "+i1);
            }
            String name = stringDefinition.substring(i1, i2);
            i1 = i2 + 1;
            i2 = stringDefinition.indexOf(',', i1);
            if (i2 < 0) {
                throw new IllegalArgumentException("Missing comma delimeter in '"+stringDefinition+"' after pos "+i1);
            }
            int childNumber = Integer.parseInt(stringDefinition.substring(i1, i2));
            path.add(new NodeId(name, childNumber));
            i1 = i2 + 1;
        } while (i1 < n);
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+stringDefinition);
        }
        return dn;
    }
    
    /**
     * Create a DOMNode from strings like "html/body/ul[5]/li[2]".
     * @param pathNames Slash-separated tag names with optional children order in brackets.
     * @return 
     */
    private static DOMNode createFromPathNames(String pathNames) {
        if (pathNames.startsWith("#")) {
            String id = pathNames.substring(1);
            DOMNode dn = new DOMNode(id);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new DOMNode("+id+") created from "+pathNames);
            }
            return dn;
        }
        while (pathNames.startsWith("/")) {
            pathNames = pathNames.substring(1);
        }
        List<NodeId> path = new LinkedList<NodeId>();
        int i1 = 0;
        do {
            int i2 = pathNames.indexOf('/', i1);
            if (i2 < 0) {
                i2 = pathNames.length();
            }
            String name;
            int ch = -1;
            int b1 = pathNames.indexOf('[', i1);
            if (b1 > 0) {
                name = pathNames.substring(i1, b1).trim();
                int b2 = pathNames.indexOf(']', b1+1);
                if (b2 > b1) {
                    String chStr = pathNames.substring(b1 + 1, b2);
                    try {
                        ch = Integer.parseInt(chStr);
                    } catch (NumberFormatException nfex) {}
                }
            } else {
                name = pathNames.substring(i1, i2).trim();
            }
            path.add(new NodeId(name, ch));
            i1 = i2 + 1;
        } while (i1 < pathNames.length());
        DOMNode dn = new DOMNode(path);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("new DOMNode("+dn.getNodePathNames()+") created from "+pathNames);
        }
        return dn;
    }
    
    /**
     * Provide serializable string definition of this object.
     */
    public String getStringDefinition() {
        StringBuilder sb = new StringBuilder("[");
        if (id != null) {
            sb.append("#");
            sb.append(id);
        } else {
            synchronized (path) {
                for (NodeId n : path) {
                    sb.append(n.name);
                    sb.append(ETX);
                    sb.append(n.childNumber);
                    sb.append(',');
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }
    
    public String getNodeName() {
        if (id != null) {
            return "#" + id;
        } else {
            return path.get(path.size() - 1).name;
        }
    }
    
    public String getNodePathNames() {
        if (id != null) {
            return "#" + id;
        }
        StringBuilder sb = new StringBuilder();
        for (NodeId ni : path) {
            sb.append(ni.name);
            int cn = ni.childNumber;
            if (0 <= cn) {
                sb.append("[");
                sb.append(Integer.toString(cn));
                sb.append("]");
            }
            sb.append("/");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public List<? extends NodeId> getPath() {
        if (path == null) {
            return null;
        }
        List<NodeId> thePath;
        synchronized (path) {
            thePath = new ArrayList<NodeId>(path);
        }
        return Collections.unmodifiableList(thePath);
    }
    
    public String getID() {
        return id;
    }
    
    /**
     * Bind to the DOM document and update the node as the document changes.
     * @param dom The DOM document
     * @throws PathNotFoundException When the DOM node can not be located.
     */
    public synchronized void bindTo(DOM dom) throws PathNotFoundException {
        if (this.dom != null) {
            throw new IllegalStateException("Still listening on "+this.dom);
        }
        this.dom = dom;
        this.domListener = new DOMListener();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("DOMNode("+getNodePathNames()+") is starting listening on "+dom);
        }
        dom.addListener(domListener);
        createNodePath();
    }
    
    private void createNodePath() throws PathNotFoundException {
        Node node;
        DOM theDOM;
        synchronized (this) {
            if (dom == null) {
                return ;
            }
            theDOM = dom;
            node = dom.getDocument();
        }
        if (node == null) {
            if (path != null) {
                throw new PathNotFoundException(path.get(0).name, 0, getNodePathNames());
            } else {
                throw new PathNotFoundException(id, 0, getNodePathNames());
            }
        }
        if (id != null) {
            node = theDOM.querySelector(node, "#" + id);
            if (node == null) {
                throw new PathNotFoundException(id, 0, getNodePathNames());
            } else {
                this.node = node;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("createNodePath() succesfully set node = "+node+" for ID = "+id);
                }
                return ;
            }
        }
        int n = path.size();
        nodePath = new ArrayList<Node>(n);
        this.node = null;
        Node parent = null;
        for (int i = 0; i < n; i++) {
            NodeId ni = path.get(i);
            if (parent == null) {
                if (!ni.name.equals(node.getLocalName())) {
                    if (node.getLocalName().isEmpty()) { // an empty root
                        nodePath.add(node);
                        parent = node;
                        i--;
                        continue;
                    }
                    throw new PathNotFoundException(ni.name, i, getNodePathNames());
                }
                nodePath.add(node);
                parent = node;
            } else {
                List<Node> children = parent.getChildren();
                if (children == null) {
                    dom.requestChildNodes(parent.getNodeId());
                    // Have to wait until we're notified that the children were set.
                    throw new PathNotFoundException(ni.name, i, getNodePathNames(), true);
                }
                Node chn = null;
                int c = ni.childNumber;
                int nc = children.size();
                if (0 <= c && c < nc) {
                    chn = getAcceptedAt(children, c);
                    if (chn != null && !ni.name.equals(chn.getLocalName())) {
                        chn = null;
                    }
                } else if (c < 0) { // Accept also children when child number is undefined.
                    for (Node nn : children) {
                        if (acceptNode(nn) && ni.name.equals(nn.getLocalName())) {
                            chn = nn;
                            break;
                        }
                    }
                    
                }
                /* Uncomment this if we should check sibling elements
                if (chn == null) {
                    if (c >= nc) {
                        c = nc - 1;
                    }
                    if (c < 0) {
                        c = 0;
                    }
                    for (int ci = c; ci < nc; ci++) {
                        if (ni.name.equals(children.get(ci).getLocalName())) {
                            chn = children.get(ci);
                            break;
                        }
                    }
                    if (chn == null) {
                        for (int ci = 0; ci < c; ci++) {
                            if (ni.name.equals(children.get(ci).getLocalName())) {
                                chn = children.get(ci);
                                break;
                            }
                        }
                    }
                }
                */
                if (chn == null) {
                    throw new PathNotFoundException(ni.name, i, getNodePathNames());
                }
                node = chn;
                nodePath.add(node);
                parent = node;
            }
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("createNodePath() succesfully set nodePath = "+nodePath+" and node = "+node);
        }
        this.node = node;
    }
    
    /**
     * Unbind from the DOM document.
     */
    public synchronized void unbind() {
        if (dom == null) {
            return;
        }
        if (domListener != null) {
            dom.removeListener(domListener);
            domListener = null;
        }
        dom = null;
        node = null;
        nodePath = null;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("DOMNode("+getNodePathNames()+") has stopped listening on DOM changes.");
        }
    }
    
    /**
     * Get the node from the DOM document.
     * @return the node, if found in the document. <code>null</code> otherwise,
     * or when not bound to a DOM document.
     */
    public synchronized Node getNode() {
        return node;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        pchs.addPropertyChangeListener(pl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        pchs.removePropertyChangeListener(pl);
    }
    
    private void fireNodeChanged(Node oldNode, Node newNode) {
        pchs.firePropertyChange(PROP_NODE_CHANGED, oldNode, newNode);
    }
    
    private void fireNodePathFailed(PathNotFoundException pex) {
        pchs.firePropertyChange(PROP_NODE_PATH_FAILED, null, pex);
    }
    
    // Belongs to DOMListener only
    private enum NOTIFY_TYPE {
        DOC_UPDATE,
        CHILDREN_SET,
        CHILD_REMOVED,
        CHILD_INSERTED,
    }

    private class DOMListener implements DOM.Listener {
        
        private RequestProcessor RP = new RequestProcessor(DOMListener.class.getName());
        
        @Override
        public void documentUpdated() {
            LOG.fine("DOM documentUpdated()");
            RP.post(new Notify(NOTIFY_TYPE.DOC_UPDATE));
        }
        
        private void documentUpdatedNotify() {
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = node;
                node = null;
                try {
                    createNodePath();
                } catch (PathNotFoundException pex) {
                    pnfex = pex;
                }
                newNode = node;
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
                return;
            }
            if (newNode != null) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodesSet(Node parent) {
            LOG.fine("DOM childNodesSet("+parent+" ["+parent.getNodeName()+":"+parent.getNodeValue()+"])");
            RP.post(new Notify(NOTIFY_TYPE.CHILDREN_SET, parent));
        }
        
        private void childNodesSetNotify(Node parent) {
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node == null || id != null) {
                    // In process of searching for the node...
                    try {
                        createNodePath();
                    } catch (PathNotFoundException pex) {
                        pnfex = pex;
                    }
                    newNode = node;
                } else {
                    int ns = nodePath.size() - 1; // We can ignore the set of children to the last node
                    for (int i = 0; i < ns; i++) {
                        if (parent.equals(nodePath.get(i))) {
                            try {
                                createNodePath();
                            } catch (PathNotFoundException pex) {
                                pnfex = pex;
                            }
                            newNode = node;
                            break;
                        }
                    }
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodeRemoved(Node parent, Node child) {
            LOG.fine("DOM childNodesRemoved("+parent+", "+child+")");
            if (id != null) {
                return ;
            }
            RP.post(new Notify(NOTIFY_TYPE.CHILD_REMOVED, parent, child));
        }
        
        private void childNodeRemovedNotify(Node parent, Node child) {
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node != null) {
                    int ns = nodePath.size();
                    for (int i = 0; i < ns; i++) {
                        if (child.equals(nodePath.get(i))) {
                            try {
                                createNodePath();
                            } catch (PathNotFoundException pex) {
                                pnfex = pex;
                            }
                            newNode = node;
                            break;
                        }
                    }
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void childNodeInserted(Node parent, Node child) {
            LOG.fine("DOM childNodesInserted("+parent+", "+child+" ["+child.getNodeName()+":"+child.getNodeValue()+"])");
            if (id != null) {
                return ;
            }
            RP.post(new Notify(NOTIFY_TYPE.CHILD_INSERTED, parent, child));
        }
        
        private void childNodeInsertedNotify(Node parent, Node child) {
            PathNotFoundException pnfex = null;
            Node oldNode;
            Node newNode;
            synchronized(DOMNode.this) {
                oldNode = newNode = node;
                if (node == null) {
                    // In process of searching for the node...
                    try {
                        createNodePath();
                    } catch (PathNotFoundException pex) {
                        pnfex = pex;
                    }
                    newNode = node;
                }
            }
            if (pnfex != null) {
                fireNodePathFailed(pnfex);
            }
            if (oldNode != newNode) {
                fireNodeChanged(oldNode, newNode);
            }
        }

        @Override
        public void attributeModified(Node node, String attrName, String attrValue) {
            // Ignored
        }

        @Override
        public void attributeRemoved(Node node, String attrName) {
            // Ignored
        }

        @Override
        public void characterDataModified(Node node) {
            // Ignored
        }

        @Override
        public void shadowRootPushed(Node host, Node shadowRoot) {
        }

        @Override
        public void shadowRootPopped(Node host, Node shadowRoot) {
        }
        
        private class Notify implements Runnable {
            
            private final NOTIFY_TYPE type;
            private final Node[] args;
            
            Notify(NOTIFY_TYPE type, Node... args) {
                this.type = type;
                this.args = args;
            }

            @Override
            public void run() {
                switch(type) {
                    case DOC_UPDATE:
                        documentUpdatedNotify();
                        break;
                    case CHILDREN_SET:
                        childNodesSetNotify(args[0]);
                        break;
                    case CHILD_INSERTED:
                        childNodeInsertedNotify(args[0], args[1]);
                        break;
                    case CHILD_REMOVED:
                        childNodeRemovedNotify(args[0], args[1]);
                        break;
                }
            }
            
        }
        
    }
    
    public static final class NodeId {
        private String name;
        private int childNumber;
        
        private NodeId(String name, int childNumber) {
            this.name = name;
            this.childNumber = childNumber;
        }
        
        public String getName() {
            return name;
        }
        
        public int getChildNumber() {
            return childNumber;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NodeId) {
                NodeId other = (NodeId) obj;
                return name.equals(other.name) && childNumber == other.childNumber;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return childNumber + (name.hashCode() >> 4);
        }

        @Override
        public String toString() {
            return name+"<"+childNumber+">";
        }
        
    }
    
    public static class PathNotFoundException extends Exception {
        
        private String nodeName;
        private int pathPos;
        private String nodePathNames;
        private boolean childrenRequested;
        
        private PathNotFoundException(String nodeName, int pathPos, String nodePathNames) {
            this(nodeName, pathPos, nodePathNames, false);
        }
        
        private PathNotFoundException(String nodeName, int pathPos, String nodePathNames, boolean childrenRequested) {
            super(nodeName+" being "+pathPos+" in "+nodePathNames);
            this.nodeName = nodeName;
            this.pathPos = pathPos;
            this.nodePathNames = nodePathNames;
            this.childrenRequested = childrenRequested;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("new PathNotFoundException("+getMessage()+"), childrenRequested = "+childrenRequested);
            }
        }

        /**
         * Name of the node that was not found.
         */
        public String getNodeName() {
            return nodeName;
        }

        /**
         * The position of the node that was not found in the full path.
         */
        public int getPathPosition() {
            return pathPos;
        }

        /**
         * The full node path.
         */
        public String getNodePathNames() {
            return nodePathNames;
        }
        
        /**
         * Test if further children are requested.
         * @return <code>true</code> when a request was sent to retrieve children,
         * <code>false</code> otherwise.
         */
        public boolean isChildrenRequested() {
            return childrenRequested;
        }
    }
}
