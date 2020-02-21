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

package org.netbeans.modules.cnd.editor.reformat;

import java.util.ArrayList;
import java.util.LinkedList;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.api.CodeStyle.VisibilityIndent;
import org.netbeans.modules.cnd.editor.reformat.BracesStack.StatementKind;
import org.netbeans.modules.cnd.editor.reformat.ContextDetector.OperatorKind;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;

/**
 *
 */
public class ReformatterImpl {
    /*package local*/ final ContextDetector ts;
    /*package local*/ final CodeStyle codeStyle;
    /*package local*/ final DiffLinkedList diffs = new DiffLinkedList();
    /*package local*/ final BracesStack braces;
    private final int startOffset;
    private final int endOffset;
    private final PreprocessorFormatter preprocessorFormatter;
    final int tabSize;
    final boolean expandTabToSpaces;
    private final QtExtension qtExtension = new QtExtension();
    
    ReformatterImpl(TokenSequence<CppTokenId> ts, int startOffset, int endOffset, CodeStyle codeStyle){
        braces = new BracesStack(codeStyle);
        int aTabSize = codeStyle.getTabSize();
        if (aTabSize <= 1) {
            aTabSize = 8;
        }
        tabSize = aTabSize;
        expandTabToSpaces = codeStyle.expandTabToSpaces();
        this.ts = new ContextDetector(ts, diffs, braces, tabSize, expandTabToSpaces);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.codeStyle = codeStyle;
        preprocessorFormatter = new PreprocessorFormatter(this);
    }
    
