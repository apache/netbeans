/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;

import java.beans.PropertyChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.util.NbBundle;

import org.netbeans.editor.Coloring;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.FontMetricsCache;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.awt.GraphicsUtils;
import org.openide.util.WeakListeners;

/** GlyphGutter is component for displaying line numbers and annotation
 * glyph icons. Component also allow to "cycle" through the annotations. It
 * means that if there is more than one annotation on the line, only one of them
 * might be visible. And clicking the special cycling button in the gutter the user
 * can cycle through the annotations.
 *
 * @author  David Konecny
 * @since 07/2001
 */

public class LinesComponent extends JComponent implements javax.accessibility.Accessible, PropertyChangeListener {


    /** Document to which this gutter is attached*/
    private final JEditorPane editorPane;

    private final EditorUI editorUI;

    /** Backroung color of the gutter */
    private Color backgroundColor;

    /** Foreground color of the gutter. Used for drawing line numbers. */
    private Color foreColor;

    /** Font used for drawing line numbers */
    private Font font;

    /** Flag whther the gutter was initialized or not. The painting is disabled till the
     * gutter is not initialized */
    private boolean init;

    /** Width of the column used for drawing line numbers. The value contains
     * also line number margins. */
    private int numberWidth;

    /** Whether the line numbers are shown or not */
    private boolean showLineNumbers = true;

    /** The gutter height is enlarged by number of lines which specifies this constant */
    private static final int ENLARGE_GUTTER_HEIGHT = 300;

    /** The hightest line number. This value is used for calculating width of the gutter */
    private int highestLineNumber = 0;

    /** Holds value of property lineNumberMargin. */
    private Insets lineNumberMargin;

    /** Holds value of property lineNumberDigitWidth. */
    private int lineNumberDigitWidth;

    private LinkedList<String> linesList;

    /** Holds value of property activeLine. */
    private int activeLine = -1;

    private static final long serialVersionUID = -4861542695772182147L;

    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    public LinesComponent(JEditorPane pane) {
        super();
        init = false;
        editorPane = pane;
        font = editorPane.getFont();
        foreColor = editorPane.getForeground();
        backgroundColor = editorPane.getBackground();
        setLineNumberDigitWidth(10);
        setLineNumberMargin(new Insets(2, 2, 2, 4));
        editorUI = org.netbeans.editor.Utilities.getEditorUI(editorPane);
        editorUI.addPropertyChangeListener(WeakListeners.propertyChange(this, editorUI));
        init();
    }

    /* Read accessible context
     * @return - accessible context
     */
    public @Override AccessibleContext getAccessibleContext () {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                public @Override AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }

