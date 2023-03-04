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
/*
 * WinVistaEditorTabCellRenderer.java
 *
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import javax.swing.*;
import org.netbeans.swing.tabcontrol.TabDisplayer;

/**
 * Windows Vista implementation of tab renderer
 *
 * @author S. Aubrecht
 */
class WinVistaEditorTabCellRenderer extends AbstractTabCellRenderer {
    //Default insets values for Vista look and feel
    private static final int TOP_INSET = 0;
    private static final int LEFT_INSET = 3;
    private static final int RIGHT_INSET = 0;
    static final int BOTTOM_INSET = 0;

    //Painters which will be used for the various states, to pass to superclass
    //constructor
    private static final TabPainter leftClip = new WinVistaLeftClipPainter();
    private static final TabPainter rightClip = new WinVistaRightClipPainter();
    private static final TabPainter normal = new WinVistaPainter();
    
    /**
     * Creates a new instance of WinVistaEditorTabCellRenderer
     */
    public WinVistaEditorTabCellRenderer() {
        super(leftClip, normal, rightClip, new Dimension(32, 42));
    }

    /**
     * Vista look and feel makes selected tab wider by 2 pixels on each side
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

    private static Color getUnselFillBrightUpperColor() {
        Color result = UIManager.getColor("tab_unsel_fill_bright_upper"); //NOI18N
        if (result == null) {
            result = new Color(235,235,235);
        }
        return result;
    }
    
    private static Color getUnselFillDarkUpperColor() {
        Color result = UIManager.getColor("tab_unsel_fill_dark_upper"); //NOI18N
        if (result == null) {
            result = new Color(229, 229, 229);
        }
        return result;
    }
    
    private static Color getUnselFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_unsel_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(214,214,214);
        }
        return result;
    }
    
    private static Color getUnselFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_unsel_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(203, 203, 203);
        }
        return result;
    }
    
    private static Color getSelFillColor() {
        Color result = UIManager.getColor("tab_sel_fill"); //NOI18N
        if (result == null) {
            result = new Color(244,244,244);
        }
        return result;
    }
    
    private static Color getFocusFillUpperColor() {
        Color result = UIManager.getColor("tab_focus_fill_upper"); //NOI18N
        if (result == null) {
            result = new Color(242, 249, 252);
        }
        return result;
    }
    
    private static Color getFocusFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_focus_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(225, 241, 249);
        }
        return result;
    }
    
    private static Color getFocusFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_focus_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(216, 236, 246);
        }
        return result;
    }
    
    private static Color getMouseOverFillBrightUpperColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_bright_upper"); //NOI18N
        if (result == null) {
            result = new Color(223,242,252);
        }
        return result;
    }
    
    private static Color getMouseOverFillDarkUpperColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_dark_upper"); //NOI18N
        if (result == null) {
            result = new Color(214,239,252);
        }
        return result;
    }
    
    private static Color getMouseOverFillBrightLowerColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_bright_lower"); //NOI18N
        if (result == null) {
            result = new Color(189,228,250);
        }
        return result;
    }
    
    private static Color getMouseOverFillDarkLowerColor() {
        Color result = UIManager.getColor("tab_mouse_over_fill_dark_lower"); //NOI18N
        if (result == null) {
            result = new Color(171,221,248);
        }
        return result;
    }
    
    private static Color getTxtColor() {
        Color result = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        if (result == null) {
            result = new Color(0, 0, 0);
        }
        return result;
    }
    
    static Color getBorderColor() {
        Color result = UIManager.getColor("tab_border"); //NOI18N
        if (result == null) {
            result = new Color(137,140,149);
        }
        return result;
    }
    
    private static Color getSelBorderColor() {
        Color result = UIManager.getColor("tab_sel_border"); //NOI18N
        if (result == null) {
            result = new Color(60,127,177);
        }
        return result;
    }
    
    private static Color getBorderInnerColor() {
        Color result = UIManager.getColor("tab_border_inner"); //NOI18N
        if (result == null) {
            result = new Color(255,255,255);
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

    void paintTabGradient( Graphics g, Polygon poly ) {
        Rectangle rect = poly.getBounds(); 
        boolean selected = isSelected();
        boolean focused = selected && isActive();
        boolean attention = isAttention();
        boolean mouseOver = isArmed();
        if (focused && !attention) {
            rect.height++;
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getFocusFillUpperColor(),  
                                         getFocusFillBrightLowerColor(), getFocusFillDarkLowerColor() );
        } else if (selected && !attention) {
            rect.height++;
            g.setColor(getSelFillColor());
            g.fillPolygon( poly );
        } else if (mouseOver && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getMouseOverFillBrightUpperColor(), getMouseOverFillDarkUpperColor(), 
                                         getMouseOverFillBrightLowerColor(), getMouseOverFillDarkLowerColor() );
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, rect,
                                         a, b);         
        } else {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, rect,
                                         getUnselFillBrightUpperColor(), getUnselFillDarkUpperColor(), 
                                         getUnselFillBrightLowerColor(), getUnselFillDarkLowerColor() );
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

    @Override
    protected void paintIconAndText( Graphics g ) {
        if( isBusy() ) {
            setIcon( BusyTabsSupport.getDefault().getBusyIcon( isSelected() ) );
        }
        super.paintIconAndText( g );
    }

    /**
     * Returns the icon which is correct for currect state of tab at given
     * index
     */
    Icon findIcon() {
        final String file;
        if( inCloseButton() && isPressed() ) {
            file = "org/openide/awt/resources/vista_close_pressed.png"; // NOI18N
        } else if ( inCloseButton() ) {
            file = "org/openide/awt/resources/vista_close_rollover.png"; // NOI18N
        } else {
            file = "org/openide/awt/resources/vista_close_enabled.png"; // NOI18N
        }
        return TabControlButtonFactory.getIcon(file);
    }

