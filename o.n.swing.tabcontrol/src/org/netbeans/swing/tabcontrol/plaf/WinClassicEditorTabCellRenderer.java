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

/**
 * Windows classic implementation of tab renderer
 *
 * @author Tim Boudreau
 */
final class WinClassicEditorTabCellRenderer extends AbstractTabCellRenderer {

    private static final TabPainter leftClip = new WinClassicLeftClipPainter();
    private static final TabPainter rightClip = new WinClassicRightClipPainter();
    private static final TabPainter normal = new WinClassicPainter();

    private static final Color GTK_TABBED_PANE_BACKGROUND_1 = new Color(255, 255, 255);
    
    static final Color ATTENTION_COLOR = new Color(255, 238, 120);
    
    private static boolean isGenericUI = !"Windows".equals(
        UIManager.getLookAndFeel().getID());

    /**
     * Creates a new instance of WinClassicEditorTabCellRenderer
     */
    public WinClassicEditorTabCellRenderer() {
          super(leftClip, normal, rightClip, new Dimension (28, 32));
      }

    public Color getSelectedForeground() {
        return UIManager.getColor("textText"); //NOI18N
    }

    public Color getForeground() {
        return getSelectedForeground();
    }
    
    /**
     * #56245 - need more space between icon and edge on classic for the case
     * of full 16x16 icons.
     */
    public int getPixelsToAddToSelection() {
        return 4;
    }    

    protected int getCaptionYAdjustment() {
        return 0;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 28 : 20;
        return d;
    }
    
    @Override
    protected void paintIconAndText( Graphics g ) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText( g );
    }

    private static final Insets INSETS = new Insets(0, 2, 0, 10);

    private static class WinClassicPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = ren.isLeftmost() ? 1 : 0;
            int y = isGenericUI ? 0 : 1;

            int width = ren.isLeftmost() ? c.getWidth() - 1 : c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;
                    
            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 3, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top + 2);
            p.addPoint(x + width - 1, y + height - 2);
            p.addPoint(x, y + height - 2);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (i == p.npoints - 4) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("controlDkShadow") :
                               UIManager.getColor("controlShadow")); //NOI18N
                    g.drawLine(xpoints[i] + 1, ypoints[i] + 1,
                               xpoints[i] + 2, ypoints[i] + 2);
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {

            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, getSelGradientColor2()));
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("TabbedPane.background") :
                               UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }
            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);

            if (!supportsCloseButton((JComponent)c)) {
                return;
            }
            
            paintCloseButton( g, (JComponent)c );
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect, Rectangle bounds) {
            boolean rightClip = ((WinClassicEditorTabCellRenderer) jc).isClipRight();
            boolean leftClip = ((WinClassicEditorTabCellRenderer) jc).isClipLeft();
            boolean notSupported = !((WinClassicEditorTabCellRenderer) jc).isShowCloseButton();
            if (leftClip || rightClip || notSupported) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
            } else {
                String iconPath = findIconPath((WinClassicEditorTabCellRenderer) jc);
                Icon icon = TabControlButtonFactory.getIcon(iconPath);
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                rect.x = bounds.x + bounds.width - iconWidth - 2;
                rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
                rect.width = iconWidth;
                rect.height = iconHeight;
            }
        }
        
        private void paintCloseButton(Graphics g, JComponent c) {
            if (((AbstractTabCellRenderer) c).isShowCloseButton()) {
                
                Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                Rectangle cbRect = new Rectangle();
                getCloseButtonRectangle((JComponent) c, cbRect, r);
                
                //paint close button
                String iconPath = findIconPath( (WinClassicEditorTabCellRenderer)c );
                Icon icon = TabControlButtonFactory.getIcon( iconPath );
                icon.paintIcon(c, g, cbRect.x, cbRect.y);
            }
        }
        
        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( WinClassicEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/openide/awt/resources/win_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/openide/awt/resources/win_close_rollover.png"; // NOI18N
            }
            return "org/openide/awt/resources/win_close_enabled.png"; // NOI18N
        }
        
        public boolean supportsCloseButton(JComponent renderer) {
            return 
                ((AbstractTabCellRenderer) renderer).isShowCloseButton();
        }

    }


    private static class WinClassicLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = isGenericUI ? 0 : 1;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 3, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top + 2);
            p.addPoint(x + width - 1, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (i == p.npoints - 4) {
                    g.setColor(ren.isSelected() ?
                               UIManager.getColor("controlDkShadow") :
                               UIManager.getColor("controlShadow")); //NOI18N
                    g.drawLine(xpoints[i] + 1, ypoints[i] + 1,
                               xpoints[i] + 2, ypoints[i] + 2);
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, getSelGradientColor2()));
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                           UIManager.getColor("TabbedPane.background") :
                           UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }
            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }
    }

    private static class WinClassicRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = isGenericUI ? 0 : 1;

            int width = c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            p.addPoint(x, y + ins.top + 2);
            p.addPoint(x + 2, y + ins.top);
            p.addPoint(x + width - 1, y + ins.top);
            p.addPoint(x + width - 1, y + height - 1);
            p.addPoint(x, y + height - 1);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            g.setColor(ren.isSelected() ?
                       UIManager.getColor("controlLtHighlight") :
                       UIManager.getColor("controlHighlight")); //NOI18N

            int[] xpoints = p.xpoints;
            int[] ypoints = p.ypoints;

            g.drawLine(xpoints[0], ypoints[0], xpoints[p.npoints - 1],
                       ypoints[p.npoints - 1]);

            for (int i = 0; i < p.npoints - 1; i++) {
                g.drawLine(xpoints[i], ypoints[i], xpoints[i + 1],
                           ypoints[i + 1]);
                if (ren.isSelected() && i == p.npoints - 4) {
                    g.setColor(ren.isActive() ?
                               UIManager.getColor("Table.selectionBackground") :
                               UIManager.getColor("control")); //NOI18n
                } else if (i == p.npoints - 4) {
                    break;
                }
                if (i == p.npoints - 3) {
                    break;
                }
            }
        }

        public void paintInterior(Graphics g, Component c) {
            WinClassicEditorTabCellRenderer ren = (WinClassicEditorTabCellRenderer) c;
            boolean wantGradient = ren.isSelected() && ren.isActive() || ((ren.isClipLeft()
                    || ren.isClipRight())
                    && ren.isPressed());

            if (wantGradient) {
                ((Graphics2D) g).setPaint(ColorUtil.getGradientPaint(0, 0, getSelGradientColor(), ren.getWidth(), 0, getSelGradientColor2()));
            } else {
                if (!ren.isAttention()) {
                    g.setColor(ren.isSelected() ?
                           UIManager.getColor("TabbedPane.background") : //NOI18N
                           UIManager.getColor("tab_unsel_fill")); //NOI18N
                } else {
                    g.setColor(ATTENTION_COLOR);
                }
            }

            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);
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
    
    private static final Color getSelGradientColor() {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            return GTK_TABBED_PANE_BACKGROUND_1; // #68200
        } else {
            return UIManager.getColor("winclassic_tab_sel_gradient"); // NOI18N
        }
    }
    
    private static final Color getSelGradientColor2() {
        return UIManager.getColor("TabbedPane.background"); // NOI18N
    }
    
}
