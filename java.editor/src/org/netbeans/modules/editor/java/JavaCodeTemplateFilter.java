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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
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
    
    private static final Logger LOG = Logger.getLogger(JavaCodeTemplateFilter.class.getName());
    private static final String EXPRESSION = "EXPRESSION"; //NOI18N
    private static final String CLASS_HEADER = "CLASS_HEADER"; //NOI18N
    
    private Tree.Kind treeKindCtx = null;
    private String stringCtx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        if (Utilities.isJavaContext(component, offset, true)) {
            final int startOffset = offset;
            final int endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;
            final Source source = Source.create(component.getDocument());
            if (source != null) {
                final AtomicBoolean cancel = new AtomicBoolean();
                ProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(Collections.singleton(source), new UserTask() {
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
                                            case CASE:
                                                if (so < controller.getTrees().getSourcePositions().getEndPosition(controller.getCompilationUnit(), ((CaseTree)tree).getExpression())) {
                                                    treeKindCtx = null;
                                                }
                                                break;
                                            case CLASS:
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
                                            case FOR_LOOP:
                                            case ENHANCED_FOR_LOOP:
                                            case WHILE_LOOP:
                                                sp = controller.getTrees().getSourcePositions();
                                                startPos = (int)sp.getStartPosition(controller.getCompilationUnit(), tree);
                                                String text = controller.getText().substring(startPos, so);
                                                if (!text.trim().endsWith(")")) {
                                                    treeKindCtx = null;
                                                }
                                        }
                                    }
                                }
                            });
                        } catch (ParseException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, NbBundle.getMessage(JavaCodeTemplateProcessor.class, "JCT-init"), cancel, false); //NOI18N
            }
        }
    }

    @Override
    public synchronized boolean accept(CodeTemplate template) {
        if (treeKindCtx == null && stringCtx == null) {
            return false;
        }
        EnumSet<Tree.Kind> treeKindContexts = EnumSet.noneOf(Tree.Kind.class);
        HashSet stringContexts = new HashSet();
        getTemplateContexts(template, treeKindContexts, stringContexts);
        return treeKindContexts.isEmpty() && stringContexts.isEmpty() && treeKindCtx != Tree.Kind.STRING_LITERAL || treeKindContexts.contains(treeKindCtx) || stringContexts.contains(stringCtx);
    }
    
    private void getTemplateContexts(CodeTemplate template, EnumSet<Tree.Kind> treeKindContexts, HashSet<String> stringContexts) {
        List<String> contexts = template.getContexts();
        if (contexts != null) {
            for(String context : contexts) {
                try {
                    treeKindContexts.add(Tree.Kind.valueOf(context));
                } catch (IllegalArgumentException iae) {
                    stringContexts.add(context);
                }
            }
        }
    }

    public static final class Factory implements CodeTemplateFilter.ContextBasedFactory {
        
        @Override
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
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
