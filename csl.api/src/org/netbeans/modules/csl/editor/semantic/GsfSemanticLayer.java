/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.csl.editor.semantic;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
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
public class GsfSemanticLayer extends AbstractHighlightsContainer implements DocumentListener {
    
    // -J-Dorg.netbeans.modules.csl.editor.semantic.GsfSemanticLayer.level=FINE
    private static final Logger LOG = Logger.getLogger(GsfSemanticLayer.class.getName());

    //private Map<Token, Coloring> colorings;
    private SortedSet<SequenceElement> colorings;
    private int version;
    private List<Edit> edits;
    private Map<Language,Map<Coloring, AttributeSet>> CACHE = new HashMap<Language,Map<Coloring, AttributeSet>>();
    private Document doc;
    private List<Lookup.Result> coloringResults = new ArrayList<Lookup.Result>(3);
    private List<LookupListener> coloringListeners = new ArrayList<LookupListener>(3);

    public static GsfSemanticLayer getLayer(Class id, Document doc) {
        GsfSemanticLayer l = (GsfSemanticLayer) doc.getProperty(id);
        
        if (l == null) {
            doc.putProperty(id, l = new GsfSemanticLayer(doc));
        }
        
        return l;
    }
    
    private static final SortedSet<SequenceElement> EMPTY_TREE_SET = new TreeSet<SequenceElement>();
    
    private GsfSemanticLayer(Document doc) {
        this.doc = doc;
        this.colorings = EMPTY_TREE_SET;
        this.version = -1;
    }
    
    //public void setColorings(final SortedMap<OffsetRange, Coloring> colorings/*, final Set<OffsetRange> addedTokens, final Set<OffsetRange> removedTokens*/) {
    void setColorings(final SortedSet<SequenceElement> colorings, final int version /*, final Set<OffsetRange> addedTokens, final Set<OffsetRange> removedTokens*/) {
        doc.render(new Runnable() {
            public @Override void run() {
                synchronized (GsfSemanticLayer.this) {
                    GsfSemanticLayer.this.colorings = colorings;
                    GsfSemanticLayer.this.edits = new ArrayList<Edit>();
                    GsfSemanticLayer.this.version = version;
                    
                    // I am not accurately computing it here
                    //if (addedTokens.isEmpty()) {
                    //    //need to fire anything here?
                    //} else {
                    //    if (addedTokens.size() == 1) {
                    //        OffsetRange t = addedTokens.iterator().next();
                    //
                    //        //fireHighlightsChange(t.offset(null), t.offset(null) + t.length()); //XXX: locking
                    //        fireHighlightsChange(t.getStart(), t.getEnd()); //XXX: locking
                    //    } else {
                            fireHighlightsChange(0, doc.getLength()); //XXX: locking
                    //    }
                    //}
                    
                    DocumentUtilities.removeDocumentListener(doc, GsfSemanticLayer.this, DocumentListenerPriority.LEXER);
                    DocumentUtilities.addDocumentListener(doc, GsfSemanticLayer.this, DocumentListenerPriority.LEXER);
                }
            }
        });
    }
    
    synchronized SortedSet<SequenceElement> getColorings() {
        return colorings;
    }

    synchronized int getVersion() {
        return version;
    }
    
    @Override
    public synchronized HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (colorings.isEmpty()) {
            return HighlightsSequence.EMPTY;
        }
        
