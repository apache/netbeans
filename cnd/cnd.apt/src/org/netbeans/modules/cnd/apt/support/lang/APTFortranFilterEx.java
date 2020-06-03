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
