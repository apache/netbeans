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
package org.netbeans.modules.editor.search.completion;

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.modules.editor.search.EditorFindSupport.RP;
import org.netbeans.modules.editor.search.EditorFindSupport.SPW;
import org.netbeans.modules.editor.search.SearchBar;
import org.netbeans.modules.editor.search.SearchNbEditorKit;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

@MimeRegistration(mimeType = SearchNbEditorKit.SEARCHBAR_MIMETYPE, service = CompletionProvider.class)
public class SearchCompletion implements CompletionProvider {

    public SearchCompletion() {
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if (queryType == COMPLETION_QUERY_TYPE) {
            return new AsyncCompletionTask(new Query(), component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    static class Query extends AsyncCompletionQuery {

        private Set<SearchCompletionItem> results;

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            SearchBar searchBar = SearchBar.getInstance();
            searchBar.setPopupMenuWasCanceled(true);
            String queryText = "";
            results = new HashSet<>();
            try {
                queryText = doc.getText(0, doc.getLength());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!queryText.trim().isEmpty()) {
                queryText = queryText.toLowerCase();
                String text = searchBar.getActualTextComponent().getText();
                String[] split = text.split("[\\p{Punct}\\s]");

                for (String s : split) {
                    if (s.toLowerCase().startsWith(queryText) && s.length() != queryText.length()) {
                        SearchCompletionItem searchCompletionItem = new SearchCompletionItem(s);
                        results.add(searchCompletionItem);
                    }
                }
                for (SPW spw : EditorFindSupport.getInstance().getHistory()) {
                    String s = spw.getSearchExpression().trim();
                    if (s.toLowerCase().startsWith(queryText) && s.length() != queryText.length()) {
                        SearchCompletionItem searchCompletionItem = new SearchCompletionItem(s);
                        results.add(searchCompletionItem);
                    }
                }
                for (RP rp : EditorFindSupport.getInstance().getReplaceHistory()) {
                    String s = rp.getReplaceExpression().trim();
                    if (s.toLowerCase().startsWith(queryText) && s.length() != queryText.length()) {
                        SearchCompletionItem searchCompletionItem = new SearchCompletionItem(s);
                        results.add(searchCompletionItem);
                    }
                }
            }
            if (resultSet != null) { // resultSet can be null only in tests!
                resultSet.addAllItems(results);
                resultSet.finish();
            }

        }
    }
}
