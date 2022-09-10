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
package org.netbeans.modules.languages.antlr;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.languages.antlr.v3.Antlr3Language;
import org.netbeans.modules.languages.antlr.v4.Antlr4Language;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

import static org.netbeans.modules.languages.antlr.AntlrIndexer.FIELD_CASE_INSENSITIVE_DECLARATION;
import static org.netbeans.modules.languages.antlr.AntlrIndexer.FIELD_DECLARATION;
import static org.netbeans.modules.languages.antlr.AntlrIndexer.transitiveImports;

/**
 *
 * @author Laszlo Kishalmi
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = Antlr3Language.MIME_TYPE, service = CompletionProvider.class),
    @MimeRegistration(mimeType = Antlr4Language.MIME_TYPE, service = CompletionProvider.class),
})
public class AntlrCompletionProvider implements CompletionProvider {

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
        private String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
            String ret = null;
            TokenHierarchy<?> tokenHierarchy = info.getSnapshot().getTokenHierarchy();
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
                Source source = Source.create(doc);
                if (source != null) {
                    UserTask task = new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            AntlrParserResult result = (AntlrParserResult) resultIterator.getParserResult(caretOffset);
                            String prefix = getPrefix(result, caretOffset, true);
                            FileObject fo = source.getFileObject();
                            if (prefix != null && fo != null) {
                                String prefix_ci = prefix.toLowerCase(Locale.ENGLISH);
                                QuerySupport qs = AntlrIndexer.getQuerySupport(fo);
                                QuerySupport.Query.Factory qf = qs.getQueryFactory();
                                List<FileObject> candidates = transitiveImports(qs, fo);
                                QuerySupport.Query query = qf.and(
                                        // Only consider the file itself or imported files
                                        qf.or(
                                                candidates.stream()
                                                        .map(fo2 -> qs.getQueryFactory().file(fo2))
                                                        .collect(Collectors.toList())
                                                        .toArray(new QuerySupport.Query[0])
                                        ),
                                        qf.field(
                                                isCaseSensitive() ? FIELD_DECLARATION : FIELD_CASE_INSENSITIVE_DECLARATION,
                                                isCaseSensitive() ? prefix : prefix_ci,
                                                isCaseSensitive() ? Kind.PREFIX : Kind.CASE_INSENSITIVE_PREFIX
                                        )
                                );

                                for (IndexResult ir : query.execute(FIELD_DECLARATION)) {
                                    for (String value : ir.getValues(FIELD_DECLARATION)) {
                                        if (isCaseSensitive()) {
                                            if (!value.startsWith(prefix)) {
                                                continue;
                                            }
                                        } else {
                                            if(! value.toLowerCase(Locale.ENGLISH).startsWith(prefix_ci)) {
                                                continue;
                                            }
                                        }
                                        String[] values = value.split("\\\\");
                                        CompletionItem item = CompletionUtilities
                                                .newCompletionItemBuilder(values[0])
                                                .startOffset(caretOffset - prefix.length())
                                                .leftHtmlText(values[0])
                                                .sortText(values[0])
                                                .build();
                                        resultSet.addItem(item);
                                    }
                                }
                            }
                        }
                    };
                    ParserManager.parse(Collections.singleton(source), task);
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                resultSet.finish();
            }
        }
    }
}
