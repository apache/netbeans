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

import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;


/**
 * Builder and updater of wrap lines used by {@link ParagraphViewChildren}.
 * 
 * @author Miloslav Metelka
 */

final class WrapInfoUpdater {

    // -J-Dorg.netbeans.modules.editor.lib2.view.WrapInfoUpdater.level=FINER
    private static final Logger LOG = Logger.getLogger(WrapInfoUpdater.class.getName());

    private static final long serialVersionUID  = 0L;

    private final WrapInfo wrapInfo;

    private final ParagraphView pView;

    private final DocumentView docView;

    /**
     * Total width that may be occupied by wrap line's content.
     */
    private float availableWidth;
    
    /**
     * Maximum width from all created wrap lines.
     */
    private float maxWrapLineWidth;

    private boolean wrapTypeWords;

    private StringBuilder logMsgBuilder;
    
    private List<WrapLine> wrapLines;

    /** Wrap line being currently built. */
    private WrapLine wrapLine;

    /** Whether wrap line has some views (or parts) added to it). */
    private boolean wrapLineNonEmpty;

    /** Current X on a wrap-line being just built. */
    private float wrapLineX;

    /** Index of child view being currently processed. */
    private int childIndex;
    
    /**
     * Offset on x-coordinate of childView measured from the first child in ParagraphViewChildren (no wrapping involved).
     * This is useful to quickly get (non-wrapped) child view's width without calling child.getPreferredSpan(X_AXIS).
     */
    private double childX;
    
    /**
     * Offset on x-coordinate of a next childView measured from the first child (without wrapping).
     * (nextChildX - childX) is childWidth which should be equivalent to child.getPreferredSpan(X_AXIS).
     */
    private double nextChildX;

    
    WrapInfoUpdater(WrapInfo wrapInfo, ParagraphView paragraphView) {
        this.wrapInfo = wrapInfo;
        this.pView = paragraphView;
        this.docView = paragraphView.getDocumentView();
        assert (this.docView != null) : "Null documentView"; // NOI18N
    }

