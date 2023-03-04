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
import org.w3c.dom.Node;

/**
 * CDATA section representation.
 */
public class CDATASection extends TextImpl implements org.w3c.dom.CDATASection {

    /**
     * Create content text node.
     */
    public CDATASection(XMLSyntaxSupport support, Token<XMLTokenId> from, int start, int end) {
        super( support, from, start, end);
    }

    @Override
    public String getNodeValue() {
        String text = first().text().toString();
        int start = 0;
        int end = text.length();
        if (text.startsWith("<![CDATA[")) {
            start = 9;
        }
        if (text.endsWith("]]>")) {
            end = text.length() - 3;
        }
        return text.substring(start, end);
    }

//    /**
//     * Create attribute text node.
//     */
//    CDATASection(XMLSyntaxSupport syntax, Token from, Attr parent) {
//        super( syntax, from, parent);
//    }

    @Override
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }
    
}

