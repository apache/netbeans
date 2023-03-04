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
package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.caret.EditorCaretEvent;
import org.netbeans.api.editor.caret.EditorCaretListener;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.openide.util.WeakListeners;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * Used by EditorCaret to color a single character under the block caret(s) with inverse colors in
 * overwrite mode when the caret blinking timer ticks.
 *
 * @author Miloslav Metelka
 */
public final class CaretOverwriteModeHighlighting implements ReleasableHighlightsContainer, PropertyChangeListener, EditorCaretListener {

    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.CaretOverwriteModeHighlighting"; //NOI18N

    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.CaretOverwriteModeHighlighting.level=FINE
    private static final Logger LOG = Logger.getLogger(CaretOverwriteModeHighlighting.class.getName());
    
    private final JTextComponent component;
    
    private boolean inited;
    
    private boolean visible;

    private EditorCaret editorCaret;

    private EditorCaretListener weakEditorCaretListener;
    
    private ListenerList<HighlightsChangeListener> listenerList = new ListenerList<>();
    
    private AttributeSet coloringAttrs;
    
    private CharSequence docText;
    
    private List<CaretInfo> sortedCarets;
    
    /** Creates a new instance of CaretSelectionLayer */
    public CaretOverwriteModeHighlighting(JTextComponent component) {
        this.component = component;
        component.putClientProperty(CaretOverwriteModeHighlighting.class, this);
    }
    
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            this.visible = visible;
            if (editorCaret != null) {
                List<CaretInfo> sortedCaretsL;
                if (visible) {
                    sortedCaretsL = editorCaret.getSortedCarets();
                    synchronized (this) {
                        sortedCarets = sortedCaretsL;
                    }
                } else {
                    synchronized (this) {
                        sortedCaretsL = sortedCarets;
                        sortedCarets = null;
                    }
                }
                if (sortedCaretsL != null) {
                    int changeStartOffset = sortedCaretsL.get(0).getDot();
                    int changeEndOffset = sortedCaretsL.get(sortedCaretsL.size() - 1).getDot() + 1;
                    fireHighlightsChange(changeStartOffset, changeEndOffset);
                }
            }
        }
    }
    
    private void init() {
        component.addPropertyChangeListener(WeakListeners.propertyChange(this, component));
        updateActiveCaret();
        updateColoring();
    }
    
    // ------------------------------------------------
    // AbstractHighlightsContainer implementation
    // ------------------------------------------------
    
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (!inited) {
            inited = true;
            init();
        }
        HighlightsSequence hs;
        boolean visibleL;
        List<CaretInfo> sortedCaretsL;
        synchronized (this) {
            visibleL = visible;
            sortedCaretsL = sortedCarets;
        }
        if (editorCaret != null && visibleL) {
            hs = new HS(sortedCaretsL, startOffset, endOffset);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("CaretOverwriteModeHighlighting.getHighlights() <" + startOffset + "," + endOffset + ">\n");
            }
        } else {
            hs = HighlightsSequence.EMPTY;
        }
        return hs;
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        listenerList.remove(listener);
    }
    
    private void fireHighlightsChange(HighlightsChangeEvent evt) {
        for (HighlightsChangeListener listener : listenerList.getListeners()) {
            listener.highlightChanged(evt);
        }
    }

    private void fireHighlightsChange(int startOffset, int endOffset) {
        fireHighlightsChange(new HighlightsChangeEvent(this, startOffset, endOffset));
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName == null || "caret".equals(propName)) { //NOI18N
            updateActiveCaret();
        } else if ("document".equals(propName)) {
            updateDocText();
        } else if ("caretColor".equals(propName) || "background".equals(propName)) {
            updateColoring();
        }
    }
    
    @Override
    public void caretChanged(EditorCaretEvent evt) {
        if (visible) {
            fireHighlightsChange(evt.getAffectedStartOffset(), evt.getAffectedEndOffset());
            List<CaretInfo> sortedCaretsL;
            sortedCaretsL = editorCaret.getSortedCarets();
            synchronized (this) {
                sortedCarets = sortedCaretsL;
            }
        }
    }

    @Override
    public void released() {
        component.removePropertyChangeListener(this);
    }
    
    private void updateActiveCaret() {
        if (visible) {
            setVisible(false);
        }
        if (editorCaret != null) {
            editorCaret.removeEditorCaretListener(weakEditorCaretListener);
            weakEditorCaretListener = null;
            editorCaret = null;
            docText = null;
        }

        Caret caret = component.getCaret();
        if (caret instanceof EditorCaret) { // Only work for editor caret
            editorCaret = (EditorCaret) caret;
        }

        if (editorCaret != null) {
            updateDocText();
            weakEditorCaretListener = WeakListeners.create(EditorCaretListener.class, this, editorCaret);
            editorCaret.addEditorCaretListener(weakEditorCaretListener);
        }
    }

    private void updateColoring() {
        coloringAttrs = AttributesUtilities.createImmutable(
                StyleConstants.Background, component.getCaretColor(),
                StyleConstants.Foreground, component.getBackground());
    }
    
    private void updateDocText() {
        JTextComponent c = component;
        CharSequence text = null;
        if (c != null) {
            Document doc = c.getDocument();
            if (doc != null) {
                text = DocumentUtilities.getText(doc);
            }
        }
        docText = text;
    }
    
    private final class HS implements SplitOffsetHighlightsSequence {
        
        private final List<CaretInfo> sortedCarets;
        
        private final int startOffset;
        
        private final int endOffset;

        private int caretOffset = -1;
        
        private int caretSplitOffset;

        private int caretIndex;
        
        HS(List<CaretInfo> sortedCarets, int startOffset, int endOffset) {
            this.sortedCarets = sortedCarets;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public boolean moveNext() {
            boolean ret = false;
            if (caretOffset == -1) { // Return first highlight
                while (caretOffset < startOffset && caretIndex < sortedCarets.size()) {
                    CaretInfo caret = sortedCarets.get(caretIndex++);
                    caretOffset = caret.getDot();
                }
                if (caretOffset != -1) {
                    ret = true;
                }
            } else {
                while (caretIndex < sortedCarets.size()) {
                    CaretInfo caret = sortedCarets.get(caretIndex++);
                    int offset = caret.getDot();
                    // Check for case if sorted carets would not be truly sorted or there would be duplicates
                    if (offset > caretOffset) {
                        caretOffset = offset;
                        checkLogHighlight();
                        return true;
                    }
                    if (offset >= endOffset) {
                        return false;
                    }
                }
            }
            if (ret) {
                caretSplitOffset = 0;
                CharSequence text = docText;
                if (text != null) {
                    char ch = text.charAt(caretOffset);
                    if (ch == '\t' || ch == '\n') {
                        caretSplitOffset = 1;
                    }
                }
                checkLogHighlight();
                return true;
            }
            return false;
        }
        
        private void checkLogHighlight() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("  CaretOverwriteModeHighlighting.Highlight <" + getStartOffset() + "_" +
                        getStartSplitOffset() + "," + getEndOffset() + "_" + getEndSplitOffset() + ">\n");
            }
        }

        @Override
        public int getStartOffset() {
            return caretOffset;
        }

        @Override
        public int getStartSplitOffset() {
            return 0;
        }

        @Override
        public int getEndOffset() {
            // Either cover whole char or just a single "virtual" char for newlines and tabs
            return (caretSplitOffset == 0) ? caretOffset + 1 : caretOffset;
        }

        @Override
        public int getEndSplitOffset() {
            return caretSplitOffset;
        }
        
        @Override
        public AttributeSet getAttributes() {
            return coloringAttrs;
        }

    }
}
