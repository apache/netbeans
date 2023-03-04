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

/**
 * Wrapping token id that allows to carry information about unsuccessful language embeddings
 * attempted on a token that carries the wrap token id.
 *
 * @author Miloslav Metelka
 */
public final class WrapTokenId<T extends TokenId> {
    
    private final T id;
    
    private final LanguageIds languageIds;
    
    
    public WrapTokenId(T id) {
        this(id, LanguageIds.EMPTY);
    }

    public WrapTokenId(T id, LanguageIds languageIds) {
        this.id = id;
        this.languageIds = languageIds;
    }

    public T id() {
        return id;
    }

    /**
     * Ids of languages for which an unsuccessful embedding attempts were done.
     *
     * @return language ids or null.
     */
    public LanguageIds languageIds() {
        return languageIds;
    }
    
}
