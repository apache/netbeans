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

package org.netbeans.modules.xml.text.completion;

import org.w3c.dom.*;

import org.netbeans.modules.xml.api.model.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * Mutable delegator to passed <code>HintContext</code> delegee.
 * Can be reinitialized to avoid extra memory allocation by init
 * methods.
 *
 * @author  Petr Kuzel
 */
class DefaultContext implements HintContext, Attr, Element {

    private Node peer;
    private String text;

    // virtual context has links to surrounding world
    private Node parent, previous, next;
    private boolean virtual;
    private short type;
    
    /** 
     * Creates new DefaultContext. 
     * User MUST call <code>init</code> prior any further operation.
     */
    public DefaultContext() {
    }

    /**
     * Turn ordinary node into a virtual one returning from its hiearchy calls:
     * <ul>
     * <li>getParentNode() - parent
     * <li>getPreviousSibling() - previous node
     * <li>getNextSibling() - next node
     * </ul>
     */
    public void initVirtualElement(Node parent, Node previous, Node next) {
        this.parent = parent;
        this.previous = previous;
        this.next = next;
        this.type = Node.ELEMENT_NODE;        
        virtual = true;
        this.peer = parent; // Not correct, but better than not setting it
    }

    /**
     * Turn ordinary node into a virtual one returning from its hiearchy calls:
     * <ul>
     * <li>getOwnerElement() - parent
     * <li>getName(), getNodeName() - text
     * </ul>
     */    
    public void initVirtualAttr(Element parent, String text) {
        this.parent = parent;
        this.next = null;
        this.previous = null;
        this.text = text;
        this.type = Node.ATTRIBUTE_NODE;
        virtual = true;
        this.peer = parent; // Not correct, but better than not setting it
    }
    
    /**
     * Init ordinary node.
     * @param node DOM node, must not be <code>null</code>
     * @param preText hint known prefix
     */
    public void init(Node node, String preText) {
        peer = node;
        text = preText;
        virtual = false;
//        System.err.println("DUMP:" + dump());
    }
    
    public boolean isInitialized() {
        return peer != null;
    }
   
