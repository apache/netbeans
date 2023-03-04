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

package org.netbeans.modules.csl.editor.completion;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import org.openide.awt.GraphicsUtils;

/**
 *
 * @author  Dusan Balek
 */
public class MethodParamsTipPaintComponent extends JToolTip {
    private int drawX;
    private int drawY;
    private int drawHeight;
    private int drawWidth;
    private Font drawFont;
    private int fontHeight;
    private int descent;
    private FontMetrics fontMetrics;

    private List<List<String>> params;
    private int idx;
    private JTextComponent component;

    public MethodParamsTipPaintComponent(List<List<String>> params, int idx, JTextComponent component){
        super();
        this.params = params;
        this.idx = idx;
        this.component = component;
    }
    
    public @Override void paint(Graphics g) {
        GraphicsUtils.configureDefaultRenderingHints(g);
        super.paint(g);
    }

    public @Override void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(getForeground());
        draw(g);
    }

    protected void draw(Graphics g) {
        Insets in = getInsets();
        GraphicsConfiguration gc = component.getGraphicsConfiguration();
        int screenWidth = gc != null ? gc.getBounds().width : Integer.MAX_VALUE;
        if (in != null) {
            drawX = in.left;
            drawY = in.top;
        } else {
            drawX = 0;
            drawY = 0;
        }
        drawY += (fontHeight - descent);

        int startX = drawX;
        drawWidth = drawX;
        // This code is deliberately a bit different from the Retouche version
        // of this class. With the way I'm using the parameters list (a 2d set
        // of parameters balanced by width) the code would incorrectly highlight
        // the idx'th element on EVERY row. So I've modified the code by moving
        // the i-count out outside the iteration over the rows, and I've removed
        // the check to see if the index is creater than the list length.
        int i = 0;
        for (List<String> p : params) {
            //int i = 0;
            //int plen = p.size() - 1;
            for (String s : p) {
                if (getWidth(s, i == idx/* || i == plen && idx > plen*/ ? getDrawFont().deriveFont(Font.BOLD) : getDrawFont()) + drawX > screenWidth) {
                    drawY += fontHeight;
                    drawX = startX + getWidth("        ", drawFont); //NOI18N
                }
                drawString(g, s, i == idx/* || i == plen && idx > plen*/ ? getDrawFont().deriveFont(Font.BOLD) : getDrawFont());
                if (drawWidth < drawX)
                    drawWidth = drawX;
                i++;
            }
            drawY += fontHeight;
            drawX = startX;
        }
        drawHeight = drawY - fontHeight + descent;
        if (in != null) {
            drawHeight += in.bottom;
            drawWidth += in.right;
        }
    }

    protected void drawString(Graphics g, String s, Font font) {
        if (g != null) {
            g.setFont(font);
            g.drawString(s, drawX, drawY);
            g.setFont(drawFont);
        }
        drawX += getWidth(s, font);
    }

    protected int getWidth(String s, Font font) {
        if (font == null) return fontMetrics.stringWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    protected int getHeight(String s, Font font) {
        if (font == null) return fontMetrics.stringWidth(s);
        return getFontMetrics(font).stringWidth(s);
    }

    public @Override void setFont(Font font) {
        super.setFont(font);
        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        
        descent = fontMetrics.getDescent();
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    public @Override Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        return new Dimension(drawWidth, drawHeight);
    }

}
