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

package org.netbeans.tax.dom;

import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 *
 * @author  Petr Kuzel
 */
class ElementImpl extends NodeImpl implements  Element {

    private final TreeElement peer;

    /** Creates a new instance of ElementImpl */
    public ElementImpl(TreeElement peer) {
        this.peer = peer;
    }


    /** Retrieves an attribute value by name.
     * @param name The name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string
     *   if that attribute does not have a specified or default value.
     *
     */
    public String getAttribute(String name) {
        return peer.getAttribute(name).getValue();
    }
    
    /** Retrieves an attribute value by local name and namespace URI.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> value as a string, or the empty string
     *   if that attribute does not have a specified or default value.
     * @since DOM Level 2
     *
     */
    public String getAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    /** Retrieves an attribute node by name.
     * <br>To retrieve an attribute node by qualified name and namespace URI,
     * use the <code>getAttributeNodeNS</code> method.
     * @param name The name (<code>nodeName</code>) of the attribute to
     *   retrieve.
     * @return The <code>Attr</code> node with the specified name (
     *   <code>nodeName</code>) or <code>null</code> if there is no such
     *   attribute.
     *
     */
    public Attr getAttributeNode(String name) {
        return Wrapper.wrap(peer.getAttribute(name));
    }
    
    /** Retrieves an <code>Attr</code> node by local name and namespace URI.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the attribute to retrieve.
     * @param localName The local name of the attribute to retrieve.
     * @return The <code>Attr</code> node with the specified attribute local
     *   name and namespace URI or <code>null</code> if there is no such
     *   attribute.
     * @since DOM Level 2
     *
     */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    /** A <code>NamedNodeMap</code> containing the attributes of this node (if
     * it is an <code>Element</code>) or <code>null</code> otherwise.
     *
     */
    public NamedNodeMap getAttributes() {
        return Wrapper.wrap(peer.getAttributes());
    }
    
    /** A <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     *
     */
    public NodeList getChildNodes() {
        return Wrapper.wrap(peer.getChildNodes());
    }
    
    /** Returns a <code>NodeList</code> of all descendant <code>Elements</code>
     * with a given tag name, in the order in which they are encountered in
     * a preorder traversal of this <code>Element</code> tree.
     * @param name The name of the tag to match on. The special value "*"
     *   matches all tags.
     * @return A list of matching <code>Element</code> nodes.
     *
     */
    public NodeList getElementsByTagName(String name) {
        throw new UOException();
    }
    
    /** Returns a <code>NodeList</code> of all the descendant
     * <code>Elements</code> with a given local name and namespace URI in
     * the order in which they are encountered in a preorder traversal of
     * this <code>Element</code> tree.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the elements to match on. The
     *   special value "*" matches all namespaces.
     * @param localName The local name of the elements to match on. The
     *   special value "*" matches all local names.
     * @return A new <code>NodeList</code> object containing all the matched
     *   <code>Elements</code>.
     * @since DOM Level 2
     *
     */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    /** The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getFirstChild() {
        return Wrapper.wrap(peer.getFirstChild());
    }
    
    /** The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getLastChild() {
        return Wrapper.wrap(peer.getLastChild());        
    }
    
    
    /** The node immediately following this node. If there is no such node,
     * this returns <code>null</code>.
     *
     */
    public Node getNextSibling() {
        return Children.getNextSibling(peer);
    }
    
    /** The name of this node, depending on its type; see the table above.
     *
     */
    public String getNodeName() {
        return getTagName();
    }
    
