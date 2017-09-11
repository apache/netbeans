/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.xdm.nodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
//import org.netbeans.modules.xml.spi.dom.NamedNodeMapImpl;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.w3c.dom.*;

/**
 * This class provides base implementation of Node Interface.
 * @author Ajit
 */
public abstract class NodeImpl implements Node, Cloneable {
    
    public static final String XMLNS = "xmlns"; // NOI18N
    
    /* flag indicating if in tree */
    private boolean inTree;
    
    /* The model to which the node belongs */
    private XDMModel model;
    
    /* id of this node */
    private int id;
    
    /* tokens */
    private List<Token> tokens;
    
    /* child nodes */
    private List<Node> children;
    
    /* attributes */
    private List<Attribute> attributes = null;

    /** Creates a new instance of BaseNode. sets id during creation */
    NodeImpl() {
        model = null;
        inTree = false;
        id = -1;
    }
    
    /**
     * Returns the id of this node
     * @return id - the id of this node
     */
    public final int getId() {
        return id;
    }
    
    /**
     * sets the id of this node
     * @param id - the id of this node
     */
    private void setId(int nodeId) {
        id = nodeId;
    }
    
    @Override
            public int hashCode() {
        return (int) getId();
    }
	
    /**
     * Determines if the node is any tree
     * @return Returns true is in tree, false otherwise
     */
    public final boolean isInTree() {
        return inTree && getModel()!=null;
    }
    
    /**
     * Marks the node and all its children added to a tree.
     */
    public void addedToTree(XDMModel model) {
        if (!isInTree()) {
            inTree = true;
            if(getModel() != model) {
                setModel(model);
                setId(model.getNextNodeId());
            } else {
                if(getId() == -1)
                    setId(model.getNextNodeId());
            }
            for (Node n: getChildren()) {
                n.addedToTree(model);
            }
            for (Node n: getAttributesForRead()) {
                n.addedToTree(model);
            }
        }
    }
    
    private static interface UniqueId {
        int nextId();
    }
    
    private UniqueId createUniqueId() {
        return new UniqueId() {
            private int lastId = -1;
            public int nextId() {
                return ++lastId;
            }
        };
    }
    
    /**
     * Recursively assigns node id's.
     */
    public void assignNodeIdRecursively() {
        assignNodeId(createUniqueId());
    }
    
    void assignNodeId(UniqueId id) {
        assert ! isInTree();
        setId(id.nextId());
        for (Node n: getChildren()) {
            ((NodeImpl)n).assignNodeId(id);
        }
        for (Node n: getAttributesForRead()) {
            ((NodeImpl)n).assignNodeId(id);
        }
    }
    
    public void assignNodeId(int id) {
        assert ! isInTree();
        setId(id);
        for (Node n: getChildren()) {
            ((NodeImpl)n).assignNodeId(id);
        }
        for (Node n: getAttributesForRead()) {
            ((NodeImpl)n).assignNodeId(id);
        }
    }
    
    protected XDMModel getModel() {
        return model;
    }
    
    private void setModel(XDMModel xdmModel) {
        assert xdmModel != null;
        model = xdmModel;
    }
    
    /**
     * @return true the passed node has same id and belongs to same model.
     * @param node Node to compare
     */
    public boolean isEquivalentNode(Node node){
        return (this==node) || getClass().isInstance(node) &&
                getModel()!=null && getModel()==((NodeImpl)node).getModel() &&
                getId() != -1  && getId()==node.getId();
    }
    
    /**
     * Validation whether a node is in a tree
     * @throws IllegalStateException if a node has already been added to a tree.
     */
    final void checkNotInTree() {
        if (isInTree()) {
            throw new IllegalStateException("mutations cannot occur on nodes already added to a tree");
        }
    }

    // DOM Node impl
    public boolean isSupported(String feature, String version) {
        return "1.0".equals(version);
    }
    
    /**
     * This api clones the node object and returns the clone. A node object has
     * content, attributes and children. The api will allow or disallow
     * modification of this underlying data based on the input.
     * @param cloneContent If true the content of clone can be modified.
     * @param cloneAttributes If true the attributes of the clone can be modified.
     * @param cloneChildren If true the children of the clone can be modified.
     * @return returns the clone of this node
     */
    public Node clone(boolean cloneContent, boolean cloneAttributes, boolean cloneChildren) {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
            if(cloneContent) {
                clone.setTokens(new ArrayList<Token>(getTokens()));
            } else {
                clone.setTokens(getTokens());
            }
            if(cloneAttributes) {
                clone.setAttributes(new ArrayList<Attribute>(getAttributesForRead()));
            } else {
                clone.setAttributes(getAttributesForRead());
            }
            if(cloneChildren) {
                clone.setChildren(new ArrayList<Node>(getChildren()));
            } else {
                clone.setChildren(getChildren());
            }
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }
    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy constructor for nodes.
     * @param deep - If true, recursively clone the subtree under the specified node;
     *               if false, clone only the node itself
     * @return the clone
     */
    public Node cloneNode(boolean deep) {
        return cloneNode(deep, true);
    }
    
