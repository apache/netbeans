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
package org.netbeans.modules.cnd.makefile.lexer;

import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import java.util.Collection;

import java.util.EnumSet;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class MakefileLanguageHierarchy extends LanguageHierarchy<MakefileTokenId> {

    @Override
    protected Collection<MakefileTokenId> createTokenIds() {
        return EnumSet.allOf(MakefileTokenId.class);
    }

    @Override
    protected Lexer<MakefileTokenId> createLexer(LexerRestartInfo<MakefileTokenId> info) {
        return new MakefileLexer(info);
    }

    @Override
    protected String mimeType() {
        return MIMENames.MAKEFILE_MIME_TYPE;
    }
}
