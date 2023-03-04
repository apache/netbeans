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

package org.netbeans.modules.gsf.testrunner.ui.output;

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Shape;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import org.netbeans.modules.gsf.testrunner.ui.output.OutputDocument.DocElement;
import org.openide.awt.GraphicsUtils;

/**
 * 
 * @author  Marian Petras
 * @author  Tim Boudreau
 */
final class OutputView extends PlainView {

    private final Segment SEGMENT = new Segment(); 
    private final OutputDocument.RootElement rootElement;

    private int selStart, selEnd;
    private static Color selectedErr;
    private static Color unselectedErr;

    private Color selectedFg, unselectedFg;

    static {
        selectedErr = UIManager.getColor("nb.output.err.foreground.selected");  //NOI18N
        if (selectedErr == null) {
            selectedErr = new Color(164, 0, 0);
        }
        unselectedErr = UIManager.getColor("nb.output.err.foreground"); //NOI18N
        if (unselectedErr == null) {
            unselectedErr = selectedErr;
        }
    }

    OutputView(Element element) {
        super(element);
        rootElement = (OutputDocument.RootElement) element;
    }

    @Override
    public void paint(Graphics g, Shape a) {
        GraphicsUtils.configureDefaultRenderingHints(g);

        Container container = getContainer();
        if (container instanceof JTextComponent) {
            final JTextComponent textComp = (JTextComponent) container;
            selStart = textComp.getSelectionStart();
            selEnd = textComp.getSelectionEnd();
            unselectedFg = textComp.isEnabled()
                    ? textComp.getForeground()
                    : textComp.getDisabledTextColor();
            selectedFg = textComp.getCaret().isSelectionVisible()
                    ? textComp.getSelectedTextColor()
                    : unselectedFg;
        }
        super.paint(g, a);
    }

    @Override
    protected void drawLine(int lineIndex, Graphics g, int x, int y) {
        DocElement docElem = rootElement.getDocElement(lineIndex);
	try {
            drawLine(docElem, g, x, y);
        } catch (BadLocationException e) {
            throw new IllegalStateException("cannot draw line " + lineIndex);   //NOI18N
        }
    }
   
    private void drawLine(DocElement elem, Graphics g, int x, int y) throws BadLocationException {
	final int p0 = elem.getStartOffset();
        final int p1 = elem.getEndOffset();
        final boolean isError = elem.isError;

        if ((selStart == selEnd) || (selectedFg == unselectedFg)) {
            /* no selection or invisible selection */
            x = drawText(g, x, y, p0, p1, isError, false, elem);
        } else if ((p0 >= selStart && p0 <= selEnd)
                && (p1 >= selStart && p1 <= selEnd)) {
            /* whole line selected */
            x = drawText(g, x, y, p0, p1, isError, true,  elem);
        } else if (selStart >= p0 && selStart <= p1) {
            if (selEnd >= p0 && selEnd <= p1) {
                x = drawText(g, x, y, p0,       selStart, isError, false, elem);
                x = drawText(g, x, y, selStart, selEnd,   isError, true,  elem);
                x = drawText(g, x, y, selEnd,   p1,       isError, false, elem);
            } else {
                x = drawText(g, x, y, p0,       selStart, isError, false, elem);
                x = drawText(g, x, y, selStart, p1,       isError, true,  elem);
            }
        } else if (selEnd >= p0 && selEnd <= p1) {
            x = drawText(g, x, y, p0,     selEnd, isError, true,  elem);
            x = drawText(g, x, y, selEnd, p1,     isError, false, elem);
        } else {
            x = drawText(g, x, y, p0,     p1,     isError, false, elem);
        }
    }

    private int drawText(Graphics g,
                         int x, int y,
                         int startOffset, int endOffset,
                         boolean error,
                         boolean selected,
                         DocElement docElem) throws BadLocationException {
        Segment s = EventQueue.isDispatchThread() ? SEGMENT : new Segment(); 
        s.array = docElem.getChars();
        s.offset = startOffset - docElem.offset;
        s.count = endOffset - startOffset;

        g.setColor(getColor(error, selected));

        return Utilities.drawTabbedText(s, x, y, g, this, startOffset);
    }
    
    private Color getColor(boolean error, boolean selected) {
        return error ? (selected ? selectedErr
                                 : unselectedErr)
                     : (selected ? selectedFg
                                 : unselectedFg);
    }

}
