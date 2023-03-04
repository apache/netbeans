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

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author marekfukala
 */
public final class GrammarResolverResult {
    
    private Tokenizer tokenizer;
    private boolean inputResolved;
    private List<ResolvedToken> resolvedTokens;    
    private Set<ValueGrammarElement> alternatives;
    private Node parseTreeRoot;

    public GrammarResolverResult(Tokenizer tokenizer, boolean inputResolved, List<ResolvedToken> resolvedTokens, Set<ValueGrammarElement> alternatives, Node parseTreeRoot) {
        this.tokenizer = tokenizer;
        this.inputResolved = inputResolved;
        this.resolvedTokens = resolvedTokens;
        this.alternatives = alternatives;
        this.parseTreeRoot = parseTreeRoot;
    }
    
    /**
     * returns a list of value items not parsed
     */
    public List<Token> left() {
        if(tokenizer.tokensList().isEmpty()) {
            return Collections.emptyList();
        } else {
            return tokenizer.tokensList().subList(tokenizer.tokenIndex(), tokenizer.tokensCount());
        }
    }
    
    public List<Token> tokens() {
        return tokenizer.tokensList();
    }

    public List<ResolvedToken> resolved() {
        return resolvedTokens;
    }

    public boolean success() {
        return inputResolved;
    }

    public Set<ValueGrammarElement> getAlternatives() {
        return alternatives;
    }
    
    public Node getParseTree() {
        return parseTreeRoot;
    }
    
}
