/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
