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

package org.netbeans.modules.xml.text.completion;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;
import java.util.HashMap;
import java.text.AttributedString;
import javax.swing.*;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLCompletionResultItemPaintComponent extends JPanel {
    private Icon icon;
    
    /**
     * Creates a new instance of XMLCompletionResultItemPaintComponent
     */
    public XMLCompletionResultItemPaintComponent() {
        super();
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    }
    
    
    public void setString(String str){
        this.str = str;
    }
    
    public void setSelected(boolean isSelected){
        this.isSelected = isSelected;
    }
    
    protected void setDeprecated(boolean isDeprecated){
        this.isDeprecated = isDeprecated;
    }

    public  boolean isSelected(){
        return isSelected;
    }

    protected boolean isDeprecated(){
        return isDeprecated;
    }

    protected Icon getIcon() {
        return icon;
    }
    
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
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
            drawX += icon.getIconWidth() + iconTextGap;
            drawHeight = Math.max(fontHeight, icon.getIconHeight());
        } else {
            drawHeight = fontHeight;
        }
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

    protected int getWidth(String s, Font font) {
        if (font != null)
            return getFontMetrics(getDrawFont()).stringWidth(s)*2;
        return (s == null)?fontMetrics.stringWidth(""):fontMetrics.stringWidth(s)*2;
    }

    protected Color getColor(String s, Color defaultColor) {
        return isSelected ? getForeground()
        : defaultColor;
    }

    private void storeWidth(String s) {
        fontMetrics.stringWidth(s);
    }

    public void setFont(Font font) {
        super.setFont(font);

        fontMetrics = this.getFontMetrics(font);
        fontHeight = fontMetrics.getHeight();
        ascent = fontMetrics.getAscent();
        if (widths != null) {
            widths.clear();
        } else {
            widths = new HashMap();
        }
        for (int i = 0; i < frequentWords.length; i++) {
            storeWidth(frequentWords[i]);
        }
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
        if (drawX > getMaximumSize().width)
            drawX = getMaximumSize().width;
        return new Dimension(drawX, drawHeight);
    }

    public static class StringPaintComponent extends XMLCompletionResultItemPaintComponent {
        private Color c;
        
        public StringPaintComponent(Color c) {
            this.c = c;
        }
        
        protected void draw(Graphics g){
            drawIcon(g, getIcon());
            drawString(g, str, c);
        }
    }
    
    static final String PACKAGE = "org/netbeans/modules/editor/resources/completion/defaultFolder.gif"; // NOI18N    
    protected int drawX;
    protected int drawY;
    protected int drawHeight;
    private Font drawFont;
    private int iconTextGap = 5;
    private int fontHeight;
    private int ascent;
    private Map widths;
    private FontMetrics fontMetrics;
    private boolean isSelected;
    private boolean isDeprecated;
    private static final String THROWS = " throws "; // NOI18N
    private static String str; //completion item text
    private static final String[] frequentWords = new String[] {
        "", " ", "[]", "(", ")", ", ", "String", THROWS // NOI18N
    };
    public static final Color KEYWORD_COLOR = Color.darkGray;
    public static final Color TYPE_COLOR = Color.black;    

}
