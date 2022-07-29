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
/*
 * MetalEditorTabCellRenderer.java
 *
 * Created on December 2, 2003, 9:30 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer for editor tabs in metal l&amp;f
 *
 * @author Tim Boudreau
 */
class MetalEditorTabCellRenderer extends AbstractTabCellRenderer {
    private static final MetalTabPainter metalborder = new MetalTabPainter();
    private static final MetalRightClippedTabPainter rightBorder = new MetalRightClippedTabPainter();
    private static final MetalLeftClippedTabPainter leftBorder = new MetalLeftClippedTabPainter();

    static final Color ATTENTION_COLOR = new Color(255, 238, 120);
    /**
     * Creates a new instance of MetalEditorTabCellRenderer
     */
    public MetalEditorTabCellRenderer() {
        super(leftBorder, metalborder, rightBorder, new Dimension(34, 29));
        setBorder(metalborder);
    }

    protected int getCaptionYAdjustment() {
        return 0;
    }

    public Dimension getPadding() {
        Dimension d = super.getPadding();
        d.width = isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton") ? 34 : 24;
        return d;
    }

    @Override
    protected void paintIconAndText( Graphics g ) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText( g );
    }

    private static class MetalTabPainter implements TabPainter {
        public Insets getBorderInsets(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            return new Insets(mtr.isSelected() ? 3 : 5,
                              mtr.isSelected() ? 10 : 9, 1, 0); //XXX
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return ((AbstractTabCellRenderer) renderer).isShowCloseButton();
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            
            //Draw the highlight first, one pixel over, to get the left side and
            //diagonal highlight (the right edge will get cut off, as we want)
            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawPolygon(p);
            p.translate(-1, 0);
            Insets ins = getBorderInsets(c);
            g.drawLine(x + 6, y + ins.top + 1, x + width - 1, y + ins.top + 1);

            if (mtr.isSelected()) {
                //draw the dark portion of the drag texture dots
                g.drawLine(4, ins.top + 6, 4, ins.top + 6);
                g.drawLine(2, ins.top + 8, 2, ins.top + 8);

                g.drawLine(4, ins.top + 10, 4, ins.top + 10);
                g.drawLine(2, ins.top + 12, 2, ins.top + 12);

                g.drawLine(4, ins.top + 14, 4, ins.top + 14);
                g.drawLine(2, ins.top + 16, 2, ins.top + 16);
            }
            
            //Draw the dark polygon
            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawPolygon(p);

            if (mtr.isSelected()) {
                //draw the dark portion of the drag texture dots
                g.drawLine(5, ins.top + 7, 5, ins.top + 7);
                g.drawLine(3, ins.top + 9, 3, ins.top + 9);

                g.drawLine(5, ins.top + 11, 5, ins.top + 11);
                g.drawLine(3, ins.top + 13, 3, ins.top + 13);

                g.drawLine(5, ins.top + 15, 5, ins.top + 15);
                g.drawLine(3, ins.top + 17, 3, ins.top + 17);
            }

            if (!mtr.isSelected()) {
                g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
                g.drawLine(x, mtr.getHeight() - 1, mtr.getWidth() - 1,
                           mtr.getHeight() - 1);
            }
        }

        public Polygon getInteriorPolygon(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = mtr.isLeftmost() ? 1 : 0;
            int y = 0;

            int width = mtr.isLeftmost() ? c.getWidth() - 1 : c.getWidth();
            int height = mtr.isSelected() ?
                    c.getHeight() + 3 : c.getHeight();

            p.addPoint(x, y + ins.top + 6);
            p.addPoint(x + 6, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintInterior(Graphics g, Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;

            if (mtr.isAttention()) {
                g.setColor(ATTENTION_COLOR);
            }
            
            Polygon p = getInteriorPolygon(c);
            g.fillPolygon(p);
            
            //Get the close button bounds, more or less
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(mtr, r, new Rectangle(0, 0,
                                                          mtr.getWidth(),
                                                          mtr.getHeight()));

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }
            paintCloseButton( g, (JComponent)c );
        }

        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            if (!((AbstractTabCellRenderer) jc).isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            String iconPath = findIconPath((MetalEditorTabCellRenderer) jc);
            Icon icon = TabControlButtonFactory.getIcon(iconPath);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            rect.x = bounds.x + bounds.width - iconWidth - 2;
            rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2))+2;
            rect.width = iconWidth;
            rect.height = iconHeight;
        }
                
        private void paintCloseButton(Graphics g, JComponent c) {
            if (((AbstractTabCellRenderer) c).isShowCloseButton()) {
                
                Rectangle r = new Rectangle(0, 0, c.getWidth(), c.getHeight());
                Rectangle cbRect = new Rectangle();
                getCloseButtonRectangle((JComponent) c, cbRect, r);
                
                //paint close button
                String iconPath = findIconPath( (MetalEditorTabCellRenderer)c );
                Icon icon = TabControlButtonFactory.getIcon( iconPath );
                icon.paintIcon(c, g, cbRect.x, cbRect.y);
            }
        }
        
        /**
         * Returns path of icon which is correct for currect state of tab at given
         * index
         */
        private String findIconPath( MetalEditorTabCellRenderer renderer ) {
            if( renderer.inCloseButton() && renderer.isPressed() ) {
                return "org/openide/awt/resources/metal_close_pressed.png"; // NOI18N
            }
            if( renderer.inCloseButton() ) {
                return "org/openide/awt/resources/metal_close_rollover.png"; // NOI18N
            }
            return "org/openide/awt/resources/metal_close_enabled.png"; // NOI18N
        }
    }

    private static class MetalLeftClippedTabPainter implements TabPainter {

        public Insets getBorderInsets(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            return new Insets(mtr.isSelected() ? 3 : 5,
                              mtr.isSelected() ? 10 : 9, 1, 0); //XXX
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            //Ensure the left edge is out of bounds
            int x = -1;
            int y = ins.top;

            int width = c.getWidth();
            int height = mtr.isSelected() ?
                    c.getHeight() + 3 : c.getHeight();

            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);

            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            Polygon p = getInteriorPolygon(c);

            p.translate(0, 1);
            g.drawPolygon(p);
            p.translate(0, -1);
            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawPolygon(p);
            if (!mtr.isSelected()) {
                g.drawLine(x, y + height - 1, x + width, y + height - 1);
            }
        }

        public void paintInterior(Graphics g, Component c) {
            Polygon p = getInteriorPolygon(c);
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            if (mtr.isAttention()) {
                g.setColor(ATTENTION_COLOR);
            }
            
            g.fillPolygon(p);
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                                            Rectangle bounds) {
            bounds.setBounds(-20, -20, 0, 0);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }
    }

    private static class MetalRightClippedTabPainter implements TabPainter {
        public Insets getBorderInsets(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            return new Insets(mtr.isSelected() ? 3 : 5,
                              mtr.isSelected() ? 10 : 9, 1, 0); //XXX
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Polygon getInteriorPolygon(Component c) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;

            Insets ins = getBorderInsets(c);
            Polygon p = new Polygon();
            int x = mtr.isLeftmost() ? 1 : 0;
            int y = 0;

            int width = c.getWidth() + 2;
            int height = mtr.isSelected() ?
                    c.getHeight() + 3 : c.getHeight();

            p.addPoint(x, y + ins.top + 6);
            p.addPoint(x + 6, y + ins.top);
            p.addPoint(x + width, y + ins.top);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            Polygon p = getInteriorPolygon(c);
            
            //Draw the highlight first, one pixel over, to get the left side and
            //diagonal highlight (the right edge will get cut off, as we want)
            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawPolygon(p);
            p.translate(-1, 0);
            Insets ins = getBorderInsets(c);
            g.drawLine(x + 6, y + ins.top + 1, x + width - 1, y + ins.top + 1);

            if (mtr.isSelected()) {
                //draw the light portion of the drag texture dots
                g.drawLine(4, ins.top + 6, 4, ins.top + 6);
                g.drawLine(2, ins.top + 8, 2, ins.top + 8);

                g.drawLine(4, ins.top + 10, 4, ins.top + 10);
                g.drawLine(2, ins.top + 12, 2, ins.top + 12);

                g.drawLine(4, ins.top + 14, 4, ins.top + 14);
                g.drawLine(2, ins.top + 16, 2, ins.top + 16);
            }
            
            //Draw the dark polygon
            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawPolygon(p);

            if (mtr.isSelected()) {
                //draw the dark portion of the drag texture dots
                g.drawLine(5, ins.top + 7, 5, ins.top + 7);
                g.drawLine(3, ins.top + 9, 3, ins.top + 9);

                g.drawLine(5, ins.top + 11, 5, ins.top + 11);
                g.drawLine(3, ins.top + 13, 3, ins.top + 13);

                g.drawLine(5, ins.top + 15, 5, ins.top + 15);
                g.drawLine(3, ins.top + 17, 3, ins.top + 17);
            }

            if (!mtr.isSelected()) {
                g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
                g.drawLine(x, c.getHeight() - 1, c.getWidth() - 1,
                           c.getHeight() - 1);
            }
        }

        public void paintInterior(Graphics g, Component c) {
            Polygon p = getInteriorPolygon(c);
            MetalEditorTabCellRenderer mtr = (MetalEditorTabCellRenderer) c;
            if (mtr.isAttention()) {
                g.setColor(ATTENTION_COLOR);
            }
            g.fillPolygon(p);
        }

        public void getCloseButtonRectangle(JComponent jc, Rectangle rect,
                                            Rectangle bounds) {
            bounds.setBounds(-20, -20, 0, 0);
        }

        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }
    }
}
