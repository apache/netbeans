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
public class ResultElement extends ResultNode implements Element {

    private Element elem;

    /** Creates a new instance of ResultElement */
    public ResultElement(Element peer, String ignorePrefix, String onlyUsePrefix) {
        super(peer, ignorePrefix, onlyUsePrefix);
        elem = peer;
    }

    public String getAttribute(String name) {
        return elem.getAttribute(name);
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        return elem.getAttributeNS(namespaceURI, localName);
    }
    
    public Attr getAttributeNode(String name) {
        return new ResultAttr(elem.getAttributeNode(name), ignorePrefix, onlyUsePrefix);
    }
    
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        return new ResultAttr(elem.getAttributeNodeNS(namespaceURI,localName), ignorePrefix, onlyUsePrefix);
    }
    
    public NodeList getElementsByTagName(String name) {
        return new ResultNode.ResultNodeList(elem.getElementsByTagName(name));
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        return new ResultNode.ResultNodeList(elem.getElementsByTagNameNS(namespaceURI, localName));
    }
    
    public String getTagName() {
        return elem.getTagName();
    }
    
    public boolean hasAttribute(String name) {
        return elem.hasAttribute(name);
    }
    
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return elem.hasAttributeNS(namespaceURI, localName);
    }
    
    public void removeAttribute(String name) throws DOMException {
        elem.removeAttribute(name);
    }
    
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        elem.removeAttributeNS(namespaceURI,localName);
    }
    
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        return elem.removeAttributeNode(oldAttr);
    }
    
    public void setAttribute(String name, String value) throws DOMException {
        elem.setAttribute(name, value);
    }
    
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
        elem.setAttributeNS(namespaceURI, qualifiedName, value);
    }
    
    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        return elem.setAttributeNode(newAttr);
    }
    
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        return elem.setAttributeNode(newAttr);
    }
    
}
