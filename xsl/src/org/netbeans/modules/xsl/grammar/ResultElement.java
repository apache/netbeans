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
