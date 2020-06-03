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
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 */
public class ShiftedTokenStream implements TokenStream {
    
    private final TokenStream tokenStream;
    
    private final int offset;

    
    public ShiftedTokenStream(TokenStream tokenStream, int offset) {
        this.tokenStream = tokenStream;
        this.offset = offset;
    }

    @Override
    public Token nextToken() throws TokenStreamException {
        Token token = tokenStream.nextToken();               
        if (token instanceof APTToken) {
            token = new ShiftedToken((APTToken) token, offset);
        }
        return token;
    }
    
    
    private static class ShiftedToken implements APTToken {
        
        private final APTToken aptToken;
        
        private final int startOffset;
        
        private final int endOffset;

        
        public ShiftedToken(APTToken aptToken, int shiftOffset) {
            this.aptToken = aptToken;
            this.startOffset = aptToken.getOffset() + shiftOffset;
            this.endOffset = aptToken.getEndOffset() + shiftOffset;
        }
        
        @Override
        public int getOffset() {
            return startOffset;
        }

        @Override
        public void setOffset(int o) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public void setEndOffset(int o) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndColumn() {
            return aptToken.getEndColumn();
        }

        @Override
        public void setEndColumn(int c) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getEndLine() {
            return aptToken.getEndLine();
        }

        @Override
        public void setEndLine(int l) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public String getText() {
            return aptToken.getText();
        }

        @Override
        public CharSequence getTextID() {
            return aptToken.getTextID();
        }

        @Override
        public void setTextID(CharSequence id) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public Object getProperty(Object key) {
            return aptToken.getProperty(key);
        }        

        @Override
        public int getColumn() {
            return aptToken.getColumn();
        }

        @Override
        public void setColumn(int c) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getLine() {
            return aptToken.getLine();
        }

        @Override
        public void setLine(int l) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public String getFilename() {
            return aptToken.getFilename();
        }

        @Override
        public void setFilename(String name) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public void setText(String t) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }

        @Override
        public int getType() {
            return aptToken.getType();
        }

        @Override
        public void setType(int t) {
            throw new UnsupportedOperationException("Not supported!"); // NOI18N
        }                
    }
    
}
