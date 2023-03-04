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

import java.util.Iterator;
import org.w3c.dom.*;
import org.netbeans.tax.*;
import org.netbeans.tax.TreeObjectList;

/**
 *
 * @author  Petr Kuzel
 */
class DocumentImpl extends NodeImpl implements Document{

    private final TreeDocumentRoot peer;

    /** Creates a new instance of AttrImpl */
    public DocumentImpl(TreeDocumentRoot peer) {
        this.peer = peer;
    }

    /** Creates an <code>Attr</code> of the given name. Note that the
     * <code>Attr</code> instance can then be set on an <code>Element</code>
     * using the <code>setAttributeNode</code> method.
     * <br>To create an attribute with a qualified name and namespace URI, use
     * the <code>createAttributeNS</code> method.
     * @param name The name of the attribute.
     * @return A new <code>Attr</code> object with the <code>nodeName</code>
     *   attribute set to <code>name</code>, and <code>localName</code>,
     *   <code>prefix</code>, and <code>namespaceURI</code> set to
     *   <code>null</code>. The value of the attribute is the empty string.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     *
     */
    public Attr createAttribute(String name) throws DOMException {
        throw new ROException();
    }
    
    /** Creates an attribute of the given qualified name and namespace URI.
     * @param namespaceURI The namespace URI of the attribute to create.
     * @param qualifiedName The qualified name of the attribute to
     *   instantiate.
     * @return A new <code>Attr</code> object with the following attributes:
     * <table border='1'>
     * <tr>
     * <th>
     *   Attribute</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>qualifiedName</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>Node.namespaceURI</code></td>
     * <td valign='top' rowspan='1' colspan='1'><code>namespaceURI</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>Node.prefix</code></td>
     * <td valign='top' rowspan='1' colspan='1'>prefix, extracted from
     *   <code>qualifiedName</code>, or <code>null</code> if there is no
     *   prefix</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.localName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>local name, extracted from
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Attr.name</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeValue</code></td>
     * <td valign='top' rowspan='1' colspan='1'>the empty
     *   string</td>
     * </tr>
     * </table>
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name
     *   contains an illegal character, per the XML 1.0 specification .
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
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        throw new ROException();
    }
    
    /** Creates a <code>CDATASection</code> node whose value is the specified
     * string.
     * @param data The data for the <code>CDATASection</code> contents.
     * @return The new <code>CDATASection</code> object.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     *
     */
    public CDATASection createCDATASection(String data) throws DOMException {
        throw new ROException();
    }
    
    /** Creates a <code>Comment</code> node given the specified string.
     * @param data The data for the node.
     * @return The new <code>Comment</code> object.
     *
     */
    public Comment createComment(String data) {
        throw new ROException();
    }
    
    /** Creates an empty <code>DocumentFragment</code> object.
     * @return A new <code>DocumentFragment</code>.
     *
     */
    public DocumentFragment createDocumentFragment() {
        throw new ROException();
    }
    
    /** Creates an element of the type specified. Note that the instance
     * returned implements the <code>Element</code> interface, so attributes
     * can be specified directly on the returned object.
     * <br>In addition, if there are known attributes with default values,
     * <code>Attr</code> nodes representing them are automatically created
     * and attached to the element.
     * <br>To create an element with a qualified name and namespace URI, use
     * the <code>createElementNS</code> method.
     * @param tagName The name of the element type to instantiate. For XML,
     *   this is case-sensitive. For HTML, the <code>tagName</code>
     *   parameter may be provided in any case, but it must be mapped to the
     *   canonical uppercase form by the DOM implementation.
     * @return A new <code>Element</code> object with the
     *   <code>nodeName</code> attribute set to <code>tagName</code>, and
     *   <code>localName</code>, <code>prefix</code>, and
     *   <code>namespaceURI</code> set to <code>null</code>.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     *
     */
    public Element createElement(String tagName) throws DOMException {
        throw new ROException();
    }
    
