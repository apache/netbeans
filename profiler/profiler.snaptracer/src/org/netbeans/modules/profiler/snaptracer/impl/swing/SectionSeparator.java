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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Separator-like component to be used instead of TitledBorder to keep the UI
 * lightweight. Use UISupport.createSectionSeparator() method instead of instantiating
 * this class directly if creating sections for the Options panel.
 *
 * @author Jiri Sedlacek
 */
public final class SectionSeparator extends JPanel {

    /**
     * Creates new instance of SectionSeparator. Uses bold font by default.
     *
     * @param text separator text
     */
    public SectionSeparator(String text) {
        this(text, null);
    }

    /**
     * Creates new instance of SectionSeparator. Uses the provided font or default
     * font if no font is provided.
     *
     * @param text separator text
     * @param font font for the caption text or null for default font
     */
    public SectionSeparator(String text, Font font) {
        if (text == null) throw new IllegalArgumentException("Text cannot be null"); // NOI18N
        initComponents(text, font);
    }

    public void setForeground(Color foreground) {
        if (label == null) super.setForeground(foreground);
        else label.setForeground(foreground);
    }

    public Color getForeground() {
        if (label == null) return super.getForeground();
        else return label.getForeground();
    }

    public void setFont(Font font) {
        if (label == null) super.setFont(font);
        else label.setFont(font);
    }

    public Font getFont() {
        if (label == null) return super.getFont();
        else return label.getFont();
    }

    private void initComponents(String text, Font font) {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new GridBagLayout());
        setOpaque(false);

        label = new JLabel(text);
        label.setForeground(getForeground());
        if (font != null) label.setFont(font);
        else label.setFont(label.getFont().deriveFont(Font.BOLD));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.weighty = 1d;
        add(label, c1);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.weightx = 1d;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.insets = new Insets(0, 4, 0, 0);
        add(new Separator(), c2);
    }

    private JLabel label;

}
