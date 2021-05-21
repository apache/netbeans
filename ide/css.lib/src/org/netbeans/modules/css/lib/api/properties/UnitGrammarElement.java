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
package org.netbeans.modules.css.lib.api.properties;

import java.util.Collection;

/**
 *
 * @author mfukala@netbeans.org
 */
public class UnitGrammarElement extends ValueGrammarElement {

    private final TokenAcceptor tokenAcceptor;
    private final String name;
    
    public UnitGrammarElement(GroupGrammarElement parent, TokenAcceptor tokenAcceptor, String elementName) {
        super(parent, elementName);
        this.tokenAcceptor = tokenAcceptor;
        this.name = new StringBuilder().append('!').append(getTokenAcceptorId()).toString();
    }
    
    public final String getTokenAcceptorId() {
        return tokenAcceptor.id();
    }
    
    /**
    * <b>
    * TODO fix - this is all wrong - there should NOT be getValue() method in the ValueGrammarElement,
    * resp. UnitGrammarElement.
    * Instead of that the ValueGrammarElement should have the getFixedValues() method,
    * which would return just one value for the FixedTextGrammarElement and possibly
    * more here int UnitGrammarElement
    * 
    * as this needs an incompatible change, I'll do that along with others post 7.3
    * </b>
    */
    @Override
    public String getValue() {
        return name;
    }
    
    /**
     * Just temporary - read javadoc for {@link #getValue()}
     * @since 1.25
     * @deprecated
     * @return null if there are no fixed value tokens, or list of the fixed token values
     */
    @Deprecated
    public Collection<String> getFixedValues() {
        return tokenAcceptor.getFixedImageTokens();
    }
    //<<<
    
    @Override
    public boolean accepts(Token token) {
        return tokenAcceptor.accepts(token);
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append(getValue())
                .append(super.toString())
                .toString();
    }

    @Override
    public void accept(GrammarElementVisitor visitor) {
        visitor.visit(this);
    }
    
}
