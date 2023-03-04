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
package org.netbeans.core.output2;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import javax.swing.text.Position.Bias;
import org.openide.awt.GraphicsUtils;
import org.openide.util.Exceptions;

/**
 * A custom Swing text View which supports line wrapping.  The default Swing
 * line wrapping code is not appropriate for our purposes - particularly, it
 * will iterate the entire buffer multiple times to determine break positions.
 * Since it would defeat the purpose of using a memory mapped file to have to
 * pull the entire thing into memory every time it's painted or its size should
 * be calculated, we have this class instead.
 * <p>
 * All position/line calculations this view does are based on the integer array
 * of line offsets kept by the writer's Lines object.
 *
 * @author Tim Boudreau, Martin Entlicher
 */
public class WrappedTextView extends View implements TabExpander {

    static final int TAB_SIZE = 8;  // The default tab size

    /**
     * The component we will paint
     */
    private JTextComponent comp;
    /**
     * Precalculated number of characters per line
     */
    private int charsPerLine = 80;
    /**
     * Precalculated font descent, used to adjust the bounding rectangle of
     * characters as returned by modelToView. This is added to the y position 
     * of character rectangles in modelToView() so painting the selection 
     * includes the complete character and does not interfere with the line above.
     */
    private int fontDescent = 4;
    /**
     * A scratch Segment object to avoid allocation while painting lines
     */
    private static final Segment SEGMENT = new Segment();
    /**
     * Precalculated width (in pixels) we are to paint into, the end being the wrap point
     */
    private int width = 0;
    /**
     * Flag indicating we need to recalculate metrics before painting
     */
    private boolean changed = true;
    /**
     * Precalculated width of a single character (assumes fixed width font).
     */
    private int charWidth = 12;
    /**
     * Precalculated height of a single character (assumes fixed width font).
     */
    private int charHeight = 7;
    /**
     * A scratchpad int array
     */
    static final int[] ln = new int[3];
    /**
     * Flag indicating that the antialiasing flag is set on the Graphics object.
     * We do a somewhat prettier arrow if it is.
     */
    private boolean aa = false;

    static final Color arrowColor = new Color (80, 162, 80);

    int tabSize;
    int tabBase;
    private int tabOffsetX = 0;
    
    private final PropertyChangeListener propertyChangeListener;

    public WrappedTextView(Element elem, JTextComponent comp,
            PropertyChangeListener propertyChangeListener1) {
        super(elem);
        this.comp = comp;
        this.propertyChangeListener = propertyChangeListener1;
    }


