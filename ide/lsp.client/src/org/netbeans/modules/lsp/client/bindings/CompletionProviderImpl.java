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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="", service=CompletionProvider.class)
public class CompletionProviderImpl implements CompletionProvider {

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & TOOLTIP_QUERY_TYPE) != 0) {
            return new AsyncCompletionTask(new AsyncCompletionQuery() {
                @Override
                protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                    try {
                        FileObject file = NbEditorUtilities.getFileObject(doc);
                        if (file == null) {
                            //TODO: beep
                            return ;
                        }
                        LSPBindings server = LSPBindings.getBindings(file);
                        if (server == null) {
                            return ;
                        }
                        String uri = Utils.toURI(file);
                        SignatureHelpParams params;
                        params = new SignatureHelpParams(new TextDocumentIdentifier(uri),
                                Utils.createPosition(doc, caretOffset));
                        SignatureHelp help = server.getTextDocumentService().signatureHelp(params).get();
                        if (help == null || help.getSignatures().isEmpty()) {
                            return ;
                        }
                        //TODO: active signature?
                        StringBuilder signatures = new StringBuilder();
                        signatures.append("<html>");
                        for (SignatureInformation info : help.getSignatures()) {
                            if (info.getParameters().isEmpty()) {
                                signatures.append("No parameter.");
                                continue;
                            }
                            String sigSep = "";
                            int idx = 0;
                            for (ParameterInformation pi : info.getParameters()) {
                                if (idx == help.getActiveParameter()) {
                                    signatures.append("<b>");
                                }
                                signatures.append(sigSep);
                                signatures.append(pi.getLabel());
                                if (idx == help.getActiveParameter()) {
                                    signatures.append("</b>");
                                }
                                sigSep = ", ";
                                idx++;
                            }
                        }
                        JToolTip tip = new JToolTip();
                        tip.setTipText(signatures.toString());
                        resultSet.setToolTip(tip);
                    } catch (BadLocationException | InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        resultSet.finish();
                    }
                }
            }, component);
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                try {
                    FileObject file = NbEditorUtilities.getFileObject(doc);
                    if (file == null) {
                        //TODO: beep
                        return ;
                    }
                    LSPBindings server = LSPBindings.getBindings(file);
                    if (server == null) {
                        return ;
                    }
                    String uri = Utils.toURI(file);
                    CompletionParams params;
                    params = new CompletionParams(new TextDocumentIdentifier(uri),
                            Utils.createPosition(doc, caretOffset));
                    CountDownLatch l = new CountDownLatch(1);
                    //TODO: Location or Location[]
                    Either<List<CompletionItem>, CompletionList> completionResult = server.getTextDocumentService().completion(params).get();
                    if (completionResult == null) {
                        return ; //no results
                    }
                    List<CompletionItem> items;
                    boolean incomplete;
                    if (completionResult.isLeft()) {
                        items = completionResult.getLeft();
                        incomplete = true;
                    } else {
                        items = completionResult.getRight().getItems();
                        incomplete = completionResult.getRight().isIncomplete();
                    }
                    for (CompletionItem i : items) {
                        String insert = i.getInsertText() != null ? i.getInsertText() : i.getLabel();
                        String leftLabel = encode(i.getLabel());
                        String rightLabel;
                        if (i.getDetail() != null) {
                            rightLabel = encode(i.getDetail());
                        } else {
                            rightLabel = null;
                        }
                        String sortText = i.getSortText() != null ? i.getSortText() : i.getLabel();
                        CompletionItemKind kind = i.getKind();
                        Icon ic = Icons.getCompletionIcon(kind);
                        ImageIcon icon = new ImageIcon(ImageUtilities.icon2Image(ic));
                        resultSet.addItem(new LspCompletionItem(i, doc, caretOffset, server, leftLabel, rightLabel, icon, sortText, insert, this, org.netbeans.modules.lsp.client.bindings.CompletionProviderImpl.this));
                    }
                } catch (BadLocationException | InterruptedException | ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    resultSet.finish();
                }
            }
        }, component);
    }

    final boolean hasCompletionResolve(LSPBindings server) {
        ServerCapabilities capabilities = server.getInitResult().getCapabilities();
        if (capabilities == null) {
            return false;
        }
        CompletionOptions completionProvider = capabilities.getCompletionProvider();
        if (completionProvider == null) return false;
        Boolean resolveProvider = completionProvider.getResolveProvider();
        return resolveProvider != null && resolveProvider;
    }

    static String escape(String s) {
        if (s != null) {
            try {
                return XMLUtil.toAttributeValue(s);
            } catch (Exception ex) {}
        }
        return s;
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
        LSPBindings server = LSPBindings.getBindings(file);
        if (server == null) {
            return 0;
        }
        return isTriggerCharacter(server, typedText) ? COMPLETION_QUERY_TYPE : 0;
    }

    boolean isTriggerCharacter(LSPBindings server, String text) {
        InitializeResult init = server.getInitResult();
        if (init == null) return false;
        ServerCapabilities capabilities = init.getCapabilities();
        if (capabilities == null) return false;
        CompletionOptions completionOptions = capabilities.getCompletionProvider();
        if (completionOptions == null) return false;
        List<String> triggerCharacters = completionOptions.getTriggerCharacters();
        if (triggerCharacters == null) return false;
        return triggerCharacters.stream().anyMatch(trigger -> text.endsWith(trigger));
    }
}