        return new GsfHighlightSequence(this, doc, startOffset, endOffset, colorings);
    }

    public synchronized void clearColoringCache() {
        CACHE.clear();
    }
    
    private synchronized void clearLanguageColoring(Language mime) {
        CACHE.remove(mime);
        
    }
    
    synchronized AttributeSet getColoring(Coloring c, final Language language) {
        AttributeSet a = null;
        Map<Coloring,AttributeSet> map = CACHE.get(language);
        if (map == null) {
            final String mime = language.getMimeType();
            a = language.getColoringManager().getColoringImpl(c);
            map = new HashMap<Coloring,AttributeSet>();
            map.put(c, a);
            CACHE.put(language, map);
            Lookup.Result<FontColorSettings> res = MimeLookup.getLookup(MimePath.get(mime)).lookupResult(FontColorSettings.class);
            coloringResults.add(res);
            LookupListener l;
            
            res.addLookupListener(
                    WeakListeners.create(LookupListener.class, 
                        l = new LookupListener() {
                        @Override
                        public void resultChanged(LookupEvent ev) {
                            clearLanguageColoring(language);
                            fireHighlightsChange(0, doc.getLength());
                        }
                    }, res)
            );
            coloringListeners.add(l);
        } else {
            a = map.get(c);
            if (a == null) {
                map.put(c, a = language.getColoringManager().getColoringImpl(c));
            }
        }
        if (a == null) {
            LOG.log(Level.FINE, "Null AttributeSet for coloring {0} in language {1}", new Object [] { c, language });
        }
        return a;
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
    private class Edit {
        public Edit(int offset, int len, boolean insert) {
            this.offset = offset;
            this.len = len;
            this.insert = insert;
        }
        
        int offset;
        int len;
        boolean insert; // true: insert, false: delete
    }
    
    /**
     * An implementation of a HighlightsSequence which can show OffsetRange
     * sections and keep them up to date during edits.
     *
     * @author Tor Norbye
     */
    private static final class GsfHighlightSequence implements HighlightsSequence {
        private Iterator<SequenceElement> iterator;
        private SequenceElement element;
        private final GsfSemanticLayer layer;
        private final int endOffset;
        private SequenceElement nextElement;
        private int nextElementStartOffset = Integer.MAX_VALUE;

        GsfHighlightSequence(GsfSemanticLayer layer, Document doc, 
                int startOffset, int endOffset, 
                SortedSet<SequenceElement> colorings) {
            this.layer = layer;
            this.endOffset = endOffset;

            SequenceElement.ComparisonItem fromInclusive = new SequenceElement.ComparisonItem(startOffset);
            SortedSet<SequenceElement> subMap = colorings.tailSet(fromInclusive);
            iterator = subMap.iterator();
        }

        private SequenceElement fetchElementFromIterator(boolean updateNextElementStartOffset) {
            int seStartOffset;
            SequenceElement se;
            if (iterator != null && iterator.hasNext()) {
                se = iterator.next();
                seStartOffset = se.range.getStart();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Fetched highlight <{0},{1}>\n", // NOI18N
                            new Object[]{seStartOffset, se.range.getEnd()});
                }
                if (seStartOffset >= endOffset) {
                    se = null;
                    seStartOffset = Integer.MAX_VALUE;
                    iterator = null;
                }
            } else {
                se = null;
                seStartOffset = Integer.MAX_VALUE;
                iterator = null;
            }
            if (updateNextElementStartOffset) {
                nextElementStartOffset = seStartOffset;
            }
            return se;
        }

        @Override
        public boolean moveNext() {
            if (nextElement != null) {
                element = nextElement;
                nextElement = fetchElementFromIterator(true);
            } else {
                if ((element = fetchElementFromIterator(false)) != null) {
                    nextElement = fetchElementFromIterator(true);
                }
            }
            return (element != null);
        }

        @Override
        public int getStartOffset() {
            return (element != null)
                    ? layer.getShiftedPos(element.range.getStart())
                    : Integer.MAX_VALUE;
        }

        @Override
        public int getEndOffset() {
            return (element != null)
                    ? Math.min(layer.getShiftedPos(element.range.getEnd()), nextElementStartOffset)
                    : Integer.MAX_VALUE;
        }

        @Override
        public AttributeSet getAttributes() {
            return (element != null)
                    ? layer.getColoring(element.coloring, element.language)
                    : SimpleAttributeSet.EMPTY;
        }
    }
}
