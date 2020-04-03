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

package org.netbeans.lib.editor.codetemplates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 * Implemenation of the code template description.
 *
 * @author Miloslav Metelka
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 300) //NOI18N
public final class CodeTemplateCompletionProvider implements CompletionProvider {

    public CompletionTask createTask(int type, JTextComponent component) {
        return (type & COMPLETION_QUERY_TYPE) == 0 || isAbbrevDisabled(component) ? null : new AsyncCompletionTask(new Query(), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static boolean isAbbrevDisabled(JTextComponent component) {
        return org.netbeans.editor.Abbrev.isAbbrevDisabled(component);
    }
    
    private static final class Query extends AsyncCompletionQuery
    implements ChangeListener {

        private JTextComponent component;
        
        private int queryCaretOffset;
        private int queryAnchorOffset;
        private List<CodeTemplateCompletionItem> queryResult;
        
        private String filterPrefix;
        
        protected @Override void prepareQuery(JTextComponent component) {
            this.component = component;
        }
        
        protected @Override boolean canFilter(JTextComponent component) {
            if (component.getCaret() == null) {
                return false;
            }
            int caretOffset = component.getSelectionStart();
            Document doc = component.getDocument();
            filterPrefix = null;
            if (caretOffset >= queryCaretOffset) {
                if (queryAnchorOffset < queryCaretOffset) {
                    try {
                        filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                        if (!isJavaIdentifierPart(filterPrefix)) {
                            filterPrefix = null;
                        }
                    } catch (BadLocationException e) {
                        // filterPrefix stays null -> no filtering
                    }
                }
            }
            return (filterPrefix != null);
        }
        
        protected @Override void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && queryResult != null) {
                resultSet.addAllItems(getFilteredData(queryResult, filterPrefix));
            }
            resultSet.finish();
        }
        
        private boolean isJavaIdentifierPart(CharSequence text) {
            for (int i = 0; i < text.length(); i++) {
                if (!(Character.isJavaIdentifierPart(text.charAt(i))) ) {
                    return false;
                }
            }
            return true;
        }
        
        private Collection<? extends CompletionItem> getFilteredData(
            Collection<? extends CompletionItem> data, 
            String prefix
        ) {
            List<CompletionItem> ret = new ArrayList<CompletionItem>();
            for (CompletionItem itm : data) {
                if (itm.getInsertPrefix().toString().startsWith(prefix)) {
                    ret.add(itm);
                }
            }
            return ret;
        }
        
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            String langPath = null;
            String identifierBeforeCursor = null;
            if (doc instanceof AbstractDocument) {
                AbstractDocument adoc = (AbstractDocument)doc;
                adoc.readLock();
                try {
                    try {
                        if (adoc instanceof BaseDocument) {
                            identifierBeforeCursor = Utilities.getIdentifierBefore((BaseDocument)adoc, caretOffset);
                        }
                    } catch (BadLocationException e) {
                        // leave identifierBeforeCursor null
                    }
                    List<TokenSequence<?>> list = TokenHierarchy.get(doc).embeddedTokenSequences(caretOffset, true);
                    if (list.size() > 1) {
                        langPath = list.get(list.size() - 1).languagePath().mimePath();
                    }
                } finally {
                    adoc.readUnlock();
                }
            }
            
            if (identifierBeforeCursor == null) {
                identifierBeforeCursor = ""; //NOI18N
            }
            
            if (langPath == null) {
                langPath = NbEditorUtilities.getMimeType(doc);
            }

            queryCaretOffset = caretOffset;
            queryAnchorOffset = caretOffset - identifierBeforeCursor.length();
            if (langPath != null) {
                String mimeType = DocumentUtilities.getMimeType(component);
                MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.get(mimeType);
                Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
                boolean ignoreCase = prefs.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
                
                CodeTemplateManagerOperation op = CodeTemplateManagerOperation.get(MimePath.parse(langPath));
                op.waitLoaded();
                
                Collection<? extends CodeTemplate> ctsPT = op.findByParametrizedText(identifierBeforeCursor, ignoreCase);
                Collection<? extends CodeTemplate> ctsAb = op.findByAbbreviationPrefix(identifierBeforeCursor, ignoreCase);
                Collection<? extends CodeTemplateFilter> filters = CodeTemplateManagerOperation.getTemplateFilters(component, queryAnchorOffset);
                
                queryResult = new ArrayList<CodeTemplateCompletionItem>(ctsPT.size() + ctsAb.size());
                Set<String> abbrevs = new HashSet<String>(ctsPT.size() + ctsAb.size());
                for (CodeTemplate ct : ctsPT) {
                    if (ct.getContexts() != null && ct.getContexts().size() > 0 && accept(ct, filters) && abbrevs.add(ct.getAbbreviation())) {
                        queryResult.add(new CodeTemplateCompletionItem(ct, false));
                    }
                }
                for (CodeTemplate ct : ctsAb) {
                    if (ct.getContexts() != null && ct.getContexts().size() > 0 && accept(ct, filters) && abbrevs.add(ct.getAbbreviation())) {
                        queryResult.add(new CodeTemplateCompletionItem(ct, true));
                    }
                }
                resultSet.addAllItems(queryResult);
            }
            
            resultSet.setAnchorOffset(queryAnchorOffset);
            resultSet.finish();
        }

        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                notify();
            }
        }
        
        private static boolean accept(CodeTemplate template, Collection/*<CodeTemplateFilter>*/ filters) {
            for(Iterator<CodeTemplateFilter> it = filters.iterator(); it.hasNext();) {
                CodeTemplateFilter filter = it.next();
                if (!filter.accept(template))
                    return false;                
            }
            return true;
        }
        
    }

}
