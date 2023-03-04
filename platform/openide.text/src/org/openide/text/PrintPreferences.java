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
package org.openide.text;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.Font;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.prefs.Preferences;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;

/**
 * Allows get, set properties that specify the look of a printed Source Editor page, 
 * including headers, footers, vertical spacing, and line numbers.
 * 
 * @author Radek Matous
 * @since 6.16
 */
public final class PrintPreferences {
    /** Constants for center, right, left position of page header, footer. */    
    public static enum Alignment { LEFT, CENTER, RIGHT};            
    
    private static final PrintPreferences  INSTANCE = new PrintPreferences();    
    /** Property name of the wrap property */
    private static final String PROP_WRAP = "wrap"; // NOI18N
    
    /** Property name of the header format  property */
    private static final String PROP_HEADER_FORMAT = "headerFormat"; // NOI18N
    
    /** Property name of the footer format property */
    private static final String PROP_FOOTER_FORMAT = "footerFormat"; // NOI18N
    
    /** Property name of the header alignment property */
    private static final String PROP_HEADER_ALIGNMENT = "headerAlignment"; // NOI18N
    
    /** Property name of the footer alignment property */
    private static final String PROP_FOOTER_ALIGNMENT = "footerAlignment"; // NOI18N
    
    /** Property names of the page format */
    private static final String PROP_PAGE_ORIENTATION = "pageOrientation";// NOI18N
    private static final String PROP_PAGE_WIDTH = "pageWidth";// NOI18N
    private static final String PROP_PAGE_HEIGHT = "pageHeight";// NOI18N
    private static final String PROP_PAGE_IMAGEABLEAREA_Y = "imageableAreaY";
    private static final String PROP_PAGE_IMAGEABLEAREA_X = "imageableAreaX";
    private static final String PROP_PAGE_IMAGEABLEAREA_WIDTH = "imageableAreaWidth";
    private static final String PROP_PAGE_IMAGEABLEAREA_HEIGHT = "imageableAreaHeight";
    
    /** Property names of the header font property */
    private static final String PROP_HEADER_FONT_NAME = "headerFontName";//NOI18N
    private static final String PROP_HEADER_FONT_STYLE = "headerFontStyle";//NOI18N
    private static final String PROP_HEADER_FONT_SIZE = "headerFontSize";//NOI18N
    
    /** Property names of the footer font property */
    private static final String PROP_FOOTER_FONT_NAME = "footerFontName";//NOI18N
    private static final String PROP_FOOTER_FONT_STYLE = "footerFontStyle";//NOI18N
    private static final String PROP_FOOTER_FONT_SIZE = "footerFontSize";//NOI18N
    
    /** Defaults for both heeader font and foote font */
    private static final String DEFAULT_FONT_NAME = "Monospaced";//NOI18N
    private static final int DEFAULT_FONT_STYLE = java.awt.Font.PLAIN;
    private static final int DEFAULT_FONT_SIZE = 6;
    
    /** Property name of the line ascent correction property */
    private static final String PROP_LINE_ASCENT_CORRECTION = "lineAscentCorrection"; // NOI18N
        
