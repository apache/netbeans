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
package org.netbeans.swing.laf.flatlaf.ui;

import com.formdev.flatlaf.util.UIScale;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.laf.flatlaf.HiDPIUtils;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.plaf.AbstractTabCellRenderer;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;
import org.netbeans.swing.tabcontrol.plaf.TabPainter;

/**
 * FlatLaf implementation of tab renderer
 */
public class FlatEditorTabCellRenderer extends AbstractTabCellRenderer {

    private static final int CLOSE_ICON_RIGHT_PAD = 2;

    private static final Color background = UIManager.getColor("EditorTab.background"); // NOI18N
    private static final Color activeBackground = Utils.getUIColor("EditorTab.activeBackground", background); // NOI18N
    private static final Color selectedBackground = Utils.getUIColor("EditorTab.selectedBackground", activeBackground); // NOI18N
    private static final Color hoverBackground = UIManager.getColor("EditorTab.hoverBackground"); // NOI18N
    private static final Color attentionBackground = UIManager.getColor("EditorTab.attentionBackground"); // NOI18N

    private static final Color foreground = Utils.getUIColor( "EditorTab.foreground", "TabbedPane.foreground" ); // NOI18N
    private static final Color activeForeground = Utils.getUIColor( "EditorTab.activeForeground", foreground ); // NOI18N
    private static final Color selectedForeground = Utils.getUIColor( "EditorTab.selectedForeground", activeForeground ); // NOI18N
    private static final Color hoverForeground = Utils.getUIColor( "EditorTab.hoverForeground", foreground ); // NOI18N
    private static final Color attentionForeground = Utils.getUIColor( "EditorTab.attentionForeground", foreground ); // NOI18N

    private static final Color underlineColor = UIManager.getColor("EditorTab.underlineColor"); // NOI18N
    private static final Color inactiveUnderlineColor = UIManager.getColor("EditorTab.inactiveUnderlineColor"); // NOI18N
    private static final Color contentBorderColor = UIManager.getColor("TabbedContainer.editor.contentBorderColor"); // NOI18N

    private static final Insets tabInsets = UIScale.scale(UIManager.getInsets("EditorTab.tabInsets")); // NOI18N
    private static final int underlineHeight = UIScale.scale(UIManager.getInt("EditorTab.underlineHeight")); // NOI18N
    private static final boolean underlineAtTop = UIManager.getBoolean("EditorTab.underlineAtTop"); // NOI18N

    private static final FlatTabPainter painter = new FlatTabPainter();

    public FlatEditorTabCellRenderer() {
        super(painter, new Dimension(tabInsets.left + tabInsets.right, tabInsets.top + tabInsets.bottom));
    }

    @Override
    public Dimension getPadding() {
        Dimension d = super.getPadding();
        if (isShowCloseButton() && !Boolean.getBoolean("nb.tabs.suppressCloseButton")) { // NOI18N
            d.width += findCloseIcon().getIconWidth() + UIScale.scale(CLOSE_ICON_RIGHT_PAD);
        }
        return d;
    }

    @Override
    protected int getCaptionYAdjustment() {
        // Workaround for a issue in AbstractTabCellRenderer.paintIconAndText(Graphics),
        // which uses font height (which includes font descent) to calculate Y-coordinate
        // when available height is equal to font height (availH <= txtH),
        // but HtmlRenderer.renderString() expects Y-coordinate at baseline.
        // So the text is painted vertically out of center.
        //
        // This seems to be no issue with other LAFs because they seem to use
        // TabPainter insets differently and the available height is larger than
        // the font height (availH > txtH), in which case 3 pixels are removed from
        // the Y-coordinate to avoid that the text is painted vertically out of center.

        FontMetrics fm = getFontMetrics(getFont());
        int txtH = fm.getHeight();
        Insets ins = getInsets();
        int availH = getHeight() - (ins.top + ins.bottom);
        return (availH <= txtH) ? -fm.getDescent() : 0;
    }

    @Override
    protected int stateChanged(int oldState, int newState) {
        int result = super.stateChanged(oldState, newState);

        // set text color
        setForeground(colorForState(foreground, activeForeground, selectedForeground,
                hoverForeground, attentionForeground));

        return result;
    }

    private Color colorForState(Color normal, Color active, Color selected, Color hover, Color attention) {
        return isAttention() ? attention
                : isArmed() ? hover
                : isSelected() ? selected
                : isActive() ? active
                : normal;
    }

