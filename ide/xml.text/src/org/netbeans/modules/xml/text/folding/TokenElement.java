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

package org.netbeans.modules.xml.text.folding;

/**
 *
 * @author Samaresh
 */
public class TokenElement {

    private TokenType type;
    private String name;
    private int startOffset;
    private int endOffset;
    private int indentLevel;

    public TokenElement(TokenType type, String name,
            int startOffset, int endOffset, int indentLevel) {
        this.type = type;
        this.name = name;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.indentLevel = indentLevel;
    }

    public TokenType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getIndentLevel() {
        return indentLevel;
    }
    
    public String toString() {
        return type + ", " + name + ", " + startOffset + ", " + endOffset;
    }

    public static enum Token {
        EQUALS_TOKEN("=", TokenType.TOKEN_ATTR_EQUAL), WHITESPACE_TOKEN(" ", TokenType.TOKEN_WHITESPACE),
        CLOSE_ELEMENT(">", TokenType.TOKEN_ELEMENT_END_TAG), //NOI18N

        SELF_CLOSE_ELEMENT("/>", TokenType.TOKEN_ELEMENT_END_TAG), //NOI18N

        CDATA_START("<![CDATA[", TokenType.TOKEN_CDATA_VAL), //NOI18N

        CDATA_END("]]>", TokenType.TOKEN_CDATA_VAL), //NOI18N

        COMMENT_START("<!--", TokenType.TOKEN_COMMENT), //NOI18N

        COMMENT_END("-->", TokenType.TOKEN_COMMENT); //NOI18N

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
        public String toString() {
            return getType() + " '" + value + "'";
        }
        private final String value;
        private final TokenType type;
    }

    public static enum TokenType {
        TOKEN_ELEMENT_NAME,
        TOKEN_ELEMENT_START_TAG,
        TOKEN_ELEMENT_END_TAG,
        TOKEN_ATTR_NAME,
        TOKEN_ATTR_NS,
        TOKEN_ATTR_VAL,
        TOKEN_ATTR_QUOTATION,
        TOKEN_ATTR_EQUAL,
        TOKEN_CHARACTER_DATA,
        TOKEN_WHITESPACE,
        TOKEN_COMMENT,
        TOKEN_COMMENT_TAG,
        TOKEN_PI_START_TAG,
        TOKEN_PI_NAME,
        TOKEN_PI_VAL,
        TOKEN_PI_END_TAG,
        TOKEN_DEC_ATTR_NAME,
        TOKEN_DEC_ATTR_VAL,
        TOKEN_CDATA_VAL,
        TOKEN_DTD_VAL,
        TOKEN_DOC_VAL,
        TOKEN_NS,
        TOKEN_NS_SEPARATOR,
    }
}
