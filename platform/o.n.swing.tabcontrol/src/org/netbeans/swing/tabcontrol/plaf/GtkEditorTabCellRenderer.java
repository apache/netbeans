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

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

/**
 * Gtk implementation of tab renderer
 *
 * @author Marek Slama
 */
final class GtkEditorTabCellRenderer extends AbstractTabCellRenderer {

    private static final TabPainter leftClip = new GtkLeftClipPainter();
    private static final TabPainter rightClip = new GtkRightClipPainter();
    private static final TabPainter normal = new GtkPainter();
    
    private static JTabbedPane dummyTab;

    static final Color ATTENTION_COLOR = new Color(255, 238, 120);

    private static final Logger LOG =
        Logger.getLogger("org.netbeans.swing.tabcontrol.plaf.GtkEditorTabCellRenderer"); // NOI18N

    /**
     * Creates a new instance of GtkEditorTabCellRenderer
     */
    public GtkEditorTabCellRenderer() {
          super(leftClip, normal, rightClip, new Dimension (28, 32));
    }
    
    public Color getSelectedForeground() {
        return UIManager.getColor("textText"); //NOI18N
    }

    public Color getForeground() {
        return getSelectedForeground();
    }

    @Override
    protected void paintIconAndText( Graphics g ) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText( g );
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
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 28 : 14;
        return d;
    }
    
    private static final Insets INSETS = new Insets(0, 2, 0, 10);
    
    private static void paintTabBackground (Graphics g, int index, int state,
    int x, int y, int w, int h) {
        if (dummyTab == null) {
            dummyTab = new JTabbedPane();
        }
        Region region = Region.TABBED_PANE_TAB;
        if( !(UIManager.getLookAndFeel() instanceof SynthLookAndFeel) ) {
            return; //#215311 - unsupported L&F installed
        }
        SynthLookAndFeel laf = (SynthLookAndFeel) UIManager.getLookAndFeel();
        SynthStyleFactory sf = laf.getStyleFactory();
        SynthStyle style = sf.getStyle(dummyTab, region);
        SynthContext context =
            new SynthContext(dummyTab, region, style, 
                state == SynthConstants.FOCUSED ? SynthConstants.SELECTED : state);
        SynthPainter painter = style.getPainter(context);
        long t1, t2;
        if (state == SynthConstants.DEFAULT) {
            t1 = System.currentTimeMillis();
            painter.paintTabbedPaneTabBackground(context, g, x, y, w, h, index);
            t2 = System.currentTimeMillis();
            if ((t2 - t1) > 200) {
                LOG.log(Level.WARNING, "painter.paintTabbedPaneTabBackground1 takes too long"
                + " x=" + x + " y=" + y + " w=" + w + " h=" + h + " index:" + index
                + " Time=" + (t2 - t1));
            }
        } else {
            BufferedImage bufIm = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufIm.createGraphics();
            g2d.setBackground(UIManager.getColor("Panel.background"));
            g2d.clearRect(0, 0, w, h);
            t1 = System.currentTimeMillis();
            painter.paintTabbedPaneTabBackground(context, g2d, 0, 0, w, h, index);
            t2 = System.currentTimeMillis();
            if ((t2 - t1) > 200) {
                LOG.log(Level.WARNING, "painter.paintTabbedPaneTabBackground1 takes too long"
                + " x=0" + " y=0" + " w=" + w + " h=" + h + " index:" + index
                + " Time=" + (t2 - t1));
            }
            // differentiate active and selected tabs, active tab made brighter,
            // selected tab darker
            RescaleOp op = state == SynthConstants.FOCUSED 
                ? new RescaleOp(1.08f, 0, null)
                : new RescaleOp(0.96f, 0, null); 
            BufferedImage img = op.filter(bufIm, null);
            g.drawImage(img, x, y, null);
        }

    }
    
    private static int getHeightDifference (GtkEditorTabCellRenderer ren) {
        return ren.isSelected() ? ren.isActive() ? 0 : 1 : 2;
    }
    
    private static class GtkPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = ren.isLeftmost() ? 1 : 0;
            int y = 1;

            int width = ren.isLeftmost() ? c.getWidth() - 1 : c.getWidth();
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;
                    
            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            return;
        }
        

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            int state = ren.isSelected() ? ren.isActive() ? SynthConstants.FOCUSED 
                    : SynthConstants.SELECTED : SynthConstants.DEFAULT;
            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, state, bounds.x, bounds.y + yDiff, 
                    bounds.width, bounds.height - yDiff);
            
            if (!supportsCloseButton((JComponent)c)) {
                return;
            }
            
            paintCloseButton( g, (JComponent)c );
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect, Rectangle bounds) {
            boolean rightClip = ((GtkEditorTabCellRenderer) jc).isClipRight();
            boolean leftClip = ((GtkEditorTabCellRenderer) jc).isClipLeft();
            boolean notSupported = !((GtkEditorTabCellRenderer) jc).isShowCloseButton();
            if (leftClip || rightClip || notSupported) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
            } else {
                String iconPath = findIconPath((GtkEditorTabCellRenderer) jc);
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
                String iconPath = findIconPath( (GtkEditorTabCellRenderer)c );
                Icon icon = TabControlButtonFactory.getIcon( iconPath );
                icon.paintIcon(c, g, cbRect.x, cbRect.y);
            }
        }
        
        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( GtkEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/openide/awt/resources/gtk_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/openide/awt/resources/gtk_close_rollover.png"; // NOI18N
            }
            return "org/openide/awt/resources/gtk_close_enabled.png"; // NOI18N
        }
        
        public boolean supportsCloseButton(JComponent renderer) {
            return ((AbstractTabCellRenderer) renderer).isShowCloseButton();
        }

    }


    private static class GtkLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = 1;

            int width = c.getWidth() + 3;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            return;
        }

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            int state = ren.isSelected() ? ren.isActive() ? SynthConstants.FOCUSED 
                    : SynthConstants.SELECTED : SynthConstants.DEFAULT;
            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, state, bounds.x, bounds.y + yDiff, 
                    bounds.width, bounds.height - yDiff);
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

    private static class GtkRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 1;

            int width = c.getWidth() + 10;
            int height = ren.isSelected() ?
                    c.getHeight() + 2 : c.getHeight() - 1;

            //Modified to return rectangle
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
        }

        public void paintInterior(Graphics g, Component c) {
            GtkEditorTabCellRenderer ren = (GtkEditorTabCellRenderer) c;
            
            Polygon p = getInteriorPolygon(c);
            int state = ren.isSelected() ? ren.isActive() ? SynthConstants.FOCUSED 
                    : SynthConstants.SELECTED : SynthConstants.DEFAULT;
            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, state, bounds.x, bounds.y + yDiff, 
                    bounds.width, bounds.height - yDiff);
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
