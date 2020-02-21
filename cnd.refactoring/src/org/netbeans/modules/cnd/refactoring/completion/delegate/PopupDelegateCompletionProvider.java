/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.completion.delegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 */
public class PopupDelegateCompletionProvider implements CompletionProvider {

    public PopupDelegateCompletionProvider() {
        // default constructor to be created as lookup service
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            final int dot = component.getCaret().getDot();
            return new AsyncCompletionTask(new Query(dot, component), component);
        }
        return null;
    }

    private static final class Query extends AsyncCompletionQuery {

        private final int creationCaretOffset;
        private final JTextComponent component;
        private Collection<CompletionItem> results;
        private int queryAnchorOffset;
        private String filterPrefix;

        /*package*/ Query(int caretOffset, JTextComponent component) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
            this.component = component;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Collection<CompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset >= 0) {
                if (items != null && items.size() > 0) {
                    this.results = items;
                    items = getFilteredData(items, this.filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(queryAnchorOffset);
                }
                resultSet.setHasAdditionalItems(false);
            }
            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    Completion.get().hideCompletion();
                }
            } else {
                Completion.get().hideCompletion();
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<CompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private Collection<CompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            List<CompletionItem> items = new ArrayList<>();
            if (init(doc, caretOffset)) {
                CsmContext context = CsmContext.create(doc, caretOffset, caretOffset, caretOffset);
                if (context != null) {
                    CsmClass enclosingClass = context.getEnclosingClass();
                    if (enclosingClass != null) {
                        List<CsmObject> path = context.getPath();
                        CsmObject last = path.get(path.size()-1);
                        if (CsmKindUtilities.isClass(last) || CsmKindUtilities.isField(last)) {
                            PopupDelegateCompletionItem item = PopupDelegateCompletionItem.createImplementItem(caretOffset, component);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                }
            }
            return items;
        }
        
        private boolean init(final BaseDocument doc, final int caretOffset) {
            filterPrefix = "";
            queryAnchorOffset = caretOffset;
            if (doc != null) {
                doc.readLock();
                try {
                    TokenItem<TokenId> tok = CndTokenUtilities.getTokenCheckPrev(doc, caretOffset);
                    if (tok != null) {
                        TokenId id = tok.id();
                        if(id instanceof CppTokenId) {
                            if (!CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory())) {
                                queryAnchorOffset = tok.offset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // skip
                } finally {
                    doc.readUnlock();
                }
            }
            return this.queryAnchorOffset >= 0;
        }

        private Collection<CompletionItem> getFilteredData(Collection<CompletionItem> data, String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                return data;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
