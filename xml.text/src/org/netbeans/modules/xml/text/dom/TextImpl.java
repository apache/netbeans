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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.netbeans.api.lexer.Token;
import org.w3c.dom.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * DOM Text implementation. Note that it is automatically
 * coalesced with <code>Text</code> siblings.
 * <p>
 * The implementation handles differently content and attribute
 * text nodes because there is no syntax element for attribute.
 *
 * @author Petr Kuzel
 */
public class TextImpl extends SyntaxNode implements org.w3c.dom.Text {

    // if attribute text node then parent otherwise null
    private AttrImpl parent;
    
    /**
     * Create content text node.
     */
    public TextImpl(XMLSyntaxSupport support, Token from, int start, int end) {
        super( support, from, start, end);
    }
    
    /**
     * Create attribute text node.
     */
    public TextImpl(XMLSyntaxSupport syntax, Token from, int start, int end, AttrImpl parent) {
        super( syntax, from, start, end);
        if (parent == null) throw new IllegalArgumentException();
        this.parent = parent;
    }
    
    /**
     * Get parent node. For content text nodes may be <code>null</code>
     */
    public Node getParentNode() {
        if (parent != null) {
            return parent;
        } else {
            return super.getParentNode();
        }
    }

    public Node getPreviousSibling() {
        if (parent == null) return super.getPreviousSibling();
        return parent.getPreviousSibling(this);
    }
    
    public Node getNextSibling() {
        if (parent == null) return super.getNextSibling();
        return parent.getNextSibling(this);
    }
    
    public short getNodeType() {
        return Node.TEXT_NODE;
    }
    
    public String getNodeValue() {
        return getData();
    }
        
    public TextImpl splitText(int offset) {
        throw new ROException();
    }
 
    public String getData() {
        return first().text().toString();
    }

    public void setData(String data) {
        throw new ROException();
    }
    
    public int getLength() {
        return getData().length();
    }
    
    public String substringData(int offset, int count) {
        return getData().substring(offset, offset + count + 1);
    }

    public void appendData(String arg) {
        throw new ROException();
    }
    
    public void insertData(int offset, String arg) {
        throw new ROException();
    }


    public void deleteData(int offset, int count) {
        throw new ROException();
    }                           

    public void replaceData(int offset, int count, String arg) {
        throw new ROException();
    }

    /**
     * Dump content of the nod efor debug purposes.
     */
    public String toString() {
        return "Text" + super.toString() + " value: '" + getNodeValue() + "'";
    }

    
}

