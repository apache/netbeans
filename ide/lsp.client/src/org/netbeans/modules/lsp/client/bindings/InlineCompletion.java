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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.netbeans.api.editor.*;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.editor.BaseKit.InsertTabAction;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.lsp.client.EnhancedTextDocumentService;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

//TODO: shutdown
public class InlineCompletion {
    private static final RequestProcessor WORKER = new RequestProcessor(InlineCompletion.class.getName(), 1, false, false);

    @OnStart
    public static class Start implements Runnable {

        @Override
        public void run() {
            EditorRegistry.addPropertyChangeListener(new EditorListener());
        }
        
    }

    private static class EditorListener implements PropertyChangeListener, CaretListener {

        public EditorListener() {
        }

        private final Set<JTextComponent> openComponents = new HashSet<>();
        private JTextComponent lastComponent;
        private AtomicReference<FileObject> currentFile = new AtomicReference<>();
        private AtomicReference<Document> currentDocument = new AtomicReference<>();
        private AtomicInteger currentCaretPos = new AtomicInteger();
        private final Task query = WORKER.create(this::doQuery);

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JTextComponent c = EditorRegistry.lastFocusedComponent();
            if (lastComponent != c) {
                if (lastComponent != null) {
                    lastComponent.removeCaretListener(this);
                }
                lastComponent = c;
                if (c != null) {
                    FileObject file = NbEditorUtilities.getFileObject(c.getDocument());
                    currentFile.set(file);
                    currentDocument.set(c.getDocument());
                    currentCaretPos.set(-1);
                    boolean wasOpen = openComponents.contains(c);
                    openComponents.clear();
                    openComponents.addAll(EditorRegistry.componentList());
                    c.addCaretListener(this);
                } else {
                    currentFile.set(null);
                    currentDocument.set(null);
                }
            }
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            currentCaretPos.set(e.getDot());
            scheduleQuery();
        }

        private void scheduleQuery() {
            query.schedule(500);
        }

