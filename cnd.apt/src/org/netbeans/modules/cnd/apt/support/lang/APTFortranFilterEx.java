/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.cnd.apt.support.lang;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter.FilterTextToken;
import org.netbeans.modules.cnd.apt.support.lang.APTBaseLanguageFilter.FilterToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 */
final class APTFortranFilterEx implements APTLanguageFilter {
    /**
     * Creates a new instance of APTBaseLanguageFilter
     */
    private final boolean filterContinueChar;
    
    public APTFortranFilterEx(String flavor) {
        filterContinueChar = APTLanguageSupport.FLAVOR_FORTRAN_FREE.equalsIgnoreCase(flavor);
    }

    @Override
    public TokenStream getFilteredStream(TokenStream origStream) {
        return new FilterStream(new ConcatinatingFilterStream(origStream));
    }

    private final class FilterStream implements TokenStream {
        private final TokenStream orig;
        private Token prevToken = null;
        private Token nextToken = null;
        private Token nextNextToken = null;
        //boolean newLine = false;

        public FilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            if (nextNextToken != null) {
                Token ret = nextToken;
                nextToken = nextNextToken;
                nextNextToken = null;
                return ret;
            }
            if (nextToken != null) {
                Token ret = nextToken;
                nextToken = null;
                return ret;
            }
            Token newToken = orig.nextToken();
            int column = newToken.getColumn();
            char first = newToken.getText().length() > 0 ? newToken.getText().charAt(0) : 0;
            if (column == 1 || first == '!') {
                boolean isComment = false;
                if (filterContinueChar) {
                    // free format
                    if (first == '!') {
                        isComment = true;
                    }
                } else {
                    if (column == 1 && (first == 'c' || first == 'C')) {
                        isComment = true;
                    }
                }
                if (isComment) {
                    while(true) {
                        Token aToken = orig.nextToken();
                        if (aToken == null) {
                            break;
                        }
                        if (aToken.getType() == APTTokenTypes.EOF) {
                            nextToken = aToken;
                            break;
                        }
                        if (aToken.getType() == APTTokenTypes.T_EOS) {
                            if (column > 1 && (prevToken == null || prevToken.getType() != APTTokenTypes.T_EOS)) {
                                nextToken = aToken;
                            }
                            break;
                        }
                    }
                    return new FilterToken((APTToken)newToken, APTTokenTypes.COMMENT);
                }
            }
            prevToken = newToken;

