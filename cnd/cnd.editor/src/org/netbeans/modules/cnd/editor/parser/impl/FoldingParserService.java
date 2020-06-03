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
package org.netbeans.modules.cnd.editor.parser.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.parser.FoldingParser;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = FoldingParser.class, position = 100)
public class FoldingParserService implements FoldingParser {

    @Override
    public List<CppFoldRecord> parse(FileObject fo, char[] buf) {
        String mimeType = fo.getMIMEType();
        Language<CppTokenId> lang = null;
        if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            lang = CppTokenId.languageHeader();
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            lang = CppTokenId.languageCpp();
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            lang = CppTokenId.languageC();
        } else if (MIMENames.PREPROC_MIME_TYPE.equals(mimeType)) {
            lang = CppTokenId.languagePreproc();
        } // TODO: C_HEADER?
        if (lang == null) {
            // Won't parse unknown mime, see Bug 252483
            return Collections.EMPTY_LIST;
        }
        Set<CppTokenId> set = new HashSet<CppTokenId>();
        set.add(CppTokenId.WHITESPACE);
        set.add(CppTokenId.NEW_LINE);
        Reader reader = new StringReader(new String(buf));
        TokenHierarchy<Reader> th = TokenHierarchy.<Reader, CppTokenId>create(
                reader,
                lang,
                set,
                null
        );

        TokenSequence<CppTokenId> tokenSequence = th.tokenSequence(lang);
        List<CppFoldRecord> list = FoldingParserImpl.parse(fo, tokenSequence);
        return list;
    }
}
