/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
