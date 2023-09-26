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
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

/**
 *
 * @author Laszlo Kishalmi
 */
@MimeRegistration(mimeType = TomlTokenId.TOML_MIME_TYPE, service = CompletionProvider.class)
public class TomlCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new TomlCompletionQuery(), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private class TomlCompletionQuery extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
            Set<String> candidates = new HashSet<>();
            try {
                AbstractDocument doc = (AbstractDocument) document;
                doc.readLock();
                StringBuilder toml;
                String simplePrefix, prefix;
                try {
                    int prefixOfs = keyPrefixOffset(doc, caretOffset);
                    prefix = doc.getText(prefixOfs, caretOffset - prefixOfs);
                    int lastDot = prefix.lastIndexOf('.');
                    simplePrefix = lastDot != -1 ? prefix.substring(lastDot + 1) : prefix;

                    toml = new StringBuilder(doc.getLength());
                    //Remove the current prefix for the parser to get better results
                    toml.append(doc.getText(0, prefixOfs));
                    toml.append(doc.getText(caretOffset, doc.getLength() - caretOffset));
                } finally {
                    doc.readUnlock();
                }
                TomlParseResult parse = Toml.parse(toml.toString());
                for (String key : parse.dottedKeySet()) {
                    String candidate = matchKey(key, prefix);
                    if (candidate != null) {
                        candidates.add(candidate);
                    }
                }
                for (String candidate : candidates) {
                    String insert = candidate.substring(simplePrefix.length());
                    resultSet.addItem(CompletionUtilities.newCompletionItemBuilder(insert)
                            .leftHtmlText(candidate)
                            .sortText(candidate)
                            .build());
                }
            } catch (BadLocationException ex) {
            } finally {
                resultSet.finish();
            }
        }

    }


    static String matchKey(String key, String prefix) {
        String ret = null;
        int keyStart = key.indexOf(prefix);
        if (keyStart == 0 || ((keyStart > 0) && (key.charAt(keyStart - 1) == '.'))) {
            int lastDot = prefix.lastIndexOf('.');
            String m = key.substring(keyStart + lastDot + 1);
            int dot = m.indexOf('.');
            ret = (dot > -1) ? m.substring(0, dot) : m;
        }
        return ret;
    }

    private static final Set<TomlTokenId> DOT_OR_KEY = EnumSet.of(TomlTokenId.DOT, TomlTokenId.KEY);

    static int keyPrefixOffset(Document doc, int offset) throws BadLocationException {
        TokenHierarchy th = TokenHierarchy.get(doc);
        TokenSequence<TomlTokenId> ts = th.tokenSequence();
        ts.move(offset);
        ts.movePrevious();
        while ((ts.token() != null) && DOT_OR_KEY.contains(ts.token().id())) {
            ts.movePrevious();
        }
        ts.moveNext();
        return ts.offset();
    }
}
