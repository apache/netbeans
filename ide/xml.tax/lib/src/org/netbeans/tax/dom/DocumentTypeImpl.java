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
 * @author  Rich Unger
 */
class DocumentTypeImpl extends NodeImpl implements DocumentType {

    private final TreeDocumentType peer;

    /** Creates a new instance of DocumentTypeImpl */
    public DocumentTypeImpl(TreeDocumentType peer) {
        this.peer = peer;
    }

    public String getName() {
        return peer.getElementName();
    }

    public NamedNodeMap getEntities() { 
        return null;
    } 

    public NamedNodeMap getNotations() { 
        return null;
    } 

    public String getPublicId() { 
        return peer.getPublicId();
    } 

    public String getSystemId() { 
        return peer.getSystemId();
    } 

    public String getInternalSubset() { 
        return null;
    } 

    /** The name of this node, depending on its type; see the table above.
     *
     */
    public String getNodeName() {
        return peer.getElementName();
    }
    
    /** A code representing the type of the underlying object, as defined above.
     *
     */
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
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
    
    /** The first child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getFirstChild() {
        return null;
    }

    /** The last child of this node. If there is no such node, this returns
     * <code>null</code>.
     *
     */
    public Node getLastChild() {
        return null;
    }

    /** Returns whether this node has any children.
     * @return <code>true</code> if this node has any children,
     *   <code>false</code> otherwise.
     *
     */
    public boolean hasChildNodes() {
        return false;
    }
    
    /** A <code>NodeList</code> that contains all children of this node. If
     * there are no children, this is a <code>NodeList</code> containing no
     * nodes.
     *
     */
    public NodeList getChildNodes() {
        return Wrapper.wrap(peer.getChildNodes());
    }
    
}
