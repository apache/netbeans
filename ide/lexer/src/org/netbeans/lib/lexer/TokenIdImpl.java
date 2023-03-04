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
 * Token id implementation.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenIdImpl implements TokenId {

    private final String name;

    private final int ordinal;

    private final String primaryCategory;

    public TokenIdImpl(String name, int ordinal, String primaryCategory) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        if (ordinal < 0) {
            throw new IllegalArgumentException("ordinal=" + ordinal
                + " of token=" + name + " cannot be < 0");
        }
        
        this.name = name;
        this.ordinal = ordinal;
        this.primaryCategory = primaryCategory;
    }
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    
    @Override
    public String toString() {
        return LexerUtilsConstants.idToString(this);
    }

    public String toStringDetail() {
        return name() + "[" + ordinal() + // NOI18N
                (primaryCategory != null ? ", \"" + primaryCategory + "\"" : "") + // NOI18N
                "]";
    }
    
}