    private Icon findCloseIcon() {
        return FlatTabControlIcon.get(TabControlButton.ID_CLOSE_BUTTON,
                inCloseButton()
                        ? (isPressed() ? TabControlButton.STATE_PRESSED : TabControlButton.STATE_ROLLOVER)
                        : TabControlButton.STATE_DEFAULT);
    }

    private static class FlatTabPainter implements TabPainter {

        @Override
        public Insets getBorderInsets(Component c) {
            return tabInsets;
        }

        @Override
        public void getCloseButtonRectangle(JComponent jc, Rectangle rect, Rectangle bounds) {
            FlatEditorTabCellRenderer ren = (FlatEditorTabCellRenderer) jc;

            if (!ren.isShowCloseButton()) {
                rect.x = -100;
                rect.y = -100;
                rect.width = 0;
                rect.height = 0;
                return;
            }
            Icon icon = ren.findCloseIcon();
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            rect.x = bounds.x + bounds.width - iconWidth - UIScale.scale(CLOSE_ICON_RIGHT_PAD);
            rect.y = bounds.y + (Math.max(0, bounds.height / 2 - iconHeight / 2));
            rect.width = iconWidth;
            rect.height = iconHeight;
        }

        @Override
        public Polygon getInteriorPolygon(Component c) {
            int x = 0;
            int y = 0;
            int width = c.getWidth();
            int height = c.getHeight();

            //just a plain rectangle
            Polygon p = new Polygon();
            p.addPoint(x, y);
            p.addPoint(x + width, y);
            p.addPoint(x + width, y + height);
            p.addPoint(x, y + height);
            return p;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            // not using borders
        }

        @Override
        public void paintInterior(Graphics g, Component c) {
            // Paint the whole tab background at scale 1x (on HiDPI screens).
            // Necessary so that it aligns nicely at bottom left and right edges
            // with the content border, which is also painted at scale 1x.
            HiDPIUtils.paintAtScale1x(g, 0, 0, c.getWidth(), c.getHeight(),
                (gd, width, height, scale) -> {
                    paintInteriorAtScale1x(gd, c, width, height, scale);
                });

            // paint close button
            FlatEditorTabCellRenderer ren = (FlatEditorTabCellRenderer) c;
            if (!ren.isClipLeft() && !ren.isClipRight())
                paintCloseButton(g, ren);
        }

        private void paintInteriorAtScale1x(Graphics2D g, Component c, int width, int height, double scale) {
            FlatEditorTabCellRenderer ren = (FlatEditorTabCellRenderer) c;
            boolean selected = ren.isSelected();

            // paint background
            g.setColor(ren.colorForState(background, activeBackground, selectedBackground,
                    hoverBackground, attentionBackground));
            g.fillRect(0, 0, width, height);

            if (selected && underlineHeight > 0) {
                // paint underline if tab is selected
                int underlineHeight = (int) Math.round(FlatEditorTabCellRenderer.underlineHeight * scale);
                g.setColor(ren.isActive() ? underlineColor : inactiveUnderlineColor);
                if (underlineAtTop)
                    g.fillRect(0, 0, width, underlineHeight);
                else
                    g.fillRect(0, height - underlineHeight, width, underlineHeight);
            } else {
                // paint bottom border
                int contentBorderWidth = HiDPIUtils.deviceBorderWidth(scale, 1);
                g.setColor(contentBorderColor);
                g.fillRect(0, height - contentBorderWidth, width, contentBorderWidth);
            }
        }

        private void paintCloseButton(Graphics g, FlatEditorTabCellRenderer ren) {
            //Get the close button bounds, more or less
            Rectangle r = new Rectangle();
            getCloseButtonRectangle(ren, r, new Rectangle(0, 0,
                                                          ren.getWidth(),
                                                          ren.getHeight()));

            if (!g.hitClip(r.x, r.y, r.width, r.height)) {
                return;
            }

            //paint close button
            Icon icon = ren.findCloseIcon();
            icon.paintIcon(ren, g, r.x, r.y);
        }

        @Override
        public boolean supportsCloseButton(JComponent renderer) {
            return renderer instanceof TabDisplayer ?
                ((TabDisplayer) renderer).isShowCloseButton() : true;
        }
    }
}
