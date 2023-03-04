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
class AttrImpl extends NodeImpl implements Attr {

    private final TreeAttribute peer;

    /** Creates a new instance of AttrImpl */
    public AttrImpl(TreeAttribute peer) {
        this.peer = peer;
    }

    /** The name of this node, depending on its type; see the table above.
     *
     */
    public String getNodeName() {
        return getName();
    }

    /** A code representing the type of the underlying object, as defined above.
     *
     */
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
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
        return getValue();        
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
    
    /** Returns the name of this attribute.
     *
     */
    public String getName() {
        return peer.getQName();
    }
    
    /** The <code>Element</code> node this attribute is attached to or
     * <code>null</code> if this attribute is not in use.
     * @since DOM Level 2
     *
     */
    public Element getOwnerElement() {
        return Wrapper.wrap(peer.getOwnerElement());
    }
    
    /** If this attribute was explicitly given a value in the original
     * document, this is <code>true</code>; otherwise, it is
     * <code>false</code>. Note that the implementation is in charge of this
     * attribute, not the user. If the user changes the value of the
     * attribute (even if it ends up having the same value as the default
     * value) then the <code>specified</code> flag is automatically flipped
     * to <code>true</code>. To re-specify the attribute as the default
     * value from the DTD, the user must delete the attribute. The
     * implementation will then make a new attribute available with
     * <code>specified</code> set to <code>false</code> and the default
     * value (if one exists).
     * <br>In summary: If the attribute has an assigned value in the document
     * then <code>specified</code> is <code>true</code>, and the value is
     * the assigned value.If the attribute has no assigned value in the
     * document and has a default value in the DTD, then
     * <code>specified</code> is <code>false</code>, and the value is the
     * default value in the DTD.If the attribute has no assigned value in
     * the document and has a value of #IMPLIED in the DTD, then the
     * attribute does not appear in the structure model of the document.If
     * the <code>ownerElement</code> attribute is <code>null</code> (i.e.
     * because it was just created or was set to <code>null</code> by the
     * various removal and cloning operations) <code>specified</code> is
     * <code>true</code>.
     *
     */
    public boolean getSpecified() {
        return peer.isSpecified();
    }
    
    /** On retrieval, the value of the attribute is returned as a string.
     * Character and general entity references are replaced with their
     * values. See also the method <code>getAttribute</code> on the
     * <code>Element</code> interface.
     * <br>On setting, this creates a <code>Text</code> node with the unparsed
     * contents of the string. I.e. any characters that an XML processor
     * would recognize as markup are instead treated as literal text. See
     * also the method <code>setAttribute</code> on the <code>Element</code>
     * interface.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     *
     */
    public String getValue() {
        return peer.getValue();
    }
    
    /** On retrieval, the value of the attribute is returned as a string.
     * Character and general entity references are replaced with their
     * values. See also the method <code>getAttribute</code> on the
     * <code>Element</code> interface.
     * <br>On setting, this creates a <code>Text</code> node with the unparsed
     * contents of the string. I.e. any characters that an XML processor
     * would recognize as markup are instead treated as literal text. See
     * also the method <code>setAttribute</code> on the <code>Element</code>
     * interface.
     * @exception DOMException
     *   NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     *
     */
    public void setValue(String value) throws DOMException {
        throw new ROException();
    }
    
}
