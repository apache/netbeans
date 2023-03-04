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
package org.netbeans.modules.languages.antlr.v3;

import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.antlr.AntlrParser;
import org.netbeans.modules.languages.antlr.AntlrParserResult;
import org.netbeans.modules.languages.antlr.AntlrTokenId;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Laszlo Kishalmi
 */
@MimeRegistration(mimeType = Antlr3Language.MIME_TYPE, service = CompletionProvider.class)
public class Antlr3CompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new AntlrCompletionQuery(), component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static boolean isCaseSensitive() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
    }

    private class AntlrCompletionQuery extends AsyncCompletionQuery {

        //TODO: This is a Lexer based pretty dumb implementation. Only offer
        //      prefix if the cursor is at the end of a start of token/lexer rule.
        //      Shall be replaced with a better approach.
        private String getPrefix(Document doc, int caretOffset, boolean upToOffset) {
            String ret = null;
            TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
            TokenSequence<?> ts = tokenHierarchy.tokenSequence();
            ts.move(caretOffset);
            if (ts.movePrevious()) {
                int len = caretOffset - ts.offset();
                Token<?> token = ts.token();
                if (token.id() == AntlrTokenId.RULE || token.id() == AntlrTokenId.TOKEN) {
                    ret = String.valueOf(token.text());
                    ret = upToOffset ? ret.substring(0, len) : ret;
                }
            }
            return ret;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            try {
                FileObject fo = EditorDocumentUtils.getFileObject(doc);
                if(fo == null) {
                    return;
                }

                boolean isCaseSensitive = isCaseSensitive();
                String prefix = getPrefix(doc, caretOffset, true);

                if(prefix == null) {
                    return;
                }

                String mprefix = isCaseSensitive ? prefix : prefix.toUpperCase();

                AntlrParserResult result = AntlrParser.getParserResult(fo);
                Map<String, AntlrParserResult.Reference> refs = result.references;

                int startOffset = caretOffset - prefix.length();
                for (String ref : refs.keySet()) {
                    String mref = isCaseSensitive ? ref : ref.toUpperCase();
                    boolean match = mref.startsWith(mprefix);
                    if (match) {
                        CompletionItem item = CompletionUtilities.newCompletionItemBuilder(ref)
                                .startOffset(startOffset)
                                .leftHtmlText(ref)
                                .sortText(ref)
                                .build();
                        resultSet.addItem(item);
                    }
                }

            } finally {
                resultSet.finish();
            }
        }

    }
}
