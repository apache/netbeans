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
package org.netbeans.modules.j2ee.persistence.jpqleditor.lexer;


import java.util.*;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author sp153251
 */
public class JPQLLanguageHierarchy extends LanguageHierarchy<JPQLTokenId> {

    private static Collection<JPQLTokenId> tokens;
    private static Map<Integer, JPQLTokenId> idToToken;

    private static void init() {
        tokens = EnumSet.allOf(JPQLTokenId.class);
    }

    @Override
    protected synchronized Collection<JPQLTokenId> createTokenIds() {
        if (tokens == null) {
            init();
        }
        return tokens;
    }

    @Override
    protected synchronized Lexer<JPQLTokenId> createLexer(LexerRestartInfo<JPQLTokenId> info) {
        return new JPQLLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-jpql";
    }

}