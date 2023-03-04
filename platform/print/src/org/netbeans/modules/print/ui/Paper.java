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
package org.netbeans.modules.print.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;

import org.netbeans.spi.print.PrintPage;
import org.netbeans.modules.print.util.Macro;
import org.netbeans.modules.print.util.Config;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.12.14
 */
final class Paper extends JComponent {

    Paper(PrintPage page, String name, Date lastModified) {
        myPage = page;
        myName = name;
        myLastModified = lastModified;

        myPaperWidth = Config.getDefault().getPaperWidth();
        myPaperHeight = Config.getDefault().getPaperHeight();
        myPageX = Config.getDefault().getPageX();
        myPageY = Config.getDefault().getPageY();
        myPageWidth = Config.getDefault().getPageWidth();
        myPageHeight = Config.getDefault().getPageHeight();

        myHasBorder = Config.getDefault().hasBorder();
        myBorderColor = Config.getDefault().getBorderColor();

        myIsPainting = true;
    }

    void setCoordinate(int number, int row, int column, double scale) {
        myNumber = number;
        myRow = row;
        myColumn = column;
        myRowNumber = String.valueOf(row + 1);
        myColumnNumber = String.valueOf(column + 1);
        setScale(scale);
    }

    void setCount(int count) {
        myCount = String.valueOf(count);

        myHeaderY = Config.getDefault().getHeaderY();
        myHasHeader = Config.getDefault().hasHeader();
        myHeaderLeft = expandTitle(Config.getDefault().getHeaderLeft());
        myHeaderCenter = expandTitle(Config.getDefault().getHeaderCenter());
        myHeaderRight = expandTitle(Config.getDefault().getHeaderRight());
        myHeaderColor = Config.getDefault().getHeaderColor();
        myHeaderFont = Config.getDefault().getHeaderFont();

        myFooterY = Config.getDefault().getFooterY();
        myHasFooter = Config.getDefault().hasFooter();
        myFooterLeft = expandTitle(Config.getDefault().getFooterLeft());
        myFooterCenter = expandTitle(Config.getDefault().getFooterCenter());
        myFooterRight = expandTitle(Config.getDefault().getFooterRight());
        myFooterColor = Config.getDefault().getFooterColor();
        myFooterFont = Config.getDefault().getFooterFont();
    }

    int getRow() {
        return myRow;
    }

    int getColumn() {
        return myColumn;
    }

    void setScale(double scale) {
        myScale = scale;

        if (myIsPainting) {
            setPreferredSize(new Dimension((int) Math.floor((myPaperWidth + SHADOW_WIDTH) * myScale), (int) Math.floor((myPaperHeight + SHADOW_WIDTH) * myScale)));
        }
        else {
            setPreferredSize(new Dimension(myPaperWidth, myPaperHeight));
        }
    }

    int getPaperWidth() {
        return myPaperWidth + SHADOW_WIDTH;
    }

    int getPaperHeight() {
        return myPaperHeight + SHADOW_WIDTH;
    }

