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

package org.netbeans.modules.xml.xdm.nodes;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;

/**
 *
 * @author Ajit
 */
public class Comment extends Text implements org.w3c.dom.Comment {
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    Comment() {
        super();
    }

    Comment(String text) {
        this();
        stripCommentMarkers(text);
    }

    private void stripCommentMarkers(String data) {
	// remove start and end markers
	String normalizedData = ""; //NOI18N
	assert data.startsWith(Token.COMMENT_START.getValue()):data;
	if (data.length() > Token.COMMENT_START.getValue().length() +
	    Token.COMMENT_END.getValue().length()) {
	    normalizedData = 
		data.substring(Token.COMMENT_START.getValue().length(),
		data.length() - Token.COMMENT_END.getValue().length());
	}
	setData(normalizedData);
    }
    
    private void addCommentTokens() {
	List<Token> tokens = getTokensForWrite();
	tokens.add(0,Token.COMMENT_START);
	tokens.add(Token.COMMENT_END);
	setTokens(tokens);
    }

    @Override
    public String getNodeValue() {
        return getData();
    }

    @Override
    public void setData(String data) {
	super.setData(data);
	addCommentTokens();
    }
    
    @Override
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }

    @Override
    public String getNodeName() {
        return "#comment"; //NOI18N
    }

}
