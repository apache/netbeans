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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.w3c.dom.*;
import org.netbeans.modules.xml.spi.dom.*;

public class EmptyTag extends Tag {

    public EmptyTag(XMLSyntaxSupport support, Token<XMLTokenId> from, int start, int end) {
        super( support, from, start, end, from.text().toString().substring(1));
    }
    
    public boolean hasChildNodes() {
        return false;
    }

    public NodeList getChildNodes() {
        return NodeListImpl.EMPTY;
    }
    
    public Tag getEndTag() {
        return this;
    }
    
    public Tag getStartTag() {
        return this;
    }

    @Override
    public boolean isStart() {
        return false;
    }

    @Override
    public boolean isEnd() {
        return false;
    }

    @Override
    public boolean isSelfClosing() {
        return true;
    }
    
    public String toString() {
        StringBuffer ret = new StringBuffer( "EmptyTag(\"" + name + "\" " );
        ret.append(getAttributes().toString());
        return ret.toString() + " " + first() + ")";
    }
    
}