    @Override
    public void print(Graphics g) {
        myIsPainting = false;
        setScale(1.0);
        super.print(g);
        myIsPainting = true;
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g = Config.getDefault().getGraphics(graphics);

        // scaling
        if (myIsPainting) {
            g.scale(myScale, myScale);
        }

        // background
        g.setColor(Color.white);
        g.fillRect(myPageX, myPageY, myPageWidth, myPageHeight);

        // page
        g.translate(myPageX, myPageY);
        myPage.print(g);
        g.translate(-myPageX, -myPageY);

        // horizontal margin
        g.setColor(Color.white);
        g.fillRect(0, 0, myPaperWidth, myPageY);
        g.fillRect(0, myPageY + myPageHeight, myPaperWidth, myPaperHeight);

        // header
        if (myHasHeader) {
            drawTitle(g, myHeaderLeft, myHeaderCenter, myHeaderRight, myHeaderY, myHeaderColor, myHeaderFont);
        }

        // footer
        if (myHasFooter) {
            drawTitle(g, myFooterLeft, myFooterCenter, myFooterRight, myFooterY, myFooterColor, myFooterFont);
        }

        // vertical margin
        g.setColor(Color.white);
        g.fillRect(0, 0, myPageX, myPaperHeight);
        g.fillRect(myPageX + myPageWidth, 0, myPaperWidth, myPaperHeight);

        // shadow
        if (myIsPainting) {
            g.setColor(Color.gray.darker());
            g.fillRect(myPaperWidth, SHADOW_WIDTH, SHADOW_WIDTH + 1, myPaperHeight);
            g.fillRect(SHADOW_WIDTH, myPaperHeight, myPaperWidth, SHADOW_WIDTH + 1);
            g.setColor(Color.lightGray);
            g.fillRect(myPaperWidth, 0, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
            g.fillRect(0, myPaperHeight, SHADOW_WIDTH + 1, SHADOW_WIDTH + 1);
        }

        // box
        if (myIsPainting) {
            g.setColor(Color.black);
            g.drawRect(0, 0, myPaperWidth, myPaperHeight);
        }

        // border
        if (myHasBorder) {
            g.setColor(myBorderColor);
            g.drawRect(myPageX, myPageY, myPageWidth, myPageHeight);
        }

        // number
        if (myIsPainting) {
            g.setColor(NUMBER_FONT_COLOR);
            g.setFont(NUMBER_FONT_NAME);
            g.drawString(Integer.toString(myNumber), NUMBER_X, NUMBER_Y);
        }
    }

    private void drawTitle(Graphics2D g, String left, String center, String right, int y, Color color, Font f) {
        g.setColor(color);
        drawTitle(g, left, myPageX, y, f);
        drawTitle(g, center, myPageX + (myPageWidth - getWidth(center, f)) / 2, y, f);
        drawTitle(g, right, myPageX + myPageWidth - getWidth(right, f), y, f);
    }

    private void drawTitle(Graphics2D g, String text, int x, int y, Font font) {
        g.setFont(font);
        g.drawString(text, x, y);
    }

    private String expandTitle(String t) {
        Date printed = new Date(System.currentTimeMillis());

        t = replace(t, Macro.NAME.getName(), myName);
        t = replace(t, Macro.ROW.getName(), myRowNumber);
        t = replace(t, Macro.COLUMN.getName(), myColumnNumber);
        t = replace(t, Macro.USER.getName(), USER_NAME);
        t = replace(t, Macro.COUNT.getName(), myCount);
        t = replace(t, Macro.MODIFIED_DATE.getName(), getDate(myLastModified));
        t = replace(t, Macro.MODIFIED_TIME.getName(), getTime(myLastModified));
        t = replace(t, Macro.PRINTED_DATE.getName(), getDate(printed));
        t = replace(t, Macro.PRINTED_TIME.getName(), getTime(printed));

        return t;
    }

    private int getWidth(String text, Font font) {
        return (int) Math.ceil(font.getStringBounds(text, Config.FONT_RENDER_CONTEXT).getWidth());
    }

    private String getDate(Date timestamp) {
        return getTimestamp(timestamp, "yyyy.MM.dd"); // NOI18N
    }

    private String getTime(Date timestamp) {
        return getTimestamp(timestamp, "HH:mm:ss"); // NOI18N
    }

    private String getTimestamp(Date timestamp, String format) {
        return new SimpleDateFormat(format).format(timestamp);
    }

    private int myNumber;
    private double myScale;
    private PrintPage myPage;
    private boolean myIsPainting;
    private int myPaperWidth;
    private int myPaperHeight;
    private int myPageX;
    private int myPageY;
    private int myPageWidth;
    private int myPageHeight;
    private int myHeaderY;
    private boolean myHasHeader;
    private String myHeaderLeft;
    private String myHeaderCenter;
    private String myHeaderRight;
    private Color myHeaderColor;
    private Font myHeaderFont;
    private int myFooterY;
    private boolean myHasFooter;
    private String myFooterLeft;
    private String myFooterCenter;
    private String myFooterRight;
    private Color myFooterColor;
    private Font myFooterFont;
    private boolean myHasBorder;
    private Color myBorderColor;
    private String myName;
    private String myCount;
    private String myRowNumber;
    private String myColumnNumber;
    private Date myLastModified;
    private int myRow;
    private int myColumn;
    private static final int NUMBER_FONT_SIZE = 35;
    private static final int SHADOW_WIDTH = 10; // .pt
    private static final int NUMBER_X = (int) Math.round(NUMBER_FONT_SIZE * 1.0);
    private static final int NUMBER_Y = (int) Math.round(NUMBER_FONT_SIZE * 1.5);
    private static final Color NUMBER_FONT_COLOR = new Color(125, 125, 255);
    private static final String USER_NAME = System.getProperty("user.name"); // NOI18N
    private static final Font NUMBER_FONT_NAME = new Font("Serif", Font.BOLD, NUMBER_FONT_SIZE); // NOI18N
}
