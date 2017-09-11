/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.highlighting;

import java.util.Enumeration;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * Multiple highlights for a particular view contains an array of highlights
 * and a starting offset to which the (ending) offsets in HighlightItem(s) are related.
 * <br>
 * The view should assign this object to its internal 'attributes' variable that holds the AttributeSet.
 * <br>
 * The AttributeSet implementation returns attributes of the first section of the compound attributes.
 *
 * @author Miloslav Metelka
 */
public final class CompoundAttributes implements AttributeSet {
    
    private static void checkHighlightItemsNonNull(HighlightItem[] highlightItems) {
        for (int i = 0; i < highlightItems.length; i++) {
            if (highlightItems[i] == null) {
                throw new IllegalStateException("highlightItems[" + i + "] == null"); // NOI18N
            }
        }
    }
    
    /**
     * Since the view may move its absolute start offset the end offsets in highlight items
     * are related to beginning of the related view.
     */
    private final HighlightItem[] highlightItems; // 8(super) + 4 = 12 bytes
    
    /**
     * Start offset of the highlights. This is a start offset of the view
     * to which this object is attached at the time the view was created
     * (it can move over time due to document insertions/removals).
     */
    private final int startOffset; // 12 + 4 = 16 bytes

    public CompoundAttributes(int startOffset, HighlightItem[] highlightItems) {
        assert (highlightItems.length >= 2) : "highlightItems.length=" + highlightItems.length + " < 2"; // NOI18N
        this.highlightItems = highlightItems;
        this.startOffset = startOffset;
//        checkHighlightItemsNonNull(highlightItems);
    }
    
    public int startOffset() {
        return startOffset;
    }
    
    public int endOffset() {
        return highlightItems[highlightItems.length - 1].getEndOffset();
    }
    
    public HighlightItem[] highlightItems() {
        return highlightItems;
    }
    
    public HighlightsSequence createHighlightsSequence(int viewStartOffset, int startOffset, int endOffsets) {
        return new HiSequence(highlightItems, viewStartOffset - startOffset, startOffset, startOffset);
    }

    private AttributeSet firstItemAttrs() {
        AttributeSet attrs = highlightItems[0].getAttributes();
        if (attrs == null) {
            attrs = SimpleAttributeSet.EMPTY;
        }
        return attrs;
    }

    @Override
    public int getAttributeCount() {
        return firstItemAttrs().getAttributeCount();
    }

    @Override
    public boolean isDefined(Object attrName) {
        return firstItemAttrs().isDefined(attrName);
    }

    @Override
    public boolean isEqual(AttributeSet attr) {
        return firstItemAttrs().isEqual(attr);
    }

    @Override
    public AttributeSet copyAttributes() {
        return firstItemAttrs().copyAttributes();
    }

    @Override
    public Object getAttribute(Object key) {
        return firstItemAttrs().getAttribute(key);
    }

    @Override
    public Enumeration<?> getAttributeNames() {
        return firstItemAttrs().getAttributeNames();
    }

    @Override
    public boolean containsAttribute(Object name, Object value) {
        return firstItemAttrs().containsAttribute(name, value);
    }

    @Override
    public boolean containsAttributes(AttributeSet attributes) {
        return firstItemAttrs().containsAttributes(attributes);
    }

    @Override
    public AttributeSet getResolveParent() {
        return firstItemAttrs().getResolveParent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        int size = highlightItems.length;
        int digitCount = ArrayUtilities.digitCount(size);
        int lastOffset = startOffset;
        for (int i = 0; i < size; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(highlightItems[i].toString(lastOffset));
            sb.append('\n');
            lastOffset = highlightItems[i].getEndOffset();
        }
        return sb.toString();
    }

    /**
     * Highlights sequence on top of ViewHighlights.
     */
    static class HiSequence implements HighlightsSequence {
        
        private final HighlightItem[] highlightItems;

        private final int shift;
        
        private final int endOffset;
        
        private int highlightIndex;

        private int hlStartOffset;
        
        private int hlEndOffset;
        
        private AttributeSet hlAttrs;
        
        HiSequence(HighlightItem[] highlightItems, int shift, int startOffset, int endOffset) {
            this.highlightItems = highlightItems;
            this.shift = shift;
            this.endOffset = endOffset;
            this.hlEndOffset = startOffset;
            if (true && highlightItems.length <= 10) {
                while (highlightIndex < highlightItems.length &&
                        highlightItems[highlightIndex].getEndOffset() + shift <= startOffset)
                {
                    highlightIndex++;
                }
            } // [TODO] Otherwise do binary search
        }

        @Override
        public boolean moveNext() {
            int lastOffset = hlEndOffset;
            while (highlightIndex < highlightItems.length) {
                HighlightItem highlight = highlightItems[highlightIndex++];
                AttributeSet attrs = highlight.getAttributes();
                if (attrs != null) {
                    hlStartOffset = lastOffset;
                    hlEndOffset = highlight.getEndOffset() + shift;
                    if (hlEndOffset > endOffset) {
                        hlEndOffset = endOffset;
                        if (hlEndOffset <= hlStartOffset) {
                            highlightIndex = highlightItems.length;
                            return false;
                        }
                    }
                    hlAttrs = attrs;
                    return true;
                }
                lastOffset = highlight.getEndOffset() + shift;
            }
            return false;
        }

        @Override
        public int getStartOffset() {
            return hlStartOffset;
        }

        @Override
        public int getEndOffset() {
            return hlEndOffset;
        }

        @Override
        public AttributeSet getAttributes() {
            return hlAttrs;
        }

    }
}
