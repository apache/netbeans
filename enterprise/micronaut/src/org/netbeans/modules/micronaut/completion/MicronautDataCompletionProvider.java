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
package org.netbeans.modules.micronaut.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = CompletionProvider.class, position = 250)
public final class MicronautDataCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        switch (queryType) {
            case COMPLETION_ALL_QUERY_TYPE:
            case COMPLETION_QUERY_TYPE:
                return new AsyncCompletionTask(new MicronautDataCompletionQuery(), component);
        }
        return null;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    private static class MicronautDataCompletionQuery extends AsyncCompletionQuery {

        private static final String ICON = "org/netbeans/modules/micronaut/resources/micronaut.png";

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            MicronautDataCompletionTask task = new MicronautDataCompletionTask();
            resultSet.addAllItems(task.query(doc, caretOffset, new MicronautDataCompletionTask.ItemFactory<CompletionItem>() {
                @Override
                public CompletionItem createFinderMethodItem(String name, String returnType, int offset) {
                    CompletionUtilities.CompletionItemBuilder builder = CompletionUtilities.newCompletionItemBuilder(name)
                            .iconResource(ICON)
                            .leftHtmlText("<b>" + name + "</b>")
                            .sortPriority(10);
                    if (returnType != null) {
                        builder.onSelect(ctx -> {
                            final Document doc = ctx.getComponent().getDocument();
                            try {
                                doc.remove(offset, ctx.getComponent().getCaretPosition() - offset);
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            String template = "${PAR#1 default=\"" + returnType + "\"} " + name + "${cursor completionInvoke}()";
                            CodeTemplateManager.get(doc).createTemporary(template).insert(ctx.getComponent());
                        });
                    } else {
                        builder.startOffset(offset);
                    }
                    return builder.build();
                }

                @Override
                public CompletionItem createFinderMethodNameItem(String prefix, String name, int offset) {
                    return CompletionUtilities.newCompletionItemBuilder(prefix + name)
                            .startOffset(offset)
                            .iconResource(ICON)
                            .leftHtmlText( prefix + "<b>" + name + "</b>")
                            .sortPriority(10)
                            .sortText(name)
                            .build();
                }

                @Override
                public CompletionItem createSQLItem(CompletionItem item) {
                    return item;
                }
            }));
            resultSet.setAnchorOffset(task.getAnchorOffset());
            resultSet.finish();
        }
    }
}
