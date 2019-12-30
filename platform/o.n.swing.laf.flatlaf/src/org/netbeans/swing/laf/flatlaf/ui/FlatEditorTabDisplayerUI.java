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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.laf.flatlaf.HiDPIUtils;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.plaf.BasicScrollingTabDisplayerUI;
import org.netbeans.swing.tabcontrol.plaf.TabCellRenderer;

/**
 * Tab displayer UI for FlatLaf look and feel
 */
public class FlatEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {

    private static final int ICON_X_PAD = 4;

    private final Color background = UIManager.getColor("EditorTab.background"); // NOI18N
    private final Color activeBackground = Utils.getUIColor("EditorTab.activeBackground", background); // NOI18N
    private final Color contentBorderColor = UIManager.getColor("TabbedContainer.editor.contentBorderColor"); // NOI18N
    private final Insets tabInsets = UIScale.scale(UIManager.getInsets("EditorTab.tabInsets")); // NOI18N

    public FlatEditorTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new FlatEditorTabDisplayerUI((TabDisplayer) c);
    }

    @Override
    protected TabCellRenderer createDefaultRenderer() {
        return new FlatEditorTabCellRenderer();
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        int prefHeight;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom
                    + tabInsets.top + tabInsets.bottom;
        } else
            prefHeight = UIScale.scale(28);
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    @Override
    public Insets getTabAreaInsets() {
        return new Insets(0, 0, 0, getControlButtons().getPreferredSize().width + ICON_X_PAD);
    }

    protected Rectangle getControlButtonsRectangle(Container parent) {
        Component c = getControlButtons();
        return new Rectangle(parent.getWidth() - c.getWidth() - ICON_X_PAD,
                (parent.getHeight() - c.getHeight()) / 2, c.getWidth(), c.getHeight());
    }

    @Override
    public void paintBackground(Graphics g) {
        int width = displayer.getWidth();
        int height = displayer.getHeight();

        // Paint the whole tab background at scale 1x (on HiDPI screens).
        // Necessary so that it aligns nicely at bottom left and right edges
        // with the content border, which is also painted at scale 1x.
        HiDPIUtils.paintAtScale1x(g, 0, 0, width, height, this::paintBackgroundAtScale1x);
    }

    private void paintBackgroundAtScale1x(Graphics2D g, int width, int height, double scale) {
        // fill background
        g.setColor (displayer.isActive() ? activeBackground : background);
        g.fillRect (0, 0, width, height);

        // paint bottom border
        int contentBorderWidth = HiDPIUtils.deviceBorderWidth(scale, 1);
        g.setColor(contentBorderColor);
        g.fillRect(0, height - contentBorderWidth, width, contentBorderWidth);
    }

    @Override
    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon ret = FlatTabControlIcon.get(buttonId, buttonState);
        return ret != null ? ret : super.getButtonIcon(buttonId, buttonState);
    }
}
