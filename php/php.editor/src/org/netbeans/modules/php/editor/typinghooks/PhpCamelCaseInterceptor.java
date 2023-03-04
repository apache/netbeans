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
package org.netbeans.modules.php.editor.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpCamelCaseInterceptor implements CamelCaseInterceptor {
    private static final String CAMEL_CASE_NAVIGATION_PROPERTY = "useCamelCaseStyleNavigation"; //NOI18N

    private static boolean isUsingCamelCase() {
        return NbPreferences.root().getBoolean(CAMEL_CASE_NAVIGATION_PROPERTY, true); //NOI18N
    }

    @Override
    public boolean beforeChange(MutableContext context) throws BadLocationException {
        return false;
    }

    @Override
    public void change(MutableContext context) throws BadLocationException {
        if (isUsingCamelCase()) {
            BaseDocument doc = (BaseDocument) context.getDocument();
            boolean reverse = context.isBackward();
            int offset = context.getOffset();
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
            if (ts == null) {
                return;
            }
            ts.move(offset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            if (reverse && ts.offset() == offset) {
                if (!ts.movePrevious()) {
                    return;
                }
            }
            Token<? extends PHPTokenId> token = ts.token();
            TokenId id = token.id();
            if (id == PHPTokenId.WHITESPACE) {
                // Just eat up the space in the normal IDE way
                if ((reverse && ts.offset() < offset) || (!reverse && ts.offset() > offset)) {
                    context.setNextWordOffset(ts.offset());
                    return;
                }
                while (id == PHPTokenId.WHITESPACE) {
                    if (reverse && !ts.movePrevious()) {
                        return;
                    } else if (!reverse && !ts.moveNext()) {
                        return;
                    }

                    token = ts.token();
                    id = token.id();
                }
                if (reverse) {
                    int start = ts.offset() + token.length();
                    if (start < offset) {
                        context.setNextWordOffset(start);
                        return;
                    }
                } else {
                    int start = ts.offset();
                    if (start > offset) {
                        context.setNextWordOffset(start);
                        return;
                    }
                }
            }
            if (id == PHPTokenId.PHP_VARIABLE || id == PHPTokenId.PHP_STRING) {
                final CharSequence s = token.text();
                int length = s.length();
                int wordOffset = offset - ts.offset();
                if (reverse) {
                    // Find previous
                    int offsetInImage = offset - 1 - ts.offset();
                    if (offsetInImage < 0) {
                        return;
                    }
                    if (offsetInImage < length && Character.isUpperCase(s.charAt(offsetInImage))) {
                        for (int i = offsetInImage - 1; i >= 0; i--) {
                            char charAtI = s.charAt(i);
                            if (charAtI == '_') {
                                // return offset of previous uppercase char in the identifier
                                context.setNextWordOffset(ts.offset() + i + 1);
                                return;
                            } else if (!Character.isUpperCase(charAtI)) {
                                // return offset of previous uppercase char in the identifier
                                context.setNextWordOffset(ts.offset() + i + 1);
                                return;
                            }
                        }
                        context.setNextWordOffset(ts.offset());
                    } else {
                        for (int i = offsetInImage - 1; i >= 0; i--) {
                            char charAtI = s.charAt(i);
                            if (charAtI == '_') {
                                context.setNextWordOffset(ts.offset() + i + 1);
                                return;
                            }
                            if (Character.isUpperCase(charAtI)) {
                                // now skip over previous uppercase chars in the identifier
                                for (int j = i; j >= 0; j--) {
                                    char charAtJ = s.charAt(j);
                                    if (charAtJ == '_') {
                                        context.setNextWordOffset(ts.offset() + j + 1);
                                        return;
                                    }
                                    if (!Character.isUpperCase(charAtJ)) {
                                        // return offset of previous uppercase char in the identifier
                                        context.setNextWordOffset(ts.offset() + j + 1);
                                        return;
                                    }
                                }
                                context.setNextWordOffset(ts.offset());
                                return;
                            }
                        }
                        context.setNextWordOffset(ts.offset());
                    }
                } else {
                    // Find next
                    int start = wordOffset + 1;
                    if (wordOffset < 0 || wordOffset >= s.length()) {
                        // Probably the end of a token sequence, such as this:
                        // <%s|%>
                        return;
                    }
                    if (Character.isUpperCase(s.charAt(wordOffset))) {
                        // if starting from a Uppercase char, first skip over follwing upper case chars
                        for (int i = start; i < length; i++) {
                            char charAtI = s.charAt(i);
                            if (!Character.isUpperCase(charAtI)) {
                                break;
                            }
                            if (s.charAt(i) == '_') {
                                context.setNextWordOffset(ts.offset() + i);
                                return;
                            }
                            start++;
                        }
                    }
                    for (int i = start; i < length; i++) {
                        char charAtI = s.charAt(i);
                        if (charAtI == '_' || Character.isUpperCase(charAtI)) {
                            context.setNextWordOffset(ts.offset() + i);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void afterChange(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(MutableContext context) {
    }

    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = CamelCaseInterceptor.Factory.class)
    public static class Factory implements CamelCaseInterceptor.Factory {

        @Override
        public CamelCaseInterceptor createCamelCaseInterceptor(MimePath mimePath) {
            return new PhpCamelCaseInterceptor();
        }

    }

}