    private static class WinVistaPainter implements TabPainter {

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        @Override
        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
              
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) jc;
            
            if (!ren.isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            Icon icon = ren.findIcon();
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            rect.x = bounds.x + bounds.width - iconWidth - 2;
            rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
            rect.width = iconWidth;
            rect.height = iconHeight;
        }


        @Override
        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;

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

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            if (ren.isLeftmost() )
                g.drawLine(0, 0, 0, height - 1);
            //top
            g.drawLine(0, 0, width - 1, 0);
            //right
            if( (ren.isActive() && ren.isNextTabSelected()) || ren.isNextTabArmed() )
                g.setColor( getSelBorderColor() );
            g.drawLine(width - 1, 0, width - 1, height - 2);
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(1, 1, width-2, 1);
            if( ren.isSelected() )
                height++;
            //left
            if (ren.isLeftmost())
                g.drawLine(1, 1, 1, height - 2);
            else
                g.drawLine(0, 1, 0, height - 2);
            //right
            g.drawLine(width-2, 1, width-2, height - 2);

            g.translate(-x, -y);
        }


        @Override
        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            Polygon poly = getInteriorPolygon(ren);
            ren.paintTabGradient( g, poly );
            
            //Get the close button bounds, more or less
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }
            
            //paint close button
            Icon icon = ren.findIcon();
            icon.paintIcon(ren, g, r.x, r.y);
        }

        @Override
        public boolean supportsCloseButton(JComponent renderer) {
            return renderer instanceof TabDisplayer ? 
                ((TabDisplayer) renderer).isShowCloseButton() : true;
        }

    }

    private static class WinVistaLeftClipPainter implements TabPainter {

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        @Override
        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
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

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            //no line
            //top
            g.drawLine(0, 0, width - 1, 0);
            //right
            if( (ren.isActive() && ren.isNextTabSelected()) || ren.isNextTabArmed() )
                g.setColor( getSelBorderColor() );
            g.drawLine(width - 1, 0, width - 1, height - 2);
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(0, 1, width-2, 1);
            if( ren.isSelected() )
                height++;
            //left
            //no line
            //right
            g.drawLine(width-2, 1, width-2, height - 2);

            g.translate(-x, -y);
        }

        @Override
        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            Polygon poly = getInteriorPolygon(ren);
            ren.paintTabGradient( g, poly );
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        @Override
        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }

    }

    private static class WinVistaRightClipPainter implements TabPainter {

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(TOP_INSET, LEFT_INSET, BOTTOM_INSET, RIGHT_INSET);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public Polygon getInteriorPolygon(Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;

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

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            g.translate(x, y);

            Color borderColor = ((ren.isActive() && ren.isSelected())
                    || ren.isArmed()) ? getSelBorderColor() : getBorderColor();
            g.setColor(borderColor);
            int left = 0;
            //left
            //no line
            //top
            g.drawLine(0, 0, width, 0);
            //right
            //no line
            //bottom
            g.setColor(getBorderColor());
            if( !ren.isSelected() ) {
                g.drawLine(0, height - 1, width - 1, height - 1);
            } else {
                g.drawLine(width - 1, height-1, width - 1, height - 1);
            }

            //inner white border
            g.setColor(getBorderInnerColor());
            //top
            g.drawLine(1, 1, width, 1);
            if( ren.isSelected() )
                height++;
            //left
            g.drawLine(0, 1, 0, height - 2);
            //right
            //no line

            g.translate(-x, -y);
        }

        @Override
        public void paintInterior(Graphics g, Component c) {
            WinVistaEditorTabCellRenderer ren = (WinVistaEditorTabCellRenderer) c;
            
            Polygon poly = getInteriorPolygon(ren);
            ren.paintTabGradient( g, poly );
        }

        @Override
        public boolean supportsCloseButton(JComponent renderer) {
            return false;
        }

        @Override
        public void getCloseButtonRectangle(JComponent jc,
                                            final Rectangle rect,
                                            Rectangle bounds) {
            rect.setBounds(-20, -20, 0, 0);
        }
    }
}
