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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.CaseLabelTree;
import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.util.Set;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter {
    
    private static final String EXPRESSION = "EXPRESSION"; //NOI18N
    private static final String CLASS_HEADER = "CLASS_HEADER"; //NOI18N
    
    private Tree.Kind treeKindCtx = null;
    private String stringCtx = null;
    
    private JavaCodeTemplateFilter(Document doc, int startOffset, int endOffset) {
        if (Utilities.isJavaContext(doc, startOffset, true)) {
            final Source source = Source.create(doc);
            if (source != null) {
                final AtomicBoolean cancel = new AtomicBoolean();
                BaseProgressUtils.runOffEventDispatchThread(() -> {
                    try {
                        ParserManager.parse(Set.of(source), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                if (cancel.get()) {
                                    return;
                                }
                                Parser.Result result = resultIterator.getParserResult(startOffset);
                                CompilationController controller = result != null ? CompilationController.get(result) : null;
                                if (controller != null && Phase.PARSED.compareTo(controller.toPhase(Phase.PARSED)) <= 0) {
                                    TreeUtilities tu = controller.getTreeUtilities();
                                    int eo = endOffset;
                                    int so = startOffset;
                                    if (so >= 0) {
                                        so = result.getSnapshot().getEmbeddedOffset(startOffset);
                                    }
                                    if (endOffset >= 0) {
                                        eo = result.getSnapshot().getEmbeddedOffset(endOffset);
                                        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(controller.getTokenHierarchy(), so);
                                        int delta = ts.move(so);
                                        if (delta == 0 || ts.moveNext() && ts.token().id() == JavaTokenId.WHITESPACE) {
                                            delta = ts.move(eo);
                                            if (delta == 0 || ts.moveNext() && ts.token().id() == JavaTokenId.WHITESPACE) {
                                                String selectedText = controller.getText().substring(so, eo).trim();
                                                SourcePositions[] sp = new SourcePositions[1];
                                                ExpressionTree expr = selectedText.length() > 0 ? tu.parseExpression(selectedText, sp) : null;
                                                if (expr != null && expr.getKind() != Tree.Kind.IDENTIFIER && !Utilities.containErrors(expr) && sp[0].getEndPosition(null, expr) >= selectedText.length()) {
                                                    stringCtx = EXPRESSION;
                                                }
                                            }
                                        }
                                    }
                                    Tree tree = tu.pathFor(so).getLeaf();
                                    if (eo >= 0 && so != eo) {
                                        if (tu.pathFor(eo).getLeaf() != tree) {
                                            return;
                                        }
                                    }
                                    treeKindCtx = tree.getKind();
                                    switch (treeKindCtx) {
                                        case CASE: {
                                            if (((CaseTree)tree).getCaseKind() == CaseTree.CaseKind.RULE) {
                                                treeKindCtx = null;
                                            } else {
                                                SourcePositions sp = controller.getTrees().getSourcePositions();
                                                List<? extends CaseLabelTree> labels = ((CaseTree)tree).getLabels();
                                                int startPos = labels.isEmpty() ? (int) sp.getEndPosition(controller.getCompilationUnit(), labels.get(labels.size() - 1))
                                                        : (int)sp.getStartPosition(controller.getCompilationUnit(), tree);
                                                String headerText = controller.getText().substring(startPos, so);
                                                int idx = headerText.indexOf(':');
                                                if (idx < 0) {
                                                    treeKindCtx = null;
                                                }
                                            }
                                            break;
                                        }
                                        case CLASS: {
                                            SourcePositions sp = controller.getTrees().getSourcePositions();
                                            int startPos = (int)sp.getEndPosition(controller.getCompilationUnit(), ((ClassTree)tree).getModifiers());
                                            if (startPos <= 0) {
                                                startPos = (int)sp.getStartPosition(controller.getCompilationUnit(), tree);
                                            }
                                            String headerText = controller.getText().substring(startPos, so);
                                            int idx = headerText.indexOf('{'); //NOI18N
                                            if (idx < 0) {
                                                treeKindCtx = null;
                                                stringCtx = CLASS_HEADER;
                                            }
                                            break;
                                        }
                                        case FOR_LOOP:
                                        case ENHANCED_FOR_LOOP:
                                            if (!isRightParenthesisOfLoopPresent(controller, so)) {
                                                treeKindCtx = null;
                                            }
                                            break;
                                        case PARENTHESIZED:
                                            if (isPartOfWhileLoop(controller, so)) {
                                                if (!isRightParenthesisOfLoopPresent(controller, so)) {
                                                    treeKindCtx = null;
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }, NbBundle.getMessage(JavaCodeTemplateProcessor.class, "JCT-init"), cancel, false); //NOI18N
            }
        }
    }
    
    private boolean isRightParenthesisOfLoopPresent(CompilationController controller, int abbrevStartOffset) {
        TokenHierarchy<?> tokenHierarchy = controller.getTokenHierarchy();
        TokenSequence<?> tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.move(abbrevStartOffset);
        if (tokenSequence.moveNext()) {
            TokenId tokenId = skipNextWhitespaces(tokenSequence);
            return tokenId == null ? false : (tokenId == JavaTokenId.RPAREN);
        }
        return false;
    }
    
    private TokenId skipNextWhitespaces(TokenSequence<?> tokenSequence) {
        TokenId tokenId = null;
        while (tokenSequence.moveNext()) {
            Token<?> token = tokenSequence.token();
            if (token != null) {
                tokenId = token.id();
            }
            if (tokenId != JavaTokenId.WHITESPACE) {
                break;
            }
        }
        return tokenId;
    }
    
    private boolean isPartOfWhileLoop(CompilationController controller, int abbrevStartOffset) {
        TreeUtilities treeUtilities = controller.getTreeUtilities();
        TreePath currentPath = treeUtilities.pathFor(abbrevStartOffset);
        TreePath parentPath = treeUtilities.getPathElementOfKind(Tree.Kind.WHILE_LOOP, currentPath);
        return parentPath != null;
    }

    @Override
    public synchronized boolean accept(CodeTemplate template) {
        if (treeKindCtx == null && stringCtx == null) {
            return false;
        }
        List<String> contexts = template.getContexts();
        if (contexts == null || contexts.isEmpty()) {
            return treeKindCtx != Tree.Kind.STRING_LITERAL;
        } else {
            return (treeKindCtx != null && contexts.contains(treeKindCtx.name()))
                || (stringCtx != null && contexts.contains(stringCtx));
        }
    }

    public static final class Factory implements CodeTemplateFilter.ContextBasedFactory {
        
        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return createFilter(component.getDocument(), offset, component.getSelectionStart() == offset ? component.getSelectionEnd() : -1);
        }

        @Override
        public CodeTemplateFilter createFilter(Document doc, int startOffset, int endOffset) {
            return new JavaCodeTemplateFilter(doc, startOffset, endOffset);
        }

        @Override
        public List<String> getSupportedContexts() {
            Tree.Kind[] values = Tree.Kind.values();
            List<String> contexts = new ArrayList<>(values.length + 1);
            for (Tree.Kind value : values) {
                contexts.add(value.name());
            }
            contexts.add(CLASS_HEADER);
            Collections.sort(contexts);
            return contexts;
        }
    }
}
