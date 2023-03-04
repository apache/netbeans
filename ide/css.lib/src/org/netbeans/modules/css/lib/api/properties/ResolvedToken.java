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

/**
 * Represents a resolved {@link Token} after a property value parsing.
 *
 * @author marekfukala
 */
public class ResolvedToken {
    
    private Token token;
    private ValueGrammarElement grammarElement;

    public ResolvedToken(Token token, ValueGrammarElement grammarElement) {
        this.token = token;
        this.grammarElement = grammarElement;
    }

    public Token token() {
        return token;
    }

    public ValueGrammarElement getGrammarElement() {
        return grammarElement;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(grammarElement.path()).append(" (").append(token()).append(")").toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResolvedToken other = (ResolvedToken) obj;
        if ((this.token == null) ? (other.token != null) : !this.token.equals(other.token)) {
            return false;
        }
        if (this.grammarElement != other.grammarElement && (this.grammarElement == null || !this.grammarElement.equals(other.grammarElement))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.token != null ? this.token.hashCode() : 0);
        hash = 97 * hash + (this.grammarElement != null ? this.grammarElement.hashCode() : 0);
        return hash;
    }
    
    
    
}
