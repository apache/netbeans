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
