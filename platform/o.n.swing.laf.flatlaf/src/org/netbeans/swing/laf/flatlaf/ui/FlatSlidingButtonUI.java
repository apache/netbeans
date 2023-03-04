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
package org.netbeans.swing.laf.flatlaf.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.SlidingButtonUI;

/**
 * Sliding button UI for FlatLaf look and feel.
 */
public class FlatSlidingButtonUI extends SlidingButtonUI {

    private boolean defaults_initialized = false;
    private Color hoverBackground;
    private Color selectedBackground;
    private Color attentionBackground;
    private Color attentionForeground;

    private static final FlatSlidingButtonUI INSTANCE = new FlatSlidingButtonUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    @Override
    protected void installDefaults(AbstractButton b) {
        super.installDefaults(b);

        if(!defaults_initialized) {
            hoverBackground = UIManager.getColor("SlidingButton.hoverBackground");
            selectedBackground = UIManager.getColor("SlidingButton.selectedBackground");
            attentionBackground = UIManager.getColor("SlidingButton.attentionBackground");
            attentionForeground = UIManager.getColor("SlidingButton.attentionForeground");
            defaults_initialized = true;
        }

        LookAndFeel.installProperty(b, "opaque", false);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (((SlidingButton) c).isBlinkState()) {
            g.setColor(attentionBackground);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }

        super.paint(g, c);
    }

    @Override
    protected void paintButtonPressed(Graphics g, AbstractButton b) {
        if (((SlidingButton) b).isBlinkState()) {
            // background already painted
            return;
        }

        ButtonModel bm = b.getModel();
        g.setColor(bm.isPressed() || bm.isArmed() || bm.isSelected() ? selectedBackground : hoverBackground);
        g.fillRect(0, 0, b.getWidth(), b.getHeight());
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        if (((SlidingButton) b).isBlinkState()) {
            Color oldForeground = b.getForeground();
            b.setForeground(attentionForeground);
            super.paintText(g, b, textRect, text);
            b.setForeground(oldForeground);
        } else {
            super.paintText(g, b, textRect, text);
        }
    }
}
