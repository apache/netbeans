/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.java;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.JTextComponent;

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

    public MethodParamsTipPaintComponent(JTextComponent component){
        super();
        this.component = component;
    }
    
    void setData(List<List<String>> params, int idx) {
        this.params = params;
        this.idx = idx;
    }
    
    void clearData() {
        this.params = null;
        this.idx = -1;
    }
    
    boolean hasData() {
        return params != null;
    }
    
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
        if (params != null) {
            for (List<String> p : params) {
                int i = 0;
                int plen = p.size() - 1;
                for (String s : p) {
                    if (getWidth(s, i == idx || i == plen && idx > plen ? getDrawFont().deriveFont(Font.BOLD) : getDrawFont()) + drawX > screenWidth) {
                        drawY += fontHeight;
                        drawX = startX + getWidth("        ", drawFont); //NOI18N
                    }
                    drawString(g, s, i == idx || i == plen && idx > plen ? getDrawFont().deriveFont(Font.BOLD) : getDrawFont());
                    if (drawWidth < drawX)
                        drawWidth = drawX;
                    i++;
                }
                drawY += fontHeight;
                drawX = startX;
            }
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

    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        
        descent = fontMetrics.getDescent();
        drawFont = font;
    }

    protected Font getDrawFont(){
        return drawFont;
    }

    public Dimension getPreferredSize() {
        draw(null);
        Insets i = getInsets();
        if (i != null) {
            drawX += i.right;
        }
        return new Dimension(drawWidth, drawHeight);
    }

}
