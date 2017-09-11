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

package org.netbeans.modules.xml.spi.dom;

import org.w3c.dom.*;

/**
 * Neutral DOM level 1 Core Node implementation.
 * All methods return <code>null</code> or <code>false</code>
 * or throws an exception if they attempt to modify DOM tree
 * or they are defined at higher DOM level. Clone method
 * returns <code>this</code> as it probably safe for read-only DOM.
 * <p>
 * As a bonus it also implements some other DOM interfaces
 * by throwing a DOMException.
 *
 * @author  Petr Kuzel
 */
public abstract class AbstractNode implements Node {

    public String getNodeName() {
        return null;
    }

    /**
     * @return false
     */
    public boolean isSupported(String feature, String version) {
        return "1.0".equals(version);
    }
    
    public void setPrefix(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getPrefix() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }

    public org.w3c.dom.Node getPreviousSibling() {
        return null;
    }
    
    //!!! rather abstract to force all to reimplement
    public abstract short getNodeType();
    
    public org.w3c.dom.Document getOwnerDocument() {
        // let it be the first item
        return null;
    }
    
    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node cloneNode(boolean param) {
        return (Node) this;  //we are immutable, only problem with references may appear
    }
    
    public org.w3c.dom.Node getNextSibling() {
        return null;
    }
    
    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node node1) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getNamespaceURI() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public org.w3c.dom.NamedNodeMap getAttributes() {
        return NamedNodeMapImpl.EMPTY;
    }
    
    public org.w3c.dom.NodeList getChildNodes() {       
        return NodeListImpl.EMPTY;
    }
    
    public String getNodeValue() throws org.w3c.dom.DOMException {
        // attribute, text, pi data
        return null;
    }
    
    public org.w3c.dom.Node appendChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public String getLocalName() {
        return null;    // some client determines DOM1 by NoSuchMethodError
    }
    
    public org.w3c.dom.Node getParentNode() {
        return null;
    }
    
    public void setNodeValue(String str) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Node getLastChild() {
        return null;
    }
    
    public boolean hasAttributes() {
        throw new UOException();    
    }
    
    public void normalize() {
        // ignore
    }
    
    public org.w3c.dom.Node removeChild(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    /**
     * @return false
     */
    public boolean hasChildNodes() {
        return false;
    }
    
    /**
     * @return null
     */
    public org.w3c.dom.Node getFirstChild() {
        return null;
    }
    
    
    // A bonus Element interface ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        throw new UOException();
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public String getAttribute(String name) {
        throw new UOException();        
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new UOException();
    }

    public Attr getAttributeNode(String name) {
        throw new UOException();
    }

    public boolean hasAttribute(String name) {
        throw new UOException();        
    }

    public String getTagName() {
        throw new UOException();        
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public void removeAttribute(String name) throws DOMException {
        throw new UOException();        
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new UOException();        
    }

    public void setAttribute(String name, String value) throws DOMException {
        throw new UOException();        
    }

    public NodeList getElementsByTagName(String name) {
        throw new UOException();        
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        throw new UOException();        
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new UOException();        
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new UOException();        
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        throw new UOException();        
    }

    
    // A bonus Attr implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public boolean getSpecified() {
        throw new UOException();
    }

    public String getName() {
        throw new UOException();                
    }

    public Element getOwnerElement() {
        throw new UOException();                
    }

    public void setValue(String value) throws DOMException {
        throw new UOException();                
    }

    public String getValue() {
        throw new UOException();        
    }

    // Notation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public String getPublicId() {
        throw new UOException();                
    }        

    public String getSystemId() {
        throw new UOException();
    }
    
   
    // Bonus Text implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public void insertData(int offset, String arg) throws DOMException {
        throw new ROException();
    }
    
    public void replaceData(int offset, int count, String arg) throws DOMException {
        throw new ROException();
    }

    public void setData(String data) throws DOMException {
        throw new ROException();
    }

    public Text splitText(int offset) throws DOMException {
        throw new ROException();
    }

    public String substringData(int offset, int count) throws DOMException {
        throw new UOException();
    }
    
    public void appendData(String arg) throws DOMException {
        throw new ROException();        
    }

    public void deleteData(int offset, int count) throws DOMException {
        throw new ROException();        
    }

    public String getData() throws DOMException {
        throw new UOException();        
    }

    public int getLength() {
        return -1; // will likely cause exception if not properly implemented
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
    
}
