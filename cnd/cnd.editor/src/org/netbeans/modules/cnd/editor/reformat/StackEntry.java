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
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;

/**
 *
 */
class StackEntry {
    public enum LikeTo {
        unknown,
        function,
        arrayInitialization,
        unifiedInitialization
    }
    
    private int index;
    private CppTokenId kind;
    private CppTokenId importantKind;
    //private boolean likeToFunction = false;
    //private boolean likeToArrayInitialization = false;
    private LikeTo likeTo = LikeTo.unknown;
    private String text;
    private int indent;
    private int selfIndent;
    private int lambdaIndent = 0;
    private int lambdaParen = 0;

    StackEntry(ExtendedTokenSequence ts) {
        super();
        index = ts.index();
        kind = ts.token().id();
        text = ts.token().text().toString();
        switch (kind) {
            case IF: //("if", "keyword-directive"),
            case ELSE: //("else", "keyword-directive"),
            case TRY: //("try", "keyword-directive"), // C++
            case CATCH: //("catch", "keyword-directive"), //C++
            case WHILE: //("while", "keyword-directive"),
            case FOR: //("for", "keyword-directive"),
            case DO: //("do", "keyword-directive"),
            case ASM: //("asm", "keyword-directive"), // gcc and C++
            case SWITCH: //("switch", "keyword-directive"),
                importantKind = kind;
                break;
            default:
                initImportant(ts);
        }
    }

