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
package org.netbeans.modules.javascript2.editor.formatter;

import com.oracle.js.parser.ir.AccessNode;
import com.oracle.js.parser.ir.BinaryNode;
import com.oracle.js.parser.ir.Block;
import com.oracle.js.parser.ir.BlockStatement;
import com.oracle.js.parser.ir.CallNode;
import com.oracle.js.parser.ir.CaseNode;
import com.oracle.js.parser.ir.CatchNode;
import com.oracle.js.parser.ir.Expression;
import com.oracle.js.parser.ir.ForNode;
import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.IfNode;
import com.oracle.js.parser.ir.LexicalContext;
import com.oracle.js.parser.ir.LiteralNode;
import com.oracle.js.parser.ir.LoopNode;
import com.oracle.js.parser.ir.Node;
import com.oracle.js.parser.ir.ObjectNode;
import com.oracle.js.parser.ir.PropertyNode;
import com.oracle.js.parser.ir.Statement;
import com.oracle.js.parser.ir.SwitchNode;
import com.oracle.js.parser.ir.TernaryNode;
import com.oracle.js.parser.ir.TryNode;
import com.oracle.js.parser.ir.UnaryNode;
import com.oracle.js.parser.ir.VarNode;
import com.oracle.js.parser.ir.WhileNode;
import com.oracle.js.parser.ir.WithNode;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.ClassNode;
import com.oracle.js.parser.ir.ExportNode;
import com.oracle.js.parser.ir.ExpressionStatement;
import com.oracle.js.parser.ir.ImportNode;
import com.oracle.js.parser.ir.JsxAttributeNode;
import com.oracle.js.parser.ir.JsxElementNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatVisitor extends NodeVisitor {

    private static final Set<TokenType> UNARY_TYPES = EnumSet.noneOf(TokenType.class);

    static {
        Collections.addAll(UNARY_TYPES, TokenType.ADD, TokenType.SUB,
                TokenType.BIT_NOT, TokenType.NOT,
                TokenType.INCPOSTFIX, TokenType.INCPREFIX,
                TokenType.DECPOSTFIX, TokenType.DECPREFIX);
    }

    private final TokenSequence<? extends JsTokenId> ts;

    private final FormatTokenStream tokenStream;

    private final int formatFinish;

    private final TokenUtils tokenUtils;

    public JsFormatVisitor(FormatTokenStream tokenStream, TokenSequence<? extends JsTokenId> ts, int formatFinish) {
        super(new LexicalContext());
        this.ts = ts;
        this.tokenStream = tokenStream;
        this.formatFinish = formatFinish;
        this.tokenUtils = new TokenUtils(ts, tokenStream, formatFinish);
    }

    @Override
    public boolean enterBlock(Block block) {
        if (isScript(block) || !isVirtual(block)) {
            if (isScript(block)) {
                handleBlockContent(block, true);
            } else {
                handleStandardBlock(block);
            }
        }

        if (isScript(block) || !isVirtual(block)) {
            return false;
        } else {
            return super.enterBlock(block);
        }
    }

    @Override
    public Node leaveBlock(Block block) {
        if (isScript(block)
                || !isVirtual(block)) {
            return null;
        } else {
            return super.leaveBlock(block);
        }
    }

    @Override
    public boolean enterCaseNode(CaseNode caseNode) {
        List<Statement> nodes = caseNode.getStatements();
        if (nodes.size() == 1) {
            Statement node = nodes.get(0);
            if (node instanceof BlockStatement) {
                return super.enterCaseNode(caseNode);
            }
        }

        if (nodes.size() >= 1) {
            // indentation mark
            FormatToken formatToken = tokenUtils.getPreviousToken(getStart(nodes.get(0)), JsTokenId.OPERATOR_COLON, true);
            if (formatToken != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }

            // put indentation mark
            formatToken = getCaseEndToken(getStart(nodes.get(0)), getFinish(nodes.get(nodes.size() - 1)));
            if (formatToken != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }

            handleBlockContent(nodes, true);
        }
        return false;
    }

    @Override
    public boolean enterJsxElementNode(JsxElementNode jsxElementNode) {
        FormatToken jsxToken = tokenStream.getToken(getStart(jsxElementNode));
        if (jsxToken != null) {
            assert jsxToken.getId() == JsTokenId.JSX_TEXT : jsxToken.toString() + " from " + jsxElementNode.toString();
            if (jsxToken.getText().toString().startsWith("<")) {
                FormatToken jsxTokenPrev = jsxToken.previous();
                if (jsxTokenPrev != null) {
                    TokenUtils.appendTokenAfterLastVirtual(jsxTokenPrev, FormatToken.forFormat(FormatToken.Kind.BEFORE_JSX_BLOCK_START), true);
                }
            }

            jsxToken = tokenUtils.getPreviousToken(getFinish(jsxElementNode) - 1, JsTokenId.JSX_TEXT);
            if (jsxToken != null && jsxToken.getText().toString().endsWith(">")) {
                assert jsxToken.getId() == JsTokenId.JSX_TEXT : jsxToken;
                TokenUtils.appendTokenAfterLastVirtual(jsxToken, FormatToken.forFormat(FormatToken.Kind.AFTER_JSX_BLOCK_END), true);
            }
        }

        for (Expression e : jsxElementNode.getChildren()) {
            if (!(e instanceof LiteralNode) && !(e instanceof JsxElementNode)) {
                // assignmentExpression
                int start = getStart(e);
                FormatToken token = tokenUtils.getPreviousToken(start, JsTokenId.JSX_EXP_BEGIN);
                if (token != null) {
                    TokenUtils.appendToken(token, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                }
                int finish = getFinish(e);
                token = tokenUtils.getNextToken(finish, JsTokenId.JSX_EXP_END);
                if (token != null) {
                    token = tokenUtils.getPreviousNonWhiteToken(token.getOffset(),
                            start, JsTokenId.JSX_EXP_END, true);
                    if (token != null) {
                        TokenUtils.appendToken(token, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                    }
                }
            }
        }
        return super.enterJsxElementNode(jsxElementNode);
    }

    @Override
    public boolean enterJsxAttributeNode(JsxAttributeNode jsxAttributeNode) {
        Expression e = jsxAttributeNode.getValue();
        if (e != null && !(e instanceof LiteralNode) && !(e instanceof JsxElementNode)) {
            // assignmentExpression or unaryNode
            int start = getStart(e);
            FormatToken token = tokenUtils.getPreviousToken(start, JsTokenId.JSX_EXP_BEGIN);
            if (token != null) {
                TokenUtils.appendToken(token, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            }
            int finish = getFinish(e);
            token = tokenUtils.getNextToken(finish, JsTokenId.JSX_EXP_END);
            if (token != null) {
                token = tokenUtils.getPreviousNonWhiteToken(token.getOffset(),
                        start, JsTokenId.JSX_EXP_END, true);
                if (token != null) {
                    TokenUtils.appendToken(token, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                }
            }
        }
        return super.enterJsxAttributeNode(jsxAttributeNode);
    }

    @Override
    public boolean enterWhileNode(WhileNode whileNode) {
        if (whileNode.isDoWhile()) {
            // within parens spaces
            int leftStart;
            Block body = whileNode.getBody();
//            if (isVirtual(body)) {
//                // unfortunately due to condition at the end of do-while
//                // we have to care about virtual block
//                List<Statement> statements = body.getStatements();
//                leftStart = getFinish(statements.get(statements.size() - 1));
//            } else {
                leftStart = getFinish(whileNode.getBody());
//            }
            markSpacesWithinParentheses(whileNode, leftStart, getFinish(whileNode),
                    FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(whileNode.getBody(), FormatToken.Kind.BEFORE_DO_BRACE);

            FormatToken whileToken = tokenUtils.getPreviousToken(getFinish(whileNode), JsTokenId.KEYWORD_WHILE);
            if (whileToken != null) {
                FormatToken beforeWhile = whileToken.previous();
                if (beforeWhile != null) {
                    tokenUtils.appendToken(beforeWhile, FormatToken.forFormat(FormatToken.Kind.BEFORE_WHILE_KEYWORD));
                }
            }
            if (handleLoop(whileNode, FormatToken.Kind.AFTER_DO_START)) {
                return false;
            }

            markEndCurlyBrace(whileNode.getBody());
            return super.enterWhileNode(whileNode);
        } else {
            // within parens spaces
            markSpacesWithinParentheses(whileNode, getStart(whileNode), getStart(whileNode.getBody()),
                    FormatToken.Kind.AFTER_WHILE_PARENTHESIS, FormatToken.Kind.BEFORE_WHILE_PARENTHESIS);

            // mark space before left brace
            markSpacesBeforeBrace(whileNode.getBody(), FormatToken.Kind.BEFORE_WHILE_BRACE);

            if (handleLoop(whileNode, FormatToken.Kind.AFTER_WHILE_START)) {
                return false;
            }

            markEndCurlyBrace(whileNode.getBody());
            return super.enterWhileNode(whileNode);
        }
    }

    @Override
    public boolean enterForNode(ForNode forNode) {
        // within parens spaces
        markSpacesWithinParentheses(forNode, getStart(forNode), getStart(forNode.getBody()),
                FormatToken.Kind.AFTER_FOR_PARENTHESIS, FormatToken.Kind.BEFORE_FOR_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(forNode.getBody(), FormatToken.Kind.BEFORE_FOR_BRACE);

        if (!forNode.isForEach() && !forNode.isForIn()) {
            Node init = forNode.getInit();
            Node test = forNode.getTest();

            FormatToken formatToken;

            // unfortunately init and test may be null
            if (init != null) {
                formatToken = tokenUtils.getNextToken(getFinish(init), JsTokenId.OPERATOR_SEMICOLON);
            } else {
                formatToken = tokenUtils.getNextToken(getStart(forNode), JsTokenId.OPERATOR_SEMICOLON,
                        getStart(forNode.getBody()));
            }
            if (formatToken != null && test != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.BEFORE_FOR_TEST));
            }

            if (test != null) {
                formatToken = tokenUtils.getNextToken(getFinish(forNode.getTest()), JsTokenId.OPERATOR_SEMICOLON);
            } else {
                // we use the position of init semicolon
                int start = formatToken != null ? formatToken.getOffset() + 1 : getStart(forNode);
                formatToken = tokenUtils.getNextToken(start, JsTokenId.OPERATOR_SEMICOLON,
                                            getStart(forNode.getBody()));
            }
            if (formatToken != null && forNode.getModify() != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken,
                        FormatToken.forFormat(FormatToken.Kind.BEFORE_FOR_MODIFY));
            }
        }
        if (handleLoop(forNode, FormatToken.Kind.AFTER_FOR_START)) {
            return false;
        }

        markEndCurlyBrace(forNode.getBody());
        return super.enterForNode(forNode);
    }

    @Override
    public boolean enterIfNode(IfNode ifNode) {
        ifNode.getTest().accept(this);

        // within parens spaces
        markSpacesWithinParentheses(ifNode, getStart(ifNode), getStart(ifNode.getPass()),
                FormatToken.Kind.AFTER_IF_PARENTHESIS, FormatToken.Kind.BEFORE_IF_PARENTHESIS);

        // pass block
        Block body = ifNode.getPass();
        // mark space before left brace
        markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_IF_BRACE);

        if (isVirtual(body)) {
            handleVirtualBlock(body, FormatToken.Kind.AFTER_IF_START);
        } else {
            enterBlock(body);
            markEndCurlyBrace(body);
        }

        // fail block
        body = ifNode.getFail();
        if (body != null) {
            if (isVirtual(body)) {
                // do the standard block related things
                List<Statement> statements = body.getStatements();
                // there might be no statements when code is broken
                if (!statements.isEmpty() && (statements.get(0) instanceof IfNode)) {
                    // we mark else if statement here
                    handleVirtualBlock(body, FormatToken.Kind.ELSE_IF_INDENTATION_INC,
                            FormatToken.Kind.ELSE_IF_INDENTATION_DEC, FormatToken.Kind.ELSE_IF_AFTER_BLOCK_START, true);
                } else {
                    // mark space before left brace
                    markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_ELSE_BRACE);

                    handleVirtualBlock(body, FormatToken.Kind.AFTER_ELSE_START);
                }
            } else {
                // mark space before left brace
                markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_ELSE_BRACE);

                enterBlock(body);
                markEndCurlyBrace(body);
            }
        }

        return false;
    }

    @Override
    public Node leaveIfNode(IfNode ifNode) {
        return null;
    }

    @Override
    public boolean enterWithNode(WithNode withNode) {
        // within parens spaces
        markSpacesWithinParentheses(withNode, getStart(withNode), getStart(withNode.getBody()),
                FormatToken.Kind.AFTER_WITH_PARENTHESIS, FormatToken.Kind.BEFORE_WITH_PARENTHESIS);

        Block body = withNode.getBody();

        // mark space before left brace
        markSpacesBeforeBrace(body, FormatToken.Kind.BEFORE_WITH_BRACE);

        if (isVirtual(body)) {
            handleVirtualBlock(body, FormatToken.Kind.AFTER_WITH_START);
            return false;
        }

        markEndCurlyBrace(body);
        return super.enterWithNode(withNode);
    }

    @Override
    public boolean enterFunctionNode(FunctionNode functionNode) {
        if (functionNode.isModule()) {
            functionNode.visitImports(this);
            functionNode.visitExports(this);
        }

//        if (functionNode.isClassConstructor() && !"constructor".equals(functionNode.getIdent().getName())) { // NOI18N
//            // generated constructor
//            return false;
//        }

        Block body = functionNode.getBody();
        // default parameters are stored as assignments inside the function
        // body block - the real block is just behind it
        if (body.isParameterBlock()) {
            List<Statement> statements = body.getStatements();
            if (!statements.isEmpty()) {
                Statement last = statements.get(statements.size() - 1);
                if (last instanceof BlockStatement) {
                    body = ((BlockStatement) last).getBlock();
                }
            }
        }
        if (isVirtual(body) && functionNode.getKind() == FunctionNode.Kind.ARROW) {
            Token nonEmpty = tokenUtils.getPreviousNonEmptyToken(getStart(body));
            if (nonEmpty != null) {
                FormatToken token = tokenStream.getToken(ts.offset());
                if (token != null) {
                    TokenUtils.appendTokenAfterLastVirtual(token, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARROW_BLOCK));
                }
            }

            handleVirtualBlock(body, FormatToken.Kind.INDENTATION_INC, FormatToken.Kind.INDENTATION_DEC, null, false);

            nonEmpty = tokenUtils.getPreviousNonEmptyToken(getFinish(body));
            if (nonEmpty != null) {
                FormatToken token = tokenStream.getToken(ts.offset());
                if (token != null) {
                    TokenUtils.appendTokenAfterLastVirtual(token, FormatToken.forFormat(FormatToken.Kind.AFTER_ARROW_BLOCK));
                }
            }
        } else {
            enterBlock(body);
        }

        if (functionNode.isProgram()) {
            return false;
        }

        int start = getStart(functionNode);
        if (functionNode.getKind() == FunctionNode.Kind.ARROW) {
            FormatToken left = tokenUtils.getNextToken(start, JsTokenId.BRACKET_LEFT_PAREN, start);
            FormatToken leftParen = left;
            if (left == null) {
                // single parameter arrow without parenthesis
                left = tokenUtils.getNextToken(start, JsTokenId.IDENTIFIER, start);
            }
            if (left != null) {
                FormatToken previous = left.previous();
                if (previous != null) {
                    TokenUtils.appendToken(previous, FormatToken.forFormat(
                            FormatToken.Kind.BEFORE_ARROW_FUNCTION_DECLARATION));
                }
            }

            handleFunctionParameters(functionNode, leftParen);
        } else {
            // the star * is not multiplication (binary operator)
            // FIXME should this be solved in lexer?
            if (functionNode.getKind() == FunctionNode.Kind.GENERATOR) {
                FormatToken star = tokenUtils.getNextToken(start, JsTokenId.OPERATOR_MULTIPLICATION);
                if (star != null) {
                    FormatToken prev = star.previous();
                    if (prev != null && (prev.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR
                            || prev.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP)) {
                        tokenStream.removeToken(prev);
                    }
                    prev = star.previous();
                    if (prev != null && (prev.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR
                            || prev.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP)) {
                        tokenStream.removeToken(prev);
                    }

                    FormatToken next = star.next();
                    if (next != null && (next.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR
                            || next.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP)) {
                        tokenStream.removeToken(next);
                    }
                    next = star.next();
                    if (next != null && (next.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR
                            || next.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP)) {
                        tokenStream.removeToken(next);
                    }
                }
            }

            FormatToken leftParen = tokenUtils.getNextToken(start, JsTokenId.BRACKET_LEFT_PAREN, getStart(body));
            if (leftParen != null) {
                FormatToken previous = leftParen.previous();
                if (previous != null) {
                    TokenUtils.appendToken(previous, FormatToken.forFormat(
                            functionNode.isAnonymous()
                                    ? FormatToken.Kind.BEFORE_ANONYMOUS_FUNCTION_DECLARATION
                                    : FormatToken.Kind.BEFORE_FUNCTION_DECLARATION));
                }

                handleFunctionParameters(functionNode, leftParen);

                // mark left brace of block - this works if function node
                // start offset is offset of the left brace
                FormatToken leftBrace = tokenUtils.getNextToken(getStart(functionNode),
                        JsTokenId.BRACKET_LEFT_CURLY, getFinish(functionNode));
                if (leftBrace != null) {
                    previous = leftBrace.previous();
                    if (previous != null) {
                        TokenUtils.appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_BRACE));
                    }
                }

                if (functionNode.isStatement() && !functionNode.isAnonymous()) {
                    FormatToken rightBrace = tokenUtils.getPreviousToken(getFinish(functionNode),
                            JsTokenId.BRACKET_RIGHT_CURLY,
                            leftBrace != null ? leftBrace.getOffset() : start);
                    if (rightBrace != null) {
                        TokenUtils.appendToken(rightBrace, FormatToken.forFormat(
                                FormatToken.Kind.AFTER_STATEMENT));
                    }
                }

                markEndCurlyBrace(functionNode);
            }

        }
        return false;
    }

    @Override
    public Node leaveFunctionNode(FunctionNode functionNode) {
        leaveBlock(functionNode.getBody());

        return null;
    }

    @Override
    public boolean enterImportNode(ImportNode importNode) {
        int finish = getFinish(importNode);
        FormatToken token = tokenUtils.getNextToken(finish, JsTokenId.OPERATOR_SEMICOLON);
        if (token != null) {
            // we treat the import as statement
            TokenUtils.appendTokenAfterLastVirtual(token, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
        }
        return false;
    }

    @Override
    public boolean enterExportNode(ExportNode exportNode) {
        int finish = getFinish(exportNode);
        FormatToken token = tokenUtils.getNextToken(finish, JsTokenId.OPERATOR_SEMICOLON);
        if (token != null) {
            // we treat the import as statement
            TokenUtils.appendTokenAfterLastVirtual(token, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
        }
        // the complex export nodes are included in top level function body anyway
        // so we do not want to to visit further
        if (exportNode.isDefault()) {
            return super.enterExportNode(exportNode);
        }
        return false;
    }

    @Override
    public boolean enterCallNode(CallNode callNode) {
        FormatToken leftBrace = tokenUtils.getNextToken(getFinish(callNode.getFunction()),
                JsTokenId.BRACKET_LEFT_PAREN, getFinish(callNode));
        if (leftBrace != null) {
            FormatToken previous = leftBrace.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL));
            }

            // mark the within parenthesis places

            // remove original paren marks
            FormatToken mark = leftBrace.next();
            assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
            tokenStream.removeToken(mark);

            // there is -1 as on the finish position may be some outer paren
            // so we really need the position precisely
            int stopMark = getStart(callNode);
            // lets calculate stop mark precisely to not to catch on arguments
            // parens in broken source
            List<Expression> args = callNode.getArgs();
            if (!args.isEmpty()) {
                stopMark = getFinish(args.get(args.size() - 1));
            }
            FormatToken rightBrace = tokenUtils.getPreviousToken(getFinish(callNode) - 1,
                    JsTokenId.BRACKET_RIGHT_PAREN, stopMark);
            if (rightBrace != null) {
                previous = TokenUtils.findVirtualToken(rightBrace,
                        FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS, true);

                // this might happen for sanitization inserted paren
                if (previous != null) {
                    tokenStream.removeToken(previous);
                }
            }

            // place the new marks
            if (!callNode.getArgs().isEmpty()) {
                TokenUtils.appendToken(leftBrace, FormatToken.forFormat(
                        FormatToken.Kind.AFTER_FUNCTION_CALL_PARENTHESIS));

                if (rightBrace != null) {
                    previous = rightBrace.previous();
                    if (previous != null) {
                        TokenUtils.appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_CALL_PARENTHESIS));
                    }
                }
            }

            // place function arguments marks
            for (Node arg : callNode.getArgs()) {
                FormatToken argToken = tokenUtils.getNextToken(getStart(arg), null);
                if (argToken != null) {
                    FormatToken beforeArg = argToken.previous();
                    if (beforeArg != null) {
                        TokenUtils.appendToken(beforeArg,
                                FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_CALL_ARGUMENT));
                    }
                }
            }
        }
        handleFunctionCallChain(callNode);

        return super.enterCallNode(callNode);
    }

    @Override
    public boolean enterClassNode(ClassNode classNode) {
        handleDecorators(classNode.getDecorators());

        Expression heritage = classNode.getClassHeritage();
        if (heritage != null) {
            heritage.accept(this);
            FormatToken extendsToken = tokenUtils.getPreviousToken(getStart(heritage), JsTokenId.KEYWORD_EXTENDS, getStart(classNode));
            if (extendsToken != null) {
                FormatToken token = extendsToken.previous();
                if (token != null) {
                    TokenUtils.appendToken(token, FormatToken.forFormat(FormatToken.Kind.BEFORE_CLASS_EXTENDS));
                }
            }
        }

        // indentation mark
        FormatToken formatToken = tokenUtils.getNextToken(heritage != null ? getFinish(heritage) : getStart(classNode),
                JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CLASS_START));

            FormatToken previous = formatToken.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(
                        FormatToken.Kind.BEFORE_CLASS_DECLARATION_BRACE));
            }
        }

        PropertyNode constructor = classNode.getConstructor();
        if (constructor != null) {
            // generated default constructor has range equal to class
            if (constructor.getStart() != classNode.getStart()
                    || constructor.getFinish() != classNode.getFinish()) {
                handleDecorators(constructor.getDecorators());
                handleClassElement(constructor, getStart(constructor));
            }
        }
        for (Node property : classNode.getClassElements()) {
            PropertyNode propertyNode = (PropertyNode) property;
            handleDecorators(propertyNode.getDecorators());
            handleClassElement(propertyNode, getStart(propertyNode));
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = tokenUtils.getPreviousNonWhiteToken(getFinish(classNode) - 1,
                getStart(classNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_CLASS_END));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }

        markEndCurlyBrace(classNode);

        return false;
    }

    @Override
    public boolean enterObjectNode(ObjectNode objectNode) {
        // indentation mark
        FormatToken formatToken = tokenUtils.getPreviousToken(getStart(objectNode), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_LEFT_BRACE));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_OBJECT_START));
            FormatToken previous = formatToken.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT));
            }
        }

        int objectFinish = getFinish(objectNode);
        for (Node property : objectNode.getElements()) {
            property.accept(this);

            PropertyNode propertyNode = (PropertyNode) property;
            if (propertyNode.getGetter() != null) {
                FunctionNode getter = (FunctionNode) propertyNode.getGetter();
                markPropertyFinish(getFinish(getter), objectFinish, false);
            }
            if (propertyNode.getSetter() != null) {
                FunctionNode setter = (FunctionNode) propertyNode.getSetter();
                markPropertyFinish(getFinish(setter), objectFinish, false);
            }

            // mark property end
            markPropertyFinish(getFinish(property), objectFinish, true);
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = tokenUtils.getPreviousNonWhiteToken(getFinish(objectNode) - 1,
                getStart(objectNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_OBJECT_END));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_RIGHT_BRACE));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }

        return false;
    }

    @Override
    public boolean enterPropertyNode(PropertyNode propertyNode) {
        FormatToken colon = tokenUtils.getNextToken(getFinish(propertyNode.getKey()),
                JsTokenId.OPERATOR_COLON, getFinish(propertyNode));
        if (colon != null) {
            TokenUtils.appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY_OPERATOR));
            FormatToken before = colon.previous();
            if (before != null) {
                TokenUtils.appendTokenAfterLastVirtual(before, FormatToken.forFormat(FormatToken.Kind.BEFORE_PROPERTY_OPERATOR));
            }
        }
        return super.enterPropertyNode(propertyNode);
    }

    @Override
    public boolean enterSwitchNode(SwitchNode switchNode) {
        // within parens spaces
        markSpacesWithinParentheses(switchNode);

        // mark space before left brace
        markSpacesBeforeBrace(switchNode);

        FormatToken formatToken = tokenUtils.getNextToken(getStart(switchNode), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
        }

        List<CaseNode> nodes = new ArrayList<>(switchNode.getCases());
        if (switchNode.getDefaultCase() != null) {
            nodes.add(switchNode.getDefaultCase());
        }

        for (CaseNode caseNode : nodes) {
            int index = getFinish(caseNode);
            List<Statement> statements = caseNode.getStatements();
            if (!statements.isEmpty()) {
                index = getStart(statements.get(0));
            }

            formatToken = tokenUtils.getPreviousToken(index, JsTokenId.OPERATOR_COLON);
            if (formatToken != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_CASE));
            }
        }

        // put indentation mark after non white token preceeding curly bracket
        formatToken = tokenUtils.getPreviousNonWhiteToken(getFinish(switchNode),
                getStart(switchNode), JsTokenId.BRACKET_RIGHT_CURLY, true);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
        }

        markEndCurlyBrace(switchNode);
        return super.enterSwitchNode(switchNode);
    }

    @Override
    public boolean enterUnaryNode(UnaryNode unaryNode) {
        TokenType type = unaryNode.tokenType();
        if (UNARY_TYPES.contains(type)) {
            if (TokenType.DECPOSTFIX.equals(type) || TokenType.INCPOSTFIX.equals(type)) {
                FormatToken formatToken = tokenUtils.getPreviousToken(getFinish(unaryNode),
                        TokenType.DECPOSTFIX.equals(type) ? JsTokenId.OPERATOR_DECREMENT : JsTokenId.OPERATOR_INCREMENT);

                if (formatToken != null) {
                    formatToken = formatToken.previous();
                    if (formatToken != null) {
                        TokenUtils.appendToken(formatToken,
                                FormatToken.forFormat(FormatToken.Kind.BEFORE_UNARY_OPERATOR));
                    }
                }
            } else {
                FormatToken formatToken = tokenUtils.getNextToken(getStart(unaryNode), null);

                // may be null when we are out of formatted area
                if (formatToken != null) {
                    // remove around binary operator tokens added during token
                    // stream creation
                    if (TokenType.ADD.equals(type) || TokenType.SUB.equals(type)) {
                        assert formatToken.getId() == JsTokenId.OPERATOR_PLUS
                                    || formatToken.getId() == JsTokenId.OPERATOR_MINUS : formatToken;
                        // we remove blindly inserted binary op markers
                        FormatToken toRemove = TokenUtils.findVirtualToken(formatToken,
                                FormatToken.Kind.BEFORE_BINARY_OPERATOR, true);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);
                        toRemove = TokenUtils.findVirtualToken(formatToken,
                                FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP, true);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.BEFORE_BINARY_OPERATOR_WRAP : toRemove;
                        tokenStream.removeToken(toRemove);

                        toRemove = TokenUtils.findVirtualToken(formatToken,
                                FormatToken.Kind.AFTER_BINARY_OPERATOR, false);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR : toRemove;
                        tokenStream.removeToken(toRemove);
                        toRemove = TokenUtils.findVirtualToken(formatToken,
                                FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP, false);
                        assert toRemove != null
                                && toRemove.getKind() == FormatToken.Kind.AFTER_BINARY_OPERATOR_WRAP : toRemove;
                        tokenStream.removeToken(toRemove);
                    }

                    TokenUtils.appendToken(formatToken,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_UNARY_OPERATOR));
                }
            }
        }

        return super.enterUnaryNode(unaryNode);
    }

    @Override
    public boolean enterTernaryNode(TernaryNode ternaryNode) {
        int start = getFinish(ternaryNode.getTest());
        FormatToken question = tokenUtils.getNextToken(start, JsTokenId.OPERATOR_TERNARY);
        if (question != null) {
            FormatToken previous = question.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR_WRAP));
            }
            TokenUtils.appendToken(question, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
            TokenUtils.appendToken(question, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR_WRAP));
            FormatToken colon = tokenUtils.getPreviousToken(getStart(ternaryNode.getFalseExpression()), JsTokenId.OPERATOR_COLON);
            if (colon != null) {
                previous = colon.previous();
                if (previous != null) {
                    TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR));
                    TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_TERNARY_OPERATOR_WRAP));
                }
                TokenUtils.appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR));
                TokenUtils.appendToken(colon, FormatToken.forFormat(FormatToken.Kind.AFTER_TERNARY_OPERATOR_WRAP));
            }
        }

        return super.enterTernaryNode(ternaryNode);
    }

    @Override
    public boolean enterCatchNode(CatchNode catchNode) {
        // within parens spaces
        markSpacesWithinParentheses(catchNode, getStart(catchNode), getStart(catchNode.getBody()),
                FormatToken.Kind.AFTER_CATCH_PARENTHESIS, FormatToken.Kind.BEFORE_CATCH_PARENTHESIS);

        // mark space before left brace
        markSpacesBeforeBrace(catchNode.getBody(), FormatToken.Kind.BEFORE_CATCH_BRACE);

        markEndCurlyBrace(catchNode.getBody());
        return super.enterCatchNode(catchNode);
    }

    @Override
    public boolean enterTryNode(TryNode tryNode) {
        // mark space before left brace
        markSpacesBeforeBrace(tryNode.getBody(), FormatToken.Kind.BEFORE_TRY_BRACE);

        Block finallyBody = tryNode.getFinallyBody();
        if (finallyBody != null) {
            // mark space before finally left brace
            markSpacesBeforeBrace(tryNode.getFinallyBody(), FormatToken.Kind.BEFORE_FINALLY_BRACE);
        }

        markEndCurlyBrace(tryNode.getBody());
        markEndCurlyBrace(finallyBody);
        return super.enterTryNode(tryNode);
    }

    @Override
    public boolean enterLiteralNode(LiteralNode literalNode) {
        Object value = literalNode.getValue();
        if (value instanceof Node[]) {
            int start = getStart(literalNode);
            int finish = getFinish(literalNode);
            FormatToken leftBracket = tokenUtils.getNextToken(start, JsTokenId.BRACKET_LEFT_BRACKET, finish);
            if (leftBracket != null) {
                if (leftBracket.previous() != null) {
                    // mark beginning of the array (see issue #250150)
                    TokenUtils.appendToken(leftBracket.previous(), FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY));
                }
                TokenUtils.appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_START));
                TokenUtils.appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_BRACKET));
                TokenUtils.appendToken(leftBracket, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
                FormatToken rightBracket = tokenUtils.getPreviousToken(finish - 1, JsTokenId.BRACKET_RIGHT_BRACKET, start + 1);
                if (rightBracket != null) {
                    FormatToken previous = rightBracket.previous();
                    if (previous != null) {
                        TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_END));
                        TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_ARRAY_LITERAL_BRACKET));
                        TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
                    }
                }
            }

            if (literalNode.isArray()) {
                Node[] items = ((LiteralNode.ArrayLiteralNode) literalNode).getValue();
                if (items != null && items.length > 0) {
                    int prevItemFinish = start;
                    for (int i = 1; i < items.length; i++) {
                        Node prevItem = items[i - 1];
                        if (prevItem != null) {
                            prevItemFinish = getFinish(prevItem);
                        }
                        FormatToken comma = tokenUtils.getNextToken(prevItemFinish, JsTokenId.OPERATOR_COMMA, finish);
                        if (comma != null) {
                            prevItemFinish = comma.getOffset();
                            TokenUtils.appendTokenAfterLastVirtual(comma,
                                    FormatToken.forFormat(FormatToken.Kind.AFTER_ARRAY_LITERAL_ITEM));
                        }
                    }
                }
            }
        }

        return super.enterLiteralNode(literalNode);
    }

    @Override
    public boolean enterVarNode(VarNode varNode) {
        if (varNode.isExport() || varNode.isDestructuring()) {
            return false;
        }
        int finish = getFinish(varNode) - 1;
        Token nextToken = tokenUtils.getNextNonEmptyToken(finish);
        if (nextToken != null && nextToken.id() == JsTokenId.OPERATOR_COMMA) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (formatToken != null) {
                FormatToken next = formatToken.next();
                assert next != null && next.getKind() == FormatToken.Kind.AFTER_COMMA : next;
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_VAR_DECLARATION));
            }
        }

        return super.enterVarNode(varNode);
    }

    // handles both standard and arrow functions
    private void handleFunctionParameters(FunctionNode functionNode, FormatToken leftParen) {
        if (leftParen != null) {
            // remove original paren marks
            FormatToken mark = leftParen.next();
            assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
            tokenStream.removeToken(mark);

            FormatToken rightParen = tokenUtils.getPreviousToken(getStart(functionNode.getBody()),
                    JsTokenId.BRACKET_RIGHT_PAREN, leftParen.getOffset());
            if (rightParen != null) {
                FormatToken previous = rightParen.previous();
                assert previous != null && previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous;
                tokenStream.removeToken(previous);
            }

            // place the new marks
            if (!functionNode.getParameters().isEmpty()) {
                TokenUtils.appendToken(leftParen, FormatToken.forFormat(
                        FormatToken.Kind.AFTER_FUNCTION_DECLARATION_PARENTHESIS));

                if (rightParen != null) {
                    FormatToken previous = rightParen.previous();
                    if (previous != null) {
                        TokenUtils.appendToken(previous, FormatToken.forFormat(
                                FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARENTHESIS));
                    }
                }
            }
        }

        // place function parameters marks
        for (IdentNode param : functionNode.getParameters()) {
            FormatToken paramToken = tokenUtils.getNextToken(getStart(param), JsTokenId.IDENTIFIER);
            if (paramToken != null) {
                // there might be "a, ...z" for example so we want the mark before the rest
                // parameter
                Token previousNonEmpty = tokenUtils.getPreviousNonEmptyToken(paramToken.getOffset());
                if (previousNonEmpty != null && previousNonEmpty.id() == JsTokenId.OPERATOR_REST) {
                    paramToken = tokenStream.getToken(ts.offset());
                }

                FormatToken beforeIdent = paramToken.previous();
                if (beforeIdent != null) {
                    TokenUtils.appendToken(beforeIdent,
                            FormatToken.forFormat(FormatToken.Kind.BEFORE_FUNCTION_DECLARATION_PARAMETER));
                }
            }
        }
    }

    private void handleFunctionCallChain(CallNode callNode) {
        Node function = callNode.getFunction();
        if (function instanceof AccessNode) {
            Node base = ((AccessNode) function).getBase();
            if (base instanceof CallNode) {
                CallNode chained = (CallNode) base;
                int finish = getFinish(chained);
                FormatToken formatToken = tokenUtils.getNextToken(finish, JsTokenId.OPERATOR_DOT);
                if (formatToken != null) {
                    TokenUtils.appendTokenAfterLastVirtual(formatToken,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_CHAIN_CALL_DOT));
                    formatToken = formatToken.previous();
                    if (formatToken != null) {
                        TokenUtils.appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.BEFORE_CHAIN_CALL_DOT));
                    }
                }
            }
        }
    }

    private boolean handleLoop(LoopNode loopNode, FormatToken.Kind afterStart) {
        Block body = loopNode.getBody();
        if (isVirtual(body)) {
            handleVirtualBlock(body, afterStart);
            return true;
        }
        return false;
    }

    private void handleStandardBlock(Block block) {
        handleBlockContent(block, true);

        // indentation mark & block start
        FormatToken formatToken = tokenUtils.getPreviousToken(getStart(block), JsTokenId.BRACKET_LEFT_CURLY, true);
        if (formatToken != null && !isScript(block)) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_INC));
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_BLOCK_START));
        }

        // put indentation mark after non white token preceeding curly bracket
        // XXX optimize ?
        formatToken = tokenUtils.getNextToken(getFinish(block) - 1, JsTokenId.BRACKET_RIGHT_CURLY);
        if (formatToken != null) {
            formatToken = tokenUtils.getPreviousNonWhiteToken(formatToken.getOffset(),
                    getStart(block), JsTokenId.BRACKET_RIGHT_CURLY, true);
            if (formatToken != null && !isScript(block)) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.INDENTATION_DEC));
            }
        }
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind afterBlock) {
        handleVirtualBlock(block, FormatToken.Kind.INDENTATION_INC, FormatToken.Kind.INDENTATION_DEC,
                afterBlock, true);
    }

    private void handleVirtualBlock(Block block, FormatToken.Kind indentationInc,
            FormatToken.Kind indentationDec, FormatToken.Kind afterBlock, boolean markStatements) {

        assert isVirtual(block) : block;

        boolean assertsEnabled = false;
        assert assertsEnabled = true;
        if (assertsEnabled) {
            if (block.getStatements().size() > 1) {
                int count = 0;
                // there may be multiple var statements due to the comma
                // separated vars translated to multiple statements in ast
                for (Node node : block.getStatements()) {
                    if (!(node instanceof VarNode)) {
                        count++;
                    }
                }
                assert count <= 1;
            }
        }

        if (block.getStart() >= block.getFinish()/*block.getStatements().isEmpty()*/) {
            return;
        }

        handleBlockContent(block, markStatements);

        //Node statement = block.getStatements().get(0);

        // indentation mark & block start
        Token token = tokenUtils.getPreviousNonEmptyToken(getStart(block));

        if (token != null) {
            FormatToken formatToken = tokenStream.getToken(ts.offset());
            if (!isScript(block)) {
                if (formatToken == null && ts.offset() <= formatFinish) {
                    formatToken = tokenStream.getTokens().get(0);
                }
                if (formatToken != null) {
                    TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationInc));
                    if (afterBlock != null) {
                        TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(afterBlock));
                    }
                }
            }
        }

        // put indentation mark after non white token
        int finish = getFinish(block);
        // empty statement has start == finish
        FormatToken formatToken = tokenUtils.getPreviousToken(
                block.getStart() < finish ? finish - 1 : finish, null, true);
        if (formatToken != null && !isScript(block)) {
            while (formatToken != null && (formatToken.getKind() == FormatToken.Kind.EOL
                    || formatToken.getKind() == FormatToken.Kind.WHITESPACE
                    || formatToken.getKind() == FormatToken.Kind.LINE_COMMENT
                    || formatToken.getKind() == FormatToken.Kind.BLOCK_COMMENT
                    || formatToken.getKind() == FormatToken.Kind.DOC_COMMENT)) {
                formatToken = formatToken.previous();
            }
            if (block.getStatementCount() == 0 && block.getStart() < block.getFinish()) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT));
            }
            TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(indentationDec));
        }
    }

    private void handleBlockContent(Block block, boolean markStatements) {
        handleBlockContent(block.getStatements(), markStatements);
    }

    private void handleBlockContent(List<Statement> statements, boolean markStatements) {
        // statements
        boolean destructuring = false;
        for (int i = 0; i < statements.size(); i++) {
            Node statement = statements.get(i);
            statement.accept(this);

            if (markStatements) {
                int start = getStart(statement);
                int finish = getFinish(statement);

                /*
                 * What do we solve here? Unfortunately nashorn parses single
                 * var statement as (possibly) multiple VarNodes. For example:
                 * var a=1,b=2; is parsed to two VarNodes. The first covering a=1,
                 * the second b=2. So we iterate subsequent VarNodes searching the
                 * last one and the proper finish token.
                 */
                if (statement instanceof VarNode) {
                    if (((VarNode) statement).isDestructuring()) {
                        destructuring = true;
                        continue;
                    }
                    if (!isDeclaration((VarNode) statement)) {
                        int index = i + 1;
                        Node lastVarNode = statement;

                        while (i + 1 < statements.size()) {
                            Node next = statements.get(++i);
                            if (!(next instanceof VarNode) || isDeclaration((VarNode) next)) {
                                i--;
                                break;
                            } else {
                                Token token = tokenUtils.getPreviousNonEmptyToken(getStart(next));
                                if (token != null && (JsTokenId.KEYWORD_VAR == token.id()
                                        || JsTokenId.KEYWORD_CONST == token.id()
                                        || JsTokenId.RESERVED_LET == token.id())) {
                                    i--;
                                    break;
                                }
                            }
                            lastVarNode = next;
                        }

                        for (int j = index; j < i + 1; j++) {
                            Node skipped = statements.get(j);
                            skipped.accept(this);
                        }

                        finish = getFinish(lastVarNode);
                    }
                }

                int searchOffset = start < finish ? finish - 1 : finish;
                // if it is destructuring the finish is at the end
                if (statement instanceof ExpressionStatement && destructuring) {
                    searchOffset = finish;
                }
                destructuring = false;
                FormatToken formatToken = tokenUtils.getPreviousToken(searchOffset, null);
                while (formatToken != null && (formatToken.getKind() == FormatToken.Kind.EOL
                        || formatToken.getKind() == FormatToken.Kind.WHITESPACE
                        || formatToken.getKind() == FormatToken.Kind.LINE_COMMENT
                        || formatToken.getKind() == FormatToken.Kind.BLOCK_COMMENT
                        || formatToken.getKind() == FormatToken.Kind.DOC_COMMENT)) {
                    formatToken = formatToken.previous();
                }
                if (formatToken != null) {
                    TokenUtils.appendTokenAfterLastVirtual(formatToken,
                            FormatToken.forFormat(FormatToken.Kind.AFTER_STATEMENT), true);
                }
            }
        }
    }

    private void handleClassElement(PropertyNode property, int start) {
        property.accept(this);

        PropertyNode propertyNode = (PropertyNode) property;
        if (propertyNode.getGetter() != null) {
            FunctionNode getter = (FunctionNode) propertyNode.getGetter();
            markClassElementFinish(getStart(getter), getFinish(getter), start,
                    false, propertyNode.getGetter());
        }
        if (propertyNode.getSetter() != null) {
            FunctionNode setter = (FunctionNode) propertyNode.getSetter();
            markClassElementFinish(getStart(setter), getFinish(setter), start,
                    false, propertyNode.getSetter());
        }

        // mark property end
        markClassElementFinish(getStart(property), getFinish(property), start,
                true, propertyNode.getValue());
    }

    private void handleDecorators(List<Expression> decorators) {
        for (Expression decorator : decorators) {
            FormatToken formatToken = tokenUtils.getPreviousNonWhiteToken(getFinish(decorator) - 1, -1, null, true);
            if (formatToken != null) {
                TokenUtils.appendTokenAfterLastVirtual(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_DECORATOR));
            }
        }
    }

    private void markSpacesWithinParentheses(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = tokenUtils.getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            markSpacesWithinParentheses(node, leftStart, token.getOffset(),
                    FormatToken.Kind.AFTER_SWITCH_PARENTHESIS, FormatToken.Kind.BEFORE_SWITCH_PARENTHESIS);
        }
    }

    private void markSpacesBeforeBrace(SwitchNode node) {
        int leftStart = getStart(node);

        // the { has to be there for switch
        FormatToken token = tokenUtils.getNextToken(leftStart, JsTokenId.BRACKET_LEFT_CURLY, getFinish(node));
        if (token != null) {
            FormatToken previous = token.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(FormatToken.Kind.BEFORE_SWITCH_BRACE));
            }
        }
    }

    /**
     * Method putting formatting tokens for within parenthesis rule. Note
     * that this method may be more secure as it can search for the left paren
     * from start of the node and for the right from the body of the node
     * avoiding possibly wrong offset of expressions/conditions.
     *
     * @param outerNode the node we are marking, such as if, while, with
     * @param leftStart from where to start search to the right for the left paren
     * @param rightStart from where to start search to the left for the right paren
     * @param leftMark where to stop searching for the left paren
     * @param rightMark where to stop searching for the right paren
     */
    private void markSpacesWithinParentheses(Node outerNode, int leftStart,
            int rightStart, FormatToken.Kind leftMark, FormatToken.Kind rightMark) {

        FormatToken leftParen = tokenUtils.getNextToken(leftStart,
                JsTokenId.BRACKET_LEFT_PAREN, getFinish(outerNode));
        if (leftParen != null) {
            FormatToken mark = leftParen.next();
            assert mark != null && mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS : mark;
            if (mark.getKind() == FormatToken.Kind.AFTER_LEFT_PARENTHESIS) {
                tokenStream.removeToken(mark);
            }

            TokenUtils.appendToken(leftParen, FormatToken.forFormat(leftMark));
            FormatToken rightParen = tokenUtils.getPreviousToken(rightStart,
                    JsTokenId.BRACKET_RIGHT_PAREN, getStart(outerNode));
            if (rightParen != null) {
                FormatToken previous = rightParen.previous();
                assert previous != null && previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS : previous;
                if (previous.getKind() == FormatToken.Kind.BEFORE_RIGHT_PARENTHESIS) {
                    tokenStream.removeToken(previous);
                }

                previous = rightParen.previous();
                if (previous != null) {
                    TokenUtils.appendToken(previous, FormatToken.forFormat(rightMark));
                }
            }
        }
    }

    private void markSpacesBeforeBrace(Block block, FormatToken.Kind mark) {
        FormatToken brace = tokenUtils.getPreviousToken(getStart(block), null,
                getStart(block) - 1);
        if (brace != null) {
            FormatToken previous = brace.previous();
            if (previous != null) {
                TokenUtils.appendToken(previous, FormatToken.forFormat(mark));
            }
        }
    }

    private void markPropertyFinish(int finish, int objectFinish, boolean checkDuplicity) {
        FormatToken formatToken = tokenUtils.getNextToken(finish, JsTokenId.OPERATOR_COMMA, objectFinish - 1);
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken,
                    FormatToken.forFormat(FormatToken.Kind.AFTER_PROPERTY), checkDuplicity);
        }
    }

    private void markClassElementFinish(int start, int finish, int classFinish,
            boolean checkDuplicity, Expression value) {

        FormatToken formatToken;
        if (value instanceof FunctionNode) {
            // method
            formatToken = tokenUtils.getPreviousToken(finish, JsTokenId.BRACKET_RIGHT_CURLY, classFinish);
        } else {
            // property
            formatToken = tokenUtils.getPreviousToken(start < finish ? finish - 1 : finish, null);
            while (formatToken != null && (formatToken.getKind() == FormatToken.Kind.EOL
                    || formatToken.getKind() == FormatToken.Kind.WHITESPACE
                    || formatToken.getKind() == FormatToken.Kind.LINE_COMMENT
                    || formatToken.getKind() == FormatToken.Kind.BLOCK_COMMENT
                    || formatToken.getKind() == FormatToken.Kind.DOC_COMMENT)) {
                formatToken = formatToken.previous();
            }
        }
        if (formatToken != null) {
            TokenUtils.appendTokenAfterLastVirtual(formatToken,
                    FormatToken.forFormat(FormatToken.Kind.AFTER_ELEMENT), checkDuplicity);
        }
    }

    private void markEndCurlyBrace(Node node) {
        if (node != null) {
            FormatToken formatToken = tokenStream.getToken(getFinish(node) - 1);
            if (formatToken != null) {
                while (formatToken.isVirtual()) {
                    formatToken = formatToken.previous();
                    if (formatToken == null) {
                        return;
                    }
                }
                if (formatToken.getId() == JsTokenId.BRACKET_RIGHT_CURLY) {
                    TokenUtils.appendToken(formatToken, FormatToken.forFormat(FormatToken.Kind.AFTER_END_BRACE));
                }
            }
        }
    }

    /**
     * Finds the next non empty token first and then move back to non whitespace
     * token.
     *
     * @param block case block
     * @return format token
     */
    private FormatToken getCaseEndToken(int start, int finish) {
        ts.move(finish);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }

        Token ret = null;
        while (ts.moveNext()) {
            Token token = ts.token();
            if ((token.id() != JsTokenId.BLOCK_COMMENT && token.id() != JsTokenId.DOC_COMMENT
                && token.id() != JsTokenId.LINE_COMMENT && token.id() != JsTokenId.EOL
                && token.id() != JsTokenId.WHITESPACE)) {
                ret = token;
                break;
            }
        }

        if (ret != null) {
            while (ts.movePrevious() && ts.offset() >= start) {
                Token current = ts.token();
                if (current.id() != JsTokenId.WHITESPACE) {
                    ret = current;
                    break;
                }
            }

            return tokenUtils.getFallback(ts.offset(), true);
        }
        return null;
    }

    private int getStart(Node node) {
        // unfortunately in binary node the token represents operator
        // so string fix would not work
        if (node instanceof BinaryNode) {
            return getStart((BinaryNode) node);
        }
        if (node instanceof FunctionNode) {
            return getFunctionStart((FunctionNode) node);
        }
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to start
        // in case it is string literal.
        int start = node.getStart();
        long firstToken = node.getToken();
        TokenType type = com.oracle.js.parser.Token.descType(firstToken);
        if (type.equals(TokenType.STRING) || type.equals(TokenType.ESCSTRING)) {
            ts.move(start - 1);
            if (ts.moveNext()) {
                Token<? extends JsTokenId> token = ts.token();
                if (token.id() == JsTokenId.STRING_BEGIN) {
                    start--;
                }
            }
        }

        return start;
    }

    private int getStart(BinaryNode node) {
        return getStart(node.lhs());
    }

    private static int getFunctionStart(FunctionNode node) {
        return com.oracle.js.parser.Token.descPosition(node.getFirstToken());
    }

    private int getFinish(Node node) {
        // we are fixing the wrong finish offset here
        // only function node has last token
        if (node instanceof FunctionNode) {
            FunctionNode function = (FunctionNode) node;
            if (node.getStart() == node.getFinish()) {
                long lastToken = function.getLastToken();
                TokenType type = com.oracle.js.parser.Token.descType(lastToken);
                int finish;
                if (type == TokenType.EOL) {
                    // when eol token length just stores line number
                    finish = com.oracle.js.parser.Token.descPosition(lastToken);
                } else {
                    finish = com.oracle.js.parser.Token.descPosition(lastToken)
                            + com.oracle.js.parser.Token.descLength(lastToken);
                }
                // check if it is a string
                if (com.oracle.js.parser.Token.descType(lastToken).equals(TokenType.STRING)) {
                    finish++;
                }
                return finish;
            } else {
                return node.getFinish();
            }
        } else if (node instanceof Block) {
            // XXX in truffle the function body finish is at last statement
            FunctionNode fn = lc.getCurrentFunction();
            if (fn != null) {
                if (fn.getBody() == node) {
                    return getFinish(fn);
                }
            }
        } else if (node instanceof VarNode) {
            VarNode var = (VarNode) node;
            if (var.getInit() instanceof ClassNode) {
                return getFinish(var.getInit());
            }
            Token token = tokenUtils.getNextNonEmptyToken(getFinishFixed(node) - 1);
            if (token != null && JsTokenId.OPERATOR_SEMICOLON == token.id()) {
                return ts.offset() + 1;
            } else {
                return getFinishFixed(node);
            }
        }

        return getFinishFixed(node);
    }

    private int getFinishFixed(Node node) {
        // All this magic is because nashorn nodes and tokens don't contain the
        // quotes for string. Due to this we call this method to add 1 to finish
        // in case it is string literal.
        int finish = node.getFinish();
        ts.move(finish);
        if (!ts.moveNext()) {
            return finish;
        }
        Token<? extends JsTokenId> token = ts.token();
        if (token.id() == JsTokenId.STRING_END || token.id() == JsTokenId.TEMPLATE_END) {
            return finish + 1;
        }

        return finish;
    }

    private boolean isScript(Block node) {
        if(!node.isFunctionBody()) {
            return false;
        }
        FunctionNode functionNode = getLexicalContext().getCurrentFunction();
        return functionNode != null && functionNode.isProgram();
    }

    private boolean isVirtual(Block block) {
        return block.getStart() == block.getFinish()
                    || com.oracle.js.parser.Token.descType(block.getToken()) != TokenType.LBRACE
                    || block.isCatchBlock();
    }

    private boolean isDeclaration(VarNode varNode) {
        if (varNode.isFunctionDeclaration() || varNode.isExport() || varNode.isDestructuring()) {
            return true;
        }
        if (varNode.getInit() instanceof ClassNode) {
            IdentNode cIdent = ((ClassNode) varNode.getInit()).getIdent();
            IdentNode vIdent = varNode.getName();
            // this is artificial var node for simple class declaration
            if (cIdent != null
                    && cIdent.getStart() == vIdent.getStart()
                    && cIdent.getFinish() == vIdent.getFinish()) {
                return true;
            }
        }
        return false;
    }
}
