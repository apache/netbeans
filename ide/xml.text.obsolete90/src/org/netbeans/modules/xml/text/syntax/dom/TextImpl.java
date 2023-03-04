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

import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

/**
 * DOM Text implementation. Note that it is automatically
 * coalesced with <code>Text</code> siblings.
 * <p>
 * The implementation handles differently content and attribute
 * text nodes because there is no syntax element for attribute.
 *
 * @author Petr Kuzel
 */
public class TextImpl extends SyntaxNode implements Text {

    // if attribute text node then parent otherwise null
    private AttrImpl parent;
    
    /**
     * Create content text node.
     */
    public TextImpl(XMLSyntaxSupport support, TokenItem from, int to) {
        super( support, from, to );
    }
    
    /**
     * Create attribute text node.
     */
    TextImpl(XMLSyntaxSupport syntax, TokenItem from, AttrImpl parent) {
        super( syntax, from, 0);
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
        
    public Text splitText(int offset) {
        throw new ROException();
    }
 
    public String getData() {
        return first().getImage();
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

