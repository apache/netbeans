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
package org.netbeans.modules.languages.go;

import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author lkishalmi
 */
public enum GoTokenId implements TokenId{

    COMMENT("comment"),
    ERROR("error"),
    IDENTIFIER("identifier"),
    KEYWORD("keyword"),
    NUMBER("number"),
    OPERATOR("operator"),
    SEPARATOR("separator"),
    STRING("string"),
    WHITESPACE("whitespace");

    private final String category;

    GoTokenId(String category) {
        this.category = category;
    }

    @Override
    public String primaryCategory() {
        return category;
    }

}
