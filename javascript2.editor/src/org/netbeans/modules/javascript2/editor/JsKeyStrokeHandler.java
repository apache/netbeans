/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.KeystrokeHandler;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
class JsKeyStrokeHandler implements KeystrokeHandler {

    public JsKeyStrokeHandler() {
    }

    @Override
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(final ParserResult info, final int caretOffset) {
        final Set<OffsetRange> ranges = new LinkedHashSet();
        if (info instanceof JsParserResult) {
            final JsParserResult jsParserResult = (JsParserResult) info;
            FunctionNode root = jsParserResult.getRoot();
            final TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(jsParserResult.getSnapshot(), caretOffset);
            if (root != null && ts != null) {

                root.accept(new NodeVisitor(new LexicalContext()) {

                    final HashSet<String> referencedFunction = new HashSet();

                    private OffsetRange getOffsetRange(IdentNode node) {
                        // because the truffle parser doesn't set correctly the finish offset, when there are comments after the indent node
                        return new OffsetRange(node.getStart(), node.getStart() + node.getName().length());
                    }

                    private OffsetRange getOffsetRange(Node node) {
                        if (node instanceof FunctionNode) {
                            return getOffsetRange((FunctionNode) node);
                        }
                        if (node instanceof IdentNode) {
                            return getOffsetRange((IdentNode)node);
                        }
                        return new OffsetRange(node.getStart(), node.getFinish());
                    }

                    private  OffsetRange getOffsetRange(FunctionNode node) {
                        return new OffsetRange(com.oracle.js.parser.Token.descPosition(node.getFirstToken()),
                                com.oracle.js.parser.Token.descPosition(node.getLastToken()) + com.oracle.js.parser.Token.descLength(node.getLastToken()));
                    }
                    
                    @Override
                    protected boolean enterDefault(Node node) {
                        OffsetRange range = getOffsetRange(node);
                        if (node != null && range.getStart() <= caretOffset && caretOffset <= range.getEnd()) {
                            ranges.add(new OffsetRange(range.getStart(),range.getEnd()));
                            return super.enterDefault(node);
                        }
                        return false;
                    }

                    @Override
                    public boolean enterFunctionNode(FunctionNode node) {
                        OffsetRange fnRange = getOffsetRange(node);
                        if (node.isProgram()) {
                            ranges.add(new OffsetRange(0, jsParserResult.getSnapshot().getText().length()));
                            if (fnRange.getStart() <= caretOffset && caretOffset <= fnRange.getEnd()) {
                                ranges.add(new OffsetRange(fnRange.getStart(), fnRange.getEnd()));
                            }
                            processFunction(node);
                            return false;
                        }
                                                
                        if (fnRange.getStart() <= caretOffset && caretOffset <= fnRange.getEnd()) {
                            ranges.add(new OffsetRange(fnRange.getStart(), fnRange.getEnd()));
                            int firstParamOffset = fnRange.getEnd();
                            int lastParamOffset = -1;
                            for (Node param : node.getParameters()) {
                                OffsetRange paramRange = getOffsetRange(param);
                                if (param.getStart() < firstParamOffset) {
                                    firstParamOffset = paramRange.getStart();
                                }
                                if (param.getFinish() > lastParamOffset) {
                                    lastParamOffset = paramRange.getEnd();
                                }
                            }
                            if (node.getParameters().size() > 1 && firstParamOffset < lastParamOffset && firstParamOffset <= caretOffset && caretOffset <= lastParamOffset) {
                                ranges.add(new OffsetRange(firstParamOffset, lastParamOffset));
                                for (Node param : node.getParameters()) {
                                    param.accept(this);
                                }
                            }
                            if (fnRange.getStart() <= caretOffset && caretOffset <= fnRange.getEnd()) {
                                ranges.add(new OffsetRange(fnRange.getStart(), fnRange.getEnd()));
                                processFunction(node);
                            }
                        }
                        return false;
                    }

                    private void processFunction(FunctionNode node) {
                        Block body = node.getBody();
                        for (Node statement : body.getStatements()) {
                            OffsetRange stRange = getOffsetRange(statement);
                            if (stRange.getStart() <= caretOffset && caretOffset <= stRange.getEnd()) {
                                statement.accept(this);
                            }
                        }
                    }

                    @Override
                    public boolean enterVarNode(VarNode node) {
                        OffsetRange range = getOffsetRange(node);
                        ts.move(range.getStart());
                        Token<? extends JsTokenId> token = LexUtilities.findPreviousIncluding(ts, Arrays.asList(JsTokenId.KEYWORD_VAR));
                        if (token != null && ts.offset() <= caretOffset && caretOffset <= range.getEnd()) {
                            ranges.add(new OffsetRange(ts.offset(), range.getEnd()));
                            return enterDefault(node);
                        }
                        return false;
                    }

                    @Override
                    public boolean enterLiteralNode(LiteralNode node) {
                        if (node.isString() && node.getStart() <= caretOffset && caretOffset <= node.getFinish()) {
                            // include the " or '
                            ranges.add(new OffsetRange(node.getStart() - 1, node.getFinish() + 1));
                        }
                        return super.enterLiteralNode(node);
                    }

                    @Override
                    public boolean enterCallNode(CallNode node) {
                        if (node.getArgs().size() > 1) {
                            OffsetRange range = getOffsetRange(node);
                            if (range.getStart() <= caretOffset && caretOffset <= range.getEnd()) {
                                ranges.add(range);
                                int firstArgOffset = node.getFinish();
                                int lastArgOffset = -1;
                                for (Node arg : node.getArgs()) {
                                    OffsetRange argRange = getOffsetRange(arg);
                                    if (argRange.getStart() < firstArgOffset) {
                                        firstArgOffset = argRange.getStart();
                                        if (arg instanceof LiteralNode && ((LiteralNode)arg).isString()) {
                                            firstArgOffset--;
                                        }
                                    }
                                    if (argRange.getEnd() > lastArgOffset) {
                                        lastArgOffset = argRange.getEnd();
                                        if (arg instanceof LiteralNode && ((LiteralNode)arg).isString()) {
                                            lastArgOffset++;
                                        }
                                    }
                                }
                                if (firstArgOffset <= caretOffset && caretOffset <= lastArgOffset) {
                                    ranges.add(new OffsetRange(firstArgOffset, lastArgOffset));
                                }
                                for (Node arg : node.getArgs()) {
                                    OffsetRange argRange = getOffsetRange(arg);
                                    if (argRange.getStart() <= caretOffset && caretOffset <= argRange.getEnd()) {
                                        arg.accept(this);
                                    }
                                }
                            }
                            return false;
                        } else {
                            return super.enterCallNode(node);
                        }
                    }

                });
            }
        }

        final ArrayList<OffsetRange> retval = new ArrayList(ranges);
        Collections.reverse(retval);
        return retval;
    }

    @Override
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
