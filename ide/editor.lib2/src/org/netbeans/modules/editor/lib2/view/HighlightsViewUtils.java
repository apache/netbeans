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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import org.netbeans.api.editor.caret.CaretInfo;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * Utilities related to HighlightsView and TextLayout management.
 * <br>
 * Unfortunately the TextLayout based on AttributedCharacterIterator does not handle
 * correctly italic fonts (at least on Mac it renders background rectangle non-italicized).
 * Therefore child views with foreground that differs from text layout's "global" foreground
 * are rendered by changing graphic's color
 * and clipping the Graphics to textLayout.getVisualBounds() of the part.
 *
 * @author Miloslav Metelka
 */

public class HighlightsViewUtils {

    private HighlightsViewUtils() {
    }

    private static Color foreColor(AttributeSet attrs) {
        return (attrs != null)
                ? (Color) attrs.getAttribute(StyleConstants.Foreground)
                : null;
    }
    
    private static Color validForeColor(AttributeSet attrs, JTextComponent textComponent) {
        Color foreColor = foreColor(attrs);
        if (foreColor == null) {
            foreColor = textComponent.getForeground();
        }
        if (foreColor == null) {
            foreColor = Color.BLACK;
        }
        return foreColor;
    }

    static Shape indexToView(TextLayout textLayout, Rectangle2D textLayoutBounds,
             int index, Position.Bias bias, int maxIndex, Shape alloc)
    {
        if (textLayout == null) {
            return alloc; // Leave given bounds
        }
        assert (maxIndex <= textLayout.getCharacterCount()) : "textLayout.getCharacterCount()=" + // NOI18N
                textLayout.getCharacterCount() + " < maxIndex=" + maxIndex; // NOI18N
        // If offset is >getEndOffset() use view-end-offset - otherwise it would throw exception from textLayout.getCaretInfo()
	int charIndex = Math.min(index, maxIndex);
        // When e.g. creating fold-preview the offset can be < startOffset
        charIndex = Math.max(charIndex, 0);
        TextHitInfo startHit;
        TextHitInfo endHit;
        if (bias == Position.Bias.Forward) {
            startHit = TextHitInfo.leading(charIndex);
        } else { // backward bias
            startHit = TextHitInfo.trailing(charIndex - 1);
        }
        endHit = (charIndex < maxIndex) ? TextHitInfo.trailing(charIndex) : startHit;
        if (textLayoutBounds == null) {
            textLayoutBounds = ViewUtils.shapeAsRect(alloc);
        }
        return TextLayoutUtils.getRealAlloc(textLayout, textLayoutBounds, startHit, endHit);
    }

    static int viewToIndex(TextLayout textLayout, double x, Shape alloc, Position.Bias[] biasReturn) {
        Rectangle2D bounds = ViewUtils.shapeAsRect(alloc);
        TextHitInfo hitInfo = x2Index(textLayout, (float)(x - bounds.getX()));
        if (biasReturn != null) {
            biasReturn[0] = hitInfo.isLeadingEdge() ? Position.Bias.Forward : Position.Bias.Backward;
        }
        return hitInfo.getInsertionIndex();
    }

    static TextHitInfo x2Index(TextLayout textLayout, float x) {
        TextHitInfo hit;
        hit = textLayout.hitTestChar(x, 0);
        // Use forward bias only since BaseCaret and other code is not sensitive to backward bias yet
        if (!hit.isLeadingEdge()) {
            hit = TextHitInfo.leading(hit.getInsertionIndex());
        }
        return hit;
    }
    
    static double getMagicX(DocumentView docView, EditorView view, int offset, Bias bias, Shape alloc) {
        JTextComponent textComponent = docView.getTextComponent();
        if (textComponent == null) {
            return 0d;
        }
        Caret caret = textComponent.getCaret();
        Point magicCaretPoint = null;
        if(caret != null) {
            if(caret instanceof EditorCaret) {
                EditorCaret editorCaret = (EditorCaret) caret;
                CaretInfo info = editorCaret.getCaretAt(offset);
                magicCaretPoint = (info != null) ? info.getMagicCaretPosition() : null;
            } else {
                magicCaretPoint = caret.getMagicCaretPosition();
            }
        }
        double x;
        if (magicCaretPoint == null) {
            Shape offsetBounds = view.modelToViewChecked(offset, alloc, bias);
            if (offsetBounds == null) {
                x = 0d;
            } else {
                x = offsetBounds.getBounds2D().getX();
            }
        } else {
            x = magicCaretPoint.x;
        }
        return x;
    }