    String dump() {
        Node parent = getParentNode();
        if (parent != null) {
            
            String name = parent.getNodeName();
                        
            // index of place we are inserted to
            
            int index = 0;
            
            Node sibling = null;
            
            // find previous ELEMENT sibling
            
            sibling = this;
            do {
                sibling = sibling.getPreviousSibling();
                if (sibling == null) break;
                if (sibling.getNodeType() == Node.ELEMENT_NODE) break;
            } while (true);
                
            while (sibling != null) {

                index++;
                
                // find previous ELEMENT sibling
                
                do {
                    sibling = sibling.getPreviousSibling();
                    if (sibling == null) break;
                    if (sibling.getNodeType() == Node.ELEMENT_NODE) break;
                } while (true);
                
            }

            // all children
            
           String names = "";
           NodeList kids = parent.getChildNodes();
           
           if (kids == null) {
               return  "X missing kids";
           }
           for (int i = 0; i < kids.getLength(); i++) {
               Node next = kids.item(i);
               if (next.getNodeType() == Node.ELEMENT_NODE) {
                    names += next.getNodeName()  + ", ";
               }
           }

           // this is context needed by Sandeep's Liasion
           return "DefaultContext: parent: " + name + " p'kids: " + names + " index: " + index;
            
        } else {
            return  "X missing parent";
        }

    }
    
    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return peer.getNodeName();
    }
    
    /**
     * Tests whether the DOM implementation implements a specific feature and
     * that feature is supported by this node.
     * @param featureThe name of the feature to test. This is the same name
     *  which can be passed to the method <code>hasFeature</code> on
     *  <code>DOMImplementation</code>.
     * @param versionThis is the version number of the feature to test. In
     *  Level 2, version 1, this is the string "2.0". If the version is not
     *  specified, supporting any version of the feature will cause the
     *  method to return <code>true</code>.
     * @return Returns <code>true</code> if the specified feature is
     *  supported on this node, <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean isSupported(String feature, String version) {
        throw new UOException();
    }
    
    public void setPrefix(String prefix) throws DOMException {
        throw new UOException();
    }
    
    /**
     * The namespace prefix of this node, or <code>null</code> if it is
     * unspecified.
     * <br>Note that setting this attribute, when permitted, changes the
     * <code>nodeName</code> attribute, which holds the qualified name, as
     * well as the <code>tagName</code> and <code>name</code> attributes of
     * the <code>Element</code> and <code>Attr</code> interfaces, when
     * applicable.
     * <br>Note also that changing the prefix of an attribute that is known to
     * have a default value, does not make a new attribute with the default
     * value and the original prefix appear, since the
     * <code>namespaceURI</code> and <code>localName</code> do not change.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.
     * @exception DOMException
     *  INVALID_CHARACTER_ERR: Raised if the specified prefix contains an
     *  illegal character.
     *  <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *  <br>NAMESPACE_ERR: Raised if the specified <code>prefix</code> is
     *  malformed, if the <code>namespaceURI</code> of this node is
     *  <code>null</code>, if the specified prefix is "xml" and the
     *  <code>namespaceURI</code> of this node is different from "
     *  http://www.w3.org/XML/1998/namespace", if this node is an attribute
     *  and the specified prefix is "xmlns" and the
     *  <code>namespaceURI</code> of this node is different from "
     *  http://www.w3.org/2000/xmlns/", or if this node is an attribute and
     *  the <code>qualifiedName</code> of this node is "xmlns" .
     * @since DOM Level 2
     */
    public String getPrefix() {
        throw new UOException();
    }
    
    /**
     * The node immediately preceding this node. If there is no such node,
     * this returns <code>null</code>.
     */
    public Node getPreviousSibling() {
        if (virtual) {
            return previous;
        } else {
            return peer.getPreviousSibling();
        }
    }
    
    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType() {
        if (virtual) return type;
        return peer.getNodeType();
    }
    
    /**
     * The <code>Document</code> object associated with this node. This is
     * also the <code>Document</code> object used to create new nodes. When
     * this node is a <code>Document</code> or a <code>DocumentType</code>
     * which is not used with any <code>Document</code> yet, this is
     * <code>null</code>.
     * @version DOM Level 2
     */
    public Document getOwnerDocument() {
        return peer.getOwnerDocument();
    }
    
    /**
     * Replaces the child node <code>oldChild</code> with <code>newChild</code>
     * in the list of children, and returns the <code>oldChild</code> node.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object,
     * <code>oldChild</code> is replaced by all of the
     * <code>DocumentFragment</code> children, which are inserted in the
     * same order. If the <code>newChild</code> is already in the tree, it
     * is first removed.
     * @param newChildThe new node to put in the child list.
     * @param oldChildThe node being replaced in the list.
     * @return The node replaced.
     * @exception DOMException
     *  HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *  allow children of the type of the <code>newChild</code> node, or if
     *  the node to put in is one of this node's ancestors.
     *  <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *  from a different document than the one that created this node.
     *  <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node or the parent of
     *  the new node is readonly.
     *  <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of
     *  this node.
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return peer.replaceChild(newChild, oldChild);
    }
    
    /**
     * @return String representing prefix as entered by user that can be used for refining results
     */
    public String getCurrentPrefix() {
        return text;
    }
    
    /**
     * Returns a duplicate of this node, i.e., serves as a generic copy
     * constructor for nodes. The duplicate node has no parent; (
     * <code>parentNode</code> is <code>null</code>.).
     * <br>Cloning an <code>Element</code> copies all attributes and their
     * values, including those generated by the XML processor to represent
     * defaulted attributes, but this method does not copy any text it
     * contains unless it is a deep clone, since the text is contained in a
     * child <code>Text</code> node. Cloning an <code>Attribute</code>
     * directly, as opposed to be cloned as part of an <code>Element</code>
     * cloning operation, returns a specified attribute (
     * <code>specified</code> is <code>true</code>). Cloning any other type
     * of node simply returns a copy of this node.
     * <br>Note that cloning an immutable subtree results in a mutable copy,
     * but the children of an <code>EntityReference</code> clone are readonly
     * . In addition, clones of unspecified <code>Attr</code> nodes are
     * specified. And, cloning <code>Document</code>,
     * <code>DocumentType</code>, <code>Entity</code>, and
     * <code>Notation</code> nodes is implementation dependent.
     * @param deepIf <code>true</code>, recursively clone the subtree under
     *  the specified node; if <code>false</code>, clone only the node
     *  itself (and its attributes, if it is an <code>Element</code>).
     * @return The duplicate node.
     */
    public Node cloneNode(boolean deep) {
        return peer.cloneNode(deep);
    }
    
    /**
     * The node immediately following this node. If there is no such node,
     * this returns <code>null</code>.
     */
    public Node getNextSibling() {
        if (virtual) {
            return next;
        } else {
            return peer.getNextSibling();
        }
    }
    
    /**
     * Inserts the node <code>newChild</code> before the existing child node
     * <code>refChild</code>. If <code>refChild</code> is <code>null</code>,
     * insert <code>newChild</code> at the end of the list of children.
     * <br>If <code>newChild</code> is a <code>DocumentFragment</code> object,
     * all of its children are inserted, in the same order, before
     * <code>refChild</code>. If the <code>newChild</code> is already in the
     * tree, it is first removed.
     * @param newChildThe node to insert.
     * @param refChildThe reference node, i.e., the node before which the new
     *  node must be inserted.
     * @return The node being inserted.
     * @exception DOMException
     *  HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *  allow children of the type of the <code>newChild</code> node, or if
     *  the node to insert is one of this node's ancestors.
     *  <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *  from a different document than the one that created this node.
     *  <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly or
     *  if the parent of the node being inserted is readonly.
     *  <br>NOT_FOUND_ERR: Raised if <code>refChild</code> is not a child of
     *  this node.
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return peer.insertBefore(newChild, refChild);
    }
    
    /**
     * The namespace URI of this node, or <code>null</code> if it is
     * unspecified.
     * <br>This is not a computed value that is the result of a namespace
     * lookup based on an examination of the namespace declarations in
     * scope. It is merely the namespace URI given at creation time.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.Per
     * the Namespaces in XML Specification  an attribute does not inherit
     * its namespace from the element it is attached to. If an attribute is
     * not explicitly given a namespace, it simply has no namespace.
     * @since DOM Level 2
     */
    public String getNamespaceURI() {
        throw new UOException();
    }
    
    /**
     * A <code>NamedNodeMap</code> containing the attributes of this node (if
     * it is an <code>Element</code>) or <code>null</code> otherwise.
     */
    public NamedNodeMap getAttributes() {
        return peer.getAttributes();
    }
    
    /**
     * A <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     */
    public NodeList getChildNodes() {
        return peer.getChildNodes();
    }
    
    /**
     * The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @exception DOMException
     *  NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *  DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *  fit in a <code>DOMString</code> variable on the implementation
     *  platform.
     */
    public String getNodeValue() throws DOMException {
        return peer.getNodeValue();
    }
    
    /**
     * Adds the node <code>newChild</code> to the end of the list of children
     * of this node. If the <code>newChild</code> is already in the tree, it
     * is first removed.
     * @param newChildThe node to add.If it is a <code>DocumentFragment</code>
     *   object, the entire contents of the document fragment are moved
     *  into the child list of this node
     * @return The node added.
     * @exception DOMException
     *  HIERARCHY_REQUEST_ERR: Raised if this node is of a type that does not
     *  allow children of the type of the <code>newChild</code> node, or if
     *  the node to append is one of this node's ancestors.
     *  <br>WRONG_DOCUMENT_ERR: Raised if <code>newChild</code> was created
     *  from a different document than the one that created this node.
     *  <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public Node appendChild(Node newChild) throws DOMException {
        return peer.appendChild(newChild);
    }
    
    /**
     * Returns the local part of the qualified name of this node.
     * <br>For nodes of any type other than <code>ELEMENT_NODE</code> and
     * <code>ATTRIBUTE_NODE</code> and nodes created with a DOM Level 1
     * method, such as <code>createElement</code> from the
     * <code>Document</code> interface, this is always <code>null</code>.
     * @since DOM Level 2
     */
    public String getLocalName() {
        throw new UOException();
    }
    
    /**
     * The parent of this node. All nodes, except <code>Attr</code>,
     * <code>Document</code>, <code>DocumentFragment</code>,
     * <code>Entity</code>, and <code>Notation</code> may have a parent.
     * However, if a node has just been created and not yet added to the
     * tree, or if it has been removed from the tree, this is
     * <code>null</code>.
     */
    public Node getParentNode() {
        if (virtual) {
            return parent;
        } else {
            return peer.getParentNode();
        }
    }
    
    public void setNodeValue(String nodeValue) throws DOMException {
        peer.setNodeValue(nodeValue);
    }
    
    /**
     * The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getLastChild() {
        return peer.getLastChild();
    }
    
    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *  <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributes() {
        throw new UOException();
    }
    
    /**
     * Puts all <code>Text</code> nodes in the full depth of the sub-tree
     * underneath this <code>Node</code>, including attribute nodes, into a
     * "normal" form where only structure (e.g., elements, comments,
     * processing instructions, CDATA sections, and entity references)
     * separates <code>Text</code> nodes, i.e., there are neither adjacent
     * <code>Text</code> nodes nor empty <code>Text</code> nodes. This can
     * be used to ensure that the DOM view of a document is the same as if
     * it were saved and re-loaded, and is useful when operations (such as
     * XPointer  lookups) that depend on a particular document tree
     * structure are to be used.In cases where the document contains
     * <code>CDATASections</code>, the normalize operation alone may not be
     * sufficient, since XPointers do not differentiate between
     * <code>Text</code> nodes and <code>CDATASection</code> nodes.
     * @version DOM Level 2
     */
    public void normalize() {
        peer.normalize();
    }
    
    /**
     * Removes the child node indicated by <code>oldChild</code> from the list
     * of children, and returns it.
     * @param oldChildThe node being removed.
     * @return The node removed.
     * @exception DOMException
     *  NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *  <br>NOT_FOUND_ERR: Raised if <code>oldChild</code> is not a child of
     *  this node.
     */
    public Node removeChild(Node oldChild) throws DOMException {
        return peer.removeChild(oldChild);
    }
    
    /**
     * Returns whether this node has any children.
     * @return  <code>true</code> if this node has any children,
     *  <code>false</code> otherwise.
     */
    public boolean hasChildNodes() {
        return peer.hasChildNodes();
    }
    
    /**
     * The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     */
    public Node getFirstChild() {
        return peer.getFirstChild();
    }

    
    public String toString() {
        return "context node(" + getNodeName() + ", prefix("+ getCurrentPrefix() + "))";
    }
    
    // Attr implementation  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public boolean getSpecified() {
        if (peer instanceof Attr) {
            return ((Attr)peer).getSpecified();
        } else {
            throw new NOA();
        }
    }
    
    public String getName() {
        if (virtual) return text;
        if (peer instanceof Attr) {
            return ((Attr)peer).getName();
        } else {
            throw new NOA();
        }        
    }
    
    public Element getOwnerElement() {
        if (virtual) return (Element) parent;
        
        if (peer instanceof Attr) {
            return ((Attr)peer).getOwnerElement();
        } else {
            throw new NOA();
        }
    }
    
    public void setValue(String value) throws DOMException {
        if (peer instanceof Attr) {
            ((Attr)peer).setValue(value);
        } else {
            throw new NOA();
        }
    }
    
    public String getValue() {
        if (peer instanceof Attr) {
            return ((Attr)peer).getValue();
        } else {
            throw new NOA();
        }
    }
    
	public String getAttribute(String str) {
		if (peer instanceof Element) {
            return ((Element)peer).getAttribute(str);
		} else {
			throw new NOE();
		}
	}	
    
	public String getAttributeNS(String str, String str1) {
		if (peer instanceof Element) {
            return ((Element)peer).getAttributeNS(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.Attr getAttributeNode(String str) {
		if (peer instanceof Element) {
            return ((Element)peer).getAttributeNode(str);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.Attr getAttributeNodeNS(String str, String str1) {
		if (peer instanceof Element) {
            return ((Element)peer).getAttributeNodeNS(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.NodeList getElementsByTagName(String str) {
		if (peer instanceof Element) {
            return ((Element)peer).getElementsByTagName(str);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.NodeList getElementsByTagNameNS(String str, String str1) {
		if (peer instanceof Element) {
            return ((Element)peer).getElementsByTagNameNS(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public String getTagName() {
		if (peer instanceof Element) {
            return ((Element)peer).getTagName();
		} else {
			throw new NOE();
		}
	}
	
	public boolean hasAttribute(String str) {
		if (peer instanceof Element) {
            return ((Element)peer).hasAttribute(str);
		} else {
			throw new NOE();
		}
	}
	
	public boolean hasAttributeNS(String str, String str1) {
		if (peer instanceof Element) {
            return ((Element)peer).hasAttributeNS(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public void removeAttribute(String str) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            ((Element)peer).removeAttribute(str);
		} else {
			throw new NOE();
		}
	}
	
	public void removeAttributeNS(String str, String str1) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            ((Element)peer).removeAttributeNS(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr attr) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            return ((Element)peer).removeAttributeNode(attr);
		} else {
			throw new NOE();
		}
	}
	
	public void setAttribute(String str, String str1) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            ((Element)peer).setAttribute(str, str1);
		} else {
			throw new NOE();
		}
	}
	
	public void setAttributeNS(String str, String str1, String str2) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            ((Element)peer).setAttributeNS(str, str1, str2);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr attr) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            return ((Element)peer).setAttributeNode(attr);
		} else {
			throw new NOE();
		}
	}
	
	public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr attr) throws org.w3c.dom.DOMException {
		if (peer instanceof Element) {
            return ((Element)peer).setAttributeNodeNS(attr);
		} else {
			throw new NOE();
		}
	}
	
    //??? or could we throw class casts
    private class NOA extends DOMException {
        
        private static final long serialVersionUID = 4600894053037825159L;
        
        NOA() {
            super(DOMException.NOT_SUPPORTED_ERR, "Peer " + peer + " is not instance of Attr!");
        }
    }
	
    private class NOE extends DOMException {
        
        private static final long serialVersionUID = 4600894053037825159L;
        
        NOE() {
            super(DOMException.NOT_SUPPORTED_ERR, "Peer " + peer + " is not instance of Element!");
        }
    }

    //
    // Implementation of DOM Level 3 methods
    //
    
    public short compareDocumentPosition (Node a) {
        throw new UOException();
    }
    
    public String getBaseURI() {
        throw new UOException();
    }
    public Object getFeature(String a, String b) {
        throw new UOException();
    }
    public String getTextContent () {
        throw new UOException();
    }
    public Object getUserData(String a) {
        throw new UOException();
    }
    public boolean isDefaultNamespace (String a)  {
        throw new UOException();
    }
    public boolean isEqualNode(Node a) {
        throw new UOException();
    }
    public boolean isSameNode(Node a) {
        throw new UOException();
    }
    public String lookupNamespaceURI(String a) {
        throw new UOException();
    }
    public String lookupPrefix(String a) {
        throw new UOException();
    }
    public void setTextContent(String a) {
        throw new UOException();
    }
    public Object setUserData(String a, Object b, UserDataHandler c) {
        throw new UOException();
    }
    // Implementation of DOM Level 3 methods for Element
    public TypeInfo getSchemaTypeInfo() {
        throw new UOException ();
    }
    public void setIdAttribute(String a, boolean b) {
        throw new UOException ();
    }
    public void setIdAttributeNS(String a, String b, boolean c) {
        throw new UOException ();
    }
    public void setIdAttributeNode(Attr a, boolean b) {
        throw new UOException ();
    }    
    // Implementation of DOM Level 3 methods for Attr 
    
    public boolean isId () {
        throw new UOException ();
    }
    // Implementation of DOM Level 3 methods for Text
    public Text replaceWholeText (String a) {
        throw new UOException ();
    }
    public String getWholeText() {
        throw new UOException ();
    }
    public boolean isElementContentWhitespace() {
        throw new UOException ();
    }
}
