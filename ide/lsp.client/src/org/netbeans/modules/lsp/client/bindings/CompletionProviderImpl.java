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

package org.netbeans.modules.lsp.client.bindings;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JToolTip;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
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
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;

/**
 *
 * @author lahvac
 */
@MimeRegistration(mimeType="", service=CompletionProvider.class)
public class CompletionProviderImpl implements CompletionProvider {

    private static final Logger LOG = Logger.getLogger(CompletionProviderImpl.class.getName());

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
                        //TODO: active signature?
                        StringBuilder signatures = new StringBuilder();
                        Utils.handleBindings(LSPBindings.getBindings(file),
                                             capa -> capa.getSignatureHelpProvider() != null,
                                             () -> new SignatureHelpParams(new TextDocumentIdentifier(Utils.toURI(file)),
                                                        Utils.createPosition(doc, caretOffset)),
                                             (server, params) -> server.getTextDocumentService().signatureHelp(params),
                                             (server, result) -> handleSignatureInfo(result, signatures));
                        if (signatures.isEmpty()) {
                            return;
                        }
                        signatures.insert(0, "<html>");
                        JToolTip tip = new JToolTip();
                        tip.setTipText(signatures.toString());
                        resultSet.setToolTip(tip);
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
                    Utils.handleBindings(LSPBindings.getBindings(file),
                                         capa -> capa.getCompletionProvider() != null,
                                         () -> new CompletionParams(new TextDocumentIdentifier(Utils.toURI(file)),
                                                                    Utils.createPosition(doc, caretOffset)),
                                         (server, params) -> server.getTextDocumentService().completion(params),
                                         (server, result) -> handleCompletionResult(resultSet, component, doc, caretOffset, server, result));
                } finally {
                    resultSet.finish();
                }
            }
        }, component);
    }

    @Messages("DN_NoParameter=No parameter.")
    private void handleSignatureInfo(SignatureHelp help, StringBuilder signatures) {
        if (help == null || help.getSignatures().isEmpty()) {
            return ;
        }
        for (SignatureInformation info : help.getSignatures()) {
            if (info.getParameters() == null || info.getParameters().isEmpty()) {
                if (info.getLabel().isEmpty()) {
                    signatures.append(Bundle.DN_NoParameter());
                } else {
                    signatures.append(info.getLabel());
                }
                signatures.append("<br>");
                continue;
            }
            String sigSep = "";
            int idx = 0;
            for (ParameterInformation pi : info.getParameters()) {
                signatures.append(sigSep);
                if (idx == help.getActiveParameter()) {
                    signatures.append("<b>");
                }
                String label;
                if (pi.getLabel().isLeft()) {
                    label = pi.getLabel().getLeft();
                } else {
                    Integer start = pi.getLabel().getRight().getFirst();
                    Integer end = pi.getLabel().getRight().getSecond();

                    label = info.getLabel().substring(start, end);
                }
                signatures.append(label);
                if (idx == help.getActiveParameter()) {
                    signatures.append("</b>");
                }
                sigSep = ", ";
                idx++;
            }
            signatures.append("<br>");
        }
    }

    private void handleCompletionResult(CompletionResultSet resultSet, JTextComponent component, Document doc, int caretOffset, LSPBindings server, Either<List<CompletionItem>, CompletionList> completionResult) {
        if (completionResult == null) {
            return ; //no results
        }
        CompletionOptions completionOptions = server.getInitResult().getCapabilities().getCompletionProvider();
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
            String leftLabel;
            String rightLabel;
            if (i.getLabelDetails() != null) {
                leftLabel = encode(i.getLabel() + (i.getLabelDetails().getDetail() != null ? i.getLabelDetails().getDetail() : ""));
                if (i.getLabelDetails().getDescription() != null) {
                    rightLabel = encode(i.getLabelDetails().getDescription());
                } else {
                    rightLabel = null;
                }
            } else {
                leftLabel = encode(i.getLabel());
                if (i.getDetail() != null) {
                    rightLabel = encode(i.getDetail());
                } else {
                    rightLabel = null;
                }
            }
            String sortText = i.getSortText() != null ? i.getSortText() : i.getLabel();
            CompletionItemKind kind = i.getKind();
            ImageIcon icon = ImageUtilities.icon2ImageIcon(Icons.getCompletionIcon(kind));
            resultSet.addItem(new org.netbeans.spi.editor.completion.CompletionItem() {
                @Override
                public void defaultAction(JTextComponent jtc) {
                    commit("");
                }
                private void commit(String appendText) {
                    CompletionItem resolved;
                    if (i.getTextEdit() == null && hasCompletionResolve(completionOptions)) {
                        CompletionItem resolvedTemp = i;
                        try {
                            resolvedTemp = server.getTextDocumentService().resolveCompletionItem(resolvedTemp).get();
                        } catch (InterruptedException | ExecutionException ex) {
                            //TODO: ?
                            LOG.log(Level.FINE, null, ex);
                        }
                        resolved = resolvedTemp;
                    } else {
                        resolved = i;
                    }
                    Either<TextEdit, InsertReplaceEdit> edit = resolved.getTextEdit();
                    if (edit != null && edit.isRight()) {
                        //TODO: the NetBeans client does not current support InsertReplaceEdits, should not happen
                        Completion.get().hideDocumentation();
                        Completion.get().hideCompletion();
                        return ;
                    }
                    NbDocument.runAtomic((StyledDocument) doc, () -> {
                        try {
                            CodeTemplate template = null;
                            TextEdit mainEdit;

                            if (edit != null ) {
                                TextEdit providedMainEdit = edit.getLeft();
                                if (resolved.getInsertTextFormat() == InsertTextFormat.Snippet) {
                                    template = CodeTemplateManager.get(doc).createTemporary(convertSnippet2CodeTemplate(providedMainEdit.getNewText()));
                                    mainEdit = new TextEdit(providedMainEdit.getRange(), "");
                                } else {
                                    mainEdit = providedMainEdit;
                                }
                            } else {
                                String toAdd;
                                if (resolved.getInsertTextFormat() == InsertTextFormat.Snippet) {
                                    //TODO: handle appendText
                                    template = CodeTemplateManager.get(doc).createTemporary(convertSnippet2CodeTemplate(resolved.getInsertText()));
                                    toAdd = "";
                                } else {
                                    toAdd = resolved.getInsertText();
                                    if (toAdd == null) {
                                        toAdd = resolved.getLabel();
                                    }
                                }
                                int[] identSpan = Utilities.getIdentifierBlock((BaseDocument) doc, caretOffset);
                                Position start;
                                Position end;
                                if (identSpan != null) {
                                    start = Utils.createPosition(doc, identSpan[0]);
                                    end = Utils.createPosition(doc, identSpan[1]);
                                } else {
                                    end = start = Utils.createPosition(doc, caretOffset);
                                }
                                mainEdit = new TextEdit(new Range(start, end), toAdd);
                            }

                            List<TextEdit> allEdits = new ArrayList<>();

                            allEdits.add(mainEdit);

                            if (resolved.getAdditionalTextEdits() != null) {
                                allEdits.addAll(resolved.getAdditionalTextEdits());
                            }

                            int insertPos = Utils.getOffset(doc, mainEdit.getRange().getStart());
                            LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
                            javax.swing.text.Position startPos = ld.createPosition(insertPos, Bias.Backward);

                            Utils.applyEditsNoLock(doc, allEdits);

                            if (template != null) {
                                //XXX: this does format
                                template.insert(component);
                            } else {
                                int endPos = startPos.hashCode() + mainEdit.getNewText().length();

                                doc.insertString(endPos, appendText, null);
                            }
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
                    COMPLETION_ITEM_RENDERER.renderCompletionItem(icon, leftLabel, rightLabel, grphcs, font, color, i, i1, bln);
                }

                @Override
                public CompletionTask createDocumentationTask() {
                    return new AsyncCompletionTask(new AsyncCompletionQuery() {
                        @Override
                        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                            CompletionItem resolved;
                            if ((i.getDetail() == null || i.getDocumentation() == null) && hasCompletionResolve(completionOptions)) {
                                CompletionItem temp;
                                try {
                                    temp = server.getTextDocumentService().resolveCompletionItem(i).get();
                                } catch (InterruptedException | ExecutionException ex) {
                                    LOG.log(Level.INFO, "Failed to retrieve documentation data", ex);
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
                                                case "html": documentation.append(content.getValue()); break;
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
    }

    private boolean hasCompletionResolve(CompletionOptions completionOptions) {
        Boolean resolveProvider = completionOptions.getResolveProvider();
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
        List<LSPBindings> servers = LSPBindings.getBindings(file);
        if (servers.isEmpty()) {
            return 0;
        }
        return servers.stream().anyMatch(server -> isTriggerCharacter(server, typedText)) ? COMPLETION_QUERY_TYPE : 0;
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

    static String convertSnippet2CodeTemplate(String snippet) {
        //TODO: error states
        StringBuilder template = new StringBuilder();
        int placeholderIdx = 0;

        for (int i = 0; i < snippet.length(); i++) {
            char c = snippet.charAt(i);
            if (c == '$') {
                c = snippet.charAt(++i);

                boolean hasBody = false;

                if (c == '{') {
                    hasBody = true;
                    c = snippet.charAt(++i);
                }

                if (Character.isLetter(c)) {
                    if (hasBody) {
                        while (c != '}') {
                            c = snippet.charAt(++i);
                        }
                    } else {
                        while (Character.isLetter(c)) {
                            c = snippet.charAt(++i);
                        }
                        i--;
                    }
                    template.append("${P").append(placeholderIdx).append("}");
                } else {
                    int tabStopIndexStart = i;

                    while (Character.isDigit(c)) {
                        if (++i >= snippet.length()) {
                            break;
                        }

                        c = snippet.charAt(i);
                    }

                    String tabStopIndex = snippet.substring(tabStopIndexStart, i);
                    String tabStopContent = "";

                    //TODO: handle variables
                    if (hasBody) {
                        int pos = i;

                        while (c != '}') {
                            c = snippet.charAt(++i);
                        }
                        tabStopContent = snippet.substring(pos, i);
                    } else {
                        i--;
                    }

                    if ("0".equals(tabStopIndex)) {
                        template.append("${cursor}");
                    } else {
                        template.append("${T");
                        template.append(tabStopIndex);
                        //TODO: choices
                        if (tabStopContent.startsWith(":")) {
                            template.append(" default=\"");
                            template.append(tabStopContent.substring(1));
                            template.append("\"");
                        }
                        template.append("}");
                    }
                }
            } else {
                template.append(c);
            }
        }

        return template.toString();
    }

    //for tests:
    static IndirectCompletionItemRenderer COMPLETION_ITEM_RENDERER = new IndirectCompletionItemRenderer() {
        @Override
        public void renderCompletionItem(ImageIcon icon, String leftLabel, String rightLabel, Graphics grphcs, Font font, Color color, int i, int i1, boolean bln) {
            CompletionUtilities.renderHtml(icon, leftLabel, rightLabel, grphcs, font, color, i, i1, bln);
        }
    };

    interface IndirectCompletionItemRenderer {
        public void renderCompletionItem(ImageIcon icon, String leftLabel, String rightLabel, Graphics grphcs, Font font, Color color, int i, int i1, boolean bln);
    }

}
