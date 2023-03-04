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
import org.netbeans.modules.xml.spi.dom.ROException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Read-only DOM Comment node.
 *
 * @author Petr Kuzel
 */
public class Comment extends SyntaxNode implements org.w3c.dom.Comment {

    public Comment(XMLSyntaxSupport support, Token<XMLTokenId> from, int start, int end) {
        super( support, from, start, end);
    }

    public String toString() {
        return "Comment" + super.toString() + "<!--" + getData() + "-->";
    }
    
    public String getNodeValue() throws org.w3c.dom.DOMException {
        return getData();  
    }
    
    public String getNodeName() {
        return "#comment";  //NOI18N
    }
    
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }
    
    public Text splitText(int offset) {
        throw new ROException();
    }
 
    /**
     * @return data without delimiters
     */
    public String getData() {
//        String data = first().getImage();  //??? it is always one image
//        return data.substring(("<!--".length() - 1) , (data.length() - "-->".length() -1 ));  //NOI18N
        return null;
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

    
}

