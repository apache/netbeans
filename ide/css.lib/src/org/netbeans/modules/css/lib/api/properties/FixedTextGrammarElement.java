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

import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Represents a fixed value in the property grammar.
 *
 * @author mfukala@netbeans.org
 */
public class FixedTextGrammarElement extends ValueGrammarElement {

    private CharSequence value;
    
    public FixedTextGrammarElement(GroupGrammarElement parent, CharSequence value, String elementName) {
        super(parent, elementName);
        this.value = value;
    }

    @Override
    public boolean accepts(Token token) {
        return LexerUtils.equals(value, token.image(), true, false);
    }
    
    @Override
    public String getValue() {
        return value.toString();
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
