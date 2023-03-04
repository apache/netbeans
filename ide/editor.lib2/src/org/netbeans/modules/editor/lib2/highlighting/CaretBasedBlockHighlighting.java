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

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.modules.editor.lib2.RectangularSelectionUtils;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * The layer for highlighting a caret row.
 * 
 * @author Vita Stejskal
 */
public abstract class CaretBasedBlockHighlighting implements HighlightsContainer, ChangeListener, PropertyChangeListener {

    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.CaretBasedBlockHighlighting.level=FINE
    private static final Logger LOG = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
    
    private boolean inited;
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private Caret caret;
    private ChangeListener caretListener;
    
    private final String coloringName;
    private final boolean extendsEOL;
    private final boolean extendsEmptyLine;
    
    private final PositionsBag selectionsBag;
    
    private AttributeSet attribs;
    
    private LookupListener lookupListener;

    /** Creates a new instance of CaretSelectionLayer */
    protected CaretBasedBlockHighlighting(JTextComponent component, String coloringName, boolean extendsEOL, boolean extendsEmptyLine) {
        // Determine the mime type
        String mimeType = BlockHighlighting.getMimeType(component);
        this.mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

        this.coloringName = coloringName;
        this.extendsEOL = extendsEOL;
        this.extendsEmptyLine = extendsEmptyLine;
        
        // Hook up the component
        this.component = component;
        
        selectionsBag = new PositionsBag(component.getDocument(), false);
    }
    