    /** A code representing the type of the underlying object, as defined above.
     *
     */
    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }
    
    /** The value of this node, depending on its type; see the table above.
     * When it is defined to be <code>null</code>, setting it has no effect.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @exception DOMException
     *   DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *   fit in a <code>DOMString</code> variable on the implementation
     *   platform.
     *
     */
    public String getNodeValue() throws DOMException {
        return null;
    }
    
    
    /** The parent of this node. All nodes, except <code>Attr</code>,
     * <code>Document</code>, <code>DocumentFragment</code>,
     * <code>Entity</code>, and <code>Notation</code> may have a parent.
     * However, if a node has just been created and not yet added to the
     * tree, or if it has been removed from the tree, this is
     * <code>null</code>.
     *
     */
    public Node getParentNode() {
        return Wrapper.wrap(peer.getParentNode());
    }    
    
    /** The node immediately preceding this node. If there is no such node,
     * this returns <code>null</code>.
     *
     */
    public Node getPreviousSibling() {
        return Children.getPreviousSibling(peer);
    }
    
    /** The name of the element. For example, in:
     * <pre> &lt;elementExample
     * id="demo"&gt; ... &lt;/elementExample&gt; , </pre>
     *  <code>tagName</code> has
     * the value <code>"elementExample"</code>. Note that this is
     * case-preserving in XML, as are all of the operations of the DOM. The
     * HTML DOM returns the <code>tagName</code> of an HTML element in the
     * canonical uppercase form, regardless of the case in the source HTML
     * document.
     *
     */
    public String getTagName() {
        return peer.getQName();
    }
    
    /** Returns <code>true</code> when an attribute with a given name is
     * specified on this element or has a default value, <code>false</code>
     * otherwise.
     * @param name The name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given name is
     *   specified on this element or has a default value, <code>false</code>
     *    otherwise.
     * @since DOM Level 2
     *
     */
    public boolean hasAttribute(String name) {
        throw new UOException();
    }
    
    /** Returns <code>true</code> when an attribute with a given local name and
     * namespace URI is specified on this element or has a default value,
     * <code>false</code> otherwise.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the attribute to look for.
     * @param localName The local name of the attribute to look for.
     * @return <code>true</code> if an attribute with the given local name
     *   and namespace URI is specified or has a default value on this
     *   element, <code>false</code> otherwise.
     * @since DOM Level 2
     *
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    /** Returns whether this node (if it is an element) has any attributes.
     * @return <code>true</code> if this node has any attributes,
     *   <code>false</code> otherwise.
     * @since DOM Level 2
     *
     */
    public boolean hasAttributes() {
        return peer.hasAttributes();
    }
    
    /** Returns whether this node has any children.
     * @return <code>true</code> if this node has any children,
     *   <code>false</code> otherwise.
     *
     */
    public boolean hasChildNodes() {
        return peer.hasChildNodes();
    }
        
    /** Removes an attribute by name. If the removed attribute is known to have
     * a default value, an attribute immediately appears containing the
     * default value as well as the corresponding namespace URI, local name,
     * and prefix when applicable.
     * <br>To remove an attribute by local name and namespace URI, use the
     * <code>removeAttributeNS</code> method.
     * @param name The name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *
     */
    public void removeAttribute(String name) throws DOMException {
        throw new ROException();
    }
    
    /** Removes an attribute by local name and namespace URI. If the removed
     * attribute has a default value it is immediately replaced. The
     * replacing attribute has the same namespace URI and local name, as
     * well as the original prefix.
     * <br>Documents which do not support the "XML" feature will permit only
     * the DOM Level 1 calls for creating/setting elements and attributes.
     * Hence, if you specify a non-null namespace URI, these DOMs will never
     * find a matching node.
     * @param namespaceURI The namespace URI of the attribute to remove.
     * @param localName The local name of the attribute to remove.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     * @since DOM Level 2
     *
     */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new ROException();
    }
    
    /** Removes the specified attribute node. If the removed <code>Attr</code>
     * has a default value it is immediately replaced. The replacing
     * attribute has the same namespace URI and local name, as well as the
     * original prefix, when applicable.
     * @param oldAttr The <code>Attr</code> node to remove from the attribute
     *   list.
     * @return The <code>Attr</code> node that was removed.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NOT_FOUND_ERR: Raised if <code>oldAttr</code> is not an attribute
     *   of the element.
     *
     */
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new ROException();
    }
    
    
    /** Adds a new attribute. If an attribute with that name is already present
     * in the element, its value is changed to be that of the value
     * parameter. This value is a simple string; it is not parsed as it is
     * being set. So any markup (such as syntax to be recognized as an
     * entity reference) is treated as literal text, and needs to be
     * appropriately escaped by the implementation when it is written out.
     * In order to assign an attribute value that contains entity
     * references, the user must create an <code>Attr</code> node plus any
     * <code>Text</code> and <code>EntityReference</code> nodes, build the
     * appropriate subtree, and use <code>setAttributeNode</code> to assign
     * it as the value of an attribute.
     * <br>To set an attribute with a qualified name and namespace URI, use
     * the <code>setAttributeNS</code> method.
     * @param name The name of the attribute to create or alter.
     * @param value Value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *
     */
    public void setAttribute(String name, String value) throws DOMException {
        throw new ROException();
    }
    
    /** Adds a new attribute. If an attribute with the same local name and
     * namespace URI is already present on the element, its prefix is
     * changed to be the prefix part of the <code>qualifiedName</code>, and
     * its value is changed to be the <code>value</code> parameter. This
     * value is a simple string; it is not parsed as it is being set. So any
     * markup (such as syntax to be recognized as an entity reference) is
     * treated as literal text, and needs to be appropriately escaped by the
     * implementation when it is written out. In order to assign an
     * attribute value that contains entity references, the user must create
     * an <code>Attr</code> node plus any <code>Text</code> and
     * <code>EntityReference</code> nodes, build the appropriate subtree,
     * and use <code>setAttributeNodeNS</code> or
     * <code>setAttributeNode</code> to assign it as the value of an
     * attribute.
     * @param namespaceURI The namespace URI of the attribute to create or
     *   alter.
     * @param qualifiedName The qualified name of the attribute to create or
     *   alter.
     * @param value The value to set in string form.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name
     *   contains an illegal character, per the XML 1.0 specification .
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is
     *   malformed per the Namespaces in XML specification, if the
     *   <code>qualifiedName</code> has a prefix and the
     *   <code>namespaceURI</code> is <code>null</code>, if the
     *   <code>qualifiedName</code> has a prefix that is "xml" and the
     *   <code>namespaceURI</code> is different from "
     *   http://www.w3.org/XML/1998/namespace", or if the
     *   <code>qualifiedName</code>, or its prefix, is "xmlns" and the
     *   <code>namespaceURI</code> is different from "
     *   http://www.w3.org/2000/xmlns/".
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *   support the <code>"XML"</code> feature, since namespaces were
     *   defined by XML.
     * @since DOM Level 2
     *
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        throw new ROException();
    }
    
    /** Adds a new attribute node. If an attribute with that name (
     * <code>nodeName</code>) is already present in the element, it is
     * replaced by the new one.
     * <br>To add a new attribute node with a qualified name and namespace
     * URI, use the <code>setAttributeNodeNS</code> method.
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing
     *   attribute, the replaced <code>Attr</code> node is returned,
     *   otherwise <code>null</code> is returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
     *   different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
     *   attribute of another <code>Element</code> object. The DOM user must
     *   explicitly clone <code>Attr</code> nodes to re-use them in other
     *   elements.
     *
     */
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new ROException();
    }
    
    /** Adds a new attribute. If an attribute with that local name and that
     * namespace URI is already present in the element, it is replaced by
     * the new one.
     * @param newAttr The <code>Attr</code> node to add to the attribute list.
     * @return If the <code>newAttr</code> attribute replaces an existing
     *   attribute with the same local name and namespace URI, the replaced
     *   <code>Attr</code> node is returned, otherwise <code>null</code> is
     *   returned.
     * @exception DOMException
     *   WRONG_DOCUMENT_ERR: Raised if <code>newAttr</code> was created from a
     *   different document than the one that created the element.
     *   <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     *   <br>INUSE_ATTRIBUTE_ERR: Raised if <code>newAttr</code> is already an
     *   attribute of another <code>Element</code> object. The DOM user must
     *   explicitly clone <code>Attr</code> nodes to re-use them in other
     *   elements.
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *   support the <code>"XML"</code> feature, since namespaces were
     *   defined by XML.
     * @since DOM Level 2
     *
     */
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new ROException();
    }
    
}
