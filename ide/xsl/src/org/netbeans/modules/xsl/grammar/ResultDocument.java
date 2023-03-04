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

package org.netbeans.modules.xsl.grammar;

import org.w3c.dom.*;

/**
 *
 * @author  asgeir@dimonsoftware.com
 */
public class ResultDocument extends ResultNode implements org.w3c.dom.Document {

    private Document doc;

    /** Creates a new instance of ResultDocument */
    public ResultDocument(Document peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        doc = peer;
    }

    public Attr createAttribute(String name) throws DOMException {
        return doc.createAttribute(name);
    }
    
    public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createAttributeNS(namespaceURI, qualifiedName);
    }
    
    public CDATASection createCDATASection(String data) throws DOMException {
        return doc.createCDATASection(data);
    }
    
    public Comment createComment(String data) {
        return doc.createComment(data);
    }
    
    public DocumentFragment createDocumentFragment() {
         return doc.createDocumentFragment();
    }
    
    public Element createElement(String tagName) throws DOMException {
        return doc.createElement(tagName);
    }
    
    public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
        return doc.createElementNS(namespaceURI, qualifiedName);
    }
    
    public EntityReference createEntityReference(String name) throws DOMException {
        return doc.createEntityReference(name);
    }
    
    public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException {
        return doc.createProcessingInstruction(target, data);
    }
    
    public Text createTextNode(String data) {
        return doc.createTextNode(data);
    }
    
    public DocumentType getDoctype() {
        return doc.getDoctype();
    }
    
    public Element getDocumentElement() {
        return doc.getDocumentElement();
    }
    
    public Element getElementById(String elementId) {
        return doc.getElementById(elementId);
    }
    
    public NodeList getElementsByTagName(String tagname) {
        return doc.getElementsByTagName(tagname);
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return doc.getElementsByTagNameNS(namespaceURI, localName);
    }
    
    public DOMImplementation getImplementation() {
        return doc.getImplementation();
    }
    
    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        return new ResultNode(doc.importNode(importedNode, deep), ignorePrefix, onlyUsePrefix);
    }
}