    /** Do initialization of the glyph gutter*/
    protected void init() {
        createLines();
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(LinesComponent.class, "ACSN_Lines_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(LinesComponent.class, "ACSD_Lines_Component")); // NOI18N
    }

    private void createLines() {
        linesList = new LinkedList<>();
        int lineCnt;
        StyledDocument doc = (StyledDocument)editorPane.getDocument();
        int lastOffset = doc.getEndPosition().getOffset();
        lineCnt = org.openide.text.NbDocument.findLineNumber(doc, lastOffset);
        for (int i = 0; i < lineCnt; i++) {
            linesList.add(Integer.toString(i + 1));
        }
    }

    public void addEmptyLines(int line, int count) {
        boolean appending = line > linesList.size();
        for (int i = 0; i < count; i++) {
            if (appending) {
                linesList.add("");
            } else {
                linesList.add(line, "");
            }
        }
    }

    /**
     * Insert line numbers. If at the end, then line numbers are added to the end of the component.
     * If in the middle, subsequent lines are overwritten.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public void insertNumbers(int line, int startNum, int count) {
        boolean appending = line >= linesList.size();
        if (appending) {
            for (int i = 0; i < count; i++, startNum++) {
                linesList.add(Integer.toString(startNum));
            }
        } else {
            int toAdd = Math.max(line + count - linesList.size(), 0);
            count -= toAdd;
            for (int i = 0; i < count; i++, startNum++, line++) {
                linesList.set(line, Integer.toString(startNum));
            }
            for (int i = 0; i < toAdd; i++, startNum++) {
                linesList.add(Integer.toString(startNum));
            }
        }
    }

    /*
     * Test method.
     *
    private void dumpResultLineNumbers() {
        System.out.print("LinesComponent: linesList = ");
        boolean was = false;
        for (int i = 0; i < linesList.size(); i++) {
            System.out.print(linesList.get(i)+", ");
        }
        System.out.println("");
    }
     */

    /**
     * Remove line numbers and leave the corresponding part of the lines component empty.
     * If at the end, then an empty space is added to the end of the component.
     * If in the middle, subsequent lines are overwritten by an empty space.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public void removeNumbers(int line, int count) {
        boolean appending = line >= linesList.size();
        if (appending) {
            for (int i = 0; i < count; i++) {
                linesList.add("");
            }
        } else {
            int toAdd = Math.max(line + count - linesList.size(), 0);
            count -= toAdd;
            for (int i = 0; i < count; i++, line++) {
                linesList.set(line, "");
            }
            for (int i = 0; i < toAdd; i++) {
                linesList.add("");
            }
        }
    }

    /**
     * Shrink the component, so that it will have <code>numLines</code> number of lines.
     * @param numLines The new number of lines
     */
    public void shrink(int numLines) {
        while (linesList.size() > numLines) {
            linesList.remove(numLines);
        }
    }

    /** Update colors, fonts, sizes and invalidate itself. This method is
     * called from EditorUI.update() */
    private void updateState(Graphics g) {
        //System.out.println("  => correction = "+lineHeightCorrection);
        String mimeType = DocumentUtilities.getMimeType(editorPane);
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        Coloring col = Coloring.fromAttributeSet(AttributesUtilities.createComposite(
                fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING),
                fcs.getFontColors(FontColorNames.DEFAULT_COLORING)));

        foreColor = col.getForeColor();
        backgroundColor = col.getBackColor();
        //System.out.println("  => foreground = "+foreColor+", background = "+backgroundColor);

        font = col.getFont();
        FontMetrics fm = g.getFontMetrics(font);
        /*
        int maxHeight = 1;
        int maxAscent = 0;
        if (fm != null) {
            maxHeight = Math.max(maxHeight, fm.getHeight());
            maxAscent = Math.max(maxAscent, fm.getAscent());
        }

        // Apply lineHeightCorrection
        lineHeight = (int)(maxHeight * lineHeightCorrection);
        lineAscent = (int)(maxAscent * lineHeightCorrection);
         */
        //System.out.println("lineheight=" + lineHeight);//+", fm height = "+fm.getHeight());
        //System.out.println("lineascent=" + lineAscent);//+", fm ascent = "+fm.getAscent());
        showLineNumbers = true;

        /*
        lineHeight = editorUI.getLineHeight();
        lineAscent = editorUI.getLineAscent();
        System.out.println("lineHeight = "+lineHeight);
        System.out.println("lineascent=" + lineAscent);

        showLineNumbers = editorUI.isLineNumberEnabled();
         */

        init = true;

        // initialize the value with current number of lines
        if (highestLineNumber <= getLineCount()) {
            highestLineNumber = getLineCount();
        }
//        System.out.println("highestLineNumber=" + highestLineNumber);
        // width of a digit..
        int maxWidth = 1;
        char[] digit = new char[1]; // will be used for '0' - '9'
        for (int i = 0; i <= 9; i++) {
             digit[0] = (char)('0' + i);
             maxWidth = Math.max(maxWidth, fm.charsWidth(digit, 0, 1));
        }
        setLineNumberDigitWidth(maxWidth);
//        System.out.println("maxwidth=" + maxWidth);
//        System.out.println("numner of lines=" + highestLineNumber);

        resize();
    }

    protected void resize() {
        Dimension dim = new Dimension();
//        System.out.println("resizing...................");
        dim.width = getWidthDimension();
        dim.height = getHeightDimension();
        // enlarge the gutter so that inserting new lines into
        // document does not cause resizing too often
        dim.height += ENLARGE_GUTTER_HEIGHT * editorUI.getLineHeight();

        numberWidth = getLineNumberWidth();
        setPreferredSize(dim);

        revalidate();
    }

    /** Return number of lines in the document */
    protected int getLineCount() {
        return linesList.size();
    }

    /** Gets number of digits in the number */
    protected int getDigitCount(int number) {
        return Integer.toString(number).length();
    }

    protected int getLineNumberWidth() {
        int newWidth = 0;
        Insets insets = getLineNumberMargin();
        if (insets != null) {
            newWidth += insets.left + insets.right;
        }
        newWidth += (getDigitCount(highestLineNumber) + 1) * getLineNumberDigitWidth();
//        System.out.println("new width=" + newWidth);
        return newWidth;
    }

    protected int getWidthDimension() {
        int newWidth = 0;

        if (showLineNumbers) {
            newWidth += getLineNumberWidth();
        }

        return newWidth;
    }

