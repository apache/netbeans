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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
