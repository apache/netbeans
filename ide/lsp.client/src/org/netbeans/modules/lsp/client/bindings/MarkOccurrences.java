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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LSPBindings.BackgroundTask;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public class MarkOccurrences implements BackgroundTask, CaretListener, PropertyChangeListener {

    private static final RequestProcessor WORKER = new RequestProcessor(MarkOccurrences.class.getName(), 1, false, false);
    private final JTextComponent component;
    private Document doc;
    private int caretPos;

    @SuppressWarnings("LeakingThisInConstructor")
    public MarkOccurrences(JTextComponent component) {
        this.component = component;
        doc = component.getDocument();
        Caret caret = component.getCaret();
        caretPos = caret != null ? caret.getDot() : -1;
        component.addCaretListener(this);
        component.addPropertyChangeListener(this);
    }

    @Override
    public void run(LSPBindings bindings, FileObject file) {
        Document localDoc;
        int localCaretPos;
        synchronized (this) {
            localDoc = this.doc;
            localCaretPos = this.caretPos;
        }
        getHighlightsBag(localDoc).setHighlights(computeHighlights(localDoc, localCaretPos));
    }

    private OffsetsBag computeHighlights(Document doc, int caretPos) {
        AttributeSet attr = getColoring(doc);
        OffsetsBag result = new OffsetsBag(doc);
        if(caretPos < 0) {
            return result;
        }
        FileObject file = NbEditorUtilities.getFileObject(doc);
        if (file == null) {
            return result;
        }
        LSPBindings server = LSPBindings.getBindings(file);
        if (server == null) {
            return result;
        }
        if (!Utils.isEnabled(server.getInitResult().getCapabilities().getDocumentHighlightProvider())) {
            return result;
        }
        String uri = Utils.toURI(file);
        try {
            List<? extends DocumentHighlight> highlights
                    = server.getTextDocumentService().documentHighlight(new DocumentHighlightParams(new TextDocumentIdentifier(uri), Utils.createPosition(doc, caretPos))).get();
            if (highlights == null) {
                return result;
            }
            for (DocumentHighlight h : highlights) {
                result.addHighlight(Utils.getOffset(doc, h.getRange().getStart()), Utils.getOffset(doc, h.getRange().getEnd()), attr);
            }
            return result;
        } catch (BadLocationException | InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
            return result;
        }
    }

    private AttributeSet getColoring(Document doc) {
        FontColorSettings fcs = MimeLookup.getLookup(NbEditorUtilities.getMimeType(doc)).lookup(FontColorSettings.class);

        if (fcs == null) {
            //in tests:
            return AttributesUtilities.createImmutable();
        }

        assert fcs != null;

        return fcs.getTokenFontColors("mark-occurrences");
    }

    @Override
    public synchronized void caretUpdate(CaretEvent e) {
        if(e != null) {
            caretPos = e.getDot();
        } else {
            caretPos = -1;
        }
        WORKER.post(() -> {
            FileObject file = NbEditorUtilities.getFileObject(doc);

            if (file != null) {
                LSPBindings.rescheduleBackgroundTask(file, this);
            }
        });
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || "document".equals(evt.getPropertyName())) {
            doc = component.getDocument();
        }
    }

    static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrences.class);

        if (bag == null) {
            bag = new OffsetsBag(doc, false);
            doc.putProperty(MarkOccurrences.class, bag);

            Object stream = doc.getProperty(Document.StreamDescriptionProperty);
            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            };

            doc.addDocumentListener(l);

            if (stream instanceof DataObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "LSP Client MarkOccurrences Highlights Bag", new Object[]{((DataObject) stream).getPrimaryFile(), bag}); //NOI18N
                Logger.getLogger("TIMER").log(Level.FINE, "LSP Client MarkOccurrences Highlights Bag Listener", new Object[]{((DataObject) stream).getPrimaryFile(), l}); //NOI18N
            }
        }

        return bag;
    }

    @MimeRegistration(mimeType = "", service = HighlightsLayerFactory.class)
    public static class HighlightsLayerFactoryImpl implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(HighlightsLayerFactory.Context context) {
            return new HighlightsLayer[]{
                //the mark occurrences layer should be "above" current row and "below" the search layers:
                HighlightsLayer.create(MarkOccurrences.class.getName(), ZOrder.SHOW_OFF_RACK.forPosition(20), true, MarkOccurrences.getHighlightsBag(context.getDocument())),};
        }

    }
}
