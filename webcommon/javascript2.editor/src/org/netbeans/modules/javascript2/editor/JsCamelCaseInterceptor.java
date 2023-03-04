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
package org.netbeans.modules.javascript2.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;

/**
 * Replaced old KeystrokeHandler and takes care about delete word actions.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsCamelCaseInterceptor implements CamelCaseInterceptor {

    @Override
    public boolean beforeChange(MutableContext context) throws BadLocationException {
        return false;
    }

    @Override
    public void change(final MutableContext context) throws BadLocationException {
        final Document doc = context.getDocument();
        final int offset = context.getOffset();
        final boolean reverse = context.isBackward();
        doc.render(() -> {
            int nextOffset = getWordOffset(doc, offset, reverse);
            context.setNextWordOffset(nextOffset);
        });
    }

    @Override
    public void afterChange(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(MutableContext context) {
    }

    protected static int getWordOffset(Document doc, int offset, boolean reverse) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, offset);
        if (ts == null) {
            return -1;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return -1;
        }
        if (reverse && ts.offset() == offset && !ts.movePrevious()) {
            return -1;
        }

        Token<? extends JsTokenId> token = ts.token();

        if (token.id() == JsTokenId.WHITESPACE) {
            // Eat up spaces
            if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                return ts.offset();
            }
        }

        if (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) {
            String image = token.text().toString();
            int imageLength = image.length();
            int offsetInImage = offset - ts.offset();

            if (reverse) {
                return getPreviousIdentifierWordOffset(ts, image, imageLength, offsetInImage);
            } else {
                return getNextIdentifierWordOffset(ts, image, imageLength, offsetInImage);
            }
        }

        return -1;
    }

    private static int getPreviousIdentifierWordOffset(TokenSequence<? extends JsTokenId> ts, String image, int imageLength, int offsetInImage) {
        offsetInImage = offsetInImage - 1;
        if (offsetInImage < 0) {
            return -1;
        }
        if (offsetInImage < imageLength && Character.isUpperCase(image.charAt(offsetInImage))) {
            for (int i = offsetInImage - 1; i >= 0; i--) {
                char charAtI = image.charAt(i);
                if (!Character.isUpperCase(charAtI)) {
                    // return offset of previous uppercase char in the identifier
                    return ts.offset() + i + 1;
                }
            }
            return ts.offset();
        } else {
            for (int i = offsetInImage - 1; i >= 0; i--) {
                char charAtI = image.charAt(i);
                if (Character.isUpperCase(charAtI)) {
                    // now skip over previous uppercase chars in the identifier
                    for (int j = i; j >= 0; j--) {
                        char charAtJ = image.charAt(j);
                        if (!Character.isUpperCase(charAtJ)) {
                            // return offset of previous uppercase char in the identifier
                            return ts.offset() + j + 1;
                        }
                    }
                    return ts.offset();
                }
            }
            return ts.offset();
        }
    }

    private static int getNextIdentifierWordOffset(TokenSequence<? extends JsTokenId> ts, String image, int imageLength, int offsetInImage) {
        int start = offsetInImage + 1;
        if (offsetInImage < 0 || offsetInImage >= image.length()) {
            return -1;
        }
        if (Character.isUpperCase(image.charAt(offsetInImage))) {
            // if starting from a Uppercase char, first skip over follwing upper case chars
            for (int i = start; i < imageLength; i++) {
                char charAtI = image.charAt(i);
                if (!Character.isUpperCase(charAtI)) {
                    break;
                }
                start++;
            }
        }
        for (int i = start; i < imageLength; i++) {
            char charAtI = image.charAt(i);
            if (Character.isUpperCase(charAtI)) {
                return ts.offset() + i;
            }
        }
        return ts.offset() + imageLength;
    }

    @MimeRegistration(mimeType = "text/javascript", service = CamelCaseInterceptor.Factory.class)
    public static class Factory implements CamelCaseInterceptor.Factory {

        @Override
        public CamelCaseInterceptor createCamelCaseInterceptor(MimePath mimePath) {
            return new JsCamelCaseInterceptor();
        }
    }
}