    static int getNextVisualPosition(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet,
            TextLayout textLayout, int textLayoutOffset, int viewStartOffset, int viewLength, DocumentView docView)
    {
        int retOffset = -1;
        biasRet[0] = Bias.Forward; // BaseCaret ignores bias
        TextHitInfo currentHit, nextHit;
        switch (direction) {
            case View.EAST:
                if (offset == -1) { // Entering view from the left.
                    // Assuming TextLayout only holds RTL or LTR text (HighlightsViewFactory should ensure)
                    retOffset = textLayout.isLeftToRight()
                            ? viewStartOffset
                            : viewStartOffset + viewLength - 1;
                } else { // Regular offset
                    int index = offset - viewStartOffset;
                    if (index >= 0 && index <= viewLength) {
                        currentHit = TextHitInfo.afterOffset(index);
                        nextHit = textLayout.getNextRightHit(currentHit);
                        int insertionIndex;
                        if (nextHit != null && (insertionIndex = nextHit.getInsertionIndex()) != viewLength) {
                            retOffset = viewStartOffset + insertionIndex;
                        } // Leave retOffset == -1
                    } // Leave retOffset == -1
                }
                break;

            case View.WEST:
                if (offset == -1) { // Entering view from the right
                    retOffset = textLayout.isLeftToRight()
                            ? viewStartOffset + viewLength - 1
                            : viewStartOffset;
                } else { // Regular offset
                    int index = offset - viewStartOffset;
                    if (index >= 0 && index <= viewLength) {
                        currentHit = TextHitInfo.afterOffset(index);
                        nextHit = textLayout.getNextLeftHit(currentHit);
                        if (nextHit != null) {
                            int insertionIndex = nextHit.getInsertionIndex();
                            // Handle RTL
                            if (textLayout.isLeftToRight() || insertionIndex != viewLength) {
                                retOffset = viewStartOffset + insertionIndex;
                            }
                        } // Leave retOffset == -1
                    } // Leave retOffset == -1
                }
                break;

            case View.NORTH:
            case View.SOUTH:
                break; // returns -1
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return retOffset;
    }

    /**
     * Paint text layout that corresponds fully or partially to the given highlights view.
     *
     * @param g graphics
     * @param textLayoutAlloc
     * @param clipBounds
     * @param docView
     * @param view
     * @param textLayout
     * @param startIndex first index inside textLayout to be rendered.
     * @param endIndex end index inside textLayout to be rendered.
     */
    static void paintHiglighted(Graphics2D g, Shape textLayoutAlloc, Rectangle clipBounds,
            DocumentView docView, EditorView view, int viewStartOffset,
            TextLayout textLayout, int textLayoutOffset,
            int startIndex, int endIndex)
    {
        Rectangle2D textLayoutRectReadonly = ViewUtils.shapeAsRect(textLayoutAlloc);
        PaintState paintState = PaintState.save(g);
        Shape origClip = g.getClip();
        try {
            // Render individual parts of the text layout with the correct highlighting
            // 1. Only the whole text layout can be rendered by its TL.draw() (in a single color)
            // 2. when TL is rendered in a color (e.g. black) it cannot be over-rendered
            //    in another color (for custom foreground) since it would look blurry.
            //
            // Therefore do the rendering in the following way:
            // 1. Collect bounds of each part (use map to get less TL.draw() invocations).
            // 2. Render background of part's bounds by clipping to collected shape.
            // 3. Render part's text in custom color by clipping of rendering of whole TL to collected shape.
            //
            JTextComponent textComponent = docView.getTextComponent();
            HighlightsSequence highlights = docView.getPaintHighlights(view,
                    textLayoutOffset + startIndex - viewStartOffset);
            // There was an aggregation of rendered parts with the same foreground color
            // into a compound java.awt.geom.Area. All areas were rendered at the end of the process.
            // That decreased TL.draw() invocations 
            // Unfortunately using java.awt.geom.Area on Mac OSX results in white horizontal lines
            // throughout the rendered text (when dragging vertical scrollbar slowly).
            // Therefore the aggregation optimization was abandoned and removed from the code.
            boolean done = false;
            while (!done && highlights.moveNext()) {
                int hiStartOffset = highlights.getStartOffset();
                int hiEndOffset = Math.min(highlights.getEndOffset(), textLayoutOffset + endIndex);
                if (hiEndOffset <= hiStartOffset) {
                    break;
                }
                // For visualized TABs it is necessary to render each TAB char text layout individually
                int renderEndOffset;
                do {
                    renderEndOffset = hiEndOffset;
                    AttributeSet attrs = highlights.getAttributes();
                    Shape renderPartAlloc;
                    // For nonPrinting TABs display use a special text-layout rectangle
                    Rectangle2D specialTextLayoutRect = null;
                    TextHitInfo startHit = TextHitInfo.leading(hiStartOffset - textLayoutOffset);
                    TextHitInfo endHit = TextHitInfo.leading(renderEndOffset - textLayoutOffset);
                    renderPartAlloc = TextLayoutUtils.getRealAlloc(textLayout, textLayoutRectReadonly, startHit, endHit);
                    if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER)) {
                        ViewHierarchyImpl.PAINT_LOG.finer("      View-Id=" + view.getDumpId() + // NOI18N
                                ", startOffset=" + view.getStartOffset() + // NOI18N
                                ", Fragment: hit<" + // NOI18N
                                startHit.getCharIndex() + "," + endHit.getCharIndex() + // NOI18N
                                ">, text='" + DocumentUtilities.getText(docView.getDocument()).subSequence( // NOI18N
                                hiStartOffset, renderEndOffset) + "', fAlloc=" + // NOI18N
                                ViewUtils.toString(renderPartAlloc.getBounds()) + ", Ascent=" + // NOI18N
                                ViewUtils.toStringPrec1(docView.op.getDefaultAscent()) + ", Color=" + // NOI18N
                                ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                    }
                    Rectangle2D renderPartBounds = renderPartAlloc.getBounds();
                    boolean hitsClip = (clipBounds == null) || renderPartAlloc.intersects(clipBounds);
                    if (hitsClip) {
                        // First render background and background related highlights
                        // Do not g.clip() before background is filled since otherwise there would be
                        // painting artifacts for italic fonts (one-pixel slanting lines) at certain positions.
                        fillBackground(g, renderPartAlloc, attrs, textComponent);
                        // Clip to part's alloc since textLayout.draw() renders fully the whole text layout
                        g.clip(renderPartAlloc);
                        paintBackgroundHighlights(g, renderPartAlloc, attrs, docView);
                        // Render foreground with proper color
                        g.setColor(HighlightsViewUtils.validForeColor(attrs, textComponent));
                        Object strikeThroughValue = (attrs != null)
                                ? attrs.getAttribute(StyleConstants.StrikeThrough)
                                : null;
                        Rectangle2D tlRect = (specialTextLayoutRect != null)
                                ? specialTextLayoutRect
                                : textLayoutRectReadonly;
                        paintTextLayout(g, tlRect, textLayout, docView);
                        if (strikeThroughValue != null) {
                            paintStrikeThrough(g, textLayoutRectReadonly, strikeThroughValue, attrs, docView);
                        }
                        g.setClip(origClip);

                    } else { // Part does not hit clip
                        if (clipBounds != null && (renderPartBounds.getX() > clipBounds.getMaxX())) {
                            done = true;
                            break;
                        }
                    }
                    hiStartOffset = renderEndOffset;
                } while (!done && renderEndOffset < hiEndOffset);
            }

        } finally {
            g.setClip(origClip);
            paintState.restore();
        }
    }

