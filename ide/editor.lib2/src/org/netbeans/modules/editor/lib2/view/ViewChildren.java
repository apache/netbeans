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

package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;

/**
 * Class that manages children of either DocumentView or ParagraphView.
 * <br>
 * For document view the class manages visual spans (end visual offsets).
 * For paragraphs the class manages end offsets of children as well as their end visual offsets.
 * <br>
 * Generally children of {@link #ParagraphView} manage their raw end offsets
 * while children of {@link #DocumentView} do not manage them (they use Position objects
 * to manage its start).
 * 
 * @author Miloslav Metelka
 */

class ViewChildren<V extends EditorView> extends GapList<V> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewChildren.class.getName());

    private static final long serialVersionUID  = 0L;

    ViewGapStorage gapStorage; // 24=super + 4 = 28 bytes
    
    ViewChildren(int capacity) {
        super(capacity);
    }

    int raw2Offset(int rawEndOffset) {
        return (gapStorage == null) ? rawEndOffset : gapStorage.raw2Offset(rawEndOffset);
    }

    int offset2Raw(int offset) {
        return (gapStorage == null) ? offset : gapStorage.offset2Raw(offset);
    }
    
    int startOffset(int index) {
        return (index > 0) ? raw2Offset(get(index - 1).getRawEndOffset()) : 0;
    }
    
    int endOffset(int index) {
        return raw2Offset(get(index).getRawEndOffset());
    }
    
    /**
     * Get view index of first view that "contains" the given offset (starts with it or it's inside)
     * by examining child views' raw end offsets.
     * <br>
     * This is suitable for paragraph view which manages its views' raw end offsets.
     * 
     * @param offset offset to search for.
     * @return view index or -1.
     */
    int viewIndexFirst(int offset) {
        // Translate relOffset into its raw form and search in raw offsets only.
        // Since the raw offsets are sorted (the gap is >= 0) this should work fine.
        offset = offset2Raw(offset);
        int last = size() - 1;
        int low = 0;
        int high = last;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            V view = get(mid);
            int rawEndOffset = view.getRawEndOffset();
            if (rawEndOffset < offset) {
                low = mid + 1;
            } else if (rawEndOffset > offset) {
                high = mid - 1;
            } else { // rawEndOffset == relOffset
                while (view.getLength() == 0 && mid > 0) {
                    view = get(--mid);
                }
                low = mid + 1;
                break;
            }
        }
        return Math.min(low, last); // Make sure last item is returned for relOffset above end
    }

    void moveOffsetGap(int index, int newOffsetGapStart) {
        if (gapStorage == null) {
            return;
        }
        int origStart = gapStorage.offsetGapStart;
        int shift = gapStorage.offsetGapLength;
        gapStorage.offsetGapStart = newOffsetGapStart;
        int viewCount = size();
        if (index == viewCount || get(index).getRawEndOffset() > origStart) {
            // Go down to check and fix views so that they are <= the offset
            while (--index >= 0) {
                EditorView view = get(index);
                int offset = view.getRawEndOffset();
                if (offset > origStart) { // Corresponds to <= in rawOffset => offset computation
                    view.setRawEndOffset(offset - shift);
                } else {
                    break;
                }
            }
        } else { // go up to check and fix the marks are above the offset
            while (index < viewCount) {
                EditorView view = get(index);
                int offset = view.getRawEndOffset();
                if (offset <= origStart) { // Corresponds to <= in rawOffset => offset computation
                    view.setRawEndOffset(offset + shift);
                } else {
                    break;
                }
                index++;
            }
        }
    }
    
    int getLength() { // Total offset length of contained child views
        return startOffset(size());
    }

    double raw2VisualOffset(double rawVisualOffset) {
        return (gapStorage == null) ? rawVisualOffset : gapStorage.raw2VisualOffset(rawVisualOffset);
    }

    double visualOffset2Raw(double visualOffset) {
        return (gapStorage == null) ? visualOffset : gapStorage.visualOffset2Raw(visualOffset);
    }

    /**
     * Start visual offset of the particular child view.
     * @param index &gt;= 0 and &lt;= size().
     * @return start visual offset of the child view at given index.
     */
    final double startVisualOffset(int index) {
        return (index > 0)
                ? raw2VisualOffset(get(index - 1).getRawEndVisualOffset())
                : 0d;
    }
    
    /**
     * End visual offset of the particular child view.
     * @param index &gt;= 0 and &lt; size().
     * @return ending visual offset of the child view at given index.
     */
    final double endVisualOffset(int index) {
        return raw2VisualOffset(get(index).getRawEndVisualOffset());
    }

    /**
     * Determine view index from given visual offset.
     *
     * @param visualOffset
     * @param measuredViewCount number of views that have their span (and end-visual-offset) measured.
     * @return index or -1 for no measured views.
     */
    final int viewIndexFirstVisual(double visualOffset, int measuredViewCount) {
        int last = measuredViewCount - 1;
        if (last == -1) {
            return -1; // No items
        }
        // Translate visualOffset into its raw form and search in raw offsets only.
        // Since the raw offsets are sorted (the gap is >= 0) this should work fine.
        visualOffset = visualOffset2Raw(visualOffset);
        int low = 0;
        int high = last;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            double rawEndVisualOffset = get(mid).getRawEndVisualOffset();
            if (rawEndVisualOffset < visualOffset) {
                low = mid + 1;
            } else if (rawEndVisualOffset > visualOffset) {
                high = mid - 1;
            } else { // exact raw end visual offset found at index
                while (mid > 0 && get(mid - 1).getRawEndVisualOffset() == visualOffset) {
                    mid--;
                }
                low = mid + 1;
                break;            }
        }
        return Math.min(low, last);
    }

    void moveVisualGap(int index, double newVisualGapStart) {
        if (gapStorage == null) {
            return;
        }
        gapStorage.visualGapStart = newVisualGapStart;
        if (index != gapStorage.visualGapIndex) {
            if (index < gapStorage.visualGapIndex) {
                for (int i = gapStorage.visualGapIndex - 1; i >= index; i--) {
                    V view = get(i);
                    view.setRawEndVisualOffset(view.getRawEndVisualOffset() + gapStorage.visualGapLength);
                }

            } else { // index > gapStorage.visualGapIndex
                for (int i = gapStorage.visualGapIndex; i < index; i++) {
                    V view = get(i);
                    view.setRawEndVisualOffset(view.getRawEndVisualOffset() - gapStorage.visualGapLength);
                }
            }
            gapStorage.visualGapIndex = index;
        }
    }

    protected String findIntegrityError(EditorView parent) {
        String err = null;
        int lastRawEndOffset = 0;
        int lastLocalEndOffset = 0;
        int lastEndOffset = parent.getStartOffset();
        double lastRawEndVisualOffset = 0d;
        double lastEndVisualOffset = 0d;
        for (int i = 0; i < size(); i++) {
            V view = get(i);
            View p = view.getParent();
            if (err == null && p != parent) {
                err = "view.getParent()=" + p + " != parent=" + parent;
            }
            int viewLength = view.getLength();
            int rawEndOffset = view.getRawEndOffset();
            int childStartOffset = view.getStartOffset();
            int childEndOffset = view.getEndOffset();
            double rawEndVisualOffset = view.getRawEndVisualOffset();
            double endVisualOffset = raw2VisualOffset(rawEndVisualOffset);
            // Check textual offset
            // rawEndOffset == -1 means that the raw offsets mechanism not being actively used (for pViews)
            if (err == null && rawEndOffset != -1 && rawEndOffset < lastRawEndOffset) {
                err = "rawEndOffset=" + rawEndOffset + " < lastRawEndOffset=" + lastRawEndOffset; // NOI18N
            }
            if (err == null && childStartOffset != lastEndOffset) {
                err = "childStartOffset=" + childStartOffset + " != lastEndOffset=" + lastEndOffset; // NOI18N
            }
            if (err == null && childEndOffset < childStartOffset) {
                err = "childEndOffset=" + childEndOffset + " < childStartOffset=" + childStartOffset; // NOI18N
            }
            if (err == null && childEndOffset - childStartOffset != viewLength) {
                err = "(childEndOffset-childStartOffset)=" + (childEndOffset - childStartOffset) + // NOI18N
                        " != view.getLength()=" + viewLength; // NOI18N
            }
            lastEndOffset = childEndOffset;

            // Check visual offset
            if (err == null && rawEndVisualOffset < lastRawEndVisualOffset) {
                err = "rawEndVisualOffset=" + rawEndVisualOffset + " < lastRawEndVisualOffset=" + // NOI18N
                        lastRawEndVisualOffset;
            }
            if (err == null && endVisualOffset < lastEndVisualOffset) {
                err = "visualOffset=" + endVisualOffset + " < lastVisualOffset=" + lastEndVisualOffset; // NOI18N
            }
            if (err == null) {
                err = checkSpanIntegrity((endVisualOffset - lastEndVisualOffset), view);
            }
            lastEndVisualOffset = endVisualOffset;

            if (err == null && gapStorage != null && rawEndOffset != -1) {
                int localEndOffset = gapStorage.raw2Offset(rawEndOffset);
                if (lastLocalEndOffset + viewLength != localEndOffset) {
                    err = "lastLocalEndOffset=" + lastLocalEndOffset + " + viewLength=" + viewLength + // NOI18N
                            " != localEndOffset=" + localEndOffset; // NOI18N
                }
                if (i < gapStorage.visualGapIndex) {
                    if (err == null && !gapStorage.isBelowVisualGap(rawEndVisualOffset)) {
                        err = "Not below visual-gap: rawEndVisualOffset=" + rawEndVisualOffset + // NOI18N
                                "(minus gap: " + (rawEndVisualOffset-gapStorage.visualGapLength) + // NOI18N
                                "), gap:" + gapStorage; // NOI18N
                    }
                } else { // Index above visual gap
                    if (err == null && gapStorage.isBelowVisualGap(rawEndVisualOffset)) {
                        err = "Not above visual-gap: rawEndVisualOffset=" + rawEndVisualOffset + // NOI18N
                                ", gap:" + gapStorage; // NOI18N
                    }
                }
                lastLocalEndOffset = localEndOffset;
            }

            if (err != null) {
                err = "ViewChildren[" + i + "]: " + err; // NOI18N
                break;
            }
        }
        return err;
    }
    
    protected String checkSpanIntegrity(double span, V view) {
        return null; // By default do not check
    }

    /**
     * Append debugging info.
     *
     * @param sb non-null string builder
     * @param indent &gt;=0 indentation in spaces.
     * @param importantIndex either an index of child that is important to describe in the output
     *  (Initial and ending two displayed plus two before and after the important index).
     *  Or -1 to display just starting and ending two. Or -2 to display all children.
     * @return
     */
    public StringBuilder appendChildrenInfo(StringBuilder sb, int indent, int importantIndex) {
        if (gapStorage != null) {
            sb.append("Gap: ");
            gapStorage.appendInfo(sb);
        }
        int viewCount = size();
        int digitCount = ArrayUtilities.digitCount(viewCount);
        int importantLastIndex = -1; // just be < 0
        int childImportantIndex = (importantIndex == -2) ? -2 : -1;
        for (int i = 0; i < viewCount; i++) {
            sb.append('\n');
            ArrayUtilities.appendSpaces(sb, indent);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            V view = get(i);
            String xyInfo = getXYInfo(i);
            view.appendViewInfo(sb, indent, xyInfo, childImportantIndex);
            boolean appendDots = false;
            if (i == 4) { // After showing first 5 items => possibly skip to important index
                if (importantIndex == -1) { // Display initial five
                    if (i < viewCount - 6) { // -6 since i++ will follow
                        appendDots = true;
                        i = viewCount - 6;
                    }
                } else if (importantIndex >= 0) {
                    importantLastIndex = importantIndex + 3;
                    importantIndex = importantIndex - 3;
                    if (i < importantIndex - 1) {
                        appendDots = true;
                        i = importantIndex - 1;
                    }
                } // otherwise importantIndex == -2 to display every child
            } else if (i == importantLastIndex) {
                if (i < viewCount - 6) { // -6 since i++ will follow
                    appendDots = true;
                    i = viewCount - 6;
                }
            }
            if (appendDots) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("...");
            }
        }
        return sb;
    }
    
    protected String getXYInfo(int index) {
        return "";
    }

}
