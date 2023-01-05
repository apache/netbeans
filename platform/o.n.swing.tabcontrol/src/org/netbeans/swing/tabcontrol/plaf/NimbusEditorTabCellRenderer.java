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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Nimbus implementation of tab renderer
 *
 * @author Marek Slama
 */
final class NimbusEditorTabCellRenderer extends AbstractTabCellRenderer {

    private static final TabPainter leftClip = new NimbusLeftClipPainter();
    private static final TabPainter rightClip = new NimbusRightClipPainter();
    private static final TabPainter normal = new NimbusPainter();
    
    static final Color ATTENTION_COLOR = new Color(255, 238, 120);

    /**
     * Creates a new instance of GtkEditorTabCellRenderer
     */
    public NimbusEditorTabCellRenderer() {
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
        return -2;
    }

    protected int getIconYAdjustment() {
        return -3;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 28 : 14;
        return d;
    }

    @Override
    protected void paintIconAndText( Graphics g ) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText( g );
    }
    
    private static final Insets INSETS = new Insets(0, 4, 0, 2);
    
    private static void paintTabBackground (Graphics g, int index, Component c,
    int x, int y, int w, int h) {

        Shape clip = g.getClip();
        NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;

        w +=1;
        boolean isPreviousTabSelected = ren.isPreviousTabSelected();
        if (isPreviousTabSelected) {
            g.setClip(x+1, y, w-1, h);
        }

        Object o = null;
        if (ren.isSelected()) {
            if (ren.isActive()) {
                o = UIManager.get("TabbedPane:TabbedPaneTab[MouseOver+Selected].backgroundPainter");
            } else {
                o = UIManager.get("TabbedPane:TabbedPaneTab[Selected].backgroundPainter");
            }
        } else {
            o = UIManager.get("TabbedPane:TabbedPaneTab[Enabled].backgroundPainter");
        }
        if (o instanceof javax.swing.Painter) {
            javax.swing.Painter painter = (javax.swing.Painter) o;
            BufferedImage bufIm = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufIm.createGraphics();
            g2d.setBackground(UIManager.getColor("Panel.background"));
            g2d.clearRect(0, 0, w, h);
            painter.paint(g2d, null, w, h);
            g.drawImage(bufIm, x, y, null);
        }

        if (isPreviousTabSelected) {
            g.setClip(clip);
        }
    }
    
    private static int getHeightDifference (NimbusEditorTabCellRenderer ren) {
        return 0;
        //return ren.isSelected() ? ren.isActive() ? 0 : 1 : 2;
    }
    
    private static class NimbusPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = ren.isLeftmost() ? 3 : 0;
            int y = 0;

            int width = ren.isLeftmost() ? c.getWidth() - 3 : c.getWidth();
            int height = c.getHeight() - 4;
                    
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
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            return;
        }
        

        public void paintInterior(Graphics g, Component c) {
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);

            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, c, bounds.x, bounds.y + yDiff, 
                    bounds.width, bounds.height - yDiff);
            
            if (!supportsCloseButton((JComponent)c)) {
                return;
            }
            
            paintCloseButton( g, (JComponent)c );
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect, Rectangle bounds) {
            boolean rightClip = ((NimbusEditorTabCellRenderer) jc).isClipRight();
            boolean leftClip = ((NimbusEditorTabCellRenderer) jc).isClipLeft();
            boolean notSupported = !((NimbusEditorTabCellRenderer) jc).isShowCloseButton();
            if (leftClip || rightClip || notSupported) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
            } else {
                String iconPath = findIconPath((NimbusEditorTabCellRenderer) jc);
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
                String iconPath = findIconPath( (NimbusEditorTabCellRenderer)c  );
                Icon icon = TabControlButtonFactory.getIcon( iconPath );
                icon.paintIcon(c, g, cbRect.x, cbRect.y);
            }
        }
        
        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( NimbusEditorTabCellRenderer renderer  ) {
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


    private static class NimbusLeftClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public Polygon getInteriorPolygon(Component c) {
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = -3;
            int y = 0;

            int width = c.getWidth() + 3;
            int height = c.getHeight() - 4;

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
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, c, bounds.x, bounds.y + yDiff, 
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

    private static class NimbusRightClipPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = 0;
            int y = 0;

            int width = c.getWidth() + 10;
            int height = c.getHeight() - 4;

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
            NimbusEditorTabCellRenderer ren = (NimbusEditorTabCellRenderer) c;
            
            Polygon p = getInteriorPolygon(c);
            Rectangle bounds = p.getBounds();
            int yDiff = getHeightDifference(ren);
            paintTabBackground(g, 0, c, bounds.x, bounds.y + yDiff, 
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
