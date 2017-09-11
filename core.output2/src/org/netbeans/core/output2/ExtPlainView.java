/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.output2;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.text.Position.Bias;
import org.netbeans.core.output2.options.OutputOptions;
import org.openide.windows.IOColors;

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

    /** set antialiasing hints when it's requested. */
    private static final boolean antialias = Boolean.getBoolean ("swing.aatext") || //NOI18N
                                             "Aqua".equals (UIManager.getLookAndFeel().getID()); // NOI18N

    private static Map<RenderingHints.Key, Object> hintsMap = null;
    
    @SuppressWarnings("unchecked")
    static Map<RenderingHints.Key, Object> getHints() {
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<RenderingHints.Key, Object>();
                if (antialias) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
        }
        return hintsMap;
    }
    
    /** Creates a new instance of ExtPlainView */
    ExtPlainView(Element elem) {
        super (elem);
    }

    @Override
    public void paint(Graphics g, Shape allocation) {
        ((Graphics2D)g).addRenderingHints(getHints());
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
    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
        // line coordinates
        Element map = getElement();
        int lineIndex = map.getElementIndex(pos);
        Rectangle lineArea = lineToRect(a, lineIndex);
        
        // determine span from the start of the line
        tabBase = lineArea.x;
        Element line = map.getElement(lineIndex);
        int lineStart = line.getStartOffset();
        Segment s = getSegment();
        getText(lineStart, pos - lineStart, s);
        int xOffs = Utilities.getTabbedTextWidth(s, metrics, tabBase, this, lineStart);

        // fill in the results and return
        lineArea.x += xOffs;
        lineArea.width = 1;
        lineArea.height = metrics.getHeight();
        return lineArea;        
    }

    @Override
    public int viewToModel(float fx, float fy, Shape a, Bias[] bias) {
        Document doc = getDocument();
        if (!(doc instanceof Document)) {
            return super.viewToModel(fx, fy, a, bias);
        }
        Rectangle alloc = a.getBounds();
        int x = (int) fx;
        int y = (int) fy;
        if (y < alloc.y) {
            // above the area covered by this icon, so the the position
            // is assumed to be the start of the coverage for this view.
            return getStartOffset();
        } else if (y > alloc.y + alloc.height) {
            // below the area covered by this icon, so the the position
            // is assumed to be the end of the coverage for this view.
            return getEndOffset() - 1;
        } else {
            // positioned within the coverage of this view vertically,
            // so we figure out which line the point corresponds to.
            // if the line is greater than the number of lines contained, then
            // simply use the last line as it represents the last possible place
            // we can position to.
            Element map = doc.getDefaultRootElement();
            int lineIndex = Math.abs((y - alloc.y) / metrics.getHeight() );
            if (lineIndex >= map.getElementCount()) {
                return getEndOffset() - 1;
            }
            Element line = map.getElement(lineIndex);
            if (x < alloc.x) {
                // point is to the left of the line
                return line.getStartOffset();
            } else if (x > alloc.x + alloc.width) {
                // point is to the right of the line
                return line.getEndOffset() - 1;
            } else {
                // Determine the offset into the text
                try {
                    int p0 = line.getStartOffset();
                    int p1 = line.getEndOffset() - 1;
                    Segment s = getSegment();
                    getText(p0, p1 - p0, s);
                    tabBase = alloc.x;
                    int offs = p0 + Utilities.getTabbedTextOffset(s, metrics, tabBase, x, this, p0);
                    return offs;
                } catch (BadLocationException e) {
                    // should not happen
                    return -1;
                }
            }
        }
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
