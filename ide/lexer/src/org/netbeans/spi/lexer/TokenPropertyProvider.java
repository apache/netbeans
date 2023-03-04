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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;

/**
 * Provides extra properties of a token.
 * <br/>
 * Normally each token has an extra instance of the property provider:
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     private final Object value;
 *
 *     TokenPropProvider(Object value) {
 *         this.value = value;
 *     }
 *      
 *     public Object getValue (Token token, Object key) {
 *         if ("type".equals(key))
 *             return value;
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * However multiple flyweight instances of the provider may be used to save memory
 * if there are just several values for a property.
 * <br/>
 * Example of two instances of a provider for boolean property "key":
 * <pre>
 * final class MyTokenPropertyProvider implements TokenPropertyProvider {
 *
 *     static final MyTokenPropertyProvider TRUE = new MyTokenPropertyProvider(Boolean.TRUE);
 *
 *     static final MyTokenPropertyProvider FALSE = new MyTokenPropertyProvider(Boolean.FALSE);
 * 
 *     private final Boolean value;
 *
 *     private MyTokenPropertyProvider(Boolean value) {
 *         this.value = value;
 *     }
 *
 *     public Object getValue(Token&lt;T&gt; token, Object key) {
 *         if ("key".equals(key)) {
 *             return value;
 *         }
 *         return null;
 *     }
 *
 * }
 * </pre>
 * <br/>
 * A special kind of token <code>PropertyToken</code> allows to carry token properties.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface TokenPropertyProvider<T extends TokenId> {
    
    /**
     * Get value of a token property.
     *
     * @param token non-null token for which the property is being retrieved.
     *  It might be useful if the property would be computed dynamically.
     * @param key non-null key for which the value should be retrieved.
     * @return value of the property or null if there is no value for the given key.
     */
    Object getValue(Token<T> token, Object key);

}
