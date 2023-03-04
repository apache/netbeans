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

/**
 * Defines whether a default embedding can be present for the given token id or not.
 * <br/>
 * It allows to speed up <code>TokenSequence.embedded()</code> calls considerably in most cases.
 * <br/>
 * This only affects the default embedding creation. Custom embedding creation
 * can always be performed by <code>TokenSequene.createEmbedding()</code>.
 *
 * @author Miloslav Metelka
 */

public enum EmbeddingPresence {

    /**
     * Creation of the default embedding for the particular {@link org.netbeans.api.lexer.TokenId}
     * will be attempted for the first time but if there will be no embedding 
     * created then there will be no other attempts for embedding creation
     * for any tokens with the same token id.
     * <br/>
     * This corresponds to the most usual case that the embedding presence
     * only depends on a token id.
     * <br/>
     * This is the default for {@link LanguageHierarchy#embeddingPresence(org.netbeans.api.lexer.TokenId)}.
     */
    CACHED_FIRST_QUERY,
    
    /**
     * Default embedding creation will always be attempted for each token since
     * the embedding presence varies (it may depend on token's text or other token properties).
     * <br/>
     * For example if a string literal token would only qualify for an embedding
     * if it would contain a '\' character but not otherwise then this method
     * should return true for string literal token id.
     * <br/>
     * This option presents no performance improvement.
     */
    ALWAYS_QUERY,

    /**
     * There is no default embedding for the given {@link org.netbeans.api.lexer.TokenId}
     * and its creation will not be attempted.
     * <br/>
     * This is useful e.g. for keywords and operators.
     */
    NONE,

}