    protected int getHeightDimension() {
        /*TEMP+ (int)editorPane.getSize().getHeight() */
        View rootView = org.netbeans.editor.Utilities.getDocumentView(editorPane);
        int height = highestLineNumber * editorUI.getLineHeight();
        if (rootView != null) {
            try {
                int lineCount = rootView.getViewCount();
                if (lineCount > 0) {
                    Rectangle rec = editorPane.modelToView(rootView.getView(lineCount - 1).getEndOffset() - 1);
                    height = rec.y + rec.height;
                }
            } catch (BadLocationException ex) {
                //
            }
        }
        return height;
    }

    /** Paint the gutter itself */
    public @Override void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (!init) {
            updateState(g);
        }

        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(backgroundColor);
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        g.setFont(font);
        g.setColor(foreColor);

        FontMetrics fm = FontMetricsCache.getFontMetrics(font, this);
        int rightMargin = 0;
        Insets margin = getLineNumberMargin();
        if (margin != null)
            rightMargin = margin.right;
        // calculate the first line which must be drawn
        View rootView = org.netbeans.editor.Utilities.getDocumentView(editorPane);
        int pos = editorPane.viewToModel(new Point(0, drawHere.y));
        int line = rootView.getViewIndex(pos, Position.Bias.Forward);
        if (line > 0) {
            --line;
        }
        try {
            // calculate the Y of the first line
            Rectangle rec = editorPane.modelToView(rootView.getView(line).getStartOffset());
            if (rec == null) {
                return;
            }
            int y = rec.y;

            // draw liune numbers and annotations while we are in visible area
            int lineHeight = editorUI.getLineHeight();
            int lineAscent = editorUI.getLineAscent();
            int lineCount = rootView.getViewCount();
            while (line < lineCount && (y + (lineHeight / 2)) <= (drawHere.y + drawHere.height)) {
                View view = rootView.getView(line);
                Rectangle rec1 = editorPane.modelToView(view.getStartOffset());
                Rectangle rec2 = editorPane.modelToView(view.getEndOffset() - 1);
                if (rec1 == null || rec2 == null) {
                    break;
                }
                y = (int)rec1.getY();
                // draw line numbers if they are turned on
                if (showLineNumbers) {
                    String lineStr = null;
                    if (line < linesList.size()) {
                        lineStr = linesList.get(line);
                    }
                    if (lineStr == null) {
                        lineStr = ""; //NOI18N
                    }
                    String activeSymbol = "*"; //NOI18N
                    int lineNumberWidth = fm.stringWidth(lineStr);
                    if (line == activeLine - 1) {
                        lineStr += activeSymbol;
                    }
                    int activeSymbolWidth = fm.stringWidth(activeSymbol);
                    lineNumberWidth += activeSymbolWidth;
                    g.drawString(lineStr, numberWidth - lineNumberWidth - rightMargin, y + lineAscent);
                }

                y += (int) (rec2.getY() + rec2.getHeight() - rec1.getY());
                line++;
            }
        } catch (BadLocationException ex) {
        }
    }

    /** Repaint whole gutter.*/
    public void changedAll() {

        if (!init)
            return;

/*        int lineCnt;
        try {
            lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
        } catch (BadLocationException e) {
            lineCnt = 1;
        }
 */

        repaint();
        checkSize();
    }

    protected void checkSize() {
        int count = getLineCount();
        if (count > highestLineNumber) {
            highestLineNumber = count;
        }
        Dimension dim = getPreferredSize();
        if (getWidthDimension() > dim.width ||
            getHeightDimension() > dim.height) {
                resize();
        }
    }

    /** Getter for property lineNumberMargin.
     * @return Value of property lineNumberMargin.
     */
    public Insets getLineNumberMargin() {
        return this.lineNumberMargin;
    }

    /** Setter for property lineNumberMargin.
     * @param lineNumberMargin New value of property lineNumberMargin.
     */
    public void setLineNumberMargin(Insets lineNumberMargin) {
        this.lineNumberMargin = lineNumberMargin;
    }

    /** Getter for property lineNumberDigitWidth.
     * @return Value of property lineNumberDigitWidth.
     */
    public int getLineNumberDigitWidth() {
        return this.lineNumberDigitWidth;
    }

    /** Setter for property lineNumberDigitWidth.
     * @param lineNumberDigitWidth New value of property lineNumberDigitWidth.
     */
    public void setLineNumberDigitWidth(int lineNumberDigitWidth) {
        this.lineNumberDigitWidth = lineNumberDigitWidth;
    }

    /** Getter for property activeLine.
     * @return Value of property activeLine.
     */
    public int getActiveLine() {
        return this.activeLine;
    }

    /** Setter for property activeLine.
     * @param activeLine New value of property activeLine.
     */
    public void setActiveLine(int activeLine) {
        this.activeLine = activeLine;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        init = false;
        repaint();
    }

}