        private void doQuery() {
            //TODO: only if the rest of the line is empty??
            FileObject file = currentFile.get();
            Document doc = currentDocument.get();
            int caretPos = currentCaretPos.get();

            if (file != null && doc != null && caretPos != (-1)) {
                ProposalItem existingProposal = ProposalItem.get(doc);
                long thisVersion = DocumentUtilities.getDocumentVersion(doc);
                if (existingProposal != null) {
                    //TODO: tracking typing modifications:
                    if (existingProposal.documentVersion == thisVersion &&
                        caretPos == existingProposal.location.getOffset()) {
                        //skip
                        return ;
                    }
                    ProposalItem.clearProposal(doc);
                }

                LSPBindings bindings = LSPBindings.getBindings(file);

                if (bindings != null) {
                    //TODO: check if the server has inline completion
                    boolean hasInlineCompletion = true;
                    if (hasInlineCompletion) {
                        ProgressHandle handle = ProgressHandle.createHandle("Running inline completion."); //TODO: progress should be handle by the server/protocol, forcing a progress here seems very intrusive
                        boolean proposalFound = false;
                        try {
                            handle.start();
                            //TODO: cancel
                            CompletableFuture<EnhancedTextDocumentService.InlineCompletionItem[]> futureProposals = bindings.getTextDocumentService().inlineCompletion(new EnhancedTextDocumentService.InlineCompletionParams(new TextDocumentIdentifier(Utils.toURI(file)), Utils.createPosition(doc, caretPos), null));
                            EnhancedTextDocumentService.InlineCompletionItem[] proposals = futureProposals.get();
                            if (proposals != null && proposals.length > 0 && thisVersion == DocumentUtilities.getDocumentVersion(doc)) {
                                //TODO: more proper re-indent possible?
                                int indent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(doc, caretPos));
                                String proposalText = proposals[0].getInsertText().replaceAll("\n", "\n" + IndentUtils.createIndentString(doc, indent));
                                ProposalItem.putProposal(bindings, doc, caretPos, proposalText);
                                proposalFound = true;
                            }
                        } catch (BadLocationException | ExecutionException | InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            if (!proposalFound) {
                                ProposalItem.clearProposal(doc);
                            }
                            handle.finish();
                        }
                    }
                }
            }
        }
    }

    private static class ProposalItem implements DocumentListener {
        private final LSPBindings bindings;
        public final Position location;
        public final long documentVersion;
        public final String text;

        private ProposalItem(LSPBindings bindings, Position location, long documentVersion, String text) {
            this.bindings = bindings;
            this.location = location;
            this.documentVersion = documentVersion;
            this.text = text;
        }

        public static void putProposal(LSPBindings bindings, Document doc, int pos, String text) {
            doc.render(() -> {
                try {
                    LineDocument ldoc = LineDocumentUtils.asRequired(doc, LineDocument.class);
                    //TODO: should take the version at the time the proposal was triggered?
                    ProposalItem i = new ProposalItem(bindings, ldoc.createPosition(pos, /*XXX*/Position.Bias.Forward), DocumentUtilities.getDocumentVersion(doc), text);
                    doc.putProperty(ProposalItem.class, i);
                    i.setShadowText(doc, pos);
                    doc.addDocumentListener(i);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }

        public static void clearProposal(Document doc) {
            ProposalItem i = (ProposalItem) doc.getProperty(ProposalItem.class);
            doc.putProperty(ProposalItem.class, null);
            if (i != null) {
                i.clearShadowText(doc);
                doc.removeDocumentListener(i);
            }
        }

        public static ProposalItem get(Document doc) {
            return (ProposalItem) doc.getProperty(ProposalItem.class);
        }

        private void setShadowText(Document doc, int caretPos) {
            OffsetsBag bag = new OffsetsBag(doc);
            int common = caretPos - location.getOffset();
            try {
                if (common < 0 || !text.startsWith(doc.getText(location.getOffset(), common))) {
                    clearProposal(doc);
                    return ;
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return ;
            }
            String reducedText = text.substring(common);
            String[] firstLineAndTheRest = reducedText.split("\n", 2);

            bag.addHighlight(caretPos, caretPos + 1, AttributesUtilities.createImmutable("shadow-text-prepend", firstLineAndTheRest[0]));

            if (firstLineAndTheRest.length > 1) {
                bag.addHighlight(caretPos + 1, caretPos + 2, AttributesUtilities.createImmutable("vertical-shadow-text-prepend", firstLineAndTheRest[1]));
            }

            getShadowTextBag(doc).setHighlights(bag);
        }

        private void clearShadowText(Document doc) {
            OffsetsBag bag = new OffsetsBag(doc);

            getShadowTextBag(doc).setHighlights(bag);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            ProposalItem i = get(doc);

            if (i == null) {
                return ;
            }

            if (!DocumentUtilities.isTypingModification(doc)) {
                clearProposal(doc);
            }

            i.setShadowText(doc, e.getOffset());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            Document doc = e.getDocument();
            ProposalItem i = get(doc);

            if (i == null) {
                return ;
            }

            if (!DocumentUtilities.isTypingModification(doc)) {
                clearProposal(doc);
            }

            i.setShadowText(doc, e.getOffset());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }

    }

    private static final Object KEY_SHADOW_TEXT = new Object();
    static OffsetsBag getShadowTextBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(KEY_SHADOW_TEXT);
        
        if (bag == null) {
            doc.putProperty(KEY_SHADOW_TEXT, bag = new OffsetsBag(doc));
        }
        
        return bag;
    }

    @MimeRegistration(mimeType="text/x-java", service=HighlightsLayerFactory.class)
    public static class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {

        public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(InlineCompletion.class.getName(), ZOrder.SYNTAX_RACK.forPosition(1700), false, getShadowTextBag(context.getDocument())),
            };
        }

    }

    @EditorActionRegistration(name = "lsp-inline-completion-proposal-accept")
    @Messages("lsp-inline-completion-proposal-accept=Inline Completion Proposal Accept")
    public static class ConfimAction extends AbstractEditorAction {

        private final InsertTabAction delegate = new InsertTabAction();

        @Override
        protected void actionPerformed(ActionEvent evt, final JTextComponent target) {
            if (target != null) {
                ProposalItem i = ProposalItem.get(target.getDocument());

                if (i != null) {
                    int caret = target.getCaret().getDot();
                    Document doc = target.getDocument();

                    try {
                        doc.insertString(caret, i.text, null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    ProposalItem.clearProposal(doc);
                } else {
                    //XXx:
                    delegate.actionPerformed(evt, target);
                }
            }
        }

    }
}