            if (newToken.getType() == APTTokenTypes.T_DIGIT_STRING) {
                String originalText = newToken.getText();
                String text = originalText.toLowerCase();
                if (text.indexOf('.') >= 0) {
                    if (text.indexOf(".and.")>0) { // NOI18N
                        int shift = text.indexOf(".and."); // NOI18N
                        final FilterToken res = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_REAL_CONSTANT, originalText.substring(0,shift), 0);
                        nextToken = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_AND, originalText.substring(shift, shift+5), shift);
                        String rest = originalText.substring(shift+5);
                        if (!rest.isEmpty()) {
                            nextNextToken = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_IDENT, rest, shift+5);
                        }
                        return res;
                    }
                    return new FilterToken((APTToken)newToken, APTTokenTypes.T_REAL_CONSTANT);
                }
            }
            if (newToken.getType() == APTTokenTypes.T_ASTERISK) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.T_ASTERISK) {
                    nextToken = null;
                    return new FilterToken((APTToken)newToken, APTTokenTypes.T_POWER);
                }
            }
            if (newToken.getType() == APTTokenTypes.DIVIDEEQUAL) {
                return new FilterToken((APTToken)newToken, APTTokenTypes.T_NE);
            }
            if (newToken.getType() == APTTokenTypes.T_EQV) {
                return new FilterToken((APTToken)newToken, APTTokenTypes.T_EQ);
            }
            if (newToken.getType() == APTTokenTypes.AND) {
                return new FilterToken((APTToken)newToken, APTTokenTypes.T_AND);
            }
            if (newToken.getType() == APTTokenTypes.OR) {
                return new FilterToken((APTToken)newToken, APTTokenTypes.T_OR);
            }
            if (newToken.getType() == APTTokenTypes.BITWISEXOR) {
                return new FilterToken((APTToken)newToken, APTTokenTypes.T_NOT);
            }
            if (newToken.getType() == APTTokenTypes.TILDE) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.TILDE) {
                    final FilterToken res = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_SLASH, "/", 0); // NOI18N
                    nextToken = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_SLASH, "/", 1); // NOI18N
                    return res;
                }
                return newToken;
            }
            if (newToken.getType() == APTTokenTypes.LESSTHAN) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.GREATERTHAN) {
                    final FilterToken res = new FilterTextToken((APTToken)newToken, APTTokenTypes.T_NE, "!=", 0); // NOI18N
                    nextToken = null;
                    return res;
                }
                return newToken;
            }
            if (newToken.getType() == APTTokenTypes.T_REAL_CONSTANT) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.T_IDENT) {
                    nextToken = null;
                    return new FilterToken((APTToken)newToken, APTTokenTypes.T_REAL_CONSTANT);
                }
                if (nextToken.getType() == APTTokenTypes.DOT) {
                    if (newToken.getText().endsWith(".and") || // NOI18N
                            newToken.getText().endsWith(".AND")) { // NOI18N
                        nextToken = new FilterToken((APTToken)nextToken, APTTokenTypes.T_AND);
                    }
                }
                if (nextToken.getType() == APTTokenTypes.DOT) {
                    if (newToken.getText().endsWith(".eq") || // NOI18N
                            newToken.getText().endsWith(".EQ")) { // NOI18N
                        nextToken = new FilterToken((APTToken)nextToken, APTTokenTypes.T_EQ);
                    }
                }
                if (nextToken.getType() == APTTokenTypes.DOT) {
                    if (newToken.getText().endsWith(".ne") || // NOI18N
                            newToken.getText().endsWith(".NE")) { // NOI18N
                        nextToken = new FilterToken((APTToken)nextToken, APTTokenTypes.T_NE);
                    }
                }
            }
            if (newToken.getType() == APTTokenTypes.DOT) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.AND) {
                    nextNextToken = orig.nextToken();
                    if (nextNextToken.getType() == APTTokenTypes.DOT) {
                        nextToken = null;
                        nextNextToken = null;
                        return new FilterToken((APTToken)newToken, APTTokenTypes.T_AND);
                    }
                } else if (nextToken.getType() == APTTokenTypes.T_IDENT) {
                    if (nextToken.getText().equalsIgnoreCase("ne")) { // NOI18N
                        nextNextToken = orig.nextToken();
                        if (nextNextToken.getType() == APTTokenTypes.DOT) {
                            nextToken = null;
                            nextNextToken = null;
                            return new FilterToken((APTToken)newToken, APTTokenTypes.T_NE);
                        }
                    }
                    if (nextToken.getText().equalsIgnoreCase("gt")) { // NOI18N
                        nextNextToken = orig.nextToken();
                        if (nextNextToken.getType() == APTTokenTypes.DOT) {
                            nextToken = null;
                            nextNextToken = null;
                            return new FilterToken((APTToken)newToken, APTTokenTypes.T_GREATERTHAN);
                        }
                    }
                    if (nextToken.getText().equalsIgnoreCase("eq")) { // NOI18N
                        nextNextToken = orig.nextToken();
                        if (nextNextToken.getType() == APTTokenTypes.DOT) {
                            nextToken = null;
                            nextNextToken = null;
                            return new FilterToken((APTToken)newToken, APTTokenTypes.T_EQ);
                        }
                    }
                    if (nextToken.getText().equalsIgnoreCase("and")) { // NOI18N
                        nextNextToken = orig.nextToken();
                        if (nextNextToken.getType() == APTTokenTypes.DOT) {
                            nextToken = null;
                            nextNextToken = null;
                            return new FilterToken((APTToken)newToken, APTTokenTypes.T_AND);
                        }
                    }
                }
            }
            
            if (newToken.getType() == APTTokenTypes.T_END) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.T_IF) {
                    nextToken = null;
                    return new FilterToken((APTToken)newToken, APTTokenTypes.T_ENDIF);
                }
            }
            if (newToken.getType() == APTTokenTypes.NOT) {
                nextToken = orig.nextToken();
                while(nextToken != null 
                        && nextToken.getType() != APTTokenTypes.T_EOS
                        && nextToken.getType() != APTTokenTypes.T_EOF
                        && nextToken.getType() != APTTokenTypes.EOF) {
                    nextToken = orig.nextToken();
                }
                return new FilterToken((APTToken)newToken, APTTokenTypes.FORTRAN_COMMENT);
            }
            if(filterContinueChar && newToken.getType() == APTTokenTypes.AMPERSAND) {
                nextToken = orig.nextToken();
                if (nextToken.getType() == APTTokenTypes.T_EOS) {
                    nextToken = null;
                    return new FilterToken((APTToken)newToken, APTTokenTypes.CONTINUE_CHAR);
                }
            }
            return newToken;
        }
    }
    
    // Concatinates two adjacent tokens if necessary (to emulate old lexer for fortran)
    private static class ConcatinatingFilterStream implements TokenStream {
        private final TokenStream orig;
        private Token nextToken = null;
        private Token nextNextToken = null;

        public ConcatinatingFilterStream(TokenStream orig) {
            this.orig = orig;
        }

        @Override
        public Token nextToken() throws TokenStreamException {
            if (nextToken == null) {
                nextToken = orig.nextToken();
            }
            if (nextNextToken == null) {
                nextNextToken = orig.nextToken();
            }
            Token retToken = concat(nextToken, nextNextToken);
            if (retToken != null) {
                nextNextToken = null;
                nextToken = null;
                return retToken;
            } else {
                retToken = nextToken;
                nextToken = nextNextToken;
                nextNextToken = null;
                return retToken;
            }
        }
        
        private Token concat(Token first, Token second) {
            switch (first.getType()) {
                case APTTokenTypes.T_REAL_CONSTANT:
                case APTTokenTypes.T_DIGIT_STRING:
                    if (second.getType() == APTTokenTypes.T_IDENT) {
                        if (APTUtils.areAdjacent((APTToken) first, (APTToken) second)) {
                            return new FilterTextToken(
                                    (APTToken) first, 
                                    first.getType(), 
                                    first.getText() + second.getText(), 
                                    0
                            );
                        }
                    }
                    break;
            }
            return null;
        }
    }
}
