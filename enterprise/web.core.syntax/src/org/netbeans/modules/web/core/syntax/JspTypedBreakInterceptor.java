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
package org.netbeans.modules.web.core.syntax;

import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.web.indent.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JspTypedBreakInterceptor implements TypedBreakInterceptor {

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        // TODO: below whitespace skipping does not work because whitespace
        // tokens between JSP tokens are actually HTML tokens and not JSP tokens.
        // Proper way is to iterate over document characters and skip all whitespaces
        // till you get to a text and then get token for the text.
        BaseDocument doc = (BaseDocument) context.getDocument();
        int caretOffset = context.getCaretOffset();
        TokenSequence<JspTokenId> ts = LexUtilities.getTokenSequence(doc, caretOffset, JspTokenId.language());
        if (ts == null) {
            return;
        }
        ts.move(caretOffset);
        String closingTagName = null;
        int end = -1;
        if (ts.moveNext() && ts.token().id() == JspTokenId.SYMBOL
                && ts.token().text().toString().equals("</")) {
            if (ts.moveNext() && ts.token().id() == JspTokenId.ENDTAG) {
                closingTagName = ts.token().text().toString();
                end = ts.offset() + ts.token().text().length();
                ts.movePrevious();
                ts.movePrevious();
            }
        }
        if (closingTagName == null) {
            return;
        }
        boolean foundOpening = false;
        if (ts.token().id() == JspTokenId.SYMBOL
                && ts.token().text().toString().equals(">")) {
            while (ts.movePrevious()) {
                if (ts.token().id() == JspTokenId.TAG) {
                    if (ts.token().text().toString().equals(closingTagName)) {
                        foundOpening = true;
                    }
                    break;
                }
            }
        }
        if (foundOpening) {
            int indent = GsfUtilities.getLineIndent(doc, caretOffset);
            StringBuilder text = new StringBuilder();
            text.append("\n");
            text.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
            int caret = text.length();
            text.append("\n");
            text.append(IndentUtils.createIndentString(doc, indent));
            context.setText(text.toString(), caret, caret);
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = JspKit.JSP_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
        @MimeRegistration(mimeType = JspKit.TAG_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    })
    public static class JspFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new JspTypedBreakInterceptor();
        }

    }
}