    private void initImportant(ExtendedTokenSequence ts) {
        int i = ts.index();
        try {
            int bracket = 0;
            int paren = 0;
            int brace = 0;
            int triangle = 0;
            boolean hasID = false;
            Token<CppTokenId> id = ts.lookPreviousImportant();
            boolean prevID = id != null && id.id() == IDENTIFIER;
            while (true) {
                if (!ts.movePrevious()) {
                    return;
                }
                Token<CppTokenId> current = ts.token();
                switch (current.id()) {
                    case TEMPLATE:
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            likeTo = LikeTo.function;
                        }
                        break;
                    }
                    case IDENTIFIER:
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            hasID = true;
                        }
                        break;
                    }
                    case RPAREN: //(")", "separator"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            likeTo = LikeTo.function;
                            Token<CppTokenId> next = ts.lookNextImportant();
                            if (next != null) {
                                if (next.id() == COLON && prevID) {
                                    likeTo = LikeTo.arrayInitialization;
                                    return;
                                }
                            }
                        }
                        paren++;
                        break;
                    }
                    case LPAREN: //("(", "separator"),
                    {
                        if (paren == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null) {
                                if (prev.id() == OPERATOR) {
                                    likeTo = LikeTo.function;
                                    return;
                                }
                            }
                            likeTo = LikeTo.arrayInitialization;
                            return;
                        }
                        paren--;
                        break;
                    }
                        
                    case LBRACKET: //[
                    {
                        bracket--;
                        if (paren == 0 && triangle == 0 && brace == 0 && bracket == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null) {
                                if (prev.id() == OPERATOR) {
                                    likeTo = LikeTo.function;
                                    return;
                                }
                                if (prev.id() == IDENTIFIER) {
                                    likeTo = LikeTo.arrayInitialization;
                                    return;
                                }
                                if (prev.id() == IDENTIFIER || prev.id() == RBRACKET || prev.id() == LBRACKET) {
                                    break;
                                }
                            }
                            likeTo = LikeTo.unknown;
                            importantKind = ARROW;
                            lambdaIndent = lambdaIndent(ts);
                            return;
                        }
                        break;
                    }
                    case RBRACKET: //]
                    {
                        bracket++;
                        break;
                    }
                        
                    case CASE:
                    case DEFAULT:
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            likeTo = LikeTo.unknown;
                            return;
                        }
                        break;
                    }
                    case RBRACE: //("}", "separator"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            if (hasID && likeTo != LikeTo.function) {
                                likeTo = LikeTo.unifiedInitialization;
                            }
                            // undefined
                            return;
                        }
                        brace++;
                        break;
                    }
                    case RETURN:
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            likeTo = LikeTo.unifiedInitialization;
                            return;
                        }
                        break;
                    }
                    case LBRACE: //("{", "separator"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            if (hasID && likeTo != LikeTo.function) {
                                likeTo = LikeTo.unifiedInitialization;
                            }
                            // undefined
                            return;
                        }
                        brace--;
                        break;
                    }
                    case SEMICOLON: //(";", "separator"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            if (hasID && likeTo != LikeTo.function) {
                                likeTo = LikeTo.unifiedInitialization;
                            }
                            // undefined
                            return;
                        }
                        break;
                    }
                    case EQ: //("=", "operator"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeTo = LikeTo.function;
                                return;
                            }
                            likeTo = LikeTo.arrayInitialization;
                            return;
                        }
                        break;
                    }
                    case NEW:
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            likeTo = LikeTo.arrayInitialization;
                            return;
                        }
                        break;
                    }
                    case GT: //(">", "operator"),
                    {
                        if (paren == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeTo = LikeTo.function;
                                return;
                            }
                            triangle++;
                        }
                        break;
                    }
                    case LT: //("<", "operator"),
                    {
                        if (paren == 0) {
                            if (triangle == 0) {
                                Token<CppTokenId> prev = ts.lookPreviousImportant();
                                if (prev != null && prev.id() == OPERATOR) {
                                    likeTo = LikeTo.function;
                                    return;
                                }
                                // undefined
                                return;
                            }
                            triangle--;
                        }
                        break;
                    }
                    case NAMESPACE: //("namespace", "keyword"), //C++
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            importantKind = current.id();
                            likeTo = LikeTo.unknown;
                            return;
                        }
                        break;
                    }
                    case CLASS: //("class", "keyword"), //C++
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            Token<CppTokenId> isEnum = ts.lookPreviousImportant();
                            if (isEnum != null && isEnum.id() == ENUM) {
                                importantKind = isEnum.id();
                            } else {
                                importantKind = current.id();
                            }
                            likeTo = LikeTo.unknown;
                            return;
                        }
                        break;
                    }
                    case STRUCT: //("struct", "keyword"),
                    case ENUM: //("enum", "keyword"),
                    case UNION: //("union", "keyword"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            if (likeTo != LikeTo.function) {
                                likeTo = LikeTo.unknown;
                                importantKind = current.id();
                                return;
                            }
                        }
                        break;
                    }
                    case EXTERN: //EXTERN("extern", "keyword"),
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            if (likeTo != LikeTo.function) {
                                likeTo = LikeTo.unknown;
                                importantKind = CppTokenId.NAMESPACE;
                                return;
                            }
                        }
                        break;
                    }
                    case IF: //("if", "keyword-directive"),
                    case ELSE: //("else", "keyword-directive"),
                    case SWITCH: //("switch", "keyword-directive"),
                    case WHILE: //("while", "keyword-directive"),
                    case DO: //("do", "keyword-directive"),
                    case FOR: //("for", "keyword-directive"),
                    case TRY: //("try", "keyword-directive"), // C++
                    case CATCH: //("catch", "keyword-directive"), //C++
                    {
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            importantKind = current.id();
                            likeTo = LikeTo.unknown;
                            return;
                        }
                        break;
                    }
                    case ARROW: // ->
                    { 
                        if (paren == 0 && triangle == 0 && brace == 0) {
                            Token<CppTokenId> prev = ts.lookPreviousImportant();
                            if (prev != null && prev.id() == OPERATOR) {
                                likeTo = LikeTo.function;
                                return;
                            }
                            importantKind = current.id();
                            likeTo = LikeTo.unknown;
                            lambdaIndent = lambdaIndent(ts);
                            return;
                        }
                        break;
                    }
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }

    private int lambdaIndent(ExtendedTokenSequence ts) {
        int i = ts.index();
        try {
            while(true) {
                if (!ts.movePrevious()){
                    return 0;
                }
                if (ts.token().id() == NEW_LINE){
                    while(true) {
                        if (!ts.moveNext()) {
                            return 0;
                        }
                        switch(ts.token().id()) {
                            case WHITESPACE:
                                break;
                            default:
                                int d = ts.getTokenPosition();
                                return d;
                        }
                    }
                }
            }
        } finally {
            ts.moveIndex(i);
            ts.moveNext();
        }
    }

    public int getLambdaIndent(){
        return lambdaIndent;
    }

    public int getLambdaParen(){
        return lambdaParen;
    }

    public void setLambdaParen(int lambdaParen){
        this.lambdaParen = lambdaParen;
    }

    public int getIndent(){
        return indent;
    }

    public void setIndent(int indent){
        this.indent = indent;
    }

    public int getSelfIndent(){
        return selfIndent;
    }

    public void setSelfIndent(int selfIndent){
        this.selfIndent = selfIndent;
    }
    
    public int getIndex() {
        return index;
    }
    
    public String getText() {
        return text;
    }

    public CppTokenId getKind() {
        return kind;
    }

    public CppTokenId getImportantKind() {
        return importantKind;
    }

    public boolean isLikeToFunction() {
        return likeTo == LikeTo.function;
    }

    public void setLikeToFunction() {
        likeTo = LikeTo.function;
    }

    public boolean isLikeToArrayInitialization() {
        return likeTo == LikeTo.arrayInitialization || likeTo == LikeTo.unifiedInitialization;
    }

    public boolean isUniformInitialization() {
        return likeTo == LikeTo.unifiedInitialization;
    }

    public void setLikeToArrayInitialization() {
        likeTo = LikeTo.arrayInitialization;
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder(kind.name());
        if (importantKind != null && kind != importantKind){
            buf.append("(").append(importantKind.name()).append(")"); // NOI18N
        } else if (likeTo == LikeTo.function) {
            buf.append("(FUNCTION)"); // NOI18N
        } else if (likeTo == LikeTo.arrayInitialization) {
            buf.append("(ARRAY_INITIALIZATION)"); // NOI18N
        } else if (likeTo == LikeTo.unifiedInitialization) {
            buf.append("(UNIFORM_INITIALIZATION)"); // NOI18N
        }
        return buf.toString();
    }
}
