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
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.Utilities;

/**
 *
 * based on idea of org.netbeans.modules.editor.java.CamelCaseOperations
 */
/* package */ class CamelCaseOperations {

    static int nextCamelCasePosition(final JTextComponent textComponent, final boolean skipEOL) throws BadLocationException {

        // get current caret position
        final int offset = textComponent.getCaretPosition();

        final Document doc = textComponent.getDocument();
        final Object[] out = new Object[] {offset, null};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    out[0] = nextCamelCasePositionImpl(doc, offset, skipEOL, textComponent);
                } catch (BadLocationException ex) {
                    out[1] = ex;
                }
            }
        };
        doc.render(r);
        if (out[1] != null) {
            throw (BadLocationException)out[1];
        }
        return (Integer)out[0];
    }

    private static int nextCamelCasePositionImpl(Document doc, int offset, boolean skipEOL, JTextComponent textComponent) throws BadLocationException {
        // get token chain at the offset
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset, true, false);
        if (ts != null) {
            Token<TokenId> token = ts.token();
            // is this an identifier or include strings,
            // btw we are sure this is not the end of token
            // due to false as last param of getCppTokenSequence call
            String category = token.id().primaryCategory();
            if (CppTokenId.IDENTIFIER_CATEGORY.equals(category)
                    || CppTokenId.PREPROCESSOR_USER_INCLUDE_CATEGORY.equals(category)
                    || CppTokenId.PREPROCESSOR_SYS_INCLUDE_CATEGORY.equals(category)) {
                CharSequence image = token.text();
                if (image != null && image.length() > 0) {
                    int length = image.length();
                    int offsetInImage = offset - ts.offset();
                    int start = offsetInImage + 1;
                    char charAt = image.charAt(offsetInImage);
                    if (charAt == '_') {
                        // if starting from a _ char, first skip over follwing _ chars
                        for (int i = start; i < length; i++) {
                            char charAtI = image.charAt(i);
                            if (charAtI != '_') {
                                break;
                            }
                            start++;
                        }
                    } else if (Character.isUpperCase(charAt)) {
                        // if starting from a Uppercase char, first skip over follwing upper case chars
                        for (int i = start; i < length; i++) {
                            char charAtI = image.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                break;
                            }
                            start++;
                        }
                    }
                    for (int i = start; i < length; i++) {
                        char charAtI = image.charAt(i);
                        if (Character.isUpperCase(charAtI) || charAtI == '_') {
                            // return offset of next uppercase char in the identifier
                            return ts.offset() + i;
                        }
                    }
                    return ts.offset() + length;
                }
            } else if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) { // NOI18N
                int endWS;
                // skip all whitespaces
                do {
                    endWS = ts.offset() + ts.token().length();
                    if (!skipEOL && ts.token().id() == CppTokenId.NEW_LINE) {
                        break;
                    }
                } while (ts.moveNext() && CppTokenId.WHITESPACE_CATEGORY.equals(ts.token().id().primaryCategory()));
                return endWS;
            }
        }

        // not recognized situation - simply return next word offset
        return Utilities.getNextWord(textComponent, offset);
    }
    
    static int previousCamelCasePosition(final JTextComponent textComponent) throws BadLocationException {
        // get current caret position
        final int offset = textComponent.getCaretPosition();

        // Are we at the beginning of the document
        if (offset == 0) {
            return 0;
        }
        final Document doc = textComponent.getDocument();
        final Object[] out = new Object[]{offset, null};
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    out[0] = previousCamelCasePositionImpl(textComponent, offset);
                } catch (BadLocationException ex) {
                    out[1] = ex;
                }
            }
        };
        doc.render(r);
        if (out[1] != null) {
            throw (BadLocationException)out[1];
        }
        return (Integer)out[0];
    }

    private static int previousCamelCasePositionImpl(JTextComponent textComponent, int offset) throws BadLocationException {
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(textComponent, offset, true, true);
        if (ts != null) {
            Token<TokenId> token = ts.token();
            // is this an identifier of include strings
            String category = token.id().primaryCategory();
            if (CppTokenId.IDENTIFIER_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_USER_INCLUDE_CATEGORY.equals(category)
                || CppTokenId.PREPROCESSOR_SYS_INCLUDE_CATEGORY.equals(category)) {
                CharSequence image = token.text();
                if (image != null && image.length() > 0) {
                    int offsetInImage = offset - 1 - ts.offset();
                    // go back to the first upper case character
                    int upperCaseOffset = offsetInImage - 1;
                    for (; upperCaseOffset > 0; upperCaseOffset--) {
                        char charAt = image.charAt(upperCaseOffset);
                        if (Character.isUpperCase(charAt) || charAt == '_') {
                            break;
                        }
                    }
                    if (upperCaseOffset > 0) {
                        char charAt = image.charAt(upperCaseOffset);
                        if (charAt == '_') {
                            // skip over previous _ chars in the identifier
                            for (int i = upperCaseOffset - 1; i >= 0; i--) {
                                char charAtI = image.charAt(i);
                                if (charAtI != '_') {
                                    // return offset of previous _ char in the identifier
                                    return ts.offset() + i + 1;
                                }
                            }
                        } else if(Character.isUpperCase(charAt)) {
                            // skip over previous uppercase chars in the identifier
                            for (int i = upperCaseOffset - 1; i >= 0; i--) {
                                char charAtI = image.charAt(i);
                                if (!Character.isUpperCase(charAtI)) {
                                    // return offset of previous uppercase char in the identifier
                                    return ts.offset() + i + 1;
                                }
                            }
                        }
                    }
                    return ts.offset();
                }
            } else if (CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) { // NOI18N
                int wsOffset = ts.offset();
                while (ts.movePrevious() && CppTokenId.WHITESPACE_CATEGORY.equals(ts.token().id().primaryCategory())) {
                    wsOffset = ts.offset();
                    if (wsOffset == 0) {
                        //at the very beginning of a file
                        return 0;
                    }
                }
                return wsOffset;
            }
        }

        // not recognized situation - simply return previous word offset
        return Utilities.getPreviousWord(textComponent, offset);
    }
}