    /** Creates an element of the given qualified name and namespace URI.
     * @param namespaceURI The namespace URI of the element to create.
     * @param qualifiedName The qualified name of the element type to
     *   instantiate.
     * @return A new <code>Element</code> object with the following
     *   attributes:
     * <table border='1'>
     * <tr>
     * <th>Attribute</th>
     * <th>Value</th>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.nodeName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.namespaceURI</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>namespaceURI</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.prefix</code></td>
     * <td valign='top' rowspan='1' colspan='1'>prefix, extracted
     *   from <code>qualifiedName</code>, or <code>null</code> if there is
     *   no prefix</td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Node.localName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>local name, extracted from
     *   <code>qualifiedName</code></td>
     * </tr>
     * <tr>
     * <td valign='top' rowspan='1' colspan='1'><code>Element.tagName</code></td>
     * <td valign='top' rowspan='1' colspan='1'>
     *   <code>qualifiedName</code></td>
     * </tr>
     * </table>
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified qualified name
     *   contains an illegal character, per the XML 1.0 specification .
     *   <br>NAMESPACE_ERR: Raised if the <code>qualifiedName</code> is
     *   malformed per the Namespaces in XML specification, if the
     *   <code>qualifiedName</code> has a prefix and the
     *   <code>namespaceURI</code> is <code>null</code>, or if the
     *   <code>qualifiedName</code> has a prefix that is "xml" and the
     *   <code>namespaceURI</code> is different from "
     *   http://www.w3.org/XML/1998/namespace" .
     *   <br>NOT_SUPPORTED_ERR: Always thrown if the current document does not
     *   support the <code>"XML"</code> feature, since namespaces were
     *   defined by XML.
     * @since DOM Level 2
     *
     */
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        throw new ROException();
    }
    
    /** Creates an <code>EntityReference</code> object. In addition, if the
     * referenced entity is known, the child list of the
     * <code>EntityReference</code> node is made the same as that of the
     * corresponding <code>Entity</code> node.If any descendant of the
     * <code>Entity</code> node has an unbound namespace prefix, the
     * corresponding descendant of the created <code>EntityReference</code>
     * node is also unbound; (its <code>namespaceURI</code> is
     * <code>null</code>). The DOM Level 2 does not support any mechanism to
     * resolve namespace prefixes.
     * @param name The name of the entity to reference.
     * @return The new <code>EntityReference</code> object.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified name contains an
     *   illegal character.
     *   <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     *
     */
    public EntityReference createEntityReference(String name) throws DOMException {
        throw new ROException();
    }
    
    /** Creates a <code>ProcessingInstruction</code> node given the specified
     * name and data strings.
     * @param target The target part of the processing instruction.
     * @param data The data for the node.
     * @return The new <code>ProcessingInstruction</code> object.
     * @exception DOMException
     *   INVALID_CHARACTER_ERR: Raised if the specified target contains an
     *   illegal character.
     *   <br>NOT_SUPPORTED_ERR: Raised if this document is an HTML document.
     *
     */
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        throw new ROException();
    }
    
    /** Creates a <code>Text</code> node given the specified string.
     * @param data The data for the node.
     * @return The new <code>Text</code> object.
     *
     */
    public Text createTextNode(String data) {
        throw new ROException();
    }
    
    /** The Document Type Declaration (see <code>DocumentType</code>)
     * associated with this document. For HTML documents as well as XML
     * documents without a document type declaration this returns
     * <code>null</code>. The DOM Level 2 does not support editing the
     * Document Type Declaration. <code>docType</code> cannot be altered in
     * any way, including through the use of methods inherited from the
     * <code>Node</code> interface, such as <code>insertNode</code> or
     * <code>removeNode</code>.
     *
     */
    public DocumentType getDoctype() {
        return null; //!!!
    }
    
    /** This is a convenience attribute that allows direct access to the child
     * node that is the root element of the document. For HTML documents,
     * this is the element with the tagName "HTML".
     *
     */
    public Element getDocumentElement() {
        Iterator it = peer.getChildNodes().iterator();
        while (it.hasNext()) {            
            Object next = it.next();
            if (next instanceof TreeElement) {
                return Wrapper.wrap((TreeElement)next);
            }
        }
        return null;
    }
    
    /** Returns the <code>Element</code> whose <code>ID</code> is given by
     * <code>elementId</code>. If no such element exists, returns
     * <code>null</code>. Behavior is not defined if more than one element
     * has this <code>ID</code>. The DOM implementation must have
     * information that says which attributes are of type ID. Attributes
     * with the name "ID" are not of type ID unless so defined.
     * Implementations that do not know whether attributes are of type ID or
     * not are expected to return <code>null</code>.
     * @param elementId The unique <code>id</code> value for an element.
     * @return The matching element.
     * @since DOM Level 2
     *
     */
    public Element getElementById(String elementId) {
        throw new UOException();
    }
    
    /** Returns a <code>NodeList</code> of all the <code>Elements</code> with a
     * given tag name in the order in which they are encountered in a
     * preorder traversal of the <code>Document</code> tree.
     * @param tagname The name of the tag to match on. The special value "*"
     *   matches all tags.
     * @return A new <code>NodeList</code> object containing all the matched
     *   <code>Elements</code>.
     *
     */
    public NodeList getElementsByTagName(String tagname) {
        throw new UOException(); //???
    }
    
    /** Returns a <code>NodeList</code> of all the <code>Elements</code> with a
     * given local name and namespace URI in the order in which they are
     * encountered in a preorder traversal of the <code>Document</code> tree.
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
    
    /** The <code>DOMImplementation</code> object that handles this document. A
     * DOM application may use objects from multiple implementations.
     *
     */
    public DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }
    
    /** The name of this node, depending on its type; see the table above.
     *
     */
    public String getNodeName() {
        return "#document";
    }
    
    /** A code representing the type of the underlying object, as defined above.
     *
     */
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
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
        return null;
    }
    
    /** Imports a node from another document to this document. The returned
     * node has no parent; (<code>parentNode</code> is <code>null</code>).
     * The source node is not altered or removed from the original document;
     * this method creates a new copy of the source node.
     * <br>For all nodes, importing a node creates a node object owned by the
     * importing document, with attribute values identical to the source
     * node's <code>nodeName</code> and <code>nodeType</code>, plus the
     * attributes related to namespaces (<code>prefix</code>,
     * <code>localName</code>, and <code>namespaceURI</code>). As in the
     * <code>cloneNode</code> operation on a <code>Node</code>, the source
     * node is not altered.
     * <br>Additional information is copied as appropriate to the
     * <code>nodeType</code>, attempting to mirror the behavior expected if
     * a fragment of XML or HTML source was copied from one document to
     * another, recognizing that the two documents may have different DTDs
     * in the XML case. The following list describes the specifics for each
     * type of node.
     * <dl>
     * <dt>ATTRIBUTE_NODE</dt>
     * <dd>The <code>ownerElement</code> attribute
     * is set to <code>null</code> and the <code>specified</code> flag is
     * set to <code>true</code> on the generated <code>Attr</code>. The
     * descendants of the source <code>Attr</code> are recursively imported
     * and the resulting nodes reassembled to form the corresponding subtree.
     * Note that the <code>deep</code> parameter has no effect on
     * <code>Attr</code> nodes; they always carry their children with them
     * when imported.</dd>
     * <dt>DOCUMENT_FRAGMENT_NODE</dt>
     * <dd>If the <code>deep</code> option
     * was set to <code>true</code>, the descendants of the source element
     * are recursively imported and the resulting nodes reassembled to form
     * the corresponding subtree. Otherwise, this simply generates an empty
     * <code>DocumentFragment</code>.</dd>
     * <dt>DOCUMENT_NODE</dt>
     * <dd><code>Document</code>
     * nodes cannot be imported.</dd>
     * <dt>DOCUMENT_TYPE_NODE</dt>
     * <dd><code>DocumentType</code>
     * nodes cannot be imported.</dd>
     * <dt>ELEMENT_NODE</dt>
     * <dd>Specified attribute nodes of the
     * source element are imported, and the generated <code>Attr</code>
     * nodes are attached to the generated <code>Element</code>. Default
     * attributes are not copied, though if the document being imported into
     * defines default attributes for this element name, those are assigned.
     * If the <code>importNode</code> <code>deep</code> parameter was set to
     * <code>true</code>, the descendants of the source element are
     * recursively imported and the resulting nodes reassembled to form the
     * corresponding subtree.</dd>
     * <dt>ENTITY_NODE</dt>
     * <dd><code>Entity</code> nodes can be
     * imported, however in the current release of the DOM the
     * <code>DocumentType</code> is readonly. Ability to add these imported
     * nodes to a <code>DocumentType</code> will be considered for addition
     * to a future release of the DOM.On import, the <code>publicId</code>,
     * <code>systemId</code>, and <code>notationName</code> attributes are
     * copied. If a <code>deep</code> import is requested, the descendants
     * of the the source <code>Entity</code> are recursively imported and
     * the resulting nodes reassembled to form the corresponding subtree.</dd>
     * <dt>
     * ENTITY_REFERENCE_NODE</dt>
     * <dd>Only the <code>EntityReference</code> itself is
     * copied, even if a <code>deep</code> import is requested, since the
     * source and destination documents might have defined the entity
     * differently. If the document being imported into provides a
     * definition for this entity name, its value is assigned.</dd>
     * <dt>NOTATION_NODE</dt>
     * <dd>
     * <code>Notation</code> nodes can be imported, however in the current
     * release of the DOM the <code>DocumentType</code> is readonly. Ability
     * to add these imported nodes to a <code>DocumentType</code> will be
     * considered for addition to a future release of the DOM.On import, the
     * <code>publicId</code> and <code>systemId</code> attributes are copied.
     * Note that the <code>deep</code> parameter has no effect on
     * <code>Notation</code> nodes since they never have any children.</dd>
     * <dt>
     * PROCESSING_INSTRUCTION_NODE</dt>
     * <dd>The imported node copies its
     * <code>target</code> and <code>data</code> values from those of the
     * source node.</dd>
     * <dt>TEXT_NODE, CDATA_SECTION_NODE, COMMENT_NODE</dt>
     * <dd>These three
     * types of nodes inheriting from <code>CharacterData</code> copy their
     * <code>data</code> and <code>length</code> attributes from those of
     * the source node.</dd>
     * </dl>
     * @param importedNode The node to import.
     * @param deep If <code>true</code>, recursively import the subtree under
     *   the specified node; if <code>false</code>, import only the node
     *   itself, as explained above. This has no effect on <code>Attr</code>
     *   , <code>EntityReference</code>, and <code>Notation</code> nodes.
     * @return The imported node that belongs to this <code>Document</code>.
     * @exception DOMException
     *   NOT_SUPPORTED_ERR: Raised if the type of node being imported is not
     *   supported.
     * @since DOM Level 2
     *
     */
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        throw new UOException();
    }
    
    /** The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getFirstChild() {
        return getDocumentElement();
    }

    /** The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getLastChild() {
        return getDocumentElement();
    }

    /** Returns whether this node has any children.
     * @return <code>true</code> if this node has any children,
     *   <code>false</code> otherwise.
     *
     */
    public boolean hasChildNodes() {
        return getDocumentElement() != null;
    }
    
    /** A <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     *
     */
    public NodeList getChildNodes() {
        return Wrapper.wrap(peer.getChildNodes());
    }
    
    //
    // Dom Level 3 methods:
    //
    
    public Node adoptNode (Node a) {
        throw new UOException ();
    }
    public String getDocumentURI () {
        throw new UOException ();
    }
    public DOMConfiguration getDomConfig() {
        throw new UOException ();
    }
    public String getInputEncoding() {
        throw new UOException ();
    }
    public boolean getStrictErrorChecking() {
        throw new UOException ();
    }
    public String getXmlEncoding () {
        throw new UOException ();
    }
    public boolean getXmlStandalone() {
        throw new UOException ();
    }
    public String getXmlVersion()  {
        throw new UOException ();
    }
    public void normalizeDocument() {
        throw new UOException ();
    }
    public Node renameNode(Node a, String nb, String c) {
        throw new UOException ();
    }
    public void setDocumentURI(String a) {
        throw new UOException ();
    }
    public void setStrictErrorChecking(boolean a) {
        throw new UOException ();
    }
    public void setXmlStandalone(boolean a) {
        throw new UOException ();
    }
    public void setXmlVersion(String a) {
        throw new UOException ();
    }
}
