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

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Mac implementation of tab renderer
 *
 * @author S. Aubrecht
 */
final class AquaEditorTabCellRenderer extends AbstractTabCellRenderer {
    //Default insets values for Mac look and feel
    private static final int TOP_INSET = 0;
    private static final int LEFT_INSET = 3;
    private static final int RIGHT_INSET = 0;
    static final int BOTTOM_INSET = 0;

    //Painters which will be used for the various states, to pass to superclass
    //constructor
    private static final TabPainter leftClip = new AquaLeftClipPainter();
    private static final TabPainter rightClip = new AquaRightClipPainter();
    private static final TabPainter normal = new AquaPainter();

    /**
     * Creates a new instance of AquaEditorTabCellRenderer
     */
    public AquaEditorTabCellRenderer() {
        super(leftClip, normal, rightClip, new Dimension(32, 42));
    }

    private Font txtFont;

    @Override
    public Font getFont() {
        if (txtFont == null) {

            txtFont = (Font) UIManager.get("windowTitleFont");
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11);
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
        }
        return txtFont;
    }

    @Override
    protected void paintIconAndText(Graphics g) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText(g);
    }

    /**
     * Mac look and feel makes selected tab wider by 2 pixels on each side
     */
    @Override
    public int getPixelsToAddToSelection() {
        return 0;
    }

    @Override
    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 32 : 16;
        return d;
    }

    private static final Color getTxtColor() {
        Color result = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        if (result == null) {
            result = new Color(0, 0, 0);
        }
        return result;
    }

    @Override
    public Color getSelectedActivatedForeground() {
        return getTxtColor();
    }

    @Override
    public Color getSelectedForeground() {
        return getTxtColor();
    }

    private static void paintTabGradient( Graphics g, AquaEditorTabCellRenderer ren, Polygon poly ) {
        Rectangle rect = poly.getBounds();
        boolean selected = ren.isSelected();
        boolean focused = selected && ren.isActive();
        boolean attention = ren.isAttention();
        boolean mouseOver = ren.isArmed();
        if (focused && !attention) {
            ColorUtil.paintMacGradientFill((Graphics2D) g, rect,
                                         UIManager.getColor("NbTabControl.selectedTabBrighterBackground"),
                                         UIManager.getColor("NbTabControl.selectedTabDarkerBackground") );
        } else if (selected && !attention) {
            ColorUtil.paintMacGradientFill((Graphics2D) g, rect,
                                         UIManager.getColor("NbTabControl.selectedTabBrighterBackground"),
                                         UIManager.getColor("NbTabControl.selectedTabDarkerBackground") );
        } else if (mouseOver && !attention) {
            ColorUtil.paintMacGradientFill((Graphics2D) g, rect,
                                         UIManager.getColor("NbTabControl.mouseoverTabBrighterBackground"),
                                         UIManager.getColor("NbTabControl.mouseoverTabDarkerBackground") );
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, rect,
                                         a, b);
        } else {
            ColorUtil.paintMacGradientFill((Graphics2D) g, rect,
                                         UIManager.getColor("NbTabControl.inactiveTabBrighterBackground"),
                                         UIManager.getColor("NbTabControl.inactiveTabDarkerBackground") );
        }

    }

    @Override
    protected int getCaptionYAdjustment() {
        return 0;
    }

    @Override
    protected int getIconYAdjustment() {
        return -2;
    }

    private static class AquaPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {

            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) jc;

            if (!ren.isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            String iconPath = findIconPath(ren);
            Icon icon = TabControlButtonFactory.getIcon(iconPath);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            rect.x = bounds.x + bounds.width - iconWidth - 5;
            rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
            rect.width = iconWidth;
            rect.height = iconHeight;
        }


        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( AquaEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/openide/awt/resources/mac_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/openide/awt/resources/mac_close_rollover.png"; // NOI18N
            }
            return "org/openide/awt/resources/mac_close_enabled.png"; // NOI18N
        }

        public Polygon getInteriorPolygon(Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = ren.isRightmost() ? c.getWidth() - 1 : c.getWidth();
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Color borderColor = UIManager.getColor("NbTabControl.borderColor");
            Color shadowColor = UIManager.getColor("NbTabControl.borderShadowColor");
            //top
            g.setColor(borderColor);
            g.drawLine(x, y, width-1, y);

            //bottom
            if( !ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x, y+height-1, width, y+height-1);

            } else {
                g.setColor(UIManager.getColor("NbTabControl.selectedTabDarkerBackground"));
                g.drawLine(x, y+height-1, width, y+height-1);
            }

            //right
            if( ren.isRightmost() || !ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x+width-1, y, x+width-1, y+height-1);
                g.setColor(shadowColor);
                g.drawLine(x+width-2, y+1, x+width-2, y+height-(ren.isSelected() ? 1 : 2));
            } else if( ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x+width-1, y, x+width-1, y+height-1);
            }
            //left
            if( !ren.isLeftmost() && !ren.isSelected() ) {
                g.setColor(shadowColor);
                g.drawLine(x, y+1, x, y+height-2);
            }
        }


        public void paintInterior(Graphics g, Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;
            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );

            //Get the close button bounds, more or less
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if( ren.isActive() && ren.isSelected() ) {
                int x = 0;
                int y = 0;
                int width = ren.getWidth();
                g.setColor(UIManager.getColor("NbTabControl.focusedTabBackground"));
                g.drawLine(x, y+1, x+width-2, y+1);
                g.drawLine(x, y+2, x+width-2, y+2);
            }

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }

            //paint close button
            String iconPath = findIconPath( ren );
            Icon icon = TabControlButtonFactory.getIcon( iconPath );
            icon.paintIcon(ren, g, r.x, r.y);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return renderer instanceof TabDisplayer ?
                ((TabDisplayer) renderer).isShowCloseButton() : true;
        }

    }

    private static class AquaLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public Polygon getInteriorPolygon(Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = ren.isRightmost() ? c.getWidth() - 1 : c.getWidth();
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {

            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Color borderColor = UIManager.getColor("NbTabControl.borderColor");

            //top
            g.setColor(borderColor);
            g.drawLine(x, y, width-1, y);

            //bottom
            if( !ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x, y+height-1, width, y+height-1);

            } else {
                g.setColor(UIManager.getColor("NbTabControl.selectedTabDarkerBackground"));
                g.drawLine(x, y+height-1, width, y+height-1);
            }

            //right
            if( ren.isRightmost() || !ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x+width-1, y, x+width-1, y+height-1);
                g.setColor(UIManager.getColor("NbTabControl.editorBorderShadowColor"));
                g.drawLine(x+width-2, y+1, x+width-2, y+height-(ren.isSelected() ? 1 : 2));
            }
            if( ren.isActive() && ren.isSelected() ) {
                g.setColor(UIManager.getColor("NbTabControl.focusedTabBackground"));
                g.drawLine(x, y+1, x+width-1, y+1);
                g.drawLine(x, y+2, x+width-1, y+2);
            }
        }

        public void paintInterior(Graphics g, Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }

    }

    private static class AquaRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = c.getWidth() + 1;
            int height = c.getHeight() - ins.bottom;

            //just a plain rectangle
            p.addPoint(x, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Color borderColor = UIManager.getColor("NbTabControl.borderColor");

            //top
            g.setColor(borderColor);
            g.drawLine(x, y, width, y);

            //bottom
            if( !ren.isSelected() ) {
                g.setColor(borderColor);
                g.drawLine(x, y+height-1, width, y+height-1);

            } else {
                g.setColor(UIManager.getColor("NbTabControl.selectedTabDarkerBackground"));
                g.drawLine(x, y+height-1, width, y+height-1);
            }

            //left
            if( !ren.isLeftmost() && !ren.isSelected() ) {
                g.setColor(UIManager.getColor("NbTabControl.editorBorderShadowColor"));
                g.drawLine(x, y+1, x, y+height-2);
            }
        }

        public void paintInterior(Graphics g, Component c) {
            AquaEditorTabCellRenderer ren = (AquaEditorTabCellRenderer) c;

            Polygon poly = getInteriorPolygon(ren);
            paintTabGradient( g, ren, poly );

            if( ren.isActive() && ren.isSelected() ) {
                int x = 0;
                int y = 0;
                int width = ren.getWidth();
                g.setColor(UIManager.getColor("NbTabControl.focusedTabBackground"));
                g.drawLine(x, y+1, x+width, y+1);
                g.drawLine(x, y+2, x+width, y+2);
            }
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }
    }
}