    static void paintNewline(Graphics2D g, Shape viewAlloc, Rectangle clipBounds,
            DocumentView docView, EditorView view, int viewStartOffset)
    {
        Rectangle2D viewRectReadonly = ViewUtils.shape2Bounds(viewAlloc);
        PaintState paintState = PaintState.save(g);
        Shape origClip = g.getClip();
        try {
            JTextComponent textComponent = docView.getTextComponent();
            SplitOffsetHighlightsSequence highlights = docView.getPaintHighlights(view, 0);
            boolean showNonPrintingChars = docView.op.isNonPrintableCharactersVisible();
            float charWidth = docView.op.getDefaultCharWidth();
            boolean logFiner = ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER);
            if (logFiner) {
                ViewHierarchyImpl.PAINT_LOG.finer("      Newline-View-Id=" + view.getDumpId() + // NOI18N
                        ", startOffset=" + viewStartOffset + ", alloc=" + viewAlloc + '\n' // NOI18N
                );

            }
            while (highlights.moveNext()) {
                int hiStartOffset = highlights.getStartOffset();
                int hiStartSplitOffset = highlights.getStartSplitOffset();
                int hiEndOffset = Math.min(highlights.getEndOffset(), viewStartOffset + 1); // TBD
                int hiEndSplitOffset = highlights.getEndSplitOffset();
                AttributeSet attrs = highlights.getAttributes();
                if (hiStartOffset > viewStartOffset) { // HL above newline
                    break;
                }

                double startX = viewRectReadonly.getX() + hiStartSplitOffset * charWidth;
                double endX = (hiEndOffset > viewStartOffset)
                        ? viewRectReadonly.getMaxX()
                        : Math.min(viewRectReadonly.getX() + hiEndSplitOffset * charWidth, viewRectReadonly.getMaxX());
                Rectangle2D.Double renderPartRect = new Rectangle2D.Double(startX, viewRectReadonly.getY(), endX - startX, viewRectReadonly.getHeight());
                fillBackground(g, renderPartRect, attrs, textComponent);
                boolean hitsClip = (clipBounds == null) || renderPartRect.intersects(clipBounds);
                if (hitsClip) {
                    // First render background and background related highlights
                    // Do not g.clip() before background is filled since otherwise there would be
                    // painting artifacts for italic fonts (one-pixel slanting lines) at certain positions.
                    // Clip to part's alloc since textLayout.draw() renders fully the whole text layout
                    g.clip(renderPartRect);
                    paintBackgroundHighlights(g, renderPartRect, attrs, docView);
                    // Render foreground with proper color
                    g.setColor(HighlightsViewUtils.validForeColor(attrs, textComponent));
                    Object strikeThroughValue = (attrs != null)
                            ? attrs.getAttribute(StyleConstants.StrikeThrough)
                            : null;
                    if (showNonPrintingChars && hiStartSplitOffset == 0) { // First part => render newline char visible representation
                        TextLayout textLayout = docView.op.getNewlineCharTextLayout();
                        if (textLayout != null) {
                            paintTextLayout(g, renderPartRect, textLayout, docView);
                        }
                    }
                    if (strikeThroughValue != null) {
                        paintStrikeThrough(g, viewRectReadonly, strikeThroughValue, attrs, docView);
                    }
                    g.setClip(origClip);
                }
                if (logFiner) {
                    ViewHierarchyImpl.PAINT_LOG.finer("        Highlight <" + 
                            hiStartOffset + '_' + hiStartSplitOffset + "," + // NOI18N
                            hiEndOffset + '_' + hiEndSplitOffset + ">, Color=" + // NOI18N
                            ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                }
                if (clipBounds != null && (renderPartRect.getX() > clipBounds.getMaxX())) {
                    break;
                }
            }
        } finally {
            g.setClip(origClip);
            paintState.restore();
        }
    }

