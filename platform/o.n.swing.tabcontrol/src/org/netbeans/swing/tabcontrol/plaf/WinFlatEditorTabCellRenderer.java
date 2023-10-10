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
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.plaf.WinFlatUtils.FlatTabControlIcon;
import org.netbeans.swing.tabcontrol.plaf.WinFlatUtils.HiDPIUtils;
import org.netbeans.swing.tabcontrol.plaf.WinFlatUtils.UIScale;
import org.netbeans.swing.tabcontrol.plaf.WinFlatUtils.Utils;

/**
 * "Fork" of {@code org.netbeans.swing.laf.flatlaf.ui.FlatEditorTabCellRenderer}, for use with the
 * Windows LAF.
 */
class WinFlatEditorTabCellRenderer extends AbstractTabCellRenderer {
    private static final Color background = UIManager.getColor("EditorTab.background"); // NOI18N
    private static final Color activeBackground = Utils.getUIColor("EditorTab.activeBackground", background); // NOI18N
    private static final Color selectedBackground = Utils.getUIColor("EditorTab.selectedBackground", activeBackground); // NOI18N
    private static final Color selectedBackgroundBottomGradient = Utils.getUIColor("EditorTab.selectedBackgroundBottomGradient", selectedBackground); // NOI18N
    private static final Color hoverBackground = UIManager.getColor("EditorTab.hoverBackground"); // NOI18N
    private static final Color unselectedHoverBackground = Utils.getUIColor("EditorTab.unselectedHoverBackground", hoverBackground); // NOI18N
    private static final Color attentionBackground = UIManager.getColor("EditorTab.attentionBackground"); // NOI18N

    private static final Color foreground = Utils.getUIColor( "EditorTab.foreground", "TabbedPane.foreground" ); // NOI18N
    private static final Color activeForeground = Utils.getUIColor( "EditorTab.activeForeground", foreground ); // NOI18N
    private static final Color selectedForeground = Utils.getUIColor( "EditorTab.selectedForeground", activeForeground ); // NOI18N
    private static final Color hoverForeground = Utils.getUIColor( "EditorTab.hoverForeground", foreground ); // NOI18N
    private static final Color attentionForeground = Utils.getUIColor( "EditorTab.attentionForeground", foreground ); // NOI18N

    private static final Color underlineColor = UIManager.getColor("EditorTab.underlineColor"); // NOI18N
    private static final Color inactiveUnderlineColor = UIManager.getColor("EditorTab.inactiveUnderlineColor"); // NOI18N
    private static final Color tabSeparatorColor = UIManager.getColor("EditorTab.tabSeparatorColor"); // NOI18N
    private static final Color contentBorderColor = UIManager.getColor("TabbedContainer.editor.contentBorderColor"); // NOI18N

    private static final Insets tabInsets = UIScale.scale(UIManager.getInsets("EditorTab.tabInsets")); // NOI18N
    private static final int underlineHeight = UIScale.scale(UIManager.getInt("EditorTab.underlineHeight")); // NOI18N
    private static final boolean underlineAtTop = UIManager.getBoolean("EditorTab.underlineAtTop"); // NOI18N
    private static boolean showTabSeparators = UIManager.getBoolean("EditorTab.showTabSeparators"); // NOI18N

    /**
     * Margin on the right of the close button. Note that {@code tabInsets.right} denotes the space
     * between the caption text and the "close" icon. Here, we set the right margin to the same
     * value as the left margin (before the tab's caption).
     */
    private static final int CLOSE_ICON_RIGHT_PAD = tabInsets.left;

    private static final boolean showSelectedTabBorder = Utils.getUIBoolean("EditorTab.showSelectedTabBorder", false); // NOI18N
    private static final boolean unscaledBorders = Utils.getUIBoolean("EditorTab.unscaledBorders", false); // NOI18N

    private static final FlatTabPainter leftClipPainter = new FlatTabPainter(true, false);
    private static final FlatTabPainter noClipPainter = new FlatTabPainter(false, false);
    private static final FlatTabPainter rightClipPainter = new FlatTabPainter(false, true);

    boolean firstTab;
    boolean lastTab;
    boolean nextTabSelected;

