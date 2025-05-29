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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
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
    @SuppressWarnings("deprecation")
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return -1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    @Override
    public List<OffsetRange> findLogicalRanges(final ParserResult info, final int caretOffset) {
        final Set<OffsetRange> ranges = new LinkedHashSet<>();
        if (info instanceof JsParserResult jsParserResult) {
            FunctionNode root = jsParserResult.getRoot();
            final TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(jsParserResult.getSnapshot(), caretOffset);
            if (root != null && ts != null) {

                root.accept(new NodeVisitor<LexicalContext>(new LexicalContext()) {

                    private OffsetRange getOffsetRange(IdentNode node) {
                        // because the truffle parser doesn't set correctly the finish offset, when there are comments after the indent node
                        return new OffsetRange(node.getStart(), node.getStart() + node.getName().length());
                    }

                    private OffsetRange getOffsetRange(Node node) {
                        Objects.requireNonNull(node);
                        if (node instanceof FunctionNode functionNode) {
                            return getOffsetRange(functionNode);
                        } else if (node instanceof IdentNode identNode) {
                            return getOffsetRange(identNode);
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
                    public boolean enterLiteralNode(LiteralNode<?> node) {
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

        final ArrayList<OffsetRange> retval = new ArrayList<>(ranges);
        Collections.reverse(retval);
        return retval;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
