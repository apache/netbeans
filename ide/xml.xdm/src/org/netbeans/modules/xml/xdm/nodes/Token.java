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

/**
 *
 * @author Ajit Bhate
 */
public class Token {
   
    Token(String val, TokenType type) {
	value = val;
	this.type = type;
    }
    
    public String getValue() {
	return value;
    }
    
    public TokenType getType() {
	return type;
    }
    
    @Override
    public int hashCode() {
	return getValue().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Token)) return false;
	Token token = (Token)obj;
	return((token.getValue().equals(getValue())) && 
	       (token.getType().equals(getType())));
    }
    
    @Override
    public String toString() {
	return getType() + " '" + value + "'";
    }
    
    public static Token create(String value, TokenType type) {
	Token t = null;
        switch(type) {
	    case TOKEN_ATTR_EQUAL: {
		t = EQUALS_TOKEN;
		break;
	    }
            case TOKEN_ELEMENT_END_TAG: {
		t = value.length() == 1 ? CLOSE_ELEMENT:SELF_CLOSE_ELEMENT;
		break;
	    }
            case TOKEN_WHITESPACE: {
                if(value.equals(" ")){
                    t = WHITESPACE_TOKEN;
                    break;
                }
            }  
	    default: {
		t = new Token(value.intern(),type);
	    }
	}
	assert t != null;
	return t;
    }
    
    private static final Token EQUALS_TOKEN = 
	new Token("=", TokenType.TOKEN_ATTR_EQUAL); //NOI18N
    
    private static final Token WHITESPACE_TOKEN =
            new Token(" ", TokenType.TOKEN_WHITESPACE);
    
    private static final Token CLOSE_ELEMENT =
	new Token(">", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    private static final Token SELF_CLOSE_ELEMENT =
	new Token("/>", TokenType.TOKEN_ELEMENT_END_TAG); //NOI18N
    
    public static final Token CDATA_START = 
	new Token("<![CDATA[", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token CDATA_END =
	new Token("]]>", TokenType.TOKEN_CDATA_VAL); //NOI18N
    
    public static final Token COMMENT_START = 
	new Token("<!--", TokenType.TOKEN_COMMENT); //NOI18N
    
    public static final Token COMMENT_END =
	new Token("-->", TokenType.TOKEN_COMMENT); //NOI18N
   
    private final String value;
    private final TokenType type;
    //private static Map<String, Token> tokenPool = new HashMap<String, Token>();
}
