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
package org.netbeans.modules.languages.jflex.parsing;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.jflex.JflexLanguage;
import org.netbeans.modules.languages.jflex.lexer.JflexTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

public class JflexParserResult extends ParserResult {

    Set<OffsetRange> embeddedJavaCode = new HashSet<>();

    public JflexParserResult(final Snapshot snapshot) {
        super(snapshot);
    }

    public JflexParserResult runParser() {
        final TokenHierarchy<?> th = getSnapshot().getTokenHierarchy();
        JflexLanguage lang = new JflexLanguage();
        TokenSequence<JflexTokenId> ts = th.tokenSequence(lang.getLexerLanguage());

        while (ts.moveNext()) {
            JflexTokenId tokenId = ts.token().id();
            if (tokenId.equals(JflexTokenId.CODE)) {
                embeddedJavaCode.add(new OffsetRange(ts.offset(), ts.offset() + ts.token().length()));
            }
        }
        return this;
    }

    public Set<OffsetRange> getEmbededJavaCodeOffsets() {
        return embeddedJavaCode;
    }

    @Override
    protected void invalidate() {

    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return Collections.emptyList();
    }
}
