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
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import java.util.*;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;

public class StartTag extends Tag {

    public StartTag(XMLSyntaxSupport support, TokenItem from, int to, String name, Collection attribs) {
        super( support, from, to, name, attribs );
    }

    public boolean hasChildNodes() {
        SyntaxElement next = getNext();
        if (next == null) return false;
        // if not well-formed
        if (next instanceof StartTag && ((StartTag)next).getEndTag() == null) return false;
        if (next instanceof EndTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node next = hasChildNodes() ? findNext(this) : null;
        
        while (next != null) {
            list.add(next);
            next = next.getNextSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    protected Tag getStartTag() {
        return this;
    }
    
    protected Tag getEndTag() {
        
        SyntaxNode next = findNext();
        
        while (next != null) {
            if (next instanceof EndTag) {
                // check well-formedness
                EndTag endTag = (EndTag) next;
                if (endTag.getTagName().equals(getTagName())) {
                    return endTag;
                } else {
                    return null;
                }
            } else if (next instanceof StartTag) {
                Tag startTag = (Tag) next;
                next = startTag.getEndTag();
                if (next == null) return null;
                next = next.findNext();
            } else {
                next = next.findNext();
            }
        }
        
        return null;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer( "StartTag(\"" + name + "\" " );
        ret.append(getAttributes().toString());
        return ret.toString() + " " + first() + ")";
    }
    
}