    void initWrapInfo() {
        this.wrapLines = new ArrayList<WrapLine>(2);
        wrapLine = new WrapLine();
        wrapTypeWords = (docView.op.getLineWrapType() == LineWrapType.WORD_BOUND);
        float visibleWidth = docView.op.getVisibleRect().width;
        TextLayout lineContinuationTextLayout = docView.op.getLineContinuationCharTextLayout();
        final float lineContTextLayoutAdvance =
            lineContinuationTextLayout == null ? 0f : lineContinuationTextLayout.getAdvance();
        // Make reasonable minimum width so that the number of visual lines does not double suddenly
        // when user would minimize the width too much. Also have enough space for line continuation mark
        availableWidth = Math.max(visibleWidth - lineContTextLayoutAdvance,
                docView.op.getDefaultCharWidth() * 4);
        logMsgBuilder = LOG.isLoggable(Level.FINE) ? new StringBuilder(100) : null;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("Building wrapLines: availWidth=").append(availableWidth); // NOI18N
            logMsgBuilder.append(", lineContCharWidth=").append(lineContTextLayoutAdvance); // NOI18N
            logMsgBuilder.append("\n"); // NOI18N
        }
        try {
            ViewPart viewOrPart = initChildVars(0, 0d); // At least one child should exist 
            do {
                if (wrapLineX + viewOrPart.width <= availableWidth) { // Within available width
                    addViewOrPart(viewOrPart);
                    viewOrPart = fetchNextView();
                } else { // Exceeds available width => must break the child view
                    boolean regularBreak = false;
                    if (wrapTypeWords) {
                        int viewOrPartStartOffset = viewOrPart.view.getStartOffset();
                        int wrapLineStartOffset;
                        if (wrapLineNonEmpty) {
                            wrapLineStartOffset = wrapLine.startView(pView).getStartOffset();
                        } else {
                            wrapLineStartOffset = viewOrPartStartOffset;
                        }
                        // Get valid wordInfo in case there's a word around childOrPartStartOffset
                        WordInfo wordInfo = getWordInfo(viewOrPartStartOffset, wrapLineStartOffset);
                        if (wordInfo != null) {
                            // Attempt to break the view (at word boundary) so that it fits.
                            ViewSplit split = breakView(viewOrPart, true);
                            if (split != null) {
                                addPart(split.startPart);
                                finishWrapLine();
                                viewOrPart = split.endPart;
                            } else { // Does not fit or cannot break
                                if (wrapLineStartOffset == wordInfo.wordStartOffset()) {
                                    int wordEndOffset = wordInfo.wordEndOffset();
                                    while (viewOrPart != null) {
                                        // Attempt to add views till end of word (so that words are complete on WL)
                                        int endOffset = viewOrPart.view.getEndOffset();
                                        if (wordEndOffset >= endOffset) {
                                            addViewOrPart(viewOrPart);
                                            viewOrPart = fetchNextView();
                                        } else { // Attempt to split at word end
                                            ViewSplit wordEndSplit = createFragment(viewOrPart, wordEndOffset, true);
                                            if (wordEndSplit != null) {
                                                addPart(wordEndSplit.startPart);
                                                viewOrPart = wordEndSplit.endPart;
                                            } else { // Cannot split at word end
                                                // Add whole view
                                                addViewOrPart(viewOrPart);
                                                viewOrPart = fetchNextView();
                                            }
                                            break;
                                        }
                                    }
                                } else {
                                    ViewPart aboveWordStartPart = removeViewsAndSplitAtWordStart(wordInfo.wordStartOffset());
                                    if (aboveWordStartPart != null) {
                                        viewOrPart = aboveWordStartPart;
                                    } else {
                                        viewOrPart = fetchNextView();
                                    }
                                }
                                finishWrapLine();
                            }
                        } else { // WordInfo == null
                            regularBreak = true;
                        }
                    } else { // Not wrapping at words boundary
                        regularBreak = true;
                    }
                    
                    if (regularBreak) {
                        /* Use allowWider=true here, so that long words are allowed to extend beyond
                        the preferred wrap width. Turning it off would just give up on breaking
                        entirely for the rest of the paragraph, yielding an even longer physical
                        line. */
                        ViewSplit split = breakView(viewOrPart, true);
                        if (split != null) {
                            addPart(split.startPart);
                            viewOrPart = split.endPart;
                        } else { // break failed
                            if (!wrapLineNonEmpty) {
                                addViewOrPart(viewOrPart);
                                viewOrPart = fetchNextView();
                            }
                        }
                        /* Keep the NewlineView that follows each paragraph together with the
                        paragraph's last wrap line. Otherwise, the NewlineView might wrap to the
                        next physical line if the last wrap line of the paragraph happens to be
                        exactly as long as the availableWidth. This would make the text caret, if
                        positioned at the end of a paragraph, end up being visually positioned on
                        the beginning of the next line instead of at the end of the current one. */
                        if (viewOrPart != null && viewOrPart.view instanceof NewlineView) {
                            /* One exception: If the wrap line ends with a space, it's actually
                            better to allow the NewlineView, and thus the text caret, to wrap to the
                            next physical line, since this is where the user's next typed character
                            will end up. This also avoids the need to position the caret outside the
                            viewport in a few cases (due to the text wrapping policy of allowing
                            whitespace characters at the end of each wrap line to extend beyond the
                            preferred wrap width). */
                            boolean wrapLineEndsWithSpace = false;
                            try {
                                int newlineOffset = viewOrPart.view.getStartOffset();
                                wrapLineEndsWithSpace = newlineOffset > 0 &&
                                    Character.isWhitespace(viewOrPart.view.getDocument()
                                    .getText(newlineOffset - 1, 1).charAt(0));
                            } catch (BadLocationException e) {
                                // Ignore.
                            }
                            if (!wrapLineEndsWithSpace) {
                                addViewOrPart(viewOrPart);
                                viewOrPart = fetchNextView();
                            }
                        }
                        finishWrapLine();
                    }
                }
            } while (childIndex < pView.getViewCount());
            finishWrapLine();
        } finally {
            if (logMsgBuilder != null) {
                logMsgBuilder.append('\n');
                LOG.fine(logMsgBuilder.toString());
            }
        }

