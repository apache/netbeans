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
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
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
public final class GsfSemanticLayer extends AbstractHighlightsContainer {
    
    private List<SequenceElement> colorings = List.of();
    private final Map<Language,Map<Coloring, AttributeSet>> cache = new HashMap<>();
    private final Document doc;

    // Write only - used to keep strong reference to Lookup.Result/LookupListener
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Lookup.Result> coloringResults = new ArrayList<>(3);
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<LookupListener> coloringListeners = new ArrayList<>(3);

    public static GsfSemanticLayer getLayer(Class id, Document doc) {
        GsfSemanticLayer l = (GsfSemanticLayer) doc.getProperty(id);
        
        if (l == null) {
            l = new GsfSemanticLayer(doc);
            doc.putProperty(id, l);
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

                fireHighlightsChange(0, doc.getLength()); //XXX: locking
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
            while (iterator.hasNext()) {
                SequenceElement i = iterator.next();
                // Skip empty highlights, the editor can handle them, though not happy about it
                // this could happen on deleting large portion of code
                if (i.start().getOffset() != i.end().getOffset()) {
                    element = i;
                    return true;
                }
            }
            element = null;
            return false;
        }

        @Override
        public int getStartOffset() {
            return element.start().getOffset();
        }

        @Override
        public int getEndOffset() {
            return element.end().getOffset();
        }

        @Override
        public AttributeSet getAttributes() {
            return getColoring(element.coloring(), element.language());
        }
    }

    /**
     * Binary search for the first SequenceElement that is left to the offset and
     * returns its index in the list.
     *
     * This is used to get an optimized iterator for GsfHighlightSequence
     * @param l ordered list of SequenceElements
     * @param offset The offset in the document.
     * @return the index of the first SequenceElement that is left to the offset
     */
    static int firstSequenceElement(List<SequenceElement> l, int offset) {
        int low = 0;
        int high = l.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            SequenceElement midVal = l.get(mid);
            int cmp = midVal.start().getOffset() - offset;

            if (cmp == 0) {
                return mid;
            } else if (low == high) {
                if (mid > 0 && cmp >= 0) {
                    return mid - 1;
                } else {
                    return low;
                }
            } else if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            }
        }
        return low;
    }

}
