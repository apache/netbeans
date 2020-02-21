/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.cplusplus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;

/**
 *
 */
public class CsmCodeTemplateFilter implements CodeTemplateFilter {
    private static boolean enabled = true;
    
    static void enableAbbreviations(boolean enabled) {
        CsmCodeTemplateFilter.enabled = enabled;
    }
    
    private final int startOffset;
    private final int endOffset;
    private final TokenId id;
    private final boolean statementsSelection;
    
    private CsmCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : startOffset;  
        this.id = getID(component, offset);
        statementsSelection = isStatementSelection(component, offset);
    }

    @Override
    public synchronized boolean accept(CodeTemplate template) {
        return enabled && (startOffset == endOffset || statementsSelection) && isTemplateContext(template);
    }

    private TokenId getID(final JTextComponent component, final int offset) {
        final Document doc = component.getDocument();
        final AtomicReference<TokenId> out = new AtomicReference<TokenId>(CppTokenId.ERROR);
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(component, offset, true, false);
                if (ts != null) {
                    if (ts.offset() <= offset) {
                        if (!ts.movePrevious()) {
                            return;
                        }
                    }
                    out.set(ts.token().id());
                }
            }
        });
        return out.get();
    }

    private boolean isStatementSelection(final JTextComponent component, final int offset) {
        final Document doc = component.getDocument();
        final AtomicBoolean applicableSelection = new AtomicBoolean(false);
        if (startOffset < endOffset) {
            doc.render(new Runnable() {

                @Override
                public void run() {
                    TokenHierarchy<? extends Document> hi = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = hi.tokenSequence();
                    // selection end between tokens?
                    ts.move(endOffset);
                    boolean res = false;
                    if(ts.moveNext()) {
                        Token<?> token = ts.token();
                        int from = ts.offset();
                        if (endOffset == from && CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
                            res = true;
                        }
                    }
                    if (!res) {
                        return;
                    }
                    // it like to end statement?
                    res = false;
                    while(ts.movePrevious()) {
                        Token<?> token = ts.token();
                        if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                            CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                            continue;
                        } else if (token.id() == CppTokenId.RBRACE ||
                                   token.id() == CppTokenId.SEMICOLON) {
                            res = true;
                            break;
                        }
                        res = false;
                        break;
                    }
                    if (!res) {
                        return;
                    }
                    // selection start between tokens?
                    ts.move(startOffset);
                    res = false;
                    if(ts.movePrevious()) {
                        Token<?> token = ts.token();
                        int to = ts.offset()+token.length();
                        if (startOffset == to && CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
                            res = true;
                        }
                    }
                    if (!res) {
                        return;
                    }
                    // it like to start statement?
                    res = false;
                    while(ts.movePrevious()) {
                        Token<?> token = ts.token();
                        if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory()) ||
                            CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                            continue;
                        } else if (token.id() == CppTokenId.LBRACE ||
                                   token.id() == CppTokenId.RBRACE ||
                                   token.id() == CppTokenId.SEMICOLON) {
                            res = true;
                            break;
                        }
                        res = false;
                        break;
                    }
                    if (!res) {
                        return;
                    }
                    // finally count braces balance
                    ts.move(startOffset);
                    int count = 0;
                    while(ts.moveNext()) {
                        Token<?> token = ts.token();
                        if (ts.offset() >= endOffset) {
                            break;
                        }
                        if (token.id() == CppTokenId.LBRACE) {
                            count++;
                        }
                        if (token.id() == CppTokenId.RBRACE) {
                            count--;
                        }
                    }
                    if (count != 0) {
                        return;
                    }
                    applicableSelection.set(true);
                }
            });
        }
        return applicableSelection.get();
    }

    private boolean isTemplateContext(CodeTemplate template) {
        boolean res = true;
        if(this.id instanceof CppTokenId) {
            switch ((CppTokenId)this.id) {
                case DOT:
                case DOTMBR:
                case SCOPE:
                case ARROW:
                case ARROWMBR:
                    res = false;
            }
        }
        return res;
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new CsmCodeTemplateFilter(component, offset);
        }
    }
}
