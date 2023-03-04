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

package org.netbeans.lib.lexer;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.token.AbstractToken;

/**
 * Type for having either token or embedding.
 *
 * @author Miloslav Metelka
 */

public interface TokenOrEmbedding<T extends TokenId> {

    /**
     * Get token reference 
     * 
     * @return <code>this</code> if this is a token instance or
     *  a wrapped token if this is an embedding container.
     */
    AbstractToken<T> token();
    
    /**
     * Get non-null embedding container if this is embedding.
     * 
     * @return non-null embedding or null if this is token.
     */
    EmbeddedTokenList<T,?> embedding();

}
