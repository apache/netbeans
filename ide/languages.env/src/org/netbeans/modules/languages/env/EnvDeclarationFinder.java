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
package org.netbeans.modules.languages.env;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.DeclarationFinder;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.env.lexer.EnvTokenId;
import org.netbeans.modules.languages.env.parser.EnvParserResult;
import org.openide.filesystems.FileObject;

public class EnvDeclarationFinder implements DeclarationFinder {

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
            if ((token.id().equals(EnvTokenId.KEY))) {
                int start = ts.offset();
                ts.moveNext();

                if (ts.token().id().equals(EnvTokenId.INTERPOLATION_DELIMITATOR)
                        || ts.token().id().equals(EnvTokenId.INTERPOLATION_OPERATOR)) {
                    int end = ts.offset();
                    return new OffsetRange(start, end);
                }

                return OffsetRange.NONE;
            } else {
                return OffsetRange.NONE;
            }
        } finally {
            doc.readUnlock();
        }
    }

    @Override
    public DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
        EnvParserResult result = (EnvParserResult) info;
        TokenSequence<?> ts = info.getSnapshot().getTokenHierarchy().tokenSequence();
        ts.move(caretOffset);
        ts.movePrevious();
        ts.moveNext();
        Token<?> token = ts.token();

        if (!token.id().equals(EnvTokenId.KEY)) {
            return DeclarationLocation.NONE;
        }

        String ref = String.valueOf(token.text());
        FileObject fo = info.getSnapshot().getSource().getFileObject();
        return findKeyDeclaration(result, ref, caretOffset, fo);
    }

    public DeclarationLocation findKeyDeclaration(EnvParserResult result, String key, int caretOffset, FileObject fo) {
        OffsetRange position = result.getDefinedKeys().get(key);

        if (position != null) {
            EnvKeyHandle handle = new EnvKeyHandle(key, fo);
            return new DeclarationFinder.DeclarationLocation(fo, position.getStart(), handle);
        }
        
        return DeclarationLocation.NONE;
    }
}
