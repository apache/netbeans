/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter.FilterToken;

/**
 *
 */
final class APTFortranEOSFilter implements APTLanguageFilter {
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    public APTFortranEOSFilter() {
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(origStream);
    }

    private static final class FilterStream implements TokenStream {
        private TokenStream orig;
        private Token currentToken;
        boolean newLine = false;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            if(newLine) {
                newLine = false;
                return currentToken;
            }
            Token newToken = orig.nextToken();
            if (currentToken != null) {
                if (currentToken.getType() == APTTokenTypes.EOF) {
                    assert newToken.getType() == APTTokenTypes.EOF;
                    return currentToken;
                } else if (newToken.getType() == APTTokenTypes.EOF || newToken.getLine() != currentToken.getLine()) {
                    Token eos = new EOSToken((APTToken) currentToken);
                    currentToken = newToken;
                    newLine = true;
                    return eos;
                }
            }
            if (newToken.getType() == APTTokenTypes.SEMICOLON) {
                currentToken = newToken;
                return new FilterToken((APTToken)currentToken, APTTokenTypes.T_EOS);
            }
            currentToken = newToken;
            return currentToken;
        }
    }

    static final class EOSToken implements APTToken {

        int offset;
        int endOffset;
        int column;
        int endColumn;
        int line;
        int endLine;
        String fileName;

        EOSToken(APTToken token) {
            offset = token.getEndOffset();
            endOffset = token.getEndOffset();
            column = token.getEndColumn();
            endColumn = token.getEndColumn();
            line = token.getEndLine();
            endLine = token.getEndLine();
            fileName = token.getFilename();
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public void setOffset(int o) {
            offset = o;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndOffset(int o) {
            endOffset = o;
        }

        @Override
        public int getEndColumn() {
            return endColumn;
        }

        @Override
        public void setEndColumn(int c) {
            endColumn = c;
        }

        @Override
        public int getEndLine() {
            return endLine;
        }

        @Override
        public void setEndLine(int l) {
            endLine = l;
        }

        @Override
        public String getText() {
            return "<EOS>"; // NOI18N
        }

        @Override
        public CharSequence getTextID() {
            return "<EOS>"; // NOI18N
        }

        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getColumn() {
            return column;
        }

        @Override
        public void setColumn(int c) {
            column = c;
        }

        @Override
        public int getLine() {
            return line;
        }

        @Override
        public void setLine(int l) {
            line = l;
        }

        @Override
        public String getFilename() {
            return fileName;
        }

        @Override
        public void setFilename(String name) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public void setText(String t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public int getType() {
            return APTTokenTypes.T_EOS;
        }

        @Override
        public void setType(int t) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        @Override
        public Object getProperty(Object key) {
            return null;
        }
    }
}
