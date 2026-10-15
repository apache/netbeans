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

package org.netbeans.modules.merge.builtin.visualizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.diff.builtin.visualizer.DEditorPane;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author ondra
 */
public final class MergePane extends DEditorPane implements HighlightsContainer, DocumentListener {

    private final List<HighLight> highlights = new LinkedList<HighLight>();
    
    @Override
    public HighlightsSequence getHighlights(int start, int end) {
        return new MergeHighlightsSequence(start, end, getHighlights());
    }

    private final List<HighlightsChangeListener> listeners = new ArrayList<HighlightsChangeListener>(1);

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
        moveRegions(offset, length);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
        moveRegions(offset, -length);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //
    }

    void addHighlight (StyledDocument doc, int line1, int line2, final java.awt.Color color) {
        if (line1 > 0) {
            --line1;
        }
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBackground(attrs, color);
        attrs.addAttribute(HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
        int startOffset = getRowStartFromLineOffset(doc, line1);
        int endOffset = getRowStartFromLineOffset(doc, line2);
        synchronized (highlights) {
            ListIterator<HighLight> it = highlights.listIterator();
            HighLight toAdd = new HighLight(startOffset, endOffset, attrs);
            while (it.hasNext()) {
                HighLight highlight = it.next();
                if (highlight.contains(startOffset)) {
                    it.remove();
                    break;
                } else if (highlight.precedes(startOffset)) {
                    it.previous();
                    break;
                }
            }
            it.add(toAdd);
        }
        fireHilitingChanged();
    }

    void removeHighlight (StyledDocument doc, int line1, int line2) {
        if (line1 > 0) {
            --line1;
        }
        int startOffset = getRowStartFromLineOffset(doc, line1);
        synchronized (highlights) {
            ListIterator<HighLight> it = highlights.listIterator();
            while (it.hasNext()) {
                HighLight highlight = it.next();
                if (highlight.contains(startOffset)) {
                    it.remove();
                    break;
                }
            }
        }
        fireHilitingChanged();
    }

    private HighLight[] getHighlights () {
        synchronized (highlights) {
            return highlights.toArray(new HighLight[0]);
        }
    }

    static int getRowStartFromLineOffset(Document doc, int lineIndex) {
        int offset;
        if (doc instanceof StyledDocument) {
            offset = org.openide.text.NbDocument.findLineOffset((StyledDocument) doc, lineIndex);
        } else {
            Element element = doc.getDefaultRootElement();
            Element line = element.getElement(lineIndex);
            offset = line.getStartOffset();
        }
        return offset;
    }

    void fireHilitingChanged() {
        synchronized(listeners) {
            for (HighlightsChangeListener listener : listeners) {
              listener.highlightChanged(new HighlightsChangeEvent(this, 0, Integer.MAX_VALUE));
            }
        }
    }

    private void moveRegions (int offset, int displacement) {
        synchronized (highlights) {
            for (ListIterator<HighLight> it = highlights.listIterator(); it.hasNext(); ) {
                HighLight highlight = it.next();
                if (highlight.startOffset > offset) {
                    highlight.move(displacement);
                }
            }
        }
        fireHilitingChanged();
    }

    private static class HighLight {

        private int           startOffset;
        private int           endOffset;
        private final AttributeSet  attrs;

        public HighLight(int startOffset, int endOffset, AttributeSet attrs) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attrs = attrs;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public AttributeSet getAttrs() {
            return attrs;
        }

        private boolean contains (int position) {
            return startOffset <= position && endOffset >= position;
        }

        private boolean precedes (int position) {
            return startOffset > position;
        }

        private void move (int displacement) {
            startOffset += displacement;
            endOffset += displacement;
        }
    }

    /**
     * Iterates over all found differences.
     */
    private static class MergeHighlightsSequence implements HighlightsSequence {

        private final int       endOffset;
        private final int       startOffset;

        private int             currentHiliteIndex = -1;
        private HighLight [] highlights;

        public MergeHighlightsSequence(int start, int end, HighLight[] allHilites) {
            this.startOffset = start;
            this.endOffset = end;
            List<HighLight> list = new LinkedList<HighLight>();
            for (HighLight hilite : allHilites) {
                if (hilite.getEndOffset() < startOffset) continue;
                if (hilite.getStartOffset() > endOffset) break;
                list.add(hilite);
            }
            highlights = list.toArray(new HighLight[0]);
        }

        @Override
        public boolean moveNext() {
            if (currentHiliteIndex >= highlights.length - 1) return false;
            currentHiliteIndex++;
            return true;
        }

        @Override
        public int getStartOffset() {
            return Math.max(highlights[currentHiliteIndex].getStartOffset(), this.startOffset);
        }

        @Override
        public int getEndOffset() {
            return Math.min(highlights[currentHiliteIndex].getEndOffset(), this.endOffset);
        }

        @Override
        public AttributeSet getAttributes() {
            return highlights[currentHiliteIndex].getAttrs();
        }
    }
}
