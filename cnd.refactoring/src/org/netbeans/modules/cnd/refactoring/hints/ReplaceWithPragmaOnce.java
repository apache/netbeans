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
package org.netbeans.modules.cnd.refactoring.hints;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
/**
 *
 */
public class ReplaceWithPragmaOnce implements Fix {
    private final BaseDocument doc;
    private final int guardBlockStart;
    private final int guardBlockEnd;
    
    public ReplaceWithPragmaOnce(Document doc, int guardBlockStart, int guardBlockEnd) {
        this.doc = (BaseDocument) doc;
        this.guardBlockStart = guardBlockStart;
        this.guardBlockEnd = guardBlockEnd;
    }
    
    @Override
    public String getText() {
        return NbBundle.getMessage(ReplaceWithPragmaOnce.class, "HINT_Pragma"); //NOI18N
    }
    
    @Override
    public ChangeInfo implement() throws Exception {
        // get offsets of #ifndef - #define
        Position startPosition = NbDocument.createPosition(doc, guardBlockStart, Position.Bias.Forward);
        Position endPosition = NbDocument.createPosition(doc, guardBlockEnd, Position.Bias.Backward);
        
        // get offsets of #endif
        final AtomicInteger result = new AtomicInteger(-1);
        Runnable runnable = new Runnable () {
            @Override
            public void run() {
                TokenSequence<TokenId> docTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                    if (docTokenSequence == null) {
                        return;
                    }
                    docTokenSequence.moveEnd();
                    while (docTokenSequence.movePrevious()) {
                        if (docTokenSequence.token().id() instanceof CppTokenId) {
                            CppTokenId tokenId = (CppTokenId) docTokenSequence.token().id();
                            if (tokenId.equals(CppTokenId.PREPROCESSOR_DIRECTIVE)) {
                                TokenSequence<CppTokenId> preprocTokenSequence = docTokenSequence.embedded(CppTokenId.languagePreproc());
                                if (preprocTokenSequence == null) {
                                    return;
                                }
                                preprocTokenSequence.moveStart();
                                while (preprocTokenSequence.moveNext()) {
                                    if (preprocTokenSequence.token().id().equals(CppTokenId.PREPROCESSOR_ENDIF)) {
                                        result.set(preprocTokenSequence.offset());
                                        return;
                                    }
                                }
                            } else if (!tokenId.primaryCategory().equals(CppTokenId.WHITESPACE_CATEGORY) 
                                    && !tokenId.primaryCategory().equals(CppTokenId.COMMENT_CATEGORY)) {
                                return;
                            }
                        }
                    }
            }
        };
        
        FutureTask<AtomicInteger> atomicOffset = new FutureTask<>(runnable, result);
        doc.render(atomicOffset);
        
        try {
            int lastOffset = atomicOffset.get().get();
            if (lastOffset != -1) {
                lastOffset--;
                Position startEndifPosition = NbDocument.createPosition(doc, lastOffset, Position.Bias.Forward);
                Position endEndifPosition = NbDocument.createPosition(doc, doc.getLength(), Position.Bias.Backward);
                doc.replace(startPosition.getOffset(), endPosition.getOffset() - startPosition.getOffset(), "#pragma once", null); // NOI18N
                doc.replace(startEndifPosition.getOffset(), endEndifPosition.getOffset() - startEndifPosition.getOffset(), "", null); // NOI18N
            }
        } catch (InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
        return null;
    }
}
