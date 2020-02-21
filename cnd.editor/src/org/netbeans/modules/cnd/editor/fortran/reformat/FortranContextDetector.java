/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.fortran.reformat;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;

/**
 *
 */
public class FortranContextDetector extends FortranExtendedTokenSequence {
    //private FortranBracesStack braces;
    /*package local*/ FortranContextDetector(TokenSequence<FortranTokenId> ts, FortranDiffLinkedList diffs, FortranBracesStack braces, int tabSize, boolean expandTabToSpaces){
        super(ts, diffs, tabSize, expandTabToSpaces);
        //this.braces = braces;
    }
    
    /*package local*/ OperatorKind getOperatorKind(Token<FortranTokenId> current){
        Token<FortranTokenId> previous = lookPreviousImportant();
        Token<FortranTokenId> next = lookNextImportant();
        if (previous != null && next != null) {
            String prevCategory = previous.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(prevCategory) ||
                (SPECIAL_CATEGORY.equals(prevCategory) && previous.id() != RPAREN)){
                switch(current.id()){
                    case OP_MUL:
                    case OP_PLUS:
                    case OP_MINUS:
                        return OperatorKind.UNARY;
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (OPERATOR_CATEGORY.equals(prevCategory)) {
                switch(previous.id()){
                    case EQ: //("=", "operator"),
                        switch(current.id()){
                            case OP_MUL:
                            case OP_PLUS:
                            case OP_MINUS:
                                return OperatorKind.UNARY;
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                    case OP_LT: //("<", "operator"),
                    case OP_LOG_EQ: //("==", "operator"),
                    case OP_LT_EQ: //("<=", "operator"),
                    case OP_GT_EQ: //(">=", "operator"),
                    case OP_NOT_EQ: //("!=","operator"),
                    case PERCENT: //("%", "operator"),
                        switch(current.id()){
                            case OP_MUL:
                            case OP_PLUS:
                            case OP_MINUS:
                                return OperatorKind.UNARY;
                            default:
                                return OperatorKind.SEPARATOR;
                        }
                }
            }
            if (NUMBER_CATEGORY.equals(prevCategory) ||
                LITERAL_CATEGORY.equals(prevCategory) ||
                STRING_CATEGORY.equals(prevCategory)){
                return OperatorKind.BINARY;
            }
            String nextCategory = next.id().primaryCategory();
            if (KEYWORD_CATEGORY.equals(nextCategory)){
                switch(current.id()){
                    case OP_PLUS:
                    case OP_MINUS:
                        return OperatorKind.BINARY;
                    default:
                        return OperatorKind.SEPARATOR;
                }
            }
            if (NUMBER_CATEGORY.equals(nextCategory) ||
                LITERAL_CATEGORY.equals(nextCategory) ||
                STRING_CATEGORY.equals(nextCategory))  {
                if (SPECIAL_CATEGORY.equals(prevCategory)||
                    OPERATOR_CATEGORY.equals(prevCategory)) {
                     switch(current.id()){
                        case OP_MUL:
                        case OP_PLUS:
                        case OP_MINUS:
                        default:
                            switch (previous.id()) {
                                case RPAREN://)")", "separator"),
                                    return OperatorKind.BINARY;
                            }
                            return OperatorKind.UNARY;
                    }
                } else {
                    return OperatorKind.BINARY;
                }
            }
            if (previous.id() == RPAREN){
                if (next.id() == IDENTIFIER) {
                    if (isPreviousStatementParen()) {
                        switch(current.id()){
                            case OP_MUL:
                            case OP_PLUS:
                            case OP_MINUS:
                                return OperatorKind.UNARY;
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
                    return OperatorKind.BINARY;
                }
                if (OPERATOR_CATEGORY.equals(nextCategory) ||
                    SPECIAL_CATEGORY.equals(nextCategory)){
                    switch(current.id()){
                        case OP_MUL:
                        case OP_PLUS:
                        case OP_MINUS:
                            return OperatorKind.BINARY;
                        default:
                            return OperatorKind.SEPARATOR;
                    }
                }
                if (next.id() == IDENTIFIER) {
                    // TODO need detect that previous ID is not type
                    switch(current.id()){
                        case OP_MUL:
                        case OP_PLUS:
                        case OP_MINUS:
                            return OperatorKind.BINARY;
                        default:
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
                    case NEW_LINE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
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
                            Token<FortranTokenId> previous = lookPreviousImportant();
                            if (previous != null) {
                                switch (previous.id()) {
                                    case KW_FORALL:
                                    case KW_IF:
                                    case KW_WHILE:
                                    case KW_SELECT:
                                    case KW_SELECTCASE:
                                    case KW_SELECTTYPE:
                                        return true;
                                    default:
                                        while(movePrevious()){
                                            switch (token().id()) {
                                                case WHITESPACE:
                                                case NEW_LINE:
                                                case LINE_COMMENT_FIXED:
                                                case LINE_COMMENT_FREE:
                                                case PREPROCESSOR_DIRECTIVE:
                                                    break;
                                                default:
                                                    return false;
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
    
    /*package local*/ static enum OperatorKind {
        BINARY,
        UNARY,
        SEPARATOR
    }
}
