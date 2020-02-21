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

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 */
class BracesStack implements Cloneable {
    
    private static final boolean TRACE_STACK = false;
    private static final boolean TRACE_STATEMENT = false;
    
    private final Stack<StackEntry> stack = new Stack<StackEntry>();
    private final CodeStyle codeStyle;
    private StatementContinuation statementContinuation = StatementContinuation.STOP;
    int lastStatementStart = -1;
    int parenDepth = 0;
    int lastKRstart = -1;
    boolean isDoWhile = false;
    boolean isLabel = false;
    int lastStatementParen = -1;

    BracesStack(CodeStyle codeStyle) {
        this.codeStyle = codeStyle;
    }

    @Override
    public BracesStack clone(){
        BracesStack clone = new BracesStack(codeStyle);
        clone.statementContinuation = statementContinuation;
        clone.lastStatementStart = lastStatementStart;
        clone.lastKRstart = lastKRstart;
        clone.parenDepth = parenDepth;
        clone.isDoWhile = isDoWhile;
        clone.lastStatementParen = lastStatementParen;
        clone.isLabel = isLabel;
        for(int i = 0; i < stack.size(); i++){
            clone.stack.add(stack.get(i));
        }
        return clone;
    }
    
    public void reset(BracesStack clone){
        statementContinuation = clone.statementContinuation;
        lastStatementStart = clone.lastStatementStart;
        lastKRstart = clone.lastKRstart;
        parenDepth = clone.parenDepth;
        isDoWhile = clone.isDoWhile;
        lastStatementParen = clone.lastStatementParen;
        isLabel = clone.isLabel;
        stack.clear();
        for(int i = 0; i < clone.stack.size(); i++){
            stack.add(clone.stack.get(i));
        }
    }

