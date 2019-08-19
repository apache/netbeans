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
package org.netbeans.conffile.ui.comp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Tim Boudreau
 */
public final class AAFileChooser extends JFileChooser {

    private Font targetFont;

    public AAFileChooser(Font font) {
        this.targetFont = font;
        setUI(new GradientBackgroundFileChooserUI(this));
    }

    @Override
    public void paint(Graphics g) {
        UIUtils.withTextAntialiasing(g, super::paint);
    }

    @Override
    public void addNotify() {
        drillThrough(this, targetFont);
        super.addNotify();
    }

    static void drillThrough(Container comp, Font font) {
        // What it takes to enable antialiasing throughout a JFileChooser
        comp.setFont(font);
        for (Component c : comp.getComponents()) {
            if (c instanceof Container) {
                drillThrough((Container) c, font);
            }
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.getText() == null || b.getText().isEmpty()) {
                    b.setUI(new AAButtonUI());
                } else {
                    b.setUI(new AltButtonUI());
                }
            } else if (c instanceof JLabel) {
                ((JLabel) c).setUI(new AALabelUI());
            } else if (c instanceof JList) {
                ListCellRenderer orig = ((JList) c).getCellRenderer();
                if (orig instanceof JLabel) {
                    ((JLabel) orig).setUI(new AALabelUI());
                }
            } else if (c instanceof JTextComponent || c instanceof JComboBox) {
                c.setBackground(Color.WHITE);
                if (c instanceof JComboBox) {
                    ListCellRenderer orig = ((JComboBox) c).getRenderer();
                    if (orig instanceof JLabel) {
                        ((JLabel) orig).setUI(new AALabelUI());
                    }
                }
            } else if (c instanceof JComponent) {
                ((JComponent) c).setOpaque(false);
            }
        }
    }
}