    public float getPreferredSpan(int axis) {
        OutputDocument doc = odoc();
        float result = 0;
        if (doc != null) {
            updateWidth();
            switch (axis) {
                case X_AXIS :
                    result = charsPerLine;
                    break;
                case Y_AXIS :
                    result = doc.getLines().getLogicalLineCountIfWrappedAt(charsPerLine) * charHeight + fontDescent;
                    break;
                default :
                    throw new IllegalArgumentException (Integer.toString(axis));
            }
        }
        return result;
    }

    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    float viewWidth = -1;
    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (viewWidth != width) {
            viewWidth = width;
            updateMetrics();
        }
    }

    private int getTabSize() {
        //Integer i = (Integer) getDocument().getProperty(PlainDocument.tabSizeAttribute);
        //int size = (i != null) ? i.intValue() : TAB_SIZE;
        //return size;
        return TAB_SIZE;
    }

    @Override
    public float nextTabStop(float x, int tabOffset) {
        if (tabSize == 0) {
            return x;
        }
        int ntabs = (((int) x) - margin() + tabOffsetX) / tabSize;
        return margin() + ((ntabs + 1) * tabSize) - tabOffsetX;
    }

    void updateMetrics() {
        Font font = comp.getFont();
        FontMetrics fm = comp.getFontMetrics(font);
        charWidth = fm.charWidth('m'); //NOI18N
        charHeight = fm.getHeight();
        fontDescent = fm.getMaxDescent();

        Graphics2D g2d = ((Graphics2D) comp.getGraphics());
        if (g2d != null) {
            aa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING) ==
                    RenderingHints.VALUE_ANTIALIAS_ON;
        }
        tabSize = getTabSize() * charWidth;
        updateWidth();
    }

    /**
     * Get the component's document as an instance of OutputDocument, if it
     * is one, returning null if it is not (briefly it will not be after the
     * editor kit has been installed - this is unavoidable).
     *
     * @return An instance of OutputDocument or null.
     */
    private OutputDocument odoc() {
        Document doc = comp.getDocument();
        if (doc instanceof OutputDocument) {
            return (OutputDocument) doc;
        }
        return null;
    }

    private void updateWidth() {
        int oldCharPerWidth = charsPerLine;
        if (comp.getParent() instanceof JViewport) {
            JViewport jv = (JViewport) comp.getParent();
            width = jv.getExtentSize().width - (aa ? 18 : 17);
        } else {
            width = comp.getWidth() - (aa ? 18 : 17);
        }
        if (width < 0) {
            width = 0;
        }
        charsPerLine = width / charWidth;
        if (charsPerLine != oldCharPerWidth) {
            propertyChangeListener.propertyChange(new PropertyChangeEvent(this,
                    "charsPerLine", oldCharPerWidth, charsPerLine));    //NOI18N
        }
    }

    /**
     * Get the left hand margin required for printing line wrap decorations.
     *
     * @return A margin in pixels
     */
    private static int margin() {
        return 9;
    }

    public void paint(Graphics g, Shape allocation) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        
        comp.getHighlighter().paint(g);

        tabBase = ((Rectangle) allocation).x + margin();

        OutputDocument doc = odoc();
        if (doc != null) {
            Rectangle clip = g.getClipBounds();
            clip.y = Math.max (0, clip.y - charHeight);
            clip.height += charHeight * 2;

            int lineCount = doc.getElementCount();
            if (lineCount == 0) {
                return;
            }

            ln[0] = clip.y / charHeight;
            Lines lines = doc.getLines();
            lines.toPhysicalLineIndex(ln, charsPerLine);

            int firstline = ln[0];
            g.setColor (comp.getForeground());
            Segment seg = SwingUtilities.isEventDispatchThread() ? SEGMENT : new Segment();

            int selStart = comp.getSelectionStart();
            int selEnd = comp.getSelectionEnd();
            int y = (clip.y - (clip.y % charHeight) + charHeight);
            int maxVisibleChars = ((clip.height + charHeight - 1) / charHeight) * charsPerLine;
            
            try {
                for (int i = firstline; i < lines.getLineCount(); i++) {
                    if (y > clip.y + clip.height) {
                        return;
                    }
                    int visibleLine = lines.realToVisibleLine(i);
                    if (visibleLine < 0) {
                        continue;
                    }
                    int lineStart = doc.getLineStart(i);
                    int lineEnd = doc.getLineEnd (i);
                    int length = lineEnd - lineStart;
                    if (length == 0) {
                        y += charHeight;
                        continue;
                    }
                    length = lines.lengthWithTabs(i);
                    LineInfo info = lines.getLineInfo(i);

                    // get number of logical lines
                    int logicalLines = length <= charsPerLine ? 1 : 
                        (charsPerLine == 0 ? length : (length + charsPerLine - 1) / charsPerLine);
                    
                    // get current (first which we will draw) logical line
                    int currLogicalLine = (i == firstline && logicalLines > 0 && ln[1] > 0 ) ? ln[1] : 0;

                    int charpos = 0;
                    //int tabOverLine = 0;    // 0 or 1
                    int charsWithTabs = 0;
                    int arrowDrawn = currLogicalLine - 1;
                    int x = 0;
                    int remainCharsOnLogicalLine = charsPerLine;
                    
                    int logLineOffset;
                    if (currLogicalLine > 0) {
                        // shift lineStart to position of first logical line that will be drawn
                        // we have lineStart - offset of the beginning of the physical line
                        // we have to add (currLogicalLine * charsPerLine) characters with expanded TABs
                        // this corresponds to a different real number of characters
                        logLineOffset = currLogicalLine * charsPerLine;
                        int[] tabShiftPtr = new int[] { 0 };
                        logLineOffset = lines.getNumPhysicalChars(lineStart, logLineOffset, tabShiftPtr);
                        lineStart += logLineOffset;
                        if (tabShiftPtr[0] > 0) {
                            //tabOverLine = 1;
                            remainCharsOnLogicalLine -= tabShiftPtr[0];
                            x = tabShiftPtr[0] * charWidth;
                            charsWithTabs += tabShiftPtr[0];
                        }
                    } else {
                        logLineOffset = 0;
                    }
                    
                    // limit number of chars needed by estimation of maximum number of chars we need to repaint
                    length = Math.min(maxVisibleChars, length - logLineOffset);
                    int sourceLength = Math.min(maxVisibleChars, lineEnd - lineStart);

                    // get just small part of document we need (no need to get e.g. whole 10 MB line)
                    doc.getText(lineStart, sourceLength, seg);

                    tabOffsetX = charWidth * currLogicalLine * charsPerLine; //logLineOffset;
                    for (LineInfo.Segment ls : info.getLineSegments()) {
                        if (ls.getEnd() < logLineOffset) {
                            continue;
                        }
                        g.setColor(ls.getColor());
                        int shift = 0;
                        while (charpos < ls.getEnd() - logLineOffset && currLogicalLine < logicalLines) {
                            int lenToDraw = Math.min(remainCharsOnLogicalLine, ls.getEnd() - logLineOffset - charpos);
                            int charsToDraw = lenToDraw;
                            if (lenToDraw > 0) {
                                charsToDraw = getCharsForLengthWithTabs(seg.array, charpos, currLogicalLine * charsPerLine + shift, lenToDraw, remainCharsOnLogicalLine);// - tabOverLine;
                                if (currLogicalLine != logicalLines - 1 && arrowDrawn != currLogicalLine) {
                                    arrowDrawn = currLogicalLine;
                                    drawArrow(g, y, currLogicalLine == logicalLines - 2);
                                }
                                Color bg = ls.getCustomBackground();
                                drawText(seg, g, x, y, lineStart, charpos, selStart, charsToDraw, selEnd, bg);
                                if (ls.getListener() != null) {
                                    underline(g, seg, charpos, charsToDraw, x, y);
                                }
                            }
                            lenToDraw = getCharLengthWithTabs(seg.array, charpos, currLogicalLine * charsPerLine + shift, charsToDraw);
                            charpos += charsToDraw;
                            if (charsToDraw == 0) {
                                break; // Prevent livelock, see bug 230840.
                            }
                            charsWithTabs += lenToDraw;
                            remainCharsOnLogicalLine -= lenToDraw;
                            x += lenToDraw * charWidth;
                            shift += lenToDraw;
                            //tabOverLine = (remainCharsOnLogicalLine < 0) ? 1 : 0;
                            while(remainCharsOnLogicalLine <= 0) {
                                shift = -remainCharsOnLogicalLine;
                                remainCharsOnLogicalLine += charsPerLine;
                                currLogicalLine++;
                                x = shift * charWidth;
                                tabOffsetX += charWidth * (charsPerLine);// + shift);
                                y += charHeight;
                                if (y > clip.y + clip.height) {
                                    return;
                                }
                                if (shift > 0) {
                                    if (selStart != selEnd) {
                                        int realPos = lineStart + charpos;
                                        int a = Math.max(selStart, realPos);
                                        int b = Math.min(selEnd, realPos + charsToDraw);
                                        if (a < b) {
                                            drawSelection(g, 0, x, y);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (charsPerLine == 0 || charsWithTabs % charsPerLine != 0) {
                        y += charHeight;
                    }
                }
                tabOffsetX = 0;
            } catch (BadLocationException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    /**
     * Draw text
     *
     * @param seg A Segment object containing the text
     * @param g The graphics context
     * @param y The baseline in the graphics context
     * @param lineStart The character position at which the line starts
     * @param charpos The current character position within the segment
     * @param selStart The character index at which the selected range, if any, starts
     * @param lenToDraw The number of characters we'll draw before we're outside the clip rectangle
     * @param selEnd The end of the selected range of text, if any
     */
    private void drawText(Segment seg, Graphics g, int x, int y, int lineStart,
            int charpos, int selStart, int lenToDraw, int selEnd, Color bg) {
        Color clr = g.getColor();
        if (selStart != selEnd) {
            int realPos = lineStart + charpos;
            int a = Math.max(selStart, realPos);
            int b = Math.min(selEnd, realPos + lenToDraw);
            if (a < b) {
                realPos = odoc().getLines().getNumLogicalChars(lineStart, realPos - lineStart) + lineStart;
                a = odoc().getLines().getNumLogicalChars(lineStart, a - lineStart) + lineStart;
                b = odoc().getLines().getNumLogicalChars(lineStart, b - lineStart) + lineStart;
                int start = x + margin() + (a - realPos) * charWidth;
                int len = (b - a) * charWidth;
                int w = charsPerLine * charWidth;
                if (start - margin() + len > w) {
                    len = w - start + margin();
                }
                g.setColor (comp.getSelectionColor());
                g.fillRect (start, y + fontDescent - charHeight, len, charHeight);
                g.setColor (clr);
            }
        }
        //g.drawChars(seg.array, charpos, lenToDraw, margin() + x, y);
        int count = seg.count;
        int offset = seg.offset;
        seg.count = lenToDraw;
        seg.offset = charpos;
        drawTextBackground(g, clr, bg, selStart != selEnd, seg, x, y, charpos);
        Utilities.drawTabbedText(seg, margin() + x, y, g, this, charpos);
        seg.count = count;
        seg.offset = offset;
    }

    private void drawTextBackground(Graphics g, Color fg, Color bg,
            boolean selection, Segment seg, int x, int y, int charpos) {
        if (bg != null && !selection) {
            int w = Utilities.getTabbedTextWidth(
                    seg, g.getFontMetrics(), x, this, charpos);
            int h = g.getFontMetrics().getHeight();
            g.setColor(bg);
            g.fillRect(x + margin(), y - h + g.getFontMetrics().getDescent(),
                    w, h);
        }
        g.setColor(fg);
    }

    private void drawSelection(Graphics g, int x1, int x2, int y) {
        Color c = g.getColor();
        g.setColor (comp.getSelectionColor());
        g.fillRect (x1 + margin(), y + fontDescent - charHeight, x2 - x1, charHeight);
        g.setColor (c);
    }

    private void underline(Graphics g, Segment seg, int charpos, int lenToDraw, int x, int y) {
        if (!ExtPlainView.isLinkUndeliningEnabled(this)) {
            return;
        }
        int underlineStart = margin() + x;
        FontMetrics fm = g.getFontMetrics();
        int underlineEnd = underlineStart + fm.charsWidth(seg.array, charpos, lenToDraw);
        int underlineShift = fm.getDescent() - 1;
        g.drawLine (underlineStart, y + underlineShift, underlineEnd, y + underlineShift);
    }

    /**
     * Draw the decorations used with wrapped lines.
     *
     * @param g A graphics to paint into
     * @param y The y coordinate of the line as a font baseline position
     */
    private void drawArrow (Graphics g, int y, boolean drawHead) {
        Color c = g.getColor();
        g.setColor (arrowColor());

        int w = width + 15;
        y+=2;

        int rpos = aa ? 8 : 4;
        if (aa) {
            g.drawArc(w - rpos, y - (charHeight / 2), rpos + 1, charHeight, 265, 185);
            w++;
        } else {
            g.drawLine (w-rpos, y - (charHeight / 2), w, y - (charHeight / 2));
            g.drawLine (w, y - (charHeight / 2)+1, w, y + (charHeight / 2) - 1);
            g.drawLine (w-rpos, y + (charHeight / 2), w, y + (charHeight / 2));
        }
        if (drawHead) {
            rpos = aa ? 7 : 8;
            int[] xpoints = new int[] {
                w - rpos,
                w - rpos + 5,
                w - rpos + 5,
            };
            int[] ypoints = new int[] {
                y + (charHeight / 2),
                y + (charHeight / 2) - 5,
                y + (charHeight / 2) + 5,
            };
            g.fillPolygon(xpoints, ypoints, 3);
        }

        g.setColor (arrowColor());
        g.drawLine (1, y - (charHeight / 2), 5, y - (charHeight / 2));
        g.drawLine (1, y - (charHeight / 2), 1, y + (charHeight / 2));
        g.drawLine (1, y + (charHeight / 2), 5, y + (charHeight / 2));

        g.setColor (c);
    }

    /**
     * Get the color used for the line wrap arrow
     *
     * @return The arrow color
     */
    private static Color arrowColor() {
        return arrowColor;
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        Rectangle result = new Rectangle();
        result.setBounds (0, 0, charWidth, charHeight);
        OutputDocument od = odoc();
        if (od != null) {
            int line, start;
            synchronized (od.getLines().readLock()) {
                line = Math.max(0, od.getLines().getLineAt(pos));
                start = od.getLineStart(line);
            }

            int column = pos - start;

            column = od.getLines().getNumLogicalChars(start, column);

            int row = od.getLines().getLogicalLineCountAbove(line, charsPerLine);
            int end = getLineEnd(line, od.getLines());
            int len = od.getLines().getNumLogicalChars(start, end - start);
            //#104307
            if ((column >= charsPerLine)
                    && charsPerLine != 0) {
                row += (column % charsPerLine == 0 && column == len)
                        ? column / charsPerLine - 1
                        : column / charsPerLine;
                column = (column % charsPerLine == 0 && column == len)
                        ? charsPerLine
                        : column % charsPerLine;
            }
            result.y = (row * charHeight) + fontDescent;
            result.x = margin() + (column * charWidth);
//            System.err.println(pos + "@" + result.x + "," + result.y + " line " + line + " start " + start + " row " + row + " col " + column);
        }
        
        return result;
    }

    public int viewToModel(float x, float y, Shape a, Position.Bias[] biasReturn) {
        OutputDocument od = odoc();
        if (od != null) {
            int ix = Math.max((int) x - margin(), 0);
            int iy = (int) y - fontDescent;

            ln[0] = (iy / charHeight);
            od.getLines().toPhysicalLineIndex(ln, charsPerLine);
            int logicalLine = ln[0];
            int wraps = ln[2] - 1;

            int totalLines = od.getLines().getLineCount();
            if (totalLines == 0) {
                return 0;
            }
            if (logicalLine >= totalLines) {
                return od.getLength();
            }

            int lineStart = od.getLineStart(logicalLine);
            int lineLength = od.getLines().lengthWithTabs(logicalLine);
            int lineEnd = lineStart + lineLength;//od.getLineEnd(logicalLine);

            int column = ix / charWidth;
            if (column > lineLength) {
                column = lineLength;
            }

            int result = wraps > 0 ?
                Math.min(lineEnd, lineStart + (ln[1] * charsPerLine) + column)
                : Math.min(lineStart + column, lineEnd);
            Lines lines = od.getLines();
            result = lines.getNumPhysicalChars(lineStart, result - lineStart, null) + lineStart;
            result = Math.min (od.getLength(), result);
            return result;
/*            System.err.println ("ViewToModel " + ix + "," + iy + " = " + result + " physical ln " + physicalLine +
                    " logical ln " + logicalLine + " on wrap line " + ln[1] + " of " + wraps + " charsPerLine " +
                    charsPerLine + " column " + column + " line length " + lineLength);
//            System.err.println ("v2m: [" + ix + "," + iy + "] = " + result);
*/
        } else {
            return 0;
        }
    }

    private int getCharLengthWithTabs(char[] array, int charpos, int tabLineOffset, int lenToDraw) {
        int n = Math.min(array.length, charpos + lenToDraw);
        int tabExpand = 0;
        for (int i = charpos; i < n; i++) {
            if ('\t' == array[i]) {
                int numSpaces = TAB_SIZE - (((i - charpos + tabLineOffset) + tabExpand) % TAB_SIZE);
                tabExpand += numSpaces - 1;
                lenToDraw += numSpaces - 1;
            }
        }
        return lenToDraw;
    }

    private int getCharsForLengthWithTabs(char[] array, int charpos, int tabLineOffset, int lenToDraw, int length) {
        int n = Math.min(array.length, charpos + lenToDraw);
        int lengthWithTab = 0;
        int tabExpand = 0;
        int i;
        for (i = charpos; i < n && lengthWithTab < length; i++) {
            if ('\t' == array[i]) {
                int numSpaces = TAB_SIZE - (((i - charpos + tabLineOffset) + tabExpand) % TAB_SIZE);
                tabExpand += numSpaces - 1;
                lengthWithTab += numSpaces;
            } else {
                lengthWithTab++;
            }
        }
        if (lengthWithTab > length && i > (charpos + 1) && array[i-1] != '\t') {
            i--;
        }
        return i - charpos;
    }

    /**
     * Replaces usage of slow
     * Utilities.getPositionAbove()/Utilities.getPositionBelow(), skips hidden
     * lines.
     */
    @Override
    public int getNextVisualPositionFrom(int pos, Bias b, Shape a,
            int direction, Bias[] biasRet) throws BadLocationException {
        Element elem = getElement();
        if (pos == -1) {
            pos = (direction == SOUTH || direction == EAST)
                    ? getStartOffset()
                    : (getEndOffset() - 1);
        }

        int lineIndex;
        int visibleLineIndex;
        int origLineIndex;
        PositionInfo pi;
        Lines lines = odoc().getLines();
        switch (direction) {
            case NORTH:
                pi = getPositionInfo(pos);
                if (pi.lineIndex > 0 || pi.innerRowIndex > 0) {
                    if (pi.innerRowIndex > 0) {
                        return jumpInLine(lines, pi, direction);
                    }
                    return jumpToLine(lines, pi, direction);
                }
                break;
            case SOUTH:
                pi = getPositionInfo(pos);
                if (pi.innerRowIndex < pi.innerRowsCount - 1) {
                    return jumpInLine(lines, pi, direction);
                }
                visibleLineIndex = lines.realToVisibleLine(pi.lineIndex);
                if (visibleLineIndex < elem.getElementCount() - 1) {
                    return jumpToLine(lines, pi, direction);
                }
                break;
            case WEST:
                origLineIndex = lines.getLineAt(pos);
                pos = Math.max(0, pos - 1);
                lineIndex = lines.getLineAt(pos);
                if (origLineIndex != lineIndex) {
                    int origVisibleLine = lines.realToVisibleLine(origLineIndex);
                    pos = elem.getElement(origVisibleLine - 1).getEndOffset() - 1;
                }
                break;
            case EAST:
                origLineIndex = lines.getLineAt(pos);
                pos = Math.min(pos + 1, elem.getEndOffset() - 1);
                lineIndex = lines.getLineAt(pos);
                if (origLineIndex != lineIndex) {
                    int origVisibleLine = lines.realToVisibleLine(origLineIndex);
                    pos = elem.getElement(origVisibleLine + 1).getStartOffset();
                }
                break;
            default:
                throw new IllegalArgumentException("Bad direction");    //NOI18N
        }
        return pos;
    }

    /**
     * Get info about a line. The offset of the returned position info will be
     * set to the first character of that line.
     */
    private PositionInfo getLineInfo(int lineIndex) {
        Lines lines = odoc().getLines();
        int lineStart = lines.getLineStart(lineIndex);
        return getPositionInfo(lines, lineIndex, lineStart, lineStart);
    }

    /**
     * Get info about a position and containing line.
     */
    private PositionInfo getPositionInfo(int offset) {
        Lines lines = odoc().getLines();
        int lineIndex = lines.getLineAt(offset);
        int lineStart = lines.getLineStart(lineIndex);
        return getPositionInfo(lines, lineIndex, lineStart, offset);
    }

    /**
     * Get info about a position. Should not be called directly.
     */
    private PositionInfo getPositionInfo(Lines lines, int lineIndex,
            int lineStart, int offset) {
        PositionInfo pi = new PositionInfo();

        pi.offset = offset;
        pi.lineIndex = lineIndex;
        pi.lineStart = lineStart;
        pi.lineEnd = getLineEnd(pi.lineIndex, lines);
        int lineLen = pi.lineEnd - pi.lineStart;
        int column = offset - pi.lineStart;
        int logicalColumn = lines.getNumLogicalChars(pi.lineStart, column);
        pi.logicalLineLength = lines.getNumLogicalChars(pi.lineStart, lineLen);
        int innerRowsCount = (pi.logicalLineLength / charsPerLine)
                + (pi.logicalLineLength % charsPerLine > 0 ? 1 : 0);
        pi.innerRowsCount = Math.max(1, innerRowsCount);
        pi.innerRowIndex = logicalColumn / charsPerLine
                - (pi.logicalLineLength > 0 && logicalColumn == pi.logicalLineLength
                && logicalColumn % charsPerLine == 0
                ? 1 : 0);
        if (lineLen > 0 && pi.lineEnd == offset
                && pi.logicalLineLength % charsPerLine == 0) {
            // handle last char in line
            pi.innerColumn = charsPerLine - (pi.innerRowsCount > 1 ? 1 : 0);
        } else {
            pi.innerColumn = lines.getNumLogicalChars(
                    pi.lineStart, offset - pi.lineStart) % charsPerLine;
        }
        return pi;
    }

    /**
     * Jump to appropriate position from a position in a neighboring line. The
     * line above/below is assumed to exist.
     *
     * @param pi Source position.
     * @param direction SwingConstants.NORTH for jumping to line above,
     * SwingConstants.SOUTH for line below.
     * @param lines The lines object.
     */
    private int jumpToLine(Lines lines, PositionInfo pi, int direction) {
        assert direction == NORTH || direction == SOUTH;

        int newRealLine = findNearestVisibleLine(lines, pi.lineIndex, direction);
        if (newRealLine < 0) {
            return pi.offset;
        }
        PositionInfo targetLine = getLineInfo(newRealLine);
        int newInnerRow = direction == NORTH
                ? targetLine.innerRowsCount - 1
                : 0;

        int logicalLineStart = targetLine.lineStart + lines.getNumPhysicalChars(
                targetLine.lineStart, newInnerRow * charsPerLine, null);

        int physicalColumn = lines.getNumPhysicalChars(logicalLineStart,
                pi.innerColumn, null);
        int physicalPos = fixPhysicalPosition(lines, logicalLineStart
                + physicalColumn, newInnerRow, targetLine.lineStart);
        return Math.min(physicalPos, targetLine.lineEnd);
    }

    /**
     * Find the nearest visible from {@code realLineIndex}.
     *
     * @param lines Info about lines.
     * @param realLineIndex Real index of line above/below which the first
     * visible line should be found.
     * @param direction SwingConstants.SOUTH or SwingConstants.NORTH.
     * @return Real line index of the nearest visible line, or -1 if no such
     * line exists.
     */
    private int findNearestVisibleLine(Lines lines, int realLineIndex,
            int direction) {
        assert direction == SOUTH || direction == NORTH;
        int inc = direction == SOUTH ? 1 : -1;
        int visibleLine = lines.realToVisibleLine(realLineIndex);
        if (visibleLine < 0) {
            // the source line is not visible, let's search
            for (int i = realLineIndex + inc; i >= 0
                    && i < lines.getLineCount(); i += inc) {
                if (lines.realToVisibleLine(i) >= 0) {
                    return i;
                }
            }
            return -1;
        } else {
            return lines.visibleToRealLine(visibleLine + inc);
        }
    }

    /**
     * Jump to a logical line in a physical line.
     *
     * @param pi Source position.
     * @param lines Lines object.
     * @param direction Direction, SwingConstants.NORTH or SwingConstants.SOUTH.
     */
    private int jumpInLine(Lines lines, PositionInfo pi, int direction) {
        assert direction == NORTH || direction == SOUTH;
        assert pi.innerRowIndex > 0 || direction == SOUTH;
        assert pi.innerRowIndex + 1 < pi.innerRowsCount || direction == NORTH;

        int newRow = pi.innerRowIndex + ((direction == SOUTH) ? 1 : -1);
        int newLogicalColumn = newRow * charsPerLine + pi.innerColumn;
        int newPos = pi.lineStart + lines.getNumPhysicalChars(pi.lineStart,
                newLogicalColumn, null);

        newPos = fixPhysicalPosition(lines, newPos, newRow, pi.lineStart);
        return Math.min(pi.lineEnd, Math.max(pi.lineStart, newPos));
    }

    /**
     * When computing physical position from logical position (including tabs),
     * and the position is inside a tab, the tab offset is returned. It is a
     * problem if start of the tab is in another line than the position inside
     * the tab. This method checks it and corrects the physical position.
     *
     * @param lines Lines object.
     * @param pos Physical position.
     * @param dir Direction, SOUTH or NORTH.
     * @param newRow Index or inner row in which the position should be placed.
     * @param lineStart Start offset of the physical line.
     *
     * @return Position that is sure to be in the correct inner line.
     */
    private int fixPhysicalPosition(Lines lines, int pos, int newRow,
            int lineStart) {
        //check that new pos is really in the next logical inner line
        int newLogicalLineStart = newRow * charsPerLine;
        int computedLogicalColumn = lines.getNumLogicalChars(lineStart,
                pos - lineStart);
        if (computedLogicalColumn < newLogicalLineStart) {
            return pos + 1;
        }
        return pos;
    }

    /**
     * Find position of the last char in line.
     */
    private int getLineEnd(int realLineIndex, Lines lines) {
        synchronized (lines.readLock()) {
            return realLineIndex + 1 < lines.getLineCount()
                    ? lines.getLineStart(realLineIndex + 1) - 1
                    : lines.getCharCount();
        }
    }

    /**
     * Simple data structure for short-term storing of information about a
     * position in a wrapped line.
     */
    private static final class PositionInfo {

        /**
         * Physical character offset. Not counting tabs.
         */
        public int offset;
        /**
         * Real line index. Counting invisible lines.
         */
        public int lineIndex;
        /**
         * Start offset of the line.
         */
        public int lineStart;
        /**
         * End offset of the line.
         */
        public int lineEnd;
        /**
         * Logical length (expanded tabs) of the line.
         */
        public int logicalLineLength;
        /**
         * Number of inner wrapped rows for the real row.
         */
        public int innerRowsCount;
        /**
         * Index of inner row that contains the position.
         */
        public int innerRowIndex;
        /**
         * Column in the inner row at which the position is displayed.
         */
        public int innerColumn;
    }
}
