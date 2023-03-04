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

package org.netbeans.modules.groovy.editor.typinghooks;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author Martin Janicek
 */
public class GroovyDeletedTextInterceptor implements DeletedTextInterceptor {

    @MimeRegistration(mimeType = GroovyTokenId.GROOVY_MIME_TYPE, service = DeletedTextInterceptor.Factory.class)
    public static class GroovyDeletedTextInterceptorFactory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new GroovyDeletedTextInterceptor();
        }
    }

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void remove(Context context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        final char ch = context.getText().charAt(0);
        final int dotPos = context.getOffset() - 1;

        switch (ch) {

            case '{':
            case '(':
            case '[': { // and '{' via fallthrough
                char tokenAtDot = LexUtilities.getTokenChar(doc, dotPos);

                if (((tokenAtDot == ']')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET, dotPos) != 0))
                        || ((tokenAtDot == ')')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LPAREN, GroovyTokenId.RPAREN, dotPos) != 0))
                        || ((tokenAtDot == '}')
                        && (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE, dotPos) != 0))) {
                    doc.remove(dotPos, 1);
                }
                break;
            }

            case '|':
            case '\"':
            case '\'':
                char[] match = doc.getChars(dotPos, 1);

                if ((match != null) && (match[0] == ch)) {
                    doc.remove(dotPos, 1);
                }
        }
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }
}