    public Node cloneNode(boolean deep, boolean cloneNamespacePrefix) {
        Document root = isInTree() ? (Document) getOwnerDocument() : null;
        Map<Integer,String> allNamespaces = null;
        if (cloneNamespacePrefix && root != null) {
            allNamespaces = root.getNamespaceMap();
        }
        Map<String,String> clonePrefixes = new HashMap<String,String>();
        return cloneNode(deep, allNamespaces, clonePrefixes);
    }
    
    public Node cloneNode(boolean deep, Map<Integer,String> allNS, Map<String,String> clonePrefixes) {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
            clone.model = null;
            clone.setTokens(new ArrayList<Token>(getTokens()));
            if(deep) {
                List<Node> cloneChildren = new ArrayList<Node>(getChildren().size());
                for (Node c : getChildren()) {
                    NodeImpl child = (NodeImpl) c;
                    NodeImpl cloneChild = (NodeImpl)child.cloneNode(deep, allNS, clonePrefixes);
                    cloneChildren.add(cloneChild);
                }
                clone.setChildren(cloneChildren);
            }

            //whether deep or not, attributes are always cloned
            List<Attribute> cloneAttributes = new ArrayList<Attribute>(getAttributesForRead().size());
            for (Attribute attribute:getAttributesForRead()) {
                cloneAttributes.add((Attribute)attribute.cloneNode(deep, allNS, clonePrefixes));
            }
            clone.setAttributes(cloneAttributes);
            
            cloneNamespacePrefixes(allNS, clonePrefixes);
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }
    
    protected void cloneNamespacePrefixes(Map<Integer,String> allNS, Map<String,String> prefixes) {
        if (allNS == null) return;
        
        String namespace = allNS.get(getId());
        if (namespace != null) {
            String prefix = getPrefix();
            if (prefix != null) {
                prefixes.put(prefix, namespace);
            } else {
                prefixes.put(XMLConstants.DEFAULT_NS_PREFIX, namespace);
            }
        }
    }
    
    public Node cloneShallowWithModelContext() {
        try {
            NodeImpl clone = (NodeImpl)super.clone();
            clone.inTree = false;
            clone.setTokens(new ArrayList<Token>(getTokens()));
            if(hasChildNodes()) clone.setChildren(new ArrayList<Node>(getChildren()));
            if(hasAttributes()) clone.setAttributes(new ArrayList<Attribute>(getAttributesForRead()));
            return clone;
        } catch (CloneNotSupportedException cne) {
            throw new RuntimeException(cne);
        }
    }
    
    /**
     * Returns whether this node has any children.
     * @return Returns true if this node has any children, false otherwise.
     */
    public boolean hasChildNodes() {
        return !getChildren().isEmpty();
    }
    
    /**
     * A NodeList that contains all children of this node.
     * @return Returns nodelist containing children
     */
    public NodeList getChildNodes() {
        if (!hasChildNodes()) return NodeListImpl.EMPTY;
        return new NodeListImpl(getChildren());
    }
    
    /**
     * The first child of this node. If there is no such node, this returns null.
     * @return first child
     */
    public Node getFirstChild() {
        if (!hasChildNodes()) return null;
        return getChildren().get(0);
    }
    
    /**
     * The last child of this node. If there is no such node, this returns null.
     * @return last child
     */
    public Node getLastChild() {
        if (!hasChildNodes()) return null;
        return getChildren().get(getChildren().size()-1);
    }
    
