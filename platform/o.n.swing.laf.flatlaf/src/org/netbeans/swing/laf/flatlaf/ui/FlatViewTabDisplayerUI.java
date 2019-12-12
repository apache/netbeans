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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.laf.flatlaf.HiDPIUtils;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.AbstractViewTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport;
import org.netbeans.swing.tabcontrol.plaf.TabLayoutModel;
import org.openide.awt.HtmlRenderer;

/**
 * User interface of view type tabs designed to be consistent with FlatLaf
 * look and feel.
 */
public class FlatViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    // Do not change or scale ICON_X_PAD because super class has a copy of this field.
    // Scaling ICON_X_PAD would truncate tab title.
    private static final int ICON_X_PAD = 4;

    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color
            background,             // background of tabs and tabs area if view group is inactive
            activeBackground,       // background of tabs and tabs area if view group is active;  optional; defaults to foreground
            selectedBackground,     // background of tab if tab is selected in active view group; optional; defaults to activeBackground
            hoverBackground,        // background of tab if mouse is over tab
            attentionBackground,    // background of tab if tab is in attension mode

            foreground,             // text color if view group is inactive;               optional; defaults to TabbedPane.foreground
            activeForeground,       // text color if view group is active;                 optional; defaults to foreground
            selectedForeground,     // text color if tab is selected in active view group; optional; defaults to activeForeground
            hoverForeground,        // text color if mouse is over tab;                    optional; defaults to foreground
            attentionForeground,    // text color if tab is in attension mode;             optional; defaults to foreground

            underlineColor,         // underline color of selected active tabs
            inactiveUnderlineColor, // underline color of selected inactive tabs
            contentBorderColor;     // bottom border color

    private static Insets tabInsets;
    private static int underlineHeight;     // height of "underline" painted at bottom of tab to indicate selection
    private static boolean underlineAtTop;  // paint "underline" at top of tab

    private Font font;

    public FlatViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new FlatViewTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        initColors();
        getLayoutModel().setPadding(new Dimension(tabInsets.left + tabInsets.right, 0));
    }

    @Override
    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm.getHeight() + tabInsets.top + tabInsets.bottom;
        return new Dimension(100, height);
    }

    @Override
    protected void paintTabContent(Graphics g, int index, String text, int x, int y, int width, int height) {
        int txtLeftPad = tabInsets.left;
        int txtRightPad = tabInsets.right;

        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            // layout buttons
            Component buttons = getControlButtons();
            if (null != buttons) {
                Dimension buttonsSize = buttons.getPreferredSize();
                if (width < buttonsSize.width + ICON_X_PAD) {
                    buttons.setVisible(false);
                } else {
                    buttons.setVisible(true);
                    txtWidth = width - (buttonsSize.width + ICON_X_PAD + txtLeftPad + txtRightPad);
                    buttons.setLocation(x + width - buttonsSize.width - ICON_X_PAD, y + (height - buttonsSize.height) / 2);
                }
            }
        } else {
            txtWidth = width - (txtLeftPad + txtRightPad);
        }

        // paint busy icon
        if (isTabBusy(index)) {
            Icon busyIcon = BusyTabsSupport.getDefault().getBusyIcon(isSelected(index));
            txtWidth -= busyIcon.getIconWidth() - UIScale.scale(3) - txtLeftPad;
            busyIcon.paintIcon(displayer, g, x + txtLeftPad, y + (height - busyIcon.getIconHeight()) / 2);
            x += busyIcon.getIconWidth() + UIScale.scale(3);
        }

        // text color
        Color c = colorForState(index, foreground, activeForeground, selectedForeground,
                hoverForeground, attentionForeground);

        // paint text
        int txtX = x + tabInsets.left;
        int txtY = y + tabInsets.top + fm.getAscent();
        int availH = height - tabInsets.top - tabInsets.bottom;
        if (availH > fm.getHeight()) {
            txtY += (availH - fm.getHeight()) / 2;
        }
        HtmlRenderer.renderString(text, g, txtX, txtY, txtWidth, height,
                getTxtFont(), c, HtmlRenderer.STYLE_TRUNCATE, true);
    }

    @Override
    protected void paintTabBorder(Graphics g, int index, int x, int y, int width, int height) {
        // not using borders
    }

    @Override
    protected void paintTabBackground(Graphics g, int index, int x, int y, int width, int height) {
        // Paint the whole tab background at scale 1x (on HiDPI screens).
        // Necessary so that it aligns nicely at bottom left and right edges
        // with the content border, which is also painted at scale 1x.
        HiDPIUtils.paintAtScale1x(g, x, y, width, height,
                (g1x, width1x, height1x, scale) -> {
                    paintTabBackgroundAtScale1x(g1x, index, width1x, height1x, scale);
                });
    }

    private void paintTabBackgroundAtScale1x(Graphics2D g, int index, int width, int height, double scale) {
        // paint background
        g.setColor(colorForState(index, background, activeBackground, selectedBackground,
                hoverBackground, attentionBackground));
        g.fillRect(0, 0, width, height);

        if (isSelected(index) && underlineHeight > 0) {
            // paint underline if tab is selected
            int underlineHeight = (int) Math.round(this.underlineHeight * scale);
            g.setColor(isActive() ? underlineColor : inactiveUnderlineColor);
            if (underlineAtTop) {
                g.fillRect(0, 0, width, underlineHeight);
            } else {
                g.fillRect(0, height - underlineHeight, width, underlineHeight);
            }
        } else {
            // paint bottom border
            int contentBorderWidth = HiDPIUtils.deviceBorderWidth(scale, 1);
            g.setColor(contentBorderColor);
            g.fillRect(0, height - contentBorderWidth, width, contentBorderWidth);
        }
    }

    @Override
    protected void paintDisplayerBackground(Graphics g, JComponent c) {
        // Fill the whole displayer background to avoid occasional 1px gaps
        // between tabs on HiDPI screens at 125%, 150% or 175%.
        paintTabBackground(g, -1, 0, 0, c.getWidth(), c.getHeight());

        super.paintDisplayerBackground(g, c);
    }

    private Color colorForState(int index, Color normal, Color active, Color selected, Color hover, Color attention) {
        return isAttention(index) ? attention
                : isMouseOver(index) ? hover
                : isActive() ? (isSelected(index) ? selected : active)
                : normal;
    }

    @Override
    protected Font getTxtFont() {
        if (font == null) {
            font = UIManager.getFont("ViewTab.font"); // NOI18N
            if (font == null) {
                font = UIManager.getFont("Label.font"); // NOI18N
            }
        }
        return font;
    }

    /**
     * @return true if tab with given index has mouse cursor above, false otherwise.
     */
    boolean isMouseOver(int index) {
        if (index < 0) {
            return false;
        }
        return ((OwnController) getController()).getMouseIndex() == index;
    }

    /**
     * Initialization of colors
     */
    private static void initColors() {
        if (!colorsReady) {
            background = UIManager.getColor("ViewTab.background"); // NOI18N
            activeBackground = Utils.getUIColor("ViewTab.activeBackground", background); // NOI18N
            selectedBackground = Utils.getUIColor("ViewTab.selectedBackground", activeBackground); // NOI18N
            hoverBackground = UIManager.getColor("ViewTab.hoverBackground"); // NOI18N
            attentionBackground = UIManager.getColor("ViewTab.attentionBackground"); // NOI18N

            foreground = Utils.getUIColor("ViewTab.foreground", "TabbedPane.foreground"); // NOI18N
            activeForeground = Utils.getUIColor("ViewTab.activeForeground", foreground); // NOI18N
            selectedForeground = Utils.getUIColor("ViewTab.selectedForeground", activeForeground); // NOI18N
            hoverForeground = Utils.getUIColor("ViewTab.hoverForeground", foreground); // NOI18N
            attentionForeground = Utils.getUIColor("ViewTab.attentionForeground", foreground); // NOI18N

            underlineColor = UIManager.getColor("ViewTab.underlineColor"); // NOI18N
            inactiveUnderlineColor = UIManager.getColor("ViewTab.inactiveUnderlineColor"); // NOI18N
            contentBorderColor = UIManager.getColor("TabbedContainer.view.contentBorderColor"); // NOI18N

            tabInsets = UIManager.getInsets("ViewTab.tabInsets"); // NOI18N
            underlineHeight = UIManager.getInt("ViewTab.underlineHeight"); // NOI18N
            underlineAtTop = UIManager.getBoolean("ViewTab.underlineAtTop"); // NOI18N

            // scale on Java 8 and Linux
            tabInsets = UIScale.scale(tabInsets);
            underlineHeight = UIScale.scale(underlineHeight);

            colorsReady = true;
        }
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon ret = FlatTabControlIcon.get(buttonId, buttonState);
        return ret != null ? ret : super.getButtonIcon(buttonId, buttonState);
    }

    @Override
    public void postTabAction(TabActionEvent e) {
        super.postTabAction(e);
        if (TabDisplayer.COMMAND_MAXIMIZE.equals(e.getActionCommand())) {
            ((OwnController) getController()).updateHighlight(-1);
        }
    }

    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {

        /**
         * holds index of tab in which mouse pointer was lastly located. -1
         * means mouse pointer is out of component's area
         */
        // TBD - should be part of model, not controller
        private int lastIndex = -1;

        /**
         * @return Index of tab in which mouse pointer is currently located.
         */
        public int getMouseIndex() {
            return lastIndex;
        }

        /**
         * Triggers visual tab header change when mouse enters/leaves tab in
         * advance to superclass functionality.
         */
        @Override
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point pos = e.getPoint();
            updateHighlight(getLayoutModel().indexOfPoint(pos.x, pos.y));
        }

        /**
         * Resets tab header in advance to superclass functionality
         */
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            if (!inControlButtonsRect(e.getPoint())) {
                updateHighlight(-1);
            }
        }

        /**
         * Invokes repaint of dirty region if needed
         */
        private void updateHighlight(int curIndex) {
            if (curIndex == lastIndex) {
                return;
            }
            // compute region which needs repaint
            TabLayoutModel tlm = getLayoutModel();
            int x, y, w, h;
            Rectangle repaintRect = null;
            if (curIndex != -1) {
                x = tlm.getX(curIndex) - 1;
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex) + 2;
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex) - 1;
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex) + 2;
                h = tlm.getH(lastIndex);
                if (repaintRect != null) {
                    repaintRect = repaintRect.union(new Rectangle(x, y, w, h));
                } else {
                    repaintRect = new Rectangle(x, y, w, h);
                }
            }
            // trigger repaint if needed, update index
            if (repaintRect != null) {
                getDisplayer().repaint(repaintRect);
            }
            lastIndex = curIndex;
        }
    } // end of OwnController
}
