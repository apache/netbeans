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

package org.netbeans.modules.xml.schema.completion;

import java.awt.*;
import javax.swing.*;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public abstract class CompletionPaintComponent extends JPanel {
    public static final int DEFAULT_ICON_WIDTH = 16;
    public static final int DEFAULT_ICON_TEXT_GAP = 5;

    protected int drawX;
    protected int drawY;
    protected int drawHeight;
    private Font drawFont;
    private int iconTextGap = DEFAULT_ICON_TEXT_GAP;
    private int fontHeight;
    private int ascent;
    private FontMetrics fontMetrics;
    private boolean isSelected;
    private CompletionResultItem completionItem;

    private static final String THROWS = " throws "; // NOI18N
    private static String str; //completion item text

    /**
     * Creates a new instance of CompletionPaintComponent
     */
    public CompletionPaintComponent(CompletionResultItem item) {
        this.completionItem = item;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
    
    public  boolean isSelected() {
        return isSelected;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        java.awt.Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        draw(g);
    }

    protected void draw(Graphics g) {
        drawIcon(g, completionItem.getIcon());
        drawString(g, completionItem.getDisplayText(), completionItem.getPaintColor(),
            getDrawFont());
    }

    /**
     * Draw the icon if it is valid for the given type.
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
            drawX += icon.getIconWidth() + iconTextGap;
            drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            int extraPaintGap = completionItem.getExtraPaintGap();
            drawX += extraPaintGap + iconTextGap;
            drawHeight = fontHeight;
        }
        if (i != null) {
            drawHeight += i.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;
    }

    protected void drawString(Graphics g, String s, Color c, Font font) {
        if (g != null) {
            g.setColor(getColor(s, c));
            g.setFont(font);
        }
        drawStringToGraphics(g, s, font);
    }

    protected void drawStringToGraphics(Graphics g, String s, Font font) {
        if (g != null) {
            g.drawString(s, drawX, drawY);
        }
        drawX += getWidth(s, font);
    }

    protected int getWidth(String s, Font font) {
        if (font != null)
            return getFontMetrics(getDrawFont()).stringWidth(s)*2;
        return (s == null)?fontMetrics.stringWidth(""):fontMetrics.stringWidth(s)*2;
    }

    protected Color getColor(String s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        drawFont = font;
    }

    protected Font getDrawFont() {
        return drawFont;
    }

    @Override
    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        if (drawX > getMaximumSize().width)
            drawX = getMaximumSize().width;
        return new Dimension(drawX, drawHeight);
    }
    
    CompletionResultItem getCompletionItem() {
        return completionItem;
    }
    
    public static class AttributePaintComponent extends CompletionPaintComponent {
        public AttributePaintComponent(CompletionResultItem item) {
            super(item);
        }

        @Override
        protected Font getDrawFont() {
            AbstractAttribute aa = (AbstractAttribute)getCompletionItem().
                    getAXIComponent();
            if(aa instanceof Attribute) {
                if(((Attribute)aa).getUse() == Use.REQUIRED) {
                    return super.getDrawFont().deriveFont(Font.BOLD);
                }
            }

            return super.getFont();
        }
    }

    public static class DefaultCompletionPaintComponent extends CompletionPaintComponent {
        public DefaultCompletionPaintComponent(CompletionResultItem item) {
            super(item);
        }
    }

    public static class ElementPaintComponent extends CompletionPaintComponent {
        public ElementPaintComponent(CompletionResultItem item) {
            super(item);
        }        
    }
    
    public static class ValuePaintComponent extends CompletionPaintComponent {
        public ValuePaintComponent(CompletionResultItem item) {
            super(item);
        }        
    }
}