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

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import javax.swing.text.BadLocationException;

public class DocumentImpl extends AbstractNode implements org.w3c.dom.Document {

    SyntaxElement syntax;

    DocumentImpl(SyntaxElement element) {
        syntax = element;
    }

    public org.w3c.dom.Attr createAttribute(String str) throws org.w3c.dom.DOMException {
        return null;
    }

    public org.w3c.dom.Element getElementById(String str) {
        return null;
    }
    
    public String getVersion() {
        throw new UOException();
    }
    
    public org.w3c.dom.Element createElement(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public boolean getStrictErrorChecking() {
        throw new UOException();
    }
    
    public org.w3c.dom.DOMImplementation getImplementation() {
        return new DOMImplementationImpl();
    }
    
    public org.w3c.dom.Element createElementNS(String str, String str1) throws org.w3c.dom.DOMException {
        throw new UOException();
    }
    
    public org.w3c.dom.DocumentFragment createDocumentFragment() {
        return null;
    }
    
    public org.w3c.dom.NodeList getElementsByTagNameNS(String str, String str1) {
        return NodeListImpl.EMPTY;  //???
    }
    
    public void setVersion(String str) {
        throw new UOException();
    }
    
    public org.w3c.dom.Attr createAttributeNS(String str, String str1) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public void setStrictErrorChecking(boolean param) {
        throw new UOException();
    }
    
    public void setEncoding(String str) {
        throw new UOException();
    }
    
    public org.w3c.dom.ProcessingInstruction createProcessingInstruction(String str, String str1) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public org.w3c.dom.NodeList getElementsByTagName(String str) {
        return NodeListImpl.EMPTY;  //???
    }
    
    public org.w3c.dom.Element getDocumentElement() {
        return null;  //!!! parse for it
    }
    
    public org.w3c.dom.DocumentType getDoctype() {
//        try {
            //SyntaxElement e = syntax.support.getElementChain(0);
            //!!! locate declaration and return wrapper
//            return new DocumentTypeImpl(null, null, 0);
//        } catch (BadLocationException ex) {
            return null;
//        }
    }
    
    public org.w3c.dom.CDATASection createCDATASection(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public org.w3c.dom.EntityReference createEntityReference(String str) throws org.w3c.dom.DOMException {
        return null;
    }
    
    public boolean getStandalone() {
        throw new UOException();
    }
    
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }
    
    public org.w3c.dom.Node adoptNode(org.w3c.dom.Node node) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
    public org.w3c.dom.Text createTextNode(String str) {
        return null;
    }
    
    public String getEncoding() {
        throw new UOException();
    }
    
    public org.w3c.dom.Comment createComment(String str) {
        return null;
    }
    
    public void setStandalone(boolean param) {
        throw new UOException();
    }
    
    public org.w3c.dom.Node importNode(org.w3c.dom.Node node, boolean param) throws org.w3c.dom.DOMException {
        throw new ROException();
    }
    
}