        wrapInfo.addAll(wrapLines);
        wrapInfo.checkIntegrity(pView);
        if (logMsgBuilder != null) {
            LOG.fine("Resulting wrapInfo:" + wrapInfo.toString(pView) + "\n");
        }
        wrapInfo.setWidth(maxWrapLineWidth);
    }
    
    private void finishWrapLine() {
        if (wrapLineNonEmpty) {
            if (wrapLineX > maxWrapLineWidth) {
                maxWrapLineWidth = wrapLineX;
            }
            wrapLines.add(wrapLine);
            wrapLine = new WrapLine();
            wrapLineNonEmpty = false;
            wrapLineX = 0f;
        }
    }
    
    private ViewPart initChildVars(int childIndex, double childX) {
        this.childIndex = childIndex;
        this.childX = childX;
        return assignChild();
    }
    
    private ViewPart assignChild() {
        nextChildX = pView.children.startVisualOffset(childIndex + 1);
        EditorView childView = pView.getEditorView(childIndex);
        float childWidth = (float) (nextChildX - childX);
        if (logMsgBuilder != null) {
            logMsgBuilder.append("child[").append(childIndex).append("]:").append(childView.getDumpId()); // NOI18N
            int startOffset = childView.getStartOffset();
            logMsgBuilder.append(" <").append(startOffset).append(",").append(startOffset + childView.getLength()); // NOI18N
            logMsgBuilder.append("> W=").append(childWidth); // NOI18N
            logMsgBuilder.append(":\n"); // NOI18N
        }
        return new ViewPart(childView, childWidth);
    }
    
    /**
     * Move next child view into childViewOrPart variable (or set it to null if there's no more children).
     */
    private ViewPart fetchNextView() {
        childIndex++; // Possibly get >view-count for multiple calls but does not matter
        if (childIndex < pView.getViewCount()) {
            childX = nextChildX;
            return assignChild();
        } else {
            return null;
        }
    }
    
    /**
     * Add current child view or its end part to current wrap line and fetch next view.
     */
    private void addView(ViewPart part) {
        assert (!part.isPart()) : "Attempt to add part instead of full view"; // NOI18N
        assert (wrapLine.endPart == null) : "End part already set"; // NOI18N
        if (wrapLineNonEmpty) {
            assert (wrapLine.endViewIndex == childIndex);
            wrapLine.endViewIndex++;
        } else { // Empty wrap line
            wrapLineNonEmpty = true;
            wrapLine.firstViewIndex = childIndex;
            wrapLine.endViewIndex = childIndex + 1;
        }
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  child added"); // NOI18N
        }
        wrapLineX += part.width;
        if (logMsgBuilder != null) {
            logWrapLineAndX();
        }
    }
    
    /**
     * Set end part of the current wrap line.
     *
     * @param parts non-null parts containing start part.
     */
    private void addStartPart(ViewPart part) {
        assert (part.isPart()) : "Attempt to add full view"; // NOI18N
        assert (wrapLine.startPart == null) : "startPart already inited"; // NOI18N
        assert (!wrapLineNonEmpty) : "wrapLineNonEmpty set"; // NOI18N
        wrapLineNonEmpty = true;
        wrapLine.firstViewIndex = childIndex + 1;
        wrapLine.endViewIndex = childIndex + 1;
        wrapLine.startPart = part;
        wrapLineX += part.width;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  startPart set<").append(part.view.getStartOffset()). // NOI18N
                            append(",").append(part.view.getEndOffset()).append(">"); // NOI18N
            logWrapLineAndX();
        }
    }

    /**
     * Set end part of the current wrap line.
     *
     * @param parts non-null parts containing start part.
     */
    private void addEndPart(ViewPart part) {
        assert (part.isPart()) : "Attempt to add full view"; // NOI18N
        assert (wrapLine.endPart == null) : "endPart already inited"; // NOI18N
        if (!wrapLineNonEmpty) {
            wrapLine.firstViewIndex = childIndex;
            wrapLine.endViewIndex = childIndex;
            wrapLineNonEmpty = true;
        }
        wrapLine.endPart = part;
        wrapLineX += part.width;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  endPart set<").append(part.view.getStartOffset()). // NOI18N
                            append(",").append(part.view.getEndOffset()).append(">"); // NOI18N
            logWrapLineAndX();
        }
    }

    
    private void addPart(ViewPart part) {
        if (part.isFirstPart()) {
            addEndPart(part);
        } else {
            addStartPart(part);
        }
    }
    
    private void addViewOrPart(ViewPart viewOrPart) {
        if (viewOrPart.isPart()) {
            addPart(viewOrPart);
        } else {
            addView(viewOrPart);
        }
    }
    
    /**
     * Remove all views in wrapLine above (or including) the given wordStartOffset
     * and add possible parts till wordStartOffset.
     * @param wordStartOffset split point
     * @return either null or a remaining part above wordStartOffset.
     */
    private ViewPart removeViewsAndSplitAtWordStart(int wordStartOffset) {
        assert (wrapLineNonEmpty) : "Empty wrap line"; // NOI18N
        assert (wrapLine.endPart == null);
        if (wrapLine.hasFullViews()) {
            boolean isFirstView = false;
            do {
                wrapLine.endViewIndex--;
                int lastViewIndex = wrapLine.endViewIndex;
                isFirstView = (lastViewIndex == wrapLine.firstViewIndex);
                EditorView view = pView.getEditorView(lastViewIndex);
                int viewStartOffset = view.getStartOffset();
                if (wordStartOffset < viewStartOffset + view.getLength()) { // Remove the child view
                    double startChildX = pView.children.startVisualOffset(lastViewIndex);
                    double childWidth = childX - startChildX;
                    wrapLineX -= childWidth;
                    if (isFirstView) {
                        if (wrapLine.startPart == null) {
                            wrapLineNonEmpty = false;
                        }
                    }
                    ViewPart viewPart = initChildVars(lastViewIndex, startChildX);
                    if (wordStartOffset > viewStartOffset) { // Fragment inside child view
                        ViewSplit wordStartSplit = createFragment(viewPart, wordStartOffset, true);
                        if (wordStartSplit != null) { // Successful fragmenting
                            addPart(wordStartSplit.startPart);
                            return wordStartSplit.endPart;
                        } else { // Fragmentation failed
                            // In order to avoid infinite loop add the complete child
                            addView(viewPart);
                            return null;
                            
                        }
                    } else if (wordStartOffset == viewStartOffset) { // Removed exactly whole view
                        return null;
                    }
                }
            } while (!isFirstView);
        }

        // Remove start part and possibly re-add initial part till wordStartOffset
        assert (wrapLine.startPart != null) : "Null wrapLine.startPart";
        if (wrapLine.startPart.view.getEndOffset() == wordStartOffset) { // startPart ends at wordStartOffset
            return null;
        }
        ViewPart startPart = wrapLine.startPart;
        wrapLine.startPart = null;
        wrapLineX = 0f;
        wrapLineNonEmpty = false;
        
        // Start view of this line was obtained by breaking a
        // view at (firstViewIndex - 1), need to go back to that view.
        double startChildX = pView.children.startVisualOffset(wrapLine.firstViewIndex - 1);
        initChildVars(wrapLine.firstViewIndex - 1, startChildX);
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  Removed startPart."); // NOI18N
        }

        // Create fragment starting at either view's start offset (or part's start offset)
        // and ending at wordStartOffset. The other fragment will start at wordStartOffset
        // and end at child view's end offset.
        ViewSplit split = createFragment(startPart, wordStartOffset, true);
        if (split != null) {
            addStartPart(split.startPart);
            return split.endPart; // Caller should replace its end part with this
        } else { // Fragmentation failed
            // In order to avoid infinite loop add the complete child
            addStartPart(startPart);
            return null; // In this case the caller should use its remaining part
        }
    }
    
    /**
     * Break either the given view split or a child view (if split is null).
     * 
     * @param part non-null part or full view to be broken.
     * @param allowWider allow wider start part than the boundaries allow
     * (normally such breaking would be refused).
     * @return view split or null if it cannot be performed.
     */
    private ViewSplit breakView(ViewPart part, boolean allowWider) {
        // Do breaking by first having a fragment starting at end offset of the previous broken part.
        // This is compatible with the FlowView way of views breaking
        EditorView view = part.view;
        int viewStartOffset = view.getStartOffset();
        float breakViewX = (float) (childX + part.xShift);
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  breakView<").append(viewStartOffset). // NOI18N
                    append(",").append(viewStartOffset + view.getLength()).append("> x="). // NOI18N
                    append(breakViewX).append(" W=").append(availableWidth - wrapLineX).append(" => "); // NOI18N
        }
        EditorView startView = (EditorView) view.breakView(View.X_AXIS, viewStartOffset,
                breakViewX,
                availableWidth - wrapLineX);
        if (startView != null && startView != view) {
            assert (startView.getStartOffset() == viewStartOffset) : "startPart.getStartOffset()=" + // NOI18N
                    startView.getStartOffset() + " != viewStartOffset=" + viewStartOffset; // NOI18N
            int startViewLength = startView.getLength();
            int viewLength = view.getLength();
            if (startViewLength != viewLength) { // Otherwise it was not a real break
                if (logMsgBuilder != null) {
                    logMsgBuilder.append("startPart<").append(startView.getStartOffset()). // NOI18N
                            append(",").append(startView.getEndOffset()).append(">"); // NOI18N
                }
                float startViewWidth = startView.getPreferredSpan(View.X_AXIS);
                if (allowWider || startViewWidth <= availableWidth - wrapLineX) {
                    EditorView endView = (EditorView) view.createFragment(viewStartOffset + startViewLength,
                            viewStartOffset + viewLength);
                    if (endView != null && endView != view && endView.getLength() == viewLength - startViewLength) {
                        if (logMsgBuilder != null) {
                            logMsgBuilder.append("\n");
                        }
                    } else { // createFragment() failed
                        if (logMsgBuilder != null) {
                            logMsgBuilder.append("createFragment <" + (viewStartOffset+startViewLength) + // NOI18N
                                    "," + (viewStartOffset+viewLength) + "> not allowed by view\n"); // NOI18N
                                    
                        }
                        startView = null;
                        endView = null;
                    }
                    int index = (part.isPart()) ? part.index : 0;
                    return new ViewSplit(
                            new ViewPart(startView, startViewWidth, part.xShift, index),
                            new ViewPart(endView, endView.getPreferredSpan(View.X_AXIS),
                                    part.xShift + startViewWidth, index + 1)
                    );
                    
                } else {
                    if (logMsgBuilder != null) {
                        logMsgBuilder.append("Fragment too wide(pW=" + startViewWidth + // NOI18N
                                ">aW=" + availableWidth + "-x=" + wrapLineX + ")\n"); // NOI18N
                    }
                }
            } else {
                if (logMsgBuilder != null) {
                    logMsgBuilder.append("startPart same length as view\n"); // NOI18N
                }
            }
        } else {
            if (logMsgBuilder != null) {
                logMsgBuilder.append("Break not allowed by view\n"); // NOI18N
            }
        }
        return null;
    }
    
    /**
     * Attempt to fragment child view.
     * @param part if it's a full child view then the split contains split of the full view.
     *  If it's a real view part then the result's startPart contains
     *  a part starting at the given part's offset and ending at breakOffset and the endPart
     *  starts at breakOffset but extends till end of full view (not the given part).
     * @param breakOffset offset where the start fragment will end and end part fragment will start.
     * @return view split or null if fragmenting cannot be performed.
     */
    private ViewSplit createFragment(ViewPart part, int breakOffset, boolean allowWider) {
        EditorView view = (part.isPart()) ? pView.getEditorView(childIndex) : part.view;
        int fragStartOffset = part.view.getStartOffset();
        int viewEndOffset = view.getEndOffset();
        assert (fragStartOffset < breakOffset) : "viewStartOffset=" + fragStartOffset + // NOI18N
                " >= breakOffset" + breakOffset; // NOI18N
        assert (breakOffset < viewEndOffset) : "breakOffset=" + breakOffset + // NOI18N
                " >= viewEndOffset" + viewEndOffset; // NOI18N
        EditorView startView = (EditorView) view.createFragment(fragStartOffset, breakOffset);
        assert (startView != null);
        if (startView != view) {
            if (logMsgBuilder != null) {
                logMsgBuilder.append(" createFragment<").append(startView.getStartOffset()). // NOI18N
                        append(",").append(startView.getEndOffset()).append(">"); // NOI18N
            }
            float startViewWidth = startView.getPreferredSpan(View.X_AXIS);
            if (allowWider || startViewWidth <= availableWidth - wrapLineX) {
                EditorView endView = (EditorView) view.createFragment(breakOffset, viewEndOffset);
                assert (endView != null) : "endView == null"; // NOI18N
                if (endView != view) {
                    int index = (part.isPart()) ? part.index : 0;
                    return new ViewSplit(
                            new ViewPart(startView, startViewWidth, part.xShift, index),
                            new ViewPart(endView, endView.getPreferredSpan(View.X_AXIS),
                            part.xShift + startViewWidth, index + 1));
                }
            }
        }
        return null;
    }
    
    /**
     * Get word info in case there's a word around boundaryOffset.
     * @param boundaryOffset there must be word's char before and after this offset
     *  to return non-null result.
     * @param startLimitOffset start offset of inspected area.
     * @return word info or null if a non-empty word cannot be created.
     */
    private WordInfo getWordInfo(int boundaryOffset, int startLimitOffset) {
        CharSequence docText = DocumentUtilities.getText(docView.getDocument());
        boolean prevCharIsWordPart = (boundaryOffset > startLimitOffset)
                && Character.isLetterOrDigit(docText.charAt(boundaryOffset - 1));
        int docTextLength;
        if (prevCharIsWordPart && (boundaryOffset < (docTextLength = docText.length()))) {
            // Check if next char is word part as well
            // [TODO] Check surrogates
            boolean nextCharIsWordPart = Character.isLetterOrDigit(docText.charAt(boundaryOffset));
            if (nextCharIsWordPart) {
                int wordEndOffset;
                for (wordEndOffset = boundaryOffset + 1;
                        wordEndOffset < docTextLength; wordEndOffset++)
                {
                    // [TODO] Check surrogates
                    if (!Character.isLetterOrDigit(docText.charAt(wordEndOffset))) {
                        break;
                    }
                }
                return new WordInfo(docText, boundaryOffset, startLimitOffset, wordEndOffset);
            }
        }
        return null;
    }

    private void logWrapLineAndX() {
        logMsgBuilder.append(" to WL[").append(wrapLines.size()). // NOI18N
                append("] endX=").append(wrapLineX).append('\n'); // NOI18N
    }

    private static final class WordInfo {
        
        private CharSequence docText;

        private int boundaryOffset;

        private int startOffset;

        private int wordEndOffset;

        private int wordStartOffset = -1;
        
        WordInfo(CharSequence docText, int boundaryOffset, int startOffset, int wordEndOffset) {
            this.docText = docText;
            this.boundaryOffset = boundaryOffset;
            this.startOffset = startOffset;
            this.wordEndOffset = wordEndOffset;
        }
        
        int wordEndOffset() {
            return wordEndOffset;
        }
        
        int wordStartOffset() {
            if (wordStartOffset == -1) {
                for (wordStartOffset = boundaryOffset - 2; // boundaryOffset-1 already checked for word-char
                        wordStartOffset >= startOffset; wordStartOffset--) {
                    // [TODO] Check surrogates
                    if (!Character.isLetterOrDigit(docText.charAt(wordStartOffset))) {
                        break;
                    }
                }
                wordStartOffset++;
            }
            return wordStartOffset;
        }

        @Override
        public String toString() {
            return "WordInfo(" + wordStartOffset() + ", " + wordEndOffset() + ")";
        }
    }


    /**
     * Result of view (or part) splitting to two parts.
     */
    private static final class ViewSplit {
        
        final ViewPart startPart;
        
        final ViewPart endPart;
        
        ViewSplit(ViewPart startPart, ViewPart endPart) {
            this.startPart = startPart;
            this.endPart = endPart;
        }
        
    }

}
