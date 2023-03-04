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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;
import org.netbeans.lib.profiler.charts.swing.Utils;

/**
 *
 * @author Jiri Sedlacek
 */
public class HeaderButton extends HeaderPanel {

    private static final HeaderButtonUI UI = new HeaderButtonUI();

    private final JButton button;


    public HeaderButton(String text, Icon icon) {
        JPanel panel = super.getClientContainer();
        panel.setLayout(new BorderLayout());
        button = new JButton(text, icon) {
            protected void processMouseEvent(MouseEvent e) {
                super.processMouseEvent(e);
                if (!isEnabled()) return;
                HeaderButton.this.processMouseEvent(e);
            }
            protected void fireActionPerformed(ActionEvent e) {
                performAction(e);
            }
        };
        panel.add(button, BorderLayout.CENTER);

        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setUI(UI);
    }

    public void setToolTipText(String text) {
        button.setToolTipText(text);
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void reset() {
        processMouseEvent(new MouseEvent(this, MouseEvent.MOUSE_EXITED,
                          System.currentTimeMillis(), 0, -1, -1, 0, false));
    }

    protected boolean processMouseEvents() { return true; }

    protected void performAction(ActionEvent e) {}
    
    public void setUI(ButtonUI ui) { if (ui == UI) super.setUI(ui); }


    private static class HeaderButtonUI extends BasicButtonUI {

        private static final Color FOCUS_COLOR = Color.BLACK;
        private static final Stroke FOCUS_STROKE =
                new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL,
                                0, new float[] {0, 2}, 0);
        private static final Color PRESSED_FOREGROUND =
                Utils.checkedColor(new Color(100, 100, 100, 70));

        protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect,
                                  Rectangle textRect, Rectangle iconRect) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setStroke(FOCUS_STROKE);
            g2.setColor(FOCUS_COLOR);
            g2.drawRect(2, 2, b.getWidth() - 5, b.getHeight() - 5);
        }

        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            g.setColor(PRESSED_FOREGROUND);
            g.fillRect(0, 0, b.getWidth(), b.getHeight());
        }

    }

}