    private void init() {
        this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));

        // Hook up the caret
        this.caret = component.getCaret();
        if (this.caret != null) {
            this.caretListener = WeakListeners.change(this, this.caret);
            this.caret.addChangeListener(caretListener);
        }

        // Calculate the current line position
        updateLineInfo(false);
    }
    
    protected final JTextComponent component() {
        return component;
    }
    
    protected final Caret caret() {
        return caret;
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
        
        return selectionsBag.getHighlights(startOffset, endOffset);
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        selectionsBag.addHighlightsChangeListener(listener);
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        selectionsBag.removeHighlightsChangeListener(listener);
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------
    
    @Override
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
            
            updateLineInfo(true);
        }
    }
    
    // ------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------
    
    @Override
    public void stateChanged(ChangeEvent e) {
        updateLineInfo(true);
    }

    protected abstract PositionsBag getCurrentBlockPositions(Document document);
    
    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------
    
    private final void updateLineInfo(boolean fire) {
        ((AbstractDocument) component.getDocument()).readLock();
        try {
            PositionsBag newPositions = getCurrentBlockPositions(component.getDocument());
            synchronized (this) {
                if (newPositions != null) {
                    selectionsBag.setHighlights(newPositions);
                } else { // newBlock is null => selection removed
                    selectionsBag.clear();
                }
            }
        } finally {
            ((AbstractDocument) component.getDocument()).readUnlock();
        }
    }
    
    protected final AttributeSet getAttribs() {
        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    final Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    setAttrs(result);
                }
            };
            Lookup lookup = MimeLookup.getLookup(mimePath);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            setAttrs(result);
            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }
        return attribs;
    }
        
    /*private*/ void setAttrs(Lookup.Result<FontColorSettings> result) {
        if (Boolean.TRUE.equals(component.getClientProperty("AsTextField"))) {
            if (UIManager.get("TextField.selectionBackground") != null) {
                attribs = AttributesUtilities.createImmutable(
                        StyleConstants.Background, (Color) UIManager.get("TextField.selectionBackground"),
                        StyleConstants.Foreground, (Color) UIManager.get("TextField.selectionForeground"));
            } else {
                final JTextField referenceTextField = (JTextField) new JComboBox<String>().getEditor().getEditorComponent();
                attribs = AttributesUtilities.createImmutable(
                        StyleConstants.Background, referenceTextField.getSelectionColor(),
                        StyleConstants.Foreground, referenceTextField.getSelectedTextColor());
            }
            return;
        }
        FontColorSettings fcs = result.allInstances().iterator().next();
        attribs = fcs.getFontColors(coloringName);
        if (attribs == null) {
            attribs = SimpleAttributeSet.EMPTY;
        } else if (extendsEOL || extendsEmptyLine) {
            attribs = AttributesUtilities.createImmutable(
                    attribs,
                    AttributesUtilities.createImmutable(
                    ATTR_EXTENDS_EOL, Boolean.valueOf(extendsEOL),
                    ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(extendsEmptyLine)));
        }
    }
    
    private static String positionToString(Position p) {
        return p == null ? "null" : p.toString(); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
    
    public static final class CaretRowHighlighting extends CaretBasedBlockHighlighting {
        
        public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.CaretRowHighlighting"; //NOI18N
        
        public CaretRowHighlighting(JTextComponent component) {
            super(component, FontColorNames.CARET_ROW_COLORING, true, false);
        }
        
        @Override
        protected PositionsBag getCurrentBlockPositions(Document document) {
            Caret caret = caret();
            PositionsBag selections = null;
            if (document != null && caret != null) {
                selections = new PositionsBag(document);
                if(caret instanceof EditorCaret) {
                    EditorCaret editorCaret = (EditorCaret) caret;
                    List<CaretInfo> carets = editorCaret.getCarets();
                    for (CaretInfo c : carets) {
                        int caretOffset = c.getDot();
                        try {
                            int startOffset = DocUtils.getRowStart(document, caretOffset, 0);
                            int endOffset = DocUtils.getRowEnd(document, caretOffset);
                            // include the new-line character or the end of the document
                            endOffset++;
                            Position startPos = document.createPosition(startOffset);
                            Position endPos = document.createPosition(endOffset);
                            selections.addHighlight(startPos, endPos, getAttribs());
                        } catch(BadLocationException e) {
                            LOG.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                } else {
                    int caretOffset = caret.getDot();
                    try {
                        int startOffset = DocUtils.getRowStart(document, caretOffset, 0);
                        int endOffset = DocUtils.getRowEnd(document, caretOffset);

                        // include the new-line character or the end of the document
                        endOffset++;
                        Position startPos = document.createPosition(startOffset);
                        Position endPos = document.createPosition(endOffset);
                        selections.addHighlight(startPos, endPos, getAttribs());
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            return selections;
        }
    } // End of CaretRowHighlighting class
    
    public static final class TextSelectionHighlighting extends CaretBasedBlockHighlighting
    implements HighlightsChangeListener {
        
        public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.TextSelectionHighlighting"; //NOI18N
        
        private int hlChangeStartOffset = -1;
        
        private int hlChangeEndOffset;
        
        private PositionsBag rectangularSelectionBag;

        public TextSelectionHighlighting(JTextComponent component) {
            super(component, FontColorNames.SELECTION_COLORING, true, true);
        }
    
        @Override
        protected PositionsBag getCurrentBlockPositions(Document document) {
            Caret caret = caret();
            PositionsBag selections = null;
            if (document != null && caret != null) {
                selections = new PositionsBag(document);
                if(caret instanceof EditorCaret) {
                    EditorCaret editorCaret = (EditorCaret) caret;
                    for (CaretInfo caretInfo : editorCaret.getCarets()) {
                        int caretOffset = caretInfo.getDot();
                        int markOffset = caretInfo.getMark();

                        if (caretOffset != markOffset) {
                            try {
                                Position start = document.createPosition(Math.min(caretOffset, markOffset));
                                Position end = document.createPosition(Math.max(caretOffset, markOffset));
                                selections.addHighlight(start, end, getAttribs());
                            } catch (BadLocationException e) {
                                LOG.log(Level.WARNING, e.getMessage(), e);
                            }
                        }
                    }
                } else {
                    int caretOffset = caret.getDot();
                    int markOffset = caret.getMark();

                    if (caretOffset != markOffset) {
                        try {
                            Position start = document.createPosition(Math.min(caretOffset, markOffset));
                            Position end = document.createPosition(Math.max(caretOffset, markOffset));
                            selections.addHighlight(start, end, getAttribs());
                        } catch (BadLocationException e) {
                            LOG.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                }
            }
            
            return selections;
        }

        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            if (!RectangularSelectionUtils.isRectangularSelection(component())) { // regular selection
                return super.getHighlights(startOffset, endOffset);
            } else { // rectangular selection
                return (rectangularSelectionBag != null)
                        ? (rectangularSelectionBag.getHighlights(startOffset, endOffset))
                        : HighlightsSequence.EMPTY;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);
            if (RectangularSelectionUtils.getRectangularSelectionProperty().equals(evt.getPropertyName())) {
//                fireHighlightsChange(0, component().getDocument().getLength());
            }
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            super.stateChanged(evt);
            Document doc;
            JTextComponent c = component();
            if (RectangularSelectionUtils.isRectangularSelection(c) && (doc = c.getDocument()) != null) {
                if (rectangularSelectionBag == null) {
                    // Btw the document is not used by PositionsBag at all
                    rectangularSelectionBag = new PositionsBag(doc);
                    rectangularSelectionBag.addHighlightsChangeListener(this);
                }
                List<Position> regions = RectangularSelectionUtils.regionsCopy(c);
                if (regions != null) {
                    AttributeSet attrs = getAttribs();
                    rectangularSelectionBag.clear();
                    int size = regions.size();
                    for (int i = 0; i < size;) {
                        Position startPos = regions.get(i++);
                        Position endPos = regions.get(i++);
                        rectangularSelectionBag.addHighlight(startPos, endPos, attrs);
                    }
                    // Fire change at once
                    if (hlChangeStartOffset != -1) {
//                        fireHighlightsChange(hlChangeStartOffset, hlChangeEndOffset);
                        hlChangeStartOffset = -1;
                    }
                }
            }
        }

        @Override
        public void highlightChanged(HighlightsChangeEvent evt) {
            if (hlChangeStartOffset == -1) {
                hlChangeStartOffset = evt.getStartOffset();
                hlChangeEndOffset = evt.getEndOffset();
            } else {
                hlChangeStartOffset = Math.min(hlChangeStartOffset, evt.getStartOffset());
                hlChangeEndOffset = Math.max(hlChangeEndOffset, evt.getEndOffset());
            }
        }
        
    } // End of TextSelectionHighlighting class
}
