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
 *
 * @author marekfukala
 */
public abstract class ValueGrammarElement extends GrammarElement {

    public ValueGrammarElement(GroupGrammarElement parent, String elementName) {
        super(parent, elementName);
    }
 
    /**
     * Check if the given text is matched by this {@link GrammarElement}
     * 
     * @since 1.10
     * @param text code to be matched
     * @return true if matches, false otherwise
     */
    public final boolean accepts(CharSequence text) {
        Tokenizer tokenizer = new Tokenizer(text);
        return tokenizer.moveNext() ? accepts(tokenizer.token()) : false;
    }
    
     /**
     * Check if the given {@link Token} is matched by this {@link GrammarElement}
     * 
     * @param token an instance of {@link Token} to be matched
     * @return true if matches, false otherwise
     */
    public abstract boolean accepts(Token token);
    
    /**
     * Returns the value of the element.
     */
    public abstract String getValue();
    
}
