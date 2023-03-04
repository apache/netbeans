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
package org.netbeans.modules.css.lib.api.properties;

import org.netbeans.modules.css.lib.api.CssTokenId;

/**
 * A token serves as an input to the GrammarParser. 
 * 
 * Represents a lexical token of a property value.
 *
 * @author marekfukala
 */
public class Token {
    
    private int offset, length;
    private CssTokenId tokenId;
    private CharSequence tokenizerInput;
    
    public Token(CssTokenId tokenId, int offset, int length, CharSequence tokenizerInput) {
        this.tokenId = tokenId;
        this.offset = offset;
        this.length = length;
        this.tokenizerInput = tokenizerInput;
    }
    
    public CssTokenId tokenId() {
        return tokenId;
    }
    
    public int offset() {
        return offset;
    }
    
    public int length() {
        return length;
    }
    
    public CharSequence image() {
        return tokenizerInput.subSequence(offset, offset + length);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(image());
        sb.append('(');
        sb.append(tokenId);
        sb.append(';');
        sb.append(offset());
        sb.append('-');
        sb.append(offset() + length());
        sb.append(')');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (this.offset != other.offset) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        if (this.tokenId != other.tokenId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.offset;
        hash = 89 * hash + this.length;
        hash = 89 * hash + (this.tokenId != null ? this.tokenId.hashCode() : 0);
        return hash;
    }
    
    
    
}
