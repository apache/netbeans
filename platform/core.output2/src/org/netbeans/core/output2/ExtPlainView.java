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

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.text.Position.Bias;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.awt.GraphicsUtils;

/**
 * Extension to PlainView which can paint hyperlinked lines in different
 * colors.  For the limited styles that the output window supports, this
 * is considerably simpler and has less overhead than the default handling
 * of StyledDocument.
 *
 * @author  Tim Boudreau
 */
class ExtPlainView extends PlainView {
    private final Segment SEGMENT = new Segment();
    private static final int MAX_LINE_LENGTH = 4096;
    private static final String LINE_TOO_LONG_MSG = org.openide.util.NbBundle.getMessage(ExtPlainView.class, "MSG_LINE_TOO_LONG");

    /** Creates a new instance of ExtPlainView */
    ExtPlainView(Element elem) {
        super (elem);
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        super.paint(g, allocation);
    }

    Segment getSegment() {
        return SwingUtilities.isEventDispatchThread() ? SEGMENT : new Segment();
    }

    private int drawText(Graphics g, int x, int y, int p0, int p1,
            boolean selected) throws BadLocationException {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            Segment s = getSegment();
            if (!getText(p0, p1 - p0, s)) {
                return x;
            }
            int end = p0 + s.count;
            Lines lines = ((OutputDocument) doc).getLines();
            int lineOffset;
            LineInfo info;
            synchronized (lines.readLock()) {
                int line = lines.getLineAt(p0);
                lineOffset = lines.getLineStart(line);
                info = lines.getLineInfo(line);
            }

            for (LineInfo.Segment ls : info.getLineSegments()) {
                if (lineOffset + ls.getEnd() <= p0) {
                    continue;
                }
                s.count = Math.min(lineOffset + ls.getEnd() - p0, end - p0);
                if (s.count == 0) {
                    return x;
                }
//                if (!getText(p0, Math.min(end, p1) - p0, s)) {
//                    return x;
//                }
                Color bg = ls.getCustomBackground();
                if (bg != null && !selected) {
                    int w = Utilities.getTabbedTextWidth(
                            s, metrics, x, this, p0);
                    int h = metrics.getHeight();
                    g.setColor(bg);
                    g.fillRect(x, y - h + metrics.getDescent(), w, h);
                }
                g.setColor(ls.getColor());
                int nx = Utilities.drawTabbedText(s, x, y, g, this, p0);
                if (ls.getListener() != null) {
                    underline(g, s, x, p0, y);
                }
                x = nx;
                p0 += s.count;
                s.offset += s.count;
            }
            return x;
        } else {
            return super.drawUnselectedText(g, x, y, p0, p1);
        }
    }

    @Override
    protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return drawText(g, x, y, p0, p1, true);
    }

    @Override
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
        return drawText(g, x, y, p0, p1, false);
    }

    /*
     * gets text from document with respect with MAXLINELEN
     * if line is too long the end of line is replaced by lineTooLong
     */
    boolean getText(int offset, int length, Segment txt) throws BadLocationException {
        Document doc = getDocument();
        Element elem = getElement();
        int lineIndex = elem.getElementIndex(offset);
        Element lineElem = elem.getElement(lineIndex);
        int lineStart = lineElem.getStartOffset();
        int lineLength = lineElem.getEndOffset() - 1 - lineStart;
        int newLen = Math.min(length, lineStart + MAX_LINE_LENGTH - offset);
        if (newLen <= 0) {
            txt.count = 0;
            return false;
        }
        doc.getText(offset, newLen, txt);
        if (lineLength > MAX_LINE_LENGTH && offset + length > lineStart + MAX_LINE_LENGTH - LINE_TOO_LONG_MSG.length()) {
            int diff = offset - (lineStart + MAX_LINE_LENGTH - LINE_TOO_LONG_MSG.length());
            int strPos = diff < 0 ? 0 : diff;
            int txtPos = diff < 0 ? -diff : 0;
            for (int i = strPos; i < LINE_TOO_LONG_MSG.length(); i++) {
                if (txtPos + i - strPos >= txt.array.length) {
                    break;
                }
                txt.array[txtPos + i - strPos] = LINE_TOO_LONG_MSG.charAt(i);
            }
        }
        return true;
    }

    private void underline(Graphics g, Segment s, int x, int p0, int y) {
        if (!isLinkUndeliningEnabled(this)) {
            return;
        }
        int textLen = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, p0);
        int underlineShift = g.getFontMetrics().getDescent() - 1;
        g.drawLine(x, y + underlineShift, x + textLen, y + underlineShift);
    }

    @Override
    public float getPreferredSpan(int axis) {
        if (axis == Y_AXIS) {
            return super.getPreferredSpan(axis);
        } else {
            if (longestLineLength == -1) {
                calcLongestLineLength();
            }
            return longestLineLength + 1;
        }
    }

    Font font;
    int tabBase;
    int longestLineLength = -1;
    Element longestLine;

    private void calcLongestLineLength() {
	Component c = getContainer();
	font = c.getFont();
	metrics = c.getFontMetrics(font);
	Element lines = getElement();
	int n = lines.getElementCount();
	longestLineLength = 0;
	for (int i = 0; i < n; i++) {
	    Element line = lines.getElement(i);
	    int w = getLineWidth(line);
	    if (w > longestLineLength) {
		longestLineLength = w;
		longestLine = line;
	    }
	}
    }

    /**
     * Calculate the width of the line represented by the given element.
     * It is assumed that the font and font metrics are up-to-date.
     */
    protected int getLineWidth(Element line) {
	int p0 = line.getStartOffset();
	int p1 = line.getEndOffset() - 1;
	int w;
        Segment s = getSegment();
	try {
            getText(p0, p1 - p0, s);
	    w = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, p0);
	} catch (BadLocationException e) {
	    w = 0;
	}
	return w;
    }

    @Override
    protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f) {
        Document doc = getDocument();
        if (!(doc instanceof Document)) {
            super.updateDamage(changes, a, f);
            return;
        }
        if (longestLineLength == -1) {
            calcLongestLineLength();
        }
	Component host = getContainer();
	updateMetrics();
	Element elem = getElement();
	DocumentEvent.ElementChange ec = changes.getChange(elem);
	Element[] added = (ec != null) ? ec.getChildrenAdded() : null;
	Element[] removed = (ec != null) ? ec.getChildrenRemoved() : null;
	if (((added != null) && (added.length > 0)) ||
	    ((removed != null) && (removed.length > 0))) {
	    // lines were added or removed...
	    if (added != null) {
		for (int i = 0; i < added.length; i++) {
		    int w = getLineWidth(added[i]);
		    if (w > longestLineLength) {
			longestLineLength = w;
			longestLine = added[i];
		    }
		}
	    }
	    if (removed != null) {
		for (int i = 0; i < removed.length; i++) {
		    if (removed[i] == longestLine) {
			calcLongestLineLength();
			break;
		    }
		}
	    }
	    preferenceChanged(null, true, true);
	    host.repaint();
	} else {
	    Element map = getElement();
	    int line = map.getElementIndex(changes.getOffset());
	    damageLineRange(line, line, a, host);
	    if (changes.getType() == DocumentEvent.EventType.INSERT) {
		// check to see if the line is longer than current longest line.
		Element e = map.getElement(line);
                int lineLen = getLineWidth(e);
		if (e == longestLine) {
		    preferenceChanged(null, true, false);
		} else if (lineLen > longestLineLength) {
                    longestLineLength = lineLen;
		    longestLine = e;
		    preferenceChanged(null, true, false);
		}
	    } else if (changes.getType() == DocumentEvent.EventType.REMOVE) {
		if (map.getElement(line) == longestLine) {
		    // removed from longest line... recalc
		    calcLongestLineLength();
		    preferenceChanged(null, true, false);
		}
	    }
	}
    }

    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * Replaces usage of slow Utilities.getPositionAbove()/Utilities.getPositionBelow()
     * skips according to MAXLINELEN
     */
    @Override
    public int getNextVisualPositionFrom(int pos, Bias b, Shape a, int direction, Bias[] biasRet) throws BadLocationException {
        Element elem = getElement();
        if (pos == -1) {
            pos = (direction == SOUTH || direction == EAST) ? getStartOffset() : (getEndOffset() - 1);
        }

        int lineIndex;
        int lineStart;
        switch (direction) {
            case NORTH:
                lineIndex = elem.getElementIndex(pos);
                lineStart = elem.getElement(lineIndex).getStartOffset();
                if (lineIndex > 0) {
                    int linePos = pos - lineStart;
                    Element el = elem.getElement(lineIndex - 1);
                    pos = el.getStartOffset();
                    pos += Math.min(Math.min(MAX_LINE_LENGTH, el.getEndOffset() - pos - 1), linePos);
                }
                break;
            case SOUTH:
                lineIndex = elem.getElementIndex(pos);
                lineStart = elem.getElement(lineIndex).getStartOffset();
                if (lineIndex < elem.getElementCount() - 1) {
                    int linePos = pos - lineStart;
                    Element el = elem.getElement(lineIndex + 1);
                    pos = el.getStartOffset();
                    pos += Math.min(Math.min(MAX_LINE_LENGTH, el.getEndOffset() - pos - 1), linePos);
                }
                break;
            case WEST:
                pos = Math.max(0, pos - 1);
                lineIndex = elem.getElementIndex(pos);
                lineStart = elem.getElement(lineIndex).getStartOffset();
                if (pos - lineStart > MAX_LINE_LENGTH) {
                    pos = lineStart + MAX_LINE_LENGTH;
                }
                break;
            case EAST:
                pos = Math.min(pos + 1, elem.getEndOffset() - 1);
                lineIndex = elem.getElementIndex(pos);
                lineStart = elem.getElement(lineIndex).getStartOffset();
                if (pos - lineStart > MAX_LINE_LENGTH) {
                    pos = (elem.getElementCount() > lineIndex + 1) ? elem.getElement(lineIndex + 1).getStartOffset() : lineStart + MAX_LINE_LENGTH;
                }
                break;
            default:
                throw new IllegalArgumentException("Bad direction: " + direction);
        }
        return pos;
    }

    static boolean isLinkUndeliningEnabled(View v) {
        Container pane = v.getContainer();
        if (pane != null) {
            OutputTab tab = (OutputTab) SwingUtilities.getAncestorOfClass(
                    OutputTab.class, pane);
            if (tab != null) {
                OutputTab outputTab = tab;
                OutputOptions.LinkStyle linkStyle;
                linkStyle = outputTab.getIO().getOptions().getLinkStyle();
                if (linkStyle == OutputOptions.LinkStyle.NONE) {
                    return false;
                }
            }
        }
        return true;
    }
}
