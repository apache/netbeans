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

package org.openide.awt;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;
import org.openide.util.Exceptions;

/**
 * A LabelUI which uses the lightweight HTML renderer. Stateless - only one
 * instance should ever exist.
 */
class HtmlLabelUI extends LabelUI {

    /** System property to automatically turn on antialiasing for html strings */
    
    static final boolean antialias = Boolean.getBoolean("nb.cellrenderer.antialiasing") // NOI18N
         ||Boolean.getBoolean("swing.aatext") // NOI18N
         ||(isGTK() && gtkShouldAntialias()) // NOI18N
         || isAqua();
    
    private static HtmlLabelUI uiInstance;
    
    private static int FIXED_HEIGHT;

    static {
        //Jesse mode
        String ht = System.getProperty("nb.cellrenderer.fixedheight"); //NOI18N

        if (ht != null) {
            try {
                FIXED_HEIGHT = Integer.parseInt(ht);
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    private static Map<Object,Object> hintsMap;
    private static Color unfocusedSelBg;
    private static Color unfocusedSelFg;
    private static Boolean gtkAA;

    public static ComponentUI createUI(JComponent c) {
        assert c instanceof HtmlRendererImpl;

        if (uiInstance == null) {
            uiInstance = new HtmlLabelUI();
        }

        return uiInstance;
    }

    public @Override Dimension getPreferredSize(JComponent c) {
        if (GraphicsEnvironment.isHeadless()) {
            // cannot create scratch graphics, so don't bother
            return super.getPreferredSize(c);
        }
        return calcPreferredSize((HtmlRendererImpl) c);
    }

    /** Get the width of the text */
    private static int textWidth(String text, Graphics g, Font f, boolean html) {
        if (text != null) {
            if (html) {
                return Math.round(
                    Math.round(
                        Math.ceil(
                            HtmlRenderer.renderHTML(
                                text, g, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, f, Color.BLACK,
                                HtmlRenderer.STYLE_CLIP, false
                            )
                        )
                    )
                );
            } else {
                return Math.round(
                    Math.round(
                        Math.ceil(
                            HtmlRenderer.renderPlainString(
                                text, g, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, f, Color.BLACK,
                                HtmlRenderer.STYLE_CLIP, false
                            )
                        )
                    )
                );
            }
        } else {
            return 0;
        }
    }

    private Dimension calcPreferredSize(HtmlRendererImpl r) {
        Insets ins = r.getInsets();
        Dimension prefSize = new java.awt.Dimension(ins.left + ins.right, ins.top + ins.bottom);
        String text = r.getText();

        Graphics g = r.getGraphics();
        Icon icon = r.getIcon();

        if (text != null) {
            FontMetrics fm = g.getFontMetrics(r.getFont());
            prefSize.height += (fm.getMaxAscent() + fm.getMaxDescent());
        }

        if (icon != null) {
            if (r.isCentered()) {
                prefSize.height += (icon.getIconHeight() + r.getIconTextGap());
                prefSize.width += icon.getIconWidth();
            } else {
                prefSize.height = Math.max(icon.getIconHeight() + ins.top + ins.bottom, prefSize.height);
                prefSize.width += (icon.getIconWidth() + r.getIconTextGap());
            }
        }
        
        //Antialiasing affects the text metrics, so use it if needed when
        //calculating preferred size or the result here will be narrower
        //than the space actually needed
        ((Graphics2D) g).addRenderingHints(getHints());

        int textwidth = textWidth(text, g, r.getFont(), r.isHtml()) + 4;

        if (r.isCentered()) {
            prefSize.width = Math.max(prefSize.width, textwidth + ins.right + ins.left);
        } else {
            prefSize.width += (textwidth + r.getIndent());
        }

        if (FIXED_HEIGHT > 0) {
            prefSize.height = FIXED_HEIGHT;
        }

        return prefSize;
    }

    @SuppressWarnings("unchecked")
    static final Map<?,?> getHints() {
        //XXX We REALLY need to put this in a graphics utils lib
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap<Object,Object>();
                if (antialias) {
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
            }
        }
        Map<?,?> ret = hintsMap;
        assert ret != null; // does this method need to be synchronized?
        return ret;
    }

    public @Override void update(Graphics g, JComponent c) {
        Color bg = getBackgroundFor((HtmlRendererImpl) c);
        HtmlRendererImpl h = (HtmlRendererImpl) c;

        if (bg != null && !(isNimbus() && h.getType() == HtmlRendererImpl.Type.TREE)) {
            int x;
            if (h.getType() == HtmlRendererImpl.Type.TABLE) {
                x = 0; // in a table we want to have the whole row selected
            } else {
                x = h.isSelected() ? ((h.getIcon() == null) ? 0 : (h.getIcon().getIconWidth() + h.getIconTextGap())) : 0;
                x += h.getIndent();
            }
            g.setColor(bg);
            g.fillRect(x, 0, c.getWidth() - x, c.getHeight());
        }

        if (h.isLeadSelection()) {
            Color focus = UIManager.getColor("Tree.selectionBorderColor"); // NOI18N

            if ((focus == null) || focus.equals(bg)) {
                focus = Color.BLUE;
            }

            if (!isGTK() && !isAqua() && !isNimbus()) {
                int x;
                if (h.getType() == HtmlRendererImpl.Type.TABLE) {
                    x = 0; // in a table we want to have the whole row selected
                } else {
                    x = ((h.getIcon() == null) ? 0 : (h.getIcon().getIconWidth() + h.getIconTextGap()));
                }
                g.setColor(focus);
                g.drawRect(x, 0, c.getWidth() - (x + 1), c.getHeight() - 1);
            }
        }

        paint(g, c);
    }

    public @Override void paint(Graphics g, JComponent c) {

        ((Graphics2D) g).addRenderingHints(getHints());

        HtmlRendererImpl r = (HtmlRendererImpl) c;

        if (r.isCentered()) {
            paintIconAndTextCentered(g, r);
        } else {
            paintIconAndText(g, r);
        }
    }

    /** Actually paint the icon and text using our own html rendering engine. */
    private void paintIconAndText(Graphics g, HtmlRendererImpl r) {
        Font f = r.getFont();
        g.setFont(f);

        FontMetrics fm = g.getFontMetrics();

        //Find out what height we need
        int txtH = fm.getMaxAscent() + fm.getMaxDescent();
        Insets ins = r.getInsets();

        //find out the available height less the insets
        int rHeight = r.getHeight();
        int availH = rHeight - (ins.top + ins.bottom);

        int txtY;

        if (availH >= txtH) {
            //Center the text if we have space
            txtY = (txtH + ins.top + ((availH / 2) - (txtH / 2))) - fm.getMaxDescent();
        } else if (r.getHeight() > txtH) {
            txtY = txtH + (rHeight - txtH) / 2 - fm.getMaxDescent();
        } else {
            //Okay, it's not going to fit, punt.
            txtY = fm.getMaxAscent();
        }
        
        int txtX = r.getIndent();

        Icon icon = r.getIcon();

        //Check the icon non-null and height (see TabData.NO_ICON for why)
        if ((icon != null) && (icon.getIconWidth() > 0) && (icon.getIconHeight() > 0)) {
            int iconY;

            if (availH > icon.getIconHeight()) {
                //add 2 to make sure icon top pixels are not cut off by outline
                iconY = ins.top + ((availH / 2) - (icon.getIconHeight() / 2)); // + 2;
            } else if (availH == icon.getIconHeight()) {
                //They're an exact match, make it 0
                iconY = ins.top;
            } else {
                //Won't fit; make the top visible and cut the rest off (option:
                //center it and clip it on top and bottom - probably even harder
                //to recognize that way, though)
                iconY = ins.top;
            }

            //add in the insets
            int iconX = ins.left + r.getIndent() + 1; //+1 to get it out of the way of the focus border

            try {
                //Diagnostic - the CPP module currently is constructing
                //some ImageIcon from a null image in Options.  So, catch it and at
                //least give a meaningful message that indicates what node
                //is the culprit
                icon.paintIcon(r, g, iconX, iconY);
            } catch (NullPointerException npe) {
                Exceptions.attachMessage(npe,
                                         "Probably an ImageIcon with a null source image: " +
                                         icon + " - " + r.getText()); //NOI18N
                Exceptions.printStackTrace(npe);
            }

            txtX = iconX + icon.getIconWidth() + r.getIconTextGap();
        } else {
            //If there's no icon, paint the text where the icon would start
            txtX += ins.left;
        }

        String text = r.getText();

        if (text == null) {
            //No text, we're done
            return;
        }

        //Get the available horizontal pixels for text
        int txtW = (icon != null)
            ? (r.getWidth() - (ins.left + ins.right + icon.getIconWidth() + r.getIconTextGap() + r.getIndent()))
            : (r.getWidth() - (ins.left + ins.right + r.getIndent()));

        Color background = getBackgroundFor(r);
        Color foreground = ensureContrastingColor(getForegroundFor(r), background);

        if (r.isHtml()) {
            HtmlRenderer._renderHTML(text, 0, g, txtX, txtY, txtW, txtH, f, foreground, r.getRenderStyle(), true, background, r.isSelected());
        } else {
            HtmlRenderer.renderPlainString(text, g, txtX, txtY, txtW, txtH, f, foreground, r.getRenderStyle(), true);
        }
    }

    private void paintIconAndTextCentered(Graphics g, HtmlRendererImpl r) {
        Insets ins = r.getInsets();
        Icon ic = r.getIcon();
        int w = r.getWidth() - (ins.left + ins.right);
        int txtX = ins.left;
        int txtY = 0;

        if ((ic != null) && (ic.getIconWidth() > 0) && (ic.getIconHeight() > 0)) {
            int iconx = (w > ic.getIconWidth()) ? ((w / 2) - (ic.getIconWidth() / 2)) : txtX;
            int icony = 0;
            ic.paintIcon(r, g, iconx, icony);
            txtY += (ic.getIconHeight() + r.getIconTextGap());
        }

        int txtW = r.getPreferredSize().width;
        txtX = (txtW < r.getWidth()) ? ((r.getWidth() / 2) - (txtW / 2)) : 0;

        int txtH = r.getHeight() - txtY;

        Font f = r.getFont();
        g.setFont(f);

        FontMetrics fm = g.getFontMetrics(f);
        txtY += fm.getMaxAscent();

        Color background = getBackgroundFor(r);
        Color foreground = ensureContrastingColor(getForegroundFor(r), background);

        if (r.isHtml()) {
            HtmlRenderer._renderHTML(
                r.getText(), 0, g, txtX, txtY, txtW, txtH, f, foreground, r.getRenderStyle(), true, background, r.isSelected()
            );
        } else {
            HtmlRenderer.renderString(
                r.getText(), g, txtX, txtY, txtW, txtH, r.getFont(), foreground, r.getRenderStyle(), true
            );
        }
    }

    /*
    (int pos, String s, Graphics g, int x,
    int y, int w, int h, Font f, Color defaultColor, int style,
    boolean paint, Color background) {  */
    static Color ensureContrastingColor(Color fg, Color bg) {
        if (bg == null) {
            if (isNimbus()) {
                bg = UIManager.getColor( "Tree.background" ); //NOI18N
                if( null == bg )
                    bg = Color.WHITE;
            } else {
                bg = UIManager.getColor("text"); //NOI18N

                if (bg == null) {
                    bg = Color.WHITE;
                }
            }
        }
        if (fg == null) {
            if (isNimbus()) {
                fg = UIManager.getColor( "Tree.foreground" ); //NOI18N
                if( null == fg )
                    fg = Color.BLACK;
            } else {
                fg = UIManager.getColor("textText"); //NOI18N
                if (fg == null) {
                    fg = Color.BLACK;
                }
            }
        }

        if (Color.BLACK.equals(fg) && Color.WHITE.equals(fg)) {
            return fg;
        }

        boolean replace = fg.equals(bg);
        int dif = 0;

        if (!replace) {
            dif = difference(fg, bg);
            replace = dif < 80;
        }

        if (replace) {
            int lum = luminance(bg);
            boolean darker = lum >= 128;

            if (darker) {
                fg = Color.BLACK;
            } else {
                fg = Color.WHITE;
            }
        }

        return fg;
    }
    
    private static int difference(Color a, Color b) {
        return Math.abs(luminance(a) - luminance(b));
    }

    private static int luminance(Color c) {
        return (299*c.getRed() + 587*c.getGreen() + 114*c.getBlue()) / 1000;
    }

    static Color getBackgroundFor(HtmlRendererImpl r) {
        if (r.isOpaque()) {
            return r.getBackground();
        }
        
        if (r.isSelected() && !r.isParentFocused() && !isGTK() && !isNimbus()) {
            return getUnfocusedSelectionBackground();
        }

        Color result = null;

        if (r.isSelected()) {
            switch (r.getType()) {
            case LIST:
                result = UIManager.getColor("List.selectionBackground"); //NOI18N

                if (result == null) { //GTK

                    //plaf library guarantees this one:
                    result = UIManager.getColor("Tree.selectionBackground"); //NOI18N
                }

                //System.err.println("  now " + result);
                break;

            case TABLE:
                result = UIManager.getColor("Table.selectionBackground"); //NOI18N

                break;

            case TREE:
                return UIManager.getColor("Tree.selectionBackground"); //NOI18N
            }

            return (result == null) ? r.getBackground() : result;
        }

        return null;
    }

    static Color getForegroundFor(HtmlRendererImpl r) {
        if (r.isSelected() && !r.isParentFocused() && !isGTK() && !isNimbus()) {
            return getUnfocusedSelectionForeground();
        }

        if (!r.isEnabled()) {
            return UIManager.getColor("textInactiveText"); //NOI18N
        }

        Color result = null;

        if (r.isSelected()) {
            switch (r.getType()) {
            case LIST:
                result = UIManager.getColor("List.selectionForeground"); //NOI18N
                break;
            case TABLE:
                result = UIManager.getColor("Table.selectionForeground"); //NOI18N
                break;
            case TREE:
                result = UIManager.getColor("Tree.selectionForeground"); //NOI18N
            }
        }

        return (result == null) ? r.getForeground() : result;
    }

    static boolean isAqua () {
        return "Aqua".equals(UIManager.getLookAndFeel().getID());
    }
    
    static boolean isGTK () {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }
    
    static boolean isNimbus () {
        return "Nimbus".equals(UIManager.getLookAndFeel().getID());
    }

    /** Get the system-wide unfocused selection background color */
    private static Color getUnfocusedSelectionBackground() {
        if (unfocusedSelBg == null) {
            //allow theme/ui custom definition
            unfocusedSelBg = UIManager.getColor("nb.explorer.unfocusedSelBg"); //NOI18N
            
            if (unfocusedSelBg == null) {
                //try to get standard shadow color
                unfocusedSelBg = UIManager.getColor("controlShadow"); //NOI18N
                
                if (unfocusedSelBg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelBg = Color.lightGray;
                }

                //Lighten it a bit because disabled text will use controlShadow/
                //gray
                if (!Color.WHITE.equals(unfocusedSelBg.brighter())) {
                    unfocusedSelBg = unfocusedSelBg.brighter();
                }
            }
        }

        return unfocusedSelBg;
    }

    /** Get the system-wide unfocused selection foreground color */
    private static Color getUnfocusedSelectionForeground() {
        if (unfocusedSelFg == null) {
            //allow theme/ui custom definition
            unfocusedSelFg = UIManager.getColor("nb.explorer.unfocusedSelFg"); //NOI18N
            
            if (unfocusedSelFg == null) {
                //try to get standard shadow color
                unfocusedSelFg = UIManager.getColor("textText"); //NOI18N
                
                if (unfocusedSelFg == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    unfocusedSelFg = Color.BLACK;
                }
            }
        }

        return unfocusedSelFg;
    }

    public static final boolean gtkShouldAntialias() {
        if (gtkAA == null) {
            Object o = Toolkit.getDefaultToolkit().getDesktopProperty("gnome.Xft/Antialias"); //NOI18N
            gtkAA = new Integer(1).equals(o) ? Boolean.TRUE : Boolean.FALSE;
        }

        return gtkAA.booleanValue();
    }
}
