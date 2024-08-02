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
package org.netbeans.modules.jakarta.web.beans.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;

/**
 *
 * @author Dusan Balek, Andrei Badea, Marek Fukala
 */
public class CCPaintComponent extends JPanel {
        
    private static final int ICON_WIDTH = 16;
    private static final int ICON_TEXT_GAP = 5;
    
    protected int drawX;
    
    protected int drawY;
    
    protected int drawHeight;
    
    private Font drawFont;
    
    private int fontHeight;
    
    private int ascent;
    
    private Map<String, Integer> widths;
    
    private FontMetrics fontMetrics;
    
    private boolean isSelected;
    
    private boolean isDeprecated;
    
    private static final String THROWS = " throws "; // NOI18N
    
    
    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };
    
    public static final Color KEYWORD_COLOR = Color.darkGray;
    public static final Color TYPE_COLOR = Color.black;
    
    /** When an outer method/constructor is rendered. */
    static final Color ENCLOSING_CALL_COLOR = Color.gray;
    /** When an active parameter gets rendered. */
    static final Color ACTIVE_PARAMETER_COLOR = Color.black;
    
    public CCPaintComponent(){
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }
    
    protected void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    protected void setDeprecated(boolean isDeprecated){
        this.isDeprecated = isDeprecated;
    }
    
    protected boolean isSelected(){
        return isSelected;
    }
    
    protected boolean isDeprecated(){
        return isDeprecated;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);
    }
    
    protected void draw(Graphics g){
    }
    
    
    /** Draw the icon if it is valid for the given type.
     * Here the initial drawing assignments are also done.
     */
    protected void drawIcon(Graphics g, Icon icon) {
        Insets i = getInsets();
        if (i != null) {
            drawX = i.left;
            drawY = i.top;
        } else {
            drawX = 0;
            drawY = 0;
        }
        
        if (icon != null) {
            if (g != null) {
                icon.paintIcon(this, g, drawX, drawY);
            }
            drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawHeight = fontHeight;
        }
        drawX += ICON_WIDTH + ICON_TEXT_GAP;
        if (i != null) {
            drawHeight += i.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;
    }
    
    protected void drawString(Graphics g, String s){
        drawString(g, s, false);
    }
    
    /** Draw string using the foreground color */
    protected void drawString(Graphics g, String s, boolean strike) {
        if (g != null) {
            g.setColor(getForeground());
        }
        drawStringToGraphics(g, s, null, strike);
    }
    
    
    /** Draw string with given color which is first possibly modified
     * by calling getColor() method to care about selection etc.
     */
    protected void drawString(Graphics g, String s, Color c) {
        if (g != null) {
            g.setColor(getColor(s, c));
        }
        drawStringToGraphics(g, s);
    }
    
    protected void drawString(Graphics g, String s, Color c, Font font, boolean strike) {
        if (g != null) {
            g.setColor(getColor(s, c));
            g.setFont(font);
        }
        drawStringToGraphics(g, s, font,  strike);
        if (g != null) {
            g.setFont(drawFont);
        }
        
    }
    
    protected void drawTypeName(Graphics g, String s, Color c) {
        if (g == null) {
            drawString(g, "   "); // NOI18N
            drawString(g, s, c);
        } else {
            int w = getWidth() - getWidth(s) - drawX;
            int spaceWidth = getWidth(" "); // NOI18N
            if (w > spaceWidth * 2) {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s);
            } else {
                drawX = getWidth() - 2 * spaceWidth - getWidth(s) - getWidth("...   "); // NOI18N
                g.setColor(getBackground());
                g.fillRect(drawX, 0, getWidth() - drawX, getHeight());
                drawString(g, "...   ", c); // NOI18N
            }
            drawString(g, s, c);
        }
    }
    
    protected void drawStringToGraphics(Graphics g, String s) {
        drawStringToGraphics(g, s, null, false);
    }
    
    protected void drawStringToGraphics(Graphics g, String s, Font font, boolean strike) {
        if (g != null) {
            if (!strike){
                g.drawString(s, drawX, drawY);
            }else{
                Graphics2D g2 = ((Graphics2D)g);
                AttributedString strikeText = new AttributedString(s);
                strikeText.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
                strikeText.addAttribute(TextAttribute.FONT, g.getFont());
                g2.drawString(strikeText.getIterator(), drawX, drawY);
            }
        }
        drawX += getWidth(s, font);
    }
    
    protected int getWidth(String s) {
        Integer i = widths.get(s);
        if (i != null) {
            return i;
        } else {
            if (s == null) {
                s = "";
            }
            return fontMetrics.stringWidth(s);
        }
    }
    
    protected int getWidth(String s, Font font) {
        if (font == null) {
            return getWidth(s);
        }
        return getFontMetrics(font).stringWidth(s);
    }
    
    protected Color getColor(String s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }
    
    private void storeWidth(String s) {
        fontMetrics.stringWidth(s);
    }
    
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        
        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        if (widths != null) {
            widths.clear();
        } else {
            widths = new HashMap<>();
        }
        for (int i = 0; i < frequentWords.length; i++) {
            storeWidth(frequentWords[i]);
        }
        drawFont = font;
    }
    
    protected Font getDrawFont(){
        return drawFont;
    }
    
    @Override
    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        if (drawX > getMaximumSize().width) {
            drawX = getMaximumSize().width;
        }
        return new Dimension(drawX, drawHeight);
    }
    
    public static class NbStringPaintComponent extends CCPaintComponent {
        
        private String str;
        
        public void setString(String str){
            this.str = str;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, null);
            drawString(g, str, TYPE_COLOR);
        }
        
    }
    
    public static final class DBElementPaintComponent extends NbStringPaintComponent {
        
    }
    
    
    public static final class BeansElementPaintComponent extends NbStringPaintComponent {
        
        private String puName;
        
        public void setContent(String puName) {
            this.puName = puName;
        }
        
        @Override
        protected void draw(Graphics g){
            
            drawString(g, puName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
}
