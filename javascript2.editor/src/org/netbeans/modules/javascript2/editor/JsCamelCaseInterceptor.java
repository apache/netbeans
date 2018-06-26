/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        doc.render(new Runnable() {
            @Override
            public void run() {
                int nextOffset = getWordOffset(doc, offset, reverse);
                context.setNextWordOffset(nextOffset);
            }
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

        if (token.id() == JsTokenId.IDENTIFIER) {
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
