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
package org.netbeans.modules.cnd.makefile.editor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.text.NbDocument;

/**
 */
public class ShellEmbeddingHighlightContainer extends AbstractHighlightsContainer {

    public static ShellEmbeddingHighlightContainer get(Document doc) {
        ShellEmbeddingHighlightContainer l = (ShellEmbeddingHighlightContainer) doc.getProperty(ShellEmbeddingHighlightContainer.class);
        if (l == null) {
            doc.putProperty(ShellEmbeddingHighlightContainer.class, l = new ShellEmbeddingHighlightContainer(doc));
        }
        return l;
    }

    private final Document doc;
    private volatile List<HighlightItem> highlights;

    private ShellEmbeddingHighlightContainer(Document doc) {
        this.doc = doc;
        this.highlights = Collections.emptyList();
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        List<HighlightItem> highlightsCopy = highlights;
        return ShellHighlightSequence.create(highlightsCopy, startOffset, endOffset);
    }

    /*package*/ void setHighlights(final List<HighlightItem> newHighlights) {
        doc.render(new Runnable() {
            @Override
            public void run() {
                int[] changedInterval = changedInterval(ShellEmbeddingHighlightContainer.this.highlights, newHighlights);
                if (changedInterval != null) {
                    ShellEmbeddingHighlightContainer.this.highlights = newHighlights;
                    fireHighlightsChange(changedInterval[0], changedInterval[1]);
                }
            }
        });
    }

    /*package*/ static int[] changedInterval(List<HighlightItem> oldHighlights, List<HighlightItem> newHighlights) {
        int oldSize = oldHighlights.size();
        int newSize = newHighlights.size();
        int minSize = Math.min(oldSize, newSize);

        int commonPrefix = 0;
        for (int i = 0; i < minSize; ++i) {
            if (HighlightItem.equals(oldHighlights.get(i), newHighlights.get(i))) {
                ++commonPrefix;
            } else {
                break;
            }
        }

        if (oldSize == newSize && commonPrefix == newSize) {
            return null;
        }

        int commonSuffix = 0;
        for (int i = 0; i < minSize; ++i) {
            if (HighlightItem.equals(oldHighlights.get(oldSize - 1 - i), newHighlights.get(newSize - 1 - i))) {
                ++commonSuffix;
            } else {
                break;
            }
        }

        int changeStart = commonPrefix == 0 ? 0 : oldHighlights.get(commonPrefix - 1).end.getOffset();
        int changeEnd = commonSuffix == 0 ? Integer.MAX_VALUE : oldHighlights.get(oldSize - commonSuffix).start.getOffset();
        return new int[]{changeStart, changeEnd};
    }

    public static final class HighlightItem {
        private final Position start;
        private final Position end;
        private final String category;

        public HighlightItem(Position start, Position end, String category) {
            this.start = start;
            this.end = end;
            this.category = category;
        }

        private static boolean equals(HighlightItem a, HighlightItem b) {
            return a.start.getOffset() == b.start.getOffset()
                    && a.end.getOffset() == b.end.getOffset()
                    && a.category.equals(b.category);
        }
    }
    
    /**
     * Returns index of the first {@link HighlightItem} that has nonzero
     * overlap with interval <code>(startOffset, infinity)</code>.
     * Returns <code>highlights.size()</code> if no overlap.
     */
    /*package*/ static int firstOverlap(List<HighlightItem> highlights, int startOffset) {
        int left = 0; // indices less than 'left' have no overlap
        int right = highlights.size(); // indices greater or equal to 'right' have overlap
        while (left < right) {
            int mid = (left + right) / 2;
            HighlightItem midItem = highlights.get(mid);
            if (midItem.end.getOffset() <= startOffset) {
                left = mid + 1;
            } else if (startOffset < midItem.start.getOffset()) {
                right = mid;
            } else {
                return mid;
            }
        }
        return left;
    }

    /**
     * Returns index of the last {@link HighlightItem} that has nonzero
     * overlap with interval <code>(-infinity, endOffset)</code>.
     * Returns <code>-1</code> if no overlap.
     */
    /*package*/ static int lastOverlap(List<HighlightItem> highlights, int endOffset) {
        int left = -1; // indices less or equal to 'left' have overlap
        int right = highlights.size() - 1; // indices greater than 'right' have no overlap
        while (left < right) {
            int mid = (left + right + 1) / 2;
            HighlightItem midItem = highlights.get(mid);
            int itemStart = midItem.start.getOffset();
            int itemEnd = midItem.end.getOffset();
            if (endOffset <= itemStart) {
                right = mid - 1;
            } else if (itemEnd < endOffset) {
                left = mid;
            } else {
                return mid;
            }
        }
        return left;
    }

    private static final class ShellHighlightSequence implements HighlightsSequence {

        private static final FontColorSettings SETTINGS = MimeLookup.getLookup(MimePath.get(MIMENames.SHELL_MIME_TYPE)).lookup(FontColorSettings.class);
        private final Iterator<HighlightItem> highlightIterator;
        private HighlightItem currentItem;

        private static HighlightsSequence create(List<HighlightItem> highlights, int startOffset, int endOffset) {
            final int startIdx;
            final int endIdx;
            if (startOffset < endOffset) {
                startIdx = firstOverlap(highlights, startOffset);
                endIdx = lastOverlap(highlights, endOffset) + 1;
            } else {
                startIdx = highlights.size();
                endIdx = 0;
            }
            if (startIdx < endIdx) {
                return new ShellHighlightSequence(highlights.subList(startIdx, endIdx).iterator());
            } else {
                return HighlightsSequence.EMPTY;
            }
        }

        private ShellHighlightSequence(Iterator<HighlightItem> highlightIterator) {
            this.highlightIterator = highlightIterator;
        }

        @Override
        public boolean moveNext() {
            boolean hasNext = highlightIterator.hasNext();
            if (hasNext) {
                currentItem = highlightIterator.next();
            } else {
                currentItem = null;
            }
            return hasNext;
        }

        @Override
        public int getStartOffset() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return currentItem.start.getOffset();
        }

        @Override
        public int getEndOffset() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return currentItem.end.getOffset();
        }

        @Override
        public AttributeSet getAttributes() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return SETTINGS.getTokenFontColors(currentItem.category);
        }
    }

    public static final class LayerFactory implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[]{
                HighlightsLayer.create(
                        ShellEmbeddingProvider.class.getName(),
                        ZOrder.SYNTAX_RACK, false, // must be below makefile syntax
                        ShellEmbeddingHighlightContainer.get(context.getDocument()))
            };
        }
    }
}
