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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;

/**
 * Node that can be used as HintContext. It's used for nested grammars.
 * It can narrow context to given namespace etc.
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultNode implements Node {

    protected Node peer;

    protected String ignorePrefix;

    protected String onlyUsePrefix;

    /** Creates a new instance of ResultNode
     * If onlyUsePrefix is non-null, the result node hirarchy will only have elements
     * with this prefix.
     * If ignorePrefix is non-null and onlyUsePrefix is null, the node hirarchy will
     * not include nodes with this prefix.
     * If both ignorePrefix and onlyUsePrefix are null, the node hirarchy will only
     * include nodes with no prefixes.
     * @peer the peer which this object contains
     * @ignorePrefix a prefix (typically ending with ":") which should be ignored in
     *      this node hirarchy
     * @onlyUsePrefix the prefix which all the nodes in the node hirarchy should have.
     */
    public ResultNode(Node peer, String ignorePrefix, String onlyUsePrefix) {
        this.peer = peer;
        this.ignorePrefix = ignorePrefix;
        this.onlyUsePrefix = onlyUsePrefix;
    }
    
    public Node appendChild(Node newChild) throws DOMException {
        return createNode(peer.appendChild(newChild));
    }
    
    public Node cloneNode(boolean deep) {
        return createNode(peer.cloneNode(deep));
    }
    
    public NamedNodeMap getAttributes() {
        return peer.getAttributes();
    }
    
    public NodeList getChildNodes() {
        return new ResultNodeList(peer.getChildNodes());
    }
    
    public Node getFirstChild() {
        NodeList childNodes = getChildNodes();
        if (childNodes.getLength() == 0) {
            return null;
        } else {
            return childNodes.item(0);
        }
    }
    
    public Node getLastChild() {
        NodeList childNodes = new ResultNodeList(peer.getChildNodes());
        if (childNodes.getLength() == 0) {
            return null;
        } else {
            return childNodes.item(childNodes.getLength()-1);
        }
    }
    
    public String getLocalName() {
        return peer.getLocalName();
    }
    
    public String getNamespaceURI() {
        return peer.getNamespaceURI();
    }
    
    public Node getNextSibling() {
        Node node = peer.getNextSibling();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getNextSibling();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public String getNodeName() {
        return peer.getNodeName();
    }
    
    public short getNodeType() {
        return peer.getNodeType();
    }
    
    public String getNodeValue() throws DOMException {
        return peer.getNodeValue();
    }
    
    public Document getOwnerDocument() {
        return peer.getOwnerDocument();
    }
    
    public Node getParentNode() {
        Node node = peer.getParentNode();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getParentNode();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public String getPrefix() {
        return peer.getPrefix();
    }
    
    public Node getPreviousSibling() {
        Node node = peer.getPreviousSibling();
        while (node != null && node.getNodeName() != null && !hasAllowedPrefix(node.getNodeName())) {
            node = node.getPreviousSibling();
        }
        
        if (node == null) {
            return null;
        } else {
            return createNode(node);
        }
    }
    
    public boolean hasAttributes() {
        return peer.hasAttributes();
    }
    
    public boolean hasChildNodes() {
        return getChildNodes().getLength() > 0;
    }
    
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return createNode(peer.insertBefore(newChild, refChild));
    }
    
    public boolean isSupported(String feature, String version) {
        return peer.isSupported(feature, version);
    }
    
    public void normalize() {
        peer.normalize();
    }
    
    public Node removeChild(Node oldChild) throws DOMException {
        return createNode(peer.removeChild(oldChild));
    }
    
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return createNode(peer.replaceChild(newChild, oldChild));
    }
    
    public void setNodeValue(String nodeValue) throws DOMException {
        peer.setNodeValue(nodeValue);
    }
    
    public void setPrefix(String prefix) throws DOMException {
        peer.setPrefix(prefix);
    }

    /**
     *  Create narrowing result node from given node.
     */
    protected Node createNode(Node orig) {
        if (orig.getNodeType() == Node.ELEMENT_NODE) {
            return new ResultElement((Element)orig, ignorePrefix, onlyUsePrefix);
        } else if (orig.getNodeType() == Node.DOCUMENT_NODE) {
            return new ResultDocument((Document)orig, ignorePrefix, onlyUsePrefix);
        } else if (orig.getNodeType() == Node.ATTRIBUTE_NODE) {
            return new ResultAttr((Attr)orig, ignorePrefix, onlyUsePrefix);
        } else {
            return orig;
        }
    }
    
    /**
     * Returns true if the prefix rules described in the constructor javadocs
     * are fulfilled, otherwise returns false.
     */
    protected boolean hasAllowedPrefix(String name) {
        if (onlyUsePrefix != null) {
            return name.startsWith(onlyUsePrefix);
        } else if (ignorePrefix != null){
            return !name.startsWith(ignorePrefix);
        } else {
            return name.indexOf(':') == -1;
        }
    }
    
    public class ResultNodeList implements NodeList{
        java.util.Vector nodeVector;
        
        public ResultNodeList(NodeList list) {
            nodeVector = new java.util.Vector(list.getLength());
            for (int ind = 0; ind < list.getLength(); ind++) {
                Node node = list.item(ind);
                if (node.getNodeName() != null && hasAllowedPrefix(node.getNodeName())) {
                    nodeVector.add(createNode(node));
                }
            }
        }
        
        public int getLength() {
            return nodeVector.size();
        }
        
        public Node item(int index) {
            return (Node)nodeVector.elementAt(index);
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
    
    // Dom Level 3 methods for Document
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
    
    private static final class UOException extends IllegalStateException {
        
    }
}
