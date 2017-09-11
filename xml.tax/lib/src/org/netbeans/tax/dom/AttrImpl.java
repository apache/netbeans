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