    public int getIndexOfChild(Node n) {
        if (n == null) return -1;
        List<Node> childs = getChildren();
        for (int i = 0; i < childs.size(); i++) {
            if (childs.get(i) == n ||
                    (childs.get(i).getId() == n.getId() &&
                        n.getId() != -1)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Adds the node newChild to the end of the list of children of this node.
     * Since the model is immutable checks if current node and node being added
     * are not already in tree.
     * @param newChild - The node to add.
     * @return The node added.
     */
    public Node appendChild(org.w3c.dom.Node node) {
        checkNotInTree();
        if(node instanceof Node) {
            NodeImpl nodeImpl = (NodeImpl) node;
            nodeImpl.checkNotInTree();
            getChildrenForWrite().add(nodeImpl);
            return nodeImpl;
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,node.getClass().getName());
        }
    }
    
    /**
     * Repalces the node oldNode with newNode.
     * Since the model is immutable checks if current node
     * and the node being put, are not already in tree.
     * @param newChild - The new node to put in the child list.
     * @param oldChild - The node being replaced in the list.
     * @return The node replaced.
     */
    public Node replaceChild(org.w3c.dom.Node newNode, org.w3c.dom.Node oldNode) {
        checkNotInTree();
        if(newNode instanceof Node && oldNode instanceof Node) {
            NodeImpl newNodeImpl = (NodeImpl) newNode;
            NodeImpl oldNodeImpl = (NodeImpl) oldNode;
            newNodeImpl.checkNotInTree();
            int oldIndex = getIndexOfChild(oldNodeImpl);
            if(oldIndex!=-1) {
                return getChildrenForWrite().set(oldIndex, newNodeImpl);
            } else {
                throw new DOMException(DOMException.NOT_FOUND_ERR,null);
            }
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Moves child node to new position.
     * @param child - The node being reordered in the list.
     * @return The node moved.
     */
    public Node reorderChild(org.w3c.dom.Node child, int index) {
        checkNotInTree();
        if (child instanceof Node) {
            NodeImpl n = (NodeImpl) child;
            if (! n.isInTree()) {
                throw new IllegalArgumentException("Node is not in tree");
            }
            int currentIndex = getIndexOfChild(n);
            if (index == currentIndex) {
                return n;
            }
            if (! getChildrenForWrite().remove(n)) {
                throw new IllegalArgumentException("Node is not in children");
            }
            index = index > currentIndex ? index-1 : index;
            getChildrenForWrite().add(index, n);
            return n;
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }

    /**
     * Rearranges children list to the given permutaion.
     * @param permutation integer array with index represents current index and 
     * value is final index after reordered.
     */
    public void reorderChildren(int[] permutation) {
        checkNotInTree();

        List<Node> copy = new ArrayList<Node>(getChildren());
        if (permutation.length != copy.size()) {
            throw new IllegalArgumentException(
                "Permutation length: "+permutation.length+" " +
                "is different than children size: "+copy.size());
        }
        List writableChildren = getChildrenForWrite();
        for (int i = 0; i < copy.size(); i++ ) {
            Node child = copy.get(i);
            writableChildren.set(permutation[i], child);
        }
    }

    /**
     * Removes the node from children list.
     * Since the model is immutable checks if current node is not already in tree.
     * @param node - The node being removed from the list.
     * @return The node removed.
     */
    public Node removeChild(org.w3c.dom.Node node) {
        checkNotInTree();
        if(node instanceof Attribute) {
            if(getAttributesForWrite().remove(node)) {
                return (Node) node;
            }
        } else if(node instanceof Node) {
            if(getChildrenForWrite().remove(node)) {
                return (Node)node;
            }
        } 
        throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
    }
    
    /**
     * Inserts the node newChild before the existing child node refChild.
     * If refChild is null, insert newChild at the end of the list of children.
     * Since the model is immutable checks if current node
     * and node being inserted are not already in tree.
     * @param newChild - The node to insert.
     * @param refChild - The reference node, i.e., the node before which the new node must be inserted.
     * @return The node being inserted.
     */
    public Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException {
        if(refChild == null)
            return appendChild(newChild);
        checkNotInTree();
        if(newChild instanceof Node && refChild instanceof Node) {
            NodeImpl newChildImpl = (NodeImpl) newChild;
            newChildImpl.checkNotInTree();
            int index = getIndexOfChild((NodeImpl)refChild);
            if(index <0)
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            getChildrenForWrite().add(index,newChildImpl);
            return newChildImpl;
        } else {
            throw new DOMException(DOMException.TYPE_MISMATCH_ERR,null);
        }
    }
    
    /**
     * Returns whether this node has any attributes.
     * @return Returns true if this node has any attributes, false otherwise.
     */
    public boolean hasAttributes() {
        return !getAttributesForRead().isEmpty();
    }
    
    /**
     * A NamedNodeMap that contains all attributes of this node.
     * @return Returns NamedNodeMap containing attributes
     */
    public NamedNodeMap getAttributes() {
        if(attributes == null || attributes.isEmpty()) return NamedNodeMapImpl.EMPTY;
        return new NamedNodeMapImpl(attributes);
    }
    
    /**
     * The Document object associated with this node.
     * @return the document object
     */
    public org.w3c.dom.Document getOwnerDocument() {
        return getModel().getDocument();
    }
    
    public Node getParentNode() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        return path.get(1);
    }
    
    public Node getNextSibling() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        NodeImpl parent = (NodeImpl)path.get(1);
        NodeImpl node = (NodeImpl)path.get(0);
        int nextIndex = parent.getIndexOfChild(node)+1;
        if(nextIndex>=parent.getChildren().size()) return null;
        return parent.getChildren().get(nextIndex);
    }
    
    public Node getPreviousSibling() {
        if (!isInTree()) return null;
        PathFromRootVisitor pfrv = new PathFromRootVisitor();
        List<Node> path = pfrv.findPath(getModel().getDocument(),this);
        if(path == null || path.size()<2) return null;
        NodeImpl parent = (NodeImpl)path.get(1);
        NodeImpl node = (NodeImpl)path.get(0);
        int prevIndex = parent.getIndexOfChild(node)-1;
        if(prevIndex<0) return null;
        return parent.getChildren().get(prevIndex);
    }
    
    /*
     * A code representing the type of the underlying object
     * abstract and to be implemented in subclasses
     */
    public abstract short getNodeType();
    
    /*
     * The name of this node, depending on its type
     * abstract and to be implemented in subclasses
     */
    public abstract String getNodeName();
    
    public String getNodeValue() throws DOMException {
        return null;
    }
    
    public void setNodeValue(String str) throws DOMException {
    }
    
    public String getLocalName() {
        return null;
    }
    
    public String getNamespaceURI(Document document) {
        assert document != null;
        return document.getNamespaceURI(this);
    }
    
    public String getNamespaceURI() {
        String namespace = lookupNamespaceLocally(getPrefix());
        if (namespace != null) return namespace;
        if (isInTree()) {
            return getModel().getDocument().getNamespaceURI(this);
        } else {
            return lookupNamespaceURI(getPrefix());
        }
    }
    
    public String lookupNamespaceURI(String prefix) {
        if(prefix == null) prefix = "";
        String namespace = lookupNamespaceLocally(prefix);
        if (namespace == null && isInTree()) {
            List<Node> pathToRoot = new PathFromRootVisitor().findPath(getModel(). getDocument(), this);
            namespace = lookupNamespace(prefix, pathToRoot);
        }
        return namespace;
    }

    public static String lookupNamespace(Node current, List<Node> ancestors) {
        String namespace = current.getNamespaceURI();
        if (namespace == null) {
            namespace = lookupNamespace(current.getPrefix(), ancestors);
        }
        return namespace;
    }
    
    public static String lookupNamespace(String prefix, List<Node> path) {
        if (path == null) return null;
        if(prefix == null) prefix = "";
        for (Node node : path) {
            String namespace = ((NodeImpl)node).lookupNamespaceLocally(prefix);
            if (namespace != null) {
                return namespace;
            }
        }
        return null;
    }
    
    String lookupNamespaceLocally(String prefix) {
        if(prefix == null) prefix = "";
        if(hasAttributes()) {
            for (Attribute attribute:getAttributesForRead()) {
                if(attribute.isXmlnsAttribute()) {
                    String key = attribute.getPrefix() == null ? "" : attribute.getLocalName();
                    if(key.equals(prefix)) {
                        return attribute.getValue();
                    }
                }
            }
        }
        return null;
    }
    
    String lookupPrefixLocally(String uri) {
        if(hasAttributes()) {
            String defaultNamespace = null;
            for (Attribute attribute:getAttributesForRead()) {
                String attrName = attribute.getName();
                if (attrName.startsWith(XMLNS)) {
                    if (attrName.length() == 5) {
                        defaultNamespace = attribute.getValue();
                    } else if (attrName.charAt(5) == ':' && uri.equals(attribute.getValue())) {
                        return attrName.substring(6);
                    }
                }
            }
            if (uri.equals(defaultNamespace)) {
                return "";
            }
        }
        return null;
    }
    
    public String lookupPrefix(String uri) {
        if(uri == null) return null;
        if(isInTree()) {
            PathFromRootVisitor pfrv = new PathFromRootVisitor();
            List<Node> path = pfrv.findPath(getModel().getDocument(),this);
            if (path != null && ! path.isEmpty()) {
                return lookupPrefix(uri, path);
            }
        } 
        return lookupPrefixLocally(uri);
    }

    public static String lookupPrefix(String uri, List<Node> path) {
        if (path == null) return null;
        for(Node node : path) {
            NodeImpl n = (NodeImpl) node;
            String prefix = n.lookupPrefixLocally(uri);
            if (prefix != null) {
                return prefix;
            }
        }
        return null;
    }
    
    public String getPrefix() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public void setPrefix(String str) throws DOMException {
    }
    
    public void normalize() {
    }
    // DOM level 3
    public short compareDocumentPosition(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    
    public String getBaseURI() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object getFeature(String a, String b) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public String getTextContent() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object getUserData(String a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isDefaultNamespace(String a)  {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isEqualNode(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public boolean isSameNode(org.w3c.dom.Node a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public void setTextContent(String a) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    public Object setUserData(String a, Object b, UserDataHandler c) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "This read-only implementation supports DOM level 1 Core and XML module.");
    }
    
    /*
     * Used by DiffMerger to merge token changes
     *
     * @param newNode
     */
    public void copyTokens(Node newNode) {
        checkNotInTree();
        setTokens(((NodeImpl)newNode).getTokens());
    }
    
    /**
     * Returns a List of all children of this node.
     * @return Returns a unmodifiable List of all children of this node.
     */
    private List<Node> getChildren() {
        return createUnmodifiableListIfNeeded(children);
    }
    
    /**
     * Returns a List of all children of this node for updates.
     * @return Returns a modifiable List of all children of this node.
     */
    private List<Node> getChildrenForWrite() {
        checkNotInTree();
        if (children == null) {
            children = new ArrayList<Node>(0);
        }
        return children;
    }
    
    /**
     * Sets the children of this node
     * @param newChildren - The list of children.
     */
    private void setChildren(List<Node> newChildren) {
        checkNotInTree();
        children = newChildren;
    }

    /**
     * Returns a readonly List of all attributes of this node.
     * @return Returns a unmodifiable List of all attributes of this node.
     */
    protected List<Attribute> getAttributesForRead() {
        return createUnmodifiableListIfNeeded(attributes);
    }
    
    /**
     * Returns a modifiable List of all attributes of this node for updates.
     * @return Returns a modifiable List of all attributes of this node.
     */
    protected List<Attribute> getAttributesForWrite() {
        checkNotInTree();
        if (attributes == null) {
            attributes = new ArrayList<Attribute>(0);
        }
        return attributes;
    }
    
    /**
     * Sets the attributes of this node
     * @param newAttributes - The list of attributes.
     */
    private void setAttributes(List<Attribute> newAttributes) {
        checkNotInTree();
        attributes = newAttributes;
    }

    /**
     * Returns the readonly lexical tokens associated with this node.
     * @return The unmodifiable list of lexical tokens.
     */
    public List<Token> getTokens() {
        return createUnmodifiableListIfNeeded(tokens);
    }

    /**
     * Returns the lexical tokens associated with this node for updates.
     * @return The modifiable list of lexical tokens.
     */
    List<Token> getTokensForWrite() {
        checkNotInTree();
        if (tokens == null) {
            tokens = new ArrayList<Token>(0);
        }
        return tokens;
    }
    
    /**
     * Sets the lexical tokens associated with this node
     * @param newTokens - The list of lexical tokens.
     */
    void setTokens(List<Token> newTokens) {
        tokens = newTokens;
    }
	
    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy constructor for nodes.
     * Used during Copy/Paste, Cut/Paste operation
     * @return the clone
     */
    public Node copy() {
        NodeImpl clone = (NodeImpl) cloneNode(true);
        clone.assignNodeId(-1);
        return clone;
    }
    
    /**
     * Wraps given list in unmodifiable list if needed.
     * This will be called by getters in read mode, and is intended to be called
     * from, r/o getters for internal data stuctures like children, attributes,
     * and tokens.
     * This api does not wrap list which is already wrapped, to avoid recursive
     * wrapping, when nodes are cloned several times.
     * Since there is no easy api to check for unmodifiable list in Collections
     * framework, we rely on instanceof ArrayList as potential candidate for 
     * wrapping.
     */
    private <E extends Object> List<E> createUnmodifiableListIfNeeded(List<E> objects) {
        // initialize unmodifiableObjects as objects as they may already be wrapped
        List<E> unmodifiableObjects = objects;
        if(objects == null) {
            // if null, return empty list
            unmodifiableObjects = Collections.emptyList();
        } else if (objects instanceof ArrayList) {
            // objects seem to be modifiable, so wrap it
            unmodifiableObjects = Collections.unmodifiableList(objects);
        }
        return unmodifiableObjects;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + getNodeName() + ">";
    }
}