    LinkedList<Diff> reformat(){
        ts.moveStart();
        Token<CppTokenId> previous = ts.lookPrevious();
        while(ts.moveNext()){
            if (ts.offset() >= endOffset) {
                break;
            }
            //System.out.println("========"+previous+"==========");
            //System.out.println(ts);
            Token<CppTokenId> current = ts.token();
            CppTokenId id = current.id();
            if (previous != null && previous.id() == PREPROCESSOR_DIRECTIVE && id != PREPROCESSOR_DIRECTIVE){
                // indent afre preprocessor directive
                if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                    if (ts.isStatementContinuation()){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                }
                if (doFormat()){
                    indentNewLine(current);
                }
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                case NEW_LINE:
                case ESCAPED_WHITESPACE:
                case WHITESPACE:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case DOXYGEN_LINE_COMMENT:
                case LINE_COMMENT:
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                case COLON:
                case SEMICOLON:
                case LBRACE:
                case RBRACE:
                case LPAREN:
                case RPAREN:
                    break;
                case IDENTIFIER:
                    if (qtExtension.isQtObject() && 
                       (qtExtension.isSignals(current) || qtExtension.isSlots(current))) {
                        break;
                    }
                    braces.setLastStatementStart(ts);
                    break;
                default:
                    braces.setLastStatementStart(ts);
            }
            switch(id){
                case PREPROCESSOR_DIRECTIVE: //(null, "preprocessor"),
                {
                    preprocessorFormatter.indentPreprocessor(previous);
                    break;
                }
                case NEW_LINE:
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        if (ts.isStatementContinuation()){
                            braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                        }
                    }
                    if (doFormat()) {
                        newLineFormat(previous, current);
                    }
                    break;
                }
                case WHITESPACE:
                {
                    if (doFormat()) {
                        whiteSpaceFormat(previous, current);
                    }
                    break;
                }
                case DOXYGEN_COMMENT:
                case BLOCK_COMMENT:
                {
                    if (doFormat()) {
                        reformatBlockComment(previous, current);
                    }
                    break;
                }
                case LBRACE: //("{", "separator"),
                {
                    int start = braces.lastStatementStart;
                    braces.push(ts);
                    if (doFormat()) {
                        braceFormat(previous, current);

                        StackEntry entry = braces.peek();
                        if (entry.getImportantKind() == CLASS ||
                            entry.getImportantKind() == STRUCT ||    
                            entry.getImportantKind() == UNION ||    
                            entry.getImportantKind() == ENUM) {
                            // add new lines before class declaration
                            newLinesBeforeDeclaration(codeStyle.blankLinesBeforeClass(), start);
                        } else if (entry.getImportantKind() == NAMESPACE){
                            // TODO blank lines before namespace
                        } else if (entry.isLikeToFunction()) {
                            // add new lines before method declaration
                            newLinesBeforeDeclaration(codeStyle.blankLinesBeforeMethods(), start);
                        } else if (entry.getImportantKind() == ARROW){
                            // lamda no action
                        } else if (entry.isLikeToArrayInitialization()) {
                            // no action
                        } else {
                            Token<CppTokenId> prevImportant = ts.lookPreviousImportant();
                            if (prevImportant != null &&
                                prevImportant.id() == SEMICOLON &&
                                braces.getLength() == 1) {
                                // TODO detect K&R style.
                                entry.setLikeToFunction();
                                newLinesBeforeDeclaration(codeStyle.blankLinesBeforeMethods(), braces.lastKRstart);
                            }
                        }
                    }
                    braces.lastKRstart = -1;
                    break;
                }
                case LPAREN: //("(", "separator"),
                {
                    if (braces.parenDepth == 0) {
                        if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                            braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        }
                        if (braces.getLength() == 0){
                            // save K&R start
                            braces.lastKRstart = braces.lastStatementStart;
                        }
                        if (braces.lastStatementParen >= 0) {
                            braces.lastStatementParen = ts.index();
                        }
                    }
                    braces.parenDepth++;
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                        } else {
                            formatLeftParen(previous, current);
                        }
                    }
                    break;
                }
                case RPAREN: //(")", "separator"),
                {
                    braces.parenDepth--;
                    if (braces.parenDepth < 0){
                        // unbalanced paren
                        braces.parenDepth = 0;
                    }
                    if (braces.parenDepth == 0) {
                        StackEntry entry = braces.peek();
                        if (entry == null || entry.getKind() != LBRACE ||
                            entry.getImportantKind() == CLASS || entry.getImportantKind() == STRUCT || 
                            entry.getImportantKind() == UNION || entry.getImportantKind() == NAMESPACE){
                            Token<CppTokenId> next = ts.lookNextImportant();
                            if (next != null && next.id() == COLON) {
                                braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE_INIT);
                            } else {
                                if (braces.getStatementContinuation() != BracesStack.StatementContinuation.CONTINUE_INIT) {
                                    braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                                }
                            }
                        }
                        if (braces.lastStatementParen >= 0) {
                            braces.lastStatementParen = -1;
                        }
                    }
                    if (doFormat()) {
                        if (isOperator2()) {
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            formatRightParen(previous, current);
                        }
                    }
                    break;
                }
                case IDENTIFIER:
                {
                    boolean isStart = false;
                    qtExtension.checkQtObject(current);
                    Token<CppTokenId> next = ts.lookNextImportant();
                    if (next != null && next.id() == COLON) {
                        if (qtExtension.isQtObject() && qtExtension.isSlots(current)) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (ts.isFirstLineToken() && prev != null &&
                               (prev.id() == PRIVATE || prev.id() == PROTECTED || prev.id() == PUBLIC)) {
                                StackEntry entry = braces.peek();
                                if (doFormat()) {
                                    if (entry != null && entry.getImportantKind() != null){
                                        switch (entry.getImportantKind()) {
                                            case CLASS: //("class", "keyword"), //C++
                                            case STRUCT: //("struct", "keyword"),
                                            case UNION:
                                                removeLineBefore(true);
                                        }
                                    }
                                }
                            }
                            break;
                        } else if (qtExtension.isQtObject() && qtExtension.isSignals(current)) {
                            StackEntry entry = braces.peek();
                            if (doFormat()) {
                                if (entry != null && entry.getImportantKind() != null){
                                    switch (entry.getImportantKind()) {
                                        case CLASS: //("class", "keyword"), //C++
                                        case STRUCT: //("struct", "keyword"),
                                        case UNION:
                                            newLineBefore(IndentKind.PARENT);
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                        isStart = ts.index() == braces.lastStatementStart;
                    }
                    if (isStart) {
                        if (next != null && next.id() == COLON) {
                            indentLabel(previous);
                        }
                    }
                    break;
                }
                case SEMICOLON: //(";", "separator"),
                {
                    StackEntry entry = braces.peek();
                    if (braces.parenDepth == 0) {
                        braces.pop(ts);
                    }
                    if (entry != null && 
                       ((entry.getKind() == DO || entry.getImportantKind() == DO)) && entry.getKind() != LBRACE) {
                        Token<CppTokenId> next = ts.lookNextImportant();
                        if (next != null && next.id() == WHILE) {
                            braces.isDoWhile = true;
                        }
                    }
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceBeforeSemi(), codeStyle.spaceKeepExtra());
                        if (braces.parenDepth == 0) {
                            if (addNewLineAfterSemocolon(current)){
                                break;
                            }
                        }
                        spaceAfter(current, codeStyle.spaceAfterSemi(), codeStyle.spaceKeepExtra());
                    }
                    if (braces.parenDepth == 0) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    } else {
                        if (entry != null) {
                            if (entry.getLambdaParen() == braces.parenDepth) {
                                braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            }
                        }
                    }
                    break;
                }
                case COMMA: //(",", "separator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            spaceBefore(previous, codeStyle.spaceBeforeComma(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceAfterComma(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
                case PRIVATE:
                case PROTECTED:
                case PUBLIC:
                {
                    StackEntry entry = braces.peek();
                    if (doFormat()) {
                        if (entry != null && entry.getImportantKind() != null){
                            switch (entry.getImportantKind()) {
                                case CLASS: //("class", "keyword"), //C++
                                case STRUCT: //("struct", "keyword"),
                                case UNION:
                                    Token<CppTokenId> next = ts.lookNextImportant();
                                    if (next != null && next.id() == COLON) {
                                        if (codeStyle.indentVisibility() == VisibilityIndent.NO_INDENT) {
                                            newLineBefore(IndentKind.PARENT);
                                        } else {
                                            newLineBefore(IndentKind.HALF);
                                        }
                                    } else if (next != null && next.id() == IDENTIFIER &&
                                               qtExtension.isQtObject() && qtExtension.isSlots(next)) {
                                        next = ts.lookNextImportant(2);
                                        if (next != null && next.id() == COLON) {
                                            if (codeStyle.indentVisibility() == VisibilityIndent.NO_INDENT) {
                                                newLineBefore(IndentKind.PARENT);
                                            } else {
                                                newLineBefore(IndentKind.HALF);
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                    break;
                }
                case COLON: //(":", "operator"),
                    processColumn(previous, current);
                    break;
                case RBRACE: //("}", "separator"),
                {
                    StackEntry entry = braces.peek();
                    StackEntry statementEntry = null;
                    if (entry != null) {
                        if (entry.getKind() == DO || entry.getImportantKind() == DO) {
                            Token<CppTokenId> next = ts.lookNextImportant();
                            if (next != null && next.id() == WHILE) {
                                braces.isDoWhile = true;
                            }
                        } else if (entry.getImportantKind() == TRY || entry.getImportantKind() == CATCH) {
                            statementEntry = braces.lookPerevious();
                        }
                    }
                    braces.pop(ts);
                    if (doFormat()) {
                        indentRbrace(entry, previous, current, statementEntry);
                    }
                    break;
                }
                case QUESTION: //("?", "operator"),
                {
                    if (doFormat()) {
                        spaceBefore(previous, codeStyle.spaceAroundTernaryOps(), codeStyle.spaceKeepExtra());
                        spaceAfter(current, codeStyle.spaceAroundTernaryOps(), codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case NOT: //("!", "operator"),
                case TILDE: //("~", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            spaceAfter(current, codeStyle.spaceAroundUnaryOps(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
                case PLUSPLUS: //("++", "operator"),
                case MINUSMINUS: //("--","operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            if (ts.isPrefixOperator(current)){
                                spaceAfter(current, codeStyle.spaceAroundUnaryOps(), codeStyle.spaceKeepExtra());
                            } else if (ts.isPostfixOperator(current)){
                                spaceBefore(previous, codeStyle.spaceAroundUnaryOps(), codeStyle.spaceKeepExtra());
                            }
                        }
                    }
                    break;
                }
                case PLUS: //("+", "operator"),
                case MINUS: //("-", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                            } else if (kind == OperatorKind.UNARY){
                                spaceAfter(current, codeStyle.spaceAroundUnaryOps(), codeStyle.spaceKeepExtra());
                            }
                        }
                    }
                    break;
                }
                case STAR: //("*", "operator"),
                case AMP: //("&", "operator"),
                case AMPAMP: //("&&", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                            } else if (kind == OperatorKind.UNARY){
                                spaceAfter(current, codeStyle.spaceAroundUnaryOps(), codeStyle.spaceKeepExtra());
                            } else if (kind == OperatorKind.TYPE_MODIFIER){
                                //TODO style of type declaration
                            }
                        }
                    }
                    break;
                }
                case GT: //(">", "operator"),
                case LT: //("<", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else if (isNewStyleCast(current)){
                            if (current.id()==LT) {
                                Token<CppTokenId> lookNextImportant = ts.lookNextImportant();
                                if (lookNextImportant != null && lookNextImportant.id() == CppTokenId.SCOPE) {
                                    spaceAfter(current, true, codeStyle.spaceKeepExtra());
                                } else {
                                    spaceAfter(current, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
                                }
                            } else {
                                spaceBefore(previous, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
                                spaceAfter(current, codeStyle.spaceAfterTypeCast(), codeStyle.spaceKeepExtra());
                            }
                        } else {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                            } else if (kind == OperatorKind.SEPARATOR){
                                //TODO style of template declaration
                            }
                        }
                    }
                    break;
                }
                case EQEQ: //("==", "operator"),
                case LTEQ: //("<=", "operator"),
                case GTEQ: //(">=", "operator"),
                case NOTEQ: //("!=","operator"),
                case BARBAR: //("||", "operator"),
                case SLASH: //("/", "operator"),
                case BAR: //("|", "operator"),
                case PERCENT: //("%", "operator"),
                case LTLT: //("<<", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            spaceBefore(previous, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
                case GTGT: //(">>", "operator"),
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            OperatorKind kind = ts.getOperatorKind(current);
                            if (kind == OperatorKind.BINARY){
                                spaceBefore(previous, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                                spaceAfter(current, codeStyle.spaceAroundBinaryOps(), codeStyle.spaceKeepExtra());
                            } else {
                                //TODO style of template declaration
                            }
                        }
                    }
                    break;
                }
                case EQ: //("=", "operator"),
                case PLUSEQ: //("+=", "operator"),
                case MINUSEQ: //("-=", "operator"),
                case STAREQ: //("*=", "operator"),
                case SLASHEQ: //("/=", "operator"),
                case AMPEQ: //("&=", "operator"),
                case BAREQ: //("|=", "operator"),
                case CARETEQ: //("^=", "operator"),
                case PERCENTEQ: //("%=", "operator"),
                case LTLTEQ: //("<<=", "operator"),
                case GTGTEQ: //(">>=", "operator"),
                {
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.STOP) {
                        braces.setStatementContinuation(BracesStack.StatementContinuation.START);
                    }
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        } else {
                            spaceBefore(previous, codeStyle.spaceAroundAssignOps(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceAroundAssignOps(), codeStyle.spaceKeepExtra());
                        }
                    }
                    if (braces.getStatementContinuation() == BracesStack.StatementContinuation.START){
                        braces.setStatementContinuation(BracesStack.StatementContinuation.CONTINUE);
                    }
                    break;
                }
                case NAMESPACE: //("namespace", "keyword"), //C++
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    break;
                }
                case IF: //("if", "keyword-directive"),
                {
                    braces.push(ts);
                    braces.lastStatementParen = ts.index();
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeIfParen(), LPAREN, codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    braces.push(ts);
                    if (doFormat()) {
                       formatElse(previous);
                    }
                    break;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    braces.push(ts);
                    braces.lastStatementParen = ts.index();
                    if (doFormat()) {
                        boolean doSpaceBefore = true;

                        if (braces.isDoWhile) {
                            if (ts.isFirstLineToken()) {
                                if (!codeStyle.newLineWhile()) {
                                    // try to remove new line
                                    newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                            codeStyle.spaceBeforeWhile(), 0);
                                    doSpaceBefore = false;
                                }
                            } else {
                                if (codeStyle.newLineWhile()) {
                                    // add new line
                                    newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                            codeStyle.spaceBeforeWhile(), 0);
                                    doSpaceBefore = false;
                                }
                            }
                        }
                        if (doSpaceBefore){
                            spaceBefore(previous, codeStyle.spaceBeforeWhile(), codeStyle.spaceKeepExtra());
                        }
                        spaceAfterBefore(current, codeStyle.spaceBeforeWhileParen(), LPAREN, codeStyle.spaceKeepExtra());
                    }
                    braces.isDoWhile = false;
                    break;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    braces.push(ts);
                    braces.lastStatementParen = ts.index();
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeForParen(), LPAREN, codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    braces.push(ts);
                    if (doFormat()) {
                        // TODO need UI?
                        spaceBefore(previous, true, false);
                    }
                    break;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    braces.push(ts);
                    braces.lastStatementParen = ts.index();
                    if (doFormat()) {
                        boolean doSpaceBefore = true;
                        if (ts.isFirstLineToken()) {
                            if (!codeStyle.newLineCatch()){
                                // try to remove new line
                                newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                                        codeStyle.spaceBeforeCatch(), 0);
                                doSpaceBefore = false;
                            }
                        } else {
                             if (codeStyle.newLineCatch()){
                                // add new line
                                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                                        codeStyle.spaceBeforeCatch(), 0);
                                doSpaceBefore = false;
                            }
                       }
                       if (doSpaceBefore){
                          spaceBefore(previous, codeStyle.spaceBeforeCatch(), codeStyle.spaceKeepExtra());
                       }
                       spaceAfterBefore(current, codeStyle.spaceBeforeCatchParen(), LPAREN, codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                {
                    braces.push(ts);
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    braces.push(ts);
                    break;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    braces.push(ts);
                    braces.lastStatementParen = ts.index();
                    if (doFormat()) {
                        spaceAfterBefore(current, codeStyle.spaceBeforeSwitchParen(), LPAREN, codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case DEFAULT: //("default", "keyword-directive"),
                case CASE: //("case", "keyword-directive"),
                {
                    braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                    break;
                }
                case BREAK: //("break", "keyword-directive"),
                {
                    break;
                }
                case CONTINUE: //("continue", "keyword-directive"),
                {
                    break;
                }
                case SCOPE:
                {
                    if (doFormat()) {
                        Token<CppTokenId> p = ts.lookPreviousImportant(1);
                        if (p != null && p.id() == IDENTIFIER) {
                            spaceBefore(previous, false, false);
                        }
                        spaceAfter(current, false, false);
                    }
                    break;
                }
                case REINTERPRET_CAST:
                case STATIC_CAST:
                case CONST_CAST:
                case DYNAMIC_CAST:
                {
                    if (doFormat()) {
                        spaceAfter(current, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
                    }
                    break;
                }
                case LBRACKET://("[", "separator"), // NOI18N
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
                case RBRACKET://("]", "separator"), // NOI18N
                {
                    if (doFormat()) {
                        if (isOperator2()) {
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
                case DOT://(".", "separator"), // NOI18N
                case DOTMBR://(".*", "separator"), // NOI18N
                case ARROW://("->", "separator"), // NOI18N
                case ARROWMBR://("->*", "separator"), // NOI18N
                {
                    if (doFormat()) {
                        if (isOperator()) {
                            spaceBefore(previous, codeStyle.spaceAfterOperatorKeyword(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                        }
                    }
                    break;
                }
            }
            previous = current;
        }
        //System.out.println("Reformatter have prepared "+diffs.getStorage().size()+" diffs");
        return diffs.getStorage();
    }
    
    /*package local*/ int getParentIndent() {
        return continuationIndent(braces.getSelfIndent());
    }

    /*package local*/ int getCaseIndent() {
        if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_HALF_INDENTED){
            if (codeStyle.indentCasesFromSwitch()) {
                return getParentIndent() + codeStyle.indentSize()/2;
            } else {
                return getParentIndent();
            }
        } else {
            if (codeStyle.indentCasesFromSwitch()) {
                return getParentIndent() + codeStyle.indentSize();
            } else {
                return getParentIndent();
            }
        }
    }

    /*package local*/ int getIndent() {
        return continuationIndent(braces.getIndent());
    }

    /*package local*/ int continuationIndent(int shift){
        StackEntry entry = braces.peek();
        if (entry != null) {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                switch (entry.getKind()){
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    case CLASS: //("class", "keyword"), //C++
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                        break;
                    case SWITCH: 
                        if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_HALF_INDENTED){
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize()/2;
                        } else {
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize();
                        }
                        break;
                    case IF: 
                    case ELSE: 
                    case FOR: 
                    case DO: 
                    case WHILE: 
                    case CATCH: 
                        if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED){
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize()/2;
                        } else {
                            shift += codeStyle.getFormatStatementContinuationIndent() - codeStyle.indentSize();
                        }
                        break;
                    default:
                    {
                        if (entry.getKind() == LBRACE) {
                            if (entry.getImportantKind() != null &&
                                entry.getImportantKind() == ENUM) {
                                break;
                            }
                        }
                        if (entry.isLikeToArrayInitialization()){
                            break;
                        }
                        StatementKind kind = braces.getLastStatementKind(ts);
                        if (kind == null || 
                            !(kind == StatementKind.CLASS ||
                              kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                            shift += codeStyle.getFormatStatementContinuationIndent();
                        }
                        break;
                    }
                }
            } else if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE_INIT){
                if (entry.getKind() == LBRACE) {
                    if (entry.getImportantKind() != null &&
                        (entry.getImportantKind() == CLASS || entry.getImportantKind() == STRUCT ||
                         entry.getImportantKind() == UNION || entry.getImportantKind() == NAMESPACE)) {
                        shift += codeStyle.getConstructorInitializerListContinuationIndent();
                    }
                }
            }
        } else {
            if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE){
                StatementKind kind = braces.getLastStatementKind(ts);
                
                if (kind == null || 
                    !(kind == StatementKind.CLASS ||
                      kind == StatementKind.FUNCTION && braces.parenDepth == 0)) {
                    shift += codeStyle.getFormatStatementContinuationIndent();
                }
            } else if (braces.getStatementContinuation() == BracesStack.StatementContinuation.CONTINUE_INIT){
                shift += codeStyle.getConstructorInitializerListContinuationIndent();
            }
        }
        if (shift > 0) {
            return shift;
        } else {
            return 0;
        }
    }
    
    private boolean addNewLineAfterSemocolon(Token<CppTokenId> current) {
        // TODO should be controlled
        // add new line after ;
        // TODO some styles allow short statement in line
        // For example: case 1: i++; break;
        if (!ts.isLastLineToken()) {
            ts.addAfterCurrent(current, 1, getIndent(), true);
            return true;
        }
        return false;
    }

    private void braceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        StackEntry entry = braces.peek();
        if (entry != null && entry.getImportantKind() != null) {
            switch (entry.getImportantKind()) {
                case NAMESPACE: //("namespace", "keyword"), //C++
                {
                    // TODO divide for neamespace
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceNamespace(),
                            codeStyle.spaceBeforeClassDeclLeftBrace(), 1);
                    return;
                }
                case CLASS: //("class", "keyword"), //C++
                case STRUCT: //("struct", "keyword"),
                case ENUM: //("enum", "keyword"),
                case UNION: //("union", "keyword"),
                {
                    Token<CppTokenId> next = ts.lookNextImportant();
                    if (next != null){
                        newLinesAfter(previous, current);
                    }
                    return;
                }
                case IF: //("if", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeIfLeftBrace(), 1);
                    return;
                }
                case ELSE: //("else", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeElseLeftBrace(), 1);
                    return;
                }
                case SWITCH: //("switch", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewLineBeforeBraceSwitch(),
                            codeStyle.spaceBeforeSwitchLeftBrace(), 1);
                    return;
                }
                case WHILE: //("while", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeWhileLeftBrace(), 1);
                    return;
                }
                case DO: //("do", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeDoLeftBrace(), 1);
                    return;
                }
                case FOR: //("for", "keyword-directive"),
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeForLeftBrace(), 1);
                    return;
                }
                case TRY: //("try", "keyword-directive"), // C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeTryLeftBrace(), 1);
                    return;
                }
                case CATCH: //("catch", "keyword-directive"), //C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBrace(),
                            codeStyle.spaceBeforeCatchLeftBrace(), 1);
                    return;
                }
                case ARROW: //("catch", "keyword-directive"), //C++
                {
                    newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceLambda(),
                        codeStyle.spaceBeforeLambdaLeftBrace(), 1);
                    return;
                }
            }
        }
        if (entry != null && entry.isLikeToFunction()) {
            Token<CppTokenId> nextImportant = ts.lookNextImportant();
            if (nextImportant != null && nextImportant.id() == RBRACE &&
                codeStyle.ignoreEmptyFunctionBody()) {
                newLine(previous, current, BracePlacement.SAME_LINE,
                        codeStyle.spaceBeforeMethodDeclLeftBrace(), 0);
            } else {
                newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceDeclaration(),
                        codeStyle.spaceBeforeMethodDeclLeftBrace(), 1);
            }
            //if (codeStyle.newLineFunctionDefinitionName()) {
            //    functionDefinitionNewLine();
            //}
        } else if (entry != null && entry.isLikeToArrayInitialization()) {
            if (entry.isUniformInitialization()) {
                // it is equvalent to constructor
                Token<CppTokenId> previousImportant = ts.lookPreviousImportant();
                if (previousImportant != null && previousImportant.id() != RETURN) {
                    spaceBefore(previous, codeStyle.spaceBeforeMethodCallParen(), codeStyle.spaceKeepExtra());
                }
            } else {
                StackEntry prevEntry = braces.lookPerevious();
                if (prevEntry != null && prevEntry.isLikeToArrayInitialization()) {
                    // it a situation int a[][]={{
                    newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE,
                            codeStyle.spaceBeforeArrayInitLeftBrace(), 0);
                } else {
                    Token<CppTokenId> p1 = ts.lookPreviousLineImportant();
                    boolean concurent = false;
                    if (p1 != null) {
                        if (p1.id() == EQ){
                            concurent |= codeStyle.spaceAroundAssignOps();
                        }
                    }
                    newLine(previous, current, CodeStyle.BracePlacement.SAME_LINE,
                            concurent || codeStyle.spaceBeforeArrayInitLeftBrace(), 0);
                    spaceAfter(current, codeStyle.spaceWithinBraces(), codeStyle.spaceKeepExtra());
                }
            }
        } else {
            // TODO add options for block spaces 
            Token<CppTokenId> p1 = ts.lookPreviousImportant();
            if (p1 != null && p1.id() == LBRACE) {
                // it a situation while(true){{
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                return;
            }
            StackEntry prevEntry = braces.lookPerevious();
            if (prevEntry != null &&
                prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == SWITCH){
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                return;
            }
            if (prevEntry == null ||
                prevEntry != null && prevEntry.getImportantKind() != null && prevEntry.getImportantKind() == NAMESPACE){
                // It is a K&R stryle of function definition
                newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
                if (entry != null) {
                    //entry.setLikeToFunction(true);
                }
                return;
            }
            newLine(previous, current, CodeStyle.BracePlacement.NEW_LINE, true, 1);
        }
    }

    // Method does not preserve token sequence position
    // Method skips redundant NL
    private void newLinesAfter(Token<CppTokenId> previous, Token<CppTokenId> current){
        int start = ts.index();
        int lastNL = -1;
        int count = 0;
    whileLabel:
        while (ts.moveNext()) {
            switch (ts.token().id()) {
                case WHITESPACE:
                    break;
                case NEW_LINE:
                    lastNL = ts.index();
                    count++;
                    break;
                default:
                    break whileLabel;
            }
        }
        ts.moveIndex(start);
        ts.moveNext();
        newLine(previous, current, codeStyle.getFormatNewlineBeforeBraceClass(),
                codeStyle.spaceBeforeClassDeclLeftBrace(), codeStyle.blankLinesAfterClassHeader()+1);
        if (count > 1) {
            ts.moveNext();
            while (ts.moveNext() && ts.index() <= lastNL) {
                ts.replaceCurrent(ts.token(), 0, 0, false);
            }
            ts.movePrevious();
            ts.movePrevious();
        }
    }

    private void formatElse(Token<CppTokenId> previous) {
        //spaceBefore(previous, codeStyle.spaceBeforeElse());
        if (ts.isFirstLineToken()) {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                boolean done = false;
                if (diff.after != null) {
                    diff.after.replaceSpaces(getParentIndent(), true);
                    done = true;
                }
                if (diff.replace != null && previous.id() == WHITESPACE) {
                    if (!done) {
                        diff.replace.replaceSpaces(getParentIndent(), true);
                        done = true;
                    } else {
                        diff.replace.replaceSpaces(0, false);
                    }
                }
                if (diff.before != null && previous.id() == WHITESPACE){
                    if (!done) {
                        diff.before.replaceSpaces(getParentIndent(), true);
                        done = true;
                    } else {
                        diff.before.replaceSpaces(0, false);
                    }
                }
                if (done) {
                    return;
                }
            }
            if (previous.id() == WHITESPACE) {
                Token<CppTokenId> p2 = ts.lookPrevious(2);
                if (p2 != null && p2.id()== NEW_LINE) {
                    ts.replacePrevious(previous, 0, getParentIndent(), true);
                } else {
                    ts.replacePrevious(previous, 0, 0, false);
                }
            } else if (previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE) {
                ts.addBeforeCurrent(0, getParentIndent(), true);
            }
        } else if (previous != null) {
            makeSpaceBefore(codeStyle.spaceBeforeElse());
        }
    }

    private void checkDefinition() {
        int index = ts.index();
        try {
            int paren = 1;
            int comma = 0;
            int constructor = -1;
            Token<CppTokenId> next = ts.lookNextImportant();
            if (next != null && next.id() != RPAREN){
                comma = 1;
            }
            while (ts.moveNext()) {
                switch (ts.token().id()) {
                    case COLON:
                        if (paren == 0){
                            constructor = ts.index();
                        }
                        break;
                    case SEMICOLON:
                        if (comma == 0) {
                            return;
                        }
                        comma--;
                        break;
                    case COMMA:
                        if (paren == 1){
                            comma++;
                        }
                        break;
                    case RPAREN:
                        paren--;
                        break;
                    case LPAREN:
                        paren++;
                        break;
                    case LBRACE:
                        if (paren == 0) {
                            if (constructor > 0) {
                                ts.moveIndex(constructor);
                                ts.movePrevious();
                            }
                            functionDefinitionNewLine();
                        }
                        return;
                    case FOR:
                    case WHILE:
                    case IF:
                    case RBRACE:
                        return;
                    default:
                        break;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private void functionDefinitionNewLine() {
        int index = ts.index();
        try {
            int paren = 0;
            boolean hasParen = false;
            while (ts.movePrevious()) {
                if (braces.lastStatementStart >= ts.index()) {
                    return;
                }
                switch (ts.token().id()) {
                    case RPAREN:
                        paren++;
                        hasParen = true;
                        break;
                    case LPAREN:
                        paren--;
                        if (paren==0 && hasParen){
                            ArrayList<Integer> nlList = new ArrayList<Integer>();
                            Token<CppTokenId> function = ts.lookPreviousImportant();
                            if (function != null && function.id()==IDENTIFIER) {
                                int startName = -1;
                                boolean isPrevID = false;
                                while (ts.movePrevious()) {
                                    switch(ts.token().id()){
                                        case COMMA:
                                        case COLON:
                                            return;
                                        case NEW_LINE:
                                            nlList.add(ts.index());
                                            break;
                                        case BLOCK_COMMENT:
                                        case WHITESPACE:
                                            break;
                                        case TILDE:
                                            startName = ts.index();
                                            isPrevID = true;
                                            break;
                                        case SCOPE:
                                            startName = ts.index();
                                            isPrevID = false;
                                            break;
                                        case IDENTIFIER:
                                            if (!isPrevID) {
                                                startName = ts.index();
                                                isPrevID = true;
                                                break;
                                            }
                                            removeLines(startName, nlList);
                                            return;
                                        default:
                                            removeLines(startName, nlList);
                                            return;
                                    }
                                }
                                return;
                            }
                        }
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }

    private void removeLines(int startName, ArrayList<Integer> nlList){
        ts.moveIndex(startName);
        ts.moveNext();
        newLineBefore(braces.getIndent());
        for(int i = 0; i < nlList.size(); i++){
            int nl = nlList.get(i);
            if (startName < nl) {
                ts.moveIndex(nl);
                ts.moveNext();
                ts.moveNext();
                removeLineBefore(false);
            }
        }
    }

    private void indentLabel(Token<CppTokenId> previous) {
        braces.isLabel = true;
        int indent = 0;
        if (!codeStyle.absoluteLabelIndent()) {
            indent = braces.getSelfIndent();
        }
        if (doFormat()) {
            if (!ts.isFirstLineToken()) {
                ts.addBeforeCurrent(1, 0, true);
            } else {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff == null) {
                    if (previous != null && previous.id() == WHITESPACE) {
                        ts.replacePrevious(previous, 0, indent, true);
                    }
                } else {
                    if (diff.after != null) {
                        diff.after.replaceSpaces(indent, true);
                    }
                    if (diff.replace != null) {
                        diff.replace.replaceSpaces(indent, true);
                    }
                }
            }
        }
    }

    private void indentRbrace(StackEntry entry, Token<CppTokenId> previous,
                              Token<CppTokenId> current, StackEntry statementEntry) {
        
        int indent = 0;
        if (entry != null) {
            indent = continuationIndent(entry.getSelfIndent());
            if (entry.isLikeToFunction() && codeStyle.getFormatNewlineBeforeBraceDeclaration() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                indent += codeStyle.indentSize();
            } else if (entry.isLikeToArrayInitialization() && codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                indent += codeStyle.indentSize();
            } else if (entry.getImportantKind() != null) {
                switch (entry.getImportantKind()) {
                    case NAMESPACE:
                        if (codeStyle.getFormatNewlineBeforeBraceNamespace() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                            indent += codeStyle.indentSize();
                        }
                        break;
                    case CLASS:
                    case STRUCT:
                    case ENUM:
                    case UNION:
                        if (codeStyle.getFormatNewlineBeforeBraceClass() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                            indent += entry.getIndent();
                        }
                        break;
                    case SWITCH:
                        if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                            indent += codeStyle.indentSize();
                        }
                        break;
                    case ARROW:
                        if (codeStyle.getFormatNewlineBeforeBraceLambda() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                            indent += codeStyle.indentSize();
                        }
                        break;
                    default:
                        if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_FULL_INDENTED) {
                            indent += codeStyle.indentSize();
                        }
                        break;
                }
            }
        }
        Token<CppTokenId> prevImportant = ts.lookPreviousImportant();
        boolean emptyBody = false;
        boolean done = false;
        if (prevImportant != null && prevImportant.id() == LBRACE &&
            entry != null && entry.isLikeToFunction() && codeStyle.ignoreEmptyFunctionBody()) {
            emptyBody = true;
            if (ts.isFirstLineToken()){
                done = removeLineBefore(true);
                if (!done) {
                    emptyBody = false;
                }
            }
        }
        boolean isNewLineArrayInit = false;
        if (entry != null && entry.isLikeToArrayInitialization()){
            if (entry.isUniformInitialization()) {
                done = removeLineBefore(codeStyle.spaceWithinBraces());
            } else {
                isNewLineArrayInit = ts.isOpenBraceLastLineToken(1);
                if (ts.isFirstLineToken() && !isNewLineArrayInit){
                    done = removeLineBefore(codeStyle.spaceWithinBraces());
                }
            }
        }
        if (previous != null && !done) {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.before != null && previous.id() == WHITESPACE) {
                    diff.before.replaceSpaces(indent, true); // NOI18N
                    done = true;
                }
                if (diff.replace != null) {
                    if (!done) {
                        if (entry != null && entry.isLikeToArrayInitialization() &&
                            !(ts.isFirstLineToken() || diff.replace.hasNewLine())) {
                            if (codeStyle.spaceWithinBraces()) {
                                diff.replace.replaceSpaces(1, false);
                            } else {
                                diff.replace.replaceSpaces(0, false);
                            }
                        } else if (emptyBody) {
                            diff.replace.setText(0, 1, false);
                        } else {
                            diff.replace.replaceSpaces(indent, true);
                        }
                    } else {
                        diff.replace.replaceSpaces(0, false); // NOI18N
                    }
                    done = true;
                }
                if (diff.after != null) {
                    if (!done) {
                        if (emptyBody) {
                            diff.after.setText(0, 1, false);
                        } else if (diff.after.hasNewLine() || ts.isFirstLineToken()) {
                            diff.after.replaceSpaces(indent, true); // NOI18N
                        } else {
                            if (entry != null && !entry.isLikeToArrayInitialization()) {
                                ts.addBeforeCurrent(1, indent, true);
                            } else {
                                if (isNewLineArrayInit) {
                                    ts.addBeforeCurrent(1, indent, true);
                                } else {
                                    spaceBefore(previous, codeStyle.spaceWithinBraces(), codeStyle.spaceKeepExtra());
                                }
                            }
                        }
                    } else if (diff.replace != null) {
                        diff.after.setText(0, 0, false);
                    }
                    done = true;
                }
            }
            if (!done) {
                if (previous.id() == WHITESPACE) {
                    if (emptyBody) {
                        ts.replacePrevious(previous, 0, 1, false);
                    } else if (ts.isFirstLineToken()) {
                        ts.replacePrevious(previous, 0, indent, true);
                    } else {
                        if (entry != null && entry.isLikeToArrayInitialization()) {
                            if (isNewLineArrayInit) {
                                ts.replacePrevious(previous, 1, indent, true);
                            } else {
                                spaceBefore(previous, codeStyle.spaceWithinBraces(), codeStyle.spaceKeepExtra());
                            }
                        } else if (braces.parenDepth <= 0) {
                            ts.replacePrevious(previous, 1, indent, true);
                        }
                    }
                } else if (previous.id() == NEW_LINE ||
                           previous.id() == PREPROCESSOR_DIRECTIVE ||
                           previous.id() == ESCAPED_WHITESPACE) {
                    ts.addBeforeCurrent(0, indent, true);
                } else {
                    if (emptyBody) {
                        ts.addBeforeCurrent(0, 1,false);
                    } else if (entry != null && !entry.isLikeToArrayInitialization()) {
                        ts.addBeforeCurrent(1, indent, true);
                    } else {
                        if (isNewLineArrayInit) {
                            ts.addBeforeCurrent(1, indent, true);
                        } else {
                            spaceBefore(previous, codeStyle.spaceWithinBraces(), codeStyle.spaceKeepExtra());
                        }
                    }
                }
            }
        }
        boolean isClassDeclaration = entry != null && entry.getImportantKind() != null &&
                                    (entry.getImportantKind() == CLASS ||
                                     entry.getImportantKind() == STRUCT ||
                                     entry.getImportantKind() == UNION ||
                                     entry.getImportantKind() == ENUM);

        Token<CppTokenId> next = ts.lookNext();
        if (isClassDeclaration) {
            if (next != null && !isNextWitespace(next)) {
                ts.addAfterCurrent(current, 0, 1, false);
            }
            return;
        }
        Token<CppTokenId> nextImportant = ts.lookNextImportant();
        if (nextImportant != null) {
            switch (nextImportant.id()) {
                case LPAREN:
                {
                    if (entry != null && entry.getImportantKind() == ARROW) {
                        return;
                    }
                    break;
                }
                case WHILE:
                {
                    StackEntry top = braces.peek();
                    if (top != null && top.getKind() == DO) {
                        if (!codeStyle.newLineWhile()) {
                            if (ts.isLastLineToken() && isNextWitespace(next)) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, 0, 0, false);
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, 1, top.getSelfIndent(), true);
                            }
                        }
                        return;
                    }
                    break;
                }
                case CATCH:
                {
                    if (statementEntry != null &&
                        (statementEntry.getKind() == TRY || statementEntry.getKind() == CATCH)) {
                        if (!codeStyle.newLineCatch()) {
                            if (ts.isLastLineToken() && isNextWitespace(next)) {
                                Token<CppTokenId> n2 = ts.lookNext(2);
                                if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                    ts.replaceNext(current, next, 0, 0, false);
                                }
                            }
                        } else {
                            if (!ts.isLastLineToken()) {
                                ts.addAfterCurrent(current, 1, statementEntry.getSelfIndent(), true);
                            }
                        }
                        return;
                    }
                    break;
                }
                case ELSE:
                {
                    if (!codeStyle.newLineElse()) {
                        if (ts.isLastLineToken() && isNextWitespace(next)) {
                            Token<CppTokenId> n2 = ts.lookNext(2);
                            if (n2 == null || n2.id() != PREPROCESSOR_DIRECTIVE) {
                                ts.replaceNext(current, next, 0, 0, false);
                            }
                        }
                    } else {
                        if (!ts.isLastLineToken()) {
                            ts.addAfterCurrent(current, 1, indent, true);
                        }
                    }
                    return;
                }
            }
        }
        next = ts.lookNextLineImportant();
        if (next != null && !(next.id() == RPAREN || next.id() == COMMA || next.id() == SEMICOLON || next.id() == NEW_LINE)) {
            if (entry != null && entry.isUniformInitialization()){
                //
            } else {
                ts.addAfterCurrent(current, 1, indent, true);
            }
        }
    }

    private boolean isNextWitespace(Token<CppTokenId> next) {
        return next.id() == WHITESPACE || next.id() == ESCAPED_WHITESPACE || next.id() == NEW_LINE;
    }
    
    private void newLineFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null) {
            boolean done = false;
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(0, false); // NOI18N
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(0, false); // NOI18N
                    }
                    done = true;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(0, false); // NOI18N
                    done = true;
                }
            }
            if (!done && previous.id() == WHITESPACE) {
                ts.replacePrevious(previous, 0, 0, false);
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == NEW_LINE) {
                return;
            }
            int parenDepth = braces.parenDepth;
            int space = -1;
            StackEntry top = braces.peek();
            if (top != null && top.getImportantKind() == ARROW) {
                parenDepth -= top.getLambdaParen();
            }
            if (parenDepth > 0) {
                for(int i = 1; i <= parenDepth; i++){
                    space = getParenthesisIndent(i);
                    if (space >= 0) {
                        break;
                    }
                }
            } else {
                if (top != null &&
                    top.isLikeToArrayInitialization() &&
                    codeStyle.alignMultilineArrayInit()) {
                    space = ts.openBraceIndent(1);
                }
            }
            if (space == -1) {
                Token<CppTokenId> first = ts.lookNextLineImportant();
                if (first != null && braces.getStatementContinuation()!=BracesStack.StatementContinuation.STOP) {
                    switch (first.id()) {
                        case CASE:
                        case DEFAULT:
                        case FOR:
                        case IF:
                        case ELSE:
                        case DO:
                        case WHILE:
                        case SWITCH:
                        case TRY:
                        case CATCH:
                        case BREAK:
                        case RETURN:
                        case CONTINUE:
                            braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                            braces.lastStatementStart = -1;
                    }
                }
                if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
                    space = getCaseIndent();
                }
                if (braces.isDoWhile && first != null && first.id() == WHILE){
                    space = getParentIndent();
                }
            }
            if (space == -1) {
                space = getIndent();
            }
            if (next.id() == WHITESPACE) {
                ts.replaceNext(current, next, 0, space, true);
            } else {
                if (space > 0) {
                    ts.addAfterCurrent(current, 0, space, true);
                }
            }
        }
    }

    private int getParenthesisIndent(int depth){
        Token<CppTokenId> prev = ts.findOpenParenToken(depth);
        if (prev != null) {
            switch (prev.id()) {
                case FOR:
                    return countParenthesisIndent(codeStyle.alignMultilineFor(), depth, codeStyle.spaceWithinForParens());
                case IF:
                    return countParenthesisIndent(codeStyle.alignMultilineIfCondition(), depth, codeStyle.spaceWithinIfParens());
                case WHILE:
                    return countParenthesisIndent(codeStyle.alignMultilineWhileCondition(), depth, codeStyle.spaceWithinWhileParens());
                case IDENTIFIER:
                {
                    if (braces.isDeclarationLevel()) {
                        return countParenthesisIndent(codeStyle.alignMultilineMethodParams(), depth, codeStyle.spaceWithinMethodDeclParens());
                    } else {
                        return countParenthesisIndent(codeStyle.alignMultilineCallArgs(), depth, codeStyle.spaceWithinMethodCallParens());
                    }
                }
                default:
                    return countParenthesisIndent(codeStyle.alignMultilineParen(), depth, codeStyle.spaceWithinParens());
            }
        }
        return -1;
    }
    
    private int countParenthesisIndent(boolean isIndent, int depth, boolean addSpace){
        if (isIndent) {
            int i = ts.openParenIndent(depth);
            if (i >= 0) {
                if (addSpace) {
                    i++;
                }
                return i;
            }
        }
        return -1;
    }
    
    // indent new line after preprocessor directive
    private void indentNewLine(Token<CppTokenId> current){
        if (current.id() == NEW_LINE) {
            return;
        }
        int space;
        Token<CppTokenId> first = ts.lookNextLineImportant();
        if (first != null && (first.id() == CASE ||first.id() == DEFAULT)){
            space = getCaseIndent();
        } else {
            space = getIndent();
        }
        if (current.id() == WHITESPACE) {
            ts.replaceCurrent(current, 0, space, true);
        } else {
            ts.addBeforeCurrent(0, space, true);
        }
    }

    private void processColumn(Token<CppTokenId> previous, Token<CppTokenId> current) {
        boolean isLabel = braces.isLabel;
        braces.isLabel = false;
        if (doFormat()) {
            if (isLabel) {
                spaceBefore(previous, false, false);
                if (!ts.isLastLineToken()) {
                    ts.addAfterCurrent(current, 1, getIndent(), true);
                }
                braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                return;
            }
            Token<CppTokenId> p = ts.lookPreviousImportant();
            if (p != null && (p.id() == PRIVATE || p.id() == PROTECTED || p.id() == PUBLIC)) {
                spaceBefore(previous, false, false);
                if (!ts.isLastLineToken()) {
                    // TODO use flase?
                    ts.addAfterCurrent(current, 1, getIndent(), true);
                }
                return;
            }
            if (p != null && p.id() == DEFAULT) {
                // TODO use flase?
                spaceBefore(previous, false, false);
                braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                return;
            }
            if (p != null && p.id() == IDENTIFIER && qtExtension.isQtObject()) {
                if (qtExtension.isSlots(p) || qtExtension.isSignals(p)) {
                    spaceBefore(previous, false, false);
                    if (!ts.isLastLineToken()) {
                        // TODO use flase?
                        ts.addAfterCurrent(current, 1, getIndent(), true);
                    }
                    return;
                }
            }
            Token<CppTokenId> p2 = ts.lookPreviousLineImportant(CppTokenId.CASE);
            if (p2 != null && p2.id() == CASE) {
                // TODO use flase?
                spaceBefore(previous, false, false);
                braces.setStatementContinuation(BracesStack.StatementContinuation.STOP);
                return;
            }
            if (ts.isQuestionColumn()) {
                spaceBefore(previous, codeStyle.spaceAroundTernaryOps(), codeStyle.spaceKeepExtra());
                spaceAfter(current, codeStyle.spaceAroundTernaryOps(), codeStyle.spaceKeepExtra());
            } else {
                spaceBefore(previous, codeStyle.spaceBeforeColon(), codeStyle.spaceKeepExtra());
                spaceAfter(current, codeStyle.spaceAfterColon(), codeStyle.spaceKeepExtra());
            }
        }
    }

    private void reformatBlockComment(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (!ts.isFirstLineToken()){
            // do not format block comments inside cole line
            return;
        }
        int originalIndent = 0;
        if (previous == null || previous.id() == NEW_LINE || previous.id() == PREPROCESSOR_DIRECTIVE){
            originalIndent = 0;
        } else if (previous.id()==WHITESPACE) {
            CharSequence s = previous.text();
            for (int i = 0; i < previous.length(); i++) {
                if (s.charAt(i) == ' '){ // NOI18N
                    originalIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    originalIndent = (originalIndent/tabSize+1)*tabSize;
                }
            }
        }
        int requiredIndent = getIndent();
        int start = -1;
        int end = -1;
        int currentIndent = 0;
        CharSequence s = current.text();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') { // NOI18N
                start = i;
                end = i;
                currentIndent = 0;
            } else if (s.charAt(i) == ' ' || s.charAt(i) == '\t') { // NOI18N
                end = i;
                if (s.charAt(i) == ' '){ // NOI18N
                    currentIndent++;
                } else if (s.charAt(i) == '\t'){ // NOI18N
                    currentIndent = (currentIndent/tabSize+1)*tabSize;
                }
            } else {
                if (start >= 0) {
                    addCommentIndent(start, end, s.charAt(i), requiredIndent, originalIndent, currentIndent);
                }
                start = -1;
            }
        }
        addCommentIndent(start, end, '*', requiredIndent, originalIndent, currentIndent); // NOI18N
    }
    
    private void addCommentIndent(int start, int end, char c, int requiredIndent, int originalIndent, int currentIndent) {
        if (start >= 0 && end >= start) {
            if (c == '*') { // NOI18N
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, 0, 1 + requiredIndent, true);
            } else {
                int indent = requiredIndent + currentIndent - originalIndent;
                if (indent < 0) {
                    indent = requiredIndent;
                }
                diffs.addFirst(ts.offset() + start + 1, ts.offset() + end + 1, 0, indent, true); 
            }
        }
    }

    private void whiteSpaceFormat(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (diff.replace != null) {
                    return;
                }
                if (diff.before != null){
                    ts.replaceCurrent(current, 0, 0, false);
                    return;
                }
            }
            if (previous.id() == NEW_LINE ||
                previous.id() == PREPROCESSOR_DIRECTIVE) {
                // already formatted
                return;
            }
        }
        Token<CppTokenId> next = ts.lookNext();
        if (next != null && next.id() == NEW_LINE) {
            // will be formatted on new line
            return;
        }
        if (previous == null) {
            ts.replaceCurrent(current, 0, 0, false);
        } else {
            if (!codeStyle.spaceKeepExtra()) {
                ts.replaceCurrent(current, 0, 1, false);
            }
        }
    }

    private void newLine(Token<CppTokenId> previous, Token<CppTokenId> current,
            CodeStyle.BracePlacement where, boolean spaceBefore, int newLineAfter){
        if (where == CodeStyle.BracePlacement.NEW_LINE) {
            newLineBefore(IndentKind.PARENT);
        } else if (where == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED) {
            newLineBefore(IndentKind.PARENT);
        } else if (where == CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED) {
            newLineBefore(IndentKind.FULL);
        } else if (where == CodeStyle.BracePlacement.SAME_LINE) {
            if (ts.isFirstLineToken()){
                if (!removeLineBefore(spaceBefore)){
                    newLineBefore(IndentKind.PARENT);
                }
            } else {
                spaceBefore(previous, spaceBefore, false);
            }
        }
        if (newLineAfter>0){
            if (ts.isLastLineToken()) {
                if (newLineAfter>1) {
                    ts.addAfterCurrent(current, newLineAfter-1, 0, true);
                }
            } else {
                ts.addAfterCurrent(current, newLineAfter, getIndent(), true);
            }
        }
    }
 
    private void newLinesBeforeDeclaration(int lines, int start) {
        int index = ts.index();
        int[] segment = ts.getNewLinesBeforeDeclaration(start);
        try {
            if (segment[0] == -1) {
                if (start <= 0) {
                    return;
                }
                ts.moveIndex(start);
                ts.moveNext();
                ts.addBeforeCurrent(lines, 0, true); // NOI18N
            } else {
                if (segment[0] == 0) {
                    return;
                }
                ts.moveIndex(start);
                ts.moveNext();
                int indent = ts.getFirstLineTokenPosition();
                ts.moveIndex(segment[0]);
                Diff toReplace = null;
                while (ts.moveNext()) {
                    if (ts.index() > segment[1]) {
                        break;
                    }
                    DiffResult diff = diffs.getDiffs(ts, 0);
                    if (diff != null) {
                        if (diff.replace != null) {
                            diff.replace.setText(0, 0, false); // NOI18N

                            if (toReplace == null) {
                                toReplace = diff.replace;
                            }
                        } else {
                            //if (!(ts.token().id() == WHITESPACE ||
                            //    ts.token().id() == NEW_LINE)) {
                            //    System.out.println("Replace token "+ts.token().text());
                            //    ts.getNewLinesBeforeDeclaration(start);
                            //}
                            if (toReplace == null) {
                                toReplace = ts.replaceCurrent(ts.token(), 0, 0, false);
                            } else {
                                ts.replaceCurrent(ts.token(), 0, 0, false);
                            }
                        }
                        if (diff.before != null) {
                            diff.before.setText(0, 0, false); // NOI18N

                            if (toReplace == null) {
                                toReplace = diff.replace;
                            }
                        }
                    } else {
                        //if (!(ts.token().id() == WHITESPACE ||
                        //    ts.token().id() == NEW_LINE)) {
                        //    System.out.println("Replace token "+ts.token().text());
                        //    ts.getNewLinesBeforeDeclaration(start);
                        //}
                        if (toReplace == null) {
                            toReplace = ts.replaceCurrent(ts.token(), 0, 0, false);
                        } else {
                            ts.replaceCurrent(ts.token(), 0, 0, false);
                        }
                    }
                }
                if (toReplace != null) {
                    toReplace.setText(lines+segment[2], indent, true);
                } else {
                    ts.moveIndex(segment[0]);
                    ts.moveNext();
                    if (ts.token().id() == WHITESPACE ||
                        ts.token().id() == NEW_LINE) {
                        ts.replaceCurrent(ts.token(), lines+segment[2], indent, true);
                    } else {
                        ts.addBeforeCurrent(lines+segment[2], indent, true);
                    }
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private static enum IndentKind {
        PARENT,
        HALF,
        FULL,
        INDENT
    }

    private void newLineBefore(IndentKind indentKind) {
        int spaces;
        switch (indentKind) {
            case PARENT:
                spaces = getParentIndent();
                break;
            case HALF:
                spaces = (getParentIndent()+getIndent())/2;
                break;
            case FULL:
                spaces = getParentIndent()+codeStyle.indentSize();
                break;
            case INDENT:
            default:
                spaces = getIndent();
                break;
        }
        newLineBefore(spaces);
    }

    private void newLineBefore(int spaces) {
        if (!ts.isFirstLineToken()) {
            Token<CppTokenId> previous = ts.lookPrevious();
            if (previous != null) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (previous.id() == WHITESPACE) {
                    if (diff != null) {
                        if (diff.after != null) {
                            diff.after.setText(1, spaces, true);
                            if (diff.replace != null) {
                                diff.replace.setText(0, 0, false);
                            }
                            return;
                        } else if (diff.replace != null) {
                            diff.replace.setText(1, spaces, true);
                            return;
                        }
                    }
                    ts.replacePrevious(previous, 1, spaces, true);
                    return;
                } else {
                    if (diff != null) {
                        if (diff.after != null) {
                            diff.after.setText(1, spaces, true);
                            return;
                        }
                    }
                }
            }
            ts.addBeforeCurrent(1, spaces, true);
        } else {
            DiffResult diff = diffs.getDiffs(ts, -1);
            if (diff != null) {
                if (diff.after != null) {
                    diff.after.replaceSpaces(spaces, true);
                    if (diff.replace != null){
                        diff.replace.replaceSpaces(0, false);
                    }
                    return;
                } else if (diff.replace != null) {
                    diff.replace.replaceSpaces(spaces, true);
                    if (diff.before != null) {
                        diff.before.replaceSpaces(0, false);
                    }
                    return;
                }
            }
            Token<CppTokenId> previous = ts.lookPrevious();
            if (previous != null) {
                if (previous.id() == WHITESPACE) {
                    ts.replacePrevious(previous, 0, spaces, true);
                } else if (previous.id() == NEW_LINE) {
                    ts.addBeforeCurrent(0, spaces, true);
                }
            }
        }
    }

    private void spaceBefore(Token<CppTokenId> previous, boolean add, boolean keepExtra){
        if (previous != null && !ts.isFirstLineToken()) {
            if (add) {
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        int spacing = 1;
                        if (keepExtra) {
                            spacing = Math.max(spacing, diff.after.spaceLength());
                        }
                        diff.after.replaceSpaces(spacing, false);
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(0, false);
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(1, false);
                        return;
                    }
                }
                if (!(previous.id() == WHITESPACE ||
                      previous.id() == NEW_LINE ||
                      previous.id() == PREPROCESSOR_DIRECTIVE)) {
                    ts.addBeforeCurrent(0, 1, false);
                }
            } else if (canRemoveSpaceBefore(previous) && !keepExtra){
                DiffResult diff = diffs.getDiffs(ts, -1);
                if (diff != null) {
                    if (diff.after != null && !diff.after.hasNewLine()) {
                        diff.after.replaceSpaces(0, false);
                        if (diff.replace != null && !diff.replace.hasNewLine()){
                            diff.replace.replaceSpaces(0, false);
                        }
                        return;
                    } else if (diff.replace != null && !diff.replace.hasNewLine()) {
                        diff.replace.replaceSpaces(0, false);
                        return;
                    }
                }
                if (previous.id() == WHITESPACE) {
                    ts.replacePrevious(previous, 0, 0, false);
                }
            }
        }
    }

    private boolean canRemoveSpaceBefore(Token<CppTokenId> previous){
        if (previous == null) {
            return false;
        }
        if (previous.id() == WHITESPACE) {
            Token<CppTokenId> p2 = ts.lookPrevious(2);
            if (p2 == null) {
                return true;
            }
            previous = p2;
        }
        CppTokenId prev = previous.id();
        CppTokenId curr = ts.token().id();
        return canRemoveSpace(prev,curr);
    }

    private boolean canRemoveSpace(CppTokenId prev, CppTokenId curr){
        if (prev == IDENTIFIER && curr == IDENTIFIER) {
            return false;
        }
        String currCategory = curr.primaryCategory();
        String prevCategory = prev.primaryCategory();
        if (KEYWORD_CATEGORY.equals(prevCategory) ||
            KEYWORD_DIRECTIVE_CATEGORY.equals(prevCategory)) {
            if (SEPARATOR_CATEGORY.equals(currCategory)) {
                return true;
            } else if (OPERATOR_CATEGORY.equals(currCategory)) {
                return true;
            } else if (curr == COLON) {
                return true;
            }
            return false;
        } else if (OPERATOR_CATEGORY.equals(prevCategory)) {
            if (OPERATOR_CATEGORY.equals(currCategory)) {
                if (curr == GT && (prev == STAR || prev == AMP)){
                    return true;
                } else if (prev == QUESTION || prev == COLON ||
                           curr == QUESTION || curr == COLON){
                    return true;
                }
                return false;
            }
            return true;
        } else if (prev == IDENTIFIER) {
            if (NUMBER_CATEGORY.equals(currCategory) ||
                LITERAL_CATEGORY.equals(currCategory) ||
                CHAR_CATEGORY.equals(currCategory) ||
                STRING_CATEGORY.equals(currCategory)) {
                return false;
            }
        }
        return true;
    }

    private boolean canRemoveSpaceAfter(Token<CppTokenId> current){
        Token<CppTokenId> next = ts.lookNext();
        if (next == null) {
            return false;
        }
        if (next.id() == WHITESPACE) {
            Token<CppTokenId> n2 = ts.lookNext(2);
            if (n2 == null) {
                return true;
            }
            next = n2;
        }
        CppTokenId curr = next.id();
        CppTokenId prev = current.id();
        return canRemoveSpace(prev,curr);
    }
    
    private void spaceAfter(Token<CppTokenId> current, boolean add, boolean keepExtra){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (add) {
                if (!isNextWitespace(next)) {
                    ts.addAfterCurrent(current, 0, 1, false);
                }
            } else if (canRemoveSpaceAfter(current) && !keepExtra){
                if (next.id() == WHITESPACE) {
                    ts.replaceNext(current, next, 0, 0, false);
                }
            }
        }
    }

    private void spaceAfterBefore(Token<CppTokenId> current, boolean add, CppTokenId before, boolean keepExtra){
        Token<CppTokenId> next = ts.lookNext();
        if (next != null) {
            if (next.id() == WHITESPACE) {
                Token<CppTokenId> p = ts.lookNext(2);
                if (p!=null && p.id()==before) {
                    if (!add && !keepExtra) {
                        ts.replaceNext(current, next, 0, 0, false); // NOI18N
                    }
                }
            } else if (next.id() == before) {
                if (add) {
                    ts.addAfterCurrent(current, 0, 1, false);
                }
            }
        }
    }

    private void formatLeftParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceAfter(current, codeStyle.spaceWithinIfParens(), codeStyle.spaceKeepExtra());
                        return;
                    case FOR:
                        spaceAfter(current, codeStyle.spaceWithinForParens(), codeStyle.spaceKeepExtra());
                        return;
                    case WHILE:
                        spaceAfter(current, codeStyle.spaceWithinWhileParens(), codeStyle.spaceKeepExtra());
                        return;
                    case SWITCH:
                        spaceAfter(current, codeStyle.spaceWithinSwitchParens(), codeStyle.spaceKeepExtra());
                        return;
                    case CATCH:
                        spaceAfter(current, codeStyle.spaceWithinCatchParens(), codeStyle.spaceKeepExtra());
                        return;
                }
            }
            p = ts.lookPreviousImportant();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                    spaceAfter(current, codeStyle.spaceWithinMethodDeclParens(), codeStyle.spaceKeepExtra());
                    if (codeStyle.newLineFunctionDefinitionName()) {
                        checkDefinition();
                    }
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case STRUCT:
                        case UNION:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceBeforeMethodDeclParen(), codeStyle.spaceKeepExtra());
                            spaceAfter(current, codeStyle.spaceWithinMethodDeclParens(), codeStyle.spaceKeepExtra());
                            if (codeStyle.newLineFunctionDefinitionName()) {
                                checkDefinition();
                            }
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceBeforeMethodCallParen(), codeStyle.spaceKeepExtra());
                spaceAfter(current, codeStyle.spaceWithinMethodCallParens(), codeStyle.spaceKeepExtra());
            } else if (p != null && 
                       (KEYWORD_CATEGORY.equals(p.id().primaryCategory()) ||
                        KEYWORD_DIRECTIVE_CATEGORY.equals(p.id().primaryCategory()))){
                switch (p.id()) {
                    case SIZEOF:
                    case TYPEID:
                    case TYPEOF:
                    case __HAS_TRIVIAL_CONSTRUCTOR:
                    case __HAS_NOTHROW_ASSIGN:
                    case __HAS_NOTHROW_COPY:
                    case __HAS_NOTHROW_CONSTRUCTOR:
                    case __HAS_TRIVIAL_ASSIGN:
                    case __HAS_TRIVIAL_COPY:
                    case __HAS_TRIVIAL_DESTRUCTOR:
                    case __IS_ABSTRACT:
                    case __IS_EMPTY:
                    case __IS_LITERAL_TYPE:
                    case __IS_POLYMORPHIC:
                    case __IS_STANDARD_LAYOUT:
                    case __IS_TRIVIAL:
                    case __IS_UNION:
                    case __UNDERLYING_TYPE:
                    case __IS_CLASS:
                    case __IS_BASE_OF:
                    case __IS_POD:                        
                    case __TYPEOF:
                    case __TYPEOF__:
                    case ALIGNOF:
                    case __ALIGNOF__:
                    case THROW:
                    case __ATTRIBUTE__:
                    case __ATTRIBUTE:
                    case _DECLSPEC:
                    case __DECLSPEC:
                    case _FAR:
                    case __FAR:
                    case _NEAR:
                    case __NEAR:
                    case _STDCALL:
                    case __STDCALL:
                        spaceBefore(previous, codeStyle.spaceBeforeKeywordParen(), codeStyle.spaceKeepExtra());
                        return;
                    case RETURN:
                        spaceBefore(previous, codeStyle.spaceBeforeKeywordParen(), codeStyle.spaceKeepExtra());
                        if (ts.isTypeCast()) {
                            spaceAfter(current, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
                        }
                        return;
                }
            } else if (ts.isTypeCast()){
                spaceAfter(current, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
            } else {
                spaceAfter(current, codeStyle.spaceWithinParens(), codeStyle.spaceKeepExtra());
            }
        }
    }

    private void formatRightParen(Token<CppTokenId> previous, Token<CppTokenId> current) {
        if (previous != null){
            Token<CppTokenId> p = ts.lookPreviousStatement();
            if (p != null) {
                switch(p.id()) {
                    case IF:
                        spaceBefore(previous, codeStyle.spaceWithinIfParens(), codeStyle.spaceKeepExtra());
                        return;
                    case FOR:
                        spaceBefore(previous, codeStyle.spaceWithinForParens(), codeStyle.spaceKeepExtra());
                        return;
                    case WHILE:
                        spaceBefore(previous, codeStyle.spaceWithinWhileParens(), codeStyle.spaceKeepExtra());
                        return;
                    case SWITCH:
                        spaceBefore(previous, codeStyle.spaceWithinSwitchParens(), codeStyle.spaceKeepExtra());
                        return;
                    case CATCH:
                        spaceBefore(previous, codeStyle.spaceWithinCatchParens(), codeStyle.spaceKeepExtra());
                        return;
                }
            }
            p = getImportantBeforeBrace();
            if (p != null && p.id() == IDENTIFIER) {
                StackEntry entry = braces.peek();
                if (entry == null){
                    spaceBefore(previous, codeStyle.spaceWithinMethodDeclParens(), codeStyle.spaceKeepExtra());
                    return;
                }
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case CLASS:
                        case STRUCT:
                        case UNION:
                        case NAMESPACE:
                            spaceBefore(previous, codeStyle.spaceWithinMethodDeclParens(), codeStyle.spaceKeepExtra());
                            return;
                    }
                }
                spaceBefore(previous, codeStyle.spaceWithinMethodCallParens(), codeStyle.spaceKeepExtra());
            } else if (p != null && checkTokenIsBuiltInTypeTraitFunction(p.id())) {
                spaceBefore(previous, codeStyle.spaceWithinParens(), codeStyle.spaceKeepExtra());
            } else if (ts.isTypeCast()){
                spaceBefore(previous, codeStyle.spaceWithinTypeCastParens(), codeStyle.spaceKeepExtra());
                spaceAfter(current, codeStyle.spaceAfterTypeCast(), codeStyle.spaceKeepExtra());
            } else {
                spaceBefore(previous, codeStyle.spaceWithinParens(), codeStyle.spaceKeepExtra());
            }
        }
    }
    
    private boolean checkTokenIsBuiltInTypeTraitFunction(CppTokenId tokenId) {
        return tokenId == TYPEID ||
                tokenId == SIZEOF ||
                tokenId == TYPEOF ||
                tokenId == __HAS_TRIVIAL_CONSTRUCTOR ||
                tokenId == __HAS_NOTHROW_ASSIGN ||
                tokenId == __HAS_NOTHROW_COPY ||
                tokenId == __HAS_NOTHROW_CONSTRUCTOR ||
                tokenId == __HAS_TRIVIAL_ASSIGN ||
                tokenId == __HAS_TRIVIAL_COPY ||
                tokenId == __HAS_TRIVIAL_DESTRUCTOR ||
                tokenId == __IS_ABSTRACT ||
                tokenId == __IS_EMPTY ||
                tokenId == __IS_LITERAL_TYPE ||
                tokenId == __IS_POLYMORPHIC ||
                tokenId == __IS_STANDARD_LAYOUT ||
                tokenId == __IS_TRIVIAL ||
                tokenId == __IS_UNION ||
                tokenId == __UNDERLYING_TYPE ||
                tokenId == __IS_CLASS ||
                tokenId == __IS_BASE_OF ||
                tokenId == __IS_POD;
    }

    private Token<CppTokenId> getImportantBeforeBrace(){
        int index = ts.index();
        try {
            if (ts.token().id() == RPAREN) {
                int depth = 1;
                while (ts.movePrevious()) {
                    switch (ts.token().id()) {
                        case RPAREN:
                            depth++;
                            break;
                        case LPAREN:
                        {
                            depth--;
                            if (depth <=0) {
                                return ts.lookPreviousImportant();
                            }
                            break;
                        }
                    }
                }
            }
            return null;
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private boolean isOperator(){
        Token<CppTokenId> p = ts.lookPreviousImportant(1);
        return p != null && p.id() == OPERATOR;
    }
    private boolean isOperator2(){
        Token<CppTokenId> p = ts.lookPreviousImportant(2);
        return p != null && p.id() == OPERATOR;
    }

    private boolean isNewStyleCast(Token<CppTokenId> current){
        if (current.id() == LT) {
            Token<CppTokenId> p = ts.lookPreviousImportant(1);
            if (p != null) {
                return p.id() == REINTERPRET_CAST ||
                       p.id() == STATIC_CAST ||
                       p.id() == CONST_CAST ||
                       p.id() == DYNAMIC_CAST;
            }
            return false;
        } else if (current.id() == GT) {
            // TODO fing matched LT
            int index = ts.index();
            try {
                int depth = 1;
                while (ts.movePrevious()) {
                    switch (ts.token().id()) {
                        case GT:
                            depth++;
                            break;
                        case LT:
                        {
                            depth--;
                            if (depth <=0) {
                                Token<CppTokenId> p = ts.lookPreviousImportant(1);
                                if (p != null) {
                                    return p.id() == REINTERPRET_CAST ||
                                           p.id() == STATIC_CAST ||
                                           p.id() == CONST_CAST ||
                                           p.id() == DYNAMIC_CAST;
                                }
                                return false;
                            }
                            break;
                        }
                        case LBRACE:
                        case RBRACE:
                        case SEMICOLON:
                            return false;
                    }
                }
            } finally {
                ts.moveIndex(index);
                ts.moveNext();
            }
        }
        return false;
    }
    
    // <importantFrom><WS><NL><WS><importantTo>
    // where <NL> replaced on nonNL
    // indexTo point to importantTo
    // importantTo shoul be not first line token
    // method removes chain <WS><NL><WS> or replaces it to on space
    private boolean makeSpaceBefore(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == NEW_LINE){
                    DiffResult diff = diffs.getDiffs(ts, 0);
                    if (diff == null || diff.replace == null || diff.replace.hasNewLine()){
                        return false;
                    }
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return false;
                } else if (ts.token().id() != WHITESPACE){
                    replaceSegment(addSpace, index);
                    return true;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    private boolean removeLineBefore(boolean addSpace){
        int index = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return false;
                }
                if (ts.token().id() == NEW_LINE){
                    if (ts.movePrevious()) {
                        if (ts.token().id() == WHITESPACE) {
                            ts.movePrevious();
                            replaceSegment(addSpace, index);
                            return true;
                        } else if ((ts.token().id() != LINE_COMMENT && ts.token().id() != DOXYGEN_LINE_COMMENT)) {
                            replaceSegment(addSpace, index);
                            return true;
                        }
                    }
                    return false;
                } else if (ts.token().id() == PREPROCESSOR_DIRECTIVE){
                    return false;
                } else if (ts.token().id() != WHITESPACE){
                    replaceSegment(addSpace, index);
                    return true;
                }
            }
        } finally {
            ts.moveIndex(index);
            ts.moveNext();
        }
    }
    
    // <importantFrom><WS><NL><WS><importantTo>
    // current ts point to importantFrom
    // indexTo point to importantTo
    // method removes chain <WS><NL><WS> or replaces it to on space
    private void replaceSegment(boolean addSpace, int indexTo) {
        boolean first = true;
        Diff diffToSpace = null;
        while (ts.index() < indexTo) {
            DiffResult diff = diffs.getDiffs(ts, 0);
            if (diff != null) {
                if (!first) {
                    if (diff.replace != null) {
                        if (diffToSpace == null) {
                            diffToSpace = diff.replace;
                        }
                        diff.replace.setText(0, 0, false);
                    } else {
                        Diff added = diffs.addFirst(ts.offset(), ts.offset()+ts.token().length(), 0, 0, false);
                        if (diffToSpace == null) {
                            diffToSpace = added;
                        }
                    }
                }
                if (diff.after != null) {
                    if (diffToSpace == null) {
                        diffToSpace = diff.after;
                    }
                    diff.after.setText(0, 0, false);
                }
            }
            if (!first && diff == null) {
                Diff added = diffs.addFirst(ts.offset(), ts.offset() + ts.token().length(), 0, 0, false);
                if (diffToSpace == null) {
                    diffToSpace = added;
                }
            }
            first = false;
            ts.moveNext();
        }
        if (addSpace) {
            if (diffToSpace != null){
                diffToSpace.setText(0, 1, false);
            } else {
                ts.addBeforeCurrent(0, 1, false);
            }
        }
    }

    /*package local*/ boolean doFormat(){
        return ts.offset() >= this.startOffset;
    }
}
