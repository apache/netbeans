/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.editor.completion;

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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Dusan Balek, Andrei Badea, Marek Fukala
 */
public class CCPaintComponent extends JPanel {
    
    static final String COLUMN_ICON = "org/netbeans/modules/j2ee/persistence/editor/completion/resources/column.gif"; //NOI18N
    static final String TABLE_ICON = "org/netbeans/modules/j2ee/persistence/editor/completion/resources/table.gif"; //NOI18
    static final String NOCONNECTION_ICON = "org/netbeans/modules/j2ee/persistence/editor/completion/resources/connectionDisconnected.gif"; //NOI18
    static final String CONNECTION_ICON = "org/netbeans/modules/j2ee/persistence/ui/resources/connection.gif"; //NOI18
    static final String PU_ICON = "org/netbeans/modules/j2ee/persistence/ui/resources/EntityNodeIcon.gif"; //NOI18
    
    private static final int ICON_WIDTH = 16;
    private static final int ICON_TEXT_GAP = 5;
    
    protected int drawX;
    
    protected int drawY;
    
    protected int drawHeight;
    
    private Font drawFont;
    
    private int fontHeight;
    
    private int ascent;
    
    private Map widths;
    
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
        Integer i = (Integer)widths.get(s);
        if (i != null) {
            return i.intValue();
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
    
    public static final class ColumnElementPaintComponent extends NbStringPaintComponent {
        
        private String tableName, columnName;
        
        public void setContent(String columnName, String tableName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(COLUMN_ICON)));
            drawString(g, tableName+".", Color.BLACK);
            drawString(g, columnName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
    
    public static final class TableElementPaintComponent extends NbStringPaintComponent {
        
        private String tableName;
        
        public void setContent(String tableName) {
            this.tableName = tableName;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(TABLE_ICON)));
            drawString(g, tableName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
    
    public static final class PersistenceUnitElementPaintComponent extends NbStringPaintComponent {
        
        private String puName;
        
        public void setContent(String puName) {
            this.puName = puName;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(PU_ICON)));
            drawString(g, puName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
    public static final class NamedQueryNameElementPaintComponent extends NbStringPaintComponent {
        
        private String puName;
        
        public void setContent(String puName) {
            this.puName = puName;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(PU_ICON)));
            drawString(g, puName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
    
    public static final class EntityPropertyElementPaintComponent extends NbStringPaintComponent {
        
        private String elName;
        
        public void setContent(String elName) {
            this.elName = elName;
        }
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(PU_ICON)));
            drawString(g, elName, Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }
        
    }
    
    public static final class NoConnectionItemPaintComponent extends NbStringPaintComponent {
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(NOCONNECTION_ICON)));
            drawString(g, NbBundle.getMessage(CCPaintComponent.class, "LBL_ConnectToDatabase"), Color.RED, getDrawFont().deriveFont(Font.BOLD), false);
        }

    }
    public static final class AddConnectionItemPaintComponent extends NbStringPaintComponent {
        
        @Override
        protected void draw(Graphics g){
            drawIcon(g, new ImageIcon(ImageUtilities.loadImage(CONNECTION_ICON)));
            drawString(g, NbBundle.getMessage(CCPaintComponent.class, "LBL_AddConnection"), Color.BLACK, getDrawFont().deriveFont(Font.BOLD), false);
        }

    }
}
