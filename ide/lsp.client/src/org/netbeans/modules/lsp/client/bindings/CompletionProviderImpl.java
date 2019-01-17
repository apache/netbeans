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
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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
                        TextDocumentPositionParams params;
                        params = new TextDocumentPositionParams(new TextDocumentIdentifier(uri),
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
                        String rightLabel = encode(""); //TODO: anything we could show there?
                        String sortText = i.getSortText() != null ? i.getSortText() : i.getLabel();
                        String header = "<html>" + "<b>" + (i.getDetail() != null ? i.getDetail() : i.getLabel()) + "</b>";
                        String documentation;
                        if (i.getDocumentation() != null) {
                            header += "<br><br>";
                            if (i.getDocumentation().isLeft()) {
                                documentation = header + i.getDocumentation().getLeft();
                            } else {
                                documentation = header + i.getDocumentation().getRight().getValue(); //TODO: convert markup!
                            }
                        } else {
                            documentation = header;
                        }
                        CompletionItemKind kind = i.getKind();
                        Icon ic = Icons.getCompletionIcon(kind);
                        ImageIcon icon = new ImageIcon(ImageUtilities.icon2Image(ic));
                        resultSet.addItem(new org.netbeans.spi.editor.completion.CompletionItem() {
                            @Override
                            public void defaultAction(JTextComponent jtc) {
                                Document doc = jtc.getDocument();
                                TextEdit te = i.getTextEdit();
                                if (te != null) {
                                    int start = Utils.getOffset(doc, te.getRange().getStart());
                                    int end = Utils.getOffset(doc, te.getRange().getEnd());
                                    NbDocument.runAtomic((StyledDocument) doc, () -> {
                                        try {
                                            doc.remove(start, end - start);
                                            doc.insertString(start, te.getNewText(), null);
                                        } catch (BadLocationException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    });
                                } else {
                                    String toAdd = i.getInsertText();
                                    if (toAdd == null) {
                                        toAdd = i.getLabel();
                                    }
                                    try {
                                        int offset = jtc.getCaretPosition();
                                        String ident = Utilities.getIdentifier((BaseDocument) doc, offset);
                                        doc.insertString(offset, toAdd.substring(ident != null ? ident.length() : 0), null);
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }

                            @Override
                            public void processKeyEvent(KeyEvent ke) {
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
                                return new CompletionTask() {
                                    @Override
                                    public void query(CompletionResultSet resultSet) {
                                        resultSet.setDocumentation(new CompletionDocumentation() {
                                            @Override
                                            public String getText() {
                                                return documentation;
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
                                        resultSet.finish();
                                    }

                                    @Override
                                    public void refresh(CompletionResultSet resultSet) {}

                                    @Override
                                    public void cancel() {}
                                };
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
    
    private String encode(String str) {
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;");
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0; //TODO: implement trigger characters, if any?
    }
    
}
