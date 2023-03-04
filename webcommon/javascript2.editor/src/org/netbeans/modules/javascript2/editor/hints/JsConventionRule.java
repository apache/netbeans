/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.hints;

import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IfNode;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.ReturnNode;
import com.oracle.js.parser.ir.ThrowNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.WhileNode;

import static com.oracle.js.parser.TokenType.EQ;
import static com.oracle.js.parser.TokenType.NE;

import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.PropertyNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.embedding.JsEmbeddingProvider;
import org.netbeans.modules.javascript2.editor.hints.JsHintsProvider.JsRuleContext;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.model.api.ModelUtils;
import org.netbeans.modules.javascript2.model.spi.PathNodeVisitor;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class JsConventionRule extends JsAstRule {

    private static final List<JsTokenId> IGNORED = new ArrayList<>();

    static {
        Collections.addAll(IGNORED, JsTokenId.BLOCK_COMMENT, JsTokenId.DOC_COMMENT,
                JsTokenId.LINE_COMMENT, JsTokenId.WHITESPACE, JsTokenId.EOL);
    }

    @Override
    void computeHints(JsRuleContext context, List<Hint> hints, int offset, HintsProvider.HintsManager manager) {
        Map<?, List<? extends AstRule>> allHints = manager.getHints();
        List<? extends AstRule> conventionHints = allHints.get(BetterConditionHint.JSCONVENTION_OPTION_HINTS);
        Rule betterConditionRule = null;
        Rule missingSemicolon = null;
        Rule duplicatePropertyName = null;
        Rule assignmentInCondition = null;
        Rule objectTrailingComma = null;
        Rule arrayTrailingComma = null;
        if (conventionHints != null) {
            for (AstRule astRule : conventionHints) {
                if (manager.isEnabled(astRule)) {
                    if (astRule instanceof BetterConditionHint) {
                        betterConditionRule = astRule;
                    } else if (astRule instanceof MissingSemicolonHint) {
                        missingSemicolon = astRule;
                    } else if (astRule instanceof DuplicatePropertyName) {
                        duplicatePropertyName = astRule;
                    } else if (astRule instanceof AssignmentInCondition) {
                        assignmentInCondition = astRule;
                    } else if (astRule instanceof ObjectTrailingComma) {
                        objectTrailingComma = astRule;
                    } else if (astRule instanceof ArrayTrailingComma) {
                        arrayTrailingComma = astRule;
                    }
                }
            }
        }
        ConventionVisitor conventionVisitor = new ConventionVisitor(
                betterConditionRule, missingSemicolon, duplicatePropertyName,
                assignmentInCondition, objectTrailingComma, arrayTrailingComma);
        conventionVisitor.process(context, hints);
    }

    @Override
    public Set<?> getKinds() {
        return Collections.singleton(JsAstRule.JS_OTHER_HINTS);
    }

    @Override
    public String getId() {
        return "jsconvention.hint"; //NOI18N
    }

    @Override
    @NbBundle.Messages("JsConventionHintDesc=JavaScript Code Convention Hint")
    public String getDescription() {
        return Bundle.JsConventionHintDesc();
    }

    @Override
    @NbBundle.Messages("JsConventionHintDisplayName=JavaScript Code Convention")
    public String getDisplayName() {
        return Bundle.JsConventionHintDisplayName();
    }

    private static class ConventionVisitor extends PathNodeVisitor {

        private final Rule betterConditionRule;
        private final Rule missingSemicolon;
        private final Rule duplicatePropertyName;
        private final Rule assignmentInCondition;
        private final Rule objectTrailingComma;
        private final Rule arrayTrailingComma;

        private List<Hint> hints;

        private JsRuleContext context;

        public ConventionVisitor(Rule betterCondition, Rule missingSemicolon,
                Rule duplicatePropertyName, Rule assignmentInCondition,
                Rule objectTrailingComma, Rule arrayTrailingComma) {
            this.betterConditionRule = betterCondition;
            this.missingSemicolon = missingSemicolon;
            this.duplicatePropertyName = duplicatePropertyName;
            this.assignmentInCondition = assignmentInCondition;
            this.objectTrailingComma = objectTrailingComma;
            this.arrayTrailingComma = arrayTrailingComma;
        }

        @NbBundle.Messages({"# {0} - expected char or string",
            "# {1} - usually text, where is expected the first parameter",
            "ExpectedInstead=Expected \"{0}\" and instead saw \"{1}\"."})
        public void process(JsRuleContext context, List<Hint> hints) {
            this.hints = hints;
            this.context = context;
            FunctionNode root = context.getJsParserResult().getRoot();
            if (root != null) {
                context.getJsParserResult().getRoot().accept(this);
            }
        }

        @NbBundle.Messages({"# {0} - char where is expected the semicolon",
            "MissingSemicolon=Expected semicolon ; after \"{0}\"."})
        private void checkSemicolon(int offset) {
            if(missingSemicolon == null) {
                return;
            }
            int fileOffset = context.parserResult.getSnapshot().getOriginalOffset(offset);
            if (fileOffset == -1) {
                return;
            }
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                    context.parserResult.getSnapshot(), offset);
            if (ts == null) {
                return;
            }
            // actually end might mark position after semicolon
            ts.move(offset - 1);
            if (ts.moveNext()) {
                if (ts.token().id() == JsTokenId.OPERATOR_SEMICOLON) {
                    return;
                }
            }
            ts.move(offset);
            if (ts.movePrevious() && ts.moveNext()) {
                JsTokenId id = ts.token().id();
                if (id == JsTokenId.ERROR) {
                    // don't display hints for error tokens.
                    return;
                }
                if ((id == JsTokenId.STRING_END || id == JsTokenId.TEMPLATE_END) && ts.moveNext()) {
                    id = ts.token().id();
                }
                if (id == JsTokenId.EOL) {
                    int position = ts.offset();
                    Token<? extends JsTokenId> next = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                    id = next.id();
                    if (id != JsTokenId.OPERATOR_SEMICOLON && id != JsTokenId.OPERATOR_COMMA && ts.movePrevious()) {
                        ts.move(position);
                        ts.moveNext();
                        id = ts.token().id();
                    }
                }
                if ((id == JsTokenId.EOL || id == JsTokenId.BRACKET_RIGHT_CURLY) && ts.movePrevious()) {
                    id = ts.token().id();
                }
                if (id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT || id == JsTokenId.LINE_COMMENT || id == JsTokenId.WHITESPACE) {
                    int position = ts.offset();
                    //try to find ; or , before
                    Token<? extends JsTokenId> prev = LexUtilities.findPrevious(ts, IGNORED);
                    if (prev != null && (prev.id() == JsTokenId.OPERATOR_SEMICOLON || prev.id() == JsTokenId.OPERATOR_COMMA)) {
                        id = prev.id();
                    } else {
                        ts.move(position);
                        ts.moveNext();

                        //try to find ; or , after
                        Token<? extends JsTokenId> next = LexUtilities.findNext(ts, IGNORED);
                        id = next.id();
                        if (id == JsTokenId.IDENTIFIER || id == JsTokenId.BRACKET_RIGHT_CURLY) {
                           // probably we are at the beginning of the next expression or at the end of the context
                           ts.movePrevious();
                        }
                    }
                }
                if (id != JsTokenId.OPERATOR_SEMICOLON && id != JsTokenId.OPERATOR_COMMA) {
                    Token<? extends JsTokenId> previous = LexUtilities.findPrevious(ts, IGNORED);
                    id = previous.id();
                    // check again whether there is not semicolon and it is not generated
                    if (id != JsTokenId.OPERATOR_SEMICOLON && id != JsTokenId.OPERATOR_COMMA
                            && !JsEmbeddingProvider.isGeneratedIdentifier(previous.text().toString())) {
                        fileOffset = context.parserResult.getSnapshot().getOriginalOffset(ts.offset());
                        if (fileOffset >= 0) {
                            addMissingSemicolonHint(fileOffset, ts.token().text().toString());
                        }
                    }
                }
            } else if (!ts.moveNext() && ts.movePrevious() && ts.moveNext()) {
                int originalOffset = ts.offset();
                CharSequence originalText = ts.token().text();

                Token<? extends JsTokenId> previous = LexUtilities.findPrevious(ts, IGNORED);
                if (previous != null && previous.id() == JsTokenId.OPERATOR_SEMICOLON) {
                    return;
                }
                // we are probably at the end of file without the semicolon
                fileOffset = context.parserResult.getSnapshot().getOriginalOffset(originalOffset);
                addMissingSemicolonHint(fileOffset, originalText.toString());
            }
        }

        private void addMissingSemicolonHint(int offset, String problemText) {
            String correctedText = problemText;
            int index = correctedText.indexOf('\n');
            if (index == 0) {
                index = correctedText.indexOf('\n', 1);
            }
            if ( index > 0 ) {
                correctedText = correctedText.substring(0, index);
            }
             hints.add(new Hint(missingSemicolon, Bundle.MissingSemicolon(correctedText),
                                    context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                                    new OffsetRange(offset, offset + correctedText.length()), null, 500));
        }

        @NbBundle.Messages("AssignmentCondition=Expected a conditional expression and instead saw an assignment.")
        private void checkAssignmentInCondition(Node condition) {
            if (assignmentInCondition == null) {
                return;
            }
            if (condition instanceof BinaryNode) {
                BinaryNode binaryNode = (BinaryNode)condition;
                if (binaryNode.isAssignment()) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(context.parserResult.getSnapshot(), condition.getStart());
                    if (ts == null) {
                        return;
                    }
                    ts.move(condition.getStart());
                    int parenBalance = 0;
                    if (ts.moveNext()) {
                        JsTokenId id = ts.token().id();

                        while ( id != JsTokenId.KEYWORD_IF && id != JsTokenId.KEYWORD_FOR && id != JsTokenId.KEYWORD_WHILE && ts.movePrevious()) {
                            id = ts.token().id();
                            if (id == JsTokenId.BRACKET_RIGHT_PAREN) {
                                parenBalance--;
                            } else if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                                parenBalance++;
                            }
                        }
                    }
                    if (parenBalance == 1) {
                        // 1 -> if ( a = b ) -> hint is valid
                        // > 1 -> if ((a=b)) -> hint is not valid - see https://developer.mozilla.org/ru/docs/Web/JavaScript/Reference/Statements/if...else
                        hints.add(new Hint(assignmentInCondition, Bundle.AssignmentCondition(),
                                context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                                ModelUtils.documentOffsetRange(context.getJsParserResult(), condition.getStart(), condition.getFinish()), null, 500));
                    }
                }
                if (binaryNode.lhs() instanceof BinaryNode) {
                    checkAssignmentInCondition(binaryNode.lhs());
                }
                if (binaryNode.rhs() instanceof BinaryNode) {
                    checkAssignmentInCondition(binaryNode.rhs());
                }
            }
        }

        private void checkCondition(BinaryNode binaryNode) {
            if (betterConditionRule == null) {
                return;
            }
            String message = null;
            switch (binaryNode.tokenType()) {
                case EQ:
                    message = Bundle.ExpectedInstead("===", "=="); //NOI18N
                    break;
                case NE:
                    message = Bundle.ExpectedInstead("!==", "!="); //NOI18N
                    break;
                default:
                    break;
            }
            if (message != null) {
                hints.add(new Hint(betterConditionRule, message,
                    context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                    ModelUtils.documentOffsetRange(context.getJsParserResult(),
                        binaryNode.getStart(), binaryNode.getFinish()), null, 500));
            }

        }

        private enum State  { BEFORE_COLON, AFTER_COLON, AFTER_CURLY, AFTER_PAREN, AFTER_BRACKET};
        @NbBundle.Messages({"# {0} - name of the duplicated property",
            "DuplicateName=Duplicate name of property \"{0}\"."})
        private void checkDuplicateLabels(ObjectNode objectNode) {
            if (duplicatePropertyName == null) {
                return;
            }
            int startOffset = context.parserResult.getSnapshot().getOriginalOffset(objectNode.getStart());
            int endOffset = context.parserResult.getSnapshot().getOriginalOffset(objectNode.getFinish());
            if (startOffset == -1 || endOffset == -1) {
                return;
            }
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(context.parserResult.getSnapshot(), objectNode.getStart());
            if (ts == null) {
                return;
            }
            ts.move(objectNode.getStart());
            State state = State.BEFORE_COLON;
            int curlyBalance = 0;
            int parenBalance = 0;
            int bracketBalance = 0;
            boolean isGetterSetter = false;
            if (ts.movePrevious() && ts.moveNext()) {
                HashSet<String> names = new HashSet<>();
                while (ts.moveNext() && ts.offset() < objectNode.getFinish()) {
                    JsTokenId id = ts.token().id();
                    switch (state) {
                        case BEFORE_COLON:
                            if (id == JsTokenId.IDENTIFIER || id == JsTokenId.STRING) {
                                String name = ts.token().text().toString();
                                if (!context.getJsParserResult().isEmbedded() || !JsEmbeddingProvider.isGeneratedIdentifier(name)) {
                                    if ("set".equals(name) || "get".equals(name)) { // NOI18N
                                        isGetterSetter = true;
                                    } else if (!names.add(name) && !isGetterSetter) {
                                        int docOffset = context.parserResult.getSnapshot().getOriginalOffset(ts.offset());
                                        if (docOffset >= 0) {
                                            hints.add(new Hint(duplicatePropertyName, Bundle.DuplicateName(name),
                                                    context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                                                    new OffsetRange(docOffset, docOffset + ts.token().length()), null, 500));
                                        }
                                    }
                                }
                            } else if (id == JsTokenId.OPERATOR_COLON) {
                                state = State.AFTER_COLON;
                            }  else if (id == JsTokenId.BRACKET_LEFT_CURLY) {
                                state = State.AFTER_CURLY;
                                isGetterSetter = false;
                            } else if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                                state = State.AFTER_PAREN;
                            } else if (id == JsTokenId.BRACKET_LEFT_BRACKET) {
                                state = State.AFTER_BRACKET;
                            }
                            break;
                        case AFTER_COLON:
                            if (id == JsTokenId.OPERATOR_COMMA) {
                                state = State.BEFORE_COLON;
                            } else if (id == JsTokenId.BRACKET_LEFT_CURLY) {
                                state = State.AFTER_CURLY;
                            } else if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                                state = State.AFTER_PAREN;
                            } else if (id == JsTokenId.BRACKET_LEFT_BRACKET) {
                                state = State.AFTER_BRACKET;
                            }
                            break;
                        case AFTER_CURLY:
                            if (id == JsTokenId.BRACKET_LEFT_CURLY) {
                                curlyBalance++;
                            } else if (id == JsTokenId.BRACKET_RIGHT_CURLY) {
                                if (curlyBalance == 0) {
                                    state = State.AFTER_COLON;
                                } else {
                                    curlyBalance--;
                                }
                            }
                            break;
                        case AFTER_PAREN :
                            if (id == JsTokenId.BRACKET_LEFT_PAREN) {
                                parenBalance++;
                            } else if (id == JsTokenId.BRACKET_RIGHT_PAREN) {
                                if (parenBalance == 0) {
                                    state = State.AFTER_COLON;
                                } else {
                                    parenBalance--;
                                }
                            }
                            break;
                       case AFTER_BRACKET :
                            if (id == JsTokenId.BRACKET_LEFT_BRACKET) {
                                bracketBalance++;
                            } else if (id == JsTokenId.BRACKET_RIGHT_BRACKET) {
                                if (bracketBalance == 0) {
                                    state = State.AFTER_COLON;
                                } else {
                                    bracketBalance--;
                                }
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public boolean enterForNode(ForNode forNode) {
            if (forNode.getTest() != null) {
                checkAssignmentInCondition(forNode.getTest().getExpression());
            }
            return super.enterForNode(forNode);
        }

        @Override
        public boolean enterIfNode(IfNode ifNode) {
            checkAssignmentInCondition(ifNode.getTest());
            return super.enterIfNode(ifNode);
        }

        @Override
        public boolean enterWhileNode(WhileNode whileNode) {
            checkAssignmentInCondition(whileNode.getTest().getExpression());
            return super.enterWhileNode(whileNode);
        }

        @Override
        public boolean enterExpressionStatement(ExpressionStatement expressionStatement) {
            Block block = getLexicalContext().getCurrentBlock();
            if (block == null || !block.isParameterBlock()) {
                checkSemicolon(expressionStatement.getFinish());
            }
            return super.enterExpressionStatement(expressionStatement);
        }

        @Override
        public boolean enterThrowNode(ThrowNode throwNode) {
            checkSemicolon(throwNode.getExpression().getFinish());
            return super.enterThrowNode(throwNode);
        }



        @Override
        @NbBundle.Messages({"# {0} - the eunexpected token",
            "UnexpectedObjectTrailing=Unexpected \"{0}\"."})
        public boolean enterObjectNode(ObjectNode objectNode) {
            checkDuplicateLabels(objectNode);
            if (objectTrailingComma != null) {
                int offset = context.parserResult.getSnapshot().getOriginalOffset(objectNode.getFinish());
                if (offset > -1) {
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                            context.parserResult.getSnapshot(), objectNode.getFinish());
                    if (ts == null) {
                        return super.enterObjectNode(objectNode);
                    }
                    ts.move(objectNode.getFinish());
                    if (ts.movePrevious() && ts.moveNext() && ts.movePrevious()) {
                        LexUtilities.findPrevious(ts, Arrays.asList(
                                JsTokenId.EOL, JsTokenId.WHITESPACE,
                                JsTokenId.BRACKET_RIGHT_CURLY, JsTokenId.LINE_COMMENT,
                                JsTokenId.BLOCK_COMMENT, JsTokenId.DOC_COMMENT));
                        if (ts.token().id() == JsTokenId.OPERATOR_COMMA) {
                            offset = context.parserResult.getSnapshot().getOriginalOffset(ts.offset());
                            if (offset >= 0) {
                                hints.add(new Hint(objectTrailingComma, Bundle.UnexpectedObjectTrailing(ts.token().text().toString()),
                                        context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                                        new OffsetRange(offset, offset + ts.token().length()), null, 500));
                            }
                        }
                    }
                }
            }
            return super.enterObjectNode(objectNode);
        }

        @Override
        @NbBundle.Messages({"# {0} - the eunexpected token",
            "UnexpectedArrayTrailing=Unexpected \"{0}\"."})
        public boolean enterLiteralNode(LiteralNode literalNode) {
            if (arrayTrailingComma != null) {
                if (literalNode.getValue() instanceof Node[]) {
                    Node previous = getPath().get(getPath().size() - 1);
                    if (previous instanceof BinaryNode && ((BinaryNode) previous).lhs() == literalNode) {
                        // destructuring assignment
                        return super.enterLiteralNode(literalNode);
                    }
                    int offset = context.parserResult.getSnapshot().getOriginalOffset(literalNode.getFinish());
                    if (offset > -1) {
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                                context.parserResult.getSnapshot(), literalNode.getFinish());
                        if (ts == null) {
                            return super.enterLiteralNode(literalNode);
                        }
                        ts.move(literalNode.getFinish());
                        if (ts.movePrevious() && ts.moveNext() && ts.movePrevious()) {
                            LexUtilities.findPrevious(ts, Arrays.asList(
                                    JsTokenId.EOL, JsTokenId.WHITESPACE,
                                    JsTokenId.BRACKET_RIGHT_BRACKET, JsTokenId.LINE_COMMENT,
                                    JsTokenId.BLOCK_COMMENT, JsTokenId.DOC_COMMENT));
                            if (ts.token().id() == JsTokenId.OPERATOR_COMMA) {
                                offset = context.parserResult.getSnapshot().getOriginalOffset(ts.offset());
                                if (offset >= 0) {
                                    hints.add(new Hint(arrayTrailingComma, Bundle.UnexpectedArrayTrailing(ts.token().text().toString()),
                                            context.getJsParserResult().getSnapshot().getSource().getFileObject(),
                                            new OffsetRange(offset, offset + ts.token().length()), null, 500));
                                }
                            }
                        }
                    }
                }
            }
            return super.enterLiteralNode(literalNode);
        }

        @Override
        public boolean enterVarNode(VarNode varNode) {
            boolean check = true;
            Node previous = getPath().get(getPath().size() - 1);
            if (previous instanceof Block) {
                Block block = (Block) previous;
                if (block.getStatements().size() == 2 && block.getStatements().get(1) instanceof ForNode) {
                    check = false;
                }
            } else if (previous instanceof ForNode) {
                check = false;
            }

            if (varNode.isFunctionDeclaration() || varNode.isExport() || varNode.isDestructuring()) {
                check = false;
            }
            if (varNode.getInit() instanceof ClassNode) {
                IdentNode cIdent = ((ClassNode) varNode.getInit()).getIdent();
                IdentNode vIdent = varNode.getName();
                // this is artificial var node for simple class declaration
                if (cIdent != null
                        && cIdent.getStart() == vIdent.getStart()
                        && cIdent.getFinish() == vIdent.getFinish()) {
                    check = false;
                }
            }

            if (check) {
                checkSemicolon(varNode.getFinish());
            }
            return super.enterVarNode(varNode);
        }

        @Override
        public boolean enterReturnNode(ReturnNode returnNode) {
            FunctionNode function = getLexicalContext().getCurrentFunction();
            if (function == null || function.getKind() != FunctionNode.Kind.ARROW
                    || com.oracle.js.parser.Token.descType(function.getBody().getToken()) == TokenType.LBRACE) {
                // if it is arrow without a real block & return
                checkSemicolon(returnNode.getFinish());
            }
            return super.enterReturnNode(returnNode);
        }

        @Override
        public boolean enterBinaryNode(BinaryNode binaryNode) {
            checkCondition(binaryNode);
            return super.enterBinaryNode(binaryNode);
        }

        @Override
        public boolean enterPropertyNode(PropertyNode propertyNode) {
            ClassNode enclosingClass = classDefinitionHierarchie.isEmpty() ? null : classDefinitionHierarchie.get(classDefinitionHierarchie.size() - 1);
            if(enclosingClass != null && propertyNode.getToken() == enclosingClass.getToken()) {
                return false;
            }
            return super.enterPropertyNode(propertyNode);
        }

        private List<ClassNode> classDefinitionHierarchie = new ArrayList<>();

        @Override
        public boolean enterClassNode(ClassNode classNode) {
            classDefinitionHierarchie.add(classNode);
            return super.enterClassNode(classNode);
        }

        @Override
        public Node leaveClassNode(ClassNode classNode) {
            classDefinitionHierarchie.remove(classDefinitionHierarchie.size() - 1);
            return super.leaveClassNode(classNode);
        }
    }
}