    private PrintPreferences() {}
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(PrintPreferences.class);
    }
    
    /**
     * Get an instance of {@link java.awt.print.PageFormat}.
     * @param pj {@link java.awt.print.PrinterJob} which is 
     * associated with the default printer.
     * @return an instance of <code>PageFormat</code> that describes the size and
     * orientation of a page to be printed.
     */
    public static PageFormat getPageFormat(PrinterJob pj) {
        PageFormat pageFormat = null;
        pageFormat = pj.defaultPage();
        Paper p = pageFormat.getPaper();
        int pageOrientation = getPreferences().getInt(PROP_PAGE_ORIENTATION, pageFormat.getOrientation());
        double paperWidth = getPreferences().getDouble(PROP_PAGE_WIDTH, p.getWidth());
        double paperHeight = getPreferences().getDouble(PROP_PAGE_HEIGHT, p.getHeight());
        
        double iaWidth = getPreferences().getDouble(PROP_PAGE_IMAGEABLEAREA_WIDTH, p.getImageableWidth());
        double iaHeight = getPreferences().getDouble(PROP_PAGE_IMAGEABLEAREA_HEIGHT, p.getImageableHeight());
        double iaX = getPreferences().getDouble(PROP_PAGE_IMAGEABLEAREA_X, p.getImageableX());
        double iaY = getPreferences().getDouble(PROP_PAGE_IMAGEABLEAREA_Y, p.getImageableY());
        
        pageFormat.setOrientation(pageOrientation);
        p.setSize(paperWidth, paperHeight);
        p.setImageableArea(iaX, iaY, iaWidth, iaHeight);
        pageFormat.setPaper(p);
        return pageFormat;
    }
    
    /**
     * @param pf <code>PageFormat</code> that describes the size and
     * orientation of a page to be printed
     */
    public static void setPageFormat(PageFormat pf) {
        getPreferences().putInt(PROP_PAGE_ORIENTATION, pf.getOrientation());
        getPreferences().putDouble(PROP_PAGE_WIDTH, pf.getPaper().getWidth());
        getPreferences().putDouble(PROP_PAGE_HEIGHT, pf.getPaper().getHeight());
        
        getPreferences().putDouble(PROP_PAGE_IMAGEABLEAREA_WIDTH, pf.getPaper().getImageableWidth());
        getPreferences().putDouble(PROP_PAGE_IMAGEABLEAREA_HEIGHT, pf.getPaper().getImageableHeight());
        getPreferences().putDouble(PROP_PAGE_IMAGEABLEAREA_X, pf.getPaper().getImageableX());
        getPreferences().putDouble(PROP_PAGE_IMAGEABLEAREA_Y, pf.getPaper().getImageableY());
    }
    
    /**
     * Wrap lines.
     * @return true if lines that are too long for the page size will be wrapped 
     * to the following line. If false is returned, then long lines will be truncated.     
     */
    public static boolean getWrap() {
        return getPreferences().getBoolean(PROP_WRAP, true);
    }
    
    /**
     * @param wrap 
     * See {@link #getWrap}
     */
    public static void setWrap(boolean wrap) {
        if (getWrap() == wrap) return;
        getPreferences().putBoolean(PROP_WRAP, wrap);
    }
    
    /**
     * See {@link #setHeaderFormat}
     * @return the text for the page header
     */
    public static String getHeaderFormat() {
        return getPreferences().get(PROP_HEADER_FORMAT,
                NbBundle.getMessage(PrintPreferences.class, "CTL_Header_format"));
    }
    
    /**
     * Set the text for the page header.
     * The following special characters can be used:
     * <ul>
     * <li>{0} is replaced with the page number.
     * <li>{1} is replaced with the date and time.
     * <li>{2} is replaced with the file name.
     * </ul>
     * @param s the text for the page header
     */
    public static void setHeaderFormat(String s) {
        if (getHeaderFormat().equals(s)) return;
        getPreferences().put(PROP_HEADER_FORMAT, s);
    }
    
    /**
     * See {@link #setFooterFormat}
     * @return the text for the page footer
     */
    public static String getFooterFormat() {
        return getPreferences().get(PROP_FOOTER_FORMAT,
                NbBundle.getMessage(PrintPreferences.class, "CTL_Footer_format"));
    }
    
    /**
     * Set the text for the page footer.
     * The following special characters can be used:
     * <ul>
     * <li>{0} is replaced with the page number.
     * <li>{1} is replaced with the date and time.
     * <li>{2} is replaced with the file name.
     * </ul>
     * @param s the text for the page footer
     */
    public static void setFooterFormat(String s) {
        if (getFooterFormat().equals(s)) return;
        getPreferences().put(PROP_FOOTER_FORMAT, s);
        
    }
    
    /**
     * @return the font for the header
     */
    public static Font getHeaderFont() {
        String name = getPreferences().get(PROP_HEADER_FONT_NAME,DEFAULT_FONT_NAME);
        int style = getPreferences().getInt(PROP_HEADER_FONT_STYLE,DEFAULT_FONT_STYLE);
        int size = getPreferences().getInt(PROP_HEADER_FONT_SIZE,DEFAULT_FONT_SIZE);
        
        return new Font(name, style, size);
    }
    
    /**
     * @param f the font for the header
     */
    public static void setHeaderFont(Font f) {
        if (getHeaderFont().equals(f)) return;
        getPreferences().put(PROP_HEADER_FONT_NAME,f.getName());
        getPreferences().putInt(PROP_HEADER_FONT_STYLE,f.getStyle());
        getPreferences().putInt(PROP_HEADER_FONT_SIZE,f.getSize());
        
    }
    
    /**
     * @return the font for the footer
     */
    public static Font getFooterFont() {
        String name = getPreferences().get(PROP_FOOTER_FONT_NAME,DEFAULT_FONT_NAME);
        int style = getPreferences().getInt(PROP_FOOTER_FONT_STYLE,DEFAULT_FONT_STYLE);
        int size = getPreferences().getInt(PROP_FOOTER_FONT_SIZE,DEFAULT_FONT_SIZE);
        
        return new Font(name, style, size);
    }
    
    /**
     * @param f the font for the footer
     */
    public static void setFooterFont(Font f) {
        if (getFooterFont().equals(f)) return;
        getPreferences().put(PROP_FOOTER_FONT_NAME,f.getName());
        getPreferences().putInt(PROP_FOOTER_FONT_STYLE,f.getStyle());
        getPreferences().putInt(PROP_FOOTER_FONT_SIZE,f.getSize());
    }
    
    /**
     * @return information whether the header is centered, left aligned, or right aligned.
     */
    public static PrintPreferences.Alignment getHeaderAlignment() {
        return Alignment.valueOf(getPreferences().get(PROP_HEADER_ALIGNMENT, Alignment.CENTER.name()));
    }
    
    /**
     * @param alignment whether the header should be centered, left aligned, or right aligned.
     */
    public static void setHeaderAlignment(PrintPreferences.Alignment alignment) {
        if (getHeaderAlignment() == alignment) return;
        getPreferences().put(PROP_HEADER_ALIGNMENT, alignment.name());
    }
    
    /**
     * @return whether the footer is centered, left aligned, or right aligned.
     */
    public static PrintPreferences.Alignment getFooterAlignment() {
        return Alignment.valueOf(getPreferences().get(PROP_FOOTER_ALIGNMENT, Alignment.CENTER.name()));
    }
    
    /**
     * @param alignment whether the footer should be centered, left aligned, or right aligned.
     */
    public static void setFooterAlignment(PrintPreferences.Alignment alignment) {
        if (getFooterAlignment() == alignment) return;
        getPreferences().put(PROP_FOOTER_ALIGNMENT, alignment.name());
    }
    
    /**
     * @return the amount of vertical space to print between lines.
     */
    public static float getLineAscentCorrection() {
        return getPreferences().getFloat(PROP_LINE_ASCENT_CORRECTION, 1.0f);
    }
    
    /** 
     * @param <code>correction</code> the amount of vertical space to print between lines.
     * @exception IllegalArgumentException if <tt>correction</tt> is less than 0.
     */
    public static void setLineAscentCorrection(float correction) {
        if (getLineAscentCorrection() == correction) return;
        if (correction < 0) {
            throw new IllegalArgumentException();
        }
        getPreferences().putFloat(PROP_LINE_ASCENT_CORRECTION, correction);
    }
}
