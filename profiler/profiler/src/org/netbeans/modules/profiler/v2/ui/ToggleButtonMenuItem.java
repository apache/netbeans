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

package org.netbeans.modules.profiler.v2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.StayOpenPopupMenu;

/**
 *
 * @author Jiri Sedlacek
 */
public class ToggleButtonMenuItem extends StayOpenPopupMenu.Item {
    
    private final JLabel label;
    private final Icon selectedIcon;
    private final Icon unselectedIcon;

    private boolean pressed;


    public ToggleButtonMenuItem(String text, Icon icon) {
        super(sizeText(text));
        setLayout(null);

        selectedIcon = createSelectedIcon(icon);
        unselectedIcon = createUnselectedIcon(icon);

        label = new JLabel(unselectedIcon, JLabel.LEADING);
        add(label, BorderLayout.WEST);
    }

    
    public void setPressed(boolean pressed) {
        if (this.pressed == pressed) return;

        this.pressed = pressed;
        label.setIcon(pressed ? selectedIcon : unselectedIcon);
        repaint();
    }

    public boolean isPressed() {
        return pressed;
    }
    

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.height = Math.max(dim.height, getComponent(0).getPreferredSize().height);
        return dim;
    }
    
    public void doLayout() {
        getComponent(0).setBounds(0, 0, getWidth(), getHeight());
    }


    private static Icon createSelectedIcon(Icon icon) {
        JComponent c = new JToggleButton() {
            {
                setSelected(true);
                if (UIUtils.isAquaLookAndFeel())
                    putClientProperty("JButton.buttonType", "textured"); // NOI18N
            }
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (UIUtils.isOracleLookAndFeel()) {
                    Color c = UIManager.getColor("List.selectionBackground"); // NOI18N
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 140));
                    g.fillRect(1, 1, getWidth() - 3, getHeight() - 2);
                }
            }
        };

        if (UIUtils.isWindowsLookAndFeel() || UIUtils.isMetalLookAndFeel() || UIUtils.isOracleLookAndFeel()) {
            JToolBar t = new JToolBar() {
                {
                    setLayout(null);
                    setOpaque(false);
                    setRollover(true);
                    setFloatable(false);
                    setBorderPainted(false);
                }
                public void setSize(int w, int h) {
                    super.setSize(w, h);
                    if (getComponentCount() > 0) getComponent(0).setBounds(0, 0, w, h);
                }
            };
            t.removeAll();
            t.add(c);
            c = t;
        }

        return createMenuIcon(icon, c);
    }
    
    private static Icon createUnselectedIcon(Icon icon) {
        return createMenuIcon(icon, null);
    }

    private static Icon createMenuIcon(Icon icon, Component decorator) {
        int h = menuIconSize();
        int w = UIUtils.isAquaLookAndFeel() ? h + 4 : h;

        BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        if (decorator != null) {
            decorator.setSize(w, h);
            decorator.paint(g);
        }

        icon.paintIcon(null, g, (w - icon.getIconWidth()) / 2, (h - icon.getIconHeight()) / 2);
        g.dispose();

        return new ImageIcon(i);
    }

    private static int menuIconSize() {
        if (UIUtils.isMetalLookAndFeel()) return 23;
        if (UIUtils.isAquaLookAndFeel()) return 26;
        if (UIUtils.isGTKLookAndFeel()) return 24;
        if (UIUtils.isNimbus()) return 25;
        if (UIUtils.isOracleLookAndFeel()) return 21;
        return 22;
    }

    private static String sizeText(String text) {
        if (UIUtils.isMetalLookAndFeel()) return "  " + text; // NOI18N
        if (UIUtils.isAquaLookAndFeel()) return "   " + text; // NOI18N
        if (UIUtils.isGTKLookAndFeel()) return " " + text; // NOI18N
        if (UIUtils.isWindowsClassicLookAndFeel()) return "  " + text; // NOI18N
        return text;
    }

}
