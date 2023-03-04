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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import java.util.*;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.w3c.dom.*;
import org.netbeans.modules.xml.spi.dom.*;

/**
 * End element of ELEMENT_NODE.
 * //??? should it be implementing Node?
 */
public class EndTag extends Tag {

    public EndTag(XMLSyntaxSupport support, Token<XMLTokenId> from, int start, int end) {
        super(support, from, start, end, from.text().toString().substring(2));
        if (name.equals("")) { // NOI18N
            // self-closing tag ? -- TODO
        }
    }

    /**
     * Create properly bound attributes
     */
    public synchronized org.w3c.dom.NamedNodeMap getAttributes() {
        Tag start = getStartTag();
        if (start != null) {
            return start.getAttributes();
        } else {
            return NamedNodeMapImpl.EMPTY;
        }
    }
    
    public boolean hasChildNodes() {
        BaseSyntaxElement prev = getPrevious();
        if (prev == null) return false;
        if (prev instanceof EndTag && ((EndTag)prev).getStartTag() == null) return false;
        if (prev instanceof StartTag) return false;
        return true;
    }
    
    public NodeList getChildNodes() {
        
        List list = new ArrayList();
        Node prev = hasChildNodes() ? findPrevious(this) : null;
        
        while (prev != null) {
            list.add(0, prev);
            prev = prev.getPreviousSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    public Tag getStartTag() {
        
        SyntaxNode prev = findPrevious();
        
        while (prev != null) {
            if (prev instanceof StartTag) {
                // check well-formedness
                StartTag startTag = (StartTag) prev;
                if (startTag.getNodeName().equals(getNodeName())) {
                    return startTag;
                } else {
                    return null;
                }
            } else if (prev instanceof EndTag) {
                EndTag endTag = (EndTag) prev;
                prev = endTag.getStartTag();
                if (prev == null) return null;
                prev = prev.findPrevious();
            } else {
                prev = prev.findPrevious();
            }
        }
        
        return null;
    }
    
    public Tag getEndTag() {
        return this;
    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public boolean isEnd() {
        return true;
    }

    @Override
    public boolean isSelfClosing() {
        return false;
    }

    
    public String toString() {
        return "EndTag(\"" + name + "\") " + first();
    }
    
}

