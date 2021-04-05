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
package org.netbeans.modules.php.editor.indent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.editor.indent.FormatToken.AssignmentAnchorToken;
import org.netbeans.modules.php.editor.indent.TokenFormatter.DocumentOptions;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CastExpression;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FinallyClause;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.TraitConflictResolutionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TryStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class FormatVisitor extends DefaultVisitor {

    private static final Logger LOGGER = Logger.getLogger(FormatVisitor.class.getName());
    private final BaseDocument document;
    private final List<FormatToken> formatTokens;
    private final TokenSequence<PHPTokenId> ts;
    private final LinkedList<ASTNode> path;
    private final DocumentOptions options;
    private final ArrayDeque<GroupAlignmentTokenHolder> groupAlignmentTokenHolders;
    private final int caretOffset;
    private final int startOffset;
    private final int endOffset;
    private boolean includeWSBeforePHPDoc;
    private boolean isCurly; // whether the last visited block is curly or standard syntax.
    private boolean isMethodInvocationShifted; // is continual indentation already included ?
    private boolean isFirstUseStatementPart;
    private boolean isFirstUseTraitStatementPart;
    private int inArrayBalance;

    public FormatVisitor(BaseDocument document, DocumentOptions documentOptions, final int caretOffset, final int startOffset, final int endOffset) {
        this.document = document;
        ts = LexUtilities.getPHPTokenSequence(document, 0);
        path = new LinkedList<>();
        options = documentOptions;
        includeWSBeforePHPDoc = true;
        formatTokens = new ArrayList<>(ts == null ? 1 : ts.tokenCount() * 2);
        this.caretOffset = caretOffset;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        formatTokens.add(new FormatToken.InitToken());
        isMethodInvocationShifted = false;
        groupAlignmentTokenHolders = new ArrayDeque<>();
        inArrayBalance = 0;
    }

    public List<FormatToken> getFormatTokens() {
        return formatTokens;
    }

    @Override
    public void scan(ASTNode node) {
        if (node == null) {
            return;
        }

        // find comment before the node.
        List<FormatToken> beforeTokens = new ArrayList<>(30);
        int indexBeforeLastComment = -1;  // remember last comment
        while (moveNext() && ts.offset() < node.getStartOffset()
                && lastIndex < ts.index()
                && ((ts.offset() + ts.token().length()) <= node.getStartOffset()
                || ts.token().id() == PHPTokenId.PHP_CLOSETAG)) {
            if (ts.token().id() == PHPTokenId.PHP_CURLY_CLOSE
                    && path.size() > 1 && path.get(1) instanceof NamespaceDeclaration) {
                // this a a fix for probalem that namespace declaration through {}, doesn't end with the end of  }
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OTHER_RIGHT_BRACE, ts.offset()));
            }
            addFormatToken(beforeTokens);
            if (ts.token().id() == PHPTokenId.PHPDOC_COMMENT_START
                    || (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
                    && TokenUtilities.textEquals("//", ts.token().text())) // NOI18N
                    && indexBeforeLastComment == -1) {
                if (ts.movePrevious() && ts.token().id() == PHPTokenId.WHITESPACE) {
                    // don't change if the line comment or a comment starts on the same line
                    if (countOfNewLines(ts.token().text()) > 0) {
                        indexBeforeLastComment = beforeTokens.size() - 1;
                    } else {
                        // #268710
                        if (ts.movePrevious() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                            indexBeforeLastComment = beforeTokens.size() - 1;
                        }
                        ts.moveNext();
                    }
                }
                ts.moveNext();
            }
        }
        includeWSBeforePHPDoc = true;
        // in case of #268710, indexBeforeLastComment may be 0
        if (indexBeforeLastComment >= 0) { // if there is a comment, put the new lines befere the comment, not directly before the node.
            for (int i = 0; i < indexBeforeLastComment; i++) {
                formatTokens.add(beforeTokens.get(i));
            }
            if (node instanceof ClassDeclaration || node instanceof InterfaceDeclaration || node instanceof TraitDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
                includeWSBeforePHPDoc = false;
            } else if (node instanceof FunctionDeclaration || node instanceof MethodDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
                includeWSBeforePHPDoc = false;
            } else if (node instanceof FieldsDeclaration || node instanceof ConstantDeclaration) {
                if (isPreviousNodeTheSameInBlock(path.get(0), (Statement) node)) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_FIELDS, ts.offset()));
                } else {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELDS, ts.offset()));
                }
                includeWSBeforePHPDoc = false;
            } else if (node instanceof UseStatement) {
                if (isPreviousNodeTheSameInBlock(path.get(0), (Statement) node)) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_USE, ts.offset()));
                } else {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE, ts.offset()));
                }
                includeWSBeforePHPDoc = false;
            } else if (node instanceof UseTraitStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE_TRAIT, ts.offset()));
                includeWSBeforePHPDoc = false;
            }
            for (int i = indexBeforeLastComment; i < beforeTokens.size(); i++) {
                formatTokens.add(beforeTokens.get(i));
            }
        } else {
            formatTokens.addAll(beforeTokens);
        }

        ts.movePrevious();

        path.addFirst(node);
        super.scan(node);
        path.removeFirst();

        while (moveNext()
                && lastIndex < ts.index()
                && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
    }

    @Override
    public void scan(Iterable<? extends ASTNode> nodes) {
        super.scan(nodes);
    }

    @Override
    public void visit(StaticStatement node) {
        List<Expression> expressions = node.getExpressions();
        for (Expression expression : expressions) {
            addAllUntilOffset(expression.getStartOffset());
            if (moveNext() && lastIndex < ts.index()) {
                addFormatToken(formatTokens); // add the first token of the expression and then add the indentation
                formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
                scan(expression);
                formatTokens.add(new FormatToken.IndentToken(expression.getEndOffset(), -1 * options.continualIndentSize));
            }
        }
    }

    @Override
    public void visit(ArrayCreation node) {
        inArrayBalance++;
        int delta = options.indentArrayItems - options.continualIndentSize;
        boolean isShortArray = false;
        if (ts.token().id() != PHPTokenId.PHP_ARRAY && lastIndex <= ts.index() // it's possible that the expression starts with array
                && !TokenUtilities.textEquals(ts.token().text(), "[")  // NOI18N
                && !(path.size() > 1 && (path.get(1) instanceof FunctionName))) { // not ["ArrayCall", "test"]()
            while (ts.moveNext() && (ts.token().id() != PHPTokenId.PHP_ARRAY && !TokenUtilities.textEquals(ts.token().text(), "[")) && lastIndex < ts.index()) { //NOI18N
                addFormatToken(formatTokens);
            }
            if (formatTokens.get(formatTokens.size() - 1).getId() == FormatToken.Kind.WHITESPACE_INDENT
                    || path.get(1) instanceof ArrayElement
                    || path.get(1) instanceof FormalParameter
                    || path.get(1) instanceof CastExpression) {
                // when the array is on the beginning of the line, indent items in normal way
                delta = options.indentArrayItems;
            }
            delta = modifyDeltaForEnclosingFunctionInvocations(delta);
            if (path.get(1) instanceof FunctionInvocation) {
                int hindex = formatTokens.size() - 1;
                while (hindex > 0 && formatTokens.get(hindex).getId() != FormatToken.Kind.TEXT
                        && formatTokens.get(hindex).getId() != FormatToken.Kind.WHITESPACE_INDENT
                        && lastIndex < ts.index()) {
                    hindex--;
                }
                if (hindex > 0 && formatTokens.get(hindex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    delta = options.indentArrayItems;
                }
            }
            if (TokenUtilities.textEquals(ts.token().text(), "[")) { // NOI18N
                formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                isShortArray = true;
            } else if (lastIndex < ts.index()) {
                addFormatToken(formatTokens); // add only "array" keyword
            }
        }
        formatTokens.add(new FormatToken.IndentToken(ts.offset(), delta));
        if (isShortArray) {
            // #268171 this has to be added after an indent token
            // because indent token offset < WAADLP token offset
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN, ts.offset() + ts.token().length()));
        }
        createGroupAlignment();
        List<ArrayElement> arrayElements = node.getElements();
        if (arrayElements != null && arrayElements.size() > 0) {
            ArrayElement arrayElement = arrayElements.get(0);
            addAllUntilOffset(arrayElement.getStartOffset());
            scan(arrayElement);
            for (int i = 1; i < arrayElements.size(); i++) {
                arrayElement = arrayElements.get(i);
                addAllUntilOffset(arrayElement.getStartOffset());
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_ARRAY_ELEMENT_LIST, ts.offset() + ts.token().length()));
                scan(arrayElement);
            }
        }
        formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), -1 * delta));
        addAllUntilOffset(node.getEndOffset());
        resetGroupAlignment();
        inArrayBalance--;
    }

    private int modifyDeltaForEnclosingFunctionInvocations(int delta) {
        int depthInFunctionInvocation = 0;
        for (int i = 1; i < path.size(); i++) {
            if (path.get(i) instanceof FunctionInvocation) {
                depthInFunctionInvocation++;
            } else {
                break;
            }
        }
        // move indenting left for every enclosing function invocation
        return depthInFunctionInvocation > 1 ? delta + (-1 * options.continualIndentSize * (depthInFunctionInvocation - 1)) : delta;
    }

    @Override
    public void visit(ArrayElement node) {
        // ArrayCreation and ListVariable has ArrayElements
        ArrayCreation arrayCreation = getParentArrayCreation();
        boolean multilinedArray = arrayCreation != null ? isMultilinedNode(arrayCreation) : false;
        if (node.getKey() != null && node.getValue() != null) {
            scan(node.getKey());
            while (ts.moveNext() && ts.offset() < node.getValue().getStartOffset()) {
                if (isKeyValueOperator(ts.token())) {
                    handleGroupAlignment(node.getKey(), multilinedArray);
                }
                addFormatToken(formatTokens);
            }
            ts.movePrevious();
            scan(node.getValue());
        } else {
            super.visit(node);
        }
    }

    private boolean isMultilinedNode(ASTNode node) {
        boolean result = false;
        try {
            result = document.getText(node.getStartOffset(), node.getEndOffset() - node.getStartOffset()).contains("\n"); //NOI18N
        } catch (BadLocationException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return result;
    }

    @CheckForNull
    private ArrayCreation getParentArrayCreation() {
        ArrayCreation result = null;
        for (int i = 0; i < path.size(); i++) {
            ASTNode parentInPath = path.get(i);
            if (parentInPath instanceof ListVariable) {
                break;
            }
            if (parentInPath instanceof ArrayCreation) {
                result = (ArrayCreation) parentInPath;
                break;
            }
        }
        return result;
    }

    private static boolean isKeyValueOperator(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals("=>", token.text()); // NOI18N
    }

    @Override
    public void visit(ArrowFunctionDeclaration node) {
        scan(node.getFormalParameters());
        addReturnType(node.getReturnType());
        scan(node.getExpression());
    }

    @Override
    public void visit(Assignment node) {
        scan(node.getLeftHandSide());
        while (ts.moveNext() && ((ts.offset() + ts.token().length()) < node.getRightHandSide().getStartOffset())
                && ts.token().id() != PHPTokenId.PHP_OPERATOR) {
            addFormatToken(formatTokens);
        }
        if (ts.token().id() == PHPTokenId.PHP_OPERATOR) {
            if (path.size() > 1) {
                ASTNode parent = path.get(1);
                if (parent instanceof StaticStatement) {
                    VariableBase leftHandSide = node.getLeftHandSide();
                    if (leftHandSide instanceof Variable || leftHandSide instanceof FieldAccess) {
                        StaticStatement staticParent = (StaticStatement) parent;
                        handleGroupAlignment(leftHandSide.getEndOffset() - staticParent.getStartOffset());
                    }
                } else if (path.size() > 1 && !(parent instanceof ForStatement)) {
                    VariableBase leftHandSide = node.getLeftHandSide();
                    if (leftHandSide instanceof Variable || leftHandSide instanceof FieldAccess || leftHandSide instanceof StaticFieldAccess) {
                        handleGroupAlignment(leftHandSide);
                    }
                }
                addFormatToken(formatTokens);
            }
        } else {
            ts.movePrevious();
        }
        scan(node.getRightHandSide());
    }

    @Override
    public void visit(Block node) {
        resetAndCreateGroupAlignment(); // for every block reset group of alignment
        if (path.size() > 1 && (path.get(1) instanceof NamespaceDeclaration
                && !((NamespaceDeclaration) path.get(1)).isBracketed())) {
            // dont process blok for namespace
            super.visit(node);
            return;
        }

        isCurly = node.isCurly();
        // move ts in every case to the next token
        while (ts.moveNext() && node.isCurly() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN
                && (ts.offset() < node.getStartOffset())
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }

        ASTNode parent = path.get(1);

        if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
            if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration || parent instanceof TraitDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS_LEFT_BRACE, ts.offset()));
            } else if (isAnonymousClass(parent)) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ANONYMOUS_CLASS_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof IfStatement) {
                IfStatement ifStatement = (IfStatement) parent;
                if (ifStatement.getFalseStatement() != null
                        && ifStatement.getFalseStatement().getStartOffset() <= node.getStartOffset()) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ELSE_LEFT_BRACE, ts.offset()));
                } else {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_LEFT_BRACE, ts.offset()));
                }
            } else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof WhileStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof DoStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_DO_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof SwitchStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_LEFT_BACE, ts.offset()));
            } else if (parent instanceof TryStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_TRY_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof CatchClause) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof FinallyClause) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FINALLY_LEFT_BRACE, ts.offset()));
            } else if (parent instanceof UseTraitStatement) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, ts.offset()));
            } else {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OTHER_LEFT_BRACE, ts.offset()));
            }
            addFormatToken(formatTokens);

            boolean indentationIncluded = false;
            while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) == 0)
                    || (lastIndex < ts.index() && isComment(ts.token()))) { // #268920 check lastIndex to prevent an infinite loop
                if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT && !indentationIncluded) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
                    indentationIncluded = true;
                }
                addFormatToken(formatTokens);
            }

            if (!indentationIncluded) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
            }

            if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration || parent instanceof TraitDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLASS_LEFT_BRACE, ts.offset()));
            } else if (isAnonymousClass(parent)) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ANONYMOUS_CLASS_LEFT_BRACE, ts.offset()));
            } else {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_OTHER_LEFT_BRACE, ts.offset()));
            }

        }

        ts.movePrevious();


        super.visit(node);

        if (node.isCurly() && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
            while (ts.moveNext() && (ts.offset() + ts.token().length()) <= node.getEndOffset()) {
                if (ts.token().id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    FormatToken lastToken = formatTokens.get(formatTokens.size() - 1);

                    if (lastToken.getId() == FormatToken.Kind.WHITESPACE
                            || lastToken.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                        formatTokens.remove(formatTokens.size() - 1);
                        formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
                        formatTokens.add(lastToken);
                    } else {
                        formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
                    }

                    boolean includeWBC = false;  // is open after close ? {}
                    if (ts.movePrevious()
                            && (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN
                            || ts.token().id() == PHPTokenId.WHITESPACE)) {
                        if (ts.token().id() == PHPTokenId.WHITESPACE) {
                            if (ts.movePrevious() && ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                                includeWBC = true;
                            }
                            ts.moveNext();
                        }
                        if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                            includeWBC = true;
                        }
                    }
                    ts.moveNext();
                    if (includeWBC) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_OPEN_CLOSE_BRACES, ts.offset()));
                    }

                    if (parent instanceof ClassDeclaration || parent instanceof InterfaceDeclaration || parent instanceof TraitDeclaration) {
                        if (includeWSBeforePHPDoc) {
                            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS_RIGHT_BRACE, ts.offset()));
                        }
                        addFormatToken(formatTokens);
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLASS, ts.offset() + ts.token().length()));
                    } else if (isAnonymousClass(parent)) {
                        if (includeWSBeforePHPDoc) {
                            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ANONYMOUS_CLASS_RIGHT_BRACE, ts.offset()));
                        }
                        addFormatToken(formatTokens);
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ANONYMOUS_CLASS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_FUNCTION, ts.offset() + ts.token().length()));
                    } else if (parent instanceof IfStatement) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else if (parent instanceof ForStatement || parent instanceof ForEachStatement) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else if (parent instanceof SwitchStatement) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_RIGHT_BACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else if (parent instanceof CatchClause || parent instanceof TryStatement || parent instanceof FinallyClause) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else if (parent instanceof UseTraitStatement) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE_TRAIT_BODY_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    } else {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OTHER_RIGHT_BRACE, ts.offset()));
                        addFormatToken(formatTokens);
                    }
                } else {
                    FormatToken lastToken = formatTokens.get(formatTokens.size() - 1);
                    if (!(lastToken.getId() == FormatToken.Kind.TEXT && lastToken.getOffset() >= ts.offset())
                            && lastIndex < ts.index()) {
                        addFormatToken(formatTokens);
                    }
                }
            }
            ts.movePrevious();
        }
        resetGroupAlignment(); //reset alignment when leaving a block
    }

    @Override
    public void visit(CastExpression node) {
        super.visit(node);
    }

    @Override
    public void visit(InterfaceDeclaration node) {
        addAllUntilOffset(node.getStartOffset());
        if (includeWSBeforePHPDoc) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
        } else {
            includeWSBeforePHPDoc = true;
        }
        super.visit(node);
    }

    @Override
    public void visit(ClassDeclaration node) {

        addAllUntilOffset(node.getStartOffset());
        if (includeWSBeforePHPDoc) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
        } else {
            includeWSBeforePHPDoc = true;
        }
        while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) {
            switch (ts.token().id()) {
                case PHP_CLASS:
                    if (!ClassDeclaration.Modifier.NONE.equals(node.getModifier())) {
                        FormatToken lastWhitespace = formatTokens.remove(formatTokens.size() - 1);
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_MODIFIERS, lastWhitespace.getOffset(), lastWhitespace.getOldText()));
                    }
                    addFormatToken(formatTokens);
                    break;
                case PHP_IMPLEMENTS:
                    if (node.getInterfaes().size() > 0) {
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset()));
                        ts.movePrevious();
                        addListOfNodes(node.getInterfaes(), FormatToken.Kind.WHITESPACE_IN_INTERFACE_LIST);
                    }
                    break;
                case PHP_EXTENDS:
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset()));
                    addFormatToken(formatTokens);
                    break;
                default:
                    addFormatToken(formatTokens);
            }
        }

        ts.movePrevious();
        super.visit(node);
    }

    private void addListOfNodes(List<? extends ASTNode> nodes, FormatToken.Kind dividingToken) {
        addUnbreakalbeSequence(nodes.get(0), true);
        for (int i = 1; i < nodes.size(); i++) {
            if (ts.moveNext() && ts.token().id() == PHPTokenId.WHITESPACE) {
                addFormatToken(formatTokens);
            } else {
                ts.movePrevious();
            }
            formatTokens.add(new FormatToken(dividingToken, ts.offset() + ts.token().length()));
            addUnbreakalbeSequence(nodes.get(i), false);
        }
    }

    @Override
    public void visit(TraitDeclaration node) {
        addAllUntilOffset(node.getStartOffset());
        if (includeWSBeforePHPDoc) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLASS, ts.offset()));
        } else {
            includeWSBeforePHPDoc = true;
        }
        while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
        super.visit(node);
    }

    @Override
    public void visit(ClassInstanceCreation node) {
        scan(node.getClassName());
        if (node.isAnonymous()) {
            while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_CURLY_OPEN) {
                switch (ts.token().id()) {
                    case PHP_CLASS:
                        addFormatToken(formatTokens);
                        List<Expression> ctorParams = node.ctorParams();
                        if (!ctorParams.isEmpty()) {
                            processArguments(ctorParams);
                        }
                        break;
                    case PHP_IMPLEMENTS:
                        if (node.getInterfaces().size() > 0) {
                            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset()));
                            ts.movePrevious();
                            addListOfNodes(node.getInterfaces(), FormatToken.Kind.WHITESPACE_IN_INTERFACE_LIST);
                        }
                        break;
                    case PHP_EXTENDS:
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS, ts.offset()));
                        addFormatToken(formatTokens);
                        scan(node.getSuperClass());
                        break;
                    default:
                        addFormatToken(formatTokens);
                }
            }

            ts.movePrevious();
            scan(node.getBody());
        } else {
            if (node.ctorParams() != null && node.ctorParams().size() > 0) {
                boolean addIndentation = !(path.get(1) instanceof ReturnStatement
                        || path.get(1) instanceof Assignment
                        || path.get(1) instanceof ExpressionStatement)
                        || (path.size() > 2 && (path.get(1) instanceof ArrayElement) && (path.get(2) instanceof ArrayCreation));
                if (addIndentation) {
                    formatTokens.add(new FormatToken.IndentToken(node.getClassName().getEndOffset(), options.continualIndentSize));
                }
                processArguments(node.ctorParams());
                if (addIndentation) {
                    formatTokens.add(new FormatToken.IndentToken(node.ctorParams().get(node.ctorParams().size() - 1).getEndOffset(), -1 * options.continualIndentSize));
                }
                addAllUntilOffset(node.getEndOffset());
            } else {
                super.visit(node);
            }
        }
    }

    private void processArguments(final List<Expression> arguments) {
        while (ts.moveNext() && ts.offset() < arguments.get(0).getStartOffset()
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
        addListOfNodes(arguments, FormatToken.Kind.WHITESPACE_IN_ARGUMENT_LIST);
    }

    @Override
    public void visit(ConditionalExpression node) {
        scan(node.getCondition());
        boolean putContinualIndent = true;
        for (ASTNode astNode : path) {
            if (astNode instanceof Assignment
                    || astNode instanceof ReturnStatement) {
                putContinualIndent = false;
                break;
            }
        }
        if (node.getOperator().isShortened()) {
            visitShortenedConditionalExpression(node, putContinualIndent);
        } else {
            visitConditionalExpression(node, putContinualIndent);
        }
    }

    private void visitShortenedConditionalExpression(ConditionalExpression node, boolean putContinualIndent) {
        assert node.getIfTrue() == null : node.getIfTrue().toString();
        final ConditionalExpression.OperatorType operator = node.getOperator();
        // XXX unify once one token for elvis exists
        switch (operator) {
            case ELVIS:
                // "?" part
                while (ts.moveNext()
                        && !(ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals("?", ts.token().text())) // NOI18N
                        && lastIndex < ts.index()) {
                    addFormatToken(formatTokens);
                }
                assert ts.token().id() == PHPTokenId.PHP_TOKEN : ts.token().id();
                assert TokenUtilities.textEquals("?", ts.token().text()) : ts.token().text().toString();
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
                }
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, ts.offset()));
                formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
                }
                // remove all next whitespaces
                StringBuilder sb = new StringBuilder();
                while (ts.moveNext()) {
                    if (ts.token().id() != PHPTokenId.WHITESPACE) {
                        ts.movePrevious();
                        break;
                    }
                    sb.append(ts.token().text().toString());
                }
                if (sb.length() > 0) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_SHORT_TERNARY_OP, ts.offset(), sb.toString()));
                }
                // ":" part
                assert node.getIfFalse() != null;
                while (ts.moveNext()
                        && !(ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(":", ts.token().text())) // NOI18N
                        && lastIndex < ts.index()) {
                    addFormatToken(formatTokens);
                }
                assert ts.token().id() == PHPTokenId.PHP_TOKEN : ts.token().id();
                assert TokenUtilities.textEquals(":", ts.token().text()) : ts.token().text().toString();
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
                }
                formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset() + ts.token().length()));
                addAllUntilOffset(node.getIfFalse().getStartOffset());
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
                scan(node.getIfFalse());
                addEndOfUnbreakableSequence(node.getIfFalse().getEndOffset());
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
                }
                break;
            default:
                // operator part
                while (ts.moveNext()
                        && !operator.isOperatorToken(ts.token())
                        && lastIndex < ts.index()) {
                    addFormatToken(formatTokens);
                }
                assert operator.isOperatorToken(ts.token()) : ts.token().id();
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
                }
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, ts.offset()));
                formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset() + ts.token().length()));
                // condition part
                addAllUntilOffset(node.getIfFalse().getStartOffset());
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
                scan(node.getIfFalse());
                addEndOfUnbreakableSequence(node.getIfFalse().getEndOffset());
                if (putContinualIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
                }
        }
    }

    private void visitConditionalExpression(ConditionalExpression node, boolean putContinualIndent) {
        assert node.getIfTrue() != null;
        // "?" part
        while (ts.moveNext()
                && !(ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals("?", ts.token().text())) // NOI18N
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        assert ts.token().id() == PHPTokenId.PHP_TOKEN : ts.token().id();
        assert TokenUtilities.textEquals("?", ts.token().text()) : ts.token().text().toString();
        if (putContinualIndent) {
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
        }
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, ts.offset()));
        formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset() + ts.token().length()));
        addAllUntilOffset(node.getIfTrue().getStartOffset());
        formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        scan(node.getIfTrue());
        addEndOfUnbreakableSequence(node.getIfTrue().getEndOffset());
        if (putContinualIndent) {
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
        // ":" part
        assert node.getIfFalse() != null;
        while (ts.moveNext()
                && !(ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(":", ts.token().text())) // NOI18N
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        assert ts.token().id() == PHPTokenId.PHP_TOKEN : ts.token().id();
        assert TokenUtilities.textEquals(":", ts.token().text()) : ts.token().text().toString();
        if (putContinualIndent) {
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
        }
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_TERNARY_OP, ts.offset()));
        formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_TERNARY_OP, ts.offset() + ts.token().length()));
        addAllUntilOffset(node.getIfFalse().getStartOffset());
        formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        scan(node.getIfFalse());
        addEndOfUnbreakableSequence(node.getIfFalse().getEndOffset());
        if (putContinualIndent) {
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
    }

    @Override
    public void visit(ConstantDeclaration node) {
        if (path.size() > 1 && path.get(1) instanceof Block) {
            Block block = (Block) path.get(1);
            int index = 0;
            List<Statement> statements = block.getStatements();
            while (index < statements.size() && statements.get(index).getStartOffset() < node.getStartOffset()) {
                index++;
            }
            addAllUntilOffset(node.getStartOffset());
            if (includeWSBeforePHPDoc && index < statements.size()
                    && index > 0 && statements.get(index - 1) instanceof ConstantDeclaration) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_FIELDS, ts.offset()));
            } else {
                if (includeWSBeforePHPDoc) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELDS, ts.offset()));
                } else {
                    includeWSBeforePHPDoc = true;
                }
            }
            while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_STRING) {
                addFormatToken(formatTokens);
            }
            FormatToken lastWhitespace = formatTokens.remove(formatTokens.size() - 1);
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_MODIFIERS, lastWhitespace.getOffset(), lastWhitespace.getOldText()));
            addFormatToken(formatTokens);
            formatTokens.add(new FormatToken.IndentToken(node.getStartOffset(), options.continualIndentSize));
            scan(node.getNames());
            if (node.getNames().size() == 1) {
                while (ts.moveNext()
                        && (ts.token().id() != PHPTokenId.PHP_TOKEN && ts.token().id() != PHPTokenId.PHP_OPERATOR)) {
                    addFormatToken(formatTokens);
                }
                if (ts.token().id() == PHPTokenId.PHP_TOKEN
                        || ts.token().id() == PHPTokenId.PHP_OPERATOR) {
                    handleGroupAlignment(node.getNames().get(0));
                    addFormatToken(formatTokens);
                } else {
                    ts.movePrevious();
                }

            }
            scan(node.getInitializers());
            if (ts.token().id() == PHPTokenId.PHP_SELF || ts.token().id() == PHPTokenId.PHP_PARENT) {
                // NETBEANS-3103 check whether the token behind self|parent is "::"
                // e.g. const CONSTANT = self::;
                int originalOffset = ts.offset();
                if (ts.moveNext()) {
                    if (ts.token().id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM) { // ::
                        addFormatToken(formatTokens);
                    } else {
                        ts.move(originalOffset);
                        ts.moveNext();
                    }
                }
            }
            formatTokens.add(new FormatToken.IndentToken(node.getStartOffset(), options.continualIndentSize * -1));
            if (index == statements.size() - 1
                    || ((index < statements.size() - 1) && !(statements.get(index + 1) instanceof ConstantDeclaration))) {
                addRestOfLine();
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_FIELDS, ts.offset() + ts.token().length()));
            }
        } else {
            addAllUntilOffset(node.getStartOffset());
            if (moveNext() && lastIndex < ts.index()) {
                // #257241 add indent tokens after const keyword
                addFormatToken(formatTokens);
                formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
                super.visit(node);
                formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), options.continualIndentSize * -1));
            }
        }
    }

    @Override
    public void visit(DoStatement node) {
        ASTNode body = node.getBody();
        if (body != null && !(body instanceof Block)) {
            addAllUntilOffset(body.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_DO_STATEMENT, ts.offset()));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
            scan(body);
            addEndOfUnbreakableSequence(body.getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
        } else {
            scan(body);
        }
        scan(node.getCondition());
        addAllUntilOffset(node.getEndOffset());
    }

    @Override
    public void visit(ExpressionStatement node) {
        if (node.getExpression() instanceof FunctionInvocation) {
            super.visit(node);
        } else {
            addAllUntilOffset(node.getStartOffset());
            if (moveNext() && lastIndex < ts.index()) {
                addFormatToken(formatTokens); // add the first token of the expression and then add the indentation
                Expression expression = node.getExpression();
                boolean addIndent = !(expression instanceof MethodInvocation || expression instanceof StaticMethodInvocation);
                if (expression instanceof Assignment) {
                    // anonymous classes
                    Assignment assignment = (Assignment) expression;
                    Expression right = assignment.getRightHandSide();
                    if (isAnonymousClass(right)) {
                        addIndent = false;
                    }
                }
                if (addIndent) {
                    formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
                    super.visit(node);
                    formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), -1 * options.continualIndentSize));
                } else {
                    super.visit(node);
                }

            }
        }
    }

    @Override
    public void visit(FieldsDeclaration node) {
        Block block = (Block) path.get(1);
        int index = 0;
        List<Statement> statements = block.getStatements();
        while (index < statements.size() && statements.get(index).getStartOffset() < node.getStartOffset()) {
            index++;
        }
        addAllUntilOffset(node.getStartOffset());
        if (includeWSBeforePHPDoc && index < statements.size()
                && index > 0 && statements.get(index - 1) instanceof FieldsDeclaration) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_FIELDS, ts.offset()));
        } else {
            if (includeWSBeforePHPDoc) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FIELDS, ts.offset()));
            } else {
                includeWSBeforePHPDoc = true;
            }
        }
        while (ts.moveNext() && !isFieldTypeOrVariableToken(ts.token())) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
        FormatToken lastWhitespace = formatTokens.remove(formatTokens.size() - 1);
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_MODIFIERS, lastWhitespace.getOffset(), lastWhitespace.getOldText()));
        if (node.getFields().size() > 1) {
            formatTokens.add(new FormatToken.IndentToken(node.getStartOffset(), options.continualIndentSize));
            super.visit(node);
            formatTokens.add(new FormatToken.IndentToken(node.getStartOffset(), options.continualIndentSize * -1));
        } else {
            super.visit(node);
        }
        if (index == statements.size() - 1
                || ((index < statements.size() - 1) && !(statements.get(index + 1) instanceof FieldsDeclaration))) {
            addRestOfLine();
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_FIELDS, ts.offset() + ts.token().length()));
        }
    }

    @Override
    public void visit(ForEachStatement node) {
        scan(node.getExpression());
        boolean wrap = node.getKey() != null;
        if (wrap) {
            int start = node.getKey().getStartOffset();
            addAllUntilOffset(node.getKey().getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        }
        scan(node.getKey());
        if (wrap) {
            addEndOfUnbreakableSequence(node.getKey().getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
        wrap = node.getValue() != null;
        if (wrap) {
            int start = node.getValue().getStartOffset();
            addAllUntilOffset(node.getValue().getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        }
        scan(node.getValue());
        if (wrap) {
            addEndOfUnbreakableSequence(node.getValue().getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
        ASTNode body = node.getStatement();
        if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
            addAllUntilOffset(body.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
            scan(node.getStatement());
            while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_ENDFOREACH) {
                addFormatToken(formatTokens);
            }
            ts.movePrevious();
            formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
        } else if (body != null && !(body instanceof Block)) {
            addNoCurlyBody(body, FormatToken.Kind.WHITESPACE_BEFORE_FOR_STATEMENT);
        } else {
            scan(node.getStatement());
        }
    }

    @Override
    public void visit(ForStatement node) {
        scan(node.getInitializers());
        boolean wrap = node.getConditions() != null && node.getConditions().size() > 0 ? true : false;
        if (wrap) {
            int start = node.getConditions().get(0).getStartOffset();
            addAllUntilOffset(start);
            formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        }
        scan(node.getConditions());
        if (wrap) {
            addEndOfUnbreakableSequence(node.getConditions().get(node.getConditions().size() - 1).getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
        wrap = node.getUpdaters() != null && node.getUpdaters().size() > 0 ? true : false;
        if (wrap) {
            int start = node.getUpdaters().get(0).getStartOffset();
            addAllUntilOffset(start);
            formatTokens.add(new FormatToken.IndentToken(start, options.continualIndentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_FOR, start));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(start, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        }
        scan(node.getUpdaters());
        if (wrap) {
            addEndOfUnbreakableSequence(node.getUpdaters().get(node.getUpdaters().size() - 1).getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        }
        ASTNode body = node.getBody();
        if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
            addAllUntilOffset(body.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
            scan(node.getBody());
            while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_ENDFOR) {
                addFormatToken(formatTokens);
            }
            ts.movePrevious();
            formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
        } else if (body != null && !(body instanceof Block)) {
            addNoCurlyBody(body, FormatToken.Kind.WHITESPACE_BEFORE_FOR_STATEMENT);
        } else {
            scan(node.getBody());
        }
    }

    @Override
    public void visit(FunctionDeclaration node) {
        if (!(path.size() > 1 && path.get(1) instanceof MethodDeclaration)) {
            while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE
                    || isComment(ts.token()))) {
                addFormatToken(formatTokens);
            }
            if (includeWSBeforePHPDoc) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
            } else {
                includeWSBeforePHPDoc = true;
            }
            ts.movePrevious();
        }
        scan(node.getFunctionName());

        // #270903 add indent
        formatTokens.add(new FormatToken.IndentToken(node.getFunctionName().getEndOffset(), options.continualIndentSize));

        List<FormalParameter> parameters = node.getFormalParameters();
        if (parameters != null && parameters.size() > 0) {
            while (ts.moveNext() && ts.offset() < parameters.get(0).getStartOffset()
                    && lastIndex < ts.index()) {
                addFormatToken(formatTokens);
            }
            ts.movePrevious();
            addListOfNodes(parameters, FormatToken.Kind.WHITESPACE_IN_PARAMETER_LIST);
        }

        // #270903 add indent
        int indentEndOffset;
        Expression returnType = node.getReturnType();
        Block body = node.getBody();
        if (returnType != null) {
            indentEndOffset = returnType.getStartOffset();
        } else if (body != null){
            indentEndOffset = body.getStartOffset();
        } else {
            indentEndOffset = node.getEndOffset();
        }
        formatTokens.add(new FormatToken.IndentToken(indentEndOffset, -1 * options.continualIndentSize));

        addReturnType(node.getReturnType());
        scan(node.getBody());
    }

    @Override
    public void visit(LambdaFunctionDeclaration node) {
        scan(node.getFormalParameters());
        addReturnType(node.getReturnType());
        scan(node.getLexicalVariables());
        Block body = node.getBody();
        if (body != null) {
            // in case of (function(){echo "foo";})() and fn() => function() use ($y) {return $y;}, missing an indent
            boolean addIndent = isParentParenthesisExpr() || isParentArrowFunctionParenthesisExpr();
            if (addIndent) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
            }

            addAllUntilOffset(body.getStartOffset());
            if (inArrayBalance == 0) {
                formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), -1 * options.continualIndentSize));
            }
            scan(body);
            if (inArrayBalance == 0) {
                formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), options.continualIndentSize));
            }

            if (addIndent) {
                formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), -1 * options.continualIndentSize));
            }
        }
    }

    private boolean isParentParenthesisExpr() {
        return path.size() > 1
                && (path.get(1) instanceof ParenthesisExpression);
    }

    private boolean isParentArrowFunctionParenthesisExpr() {
        return path.size() > 2
                && (path.get(1) instanceof ArrowFunctionDeclaration)
                && (path.get(2) instanceof ParenthesisExpression);
    }

    private void addReturnType(@NullAllowed Expression returnType) {
        if (returnType == null) {
            return;
        }
        int endPosition = returnType.getEndOffset();
        if (returnType instanceof NullableType) {
            NullableType nullableType = (NullableType) returnType;
            endPosition = nullableType.getStartOffset();
        }
        while (ts.moveNext()
                && ts.offset() < endPosition
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
        if (returnType instanceof NullableType) {
            scan((NullableType) returnType);
        }
    }

    @Override
    public void visit(FunctionInvocation node) {
        if (path.size() > 1 && path.get(1) instanceof MethodInvocation) {
            while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_OBJECT_OPERATOR
                    && ((ts.offset() + ts.token().length()) < node.getStartOffset())) {
                addFormatToken(formatTokens);
            }
            if (ts.token().id() == PHPTokenId.PHP_OBJECT_OPERATOR) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_IN_CHAINED_METHOD_CALLS, ts.offset()));
                addFormatToken(formatTokens);
            } else {
                ts.movePrevious();
            }

        }
        scan(node.getFunctionName());
        List<Expression> parameters = node.getParameters();
        if (parameters != null && parameters.size() > 0) {
            boolean addIndentation = !(path.get(1) instanceof ReturnStatement
                    || path.get(1) instanceof Assignment
                    || (path.size() > 2 && path.get(1) instanceof MethodInvocation && path.get(2) instanceof Assignment));
            FormatToken.IndentToken indentToken = new FormatToken.IndentToken(node.getFunctionName().getEndOffset(), options.continualIndentSize);
            if (addIndentation) {
                formatTokens.add(indentToken);
            }
            processArguments(parameters);

            // remove an indent token when the last param is an anonymous class
            addIndentation = removeIndentTokenForAnonymousClass(parameters, indentToken, addIndentation);

            if (addIndentation) {
                List<FormatToken> removed = new ArrayList<>();
                FormatToken ftoken = formatTokens.get(formatTokens.size() - 1);
                while (ftoken.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END
                        || (ftoken.isWhitespace() && ftoken.getId() != FormatToken.Kind.WHITESPACE_INDENT)
                        || ftoken.getId() == FormatToken.Kind.COMMENT
                        || ftoken.getId() == FormatToken.Kind.COMMENT_START
                        || ftoken.getId() == FormatToken.Kind.COMMENT_END
                        || ftoken.getId() == FormatToken.Kind.INDENT
                        || (ftoken.getId() == FormatToken.Kind.TEXT && (")".equals(ftoken.getOldText()) || "]".equals(ftoken.getOldText())))) { // NOI18N
                    formatTokens.remove(formatTokens.size() - 1);
                    removed.add(ftoken);
                    ftoken = formatTokens.get(formatTokens.size() - 1);
                }
                if (ftoken.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    formatTokens.remove(formatTokens.size() - 1); // remove WHITESPACE_INDENT
                    formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), -1 * options.continualIndentSize));
                    formatTokens.add(ftoken); // re-add WHITESPACE_INDENT
                    for (int i = removed.size() - 1; i > -1; i--) {
                        formatTokens.add(removed.get(i));
                    }
                } else {
                    for (int i = removed.size() - 1; i > -1; i--) {
                        formatTokens.add(removed.get(i));
                    }
                    formatTokens.add(new FormatToken.IndentToken(node.getEndOffset(), -1 * options.continualIndentSize));
                }
            }
        }
        addAllUntilOffset(node.getEndOffset());
    }

    private boolean removeIndentTokenForAnonymousClass(List<Expression> parameters, FormatToken.IndentToken indentToken, boolean addIndentation) {
        boolean addIndent = addIndentation;
        if (options.wrapMethodCallArgs == CodeStyle.WrapStyle.WRAP_ALWAYS
                || parameters.isEmpty()) {
            return addIndent;
        }
        Expression firstParameter = parameters.get(0);
        if (!isAnonymousClass(parameters.get(parameters.size() - 1))) {
            return addIndent;
        }

        for (Expression parameter : parameters) {
            if (isAnonymousClass(parameter)) {
                int index = formatTokens.size() - 1;
                FormatToken lastFormatToken = formatTokens.get(index);
                while (lastFormatToken.getOffset() >= firstParameter.getStartOffset()) {
                    index--;
                    lastFormatToken = formatTokens.get(index);
                    if (parameter.getStartOffset() == lastFormatToken.getOffset()) {
                        if (index - 1 >= 0
                                && formatTokens.get(index - 1).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                            lastFormatToken = formatTokens.get(index - 1);
                            break;
                        }
                    }
                }
                if (lastFormatToken.getId() != FormatToken.Kind.WHITESPACE_INDENT) {
                    formatTokens.remove(indentToken);
                    addIndent = false;
                }
                break;
            }
        }
        return addIndent;
    }

    @Override
    public void visit(InfixExpression node) {
        scan(node.getLeft());
        FormatToken.Kind whitespaceBefore = FormatToken.Kind.WHITESPACE_BEFORE_BINARY_OP;
        FormatToken.Kind whitespaceAfter = FormatToken.Kind.WHITESPACE_AFTER_BINARY_OP;

        if (node.getOperator() == InfixExpression.OperatorType.CONCAT) {
            whitespaceAfter = FormatToken.Kind.WHITESPACE_AROUND_CONCAT_OP;
            whitespaceBefore = whitespaceAfter;
        }

        while (ts.moveNext() && ts.offset() < node.getRight().getStartOffset()
                && ts.token().id() != PHPTokenId.PHP_TOKEN && !LexUtilities.isPHPOperator(ts.token().id())
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        // don't add to AND, OR, and XOR (PHPTokenId.PHP_TEXTUAL_OPERATOR)
        // see https://netbeans.org/bugzilla/show_bug.cgi?id=240274
        if (ts.token().id() == PHPTokenId.PHP_TOKEN || ts.token().id() == PHPTokenId.PHP_OPERATOR) {
            formatTokens.add(new FormatToken(whitespaceBefore, ts.offset()));
            addFormatToken(formatTokens);
            formatTokens.add(new FormatToken(whitespaceAfter, ts.offset() + ts.token().length()));
        } else {
            ts.movePrevious();
        }
        scan(node.getRight());
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "BC_IMPOSSIBLE_INSTANCEOF", justification = "Incorrect FB analysis") // NOI18N
    @Override
    public void visit(IfStatement node) {
        addAllUntilOffset(node.getCondition().getStartOffset());
        formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
        scan(node.getCondition());
        Statement trueStatement = node.getTrueStatement();
        formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
        // #268541
        boolean isTrueStatementCurly = false;
        if (trueStatement != null && trueStatement instanceof Block && !((Block) trueStatement).isCurly()) {
            isCurly = false;
            addAllUntilOffset(trueStatement.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(trueStatement.getStartOffset(), options.indentSize));
            scan(trueStatement);
            if (ts.token().id() == PHPTokenId.T_INLINE_HTML
                    && ts.moveNext() && ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                addFormatToken(formatTokens);
            }
            formatTokens.add(new FormatToken.IndentToken(trueStatement.getEndOffset(), -1 * options.indentSize));
        } else if (trueStatement != null && !(trueStatement instanceof Block)) {
            isCurly = false;
            addNoCurlyBody(trueStatement, FormatToken.Kind.WHITESPACE_BEFORE_IF_ELSE_STATEMENT);
        } else {
            isTrueStatementCurly = true;
            scan(trueStatement);
        }
        Statement falseStatement = node.getFalseStatement();
        if (falseStatement != null && falseStatement instanceof Block && !((Block) falseStatement).isCurly()
                && !(falseStatement instanceof IfStatement)) {
            isCurly = false;
            while (ts.moveNext() && ts.offset() < falseStatement.getStartOffset()) {
                if (ts.token().id() == PHPTokenId.PHP_ELSE || ts.token().id() == PHPTokenId.PHP_ELSEIF) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                } else if (lastIndex < ts.index()) {
                    addFormatToken(formatTokens);
                }
            }
            formatTokens.add(new FormatToken.IndentToken(falseStatement.getStartOffset(), options.indentSize));
            ts.movePrevious();
            scan(falseStatement);
            formatTokens.add(new FormatToken.IndentToken(falseStatement.getEndOffset(), -1 * options.indentSize));
        } else if (falseStatement != null && !(falseStatement instanceof Block) && !(falseStatement instanceof IfStatement)) {
            isCurly = false;
            while (ts.moveNext() && ts.offset() < falseStatement.getStartOffset()) {
                if (ts.token().id() == PHPTokenId.PHP_ELSE || ts.token().id() == PHPTokenId.PHP_ELSEIF) {
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ELSE_WITHOUT_CURLY, ts.offset()));
                    formatTokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                } else {
                    addFormatToken(formatTokens);
                }
            }
            ts.movePrevious();
            addAllUntilOffset(falseStatement.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(falseStatement.getStartOffset(), options.indentSize));
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_ELSE_STATEMENT, ts.offset()));
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
            scan(falseStatement);
            addEndOfUnbreakableSequence(falseStatement.getEndOffset());
            formatTokens.add(new FormatToken.IndentToken(falseStatement.getEndOffset(), -1 * options.indentSize));
        } else {
            isCurly = isTrueStatementCurly;
            scan(falseStatement);
        }

    }

    @Override
    public void visit(MethodDeclaration node) {
        while (ts.moveNext() && (ts.token().id() == PHPTokenId.WHITESPACE
                || isComment(ts.token())) && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        if (includeWSBeforePHPDoc) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FUNCTION, ts.offset()));
        } else {
            includeWSBeforePHPDoc = true;
        }
        if (lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_STRING) {
            switch (ts.token().id()) {
                case PHP_FUNCTION:
                    if (node.getModifier() > 0) {
                        FormatToken lastWhitespace = formatTokens.remove(formatTokens.size() - 1);
                        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_MODIFIERS, lastWhitespace.getOffset(), lastWhitespace.getOldText()));
                    }
                    addFormatToken(formatTokens);
                    break;
                default:
                    addFormatToken(formatTokens);
            }
        }
        ts.movePrevious();
        super.visit(node);
    }

    @Override
    public void visit(MethodInvocation node) {
        boolean shift = false;
        if (!isMethodInvocationShifted) {
            try {
                int startText = node.getDispatcher().getEndOffset();
                int endText = node.getMethod().getStartOffset();
                if (document.getText(startText, endText - startText).contains("\n")) {
                    shift = true;
                    addAllUntilOffset(node.getStartOffset());
                    boolean addIndent = !(path.size() > 1 && (path.get(1) instanceof Assignment));

                    // anonymous classes
                    if (options.wrapMethodCallArgs != CodeStyle.WrapStyle.WRAP_ALWAYS) {
                        FunctionInvocation method = node.getMethod();
                        List<Expression> parameters = method.getParameters();
                        for (Expression parameter : parameters) {
                            if (isAnonymousClass(parameter)) {
                                addIndent = false;
                                break;
                            }
                        }
                    }

                    if (addIndent) {
                        formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
                    }
                    isMethodInvocationShifted = true;
                    super.visit(node);
                    addAllUntilOffset(node.getEndOffset());
                    if (addIndent) {
                        formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), -1 * options.continualIndentSize));
                    }
                    isMethodInvocationShifted = false;
                }
            } catch (BadLocationException ex) {
                LOGGER.log(Level.WARNING, "Exception in scanning method invocation", ex);  //NOI18N
                shift = false;
            }

        }
        if (!shift) {
            super.visit(node);
        }
    }

    @Override
    public void visit(NamespaceDeclaration node) {
        addAllUntilOffset(node.getStartOffset());
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_NAMESPACE, node.getStartOffset()));
        scan(node.getName());
        addRestOfLine();
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_NAMESPACE, ts.offset() + ts.token().length()));
        scan(node.getBody());
    }

    @Override
    public void visit(Program program) {
        if (ts != null) {
            path.addFirst(program);
            ts.move(0);
            ts.moveNext();
            ts.movePrevious();
            addFormatToken(formatTokens);
            super.visit(program);
            FormatToken lastToken = formatTokens.size() > 0
                    ? formatTokens.get(formatTokens.size() - 1)
                    : null;
            while (ts.moveNext()) {
                if (lastToken == null || lastToken.isWhitespace() || lastToken.getOffset() > ts.offset()) {
                    if (lastIndex < ts.index()) {
                        addFormatToken(formatTokens);
                        lastToken = formatTokens.get(formatTokens.size() - 1);
                    }
                }
            }
            path.removeFirst();
        }
    }

    @Override
    public void visit(ReturnStatement node) {
        while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_RETURN
                && ((ts.offset() + ts.token().length()) <= node.getEndOffset())) {
            addFormatToken(formatTokens);
        }

        boolean addIndent = !isAnonymousClass(node.getExpression());

        if (ts.token().id() == PHPTokenId.PHP_RETURN) {
            addFormatToken(formatTokens);
            if (addIndent) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
            }
            super.visit(node);
            if (addIndent) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.continualIndentSize));
            }
        }
    }

    @Override
    public void visit(SingleFieldDeclaration node) {
        Variable name = node.getName();
        scan(name);
        if (node.getValue() != null) {
            while (ts.moveNext() && ts.offset() < node.getValue().getStartOffset()) {
                ASTNode parent = path.get(1);
                assert (parent instanceof FieldsDeclaration);
                FieldsDeclaration fieldsDeclaration = (FieldsDeclaration) path.get(1);
                if (ts.token().id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals("=", ts.token().text())) { //NOI18N
                    int realNodeLength = fieldsDeclaration.getModifierString().length() + " ".length() + name.getEndOffset() - name.getStartOffset(); //NOI18N
                    handleGroupAlignment(realNodeLength);
                    addFormatToken(formatTokens);
                } else {
                    addFormatToken(formatTokens);
                }
            }
            ts.movePrevious();
            if (node.getValue() != null) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), options.continualIndentSize));
                scan(node.getValue());
                formatTokens.add(new FormatToken.IndentToken(ts.offset() + ts.token().length(), -1 * options.continualIndentSize));
            }
        }
    }

    @Override
    public void visit(SwitchCase node) {
        if (node.getValue() == null) {
            ts.moveNext();
            if (lastIndex < ts.index()) {
                addFormatToken(formatTokens);
            }
        } else {
            scan(node.getValue());
        }
        formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
        if (node.getActions() != null) {
            scan(node.getActions());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
        }

    }

    @Override
    public void visit(SwitchStatement node) {
        scan(node.getExpression());
        if (node.getBody() != null && !((Block) node.getBody()).isCurly()) {
            addAllUntilOffset(node.getBody().getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(node.getBody().getStartOffset(), options.indentSize));

            if (node.getBody().getStatements().size() > 0) {
                scan(node.getBody());
                Statement lastOne = node.getBody().getStatements().get(node.getBody().getStatements().size() - 1);
                while (lastOne.getEndOffset() < formatTokens.get(formatTokens.size() - 1).getOffset()) {
                    formatTokens.remove(formatTokens.size() - 1);
                }
                while (lastOne.getEndOffset() < ts.offset()) {
                    ts.movePrevious();
                }
            } else {
                while (ts.moveNext() && ts.token().id() != PHPTokenId.PHP_ENDSWITCH
                        && ts.offset() < node.getBody().getEndOffset()) {
                    addFormatToken(formatTokens);
                }
                ts.movePrevious();
            }
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
            addAllUntilOffset(node.getEndOffset());
        } else {
            scan(node.getBody());
        }
    }

    @Override
    public void visit(TryStatement node) {
        scan(node.getBody());
        scan(node.getCatchClauses());
        scan(node.getFinallyClause());
    }

    @Override
    public void visit(CatchClause node) {
        addAllUntilOffset(node.getStartOffset());
        List<Expression> classNames = node.getClassNames();
        boolean addIndent = !classNames.isEmpty() && classNames.size() > 1;
        if (addIndent) {
            addAllUntilOffset(classNames.get(0).getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize));
        }
        scan(node.getClassNames());
        scan(node.getVariable());
        if (addIndent) {
            formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.continualIndentSize * -1));
        }
        scan(node.getBody());
    }

    @Override
    public void visit(WhileStatement node) {
        scan(node.getCondition());
        ASTNode body = node.getBody();
        if (body != null && (body instanceof Block && !((Block) body).isCurly())) {
            addAllUntilOffset(body.getStartOffset());
            formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
            scan(node.getBody());
            if (ts.token().id() == PHPTokenId.T_INLINE_HTML
                    && ts.moveNext() && ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                addFormatToken(formatTokens);
            }
            formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
        } else if (body != null && !(body instanceof Block)) {
            addNoCurlyBody(body, FormatToken.Kind.WHITESPACE_BEFORE_WHILE_STATEMENT);
        } else {
            scan(node.getBody());
        }
    }

    @Override
    public void visit(UseStatement node) {

        if (isPreviousNodeTheSameInBlock(path.get(1), node)) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_USE, ts.offset()));
        } else {
            if (includeWSBeforePHPDoc) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE, ts.offset()));
            }
        }
        includeWSBeforePHPDoc = true;

        isFirstUseStatementPart = true;
        super.visit(node);
        if (isNextNodeTheSameInBlock(path.get(1), node)) {
            addRestOfLine();
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_USE, ts.offset() + ts.token().length()));
        }
    }

    @Override
    public void visit(UseTraitStatement node) {
        if (includeWSBeforePHPDoc
                && isFirstUseTraitStatementInBlock(path.get(1), node)) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE_TRAIT, ts.offset()));
        }
        includeWSBeforePHPDoc = true;
        isFirstUseTraitStatementPart = true;
        super.visit(node);
    }

    @Override
    public void visit(GroupUseStatementPart node) {
        scan(node.getBaseNamespaceName());
        List<SingleUseStatementPart> items = node.getItems();
        int start;
        // #262205 if there is an error in group uses, the list is empty
        if (items.isEmpty()) {
            // find "{" until "}", so use the end offset
            start = node.getEndOffset() - 1;
        } else {
            start = items.get(0).getStartOffset();
        }

        while (ts.moveNext()
                && ts.offset() < start
                && lastIndex < ts.index()) {
            if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_GROUP_USE_LEFT_BRACE, ts.offset()));
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), options.indentSize));
                if (items.isEmpty()) {
                    addFormatToken(formatTokens);
                    formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_GROUP_USE_LEFT_BRACE, ts.offset() + ts.token().text().length()));
                    break;
                }
            }
            addFormatToken(formatTokens);
        }

        if (!items.isEmpty()) {
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_GROUP_USE_LEFT_BRACE, ts.offset()));
            ts.movePrevious();
            addListOfNodes(items, FormatToken.Kind.WHITESPACE_IN_GROUP_USE_LIST);
        }

        while (ts.moveNext()
                && ts.offset() < node.getEndOffset()
                && lastIndex < ts.index()) {
            if (ts.token().id() == PHPTokenId.PHP_CURLY_CLOSE) {
                formatTokens.add(new FormatToken.IndentToken(ts.offset(), -1 * options.indentSize));
                formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_GROUP_USE_RIGHT_BRACE, ts.offset()));
            }
            addFormatToken(formatTokens);
        }
    }

    @Override
    public void visit(SingleUseStatementPart statementPart) {
        if (!(path.size() > 0
                && path.get(1) instanceof GroupUseStatementPart)) {
            FormatToken lastFormatToken = formatTokens.get(formatTokens.size() - 1);
            boolean lastRemoved = false;
            if (ts.token().id() == PHPTokenId.PHP_NS_SEPARATOR
                    && lastFormatToken.getId() == FormatToken.Kind.TEXT
                    && "\\".equals(lastFormatToken.getOldText())) {
                formatTokens.remove(formatTokens.size() - 1);
                lastRemoved = true;
            }
            if (isFirstUseStatementPart) {
                formatTokens.add(new FormatToken.AnchorToken(ts.offset()));
                isFirstUseStatementPart = false;
            }
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USES_PART, ts.offset()));
            if (lastRemoved) {
                formatTokens.add(lastFormatToken);
            }
        }
        super.visit(statementPart);
    }

    @Override
    public void visit(UseTraitStatementPart node) {
        if (isFirstUseTraitStatementPart) {
            formatTokens.add(new FormatToken.AnchorToken(ts.offset()));
            isFirstUseTraitStatementPart = false;
        }
        formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_USE_TRAIT_PART, ts.offset()));
        super.visit(node);
    }

    @Override
    public void visit(TraitMethodAliasDeclaration node) {
        addRestOfLine();
        super.visit(node);
    }

    @Override
    public void visit(TraitConflictResolutionDeclaration node) {
        addRestOfLine();
        super.visit(node);
    }

    @Override
    public void visit(NullableType nullableType) {
        if (ts.moveNext()) {
            assert TokenUtilities.equals(ts.token().text(), "?"); // NOI18N
            addFormatToken(formatTokens); // add "?"
            formatTokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_NULLABLE_TYPE_PREFIX, ts.offset() + 1));
            Expression type = nullableType.getType();
            scan(type);
        }
    }

    @Override
    public void visit(DeclareStatement node) {
        List<Identifier> names = node.getDirectiveNames();
        List<Expression> values = node.getDirectiveValues();
        assert names.size() == values.size();
        for (int i = 0; i < names.size(); i++) {
            scan(names.get(i));
            // add "="
            addAllUntilOffset(values.get(i).getStartOffset());
            scan(values.get(i));
        }
        scan(node.getBody());
    }

    private int lastIndex = -1;

    private String showAssertionFor188809() {
        String result = "";
        try {
            result = "The same token (index: " + ts.index() + " - " + ts.token().id() + ", format tokens: " + formatTokens.size() //NOI18N
                    + ")  was precessed before.\nPlease report this to help fix issue 188809.\n\n" //NOI18N
                    + document.getText(0, document.getLength() - 1);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private void addFormatToken(List<FormatToken> tokens) {
        if (lastIndex == ts.index()) {
            assert false : showAssertionFor188809();
            ts.moveNext();
            return;
        }
        lastIndex = ts.index();
        switch (ts.token().id()) {
            case WHITESPACE:
                tokens.addAll(resolveWhitespaceTokens());
                break;
            case PHP_LINE_COMMENT:
                if (ts.token().text().charAt(ts.token().length() - 1) == '\n') {
                    CharSequence text = ts.token().text().subSequence(0, ts.token().length() - 1);
                    int newOffset = ts.offset() + ts.token().length() - 1;
                    if (text.length() > 0) {
                        tokens.add(new FormatToken(FormatToken.Kind.LINE_COMMENT, ts.offset(), text.toString()));
                    }
                    if (ts.moveNext()) {
                        if (ts.token().id() == PHPTokenId.WHITESPACE) {
                            if (countOfNewLines(ts.token().text()) > 0) {
                                // reset group alignment, if there is an empty line
                                resetAndCreateGroupAlignment();
                            }
                            tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, newOffset, "\n" + ts.token().text().toString()));
                            if (ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BETWEEN_LINE_COMMENTS, ts.offset()));
                            }
                            // #268710 for adding/checking the PHP_LINE_COMMENT token later
                            ts.movePrevious();
                        } else {
                            tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, newOffset, "\n"));
                            ts.movePrevious();
                        }
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, newOffset, "\n"));
                    }

                } else {
                    tokens.add(new FormatToken(FormatToken.Kind.LINE_COMMENT, ts.offset(), ts.token().text().toString()));
                }
                break;
            case PHP_OPENTAG:
            case T_OPEN_TAG_WITH_ECHO:
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_OPEN_PHP_TAG, ts.offset()));
                tokens.add(new FormatToken(FormatToken.Kind.OPEN_TAG, ts.offset(), ts.token().text().toString()));
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_OPEN_PHP_TAG, ts.offset() + ts.token().length()));
                break;
            case PHP_CLOSETAG:
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CLOSE_PHP_TAG, ts.offset()));
                tokens.add(new FormatToken(FormatToken.Kind.CLOSE_TAG, ts.offset(), ts.token().text().toString()));
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_CLOSE_PHP_TAG, ts.offset() + ts.token().length()));
                break;
            case PHP_COMMENT_START:
                tokens.add(new FormatToken(FormatToken.Kind.COMMENT_START, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_COMMENT_END:
                tokens.add(new FormatToken(FormatToken.Kind.COMMENT_END, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_COMMENT:
                tokens.add(new FormatToken(FormatToken.Kind.COMMENT, ts.offset(), ts.token().text().toString()));
                break;
            case PHPDOC_COMMENT:
                tokens.add(new FormatToken(FormatToken.Kind.DOC_COMMENT, ts.offset(), ts.token().text().toString()));
                break;
            case PHPDOC_COMMENT_START:
                tokens.add(new FormatToken(FormatToken.Kind.DOC_COMMENT_START, ts.offset(), ts.token().text().toString()));
                break;
            case PHPDOC_COMMENT_END:
                tokens.add(new FormatToken(FormatToken.Kind.DOC_COMMENT_END, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_OBJECT_OPERATOR:
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_OBJECT_OP, ts.offset()));
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_OBJECT_OP, ts.offset() + ts.token().length()));
                break;
            case PHP_CASTING:
                String text = ts.token().text().toString();
                String part1 = text.substring(0, text.indexOf('(') + 1);
                String part2 = text.substring(part1.length(), text.indexOf(')'));
                String part3 = text.substring(part1.length() + part2.length());
                StringBuilder ws1 = new StringBuilder();
                StringBuilder ws2 = new StringBuilder();
                int index = 0;
                while (index < part2.length() && part2.charAt(index) == ' ') {
                    ws1.append(' ');
                    index++;
                }
                index = part2.length() - 1;
                while (index > 0 && part2.charAt(index) == ' ') {
                    ws2.append(' ');
                    index--;
                }
                part2 = part2.trim();
                int length = 0;
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), part1));
                length += part1.length();
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_TYPE_CAST_PARENS, ts.offset() + part1.length()));
                if (ws1.length() > 0) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset() + length, ws1.toString()));
                    length += ws1.length();
                }
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset() + length, part2));
                length += part2.length();
                if (ws2.length() > 0) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset() + length, ws2.toString()));
                    length += ws2.length();
                }
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_TYPE_CAST_PARENS, ts.offset() + length));
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset() + length, part3));
                length += part3.length();
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_TYPE_CAST, ts.offset() + length));
                break;
            case PHP_TOKEN:
                CharSequence txt = ts.token().text();
                ASTNode parent = path.get(0);
                if (TokenUtilities.textEquals("(", txt)) { // NOI18N
                    if (isAnonymousClass(parent)) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ANONYMOUS_CLASS_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ANONYMOUS_CLASS_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration || parent instanceof ArrowFunctionDeclaration) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_METHOD_DEC_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof LambdaFunctionDeclaration) {
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof FunctionInvocation || parent instanceof MethodInvocation || parent instanceof ClassInstanceCreation) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_METHOD_CALL_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof IfStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_IF_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_IF_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof ForEachStatement || parent instanceof ForStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FOR_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_FOR_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_WHILE_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof SwitchStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SWITCH_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_SWITCH_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof CatchClause) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_CATCH_PARENS, ts.offset() + ts.token().length()));
                    } else if (parent instanceof ArrayCreation) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN, ts.offset() + ts.token().length()));
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    }
                } else if (TokenUtilities.textEquals(")", txt)) { // NOI18N
                    if (isAnonymousClass(parent)) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ANONYMOUS_CLASS_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof FunctionDeclaration || parent instanceof MethodDeclaration
                            || parent instanceof LambdaFunctionDeclaration || parent instanceof ArrowFunctionDeclaration) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof FunctionInvocation || parent instanceof MethodInvocation || parent instanceof ClassInstanceCreation) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof IfStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_IF_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof ForEachStatement || parent instanceof ForStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_FOR_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof WhileStatement || parent instanceof DoStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_WHILE_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof SwitchStatement) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_SWITCH_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof CatchClause) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_CATCH_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else if (parent instanceof ArrayCreation) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    }
                } else if (TokenUtilities.textEquals("[", txt)) {
                    if (parent instanceof ArrayCreation) {
                        // do not add this format token, it historically serves for case "array ()"
                        //tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN, ts.offset() + ts.token().length()));
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS, ts.offset() + ts.token().length()));
                    }
                } else if (TokenUtilities.textEquals("]", txt)) {
                    if (parent instanceof ArrayCreation) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    }
                } else if (TokenUtilities.textEquals(",", txt)) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_COMMA, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_COMMA, ts.offset() + ts.token().length()));
                } else if (TokenUtilities.textEquals(":", txt)) { // NOI18N
                    if (parent instanceof FunctionDeclaration
                            || parent instanceof MethodDeclaration
                            || parent instanceof LambdaFunctionDeclaration
                            || parent instanceof ArrowFunctionDeclaration) {
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_RETURN_TYPE_SEPARATOR, ts.offset()));
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                        tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_RETURN_TYPE_SEPARATOR, ts.offset() + ts.token().length()));
                    } else {
                        tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    }
                } else {
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                }
                break;
            case PHP_OPERATOR:
                CharSequence txt2 = ts.token().text();
                // assignment?
                if (TokenUtilities.equals(txt2, "=") // NOI18N
                        && path.get(0) instanceof DeclareStatement) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_DECLARE_EQUAL, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), txt2.toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_DECLARE_EQUAL, ts.offset() + ts.token().length()));
                    break;
                }
                if (!TokenUtilities.startsWith(txt2, "==") // NOI18N NETBEANS-2149
                        && TokenUtilities.endsWith(txt2, "=")) { // NOI18N
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ASSIGN_OP, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_ASSIGN_OP, ts.offset() + ts.token().length()));
                    break;
                }
                int origOffset = ts.offset();
                if (TokenUtilities.textEquals(txt2, "!")) { // NOI18N
                    if (ts.movePrevious()) {
                        Token<? extends PHPTokenId> previous = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.WHITESPACE));
                        if (previous.id() == PHPTokenId.PHP_RETURN) {
                            tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_KEYWORD, origOffset));
                        }
                        ts.move(origOffset);
                        ts.moveNext();
                    }
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), txt2.toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset() + ts.token().length()));
                } else if (TokenUtilities.textEquals(txt2, "=>")) { // NOI18N
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_KEY_VALUE_OP, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), txt2.toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_KEY_VALUE_OP, ts.offset() + ts.token().length()));
                } else if (TokenUtilities.textEquals(txt2, "++") // NOI18N
                        || TokenUtilities.textEquals(txt2, "--")) { // NOI18N
                    Token<? extends PHPTokenId> previousToken = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.PHP_OPERATOR, PHPTokenId.WHITESPACE));
                    if (previousToken != null) {
                        if (previousToken.id() == PHPTokenId.PHP_VARIABLE
                                || previousToken.id() == PHPTokenId.PHP_STRING
                                || (previousToken.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.equals(previousToken.text(), "]"))) { // NOI18N
                            tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset() + ts.token().length()));
                        } else if (previousToken.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.equals(previousToken.text(), ".")) { // NOI18N
                            // see PHPFormatterBrokenTest.testIssue197074_02
                            tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE, ts.offset() + ts.token().length()));
                        }
                        ts.move(origOffset);
                        ts.moveNext();
                    }
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), txt2.toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AROUND_UNARY_OP, ts.offset() + ts.token().length()));
                } else if (TokenUtilities.textEquals(txt2, "|") // NOI18N
                        && path.get(0) instanceof CatchClause) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_MULTI_CATCH_SEPARATOR, ts.offset()));
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_MULTI_CATCH_SEPARATOR, ts.offset() + ts.token().length()));
                } else {
                    tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                }
                break;
            case PHP_WHILE:
                if (path.get(0) instanceof DoStatement && isCurly) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_WHILE, ts.offset()));
                }
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_ELSE:
            case PHP_ELSEIF:
                if (isCurly) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_ELSE, ts.offset()));
                }
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_SEMICOLON:
                if (!(ts.movePrevious() && ts.token().id() == PHPTokenId.WHITESPACE
                        && countOfNewLines(ts.token().text()) > 0)) {
                    // if a line starts with the semicolon, don't put this whitespace
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_SEMI, ts.offset() + ts.token().length()));
                }
                ts.moveNext();
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                if (path.size() > 0 && !(path.get(0) instanceof ForStatement)) {
                    tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_AFTER_SEMI, ts.offset() + ts.token().length()));
                }
                break;
            case PHP_CATCH:
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_CATCH, ts.offset()));
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                break;
            case PHP_FINALLY:
                tokens.add(new FormatToken(FormatToken.Kind.WHITESPACE_BEFORE_FINALLY, ts.offset()));
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
                break;
            case T_INLINE_HTML:
                FormatToken.InitToken token = (FormatToken.InitToken) formatTokens.get(0);
                if (!token.hasHTML() && !isWhitespace(ts.token().text())) {
                    token.setHasHTML(true);
                }
                int tokenStartOffset = ts.offset();
                StringBuilder sb = new StringBuilder(ts.token().text());
                // merge all html following tokens to one format token;
                while (ts.moveNext() && ts.token().id() == PHPTokenId.T_INLINE_HTML) {
                    sb.append(ts.token().text());
                }

                if (ts.moveNext()) {
                    ts.movePrevious();
                    ts.movePrevious();
                    tokens.add(new FormatToken(FormatToken.Kind.HTML, tokenStartOffset, sb.toString()));
                } else {
                    // this is the last token in the document
                    lastIndex--;
                }
                break;
            default:
                tokens.add(new FormatToken(FormatToken.Kind.TEXT, ts.offset(), ts.token().text().toString()));
        }
    }

    private List<FormatToken> resolveWhitespaceTokens() {
        final List<FormatToken> result = new LinkedList<>();
        int countNewLines = countOfNewLines(ts.token().text());
        if (countNewLines > 1) {
            // reset group alignment, if there is an empty line
            resetAndCreateGroupAlignment();
        }
        String tokenText = ts.token().text().toString();
        int tokenStartOffset = ts.offset();
        if (countNewLines > 0) {
            result.add(new FormatToken(FormatToken.Kind.WHITESPACE_INDENT, tokenStartOffset, adjustLastWhitespaceToken(ts.token())));
        } else {
            int tokenEndOffset = tokenStartOffset + ts.token().length();
            if (GsfUtilities.isCodeTemplateEditing(document)
                    && caretOffset > tokenStartOffset
                    && caretOffset < tokenEndOffset
                    && tokenStartOffset > startOffset
                    && tokenEndOffset < endOffset) {
                int devideIndex = caretOffset - tokenStartOffset;
                String firstTextPart = tokenText.substring(0, devideIndex);
                result.add(new FormatToken(FormatToken.Kind.WHITESPACE, tokenStartOffset, firstTextPart));
                result.add(new FormatToken(FormatToken.Kind.WHITESPACE, tokenStartOffset + firstTextPart.length(), tokenText.substring(devideIndex)));
            } else {
                result.add(new FormatToken(FormatToken.Kind.WHITESPACE, tokenStartOffset, adjustLastWhitespaceToken(ts.token())));
            }
        }
        return result;
    }

    /**
     * This is an ugly hack.
     *
     * Source which is lexed is adjusted by someone and '\n' is added at the end of source,
     * even though there is NO new line at the end of file (source). Then FormatVisitor adds an extra
     * formatting token of an invalid value and TokenFormatter can't count trailing new lines
     * properly.
     *
     * @param token
     * @return if last token is processed, then text without one '\n', tokenText otherwise
     */
    private String adjustLastWhitespaceToken(Token<PHPTokenId> token) {
        assert token.id() == PHPTokenId.WHITESPACE;
        String result;
        String tokenText = token.text().toString();
        boolean isLast;
        if (ts.moveNext()) {
            isLast = false;
            ts.movePrevious();
        } else {
            isLast = true;
        }
        if (isLast) {
            int firstNewLineOffset = tokenText.indexOf('\n');
            result = tokenText.substring(0, firstNewLineOffset) + tokenText.substring(firstNewLineOffset + 1);
        } else {
            result = tokenText;
        }
        return result;
    }

    private void addAllUntilOffset(int offset) {
        while (moveNext() && ts.offset() < offset
                && (ts.offset() + ts.token().length()) <= offset) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();
    }

    private void addRestOfLine() {
        while (ts.moveNext()
                && ts.token().id() != PHPTokenId.PHP_LINE_COMMENT
                && ((ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) == 0)
                || isComment(ts.token())
                || ts.token().id() == PHPTokenId.PHP_SEMICOLON) && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
                || (ts.token().id() == PHPTokenId.WHITESPACE && countOfNewLines(ts.token().text()) == 0)) {
            addFormatToken(formatTokens);
            if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT && ts.moveNext() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                addFormatToken(formatTokens);
            } else {
                ts.movePrevious();
            }
        } else {
            ts.movePrevious();
        }
    }

    /**
     *
     * @param chs
     * @return number of new lines in the input
     */
    private int countOfNewLines(CharSequence chs) {
        int count = 0;
        for (int i = 0; i < chs.length(); i++) {
            if (chs.charAt(i) == '\n') { // NOI18N
                count++;
            }
        }
        return count;
    }

    private void addEndOfUnbreakableSequence(int endOffset) {
        boolean wasLastLineComment = false;
        while (ts.moveNext()
                && ((ts.token().id() == PHPTokenId.WHITESPACE
                && countOfNewLines(ts.token().text()) == 0)
                || isComment(ts.token()))
                && lastIndex < ts.index()) {
            if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
                    && !TokenUtilities.textEquals("//", ts.token().text())) { // NOI18N
                addFormatToken(formatTokens);
                wasLastLineComment = true;
                break;
            }
            addFormatToken(formatTokens);

        }
        if (wasLastLineComment) {
            while (ts.moveNext()
                    && (ts.token().id() == PHPTokenId.PHP_COMMENT_START
                        || ts.token().id() == PHPTokenId.PHP_COMMENT_END
                        || ts.token().id() == PHPTokenId.PHP_COMMENT)) {
                addFormatToken(formatTokens);
            }
            ts.movePrevious();
            FormatToken last = formatTokens.remove(formatTokens.size() - 1);
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length() - 1, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
            formatTokens.add(last);
        } else {
            ts.movePrevious();
            if ((ts.token().id() == PHPTokenId.WHITESPACE
                    && countOfNewLines(ts.token().text()) == 0)) {
                List<FormatToken> removedWhitespaces = new ArrayList<>();
                do {
                    removedWhitespaces.add(formatTokens.remove(formatTokens.size() - 1));
                } while (formatTokens.get(formatTokens.size() - 1).isWhitespace());
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
                Collections.reverse(removedWhitespaces);
                for (FormatToken formatToken : removedWhitespaces) {
                    formatTokens.add(formatToken);
                }
            } else {
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
            }
        }
    }

    private void addUnbreakalbeSequence(ASTNode node, boolean addAnchor) {
        formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        addAllUntilOffset(node.getStartOffset());
        if (addAnchor) {
            formatTokens.add(new FormatToken.AnchorToken(ts.offset() + ts.token().length()));
        }
        scan(node);
        while (ts.moveNext()
                && (ts.token().id() == PHPTokenId.WHITESPACE
                || isComment(ts.token())
                || (ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(",", ts.token().text()))) // NOI18N
                && lastIndex < ts.index()) {
            addFormatToken(formatTokens);
        }
        ts.movePrevious();

        int index = formatTokens.size() - 1;
        FormatToken lastToken = formatTokens.get(index);
        FormatToken removedWS = null;
        if (lastToken.getId() == FormatToken.Kind.WHITESPACE || lastToken.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
            removedWS = formatTokens.remove(formatTokens.size() - 1);
            index--;
            lastToken = formatTokens.get(index);
        }

        if (lastToken.getId() == FormatToken.Kind.WHITESPACE_AFTER_COMMA) {
            formatTokens.remove(index);
            formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
            formatTokens.add(lastToken);
            if (removedWS != null) {
                formatTokens.add(removedWS);
            }
        } else {
            if (lastToken.getId() == FormatToken.Kind.LINE_COMMENT && removedWS != null) {
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length() - 1, null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
                formatTokens.add(removedWS);
            } else {
                formatTokens.add(new FormatToken.UnbreakableSequenceToken(ts.offset() + ts.token().length(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_END));
                if (removedWS != null) {
                    formatTokens.add(removedWS);
                }
            }

        }
    }

    private boolean isComment(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHPDOC_COMMENT
                || token.id() == PHPTokenId.PHPDOC_COMMENT_END
                || token.id() == PHPTokenId.PHPDOC_COMMENT_START
                || token.id() == PHPTokenId.PHP_COMMENT
                || token.id() == PHPTokenId.PHP_COMMENT_END
                || token.id() == PHPTokenId.PHP_COMMENT_START
                || token.id() == PHPTokenId.PHP_LINE_COMMENT;
    }

    private boolean isPreviousNodeTheSameInBlock(ASTNode astNode, Statement statement) {
        int index = 0;   // index of the current statement in the block
        List<Statement> statements = null;

        if (astNode instanceof Block) {
            statements = ((Block) astNode).getStatements();
        } else if (astNode instanceof Program) {
            statements = ((Program) astNode).getStatements();
        }
        if (statements != null) {
            while (index < statements.size() && statements.get(index).getStartOffset() < statement.getStartOffset()) {
                index++;
            }
            return (index < statements.size()
                    && index > 0
                    && statements.get(index - 1).getClass().equals(statement.getClass()));
        }
        return false;
    }

    private boolean isNextNodeTheSameInBlock(ASTNode astNode, Statement statement) {
        int index = 0;   // index of the current statement in the block
        List<Statement> statements = null;

        if (astNode instanceof Block) {
            statements = ((Block) astNode).getStatements();
        } else if (astNode instanceof Program) {
            statements = ((Program) astNode).getStatements();
        }
        if (statements != null) {
            while (index < statements.size() && statements.get(index).getStartOffset() < statement.getStartOffset()) {
                index++;
            }
            return (index == statements.size() - 1
                    || !((index < statements.size() - 1) && (statements.get(index + 1).getClass().equals(statement.getClass()))));
        }
        return false;
    }

    private void addNoCurlyBody(ASTNode body, FormatToken.Kind before) {
        addAllUntilOffset(body.getStartOffset());
        if (ts.moveNext() && ts.token().id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(")", ts.token().text())) { // NOI18N
            // the body is not defined yet. See issue #187665
            addFormatToken(formatTokens);
        } else {
            ts.movePrevious();
        }
        formatTokens.add(new FormatToken.IndentToken(body.getStartOffset(), options.indentSize));
        if (!(body instanceof ASTError)) {
            formatTokens.add(new FormatToken(before, body.getStartOffset()));
        }
        formatTokens.add(new FormatToken.UnbreakableSequenceToken(body.getStartOffset(), null, FormatToken.Kind.UNBREAKABLE_SEQUENCE_START));
        scan(body);
        addEndOfUnbreakableSequence(body.getEndOffset());
        formatTokens.add(new FormatToken.IndentToken(body.getEndOffset(), -1 * options.indentSize));
    }

    private boolean moveNext() {
        boolean value = ts.moveNext();
        if (value) {
            FormatToken last = formatTokens.get(formatTokens.size() - 1);
            value = !(last.getId() == FormatToken.Kind.TEXT && last.getOffset() >= ts.offset());
        }
        return value;
    }

    /**
     *
     * @param node and identifier that is before the operator that is aligned in
     * the group
     */
    private void handleGroupAlignment(int nodeLength, boolean multilined) {
        if (groupAlignmentTokenHolders.isEmpty()) {
            createGroupAlignment();
        }
        GroupAlignmentTokenHolder tokenHolder = groupAlignmentTokenHolders.peek();
        FormatToken.AssignmentAnchorToken previousGroupToken = tokenHolder.getToken();
        if (previousGroupToken == null) {
            // it's the first line in the group
            previousGroupToken = new FormatToken.AssignmentAnchorToken(ts.offset(), multilined);
            previousGroupToken.setLenght(nodeLength);
            previousGroupToken.setMaxLength(nodeLength);
        } else {
            // it's a next line in the group.
            FormatToken.AssignmentAnchorToken aaToken = new FormatToken.AssignmentAnchorToken(ts.offset(), multilined);
            aaToken.setLenght(nodeLength);
            aaToken.setPrevious(previousGroupToken);
            aaToken.setIsInGroup(true);
            if (!previousGroupToken.isInGroup()) {
                previousGroupToken.setIsInGroup(true);
            }
            if (previousGroupToken.getMaxLength() < nodeLength) {
                // if the length of the current identifier is bigger, then is in
                // the group so far, change max length for all items in the group
                previousGroupToken = aaToken;
                do {
                    aaToken.setMaxLength(nodeLength);
                    aaToken = aaToken.getPrevious();
                } while (aaToken != null);
            } else {
                aaToken.setMaxLength(previousGroupToken.getMaxLength());
                previousGroupToken = aaToken;
            }
        }
        tokenHolder.setToken(previousGroupToken);
        formatTokens.add(previousGroupToken);
    }

    private void handleGroupAlignment(int nodeLength) {
        handleGroupAlignment(nodeLength, false);
    }

    private void handleGroupAlignment(ASTNode node) {
        handleGroupAlignment(node.getEndOffset() - node.getStartOffset(), false);
    }

    private void handleGroupAlignment(ASTNode node, boolean multilined) {
        handleGroupAlignment(node.getEndOffset() - node.getStartOffset(), multilined);
    }

    private void resetAndCreateGroupAlignment() {
        resetGroupAlignment();
        createGroupAlignment();
    }

    private void resetGroupAlignment() {
        if (!groupAlignmentTokenHolders.isEmpty()) {
            groupAlignmentTokenHolders.pop();
        }
    }

    private void createGroupAlignment() {
        groupAlignmentTokenHolders.push(new GroupAlignmentTokenHolderImpl());
    }

    private boolean isFirstUseTraitStatementInBlock(ASTNode parentNode, UseTraitStatement node) {
        if (parentNode instanceof Block) {
            List<Statement> statements = ((Block) parentNode).getStatements();
            return !statements.isEmpty()
                    && statements.get(0).equals(node);
        }
        return true;
    }

    private boolean isFieldTypeOrVariableToken(Token<PHPTokenId> token) {
        return PHPTokenId.PHP_VARIABLE == token.id()
                || PHPTokenId.PHP_ARRAY == token.id()
                || PHPTokenId.PHP_ITERABLE == token.id()
                || PHPTokenId.PHP_PARENT == token.id()
                || PHPTokenId.PHP_SELF == token.id()
                || PHPTokenId.PHP_TYPE_BOOL == token.id()
                || PHPTokenId.PHP_TYPE_INT == token.id()
                || PHPTokenId.PHP_TYPE_FLOAT == token.id()
                || PHPTokenId.PHP_TYPE_OBJECT == token.id()
                || PHPTokenId.PHP_TYPE_STRING == token.id()
                || PHPTokenId.PHP_NS_SEPARATOR == token.id() // \
                || (PHPTokenId.PHP_TOKEN == token.id() && TokenUtilities.textEquals(token.text(), "?")) // NOI18N
                || PHPTokenId.PHP_TYPE_VOID == token.id() // not supported type but just check it
                || PHPTokenId.PHP_CALLABLE == token.id() // not supported type but just check it
                ;
    }

    private interface GroupAlignmentTokenHolder {

        void setToken(FormatToken.AssignmentAnchorToken token);

        FormatToken.AssignmentAnchorToken getToken();
    }

    private static class GroupAlignmentTokenHolderImpl implements GroupAlignmentTokenHolder {
        private AssignmentAnchorToken token;

        @Override
        public void setToken(AssignmentAnchorToken token) {
            this.token = token;
        }

        @Override
        public AssignmentAnchorToken getToken() {
            return token;
        }

    }

    protected static boolean isWhitespace(final CharSequence text) {
        int index = 0;
        while (index < text.length()
                && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index == text.length();
    }

    private static boolean isAnonymousClass(ASTNode astNode) {
        return astNode instanceof ClassInstanceCreation && ((ClassInstanceCreation) astNode).isAnonymous();
    }

}
