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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 */
public class ContextDetector extends ExtendedTokenSequence {
    private final BracesStack braces;
    /*package local*/ ContextDetector(TokenSequence<CppTokenId> ts, DiffLinkedList diffs, BracesStack braces, int tabSize, boolean expandTabToSpaces){
        super(ts, diffs, tabSize, expandTabToSpaces);
        this.braces = braces;
    }
    
    /*package local*/ boolean isStatementContinuation(){
        Token<CppTokenId> prev = lookPreviousImportant();
        Token<CppTokenId> next = lookNextImportant();
        if (token().id() == WHITESPACE && next != null && next.id() == IDENTIFIER) {
            next = lookNextImportant(2);
        }
        if (prev == null || next == null){
            return false;
        }
        if (next.id() == IDENTIFIER) {
            if (prev.id() == IDENTIFIER) {
                return false;
            } else if (prev.id() == RPAREN) {
                prev =lookPreviousStatement();
                if (prev == null || prev.id() == IDENTIFIER){
                    return false;
                }
            }
        }
        return true;
    }

    /*package local*/ boolean isQuestionColumn(){
        int index = index();
        try {
            int depth = 0;
            while(movePrevious()) {
                if (braces.lastStatementStart >= index()){
                    return false;
                }
                switch (token().id()) {
                    case RPAREN:
                        depth++;
                        break;
                    case LPAREN:
                        depth--;
                        if (depth < 0) {
                            return false;
                        }
                        break;
                    case QUESTION:
                        if (depth == 0) {
                            return true;
                        }
                        break;
                    case LBRACE:
                    case RBRACE:
                    case SEMICOLON:
                        return false;
                }
            }
            return false;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    /*package local*/ boolean isLikeTemplate(Token<CppTokenId> current){
        int index = index();
        try {
            boolean back = current.id() == GT;
            Token<CppTokenId> head;
            if (!back){
                head = lookPreviousImportant();
                if (head == null || head.id() != IDENTIFIER){
                    return false;
                }
            }
            int depth = 0;
            while(true) {
                if (back) {
                    if (!movePrevious()) {
                        return false;
                    }
                } else {
                    if (!moveNext()){
                        return false;
                    }
                }
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    case GTGT:
                        if (back) {
                            depth += 2;
                        } else {
                            if (depth == 1) {
                                // end of template
                                return true;
                            }
                            depth -= 2;
                        }
                        break;
                    case GT:
                        if (back) {
                            depth++;
                        } else {
                            if (depth == 0) {
                                // end of template
                                return true;
                            }
                            depth--;
                        }
                        break;
                    case LT:
                        if (back) {
                            if (depth == 0) {
                                // start of template
                                head = lookPreviousImportant();
                                if (head == null || head.id() != IDENTIFIER){
                                    return false;
                                }
                                return true;
                            }
                            depth--;
                        } else {
                            depth++;
                        }
                        break;
                    case FALSE:
                    case TRUE:
                    case INT_LITERAL:
                    case LONG_LITERAL:
                    case LONG_LONG_LITERAL:
                    case FLOAT_LITERAL:
                    case DOUBLE_LITERAL:
                    case UNSIGNED_LITERAL:
                    case UNSIGNED_LONG_LITERAL:
                    case UNSIGNED_LONG_LONG_LITERAL:
                    case CHAR_LITERAL:
                    case RAW_STRING_LITERAL:
                    case STRING_LITERAL:
                        //it's a template specialization
                        break;
                    case SCOPE:
                    case STRUCT:
                    case CLASS:
                    case CONST:
                    case VOLATILE:
                    case VOID:
                    case UNSIGNED:
                    case CHAR:
                    case SHORT:
                    case INT:
                    case LONG:
                    case FLOAT:
                    case DOUBLE:
                    case AMP:
                    case STAR:
                    case COMMA:
                    case IDENTIFIER:
                        break;
                    case ELLIPSIS:
                        return true;
                    default:
                        return false;
                }
            }
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    /*package local*/ boolean isTypeCast() {
        int index = index();
        try {
            boolean findId = false;
            boolean findModifier = false;
            if (token().id() == RPAREN) {
                while (movePrevious()) {
                    switch (token().id()) {
                        case LPAREN:
                        {
                            if (findId) {
                                moveIndex(index);
                                moveNext();
                                return checknextAfterCast();
                            }
                            return false;
                        }
                        case STRUCT:
                        case CONST:
                        case VOID:
                        case UNSIGNED:
                        case CHAR:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            return true;
                        case IDENTIFIER:
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                            if (findId) {
                                return false;
                            }
                            findModifier = true;
                            break;
                        case WHITESPACE:
                        case ESCAPED_WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case DOXYGEN_LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            } else if (token().id() == LPAREN) {
                while (moveNext()) {
                    switch (token().id()) {
                        case RPAREN:
                        {
                            if (findId) {
                                return checknextAfterCast();
                            }
                            return false;
                        }
                        case CONST:
                        case STRUCT:
                        case VOID:
                        case UNSIGNED:
                        case CHAR:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            return true;
                        case IDENTIFIER:
                            if (findModifier) {
                                return false;
                            }
                            findId = true;
                            break;
                        case AMP:
                        case STAR:
                        case LBRACKET:
                        case RBRACKET:
                            findModifier = true;
                            break;
                        case WHITESPACE:
                        case ESCAPED_WHITESPACE:
                        case NEW_LINE:
                        case LINE_COMMENT:
                        case DOXYGEN_LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case PREPROCESSOR_DIRECTIVE:
                            break;
                        default:
                            return false;
                    }
                }
            }
            return false;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }

    private boolean checknextAfterCast() {
        Token<CppTokenId> next = lookNextImportant();
        if (next != null) {
            String prevCategory = next.id().primaryCategory();
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                    LITERAL_CATEGORY.equals(prevCategory) ||
                    CHAR_CATEGORY.equals(prevCategory) ||
                    STRING_CATEGORY.equals(prevCategory)) {
                return true;
            }
            switch (next.id()) {
                case IDENTIFIER:
                case TILDE:
                case LPAREN:
                    return true;
            }
        }
        return false;
    }

    /*package local*/ boolean isPrefixOperator(Token<CppTokenId> current){
        Token<CppTokenId> previous = lookPreviousImportant();
        if (previous != null) {
            String prevCategory = previous.id().primaryCategory();
            if (OPERATOR_CATEGORY.equals(prevCategory)) {
                return true;
            }
            switch(previous.id()){
                case RBRACE:
                case LBRACE:
                case LPAREN:
                case LBRACKET:
                case SEMICOLON:
                case COMMA:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    /*package local*/ boolean isPostfixOperator(Token<CppTokenId> current){
        Token<CppTokenId> next = lookNextImportant();
        if (next != null) {
            String nextCategory = next.id().primaryCategory();
            if (OPERATOR_CATEGORY.equals(nextCategory)) {
                return true;
            }
            switch(next.id()){
                case RBRACE:
                case RPAREN:
                case RBRACKET:
                case SEMICOLON:
                case COMMA:
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }
    
    /*package local*/ OperatorKind getOperatorKind(Token<CppTokenId> current){
        Token<CppTokenId> previous = lookPreviousImportant();
        Token<CppTokenId> next = lookNextImportant();
        if (previous != null && next != null) {
            String prevCategory = previous.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(prevCategory) ||
                KEYWORD_DIRECTIVE_CATEGORY.equals(prevCategory) ||
                next.id() == OPERATOR ||
                (SEPARATOR_CATEGORY.equals(prevCategory) &&
                 previous.id() != RPAREN && previous.id() != RBRACKET)){
                switch(current.id()){
                    case STAR:
                    case AMPAMP:
                    case AMP:
                        return OperatorKind.TYPE_MODIFIER;
                    case PLUS:
                    case MINUS:
                        return OperatorKind.UNARY;
                    case GT:
                    case LT:
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (OPERATOR_CATEGORY.equals(prevCategory)) {
                switch(previous.id()){
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
                        switch(current.id()){
                            case STAR:
                            case AMP:
                            case PLUS:
                            case MINUS:
                                return OperatorKind.UNARY;
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    case LT: //("<", "operator"),
                    case EQEQ: //("==", "operator"),
                    case LTEQ: //("<=", "operator"),
                    case GTEQ: //(">=", "operator"),
                    case NOTEQ: //("!=","operator"),
                    case AMPAMP: //("&&", "operator"),
                    case BARBAR: //("||", "operator"),
                    case SLASH: //("/", "operator"),
                    case BAR: //("|", "operator"),
                    case PERCENT: //("%", "operator"),
                    case LTLT: //("<<", "operator"),
                    case GTGT: //(">>", "operator"),
                        switch(current.id()){
                            case STAR:
                            case AMP:
                            case PLUS:
                            case MINUS:
                                return OperatorKind.UNARY;
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    case GT: //(">", "operator"),
                        switch(current.id()){
                            case PLUS:
                            case MINUS:
                                return OperatorKind.UNARY;
                            case GT:
                            case LT:
                                return OperatorKind.SEPARATOR;
                        }
                        break;
                }
            }
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                LITERAL_CATEGORY.equals(prevCategory) ||
                CHAR_CATEGORY.equals(prevCategory) ||
                STRING_CATEGORY.equals(prevCategory)){
                if (current.id() == GT && isLikeTemplate(current)) {
                    // get<0>()
                    //      ^
                    return OperatorKind.SEPARATOR;
                } else {
                    return OperatorKind.BINARY;
                }
            }
            String nextCategory = next.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(nextCategory)){
                switch(current.id()){
                    case STAR:
                    case AMPAMP:
                    case AMP:
                        return OperatorKind.BINARY;
                    case PLUS:
                    case MINUS:
                        return OperatorKind.BINARY;
                    case GT:
                    case LT:
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (NUMBER_CATEGORY.equals(nextCategory) ||
                LITERAL_CATEGORY.equals(nextCategory) ||
                CHAR_CATEGORY.equals(nextCategory) ||
                STRING_CATEGORY.equals(nextCategory))  {
                if (SEPARATOR_CATEGORY.equals(prevCategory)||
                    OPERATOR_CATEGORY.equals(prevCategory)) {
                     switch(current.id()){
                        case STAR:
                        case AMP:
                        case AMPAMP:
                            return OperatorKind.TYPE_MODIFIER;
                        case GT:
                        case LT:
                            return OperatorKind.BINARY;
                        case PLUS:
                        case MINUS:
                        default:
                            switch (previous.id()) {
                                case RPAREN://)")", "separator"),
                                case RBRACKET://("]", "separator"),
                                    return OperatorKind.BINARY;
                            }
                            return OperatorKind.UNARY;
                    }
                } else {
                    if (previous.id() == IDENTIFIER && current.id() == LT && isLikeTemplate(current)) {
                        // get<0>()
                        //    ^
                        return OperatorKind.SEPARATOR;
                    } else {
                        return OperatorKind.BINARY;
                    }
                }
            }
            if (previous.id() == RPAREN || previous.id() == RBRACKET){
                if (next.id() == IDENTIFIER) {
                    if (isPreviousStatementParen() || isPreviousTypeCastParen()) {
                        switch(current.id()){
                            case STAR:
                            case AMP:
                            case AMPAMP:
                                return OperatorKind.TYPE_MODIFIER;
                            case PLUS:
                            case MINUS:
                                return OperatorKind.UNARY;
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    } else {
                        return OperatorKind.BINARY;
                    }
                }
            }
            if (previous.id() == IDENTIFIER){
                if (next.id() == LPAREN){
                    // TODO need detect that previous ID is not type
                    if (braces.isDeclarationLevel()){
                        switch(current.id()){
                            case STAR:
                            case AMP:
                            case AMPAMP:
                                return OperatorKind.TYPE_MODIFIER;
                            case PLUS:
                            case MINUS:
                                return OperatorKind.BINARY;
                            case GT:
                            case LT:
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    } else {
                        switch(current.id()){
                            case GT:
                            case LT:
                                if (braces.isDeclarationLevel()) {
                                    return OperatorKind.SEPARATOR;
                                } else if (isLikeTemplate(current)) {
                                    return OperatorKind.SEPARATOR;
                                }
                            default:
                                return OperatorKind.BINARY;
                        }
                    }
                }
                if (OPERATOR_CATEGORY.equals(nextCategory) ||
                    SEPARATOR_CATEGORY.equals(nextCategory)){
                    switch(current.id()){
                        case STAR:
                        case AMP:
                        case AMPAMP:
                            return OperatorKind.TYPE_MODIFIER;
                        case PLUS:
                        case MINUS:
                            return OperatorKind.BINARY;
                        case GT:
                        case LT:
                        default:
                            return OperatorKind.SEPARATOR;
                    }
                }
                if (next.id() == IDENTIFIER) {
                    // TODO need detect that previous ID is not type
                    switch(current.id()){
                        case STAR:
                        case AMP:
                        case AMPAMP:
                            if (braces.isDeclarationLevel()) {
                                return OperatorKind.TYPE_MODIFIER;
                            } else if (isLikeForDeclaration()) {
                                return OperatorKind.TYPE_MODIFIER;
                            } else if (isLikeExpession()) {
                                return OperatorKind.BINARY;
                            }
                            return OperatorKind.SEPARATOR;
                        case PLUS:
                        case MINUS:
                            return OperatorKind.BINARY;
                        case GT:
                        case LT:
                        default:
                            if (braces.isDeclarationLevel()) {
                                return OperatorKind.SEPARATOR;
                            } else if (isLikeTemplate(current)) {
                                return OperatorKind.SEPARATOR;
                            } else if (isLikeExpession()) {
                                return OperatorKind.BINARY;
                            }
                            return OperatorKind.SEPARATOR;
                    }
                }
            }
        }
        return OperatorKind.SEPARATOR;
    }
    
    private boolean isPreviousStatementParen(){
        int index = index();
        try {
            while(movePrevious()){
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return isStatementParen();
                }
            }
            return true;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }

    private boolean isPreviousTypeCastParen(){
        int index = index();
        try {
            while(movePrevious()){
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                        break;
                    default:
                        return isTypeCast();
                }
            }
            return true;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    private boolean isLikeForDeclaration(){
        StackEntry entry = braces.peek();
        if (entry == null || entry.getKind() != FOR){
            return false;
        }
        int index = index();
        int level = 1;
        try {
            while (movePrevious()) {
                switch (token().id()) {
                    case RPAREN:
                        level++;
                        break;
                    case LPAREN:
                        level--;
                        break;
                    case FOR:
                        if (level == 0) {
                            return true;
                        }
                        return false;
                    case SEMICOLON:
                    case EQ:
                        return false;
                    default:
                        break;
                }
            }
            return false;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    private boolean isStatementParen(){
        if (token().id() == RPAREN){
            int level = 1;
            while(movePrevious()){
                switch (token().id()) {
                    case RPAREN:
                        level++;
                        break;
                    case LPAREN:
                        level--;
                        if (level == 0){
                            Token<CppTokenId> previous = lookPreviousImportant();
                            if (previous != null) {
                                switch (previous.id()) {
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                    case CATCH:
                                    case SWITCH:
                                        return true;
                                    default:
                                        while(movePrevious()){
                                            switch (token().id()) {
                                                case WHITESPACE:
                                                case ESCAPED_WHITESPACE:
                                                case NEW_LINE:
                                                case LINE_COMMENT:
                                                case DOXYGEN_LINE_COMMENT:
                                                case BLOCK_COMMENT:
                                                case DOXYGEN_COMMENT:
                                                case PREPROCESSOR_DIRECTIVE:
                                                    break;
                                                default:
                                                    return index() == braces.lastStatementStart;
                                            }
                                        }
                                        break;
                                }
                            }
                            return false;
                        }
                        break;
                }
            }
        }
        return false;
    }
    
    private boolean isLikeExpession(){
        StackEntry entry = braces.peek();
        if (entry != null) {
            if ((entry.getKind() == FOR || entry.getKind() == WHILE || entry.getKind() == IF)){
                return true;
            } else {
                if (entry.getImportantKind() != null) {
                    switch (entry.getImportantKind()) {
                        case NAMESPACE:
                        case STRUCT:
                        case CLASS:
                        case UNION:
                        case ENUM:
                            if (braces.parenDepth == 1) {
                                return false;
                            }
                    }
                }
            }
        }
        int index = index();
        try {
            while(moveNext()){
                switch (token().id()) {
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case NEW_LINE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case PREPROCESSOR_DIRECTIVE:
                    case IDENTIFIER:
                        break;
                    case COMMA:
                    case SEMICOLON:
                    case EQ:
                        return false;
                    default:
                        return true;
                }
            }
            return true;
        } finally {
            moveIndex(index);
            moveNext();
        }
    }
    
    /*package local*/ static enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR,
        TYPE_MODIFIER
    }
}
