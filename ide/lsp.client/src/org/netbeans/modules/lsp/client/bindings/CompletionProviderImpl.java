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

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
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
                        resultSet.addItem(new org.netbeans.spi.editor.completion.CompletionItem() {
                            @Override
                            public void defaultAction(JTextComponent jtc) {
                                commit("");
                            }
                            private void commit(String appendText) {
                                Either<TextEdit, InsertReplaceEdit> edit = i.getTextEdit();
                                if (edit != null && edit.isRight()) {
                                    //TODO: the NetBeans client does not current support InsertReplaceEdits, should not happen
                                    Completion.get().hideDocumentation();
                                    Completion.get().hideCompletion();
                                    return ;
                                }
                                TextEdit te = edit != null ? edit.getLeft() : null;
                                NbDocument.runAtomic((StyledDocument) doc, () -> {
                                    try {
                                        int endPos;
                                        if (te != null) {
                                            int start = Utils.getOffset(doc, te.getRange().getStart());
                                            int end = Utils.getOffset(doc, te.getRange().getEnd());
                                            doc.remove(start, end - start);
                                            doc.insertString(start, te.getNewText(), null);
                                            endPos = start + te.getNewText().length();
                                        } else {
                                            String toAdd = i.getInsertText();
                                            if (toAdd == null) {
                                                toAdd = i.getLabel();
                                            }
                                            int[] identSpan = Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);
                                            if (identSpan != null) {
                                                doc.remove(identSpan[0], identSpan[1] - identSpan[0]);
                                                doc.insertString(identSpan[0], toAdd, null);
                                                endPos = identSpan[0] + toAdd.length();
                                            } else {
                                                doc.insertString(caretOffset, toAdd, null);
                                                endPos = caretOffset + toAdd.length();
                                            }
                                        }
                                        doc.insertString(endPos, appendText, null);
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                });
                                Completion.get().hideDocumentation();
                                Completion.get().hideCompletion();
                            }

                            @Override
                            public void processKeyEvent(KeyEvent ke) {
                                if (ke.getID() == KeyEvent.KEY_TYPED) {
                                    String commitText = String.valueOf(ke.getKeyChar());
                                    List<String> commitCharacters = i.getCommitCharacters();

                                    if (commitCharacters != null && commitCharacters.contains(commitText)) {
                                        commit(commitText);
                                        ke.consume();
                                        if (isTriggerCharacter(server, commitText)) {
                                            Completion.get().showCompletion();
                                        }
                                    }
                                }
                            }

                            @Override
                            public int getPreferredWidth(Graphics grphcs, Font font) {
                                return CompletionUtilities.getPreferredWidth(leftLabel, rightLabel, grphcs, font);
                            }

                            @Override
                            public void render(Graphics grphcs, Font font, Color color, Color color1, int i, int i1, boolean bln) {
                                CompletionUtilities.renderHtml(icon, leftLabel, rightLabel, grphcs, font, color, i, i1, bln);
                            }

                            @Override
                            public CompletionTask createDocumentationTask() {
                                return new AsyncCompletionTask(new AsyncCompletionQuery() {
                                    @Override
                                    protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                                        CompletionItem resolved;
                                        if ((i.getDetail() == null || i.getDocumentation() == null) && hasCompletionResolve(server)) {
                                            CompletionItem temp;
                                            try {
                                                temp = server.getTextDocumentService().resolveCompletionItem(i).get();
                                            } catch (InterruptedException | ExecutionException ex) {
                                                Exceptions.printStackTrace(ex);
                                                temp = i;
                                            }
                                            resolved = temp;
                                        } else {
                                            resolved = i;
                                        }
                                        if (resolved.getDocumentation() != null || resolved.getDetail() != null) {
                                            resultSet.setDocumentation(new CompletionDocumentation() {
                                                @Override
                                                public String getText() {
                                                    StringBuilder documentation = new StringBuilder();
                                                    documentation.append("<html>\n");
                                                    if (resolved.getDetail() != null) {
                                                        documentation.append("<b>").append(escape(resolved.getDetail())).append("</b>");
                                                        documentation.append("\n<p>");
                                                    }
                                                    if (resolved.getDocumentation() != null) {
                                                        MarkupContent content;
                                                        if (resolved.getDocumentation().isLeft()) {
                                                            content = new MarkupContent();
                                                            content.setKind("plaintext");
                                                            content.setValue(resolved.getDocumentation().getLeft());
                                                        } else {
                                                            content = resolved.getDocumentation().getRight();
                                                        }
                                                        switch (content.getKind()) {
                                                            default:
                                                            case "plaintext": documentation.append("<pre>\n").append(content.getValue()).append("\n</pre>"); break;
                                                            case "markdown": documentation.append(HtmlRenderer.builder().build().render(Parser.builder().build().parse(content.getValue()))); break;
                                                        }
                                                    }
                                                    return documentation.toString();
                                                }
                                                @Override
                                                public URL getURL() {
                                                    return null;
                                                }
                                                @Override
                                                public CompletionDocumentation resolveLink(String link) {
                                                    return null;
                                                }
                                                @Override
                                                public Action getGotoSourceAction() {
                                                    return null;
                                                }
                                            });
                                        }
                                        resultSet.finish();
                                    }
                                });
                            }

                            @Override
                            public CompletionTask createToolTipTask() {
                                return null;
                            }

                            @Override
                            public boolean instantSubstitution(JTextComponent jtc) {
                                return false;
                            }

                            @Override
                            public int getSortPriority() {
                                return 100;
                            }

                            @Override
                            public CharSequence getSortText() {
                                return sortText;
                            }

                            @Override
                            public CharSequence getInsertPrefix() {
                                return insert;
                            }
                        });
                    }
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
    
    private boolean hasCompletionResolve(LSPBindings server) {
        ServerCapabilities capabilities = server.getInitResult().getCapabilities();
        if (capabilities == null) return false;
        CompletionOptions completionProvider = capabilities.getCompletionProvider();
        if (completionProvider == null) return false;
        Boolean resolveProvider = completionProvider.getResolveProvider();
        return resolveProvider != null && resolveProvider;
    }

    private static String escape(String s) {
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
    
    private boolean isTriggerCharacter(LSPBindings server, String text) {
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
