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
package org.netbeans.modules.print.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.util.StringTokenizer;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import static org.netbeans.modules.print.util.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.03.21
 */
public final class Config {

    private Config() {}

    public boolean showPageSetup() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat oldFormat = getPageFormat();
        PageFormat newFormat = job.pageDialog(oldFormat);

        if (oldFormat == newFormat) {
            return false;
        }
        myPageFormat = newFormat;

        // save
        set(PAGE_ORIENTATION, myPageFormat.getOrientation());
        Paper paper = myPageFormat.getPaper();

        set(PAPER_WIDTH, paper.getWidth());
        set(PAPER_HEIGHT, paper.getHeight());

        set(AREA_X, paper.getImageableX());
        set(AREA_Y, paper.getImageableY());

        set(AREA_WIDTH, paper.getImageableWidth());
        set(AREA_HEIGHT, paper.getImageableHeight());

        return true;
    }

    public PageFormat getPageFormat() {
        PrinterJob job = PrinterJob.getPrinterJob();

        if (myPageFormat == null) {
            myPageFormat = job.defaultPage();

            // restore
            myPageFormat.setOrientation(round(get(PAGE_ORIENTATION, PageFormat.PORTRAIT)));
            Paper paper = myPageFormat.getPaper();

            if (get(PAPER_WIDTH, null) != null && get(PAPER_HEIGHT, null) != null) {
                paper.setSize(get(PAPER_WIDTH, INCH), get(PAPER_HEIGHT, INCH));
            }
            if (get(AREA_X, null) != null && get(AREA_Y, null) != null && get(AREA_WIDTH, null) != null && get(AREA_HEIGHT, null) != null) {
                paper.setImageableArea(get(AREA_X, INCH), get(AREA_Y, INCH), get(AREA_WIDTH, INCH), get(AREA_HEIGHT, INCH));
            }
            myPageFormat.setPaper(paper);
        }
        return myPageFormat;
    }

    // paper
    public int getPaperWidth() {
        return (int) Math.floor(getPageFormat().getWidth());
    }

    public int getPaperHeight() {
        return (int) Math.floor(getPageFormat().getHeight());
    }

    // page
    public int getPageX() {
        return round(getPageFormat().getImageableX());
    }

    public int getPageY() {
        int y = round(getPageFormat().getImageableY());

        if (hasHeader()) {
            y += getBound(getHeaderFont()).getHeight();
        }
        return y;
    }

    public int getPageWidth() {
        return (int) Math.floor(getPageFormat().getImageableWidth());
    }

    public int getPageHeight() {
        int height = (int) Math.floor(getPageFormat().getImageableHeight());

        if (hasHeader()) {
            height -= getBound(getHeaderFont()).getHeight();
        }
        if (hasFooter()) {
            height -= getBound(getFooterFont()).getHeight();
        }
        return height;
    }

    // header
    public int getHeaderY() {
        return getPageY() - round(getBound(getHeaderFont()).getMaxY());
    }

    public int getFooterY() {
        return getPageY() + getPageHeight() + round(getBound(getFooterFont()).getHeight() - getBound(getFooterFont()).getMaxY());
    }

    public boolean hasBorder() {
        return get(BORDER, true);
    }

    public void setBorder(boolean value) {
        set(BORDER, value);
    }

    public boolean isSelection() {
        return get(SELECTION, false);
    }

    public void setSelection(boolean value) {
        set(SELECTION, value);
    }

    public boolean isAsEditor() {
        return get(AS_EDITOR, false);
    }

    public void setAsEditor(boolean value) {
        set(AS_EDITOR, value);
    }

    public Color getBorderColor() {
        return getColor(get(BORDER_COLOR, null), Color.black);
    }

    public void setBorderColor(Color value) {
        set(BORDER_COLOR, getString(value));
    }

    public boolean hasHeader() {
        return get(HEADER, true);
    }

    public void setHeader(boolean value) {
        set(HEADER, value);
    }

    public String getHeaderLeft() {
        return get(HEADER_LEFT, HEADER_LEFT_TEXT);
    }

    public String getHeaderCenter() {
        return get(HEADER_CENTER, HEADER_CENTER_TEXT);
    }

    public String getHeaderRight() {
        return get(HEADER_RIGHT, HEADER_RIGHT_TEXT);
    }

    public void setHeaderLeft(String value) {
        set(HEADER_LEFT, value);
    }

    public void setHeaderCenter(String value) {
        set(HEADER_CENTER, value);
    }

    public void setHeaderRight(String value) {
        set(HEADER_RIGHT, value);
    }

    public Color getHeaderColor() {
        return getColor(get(HEADER_COLOR, null), Color.black);
    }

    public Font getHeaderFont() {
        return getFont(get(HEADER_FONT, null), DEFAULT_TITLE_FONT);
    }

    public void setHeaderColor(Color value) {
        set(HEADER_COLOR, getString(value));
    }

    public void setHeaderFont(Font value) {
        set(HEADER_FONT, getString(value));
    }

    // footer
    public boolean hasFooter() {
        return get(FOOTER, true);
    }

    public void setFooter(boolean value) {
        set(FOOTER, value);
    }

    public String getFooterLeft() {
        return get(FOOTER_LEFT, FOOTER_LEFT_TEXT);
    }

    public String getFooterCenter() {
        return get(FOOTER_CENTER, FOOTER_CENTER_TEXT);
    }

    public String getFooterRight() {
        return get(FOOTER_RIGHT, FOOTER_RIGHT_TEXT);
    }

    public void setFooterLeft(String value) {
        set(FOOTER_LEFT, value);
    }

    public void setFooterCenter(String value) {
        set(FOOTER_CENTER, value);
    }

    public void setFooterRight(String value) {
        set(FOOTER_RIGHT, value);
    }

    public Color getFooterColor() {
        return getColor(get(FOOTER_COLOR, null), Color.black);
    }

    public Font getFooterFont() {
        return getFont(get(FOOTER_FONT, null), DEFAULT_TITLE_FONT);
    }

    public void setFooterColor(Color value) {
        set(FOOTER_COLOR, getString(value));
    }

    public void setFooterFont(Font value) {
        set(FOOTER_FONT, getString(value));
    }

    // text
    public void setWrapLines(boolean value) {
        set(WRAP_LINES, value);
    }

    public boolean isWrapLines() {
        return get(WRAP_LINES, false);
    }

    public boolean isLineNumbers() {
        return get(LINE_NUMBERS, false);
    }

    public void setLineNumbers(boolean value) {
        set(LINE_NUMBERS, value);
    }

    public boolean isUseFont() {
        return get(USE_FONT, true);
    }

    public void setUseFont(boolean value) {
        set(USE_FONT, value);
    }

    public boolean isUseColor() {
        return get(USE_COLOR, true);
    }

    public void setUseColor(boolean value) {
        set(USE_COLOR, value);
    }

    public Color getTextColor() {
        return getColor(get(TEXT_COLOR, null), Color.black);
    }

    public void setTextColor(Color value) {
        set(TEXT_COLOR, getString(value));
    }

    public Font getTextFont() {
        return getFont(get(TEXT_FONT, null), DEFAULT_TEXT_FONT);
    }

    public void setTextFont(Font value) {
        set(TEXT_FONT, getString(value));
    }

    public Color getBackgroundColor() {
        return getColor(get(BACKGROUND_COLOR, null), DEFAULT_BACGROUND_COLOR);
    }

    public void setBackgroundColor(Color value) {
        set(BACKGROUND_COLOR, getString(value));
    }

    public double getLineSpacing() {
        return get(LINE_SPACING, 1.0);
    }

    public void setLineSpacing(double value) {
        set(LINE_SPACING, value);
    }

    // zoom
    public void setZoom(double value) {
        set(ZOOM, value);
    }

    public double getZoom() {
        return get(ZOOM, 1.0);
    }

    // service
    private Preferences getPreferences() {
        return NbPreferences.forModule(Config.class);
    }

    private String get(String name, String defaultValue) {
        return getPreferences().get(name, defaultValue);
    }

    private boolean get(String name, boolean defaultValue) {
        return getPreferences().getBoolean(name, defaultValue);
    }

    private double get(String name, double defaultValue) {
        return getPreferences().getDouble(name, defaultValue);
    }

    private void set(String name, String value) {
        getPreferences().put(name, value);
    }

    private void set(String name, boolean value) {
        getPreferences().putBoolean(name, value);
    }

    private void set(String name, double value) {
        getPreferences().putDouble(name, value);
    }

    private String getString(Color value) {
        return value.getRed() + COMMA + value.getGreen() + COMMA + value.getBlue();
    }

    private String getString(Font value) {
        return value.getName() + COMMA + value.getStyle() + COMMA + value.getSize();
    }

    private Color getColor(String value, Color defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        StringTokenizer stk = new StringTokenizer(value, COMMA);

        int red = integer(stk);
        int green = integer(stk);
        int blue = integer(stk);

        if (red == -1 || green == -1 || blue == -1) {
            return defaultValue;
        }
        return new Color(red, green, blue);
    }

    private Font getFont(String value, Font defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        StringTokenizer stk = new StringTokenizer(value, COMMA);

        String name = getString(stk);
        int style = integer(stk);
        int size = integer(stk);

        if (name == null || style == -1 || size == -1) {
            return defaultValue;
        }
        return new Font(name, style, size);
    }

    private int integer(StringTokenizer stk) {
        if ( !stk.hasMoreTokens()) {
            return -1;
        }
        return getInt(stk.nextToken());
    }

    private String getString(StringTokenizer stk) {
        if ( !stk.hasMoreTokens()) {
            return null;
        }
        return stk.nextToken();
    }

    private Rectangle2D getBound(Font font) {
        return font.getMaxCharBounds(FONT_RENDER_CONTEXT);
    }

    public static String getPageOfCount(String page, String count) {
        return i18n(Config.class, "LBL_Page_of_Count", page, count); // NOI18N
    }

    private static String getRowColumn(String row, String column) {
        return i18n(Config.class, "LBL_Row_Column", row, column); // NOI18N
    }

    public static Config getDefault() {
        return DEFAULT;
    }

    public Graphics2D getGraphics(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        return graphics;
    }

    private PageFormat myPageFormat;
    private static final Config DEFAULT = new Config();
    private static final double INCH = 72.0; // .pt
    private static final Font DEFAULT_TITLE_FONT = new Font("Serif", Font.PLAIN, 10); //NOI18N
    private static final Font DEFAULT_TEXT_FONT = new Font("Monospaced", Font.PLAIN, 10); // NOI18N
    private static final Color DEFAULT_BACGROUND_COLOR = new Color(255, 250, 255);
    private static final String COMMA = ","; // NOI18N
    private static final String EMPTY = ""; // NOI18N
    private static final String HEADER_LEFT_TEXT = Macro.NAME.getName();
    private static final String HEADER_CENTER_TEXT = EMPTY;
    private static final String HEADER_RIGHT_TEXT = EMPTY;
    private static final String FOOTER_LEFT_TEXT = getPageOfCount(getRowColumn(Macro.ROW.getName(), Macro.COLUMN.getName()), Macro.COUNT.getName());
    private static final String FOOTER_CENTER_TEXT = EMPTY;
    private static final String FOOTER_RIGHT_TEXT = Macro.MODIFIED_DATE.getName() + "  " + Macro.MODIFIED_TIME.getName(); // NOI18N
    private static final String SELECTION = "print.text.selection"; // NOI18N
    private static final String AS_EDITOR = "print.text.as.editor"; // NOI18N
    private static final String WRAP_LINES = "print.text.wrap.lines"; // NOI18N
    private static final String LINE_NUMBERS = "print.text.line.numbers"; // NOI18N
    private static final String USE_FONT = "print.text.use.font"; // NOI18N
    private static final String USE_COLOR = "print.text.use.color"; // NOI18N
    private static final String TEXT_COLOR = "print.text.color"; // NOI18N
    private static final String TEXT_FONT = "print.text.font"; // NOI18N
    private static final String LINE_SPACING = "print.text.line.spacing"; // NOI18N
    private static final String BACKGROUND_COLOR = "print.text.background.color"; // NOI18N
    private static final String ZOOM = "print.zoom"; // NOI18N
    private static final String BORDER = "print.border"; // NOI18N
    private static final String BORDER_COLOR = "print.border.color"; // NOI18N
    private static final String HEADER = "print.header"; // NOI18N
    private static final String HEADER_LEFT = "print.header.left"; // NOI18N
    private static final String HEADER_CENTER = "print.header.center"; // NOI18N
    private static final String HEADER_RIGHT = "print.header.right"; // NOI18N
    private static final String HEADER_COLOR = "print.header.color"; // NOI18N
    private static final String HEADER_FONT = "print.header.font"; // NOI18N
    private static final String FOOTER = "print.footer"; // NOI18N
    private static final String FOOTER_LEFT = "print.footer.left"; // NOI18N
    private static final String FOOTER_CENTER = "print.footer.center"; // NOI18N
    private static final String FOOTER_RIGHT = "print.footer.right"; // NOI18N
    private static final String FOOTER_COLOR = "print.footer.color"; // NOI18N
    private static final String FOOTER_FONT = "print.footer.font"; // NOI18N
    private static final String PAGE_ORIENTATION = "print.page.orientation"; // NOI18N
    private static final String PAPER_WIDTH = "print.paper.width"; // NOI18N
    private static final String PAPER_HEIGHT = "print.paper.height"; // NOI18N
    private static final String AREA_X = "print.area.x"; // NOI18N
    private static final String AREA_Y = "print.area.y"; // NOI18N
    private static final String AREA_WIDTH = "print.area.width"; // NOI18N
    private static final String AREA_HEIGHT = "print.area.height"; // NOI18N
    public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);
}
