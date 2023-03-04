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
package org.netbeans.modules.print.provider;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.27
 */
final class ComponentDocument extends JComponent {

    ComponentDocument(String text) {
//out();
//out();
//out("TEXT PROVIDER");
//out(text);
        init();
        prepare(text);
        perform();
    }

    ComponentDocument(AttributedCharacterIterator[] iterators) {
        init();
        prepare(iterators);
        perform();
    }

    private void init() {
//out();
//out("INIT");
        myWrapLines = Config.getDefault().isWrapLines();
        myLineNumbers = Config.getDefault().isLineNumbers();
        myTextColor = Config.getDefault().getTextColor();
        myTextFont = Config.getDefault().getTextFont();
        myBackgroundColor = Config.getDefault().getBackgroundColor();
        myLineSpacing = Config.getDefault().getLineSpacing();
        myLines = new ArrayList<ComponentLine>();
    }

    private void prepare(String text) {
        LineTokenizer stk = new LineTokenizer(text);

        while (stk.hasMoreTokens()) {
            ComponentLine line = new ComponentLine(trimEnded(stk.nextToken()), myTextFont, myTextColor);
//out();
//out("prepare");
//out(line.getWidth() + " '" + line + "'");
//line.show();
            myLines.add(line);
        }
    }

    private void prepare(AttributedCharacterIterator[] iterators) {
        for (AttributedCharacterIterator iterator : iterators) {
            ComponentLine line = new ComponentLine(iterator, myTextFont, myTextColor);
//out();
//out(line.getWidth() + " '" + line + "'");
//line.show();
            myLines.add(line);
        }
    }

    private void perform() {
        removeEmptyLinesAtTheEnd();

        if (myLineNumbers) {
            prepareLineNumbering();
        }
        calculateOffset();

        if (myWrapLines) {
            prepareWrapLines();
        }
        else {
            prepareNoWrapLines();
        }
        calculateMetrics();
    }

    private void removeEmptyLinesAtTheEnd() {
        int i = myLines.size() - 1;

        while (i >= 0) {
            ComponentLine line = myLines.get(i--);

            if (line.isEmpty()) {
                myLines.remove(line);
            }
            else {
                break;
            }
        }
    }

    private String trimEnded(String value) {
        int i = value.length() - 1;

        while (i >= 0 && value.charAt(i) == ' ') {
            i--;
        }
        return value.substring(0, i + 1);
    }

    private void prepareLineNumbering() {
        int length = (myLines.size() + "").length(); // NOI18N
        int number = 1;

        for (ComponentLine line : myLines) {
            line.prepend(getNumber(number++, length));
        }
    }

    private void prepareNoWrapLines() {
//out();
        int maxWidth = 0;

        for (ComponentLine line : myLines) {
            int width = line.getWidth();

            if (width > maxWidth) {
                maxWidth = width;
            }
//out("" + maxWidth + " " + width + " '" + line + "'");
        }
        myWidth = maxWidth + myMinOffset;
//out();
//out(" WIDTH: " + myWidth);
    }

    private void prepareWrapLines() {
        myWidth = Config.getDefault().getPageWidth();
//out("Width: " + myWidth);
        List<ComponentLine> lines = new ArrayList<ComponentLine>();

        for (ComponentLine line : myLines) {
//out("  see: " + line.getWidth() + " " + line);
            if (line.getWidth() + myMinOffset <= myWidth) {
                lines.add(line);
            }
            else {
                addWordWrappedLine(lines, line);
            }
        }
        myLines = lines;
    }

    private void addWordWrappedLine(List<ComponentLine> lines, ComponentLine line) {
//out();
//out("add word wrap: '" + line);
        if (line.getWidth() + myMinOffset <= myWidth) {
            lines.add(line);
            return;
        }
        int last = line.length();
        ComponentLine part;
        int k;

        while (true) {
//out("  while: '" + line + "' " + last);
            k = line.lastIndexOf(' ', last - 1);

            if (k == -1) {
                addCharWrappedLine(lines, line);
                break;
            }
            last = k;
            part = line.substring(0, k);
            checkOffset(part);

            if (part.getWidth() + myMinOffset <= myWidth) {
//out("   -- '" + part + "' " + k);
                if (part.isEmpty()) {
                    addCharWrappedLine(lines, line);
                }
                else {
                    lines.add(part);
                    part = line.substring(k + 1);
                    checkOffset(part);
                    addWordWrappedLine(lines, part);
                }
                break;
            }
        }
    }

