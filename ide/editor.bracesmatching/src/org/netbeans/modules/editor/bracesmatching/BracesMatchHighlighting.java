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
package org.netbeans.modules.editor.bracesmatching;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.ZOrder;

/**
 *
 * @author Vita Stejskal
 */
public class BracesMatchHighlighting extends AbstractHighlightsContainer
        implements ReleasableHighlightsContainer, ChangeListener, PropertyChangeListener, HighlightsChangeListener, DocumentListener
{
    private static final Logger LOG = Logger.getLogger(BracesMatchHighlighting.class.getName());

    private static final String BRACES_MATCH_COLORING = "nbeditor-bracesMatching-match"; //NOI18N
    private static final String BRACES_MISMATCH_COLORING = "nbeditor-bracesMatching-mismatch"; //NOI18N

    private static final String BRACES_MATCH_MULTICHAR_COLORING = "nbeditor-bracesMatching-match-multichar"; //NOI18N
    private static final String BRACES_MISMATCH_MULTICHAR_COLORING = "nbeditor-bracesMatching-mismatch-multichar"; //NOI18N

    private final JTextComponent component;
    private final Document document;

    private Caret caret = null;
    private ChangeListener caretListener;

    private final OffsetsBag bag;
    private final AttributeSet bracesMatchColoring;
    private final AttributeSet bracesMismatchColoring;
    private final AttributeSet bracesMatchMulticharColoring;
    private final AttributeSet bracesMismatchMulticharColoring;

    private boolean released;

    public BracesMatchHighlighting(JTextComponent component, Document document) {
        this.document = document;

        String mimeType = getMimeType(component);
        MimePath mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

        // Load the colorings
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet match = fcs.getFontColors(BRACES_MATCH_COLORING);
        AttributeSet mismatch = fcs.getFontColors(BRACES_MISMATCH_COLORING);
        AttributeSet matchMultichar = fcs.getFontColors(BRACES_MATCH_MULTICHAR_COLORING);
        AttributeSet mismatchMultichar = fcs.getFontColors(BRACES_MISMATCH_MULTICHAR_COLORING);
        this.bracesMatchColoring = match != null ? match : SimpleAttributeSet.EMPTY;
        this.bracesMismatchColoring = mismatch != null ? mismatch : SimpleAttributeSet.EMPTY;
        this.bracesMatchMulticharColoring = matchMultichar != null ? matchMultichar : SimpleAttributeSet.EMPTY;
        this.bracesMismatchMulticharColoring = mismatchMultichar != null ? mismatchMultichar : SimpleAttributeSet.EMPTY;

        // Create and hook up the highlights bag
        this.bag = new OffsetsBag(document, true);
        this.bag.addHighlightsChangeListener(this);

        // Hook up the component
        this.component = component;
        this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));

        // Hook up the caret
        this.caret = component.getCaret();
        if (this.caret != null) {
            this.caretListener = WeakListeners.change(this, this.caret);
            this.caret.addChangeListener(caretListener);
        }

        // Refresh the layer
        refresh();
    }

    // ------------------------------------------------
    // AbstractHighlightsContainer implementation
    // ------------------------------------------------

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    // ------------------------------------------------
    // HighlightsChangeListener implementation
    // ------------------------------------------------

    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
// XXX: not neccessary
//        final int startOffset = event.getStartOffset();
//        final int endOffset = event.getEndOffset();
//
//        SwingUtilities.invokeLater(new Runnable() {
//            private boolean inDocumentRender = false;
//            public void run() {
//                if (inDocumentRender) {
//                    fireHighlightsChange(startOffset, endOffset);
//                } else {
//                    inDocumentRender = true;
//                    document.render(this);
//                }
//            }
//        });
    }

    // ------------------------------------------------
    // DocumentListener implementation
    // ------------------------------------------------

    public void insertUpdate(DocumentEvent e) {
        refresh();
    }

    public void removeUpdate(DocumentEvent e) {
        refresh();
    }

    public void changedUpdate(DocumentEvent e) {
        refresh();
    }

    // ------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------

    public void stateChanged(ChangeEvent e) {
        refresh();
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || "caret".equals(evt.getPropertyName())) { //NOI18N
            if (caret != null) {
                caret.removeChangeListener(caretListener);
                caretListener = null;
            }

            caret = component.getCaret();

            if (caret != null) {
                caretListener = WeakListeners.change(this, caret);
                caret.addChangeListener(caretListener);
            }

            refresh();
        } else if (MasterMatcher.PROP_SEARCH_DIRECTION.equals(evt.getPropertyName()) ||
                MasterMatcher.PROP_CARET_BIAS.equals(evt.getPropertyName()) ||
                MasterMatcher.PROP_MAX_BACKWARD_LOOKAHEAD.equals(evt.getPropertyName()) ||
                MasterMatcher.PROP_MAX_FORWARD_LOOKAHEAD.equals(evt.getPropertyName())
        ) {
            refresh();
        }
    }

    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------

    private void refresh() {
        if (released) {
            return; // No longer notify the matcher since it would leak to memory leak of MasterMatcher.lastResult
        }

        Caret c = this.caret;
        if (c == null) {
            bag.clear();
        } else {
            MasterMatcher.get(component).highlight(
                    document,
                    c.getDot(),
                    bag,
                    bracesMatchColoring,
                    bracesMismatchColoring,
                    bracesMatchMulticharColoring,
                    bracesMismatchMulticharColoring
            );
        }
    }

    private static String getMimeType(JTextComponent component) {
        Document doc = component.getDocument();
        String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        if (mimeType == null) {
            EditorKit kit = component.getUI().getEditorKit(component);
            if (kit != null) {
                mimeType = kit.getContentType();
            }
        }
        return mimeType;
    }

    @Override
    public void released() {
        released = true;
//        component.removePropertyChangeListener(this);
//        bag.removeHighlightsChangeListener(this);
//        document.removeDocumentListener(this);
    }

    public static final class Factory implements HighlightsLayerFactory {
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer [] {
                    HighlightsLayer.create(
                            "org-netbeans-modules-editor-bracesmatching-BracesMatchHighlighting", //NOI18N
                            ZOrder.SHOW_OFF_RACK.forPosition(400),
                            true,
                            new BracesMatchHighlighting(context.getComponent(), context.getDocument())
                    )
            };
        }
    } // End of Factory class
}