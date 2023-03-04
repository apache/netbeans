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

package org.netbeans.lib.lexer.token;

import java.util.EnumSet;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Property provider that stores {@link org.netbeans.api.lexer.PartType} information.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PartTypePropertyProvider implements TokenPropertyProvider<TokenId> {
    
    private static final PartTypePropertyProvider[] partTypeOrdinal2Provider
            = new PartTypePropertyProvider[PartType.class.getEnumConstants().length];

    static {
        for (PartType partType : EnumSet.allOf(PartType.class)) {
            partTypeOrdinal2Provider[partType.ordinal()] = new PartTypePropertyProvider(partType);
        }
    }
    
    public static <T extends TokenId> TokenPropertyProvider<T> get(PartType partType) {
        @SuppressWarnings("unchecked")
        TokenPropertyProvider<T> provider = (TokenPropertyProvider<T>) partTypeOrdinal2Provider[partType.ordinal()];
        return provider;
    }
    
    public static <T extends TokenId> TokenPropertyProvider<T> createDelegating(
            PartType partType, TokenPropertyProvider<T> delegate
    ) {
        return new Delegating<T>(partType, delegate);
    }
    
    private PartType partType;

    public PartTypePropertyProvider(PartType partType) {
        this.partType = partType;
    }

    public Object getValue(Token<TokenId> token, Object key) {
        if (key == PartType.class) {
            return partType;
        }
        return null;
    }
    
    private static final class Delegating<T extends TokenId> implements TokenPropertyProvider<T> {
        
        private final PartType partType;
        
        private final TokenPropertyProvider<T> delegate;
        
        Delegating(PartType partType, TokenPropertyProvider<T> delegate) {
            assert (delegate != null) : "delegate expected to be non-null. Use PartTypePropertyProvider.get() instead."; // NOTICES
            this.partType = partType;
            this.delegate = delegate;
        }

        public Object getValue(Token<T> token, Object key) {
            if (key == PartType.class) {
                return partType;
            }
            return delegate.getValue(token, key);
        }

    }
    
}
