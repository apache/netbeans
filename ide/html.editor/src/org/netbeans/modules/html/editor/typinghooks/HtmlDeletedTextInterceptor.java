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
package org.netbeans.modules.html.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.DeletedTextInterceptor;

/**
 *
 * @author marek
 */
public class HtmlDeletedTextInterceptor implements DeletedTextInterceptor {

    @Override
    public boolean beforeRemove(Context context) throws BadLocationException {
        //no-op
        return false;
    }

    //called *after* the change
    @Override
    public void remove(Context context) throws BadLocationException {
        Document doc = context.getDocument();
        int dotPos = context.getOffset();
        char ch = context.getText().charAt(0);
        if (ch == '\'' || ch == '"') { //NOI18N
            TokenSequence<HTMLTokenId> ts = LexUtilities.getTokenSequence((BaseDocument) doc, dotPos, HTMLTokenId.language());
            if (ts != null) {
                int diff = ts.move(dotPos);
                if (diff != 1) {
                    //we only handle situation where leading quote is removed from an attribute value -- the diff must be 1
                    return;
                }
                if (!ts.moveNext()) {
                    return;
                }
                Token<HTMLTokenId> token = ts.token();
                if (token.id() == HTMLTokenId.VALUE || token.id() == HTMLTokenId.VALUE_CSS || token.id() == HTMLTokenId.VALUE_JAVASCRIPT) {
                    char first = token.text().charAt(0);
                    if (first == ch) {
                        //user pressed backspace in empty value: <div class="|" + BACKSPACE
                        //now the text is: <div class=|"
                        //expected result: remove the second quote
                        doc.remove(dotPos - 1, 1);
                    }
                }
            }

        }
    }

    @Override
    public void afterRemove(Context context) throws BadLocationException {
        //no-op
    }

    @Override
    public void cancelled(Context context) {
        //no-op
    }

    @MimeRegistration(mimeType = "text/html", service = DeletedTextInterceptor.Factory.class)  //NOI18N
    public static final class Factory implements DeletedTextInterceptor.Factory {

        @Override
        public DeletedTextInterceptor createDeletedTextInterceptor(MimePath mimePath) {
            return new HtmlDeletedTextInterceptor();
        }

    }

}