    private void addCharWrappedLine(List<ComponentLine> lines, ComponentLine line) {
        if (line.getWidth() + myMinOffset <= myWidth) {
            lines.add(line);
            return;
        }
        ComponentLine part;
        int k = line.length();

        while (k >= 0) {
            part = line.substring(0, k);
            checkOffset(part);

            if (part.getWidth() + myMinOffset <= myWidth) {
                lines.add(part);
                part = line.substring(k);
                checkOffset(part);
                addCharWrappedLine(lines, part);
                break;
            }
            k--;
        }
    }

    private void checkOffset(ComponentLine line) {
        int offset = -line.getOffset();

        if (offset > myMinOffset) {
            myMinOffset = offset;
        }
    }

    private void calculateOffset() {
        myMinOffset = 0;

        for (ComponentLine line : myLines) {
            checkOffset(line);
        }
//out("OFFSET: " + myMinOffset);
    }

    private void calculateMetrics() {
        myHeight = 0;

        int size = myLines.size();
        myAscent = new int[size];
        myDescent = new int[size];
        myLeading = new int[size];
        myCorrection = new int[size];

        int pageHeight = Config.getDefault().getPageHeight();
        int breakPosition = pageHeight;
        int prevPos;
//out();

        for (int i = 0; i < size; i++) {
            ComponentLine line = myLines.get(i);

            myAscent[i] = (int) Math.round(line.getAscent() * myLineSpacing);
            myDescent[i] = line.getDescent();
            myCorrection[i] = 0;
            prevPos = myHeight;
//out(getAscent(line) + " " + getDescent(line) + " " + getLeading(line));

            myHeight += myAscent[i] + myDescent[i];

            if (myHeight > breakPosition && prevPos < breakPosition) {
                myCorrection[i] = breakPosition - prevPos;
                myHeight += myCorrection[i];
                breakPosition += pageHeight;
            }
            if (i != size - 1) {
                myLeading[i] = line.getLeading();
                myHeight += myLeading[i];
            }
        }
//out("HEIGHT: " + myHeight);
    }

    @Override
    public int getWidth() {
        return myWidth;
    }

    @Override
    public int getHeight() {
        return myHeight;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = Config.getDefault().getGraphics(graphics);

        g.setColor(myBackgroundColor);
        g.fillRect(0, 0, myWidth, myHeight);

        int y = 0;

        for (int i = 0; i < myLines.size(); i++) {
            ComponentLine line = myLines.get(i);
            y += myCorrection[i] + myAscent[i];
            line.draw(g, myMinOffset, y);

            y += myDescent[i] + myLeading[i];
        }
    }

    private String getNumber(int number, int length) {
        StringBuilder builder = new StringBuilder();
        builder.append(number);

        for (int i = builder.length(); i < length; i++) {
            builder.insert(0, " "); // NOI18N
        }
        builder.append(" "); // NOI18N

        return builder.toString();
    }

    // ---------------------------------------
    private static final class LineTokenizer {

        public LineTokenizer(String value) {
            myValue = value;
            myLength = value.length();
            myBuilder = new StringBuilder();
        }

        public boolean hasMoreTokens() {
            return myPos < myLength;
        }

        public String nextToken() {
            myBuilder.setLength(0);
            String separator = "";
            char c;

            while (myPos < myLength) {
                c = myValue.charAt(myPos);
                myPos++;

                if (c == '\r' || c == '\n') {
                    if (c == '\r' && myPos < myLength && myValue.charAt(myPos) == '\n') {
                        // Unix - "\n", Windows - "\r\n", Mac - "\r"
                        myPos++;
                    }
                    break;
                }
                myBuilder.append(c);
            }
            return myBuilder.toString();
        }

        private int myPos;
        private int myLength;
        private String myValue;
        private StringBuilder myBuilder;
    }

    private int[] myCorrection;
    private int[] myAscent;
    private int[] myDescent;
    private int[] myLeading;
    private int myMinOffset;
    private Font myTextFont;
    private int myWidth;
    private int myHeight;
    private Color myBackgroundColor;
    private Color myTextColor;
    private double myLineSpacing;
    private boolean myWrapLines;
    private boolean myLineNumbers;
    private List<ComponentLine> myLines;
}