    public void push(ExtendedTokenSequence ts) {
        StackEntry prevEntry = peek();
        int prevIndent = 0;
        int prevSelfIndent = 0;
        int statementIndent = codeStyle.indentSize();
        if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED){
            statementIndent = codeStyle.indentSize()/2;
        }
        //if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_FULL_INDENTED){
        //    statementIndent = codeStyle.indentSize();
        //}
        int switchIndent = codeStyle.indentSize();
        if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_HALF_INDENTED){
            switchIndent = codeStyle.indentSize()/2;
        }
        if (prevEntry != null){
            prevIndent = prevEntry.getIndent();
            prevSelfIndent = prevEntry.getSelfIndent();
        }
        StackEntry newEntry = new StackEntry(ts);
        switch (newEntry.getKind()) {
            case ELSE: //("else", "keyword-directive"),
                if (prevEntry != null && 
                   (prevEntry.getKind() == IF || prevEntry.getKind() == ELSE)) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case IF: //("if", "keyword-directive"),
                if (prevEntry != null && prevEntry.getKind() == ELSE) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case WHILE: //("while", "keyword-directive"),
                if (isDoWhile) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case CATCH: //("catch", "keyword-directive"), //C++
            case DO: //("do", "keyword-directive"),
            case TRY: //("try", "keyword-directive"), // C++
            case FOR: //("for", "keyword-directive"),
            case ASM: //("asm", "keyword-directive"), // gcc and C++
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case SWITCH: //("switch", "keyword-directive"),
                newEntry.setIndent(prevIndent + switchIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case LBRACE:
            {
                CppTokenId kind = newEntry.getImportantKind();
                if (kind != null) {
                    switch (kind) {
                        case SWITCH: //("switch", "keyword-directive"),
                            if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_HALF_INDENTED){
                                if (codeStyle.indentCasesFromSwitch()) {
                                    newEntry.setIndent(prevIndent + codeStyle.indentSize());
                                    newEntry.setSelfIndent(prevIndent);
                                    break;
                                }
                                newEntry.setIndent(prevIndent+switchIndent);
                                newEntry.setSelfIndent(prevIndent);
                            } else {
                                if (codeStyle.indentCasesFromSwitch()) {
                                    newEntry.setIndent(prevIndent + switchIndent);
                                    newEntry.setSelfIndent(prevSelfIndent);
                                    break;
                                }
                                newEntry.setIndent(prevIndent);
                                newEntry.setSelfIndent(prevSelfIndent);
                            }
                            break;
                        case ELSE: //("else", "keyword-directive"),
                        case IF: //("if", "keyword-directive"),
                        case TRY: //("try", "keyword-directive"), // C++
                        case CATCH: //("catch", "keyword-directive"), //C++
                        case WHILE: //("while", "keyword-directive"),
                        case FOR: //("for", "keyword-directive"),
                        case DO: //("do", "keyword-directive"),
                        case ASM: //("asm", "keyword-directive"), // gcc and C++
                            if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED){
                                newEntry.setIndent(prevIndent+statementIndent);
                                newEntry.setSelfIndent(prevIndent);
                            } else {
                                newEntry.setIndent(prevIndent);
                                newEntry.setSelfIndent(prevSelfIndent);
                            }
                            break;
                        case NAMESPACE: //("namespace", "keyword"), //C++
                            if (codeStyle.indentNamespace()) {
                                statementIndent = codeStyle.indentSize();
                                if (codeStyle.getFormatNewlineBeforeBraceNamespace() == BracePlacement.NEW_LINE_HALF_INDENTED){
                                    statementIndent /= 2;
                                }
                                newEntry.setIndent(prevIndent + statementIndent);
                                newEntry.setSelfIndent(prevIndent);
                            } else {
                                newEntry.setIndent(prevIndent);
                                newEntry.setSelfIndent(prevIndent);
                            }
                            break;
                        case CLASS: //("class", "keyword"), //C++
                        case STRUCT: //("struct", "keyword"),
                        case ENUM: //("enum", "keyword"),
                        case UNION: //("union", "keyword"),
                            statementIndent = codeStyle.indentSize();
                            if (codeStyle.getFormatNewlineBeforeBraceClass() == BracePlacement.NEW_LINE_HALF_INDENTED){
                                statementIndent /= 2;
                            }
                            newEntry.setIndent(prevIndent + statementIndent);
                            newEntry.setSelfIndent(prevIndent);
                            break;
                        case ARROW: // LAMBDA C++
                            if (newEntry.getLambdaIndent() >= prevIndent) {
                                prevIndent = newEntry.getLambdaIndent();
                            }
                            statementIndent = codeStyle.indentSize();
                            if (codeStyle.getFormatNewlineBeforeBraceLambda() == BracePlacement.NEW_LINE_HALF_INDENTED){
                                newEntry.setIndent(prevIndent + statementIndent);
                                statementIndent /= 2;
                                newEntry.setSelfIndent(prevIndent + statementIndent);
                            } else {
                                newEntry.setIndent(prevIndent + statementIndent);
                                newEntry.setSelfIndent(prevIndent);
                            }
                            newEntry.setLambdaParen(parenDepth);
                    }
                } else if (newEntry.isLikeToFunction()){
                    statementIndent = codeStyle.indentSize();
                    if (codeStyle.getFormatNewlineBeforeBraceDeclaration() == BracePlacement.NEW_LINE_HALF_INDENTED){
                        statementIndent /= 2;
                    }
                    newEntry.setIndent(prevIndent + statementIndent);
                    newEntry.setSelfIndent(prevIndent);
                } else if (newEntry.isLikeToArrayInitialization()){
                    newEntry.setIndent(prevIndent + statementIndent);
                    newEntry.setSelfIndent(prevIndent);
                } else {
                    if (prevEntry != null && prevEntry.getImportantKind() == SWITCH) {
                        if (codeStyle.getFormatNewLineBeforeBraceSwitch() == BracePlacement.NEW_LINE_HALF_INDENTED){
                            newEntry.setIndent(prevIndent + switchIndent);
                            newEntry.setSelfIndent(prevIndent);
                        } else {
                            if (codeStyle.indentCasesFromSwitch()) {
                                newEntry.setIndent(prevSelfIndent + codeStyle.indentSize() + switchIndent);
                                newEntry.setSelfIndent(prevSelfIndent + switchIndent);
                            } else {
                                newEntry.setIndent(prevSelfIndent + switchIndent);
                                newEntry.setSelfIndent(prevSelfIndent);
                            }
                        }
                    } else {
                        newEntry.setIndent(prevIndent + statementIndent);
                        newEntry.setSelfIndent(prevIndent);
                    }
                }
            }
        }
        push(newEntry);
    }
        
    public int getIndent(){
        StackEntry top = peek();
        if (top != null) {
            return top.getIndent();
        }
        return 0;
    }

    public int getSelfIndent(){
        StackEntry top = peek();
        if (top != null) {
            return top.getSelfIndent();
        }
        return 0;
    }

    private void push(StackEntry entry) {
        statementContinuation = StatementContinuation.STOP;
        if (entry.getKind() == ELSE){
            if (stack.size() > 0 && 
                (stack.peek().getKind() == IF || stack.peek().getKind() == ELSE)) {
                stack.pop();
            }
        }
        if (!(entry.getImportantKind() != null ||
              entry.isLikeToArrayInitialization())) {
            if (peek() != null && peek().isLikeToArrayInitialization()){
                // this is two dimensiomal arry initialization
                entry.setLikeToArrayInitialization();
                if (parenDepth > 0) {
                    entry.setLikeToArrayInitialization();
                }
            }
        }
        if (entry.getKind() == LBRACE){
            if(entry.isLikeToArrayInitialization()) {
                if (parenDepth > 0) {
                    // This is array in paraneter
                    entry.setLikeToArrayInitialization();
                }
            } else {
                clearLastStatementStart();
                if (entry.getImportantKind() == ARROW) {
                    parenDepth = 0;
                }
            }
        } else if (lastStatementStart != entry.getIndex()) {
            lastStatementStart = entry.getIndex();
            if (TRACE_STATEMENT) {System.out.println("start of Statement/Declaration:"+entry.getText());} // NOI18N
        }
        stack.push(entry);
        if (TRACE_STACK) {System.out.println("push: "+toString());} // NOI18N
    }

    public void pop(ExtendedTokenSequence ts) {
        StackEntry peek = peek();
        if (peek != null) {
            if (peek.getImportantKind() == ARROW) {
                parenDepth = peek.getLambdaParen();
            }
        }
        if (parenDepth <= 0) {
            clearLastStatementStart();
        }
        statementContinuation = StatementContinuation.STOP;
        popImpl(ts);
        if (TRACE_STACK) {System.out.println("pop "+ts.token().id().name()+": "+toString());} // NOI18N
    }

    public void popImpl(ExtendedTokenSequence ts) {
        if (stack.empty()) {
            return;
        }
        CppTokenId id = ts.token().id();
        if (id == RBRACE) {
            popBrace(ts);
        } else {
            popStatement(ts);
        }
    }

    public void popBrace(ExtendedTokenSequence ts) {
        int brace = 0;
        StackEntry top = null;
        for (int i = stack.size() - 1; i >= 0; i--) {
            top = stack.get(i);
            if (top.getKind() == LBRACE) {
                brace = i - 1;
                stack.setSize(i);
                break;
            }
        }
        if (brace < 0) {
            stack.setSize(0);
            return;
        }
        if (top != null) {
            if (top.isLikeToArrayInitialization()) {
                return;
            }
        }
        popStatement(ts);
    }

    public void popStatement(ExtendedTokenSequence ts) {
        Token<CppTokenId> next = getNextImportant(ts);
        for (int i = stack.size() - 1; i >= 0; i--) {
            StackEntry top = stack.get(i);
            switch (top.getKind()) {
                case LBRACE: {
                    stack.setSize(i + 1);
                    return;
                }
                case IF: //("if", "keyword-directive"),
                {
                    if (next != null && next.id() == ELSE) {
                        if (i > 0 && stack.get(i-1).getKind() == ELSE) {
                            stack.setSize(i);
                            return;
                        } else {
                            stack.setSize(i + 1);
                            return;
                        }
                    }
                    break;
                }
                case DO: //("do", "keyword-directive"),
                {
                    if (next != null && next.id() == WHILE) {
                        if (i+1 < stack.size() && stack.get(i+1).getKind() == WHILE) {
                            break;
                        } else {
                            stack.setSize(i + 1);
                            return;
                        }
                    }
                    break;
                }
                case TRY: //("try", "keyword-directive"), // C++
                case CATCH: //("catch", "keyword-directive"), //C++
                    if (next != null && next.id() == CATCH) {
                        if (i > 0) {
                            stack.setSize(i);
                            return;
                        }
                    }
                    break;
                case ELSE: //("else", "keyword-directive"),
                case SWITCH: //("switch", "keyword-directive"),
                case FOR: //("for", "keyword-directive"),
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                case WHILE: //("while", "keyword-directive"),
                    break;
            }
        }
        stack.setSize(0);
    }
    
    private boolean isStatement(StackEntry top){
        if (top != null) {
            switch (top.getKind()) {
                case IF: //("if", "keyword-directive"),
                case ELSE: //("else", "keyword-directive"),
                case TRY: //("try", "keyword-directive"), // C++
                case CATCH: //("catch", "keyword-directive"), //C++
                case SWITCH: //("switch", "keyword-directive"),
                case FOR: //("for", "keyword-directive"),
                case ASM: //("asm", "keyword-directive"), // gcc and C++
                case DO: //("do", "keyword-directive"),
                case WHILE: //("while", "keyword-directive"),
                    return true;
            }
        }
        return false;
    }
    
    public boolean isDeclarationLevel(){
        StackEntry top = peek();
        if (top == null) {
            return true;
        }
        if (top.getKind() == CATCH){
            return true;
        }
        if (isStatement(top)){
            return false;
        }
        CppTokenId id = top.getImportantKind();
        if (id == null){
            return false;
        }
        return id == CppTokenId.NAMESPACE || id == CppTokenId.CLASS || id == CppTokenId.STRUCT || id == CppTokenId.UNION;
    }
    
    public StackEntry peek() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public int getLength() {
        return stack.size();
    }
    
    public int switchDepth(){
        int res = 0;
        StackEntry prev = null;
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (entry.getKind() == LBRACE) {
                if (prev != null && prev.getKind() == SWITCH) {
                    res++;
                }
            }
            prev = entry;
        }
        return res;
    }

    public StackEntry lookPerevious(){
        if (stack.size() < 2) {
            return null;
        }
        return stack.get(stack.size()-2);
        
    }
    
    private Token<CppTokenId> getNextImportant(ExtendedTokenSequence ts) {
        int i = ts.index();
        try {
            while (true) {
                if (!ts.moveNext()) {
                    return null;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case ASM: //("asm", "keyword-directive"), // gcc and C++
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                        return current;
                    default:
                        return null;
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < stack.size(); i++){
            StackEntry entry = stack.get(i);
            if (i > 0) {
                buf.append(", "); // NOI18N
            }
            buf.append(entry.toString());
        }
        buf.append("+"+getIndent()+"-"+getSelfIndent()); // NOI18N
        return buf.toString();
    }

    public StatementContinuation getStatementContinuation() {
        return statementContinuation;
    }

    public void setStatementContinuation(StatementContinuation statementContinuation) {
        this.statementContinuation = statementContinuation;
    }

    public StatementKind getLastStatementKind(ExtendedTokenSequence ts) {
        if (lastStatementStart < 0) {
            return null;
        }
        int i = ts.index();
        try {
            int paren = 0;
            int curly = 0;
            int triangle = 0;
            ts.moveIndex(lastStatementStart);
            StatementKind res = null;
            while (true) {
                if (!ts.moveNext()) {
                    return null;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case RPAREN: //(")", "separator"),
                    {
                        paren--;
                        break;
                    }
                    case LPAREN: //("(", "separator"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            if (isDeclarationLevel()){
                                return StatementKind.FUNCTION;
                            } else {
                                return StatementKind.EXPRESSION_STATEMENT;
                            }
                        }
                        paren++;
                        break;
                    }
                    case TEMPLATE:
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            if (isDeclarationLevel()){
                                return StatementKind.FUNCTION;
                            }
                        }
                        break;
                    }
                    case RBRACE: //("}", "separator"),
                    case LBRACE: //("{", "separator"),
                    case SEMICOLON: //(";", "separator"),
                    {
                       if (isDeclarationLevel()){
                           if (res != null){
                               return res;
                           }
                           return StatementKind.FUNCTION;
                        } else {
                            return StatementKind.DECLARATION_STATEMENT;
                        }
                    }
                    case EQ: //("=", "operator"),
                    {
                       if (isDeclarationLevel()){
                            return StatementKind.DECLARATION_STATEMENT;
                        } else {
                            return StatementKind.EXPRESSION_STATEMENT;
                        }
                    }
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
                        if (paren == 0) {
                            return StatementKind.EXPRESSION_STATEMENT;
                        }
                        break;
                    }
                    case GT: //(">", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            triangle--;
                        }
                        break;
                    }
                    case LT: //("<", "operator"),
                    {
                        if (paren == 0 && curly == 0) {
                            triangle++;
                        }
                        break;
                    }
                    case NAMESPACE: //("namespace", "keyword"), //C++
                        return StatementKind.NAMESPACE;
                    case CLASS: //("class", "keyword"), //C++
                        return StatementKind.CLASS;
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            res = StatementKind.CLASS;
                        }
                        break;
                    }
                    case EXTERN: //EXTERN("extern", "keyword"),
                    {
                        if (paren == 0 && curly == 0 && triangle == 0) {
                            res = StatementKind.NAMESPACE;
                        }
                        break;
                    }
                    case ASM: //("if", "keyword-directive"),
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                       return StatementKind.COMPAUND_STATEMENT;
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }
    
    public void clearLastStatementStart() {
        lastStatementStart = -1;
    }
    
    public void setLastStatementStart(ExtendedTokenSequence ts) {
        if (lastStatementStart == -1) {
            lastStatementStart = ts.index();
            if (TRACE_STATEMENT) {System.out.println("start of Statement/Declaration:"+ts.token().text());} // NOI18N
        }
    }
    
    public static enum StatementContinuation {
        START,
        CONTINUE,
        CONTINUE_INIT,
        STOP;
    }

    public static enum StatementKind {
        NAMESPACE,
        CLASS,
        FUNCTION,
        DECLARATION_STATEMENT,
        COMPAUND_STATEMENT,
        EXPRESSION_STATEMENT;
    }
}
