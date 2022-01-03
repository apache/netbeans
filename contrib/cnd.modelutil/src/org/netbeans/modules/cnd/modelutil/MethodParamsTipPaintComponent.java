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
package org.netbeans.modules.cnd.modelutil;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

/**
 *
 */
public class MethodParamsTipPaintComponent extends JToolTip {

    private int drawX;
    private int drawY;
    private int drawHeight;
    private int drawWidth;
    private Font drawFont;
    private int fontHeight;
    private int ascent;
    private FontMetrics fontMetrics;
    private List<List<String>> params;
    private int idx;

    public MethodParamsTipPaintComponent(List<List<String>> params, int idx) {
        super();
        this.params = params;
        this.idx = idx;
    }

    @Override
    public void paintComponent(Graphics g) {
        // clear background
        g.setColor(getBackground());
        Rectangle r = g.getClipBounds();
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(getForeground());
        draw(g);
    }

    protected void draw(Graphics g) {
        Insets in = getInsets();
        if (in != null) {
            drawX = in.left;
            drawY = in.top;
        } else {
            drawX = 0;
            drawY = 0;
        }
        drawHeight = fontHeight;
        if (in != null) {
            drawHeight += in.bottom;
        }
        drawHeight += drawY;
        drawY += ascent;

        int startX = drawX;
        drawWidth = drawX;
        int i = 0;
        for (Iterator<List<String>> it = params.iterator(); it.hasNext(); i = 0) {
            for (Iterator<String> itt = it.next().iterator(); itt.hasNext(); i++) {
                String s = itt.next();
                drawString(g, s, i == idx ? getDrawFont().deriveFont(Font.BOLD) : null);
            }
            if (drawWidth < drawX) {
                drawWidth = drawX;
            }
            drawY += drawHeight;
            drawX = startX;
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
        if (font == null) {
            return fontMetrics.stringWidth(s);
        }
        return getFontMetrics(font).stringWidth(s);
    }

    protected int getHeight(String s, Font font) {
        if (font == null) {
            return fontMetrics.stringWidth(s);
        }
        return getFontMetrics(font).stringWidth(s);
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
        return new Dimension(drawWidth, drawHeight * params.size());
    }
}
