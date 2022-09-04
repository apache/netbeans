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
package org.netbeans.modules.languages.antlr;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 *
 * @author lkishalmi
 */
public class AntlrDeclarationFinder implements DeclarationFinder {

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        AntlrParserResult result = (AntlrParserResult) info;
        TokenSequence<?> ts = info.getSnapshot().getTokenHierarchy().tokenSequence();
        ts.move(caretOffset);
        ts.movePrevious();
        ts.moveNext();
        Token<?> token = ts.token();
        String ref = String.valueOf(token.text());
        AntlrParserResult.Reference aref = result.references.get(ref);
        return aref != null ? new DeclarationLocation(aref.source, aref.defOffset.getStart()) : DeclarationLocation.NONE;
    }

    @Override
    public OffsetRange getReferenceSpan(Document document, int caretOffset) {
        AbstractDocument doc = (AbstractDocument) document;
        doc.readLock();
        try {
            TokenHierarchy<Document> th = TokenHierarchy.get(doc);
            TokenSequence<?> ts = th.tokenSequence();
            ts.move(caretOffset);
            ts.movePrevious();
            ts.moveNext();
            Token<?> token = ts.token();
            if ((token.id() == AntlrTokenId.RULE) || (token.id() == AntlrTokenId.TOKEN)) {
                int start = ts.offset();
                ts.moveNext();
                int end = ts.offset();
                return new OffsetRange(start, end);
            } else {
                return OffsetRange.NONE;
            }
        } finally {
            doc.readUnlock();
        }
    }

}
