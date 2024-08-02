/**
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
package org.netbeans.modules.lsp.client.bindings;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.lsp.CompletionCollector;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

@MimeRegistration(mimeType = "", service = CompletionProvider.class)
public class LspCompletionProviderImpl implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                FileObject file = NbEditorUtilities.getFileObject(doc);
                if (file == null) {
                    //TODO: beep
                    resultSet.finish();
                    return;
                }
                final String mime = file.getMIMEType();
                for (Lookup.Item<CompletionProvider> item : MimeLookup.getLookup(mime).lookupResult(CompletionProvider.class).allItems()) {
                    String id = item.getId();
                    if (id.startsWith("Editors/"+ mime)) {
                        // found real CompletionProvider - don't bridge LSP API
                        resultSet.finish();
                        return;
                    }
                }
                Consumer<org.netbeans.api.lsp.Completion> consumer = (i) -> {
                    String insert = i.getInsertText() != null ? i.getInsertText() : i.getLabel();
                    String leftLabel = encode(i.getLabel());
                    String rightLabel = null;
                    try {
                        if (i.getDetail() != null) {
                            rightLabel = encode(i.getDetail().get());
                        }
                    } catch (InterruptedException | ExecutionException interruptedException) {
                        // leave null
                    }
                    String sortText = i.getSortText() != null ? i.getSortText() : i.getLabel();
                    org.netbeans.api.lsp.Completion.Kind kind = i.getKind();
                    Icon ic = Icons.getCompletionIcon(kind);
                    ImageIcon icon = new ImageIcon(ImageUtilities.icon2Image(ic));
                    resultSet.addItem(new LspApiCompletionItem(i, doc, caretOffset, leftLabel, rightLabel, icon, sortText, insert));
                };
                org.netbeans.api.lsp.Completion.Context context = new org.netbeans.api.lsp.Completion.Context(org.netbeans.api.lsp.Completion.TriggerKind.Invoked, null);
                for (CompletionCollector cc : MimeLookup.getLookup(mime).lookupAll(CompletionCollector.class)) {
                    cc.collectCompletions(doc, caretOffset, context, consumer);
                }
                resultSet.finish();
            }
        }, component);
    }

    private String encode(String str) {
        return str.replace("&", "&amp;")
                .replace("<", "&lt;");
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        FileObject file = NbEditorUtilities.getFileObject(component.getDocument());
        if (file == null) {
            return 0;
        }
        return 0;
    }

    private static class LspApiCompletionItem extends AbstractCompletionItem<TextEdit> {

        private final org.netbeans.api.lsp.Completion i;

        public LspApiCompletionItem(org.netbeans.api.lsp.Completion i, Document doc, int caretOffset, String leftLabel, String rightLabel, ImageIcon icon, String sortText, String insert) {
            super(doc, caretOffset, leftLabel, rightLabel, icon, sortText, insert);
            this.i = i;
        }

        @Override
        TextEdit findEdit(boolean[] hideNow) {
            return i.getTextEdit();
        }

        @Override
        boolean isTextEdit(TextEdit te) {
            return te != null;
        }

        @Override
        int findStart(Document doc, TextEdit te) {
            return te.getStartOffset();
        }

        @Override
        int findEnd(Document doc, TextEdit te) {
            return te.getEndOffset();
        }

        @Override
        String findNewText(TextEdit te) {
            return te.getNewText();
        }

        @Override
        List<String> getCommitCharacters() {
            return i.getCommitCharacters().stream().map(String::valueOf).collect(Collectors.toList());
        }

        @Override
        boolean isTriggerCharacter(String commitText) {
            return false;
        }

        @Override
        Supplier<String[]> resolveDocumentation(Document doc, int caretOffset) {
            if (i.getDetail() == null && i.getDocumentation() == null) {
                return null;
            } else {
                return () -> {
                    String detail = null;
                    String documentation = null;
                    try {
                        if (i.getDetail() != null) {
                            detail = i.getDetail().get();
                        }
                    } catch (InterruptedException | ExecutionException interruptedException) {
                        // leave null
                    }
                    try {
                        if (i.getDocumentation() != null) {
                            documentation = i.getDocumentation().get();
                        }
                    } catch (InterruptedException | ExecutionException interruptedException) {
                        // leave null
                    }
                    return new String[]{detail, documentation};
                };
            }
        }
    }
}
