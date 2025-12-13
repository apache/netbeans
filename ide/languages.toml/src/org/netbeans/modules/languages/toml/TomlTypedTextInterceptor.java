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
package org.netbeans.modules.languages.toml;

import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author lkishalmi
 */
public class TomlTypedTextInterceptor implements TypedTextInterceptor {

    private int caretPosition = -1;

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        String txt = context.getText();
        if (context.getReplacedText().length() == 0) {
            switch (txt) {
                case "{":
                    context.setText("{}", 1);
                    break;
                case "}":
                    if ("}".equals(textAfter(context, 1))) {
                        skipNext(context);
                    }
                    break;
                case "[":
                    context.setText("[]", 1);
                    break;
                case "]":
                    if ("]".equals(textAfter(context, 1))){
                        skipNext(context);
                    }
                    break;
                case " ":
                    String b = textBefore(context, 1);
                    String a = textAfter(context, 1);
                    if ( ("{".equals(b) && "}".equals(a))
                            || ("[".equals(b) && "]".equals(a))) {
                        context.setText("  ", 1);
                    }
                    break;
                case "\"":
                    if (!isInMultilineString(context)) {
                        if (!"\"\"".equals(textBefore(context, 2))) {
                            if ("\"".equals(textAfter(context, 1))){
                                skipNext(context);
                            } else {
                                int quotes = quotesInLine(context, '"');
                                if (quotes % 2 == 0) {
                                    context.setText("\"\"", 1);
                                }
                            }
                        }
                    }
                    break;
                case "'":
                    if (!isInMultilineString(context)) {
                        if (!"''".equals(textBefore(context, 2))) {
                            if ("'".equals(textAfter(context, 1))){
                                skipNext(context);
                            } else {
                                int quotes = quotesInLine(context, '\'');
                                if (quotes % 2 == 0) {
                                    context.setText("''", 1);
                                }
                            }
                        }
                    }
                    break;
            }
        } else if (("\"".equals(txt) || "'".equals(txt)) && !isInMultilineString(context)) {
            context.setText(txt + context.getReplacedText() + txt, context.getReplacedText().length() + 2);
        }
    }

    private void skipNext(MutableContext context) {
        context.setText("", 0);
        caretPosition = context.getOffset() + 1;
    }

    private static String textAfter(Context context, int length) throws BadLocationException {
        int next = Math.min(length, context.getDocument().getLength() - context.getOffset());
        return context.getDocument().getText(context.getOffset(), next);
    }

    private static String textBefore(Context context, int lenght) throws BadLocationException {
        int pre = Math.min(lenght, context.getOffset());
        return context.getDocument().getText(context.getOffset() - pre, pre);
    }

    private static final Set<? extends TokenId> STRING_OR_WS = EnumSet.of(TomlTokenId.STRING, TomlTokenId.WHITESPACE);
    
    private static boolean isInMultilineString(Context context){
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        TokenSequence<?> ts = th.tokenSequence();
        ts.move(context.getOffset());
        ts.movePrevious();
        while ((ts.token() != null) && STRING_OR_WS.contains(ts.token().id())) {
            ts.movePrevious();
        }
        return (ts.token() != null) 
                && (TomlTokenId.STRING_QUOTE == ts.token().id()) 
                && ts.token().text().length() == 3;
    }

    private static int quotesInLine(Context context, char quote) throws BadLocationException {
        LineDocument doc = (LineDocument) context.getDocument();
        int lineStart = LineDocumentUtils.getLineStart(doc, context.getOffset());
        int lineEnd = LineDocumentUtils.getLineEndOffset(doc, context.getOffset());
        char[] line = doc.getText(lineStart, lineEnd - lineStart).toCharArray();

        int quotes = 0;
        for (int i = 0; i < line.length; i++) {
            char d = line[i];
            if ('\\' == d) {
                i++;
                continue;
            }
            if (quote == d) quotes++;
        }
        return quotes;
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        if (caretPosition > -1) {
            context.getComponent().setCaretPosition(caretPosition);
            caretPosition = -1;
        }
    }

    @Override
    public void cancelled(Context context) {
    }

    @MimeRegistration(mimeType = TomlTokenId.TOML_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class TomlTypedTextInterceptorFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new TomlTypedTextInterceptor();
        }

    }
}
