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
package org.netbeans.modules.languages.yaml;

import org.netbeans.api.lexer.TokenId;

/**
 * Token type definitions for YAML
 *
 * @author Tor Norbye
 */
public enum YamlTokenId implements TokenId {

    TEXT("identifier"),
    COMMENT("comment"),
    /**
     * Contents inside <%# %>
     */
    RUBYCOMMENT("comment"),
    /**
     * Contents inside <%= %>
     */
    RUBY_EXPR("ruby"),
    /**
     * Contents inside <% %>
     */
    RUBY("ruby"),
    /**
     * <% or %>
     */
    DELIMITER("ruby-delimiter"),
    PHP("php"),
    MUSTACHE("mustache"),
    MUSTACHE_DELIMITER("mustache-delimiter");
    private final String primaryCategory;

    YamlTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public static boolean isRuby(TokenId id) {
        return id == RUBY || id == RUBY_EXPR || id == RUBYCOMMENT;
    }

}
