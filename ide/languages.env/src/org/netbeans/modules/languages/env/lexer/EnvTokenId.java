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
package org.netbeans.modules.languages.env.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;

public enum EnvTokenId implements TokenId {
    COMMENT("comment"),
    KEY("key"),
    STRING("string"),
    KEYWORD("keyword"),
    VALUE("value"),
    DELIMITATOR("delimitator"),
    DOLLAR("dollar"),
    OPERATOR("operator"),
    INTERPOLATION_DELIMITATOR("operator"),
    INTERPOLATION_OPERATOR("interpolation_operator"),
    WS("whitespace"),
    ERROR("error");
    private final String primaryCategory;

    EnvTokenId(String category) {
        this.primaryCategory = category;
    }

    @Override

    public String primaryCategory() {
        return primaryCategory;
    }

    public static abstract class EnvLanguageHierarchy extends LanguageHierarchy<EnvTokenId> {

        @Override
        protected Collection<EnvTokenId> createTokenIds() {
            return EnumSet.allOf(EnvTokenId.class);
        }

        @Override
        protected LanguageEmbedding<? extends TokenId> embedding(Token<EnvTokenId> token,
                LanguagePath languagePath, InputAttributes inputAttributes) {

            return null;
        }
    }
}
