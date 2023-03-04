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

package org.netbeans.modules.refactoring.java.ui.elements;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class JCheckBoxIcon implements Icon {
    private final JPanel delegate;
    private final Dimension dimension;
    private final boolean selected;

    public JCheckBoxIcon(boolean selected, Dimension dimension) {
        this.selected = selected;
        this.dimension = dimension;
        BorderLayout layout = new BorderLayout();
        this.delegate = new JPanel(layout, false);
        this.delegate.setBorder(null);
        this.delegate.setOpaque(false);
        JCheckBox jCheckBox = new JCheckBox(null, null, selected);
        jCheckBox.setMargin(new Insets(0, 0, 0, 0));
        this.delegate.add(jCheckBox, BorderLayout.CENTER);
        this.delegate.setSize(jCheckBox.getPreferredSize());
        this.delegate.addNotify();
        this.delegate.validate();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (delegate.getWidth() != 0 && delegate.getHeight() != 0) {
            BufferedImage img = new BufferedImage(delegate.getWidth(), delegate.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            delegate.paintAll(graphics);
            g.drawImage(img, x, y, dimension.width, dimension.height, null);
        }
    }

    @Override
    public int getIconWidth() {
        return dimension.width;
    }

    @Override
    public int getIconHeight() {
        return dimension.height;
    }
    
}
