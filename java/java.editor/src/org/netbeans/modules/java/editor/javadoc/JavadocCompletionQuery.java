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

package org.netbeans.modules.java.editor.javadoc;

import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Pokorsky
 */
final class JavadocCompletionQuery extends AsyncCompletionQuery{
    
    private final int queryType;
    
    private int caretOffset;
    private List<CompletionItem> items;
    private JTextComponent component;

    public JavadocCompletionQuery(int queryType) {
        this.queryType = queryType;
    }

    @Override
    protected void prepareQuery(JTextComponent component) {
        this.component = component;
    }

    @Override
    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
        try {
            this.caretOffset = caretOffset;
            items = null;
            Source source = Source.create(doc);
            if (source != null) {
                if ((queryType & CompletionProvider.COMPLETION_QUERY_TYPE) != 0) {
                    JavadocCompletionTask<CompletionItem> task = JavadocCompletionTask.create(caretOffset, new JavadocCompletionItem.Factory(),
                            queryType == CompletionProvider.COMPLETION_ALL_QUERY_TYPE, this::isTaskCancelled);
                    setCompletionHack(true);
                    ParserManager.parse(Collections.singletonList(source), task);
                    setCompletionHack(false);
                    items = task.getResults();
                    if (items != null) {
                        resultSet.addAllItems(items);
                    }
                    resultSet.setHasAdditionalItems(task.hasAdditionalItems());
                    int anchorOffset = task.getAnchorOffset();
                    if (anchorOffset > -1) {
                        resultSet.setAnchorOffset(anchorOffset);
                    }
                }
            }
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            resultSet.finish();
        }
    }

    @Override
    protected boolean canFilter(JTextComponent component) {
        final int newOffset = component.getSelectionStart();
        final Document doc = component.getDocument();
        if (newOffset > caretOffset && items != null && !items.isEmpty()) {
            try {
                String prefix = doc.getText(caretOffset, newOffset - caretOffset);
                if (!isJavaIdentifierPart(prefix)) {
                    Completion.get().hideDocumentation();
                    Completion.get().hideCompletion();
                }
            } catch (BadLocationException ble) {
            }
        }
        return false;
    }
    
    private boolean isJavaIdentifierPart(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!(Character.isJavaIdentifierPart(text.charAt(i))))
                return false;
        }
        return true;
    }

    /** #145615: this helps to work around the issue with stuck
     * {@code JavaSource.runWhenScanFinished}
     * It is copied from {@code JavaCompletionQuery}.
     */
    private void setCompletionHack(boolean flag) {
        if (component != null) {
            component.putClientProperty("completion-active", flag); //NOI18N
        }
    }


    static List<CompletionItem> runCompletionQuery(int queryType, Document doc, int caret) {
        JavadocCompletionTask<CompletionItem> task = JavadocCompletionTask.create(caret, new JavadocCompletionItem.Factory(),
                queryType == CompletionProvider.COMPLETION_ALL_QUERY_TYPE, null);
        Source source = Source.create(doc);
        if (source != null) {
            try {
                ParserManager.parse(Collections.singletonList(source), task);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return task.getResults();
    }
}