    public WinFlatEditorTabCellRenderer() {
        super(leftClipPainter, noClipPainter, rightClipPainter,
                new Dimension(tabInsets.left + tabInsets.right, tabInsets.top + tabInsets.bottom));
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
    protected int getCaptionYPosition(Graphics g) {
        Font font = getFont();
        FontRenderContext frc = (g instanceof Graphics2D)
                ? ((Graphics2D) g).getFontRenderContext()
                : g.getFontMetrics(font).getFontRenderContext();
        /* Don't rely on FontMetrics.getAscent() to get the ascent; it can return values much bigger
        than the actual, visual size of the letters. Use the actual height of a flat-topped
        upper-case letter instead. */
        double txtVisualAscent = font.createGlyphVector(frc, "H")
                .getVisualBounds().getHeight();
        Insets ins = getInsets();
        int availH = getHeight() - (ins.top + ins.bottom);
        final int effectiveIconYAdjustment = 1 + getIconYAdjustment();

        // Center the visual ascent portion of the text vertically with respect to the icon.
        return ins.top + (int) Math.round((availH + txtVisualAscent) / 2)
                + effectiveIconYAdjustment;
    }

    @Override
    protected int stateChanged(int oldState, int newState) {
        int result = super.stateChanged(oldState, newState);

        // set text color
        setForeground(colorForState(foreground, activeForeground, selectedForeground,
                hoverForeground, hoverForeground, attentionForeground));

        return result;
    }

    private Color colorForState(Color normal, Color active, Color selected,
            Color selectedHover, Color unselectedHover, Color attention)
    {
        return isAttention() ? attention
                : isArmed() ? (isSelected() ? selectedHover : unselectedHover)
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
        final boolean leftClip;
        final boolean rightClip;

        public FlatTabPainter(boolean leftClip, boolean rightClip) {
            this.leftClip = leftClip;
            this.rightClip = rightClip;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return tabInsets;
        }

        @Override
        public void getCloseButtonRectangle(JComponent jc, Rectangle rect, Rectangle bounds) {
            WinFlatEditorTabCellRenderer ren = (WinFlatEditorTabCellRenderer) jc;

            if (!ren.isShowCloseButton() || leftClip || rightClip) {
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
            // Ad hoc adjustment for the Windows LAF.
            int yAdjustment = 2;
            rect.y = bounds.y + Math.max(0, (bounds.height - iconHeight) / 2) - 1 + yAdjustment;
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
            WinFlatEditorTabCellRenderer ren = (WinFlatEditorTabCellRenderer) c;
            if (!ren.isClipLeft() && !ren.isClipRight())
                paintCloseButton(g, ren);
        }

        private static void fillGradientRect(Graphics2D g, int x, int y, int width, int height,
                Color color, Color bottomGradient, int gradientOffset)
        {
            if (bottomGradient.equals(color)) {
                g.setColor(color);
            } else {
                g.setPaint(new GradientPaint(
                        0, y          + gradientOffset, color,
                        0, y + height - gradientOffset, bottomGradient, false));
            }
            g.fillRect(x, y, width, height);
        }

        private void paintInteriorAtScale1x(Graphics2D g, Component c, int width, int height, double scale) {
            WinFlatEditorTabCellRenderer ren = (WinFlatEditorTabCellRenderer) c;
            boolean selected = ren.isSelected();

            // get background color
            Color bg = ren.colorForState(
                    background, activeBackground, selectedBackground,
                    hoverBackground, unselectedHoverBackground, attentionBackground);

            boolean showSeparator = showTabSeparators && !ren.lastTab
                    && !selected && !ren.nextTabSelected && !rightClip;

            int contentBorderWidth = unscaledBorders ? 1 : HiDPIUtils.deviceBorderWidth(scale, 1);
            int tabSeparatorWidth = showSeparator ? contentBorderWidth : 0;
            int underlineHeight = (int) Math.round(WinFlatEditorTabCellRenderer.underlineHeight * scale);

            fillGradientRect(g, 0, 0, width - (bg != background ? tabSeparatorWidth : 0), height, bg,
                    selected && !selectedBackground.equals(selectedBackgroundBottomGradient)
                            ? selectedBackgroundBottomGradient : bg,
                    (underlineAtTop ? underlineHeight : 0));

            if (selected) {
                if (showSelectedTabBorder) {
                    g.setColor(contentBorderColor);
                    g.fillRect(0, 0, width - tabSeparatorWidth, contentBorderWidth); // Top
                    if (!leftClip) {
                        g.fillRect(0, 0, contentBorderWidth, height); // Left
                    }
                    if (!rightClip) {
                        g.fillRect(width - tabSeparatorWidth - contentBorderWidth, 0, contentBorderWidth, height); // Right
                    }
                }

                if (underlineHeight > 0) {
                    // paint underline if tab is selected
                    g.setColor(ren.isActive() ? underlineColor : inactiveUnderlineColor);
                    if (underlineAtTop) {
                        g.fillRect(0, 0, width - tabSeparatorWidth, underlineHeight);
                    } else {
                        g.fillRect(0, height - underlineHeight, width - tabSeparatorWidth, underlineHeight);
                    }
                }
            } else {
                // paint bottom border
                g.setColor(contentBorderColor);
                g.fillRect(0, height - contentBorderWidth, width, contentBorderWidth);
            }

            if (showSeparator) {
                int offset = (int) (4 * scale);
                g.setColor(tabSeparatorColor);
                g.fillRect(width - tabSeparatorWidth, offset, tabSeparatorWidth, height - (offset * 2) - 1);
            }
        }

        private void paintCloseButton(Graphics g, WinFlatEditorTabCellRenderer ren) {
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
