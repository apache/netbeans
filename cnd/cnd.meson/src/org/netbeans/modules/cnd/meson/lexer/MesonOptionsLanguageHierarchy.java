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

package org.netbeans.modules.cnd.meson.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class MesonOptionsLanguageHierarchy extends LanguageHierarchy<MesonOptionsTokenId> {
    private final String mime;

    public MesonOptionsLanguageHierarchy(String mime) {
        this.mime = mime;
    }

    @Override
    protected synchronized Collection<MesonOptionsTokenId> createTokenIds() {
        return EnumSet.allOf(MesonOptionsTokenId.class);
    }

    @Override
    protected Lexer<MesonOptionsTokenId> createLexer(LexerRestartInfo<MesonOptionsTokenId> info) {
        return new MesonOptionsLexer(info);
    }

    @Override
    protected String mimeType() {
        return mime;
    }
}