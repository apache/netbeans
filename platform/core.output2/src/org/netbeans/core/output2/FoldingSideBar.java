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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.core.output2.ui.AbstractOutputPane;

/**
 * Component that draws controls for expanding and collapsing of folds.
 *
 * @author jhavlin
 */
public class FoldingSideBar extends JComponent {

    private static final Logger LOG =
            Logger.getLogger(FoldingSideBar.class.getName());
    private final int BAR_WIDTH = 15;

    private final JEditorPane textView;
    private AbstractLines lines;
    private int charsPerLine = 80;
    private boolean wrapped;
    private int activeFold = -1;

    public FoldingSideBar(JEditorPane textView, AbstractOutputPane outputPane) {
        this.textView = textView;
        this.lines = getLines();
        textView.addPropertyChangeListener("document", //NOI18N
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        FoldingSideBar.this.lines = getLines();
                    }
                });
        textView.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                setPreferredSize(new Dimension(BAR_WIDTH,
                        FoldingSideBar.this.textView.getHeight()));
                repaint();
            }
        });
        setMinimumSize(new Dimension(BAR_WIDTH, 0));
        setPreferredSize(new Dimension(BAR_WIDTH, textView.getHeight()));
        setMaximumSize(new Dimension(BAR_WIDTH, Integer.MAX_VALUE));
        wrapped = outputPane.isWrapped();
        addMouseListener(new FoldingMouseListener());
        addMouseMotionListener(new FoldingMouseListener()); //TODO one is enough
    }

    private AbstractLines getLines() {
        Document doc = textView.getDocument();
        if (doc instanceof OutputDocument) {
            Lines l = ((OutputDocument) doc).getLines();
            if (l instanceof AbstractLines) {
                return (AbstractLines) l;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Rectangle cp = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(cp.x, cp.y, cp.width, cp.height);
        if (lines == null) {
            return;
        }
        g.setColor(getForeground());
        FontMetrics fontMetrics = textView.getFontMetrics(textView.getFont());
        int lineHeight = fontMetrics.getHeight();
        int descent = fontMetrics.getDescent();
        int offset = 0;
        try {
            Rectangle modelToView = textView.modelToView(0);
            offset = modelToView == null ? 0 : modelToView.y;
        } catch (BadLocationException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        offset += lineHeight - fontMetrics.getAscent();

        int size = lines.getLineCount();
        int logLine = 0; // logical line (including wrapped lines)
        int nextLogLine;
        int firstVisibleLine = Math.max(0, getLineAtPosition(cp.y) - 1);
        int lastVisibleLine = getLastVisibleLine(cp, size);
        for (int i = firstVisibleLine; i < lastVisibleLine; i++) {
            if (!lines.isVisible(i)) {
                continue;
            }
            nextLogLine = findLogicalLineIndex(findNextVisibleLine(i), size);
            drawLineGraphics(g, i, logLine, nextLogLine, offset, lineHeight,
                    descent);
            logLine = nextLogLine;
        }
    }

    /**
     * Get real index of the last line visible in the current clip bounds.
     *
     * @param cp Clip bounds.
     * @param realLineSize Total count of real lines.
     */
    private int getLastVisibleLine(Rectangle cp, int realLineSize) {
        int lineAtClipBoundsEnd = getLineAtPosition(cp.y + cp.height);
        return lineAtClipBoundsEnd < 0
                ? realLineSize - 1
                : Math.min(realLineSize - 1, lineAtClipBoundsEnd + 1);
    }

    /**
     * Find absolute line number at a y coordinate.
     */
    private int getLineAtPosition(int y) {
        // TODO refactor, the same code as in paint()
        FontMetrics fontMetrics = textView.getFontMetrics(textView.getFont());
        int lineHeight = fontMetrics.getHeight();
        int offset = 0;
        try {
            Rectangle modelToView = textView.modelToView(0);
            offset = modelToView == null ? 0 : modelToView.y;
        } catch (BadLocationException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        offset += lineHeight - fontMetrics.getAscent();
        // end TODO
        int logicalLine = (y - offset) / lineHeight;
        final int physicalLine;
        if (wrapped) {
            int[] info = new int[]{logicalLine, 0, 0};
            lines.toPhysicalLineIndex(info, charsPerLine);
            physicalLine = info[0];
        } else {
            physicalLine = logicalLine < lines.getVisibleLineCount()
                    ? lines.visibleToRealLine(logicalLine)
                    : -1;
        }
        return physicalLine;
    }

    /**
     * @param g Graphics to draw into.
     * @param line Physical line index.
     * @param logLine Logical visible line index.
     * @param nextLogLine Logical index of the next visible line.
     * @param offset Y offset of the first line (pixels).
     * @param lineHeight Height of line (pixels).
     * @param descent Descent of font metrics (pixels).
     */
    private void drawLineGraphics(Graphics g, int line, int logLine,
            int nextLogLine, int offset, int lineHeight, int descent) {

        int currOffset;
        int nextOffset;
        try {
            currOffset = lines.getFoldOffsets().get(line);
            nextOffset = line + 1 < lines.getFoldOffsets().size()
                    ? lines.getFoldOffsets().get(line + 1) : 0;
        } catch (IndexOutOfBoundsException ioobe) { // Some lines were removed.
            LOG.log(Level.FINE, null, ioobe);
            return;
        }
        int startY = logLine * lineHeight + offset;
        int endY = nextLogLine * lineHeight + offset;
        if (nextOffset == 1) {
            drawButton(g, startY, endY, line);
        } else if (currOffset != 0 && currOffset + 1 == nextOffset) {
            if (isActive(line)) {
                g.drawLine(6, startY, 6, endY);
            }
            g.drawLine(7, startY, 7, endY);
        } else if (currOffset > 0 && nextOffset == 0) {
            drawFoldEnd(g, line, startY, lineMid(endY, lineHeight, descent));
        } else if (currOffset > 0 && nextOffset > 0) {
            drawNestedFoldEnd(g, line, startY, endY,
                    lineMid(endY, lineHeight, descent));
        }
    }

    private static int lineMid(int lineEndY, int lineHeight, int descent) {
        return lineEndY - (lineHeight / 2) - descent;
    }

    /**
     * Draw graphics for a line that is at the start of a fold, which includes
     * expand/collapse button.
     *
     * @param g
     * @param lineStartY Y coordinate of the start of the line.
     * @param lineEndY Y coordinate of the end of the line.
     */
    private void drawButton(Graphics g, int lineStartY, int lineEndY, int line) {
        boolean collapsed = !lines.isVisible(line + 1);
        g.drawRect(2, lineStartY, 10, 10);
        g.drawLine(5, lineStartY + 5, 9, lineStartY + 5);
        if (collapsed) {
            g.drawLine(7, lineStartY + 3, 7, lineStartY + 7);
        }
        if (lineEndY > lineStartY + 10
                && (!collapsed || isLastVisibleLineInFold(line))) {
            g.drawLine(7, lineStartY + 10, 7, lineEndY);
            if (isActive(line)) {
                g.drawLine(6, lineStartY + 10, 6, lineEndY);
            }
        }
    }

    private boolean isLastVisibleLineInFold(int line) {
        if (lines.getFoldOffsets().get(line) > 0) {
            int visibleLine = lines.realToVisibleLine(line);
            int nextVisibleRealIndex = lines.visibleToRealLine(visibleLine + 1);
            if (nextVisibleRealIndex >= lines.getFoldOffsets().size()) {
                return true;
            }
            return lines.getFoldOffsets().get(nextVisibleRealIndex) > 0;
        } else {
            return false;
        }
    }

    /**
     * Draw graphics for a line at the end of a nested fold.
     *
     * @param g
     * @param lineStartY Y coordinate of the start of the line.
     * @param lineMid Y coordinate of the middle of the last logical line.
     */
    private void drawNestedFoldEnd(Graphics g, int lineIndex,
            int lineStartY, int lineEndY, int lineMid) {
        g.drawLine(7, lineStartY, 7, lineEndY);
        g.drawLine(7, lineMid, 11, lineMid);
        if (isActive(lineIndex)) {
            g.drawLine(6, lineStartY, 6, lineMid);
            g.drawLine(7, lineMid - 1, 11, lineMid - 1);
            if (isActive(findNextVisibleLine(lineIndex))) {
                g.drawLine(6, lineMid, 6, lineEndY);
            }
        }
    }

    /**
     * Draw graphics for a line at the end of a fold.
     *
     * @param g
     * @param lineStartY Y coordinate of the start of the line.
     * @param lineMid Y coordinate of the middle of the last logical line.
     */
    private void drawFoldEnd(Graphics g, int lineIndex, int lineStartY,
            int lineMid) {
        g.drawLine(7, lineStartY, 7, lineMid);
        g.drawLine(7, lineMid, 11, lineMid);
        if (isActive(lineIndex)) {
            g.drawLine(6, lineStartY, 6, lineMid);
            g.drawLine(7, lineMid - 1, 11, lineMid - 1);
        }
    }

    /**
     * That logical line index for physical line {@code physicalLineIndex}. If
     * the physical line index is bigger or equal to count of physical lines,
     * return total count of logical lines.
     *
     * @param physicalLineIndex Index of physical (not wrapped) visible line.
     * @param size Total count of physical visible lines.
     */
    private int findLogicalLineIndex(int physicalLineIndex, int size) {
        if (wrapped) {
            if (physicalLineIndex < size) {
                return lines.getLogicalLineCountAbove(
                        physicalLineIndex, charsPerLine);
            } else {
                return lines.getLogicalLineCountIfWrappedAt(charsPerLine);
            }
        } else {
            return lines.realToVisibleLine(physicalLineIndex);
        }
    }

    /**
     * Find next visible line below a line.
     *
     * @param physicalLine Physical index of a visible line.
     * @return Physical index of the nearest visible line below
     * {@code physicalLine}.
     */
    private int findNextVisibleLine(int physicalLine) {
        int visibleLineIndex = lines.realToVisibleLine(physicalLine);
        if (visibleLineIndex < 0) {
            return lines.getVisibleLineCount() - 1;
        }
        if (visibleLineIndex + 1 < lines.getVisibleLineCount()) {
            return lines.visibleToRealLine(visibleLineIndex + 1);
        } else {
            return lines.getVisibleLineCount() - 1;
        }
    }

    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
        repaint();
    }

    public void setCharsPerLine(int charsPerLine) {
        this.charsPerLine = charsPerLine;
        repaint();
    }

    /**
     * Check whether the the line belong to the fold under cursor.
     *
     * @param line Physical line index.
     */
    private boolean isActive(int line) {
        int parent = line;
        while (parent != activeFold && parent >= 0) {
            int foldOffset = lines.getFoldOffsets().get(parent);
            if (foldOffset == 0) {
                break;
            } else {
                parent = parent - foldOffset;
            }
        }
        return parent == activeFold;
    }

    private class FoldingMouseListener extends MouseAdapter {

        @Override
        public void mouseExited(MouseEvent e) {
            activeFold = -1;
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (lines == null) {
                return;
            }
            int physicalRealLine = getLineForEvent(e);
            updateActiveFold(physicalRealLine);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (lines == null) {
                return;
            }
            int physicalRealLine = getLineForEvent(e);
            updateActiveFold(physicalRealLine);
            if (activeFold == physicalRealLine) {
                if (lines.isVisible(physicalRealLine + 1)) {
                    lines.hideFold(physicalRealLine);
                } else {
                    lines.showFold(physicalRealLine);
                }
            }
        }

        private void updateActiveFold(int physicalLine) {
            int origActiveFold = activeFold;
            if (physicalLine < 0) {
                activeFold = -1;
            } else if (physicalLine + 1 < lines.getFoldOffsets().size()
                    && lines.getFoldOffsets().get(physicalLine + 1) == 1) {
                activeFold = physicalLine;
            } else if (physicalLine < lines.getFoldOffsets().size()) {
                activeFold = physicalLine
                        - lines.getFoldOffsets().get(physicalLine);
            } else {
                activeFold = -1;
            }
            if (activeFold != origActiveFold) {
                repaint();
            }
        }

        private int getLineForEvent(MouseEvent e) {
           return getLineAtPosition(e.getY());
        }
    }
}
