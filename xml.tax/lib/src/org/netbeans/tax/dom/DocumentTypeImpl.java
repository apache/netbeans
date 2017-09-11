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
