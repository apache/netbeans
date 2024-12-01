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

package org.netbeans.modules.csl.editor.semantic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * A highlight layer for semantic highlighting OffsetRange lists provided by language clients
 * 
 * @author Tor Norbye
 */
public final class GsfSemanticLayer extends AbstractHighlightsContainer implements DocumentListener {
    
    // -J-Dorg.netbeans.modules.csl.editor.semantic.GsfSemanticLayer.level=FINE
    private static final Logger LOG = Logger.getLogger(GsfSemanticLayer.class.getName());

    private List<SequenceElement> colorings = List.of();
    private List<Edit> edits;
    private final Map<Language,Map<Coloring, AttributeSet>> cache = new HashMap<>();
    private final Document doc;
    private final List<Lookup.Result> coloringResults = new ArrayList<>(3);
    private final List<LookupListener> coloringListeners = new ArrayList<>(3);

    public static GsfSemanticLayer getLayer(Class id, Document doc) {
        GsfSemanticLayer l = (GsfSemanticLayer) doc.getProperty(id);
        
        if (l == null) {
            doc.putProperty(id, l = new GsfSemanticLayer(doc));
        }
        
        return l;
    }
    
    private GsfSemanticLayer(Document doc) {
        this.doc = doc;
    }
    
    void setColorings(final SortedSet<SequenceElement> colorings) {
        doc.render(() -> {
            synchronized (GsfSemanticLayer.this) {
                GsfSemanticLayer.this.colorings = List.copyOf(colorings);
                GsfSemanticLayer.this.edits = new ArrayList<>();

                fireHighlightsChange(0, doc.getLength()); //XXX: locking
                
                DocumentUtilities.removeDocumentListener(doc, GsfSemanticLayer.this, DocumentListenerPriority.LEXER);
                DocumentUtilities.addDocumentListener(doc, GsfSemanticLayer.this, DocumentListenerPriority.LEXER);
            }
        });
    }
    
    @Override
    public synchronized HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (colorings.isEmpty()) {
            return HighlightsSequence.EMPTY;
        }
        int seqStart = firstSequenceElement(colorings, startOffset);
        return new GsfHighlightSequence(colorings.listIterator(seqStart));
    }

    public synchronized void clearColoringCache() {
        cache.clear();
    }
    
    private synchronized void clearLanguageColoring(Language mime) {
        cache.remove(mime);
        
    }
    
    synchronized AttributeSet getColoring(Coloring coloring, final Language language) {
        Map<Coloring,AttributeSet> langColoring = cache.computeIfAbsent(language, (lang) -> {
            registerColoringChangeListener(lang);
            return new HashMap<>();
        });
        return langColoring.computeIfAbsent(coloring, (c) -> language.getColoringManager().getColoringImpl(c));
    }

    private void registerColoringChangeListener(Language language) {
        String mime = language.getMimeType();
        Lookup.Result<FontColorSettings> res = MimeLookup.getLookup(MimePath.get(mime)).lookupResult(FontColorSettings.class);
        coloringResults.add(res);
        LookupListener l = (LookupEvent ev) -> {
            clearLanguageColoring(language);
            fireHighlightsChange(0, doc.getLength());
        };

        res.addLookupListener(
                WeakListeners.create(LookupListener.class, l , res)
        );
        coloringListeners.add(l);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        synchronized (GsfSemanticLayer.this) {
            edits.add(new Edit(e.getOffset(), e.getLength(), true));
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        synchronized (GsfSemanticLayer.this) {
            edits.add(new Edit(e.getOffset(), e.getLength(), false));
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
    }
    
    // Compute an adjusted offset
    public int getShiftedPos(int pos) {
        List<Edit> list = edits;
        int len = list.size();
        if (len == 0) {
            return pos;
        }
        for (int i = 0; i < len; i++) {
            Edit edit = list.get(i);
            if (pos > edit.offset) {
                if (edit.insert) {
                    pos += edit.len;
                } else if (pos < edit.offset+edit.len) {
                    pos = edit.offset;
                } else {
                    pos -= edit.len;
                }
            }
        }
        
        if (pos < 0) {
            pos = 0;
        }

        return pos;
    }
    
    /**
     * An Edit is a modification (insert/remove) we've been notified about from the document
     * since the last time we updated our "colorings" object.
     * The list of Edits lets me quickly compute the current position of an original
     * position in the "colorings" object. This is typically going to involve only a couple
     * of edits (since the colorings object is updated as soon as the user stops typing).
     * This is probably going to be more efficient than updating all the colorings offsets
     * every time the document is updated, since the colorings object can contain thousands
     * of ranges (e.g. for every highlight in the whole document) whereas asking for the
     * current positions is typically only done for the highlights visible on the screen.
     */
    private record Edit(int offset, int len, boolean insert) {}
    
    /**
     * An implementation of a HighlightsSequence which can show OffsetRange
     * sections and keep them up to date during edits.
     *
     * @author Tor Norbye
     */
    private final class GsfHighlightSequence implements HighlightsSequence {
        private final Iterator<SequenceElement> iterator;
        private SequenceElement element;

        GsfHighlightSequence(Iterator<SequenceElement> it) {
            iterator = it;
        }

        @Override
        public boolean moveNext() {
            element = iterator.hasNext() ? iterator.next() : null;
            return element != null;
        }

        @Override
        public int getStartOffset() {
            return getShiftedPos(element.range().getStart());
        }

        @Override
        public int getEndOffset() {
            return getShiftedPos(element.range().getEnd());
        }

        @Override
        public AttributeSet getAttributes() {
            return getColoring(element.coloring(), element.language());
        }
    }

    private static <T> int firstSequenceElement(List<SequenceElement> l, int offset) {
        int low = 0;
        int high = l.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            SequenceElement midVal = l.get(mid);
            int cmp = midVal.range().getStart() - offset;

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return low;
    }

}