    static void paintTabs(Graphics2D g, Shape viewAlloc, Rectangle clipBounds,
            DocumentView docView, EditorView view, int viewStartOffset)
    {
        Rectangle2D viewRectReadonly = ViewUtils.shape2Bounds(viewAlloc);
        PaintState paintState = PaintState.save(g);
        Shape origClip = g.getClip();
        try {
            JTextComponent textComponent = docView.getTextComponent();
            SplitOffsetHighlightsSequence highlights = docView.getPaintHighlights(view, 0);
            boolean showNonPrintingChars = docView.op.isNonPrintableCharactersVisible();
            float charWidth = docView.op.getDefaultCharWidth();
            int viewEndOffset = viewStartOffset + view.getLength();
            boolean logFiner = ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER);
            if (logFiner) {
                ViewHierarchyImpl.PAINT_LOG.finer("      Tab-View-Id=" + view.getDumpId() + // NOI18N
                        ", startOffset=" + viewStartOffset + ", alloc=" + viewAlloc + '\n' // NOI18N
                );
            }
            int tabCharOffset = viewStartOffset; // Currently processed offset
            double tabCharX = viewRectReadonly.getX();
            Rectangle2D nextTabCharRectReadonly = ViewUtils.shapeAsRect(view.modelToViewChecked(tabCharOffset + 1, viewAlloc, Bias.Forward));
            double nextTabCharX = nextTabCharRectReadonly.getX();
            boolean done = false;
            while (!done && highlights.moveNext()) {
                int hiStartOffset = highlights.getStartOffset();
                int hiStartSplitOffset = highlights.getStartSplitOffset();
                int hiEndOffset = Math.min(highlights.getEndOffset(), viewEndOffset); // TBD
                int hiEndSplitOffset = highlights.getEndSplitOffset();
                AttributeSet attrs = highlights.getAttributes();
                int fragStartOffset = hiStartOffset;
                int fragStartSplitOffset = hiStartSplitOffset;
                int fragEndOffset;
                int fragEndSplitOffset;
                boolean fetchNextTab;
                boolean hiDone = false;
                while (!hiDone) {
                    if (hiEndOffset > tabCharOffset + 1 || (hiEndOffset == tabCharOffset + 1 && hiEndSplitOffset > 0)) {
                        // Highlight spans tabs
                        fragEndOffset = tabCharOffset + 1;
                        fragEndSplitOffset = 0;
                        fetchNextTab = true;
                    } else { // Highlight within current tab char
                        fragEndOffset = hiEndOffset;
                        fragEndSplitOffset = hiEndSplitOffset;
                        fetchNextTab = (hiEndOffset == tabCharOffset + 1);
                        hiDone = true;
                    }
                    double fragX = tabCharX + fragStartSplitOffset * charWidth;
                    double fragEndX = (fragEndOffset != tabCharOffset) // frag starts at next char
                        ? nextTabCharX
                        : Math.min(tabCharX + hiEndSplitOffset * charWidth, nextTabCharX);
                    Rectangle2D fragRect = new Rectangle2D.Double(fragX, viewRectReadonly.getY(), fragEndX - fragX, viewRectReadonly.getHeight());
                    boolean hitsClip = (clipBounds == null) || fragRect.intersects(clipBounds);
                    String logInfo = logFiner
                            ? "<" + fragStartOffset + '_' + fragStartSplitOffset + "," + // NOI18N
                                    fragEndOffset + '_' + fragEndSplitOffset + ">  Rect=" + ViewUtils.toString(fragRect) // NOI18N
                            : null;
                    boolean clipNarrow = false;
                    if (hitsClip) {
                        fillBackground(g, fragRect, attrs, textComponent);
                        if (logFiner) {
                            ViewHierarchyImpl.PAINT_LOG.finer("            Fragment-fill-background " +
                                    logInfo + " Color=" + ViewUtils.toString(g.getColor()) + '\n'); // NOI18N
                        }
                        // First render background and background related highlights
                        // Do not g.clip() before background is filled since otherwise there would be
                        // painting artifacts for italic fonts (one-pixel slanting lines) at certain positions.
                        // Clip to part's alloc since textLayout.draw() renders fully the whole text layout
                        g.clip(fragRect);
                        clipNarrow = true;
                        paintBackgroundHighlights(g, fragRect, attrs, docView);
                    } else {
                        if (logFiner) {
                            ViewHierarchyImpl.PAINT_LOG.finer("            Fragment-skipped-no-clip-hit " + logInfo + '\n'); // NOI18N
                        }
                    }
                    if (hitsClip && showNonPrintingChars && fragStartSplitOffset == 0) { // First part => render newline char visible representation
                        TextLayout textLayout = docView.op.getTabCharTextLayout(fragRect.getWidth());
                        // Render foreground with proper color
                        g.setColor(HighlightsViewUtils.validForeColor(attrs, textComponent));
                        Object strikeThroughValue = (attrs != null)
                                ? attrs.getAttribute(StyleConstants.StrikeThrough)
                                : null;
                        if (textLayout != null) {
                            if (logFiner) {
                                ViewHierarchyImpl.PAINT_LOG.finer("            Render TAB char textLayout with color=" + // NOI18N
                                        ViewUtils.toString(g.getColor()) + ", Rect=" + fragRect + '\n'); // NOI18N
                            }
                            paintTextLayout(g, fragRect, textLayout, docView);
                        }
                        if (strikeThroughValue != null) {
                            paintStrikeThrough(g, viewRectReadonly, strikeThroughValue, attrs, docView);
                        }
                    }
                    if (clipNarrow) {
                        g.setClip(origClip);
                    }
                    if (fetchNextTab) {
                        fetchNextTab = false;
                        tabCharOffset++;
                        if (tabCharOffset == viewEndOffset) {
                            hiDone = true;
                            done = true;
                        } else {
                            tabCharX = nextTabCharX;
                            nextTabCharX = (tabCharOffset == viewEndOffset - 1)
                                    ? viewRectReadonly.getMaxX()
                                    : (nextTabCharRectReadonly = ViewUtils.shapeAsRect(view.modelToViewChecked(tabCharOffset + 1,
                                            viewAlloc, Bias.Forward))).getX();
                        }
                    }
                    fragStartOffset = fragEndOffset;
                    fragStartSplitOffset = fragEndSplitOffset;
                    if (clipBounds != null && (fragRect.getX() > clipBounds.getMaxX())) {
                        hiDone = true;
                        done = true;
                    }
                } // fragments-while
            } // highlights-while
        } finally {
            g.setClip(origClip);
            paintState.restore();
        }
    }

    static void fillBackground(Graphics2D g, Shape partAlloc, AttributeSet attrs, JTextComponent c) {
        // Render background
        if (ViewUtils.applyBackgroundColor(g, attrs, c)) {
            // Fill the alloc (not allocBounds) since it may be non-rectangular
            g.fill(partAlloc);
        }
    }

    /**
     * Render border lines, underline or wave underline and text limit line (but do not clear background).
     * 
     * @param g
     * @param partAlloc
     * @param attrs
     * @param docView 
     */
    static void paintBackgroundHighlights(Graphics2D g, Shape partAlloc, AttributeSet attrs, DocumentView docView) {
        // Paint background
        Rectangle2D partAllocBounds = ViewUtils.shapeAsRect(partAlloc);
        // Also get integer coords for text limit line and other renderings
        int x = (int) partAllocBounds.getX();
        int y = (int) partAllocBounds.getY();
        int lastX = (int) (Math.ceil(partAllocBounds.getMaxX()) - 1);
        int lastY = (int) (Math.ceil(partAllocBounds.getMaxY()) - 1);
        paintTextLimitLine(g, docView, x, y, lastX, lastY);
        // Paint extra 
        if (attrs != null) {
            Color leftBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.LeftBorderLineColor);
            Color rightBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.RightBorderLineColor);
            Color topBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.TopBorderLineColor);
            Color bottomBorderLineColor = (Color) attrs.getAttribute(EditorStyleConstants.BottomBorderLineColor);

            // Possibly paint underline
            Object underlineValue = attrs.getAttribute(StyleConstants.Underline);
            if (underlineValue != null) {
                Color underlineColor;
                if (underlineValue instanceof Boolean) { // Correct swing-way
                    underlineColor = Boolean.TRUE.equals(underlineValue)
                            ? docView.getTextComponent().getForeground()
                            : null;
                } else { // NB bug - it's Color instance
                    underlineColor = (Color) underlineValue;
                }
                if (underlineColor != null) {
                    g.setColor(underlineColor);
                    Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                    float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                    g.fillRect(
                            (int) partAllocBounds.getX(),
                            (int) (partAllocBounds.getY() + docView.op.getDefaultAscent() + underlineAndStrike[0]),
                            (int) partAllocBounds.getWidth(),
                            (int) Math.max(1, Math.round(underlineAndStrike[1]))
                    );
                }
            }

            // Possibly paint wave underline
            Color waveUnderlineColor = (Color) attrs.getAttribute(EditorStyleConstants.WaveUnderlineColor);
            if (waveUnderlineColor != null && bottomBorderLineColor == null) { // draw wave underline
                g.setColor(waveUnderlineColor);
                float ascent = docView.op.getDefaultAscent();
                Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                int yU = (int)(partAllocBounds.getY() + underlineAndStrike[0] + ascent + 0.5);
                int wavePixelCount = (int) partAllocBounds.getWidth() + 1;
                if (wavePixelCount > 0) {
                    int[] waveForm = {0, 0, -1, -1};
                    int[] xArray = new int[wavePixelCount];
                    int[] yArray = new int[wavePixelCount];

                    int waveFormIndex = x % 4;
                    for (int i = 0; i < wavePixelCount; i++) {
                        xArray[i] = x + i;
                        yArray[i] = yU + waveForm[waveFormIndex];
                        waveFormIndex = (++waveFormIndex) & 3;
                    }
                    g.drawPolyline(xArray, yArray, wavePixelCount - 1);
                }
            }

            // Possibly paint an extra framing (e.g. for code templates)
            if (leftBorderLineColor != null) {
                g.setColor(leftBorderLineColor);
                g.drawLine(x, y, x, lastY);
            }
            if (rightBorderLineColor != null) {
                g.setColor(rightBorderLineColor);
                g.drawLine(lastX, y, lastX, lastY);
            }
            if (topBorderLineColor != null) {
                g.setColor(topBorderLineColor);
                g.drawLine(x, y, lastX, y);
            }
            if (bottomBorderLineColor != null) {
                g.setColor(bottomBorderLineColor);
                g.drawLine(x, lastY, lastX, lastY);
            }
        }
    }
    
    static void paintTextLimitLine(Graphics2D g, DocumentView docView, int x, int y, int lastX, int lastY) {
        int textLimitLineX = docView.op.getTextLimitLineX();
        if (textLimitLineX > 0 && textLimitLineX >= x && textLimitLineX <= lastX) {
            g.setColor(docView.op.getTextLimitLineColor());
            g.drawLine(textLimitLineX, y, textLimitLineX, lastY);
        }
    }

    /**
     * Paint strike-through line for a font currently set to the graphics
     * with the color currently set to the graphics.
     * <br>
     * It's assumed that the clipping is set appropriately because the method
     * renders whole textLayoutAlloc with the strike-through.
     *
     * @param g
     * @param textLayoutBounds
     * @param strikeThroughValue non-null value for StyleConstants.StrikeThrough attribute in attrs
     * @param attrs non-null attrs
     * @param docView
     */
    static void paintStrikeThrough(Graphics2D g, Rectangle2D textLayoutBounds,
            Object strikeThroughValue, AttributeSet attrs, DocumentView docView)
    {
        Color strikeThroughColor;
        if (strikeThroughValue instanceof Boolean) { // Correct swing-way
            JTextComponent c = docView.getTextComponent();
            strikeThroughColor = Boolean.TRUE.equals(strikeThroughValue) ? g.getColor() : null;
        } else { // NB bug - it's Color instance
            strikeThroughColor = (Color) strikeThroughValue;
        }
        if (strikeThroughColor != null) {
            Color origColor = g.getColor();
            try {
                g.setColor(strikeThroughColor);
                Font font = ViewUtils.getFont(attrs, docView.getTextComponent().getFont());
                float[] underlineAndStrike = docView.op.getUnderlineAndStrike(font);
                g.fillRect(
                        (int) textLayoutBounds.getX(),
                        (int) (textLayoutBounds.getY() + docView.op.getDefaultAscent() + underlineAndStrike[2]), // strikethrough offset
                        (int) textLayoutBounds.getWidth(),
                        (int) Math.max(1, Math.round(underlineAndStrike[3])) // strikethrough thickness
                );
            } finally {
                g.setColor(origColor);
            }
        }
    }

    static void paintTextLayout(Graphics2D g, Rectangle2D textLayoutBounds,
            TextLayout textLayout, DocumentView docView)
    {
        float x = (float) textLayoutBounds.getX();
        float ascentedY = (float) (textLayoutBounds.getY() + docView.op.getDefaultAscent());
        // TextLayout is unable to do a partial render
        // Both x and ascentedY should already be floor/ceil-ed
        textLayout.draw(g, x, ascentedY);
    }

    static View breakView(int axis, int breakPartStartOffset, float x, float len,
            HighlightsView fullView, int partShift, int partLength, TextLayout partTextLayout)
    {
        if (axis == View.X_AXIS) {
            DocumentView docView = fullView.getDocumentView();
            // [TODO] Should check for RTL text
            assert (partTextLayout != null) : "Null partTextLayout";
            if (docView != null && partLength > 1) {
                // The logic
                int fullViewStartOffset = fullView.getStartOffset();
                int partStartOffset = fullViewStartOffset + partShift;
                if (breakPartStartOffset - partStartOffset < 0 || breakPartStartOffset - partStartOffset > partLength) {
                    throw new IllegalArgumentException("offset=" + breakPartStartOffset + // NOI18N
                            "partStartOffset=" + partStartOffset + // NOI18N
                            ", partLength=" + partLength // NOI18N
                    );
                }
                // Compute charIndex relative to given textLayout
                int breakCharIndex = breakPartStartOffset - partStartOffset;
                assert (breakCharIndex >= 0);
                float breakCharIndexX;
                if (breakCharIndex != 0) {
                    TextHitInfo hit = TextHitInfo.leading(breakCharIndex);
                    float[] locs = partTextLayout.getCaretInfo(hit);
                    breakCharIndexX = locs[0];
                } else {
                    breakCharIndexX = 0f;
                }
                TextHitInfo hitInfo = x2Index(partTextLayout, breakCharIndexX + len);
                // Check that the width is not too wide
                float[] locs = partTextLayout.getCaretInfo(hitInfo);
                float endX = locs[0];
                if (endX - breakCharIndexX > len) {
                    if (hitInfo.getCharIndex() > 0) {
                        hitInfo = TextHitInfo.leading(hitInfo.getCharIndex() - 1);
                    }
                }

                int breakPartEndOffset = partStartOffset + hitInfo.getCharIndex();
                if (breakPartEndOffset > breakPartStartOffset) {
                    // Now perform corrections if wrapping at word boundaries is required
                    if (docView.op.getLineWrapType() == LineWrapType.WORD_BOUND) {
                        CharSequence paragraph = DocumentUtilities.getText(docView.getDocument())
                                .subSequence(breakPartStartOffset, partStartOffset + partLength);
                        /* Don't enable allowWhitespaceBeyondEnd if we are printing the line
                        continuation character
                        (see DocumentViewOp.getLineContinuationCharTextLayout), since the latter
                        would usually then end up beyond the edge of the editor viewport. */
                        boolean allowWhitespaceBeyondEnd =
                                !docView.op.isNonPrintableCharactersVisible();
                        breakPartEndOffset = adjustBreakOffsetToWord(paragraph,
                                breakPartEndOffset - breakPartStartOffset,
                                allowWhitespaceBeyondEnd) + breakPartStartOffset;
                    }
                }

                // Length must be > 0; BTW TextLayout can't be constructed with empty string.
                boolean doNotBreak =
                    // No need to split the line in two if the first part would be empty.
                    (breakPartEndOffset - breakPartStartOffset == 0) ||
                    // No need to split the line in two if the second part would be empty.
                    (breakPartEndOffset - breakPartStartOffset >= partLength);
//                if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
//                    ViewHierarchyImpl.BUILD_LOG.fine("HV.breakView(): <"  + partStartOffset + // NOI18N
//                            "," + (partStartOffset+partLength) + // NOI18N
//                        "> => <" + breakPartStartOffset + "," + (partStartOffset+breakPartEndOffset) + // NOI18N
//                        ">, x=" + x + ", len=" + len + // NOI18N
//                        ", charIndexX=" + breakCharIndexX + "\n"); // NOI18N
//                }
                if (doNotBreak) {
                    return null;
                }
                return new HighlightsViewPart(fullView, breakPartStartOffset - fullViewStartOffset,
                        breakPartEndOffset - breakPartStartOffset);
            }
        }
        return null;
    }

    // Package-private for testing.
    /**
     * Calculate the position at which to break a line in a paragraph. A break offset of X means
     * that the character with index (X-1) in {@code paragraph} will be the last one on the physical
     * line.
     *
     * <p>The current implementation avoids creating lines with leading whitespace (when words are
     * separated by at most one whitespace character), allows lines to be broken after hyphens, and,
     * if {@code allowWhitespaceBeyondEnd} is true, allows one whitespace character to extend beyond
     * the preferred break width to make use of all available horizontal space. Very long
     * unbreakable words may extend beyond the preferred break offset regardless of the setting of
     * {@code allowWhitespaceBeyondEnd}.
     *
     * <p>It was previously considered to allow an arbitrary number of whitespace characters to
     * trail off the end of each wrap line, rather than just one. In the end, it turned out to be
     * better to limit this to just one character, as this conveniently avoids the need to ever
     * position the visual text caret outside the word-wrapped editor viewport (except in cases of
     * very long unbreakable words).
     *
     * @param paragraph a long line of text to be broken, i.e. a paragraph, or the remainder of a
     *        paragraph if some of its initial lines of wrapped text have already been laid out
     * @param preferredMaximumBreakOffset the preferred maximum break offset
     * @param allowWhitespaceBeyondEnd if true, allow one whitespace character to extend beyond
     *        {@code preferredMaximumBreakOffset} even when this could be avoided by choosing a
     *        smaller break offset
     */
    static int adjustBreakOffsetToWord(CharSequence paragraph,
            final int preferredMaximumBreakOffset, boolean allowWhitespaceBeyondEnd)
    {
        if (preferredMaximumBreakOffset < 0) {
            throw new IllegalArgumentException();
        }
        if (preferredMaximumBreakOffset > paragraph.length()) {
            throw new IllegalArgumentException();
        }
        /* BreakIterator.getLineInstance already seems to have a cache; creating a new instance here
        is just the cost of BreakIterator.clone(). So don't bother trying to cache the BreakIterator
        here. */
        BreakIterator bi = BreakIterator.getLineInstance(Locale.US);
        /* Use CharSequenceCharacterIterator to avoid copying the entire paragraph string every
        time. */
        bi.setText(new CharSequenceCharacterIterator(paragraph));

        int ret;
        if (preferredMaximumBreakOffset == 0) {
            // Skip forward to next boundary.
            ret = 0;
        } else if (
            allowWhitespaceBeyondEnd && preferredMaximumBreakOffset < paragraph.length() &&
            Character.isWhitespace(paragraph.charAt(preferredMaximumBreakOffset)))
        {
            // Allow one whitespace character to extend beyond the preferred break offset.
            return preferredMaximumBreakOffset + 1;
        } else {
            // Skip backwards to previous boundary.
            ret = bi.isBoundary(preferredMaximumBreakOffset)
                ? preferredMaximumBreakOffset
                : bi.preceding(preferredMaximumBreakOffset);
            if (ret == BreakIterator.DONE) {
                return preferredMaximumBreakOffset;
            }
        }
        if (ret == 0) {
            // Skip forward to next boundary (for words longer than the preferred break offset).
            ret = preferredMaximumBreakOffset > 0 && bi.isBoundary(preferredMaximumBreakOffset)
                ? preferredMaximumBreakOffset
                : bi.following(preferredMaximumBreakOffset);
            if (ret == BreakIterator.DONE) {
                ret = preferredMaximumBreakOffset;
            }
            /* The line-based break iterator will include whitespace trailing a word as well. Strip
            this off so we can apply our own policy here. */
            int retBeforeTrim = ret;
            while (ret > preferredMaximumBreakOffset &&
                Character.isWhitespace(paragraph.charAt(ret - 1)))
            {
                ret--;
            }
            /* If allowWhitespaceBeyondEnd is true, allow at most one whitespace character to trail
            the word at the end. */
            if ((allowWhitespaceBeyondEnd || ret == 0) && retBeforeTrim > ret) {
                ret++;
            }
        }
        return ret;
    }
}
